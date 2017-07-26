package massim;

import massim.config.TeamConfig;
import massim.protocol.Message;
import massim.protocol.MessageContent;
import massim.protocol.messagecontent.*;
import massim.util.Log;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
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
    private int maxPacketLength;

    /**
     * If an agent's sendQueue is already "full", the oldest element will be removed before a new one is added
     */
    private int sendBufferSize = 4;

    /**
     * Creates a new agent manager responsible for sending and receiving messages.
     * @param teams a list of all teams to configure the manager for
     * @param agentTimeout the timeout to use for request-action messages (to wait for actions) in milliseconds
     * @param maxPacketLength the maximum size of packets to <b>process</b> (they are received anyway, just not parsed
     *                        in case they are too big)
     */
    AgentManager(List<TeamConfig> teams, long agentTimeout, int maxPacketLength) {
        teams.forEach(team -> team.getAgentNames().forEach((name) -> {
            agents.put(name, new AgentProxy(name, team.getName(), team.getPassword(name)));
        }));
        this.agentTimeout = agentTimeout;
        this.maxPacketLength = maxPacketLength;
    }

    /**
     * Stops all related threads and closes all sockets involved.
     */
    void stop(){
        disconnecting = true;
        agents.values().forEach(AgentProxy::close);
    }

    /**
     * Sets a new socket for the given agent that was just authenticated (again or for the first time).
     * @param s the new socket opened for the agent
     * @param agentName the name of the agent
     */
    void handleNewConnection(Socket s, String agentName){
        if (agents.containsKey(agentName)) agents.get(agentName).handleNewConnection(s);
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
    void handleInitialPercepts(Map<String, SimStart> initialPercepts) {
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
    Map<String, Action> requestActions(Map<String, RequestAction> percepts) {
        // each thread needs to countdown the latch when it finishes
        CountDownLatch latch = new CountDownLatch(percepts.keySet().size());
        Map<String, Action> resultMap = new ConcurrentHashMap<>();
        percepts.forEach((agName, percept) -> {
            // start a new thread to get each action
            new Thread(() -> {
                Action action = agents.get(agName).requestAction(percept);
                latch.countDown();
                resultMap.put(agName, action);
            }).start();
        });
        try {
            latch.await(2 * agentTimeout, TimeUnit.MILLISECONDS); // timeout ensured by threads; use this one for safety reasons
        } catch (InterruptedException e) {
            Log.log(Log.Level.ERROR, "Latch interrupted. Actions probably incomplete.");
        }
        return resultMap;
    }

    /**
     * Sends sim-end percepts to the agents.
     * @param finalPercepts mapping from agent names to sim-end percepts
     */
    void handleFinalPercepts(Map<String, SimEnd> finalPercepts) {
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
        void handleInitialPercept(SimStart percept) {
            lastSimStartMessage = new Message(System.currentTimeMillis(), percept).toXML();
            sendMessage(lastSimStartMessage);
        }

        /**
         * Creates a request-action message and sends it to the agent.
         * Should be called within a new thread, as it blocks up to {@link #agentTimeout} milliseconds.
         * @param percept the step percept to forward
         * @return the action that was received by the agent (or {@link Action#STD_NO_ACTION})
         */
        Action requestAction(RequestAction percept) {
            long id = messageCounter.getAndIncrement();
            percept.finalize(id, System.currentTimeMillis() + agentTimeout);
            CompletableFuture<Document> futureAction = new CompletableFuture<>();
            futureActions.put(id, futureAction);
            sendMessage(new Message(System.currentTimeMillis(), percept).toXML());
            try {
                // wait for action to be received
                Document doc = futureAction.get(agentTimeout, TimeUnit.MILLISECONDS);
                Message msg = Message.parse(doc, Action.class);
                if(msg != null){
                    MessageContent content = msg.getContent();
                    if(content instanceof Action) {
                        return (Action) content;
                    }
                }
            } catch (InterruptedException | ExecutionException e) {
                Log.log(Log.Level.ERROR, "Interrupted while waiting for action.");
            } catch (TimeoutException e) {
                Log.log(Log.Level.NORMAL, "No valid action available in time for agent " + name + ".");
            }
            return Action.STD_NO_ACTION;
        }

        /**
         * Creates and send a sim-end message to the agent.
         * @param percept the percept to append to the message.
         */
        void handleFinalPercept(SimEnd percept) {
            lastSimStartMessage = null; // now we can stop resending it
            sendMessage(new Message(System.currentTimeMillis(), percept).toXML());
        }

        /**
         * Sets a new endpoint for sending and receiving messages. If a socket is already present, it is replaced and closed.
         * @param newSocket the new socket to use for this agent
         */
        private void handleNewConnection(Socket newSocket){
            // potentially close old socket
            if (sendThread != null) sendThread.interrupt();
            if (receiveThread != null) receiveThread.interrupt();
            if (socket != null) try { socket.close(); } catch (IOException ignored) {}
            // set new socket and open new threads
            socket = newSocket;
            sendQueue.clear();
            // resend sim start message if available
            if(lastSimStartMessage != null) sendQueue.addFirst(lastSimStartMessage);
            sendThread = new Thread(this::send);
            sendThread.start();
            receiveThread = new Thread(this::receive);
            receiveThread.start();
        }

        /**
         * Reads XML documents (0-terminated) from the socket. If any "packet" is bigger than
         * {@link #maxPacketLength}, the read bytes are immediately discarded until the next 0 byte.
         */
        private void receive() {
            DocumentBuilder docBuilder;
            InputStream in;
            try {
                docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                in = new BufferedInputStream(socket.getInputStream());

                ByteArrayOutputStream buffer = new ByteArrayOutputStream(maxPacketLength);
                int readBytes = 0;
                boolean skipping = false;
                while (!disconnecting){
                    int b = in.read();
                    if (!skipping && b != 0) buffer.write(b);
                    if(b == -1) break; // stream ended
                    if (b == 0){
                        if (skipping){
                            skipping = false; // new packet next up
                        }
                        else {
                            // document complete
                            Document doc = docBuilder.parse(new ByteArrayInputStream(buffer.toByteArray()));
                            handleReceivedDoc(doc);
                            buffer = new ByteArrayOutputStream();
                            readBytes = 0;
                        }
                    }
                    if (readBytes++ >= maxPacketLength){
                        buffer = new ByteArrayOutputStream();
                        readBytes = 0;
                        skipping = true;
                    }
                }
            } catch (IOException e) {
                Log.log(Log.Level.ERROR, "Error receiving document. Stop receiving.");
            } catch (ParserConfigurationException e) {
                Log.log(Log.Level.ERROR, "Parser error. Stop receiving.");
            } catch (SAXException e) {
                e.printStackTrace();
            }
        }

        /**
         * Handles one received document (from the remote agent).
         * @param doc the document that needs to be processed
         */
        private void handleReceivedDoc(Document doc) {

            Message message = Message.parse(doc);
            if(message == null) {
                Log.log(Log.Level.ERROR, "Received invalid message.");
                return;
            }
            if(message.getContent() instanceof Action){
                long actionID = ((Action) message.getContent()).getID();
                if(actionID != -1 && futureActions.containsKey(actionID)){
                    futureActions.get(actionID).complete(doc);
                }
                else Log.log(Log.Level.ERROR, "Invalid action id " + actionID + " from " + name);
            }
            else{
                Log.log(Log.Level.NORMAL, "Received unknown message type from " + name);
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
                    // send packet
                    OutputStream out = socket.getOutputStream();
                    out.write(buffer.toByteArray());
                    out.write(0);
                    out.flush();
                } catch (InterruptedException | TransformerException | IOException e) {
                    Log.log(Log.Level.DEBUG, "Error writing to socket. Stop sending now.");
                    break;
                }
            }
        }

        /**
         * Closes socket and stops threads (if they exist).
         */
        private void close() {
            sendMessage(new Message(System.currentTimeMillis(), new Bye()).toXML());
            try {
                if(sendThread!=null)
                    sendThread.join(5000); // give bye-message some time to be sent (but not too much)
            } catch (InterruptedException e) {
                Log.log(Log.Level.ERROR, "Interrupted while waiting for disconnection.");
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
         * Puts the given message into the send queue as soon as possible.
         * @param message the message document to send
         */
        private void sendMessage(Document message){
            if(sendQueue.size() > sendBufferSize) try {
                sendQueue.take();
            } catch (InterruptedException ignored) {}
            try {
                sendQueue.put(message);
            } catch (InterruptedException e) {
                Log.log(Log.Level.ERROR, "Interrupted while trying to put message into queue.");
            }
        }
    }
}
