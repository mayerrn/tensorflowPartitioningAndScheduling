package Device;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;

import Partitioning.ScheduledTimeSlots;
import evaluation.Evaluation;
import graph.ConstraintInterface;
import graph.Graph;
import graph.Node;
import graph.ReceiveNode;
import graph.SendNode;
import misc.Config;
import scheduler.Scheduler;

public class Device {
	// unique id for a device
	private int id;
	
	private Graph graph = new Graph();
	// total RAM available
	private int memory;
	// free RAM left available when adding nodes, used in partitioning
	private int freeMemoryEstimaton;

	// number of Operations per execution unit
	private int numberOfOperations;
	//for Partitioning Strategy
	private int sumOfOperationsOfAlreadyAssignedNodes;
	// Device Type
	private DeviceType type;

	// important variables for scheduling
	// local time, which is important for scheduling simulation
	private double localTimestamp;
	// contains while scheduling how much memory is actually used
	private int currentlyUsedMemory = 0;

	// all nodes which can be scheduled right now
	private Queue<Node> readyToScheduleNodes = new LinkedList<Node>();

	// timestamp of receiving a message from a send node, receive node which
	// received the message
	private ArrayList<ReceiveNode> receiveNodes = new ArrayList<ReceiveNode>();

	// communication costs to all other devices
	private int[] transferRate;

	private HashMap<Integer, Device> devices;

	private Set<Integer> nodeIdWithCrossDeviceEdges = new HashSet<Integer>();

	private DeviceEvaluation deviceEvaluation = new DeviceEvaluation();
	private Evaluation evaluation;
	
	//for HeftPartitioning
	private TreeSet<ScheduledTimeSlots> scheduledNodes = new TreeSet<ScheduledTimeSlots>();
	
	//for evaluation
	private double lastNodeFinishedTimestamp = 0.0;

	public Device(String csvLine, HashMap<Integer, Device> devices, Evaluation evaluation) {
		String[] components = csvLine.split(Config.CATEGORY_SEPERATOR);
		this.id = Integer.parseInt(components[0]);
		this.memory = Integer.parseInt(components[1]);
		this.freeMemoryEstimaton = this.memory;
		this.numberOfOperations = Integer.parseInt(components[2]);
		processTransferRates(components[3].split(Config.ARRAY_SEPERATOR), devices);
		this.type = DeviceType.valueOf(components[4]);
		this.devices = devices;
		localTimestamp = 0.0;
		this.evaluation = evaluation;
	}

	public int getId() {
		return this.id;
	}

	public boolean canAddComponent(ConstraintInterface component) {
		if (component.getDeviceConstraint() != DeviceConstraint.NO
				&& !component.getDeviceConstraint().toString().equals(this.type.toString())) {
			return false;
		}
		if (this.freeMemoryEstimaton < component.getTotalRamDemand()) {
			return false;
		}
		return true;
	}

	public void addNode(Node node) {
		this.graph.addNode(node);
		this.currentlyUsedMemory += node.getRamToStore();
		this.sumOfOperationsOfAlreadyAssignedNodes += node.getNumberOfOperations();
		
		if (node.getIncomingNodes().size() == 0) {
			readyToScheduleNodes.add(node);
		}

		this.freeMemoryEstimaton = this.freeMemoryEstimaton - node.getTotalRamDemand();

		if (this.freeMemoryEstimaton < 0) {
			System.err.println("Error, memory is used but is not available: " + this.toString() + node.toString());
		}

		if (node.getClass().equals(ReceiveNode.class)) {
			deviceEvaluation.addedReceiveNode();
		} else if(node.getClass().equals(SendNode.class)){
			deviceEvaluation.addedSendNode();
		} else {
			deviceEvaluation.addedNode();
		}
	}

	public double getLocalTimestamp() {
		return this.localTimestamp;
	}

	public void setLocalTimestamp(double localTimestamp) {
		if (localTimestamp < this.localTimestamp) {
			System.err.println("can't set time to later point in time");
			System.err.println(localTimestamp);
			System.err.println("Device " + this.getId() + ": " + this.localTimestamp);
		}

		this.localTimestamp = localTimestamp;
	}

	public void addOutgoingNodeIdOfCrossDeviceEdge(int nodeId) {
		this.nodeIdWithCrossDeviceEdges.add(nodeId);
	}

