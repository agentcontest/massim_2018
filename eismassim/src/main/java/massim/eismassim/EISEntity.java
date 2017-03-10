package massim.eismassim;

import eis.exceptions.ActException;
import eis.exceptions.PerceiveException;
import eis.iilang.Action;
import eis.iilang.Numeral;
import eis.iilang.Parameter;
import eis.iilang.Percept;
import massim.eismassim.entities.CityEntity;
import massim.eismassim.util.Conversions;
import massim.protocol.*;
import massim.protocol.messagecontent.*;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * An entity for the EIS to realize client-server communication following the MASSim protocol.
 */
public abstract class EISEntity implements Runnable{

    private static EnvironmentInterface EI;

    // config for all entities
    private static int timeout; // timeout for performing actions (if scheduling is enabled)
    private static boolean scheduling = false; // send only one action per action-id?
    private static boolean times = false; // annotate percepts with timestamp?
    private static boolean notifications = false; // send percepts as notifications?
    private static boolean queued = false;

    // config for this entity
    private String name;
    private String username;
    private String password;
    private String host;
    private int port;
    private boolean useXML = false;
    private boolean useIILang = false;

    private boolean connected = false;
    private Socket socket;
    private InputStream in;
    private OutputStream out;
    private volatile boolean terminated = false;

    private Set<Percept> simStartPercepts = Collections.synchronizedSet(new HashSet<>());
    private Set<Percept> requestActionPercepts = Collections.synchronizedSet(new HashSet<>());
    private Set<Percept> simEndPercepts = Collections.synchronizedSet(new HashSet<>());
    private Set<Percept> byePercepts = Collections.synchronizedSet(new HashSet<>());

    // used to store the percepts in the order of arrival, if queuing is activated
    private AbstractQueue<Collection<Percept>> perceptsQueue = new ConcurrentLinkedQueue<>();

    // action IDs
    private long lastUsedActionId;
    protected long currentActionId;
    private long lastUsedActionIdPercept;

    /**
     * @return an array containing all classes representing percepts (extending
     * {@link RequestAction} and {@link SimStart} in the entity's
     * scenario)
     */
    protected abstract Class[] getPerceptTypes();

    /**
     * Maps the sim-start-message to IILang.
     * @param startPercept the sim-start message to map
     * @return a list of percepts derived from the sim-start message
     */
    protected abstract List<Percept> simStartToIIL(SimStart startPercept);

    /**
     * Maps the request-action-message to IILang.
     * @param message the step percept message
     * @return a collection of percepts derived from the request-action message
     */
    protected abstract Collection<Percept> requestActionToIIL(Message message);

    /**
     * Maps the sim-end-message to IILang.
     * @param endPercept the sim-end percept to map
     * @return a collection of percepts derived from the sim-end message
     */
    protected abstract Collection<Percept> simEndToIIL(SimEnd endPercept);

    /**
     * Maps an IILang-action to XML.
     * @param action the action to transform
     * @return the XML document (to send)
     */
    protected abstract Document actionToXML(Action action);

    /**
     * Sets the environment interface for all entities
     * @param environmentInterface the EI
     */
    static void setEnvironmentInterface(EnvironmentInterface environmentInterface) {
        EISEntity.EI = environmentInterface;
    }

    /**
     * Set the timeout of all entities.
     * @param timeout the timeout for all entities in ms for scheduling (if scheduling is enabled)
     */
    static void setTimeout(int timeout) {
        EISEntity.timeout = timeout;
    }

    /**
     * Enables timestamp annotations for percepts.
     */
    static void enableTimeAnnotations() {
        times = true;
    }

    /**
     * Enables scheduling, i.e. only one action can be sent per action id.
     */
    static void enableScheduling() {
        scheduling = true;
    }

    /**
     * Enables notifications.
     */
    static void enableNotifications() {
        notifications = true;
    }

    /**
     * Enables queued percepts.
     */
    static void enablePerceptQueue() {
        queued = true;
    }

    /**
     * Enables xml output for percepts.
     */
    void enableXML() {
        useXML = true;
    }

    /**
     * Enables IILang output for percepts.
     */
    void enableIILang() {
        useIILang = true;
    }

    /**
     * @return the name of this entity
     */
    String getName(){
        return name;
    }

    /**
     * Factory method for creating a scenario-specific entity.
     * @param name name of the entity
     * @param scenario the scenario to use
     * @param host massim server address
     * @param port massim server port
     * @param username entity user name for massim server
     * @param password password for massim server
     * @return an entity with the given parameters or null if the scenario is not known
     */
    static EISEntity createEntity(String name, String scenario, String host, int port, String username, String password) {
        EISEntity entity = null;
        switch(scenario){
            case "city2017":
                entity = new CityEntity();
                break;
        }
        if(entity != null){
            entity.name = name;
            entity.host = host;
            entity.port = port;
            entity.username = username;
            entity.password = password;
        }
        return entity;
    }

