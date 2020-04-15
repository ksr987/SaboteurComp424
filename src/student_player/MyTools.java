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
	static ArrayList<String> row_12_priority = new ArrayList<String>();
	

	/*
	 * hiddenPos: position of hidden objectives in the board
	 * LOW_PRIORITY, MEDIUM_PRIORITY, HIGH_PRIORITY: Priorities set for different types of tiles  
	 */
	static int[][] hiddenPos = {{12,3},{12,5},{12,7}};
	final double LOW_PRIORITY = 0.0;
	final double MEDIUM_PRIORITY = 50.0;
	final double HIGH_PRIORITY = 100.0;

	//Evaluation function that returns the heuristic of a legal move based on the board state
	public static double evaluateGreedyMove(Node node, SaboteurMove move) {
		SaboteurBoardStateClone boardState = node.getState();
		SaboteurCard cardPlayed = move.getCardPlayed();

		// will be useful to know the state before playing Destroy and Map
		SaboteurBoardStateClone parentBoardState = node.getParent().getState();

		/* For Bonus, if it is a legal move, then we are malused and we should play it anyway
		 * It is more important than a map
		 */
		if (cardPlayed instanceof SaboteurBonus)
			return 100000;

		/* For Map, it would matter way more to play it when the nugget is not revealed yet, 
		 * and would not have any use otherwise
		 */
		else if (cardPlayed instanceof SaboteurMap) {
			if (parentBoardState.isNuggetFound()) return -100; // No use playing Map if already know the position of nugget
			int positionPlayed = 0;

			for (int i = 0; i < hiddenPos.length; i++) {
				if (move.getPosPlayed()[0] == hiddenPos[i][0] && move.getPosPlayed()[1] == hiddenPos[i][1])
					positionPlayed = i;
			}
			// very confusing, but if we see current board state, the card has already been played so it's already
			// revealed. Therefore, should look at two states above to get our state the turn before (we have to look
			// one more up as we cannot see our cards from the opponent's perspective during his turn)
			try {
				//				SaboteurBoardStateClone grandparentBoardState = node.getParent().getParent().getState();
				if (boardState.getTurnPlayer() == 1) {
					if (parentBoardState.player2hiddenRevealed[positionPlayed]) {
						return -100;
					}
				} else if (parentBoardState.player1hiddenRevealed[positionPlayed]) {
					return -100;
				}
			} catch (Exception e) {
				System.out.println("No grandparent yet");
			}

			//			int positionPlayed = 0;
			//			for (int i = 0; i < hiddenPos.length; i++) {
			//				if (move.getPosPlayed()[0] == hiddenPos[i][0] && move.getPosPlayed()[1] == hiddenPos[i][1])
			//					positionPlayed = i;
			//			}
			//			if (clonedState.getTurnPlayer() == 1 && clonedState.player1hiddenRevealed[positionPlayed]) continue;
			//			if (clonedState.getTurnPlayer() == 2 && clonedState.player2hiddenRevealed[positionPlayed]) continue;
			return 10000; 
		}

		/* For Malus, it is a sounder idea to play it towards the end, because at the beginning we 
		 * are both working towards reaching the hidden tiles, but at the end we want to prevent the 
		 * opponent from doing so. Plus, it is more likely that the agent would have dropped Bonuses 
		 * throughout the game because they are not as high of a priority, so it may be scarce of it by the end.
		 */
		else if (cardPlayed instanceof SaboteurMalus) {
			if (boardState.getTurnNumber() < 30) return 0;
			else return 100;
		}

		/* For Drop, if you have Map, and nugget is revealed, it doesn't make sense to have it
		 * Same for deadends, they're just going to block path
		 */
		else if (cardPlayed instanceof SaboteurDrop) {
			SaboteurCard cardDropped = boardState.getCurrentPlayerCards().get(move.getPosPlayed()[0]);
			if (cardDropped instanceof SaboteurMap && boardState.isNuggetFound()) {
				return 10000000;
			}
			else if (cardDropped instanceof SaboteurTile) {
				String index = ((SaboteurTile) cardDropped).getIdx();
				if (map.get(index) == 0) {
					return 10;
				}
			}
		}

		/*Destroy the cards that are deadends and blocking a path from origin to a hidden tile
		 */
		else if (cardPlayed instanceof SaboteurDestroy) {


			//
			//			ArrayList<int[]> originTargets = new ArrayList<int[]>();
			//			for (int[] pos : hiddenPos) originTargets.add(pos);
			//			int[] targetPos = {5, 5};
			//			
			//			boolean cardPathUsingCard = boardState.cardPath(originTargets, targetPos, true);
			//			boolean cardPathHidden = boardState.pathToHidden(new SaboteurTile[]{new SaboteurTile("nugget"),new SaboteurTile("hidden1"),new SaboteurTile("hidden2")});
			//
			//			// if there is a card path from origin to a hidden tile but there is no path of 1s, then 
			//			// we can say that the road is blocked by a deadend card, we should remove it.
			//			// Note the exclusive or (we want either one or the other, but not both at same time)
			//
			//			if (cardPathUsingCard ^ cardPathHidden) {
			//				// get the tile at the position of the Destroy card
			//				SaboteurTile tile = boardState.getBoardForDisplay()[xpos][ypos];
			//				// third priority are deadends and have score 0
			//				if (opening_map.get(tile.getIdx()) == 0) return 10000000;
			//			}
			//			return 0;

			SaboteurTile cardPlayedOn = parentBoardState.getBoardForDisplay()[move.getPosPlayed()[0]][move.getPosPlayed()[1]];
			if (move.getPosPlayed()[0] == 12 && !row_12_priority.contains(cardPlayedOn.getIdx())) return 1000000000;
			if (map.get(cardPlayedOn.getIdx()) == 0) // we know it's 0 priority, deadend
				// TODO: better design, add "isInSquare" method
				if (move.getPosPlayed()[0] > 5 && move.getPosPlayed()[1] >= 3 && move.getPosPlayed()[1] <= 7) {
//					ArrayList<int[]> originTargets = new ArrayList<int[]>();
//					for (int[] pos : hiddenPos) originTargets.add(pos);
//					int[] targetPos = new int[] {5, 5};
//					boolean cardPath = boardState.cardPath(originTargets, targetPos, true);
//					if (cardPath) 
					return 100000000;
				}
		}


		/* The heuristic for a card is slightly more complex. It considers
		 * - the distance to the objective. 
		 * 		To is selected as: If the objective is found, it is the distance from card to golden nugget position. Else, it is the middle hidden card position (might change that if hidden placed at random)
		 * 		From is selected: If there is a path form the origin to the card, then the distance is from card, otherwise it's from origin (that's to give more priority to restore a broken path rather than extend it further)
		 * - the priority of the tile (pre-defined, but might consider dynamically changing based on other factors i.e. are we at end game?)
		 * - the direction of the tile. If there is end inwards (not going above entrance, or beyond hidden...), then it is TRUE (set to 1). Otherwise, set to FALSE 
		 */
		else if (cardPlayed instanceof SaboteurTile) {
			double distance;
			int[] beginningPosition;
			int[] endPosition = new int[2];

			// check whether there is a card path from card played to origin, otherwise the distance becomes that of origin to nugget or tile
			ArrayList<int[]> originTargets = new ArrayList<int[]>();
			int[] pos = {5, 5};
			originTargets.add(pos);
			int[] targetPos = move.getPosPlayed();

			if (isSuperMove(boardState, move)) return 1000000;

			boolean cardPath = boardState.cardPath(originTargets, targetPos, true);

			if (cardPath) beginningPosition = move.getPosPlayed();
			else beginningPosition = originTargets.get(0);

			if (boardState.isNuggetFound()) endPosition = boardState.getNuggetPosition();
			else {
				endPosition[0] = hiddenPos[1][0];
				endPosition[1] = hiddenPos[1][1];
			}

			distance = Math.sqrt(Math.pow(endPosition[1] - beginningPosition[1], 2) + Math.pow(endPosition[0] - beginningPosition[0], 2));

			String idx = ((SaboteurTile) cardPlayed).getIdx();
			int priority = map.get(idx);
			boolean isInwards = playedInwards(move, boardState);

			if (isInwards) return priority / distance;
			return 0;
		}
		return 0;
	}

	private static boolean isSuperMove(SaboteurBoardStateClone boardState, SaboteurMove move) {
		String index = ((SaboteurTile) move.getCardPlayed()).getIdx();
		int xPos = move.getPosPlayed()[1];
		int yPos = move.getPosPlayed()[0];

		if (yPos == 12) {
			if (xPos < 3) {
				if (index == "8" || index == "9" || index == "9_flip" || index == "10" || index == "6_flip" || index == "7" || index == "5") return true;		
			}
			if (xPos > 7) {
				if (index == "8" || index == "9" || index == "9_flip" || index == "10" || index == "6" || index == "7_flip" || index == "5_flip") return true;		
			}
			else {
				if (index == "8" || index == "9" || index == "9_flip" || index == "7" || index == "10" || index == "7_flip" || index == "5_flip" || index == "7" || index == "5") return true;		
			}
		}
//		if (yPos == 11) {
//			if (xPos < 3) {
//				if (index == "0" || index == "6_flip" || index == "7" || index == "8") return true;		
//			}
//			if (xPos > 7) {
//				if (index == "0" || index == "6_flip" || index == "7" || index == "8") return true;		
//			}
//			else {
//				if (index == "0" || index == "9_flip" || index == "7" || index == "8") return true;		
//
//			}
//		}

		return false;
	}

	//checks if the tile played is towards the hidden objectives, or away from it based on the tile path and the position of the move
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

	/**
	 * This method finds the move from the current set of legal moves that would lead us closest to the nugget
	 * It uses hypothenuse formula, but it's better to compare from the 1-end of the card path.
	 * An improvement would be Manhattan distance, (knowing our cards and 
	 * estimating the cards left to draw using knowledge of the cards played) from all possible paths and picking 
	 * (and sticking to) the least.

	 * @param nuggetPosition
	 * @param legalMoves
	 * @return SaboteurMove the closest move
	 */
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

	/**
	 * method that selects the leaf node in a tree starting from root node, based on the Upper Confidence Tree calculation 
	 * @param rootNode
	 * @return leaf node
	 */
	public static Node MCTS_Selection(Node rootNode) {
		Node parentNode = rootNode;

		// while we still haven't reached a leaf...
		while(parentNode.getChildArray().size()>0) {
			double max_uct = 0;
			Node currentNode = parentNode.getChildArray().get(0);

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
				}
			}
			parentNode = currentNode;
		}
		return parentNode;
	}

	/**
	 * Expand all the possible legal moves by creating new node for each legal move
	 * @param selectedNode
	 */
	public static void MCTS_Expansion(Node selectedNode) {
		SaboteurBoardStateClone clonedState = new SaboteurBoardStateClone(selectedNode.getState());
		ArrayList<SaboteurMove> legal_moves = clonedState.getAllLegalMoves();

		for (SaboteurMove move : legal_moves) {
			try {
				clonedState = new SaboteurBoardStateClone(selectedNode.getState());

				SaboteurBoardStateClone newClonedState = clonedState.processMove(move);
				Node node = new Node(newClonedState, selectedNode, move);
				double heuristic = MyTools.evaluateGreedyMove(node, move);
				node.setHeuristic(heuristic);
				// Here, we should be adding nodes in descending order of their heuristic so that in simulation
				// we pick the one
				selectedNode.getChildArray().add(node);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Simulate the game play by taking the leaf node, and running the subsequent moves randomly
	 * @param currentNode
	 * @return the result of the playout
	 */
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
						e.printStackTrace();
					}
				}
				else {
					clonedState = clonedState.processMove(clonedState.getRandomMove());
				}
			}

		}
		int winner = clonedState.getWinner();
		if (winner == currentNode.getState().getTurnPlayer()) {
			return 1;	// our player won
		}
		else if (winner == Integer.MAX_VALUE) return 0.5;	// it's a draw
		else return 0; //our player lost
	}

	/**
	 * Backpropagate the result of the playout to all the nodes until we reach root node
	 * @param currentNode
	 * @param playoutResult
	 */
	public static void MCTS_Backpropagation(Node currentNode, double playoutResult) {
		Node tempNode = currentNode;
		while (tempNode != null) {
			tempNode.setVisitCount(tempNode.getVisitCount() + 1);

			if(playoutResult==0.5) {
				tempNode.setWinScore(tempNode.getWinScore()+0.5);
			}
			// playoutResult is the ID of the player that won
			else if (tempNode.getState().getTurnPlayer() == playoutResult) {
				tempNode.setWinScore(tempNode.getWinScore() + 1);
			}
			tempNode = tempNode.getParent();	
		}
	}

	/**
	 * Add priority of the tiles based on if it leads to continuous path or blocked path
	 * we split them into three categories of priority i.e. the more versatile ones, the deadends, 
	 * and the rest (at priority 2)
	 */
	public static void addPriorityTiles() {

		//add indexes of tiles in different priority lists
		String[] firstPriority = {"0", "5", "6", "8", "9", "10", "6_flip", "7_flip"};
		String[] secondPriority = {"5_flip", "7", "9_flip", "1"};
		String[] thirdPriority = {"2", "2_flip", "3", "3_flip", "4", "4_flip", "11", "11_flip", "12", "12_flip", "14", "14_flip", "15", "13"};

		for(String tile: firstPriority) map.put(tile, 100);
		for(String tile: secondPriority) map.put(tile, 50);
		for(String tile: thirdPriority) map.put(tile, 0);		
		
		String[] arr = {"8", "9", "9_flip", "7", "10", "7_flip", "5_flip", "7", "5"};
		for (String a : arr) row_12_priority.add(a);
	}
}