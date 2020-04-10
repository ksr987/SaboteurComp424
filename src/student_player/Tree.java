package student_player;

public class Tree {
    Node root;

    public Tree(SaboteurBoardStateClone initialState) {
        root = new Node(initialState);
    }

    public Tree(Node root) {
        this.root = root;
    }

    public Node getRoot() {
        return root;
    }

    public void setRoot(Node root) {
        this.root = root;
    }

    public void addChild(Node parent, Node child) {
        parent.getChildArray().add(child);
    }

}