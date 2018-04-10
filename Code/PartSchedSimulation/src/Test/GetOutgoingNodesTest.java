package Test;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import org.junit.Test;

import Device.Device;
import Device.DeviceReader;
import Partitioning.PartitioningPostProcessor;
import Partitioning.PartitioningStrategy;
import evaluation.Evaluation;
import graph.CreationGraph;
import graph.GraphReader;
import graph.Node;
import scheduler.Scheduler;

public class GetOutgoingNodesTest {
	@Test
	public void checkIfAllNodesAreReachable(){
		Evaluation evaluation = new Evaluation();
		GraphReader reader = new GraphReader("../../Sample_Data/TensorFlow sample code/convolutional_network_graph.csv");
		
		CreationGraph graph = reader.readGraph();
		int numberOfNodes = graph.getNodes().size();
		//graph.printStatistics();

		//("../../Sample_Data/Device/sample2016_11_06_13_18_22.csv");
		DeviceReader deviceReader = new DeviceReader("../../Sample_Data/Device/Scheduling_Test_device.csv", evaluation);
		HashMap<Integer, Device> devices = deviceReader.readDevices();

		/*  
		 * for(Device device: devices.values()){
		 * System.out.println(device.toString()); }
		 */
		int graphSize = graph.getNodes().size();
		Queue<Node> nodesQueue = new LinkedList<Node>();
		nodesQueue.addAll(graph.getSourceNodes());
		int size = 0;
		
		while(!nodesQueue.isEmpty()){
	/*		Node node = nodesQueue.poll();
			//System.out.println(node.getOutgoingNodes().size());
			//System.out.println(node.getIncomingNodes().size());
			//aber so mehr Knoten in der Queue als beabsichtigt, weil sie bei outgoing nodes hinzugefügt werden
			if(node.getDeviceId() == -1){
				for(Node outgoingNode: node.getOutgoingNodes()){
					if(outgoingNode.getDeviceId() == -1){
						nodesQueue.add(outgoingNode);
					}
				}
				size++;
				node.setDeviceId(1);
			}
		
		System.out.println(size + "graphSize" + graphSize);
		*/
			Node node = nodesQueue.poll();
			//System.out.println(node.getOutgoingNodes().size());
			//System.out.println(node.getIncomingNodes().size());
				for(Node outgoingNode: node.getOutgoingNodes()){
					//2 bedeuted schon in der queue hinzugefügt
					if(outgoingNode.getDeviceId() != 2){
						outgoingNode.setDeviceId(2);
						nodesQueue.add(outgoingNode);
					}				
			}
			size++;
		
		}
		
		assertEquals(size, graphSize);

	/*	PartitioningStrategy partitioningStrategy = PartitioningStrategy.strategyForString("DividePathPartitioning");
		partitioningStrategy.partition(graph, devices);

		Scheduler scheduler = Scheduler.strategyForString("FifoScheduler");

		PartitioningPostProcessor postProcessor = new PartitioningPostProcessor(graph, devices,scheduler);
		postProcessor.postProcess();
*/
		
			
	}
	/*
	public static void reachableNodes(HashSet<Node> nodes){
		Queue<Node> nodesQueue = new LinkedList<Node>();
		nodesQueue.addAll(nodes);
		int size = 0;
		
		while(!nodesQueue.isEmpty()){
		Node node = nodesQueue.poll();
		//System.out.println(node.getOutgoingNodes().size());
		//System.out.println(node.getIncomingNodes().size());
			for(Node outgoingNode: node.getOutgoingNodes()){
				//2 bedeuted schon in der queue hinzugefügt
				if(outgoingNode.toDelete != 3){
					outgoingNode.toDelete = 3;
					nodesQueue.add(outgoingNode);
				}				
		}
			size++;
	
	}	
	System.out.println(size + "graphSize");
	}
	*/
}
