package student_player;

import boardgame.Move;

import Saboteur.SaboteurPlayer;

import java.util.ArrayList;
import java.util.List;

import Saboteur.SaboteurBoardState;

/** A player file submitted by a student. */
public class StudentPlayer extends SaboteurPlayer {

	/*
	 * Student IDs of both team members initialized to a final variable
	 */
	private static final String KETAN_SID = "260732873";
	private static final String ALAIN_SID = "260714615";
	
	private static final int timeout = 2;
	
    /**
     * You must modify this constructor to return your student number. This is
     * important, because this is what the code that runs the competition uses to
     * associate you with your agent. The constructor should do nothing else.
     */
    public StudentPlayer() {
    	
        super(ALAIN_SID);
    }
    					
    /**
     * This is the primary method that you need to implement. The ``boardState``
     * object contains the current state of the game, which your agent must use to
     * make decisions.
     */
    public Move chooseMove(SaboteurBoardState boardState) {
    	
    	/*
    	 * Debugging space
    	 */
    	int[] origin = {5, 5};
    	ArrayList<int[]> card = new ArrayList<int[]>();
    	int[] posPlayed = {5, 4};
    	card.add(posPlayed);
    	// there should definitely be a path of card and 1 for those straight tunnel things especially at beginning,
    	// so both should return true, but they don't
		System.out.println("There is a path of cards? " + boardState.cardPath(card, origin, true));
		System.out.println("There is a path of 1s? " + boardState.cardPath(card, origin, false));
    	
		// end of debugging space
		
    	//add priorities of tiles in the hashmap
    	if (MyTools.opening_map.isEmpty()) MyTools.addPriorityTiles();
    	
    	long initial = System.currentTimeMillis();
    	
    	SaboteurBoardStateClone clonedState = new SaboteurBoardStateClone(boardState);
    	Tree tree = new Tree(clonedState);
    	Node rootNode = tree.getRoot();
    	
    	//perform MCTS steps before the timeout of 2 sec (with a safety gap)
    	while (System.currentTimeMillis() - initial < (timeout * 999)) {
    				
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
    			//find move to play with max heuristic
    			double maxHeuristic = Integer.MIN_VALUE;
    			Node maxNode = childArray.get(0);
    			for(Node node: childArray) {
    				if(node.heuristic >maxHeuristic) {
    					maxHeuristic = node.heuristic;
    					maxNode = node;
    				}
    			}
    			System.out.println("Picked node " + maxNode.getMovePlayed() + "with heuristic " + maxNode.heuristic);
    			simulation_node = maxNode;
    		}
    		
    		//store result of simulation
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
    	
    	//verify if the move associated with the picked node is legal
    	if (!boardState.isLegal(picked_node.getMovePlayed())) {
    		myMove = boardState.getRandomMove();
    	}
        return myMove;
    }
}