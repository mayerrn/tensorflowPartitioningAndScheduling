package graph;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

//graph that is not partitioned yet and created during reading input data (has no send and receive nodes)
public class CreationGraph extends Graph {
	
	
	protected Map<Integer, ColocationComponent> colocationMap = new HashMap<Integer, ColocationComponent>();
	int colocationId = 0;
	int maxNodeId = 0;
	
	private HashSet<Node> sinkNodes = new HashSet<Node>();
	private HashSet<Node> sourceNodes = new HashSet<Node>();

	
	public void addEntry(CreationNodeEntry entry) {
		Node node = new Node(entry);
		
		HashSet<Integer> outgoingNodes = entry.getOutgoingNodes();
		for (Integer outgoingId : outgoingNodes) {
			Node outgoing = this.nodeMap.get(outgoingId);
			if (outgoing != null) {
				outgoing.getIncomingNodes().add(node);
				node.getOutgoingNodes().add(outgoing);
			}
		}
		//just necessary for mode: remainedOperationsTillSinkNode
		//for additional calculations like path length
		if(outgoingNodes.size() == 0){
			sinkNodes.add(node);
		}
		
		HashSet<Integer> incomingNodes = entry.getIncomingNodes();
		for (Integer incomingId : incomingNodes) {
			Node incoming = this.nodeMap.get(incomingId);
			if (incoming != null) {
				node.getIncomingNodes().add(incoming);
				incoming.getOutgoingNodes().add(node);
			}
		}
		
		//just necessary for mode: DividePathPartitioning
		//for additional calculations like path length
		if(incomingNodes.size() == 0){
			sourceNodes.add(node);
		}
		
		this.calculateColocation(node,entry.getColocationNodes());
		//export colocation groups
		
		if(node.getId() > maxNodeId){
			maxNodeId = node.getId();
		}
		this.addNode(node);
	}
	
	public Map<Integer, ColocationComponent> getColocationMap(){
		return this.colocationMap;
	}
	
	public void removeNode(Node node){
		this.nodeMap.remove(node.getId());
	}
	
	private void calculateColocation(Node currentNode, Set<Integer> colocationNodeIds){
		boolean foundExistingColocation = false;
		int colocationIdToMerge = -1;

		for (Integer colocationId : colocationNodeIds) {
			Node colocationNode = this.nodeMap.get(colocationId);
			//object for colocation node is already created
			if (colocationNode != null) {
				// first node to be colocated
				if (!foundExistingColocation) {
					ColocationComponent component;
					if (colocationNode.isAssignedOnColocationComponent()) {
						colocationIdToMerge = colocationNode.getColocationId();
						component = colocationMap.get(colocationIdToMerge);
					} else {
						component = new ColocationComponent(colocationId);
						component.addNode(colocationNode);
						colocationIdToMerge = colocationId;
						colocationMap.put(colocationId, component);
						colocationId++;
					}
					component.addNode(currentNode);
					foundExistingColocation = true;
					
				} else {
					ColocationComponent component = colocationMap.get(colocationIdToMerge);
					if (colocationNode.isAssignedOnColocationComponent()) {
						if (colocationNode.getColocationId() != currentNode.getColocationId()) {
							int componentToRemove = colocationNode.getColocationId();
							ColocationComponent componentToMerge = colocationMap.get(componentToRemove);
							component.merge(componentToMerge);
							colocationMap.remove(componentToRemove);
						}
					} else {
						component.addNode(colocationNode);
					}
				}
				
			}
		}
	}
	
	public int getMaxNodeId(){
		return this.maxNodeId;
	}
	
	public void exportColocationStatistics(String filePath , String graphName){
		
		File file = new File(filePath);
		// if file doesnt exists, then create it
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				System.err.println(e.toString());
			}
		}

		FileWriter fw;
		String csvSeperator = ",";
		try {
			fw = new FileWriter(file.getAbsoluteFile(),true);
			BufferedWriter bw = new BufferedWriter(fw);
			StringBuilder builder = new StringBuilder();
			int n = 0;
			for(Integer componentId: this.getColocationMap().keySet()){
				ColocationComponent component = this.getColocationMap().get(componentId);
				builder.append(component.getNodes().size());
				builder.append(csvSeperator);
				n += component.getNodes().size();
			}
			
			System.out.println("Nodes in colocation constraint: " + n);
			System.out.println("Total number of nodes: " + this.numberOfNodes());

			bw.write(graphName + "\n" + n + "\n" + this.numberOfNodes() +  "\n" + builder.toString());
			bw.close();
			fw.close();
		} catch (IOException e) {
			System.err.println(e.toString());
		}
		

	}
	
	//execute before partitioning
	public void printStatistics(){
		int n = 0;
		for(Integer componentId: this.getColocationMap().keySet()){
			ColocationComponent component = this.getColocationMap().get(componentId);
			n += component.getNodes().size();
			System.out.print("Colocation " + component.getId() + ": ");
			for(Node node: component.getNodes()){
				System.out.print(node.getId() + ", ");
				
			}
			System.out.println((float) component.getNodes().size()/ (float) this.numberOfNodes());
			System.out.println("------");
		}
		System.out.println("Nodes in colocation constraint: " + n);
		System.out.println("Total number of nodes: " + this.numberOfNodes());
	}
	
	public HashSet<Node> getSinkNodes(){
		return this.sinkNodes;
	}
	
	public HashSet<Node> getSourceNodes(){
		return this.sourceNodes;
	}

}
