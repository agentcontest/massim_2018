package massim;

import massim.config.TeamConfig;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Handles agent accounts and network connections to all agents.
 * @author ta10
 */
class AgentManager {

    private Map<String, AgentProxy> agents = new HashMap<>();

    private long agentTimeout;
    private boolean disconnecting = false;
    int maximumPacketLength = 65536;

    /**
     * Creates a new agent manager responsible for sending and receiving messages.
     * @param teams a list of all teams to configure the manager for
     */
    AgentManager(List<TeamConfig> teams, long agentTimeout) {
        teams.forEach(team -> team.getAgents().forEach((name, pass) -> {
            agents.put(name, new AgentProxy(name, team.getName(), pass));
        }));
        this.agentTimeout = agentTimeout;
    }

    /**
     * Stops all related threads and closes all sockets involved.
     */
    void stop(){
        agents.values().parallelStream().forEach(AgentProxy::close);
    }

    /**
     * Sets a new socket for the given agent that was just authenticated (again or for the first time).
     * @param s the new socket opened for the agent
     * @param agentName the name of the agent
     */
    void setSocket(Socket s, String agentName){
        if (agents.containsKey(agentName)){
            agents.get(agentName).setSocket(s);
        }
    }

    /**
     * Checks if the given credentials are valid.
     * @param user name of the agent
     * @param inPass password of the agent
     * @return true iff the credentials are valid
     */
    boolean auth(String user, String inPass) {
        return agents.containsKey(user) && agents.get(user).password.equals(inPass);
    }

    /**
     * Sends initial percepts to the agents and stores them for later (possible agent reconnection).
     * @param initialPercepts mapping from agent names to initial percepts
     */
    void handleInitialPercepts(Map<String, Percept> initialPercepts) {
        initialPercepts.forEach((agName, percept) -> {
            if (agents.containsKey(agName)){
                agents.get(agName).handleInitialPercept(percept);
            }
        });
    }

    /**
     * Uses the percepts to send a request-action message and waits for the action answers.
     * {@link #agentTimeout} is used to limit the waiting time per agent.
     * @param percepts mapping from agent names to percepts of the current simulation state
     * @return mapping from agent names to actions received in response
     */
    Map<String, Action> requestActions(Map<String, Percept> percepts) {
        // each thread needs to countdown the latch when it finishes
        CountDownLatch latch = new CountDownLatch(agents.keySet().size());
        Map<String, Action> resultMap = new ConcurrentHashMap<>();
        percepts.forEach((agName, percept) -> {
            // start a new thread to get each action
            new Thread(() -> agents.get(agName).requestAction(percept)).start();
        });
        try {
            latch.await(2 * agentTimeout, TimeUnit.MILLISECONDS); // timeout ensured by threads; use this one for safety reasons
        } catch (InterruptedException e) {
            Log.log(Log.ERROR, "Latch interrupted. Actions probably incomplete.");
        }
        return resultMap;
    }

    /**
     * Sends sim-end percepts to the agents.
     * @param finalPercepts mapping from agent names to sim-end percepts
     */
    void handleFinalPercepts(Map<String, Percept> finalPercepts) {
        finalPercepts.forEach((agName, percept) -> {
            if (agents.containsKey(agName)){
                agents.get(agName).sendPerceptMessage("sim-end", percept, -1);
            }
        });
    }

    /**
     * Stores account info of an agent.
     * Receives messages from and sends messages to remote agents.
     */
    private class AgentProxy {

        private String name;
        private String teamName;
        private String password;

        private Socket socket;

        private final LinkedBlockingQueue<Document> sendQueue = new LinkedBlockingQueue<>();
        private Map<Long, Document> actionQueue = new HashMap<>();
        private Thread sendThread;
        private Thread receiveThread;

        private AtomicLong messageCounter = new AtomicLong();
        private Map<Long, CompletableFuture<Document>> futureActions = new ConcurrentHashMap<>();

        private AgentProxy(String name, String team, String pass) {
            this.name = name;
            this.teamName = team;
            this.password = pass;
        }

