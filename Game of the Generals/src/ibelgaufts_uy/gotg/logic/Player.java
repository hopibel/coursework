package ibelgaufts_uy.gotg.logic;

/**
 * Interface for player controllers
 */
public interface Player {
	/**
	 * @return the Move to be played. null if no move has been made
	 */
	public Move getMove();
}