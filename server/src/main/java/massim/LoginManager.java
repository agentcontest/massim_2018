package massim;

import massim.protocol.Message;
import massim.protocol.messagecontent.AuthRequest;
import massim.protocol.messagecontent.AuthResponse;
import massim.util.Log;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Responsible for network actions.
 * Created in 2017.
 * @author ta10
 */
class LoginManager {

    private boolean stopped = false;
    private ServerSocket serverSocket;
    private Thread thread;
    private AgentManager agentManager;


    /**
     * Creates a new listener waiting for incoming connections.
     * @param port the port on which to listen
     * @param backlog the backlog of the socket
     * @param agentMng the agent connection manager
     * @throws IOException if socket with the given data cannot be opened
     */
    LoginManager(AgentManager agentMng, int port, int backlog) throws IOException {
        agentManager = agentMng;
        serverSocket = new ServerSocket(port, backlog, null);
        thread = new Thread(() -> {
            while (!stopped) {
                try {
                    Log.log(Log.Level.DEBUG, "Waiting for connection...");
                    Socket s = serverSocket.accept();
                    Log.log(Log.Level.DEBUG,"Got a connection.");
                    Thread t = new Thread(() -> handleSocket(s));
                    t.start();
                } catch (IOException e) {
                    Log.log(Log.Level.DEBUG,"Stop listening");
                }
            }
        });
    }

    /**
     * Starts listening on the socket.
     */
    void start() {
        thread.start();
    }

    /**
     * Stops listening.
     */
    void stop() {
        try {
            stopped = true;
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates and sends an auth-response message on the given socket.
     * @param s the socket to send on
     * @param result whether the authentication was successful
     */
    private void sendAuthResponse(Socket s, AuthResponse.AuthenticationResult result) {
        try {
            OutputStream out = s.getOutputStream();
            Document doc = new Message(System.currentTimeMillis(), new AuthResponse(result)).toXML();
            TransformerFactory.newInstance().newTransformer().transform(new DOMSource(doc), new StreamResult(out));
            out.write(0);
        } catch (IOException | TransformerException | TransformerFactoryConfigurationError e) {
            Log.log(Log.Level.CRITICAL, "Auth response could not be sent.");
            e.printStackTrace();
        }
    }

    /**
     * Tries to perform agent authentication on the new socket.
     * @param s the socket to use
     */
    private void handleSocket(Socket s) {
        try {
            InputStream is = s.getInputStream();
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int b;
            while (true) {
                b = is.read();
                if (b == 0) break; // message completely read
                else if (b == -1) return; // stream ended
                else buffer.write(b);
            }
            Document authDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(
                    new ByteArrayInputStream(buffer.toByteArray()));
            Message receivedMsg = Message.parse(authDoc);
            if(receivedMsg != null){
                if(receivedMsg.getContent() != null && receivedMsg.getContent() instanceof AuthRequest) {
                    AuthRequest auth = (AuthRequest) receivedMsg.getContent();
                    Log.log(Log.Level.NORMAL, "got authentication: username=" + auth.getUsername() + " password="
                            + auth.getPassword() + " address=" + s.getInetAddress().getHostAddress());
                    // check credentials and act accordingly
                    if (agentManager.auth(auth.getUsername(), auth.getPassword())) {
                        sendAuthResponse(s, AuthResponse.AuthenticationResult.OK);
                        agentManager.handleNewConnection(s, auth.getUsername());
                    } else {
                        Log.log(Log.Level.ERROR, "Got invalid authentication from: " + s.getInetAddress().getHostAddress());
                        sendAuthResponse(s, AuthResponse.AuthenticationResult.FAILED);
                        try {
                            s.close();
                        } catch (IOException ignored) {}
                    }
                }
                else{
                    Log.log(Log.Level.ERROR, "Received message content: " + receivedMsg.getContent());
                }
            }
            else{
                Log.log(Log.Level.ERROR, "Received wrong message, expected auth-request.");
            }
        } catch (IOException e) {
            Log.log(Log.Level.ERROR, "Error while receiving authentication message");
            e.printStackTrace();
        } catch (ParserConfigurationException | SAXException e) {
            Log.log(Log.Level.ERROR, "Error while parsing authentication message");
            e.printStackTrace();
        }
    }
}
