package optimalPartitioning;

import graph.CreationGraph;
import graph.Graph;
import graph.Node;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import Device.Device;

public class SearchSpaceState implements Comparable<SearchSpaceState> {
	
	public static long NEXT_ID = 0;
	public static Collection<Device> devices;
	public static Collection<Node> nodes;
	public static CreationGraph graph;

	private long id;
	private HashMap<Integer,Integer> usedMemory; // device id --> memory
//	private HashMap<Integer, List<Node>> assignments; // device id --> list of vertex ids
	private HashMap<Integer, Device> assignments; // node id --> device
	private double costsCache = -1; // if the costs are calculated, we do not have to recalculate!
	
	public SearchSpaceState() {
//		this.assignments = new HashMap<Integer, List<Node>>();
		this.assignments = new HashMap<Integer, Device>();
		this.usedMemory = new HashMap<Integer, Integer>();
		this.id = SearchSpaceState.NEXT_ID;
		SearchSpaceState.NEXT_ID++;
		costsCache = -1;
	}
	
	/**
	 * Returns the average execution time of the graph
	 * @return
	 */
	public double getCosts() {
		if (costsCache>-1) {
			return costsCache;
		}
		int maxIterations = 1;
		double sum = 0;
		for (int i=0; i<maxIterations; i++) {
			List<Node> l = getTopologicalOrder(shuffle(nodes));
			double length = getScheduleLength(l);
//			System.out.println("Length: " + length);
			sum += length;
		}
		double avg = sum / (double) maxIterations;
//		System.out.println("Average: " + avg);
//		System.out.println();
		costsCache = avg;
		return avg;
	}
	
	/**
	 * Returns the length of the scheduling given by the current partitioning
	 * and the topological order.
	 * @param l
	 */
	private double getScheduleLength(List<Node> topologicalOrder) {
		
		// initialize
		HashMap<Integer, Double> startTime = new HashMap<Integer, Double>();
		for (Node node : topologicalOrder) {
			startTime.put(node.getId(), 0.0);
		}
		HashMap<Integer, Double> deviceTime = new HashMap<Integer, Double>();
		for (Device device : devices) {
			deviceTime.put(device.getId(), 0.0);
		}
		
		// determine makespan
		double maxTime = 0;
		for (Node node : topologicalOrder) {
			if (assignments.containsKey(node.getId())) {
				Device device = assignments.get(node.getId());
				
				// determine start time of node and execution time of node
				double t = startTime.get(node.getId());
				double execTime = (double)node.getNumberOfOperations()
						/ (double)device.getNumberOfOperations();
				
				// if device time is already larger -> take device time as starting slot
				double t_dev = deviceTime.get(device.getId());
				if (t_dev>t) {
					startTime.put(node.getId(), t_dev);
					t = t_dev;
				}
				
				// device time always ticks
				deviceTime.put(device.getId(), t_dev + execTime);
				
				// track maximal seen time slot
				if (t+execTime>maxTime) {
					maxTime = t+execTime;
				}
				
				// update all out neighbors
				for (Node outNode : node.getOutgoingNodes()) {
					if (assignments.containsKey(outNode.getId())) {
						Device dev2 = assignments.get(outNode.getId());
						double tmpTime;
						if (device.getId()==dev2.getId()) {
							tmpTime = t + execTime;
						} else {
							double commLat = (double) outNode.getOutgoingTensorSize() 
									/ (double) dev2.getCommunicationCost()[device.getId()];
							tmpTime = t + execTime + commLat;
						}
						double oldStartTime = startTime.get(outNode.getId());
						if (tmpTime>oldStartTime) {
							startTime.put(outNode.getId(), tmpTime);
						}
					}
				}
			}
//			System.out.println("Start times: " + startTime);
//			System.out.println();
		}
		return maxTime;		
	}
	
	/** :)
	 * We use the Depth-firts search algorithm:
	 * https://en.wikipedia.org/wiki/Topological_sorting
	 * @return
	 */
	private List<Node> getTopologicalOrder(Collection<Node> nodes) {
		List<Node> l = new ArrayList<Node>();
		Set<Integer> marked = new HashSet<Integer>(); // vertex ids of already visited vertices
		Set<Integer> tmp_marked = new HashSet<Integer>(); // vertex ids of already visited vertices (is DAG?)
		
		while (marked.size()<nodes.size()) {
			// select unvisited node
			Node n = null;
			for (Node node : nodes) {
				if (!marked.contains(node.getId())) {
					n = node;
					break;
				}
			}
			visit(n, marked, tmp_marked, l);
		}
		return l;
	}
	
	/** 
	 * :)
	 * Randomly shuffles the list of nodes.
	 * @param nodes
	 * @return
	 */
	private List<Node> shuffle(Collection<Node> nodes) {
		List<Node> l = new LinkedList<Node>();
		Random random = new Random();
		for (Node node : nodes) {
			int i;
			if (l.size()>0) {
				i = random.nextInt(l.size());
			} else {
				i = 0;
			}
			l.add(i,node);
		}
		return l;
	}

	/**
	 * Helper function for getTopologicalOrder()
	 * @param n
	 */
	private void visit(Node n, Set<Integer> marked, Set<Integer> tmp_marked, List<Node> l) {
		if (tmp_marked.contains(n.getId())) {
			System.err.println("Error in topological order: Graph is not a DAG!");
			return;
		}
		if (!marked.contains(n.getId())) {
			tmp_marked.add(n.getId());
			for (Node m : n.getOutgoingNodes()) {
				visit(m, marked, tmp_marked, l);
			}
			marked.add(n.getId());
			tmp_marked.remove(n.getId());
			l.add(0,n);
		}
	}

