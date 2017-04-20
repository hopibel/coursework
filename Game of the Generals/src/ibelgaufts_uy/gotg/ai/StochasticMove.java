package ibelgaufts_uy.gotg.ai;

import java.util.List;

import ibelgaufts_uy.gotg.logic.GGMove;
import ibelgaufts_uy.gotg.logic.Move;

public class StochasticMove extends GGMove {
	public double probability = 1;
	List<Move> outcomes;

	public StochasticMove(GGMove move) {
		super(move, Outcome.Attacker);
	}

	@Override
	public boolean isStochastic() {
		return true;
	}

	@Override
	public List<Move> getOutcomes() {
		return outcomes;
	}
	
	public void setOutcomes(List<Move> outcomes) {
		this.outcomes = outcomes;
	}

	@Override
	public double getProbability() {
		return probability;
	}

	public void setProbability(double probability) {
		this.probability = probability;
	}
}