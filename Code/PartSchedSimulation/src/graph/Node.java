package graph;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import Device.Device;
import Device.DeviceConstraint;
import misc.Config;
import misc.Util;

public class Node implements ConstraintInterface{
	
	//general properties
	private int id;
	private int outgoingTensorSize;
	private int numberOfOperations;
	private HashSet<Node> outgoingNodes = new HashSet<Node>();
	private HashSet<Node> incomingNodes = new HashSet<Node>();
	private int RamToStore = 0;
	//counts how many nodes have consumed this nodes input (if number equals number of outgoing nodes the output can be deleted)
	private int consumedInput = 0;
	//counts how many input tensors are already computed, node can be executed if availableInput is equal to number of incoming nodes
	private int availableInput = 0;

	//is -1, when it is not yet calculated
	private int maxEstimatedTensorRamUsage = -1;
	
	//e.g. CPU if kernel for this operation is only implemented on a CPU device
	private DeviceConstraint constraint;
	
	//is -1, when it is not yet assigned
	private int deviceId = -1;
	//is -1, when it is not colocated
	private int colocationId = -1;
	
	
	//properties that are set depending on which partitioning and scheduling is seleceted
	
	//for scheduling: LargestRemainedOperationFirst
	//is 0 when operation is not supported
	private int remainedOperationsTillSinkNode = 0;
	private int remainedOperationsReceivedFromOutput = 0;

	//LargestTimeToFinishFirst
	private double remainedTimeToFinishTillSinkNode = 0;
	private int remainedTimesReceivedFromOutput = 0;
	

	//for partitioning
	//DFSPartitioning
	private boolean dfsAlreadyVisited = false;
	
	//DFS Partitioning,DividePath2Partitioning,...
	private int remainedOperationsTillSinkNodeWithoutSendReceiveNodes = 0;
	private int remainedOperationsReceivedFromOutputWithOutSendReceiveNodes = 0;
	//TODO ggf refractoring und WIthoutSendReceiveNodes erstetzen, würde dann einfach vom zeitpunkt der Berechnung abhängen
	private int opsTillNodeWithoutSendReceiveNodes = 0;
	//counts how many incoming nodes have already told the node their values
	private int opsTillNodeNumberOfInputsWithoutSendReceiveNodes = 0;	
	
	
	
	private int pathId = -1;
	private Node predecessorNode = null;
	private Node successorNode = null;

	
	//for DividePath
	public HashSet<Node> outgoingNodesRemained;
	public HashSet<Node> incomingNodesRemained;
	
	
	//for RangeBasedImportanceHashing
	//sum of #operation till sink node + #ops from source node
	private int sumOpsTillSinkNodeOpsFromSourceNode = 0;
	
	//for HeftPartitioning, AssignLongestPath
	private double finishTime = -1.0;
	private double upwardsRank = 0;
	private int upwardsRankReceived = 0;
	private double tieBreaker = Math.random();
	//for AssignLongestPath
	private Node upwardsRankSuccessorNode = null;
	
	public Node(CreationNodeEntry entry){
		setInformation(entry.getId(),entry.getOutgoingTensorSize(),entry.getNumberOfOperations(), entry.getRamUsage(), entry.getConstraint());
	}
	
	protected Node(int id, int outgoingTensorSize, int numberOfOperations, int RamToStore, DeviceConstraint constraint){
		setInformation(id,outgoingTensorSize,numberOfOperations, RamToStore, constraint);
	}
	
	public void setInformation(int id, int outgoingTensorSize, int numberOfOperations,  int RamToStore, DeviceConstraint constraint){
		this.id = id;
		this.outgoingTensorSize = outgoingTensorSize;
		this.numberOfOperations = numberOfOperations;
		this.RamToStore = RamToStore;
		this.constraint = constraint;
	}
	
	protected Node(int id){
		this.id = id;
	}
	
	public int getId(){
		return this.id;
	}

	public Set<Node> getOutgoingNodes() {
		return outgoingNodes;
	}

	public Set<Node> getIncomingNodes() {
		return incomingNodes;
	}

	public int getDeviceId(){
		return this.deviceId;
	}
	
