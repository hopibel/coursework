package ibelgaufts_uy.gotg.ai;

import ibelgaufts_uy.gotg.logic.Move;
import ibelgaufts_uy.gotg.logic.State;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Node for Monte Carlo Search Tree<br>
 *
 * Based on example code by Simon Lucas from mcts.ai/code/java.html<br>
 * The original example simulated coin flips.
 */
public class MCTSNode {
	private final Random rng = new Random();
//	private final double epsilon = 1e-6; // Double.MIN_VALUE still caused ties
	private State gameState;
	private Move move; // Move leading to this gameState

	List<MCTSNode> children;
	double visits;
	double totalValue;
	double C; // exploration bias factor

	/**
	 * Generate an MCTSNode for the given game state
	 * @param g - Game object
	 * @param C - exploration over exploitation bias constant
	 */
	public MCTSNode(State g, double C) {
		gameState = g.copy();
		this.C = C;
		visits = totalValue = 0;
		if(C <= 0) {
			throw new IllegalArgumentException("C is not a positive number");
		}
	}
	
	/**
	 * Generate an MCTSNode for the given game state
	 * @param g - Game object
	 */
	public MCTSNode(State g) {
		this(g, Math.sqrt(2));
	}
	
	private MCTSNode(State g, Move m, double C) {
		this(g, C);
		move = m;
	}

	/**
	 * Select a non-terminal (game-wise) leaf node and expand it with next moves.
	 */
	public void selectAction() {
		List<MCTSNode> visited = new ArrayList<MCTSNode>();
		MCTSNode current = this;
		visited.add(this);

		// Find a leaf node to expand (iterative)
		while(!current.isLeaf()) {
			current = current.select();
			visited.add(current);
		}
		current.expand(gameState.getMoves());
		MCTSNode newNode = current.select();
//System.out.println(newNode==null);
		visited.add(newNode);
		State state = simulate(newNode);
		for(MCTSNode node : visited) {
			node.updateStats(state);
		}
	}

	/**
	 * Add possible moves as child nodes
	 * @param moves - possible moves from Game.getMoves()
	 */
	public void expand(List<? extends Move> moves) {
		int actions = moves.size();
		children = new ArrayList<MCTSNode>();
		for(int i = 0; i < actions; ++i) {
			State child = gameState.copy();
			try {
				child.playMove(moves.get(i));
			} catch (Exception e) {
				e.printStackTrace();
				throw new RuntimeException("Fatal error: getMoves() must only return legal moves");
			}
			children.add(new MCTSNode(child, moves.get(i), C));
		}
	}

	public MCTSNode select() {
		MCTSNode selected = null;
		double bestValue = Double.NEGATIVE_INFINITY;
		for(MCTSNode c : children) {
			double nodeScore = c.totalValue / (c.visits + Double.MIN_VALUE);
			double bias = C * Math.sqrt(Math.log(visits + 1) / (c.visits + Double.MIN_VALUE));
//System.out.println(Math.sqrt(Math.log(visits + 1) / (c.visits + Double.MIN_VALUE)) == Double.POSITIVE_INFINITY);
			double rand = rng.nextDouble() * Double.MIN_VALUE;
			double uctValue = nodeScore + bias + rand;
//				(c.totalValue / (c.visits + epsilon)) + C * Math.sqrt(Math.log(visits + 1) / (c.visits + epsilon)) + rng.nextDouble() * epsilon; // small random number for tie-breaking
			if(uctValue > bestValue) {
				selected = c;
				bestValue = uctValue;
			}
//System.out.println(uctValue + " : " + bestValue);
//System.out.println(nodeScore + " " + bias + " " + rand);
		}

		return selected;
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
	public State simulate(MCTSNode node) {
		State sim = node.gameState.copy();

		while(!sim.getMoves().isEmpty()) {
			List<? extends Move> moves = sim.getMoves();
			try {
				sim.playMove(moves.get(rng.nextInt(moves.size())));
			} catch (Exception e) {
				e.printStackTrace();
				throw new RuntimeException("Fatal error: getMoves() must only return legal moves");
			}
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
	 * @return MCTSNode containing the game State with the best move
	 */
	public MCTSNode getBestMove() {
		// Return a random move if selectAction hasn't been called before
		if(children == null) {
			selectAction();
		}

		MCTSNode best = null;
		double max = Double.NEGATIVE_INFINITY;
		for(MCTSNode c : children){
			double nodeScore = c.visits;
			double randomizer = rng.nextDouble() * Double.MIN_VALUE;
			if(nodeScore + randomizer > max){
				max = nodeScore + randomizer;
				best = c;
			}
		}

		return best;
	}

	/**
	 * @return the State for this node
	 */
	public State getState() {
		return gameState;
	}
	
	public Move getMove() {
		return move;
	}
}