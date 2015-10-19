package problem;

import java.io.IOException;
import java.util.*;

/**
 * Created by Dom on 19/10/2015.
 */
public class State {
    private List<Integer> state;
    private int timesVisited;
    private int reward;
    private Map<Link, Double> actions;
    private ProblemSpec spec;
    //private Double totalFail;


    public State(List<Integer> state, Set<List<Integer>> actions, ProblemSpec spec) throws NullPointerException {
        if (state == null) throw new NullPointerException("state was null");
        if (actions == null) throw new NullPointerException("actions was null");
        for (List<Integer> action : actions) if (action == null)
            throw new NullPointerException("One of the actions was null");
        if (spec == null) throw new NullPointerException("spec was null");
        try {
            this.state = state;
            timesVisited = 0;
            reward = 0;
            this.actions = new HashMap<>();
            generateLinks(actions);
            this.spec = spec;
            //totalFail = 0.0;
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("There was a mismatch with one of your classes");
        }
    }

    public List<Integer> getState() { return state; }

    public void visit() { timesVisited++; }

    public int getTimesVisited() { return timesVisited; }

    public void setReward(int reward) { this.reward = reward; }

    public int getReward() { return reward; }

    private void generateLinks(Set<List<Integer>> actions) throws NullPointerException {
        for (List<Integer> action : actions) {
            if (actionApplies(action)) {
                this.actions.put(new Link(this, action), 0.0);
            }
        }
    }

    private Boolean actionApplies(List<Integer> action) {
        Fridge fridge = spec.getFridge();
        int totalItems = 0;
        for (int i = 0; i < action.size()-1; i++) {
            if (action.get(i)+state.get(i) > fridge.getMaxItemsPerType()) return false;
            totalItems += (action.get(i) + state.get(i));
        }
        if (totalItems > fridge.getCapacity()) return false;
        return true;
    }

    // Part of the useless section. Useful only for greedy searching
    // To renew, uncomment following commented out code
    // (1 in globals, 1 in constructor and below two methods)
    /*private void calculateFailure() {
        for (int i = 0; i < state.size()-1; i++) {
            int amount = state.get(i);
            List<Double> probs = spec.getProbabilities().get(i).getRow(amount);
            Double failure = 0.0;
            for (int d = probs.size()-1; d > amount; d--) {
                failure += probs.get(d)*(d-amount);
            }
            totalFail += failure;
        }
    }

    public Double getTotalFailure() { return totalFail; }
    */
}