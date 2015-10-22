package solver;

import problem.*;

import java.io.IOException;
import java.util.*;

public class MySolver implements OrderingAgent {

	private ProblemSpec spec;
	private int weeksPassed;
	private Set<List<Integer>> allActions;
	private List<State> states;
	private Simulator mySim;
	final private double GREEDYTHRESH = 0.7;
	final private double THRESHOLD = 0.9;
	final private int TIMEOUT = 55000;
	final private boolean greedy = true;
	final private int SIMULATE_ACTION_MULTIPLE = 100;
	//private double current;
	
	public MySolver(ProblemSpec spec) throws IOException {
		this.spec = spec;
	}

	public void doOfflineComputation() {
		allActions = createActions();
		weeksPassed = 0;
		mySim = new Simulator(spec);
		states = new ArrayList<>();
	}

	public List<Integer> generateShoppingList(List<Integer> inventory, int numWeeksLeft) {
		State current = checker(new State(inventory, allActions, spec));
		return MCST(current);
	}

	private List<Integer> MCST(State state) {
		double startTime = System.currentTimeMillis();
		while (!outOfTime(startTime)) search(state, 0);
		return state.bestAction().getAction();
	}

	private double search(State state, int depth) {
		if (Math.pow(spec.getDiscountFactor(), depth) < THRESHOLD) {
			return state.getReward();
		}
		if (terminal(depth)) {
			return state.getReward();
		}
		if (!state.allVisited()) {
			List<Integer> action = state.getUnvisited();
			Link newLink = new Link(state, action);
			state.addLink(newLink);
			newLink.setLinkReward(estimate(state, action, 0));
			return 0;
		} else {
			Link action = state.bestAction();
			State newState = checker(simulateAction(state, action.getAction())); // !Liam - changed this to call average
			Double searched = search(newState, depth + 1);
			Double q = (double) state.getReward() + spec.getDiscountFactor() * searched;
			updateValue(state, action, q);
			return 0;
		}
	}

	private boolean terminal(int depth) {
		return depth + weeksPassed >= spec.getNumWeeks();
	}

	private boolean outOfTime(double time) {
		return System.currentTimeMillis() - time > TIMEOUT;
	}

	public Set<List<Integer>> createActions() {
		Set<List<Integer>> allActions = new HashSet<>();
		List<Integer> initial = new ArrayList<>(spec.getFridge().getMaxTypes());
		for (int i = 0; i < spec.getFridge().getMaxTypes(); i++) initial.add(i, 0);
		actionsHelper(allActions, 0, initial);
		return allActions;
	}

	private void actionsHelper(Set<List<Integer>> allActions, int ordered,
							   List<Integer> current) {
		int total = 0;
		if (ordered > spec.getFridge().getMaxPurchase()) return;
		//checks action is not already added to allActions
		if (allActions.contains(current)) return;
		//checks that fridge is under capacity
		for (int type: current) {
			total += type;
			if (total > spec.getFridge().getCapacity()) return;
		}
		allActions.add(current);
		for (int i = 0; i < spec.getFridge().getMaxTypes(); i++) {
			if (current.get(i) >=  spec.getFridge().getMaxItemsPerType()) continue;
			List<Integer> newCurrent = new LinkedList<>(current);
			newCurrent.remove(i);
			newCurrent.add(i, current.get(i) + 1);
			actionsHelper(allActions, ordered + 1, newCurrent);
		}
	}

	private double estimate(State state, List<Integer> action, int depth) {
		State nextState = checker(simulateAction(state, action));
		if (Math.pow(spec.getDiscountFactor(), depth) < THRESHOLD || terminal(depth)) return (double) state.getReward();
		else {
			List<Integer> nextAction;
			if (greedy) nextAction = nextState.greedyAction();
			else nextAction = nextState.peekUnvisited();
			return (double)state.getReward() + spec.getDiscountFactor()*estimate(nextState, nextAction, depth+1);
		}
	}

	/**
	 * Simulates action on currentState SIMULATE_ACTION_MULTIPLE times.
	 * @param currentState
	 * @param action
	 * @return
	 */
	private State simulateActionAverage(State currentState, List<Integer> action) {
		Map<State, Integer> statePenalties = new HashMap<>();	// key is state, value is reward

		for (int i = 0; i < SIMULATE_ACTION_MULTIPLE; i++) {
			// get simulated state
			State simulatedState = simulateAction(currentState, action);
			// add simulated state to statePenalties. Average the reward if already in map
			if (statePenalties.containsKey(simulatedState)) {
				// average reward and update
				int currentReward = statePenalties.get(simulatedState);
				int newReward = simulatedState.getReward();
				int avgReward = (currentReward + newReward) / 2;
				statePenalties.replace(simulatedState, avgReward);
			} else {
				statePenalties.put(simulatedState, simulatedState.getReward());
			}
		}
		// have completed all simulations. Now grab the best state
		State currentBestState = null;
		int currentBestReward = Integer.MAX_VALUE;
		for (State state : statePenalties.keySet()) {
			if (state.getReward() < currentBestReward) {
				currentBestState = state;
				currentBestReward = state.getReward();
			}
		}
		return currentBestState;
	}

	/**
	 * simulates action on current state, and updates the re
	 * @param currentState
	 * @param action
	 * @return
	 */
	private State simulateAction(State currentState, List<Integer> action) {
		// copy currentState to newState
		List<Integer> newState = new ArrayList<>();
		for (int i = 0; i < spec.getFridge().getMaxTypes(); i++)
			newState.add(currentState.getState().get(i) + action.get(i));
		// get what user wants
		List<Integer> want = mySim.sampleUserWants(newState);
		List<Integer> nextState = new ArrayList<>(); // the nextState after applying want to newState

		// loop through the state, applying want to newState, and check expected penalty.
		int penalty = 0;
		for (int i = 0; i < newState.size(); i++) {
			if (newState.get(i) >= want.get(i)) {
				// we have what user wanted, set nextState accordingly
				nextState.add(newState.get(i)-want.get(i));
			} else {
				// didn't have what user wanted, add penalty
				nextState.add(0);
				penalty += (want.get(i)-newState.get(i));
			}
		}
		State simulatedState = checker(new State(nextState, allActions, spec));
		simulatedState.setReward(penalty);
		return simulatedState;
	}

	private void updateValue(State state, Link action, Double q) {
		action.setLinkReward((action.getLinkReward() * action.getTimesTaken() + q) / (action.getTimesTaken() + 1));
		state.visit();
		action.actionTaken();
	}

	/**
	 * Checks whether state is in the current list of states.
	 * If state is in states, return state. Otherwise add state to states
	 * 		and return state.
	 * @param state the state to check.
	 * @return	the state.
	 */
	private State checker(State state) {
		int occurrence = states.indexOf(state);
		if (occurrence < 0) states.add(state);
		else return states.get(occurrence);
		return state;
	}

}
