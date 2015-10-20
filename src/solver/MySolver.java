package solver;

import problem.*;

import java.io.IOException;
import java.util.*;

// TODO: Figure out the problems
// TODO: Dom's suggestion: Make a list of all states
// 		TODO: Whenever a new state is made, check the list before making it
//		TODO: Whenever a new link is made, check it's fromState's actions before making it
// TODO: Probably won't fix everything.
// TODO: The current problem: We are getting null rewards and infinite other stuff

public class MySolver implements OrderingAgent {

	private ProblemSpec spec;
	// private List<Matrix> probabilities;
	// private List<Integer> action = new ArrayList<>();
	private int weeksPassed;
	private Set<List<Integer>> allActions;
	private Simulator mySim;
	final private double THRESHOLD = 0.7;
	//private double current;
	
	public MySolver(ProblemSpec spec) throws IOException {
		this.spec = spec;
	}

	public void doOfflineComputation() {
		allActions = createActions();
		weeksPassed = 0;
		mySim = new Simulator(spec);
	}

	/*public void doOnlineComputation() {
		double current = System.currentTimeMillis();
		createActions();

		double start = System.currentTimeMillis();
		while (current < start+50000) {
			MCST(first);
		}
		//generateShoppingList();

	}*/

		//TODO look at this more tomorrow god damn it im tired
	public List<Integer> generateShoppingList(List<Integer> inventory,
	        int numWeeksLeft) {

		State current = new State(inventory, allActions, spec);
		System.out.println("Time for some MCST with " + current.getState().toString());
		System.out.println("He has a list to look through of " + current.getAllUnvisited());
		return MCST(current);


		/**
		// Example code that buys one of each item type.
        // TODO Replace this with your own code.
		List<Integer> shopping = new ArrayList<Integer>();
		int totalItems = 0;
		for (int i : inventory) {
			totalItems += i;
		}

		int totalShopping = 0;
		for (int i = 0; i < fridge.getMaxTypes(); i++) {
			if (totalItems >= fridge.getCapacity() ||
			        totalShopping >= fridge.getMaxPurchase()) {
				shopping.add(0);
			} else {
				shopping.add(1);
				totalShopping ++;
				totalItems ++;
			}
		}
		return shopping; **/
	}




	/*
	 * Just doing some maths
	 */


	// TODO: add an action
	private List<Integer> MCST(State state) {
		double startTime = System.currentTimeMillis();
		while (!outOfTime(startTime)) {
			search(state, 0);
			/*for (Link link : state.getActions().keySet()) {
				System.out.println("This is the action: " + link.getAction() + " and this is the reward " + state.getActions().get(link.getAction()));
			}*/
		}
		// TODO: make a getter and setter - shots not (isaac)
		System.out.println("------START------");
		System.out.println(state.bestAction().getAction().toString());
		System.out.println(state.getActions().get(state.bestAction()));
		System.out.println("-------END-------");
		return state.bestAction().getAction();
	}

	private double search(State state, int depth) {
		//TODO: make this number good as shit
		if (Math.pow(spec.getDiscountFactor(), depth) < THRESHOLD) {
			return -1.0;
		}
		if (terminal(depth)) {
			return state.getReward();
		}
		if (!state.allVisited()) {
			List<Integer> action = state.getUnvisited();
			state.updateLink(new Link(state, action), estimate(state, action));
			return -1.0;
		} else {
			Link action = state.bestAction();
			State newState = simulateAction(state, action.getAction());
			Double searched = search(newState, depth + 1);
			Double q;
			if (searched < 0) {
				q = (double) state.getReward();
			} else {
				q = state.getReward() + spec.getDiscountFactor() * searched;
			}
			updateValue(state, action, q);
			action.addNextState(newState);




		}
		//dom is an idiot tho...
		return 0;
	}

	private boolean terminal(int depth) {
		return depth + weeksPassed >= spec.getNumWeeks();
	}

	private boolean outOfTime(double time) {
		if (System.currentTimeMillis()-time > 10000) {
			return true;
		}
		return false;
	}

/*
	private State piUCT(State state) {
		// page 23 of 14/10 slides

		// for each action, calculate argmax

		double currentMax = 0;
		State currentMaxState = null;

		for (List<Integer> action : state.createActions().keySet()) {
			// calculate pi, update current max state
		}

		return currentMaxState;

	}
	*/

	public Set<List<Integer>> createActions() {
		Set<List<Integer>> allActions = new HashSet<>();
		List<Integer> initial = new ArrayList<>
				(spec.getFridge().getMaxTypes());
		for (int i = 0; i < spec.getFridge().getMaxTypes(); i++) {
			initial.add(i, 0);
		}
		actionsHelper(allActions, 0, initial);
		System.out.println("print stuiff" + allActions.toString());
		return allActions;
	}

	private void actionsHelper(Set<List<Integer>> allActions, int ordered,
						  List<Integer> current) {
		int total = 0;

		if (ordered > spec.getFridge().getMaxPurchase()) {
			return;
		}
		//checks action is not already added to allActions
		if (allActions.contains(current)) {
			return;
		}
		//checks that fridge is under capacity
		for (int type: current) {
			total += type;
			if (total > spec.getFridge().getCapacity()) {
				return;
			}
		}
		allActions.add(current);
		for (int i = 0; i < spec.getFridge().getMaxTypes(); i++) {
			if (current.get(i) >=  spec.getFridge().getMaxItemsPerType()) {
				continue;
			}
			List<Integer> newCurrent = new LinkedList<>(current);
			newCurrent.remove(i);
			newCurrent.add(i, current.get(i) + 1);
			actionsHelper(allActions, ordered + 1, newCurrent);


		}
	}

	private double estimate(State state, List<Integer> action) {
		return estimateHelper(state, action, 0);
	}

	private Double estimateHelper(State state, List<Integer> action, int depth) {
		State nextState = simulateAction(state, action);
		if (Math.pow(spec.getDiscountFactor(), depth) < THRESHOLD || terminal(depth)) {
			return (double) state.getReward();
		} else {
			if (state.allVisited()) {
				// TODO: grab a random action that has already been looked at.
			}
			List<Integer> nextAction = nextState.peekUnvisited();
			return (double)state.getReward() + spec.getDiscountFactor()*estimateHelper(nextState, nextAction, depth+1);
		}
	}

	private State simulateAction(State currentState, List<Integer> action) {
		List<Integer> newState = new ArrayList<>();
		for (int i = 0; i < spec.getFridge().getMaxTypes(); i++) {
			newState.add(currentState.getState().get(i) + action.get(i));
		}
		List<Integer> want = mySim.sampleUserWants(newState);
		List<Integer> nextState = new ArrayList<>();
		int penalty = 0;
		for (int i = 0; i < newState.size(); i++) {
			if (newState.get(i) >= want.get(i)) {
				nextState.add(newState.get(i)-want.get(i));
			} else {
				nextState.add(0);
				penalty += (want.get(i)-newState.get(i));
			}
		}
		State simulatedState = new State(nextState, allActions, spec);
		simulatedState.setReward(penalty);
		return simulatedState;

	}

	private void updateValue(State state, Link action, Double q) {
		System.out.println();
		Double newReward = (state.linkReward(action)*action.getTimesTaken()+q)/action.getTimesTaken()+1;
		state.updateLink(action, newReward);
		state.visit();
		action.actionTaken();
	}

}
