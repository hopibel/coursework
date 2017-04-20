package ibelgaufts_uy.gotg.logic;

import java.util.ArrayList;
import java.util.List;

public class Piece {
	public enum Color {
		White, Black, Grey
	}
	private Color color;

	public static enum Rank {
		Flag, Private, Sergeant, Second_Lt, First_Lt, Captain, Major, Lt_Colonel,
			Colonel, One_Star, Two_Star, Three_Star, Four_Star, Five_Star, Spy, Unknown, Empty;
		public static final Rank values[] = values();
	}
	private Rank rank;

	private int row;
	private int col;

	/**
	 * @param first - true for player 1, false for player 2
	 * @return a List containing a full set of Pieces for the specified player
	 * all positioned at -1,-1. Positions must be set before play begins.
	 */
	public static List<Piece> generatePieces(Color color) {
		List<Piece> pieces = new ArrayList<>();

		// flag
		pieces.add(new Piece(color, Piece.Rank.values[0]));

		// spies
		pieces.add(new Piece(color, Piece.Rank.values[1]));
		pieces.add(new Piece(color, Piece.Rank.values[1]));

		// privates
		for (int i = 0; i < 6; ++i) {
			pieces.add(new Piece(color, Piece.Rank.values[2]));
		}

		// the rest
		for (int i = 3; i <= 14; ++i) {
			pieces.add(new Piece(color, Piece.Rank.values[i]));
		}

		return pieces;
	}

	private Piece(Color color, Rank rank, int row, int col) throws IllegalArgumentException {
		this.color = color;
		this.rank = rank;
		this.row = row;
		this.col = col;
	}
	
	private Piece(Color color, Rank rank) {
		this(color, rank, -1, -1);
	}

	public Piece(Piece p) {
		this(p.color, p.rank, p.row, p.col);
	}

	public static Piece createEmpty(int row, int col) {
		return new Piece(Rank.Empty, row, col);
	}
	
	public static Piece createUnknown(Color color, int row, int col) {
		return new Piece(color, Rank.Unknown, row, col);
	}

	private Piece(Rank rank, int row, int col) {
		this.color = Color.Grey;
		this.rank = rank;
		this.row = row;
		this.col = col;
	}

	/**
	 * @return the ID number (1 or 2) of the player that owns this piece
	 */
	public Color getColor() {
		return color;
	}
	
	public Rank getRank() {
		return rank;
	}
	
	public int getRow() {
		return row;
	}
	
	public void setRow(int row) {
		this.row = row;
	}
	
	public int getColumn() {
		return col;
	}
	
	public void setColumn(int col) {
		this.col = col;
	}

	public void setRank(Rank rank) {
		this.rank = rank;
	}
}