    /**
     * Stops this entity and its thread. Closes the socket, if there is one.
     */
    public void terminate(){
        terminated = true;
        if(socket != null){
            try {
                socket.close();
            } catch (IOException ignored) {}
        }
    }

    @Override
    public void run() {
        while (!terminated){
            // connect if not connected
            if (!connected) {
                establishConnection();
                continue;
            }

            // receive a message
            Document doc;
            try {
                doc = receiveDocument();
            } catch (IOException | SAXException | ParserConfigurationException e) {
                releaseConnection();
                continue;
            }

            // process message
            Message msg = Message.parse(doc, getPerceptTypes());
            if (msg == null) continue;

            if (msg.getContent() instanceof SimStart) {
                simStartPercepts.clear();
                simStartPercepts.add(new Percept("simStart"));
                simStartPercepts.addAll(simStartToIIL((SimStart) msg.getContent()));

                if (times) annotatePercepts(simStartPercepts, new Numeral(msg.getTimestamp()));
                if (notifications) EI.sendNotifications(getName(), simStartPercepts);
                if (queued) perceptsQueue.add(Collections.synchronizedSet(new HashSet<>(simStartPercepts)));
            }
            else if (msg.getContent() instanceof RequestAction) {
                RequestAction rac = (RequestAction) msg.getContent();
                long id = rac.getId();

                requestActionPercepts.clear();
                requestActionPercepts.add(new Percept("requestAction"));
                requestActionPercepts.addAll(requestActionToIIL(msg));

                if (times) annotatePercepts(requestActionPercepts, new Numeral(msg.getTimestamp()));
                if (notifications) EI.sendNotifications(this.getName(), requestActionPercepts);
                currentActionId = id;
                if (queued) perceptsQueue.add(Collections.synchronizedSet(new HashSet<>(requestActionPercepts)));
            }
            else if (msg.getContent() instanceof SimEnd) {
                simStartPercepts.clear();
                requestActionPercepts.clear();
                simEndPercepts.clear();
                simEndPercepts.add(new Percept("simEnd"));
                simEndPercepts.addAll(simEndToIIL((SimEnd) msg.getContent()));
                if (times) annotatePercepts(simEndPercepts,new Numeral(msg.getTimestamp()));
                if (notifications) EI.sendNotifications(this.getName(), simEndPercepts);
                if (queued) perceptsQueue.add(Collections.synchronizedSet(new HashSet<>(simEndPercepts)));
            }
            else if (msg.getContent() instanceof Bye) {
                simStartPercepts.clear();
                requestActionPercepts.clear();
                byePercepts.clear();
                byePercepts.add(new Percept("bye"));
                if (times) annotatePercepts(byePercepts,new Numeral(msg.getTimestamp()));
                if (notifications) EI.sendNotifications(this.getName(), byePercepts);
                if (queued) perceptsQueue.add(Collections.synchronizedSet(new HashSet<>(byePercepts)));
            }
            else {
                log("unexpected type " + msg.getContent().getClass());
            }
        }
    }

    /**
     * @return true if the entity is connected to a massim server
     */
    boolean isConnected() {
        return connected;
    }