        private void setSocket(Socket newSocket){
            close(); // potentially close old socket
            socket = newSocket;
            sendThread = new Thread(this::send);
            sendThread.start();
            receiveThread = new Thread(this::receive);
            receiveThread.start();
        }

        private void receive() {
            DocumentBuilder docBuilder = null;
            InputStream in = null;
            try {
                docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                in = socket.getInputStream();
            } catch (IOException e) {
                Log.log(Log.ERROR,"Unable to get InputStream from Socket. Stop receiving.");
                return;
            } catch (ParserConfigurationException e) {
                Log.log(Log.ERROR, "Parser error. Stop receiving.");
                return;
            }

            /*
             * TODO: does this make sense at all? Try a simpler approach.
             */
            ByteArrayOutputStream packetBuffer = new ByteArrayOutputStream();
            boolean seekNextEnd = false; // is the terminating null-byte overdue?
            int packetLen = 0; // length of current packet so far
            try {
                while (true) {
                    // Read at least 1 byte or as many as available, limited by maximum packet length
                    int amount = in.available();
                    amount = Math.max(amount, 1);
                    amount = Math.min(amount, maximumPacketLength);
                    byte[] buffer = new byte[amount];
                    in.read(buffer);

                    // process read bytes
                    int firstNotCopied = 0;
                    for (int i = 0; i < amount; i++) {
                        // if we've found a null byte or if we're at the end of the buffer (or both)
                        if ((buffer[i] == 0 || i == amount - 1) && !seekNextEnd) {
                            // first check if we're breaching maximum packet length
                            packetLen += i - firstNotCopied;
                            if (packetLen > maximumPacketLength) {
                                Log.log(Log.NORMAL, "Packet too long.");
                                seekNextEnd = true;
                            } else // and possibly write data to packet buffer
                                packetBuffer.write(buffer, firstNotCopied,i - firstNotCopied + (buffer[i] == 0? 0 : 1));
                        }
                        if (buffer[i] == 0) { // if we've found a null byte some packet has to end here.
                            //convert packet to XML
                            Document doc = docBuilder.parse(new ByteArrayInputStream(packetBuffer.toByteArray()));
                            handleReceivedDoc(doc);
                            packetBuffer = new ByteArrayOutputStream();
                            seekNextEnd = false;
                            packetLen = 0;
                            firstNotCopied = i + 1;
                        }
                    }
                }
            } catch (IOException | SAXException ignored) {}
        }

        /**
         * Handles one received document.
         * @param doc the document that needs to be processed
         */
        private void handleReceivedDoc(Document doc) {
            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = null;
            try {
                transformer = factory.newTransformer();
                transformer.setOutputProperty("indent","yes");
            } catch (TransformerConfigurationException e) {
                return;
            }
            Element root = doc.getDocumentElement();
            if (root == null) {
                Log.log(Log.NORMAL,"Received document with missing root element.");
            }
            else if (root.getNodeName().equals("message")) {
                if (root.getAttribute("type").equals("action")) {
                    Log.log(Log.NORMAL, "processing action");
                    long actionID;
                    NodeList actions = root.getElementsByTagName("action");
                    if (actions.getLength() == 0) {
                        Log.log(Log.ERROR,"No action element inside action message.");
                        return;
                    }
                    try {
                        actionID = Long.parseLong(((Element)actions.item(0)).getAttribute("id"));
                    } catch (NumberFormatException e) {
                        Log.log(Log.ERROR, "Received invalid or no action id.");
                        return;
                    }
                    if (futureActions.containsKey(actionID)){
                        futureActions.get(actionID).complete(doc);
                    }
                }
                else {
                    Log.log(Log.NORMAL,"Received unknown message type.");
                    try {
                        transformer.transform(new DOMSource(doc), new StreamResult(System.out));
                    } catch(Exception ignored) {}
                }
            } else {
                Log.log(Log.NORMAL,"Received invalid message.");
                try {
                    transformer.transform(new DOMSource(doc),new StreamResult(System.out));
                } catch(Exception ignored) {}
            }
        }

