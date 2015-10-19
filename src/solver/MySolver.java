package solver;

import problem.Fridge;
import problem.Matrix;
import problem.ProblemSpec;
import problem.State;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MySolver implements OrderingAgent {
	
	private ProblemSpec spec = new ProblemSpec();
	private Fridge fridge;
    private List<Matrix> probabilities;
	private List<Integer> action = new ArrayList<>();
	private int weeksPassed = 0;
	
	public MySolver(ProblemSpec spec) throws IOException {
	    this.spec = spec;
		fridge = spec.getFridge();
        probabilities = spec.getProbabilities();
	}
	
	public void doOfflineComputation() {
	    // TODO Write your own code here.
	}
	
	public List<Integer> generateShoppingList(List<Integer> inventory,
	        int numWeeksLeft) {
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
		return shopping;
	}
	/*
	 * Just doing some maths
	 */

	private double failChance(List<Integer> inventory) {
		/*
		 * TODO: Look at how to get chances from the matrices.
		 * TODO: Learn the code before Isaac gets mad.
		 */
		return 0;
	}
	// TODO: add an action
	private List<Integer> MCST(State state , int ammount) {
		double startTime = System.currentTimeMillis();
		while (!outOfTime(startTime)) {
			Search(state, 0);
		}
		// TODO: make a getter and setter - shots not (isaac)
		return action;
	}

	private double Search(State state, int depth) {
		//TODO: make this number good as shit
		if (Math.pow(spec.getDiscountFactor(), depth) < 0.1) {
			return -1.0;
		}
		if (Terminal(depth)) {
			return state.getReward();
		}
		//if ()
		return 0;
	}

	private boolean Terminal(int depth) {
		if (depth+weeksPassed >= spec.getNumWeeks()) {
			return true;
		} else {
			return false;
		}
	}

	private boolean outOfTime(double time) {
		if (System.currentTimeMillis()-time > 30000) {
			return true;
		}
		return false;
	}
}
