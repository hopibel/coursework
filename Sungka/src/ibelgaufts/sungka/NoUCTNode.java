package ibelgaufts.sungka;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Node for Monte Carlo Tree Search<br>
 *
 * This class implements MCTS without the UCT (Upper Confidence Bounds for Trees) enhancement
 * for the purpose of determining the exploration bias constant C.
 */
public class NoUCTNode {
	private final Random rng = new Random();
//	private final double epsilon = 1e-6; // Double.MIN_VALUE still caused ties
	private State gameState;

	List<NoUCTNode> children;
	double visits;
	double totalValue;

	/**
	 * Generate an MCTSNode for the given game state
	 * @param g - Game object
	 */
	public NoUCTNode(State g) {
		gameState = g.copy();
		visits = totalValue = 0;
	}

	/**
	 * Select a non-terminal (game-wise) leaf node and expand it with next moves.
	 */
	public void selectAction() {
		List<NoUCTNode> visited = new ArrayList<NoUCTNode>();
		NoUCTNode current = this;
		visited.add(this);

		// Find a leaf node to expand (iterative)
		while(!current.isLeaf()) {
			current = current.select();
			visited.add(current);
		}
		current.expand(gameState.getMoves());
		NoUCTNode newNode = current.select();
//System.out.println(newNode==null);
		visited.add(newNode);
		State state = simulate(newNode);
		for(NoUCTNode node : visited) {
			node.updateStats(state);
		}
	}

	/**
	 * Add possible moves as child nodes
	 * @param moves - possible moves from Game.getMoves()
	 */
	public void expand(List<? extends State> moves) {
		int actions = moves.size();
		children = new ArrayList<NoUCTNode>();
		for(int i = 0; i < actions; ++i) {
			children.add(new NoUCTNode(moves.get(i)));
		}
	}

	public NoUCTNode select() {
		return children.get(rng.nextInt(children.size()));
	}

	/**
	 * @return True if the node has no children. False otherwise.
	 */
	public boolean isLeaf() {
		return children == null;
	}

	/**
	 * Simulate a game from the current node's gameState by playing random moves.
	 * @param node
	 * @return the terminal state of the simulated game.
	 */
	public State simulate(NoUCTNode node) {
		State sim = node.gameState.copy();

		while(!sim.getMoves().isEmpty()) {
			List<? extends State> moves = sim.getMoves();
			sim = moves.get(rng.nextInt(moves.size()));
		}

//System.out.println("Me: " + gameState.getCurrentPlayer() + " Winner: " + sim.getResult(gameState.getCurrentPlayer()));

		return sim;
	}

	private void updateStats(State state) {
		++visits;
//		totalValue += state.getResult(gameState.getCurrentPlayer());
		totalValue += state.getResult(gameState.getPreviousPlayer());
	}

	/**
	 * For visualization purposes
	 * @return number of children
	 */
	public int arity() {
		return children == null ? 0 : children.size();
	}

	/**
	 * @return MCTSNode containing the Game with the best move
	 */
	public NoUCTNode getBestMove() {
		// Return a random move if selectAction hasn't been called before
		if(children == null) {
			selectAction();
		}

		NoUCTNode best = null;
		double max = Double.NEGATIVE_INFINITY;
		for(NoUCTNode c : children){
			double nodeScore = c.totalValue / c.visits;
			double randomizer = rng.nextDouble() * Double.MIN_VALUE;
			if(nodeScore + randomizer > max){
				max = nodeScore + randomizer;
				best = c;
			}
		}

		return best;
	}

	/**
	 * @return the Game state for this node
	 */
	public State getState() {
		return gameState;
	}
}