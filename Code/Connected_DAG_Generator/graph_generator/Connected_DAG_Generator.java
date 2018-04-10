package graph_generator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

//http://stackoverflow.com/questions/12790337/generating-a-random-dag
//http://jgrapht.org/javadoc/
//isGraphConnected()
//Problem: Knoten geringeren Levels haben zu großer Wahrscheinlichkeit mehr Kanten
//Currently, the inspector supports connected components for an undirected graph and weakly connected components for a directed graph.

public class Connected_DAG_Generator {

	static boolean shallCreateGraphVisualization = false;
	static int m = 0; // the number of edges
	
	public static void main(String[] args) throws IOException {

		ArrayList<Node> nodes = new ArrayList<Node>();

		StringBuilder graphVis = new StringBuilder();
		
		float edgeProb = 0.001f;
		generateNodesAndEdges(nodes, graphVis, edgeProb, // GraphConfig.EDGE_PROHABILITY
				GraphConfig.EDGE_LEVEL_LIMIT, GraphConfig.EDGE_LEVEL_FUNCTION);
		//generateNodesAndEdgesRecursiv(nodes, graphVis);
		addColocations(nodes);

		// do the visualization string creation after setting colocation
		// constraints, if not colocation constraints are not set correctly
		String graphPath = "../../Sample_Data/Graph/Evaluation/" + nodes.size() + "n_"
				+ m + "_" + Util.currentTime();
		/*
		 * StringBuilder content = new StringBuilder();
		 * 
		 * for (Node node : nodes) { content.append(node.toString()); }
		 * 
		 * GraphWriter writer = new GraphWriter();
		 * writer.write(content.toString(), graphPath);
		 * if(shallCreateGraphVisualization){
		 * writer.writeVisualisation(graphVis.toString(), graphPath); }
		 */
		File file = new File(graphPath + ".csv");

		// if file doesnt exists, then create it
		if (!file.exists()) {
			file.createNewFile();
		}

		FileWriter fw = new FileWriter(file.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write(GraphConfig.configToString() + "\n");
		bw.write(
				"Id,(Outgoing) node,(Incoming) node,Colocation nodes,#tensorSize, #operations, RAM storage,Device constraint ,name\n");
		for (Node node : nodes) {
			bw.write(node.toString());
		}
		bw.close();
		fw.close();
		// now change properties of the graph

		// String[] edge_probability = {"0.025","0.05","0.","0.1", "0.2","0.4",
		// "0.6", "0.8","1"};
		/*
		 * String[] edge_probability = { "0.05", "0.1", "0.2", "0.3", "0.4" };
		 * int nodeLevelDistance = -1; // watch out that all nodes are reachable
		 * -> need sink node // ode sink = new Node(0, "SINK"); // not problem
		 * anymore for (String factor : edge_probability) { content = new
		 * StringBuilder(); // remove all edges for (Node nodeToRemoveEdges :
		 * nodes) { nodeToRemoveEdges.removeAllEdges(); } for (Node node :
		 * nodes) { // könnte man beschleunigen da es sich um eine Arraylist
		 * handelt for (Node possibleEdge : nodes) { // To avoid cycles, would
		 * be faster to exploid properties of // the arraylist if (node.level <
		 * possibleEdge.level) { if (Math.random() < Float.parseFloat(factor)) {
		 * if (nodeLevelDistance < 0 || (possibleEdge.level - node.level <
		 * nodeLevelDistance)) { node.getOutgoingNodes().add(possibleEdge);
		 * possibleEdge.getIncomingNodes().add(node);
		 * 
		 * } } } } } // koennte man auch schon vorher hinzufuegen, aber um
		 * Fehler // auszuschließen for (Node node : nodes) {
		 * content.append(node.toString()); } writer.write(content.toString(),
		 * graphPath + "_" + factor); }
		 */
		// System.out.println("isReachable:" +
		// isEveryNodeReachable(sink,numberOfNodes));
		
		if(shallCreateGraphVisualization){
			System.out.println(graphVis.toString());
		}
	}
	
	// nodeLevelDistance if there should be no limitation
	static void generateNodesAndEdges(ArrayList<Node> nodes, StringBuilder graphVis,
			float edgeProbability,
			int nodeLevelDistance, float function) {
		int numberOfNodes = 1;
		graphVis.append("digraph {\n");
		int numberOfEdges = 0;
		for (int i = 0; i < GraphConfig.NUMBER_OF_LEVELS; i++) {
			int numberOfNewNodes = Util.intFromRange(GraphConfig.MIN_PER_LEVEL, GraphConfig.MAX_PER_LEVEL);
			Set<Node> newNodes = new HashSet<Node>();
			for (int j = 0; j < numberOfNewNodes; j++) {
				Node newNode = new Node(numberOfNodes, "");
				newNodes.add(newNode);
				newNode.level = i;
				numberOfNodes++;
				for (Node node : nodes) {
					// Knoten sind auf level beschränkt, nicht sehr dicht
					// compute prohability differently
					if (function > 0) {
						if (Math.random() < 1 / Math.pow(function, (i - node.level)) * edgeProbability) {
							// System.out.println(1/Math.pow(function,(i -
							// node.level)) * edgeProbability);
							node.getOutgoingNodes().add(newNode);
							newNode.getIncomingNodes().add(node);
							if (shallCreateGraphVisualization) {
								graphVis.append("  " + node.getId() + "->" + newNode.getId() + ";\n");
							}
							numberOfEdges ++;
						}
					} else {
						if (Math.random() < edgeProbability) {
							if (nodeLevelDistance < 0 || (i - node.level < nodeLevelDistance)) {
								node.getOutgoingNodes().add(newNode);
								newNode.getIncomingNodes().add(node);
								if (shallCreateGraphVisualization) {
									graphVis.append("  " + node.getId() + "->" + newNode.getId() + ";\n");
								}
								numberOfEdges ++;
							}
						}
					}
				}
				
				
				
				// only add nodes with input
				if (newNode.getIncomingNodes().size() == 0) {
					// sink.getOutgoingNodes().add(newNode);
					// newNode.getIncomingNodes().add(sink);
					// graphVis.append(" " + sink.getId() + "->"+
					// newNode.getId() +";\n");
				}
			}
			System.out.println(i + " " + nodes.size());
			nodes.addAll(newNodes);
		}
		
		//count how many edges are computed and add percentage of long range vertices
		//add long range edges
		System.out.println("NumberOfEdgesWithoutLongRange:" + numberOfEdges);
		int longRangeEdges = 0;
		for(int i = 0; i< GraphConfig.PERCENTAGE_OF_LONG_RANGE_EDGES * numberOfEdges; i++){
			int firstIndex = (int) (Math.random() * nodes.size());
			int secondIndex = (int) (Math.random() * nodes.size());
			if(firstIndex != secondIndex && nodes.get(firstIndex).level != nodes.get(secondIndex).level){
				Node firstNode = null;
				Node secondNode = null;
				if(firstIndex > secondIndex){
					int temp = firstIndex;
					firstIndex = secondIndex;
					secondIndex = temp;
				}
				firstNode = nodes.get(firstIndex);
				secondNode = nodes.get(secondIndex);
				//und nicht Kante schon existiert
				if(!firstNode.getOutgoingNodes().contains(secondNode)){
				firstNode.getOutgoingNodes().add(secondNode);
				secondNode.getIncomingNodes().add(firstNode);
				if (shallCreateGraphVisualization) {
					graphVis.append("  " + firstNode.getId() + "->" + secondNode.getId() + ";\n");
				}
				numberOfEdges ++;
				longRangeEdges++;
				}
			}

		}
		System.out.println("numberOfNodes:" + numberOfNodes);
		System.out.println("NumberOfEdgesLongRange:" + longRangeEdges);
		System.out.println("NumberOfEdges:" + numberOfEdges);
		m = numberOfEdges;

		
		// add sink node after, that no edge from sink node a generated
		// nodes.add(sink);
		graphVis.append("}\n");
	}
	
	
	static int numberOfRecursivelyGeneratedNodes = 1;
	static void generateNodesAndEdgesRecursiv(ArrayList<Node> nodes, StringBuilder graphVis) {
		graphVis.append("digraph {\n");
		Node newNode = new Node(1, "");
		newNode.level = 0;
		nodes.add(newNode);
		recursiveNode(nodes, graphVis, newNode, 6, 150);
		System.out.println("nodes" + numberOfRecursivelyGeneratedNodes);
		int numberOfEdges = numberOfRecursivelyGeneratedNodes;
		int numberOfEdgesRandomly = 0;
			//add random edges
			for(int i = 0; i< 0.1 * numberOfRecursivelyGeneratedNodes; i++){
				int firstIndex = (int) (Math.random() * nodes.size());
				int secondIndex = (int) (Math.random() * nodes.size());
				if(firstIndex != secondIndex && nodes.get(firstIndex).level != nodes.get(secondIndex).level){
					Node firstNode = null;
					Node secondNode = null;
					if(firstIndex > secondIndex){
						int temp = firstIndex;
						firstIndex = secondIndex;
						secondIndex = temp;
					}
					firstNode = nodes.get(firstIndex);
					secondNode = nodes.get(secondIndex);
					//und nicht Kante schon existiert
					if(!firstNode.getOutgoingNodes().contains(secondNode)){
					firstNode.getOutgoingNodes().add(secondNode);
					secondNode.getIncomingNodes().add(firstNode);
					numberOfEdgesRandomly ++;
					numberOfEdges ++ ;
					if (shallCreateGraphVisualization) {
						graphVis.append("  " + firstNode.getId() + "->" + secondNode.getId() + ";\n");
					}
					}
				}

			}
			graphVis.append("}\n");
			System.out.println("number of nodes" + nodes.size());
			System.out.println("edges"  + numberOfEdges);
			System.out.println("random" + numberOfEdgesRandomly);
			
	}
	
	static void recursiveNode(ArrayList<Node> nodes, StringBuilder graphVis, Node predecessorNode, int maxNumberOfSuccessors, int maxLevel){
		int numberOfNodes = 1;
		if(Math.random() < 0.05){
			numberOfNodes = Util.intFromRange(2, maxNumberOfSuccessors);
		}
		System.out.println(predecessorNode.level);
		for(int i = 0; i < numberOfNodes; i++){
		Node newNode = new Node(nodes.size() + 1, "");
		numberOfRecursivelyGeneratedNodes ++;
		newNode.level = predecessorNode.level + 1;
		predecessorNode.getOutgoingNodes().add(newNode);
		newNode.getIncomingNodes().add(predecessorNode);
		if (shallCreateGraphVisualization) {
			graphVis.append("  " + predecessorNode.getId() + "->" + newNode.getId() + ";\n");
		}
		nodes.add(newNode);
		System.out.println("bal" + Math.random());
			if(newNode.level < maxLevel* (Math.random()/3  + 0.33)){
				recursiveNode(nodes, graphVis, newNode, maxNumberOfSuccessors, maxLevel); 
			}
		}
	}

	static void addColocations(ArrayList<Node> nodes) {
		/*
		 * //we need a different strategy for colocations for(Node node: nodes){
		 * for(Node node1: nodes){ //do not colocate with itself && auch nur
		 * einmal testen (nicht zweimal für gleiche kombination) if(node.getId()
		 * < node1.getId()){ if(Math.random() <
		 * GraphConfig.COLOCATION_PROHABILITY){
		 * node.getColocationNodes().add(node1);
		 * node1.getColocationNodes().add(node); } } } }
		 */
		// Normalverteilung, dass integral so groß ist wie ein drittel der
		// knoten
		// Größe der größten Colocation group 0.05 * node size
		// TODO0.05
		int colocatedNodes = 0;
		for (int i = 2; i < nodes.size() * GraphConfig.SIZE_OF_BIGGEST_COLOCATION_GROUP; i++) {
			//int numberOfColocactionGroups = (int) (nodes.size() / 5 * Math.random() * Math.pow(2, -(i + 2)));
			int numberOfColocactionGroups = (int) (nodes.size() / 5 * Math.random() * Math.pow(2, -i));
			colocatedNodes += i* numberOfColocactionGroups;
			for (int j = 0; j < numberOfColocactionGroups; j++) {
				HashSet<Node> nodesColocated = new HashSet<Node>();
				DeviceConstraint colocatonConstraint = DeviceConstraint.NO;

				while (nodesColocated.size() < i) {
					Node node = nodes.get(Util.intFromRange(0, nodes.size() - 1));
					if (!node.alreadyColocated && (node.getDeviceConstraint() == DeviceConstraint.NO
							|| node.getDeviceConstraint() == colocatonConstraint
							|| colocatonConstraint == DeviceConstraint.NO)) {
						node.alreadyColocated = true;
						nodesColocated.add(node);
						if (node.getDeviceConstraint() != DeviceConstraint.NO) {
							colocatonConstraint = node.getDeviceConstraint();
						}
					}
				}
				for (Node node : nodesColocated) {
					for (Node node1 : nodesColocated) {
						if (node.getId() < node1.getId()) {
							node.getColocationNodes().add(node1);
							node1.getColocationNodes().add(node);
						}
					}
				}
			}
		}
		System.out.println("Colocated nodes" + colocatedNodes);
	}

	/*
	 * public static boolean isAcylic(Set<Node> firstLevelNodes){ return false;
	 * }
	 */
	// assuming that there is no cycle
	// like sink is created, is always true
	/*
	 * public static boolean isEveryNodeReachable(Node sink, int
	 * totalNumberOfNodes){ Stack<Node> stack = new Stack<Node>();
	 * stack.push(sink); while(!stack.isEmpty()){ Node node = stack.pop();
	 * totalNumberOfNodes--; for(Node outgoing: node.outgoingNodes){
	 * if(!outgoing.visited){ stack.push(outgoing); outgoing.visited = true; } }
	 * } System.out.println(totalNumberOfNodes); return totalNumberOfNodes == 0;
	 * 
	 * }
	 */

}
