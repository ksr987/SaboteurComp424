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

	static boolean goldenNuggetFound = false;
	static int[][] hiddenPos = {{12,3},{12,5},{12,7}};
	static int[] goldenNuggetPosition = {};
	static HashMap<SaboteurTile, Integer> map = new HashMap<SaboteurTile, Integer>();
	
	final int LOW_PRIORITY = 0;
	final int MEDIUM_PRIORITY = 50;
	final int HIGH_PRIORITY = 100;
	
	// We already know all the moves passed in this function are legal
	public static double evaluateGreedyMove(SaboteurBoardStateClone boardState, SaboteurMove move) {

		SaboteurCard cardPlayed = move.getCardPlayed();
		
		/**
		 * For Bonus, if it is a legal move, then we are malused and we should play it anyway
		 * It is more important than a map
		 */
		if (cardPlayed instanceof SaboteurBonus)
			return 1000;

		/**
		 * For Map, it would matter way more to play it when the nugget is not revealed yet, 
		 * and would not have any use otherwise
		 */
		else if (cardPlayed instanceof SaboteurMap) {
			if (goldenNuggetFound) return -100; // No use playing Map if already know the position of nugget
			else return 100; 
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
//		else if (cardPlayed instanceof SaboteurDestroy) {
//
//		}

		else if (cardPlayed instanceof SaboteurTile) {
			if (goldenNuggetFound) {
				double distance = Math.sqrt(Math.pow(move.getPosPlayed()[0] - goldenNuggetPosition[0], 2) + Math.pow(move.getPosPlayed()[1] - goldenNuggetPosition[1], 2));
				return 0;
			}
			else {
				String idx = ((SaboteurTile) cardPlayed).getIdx();
				return map.get(idx);
			}
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
		//if (! goldenNuggetFound) goldenNuggetFound(clonedState);

		//System.out.println("Legal Moves size: " + legal_moves.size());
		for (SaboteurMove move : legal_moves) {
			try {
				clonedState = new SaboteurBoardStateClone(selectedNode.getState());
				SaboteurBoardStateClone newClonedState = clonedState.processMove(move);
				// TODO: For now, all heuristics are 0
				double heuristic = MyTools.evaluateGreedyMove(clonedState, move);
				//				Node node = new Node(newClonedState, selectedNode, move);
				Node node = new Node(newClonedState, selectedNode, move, heuristic);
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
	
public static void addPriorityTiles() {
		
		SaboteurTile[] firstPriority = {new SaboteurTile("0"), new SaboteurTile("5"),new SaboteurTile("6"),
				new SaboteurTile("8"),new SaboteurTile("9"),new SaboteurTile("10"), new SaboteurTile("6_flip"),
				new SaboteurTile("7_flip")};
		SaboteurTile[] secondPriority = {new SaboteurTile("5_flip"), new SaboteurTile("7"), new SaboteurTile("9_flip")};
		SaboteurTile[] thirdPriority = {new SaboteurTile("1"), new SaboteurTile("2"), new SaboteurTile("2_flip"), 
				new SaboteurTile("3"), new SaboteurTile("3_flip"), new SaboteurTile("4"), new SaboteurTile("4_flip"),
				new SaboteurTile("11"),new SaboteurTile("11_flip"), new SaboteurTile("12"), new SaboteurTile("12_flip"),
				new SaboteurTile("13"), new SaboteurTile("14"), new SaboteurTile("14_flip"),new SaboteurTile("15")};
		
		for(SaboteurTile tile: firstPriority) map.put(tile, 100);
		for(SaboteurTile tile: secondPriority) map.put(tile, 50);
		for(SaboteurTile tile: thirdPriority) map.put(tile, 0);		
	}
}