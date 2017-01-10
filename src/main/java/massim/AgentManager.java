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
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Handles agent accounts and network connections to all agents.
 * @author ta10
 */
class AgentManager {

    private Map<String, AgentProxy> agents = new HashMap<>();
    private LinkedBlockingQueue<Document> sendQueue = new LinkedBlockingQueue<>();
    private Map<Long, Document> actionQueue = new HashMap<>();

    private boolean disconnecting = false;
    int maximumPacketLength = 65536;

    /**
     * Creates a new agent manager responsible for sending and receiving messages.
     * @param teams a list of all teams to configure the manager for
     */
    AgentManager(List<TeamConfig> teams) {
        teams.forEach(team -> team.getAgents().forEach((name, pass) -> {
            agents.put(name, new AgentProxy(name, team.getName(), pass));
        }));
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
     * Stores account info of an agent.
     * Receives messages from and sends messages to remote agents.
     */
    private class AgentProxy {

        private String name;
        private String teamName;
        private String password;

        private Socket socket;

        private Thread sendThread;
        private Thread receiveThread;

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
                    synchronized(actionQueue) {
                        if (actionQueue.containsKey(actionID)) {
                            actionQueue.put(actionID, doc);
                            actionQueue.notify();
                        }
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
         * Sends all messages from {@link AgentManager#sendQueue}, blocks if it is empty.
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
    }
}
