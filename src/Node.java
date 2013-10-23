import java.util.ArrayList;
import java.util.List;

public class Node {
	
	public String statement;
	public List<Node> predecessors;
	public List<Node> successors;
	public int number;
	
	public Node() {
		this.predecessors = new ArrayList<Node>();
		this.successors = new ArrayList<Node>();
		number = -1;
	}

}
