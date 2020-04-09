package student_player;

import java.util.ArrayList;
import java.util.List;

import Saboteur.SaboteurBoardState;

public class Node {
    Node parent;
    List<Node> childArray;
    SaboteurBoardState state;
    int visitCount;
    int winScore;
    
    public Node(SaboteurBoardState state) {
        this.state = state;
        childArray = new ArrayList<>();
    }

    
    public Node(SaboteurBoardState state, Node parent) {
        this.state = state;
        this.parent = parent;
        this.childArray = new ArrayList<>();
    }
    
    public Node(SaboteurBoardState state, Node parent, List<Node> childArray) {
        this.state = state;
        this.parent = parent;
        this.childArray = childArray;
    }
    
    public Node getParent() {
        return parent;
    }

    public void setParent(Node parent) {
        this.parent = parent;
    }

    public List<Node> getChildArray() {
        return childArray;
    }

    public void setChildArray(List<Node> childArray) {
        this.childArray = childArray;
    }
    

	public int getWinScore() {
		return winScore;
	}

	public int getVisitCount() {
		return visitCount;
	}

	public SaboteurBoardState getState() {
		return state;
	}
}