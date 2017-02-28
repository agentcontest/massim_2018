package massim.protocol;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Holds all the world data that does not change during the simulation.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public abstract class StaticWorldData extends WorldData {}
