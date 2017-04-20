package ibelgaufts_uy.gotg.logic;

public interface Observer {
	public void update(GGMove move);

	public void initializeBoard(Piece.Color color, Piece[][] board);
}