package massim.protocol.scenario.city.data;

import javax.xml.bind.annotation.*;
import java.util.List;
import java.util.Vector;
import java.util.stream.Collectors;

/**
 * Holds complete info of an item for serialization.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class ItemData {

    @XmlAttribute
    private String name;

    @XmlAttribute
    private int volume;

    @XmlElement(name="item")
    private List<ItemAmountData> parts;

    @XmlElement(name="tool")
    private List<ToolData> tools;

    /**
     * For JAXB
     */
    private ItemData(){}

    /**
     * Constructor.
     * @param name name of the item
     * @param volume volume of the item
     * @param requiredParts items required to assemble the item (may be null or empty)
     * @param tools tools required to assemble the item (may be null or empty)
     */
    public ItemData(String name, int volume, List<ItemAmountData> requiredParts, List<ToolData> tools){
        this.name = name;
        this.volume = volume;
        if(requiredParts.size() > 0) parts = requiredParts;
        if(tools.size() > 0) this.tools = tools;
    }

    /**
     * @return the item's name
     */
    public String getName() {
        return name == null? "" : name;
    }

    /**
     * @return the item's volume
     */
    public int getVolume() {
        return volume;
    }

    /**
     * @return a list of the required items to craft this item or null if no items are required
     */
    public List<ItemAmountData> getParts() {
        return parts == null? new Vector<>() : parts;
    }

    /**
     * @return a list of all tools necessary to craft this item or null if no tools required
     */
    public List<String> getTools() {
        return tools == null? new Vector<>() : tools.stream()
                                                    .map(ToolData::getName)
                                                    .collect(Collectors.toList());
    }
}
