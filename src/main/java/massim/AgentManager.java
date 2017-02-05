package massim;

import massim.config.TeamConfig;
import massim.messages.*;
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

    /**
     * Creates a new agent manager responsible for sending and receiving messages.
     * @param teams a list of all teams to configure the manager for
     * @param agentTimeout the timeout to use for request-action messages (to wait for actions) in milliseconds
     */
    AgentManager(List<TeamConfig> teams, long agentTimeout) {
        teams.forEach(team -> team.getAgentNames().forEach((name) -> {
            agents.put(name, new AgentProxy(name, team.getName(), team.getPassword(name)));
        }));
        this.agentTimeout = agentTimeout;
    }

    /**
     * Stops all related threads and closes all sockets involved.
     */
    void stop(){
        disconnecting = true;
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
            agents.get(agentName).resendSimStartMessage();
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
    void handleInitialPercepts(Map<String, SimStartContent> initialPercepts) {
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
    Map<String, ActionContent> requestActions(Map<String, RequestActionContent> percepts) {
        // each thread needs to countdown the latch when it finishes
        CountDownLatch latch = new CountDownLatch(agents.keySet().size());
        Map<String, ActionContent> resultMap = new ConcurrentHashMap<>();
        percepts.forEach((agName, percept) -> {
            // start a new thread to get each action
            new Thread(() -> agents.get(agName).requestAction(percept, latch)).start();
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
    void handleFinalPercepts(Map<String, SimEndContent> finalPercepts) {
        finalPercepts.forEach((agName, percept) -> {
            if (agents.containsKey(agName)){
                agents.get(agName).handleFinalPercept(percept);
            }
        });
    }

    /**
     * Stores account info of an agent.
     * Receives messages from and sends messages to remote agents.
     */
    private class AgentProxy {

        // things that do not change
        private String name;
        private String teamName;
        private String password;

        // networking things
        private Socket socket;
        private Thread sendThread;
        private Thread receiveThread;

        // concurrency magic
        private AtomicLong messageCounter = new AtomicLong();
        private LinkedBlockingDeque<Document> sendQueue = new LinkedBlockingDeque<>();
        private Map<Long, CompletableFuture<Document>> futureActions = new ConcurrentHashMap<>();

        private Document lastSimStartMessage;

        /**
         * Creates a new instance with the given credentials.
         * @param name the name of the agent
         * @param team the name of the agent's team
         * @param pass the password to authenticate the agent with
         */
        private AgentProxy(String name, String team, String pass) {
            this.name = name;
            this.teamName = team;
            this.password = pass;
        }

        /**
         * Creates a message for the given initial percept and sends it to the remote agent.
         * @param percept the initial percept to forward
         */
        void handleInitialPercept(SimStartContent percept) {
            lastSimStartMessage = new Message(System.currentTimeMillis(), percept).toXML();
            sendMessage(lastSimStartMessage);
        }

        /**
         * Creates a request-action message and sends it to the agent.
         * Should be called within a new thread, as it blocks up to {@link #agentTimeout} milliseconds.
         * @param percept the step percept to forward
         * @param latch the latch to count down after the action is acquired (or not)
         * @return the action that was received by the agent (or {@link ActionContent#STD_NO_ACTION})
         */
        ActionContent requestAction(RequestActionContent percept, CountDownLatch latch) {
            long id = messageCounter.getAndIncrement();
            percept.finalize(id, System.currentTimeMillis() + agentTimeout);
            CompletableFuture<Document> futureAction = new CompletableFuture<>();
            futureActions.put(id, futureAction);
            sendMessage(new Message(System.currentTimeMillis(), percept).toXML());
            try {
                // wait for action to be received
                latch.countDown();
                Document doc = futureAction.get(agentTimeout, TimeUnit.MILLISECONDS);
                Message msg = Message.parse(doc, ActionContent.class);
                if(msg != null){
                    MessageContent content = msg.getContent();
                    if(content instanceof ActionContent) return (ActionContent) content;
                }
            } catch (InterruptedException | ExecutionException e) {
                Log.log(Log.ERROR, "Interrupted while waiting for action.");
            } catch (TimeoutException e) {
                Log.log(Log.NORMAL, "No valid action available in time for agent " + name + ".");
            }
            latch.countDown();
            return ActionContent.STD_NO_ACTION;
        }

        /**
         * Creates and send a sim-end message to the agent.
         * @param percept the percept to append to the message.
         */
        void handleFinalPercept(SimEndContent percept) {
            lastSimStartMessage = null; // now we can stop resending it
            sendMessage(new Message(System.currentTimeMillis(), percept).toXML());
        }

        /**
         * Sets a new endpoint for sending and receiving messages. If a socket is already present, it is replaced and closed.
         * @param newSocket the new socket to use for this agent
         */
        private void setSocket(Socket newSocket){
            // potentially close old socket
            if (sendThread != null) sendThread.interrupt();
            if (receiveThread != null) receiveThread.interrupt();
            if (socket != null){
                try { socket.close(); } catch (IOException ignored) {}
            }
            // set new socket and open new threads
            socket = newSocket;
            sendThread = new Thread(this::send);
            sendThread.start();
            receiveThread = new Thread(this::receive);
            receiveThread.start();
        }

        private void receive() {
            DocumentBuilder docBuilder;
            InputStream in;
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
                int maximumPacketLength = 65536;
                while (!disconnecting) { // we can stop receiving when disconnect is triggered
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
         * Handles one received document (from the remote agent).
         * @param doc the document that needs to be processed
         */
        private void handleReceivedDoc(Document doc) {
            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer;
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
                    if (disconnecting && sendQueue.isEmpty()) { // we can stop when everything is sent (e.g. the bye message)
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
            sendMessage(createEmptyMessage("bye"));
            try {
                sendThread.join(5000); // give bye-message some time to be sent (but not too much)
            } catch (InterruptedException e) {
                Log.log(Log.ERROR, "Interrupted while waiting for disconnection.");
            }
            if (sendThread != null) sendThread.interrupt();
            if (receiveThread != null) receiveThread.interrupt();
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException ignored) {}
            }
        }

        /**
         * Creates an "empty" message XML object.
         * @param type the type of the message ("request-action" etc.)
         * @return the message document
         */
        private Document createEmptyMessage(String type){
            DocumentBuilder docBuilder;
            try {
                docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            } catch (ParserConfigurationException e) {
                Log.log(Log.ERROR, "Serious parser exception. Could not create message.");
                return null;
            }
            Document doc = docBuilder.newDocument();
            Element root = doc.createElement("message");
            doc.appendChild(root);
            root.setAttribute("timestamp", Long.toString(System.currentTimeMillis()));
            root.setAttribute("type", type);
            return doc;
        }

        /**
         * Puts the given message into the send queue as soon as possible.
         * @param message the message document to send
         */
        private void sendMessage(Document message){
            try {
                sendQueue.put(message);
            } catch (InterruptedException e) {
                Log.log(Log.ERROR, "Interrupted while trying to put message into queue.");
            }
        }

        /**
         * If there already exists a sim start message, it is added to the front of the {@link #sendQueue}.
         */
        void resendSimStartMessage() {
            Document message = lastSimStartMessage;
            if (message != null && !sendQueue.contains(message)){
                sendQueue.addFirst(message);
            }
        }
    }
}
