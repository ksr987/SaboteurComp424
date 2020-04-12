package student_player;

import boardgame.Move;

import Saboteur.SaboteurPlayer;
import Saboteur.cardClasses.SaboteurBonus;
import Saboteur.cardClasses.SaboteurCard;
import Saboteur.cardClasses.SaboteurDrop;
import Saboteur.cardClasses.SaboteurMalus;
import Saboteur.cardClasses.SaboteurMap;
import Saboteur.cardClasses.SaboteurTile;

import java.util.ArrayList;
import java.util.List;

import Saboteur.SaboteurBoardState;
import Saboteur.SaboteurMove;

/** A player file submitted by a student. */
public class StudentPlayer extends SaboteurPlayer {

	private static final String KETAN_SID = "260732873";
	private static final String ALAIN_SID = "260714615";

	public static int player_id;
	/**
	 * You must modify this constructor to return your student number. This is
	 * important, because this is what the code that runs the competition uses to
	 * associate you with your agent. The constructor should do nothing else.
	 */
	public StudentPlayer() {
		super(KETAN_SID);
	}

	/**
	 * This is the primary method that you need to implement. The ``boardState``
	 * object contains the current state of the game, which your agent must use to
	 * make decisions.
	 */
	public Move chooseMove(SaboteurBoardState boardState) {
//		SaboteurMove move = MyTools.evaluateGreedyMove(boardState.getAllLegalMoves(), boardState.getCurrentPlayerCards(), boardState.getTurnPlayer(), boardState.getBoardForDisplay());
//		if (move != null) {
//			System.out.println("Found in Evaluation");
//			return move;
//		}
		
		// is not under malus, golden nugget not found, doesn't have map
		long current = System.currentTimeMillis();

		SaboteurBoardStateClone clonedState = new SaboteurBoardStateClone(boardState);
		Tree tree = new Tree(clonedState);
		Node rootNode = tree.getRoot();

		// TODO: should be less than 2000, but by how much? see how much the rest of the function (after while loop) takes and add a margin of safety
		while (System.currentTimeMillis() - current  < 2000) {

			// Selection
			Node selectedNode = MyTools.MCTS_Selection(rootNode);

			// Expansion
			if (!selectedNode.state.gameOver()) {
				MyTools.MCTS_Expansion(selectedNode);
			}
			// Simulation
			Node simulation_node = selectedNode;
			if (selectedNode.getChildArray().size() > 0) {
				List<Node> childArray = selectedNode.getChildArray();
				int random_index = (int) (childArray.size() * Math.random());
				simulation_node = childArray.get(random_index);
			}

			double playoutResult = MyTools.MCTS_Simulation(simulation_node);

			// Backpropagation
			MyTools.MCTS_Backpropagation(simulation_node, playoutResult);
		}

		// Get child with maximum score
		List<Node> root_children = rootNode.getChildArray();
		Node picked_node = root_children.get(0);

		for (Node child: root_children) {
			int visit_count = child.getVisitCount();
			if (visit_count > picked_node.getVisitCount()) picked_node = child; 
		}

		tree.setRoot(picked_node);

		Move myMove = picked_node.getMovePlayed();

		// Return your move to be processed by the server.
		return myMove;
	}
}