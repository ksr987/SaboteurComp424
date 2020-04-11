package student_player;

import java.util.ArrayList;
import java.util.Arrays;

import Saboteur.SaboteurMove;
import Saboteur.cardClasses.SaboteurBonus;
import Saboteur.cardClasses.SaboteurCard;
import Saboteur.cardClasses.SaboteurDestroy;
import Saboteur.cardClasses.SaboteurDrop;
import Saboteur.cardClasses.SaboteurMalus;
import Saboteur.cardClasses.SaboteurMap;
import Saboteur.cardClasses.SaboteurTile;

public class MyTools {


	public static final int originPos = 5;

	public static final ArrayList<int[]> hiddenPosDynamic = new ArrayList<int[]>();

	public static final int[][] hiddenPos = {{originPos+7,originPos-2},{originPos+7,originPos},{originPos+7,originPos+2}};

	public void findHiddenPos(Node selectedNode) {
		SaboteurTile[][] tiles = selectedNode.getState().getHiddenBoard();
		for (int x = 0; x < tiles.length; x++) {
			for (int y = 0; y < tiles[x].length; y++) {
				// -1 means empty i.e. hidden objective found
				if (tiles[x][y] != null) {
					if (tiles[x][y].getIdx().equals("8") && !tiles[x][y].getIdx().equals("entrance")) {
						// TODO: Coordinates might not be consistent with convention
						int[] temp = {x, y};
						hiddenPosDynamic.add(temp);
					}
				}
			}
		}
	}

	public static Node MCTS_Selection(Node rootNode) {
		Node parentNode = rootNode;

		// while we still haven't reached a leaf...
		while(! parentNode.getChildArray().isEmpty()) {

			double max_uct = 0;
			Node currentNode = parentNode;

			// find the child node with maximum UCT

			for (Node node : parentNode.getChildArray()) {
				int wins = node.getWinScore();
				int total_simulations_parent = parentNode.getVisitCount();
				int total_simulations_child = node.getVisitCount();

				double uct = (double) (wins) / (double) (total_simulations_child)
						+ Math.sqrt(2 * Math.log(total_simulations_parent) / (double) total_simulations_child);

				if (uct > max_uct) {
					max_uct = uct;
					currentNode = node;
				}
			}
			parentNode = currentNode;
		}
		return parentNode;
	}

	public static void MCTS_Expansion(Node selectedNode) {
		ArrayList<SaboteurMove> legal_moves = selectedNode.getState().getAllLegalMoves();
		for (SaboteurMove move : legal_moves) {
			SaboteurBoardStateClone clonedState = new SaboteurBoardStateClone(selectedNode.getState());
			try {
				clonedState.processMove(move);
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println(Arrays.deepToString(clonedState.getHiddenBoard()).replace("], ", "]\n"));
			}
			Node node = new Node(clonedState, selectedNode, move);
			selectedNode.getChildArray().add(node);
		}
	}

	public static int MCTS_Simulation(Node currentNode) {
		Node clonedNode = currentNode;
		SaboteurBoardStateClone clonedState = clonedNode.getState();

		while(! clonedState.gameOver()) {
			ArrayList<SaboteurMove> legal_moves = clonedState.getAllLegalMoves();
			int random_index = (int) (Math.random() * legal_moves.size());
			System.out.println(legal_moves.size());
			//			clonedState.processMove(clonedState.getRandomMove());
			clonedState.processMove(legal_moves.get(random_index));

			// should we take care of toggling between player 1 and 2?
		}
		// can deduce who won depending on whose turn it is
		// this will be the "playout result" i.e. the id of the player that won
		return clonedState.getTurnPlayer();
	}

	public static void MCTS_Backpropagation(Node currentNode, int playoutResult) {
		Node tempNode = currentNode;
		while (tempNode != null) {
			tempNode.setVisitCount(tempNode.getVisitCount() + 1);
			// playoutResult is the ID of the player that won
			if (tempNode.getState().getTurnPlayer() == playoutResult) {
				tempNode.setWinScore(tempNode.getWinScore() + 1);
			}
			tempNode = tempNode.getParent();	
		}
	}
}