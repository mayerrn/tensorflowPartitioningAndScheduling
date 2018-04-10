package scheduler;

import java.util.HashMap;
//import java.util.HashSet;
import java.util.Queue;

import Device.Device;
import graph.Node;

public abstract class Scheduler {
	
	protected HashMap<Integer, Device> devices; 
	
	public abstract Node getNextNodeToSchedule(Queue<Node> readyToScheduleNodes);
	
	public static Scheduler strategyForString(String strategyName){
		if(strategyName.equals("FifoScheduler")){
			return new FifoScheduler();
		}else if(strategyName.equals("LargestRemainedOperationFirstScheduler")){
			return new LargestRemainedOperationFirstScheduler();
		}else if(strategyName.equals("HeftScheduler")){
			return new HeftScheduler();
		}else if(strategyName.equals("LargestRemainedTimeToFinishFirst")){
			return new LargestRemainedTimeToFinishFirst();
		}else if(strategyName.equals("HighDegreeVerticesFirstScheduler")){
			return new HighDegreeVerticesFirstScheduler();
		}
		
		return null;
	}
	
	public static String abbreavatonForSchedulingName(String strategyName){
		if(strategyName.equals("FifoScheduler")){
			return "FIFO";
		}else if(strategyName.equals("LargestRemainedOperationFirstScheduler")){
			return "LargestOpsFirst";
		}else if(strategyName.equals("HeftScheduler")){
			return " ";
		}else if(strategyName.equals("LargestRemainedTimeToFinishFirst")){
			return "PCT";
		}else if(strategyName.equals("HighDegreeVerticesFirstScheduler")){
			return "MSR";
		}
		return null;
	}

	public void setDevices(HashMap<Integer, Device> devices) {
		this.devices = devices;
	}
}
