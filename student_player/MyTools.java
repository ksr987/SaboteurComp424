package student_player;

import java.util.ArrayList;

import Saboteur.SaboteurBoardState;
import Saboteur.SaboteurMove;
import Saboteur.cardClasses.SaboteurBonus;
import Saboteur.cardClasses.SaboteurCard;
import Saboteur.cardClasses.SaboteurDestroy;
import Saboteur.cardClasses.SaboteurMalus;
import Saboteur.cardClasses.SaboteurMap;
import Saboteur.cardClasses.SaboteurTile;

public class MyTools {
	public static double getSomething() {
		return Math.random();
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
		ArrayList<SaboteurCard> player_cards = selectedNode.getState().getCurrentPlayerCards();
		SaboteurBoardState clonedState = selectedNode.getState();

		int player_id = clonedState.getTurnPlayer();
		
		// TODO: if card played by opponent is Malus, then should play Bonus other drop card
		
		for (SaboteurCard card : player_cards) {
			
			if (card instanceof SaboteurMalus) {
				SaboteurMove move = new SaboteurMove(card, 0, 0, player_id);
				clonedState.processMove(move);
				Node node = new Node(clonedState, selectedNode);
			}
			
			else if (card instanceof SaboteurMap) {
				
				SaboteurTile[][] tiles = clonedState.getHiddenBoard();
				for (int x = 0; x < tiles.length; x++) {
					for (int y = 0; y < tiles[x].length; y++) {
						if (tiles[x][y].getIdx() == "8") {
							SaboteurMove move = new SaboteurMove(new SaboteurMap(), x, y, player_id);
							clonedState.processMove(move);
							Node node = new Node(clonedState, selectedNode);
						}
					}
				}
			}
			
			else if (card instanceof SaboteurDestroy) {
				// TODO: Get all tile cards except entrance and hidden objects, then make one move per tile card
			}
			
			else if (card instanceof SaboteurTile) {
				ArrayList<int[]> positions = clonedState.possiblePositions((SaboteurTile) card);
				positions.addAll(clonedState.possiblePositions(((SaboteurTile) card).getFlipped()));
				for (int[] position : positions) {
					SaboteurMove move = new SaboteurMove(card, position[0], position[2], clonedState.getTurnPlayer());
					Node node = new Node (clonedState, selectedNode);
				}
			}
		
		}
		
		// TODO: if all our moves are illegal, then drop card

		
	}

	public static int MCTS_Simulation(Node currentNode) {
		// TODO Auto-generated method stub
		return 0;
	}

	public static void MCTS_Backpropagation(Node currentNode, int playoutResult) {
		// TODO Auto-generated method stub

	}
}