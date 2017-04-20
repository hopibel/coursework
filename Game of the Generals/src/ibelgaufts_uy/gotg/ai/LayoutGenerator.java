package ibelgaufts_uy.gotg.ai;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import ibelgaufts_uy.gotg.logic.GGBoard;
import ibelgaufts_uy.gotg.logic.InvalidPieceException;
import ibelgaufts_uy.gotg.logic.Piece;

public class LayoutGenerator implements Runnable {
	private Piece.Color color;
	private static Random rng = new Random();
	private List<Piece> bestLayout;

	public LayoutGenerator(Piece.Color color) {
		this.color = color;
	}

	@Override
	public void run() {
		double bestScore = 0;

		for (int tree = 0; tree < 20; ++tree) {
			GGBoard state = new GGBoard();
			List<Piece> layout = generateLayout(color);
			try {
				state.placePieces(color == Piece.Color.White ? layout : generateLayout(Piece.Color.White));
				state.placePieces(color == Piece.Color.Black ? layout : generateLayout(Piece.Color.Black));
			} catch (InvalidPieceException e) {
				e.printStackTrace();
				throw new RuntimeException("this shouldn't happen");
			}
			MCTSNode mc = new MCTSNode(state);

			for (int playout = 0; playout < 100; ++playout) {
				mc.selectAction();
			}
			double score = mc.totalValue;
			if (color == Piece.Color.White) {
				score *= -1;
			}
			if (score > bestScore) {
				bestScore = score;
				bestLayout = layout;
			}
		}
	}

	private List<Piece> generateLayout(Piece.Color color) {
		List<Piece> pieces = Piece.generatePieces(color);
		List<Piece> layout = new ArrayList<>();
		int row = color == Piece.Color.White ? 0 : 6;
		int max = row + 2;
		for (; row < max; ++row) {
			for (int col = 0; col < 9; ++col) {
				int index = rng.nextInt(pieces.size());
				Piece p = pieces.get(index);
				pieces.remove(index);
				p.setRow(row);
				p.setColumn(col);
				layout.add(p);
			}
		}
		int index;
		Piece p;
		switch (color) {
		case White:
			index = rng.nextInt(pieces.size());
			p = pieces.get(index);
			pieces.remove(index);
			p.setRow(2);
			p.setColumn(0);
			layout.add(p);

			index = rng.nextInt(pieces.size());
			p = pieces.get(index);
			pieces.remove(index);
			p.setRow(2);
			p.setColumn(4);
			layout.add(p);

			index = rng.nextInt(pieces.size());
			p = pieces.get(index);
			pieces.remove(index);
			p.setRow(2);
			p.setColumn(8);
			layout.add(p);
			break;
		case Black:
			index = rng.nextInt(pieces.size());
			p = pieces.get(index);
			pieces.remove(index);
			p.setRow(5);
			p.setColumn(0);
			layout.add(p);

			index = rng.nextInt(pieces.size());
			p = pieces.get(index);
			pieces.remove(index);
			p.setRow(5);
			p.setColumn(4);
			layout.add(p);

			index = rng.nextInt(pieces.size());
			p = pieces.get(index);
			pieces.remove(index);
			p.setRow(5);
			p.setColumn(8);
			layout.add(p);
			break;
		default :
			throw new RuntimeException("generateLayout should not be called with Grey");
		}

		return layout;
	}

	public List<Piece> getBestLayout() {
		try {
			Thread.currentThread().join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return bestLayout;
	}
}