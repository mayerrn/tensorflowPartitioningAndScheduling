package Partitioning;

import java.util.HashMap;

import basic_ILS_Partitioning.Basic_ILS;
import optimalPartitioning.OptimalPartitioning;
import Device.Device;
import graph.ColocationComponent;
import graph.ConstraintInterface;
import graph.CreationGraph;
import graph.Node;

public abstract class PartitioningStrategy {
	
	public abstract void partition(CreationGraph graph, HashMap<Integer, Device> devices);

	protected static void addSendReceiveNodes(HashMap<Integer, Device> devices, int id) {
		// possible as count begins with 0 (do not use -1)
		int minId = id;
		for (Device device : devices.values()) {
			minId = device.addSendReceiveNodes(minId);
		}		
	}

	public void assignNode(Node node, int deviceId, HashMap<Integer, Device> devices, CreationGraph graph) {
		Device device = devices.get(deviceId);
		if (device != null) {
			node.setDeviceId(deviceId);

			for (Node incoming : node.getIncomingNodes()) {
				if (incoming.isAssigned()) {
					if (incoming.getDeviceId() != node.getDeviceId()) {
						Device deviceIncoming = devices.get(incoming.getDeviceId());
						deviceIncoming.addOutgoingNodeIdOfCrossDeviceEdge(incoming.getId());
					}
				}
			}
			for (Node outgoing : node.getOutgoingNodes()) {
				if (outgoing.isAssigned()) {
					if (outgoing.getDeviceId() != node.getDeviceId()) {
						device.addOutgoingNodeIdOfCrossDeviceEdge(node.getId());
					}
				}
			}
			device.addNode(node);
			graph.removeNode(node);
		} else {
			System.err.println("Device not found: " + deviceId);
		}
	}
	
	public static PartitioningStrategy strategyForString(String strategyName){
		if(strategyName.equals("Hashing")){
			return new Hashing();
		}
		if(strategyName.equals("DivideSuccessorNodesPartitioning")){
			return new DivideSuccessorNodesPartitioning();
		}
		if(strategyName.equals("WeightFunctionPartitioning")){
			return new WeightFunctionPartitioning();
		}
		if(strategyName.equals("DFSPartitioning")){
			return new DFSPartitioning();
		}
		if(strategyName.equals("DividePath2Partitioning")){
			return new DividePath2Partitioning();
		}
		if(strategyName.equals("DividePathPartitioning")){
			return new DividePathPartitioning();
		}
		if(strategyName.equals("RangeBasedImportancePartitioning")){
			return new RangeBasedImportancePartitioning();
		}
		if(strategyName.equals("HeftPartitioning")){
			return new HeftPartitioning();
		}
		if(strategyName.equals("AssignLongestPath")){
			return new AssignLongestPath();
		}
		if (strategyName.equals("OptimalPartitioning")) {
			return new OptimalPartitioning();
		}
		if (strategyName.equals("Basic_ILS")) {
			return new Basic_ILS();
		}
//		if (strategyName.equals("OptimalPartitioningCrazyHeuristic")) {
//			return new OptimalPartitioningCrazyHeuristic();
//		}
		return null;
	}
	
	public static String abbrevationForPartitioningName(String strategyName){
		if(strategyName.equals("Hashing")){
			return "Hash";
		}
		if(strategyName.equals("DivideSuccessorNodesPartitioning")){
			return "DivideSuccessors";
		}
		if(strategyName.equals("WeightFunctionPartitioning")){
			return "MITE";
		}
		if(strategyName.equals("DFSPartitioning")){
			return "DFS";
		}
		if(strategyName.equals("DividePath2Partitioning")){
			return "DividePath2";
		}
		if(strategyName.equals("DividePathPartitioning")){
			return "ICP";
		}
		if(strategyName.equals("RangeBasedImportancePartitioning")){
			return "BatchSplit";
		}
		if(strategyName.equals("HeftPartitioning")){
			return "HEFT";
		}
		if(strategyName.equals("AssignLongestPath")){
			return "CP";
		}
		if (strategyName.equals("OptimalPartitioning")) {
			return "OPTI";
		}
		if (strategyName.equals("Basic_ILS")) {
			return "BILS";
		}
//		if (strategyName.equals("OptimalPartitioningCrazyHeuristic")) {
//			return "OPTIC_H";
//		}
		return null;
	}
	
	protected static double additionalTraffic(Device device, ConstraintInterface component, int numberOfDevices){
		int communicationTime = 0;
		if(component.getClass().equals(ColocationComponent.class)){
			for(Node node : ((ColocationComponent) component).getNodes()){
				communicationTime +=  additionalTrafficForNode(device, node, numberOfDevices);
			}
			
		}else{
			communicationTime = additionalTrafficForNode(device,((Node) component), numberOfDevices);
		}
		
		return communicationTime;
	}
	
	//Wichtigkeit von den transferKosten
	//average transferSpeed/average numberOfOperations/ , ggf auch max transferSpeed durch max nubmerofOperations
	//transfer_rate/max_transferRate (Element 0,1) * importance
	//multiplizieren mit was man hinzufügt -> wird klein 
	
	//macht das überhaupt einen unterschied -> wird überall dazu multipliziert??
	public static double importanceTransfer = 1;
	
	//ggf noch Faktor wie bei Gewichtung, wie hoch transfer costs zu operation costs
	protected static int additionalTrafficForNode(Device device, Node node, int numberOfDevices){

		boolean[] outgoingNodes = new boolean[numberOfDevices];

		int additionalTraffic = 0;
			
			for(Node incoming: node.getIncomingNodes()){
				//ggf. add small punishment for nodes we don't know
				if(incoming.getDeviceId() != -1 && incoming.getDeviceId() != device.getId()){
					if(!hasPredNodeAlreadyEdgeToThisDevice(incoming , device.getId())){
						additionalTraffic +=  (int)((double)incoming.getOutgoingTensorSize() * (double) importanceTransfer / (double) device.getCommunicationCost()[incoming.getDeviceId()]);
					}
				}
			}
			
			for(Node outgoing: node.getOutgoingNodes()){
				if(outgoing.getDeviceId() != -1 && outgoing.getDeviceId() != device.getId()){
					if(!outgoingNodes[device.getId()]){
						//importanceTransfer könnte rausgezogen werden ud ist nachher im produkt -> keine Bedeutung
						additionalTraffic += (int) ((double)node.getOutgoingTensorSize() * (double) importanceTransfer) /(double) device.getCommunicationCost()[outgoing.getDeviceId()];
					outgoingNodes[device.getId()] = true;
					}
				}
			}
		
			return additionalTraffic;
		}
	//könnte man auch smarter machen: boolean array in node
	private static boolean hasPredNodeAlreadyEdgeToThisDevice(Node node , int deviceId){
		for(Node successorNode: node.getOutgoingNodes()){
			if(successorNode.getId() == deviceId){
				return true;
			}
		}
		return false;
	}
	protected  static  double getExecutionTimeForDevice(Device device, ConstraintInterface component){
		return  (double) (device.getSumOfOperationsOfAlreadyAssignedNodes() + component.getNumberOfOperations())/(double) device.getNumberOfOperations();
	
	}
}
