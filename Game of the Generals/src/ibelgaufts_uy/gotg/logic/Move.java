package ibelgaufts_uy.gotg.logic;

import java.util.List;

/**
 * Abstract class for Game moves
 */
public abstract class Move {
	private double probability = 1;

	/**
	 * Return a value indicating whether this move has multiple possible outcomes.
	 */
	public boolean isStochastic() {
		return false;
	}

	/**
	 * Return a List of possible outcomes for this Move. Only used if isStochastic() returns true.
	 */
	public List<Move> getOutcomes() {
		return null;
	}
	
	public void setProbability(double probability) {
		this.probability = probability;
	}
	
	/**
	 * Return the probability of this outcome as a double in the range [0, 1].
	 */
	public double getProbability() {
		return probability;
	}
	
	public abstract boolean isEqual(Move move);
}