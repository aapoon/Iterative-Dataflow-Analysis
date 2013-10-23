import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class Digraph {
	private Map<String, Set<String>> map;
	
	public Digraph() {
		map = new HashMap<String, Set<String>>();
	}
	
	public void addEdge(String v, String w) {
		if(!map.containsKey(v)) {addVertex(v);}
		if(!map.containsKey(w)) {addVertex(w);}
		map.get(v).add(w);
	}
	
	public void addVertex(String v) {
		if(!map.containsKey(v)) {
			map.put(v, new HashSet<String>());
		}
	}
	
	public Iterable<String> getAdjacentVertices(String v) {
		if(!map.containsKey(v)) {return new HashSet<String>();}
		return map.get(v);
	}
	
	public Set<String> getVertices() {
		return map.keySet();
	}

}
