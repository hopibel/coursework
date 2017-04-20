package ibelgaufts_uy.gotg.logic;

import ibelgaufts_uy.gotg.logic.GGMove.Outcome;

import java.util.ArrayList;
import java.util.List;

/**
 * Game of the Generals (GG) board. Smart model, so most of the game logic is contained here.
 */
public class GGBoard implements State {
	private Piece[][] board = new Piece[8][9];
	private Piece.Color currentPlayer = Piece.Color.White;
	private List<Piece> changed = new ArrayList<>();
	private boolean ready1, ready2, finished;
	private Piece.Color winner = Piece.Color.Grey;

	public GGBoard() {
		// Fill board with placeholder pieces
		for (int i = 0; i < 8; ++i) {
			for (int j = 0; j < 9; ++j) {
				board[i][j] = Piece.createEmpty(i, j);
			}
		}
	}
	
	public GGBoard(Piece[][] board) {
		this();
		for (int i = 0; i < board.length; ++i) {
			for (int j = 0; j < board[0].length; ++j) {
				if (board[i][j] == null) continue;
				this.board[i][j] = new Piece(board[i][j]);
			}
		}
	}

	public void placePieces(List<Piece> pieces) throws InvalidPieceException {
		if(pieces.size() > 21) {
			throw new InvalidPieceException("Too many pieces. Expected 21, received " + pieces.size());
		}

		int[] ranks = {1, 2, 6, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1};

		for (Piece p : pieces) {
			int row = p.getRow();
			int col = p.getColumn();

			if (board[row][col].getColor() != Piece.Color.Grey) {
				throw new InvalidPieceException(row + "," + col + " is already occupied");
			}
			
			if ((p.getColor() == Piece.Color.White && row > 2) || (p.getColor() == Piece.Color.Black && row < 5)) {
				throw new InvalidPieceException(row + "," + col + " is not a valid square for player " + (p.getColor() == Piece.Color.White ? 1 : 2));
			}

			if (row < 0 || row > 7 || col < 0 || col > 8) {
				throw new InvalidPieceException(row + "," + col + " is out of bounds");
			}

			--ranks[p.getRank().ordinal()];
		}

		for(int i = 0; i < ranks.length; ++i) {
			if (ranks[i] > 0) {
				throw new InvalidPieceException("Missing " + ranks[i] + " " + Piece.Rank.values[i].toString() + " pieces");
			} else if (ranks[i] < 0) {
				throw new InvalidPieceException("Found " + ranks[i] + " too many " + Piece.Rank.values[i].toString() + " pieces");
			}
		}

		for (Piece p : pieces) {
			board[p.getRow()][p.getColumn()] = p;
			changed.add(p);
		}

		if (pieces.get(0).getColor() == Piece.Color.White) {
			ready1 = true;
		} else {
			ready2 = true;
		}
	}

