package graph;

public class SendNode extends TransferNode{
	public SendNode(int id, int outgoingTensorSize) {
		super(id, outgoingTensorSize, NodeType.NodeTypeSend);
	}
}
