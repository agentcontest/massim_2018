package massim;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

/**
 * Test cases for the (complete) MASSim server.
 */
public class ServerTest {

    @org.junit.Test
    public void main() throws Exception {

        InputStream is = this.getClass().getResourceAsStream("TestConfig.json");
        String conf = new BufferedReader(new InputStreamReader(is)).lines().collect(Collectors.joining("\n"));

        Server.main(new String[]{"-confString", conf});
    }
}