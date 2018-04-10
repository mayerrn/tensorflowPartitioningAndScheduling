package Partitioning;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;

import Device.Device;
import Partitioning.DivideSuccessorNodesPartitioning.NodeBundle;
import graph.ColocationComponent;
import graph.ConstraintInterface;
import graph.CreationGraph;
import graph.Node;
import graph.Path;

//generates disjunkt pathes, only assigns longest path, interesting
public class DividePath2Partitioning extends PartitioningStrategy{
	
	@Override
	public void partition(CreationGraph graph, HashMap<Integer, Device> devices) {
		HashSet<Path> paths = new HashSet<Path>(); 
		Queue<Node> sourceNodes = new LinkedList<Node>();
		int id = 0;
		
		//überarbeiten
		for(Node node: graph.getSourceNodes()){
			sourceNodes.add(node);
		}
		
		while(!sourceNodes.isEmpty()){
			
			Node pathLeader = sourceNodes.poll();
			System.out.println("Start" + pathLeader.getId());

			Path path = new Path(id);

			boolean finishedPath = false;
			
			while(!finishedPath){
				System.out.println("PathLeader: " + pathLeader.getId());// + " SuccessorNode: " + pathLeader.successorNode.getId());
				//evtl noch überlegen was min eingehenden passiert, die auf diesen knoten zeigen
				if(pathLeader.getOutgoingNodes().size() ==  0){
					path.addNode(pathLeader, graph);
					finishedPath = true;
				}
				
				Node newPathLeader = null;		
				for(Node outgoing: pathLeader.getOutgoingNodes()){
			/*		if(outgoing.getId() == 2 || outgoing.getId() == 4){
						System.out.println("	lala" + outgoing.getId());
						System.out.println("	Out" + outgoing.predecessorNode.getId());
						System.out.println("	Out" + (outgoing.predecessorNode == pathLeader));
						System.out.println("	lalal" + pathLeader.getId());
					} */
					
					System.out.println("	Out" + outgoing.getId());
					//System.out.println("Outgoing.predecessor: " + outgoing.predecessorNode.getId());
					if(outgoing == pathLeader.getSuccessorNode()){
						if(outgoing.getPredecessorNode() == pathLeader){
							System.out.println("	Fall: Gegenseite\n");
							path.addNode(pathLeader, graph);
							//Aufpassen dass nicht zu früh überschrieben
							newPathLeader = outgoing;
						}else{
							//FALLS JA PRÜFEN OB SCHON HINZUGEFÜGT, KÖNNTE MIT MEHREREN NICHT WEITERGEHEN, nein weil pathldeder nur einen successornodes hat
							//path.addNode(pathLeader);
							//Pfadende
							path.addNode(pathLeader, graph);
						    finishedPath = true;
							System.out.println("	Fall: Pfadende\n");
						}
					}else if(outgoing.getPredecessorNode() == pathLeader){
						if(outgoing != pathLeader.getSuccessorNode()){
							//Pfadanfang
							System.out.println("	Fall: Pfadanfang\n");
							//keine Angst, dass ein Knoten mehrmals ausgeführt werden kann. Dieser Fall sollte nicht auftreten, da jeder outgoingNode nur einen predecessor hat
							sourceNodes.add(outgoing);
							
						}
						
					}else if(outgoing.getPredecessorNode() != pathLeader && (outgoing != pathLeader.getSuccessorNode())){
						//do nothing
						System.out.println("	Do nothing\n");
					}
					
				}
				
				pathLeader = newPathLeader;
							
			}	
			paths.add(path);
			id++;
		}
		
		
		//Arraylist of pathes
		int lala = 0;
		int numberOfNodesInPath = 0;
		Path maxPath = null;
		int maxPathLength = 0;
		//create stats
		for(Path path: paths){
			
			if(path.getNodes().size() > maxPathLength){
				maxPath = path;
				maxPathLength = path.getNodes().size();
			}
			if(path.getNodes().size() > 10){
				System.out.println("Pfadlänge: " + path.getNodes().size());
			}else{
				//System.out.println(path.getNodes().size());
			}
			numberOfNodesInPath += path.getNodes().size();
		}
		
		
		
		System.out.println("0Path" + lala);
		System.out.println(numberOfNodesInPath);
		System.out.println("Knotenanzahl"+ graph.getNodes().size());
		System.out.println("Pfadanzahl" + id);
	
		
		int deviceHash = 0;
		//TODO get fastest device!
		
		//only assigns longest path
		HashSet<Integer> removeColocation = new HashSet<Integer>();
		for(Node node: maxPath.getNodes()){
			if(node.getColocationId() != -1){
				ColocationComponent component = graph.getColocationMap().get(node.getColocationId());
				for (Node coloNode : component.getNodes()) {
					if(coloNode.getDeviceId() == -1){
					assignNode(coloNode, deviceHash, devices, graph);
					}
				}
				removeColocation.add(node.getColocationId());
			}else{
				assignNode(node, deviceHash, devices, graph);
			}
		}
		deviceHash = 1;
		for (Integer componentId : removeColocation) {
			graph.getColocationMap().remove(componentId);
		}
		
		// assign colocation components
		for (Integer componentId : graph.getColocationMap().keySet()) {
			ColocationComponent component = graph.getColocationMap().get(componentId);

			int deviceId = getDeviceIdToAssignComponentTo(component, deviceHash, devices);
			for (Node node : component.getNodes()) {
				assignNode(node, deviceId, devices, graph);
			}
			deviceHash = (deviceHash + 1) % devices.size();
		}
		// assign nodes (which are not a member of the colocation components)
		HashSet<Node> nodesNotColocated = new HashSet<Node>(graph.getNodes());
		for (Node node : nodesNotColocated) {
			int deviceId = getDeviceIdToAssignComponentTo(node, deviceHash, devices);
			assignNode(node, deviceId, devices, graph);
			deviceHash = (deviceHash + 1) % devices.size();
		}

	}

	public int getDeviceIdToAssignComponentTo(ConstraintInterface component, int startValue,
			HashMap<Integer, Device> devices) {
		int i = startValue;
		while (i < startValue + devices.size()) {
			int index = i % devices.size();
			if (devices.get(index).canAddComponent(component)) {
				return index;
			} else {
				i++;
			}
		}
		System.err.println("Component with constraint: " + component.getDeviceConstraint() + ", memory size: "
				+ component.getTotalRamDemand() + " could not be matched to any device");
		return -1;
	}
}
