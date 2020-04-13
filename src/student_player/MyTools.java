package student_player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.PriorityQueue;
import Saboteur.SaboteurMove;
import Saboteur.cardClasses.SaboteurBonus;
import Saboteur.cardClasses.SaboteurCard;
import Saboteur.cardClasses.SaboteurDestroy;
import Saboteur.cardClasses.SaboteurDrop;
import Saboteur.cardClasses.SaboteurMalus;
import Saboteur.cardClasses.SaboteurMap;
import Saboteur.cardClasses.SaboteurTile;

public class MyTools {

	static HashMap<String, Integer> map = new HashMap<String, Integer>();
	static int[][] hiddenPos = {{12,3},{12,5},{12,7}};
	final double LOW_PRIORITY = 0.0;
	final double MEDIUM_PRIORITY = 50.0;
	final double HIGH_PRIORITY = 100.0;

	// We already know all the moves passed in this function are legal
	public static double evaluateGreedyMove(SaboteurBoardStateClone boardState, SaboteurMove move) {

		SaboteurCard cardPlayed = move.getCardPlayed();

		/**
		 * For Bonus, if it is a legal move, then we are malused and we should play it anyway
		 * It is more important than a map
		 */
		if (cardPlayed instanceof SaboteurBonus)
			return 100000;

		/**
		 * For Map, it would matter way more to play it when the nugget is not revealed yet, 
		 * and would not have any use otherwise
		 */
		else if (cardPlayed instanceof SaboteurMap) {
			if (boardState.isNuggetFound()) return -100; // No use playing Map if already know the position of nugget
			else return 10000; 
		}

		/**
		 * For Malus, it is a sounder idea to play it towards the end, because at the beginning we 
		 * are both working towards reaching the hidden tiles, but at the end we want to prevent the 
		 * opponent from doing so. Plus, it is more likely that the agent would have dropped Bonuses 
		 * throughout the game because they are not as high of a priority, so it may be scarce of it by the end.

		 */
		else if (cardPlayed instanceof SaboteurMalus) {
			if (boardState.getTurnNumber() < 25) return 0;
			else return 100;
		}
		/**
		 * For Destroy, it matters more which card are we destroying, so we set a heuristic score 
		 * according to that. However, it also matters not to sabotage ourselves, and the path the 
		 * we are building. We can compute the most likely path since we know our cards and the cards 
		 * that can be played from the opponent and those that can be drawn. 
		 */

		// Destroy the cards that are playing outwards, giving more priority to the tiles closer to hidden objective
		else if (cardPlayed instanceof SaboteurDestroy) {

			int xpos = move.getPosPlayed()[0];
			int ypos = move.getPosPlayed()[1];

			if(boardState.getHiddenBoard()[xpos][ypos]!=null) {

				if(playedInwards(move, boardState)) return -100;
				else return 100;
			}

		}

		else if (cardPlayed instanceof SaboteurTile) {
			double distance;
			if (boardState.isNuggetFound()) {
				int[] goldenNuggetPosition = boardState.getNuggetPosition();
				//				System.out.print("GOLDEN NUGGET FOUND");
				distance = Math.sqrt(Math.pow(move.getPosPlayed()[0] - goldenNuggetPosition[0], 2) + Math.pow(move.getPosPlayed()[1] - goldenNuggetPosition[1], 2));
			}

			else {
				distance = Math.sqrt(Math.pow(move.getPosPlayed()[0] - hiddenPos[1][0], 2) + Math.pow(move.getPosPlayed()[1] - hiddenPos[1][1], 2));
			}
			String idx = ((SaboteurTile) cardPlayed).getIdx();
			int priority = map.get(idx);
			boolean isInwards = playedInwards(move, boardState);
			if (isInwards) return priority / distance;
			return 0;
		}
		return 0;
	}

	// This method identifies whether the card played is going inwards i.e. towards 
	// the hidden objectives. At least one of its extremes should be inside of the "square" formed by the entrace and hidden tiles

