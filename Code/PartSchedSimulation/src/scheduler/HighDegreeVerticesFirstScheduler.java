package scheduler;

import java.util.Queue;

import graph.Node;

public class HighDegreeVerticesFirstScheduler extends Scheduler {

	
	@Override
	public Node getNextNodeToSchedule(Queue<Node> readyToScheduleNodes) {
		Node selectedNode = null;
		double currentMax = 0.0;
	
		for(Node node: readyToScheduleNodes){
			//System.out.println(node.getRemainedOperationsTillSinkNode() );
			if(selectedNode == null){
				selectedNode = node;
				currentMax = getValueForNode(node);
			}else{
				double currentValue = this.getValueForNode(node);
				if(currentMax < currentValue){
					selectedNode = node;
					currentMax = currentValue;
				}else if(currentMax == currentValue){
					//added tiebreaker
					if(node.getRemainedTimeToFinishTillSinkNode() > selectedNode.getRemainedTimeToFinishTillSinkNode()){
						selectedNode = node;
					}
				}
				//System.out.println("value: " + currentValue);
			}
		}
		//System.out.println(selectedNode.getRemainedOperationsTillSinkNode() + ", node: " + selectedNode.getId());
		//System.out.println("poss: " + readyToScheduleNodes.size() + "  " + currentMax);

		return selectedNode;
	}
	//evtl noch transferRate beachten
	//Punkte: 1 Punkt für outgoingNode auf gleichem Device
	//3 Punkte für outglingNode auf anderem Device
	// --> vllt sogar am wichtigsten //1 Punkt wenn anderer Knoten dann scheduable wird + Punkt wenn numberOfScheduableNodes gering ist!!! -> weil dann verhindert man leerstellen	
	
	//ggf. noch Gewicht der Nachfolgerknoten beruecksichtigen
	private double getValueForNode(Node node){
		double objective = 0.0;

		for(Node successor: node.getOutgoingNodes()){
			objective += 1;
			if(successor.getDeviceId() != node.getDeviceId()){
				objective += 1;
				if(successor.getIncomingNodes().size() - successor.getAvailableInputNumber() == 1){
					objective += 1;
					if(!this.devices.get(successor.getDeviceId()).isScheduable()){
						objective += 5;
					}
				}
			}
		}
		
		return objective;
	}
}