	public void setDeviceId(int deviceId){
		if(this.deviceId != -1){
			System.err.println("Device Id is set mutliple times" + deviceId + " " + this.deviceId);			
		}
		this.deviceId = deviceId;
	}
	
	public boolean isAssigned(){
		return this.getDeviceId() != -1;
	}
	public int getOutgoingTensorSize() {
		return outgoingTensorSize;
	}

	public int getNumberOfOperations() {
		return numberOfOperations;
	}
	
	public boolean isAssignedOnColocationComponent(){
		return this.colocationId != -1;
	}
	
	public int getColocationId(){
		return this.colocationId;
	}
	
	public void setColocationId(int colocationId){
		this.colocationId = colocationId;
	}

	public int getRamToStore(){
		return this.RamToStore;
	}
	
	//TODO sum of input and output tensor -> bad heuristic -> might improve it later
	public int getMaxEstimatedTensorRAMUsage(){
		if(maxEstimatedTensorRamUsage == -1){
			this.maxEstimatedTensorRamUsage = this.outgoingTensorSize;
			for(Node node: this.getIncomingNodes()){
				this.maxEstimatedTensorRamUsage += node.outgoingTensorSize;
			}
		}

		return this.maxEstimatedTensorRamUsage;
	}
	

	@Override
	public int getTotalRamDemand() {
		// TODO Auto-generated method stub
		return this.getRamToStore() + this.getMaxEstimatedTensorRAMUsage();
	}

	@Override
	public DeviceConstraint getDeviceConstraint() {
		// TODO Auto-generated method stub
		return this.constraint;
	}
	
	public String toString(){
		StringBuilder builder = new StringBuilder();
		builder.append("Node: " + this.id + "\n");
		builder.append("OutgoingTensorSize: "+ this.outgoingTensorSize + "\n");
		builder.append("Number of operations "+ this.numberOfOperations + "\n");
		builder.append("Required Ram for storing: " + this.RamToStore + "\n");
		builder.append("Max estimated storage for tensors:" + this.getMaxEstimatedTensorRAMUsage()+ "\n");
		builder.append("Device constraint: " + this.constraint + "\n");
		builder.append("Colocation Id: " + this.colocationId + "\n");
		builder.append("Device Id: " + this.deviceId + "\n");
		builder.append("Type: " + this.getClass() + "\n");
		builder.append("Outgoing Nodes: ");
		for(Node node: this.outgoingNodes){
			builder.append(node.getId() + "; ");
		}
		builder.append("\n");
		builder.append("Incoming Nodes: ");
		for(Node node: this.incomingNodes){
			builder.append(node.getId() + "; ");
		}
		builder.append("\n");
		builder.append("Remained node: " + this.remainedOperationsTillSinkNode + "\n");
		builder.append("\n");

		return builder.toString();
	}
	
	public void consumedInput(){
		consumedInput ++;
	}
	public boolean canDeleteOutput(){
		return consumedInput == this.outgoingNodes.size();
	}
	
	public void inputAvailable(){
		this.availableInput ++;
	}
	public boolean isScheduable(){
		return this.availableInput == this.incomingNodes.size();
	}
	
	
	public int getRemainedOperationsTillSinkNode(){
		return this.remainedOperationsTillSinkNode;
	}
	

	public void calculateRemainedOperationsTillSinkNode(Node outgoingNode){
		if(outgoingNode != null){
			this.remainedOperationsReceivedFromOutput ++;
			if(this.remainedOperationsTillSinkNode < outgoingNode.remainedOperationsTillSinkNode){
				this.remainedOperationsTillSinkNode = outgoingNode.remainedOperationsTillSinkNode;
			}
		}

		if(this.remainedOperationsReceivedFromOutput == this.getOutgoingNodes().size()){
			this.remainedOperationsTillSinkNode = this.remainedOperationsTillSinkNode + this.numberOfOperations;
			//our value is correctly calculated -> notify inputs
			for(Node incomingNode: this.incomingNodes){
				incomingNode.calculateRemainedOperationsTillSinkNode(this);
			}
		}
	}
	
