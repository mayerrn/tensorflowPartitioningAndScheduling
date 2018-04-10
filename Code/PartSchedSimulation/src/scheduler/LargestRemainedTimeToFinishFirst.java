package scheduler;

import java.util.Queue;

import graph.Node;

public class LargestRemainedTimeToFinishFirst extends Scheduler{
	
	@Override
	public Node getNextNodeToSchedule(Queue<Node> readyToScheduleNodes){
		Node selectedNode = null;
		
		for(Node node: readyToScheduleNodes){
			//System.out.println(node.getRemainedOperationsTillSinkNode() );
			if(selectedNode == null){
				selectedNode = node;
			}else{
				//System.out.println(node.getRemainedOperationsTillSinkNode());
				if(node.getRemainedTimeToFinishTillSinkNode() > selectedNode.getRemainedTimeToFinishTillSinkNode()){
					selectedNode = node;
				}
			}
		}
		//System.out.println(selectedNode.getRemainedTimeToFinishTillSinkNode() + ", node: " + selectedNode.getId());
		return selectedNode;
	}

}
