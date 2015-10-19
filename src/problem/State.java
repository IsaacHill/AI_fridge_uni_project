package problem;

import java.io.IOException;
import java.util.*;

/**
 * Created by Dom on 19/10/2015.
 */
public class State {
    private List<Integer> state;
    private int reward = 0;
    private ProblemSpec spec;
    private Map<List<Integer>,Double> actions = null;
    private int timesVisited;
    private Double totalFail = 0.0;


    public State(List<Integer> state, ProblemSpec spec) throws IOException {
        this.state = state;
        this.spec = spec;
        // TODO: List of pseudo states available to go to

    }

    public List<Integer> getState() {
        return state;
    }

    public int getReward() {
        return reward;
    }

    public void setReward(int reward) { this.reward = reward; }
    //public Array

    private void calculateFailure() {
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

}