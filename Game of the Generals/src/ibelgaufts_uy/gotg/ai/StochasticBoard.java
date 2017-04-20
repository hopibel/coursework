package ibelgaufts_uy.gotg.ai;

import ibelgaufts_uy.gotg.logic.GGBoard;
import ibelgaufts_uy.gotg.logic.GGMove;
import ibelgaufts_uy.gotg.logic.GGMove.Outcome;
import ibelgaufts_uy.gotg.logic.IllegalMoveException;
import ibelgaufts_uy.gotg.logic.Move;
import ibelgaufts_uy.gotg.logic.Piece;
import ibelgaufts_uy.gotg.logic.State;
import ibelgaufts_uy.gotg.logic.Piece.Rank;

import java.util.ArrayList;
import java.util.List;

public class StochasticBoard implements State {
	private StochasticPiece[][] board = new StochasticPiece[8][9];
	private int[] pieceCounts = {1, 6, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2}; // Rank.ordinal() == index
	private List<StochasticPiece> white = new ArrayList<>();
	private List<StochasticPiece> black = new ArrayList<>();

	private Piece.Color currentPlayer = Piece.Color.White;
	private boolean ready1, ready2, finished;
	private Piece.Color winner = Piece.Color.Grey;
	private double probability = 1.0;

	private StochasticBoard() {}

	public StochasticBoard(Piece[][] pieces) {
		if (pieces.length != 8 || pieces[0].length != 9) {
			throw new RuntimeException("Board not properly initialized");
		}

		for (int i = 0; i < pieces.length; ++i) {
			for (int j = 0; j < pieces[0].length; ++j) {
				if (pieces[i][j].getRank() == Rank.Empty) continue;
				StochasticPiece p = new StochasticPiece(pieces[i][j]);
				board[i][j] = p;
				if (p.piece.getColor() == Piece.Color.White) {
					white.add(p);
				} else {
					black.add(p);
				}
			}
		}
	}

	public GGBoard generateBoard() {
		Piece[][] newboard = new Piece[8][9];
		List<StochasticPiece> pieces = white.get(0).piece.getRank() == Rank.Unknown ? white : black;
		int[] counts = pieceCounts.clone();

		for (StochasticPiece p : pieces) {
			double total = 0;
			for (double weight : p.probabilities) {
				total += weight;
			}

			int index;
			while (true) {
				index = -1;
				double random = Math.random() * total;
				for (int i = 0; i < p.probabilities.length; ++i) {
					random -= p.probabilities[i];
					if (random <= 0) {
						index = i;
						break;
					}
				}
				if (counts[index] > 0) {
					counts[index] -= 1;
					break;
				}
			}
			newboard[p.piece.getRow()][p.piece.getColumn()] = new Piece(p.piece);
			newboard[p.piece.getRow()][p.piece.getColumn()].setRank(Rank.values[index]);
		}

		pieces = white.get(0).piece.getRank() == Rank.Unknown ? black : white;
		for (StochasticPiece p : pieces) {
			newboard[p.piece.getRow()][p.piece.getColumn()] = new Piece(p.piece);
		}

		return new GGBoard(newboard);
	}

	@Override
	public List<? extends Move> getMoves() {
		List<Piece> pieces = new ArrayList<>();
		for (int i = 0; i < 8; ++i) {
			for (int j = 0; j < 9; ++j) {
				if (board[i][j].piece.getColor() == currentPlayer) {
					pieces.add(board[i][j].piece);
				}
			}
		}
		
		List<GGMove> moves = new ArrayList<>();
		for (Piece p : pieces) {
			int r = p.getRow();
			int c = p.getColumn();
			moves.add(new GGMove(r, c, r+1, c));
			moves.add(new GGMove(r, c, r, c+1));
			moves.add(new GGMove(r, c, r-1, c));
			moves.add(new GGMove(r, c, r, c-1));
		}
		List<GGMove> legal = new ArrayList<>();
		for (GGMove m : moves) {
			if (isValid(m)) {
				if (board[m.getTargetRow()][m.getTargetCol()] != null) {
					StochasticMove s = new StochasticMove(m);
					s.setOutcomes(generateOutcomes(s));
					legal.add(s);
				} else {
					legal.add(m);
				}
			}
		}

		return legal;
	}

