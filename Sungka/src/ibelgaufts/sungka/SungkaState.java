package ibelgaufts.sungka;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Contains the state of the sungka board. Ideally this would be used by the
 * GUI but the animation requires updating the UI every time the board
 * is changed. This class does not store intermediate states and so cannot be
 * used. This class is instead used by the AI to simulate games.
 * <br><br>
 * Cups on the board are indexed as follows:
 * <pre>
 *   |14|13|12|11|10| 9| 8|
 * 15|--------------------| 7
 *   | 0| 1| 2| 3| 4| 5| 6|
 * </pre>
 */
public class SungkaState implements State {
	private int[] board;
	private int turn; // 1 = player1, 2 = player2

	private boolean firstTurn; // first turn is simultaneous
	private int pos1, pos2; // position of each player's hand 
	private int hand1, hand2; // contents of each player's hand
	private int next; // first to move when resuming first turn
	private int lastMove;
	private int previousPlayer;

	/**
	 * status of the game:
	 * PLAYER1WIN, PLAYER2WIN, DRAW, ONGOING
	 */
	public enum status {
		PLAYER1WIN, PLAYER2WIN, DRAW, ONGOING
	}

	public SungkaState() {
		board = new int[16];
		Arrays.fill(board, 7);
		board[7] = board[15] = 0;
		turn = 1;

		firstTurn = true;
		pos1 = pos2 = -1;
		hand1 = hand2 = 0;
		next = 1;
		previousPlayer = 2; // Pretend previous player was player2
	}

	/* (non-Javadoc)
	 * @see ibelgaufts.sungka.Game#copy()
	 */
	public SungkaState copy() {
		SungkaState s = new SungkaState();
		s.board = board.clone();
		s.turn = turn;
		s.firstTurn = firstTurn;
		s.pos1 = pos1;
		s.pos2 = pos2;
		s.hand1 = hand1;
		s.hand2 = hand2;
		s.next = next;
		s.lastMove = lastMove;
		s.previousPlayer = previousPlayer;
		return s;
	}

	/**
	 * Plays move from the cup at the given index.
	 * During the first turn the game is paused when new input is required
	 *
	 * @param pos - cup to play
	 * @throws IllegalArgumentException when pos is not a legal move
	 */
	public void playMove(int pos) throws IllegalArgumentException {
		if(isFinished()) {
			return;
		}

		validate(pos);
		lastMove = pos;

		if(firstTurn) {
			moveFirst(pos);
			return;
		}

		int hand = board[pos];
		board[pos] = 0;

		while(hand > 0) {
			int[] state = dropStone(pos, hand);
			pos = state[0];
			hand = state[1];

			// -1 == wait for move
			if(pos == -1) {
				return;
			}
		}

		previousPlayer = turn;
		turn = 3 - turn; // next turn
		if(!canMove()) {
			turn = 3 - turn; // skip player if they have no valid moves
		}
	}

	/* (non-Javadoc)
	 * @see ibelgaufts.sungka.Game#playMove(ibelgaufts.sungka.Game)
	 */
	@Override
	public void playMove(State move) {
		validate(move);
		
		SungkaState s = (SungkaState) move;
		board = s.board.clone();
		turn = s.turn;
		firstTurn = s.firstTurn;
		pos1 = s.pos1;
		pos2 = s.pos2;
		hand1 = s.hand1;
		hand2 = s.hand2;
		next = s.next;
		lastMove = s.lastMove;
	}

	/**
	 * @return a copy of the board array
	 */
	public int[] getBoard() {
		return board.clone();
	}
	
	/* (non-Javadoc)
	 * @see ibelgaufts.sungka.Game#getCurrentPlayer()
	 */
	public int getCurrentPlayer() {
		return turn;
	}

	public int getPreviousPlayer() {
		return previousPlayer;
	}

	/**
	 * @return the cup last played
	 */
	public int getLastMove() {
		return lastMove;
	}

	/* (non-Javadoc)
	 * @see ibelgaufts.sungka.Game#getMoves()
	 */
	public List<SungkaState> getMoves() {
		List<SungkaState> moves = new ArrayList<SungkaState>();

		for(int i = turn == 1 ? 0 : 8; i < getHome(); ++i) {
			if(board[i] > 0) {
				SungkaState s = this.copy();
				s.playMove(i);
				moves.add(s);
			}
		}

		return moves;
	}

