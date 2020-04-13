package student_player;

import boardgame.Move;

import Saboteur.SaboteurPlayer;

import java.util.List;

import Saboteur.SaboteurBoardState;

/** A player file submitted by a student. */
public class StudentPlayer extends SaboteurPlayer {

	private static final String KETAN_SID = "260732873";
	private static final String ALAIN_SID = "260714615";
	private static final int timeout = 1;
	public static int player_id;
	
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
    	
    	if (MyTools.map.isEmpty()) MyTools.addPriorityTiles();
    	
    	long initial = System.currentTimeMillis();
    	
    	SaboteurBoardStateClone clonedState = new SaboteurBoardStateClone(boardState);
    	Tree tree = new Tree(clonedState);
    	Node rootNode = tree.getRoot();
    	
//    	// TODO: should be less than 2000, but by how much? see how much the rest of the function (after while loop) takes and add a margin of safety
    	while (System.currentTimeMillis() - initial < (timeout * 900)) {
    				
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
    			double maxHeuristic = Integer.MIN_VALUE;
    			Node maxNode = childArray.get(0);
    			for(Node node: childArray) {
    				if(node.heuristic >maxHeuristic) {
    					maxHeuristic = node.heuristic;
    					maxNode = node;
    				}
    			}
    			System.out.println(maxNode.heuristic);
    			//int random_index = (int) (childArray.size() * Math.random());
    			simulation_node = maxNode;
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
    	if (!boardState.isLegal(picked_node.getMovePlayed())) {
    		System.out.println("Move is illegal");
    		myMove = boardState.getRandomMove();
    	}
        return myMove;
    }
}
