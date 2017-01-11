package massim;

import org.w3c.dom.Element;

/**
 * Represents any percept that can be sent to an agent (via XML).
 * @author ta10
 */
public interface Percept {

    /**
     * Creates an XML representation of this percept.
     * @param simElement the ("simulation") element that will hold the converted percept
     */
    void toXML(Element simElement);
}
