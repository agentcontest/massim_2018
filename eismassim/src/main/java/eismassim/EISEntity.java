package eismassim;

import eis.iilang.Action;
import eis.iilang.Percept;

import java.util.LinkedList;

/**
 * An entity for the EIS to realize client-server communication following the MASSim protocol.
 */
public class EISEntity {

    private String name;

    /**
     * New instances created with factory method {@link #createEntity(String, String, String, int, String, String)}.
     */
    private EISEntity(String name){
        this.name = name;
    }

    public static int timeout;

    public static void enableTimeAnnotations() {
    }

    public static void enableScheduling() {
    }

    public static void enableNotifications() {
    }

    public static void setTimeout(int timeout) {
        EISEntity.timeout = timeout;
    }

    public static void enablePerceptQueue() {
    }

    public static EISEntity createEntity(String name, String scenario, String host, int port, String username, String password) {
        return new EISEntity(name);
    }

    public void enableXML() {
    }

    public String getName(){
        return name;
    }

    public void enableIILang() {
    }

    public boolean isConnected() {
        return false;
    }

    public LinkedList<Percept> getAllPercepts() {
        return null;
    }

    public void performAction(Action action) {
    }
    // TODO implement
}
