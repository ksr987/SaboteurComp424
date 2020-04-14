package student_player;

import java.util.ArrayList;
import java.util.List;

import Saboteur.SaboteurMove;

public class Node {
    Node parent;
    List<Node> childArray;
    SaboteurBoardStateClone state;
    SaboteurMove movePlayed;
    int visitCount;
    double winScore;
    double heuristic;
    
    public Node(SaboteurBoardStateClone state) {
        this.state = state;
        this.childArray = new ArrayList<>();
        this.visitCount = 0;
        this.winScore = 0;
    }
    
    public Node(SaboteurBoardStateClone state, Node parent) {
        this.state = state;
        this.parent = parent;
        this.childArray = new ArrayList<>();
        this.visitCount = 0;
        this.winScore = 0;
    }
    
    public Node(SaboteurBoardStateClone state, Node parent, SaboteurMove move) {
        this.state = state;
        this.parent = parent;
        this.childArray = new ArrayList<>();
        this.visitCount = 0;
        this.winScore = 0;
        this.movePlayed = move;
    }
    
    public Node(SaboteurBoardStateClone state, Node parent, SaboteurMove move, double heuristic) {
        this.state = state;
        this.parent = parent;
        this.childArray = new ArrayList<>();
        this.visitCount = 0;
        this.winScore = 0;
        this.movePlayed = move;
        this.heuristic = heuristic;
    }
    
    public Node(SaboteurBoardStateClone state, Node parent, List<Node> childArray) {
        this.state = state;
        this.parent = parent;
        this.childArray = childArray;
        visitCount = 0;
        winScore = 0;
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
    

	public double getWinScore() {
		return winScore;
	}
	
	public SaboteurMove getMovePlayed() {
		return this.movePlayed;
	}
	public void setWinScore(double score) {
		this.winScore = score;
	}

	public int getVisitCount() {
		return visitCount;
	}
	
	public void setVisitCount(int count) {
		this.visitCount = count;
	}

	public SaboteurBoardStateClone getState() {
		return state;
	}
}