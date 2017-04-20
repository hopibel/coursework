package ibelgaufts_uy.gotg.logic;

import java.util.ArrayList;
import java.util.List;

public class GGMove extends Move {
	private int sourceRow, sourceCol;
	private int targetRow, targetCol;
	
	public enum Outcome {
		Attacker, Defender, Draw
	}
	
	private Outcome outcome;
	private double probability = 1;

	public GGMove() {
		outcome = Outcome.Attacker;
	}

	public GGMove(int sourceRow, int sourceCol, int targetRow, int targetCol) {
		this.sourceRow = sourceRow;
		this.sourceCol = sourceCol;
		this.targetRow = targetRow;
		this.targetCol = targetCol;
		outcome = Outcome.Attacker; // Piece vs Empty counts as Attacker victory
	}

	public GGMove(GGMove m, Outcome outcome) {
		sourceRow = m.getSourceRow();
		sourceCol = m.getSourceCol();
		targetRow = m.getTargetRow();
		targetCol = m.getTargetCol();
		this.outcome = outcome;
	}

	public int getSourceRow() {
		return sourceRow;
	}
	
	public int getSourceCol() {
		return sourceCol;
	}

	public int getTargetRow() {
		return targetRow;
	}
	
	public int getTargetCol() {
		return targetCol;
	}

	public void setOutcome(Outcome outcome) {
		this.outcome = outcome;
	}

	public Outcome getOutcome() {
		return outcome;
	}

	@Override
	public boolean isEqual(Move move) {
		if (!(move instanceof GGMove)) {
			return false;
		}
		GGMove m = (GGMove) move;
		
		if (sourceRow != m.getSourceRow()
			|| sourceCol != m.getSourceCol()
			|| targetRow != m.getTargetRow()
			|| targetCol != m.getTargetCol()) {
			return false;
		} else {
			return true;
		}

	}

//	@Override
//	public List<Move> getOutcomes() {
//		List<Move> outcomes = new ArrayList<>();
//		outcomes.add(new GGMove(this, Outcome.Attacker));
//		outcomes.add(new GGMove(this, Outcome.Defender));
//		outcomes.add(new GGMove(this, Outcome.Draw));
//		return outcomes;
//	}
}