package massim;

import org.w3c.dom.Document;

/**
 * We probably need this
 * TODO
 * @author ta10
 */
public class Action {

    public static final Action NO_ACTION = new Action();

    /**
     * Parses the given XML document to a valid Action object.
     * @param doc the XML action to parse
     * @return an action object according to the XML file or {@link #NO_ACTION} if sth. went wrong
     */
    static Action parse(Document doc) {
        //TODO!!
        return NO_ACTION;
    }
}
