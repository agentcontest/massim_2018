package massim;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;

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
                    Log.log(Log.DEBUG, "Waiting for connection...");
                    Socket s = serverSocket.accept();
                    Log.log(Log.DEBUG,"Got a connection.");
                    Thread t = new Thread(() -> handleSocket(s));
                    t.start();
                } catch (IOException e) {
                    Log.log(Log.DEBUG,"Stop listening");
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

    private void sendAuthResponse(Socket s, boolean result) {
        try {
            OutputStream out = s.getOutputStream();
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            Element elRoot = doc.createElement("message");
            elRoot.setAttribute("type","auth-response");
            elRoot.setAttribute("timestamp", String.valueOf(System.currentTimeMillis()));
            doc.appendChild(elRoot);
            Element elAuth = doc.createElement("authentication");
            elAuth.setAttribute("result", result? "ok" : "fail");
            elRoot.appendChild(elAuth);
            TransformerFactory.newInstance().newTransformer().transform(new DOMSource(doc), new StreamResult(out));
            out.write(0);
        } catch (IOException | TransformerException | TransformerFactoryConfigurationError | ParserConfigurationException e) {
            Log.log(Log.CRITICAL);
            e.printStackTrace();
        }
    }

    /**
     * Tries to perform agent authentication on the new socket.
     * @param s the socket to use
     */
    private void handleSocket(Socket s) {

        Log.log(Log.DEBUG, "Retrieve authentication from client.");
        DocumentBuilder docBuilder;
        try {
            docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
            return;
        }
        String user = "";
        String pass = "";
        try {
            String inString = "";
            InputStream is = s.getInputStream();
            byte b[] = new byte[1];
            while (true) {
                b[0] = (byte)is.read();
                if (b[0] == 0) break;
                else inString += (new String(b,0,1,"UTF-8"));
            }
            Log.log(Log.DEBUG, inString);
            Document authDoc = docBuilder.parse(new ByteArrayInputStream(inString.getBytes()));
            NodeList nl = authDoc.getElementsByTagName("authentication");
            if (nl.getLength() == 0) {
                Log.log(Log.ERROR, "Error while parsing authentication");
                return;
            }
            Element root = (Element)nl.item(0);
            user = root.getAttribute("username");
            pass = root.getAttribute("password");
        } catch (IOException e) {
            Log.log(Log.ERROR, "IO error while receiving authentication");
            e.printStackTrace();
        } catch (SAXException e) {
            Log.log(Log.ERROR, "Error while parsing authentication");
            e.printStackTrace();
        }
        Log.log(Log.NORMAL, "got authentication: username=" + user + " password=" + pass
                + " address=" + s.getInetAddress().getHostAddress());

        // check credentials and act accordingly
        if (agentManager.auth(user, pass)) {
            sendAuthResponse(s, true);
            agentManager.setSocket(s, user);
        }
        else{
            Log.log(Log.CRITICAL,"Got invalid authentication from: " + s.getInetAddress().getHostAddress());
            sendAuthResponse(s,false);
            try { s.close(); } catch (IOException ignored) {}
        }
    }
}
