package massim.scenario.city.data.jaxb;

import massim.scenario.city.data.Item;
import massim.scenario.city.data.Tool;

import javax.xml.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    private List<String> tools;

    /**
     * For JAXB
     */
    private ItemData(){}

    /**
     * Constructor.
     * @param original the item to extract the data from
     */
    public ItemData(Item original){
        this.name = original.getName();
        this.volume = original.getVolume();
        if (original.getRequiredItems().size() > 0) parts = new Vector<>();
        for (Map.Entry<Item, Integer> entry : original.getRequiredItems().entrySet()) {
            parts.add(new ItemAmountData(entry.getKey().getName(), entry.getValue()));
        }
        Set<Tool> requiredTools = original.getRequiredTools();
        if(requiredTools.size() > 0) tools = original.getRequiredTools().stream().map(Item::getName).collect(Collectors.toList());
    }

    /**
     * @return the item's name
     */
    public String getName() {
        return name;
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
        return parts;
    }

    /**
     * @return a list of all tools necessary to craft this item or null if no tools required
     */
    public List<String> getTools() {
        return tools;
    }
}
