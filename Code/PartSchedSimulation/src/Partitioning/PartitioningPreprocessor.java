package Partitioning;


import java.util.HashMap;

import Device.Device;
import graph.CreationGraph;
import graph.Node;


//TODO implement Graph Preprocessing in Creation graph if the given value can be computed during addEntry- steps
public class PartitioningPreprocessor {
	
	private CreationGraph graph;
	private PartitioningStrategy strategy;
	public PartitioningPreprocessor(CreationGraph graph, PartitioningStrategy strategy){
		this.graph = graph;
		this.strategy = strategy;
	}
	
	public void preprocess(HashMap<Integer, Device> devices){
		if(strategy.getClass().equals(DivideSuccessorNodesPartitioning.class)){
			for(Node node: graph.getSinkNodes()){
				node.calculateRemainedOperationsTillSinkNodeWithoutSendReceive(null);
			}
		}

		if(strategy.getClass().equals(HeftPartitioning.class) || strategy.getClass().equals(AssignLongestPath.class)){
			double averageTransferSpeed = 0;
			double averageComputationSpeed = 0;
			for(Device device: devices.values()){
				averageComputationSpeed += (double) device.getNumberOfOperations();
				if(averageTransferSpeed == 0){
					for(int i = 0; i < device.getCommunicationCost().length; i++){
						averageTransferSpeed += device.getCommunicationCost()[i];
					}
				}
			}
			averageComputationSpeed = ((double) averageComputationSpeed)/devices.size();
			averageTransferSpeed = averageTransferSpeed/devices.size();
			for(Node node: graph.getSinkNodes()){
				node.calculateUpwardsRank(null,averageTransferSpeed,averageComputationSpeed);
			}			
		}
		
		if(strategy.getClass().equals(DividePath2Partitioning.class) || strategy.getClass().equals(RangeBasedImportancePartitioning.class) || strategy.getClass().equals(WeightFunctionPartitioning.class) || strategy.getClass().equals(DFSPartitioning.class)){
			for(Node node: graph.getSourceNodes()){
				node.calculateOpsTillNodeWithoutSendReceive(null, false);
			}
			for(Node node: graph.getSinkNodes()){
				node.calculateRemainedOperationsTillSinkNodeWithoutSendReceive(null);
			}
		}
	}
}
