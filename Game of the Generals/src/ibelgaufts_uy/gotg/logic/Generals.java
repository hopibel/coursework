package ibelgaufts_uy.gotg.logic;

import ibelgaufts_uy.gotg.logic.Piece.Color;

import java.util.ArrayList;
import java.util.List;

/**
 * Game of the Generals. This class passes moves from Player objects to a GGBoard.<br>
 * Usage:<br>
 * 1. Set controllers for each player<br>
 * 2. Set pieces for each player<br>
 * 3. new Thread(Generals).start();
 */
public class Generals implements Runnable {
	private Player player1, player2, activePlayer;
	private List<Observer> observers = new ArrayList<>();

	private GGBoard board;

	@Override
	public void run() {
		try {
			board = new GGBoard();

			// Wait for clients to connect and place their pieces
			while ((player1 == null || !board.isReady(Piece.Color.White)) || (player2 == null || !board.isReady(Piece.Color.Black))){
				Thread.sleep(1000);
			}
			notifyObservers();

			activePlayer = player1;
			while(!board.isFinished()){
				Move move;
				do {
					move = activePlayer.getMove();
					Thread.sleep(100);
				} while (!board.isValid(move));
System.out.println("Move received");
				try {
					board.playMove(move);
				} catch (IllegalMoveException e) {
System.out.println("illegal move somehow got through");
					e.printStackTrace();
					throw new RuntimeException("Move still illegal despite passing validation check");
				}
System.out.println("Move played");
				swapActivePlayer();
				notifyObservers(move);
			}
		} catch (InterruptedException e) {
			return;
		}
	}

	private void swapActivePlayer() {
		if (activePlayer == player1) {
			activePlayer = player2;
		} else {
			activePlayer = player1;
		}
	}

	private void notifyObservers(Move move) {	
		for (Observer ob : observers) {
			ob.update((GGMove) move);
		}
System.out.println("players updated");
	}

	private void notifyObservers() {
		for (Observer ob : observers) {
			if (ob == player1) {
				ob.initializeBoard(Color.White, board.getBoard(Color.White));
			} else if (ob == player2) {
				ob.initializeBoard(Color.Black, board.getBoard(Color.Black));
			} else {
				ob.initializeBoard(Color.Grey, board.getBoard(Color.Grey));
			}
		}
	}

	/**
	 * Set a PlayerHandler for the specified Player
	 * @param first - selected player. true for 1, false for 2
	 * @param controller - Player controller
	 * @throws Exception when the selected player is already set
	 */
	public void setPlayer(Piece.Color color, Player controller) throws Exception {
		if (color == Piece.Color.White) {
			if (player1 != null) {
				throw new Exception("There already is a Player 1");
			}
			player1 = controller;
		} else if (color == Piece.Color.Black) {
			if (player2 != null) {
				throw new Exception("There already is a Player 2");
			}
			player2 = controller;
		} else {
			throw new Exception("Grey is not a valid player");
		}
	}

	public void setPieces(List<Piece> pieces) throws InvalidPieceException {
		board.placePieces(pieces);
	}

	public void addObserver(Observer o) {
		observers.add(o);
	}

	public Piece[][] getBoard(Piece.Color player) {
		return board.getBoard(player);
	}
	
	public int getCurrentPlayer() {
		return board.getCurrentPlayer();
	}
}