package problem;

import java.io.IOException;
import java.util.List;

/**
 * Created by Dom on 19/10/2015.
 */
public class State {
    private List<Integer> state;
    private Double totalFail = 0.0;

    public State(List<Integer> state, ProblemSpec spec) throws IOException {
        this.state = state;
        List<Matrix> probabilities = spec.getProbabilities();
        for (int i = 0; i < state.size()-1; i++) {
            int amount = state.get(i);
            List<Double> probs = probabilities.get(i).getRow(amount);
            Double failure = 0.0;
            for (int d = probs.size()-1; d > amount; d--) {
                failure += probs.get(d)*(d-amount);
            }
            totalFail += failure;
        }
        // TODO: Current state chance of failure?
        // TODO: List of pseudo states available to go to
        // TODO: List of integers to signify what the state is
        // TODO: Save the problemspec as well, for convinience? Nah, fuck that.
    }
}
