package graph;

public class ReceiveNode extends TransferNode{
	
	private double receiveTime = Double.MAX_VALUE;
	
	public ReceiveNode(int id, int outgoingTensorSize) {
		super(id, outgoingTensorSize, NodeType.NodeTypeReceive);
	}
	
	public double getReceiveTime(){
		return this.receiveTime;
	}
	
	public void setReceiveTime(double receiveTime){
		if(this.receiveTime != Double.MAX_VALUE){
			System.err.println("Receive time is set multiple times!");
		}
		this.receiveTime = receiveTime;
	}
	
	
}
