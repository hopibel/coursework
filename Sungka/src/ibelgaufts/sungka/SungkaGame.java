package ibelgaufts.sungka;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.SwingUtilities;

/**
 * Cups on the board are indexed as follows:
 * <pre>
 *   |14|13|12|11|10| 9| 8|
 * 15|--------------------| 7
 *   | 0| 1| 2| 3| 4| 5| 6|
 * </pre>
 */
public class SungkaGame implements Runnable {
	private int[] board;
	private State state;
	private boolean animate;
	private Player player1;
	private Player player2;
	private Player activePlayer;
	private Player spectator;

	private List<Integer> history;
	private boolean turnOne;

	private SungkaState gameState;

	/**
	 * Turn enum:<br>
	 * PLAYER1, PLAYER2
	 */
	public enum Turn {
		PLAYER1, PLAYER2
	}

	/**
	 * Current game state:<br>
	 * PLAYER1, PLAYER2, PLAYER1_WIN, PLAYER2_WIN, DRAW
	 */
	public enum State {
		PLAYER1, PLAYER2, PLAYER1_WIN, PLAYER2_WIN, DRAW
	}

	/**
	 * New sungka game
	 */
	public SungkaGame() {
		board = new int[16];
		Arrays.fill(board, 7);
		board[7] = board[15] = 0;
		animate = false;
		history = new ArrayList<Integer>();
		gameState = new SungkaState();
		player1 = player2 = null;
	}

	/**
	 * @return a copy of the game as a Game object for use by the AI
	 */
	public SungkaState getGameState() {
		return gameState.copy();
	}

	/**
	 * Kludge to get the GUI to update when playing AI vs AI
	 * @param spec
	 */
	public void setObserver(Player spec) {
		spectator = spec;
	}

	/**
	 * @return true if game is in the simultaneous first turn
	 */
	public boolean isFirstTurn() {
		return turnOne;
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		// Wait for clients to connect
		while (player1 == null || player2 == null){
			try {Thread.sleep(1000);} catch (InterruptedException e) {}
		}

		// Draw initial state
		updateClients();

		activePlayer = player1;
		state = State.PLAYER1;

		firstTurn();

		activePlayer = player1;
		state = State.PLAYER1;

		while(!isFinished()){
			playTurn();
			swapActivePlayer();
		}
	}

/**
 * First turn must be executed simultaneously for both players due to the rules
 * being unfairly biased towards the first player.
 */
	private void firstTurn() {
		turnOne = true;

		int player1_move = waitForMove();
		gameState.playMove(player1_move);
		activePlayer = player2;
		state = State.PLAYER2;
		int player2_move = waitForMove();
		gameState.playMove(player2_move);

		int player1_hand = board[player1_move];
		board[player1_move] = 0;
		int player2_hand = board[player2_move];
		board[player2_move] = 0;

		if(animate) {
			updateClients(player1_move);
			updateClients(player2_move);
		}

		int[] playerstate;

		while(player1_hand > 0 || player2_hand > 0) {

			activePlayer = player1;
			state = State.PLAYER1;
			playerstate = dropStone(player1_move, player1_hand);
			player1_move = playerstate[0];
			player1_hand = playerstate[1];

			activePlayer = player2;
			state = State.PLAYER2;
			playerstate = dropStone(player2_move, player2_hand);
			player2_move = playerstate[0];
			player2_hand = playerstate[1];
		}

		updateClients();
		turnOne = false;
	}

	private void playTurn() {
		if(!canMove()) {
			return;
		}

		int move = waitForMove();
		int hand = board[move];
		board[move] = 0;

		gameState.playMove(move);

		if(animate) {
			updateClients(move);
		}

		int[] playerstate;

		while(hand > 0) {
			playerstate = dropStone(move, hand);
			move = playerstate[0];
			hand = playerstate[1];
		}
	}

	private int[] dropStone(int move, int hand) {
		if(hand > 0) {
			if(++move == getOpponentHome()) {++move;} // Skip opponent's home cup
			if(move > 15) {move = 0;}

			++board[move];
			--hand;

			if(animate) {
				updateClients(move);
			}

			if(hand == 0) {
				if(isHome(move) && canMove()) {
					move = waitForMove();
					hand = board[move];
					board[move] = 0;

					gameState.playMove(move);

					if(animate) {
						updateClients(move);
					}
				} else if(isOwnSide(move) && board[move] > 1) { // Nonempty cup
					hand = board[move];
					board[move] = 0;

					if(animate) {
						updateClients(move);
					}
				} else if(isOwnSide(move) && board[move] == 1/* && board[14-move] > 0*/) { // Capture
					hand += board[move];
					board[move] = 0;

					if(animate) {
						updateClients(move);
					}

					hand += board[14-move];
					board[14-move] = 0;

					if(animate) {
						updateClients(14-move);
					}

					board[getHome()] += hand;

					if(animate) {
						updateClients(getHome());
					}

					hand = 0; // Turn over
				}
			}
		}

		return new int[] {move, hand};
	}

