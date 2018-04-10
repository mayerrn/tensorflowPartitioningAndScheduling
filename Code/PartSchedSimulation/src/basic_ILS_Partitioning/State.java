package basic_ILS_Partitioning;

import graph.ColocationComponent;
import graph.ConstraintInterface;
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

import optimalPartitioning.SearchSpaceState;
import Device.Device;

public class State {
	
	private HashMap<Integer,Integer> usedMemory; // device id --> memory
	private HashMap<Integer, Device> assignments; // node id --> device
	private HashMap<Integer, Double> rankEFT; // node id --> rank
	
	
	public State() {
		this.assignments = new HashMap<Integer, Device>();
		this.usedMemory = new HashMap<Integer, Integer>();
		this.rankEFT = new HashMap<Integer, Double>();
	}

	public static State getRandomState() {
		State state = new State();
		Random random = new Random();
		for (Node node : Basic_ILS.graph.getNodes()) {
			List<Device> devices = new ArrayList<Device>();
			for (Device device : Basic_ILS.devices) {
				if (state.deviceConstraint(node, device)
						&& state.colocationConstraint(node, device)
						&& state.memoryConstraint(node, device)) {
					devices.add(device);
				}
			}
			if (devices.size()>0) {
				Device device = devices.get(random.nextInt(devices.size()));
				state.add(node, device);
			} else {
				System.err.println("Can not find an initial assignment of nodes to devices!");
				return getRandomState();
			}
		}
		
		// update the earliest finishing times
		state.calculateRanksEFT();
		return state;
	}
	
	/**
	 * Add node to device. The additional costs
	 * are automatically added to the total costs of this state.
	 * @param node
	 * @param device
	 */
	private void add(Node node, Device device) {
		assignments.put(node.getId(), device);
		int usedMem = usedMemory.getOrDefault(device.getId(), 0)
				+ node.getTotalRamDemand();
		usedMemory.put(device.getId(), usedMem);
	}
	
	/**
	 * Remove node from the device it was assigned to.
	 * @param node
	 */
	private void remove(Node node) {
		Device device = assignments.remove(node.getId());
		int usedMem = usedMemory.get(device.getId()) - node.getTotalRamDemand();
		usedMemory.put(device.getId(), usedMem);
	}
	
	/**
	 * Returns the maximal earliest finishing time of a sink node.
	 * @return
	 */
	public double getCosts() {
		double maxFinishingTime = -1;
		for (Node node : Basic_ILS.graph.getSinkNodes()) {
			double rank = rankEFT.get(node.getId());
			if (rank>maxFinishingTime) {
				maxFinishingTime = rank;
			}
		}
		return maxFinishingTime;
	}

	/**
	 * Calculates the ranks for all vertices, i.e., the
	 * earliest finishing times (EFT).
	 */
	private void calculateRanksEFT() {
		
		// calculate EFT
		HashMap<Integer,Double> deviceTime = new HashMap<Integer,Double>(); // device id -> latest busy time
		HashMap<Integer,Integer> inCount = new HashMap<Integer,Integer>(); // node id -> available in-tensors
		rankEFT = new HashMap<Integer,Double>(); // node id -> EFT rank
		List<Node> schedulableNodes = new ArrayList<Node>(Basic_ILS.graph.getSourceNodes());
		while (!schedulableNodes.isEmpty()) {
			Node node = schedulableNodes.remove(0);
			Device device = assignments.get(node.getId());
			
			// calculate eft for node on device
			double deviceFree = deviceTime.getOrDefault(device.getId(),0.0);
			double compCosts = (double)node.getNumberOfOperations()
					/ (double)device.getNumberOfOperations();
			double tensorsAvailableTime = 0;
			for (Node in : node.getIncomingNodes()) {
				Device dev_in = assignments.get(in.getId());
				double commCosts = 0;
				if (device.getId()!=dev_in.getId()) {
					// assigned to different devices
					commCosts = (double) in.getOutgoingTensorSize() 
							/ (double) dev_in.getCommunicationCost()[device.getId()];
				}
				double tensorAvailable = rankEFT.get(in.getId()) + commCosts;
				if (tensorAvailable>tensorsAvailableTime) {
					tensorsAvailableTime = tensorAvailable;
				}
			}
			double eft = Math.max(deviceFree, tensorsAvailableTime) + compCosts;
			rankEFT.put(node.getId(), eft);
			if (eft>deviceFree) {
				deviceTime.put(device.getId(), eft);
			}
			
			// update all outgoing nodes -> maybe they become schedulable?
			for (Node out : node.getOutgoingNodes()) {
				int count = inCount.getOrDefault(out.getId(),0) + 1;
				inCount.put(out.getId(), count);
				if (count == out.getIncomingNodes().size()) {
					schedulableNodes.add(out);
				}
			}
		}
//		System.out.println();
//		System.out.println(rankEFT);
//		System.out.println(toString());
//		System.out.println();
//		
//		
//		for (Node sink : Basic_ILS.graph.getSinkNodes()) {
//			EFT(sink);
//		}
	}
	
//	private void EFT(Node node) {
//		double max = 0;
//		Device device = assignments.get(node.getId());
//		for (Node in : node.getIncomingNodes()) {
//			
//			// update rank of predecessor node
//			EFT(in);
//			
//			// calculate the costs from this predecessor to node
//			double predEFT = rank_EFT.get(in.getId());
//			Device device_in = assignments.get(in.getId());
//			double commCosts = 0;
//			if (device.getId()!=device_in.getId()) {
//				// assigned to different devices
//				commCosts = (double) in.getOutgoingTensorSize() 
//						/ (double) device_in.getCommunicationCost()[device.getId()];
//			}
//			if (predEFT+commCosts>max) {
//				max = predEFT+commCosts;
//			}
//		}
//		double compCosts = (double)node.getNumberOfOperations()
//				/ (double)device.getNumberOfOperations();
//		rank_EFT.put(node.getId(),max + compCosts);
//	}

