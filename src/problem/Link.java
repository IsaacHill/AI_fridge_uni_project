package problem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Isaac-macPro on 19/10/15.
 */
public class Link {

    // Global Variables
    private State fromState;        // The State that the Link comes from
    private List<Integer> action;   // The action that the Link represents
    private int numDone;            // The number of times this Link has been explored
    private double linkReward;

    /**
     * Constructor method for the Link class
     *
     * @param fromState
     *          The State that Link starts from
     * @param action
     *          The action (Amount of groceries to purchase) that the link represents
     * @throws NullPointerException
     *          If any input is null
     * @throws IllegalArgumentException
     *          If any input is the incorrect class
     */
    public Link(State fromState, List<Integer> action) throws NullPointerException, IllegalArgumentException {
        if (fromState == null) throw new NullPointerException("fromState was null");
        if (action == null) throw new NullPointerException("action was null");
        try {
            this.fromState = fromState;
            this.action = cloneAction(action);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("fromState was " + fromState.getClass() + " instead of State");
        }
        numDone = 0;
        linkReward = 0.0;
    }

    /**
     * A method to clone and return the action that the Link represents
     * @return the action that the link represents
     */
    public List<Integer> getAction() { return cloneAction(action); }

    /**
     * A method to increment the amount of times this link is traversed
     */
    public void actionTaken() { numDone++; }

    /**
     * A method to return the amount of times this link has been traversed
     * @return the amount of times this link has been traversed
     */
    public int getTimesTaken() { return numDone; }

    public void setLinkReward(double d) { linkReward = d; }

    public double getLinkReward() { return linkReward; }

    /**
     * A method to clone the action passed in and out of it
     * @param action
     *          An action to represent groceries purchased by the fridge
     * @return a clone of the action
     */
    private List<Integer> cloneAction(List<Integer> action) {
        List<Integer> newAction = new ArrayList<>();
        for (int i : action) {
            newAction.add(i);
        }
        return newAction;
    }
}
