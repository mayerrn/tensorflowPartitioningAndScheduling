package graph;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Graph {
	protected Map<Integer,Node> nodeMap;
	
	public Graph(){
		nodeMap = new HashMap<Integer,Node>();
	}
	
	public Collection<Node> getNodes(){
		return nodeMap.values(); 
	}
	
	public void addNode(Node node){
		nodeMap.put(node.getId(),node);
	}
	
	public Node getNodeForId(int id){
		return nodeMap.get(id);
	}
	
	public int numberOfNodes(){
		return nodeMap.size();
	}
	
	public String nodesToString(){
		StringBuilder builder = new StringBuilder();
		for(Node node: this.getNodes()){
			builder.append(node.getId() + ";");
		}
		return builder.toString();
	}
	
	public String graphVisualisation(){
		//http://graphs.grevian.org/example think about ranking ->very useful
		StringBuilder builder = new StringBuilder();
		for(Node node: this.getNodes()){
			for(Node outgoing: node.getOutgoingNodes()){
				builder.append(node.getId());
				builder.append(" -> ");
				builder.append(outgoing.getId());
				builder.append(";\n");
			}
		}

		return builder.toString();
	}
}
