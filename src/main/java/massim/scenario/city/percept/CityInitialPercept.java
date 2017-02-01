package massim.scenario.city.percept;

import massim.messages.SimStartPercept;
import massim.scenario.city.data.Entity;
import massim.scenario.city.data.Item;
import massim.scenario.city.data.Role;
import massim.scenario.city.data.WorldState;

import javax.xml.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Represents a sim-start percept in the City scenario.
 * @author ta10
 */
@XmlRootElement(name="simulation")
@XmlAccessorType(XmlAccessType.NONE)
public class CityInitialPercept extends SimStartPercept {

    @XmlAttribute(name="id")
    private String simId;

    @XmlAttribute
    private int steps;

    @XmlAttribute
    private String map;

    @XmlAttribute
    private long seedCapital;

    @XmlAttribute(name="team")
    private String teamName;

    @XmlElement
    public RoleData role;

    @XmlElement
    public List<ItemData> items;

    /**
     * for JAXB
     */
    private CityInitialPercept(){}

    public CityInitialPercept(String agentName, WorldState world) {
        //TODO use data objects as parameters so they are only generated once
        if(world == null) return;
        simId = world.getSimID();
        steps = world.getSteps();
        teamName = world.getTeamForAgent(agentName);
        map = world.getMapName();
        seedCapital = world.getSeedCapital();
        Entity entity = world.getEntity(agentName);
        this.role = new RoleData(entity.getRole());
        this.items = world.getItems().stream().map(ItemData::new).collect(Collectors.toList());
    }

    public String getId(){
        return simId;
    }

    public int getSteps(){
        return steps;
    }

    public String getTeam(){
        return teamName;
    }

    public RoleData getRoleData(){
        return role;
    }

    public List<ItemData> getItemData(){
        return items;
    }

    public String getMapName(){
        return map;
    }

    public long getSeedCapital(){
        return seedCapital;
    }


//    public void toXML(Element target) {
//        Entity entity = world.getEntity(agentName);
//
//        target.setAttribute("steps", String.valueOf(world.getSteps()));
//        target.setAttribute("team", world.getTeamForAgent(agentName));
//        target.setAttribute("map", world.getMapName());
//        target.setAttribute("seedCapital", String.valueOf(world.getSeedCapital()));
//
//        Document doc = target.getOwnerDocument();
//
//        Element elRole = doc.createElement("role");
//        elRole.setAttribute("name", entity.getRole().getName());
//        elRole.setAttribute("speed", String.valueOf(entity.getRole().getSpeed()));
//        elRole.setAttribute("maxLoad", String.valueOf(entity.getRole().getMaxLoad()));
//        elRole.setAttribute("maxBattery", String.valueOf(entity.getRole().getMaxBattery()));
//        for(Tool tool: entity.getRole().getTools()){
//            Element elTool = doc.createElement("tool");
//            elTool.setAttribute("name", tool.getName());
//            elRole.appendChild(elTool);
//        }
//        target.appendChild(elRole);
//
//        Element elProducts = doc.createElement("products");
//        for(Item i: world.getItems()){
//            Element elProduct = doc.createElement("product");
//            elProduct.setAttribute("name", i.getName());
//            elProduct.setAttribute("volume", String.valueOf(i.getVolume()));
//            if(i.needsAssembly()){
//                Element elConsumed = doc.createElement("consumed");
//                for(Map.Entry<Item,Integer> e : i.getRequiredItems().entrySet()){
//                    Element elItem = doc.createElement("item");
//                    elItem.setAttribute("name", e.getKey().getName());
//                    elItem.setAttribute("amount", String.valueOf(e.getValue()));
//                    elConsumed.appendChild(elItem);
//                }
//                elProduct.appendChild(elConsumed);
//                Element elTools = doc.createElement("tools");
//                for(Tool tool : i.getRequiredTools()){
//                    Element elItem = doc.createElement("item");
//                    elItem.setAttribute("name", tool.getName());
//                    elTools.appendChild(elItem);
//                }
//                elProduct.appendChild(elTools);
//            }
//            elProducts.appendChild(elProduct);
//        }
//        target.appendChild(elProducts);
//    }

    /**
     * Info of a role for serialization.
     */
    @XmlRootElement(name = "role")
    @XmlAccessorType(XmlAccessType.NONE)
    public static class RoleData {
        @XmlAttribute
        public String name;
        @XmlAttribute
        int speed;
        @XmlAttribute
        int battery;
        @XmlAttribute
        int load;
        @XmlElement(name="tool")
        List<String> tools;
        private RoleData(){} //used by jaxb
        public RoleData(Role role){
            this.name = role.getName();
            this.speed = role.getSpeed();
            this.battery = role.getMaxBattery();
            this.load = role.getMaxLoad();
            this.tools = role.getTools().stream().map(Item::getName).collect(Collectors.toList());
        }
    }

    /**
     * Info of an item for serialization.
     */
    @XmlRootElement(name = "item")
    @XmlAccessorType(XmlAccessType.NONE)
    public static class ItemData {
        @XmlAttribute
        public String name;
        @XmlAttribute
        public int volume;
        @XmlElementWrapper(name="part")
        public Map<String, Integer> requirements = new HashMap<>();
        @XmlElement(name="tool")
        public List<String> tools;

        private ItemData(){} //jaxb
        public ItemData(Item original){
            this.name = original.getName();
            this.volume = original.getVolume();
            for (Map.Entry<Item, Integer> entry : original.getRequiredItems().entrySet()) {
                requirements.put(entry.getKey().getName(), entry.getValue());
            }
            tools = original.getRequiredTools().stream().map(Item::getName).collect(Collectors.toList());
        }
    }
}