	@Override
	public List<? extends Move> getMoves() {
		List<Piece> pieces = new ArrayList<>();
		for (int i = 0; i < 8; ++i) {
			for (int j = 0; j < 9; ++j) {
				if (board[i][j].getColor() == currentPlayer) {
					pieces.add(board[i][j]);
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
				legal.add(m);
			}
		}

		return legal;
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

		Piece p = board[srow][scol];
		Piece t = board[trow][tcol];
		board[srow][scol] = Piece.createEmpty(srow, scol);
		changed.add(board[srow][scol]);

		if (t.getRank() == Piece.Rank.Empty
			|| (p.getRank() == Piece.Rank.Private && t.getRank() == Piece.Rank.Spy)
			|| (p.getRank() == Piece.Rank.Spy && t.getRank() != Piece.Rank.Private)
			|| p.getRank().ordinal() > t.getRank().ordinal()) { // Empty or capture
			p.setRow(trow);
			p.setColumn(tcol);
			board[trow][tcol] = p;
			
			if (t.getRank() == Piece.Rank.Flag // Captured flag
				|| (p.getRank() == Piece.Rank.Flag // Reached opposite side of board
					&& ((currentPlayer == Piece.Color.White && trow == 7)
						|| (currentPlayer == Piece.Color.Black && trow == 0)
					)
				)) {
				finished = true;
				winner = currentPlayer;
			}
			changed.add(board[trow][tcol]);
			m.setOutcome(Outcome.Attacker);
		} else if (p.getRank().ordinal() == t.getRank().ordinal()) { // Mutual annihilation or flag capture
			if (t.getRank() == Piece.Rank.Flag) {
				finished = true;
				winner = currentPlayer;
				board[trow][tcol] = p;
			} else {
				board[trow][tcol] = Piece.createEmpty(trow, tcol);
			}
			changed.add(board[trow][tcol]);
			m.setOutcome(Outcome.Draw);
		} else { // Loss
			if (p.getRank() == Piece.Rank.Flag) { // You just committed suicide
				finished = true;
				winner = (currentPlayer == Piece.Color.White ? Piece.Color.Black : Piece.Color.White);
			}
			m.setOutcome(Outcome.Defender);
		}

		currentPlayer = currentPlayer == Piece.Color.White ? Piece.Color.Black : Piece.Color.White;
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
		GGBoard g = new GGBoard();
		for (int i = 0; i < 8; ++i) {
			for (int j = 0; j < 9; ++i) {
				g.board[i][j] = new Piece(board[i][j]);
			}
		}
		g.currentPlayer = currentPlayer;
		g.changed = new ArrayList<>();
		for (Piece p : changed) {
			g.changed.add(new Piece(p));
		}
		g.ready1 = ready1;
		g.ready2 = ready2;
		g.finished = finished;
		g.winner = winner;
		
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
		if (move == null || !(move instanceof GGMove)) return false;
		GGMove m = (GGMove) move;

		Piece source = board[m.getSourceRow()][m.getSourceCol()];
		Piece target = board[m.getTargetRow()][m.getTargetCol()];
System.out.println(m.getSourceRow()+","+m.getSourceCol()+ " color:"+ source.getColor().toString() + " rank:" + source.getRank().toString() + " " + m.getTargetRow()+","+m.getTargetCol() + " color:" + target.getColor().toString());
		if (m.getSourceRow() < 0
			|| m.getSourceRow() > 7
			|| m.getSourceCol() < 0
			|| m.getSourceCol() > 8
			|| m.getTargetRow() < 0
			|| m.getTargetRow() > 7
			|| m.getTargetCol() < 0
			|| m.getTargetCol() > 8
			|| source.getRank() == Piece.Rank.Empty
			|| source.getColor() != currentPlayer
			|| target.getColor() == currentPlayer
			|| Math.abs(m.getTargetRow()-m.getSourceRow()) > 1
			|| Math.abs(m.getTargetCol()-m.getTargetCol()) > 1) {
			return false;
		} else {
			return true;
		}
	}

	public boolean isReady(Piece.Color color) {
		return (color == Piece.Color.White ? ready1 : ready2);
	}

	public List<Piece> getChanged(Piece.Color color) {
		List<Piece> pieces = new ArrayList<>();
		for (Piece p : changed) {
			if (p.getRank() == Piece.Rank.Empty) {
				pieces.add(p);
				continue;
			}
			if (p.getColor() != color) {
				pieces.add(Piece.createUnknown(p.getColor(), p.getRow(), p.getColumn()));
			} else {
				pieces.add(p);
			}
		}
		return pieces;
	}

	public void clearChanged() {
		changed = new ArrayList<>();
	}

	public Piece[][] getBoard(Piece.Color player) {
		Piece[][] pieces = new Piece[8][9];
		for (int i = 0; i < 8; ++i) {
			for (int j = 0; j < 9; ++j) {
				if (board[i][j].getRank() == Piece.Rank.Empty) {
					pieces[i][j] = Piece.createEmpty(i, j);
					continue;
				}
				if (player == Piece.Color.Grey) {
					pieces[i][j] = new Piece(board[i][j]);
				} else if (board[i][j].getColor() != player) {
					pieces[i][j] = Piece.createUnknown(board[i][j].getColor(), i, j);
				} else {
					pieces[i][j] = new Piece(board[i][j]);
				}
			}
		}
		
		return pieces;
	}
}