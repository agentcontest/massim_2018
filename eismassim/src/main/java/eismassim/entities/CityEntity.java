package eismassim.entities;

import eis.iilang.Action;
import eis.iilang.Percept;
import eismassim.EISEntity;
import massim.messages.RequestActionContent;
import massim.messages.SimEndContent;
import massim.messages.SimStartContent;
import org.w3c.dom.Document;

import java.util.Collection;
import java.util.List;

/**
 * An EIS compatible entity for the 2017 MAPC City scenario.
 */
public class CityEntity extends EISEntity{

    /**
     * Creates a new CityEntity. To create a new one, call factory method
     * {@link EISEntity#createEntity(String, String, String, int, String, String)}
     */
    public CityEntity() {}

    @Override
    protected Class[] getPerceptTypes() {
        return new Class[0];
    }

    @Override
    protected List<Percept> simStartToIIL(SimStartContent startPercept) {
        return null;
    }

    @Override
    protected Collection<Percept> requestActionToIIL(RequestActionContent content) {
        return null;
    }

    @Override
    protected Collection<Percept> simEndToIIL(SimEndContent endPercept) {
        return null;
    }

    @Override
    public Document actionToXML(Action action) {
        return null;
    }
}