	private List<Move> generateOutcomes(StochasticMove s) {
		List<Move> moves = new ArrayList<>();

		int srow = s.getSourceRow();
		int scol = s.getSourceCol();
		int trow = s.getTargetRow();
		int tcol = s.getTargetCol();

		GGMove move = new GGMove(s, Outcome.Attacker);
		if (board[trow][tcol] != null) {
			if (board[srow][scol].piece.getRank() == Rank.Private) {
				move.setProbability(board[trow][tcol].probabilities[Rank.Spy.ordinal()]);
			} else if (board[srow][scol].piece.getRank() == Rank.Spy) {
				move.setProbability(1 - board[trow][tcol].probabilities[Rank.Private.ordinal()]);
			} else {
				double prob = 0;
				for (int i = 0; i < board[srow][scol].piece.getRank().ordinal(); ++i) {
					prob += board[trow][tcol].probabilities[i];
				}
				move.setProbability(prob);
			}
		}
		if (move.getProbability() > 0) moves.add(move);

		move = new GGMove(s, Outcome.Defender);
		if (board[trow][tcol] != null) {
			if (board[srow][scol].piece.getRank() == Rank.Private) {
				double prob = 1 - board[trow][tcol].probabilities[Rank.Spy.ordinal()];
				prob -= board[trow][tcol].probabilities[Rank.Flag.ordinal()];
				move.setProbability(prob);
			} else if (board[srow][scol].piece.getRank() == Rank.Spy) {
				move.setProbability(board[trow][tcol].probabilities[Rank.Private.ordinal()]);
			} else {
				double prob = 0;
				for (int i = board[srow][scol].piece.getRank().ordinal(); i < board[trow][tcol].probabilities.length; ++i) {
					prob += board[trow][tcol].probabilities[i];
				}
				move.setProbability(prob);
			}

			if (move.getProbability() > 0) moves.add(move);
		}

		move = new GGMove(s, Outcome.Draw);
		if (board[trow][tcol] != null) {
			move.setProbability(board[trow][tcol].probabilities[board[srow][scol].piece.getRank().ordinal()]);

			if (move.getProbability() > 0) moves.add(move);
		}

		return moves;
	}

	@Override
	public void playMove(Move move) throws IllegalMoveException {
		if (!isValid(move)) {
			throw new IllegalMoveException("Illegal move");
		}

		GGMove m = (GGMove) move;
		int srow = m.getSourceRow();
		int scol = m.getSourceCol();
		int trow = m.getTargetRow();
		int tcol = m.getTargetCol();

		StochasticPiece p = board[srow][scol];
		StochasticPiece t = board[trow][tcol];
		board[srow][scol] = null;

		p.piece.setRow(trow);
		p.piece.setColumn(tcol);

		if (t == null) {
			board[trow][tcol] = p;
		} else if (m.getOutcome() == Outcome.Attacker) {
			List<StochasticPiece> pieces = getCurrentPlayer() == 1 ? black : white;
			pieces.remove(board[trow][tcol]);
			updateProbabilities(pieces, m);

			board[trow][tcol] = p;
		} else if (m.getOutcome() == Outcome.Defender) {
			List<StochasticPiece> pieces = getCurrentPlayer() == 1 ? white : black;
			pieces.remove(p);
			pieces = getCurrentPlayer() == 1 ? black : white;
			updateProbabilities(pieces, m);
		} else if (m.getOutcome() == Outcome.Draw) {
			white.remove(p);
			black.remove(p);
			updateProbabilities(getCurrentPlayer() == 1 ? black : white, m);
		}

		currentPlayer = currentPlayer == Piece.Color.White ? Piece.Color.Black : Piece.Color.White;
	}

