package Partitioning;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import Device.Device;
import graph.ConstraintInterface;
import graph.CreationGraph;
import graph.Node;
import graph.Path;

public class AssignLongestPath extends PartitioningStrategy{
	//wie in DividePath2Partitioning rausgefunden sind die zweitlängsten Pfade oft extrem kurz -> but maybe another strategy leads to more pathes (divide path) -> however nodes are already assigned then 

	@Override
	public void partition(CreationGraph graph, HashMap<Integer, Device> devices) {
		
		List<Device> devicesSorted = new ArrayList<Device>(devices.values());
		Collections.sort(devicesSorted, new Comparator<Device>() {
			@Override
			public int compare(Device device1, Device device2) {
				return device2.getNumberOfOperations() - device1.getNumberOfOperations();
			}
		});
	/*	for(Device device: devicesSorted){
			System.out.println(device.getNumberOfOperations());
		}
*/
		Node criticalPathNode = null;
		for (Node node : graph.getSourceNodes()) {
			if(criticalPathNode == null || criticalPathNode.getUpwardsRank() < node.getUpwardsRank()){
				criticalPathNode = node;
			}
		}
		//assignLongestPath
		int numberOfNodesOnCriticalPath = 0;
		while(criticalPathNode != null){
			
			//could be already assigned due to colocation within critical path
			//check feasible devices and choose fastest -> map longest path, only change if constraints don't match anymore
			//danach minimieren nach #Ops/speed
			if(criticalPathNode.getDeviceId() == -1){
				if (criticalPathNode.getColocationId() != -1) {
					int deviceId = getDeviceIdToAssignComponentTo(graph.getColocationMap().get(criticalPathNode.getColocationId()),
							devicesSorted);
					for (Node colocationNode : graph.getColocationMap().get(criticalPathNode.getColocationId()).getNodes()) {
						assignNode(colocationNode, deviceId, devices, graph);
					}

				} else {
					int deviceId = getDeviceIdToAssignComponentTo(criticalPathNode, devicesSorted);
					assignNode(criticalPathNode, deviceId, devices, graph);
				}
			}
			criticalPathNode = criticalPathNode.getUpwardsRankSuccessorNode();
			numberOfNodesOnCriticalPath ++;
			//try to add very
		}
		// assign nodes (which are not a member of the critical pat)h
		HashSet<Node> nodesNodeOnCritcalPath = new HashSet<Node>(graph.getNodes());
		System.out.println("not on critial path (or colocated to critical path):" + nodesNodeOnCritcalPath.size());
		System.out.println("On critical path:" + numberOfNodesOnCriticalPath);
		
		for (Node node : nodesNodeOnCritcalPath) {
			if(node.getDeviceId() == -1){
				if (node.getColocationId() != -1) {
					int deviceId = getDeviceIdToAssignNonCriticalPathComponent(graph.getColocationMap().get(node.getColocationId()),
							devices);
					for (Node colocationNode : graph.getColocationMap().get(node.getColocationId()).getNodes()) {
						assignNode(colocationNode, deviceId, devices, graph);
					}

				} else {
					int deviceId = getDeviceIdToAssignNonCriticalPathComponent(node, devices);
					assignNode(node, deviceId, devices, graph);
				}
			}
		}
	}
	
	public int getDeviceIdToAssignComponentTo(ConstraintInterface component, List<Device> devicesSorted) {
		for (int i = 0; i< devicesSorted.size(); i++) {
			if (devicesSorted.get(i).canAddComponent(component)) {
				return devicesSorted.get(i).getId();
			} else {
				i++;
			}
		}
		System.err.println("Component with constraint: " + component.getDeviceConstraint() + ", memory size: "
				+ component.getTotalRamDemand() + " could not be matched to any device");
		return -1;
	}
	
	public int getDeviceIdToAssignNonCriticalPathComponent(ConstraintInterface component,
			HashMap<Integer, Device> devices) {
		Device assignCandidate = null;
		double minValue = Double.MAX_VALUE;
		
		for(Device device: devices.values()) {
			if (device.canAddComponent(component)) {
				//double executionTime = getExecutionTimeForDevice(device, component) but worse results???
				double executionTime = (device.getSumOfOperationsOfAlreadyAssignedNodes()/(double) device.getNumberOfOperations());		
				if(executionTime < minValue){
					assignCandidate = device;
					minValue = executionTime;
				}
				
			}
		}
		if(assignCandidate != null){
			return assignCandidate.getId();
		}
		System.err.println("Component with constraint: " + component.getDeviceConstraint() + ", memory size: "
				+ component.getTotalRamDemand() + " could not be matched to any device");
		return -1;
	}
	/*
	public int getDeviceIdToAssignNonCriticalPathComponent(ConstraintInterface component,
			HashMap<Integer, Device> devices) {
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
		
		//Ideas: //Verhaltnis CPU zu GPU devices und anteilmßig eher auf kleineres mappen
		
//		double importance = this.calculateImportance(component);
		for(Device device: devices.values()) {
			if (device.canAddComponent(component)) {
				//how much free memory exists on that machine
				//double memory =  ((double) device.getMemory() - device.getFreeMemoryEstimation()) / (double) device.getMemory();
				
				//Vertex weight -> aber nur wichitg, wenn hohe largest ops
				//num ops auf maschine/Geschwindigkeit * Priorität (basierend auf Pfadlänge) -> versuchen -> normen?
				//Verhältnis (1-node.sum / max(node.sum)
				//the more important a component is the higher the probability to map it on a fast device
				
				//double importanceSpeedFactor = 1- (importance * (1-((fastestFeasibleDeviceSpeed - device.getNumberOfOperations())/fastestFeasibleDeviceSpeed)));				
				//double importanceSpeedFactor = 1 - (importance * (device.getNumberOfOperations())/fastestFeasibleDeviceSpeed);
						
				//time which device need to calculate everything
				double executionTime = 1.0;
				if(maxExecutionTime != 0.0){
					executionTime = getExecutionTimeForDevice(device, component)/maxExecutionTime;
					if(executionTime == 0.0){
						executionTime = 0.000001;
					}
				}
				//AdditionalCommunicationCost: sollte man eher das maximum nehmen, bestrafen, wenn noch nicht zugeweisen (wenn man sicher weiß dass es keine zusätzlichen Kosten gibt, ist das besser als wenn Knoten einfach nicht zugewiesen ist).
				//Edge weight beachten
				//wie viele Nachbarn (successor/predecessor sind schon auf der Maschine)
				//Send/receive nodes berücksichtigen -> berücksichtigt mit Array

				//communication cost/transfer rate
				double additionalCommunicationTime = 1.0;
				if(maxTraffic != 0.0){
					additionalCommunicationTime =  (double) additionalTraffic(device, component, numberOfDevices)/maxTraffic;
					if(additionalCommunicationTime == 0.0){
						additionalCommunicationTime = 0.000001;
					}
				}

				//System.out.println(additionalCommunicationTime + " " + executionTime + " " + memory + " " + importanceSpeedFactor);
				//double objective_value = this.weightAdditionalCommunicationTime * additionalCommunicationTime + this.weightExecutionTime*  executionTime + this.weightMemory * memory + this.weightImportanceSpeed * importanceSpeedFactor;
			   double objective_value =  executionTime;//* importanceSpeedFactor * memory;
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
	*/
}
