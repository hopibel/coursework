package ibelgaufts.sungka;

/**
 * A sungka AI that uses monte carlo tree search. Can play any game that supports
 * the Player interface.
 */
public class SungkaAI implements Player {
	private SungkaGame engine;
	private SungkaState game;
	private int iter;
	private double C = Math.sqrt(2);

	/**
	 * Instantiate an AI player
	 * @param engine - SungkaGame object to play
	 * @param simulations - how many simulations to run per turn
	 */
	public SungkaAI(SungkaGame engine, int simulations) {
		this.engine = engine;
		iter = simulations;
	}

	/**
	 * Instantiate an AI player
	 * @param engine - SungkaGame object to play
	 * @param simulations - how many simulations to run per turn
	 * @param C - UCT exploration bias constant
	 */
	public SungkaAI(SungkaGame engine, int simulations, double C) {
		this(engine, simulations);
		this.C = C;
	}

	/* (non-Javadoc)
	 * @see ibelgaufts.sungka.PlayerHandler#getMove()
	 */
	public int getMove() {
		game = engine.getGameState();
		MCTSNode mc = new MCTSNode(game, C);

		if(game.getMoves().size() == 1) {
			return ((SungkaState) mc.getBestMove().getState()).getLastMove();
		}

		for(int i = 0; i < iter; ++i) {
			mc.selectAction();
		}

//		TreeView tv = new TreeView(mc);
//		tv.showTree(iter + " simulations");
System.out.println(mc.getBestMove().getState().getPreviousPlayer());
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