	// minId is the id of next Node which can be created
	public int addSendReceiveNodes(int minId) {
		int id = minId;
		for (Integer nodeId : this.nodeIdWithCrossDeviceEdges) {

			Node node = this.graph.getNodeForId(nodeId);

			// device and nodes (from this specific node)
			HashMap<Integer, Set<Node>> deviceMap = new HashMap<Integer, Set<Node>>();
			// find the outgoing edges with go to the same device -> only have
			// to add one send/receive node pair for that
			for (Node outgoing : node.getOutgoingNodes()) {
				if (outgoing.getDeviceId() != node.getDeviceId()) {
					Set<Node> deviceNodeSet = deviceMap.get(outgoing.getDeviceId());
					if (deviceNodeSet == null) {
						deviceNodeSet = new HashSet<Node>();
						deviceMap.put(outgoing.getDeviceId(), deviceNodeSet);
					}
					deviceNodeSet.add(outgoing);
				}
			}

			for (Integer deviceId : deviceMap.keySet()) {
				Set<Node> nodes = deviceMap.get(deviceId);
				Node send = new SendNode(id, node.getOutgoingTensorSize());
				Node receive = new ReceiveNode(id + 1, node.getOutgoingTensorSize());
				id = id + 2;
				send.getOutgoingNodes().add(receive);
				receive.getIncomingNodes().add(send);
				send.setDeviceId(node.getDeviceId());
				receive.setDeviceId(deviceId);
				this.addSendNode(send, nodes, node);
				Device deviceReceiveNode = devices.get(deviceId);
				if (deviceReceiveNode != null) {
					deviceReceiveNode.addReceiveNode(receive, nodes, node);
				} else {
					System.err.println("Receive device not found: " + deviceId + " " + node.getId());
				}
			}
		}
		// returns the next id, where ids can be chosen from
		return id;
	}

	public void addSendNode(Node sendNode, Set<Node> receiveOutgoing, Node nodeBeforeSendNode) {
		nodeBeforeSendNode.getOutgoingNodes().add(sendNode);
		nodeBeforeSendNode.getOutgoingNodes().removeAll(receiveOutgoing);
		sendNode.getIncomingNodes().add(nodeBeforeSendNode);
		this.addNode(sendNode);
	}

	public void addReceiveNode(Node receiveNode, Set<Node> receiveOutgoing, Node nodeBeforeSendNode) {
		receiveNode.getOutgoingNodes().addAll(receiveOutgoing);
		for (Node node : receiveOutgoing) {
			node.getIncomingNodes().remove(nodeBeforeSendNode);
			node.getIncomingNodes().add(receiveNode);
		}
		this.addNode(receiveNode);
	}

	private void processTransferRates(String[] costs, HashMap<Integer, Device> devices) {
		// array length = total number of devices
		this.transferRate = new int[this.id + costs.length + 1];
		for (int i = 0; i < this.getId(); i++) {
			Device device = devices.get(i);
			this.transferRate[i] = device.transferRate[this.getId()];
		}
		this.transferRate[this.id] = 0;
		if (!costs[0].equals("")) {
			for (int i = 0; i < costs.length; i++) {
				int communicationCost = Integer.parseInt(costs[i]);
				this.transferRate[i + this.getId() + 1] = communicationCost;
			}
		}
	}

	public double getNextReceiveMessageTimestamp() {
		double min = Double.MAX_VALUE;
		for (ReceiveNode node: receiveNodes) {
			// System.out.println("Device " + this.getId() + ", receive node: "
			// + number);
			if (node.getReceiveTime() < min) {
				min = node.getReceiveTime();
			}
		}
		return min;
	}

	public boolean isScheduable() {
		this.receivePastData();
		return this.readyToScheduleNodes.size() != 0;
	}

	public void receivePastData() {
		// check nodes in the receive queue //where input has arrived in the
		// past
		Iterator<ReceiveNode> it = receiveNodes.iterator();
		while (it.hasNext()) {
			ReceiveNode receiveNode = it.next();
			// arrival time of the data
			if (receiveNode.getReceiveTime() <= this.localTimestamp) {
				evaluation.increaseReceiveSchedules();
				this.currentlyUsedMemory += receiveNode.getOutgoingTensorSize();
				for (Node outgoingNode : receiveNode.getOutgoingNodes()) {
					outgoingNode.inputAvailable();
					if (outgoingNode.isScheduable()) {
						readyToScheduleNodes.add(outgoingNode);
					}
				}
				it.remove(); // avoids a ConcurrentModificationException
			}
		}
	}

