package ibelgaufts.sungka;

import java.util.List;
import java.util.ArrayList;

public class AITest {
	public static void main(String[] args) throws InterruptedException {
		double constants[] = {0.00001, 0.25, 0.5, 0.75, 1, 1.25, 1.5, 1.75, 2};
		List<Double> data = new ArrayList<Double>();
		for(int i = 0; i < constants.length; ++i) {
			data.add(0.0);
		}

		for(int i = 0; i < 100; ++i) {
			System.out.println("Trial: " + (i+1));
			for(int j = 0; j < constants.length; ++j) {
				SungkaGame game = new SungkaGame();

				// Experimental
				SungkaAI ai1 = new SungkaAI(game, 10000/*, constants[j]*/);
				game.setPlayer(SungkaGame.Turn.PLAYER1, ai1);
	
				// Control
				SungkaAI ai2 = new SungkaAI(game, 1000);
				game.setPlayer(SungkaGame.Turn.PLAYER2, ai2);

				Thread thread = new Thread(game);
				thread.start();
				thread.join();
				data.set(j, data.get(j)+(game.getGameState().getResult(1)/* > 49 ? 1 : 0*/));
				System.out.println("C:" + constants[j] + "\t" + data.get(j)/(i+1)*100 + "%");
			}
		}
	}
}