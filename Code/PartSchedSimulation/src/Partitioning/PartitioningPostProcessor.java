package Partitioning;

import java.util.HashMap;

import Device.Device;
import graph.CreationGraph;
import graph.Node;
import scheduler.FifoScheduler;
import scheduler.HighDegreeVerticesFirstScheduler;
import scheduler.LargestRemainedOperationFirstScheduler;
import scheduler.LargestRemainedTimeToFinishFirst;
import scheduler.Scheduler;

public class PartitioningPostProcessor {
	
		private CreationGraph graph;
		private HashMap<Integer, Device> devices;
		private Scheduler scheduler;
		
		public PartitioningPostProcessor(CreationGraph graph, HashMap<Integer, Device> devices, Scheduler scheduler){
			this.graph = graph;
			this.devices = devices;
			this.scheduler = scheduler;
		}
		
		public void postProcess(){
			PartitioningStrategy.addSendReceiveNodes(devices, graph.getMaxNodeId() + 1);
			
			if(scheduler.getClass().equals(LargestRemainedOperationFirstScheduler.class)){
				for(Node node: graph.getSinkNodes()){
					node.calculateRemainedOperationsTillSinkNode(null);
				}
			}
			if(scheduler.getClass().equals(LargestRemainedTimeToFinishFirst.class) || scheduler.getClass().equals(HighDegreeVerticesFirstScheduler.class)){
				for(Node node: graph.getSinkNodes()){
					node.calculateRemainedTimeToFinishTillSinkNode(null, devices);
				}
			}
		}
}