	private void printScheduableNodes() {
		StringBuilder builder = new StringBuilder();
		builder.append("Device: " + this.getId() + ", scheduable nodes: ");
		for (Node node : readyToScheduleNodes) {
			builder.append(node.getId());
			builder.append(", ");
		}
		builder.append("\n");
		System.out.println(builder.toString());
	}

	private void printReceiveNodes() {
		StringBuilder builder = new StringBuilder();
		builder.append("Device: " + this.getId() + ", nodes in receive queue: ");
		for (ReceiveNode node : receiveNodes) {
			builder.append(node.getReceiveTime() + " : " + node.getId());
			builder.append(", ");
		}
		builder.append("\n");
		System.out.println(builder.toString());
	}

	public void schedule(Scheduler scheduler) {
		this.receivePastData();
		// System.out.println("used memory: " + currentlyUsedMemory);
		// muss am Ende bei ausfuehrung des letzten Knoten einmal erhoben
		// werden, weil es immer vor dem schedulen gemessen wird
		deviceEvaluation.addRamUsage(currentlyUsedMemory);
		deviceEvaluation.addSchedulingPossibility(readyToScheduleNodes.size());
		// this.printReceiveNodes();
		// this.printScheduableNodes();

		// only difference in scheduling strategies
		Node node = scheduler.getNextNodeToSchedule(readyToScheduleNodes);

		// System.out.println("Selected node to schedule: " + node.getId());

		readyToScheduleNodes.remove(node);

		double computationTime = (double) node.getNumberOfOperations() / (double) this.numberOfOperations;

		deviceEvaluation.addCalculationTime(this.localTimestamp,computationTime);
		this.localTimestamp += computationTime;
		this.lastNodeFinishedTimestamp = this.localTimestamp;
		// send Knoten, a receive not should not be in here however (is
		// scheduled asynchronously as soon as receive timestamp is bigger than
		// local timestamp)
		if (node.getClass().equals(SendNode.class)) {
				evaluation.increaseSendSchedules();
				scheduledSendNode(node);
		} else {
			
			if(node.getClass().equals(ReceiveNode.class)){
				System.err.println("Should not be a ReceiveNode");
			}
			// just count non receive/send nodes
			evaluation.increaseSchedulingCounter();
			this.currentlyUsedMemory += node.getOutgoingTensorSize();
			for (Node incomingNode : node.getIncomingNodes()) {
				incomingNode.consumedInput();
				if (incomingNode.canDeleteOutput()) {
					this.currentlyUsedMemory = this.currentlyUsedMemory - incomingNode.getOutgoingTensorSize();
				}
			}
			// notify output
			// is not a send or receive node for sure -> outgoingNodes are on
			// the same device for sure
			for (Node outgoingNode : node.getOutgoingNodes()) {
				outgoingNode.inputAvailable();
				if (outgoingNode.isScheduable()) {
					readyToScheduleNodes.add(outgoingNode);
				}
			}
		}
	}


	
	private void scheduledSendNode(Node node){
		
		if (node.getIncomingNodes().size() != 1) {
			System.err.println("Number of incoming edges in send node is not 1");
		}
		// in fact there can be only one input node
		for (Node incomingNode : node.getIncomingNodes()) {
			//notify node before send not that input is consumed
			incomingNode.consumedInput();
			if (incomingNode.canDeleteOutput()) {
				this.currentlyUsedMemory = this.currentlyUsedMemory - incomingNode.getOutgoingTensorSize();
			}
		}

		if (node.getOutgoingNodes().size() != 1) {
			System.err.println("Number of outgoing edges in send node is not 1");
		}
		ReceiveNode receiveNode = null;
		for (Node outgoingNode : node.getOutgoingNodes()) {
			receiveNode = (ReceiveNode) outgoingNode;
		}
		// get device where receive node is located
		Device deviceReceiveNode = devices.get(receiveNode.getDeviceId());
		double transferTime = (double) node.getOutgoingTensorSize()
				/ (double) this.transferRate[receiveNode.getDeviceId()];
		double dataArrivalTime = this.localTimestamp + transferTime;
		evaluation.addCommunicationCost(node.getOutgoingTensorSize());
		int numberOfReceiveNodes = deviceReceiveNode.receiveNodes.size();

		receiveNode.setReceiveTime(dataArrivalTime);
		deviceReceiveNode.receiveNodes.add(receiveNode);
		
		//one receive node has to be added in set
		if(numberOfReceiveNodes + 1 != deviceReceiveNode.receiveNodes.size()){
			System.err.println("Error, node " + receiveNode.getId() + " not added to receive nodes or replaces another existing node");
			System.err.println("Data arrival time: "+ dataArrivalTime + " numberOfReceiveNodes before change " +numberOfReceiveNodes + "after insertion: " + deviceReceiveNode.receiveNodes.size());
			deviceReceiveNode.printReceiveNodes();
		}
		// System.out.println("Send to device" +
		// deviceReceiveNode.getId() + " " + dataArrivalTime )
	}
	
