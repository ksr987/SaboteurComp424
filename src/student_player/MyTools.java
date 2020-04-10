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
		ArrayList<SaboteurCard> player_cards = selectedNode.getState().getUpdatedCurrentPlayerCards();
		int player_id = selectedNode.getState().getTurnPlayer();
		boolean is_malused = false;
		boolean have_to_drop = true;

		// if card played by opponent is Malus, then should play Bonus otherwise drop card.
		if (legal_moves.size() == 1) is_malused = true;


		for (SaboteurCard card : player_cards) {

			if (card instanceof SaboteurMalus && !is_malused) {
				
				//sabmove constructor -> card to be moved, x,y,player id
				SaboteurMove move = new SaboteurMove(card, 0, 0, player_id);
				SaboteurBoardStateClone clonedState = selectedNode.getState();
				clonedState.processMove(move);
				Node node = new Node(clonedState, selectedNode, move);
				selectedNode.getChildArray().add(node);
				
				//TODO: have_to_drop???
				have_to_drop = false;
			}

			// doesn't matter if malus'ed or not
			else if (card instanceof SaboteurMap) {

				SaboteurTile[][] tiles = selectedNode.getState().getUpdatedHiddenBoard();
				for (int x = 0; x < tiles.length; x++) {
					for (int y = 0; y < tiles[x].length; y++) {
						// -1 means empty i.e. hidden objective found
						System.out.println(tiles[x][y].getName());
						if (tiles[x][y].getIdx().equals("8") && !tiles[x][y].getIdx().equals("entrance")) {
							SaboteurMove move = new SaboteurMove(new SaboteurMap(), x, y, player_id);
							SaboteurBoardStateClone clonedState = selectedNode.getState();
							clonedState.processMove(move); // should have catch in case of exception (do for each processMove below as well)
							Node node = new Node(clonedState, selectedNode, move);
							selectedNode.getChildArray().add(node);
							have_to_drop = false;
						}
					}
				}
			}

			// doesn't matter if malus'ed or not
			else if (card instanceof SaboteurDestroy) {
				// TODO: Get all tile cards except entrance and hidden objects, then make one move per tile card
				SaboteurTile[][] tiles = selectedNode.getState().getUpdatedHiddenBoard();
				for (int x = 0; x < tiles.length; x++) {
					for (int y = 0; y < tiles[x].length; y++) {
						// 8 is the card index for hidden objectives (and start point too?)
						if (tiles[x][y].getIdx() != "8") {
							SaboteurMove move = new SaboteurMove(new SaboteurDestroy(), x, y, player_id);
							SaboteurBoardStateClone clonedState = selectedNode.getState();
							clonedState.processMove(move); // should have catch in case of exception (do for each processMove below as well)
							Node node = new Node(clonedState, selectedNode, move);
							selectedNode.getChildArray().add(node);
							have_to_drop = false;
						}
					}
				}
			}

			else if (card instanceof SaboteurTile && !is_malused) {
				ArrayList<int[]> positions = selectedNode.getState().possiblePositions((SaboteurTile) card);
				positions.addAll(selectedNode.getState().possiblePositions(((SaboteurTile) card).getFlipped()));
				for(int i=0;i<positions.size();i++) {
					System.out.print(Arrays.toString(positions.get(i)));
				}
				for (int[] position : positions) {
					SaboteurMove move = new SaboteurMove(new SaboteurTile(card.getName()), position[0], position[1], selectedNode.getState().getTurnPlayer());
					SaboteurBoardStateClone clonedState = new SaboteurBoardStateClone(selectedNode.getState());
					Node node = new Node (clonedState, selectedNode, move);
					selectedNode.getChildArray().add(node);
					have_to_drop = false;
					clonedState.processMove(move);
				}
			}

			else if (card instanceof SaboteurBonus && is_malused) {
				SaboteurMove move = new SaboteurMove(card, 0, 0, selectedNode.getState().getTurnPlayer());
				SaboteurBoardStateClone clonedState = selectedNode.getState();
				clonedState.processMove(move);
				Node node = new Node (clonedState, selectedNode, move);
				selectedNode.getChildArray().add(node);
				have_to_drop = false;
			}

		}

		// if all our moves are illegal, then drop card (should be tile preferably)
		if (have_to_drop) {

			boolean dropped = false;
			for (int i = 0; i < player_cards.size(); i++) {
				if (player_cards.get(i) instanceof SaboteurTile) {
					dropped = true;
					SaboteurMove move = new SaboteurMove(new SaboteurDrop(), i, 0, selectedNode.getState().getTurnPlayer());
					SaboteurBoardStateClone clonedState = selectedNode.getState();
					clonedState.processMove(move);
					Node node = new Node (clonedState, selectedNode, move);
					selectedNode.getChildArray().add(node);
					// break; maybe one drop move per tile, maybe only one drop?
				}
			}
			// if no card we have is a Tile...
			if (!dropped) {
				int random_card_index = (int) (Math.random() * player_cards.size());
				SaboteurMove move = new SaboteurMove(new SaboteurDrop(), random_card_index, 0, selectedNode.getState().getTurnPlayer());
				SaboteurBoardStateClone clonedState = selectedNode.getState();
				clonedState.processMove(move);
				Node node = new Node (clonedState, selectedNode, move);
				selectedNode.getChildArray().add(node);

			}
		}
	}

	public static int MCTS_Simulation(Node currentNode) {
		Node clonedNode = currentNode;
		SaboteurBoardStateClone clonedState = clonedNode.getState();
		int our_id = 1; // fix 

		while(! clonedState.gameOver()) {
//			ArrayList<SaboteurMove> legal_moves = clonedState.getAllLegalMoves();
//			int random_index = (int) (Math.random() * legal_moves.size());
			clonedState.processMove(clonedState.getRandomMove());
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