	private void updateProbabilities(List<StochasticPiece> pieces, GGMove m) {
		switch (m.getOutcome()) {
		case Attacker:
			for (StochasticPiece p : pieces) {
				/*
				 * P(A|Ccap2)=P(Ccap2|A)*P(A)/P(Ccap2)
				 * P(Ccap2|A)=P(Ccap2)/P(A')
				 * P(A')=P(A)-P(A)/#A+P(B)+P(C)...
				 * P(A|Ccap2)=P(A)/P(A')
				 */
				double[] newprob = new double[15];
				for (int i = 0; i < newprob.length; ++i) {
					double prob = 0;
					double not = 0;
					for (int j = 0; j < p.probabilities.length; ++j) {
						if (i == j) {
							prob += p.probabilities[j];
							not += p.probabilities[j]*(1-(1/pieceCounts[j]));
						} else {
							not += p.probabilities[j];
						}
					}
					newprob[i] = prob/not;
				}
				p.probabilities = newprob;
			}
			break;
		case Defender:
			for (StochasticPiece p : pieces) {
				double[] newprob = new double[15];
				for (int i = 0; i < newprob.length; ++i) {
					double not = 0;
					for (int j = 0; j < p.probabilities.length; ++i) {
						if (i == j) {
							not += p.probabilities[j]-(1/(pieces.size()+1));
						} else {
							not += p.probabilities[j];
						}
					}
					newprob[i] = p.probabilities[i]/(pieceCounts[i]*not);
				}
				p.probabilities = newprob;
			}
			break;
		case Draw:
			for (StochasticPiece p : pieces) {
				double[] newprob = new double[15];
				for (int i = 0; i < newprob.length; ++i) {
					double prob = 0;
					double not = 0;
					for (int j = 0; j < p.probabilities.length; ++j) {
						if (i == j) {
							prob += p.probabilities[j];
							not += p.probabilities[j]*(1-(1/pieceCounts[j]));
						} else {
							not += p.probabilities[j];
						}
					}
					newprob[i] = prob/not;
				}
				p.probabilities = newprob;
			}
			break;
		}
	}

	@Override
	public boolean isFinished() {
		return finished;
	}
	
	@Override
	public double getResult(int player) {
		if (!finished) return 0.5; // Count unfinished games as draws
		switch (winner) {
		case White:
			return player == 1 ? 1 : 0;
		case Black:
			return player == 2 ? 1 : 0;
		default :
			return 0.5;
		}
	}
	
	@Override
	public State copy() {
		StochasticBoard g = new StochasticBoard();
		for (int i = 0; i < 8; ++i) {
			for (int j = 0; j < 9; ++i) {
				g.board[i][j] = new StochasticPiece(board[i][j]);
			}
		}
		for (int i = 0; i < pieceCounts.length; ++i) {
			g.pieceCounts[i] = pieceCounts[i];
		}
		for (StochasticPiece p : white) {
			g.white.add(new StochasticPiece(p));
		}
		for (StochasticPiece p : black) {
			g.black.add(new StochasticPiece(p));
		}
		g.currentPlayer = currentPlayer;
		g.ready1 = ready1;
		g.ready2 = ready2;
		g.finished = finished;
		g.winner = winner;
		g.probability = probability;

		return g;
	}

	@Override
	public int getCurrentPlayer() {
		return currentPlayer == Piece.Color.White ? 1 : 2;
	}

	@Override
	public int getPreviousPlayer() {
		return 3 - getCurrentPlayer();
	}

	public boolean isValid(Move move) {
		if (!(move instanceof GGMove)) return false;
		GGMove m = (GGMove) move;

		Piece source = board[m.getSourceRow()][m.getSourceCol()].piece;
		Piece target = board[m.getTargetRow()][m.getTargetCol()].piece;
		if (source == null
			|| m.getSourceRow() < 0
			|| m.getSourceRow() > 7
			|| m.getSourceCol() < 0
			|| m.getSourceCol() > 8
			|| m.getTargetRow() < 0
			|| m.getTargetRow() > 7
			|| m.getTargetCol() < 0
			|| m.getTargetCol() > 8
			|| source.getColor() != currentPlayer
			|| target.getColor() == currentPlayer) {
			return false;
		}
	
		return true;
	}
}