	private static boolean playedInwards(SaboteurMove move, SaboteurBoardStateClone boardState) {

		//this method only valid for SaboteurTile instance
		if(!(move.getCardPlayed() instanceof SaboteurTile)) return false;

		int[][] tilePath = ((SaboteurTile) move.getCardPlayed()).getPath();
		boolean topMiddleOpen = tilePath[1][2] ==1;
		boolean leftMiddleOpen = tilePath[0][1] ==1;
		boolean rightMiddleOpen = tilePath[2][1] ==1;
		boolean bottomMiddleOpen = tilePath[1][0] ==1;

		//left square
		//consider only 3 edges as the common edge can be flexible

		//top edge
		if(move.getPosPlayed()[1]<=5 && !(leftMiddleOpen || rightMiddleOpen || bottomMiddleOpen) && topMiddleOpen) return false;

		//left edge
		if(move.getPosPlayed()[0]<=3 && !(topMiddleOpen || rightMiddleOpen || bottomMiddleOpen) && leftMiddleOpen) return false;

		//bottom edge
		if(move.getPosPlayed()[1]>=12 && !(topMiddleOpen || rightMiddleOpen || leftMiddleOpen) && bottomMiddleOpen) return false;

		//right square

		//right edge
		if(move.getPosPlayed()[0]>=7 && !(topMiddleOpen || leftMiddleOpen || bottomMiddleOpen) && rightMiddleOpen) return false;

		return true;
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
//			boolean shouldExitLoop = true;
			double max_uct = 0;
			Node currentNode = parentNode;

			// find the child node with maximum UCT
			for (Node node : parentNode.getChildArray()) {
				double wins = node.getWinScore();
				int total_simulations_parent = parentNode.getVisitCount();
				int total_simulations_child = node.getVisitCount();

				double uct = (double) (wins) / (double) (total_simulations_child)
						+ Math.sqrt(2 * Math.log(total_simulations_parent) / (double) total_simulations_child)
						+ node.heuristic / (double) (total_simulations_child);

				if (uct > max_uct) {
					max_uct = uct;
					currentNode = node;
//					shouldExitLoop = false;
				}
			}
			parentNode = currentNode;
//			if (shouldExitLoop == true) {
//				return parentNode;
//			}
		}
		return parentNode;
	}

	public static void MCTS_Expansion(Node selectedNode) {
		SaboteurBoardStateClone clonedState = new SaboteurBoardStateClone(selectedNode.getState());
		ArrayList<SaboteurMove> legal_moves = clonedState.getAllLegalMoves();

		//System.out.println("Legal Moves size: " + legal_moves.size());
		for (SaboteurMove move : legal_moves) {
			try {
				clonedState = new SaboteurBoardStateClone(selectedNode.getState());
				SaboteurBoardStateClone newClonedState = clonedState.processMove(move);
				double heuristic = MyTools.evaluateGreedyMove(clonedState, move);
				Node node = new Node(newClonedState, selectedNode, move, heuristic);
				//				System.out.println("Heuristic for " + move.getCardPlayed().getName() + " is " + heuristic);
				// Here, we should be adding nodes in descending order of their heuristic so that in simulation
				// we pick the one
				selectedNode.getChildArray().add(node);
			} catch (Exception e) {
				e.printStackTrace();
				//System.out.println(Arrays.deepToString(clonedState.getHiddenBoard()).replace("], ", "]\n"));
			}
		}
	}

	public static double MCTS_Simulation(Node currentNode) {
		Node clonedNode = currentNode;
		SaboteurBoardStateClone clonedState = clonedNode.getState();
		ArrayList<SaboteurMove> legal_moves = clonedState.getAllLegalMoves();
		while(! clonedState.gameOver() && legal_moves.size()>0) {
			legal_moves = clonedState.getAllLegalMoves();
			if(legal_moves.size()>0) {
				int random_index = (int) (Math.random() * legal_moves.size());
				if(clonedState.isLegal(legal_moves.get(random_index))) {
					try {
						clonedState = clonedState.processMove(legal_moves.get(random_index));		
					} catch (Exception e) {
						System.out.println("NIMIT IS HIGH");
					}
				}
				else {
					clonedState = clonedState.processMove(clonedState.getRandomMove());
				}
			}

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

	public static void addPriorityTiles() {

		String[] firstPriority = {"0", "5", "6", "8", "9", "10", "6_flip", "7_flip", "13"};
		String[] secondPriority = {"5_flip", "7", "9_flip", "1", "2", "2_flip"};
		String[] thirdPriority = {"1", "2", "2_flip", "3", "3_flip", "4", "4_flip", "11", "11_flip", "12", "12_flip", "14", "14_flip", "15"};


		for(String tile: firstPriority) map.put(tile, 100);
		for(String tile: secondPriority) map.put(tile, 50);
		for(String tile: thirdPriority) map.put(tile, 0);		
	}
}