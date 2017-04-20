package ibelgaufts_uy.gotg.ai;

import ibelgaufts_uy.gotg.logic.Piece;

public class StochasticPiece {
	static final double p = 1.0/21.0;
	double[] probabilities = {p, 6*p, p, p, p, p, p, p, p, p, p, p, p, p, 2*p};
	Piece piece;

	public StochasticPiece(Piece piece) {
		this.piece = new Piece(piece);
	}

	public StochasticPiece(StochasticPiece piece) {
		for(int i = 0; i < probabilities.length; ++i) {
			probabilities[i] = piece.probabilities[i];
		}
		this.piece = new Piece(piece.piece);
	}
}