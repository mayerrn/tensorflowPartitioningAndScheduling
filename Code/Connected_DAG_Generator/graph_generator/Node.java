package graph_generator;
import java.util.HashSet;
import java.util.Set;


public class Node {
	private int id;
	String name;
	private int outgoingTensorSize;
	private int numberOfOperations;
	private Set<Node> outgoingNodes = new HashSet<Node>();
	private Set<Node> incomingNodes = new HashSet<Node>();
	private Set<Node> colocationNodes = new HashSet<Node>();
	private int RamForStoring;
	private DeviceConstraint constraint;
	public boolean alreadyColocated = false;
	public int level = -1;

	
	//private boolean visited = false;
	public Node(int id, String name){
		this.id = id;
		this.outgoingTensorSize = Util.intFromRange(GraphConfig.MIN_EDGE_WEIGHT, GraphConfig.MAX_EDGE_WEIGHT);
		this.numberOfOperations = Util.intFromRange(GraphConfig.MIN_OP_WEIGHT, GraphConfig.MAX_OP_WEIGHT);
		this.name = name;
		this.RamForStoring = Util.intFromRange(GraphConfig.MIN_RAM_FOR_NODE_STORAGE, GraphConfig.MAX_RAM_FOR_NODE_STORAGE);
		this.constraint = this.getConstraint();
	}
	
	

	public int getId() {
		return id;
	}

	public Set<Node> getOutgoingNodes() {
		return outgoingNodes;
	}

	public Set<Node> getIncomingNodes() {
		return incomingNodes;
	}
	
	public Set<Node> getColocationNodes() {
		return colocationNodes;
	}
	
	private DeviceConstraint getConstraint(){
		if(Math.random() < GraphConfig.CPU_CONSTRAINT_PROHABILITY){
			return DeviceConstraint.CPU;
		}//by doing it like that probability is not exact the same like in Config but this should be fine.
		else if( Math.random() < GraphConfig.GPU_CONSTRAINT_PROHABILITY){
			return DeviceConstraint.GPU;
		}
		return DeviceConstraint.NO;
	}
	
	public DeviceConstraint getDeviceConstraint(){
		return this.constraint;
	}
	
	public int getNumberOfOperations(){
		return this.numberOfOperations;
	}
	
	public void setNumberOfOperations(int numberOfOperations){
		this.numberOfOperations = numberOfOperations;
	}
	
	public String toString(){
		StringBuilder builder = new StringBuilder();
		builder.append(this.getId());
		builder.append(GraphConfig.CATEGORY_SEPERATOR);
		builder.append(Util.nodeSetToString(this.getOutgoingNodes()));
		builder.append(GraphConfig.CATEGORY_SEPERATOR);
		builder.append(Util.nodeSetToString(this.getIncomingNodes()));
		builder.append(GraphConfig.CATEGORY_SEPERATOR);
		builder.append(Util.nodeSetToString(this.getColocationNodes()));
		builder.append(GraphConfig.CATEGORY_SEPERATOR);
		builder.append(outgoingTensorSize);
		builder.append(GraphConfig.CATEGORY_SEPERATOR);
		builder.append(numberOfOperations);
		builder.append(GraphConfig.CATEGORY_SEPERATOR);
		builder.append(this.RamForStoring);
		builder.append(GraphConfig.CATEGORY_SEPERATOR);
		builder.append(this.constraint);
		builder.append(GraphConfig.CATEGORY_SEPERATOR);
		builder.append(name);
		builder.append("\n");
			
		return builder.toString();
	}
	
	public void removeAllEdges(){
		this.outgoingNodes = new HashSet<Node>();
		this.incomingNodes = new HashSet<Node>();
	}
	
}
