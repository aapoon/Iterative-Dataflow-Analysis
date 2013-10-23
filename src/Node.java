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
	public boolean visited_predecessors = false;
	public Node exit;
	public Set<String> gen;
	public Set<String> kill;
	public Set<String> LiveOut;
	public int traversal_count = 0;
	
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