	/**
	 * The additional costs when node is assigned to device.
	 * Can be negative if there is a cost improvement.
	 * @param node
	 * @param device
	 * @return
	 */
	public double getCostDelta(Node node, Device device) {
		double oldCosts = getCosts();
		Device oldDevice = assignments.get(node.getId());
		double delta;
		if (oldDevice.getId()!=device.getId()) {
			this.remove(node);
			this.add(node, device);
			calculateRanksEFT();
			double newCosts = getCosts();
			delta = newCosts - oldCosts; // if newCosts <oldCosts we have negative difference (improvement)
			this.remove(node);
			this.add(node, oldDevice);
			calculateRanksEFT();
		} else {
			delta = 0;
		}
		return delta;
	}

	public void move(Node bestNode, Device bestDevice) {
		if (assignments.get(bestNode.getId()).getId()!=bestDevice.getId()) {
			remove(bestNode);
			add(bestNode, bestDevice);
			// the earliest finishing times may have changed!
			calculateRanksEFT();
		}
	}
	
	/**
	 * Swaps the assignments between both devices for all
	 * nodes such that colocation and device constraints are still valid.
	 * @param dev1
	 * @param dev2
	 */
	public void swapAssignments(Device dev1, Device dev2) {
		List<Node> dev1_nodes = new ArrayList<Node>();
		List<Node> dev2_nodes = new ArrayList<Node>();
		for (Integer node_id : assignments.keySet()) {
			Device assignedDevice = assignments.get(node_id);
			if (assignedDevice.getId()==dev1.getId()) {
				dev1_nodes.add(Basic_ILS.graph.getNodeForId(node_id));
			}
			if (assignedDevice.getId()==dev2.getId()) {
				dev2_nodes.add(Basic_ILS.graph.getNodeForId(node_id));
			}
		}
		
		// assign nodes from dev1 to dev2
		for (Node node : dev1_nodes) {
			if (deviceConstraint(node,dev2)) {
				remove(node);
				add(node,dev2);
			}
		}
		
		// assign nodes from dev2 to dev1
		for (Node node : dev2_nodes) {
			if (deviceConstraint(node,dev1)) {
				remove(node);
				add(node,dev1);
			}
		}
		
		// TODO: debug only - can be removed in production
		for (Node node : dev1_nodes) {
			if (!colocationConstraint(node, dev2)) {
				System.err.println("Error: swapping leads to inconsistent state!");
				System.exit(-1);
			}
		}
		for (Node node : dev2_nodes) {
			if (!colocationConstraint(node, dev1)) {
				System.err.println("Error: swapping leads to inconsistent state!");
				System.exit(-1);
			}
		}
		
		// if memory constraint is violated, we have to bring back some nodes
		
	}

	public State copy() {
		State state = new State();
		
		// copy assignments
		state.assignments = new HashMap<Integer, Device>();
		for (Integer key : this.assignments.keySet()) {
			state.assignments.put(key, this.assignments.get(key));
		}
		
		// copy usedMemory
		state.usedMemory = new HashMap<Integer, Integer>();
		for (Integer key : this.usedMemory.keySet()) {
			state.usedMemory.put(key, this.usedMemory.get(key));
		}
		
		// copy rankEFT
		state.rankEFT = new HashMap<Integer, Double>();
		for (Integer key : this.rankEFT.keySet()) {
			state.rankEFT.put(key, this.rankEFT.get(key));
		}
		return state;
	}

	public boolean deviceConstraint(Node node, Device device) {
		return device.canAddComponent(node);
	}

	public boolean colocationConstraint(Node node, Device device) {
		if (node.getColocationId()==-1) {
			return true;
		}
		Collection<Node> colocatedNodes = Basic_ILS.graph.getColocationMap()
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

	public boolean memoryConstraint(Node node, Device device) {
		int devMem = device.getMemory();
		int totalUsedMem = usedMemory.getOrDefault(device.getId(),0);
		return totalUsedMem + node.getTotalRamDemand() < devMem;
	}

	public Device getDevice(Node node) {
		return assignments.get(node.getId());
	}
	
	@ Override
	public String toString() {
		String s = "";
		for (Integer key : assignments.keySet()) {
			s += key + "->" + assignments.get(key).getId() + "\t";
		}
		return s;
	}

}
