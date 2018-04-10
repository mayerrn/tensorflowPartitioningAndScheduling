package scheduler;

//import java.util.HashSet;
import java.util.Queue;

import graph.Node;

public class LargestRemainedOperationFirstScheduler extends Scheduler{
	
	public Node getNextNodeToSchedule(Queue<Node> readyToScheduleNodes){
		Node selectedNode = null;
		
		for(Node node: readyToScheduleNodes){
			//System.out.println(node.getRemainedOperationsTillSinkNode() );
			if(selectedNode == null){
				selectedNode = node;
			}else{
				//System.out.println(node.getRemainedOperationsTillSinkNode());
				if(node.getRemainedOperationsTillSinkNode() > selectedNode.getRemainedOperationsTillSinkNode()){
					selectedNode = node;
				}
			}
		}
		//System.out.println(selectedNode.getRemainedOperationsTillSinkNode() + ", node: " + selectedNode.getId());

		return selectedNode;
	}
}
