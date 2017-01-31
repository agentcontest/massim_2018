package massim.scenario.city.percept;

import massim.Percept;
import massim.scenario.city.data.WorldState;
import org.w3c.dom.Element;

/**
 * A regular percept that is sent each step.
 */
public class StepPercept implements Percept {

    private WorldState state;

    public StepPercept(WorldState state){
            this.state = state;
    }

    @Override
    public void toXML(Element simElement) {

    }
}