	public int getRemainedOperationsTillSinkNodeWithoutSendReceiveNodes(){
		return this.remainedOperationsTillSinkNodeWithoutSendReceiveNodes;
	}
	
	public int getOpsTillNodeWithoutSendReceiveNodes(){
		return this.opsTillNodeWithoutSendReceiveNodes;
	}
	
	

	public void calculateRemainedOperationsTillSinkNodeWithoutSendReceive(Node outgoingNode){
		
		if(outgoingNode != null){
			this.remainedOperationsReceivedFromOutputWithOutSendReceiveNodes++;
			if(this.remainedOperationsTillSinkNodeWithoutSendReceiveNodes < outgoingNode.remainedOperationsTillSinkNodeWithoutSendReceiveNodes){
				this.remainedOperationsTillSinkNodeWithoutSendReceiveNodes = outgoingNode.remainedOperationsTillSinkNodeWithoutSendReceiveNodes;
				this.successorNode = outgoingNode;
			}
		}
		
		if(remainedOperationsReceivedFromOutputWithOutSendReceiveNodes == this.getOutgoingNodes().size()){
			this.remainedOperationsTillSinkNodeWithoutSendReceiveNodes = this.remainedOperationsTillSinkNodeWithoutSendReceiveNodes + this.numberOfOperations;
			this.sumOpsTillSinkNodeOpsFromSourceNode = getSumOpsTillSinkNodeOpsFromSourceNode() + this.remainedOperationsTillSinkNodeWithoutSendReceiveNodes;
			//our value is correctly calculated -> notify inputs
			for(Node incomingNode: this.incomingNodes){
				incomingNode.calculateRemainedOperationsTillSinkNodeWithoutSendReceive(this);
			}


		}
	}
	
		public void calculateOpsTillNodeWithoutSendReceive(Node incomingNode, boolean dividePathMethod){
			if(opsTillNodeNumberOfInputsWithoutSendReceiveNodes == -42){
				this.opsTillNodeNumberOfInputsWithoutSendReceiveNodes = 0;
				this.opsTillNodeWithoutSendReceiveNodes = -1;
				this.predecessorNode = null;
			}
			
			if(incomingNode != null){
				this.opsTillNodeNumberOfInputsWithoutSendReceiveNodes ++;
				if(this.opsTillNodeWithoutSendReceiveNodes < incomingNode.opsTillNodeWithoutSendReceiveNodes + incomingNode.numberOfOperations){
					this.opsTillNodeWithoutSendReceiveNodes = incomingNode.opsTillNodeWithoutSendReceiveNodes + incomingNode.numberOfOperations;
					this.predecessorNode = incomingNode;
				}
			}
						
			int numberOfIncomingNodes = 0;
			if(dividePathMethod){
				numberOfIncomingNodes = this.incomingNodesRemained.size();
			}else{
				numberOfIncomingNodes = this.getIncomingNodes().size();
			}
			
			if(opsTillNodeNumberOfInputsWithoutSendReceiveNodes == numberOfIncomingNodes){
				this.sumOpsTillSinkNodeOpsFromSourceNode = getSumOpsTillSinkNodeOpsFromSourceNode() + opsTillNodeWithoutSendReceiveNodes;
				//our value is correctly calculated -> notify inputs
				HashSet<Node> outgoingNodes;
				if(dividePathMethod){
					outgoingNodes = this.outgoingNodesRemained;
				}else{
					outgoingNodes = this.outgoingNodes;
				}
				
				
				for(Node outgoingNode: outgoingNodes){
					outgoingNode.calculateOpsTillNodeWithoutSendReceive(this, dividePathMethod);
				}
				//reset if it is recalculated
				opsTillNodeNumberOfInputsWithoutSendReceiveNodes = -42;
			}
		}
		
		
		public double getRemainedTimeToFinishTillSinkNode(){
			return this.remainedTimeToFinishTillSinkNode;
		}
		