	//for HeftPartitioning
	public ScheduledTimeSlots getScheduledTimeSlotForNode(Node node){
		double earliestStartTime = node.getEarliestStartTimeForDevice(this);
		Iterator<ScheduledTimeSlots> iterator = scheduledNodes.iterator();

		ScheduledTimeSlots lastSlot = new ScheduledTimeSlots(0,0, this.getId());
		//System.out.println();
		//System.out.println(this.getId());
		while (iterator.hasNext()) {

			ScheduledTimeSlots slot = iterator.next();
			//System.out.println(slot.getStartTime() + " - " + slot.getEndTime());
			
			if(slot.getStartTime() > earliestStartTime){
				double startTime =  Math.max(lastSlot.getEndTime(), earliestStartTime);
				double freeDuration = slot.getStartTime() - startTime;
				double processingDuration = (double) node.getNumberOfOperations()/ (double)this.getNumberOfOperations();
				if(freeDuration >= processingDuration){
					//System.out.println("insertion: " + startTime + " - " + (startTime + processingDuration));
					//System.err.println("called");
					return new ScheduledTimeSlots(startTime,startTime + processingDuration, this.getId());
				}
			}
			lastSlot = slot;
		}
		//have not found any fitting gap, add at the end
		double processingDuration = (double) node.getNumberOfOperations()/(double) this.getNumberOfOperations();
		double startTime =  Math.max(lastSlot.getEndTime(), earliestStartTime);
		return new ScheduledTimeSlots(startTime,startTime + processingDuration, this.getId());
	}
	
	public void addScheduledTimeSlot(ScheduledTimeSlots timeslot){
		scheduledNodes.add(timeslot);
		//System.out.println("scheduledNodes-size" + scheduledNodes.size());
	}

	public Graph getGraph() {
		return this.graph;
	}

	public String graphVisualisation() {
		StringBuilder builder = new StringBuilder();
		builder.append("subgraph cluster_" + this.id + " {\n");
		builder.append("label=\"Device " + this.id + "\";\n");
		builder.append(this.graph.nodesToString() + "\n");
		builder.append("}\n\n");
		builder.append(this.graph.graphVisualisation() + "\n");
		return builder.toString();
	}

	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Device: " + this.id + "\n");
		builder.append("memory: " + this.memory + "\n");
		builder.append("estimated memory usage: " + (this.memory - this.freeMemoryEstimaton) + "\n");
		builder.append("numberOfOperations: " + this.numberOfOperations + "\n");
		builder.append("type: " + this.type + "\n");
		builder.append("number of Nodes: " + this.graph.numberOfNodes() + "\n");
		builder.append("evaluation" + "\n");
		builder.append(deviceEvaluation.toString());
		return builder.toString();
	}
	
	public int getNumberOfOperations(){
		return this.numberOfOperations;
	}
	
	public int getSumOfOperationsOfAlreadyAssignedNodes(){
		return this.sumOfOperationsOfAlreadyAssignedNodes;
	}
	
	public int getMemory(){
		return this.memory;
	}
	public int getFreeMemoryEstimation(){
		return this.freeMemoryEstimaton;
	}

	public DeviceEvaluation getEvaluation() {
		return this.deviceEvaluation;
	}

	public int getCurrentlyUsedMemory() {
		return this.currentlyUsedMemory;
	}

	// only for testing
	public int[] getCommunicationCost() {
		return this.transferRate;
	}

	// only for testing
	public int getFreeMemory() {
		return this.freeMemoryEstimaton;
	}
	//only for evaluation
	public double getLastNodeFinishedTimestamp() {
		return lastNodeFinishedTimestamp;
	}




}
