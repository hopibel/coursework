package ibelgaufts_uy.gotg.logic;

import java.util.List;

/**
 * Interface for representing game state
 */
public interface State {
	/**
	 * Generates all legal moves for the current game state
	 * @return a List of legal Moves
	 */
	public List<? extends Move> getMoves();

	/**
	 * @param move - the Move to be played
	 * @throws Exception illegal Move
	 */
	public void playMove(Move move) throws Exception;

	/**
	 * @return true if the game is finished
	 */
	public boolean isFinished();

	/**
	 * Get the result from the perspective of player.<br>
	 * @param player - ID of the player
	 * @return the score from player's perspective
	 */
	public double getResult(int player);

	/**
	 * @return an independent copy of the game state
	 */
	public State copy();
	
	/**
	 * @return the ID of the current player
	 */
	public int getCurrentPlayer();

	/**
	 * @return the ID of the previous player
	 */
	public int getPreviousPlayer();
}