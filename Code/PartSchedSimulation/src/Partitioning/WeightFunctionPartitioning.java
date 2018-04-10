package Partitioning;

import java.util.HashMap;
import java.util.HashSet;

import Device.Device;
import graph.ColocationComponent;
import graph.ConstraintInterface;
import graph.CreationGraph;
import graph.Node;

public class WeightFunctionPartitioning extends PartitioningStrategy{
	private double maxSumCriticalPath = 0;

	
	public void partition(CreationGraph graph, HashMap<Integer, Device> devices) {

		for(Node sourceNode: graph.getSourceNodes()){
			if(sourceNode.getSumOpsTillSinkNodeOpsFromSourceNode() > maxSumCriticalPath){
				maxSumCriticalPath = (double) sourceNode.getSumOpsTillSinkNodeOpsFromSourceNode();
			}
		}
		
		// assign colocation components
		for (Integer componentId : graph.getColocationMap().keySet()) {
			ColocationComponent component = graph.getColocationMap().get(componentId);

			int deviceId = getDeviceIdToAssignComponentTo(component, devices);
			for (Node node : component.getNodes()) {
				assignNode(node, deviceId, devices, graph);
			}

		}
		// assign nodes (which are not a member of the colocation components)
		HashSet<Node> nodesNotColocated = new HashSet<Node>(graph.getNodes());
		for (Node node : nodesNotColocated) {
			int deviceId = getDeviceIdToAssignComponentTo(node, devices);
			assignNode(node, deviceId, devices, graph);
		}

	}

	public int getDeviceIdToAssignComponentTo(ConstraintInterface component,
			HashMap<Integer, Device> devices) {
		
		Device assignCandidate = null;
		double minValue = Double.MAX_VALUE;
		
 		double fastestFeasibleDeviceSpeed = 0;
		int numberOfDevices = devices.size();
		
		double maxTraffic = 0.0;
		double maxExecutionTime = 0.0;
		//should normally be between 0 and 1
		double minMemory = 1;
		
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
				if(minMemory > memory && memory != 0){
					minMemory = memory;
				}
			} 
		}
		
		//Ideas: //Verhaltnis CPU zu GPU devices und anteilmßig eher auf kleineres mappen
		
		double importance = this.calculateImportance(component);
		for(Device device: devices.values()) {
			if (device.canAddComponent(component)) {
				//how much free memory exists on that machine
				double memory =  ((double) device.getMemory() - device.getFreeMemoryEstimation()) / (double) device.getMemory();
				
				//Vertex weight -> aber nur wichitg, wenn hohe largest ops
				//num ops auf maschine/Geschwindigkeit * Priorität (basierend auf Pfadlänge) -> versuchen -> normen?
				//Verhältnis (1-node.sum / max(node.sum)
				//the more important a component is the higher the probability to map it on a fast device
				
				//double importanceSpeedFactor = 1- (importance * (1-((fastestFeasibleDeviceSpeed - device.getNumberOfOperations())/fastestFeasibleDeviceSpeed)));				
				//double importanceSpeedFactor = Math.abs(importance -  (device.getNumberOfOperations()/fastestFeasibleDeviceSpeed));
				double importanceSpeedFactor = 1 - (importance * (device.getNumberOfOperations())/fastestFeasibleDeviceSpeed);
				//ggf + 0.0000001  weglassen, weil dann Wert diese geräts 0 wird
				
				//time which device need to calculate everything
				double executionTime = 1.0;
				if(maxExecutionTime != 0.0){
					executionTime = getExecutionTimeForDevice(device, component)/maxExecutionTime;
					
					//nur zur sicherheit, sollte eigentlich gar nicht auftreten da numberOfOperations von einem Knoten nicht 0 sein sollten (außer bei send receive node und der wird nicht beim placement zuweisen)
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
				if(importanceSpeedFactor == 0.0){
					importanceSpeedFactor = 0.000001;
				}
				if(memory == 0.0){
					memory = minMemory/10.0d;
				}
				
				//System.out.println(additionalCommunicationTime + " " + executionTime + " " + memory + " " + importanceSpeedFactor);
				//double objective_value = this.weightAdditionalCommunicationTime * additionalCommunicationTime + this.weightExecutionTime*  executionTime + this.weightMemory * memory + this.weightImportanceSpeed * importanceSpeedFactor;
			   double objective_value =  additionalCommunicationTime * executionTime * importanceSpeedFactor * memory;
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

/*	private double additionalCommunicationCost(Device device, ConstraintInterface component, int numberOfDevices){
		double communicationTime = 0.0;
		if(component.getClass().equals(ColocationComponent.class)){
			for(Node node : ((ColocationComponent) component).getNodes()){
				communicationTime +=  additionalCommunicationTimeForNode(device, node, numberOfDevices);
			}
			
		}else{
			communicationTime = additionalCommunicationTimeForNode(device,((Node) component), numberOfDevices);
		}
		
		return communicationTime;
	}
	*/
	
	
	
/*private double additionalCommunicationTimeForNode(Device device, Node node, int numberOfDevices){

	boolean[] incomingNodes = new boolean[numberOfDevices];
	boolean[] outgoingNodes = new boolean[numberOfDevices];

	double additionalCommunicationTime = 0.0;
		
		//nicht beachtet: bundeling of send/receive nodes
		for(Node incoming: node.getIncomingNodes()){
			//ggf. add small punishment for nodes we don't know
			if(incoming.getDeviceId() != -1 && incoming.getDeviceId() != device.getId()){
				if(!incomingNodes[device.getId()]){
					additionalCommunicationTime += (double) incoming.getOutgoingTensorSize() / (double) device.getCommunicationCost()[incoming.getDeviceId()];
					incomingNodes[device.getId()] = true;
				}
			}
		}
		for(Node outgoing: node.getOutgoingNodes()){
			if(outgoing.getDeviceId() != -1 && outgoing.getDeviceId() != device.getId()){
				if(!outgoingNodes[device.getId()]){
				additionalCommunicationTime += (double) node.getOutgoingTensorSize() / (double) device.getCommunicationCost()[outgoing.getDeviceId()];
				outgoingNodes[device.getId()] = true;
				}
			}
		}
	
		return additionalCommunicationTime;
	}
*/
	//the closer the importance is to 1 the more important the component or node is
	private double calculateImportance(ConstraintInterface component){ 
		if(component.getClass().equals(ColocationComponent.class)){
			double sum = 0;
			for(Node node : ((ColocationComponent) component).getNodes()){
				sum += node.getSumOpsTillSinkNodeOpsFromSourceNode();
			}
			return sum/(this.maxSumCriticalPath * (double)((ColocationComponent) component).getNodes().size());
		}else{
			return (double)((Node) component).getSumOpsTillSinkNodeOpsFromSourceNode()/ (double) this.maxSumCriticalPath;
		}
	}
}


