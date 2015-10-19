package problem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Isaac-macPro on 19/10/15.
 */
public class Link {

    // Global Variables
    private State fromState;
    private List<Integer> action;
    private int numDone;
    private List<State> toStates;

    public Link(State fromState, List<Integer> action) throws IOException {
        if (fromState == null) throw new IOException("fromState was null");
        if (action == null) throw new IOException("action was null");
        try {
            this.fromState = fromState;
            this.action = cloneAction(action);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("fromState was " + fromState.getClass() + " instead of State");
        }
        numDone = 0;
        toStates = new ArrayList<>();
    }

    public List<Integer> getAction() { return cloneAction(action); }

    public void actionTaken() { numDone++; }

    public int getTimesTaken() { return numDone; }

    public List<State> getNextStates() { return toStates; }

    public void addNextState(State s) { toStates.add(s); }

    private List<Integer> cloneAction(List<Integer> action) {
        List<Integer> newAction = new ArrayList<>();
        for (int i : action) {
            newAction.add(i);
        }
        return newAction;
    }
}
