package scheduler;

import java.util.Queue;

import graph.Node;

public class HeftScheduler extends Scheduler{
	
	public Node getNextNodeToSchedule(Queue<Node> readyToScheduleNodes){
		Node selectedNode = null;
		
		for(Node node: readyToScheduleNodes){
			//System.out.println(node.getRemainedOperationsTillSinkNode() );
			if(selectedNode == null){
				selectedNode = node;
			}else{
				if(node.getFinishTime() < selectedNode.getFinishTime()){
					//System.out.println(node.getUpwardsRank());
					selectedNode = node;
				}
			}
		}
		//System.out.println(selectedNode.getRemainedOperationsTillSinkNode() + ", node: " + selectedNode.getId());

		return selectedNode;
	}
}