package ibelgaufts.sungka;

/**
 * A sungka AI that uses monte carlo tree search. Can play any game that supports
 * the Player interface.
 */
public class TestAI implements Player {
	private SungkaGame engine;
	private SungkaState game;
	private int iter;

	/**
	 * Instantiate an AI player
	 * @param engine - SungkaGame object to play
	 * @param simulations - how many simulations to run per turn
	 */
	public TestAI(SungkaGame engine, int simulations) {
		this.engine = engine;
		iter = simulations;
	}

	/* (non-Javadoc)
	 * @see ibelgaufts.sungka.PlayerHandler#getMove()
	 */
	public int getMove() {
		game = engine.getGameState();
		NoUCTNode mc = new NoUCTNode(game);

		if(game.getMoves().size() == 1) {
			return ((SungkaState) mc.getBestMove().getState()).getLastMove();
		}

		for(int i = 0; i < iter; ++i) {
			mc.selectAction();
		}

//		TreeView tv = new TreeView(mc);
//		tv.showTree(iter + " simulations");

		return ((SungkaState) mc.getBestMove().getState()).getLastMove();
	}

	// Not needed by the AI
	/* (non-Javadoc)
	 * @see ibelgaufts.sungka.PlayerHandler#redraw()
	 */
	public void redraw() {}

	/* (non-Javadoc)
	 * @see ibelgaufts.sungka.PlayerHandler#redrawCell(int)
	 */
	public void redrawCell(int i) {}
}