		public void calculateRemainedTimeToFinishTillSinkNode(Node outgoingNode, HashMap<Integer, Device> devices){
			if(outgoingNode != null){
				this.remainedTimesReceivedFromOutput ++;
				double arrivingTime;
				if(outgoingNode.getDeviceId() == this.getDeviceId()){
					arrivingTime = outgoingNode.remainedTimeToFinishTillSinkNode;
				}else{
					double transferRate = (double) devices.get(outgoingNode.getDeviceId()).getCommunicationCost()[this.getDeviceId()];
					if(transferRate == 0){
						System.err.println("Transfer rate should not be 0:" + outgoingNode.getDeviceId() + "+" + this.getDeviceId());
					}
					arrivingTime = outgoingNode.remainedTimeToFinishTillSinkNode + (double)outgoingNode.getOutgoingTensorSize()/transferRate;
				}
				
				if(this.remainedTimeToFinishTillSinkNode < arrivingTime){
					this.remainedTimeToFinishTillSinkNode = arrivingTime;
				}
			}

			if(this.remainedTimesReceivedFromOutput == this.getOutgoingNodes().size()){
				this.remainedTimeToFinishTillSinkNode = this.remainedTimeToFinishTillSinkNode + (double) this.numberOfOperations/ (double) devices.get(this.getDeviceId()).getNumberOfOperations();
				//our value is correctly calculated -> notify inputs
				for(Node incomingNode: this.incomingNodes){
					incomingNode.calculateRemainedTimeToFinishTillSinkNode(this, devices);
				}
			}
		}
		
		
		public void copyNodes(){
			this.incomingNodesRemained = new HashSet<Node>(this.incomingNodes);
			this.outgoingNodesRemained = new HashSet<Node>(this.outgoingNodes);

		}
		
		public double getEarliestStartTimeForDevice(Device device){
			double earliestStartTime = Double.MAX_VALUE;
			boolean changed = false;
			for(Node predecessorNode: this.getIncomingNodes()){
				double startTime = predecessorNode.getFinishTime() + (double) predecessorNode.getOutgoingTensorSize()/(double) device.getCommunicationCost()[predecessorNode.getDeviceId()];
				if(startTime < earliestStartTime){
					earliestStartTime = startTime;
					changed = true;
				}
			}
			//important for source nodes
			if(!changed){
				return 0.0;
			}
			return earliestStartTime;
		}
		
		public void calculateUpwardsRank(Node successorNode, double averageTransferSpeed, double averageComputationSpeed){

			if(successorNode != null){
				this.upwardsRankReceived ++;
				if(this.upwardsRank <  successorNode.upwardsRank){
					this.upwardsRankSuccessorNode = successorNode;
					this.upwardsRank =  successorNode.upwardsRank + (this.getOutgoingTensorSize()/averageTransferSpeed);
				}
			}
									
			if(upwardsRankReceived == this.getOutgoingNodes().size()){
				this.upwardsRank = this.upwardsRank + ((double) this.numberOfOperations/averageComputationSpeed);
				//our value is correctly calculated -> notify inputs
				for(Node predecessorNode: this.incomingNodes){
					predecessorNode.calculateUpwardsRank(this, averageTransferSpeed, averageComputationSpeed);
				}
			}
		}
		
		public double getUpwardsRank(){
			return this.upwardsRank;
		}

		public boolean isDfsAlreadyVisited() {
			return dfsAlreadyVisited;
		}

		public void setDfsAlreadyVisited(boolean dfsAlreadyVisited) {
			this.dfsAlreadyVisited = dfsAlreadyVisited;
		}

		public int getPathId() {
			return pathId;
		}

		public void setPathId(int pathId) {
			this.pathId = pathId;
		}

		public Node getPredecessorNode() {
			return predecessorNode;
		}

		public Node getSuccessorNode() {
			return successorNode;
		}

		public int getSumOpsTillSinkNodeOpsFromSourceNode() {
			return sumOpsTillSinkNodeOpsFromSourceNode;
		}


		public double getTieBreaker() {
			return tieBreaker;
		}

		public Node getUpwardsRankSuccessorNode() {
			return upwardsRankSuccessorNode;
		}

		public double getFinishTime() {
			return finishTime;
		}

		public void setFinishTime(double finishTime) {
			this.finishTime = finishTime;
		}
		
		public int getAvailableInputNumber(){
			return this.availableInput;
		}


}