	/* (non-Javadoc)
	 * @see ibelgaufts.sungka.Game#getResult(int)
	 */
	@Override
	public double getResult(int player) {
		if(board[7] == board[15]) {
			return 0.5;
		}

/*		int sum1, sum2;
		sum1 = sum2 = 0;
		for(int i = 0; i <= 7; ++i) {
			sum1 += board[i];
		}
		for(int i = 8; i <= 15; ++i) {
			sum2 += board[i];
		}
*/
		if(player == 1) {
			return board[7] > board[15] ? 1 : 0;
//			return board[7];
//			return sum1;
		} else {
			return board[15] > board[7] ? 1 : 0;
//			return board[15];
//			return sum2;
		}
	}

	/**
	 * @param game - Game object to compare with
	 * @return whether or not game is equal to this object
	 */
	public boolean equals(State game) {
		if(!(game instanceof SungkaState)) {
			return false;
		}

		SungkaState s = (SungkaState) game;

		if(Arrays.equals(s.board, board)
			&& s.turn == turn
			&& s.firstTurn == firstTurn
			&& s.pos1 == pos1
			&& s.pos2 == pos2
			&& s.hand1 == hand1
			&& s.hand2 == hand2
			&& s.next == next)
		{
			return true;
		}
		
		return false;
	}

	private void validate(int pos) {
		if(pos < 0 || pos >= 15 || pos == 7) {
			throw new IllegalArgumentException("Bad move index");
		}
		if(!isOwnSide(pos)) {
			throw new IllegalArgumentException("Player " + turn + " can't play that move");
		}
		if(board[pos] == 0) {
			throw new IllegalArgumentException("Empty cup: " + pos);
		}
	}

	private void validate(State move) {
		for(SungkaState m : getMoves()) {
			if(m.equals(move)) {
				return;
			}
		}
		
		throw new IllegalArgumentException("Illegal move");
	}

	private void moveFirst(int move) {
		switch(turn) {
		case 1:
			pos1 = move;
			hand1 = board[pos1];
			board[pos1] = 0;
			break;
		case 2:
			pos2 = move;
			hand2 = board[pos2];
			board[pos2] = 0;
			break;
		}

		// -1 = wait for move
		if(pos1 == -1) {
			turn = 1;
			return;
		} else if(pos2 == -1) {
			turn = 2;
			return;
		}

		while(hand1 + hand2 > 0) {
			if(hand1 > 0 && next == 1) {
				turn = 1;
				int[] state = dropStone(pos1, hand1);
				pos1 = state[0];
				hand1 = state[1];

				if(hand2 > 0) {
					next = 2;
				}
				if(pos1 == -1) {
					turn = 1;
					return;
				}
			}

			if(hand2 > 0 && next == 2) {
				turn = 2;
				int[] state = dropStone(pos2, hand2);
				pos2 = state[0];
				hand2 = state[1];

				if(hand1 > 0) {
					next = 1;
				}
				if(pos2 == -1) {
					turn = 2;
					return;
				}
			}
		}

		// Not waiting and both hands are 0
		if(hand1 + hand2 == 0) {
			firstTurn = false;
			next = 0;
			turn = 1;
		}
	}

	private int[] dropStone(int pos, int hand) {
		if(hand > 0) {
			if(++pos == getOpponentHome()) {++pos;} // Skip opponent's home cup
			if(pos > 15) {pos = 0;}

			++board[pos];
			--hand;

			if(hand == 0) {
				if(isHome(pos) && canMove()) { // Home cup
					pos = -1;
				} else if(isOwnSide(pos) && board[pos] > 1) { // Nonempty cup
					hand = board[pos];
					board[pos] = 0;
				} else if(isOwnSide(pos) && board[pos] == 1/* && board[14-pos] > 0*/) { // Capture
					hand += board[pos];
					board[pos] = 0;
					hand += board[14-pos];
					board[14-pos] = 0;
					board[getHome()] += hand;
					hand = 0; // Turn over
//					pos = -1;
				}
			}
		}

		return new int[] {pos, hand};
	}

	private int getHome() {
		return turn == 1 ? 7 : 15;
	}

	private int getOpponentHome() {
		return turn == 1 ? 15 : 7;
	}

	private boolean isHome(int pos) {
		return pos == getHome() ? true : false;
	}

	private boolean canMove() {
		int home = getHome();
		for(int i = turn == 1 ? 0 : 8; i < home; ++i) {
			if(board[i] > 0) {
				return true;
			}
		}
		return false;
	}

	private boolean isOwnSide(int pos) {
		if(turn == 1) {
			return pos >= 0 && pos < 7;
		} else {
			return pos > 7 && pos < 15;
		}
	}

	private boolean isFinished() {
		for(int i = 0; i < board.length; ++i) {
			if(i == 7 || i == 15) {
				continue;
			}
			
			if(board[i] > 0) {
				return false;
			}
		}
		
		return true;
	}
}