        /**
         * Sends all messages from {@link #sendQueue}, blocks if it is empty.
         */
        private void send() {
            while (true) {
                try {
                    if (disconnecting && sendQueue.isEmpty()) {
                        //TODO disconnecting ?? (& other syncs)
                        break;
                    }
                    Document sendDoc = sendQueue.take();
                    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                    TransformerFactory.newInstance().newTransformer().transform(
                            new DOMSource(sendDoc), new StreamResult(buffer));
                    sendPacket(buffer.toByteArray());
                } catch (InterruptedException | TransformerException | IOException e) {
                    Log.log(Log.DEBUG, "Error writing to socket. Stop sending now.");
                    break;
                }
            }
        }

        /**
         * Sends the byte array over the socket (with trailing 0).
         * @param b the bytes to send
         * @throws IOException if sending fails at any point
         */
        private void sendPacket(byte[] b) throws IOException {
            OutputStream out = socket.getOutputStream();
            out.write(b);
            out.write(0);
            out.flush();
        }

        /**
         * Closes socket and stops threads (if they exist).
         */
        private void close() {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException ignored) {}
            }
            if (sendThread != null) sendThread.interrupt();
            if (receiveThread != null) receiveThread.interrupt();
        }

        /**
         * Creates a message for the given initial percept and sends it to the remote agent.
         * @param percept the initial percept to forward
         */
        void handleInitialPercept(Percept percept) {
            sendPerceptMessage("sim-start", percept, -1);
        }

        /**
         * Creates a request-action message and sends it to the agent.
         * Should be called within a new thread, as it blocks up to {@link #agentTimeout} milliseconds.
         * @param percept the step percept to forward
         */
        Action requestAction(Percept percept) {
            long id = messageCounter.getAndIncrement();
            CompletableFuture<Document> futureAction = new CompletableFuture<>();
            futureActions.put(id, futureAction);
            id = sendPerceptMessage("request-action", percept, id);
            if(id != -1) { // id might be -1 again if sth. went wrong while sending the message
                try {
                    // wait for action to be received
                    return Action.parse(futureAction.get(agentTimeout, TimeUnit.MILLISECONDS));
                } catch (InterruptedException | ExecutionException e) {
                    Log.log(Log.ERROR, "Interrupted while waiting for action.");
                } catch (TimeoutException e) {
                    Log.log(Log.NORMAL, "No valid action available in time for agent " + name + ".");
                }
            }
            return Action.NO_ACTION;
        }

        /**
         * Creates a default message that can be sent to agents.
         * @param msgType the type of the message. Current types include "sim-start", "sim-end", "request-action", etc.
         * @param percept the percept to attach to the message
         * @param messageID the messageID to use. If it is -1 and a real one is needed, a new one will be drawn from {@link #messageCounter}
         * @return the message id used or -1 (in case of sim-end or error)
         */
        private long sendPerceptMessage(String msgType, Percept percept, long messageID) {
            DocumentBuilder docBuilder = null;
            try {
                docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            } catch (ParserConfigurationException e) {
                Log.log(Log.ERROR, "Serious parser exception. Could not create message.");
                return messageID;
            }
            Document doc = docBuilder.newDocument();
            Element root = doc.createElement("message");
            doc.appendChild(root);
            long timestamp = System.currentTimeMillis();
            root.setAttribute("timestamp", Long.toString(timestamp));
            root.setAttribute("type", msgType);

            Element simElement;
            switch(msgType) {
                case "sim-start":
                    simElement = doc.createElement("simulation");
                    break;
                case "request-action":
                    simElement = doc.createElement("perception");
                    simElement.setAttribute("deadline", String.valueOf(timestamp + agentTimeout));
                    break;
                case "sim-end":
                    simElement = doc.createElement("sim-result");
                    break;
                default:
                    Log.log(Log.ERROR, "Unknown message type.");
                    return messageID;
            }
            if (!msgType.equals("sim-end")){ // only sim-end has no ID
                long id = messageID == -1? messageCounter.getAndIncrement() : messageID;
                simElement.setAttribute("id", String.valueOf(id));
            }
            doc.appendChild(simElement);
            percept.toXML(simElement);
            try {
                sendQueue.put(doc);
            } catch (InterruptedException e) {
                Log.log(Log.ERROR, "Interrupted while trying to put message into queue.");
            }
            return messageID;
        }
    }
}