    /**
     * Retrieves all percepts for this entity.
     * If scheduling is enabled, the method blocks until a new action id, i.e. new percepts, are received
     * or the configured timeout is reached.
     * If queued is enabled, scheduling is overridden. Also, if queued is enabled, this method has to be called
     * repeatedly, as only one collection of percepts is removed from the queue with each call (until an empty list
     * is returned).
     * @return all percepts for this entity
     * @throws PerceiveException if timeout configured and occurred
     */
    LinkedList<Percept> getAllPercepts() throws PerceiveException{
        if (scheduling && !queued) {
            // wait for new action id or timeout
            long startTime = System.currentTimeMillis();
            while (currentActionId <= lastUsedActionIdPercept || currentActionId == -1 ) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException ignored) {}
                if (System.currentTimeMillis() - startTime >= timeout) {
                    throw new PerceiveException("timeout. no valid action-id available in time");
                }
            }
            lastUsedActionIdPercept = currentActionId;
        }

        if(!queued){
            //return all percepts
            LinkedList<Percept> ret = new LinkedList<>();
            ret.addAll(simStartPercepts);
            ret.addAll(requestActionPercepts);
            ret.addAll(simEndPercepts);
            ret.addAll(byePercepts);
            if (useIILang) log(ret.toString());
            return ret;
        }
        else{
            //return only the first queued elements
            return perceptsQueue.peek() != null? new LinkedList<>(perceptsQueue.poll()) : new LinkedList<>();
        }
    }

    /**
     * Performs an action by transforming it to XML and sending it to the massim server.
     * @param action the action to perform
     * @throws ActException if the action could not be sent
     */
    void performAction(Action action) throws ActException{
        // connect if not connected, release if connection not possible
        if (!connected) {
            establishConnection();
            if (!connected) {
                releaseConnection();
                throw new ActException(ActException.FAILURE, "no valid connection");
            }
        }

        // wait for a valid action id
        long startTime = System.currentTimeMillis();
        if (scheduling) {
            while (currentActionId <= lastUsedActionId || currentActionId == -1) {
                try { Thread.sleep(10); } catch (InterruptedException ignored) {}
                if (System.currentTimeMillis() - startTime >= timeout) {
                    throw new ActException(ActException.FAILURE, "timeout. no valid action-id available in time");
                }
            }
        }

        Document doc = actionToXML(action);
        try {
            assert currentActionId != lastUsedActionId;
            sendDocument(doc);
            lastUsedActionId = currentActionId;
        } catch (TransformerException | IOException e) {
            releaseConnection();
            throw new ActException(ActException.FAILURE, "sending action failed", e);
        }
    }

    /**
     * Tries to connect to a MASSim server. Including authentication and all.
     */
    void establishConnection() {
        try {
            socket = new Socket(host, port);
            in = socket.getInputStream();
            out = socket.getOutputStream();
        } catch (UnknownHostException e) {
            log("unknown host " + e.getMessage());
            return;
        } catch (IOException e) {
            log(e.getMessage());
            return;
        }
        log("socket successfully created");

        boolean result = authenticate();
        if (result) {
            log("authentication acknowledged");
        }
        else {
            log("authentication denied");
            return;
        }

        lastUsedActionId = -1;
        currentActionId = -1;
        lastUsedActionIdPercept = -1;
        connected = true;
        log("connection successfully authenticated");

        // start a listening thread
        new Thread(this).start();
        log("listening for incoming messages");
    }

    /**
     * Sends an authentication-message to the server and waits for the reply.
     * @return true if authentication succeeded
     */
    private boolean authenticate() {

        // create and try to send message
        Message authReq = new Message(null, new AuthRequest(username, password));
        try {
            sendDocument(authReq.toXML());
        } catch (IOException | TransformerException e) {
            log(e.getMessage());
            return false;
        }

        // get responseMsg
        Document xmlResponse;
        try {
            xmlResponse = receiveDocument();
        }
        catch (IOException | SAXException | ParserConfigurationException e) {
            e.printStackTrace();
            return false;
        }
        Message responseMsg = Message.parse(xmlResponse);

        // check for success
        if (responseMsg == null || !(responseMsg.getContent() instanceof AuthResponse)) return false;
        AuthResponse authResponse = (AuthResponse) responseMsg.getContent();
        return authResponse.getResult() == AuthResponse.AuthenticationResult.OK;
    }

    /**
     * Annotates a collection of percepts with a given parameter
     * @param percepts the percepts to annotate
     * @param param the new parameter
     */
    private void annotatePercepts(Collection<Percept> percepts, Parameter param) {
        for( Percept p : percepts ) p.addParameter(param);
    }

    /**
     * Tries to close the current socket if it exists.
     * Then sleeps for a second.
     */
    private void releaseConnection() {
        if (socket != null){
            try {
                socket.close();
            }
            catch(IOException ignored) {}
        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ignored) {}
        log("connection released");
    }

    /**
     * Logs a message prefixed with this entity's name
     * @param s the string to log
     */
    protected void log(String s) {
        Log.log("Entity " + name + ": " + s);
    }

    /**
     * Sends a document.
     * @param doc the document to be sent
     * @throws IOException if the document could not be sent
     */
    private void sendDocument(Document doc) throws IOException, TransformerException {
        TransformerFactory.newInstance().newTransformer().transform(new DOMSource(doc), new StreamResult(out));
        out.write(0);
        out.flush();
        if (useXML) log(Conversions.docToString(doc, true) + " sent");
    }

    /**
     * Receives a document from the server,
     * @return the received document.
     * @throws IOException if no document could be received
     * @throws ParserConfigurationException if there's some bad problem with the DOM parser
     * @throws SAXException if an error occurred during parsing
     */
    private Document receiveDocument() throws IOException, ParserConfigurationException, SAXException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int read = in.read();
        while (read != 0) {
            if (read == -1) throw new IOException();
            buffer.write(read);
            read = in.read();
        }
        byte[] raw = buffer.toByteArray();
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(raw));
        if (useXML) log(Conversions.docToString(doc, true) + " received");
        return doc;
    }
}