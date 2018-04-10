package Device;

import graph.NodeType;

public class DeviceEvaluation {

	private double calculationTime = 0;
	// how many possibilities has scheduling algorithm to select a node
	public int[] schedulingPossibilities;
	// at which position we are in the schedulingPossibilities array
	private int schedulingPointer = 0;
	// used Ram of device before each scheduling decision
	public int[] RamUsage;
	private int ramUsagePointer = 0;
	
	public double[] startTimeScheduling;
	private int startTimeSchedulingPointer = 0;	
	
	public double[] executionDuration;
	private int executionDurationPointer = 0;	
	private double maxExecutionTimePerNode;
	
	double totalExecutionTime = 0.0;

	int maxRamUsage = -1;
	int maxSchedulingPossibilities = -1;

	private int numberOfSendNodes;
	private int numberOfReceiveNodes;
	//number of nodes without send and receive nodes
	private int numberOfNodes;

	public DeviceEvaluation() {

	}

	public int getNumberOfTransferNodes() {
		return this.numberOfReceiveNodes + this.numberOfSendNodes;
	}

	
	public void addedSendNode() {
			this.numberOfSendNodes++;
	}
	
	public void addedReceiveNode() {
		this.numberOfReceiveNodes++;
	}

	public void addedNode() {
		numberOfNodes++;
	}

	public int getNumberOfNodes() {
		return this.numberOfNodes;
	}

	public void addCalculationTime(double startTime, double executionTime) {
		if (startTimeScheduling == null) {
			// as only the send nodes are scheduled
			this.startTimeScheduling = new double[this.numberOfNodes + this.numberOfSendNodes];
			this.executionDuration = new double[this.numberOfNodes + this.numberOfSendNodes];
		}
		if(executionTime > this.maxExecutionTimePerNode){
			this.maxExecutionTimePerNode = executionTime;
		}
		
		this.startTimeScheduling[this.startTimeSchedulingPointer] = startTime;
		this.startTimeSchedulingPointer ++;
		this.executionDuration[this.executionDurationPointer] = executionTime;
		this.executionDurationPointer++;
		
		this.calculationTime += executionTime;
	}

	public double getCalculationTime() {
		return calculationTime;
	}

	public void addSchedulingPossibility(int numberOfPossibilities) {
		if (schedulingPossibilities == null) {
			// as only the send nodes are scheduled
			this.schedulingPossibilities = new int[this.numberOfNodes + this.numberOfSendNodes];
		}

		if (numberOfPossibilities > maxSchedulingPossibilities) {
			maxSchedulingPossibilities = numberOfPossibilities;
		}
		schedulingPossibilities[schedulingPointer] = numberOfPossibilities;
		schedulingPointer++;
	}

	public void addRamUsage(int ramUsage) {
		if (RamUsage == null) {
			// as only the send nodes are scheduled
			// as we want to know RAM at the beginning and end: +1
			this.RamUsage = new int[this.numberOfNodes + this.numberOfSendNodes + 1];
		}
		if (ramUsage > maxRamUsage) {
			maxRamUsage = ramUsage;
		}
		RamUsage[ramUsagePointer] = ramUsage;
		ramUsagePointer++;
	}

	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("number of nodes: " + this.getNumberOfNodes() + "\n");
		builder.append("number of transfer nodes: " + this.getNumberOfTransferNodes() + "\n");
		builder.append("utilization: " + this.getActiveCalculationPercentage() + "\n");
		builder.append("max ram usage: " + this.maxRamUsage + "\n");
		if(RamUsage != null){
		for (int i = 0; i < RamUsage.length; i++) {
			builder.append(RamUsage[i] + ", ");
		}
		}
		builder.append("\n");
		builder.append("max scheduling possibilities: " + this.maxSchedulingPossibilities + "\n");
		if(schedulingPossibilities == null){
			schedulingPossibilities = new int[0];
			startTimeScheduling = new double[0];
			executionDuration = new double[0];
			System.err.println("Empty partition");
		}
		for (int i = 0; i < schedulingPossibilities.length; i++) {
			builder.append(schedulingPossibilities[i] + ", ");
		}
		builder.append("\n");
		builder.append("start time: ");

		for (int i = 0; i < this.startTimeScheduling.length; i++) {
			builder.append(startTimeScheduling[i] + ", ");
		}
		builder.append("\n");
		builder.append("max per node executionTime: " + this.maxExecutionTimePerNode + "\n");
		for (int i = 0; i < this.executionDuration.length; i++) {
			builder.append(executionDuration[i] + ", ");
		}
		builder.append("\n");
		return builder.toString();
	}

	public int[] getSchedulingPossibilities(){
		if(this.schedulingPossibilities == null){
			return new int[0];
		}
		return this.schedulingPossibilities;
	}
	
	public int[] getRamUsage(){
		if(this.RamUsage == null){
			return new int[0];
		}
		return this.RamUsage;
	}
	
	public double[] getExecutionDuration(){
		if(this.executionDuration == null){
			return new double[0];
		}
		return this.executionDuration;
	}
	
	public double[] getStartTimeScheduling(){
		if(this.startTimeScheduling == null){
			return new double[0];
		}
		return this.startTimeScheduling;
	}
	
	public void setTotalExecutionTime(double time) {
		this.totalExecutionTime = time;
	}

	public double getActiveCalculationPercentage() {
		return this.calculationTime / this.totalExecutionTime;
	}

}