	private int getHome() {
		switch(state) {
		case PLAYER1:
			return 7;
		case PLAYER2:
			return 15;
		default:
			return 7; // Shouldn't happen
		}
	}

	private int getOpponentHome() {
		switch(state) {
		case PLAYER1:
			return 15;
		case PLAYER2:
			return 7;
		default:
			return 7; // Shouldn't happen
		}
	}


	private void updateClients() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				player1.redraw();
				player2.redraw();
				if(spectator != null) {spectator.redraw();}
			}
		});
	}

	// Ugly. It's for the animation bonus
	private void updateClients(int i) {
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			System.err.println("Animation wait interrupted");
		}
		player1.redrawCell(i);
		player2.redrawCell(i);
		if(spectator != null) {spectator.redrawCell(i);}
	}

	private boolean isOwnSide(int move) {
		switch(state) {
		case PLAYER1:
			return move >= 0 && move < 7;
		case PLAYER2:
			return move > 7 && move < 15;
		default:
			return false; // Shouldn't happen
		}
	}

	private boolean isHome(int move) {
		switch(state) {
		case PLAYER1:
			return move == 7;
		case PLAYER2:
			return move == 15;
		default:
			return false; // Shouldn't happen
		}
	}

	private int waitForMove() {
		updateClients();

		int move;
		do {
			move = activePlayer.getMove();
			try {Thread.sleep(100);} catch (InterruptedException e) {/* Handle a return to menu? */} // Poll for input every 100ms
		} while(!isMoveValid(move));
		
		history.add(move);
		return move;
	}

	private void swapActivePlayer() {
		switch(state) {
		case PLAYER1:
			activePlayer = player2;
			state = State.PLAYER2;
			break;
		case PLAYER2:
			activePlayer = player1;
			state = State.PLAYER1;
			break;
		default:
			break;
		}
		
		if(!animate) {
			updateClients();
		}
	}

	/**
	 * Set a PlayerHandler for the specified Player
	 * @param player - Player enum with a value of either PLAYER1 or PLAYER2
	 * @param handler - PlayerHandler to get moves from
	 */
	public void setPlayer(Turn player, Player handler) {
		switch(player) {
		case PLAYER1: player1 = handler; break;
		case PLAYER2: player2 = handler; break;
		}
	}

	private boolean isFinished() {
		for(int i = 0; i < 15; ++i) {
			if(i == 7) {continue;}
			if(board[i] > 0) {return false;}
		}
		if(board[7] > board[15]) {
			state = State.PLAYER1_WIN;
		} else if(board[15] > board[7]) {
			state = State.PLAYER2_WIN;
		} else {
			state = State.DRAW;
		}

		updateClients();

		return true;
	}

	private boolean canMove() {
		int i;
		switch(state) {
		case PLAYER1:
			i = 0;
			break;
		case PLAYER2:
			i = 8;
			break;
		default: // Shouldn't happen
			updateClients();
			return false;
		}
		int n = i + 7;
		for(; i < n; ++i) {
			if(board[i] > 0) {
				return true;
			}
		}

		if(!animate) {
			updateClients();
		}
		return false;
	}

	private boolean isMoveValid(int move) {
		if(isOwnSide(move) && board[move] != 0) {
			return true;
		} else {
			return false;
		}
	}

/**
 * Get the number of stones from a given cup
 * 
 * @param i - index of the cup
 * @return number of stones in the cup
 */
	public int getStones(int i) {
		return board[i];
	}

	/**
	 * @return a copy of the array game board
	 */
	public int[] getBoard() {
		return board.clone();
	}

	/**
	 * @return a List of Integers representing the sequence of moves leading to the current game state
	 */
	public List<Integer> getHistory() {
		return new ArrayList<Integer>(history);
	}

	/**
	 * @return the current state of the game as a State enum
	 */
	public State getState() {
		return state;
	}

	/**
	 * @param b - whether to animate moves or just update after executing a turn
	 */
	public void setAnimate(boolean b) {
		animate = b;
	}
}