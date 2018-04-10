package Partitioning;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import Device.Device;
import graph.ConstraintInterface;
import graph.CreationGraph;
import graph.Node;

public class DFSPartitioning extends PartitioningStrategy {
	@Override
	public void partition(CreationGraph graph, HashMap<Integer, Device> devices) {
		//sort source nodes based on importance
		
		List<Node> sourceNodes = new ArrayList<Node>(graph.getSourceNodes());
		// Sorting
		Collections.sort(sourceNodes, new Comparator<Node>() {
			@Override
			public int compare(Node node1, Node node2) {
				return node2.getSumOpsTillSinkNodeOpsFromSourceNode() - node1.getSumOpsTillSinkNodeOpsFromSourceNode();
			}
		});
		
		for(Node node:sourceNodes){
			// new path start here
			this.dfs(graph, node, devices);
		}
	}

	public void dfs(CreationGraph graph, Node node, HashMap<Integer, Device> devices) {
		node.setDfsAlreadyVisited(true);
		
		int id = 1;
		if (node.getDeviceId() == -1) {
			if (node.getColocationId() != -1) {
				id = getDeviceIdToAssignComponentTo(graph.getColocationMap().get(node.getColocationId()),
						devices);
				for (Node colocationNode : graph.getColocationMap().get(node.getColocationId()).getNodes()) {
					assignNode(colocationNode, id, devices, graph);
				}

			} else {
				id = getDeviceIdToAssignComponentTo(node, devices);
				assignNode(node, id, devices, graph);
			}
		}
		for (Node nodeOutgoing : node.getOutgoingNodes()) {
			if (!nodeOutgoing.isDfsAlreadyVisited()) {
				// hier frangen neue ofade an
				// hier heuristic von Pfad wechseln und auf wleches Device...
				// vllt mit largestOperationFirst
				// ggf Pfadwechsel wenn Knoten sehr wichtig, und Gerät langsam,
				// wenn Transferzeit, TensorSize klein
			/*	if (Math.random() < 0.1) {
					dfs(graph, nodeOutgoing, getDeviceWithMinimumComputationTime(devices).getId(), devices);
					//additionalTrafficForNode
				} else {
					dfs(graph, nodeOutgoing, id, devices);
				}
				*/
				dfs(graph, nodeOutgoing, devices);
			}
		}

	}
	
	public int getDeviceIdToAssignComponentTo(ConstraintInterface component,
			HashMap<Integer, Device> devices) {
		
		int averageNumberOfOperations = 0;
		for(Device device: devices.values()){
			averageNumberOfOperations += device.getNumberOfOperations();
		}
		averageNumberOfOperations = averageNumberOfOperations/devices.size();
		//importanceTransfer = averageNumberOfOperations;

		
		Device assignCandidate = null;
		double minValue = Double.MAX_VALUE;
		
 		double fastestFeasibleDeviceSpeed = 0;
		int numberOfDevices = devices.size();
		
		double maxTraffic = 0.0;
		double maxExecutionTime = 0.0;
		//should normally be between 0 and 1
		double minMemory = 2;
		
		for(Device device: devices.values()) {
			if (device.canAddComponent(component)) {
				
				double currentExecutionTime = getExecutionTimeForDevice(device, component);
				double currentTraffic = additionalTraffic(device, component, numberOfDevices);
				
				if(maxTraffic < currentTraffic){
					maxTraffic = currentTraffic;
				}
				if(maxExecutionTime < currentExecutionTime){
					maxExecutionTime = currentExecutionTime;
				}
				
				if(fastestFeasibleDeviceSpeed < device.getNumberOfOperations() ){
					fastestFeasibleDeviceSpeed = (double) device.getNumberOfOperations();
				}
				double memory =  ((double) device.getMemory() - device.getFreeMemoryEstimation()) / (double) device.getMemory();
				if(minMemory > memory){
					minMemory = memory;
				}
			} 
		}
		
		
		for(Device device: devices.values()) {
			if (device.canAddComponent(component)) {
				//how much free memory exists on that machine
				double memory =  ((double) device.getMemory() - device.getFreeMemoryEstimation()) / (double) device.getMemory();
					
				//time which device need to calculate everything
				double executionTime = 1.0;
				if(maxExecutionTime != 0.0){
					executionTime = getExecutionTimeForDevice(device, component)/maxExecutionTime;
					if(executionTime == 0.0){
						executionTime = 0.000001;
					}
				}

				double additionalCommunicationTime = 1.0;
				if(maxTraffic != 0.0){
					additionalCommunicationTime =  (double) additionalTraffic(device, component, numberOfDevices)/maxTraffic;
					if(additionalCommunicationTime == 0.0){
						additionalCommunicationTime = 0.000001;
					}
				}
				if(memory == 0.0){
					memory = minMemory/10.0d;
				}
				
			   double objective_value =  executionTime * additionalCommunicationTime; //* memory;
			   //System.out.println(additionalCommunicationTime + " " + executionTime + " " + importanceSpeedFactor + " " + objective_value);
				if(objective_value < minValue){
					assignCandidate = device;
					minValue = objective_value;
				}
				
			} else {
				
			}
		}
		if(assignCandidate != null){
		return assignCandidate.getId();
		}
		System.err.println("Component with constraint: " + component.getDeviceConstraint() + ", memory size: "
				+ component.getTotalRamDemand() + " could not be matched to any device");
		return -1;
	}
}
