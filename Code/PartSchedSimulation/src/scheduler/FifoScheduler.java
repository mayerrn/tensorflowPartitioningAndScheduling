package scheduler;

//import java.util.HashSet;
import java.util.Queue;

import graph.Node;

public class FifoScheduler extends Scheduler {
	public Node getNextNodeToSchedule(Queue<Node> readyToScheduleNodes){
		return readyToScheduleNodes.peek();
	}
}
