import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Node {
	
	public String statement;
	public List<Node> predecessors;
	public List<Node> successors;
	public int number;
	public Set<Integer> dom;
	public boolean visited_predecessors = false; // used to calculate dom
	public Node exit; // only used by entry node
	public Set<String> gen;
	public Set<String> kill;
	public Set<String> LiveOut;
	public int traversal_count = 0; // used to calculate LiveOut
	
	public Node() {
		this.predecessors = new ArrayList<Node>();
		this.successors = new ArrayList<Node>();
		number = -1;
		dom = new HashSet<Integer>();
		gen = new HashSet<String>();
		kill = new HashSet<String>();
		LiveOut = new HashSet<String>();
	}

}
