package graph;

import Device.DeviceConstraint;

public class TransferNode extends Node {

	private NodeType type;	
	
	protected TransferNode(int id, int outgoingTensorSize, NodeType type) {
		super(id, outgoingTensorSize, 10, 10, DeviceConstraint.NO);
		this.type = type;
	}

	//TODO find possibility that ram use for storage is also consider for Transfer nodes
	@Override
	public int getTotalRamDemand() {
		//input und output tensor sizes werden schon bei richtigen Knoten berücksichtigt 
		// TODO Auto-generated method stub
		return 0;
	}
	
	public NodeType getType(){
		return this.type;
	}
}
