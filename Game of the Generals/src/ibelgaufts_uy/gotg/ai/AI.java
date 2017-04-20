package ibelgaufts_uy.gotg.ai;

import java.util.ArrayList;
import java.util.List;

import ibelgaufts_uy.gotg.logic.*;

public class AI implements Player, Observer, Runnable {
	private Generals game;
	private StochasticBoard state;
	private Piece.Color color;
	private LayoutGenerator lgen;

	public AI(Generals game, Piece.Color color) {
		this.game = game;
		this.color = color;
		new Thread(this).start();
	}

	@Override
	public void run() {
		lgen = new LayoutGenerator(color);
		Thread t = new Thread(lgen);
		t.start();
		try {
			t.join();
			game.setPieces(lgen.getBestLayout());
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("This shouldn't happen");
		}
	}

	@Override
	public Move getMove() {
		List<GGMove> moves = new ArrayList<>();
		List<Integer> counts = new ArrayList<>();

		if(state.getMoves().size() == 1) {
			return state.getMoves().get(0);
		}

		for (int i = 0; i < 20; ++i) {
			MCTSNode mc = new MCTSNode(state.generateBoard());

			for(int j = 0; j < 100; ++i) {
				mc.selectAction();
			}
			GGMove m = (GGMove) mc.getBestMove().getMove();
			boolean add = true;
			for (int j = 0; j < moves.size(); ++j) {
				if (moves.get(j).isEqual(m)) {
					counts.set(j, counts.get(j) + 1);
					add = false;
					break;
				}
			}
			if (add) {
				moves.add(m);
				counts.add(1);
			}
		}
		
		GGMove bestMove = moves.get(0);
		int best = counts.get(0);
		for (int i = 1; i < moves.size(); ++i) {
			if (counts.get(i) > best) {
				bestMove = moves.get(i);
				best = counts.get(i);
			}
		}

		return bestMove;
	}

	@Override
	public void update(GGMove move) {
		try {
			state.playMove(move);
		} catch (IllegalMoveException e) {
			e.printStackTrace();
			throw new RuntimeException("update() /cannot/ return an illegal move by design");
		}
	}

	@Override
	public void initializeBoard(Piece.Color color, Piece[][] board) {
		state = new StochasticBoard(board);
	}


}