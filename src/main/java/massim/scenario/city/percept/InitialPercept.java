package massim.scenario.city.percept;

import massim.Percept;
import massim.scenario.city.data.Entity;
import massim.scenario.city.data.Item;
import massim.scenario.city.data.Tool;
import massim.scenario.city.data.WorldState;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Map;

/**
 * Represents a sim-start percept in the City scenario.
 * @author ta10
 */
public class InitialPercept implements Percept{

    private String agentName;
    private WorldState world;

    public InitialPercept(String agentName, WorldState world) {
        this.agentName = agentName;
        this.world = world;
    }

    @Override
    public void toXML(Element target) {
        Entity entity = world.getEntity(agentName);

        target.setAttribute("steps", String.valueOf(world.getSteps()));
        target.setAttribute("team", world.getTeamForAgent(agentName));
        target.setAttribute("map", world.getMapName());
        target.setAttribute("seedCapital", String.valueOf(world.getSeedCapital()));

        Document doc = target.getOwnerDocument();

        Element elRole = doc.createElement("role");
        elRole.setAttribute("name", entity.getRole().getName());
        elRole.setAttribute("speed", String.valueOf(entity.getRole().getSpeed()));
        elRole.setAttribute("maxLoad", String.valueOf(entity.getRole().getMaxLoad()));
        elRole.setAttribute("maxBattery", String.valueOf(entity.getRole().getMaxBattery()));
        for(Tool tool: entity.getRole().getTools()){
            Element elTool = doc.createElement("tool");
            elTool.setAttribute("name", tool.getName());
            elRole.appendChild(elTool);
        }
        target.appendChild(elRole);

        Element elProducts = doc.createElement("products");
        for(Item i: world.getItems()){
            Element elProduct = doc.createElement("product");
            elProduct.setAttribute("name", i.getName());
            elProduct.setAttribute("volume", String.valueOf(i.getVolume()));
            if(i.needsAssembly()){
                Element elConsumed = doc.createElement("consumed");
                for(Map.Entry<Item,Integer> e : i.getRequiredItems().entrySet()){
                    Element elItem = doc.createElement("item");
                    elItem.setAttribute("name", e.getKey().getName());
                    elItem.setAttribute("amount", String.valueOf(e.getValue()));
                    elConsumed.appendChild(elItem);
                }
                elProduct.appendChild(elConsumed);
                Element elTools = doc.createElement("tools");
                for(Tool tool : i.getRequiredTools()){
                    Element elItem = doc.createElement("item");
                    elItem.setAttribute("name", tool.getName());
                    elTools.appendChild(elItem);
                }
                elProduct.appendChild(elTools);
            }
            elProducts.appendChild(elProduct);
        }
        target.appendChild(elProducts);
    }
}