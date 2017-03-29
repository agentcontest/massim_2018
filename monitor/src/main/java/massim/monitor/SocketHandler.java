package massim.monitor;

import org.webbitserver.BaseWebSocketHandler;
import org.webbitserver.WebSocketConnection;

import java.util.HashSet;

public class SocketHandler extends BaseWebSocketHandler {

    private final HashSet<WebSocketConnection> pool = new HashSet<WebSocketConnection>();

    @Override
    public void onOpen(WebSocketConnection connection) {
        this.pool.add(connection);
    }

    @Override
    public void onClose(WebSocketConnection connection) {
        this.pool.remove(connection);
    }
}
