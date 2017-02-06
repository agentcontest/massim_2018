package eismassim;

import eis.EIDefaultImpl;
import eis.exceptions.ActException;
import eis.exceptions.NoEnvironmentException;
import eis.exceptions.PerceiveException;
import eis.iilang.Action;
import eis.iilang.Percept;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

/**
 * Environment interface to the MASSim server following the Environment Interface Standard (EIS).
 * Supports the City Scenario 2017.
 */
public class EnvironmentInterface extends EIDefaultImpl{

    private Set<String> supportedActions = new HashSet<>();

    public EnvironmentInterface(){
        super();

        //TODO add supported actions
    }

    @Override
    protected LinkedList<Percept> getAllPerceptsFromEntity(String entity) throws PerceiveException, NoEnvironmentException {
        return null; //TODO
    }

    @Override
    protected boolean isSupportedByEnvironment(Action action) {
        return false; //TODO
    }

    @Override
    protected boolean isSupportedByType(Action action, String type) {
        return false; //TODO
    }

    @Override
    protected boolean isSupportedByEntity(Action action, String entity) {
        return supportedActions.contains(action.getName());
    }

    @Override
    protected Percept performEntityAction(String entity, Action action) throws ActException {
        return null; //TODO
    }
}
