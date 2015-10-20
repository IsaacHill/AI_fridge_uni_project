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
    private double C = Math.sqrt(2.0); // TODO: change c value, stop taking shit from you fuckers
    private List<Integer> state;        // The current state of the fridge
    private int timesVisited;           // The amount of times this state has been visited
    private int reward;                 // The last reward gotten from reaching this state
    private Stack<List<Integer>> unvisted;
    private Map<Link, Double> actions;  // A mapping of each action that can be taken to estimate of failure
    private ProblemSpec spec;           // The problem spec, holding all information of probability and fridge
    //private Double totalFail;

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
            this.actions = new HashMap<>();
            this.unvisted = new Stack<>();
            generateLinks(actions);
            //totalFail = 0.0;
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
     * A method to return the amount of times this node has been reached
     * @return the amount of times this node has been reached
     */
    public int getTimesVisited() { return timesVisited; }

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
     * @param reward
     *          The new estimated reward for taking this path
     */
    public void updateLink(Link link, Double reward) { actions.put(link, reward); }

    public Double linkReward(Link link) {
        return actions.get(link);
    }
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
               // System.out.println("serious?");
                unvisted.push(action);
                //this.actions.put(new Link(this, action), -1.0);
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
       /** System.out.println("+============+");
        System.out.println("action state " + action.toString());
        System.out.println("state state " + state.toString());
        System.out.println("size = " +fridge.getMaxItemsPerType() + " or " +spec.getFridge().getMaxItemsPerType()); **/
        // Check if the action raises an item to over the max that can be eaten
        // Check if the action raises total amount of items to over the fridge capacity
        for (int i = 0; i < action.size()-1; i++) {
            if (action.get(i)+state.get(i) > fridge.getMaxItemsPerType()) {
             //   System.out.println("WHY");
                return false;
            }
            totalItems += (action.get(i) + state.get(i));
        }
        if (totalItems > fridge.getCapacity()) return false;
        return true;
    }


    public Stack<List<Integer>> getUnvisted() {
        return unvisted;
    }

    public Link bestAction() {
        Link bestLink = null;
        Double bestLinkScore = null;
        for (Link action : actions.keySet()) {
            Double currentScore = actions.get(action) + (C * Math.sqrt(((Math.log(timesVisited))/action.getTimesTaken())));
            //System.out.println(actions.get(action));
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