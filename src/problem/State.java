package problem;

import java.util.*;

/**
 * Created by Dom on 19/10/2015.
 *
 * The state class represents a state in a graph, a list of items currently stocked in the
 * fridge before each purchase action.
 */
public class State {
    // Global Variables
    private double C = Math.sqrt(2.0);  // Constant used for calculation
    private List<Integer> state;        // The current state of the fridge
    private int timesVisited;           // The amount of times this state has been visited
    private int reward;                 // The last reward gotten from reaching this state
    private List<List<Integer>> unvisited;
    private List<Link> actions;         // A mapping of each action that can be taken to estimate of failure
    private ProblemSpec spec;           // The problem spec, holding all information of probability and fridge

    /**
     * Constructor method for the State class.
     * @param state
     *          The state of the fridge, a list of items currently in the fridge
     * @param actions
     *          The list of every action that can be taken before reduction
     * @param spec
     *          The problem spec, holding all the information about the fridge
     * @throws NullPointerException
     *          When any input is null
     * @throws IllegalArgumentException
     *          If there was a mismatch with classes
     */
    public State(List<Integer> state, Set<List<Integer>> actions, ProblemSpec spec)
            throws NullPointerException, IllegalArgumentException {
        if (state == null) throw new NullPointerException("state was null");
        if (actions == null) throw new NullPointerException("actions was null");
        for (List<Integer> action : actions) if (action == null)
            throw new NullPointerException("One of the actions was null");
        if (spec == null) throw new NullPointerException("spec was null");
        try {
            this.state = state;
            timesVisited = 0;
            reward = 0;
            this.spec = spec;
            this.actions = new ArrayList<>();
            this.unvisited = new ArrayList<>();
            generateLinks(actions);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("There was a mismatch with one of your classes");
        }
    }

    /**
     * A method to return the state of the fridge
     * @return the state of the fridge
     */
    public List<Integer> getState() { return state; }

    /**
     * Increments the amount of times this node has been reached during the search
     */
    public void visit() { timesVisited++; }

    /**
     * Sets the most recent reward (failures) received for reaching this node
     * @param reward
     *          The amount of failures received
     */
    public void setReward(int reward) { this.reward = reward; }

    /**
     * A method to return the most recently received reward for reaching this node
     * @return the reward for reaching this node
     */
    public int getReward() { return reward; }

    /**
     * A method to update a link in the list of potential actions
     * @param link
     *          The link that is being updated
     */
    public void addLink(Link link) { actions.add(link); }

    /**
     * Takes a list of actions, filters out all actions that cannot be applied to the current state and
     * turns all the remaining actions into Links, adding them to the list of actions for this state
     * @param actions
     *          The total list of actions any state might be able to perform
     * @throws NullPointerException
     *          If any input is null
     * @throws IllegalArgumentException
     *          If any class is mismatched
     */
    private void generateLinks(Set<List<Integer>> actions) throws NullPointerException, IllegalArgumentException {
        for (List<Integer> action : actions) {
            // If the action can be performed, make it into a link and add it to the list of actions
            if (actionApplies(action)) {
                unvisited.add(action);
                this.actions.add(new Link(this, action));
            }
        }
    }

    /**
     * This method takes an action from the list and sees if this state can perform it
     * @param action
     *          An actions from the list of all possible actions
     * @return true if the action can be performed by this state, otherwise false
     */
    private Boolean actionApplies(List<Integer> action) {
        Fridge fridge = spec.getFridge();
        int totalItems = 0;
        int optimalAmount = Math.min(fridge.getMaxPurchase(), fridge.getCapacity() - totalItems(state));
        if (optimalAmount != totalItems(action)) return false;
        // Check if the action raises an item to over the max that can be eaten
        // Check if the action raises total amount of items to over the fridge capacity
        for (int i = 0; i < action.size(); i++) {
            if (action.get(i)+state.get(i) > fridge.getMaxItemsPerType()) {
                return false;
            }
            totalItems += (action.get(i) + state.get(i));
        }
        return totalItems <= fridge.getCapacity();
    }

    public Boolean allVisited() { return unvisited.isEmpty(); }

    public List<Integer> getUnvisited() {
        int random = (int)Math.floor(Math.random()*(double)unvisited.size());
        if (random >= unvisited.size()) random = 0;
        List<Integer> unvisitedState = unvisited.get(random);
        unvisited.remove(random);
        return unvisitedState;
    }

    public List<Integer> randomAction() {
        int random = (int)Math.floor(Math.random()*((double)actions.size()));
        if (random >= actions.size()+unvisited.size()) random = 0;
        return actions.get(random).getAction();
    }

//    public List<Integer> greedy

    public Link bestAction() {
        Link bestLink = null;
        Double bestLinkScore = null;
        for (Link action : actions) {
            double ns = Math.log(timesVisited);
            double nsa = action.getTimesTaken();
            double div = ns/nsa;
            double square = Math.sqrt(div);
            double currentScore = action.getLinkReward() - C * square;
            if (bestLink == null) {
                bestLink = action;
                bestLinkScore = currentScore;
            }
            if (currentScore < bestLinkScore) {
                bestLink = action;
                bestLinkScore = currentScore;
            }
        }
        return bestLink;
    }

    private int totalItems(List<Integer> action) {
        int fullness = 0;
        for (int i : action) fullness += i;
        return fullness;
    }

    @Override
    public boolean equals(Object o) {
        if (o.getClass() != this.getClass()) return false;
        State compareState = (State)o;
        return equals(compareState.getState());
    }

    public boolean equals(List<Integer> state) {
        for (int i = 0; i < getState().size(); i++) {
            if (!Objects.equals(getState().get(i), state.get(i))) return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int easyInt = 0;
        for (int i = 0; i < this.getState().size(); i++) {
            easyInt += this.getState().get(i)*(10^i);
        }
        return easyInt;
    }

    public List<Integer> greedyAction() {
        List<Integer> emptyState = new ArrayList<>();
        for (int i = 0; i < spec.getFridge().getMaxTypes(); i++) emptyState.add(0);
        List<Integer> currentBest = emptyState;
        double currentBestPenalty = calculateFailure(emptyState);
        for (List<Integer> current : unvisited) {
            int currentPenalty = calculateFailure(current);
            if (calculateFailure(current) < currentBestPenalty) {
                currentBest = current;
                currentBestPenalty = currentPenalty;
            }
        }
        for (Link currentLink : actions) {
            List<Integer> current = currentLink.getAction();
            int currentPenalty = calculateFailure(current);
            if (calculateFailure(current) < currentBestPenalty) {
                currentBest = current;
                currentBestPenalty = currentPenalty;
            }
        }
        return currentBest;
    }

    private int calculateFailure(List<Integer> action) {
        int totalFailure = 0;
        List<Integer> state = buyShit(action);
        for (int i = 0; i < state.size()-1; i++) {
            int amount = state.get(i);
            List<Double> probs = spec.getProbabilities().get(i).getRow(amount);
            Double failure = 0.0;
            for (int d = probs.size()-1; d > amount; d--) {
                failure += probs.get(d)*(d-amount);
            }
            totalFailure += failure;
        }
        return totalFailure;
    }

    private List<Integer> buyShit(List<Integer> action) {
        List<Integer> pseudo = new ArrayList<>();
        for (int i = 0; i < action.size(); i++) pseudo.add(action.get(i) + state.get(i));
        return pseudo;
    }
}