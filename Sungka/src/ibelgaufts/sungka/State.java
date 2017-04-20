package ibelgaufts.sungka;

import java.util.List;


/**
 * Interface for representing game state
 */
public interface State {
	/**
	 * Generates all legal moves for the current game state
	 * @return a List of new game states
	 */
	public List<? extends State> getMoves();

	/**
	 * Play the given Game state
	 * @param move - the Game state that will replace the current one
	 */
	public void playMove(State move);

	/**
	 * Get the result from the perspective of player.<br>
	 * Should be called after game ends.
	 * 
	 * @param player - should be same index as returned by getCurrentPlayer
	 * @return the score from player's perspective
	 */
	public double getResult(int player);

	/**
	 * Duplicate the game state
	 * @return a copy of the class
	 */
	public State copy();
	
	/**
	 * @return the ID of the current player
	 */
	public int getCurrentPlayer();

	/**
	 * Returns the ID of the previous player so the AI can evaluate from the perspective of the player
	 * whose move led to the current game state.
	 * @return the ID of the previous player
	 */
	public int getPreviousPlayer();
}