	/**
	 * Adds the node to the device
	 * @param node
	 * @param device
	 */
	public void addNode2Device(Node node, Device device) {
//		assignments.putIfAbsent(device.getId(), new ArrayList<Node>());
//		assignments.get(device.getId()).add(node);
		assignments.put(node.getId(), device);
		
		int usedMem = usedMemory.getOrDefault(device.getId(), 0)
				+ node.getTotalRamDemand();
		usedMemory.put(device.getId(), usedMem);
	}

	/**
	 * Returns all states that arise if the node is added to any of the devices.
	 * (Pruning is performed separately).
	 * If there are no possible successor states (e.g. collocation/device constraints)
	 * return empty list.
	 * @param node
	 * @return
	 */
	public List<SearchSpaceState> getSuccessorStates(Node node) {
		List<SearchSpaceState> successors = new ArrayList<SearchSpaceState>();
		for (Device device : devices) {
			if (device.canAddComponent(node) // Device constraint
					&& collocationConstraint(node, device) // Collocation constraints
					&& memoryConstraint(node, device) // memory constraints
					) {
				SearchSpaceState sss = this.copy();
				sss.addNode2Device(node, device);
				successors.add(sss);
			}
		}
//		System.out.println("Node: " + node);
//		System.out.println("Successors: " + successors);
		return successors;
	}

	private boolean memoryConstraint(Node node, Device device) {
		int devMem = device.getMemory();
		int totalUsedMem = usedMemory.getOrDefault(device.getId(),0);
		if (totalUsedMem + node.getTotalRamDemand() < devMem) {
			return true;
		}
		return false;
	}

	private boolean collocationConstraint(Node node, Device device) {
		if (node.getColocationId()==-1) {
			return true;
		}
		Collection<Node> colocatedNodes = graph.getColocationMap()
				.get(node.getColocationId()).getNodes();
		for (Node n : colocatedNodes) {
			if (assignments.containsKey(n.getId())
					&& assignments.get(n.getId()).getId()!=device.getId()) {
				// colocated node is placed on other device
				return false;
			}
		}
		return true;
	}

	private SearchSpaceState copy() {
		SearchSpaceState sss = new SearchSpaceState();
		
		// copy assignments
		sss.assignments = new HashMap<Integer, Device>();
		for (Integer key : this.assignments.keySet()) {
			sss.assignments.put(key, this.assignments.get(key));
		}
		
		// copy usedMemory
		sss.usedMemory = new HashMap<Integer, Integer>();
		for (Integer key : this.usedMemory.keySet()) {
			sss.usedMemory.put(key, this.usedMemory.get(key));
		}
		return sss;
	}

	public int getDevice(Node node) {
		return assignments.get(node.getId()).getId();
	}
	
	public int numberOfAssignedNodes() {
		return assignments.size();
	}

	/**
	 * Returns true if all nodes are assigned
	 * @return
	 */
	public boolean isEndState() {
		return assignments.size() == nodes.size();
	}
	
	@ Override
	public String toString() {
		String s = "";
		for (Integer key : assignments.keySet()) {
			s += key + "->" + assignments.get(key).getId() + "\t";
		}
		return s;
	}
	
	@Override
	public boolean equals(Object o) {
		SearchSpaceState sss = (SearchSpaceState) o;
		return this.id==sss.id;
	}
	
	@Override
	public int compareTo(SearchSpaceState sss) {
//		SearchSpaceState sss = (SearchSpaceState) o;
		Long i1 = sss.id;
		Long i2 = this.id;
		return i1.compareTo(i2);
	}

//	/**
//	 * Returns the longest path of this state.
//	 * We assume that all vertices are already assigned to devices.
//	 * @return
//	 */
//	public double getCosts_lp() {
//		HashMap<Integer, Double> pathLengths = new HashMap<Integer, Double>(); // node id --> path length
//		HashMap<Integer, Double> weights = new HashMap<Integer, Double>(); // node id --> computation time on device
//		
//		// initialization
//		for (Node node : nodes) {
//			Device device = assignments.get(node.getId());
//			double duration = (double)node.getNumberOfOperations() / (double)device.getNumberOfOperations();
//			pathLengths.put(node.getId(), duration);
//			weights.put(node.getId(), duration);
//		}
//		
//		// calculate topological ordering
//		List<Node> topologicalOrder = getTopologicalOrder(nodes);
//		
//		// calculate longest paths
//		double max = 0;
//		for (Node node : topologicalOrder) {
//			double length = pathLengths.get(node.getId());
//			Device dev1 = assignments.get(node.getId());
//			for (Node neighbor : node.getIncomingNodes()) {
//				Device dev2 = assignments.get(neighbor.getId());
//				double newLength;
//				if (dev1.getId()==dev2.getId()) {
//					// node & neighbor on same device --> no communication latency
//					newLength = pathLengths.get(neighbor.getId()) + weights.get(node.getId());
//				} else {
//					// node & neighbor on different devices --> communication latency
//					double commLat = (double) neighbor.getOutgoingTensorSize() 
//							/ (double) dev2.getCommunicationCost()[dev1.getId()];
//					newLength = pathLengths.get(neighbor.getId()) + weights.get(node.getId())
//							+ commLat;
//				}
//				if (newLength>length) {
//					pathLengths.put(node.getId(), newLength);
//				}
//				if (newLength>max) {
//					max = newLength;
//				}
//			}
//		}
//		
//		// costs = longest path (TODO: we could also differentiate other metrics such as the robustness, etc.)
//		return max;
//	}
}
