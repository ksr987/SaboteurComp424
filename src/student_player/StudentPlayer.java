package student_player;

import boardgame.Move;

import Saboteur.SaboteurPlayer;

import java.util.List;

import Saboteur.SaboteurBoardState;

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
    	long current = System.currentTimeMillis();
    	
    	SaboteurBoardStateClone clonedState = new SaboteurBoardStateClone(boardState);
    	Tree tree = new Tree(clonedState);
    	Node rootNode = tree.getRoot();
    	
    	
//    	// TODO: should be less than 2000, but by how much? see how much the rest of the function (after while loop) takes and add a margin of safety
//    	while (current - System.currentTimeMillis() < 30000) {
    		
//    		Node rootNode = new Node(null, null, 11,21,1);
//    		Node node1level1 = new Node(rootNode, null, 7,10,0);
//    		Node node2level1 = new Node(rootNode, null, 0,3,0);
//    		Node node3level1 = new Node(rootNode, null, 3,8,0);
//    		
//    		ArrayList<Node> children1Level1 = new ArrayList<Node>();
//    		children1Level1.add(node1level1);
//    		children1Level1.add(node2level1);
//    		children1Level1.add(node3level1);
//    		
//    		rootNode.childArray = children1Level1;
//    		
//    		Tree tree = new Tree(rootNode);
//    		
//    		Node node1level2 = new Node(node1level1, null, 2,4,1);
//    		Node node2level2 = new Node(node1level1, null, 1,6,1);
//    		
//    		ArrayList<Node> children1Level2 = new ArrayList<Node>();
//    		children1Level2.add(node1level2);
//    		children1Level2.add(node2level2);
//    		
//    		node1level1.childArray = children1Level2;
//    		
//    		Node node3level2 = new Node(node3level1, null, 1,2,1);
//    		Node node4level2 = new Node(node3level1, null, 2,3,1);
//    		Node node5level2 = new Node(node3level1, null, 2,3,1);
//    		
//    		ArrayList<Node> children2Level2 = new ArrayList<Node>();
//    		children2Level2.add(node3level2);
//    		children2Level2.add(node4level2);
//    		children2Level2.add(node5level2);
//    		
//    		node3level1.childArray = children2Level2;
//    		
//    		Node node1level3 = new Node(node2level2, null, 2,3,0);
//    		Node node2level3 = new Node(node2level2, null, 3,3,0);
//    		
//    		ArrayList<Node> children1Level3 = new ArrayList<Node>();
//    		children1Level3.add(node1level3);
//    		children1Level3.add(node2level3);
//    		
//    		node2level2.childArray = children1Level3;
//    		
    		// Selection
    		Node selectedNode = MyTools.MCTS_Selection(rootNode);
//    		
//    		Node node1level4 = new Node(node2level3, null, 0,0,1);
//    		
//    		ArrayList<Node> children1Level4 = new ArrayList<Node>();
//    		children1Level4.add(node1level4);
//    		
//    		node2level3.childArray = children1Level4;
    		
    		
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
    		
//    		MyTools.MCTS_Backpropagation(node1level4, 1);
    		
//    		System.out.println(node1level4.visitCount + " " + node1level4.winScore);
//    		System.out.println(node2level3.visitCount + " " + node2level3.winScore);
//    		System.out.println(node2level2.visitCount + " " + node2level2.winScore);
//    		System.out.println(node1level1.visitCount + " " + node1level1.winScore);
//    		System.out.println(rootNode.visitCount + " " + rootNode.winScore);
//    		
//    	}
//    	
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