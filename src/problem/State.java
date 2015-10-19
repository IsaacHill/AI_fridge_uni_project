package problem;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Dom on 19/10/2015.
 */
public class State {
    private List<Integer> state;
    private List<Matrix> probabilities = null;
    private Double totalFail = 0.0;
    private int reward = 0;
    private ProblemSpec spec;
    private Map<List<Integer>,Double> actions = null;
    private int timesVisited;

    public State(List<Integer> state, ProblemSpec spec) throws IOException {
        this.state = state;
        this.spec = spec;
        this.probabilities = spec.getProbabilities();
        // TODO: List of pseudo states available to go to

    }

    public void calculateFailure() {
        for (int i = 0; i < state.size()-1; i++) {
            int amount = state.get(i);
            List<Double> probs = probabilities.get(i).getRow(amount);
            Double failure = 0.0;
            for (int d = probs.size()-1; d > amount; d--) {
                failure += probs.get(d)*(d-amount);
            }
            totalFail += failure;
        }

    }

    public List<Integer> getState() {
        return state;
    }

    public int getReward() {
        return reward;
    }

    public void setReward(int reward) {
        this.reward = reward;
    }

    public HashMap<List<Integer>, Double> createActions() {
        int everyType = spec.getFridge().getMaxTypes();
        int maxType = spec.getFridge().getMaxItemsPerType();
        List<Integer> current = getState();
        for (Integer type : current) {
            //fwaur
        }


    }
    //public Array
}