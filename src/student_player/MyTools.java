package student_player;

import java.util.ArrayList;
import Saboteur.SaboteurMove;
import Saboteur.cardClasses.SaboteurBonus;
import Saboteur.cardClasses.SaboteurCard;
import Saboteur.cardClasses.SaboteurDestroy;
import Saboteur.cardClasses.SaboteurDrop;
import Saboteur.cardClasses.SaboteurMalus;
import Saboteur.cardClasses.SaboteurMap;
import Saboteur.cardClasses.SaboteurTile;

public class MyTools {

	static boolean goldenNuggetFound = false;
	static int[][] hiddenPos = {{12,3},{12,5},{12,7}};
	static int[] goldenNuggetPosition = {};
	
	// We already know all the moves passed in this function are legal
	public static double evaluateGreedyMove(SaboteurBoardStateClone boardState, SaboteurMove move) {

		SaboteurCard cardPlayed = move.getCardPlayed();
		
		if (cardPlayed instanceof SaboteurBonus) return 0;
		
		else if (cardPlayed instanceof SaboteurMap) {
			if (goldenNuggetFound) return 0;
			else return 0; // No use playing Map if already know the position of nugget
		}
		
		else if (cardPlayed instanceof SaboteurMalus) return 0;
		
		else if (cardPlayed instanceof SaboteurDestroy) return 0;
		
		else if (cardPlayed instanceof SaboteurTile) {
			if (goldenNuggetFound) {
				double distance = Math.sqrt(Math.pow(move.getPosPlayed()[0] - goldenNuggetPosition[0], 2) + Math.pow(move.getPosPlayed()[1] - goldenNuggetPosition[1], 2));
				return 0;
			}
			else return 0;
		}
		
		return 0;
	}

	public static SaboteurMove findClosestMove(int[] nuggetPosition, ArrayList<SaboteurMove> legalMoves) {
		double currentMin = Integer.MAX_VALUE;
		SaboteurMove minMove = legalMoves.get(0);
		for (SaboteurMove move : legalMoves) {
			int x = move.getPosPlayed()[0];
			int y = move.getPosPlayed()[1];
			double distance = Math.sqrt(Math.pow(x - nuggetPosition[0], 2) + Math.pow(y - nuggetPosition[1], 2));
			if (distance < currentMin) {
				currentMin = distance;
				minMove=  move;
			}
		}
		return minMove;
	}

	public static Node MCTS_Selection(Node rootNode) {
		Node parentNode = rootNode;

		// while we still haven't reached a leaf...
		while(!parentNode.getChildArray().isEmpty()) {

			double max_uct = 0;
			Node currentNode = parentNode;

			// find the child node with maximum UCT

			for (Node node : parentNode.getChildArray()) {
				double wins = node.getWinScore();
				int total_simulations_parent = parentNode.getVisitCount();
				int total_simulations_child = node.getVisitCount();

				double uct = (double) (wins) / (double) (total_simulations_child)
						+ Math.sqrt(2 * Math.log(total_simulations_parent) / (double) total_simulations_child)
						+ (node.heuristic / (double) (total_simulations_child));

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
		SaboteurBoardStateClone clonedState = new SaboteurBoardStateClone(selectedNode.getState());
		ArrayList<SaboteurMove> legal_moves = clonedState.getAllLegalMoves();
		if (! goldenNuggetFound) goldenNuggetFound(clonedState);

		//System.out.println("Legal Moves size: " + legal_moves.size());
		for (SaboteurMove move : legal_moves) {
			try {
				clonedState = new SaboteurBoardStateClone(selectedNode.getState());
				SaboteurBoardStateClone newClonedState = clonedState.processMove(move);
				// TODO: For now, all heuristics are 0
				double heuristic = MyTools.evaluateGreedyMove(clonedState, move);

				Node node = new Node(newClonedState, selectedNode, move, heuristic);
				selectedNode.getChildArray().add(node);
				//System.out.println(move.toPrettyString());
			} catch (Exception e) {
				e.printStackTrace();
				//System.out.println(Arrays.deepToString(clonedState.getHiddenBoard()).replace("], ", "]\n"));
			}
		}
	}

	public static double MCTS_Simulation(Node currentNode) {
		Node clonedNode = currentNode;
		SaboteurBoardStateClone clonedState = clonedNode.getState();
		while(! clonedState.gameOver()) {
			ArrayList<SaboteurMove> legal_moves = clonedState.getAllLegalMoves();
			int random_index = (int) (Math.random() * legal_moves.size());
			clonedState = clonedState.processMove(legal_moves.get(random_index));
		}
		int winner = clonedState.getWinner();
		if (winner == currentNode.getState().getTurnPlayer()) return 1;	// our player won
		else if (winner == Integer.MAX_VALUE) return 0.5;	// it's a draw
		else return 0;
	}

	public static void MCTS_Backpropagation(Node currentNode, double playoutResult) {
		Node tempNode = currentNode;
		while (tempNode != null) {
			tempNode.setVisitCount(tempNode.getVisitCount() + 1);

			if(playoutResult==0.5) {
				tempNode.setWinScore(tempNode.getWinScore()+0.5);
				// tempNode.setWinScore(tempNode.getWinScore() + 0.5 * tempNode.heuristic);

			}
			// playoutResult is the ID of the player that won
			else if (tempNode.getState().getTurnPlayer() == playoutResult) {
				tempNode.setWinScore(tempNode.getWinScore() + 1);
				// tempNode.setWinScore(tempNode.getWinScore() + tempNode.heuristic);
			}
			tempNode = tempNode.getParent();	
		}
	}

	public static void goldenNuggetFound(SaboteurBoardStateClone boardState) {
		SaboteurTile[][] boardForDisplay = boardState.getBoardForDisplay();

		for (int[] position : hiddenPos) {
			SaboteurTile hiddenTile = boardForDisplay[position[0]][position[1]];
			if (hiddenTile != null ) {
				// TODO: Make sure it's indeed "nugget"
				if (hiddenTile.getName() == "nugget") {
					goldenNuggetFound = true;
					goldenNuggetPosition[0] = position[0];
					goldenNuggetPosition[1] = position[1];	
				}
			}
		}
	}
}