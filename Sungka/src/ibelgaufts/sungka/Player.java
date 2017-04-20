package ibelgaufts.sungka;

/**
 * Interface for player controllers connected to a Game
 */
public interface Player {
	/**
	 * @return the move to be played (TODO: should be a Move class for generality)
	 */
	int getMove();

	/**
	 * Informs a Player to update and redraw it's copy of the game state
	 */
	void redraw();

	/**
	 * Informs a Player to update and redraw a single cell (TODO: remove. as soon as sungka MP is submitted)
	 * @param i
	 */
	void redrawCell(int i);
}