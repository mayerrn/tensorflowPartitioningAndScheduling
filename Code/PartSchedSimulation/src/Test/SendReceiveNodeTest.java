package Test;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.HashSet;

import org.junit.Test;

import Device.Device;
import Device.DeviceReader;
import Partitioning.Hashing;
import Partitioning.PartitioningPostProcessor;
import evaluation.Evaluation;
import graph.CreationGraph;
import graph.GraphReader;
import graph.Node;
import graph.ReceiveNode;
import graph.SendNode;
import graph.TransferNode;
import misc.Util;
import scheduler.Scheduler;

public class SendReceiveNodeTest {

	@Test
	public void sendReceiveNodes() {
		String graphPath = "../../Sample_Data/Test/SendReceiveNodes_graph.csv";

		GraphReader reader = new GraphReader(graphPath);
		CreationGraph graph = reader.readGraph();
		
		Evaluation evaluation = new Evaluation();
		DeviceReader deviceReader = new DeviceReader("../../Sample_Data/Test/SendReceiveNodes_device.csv",evaluation);
		HashMap<Integer,Device> devices = deviceReader.readDevices();
		
		Hashing hash = new Hashing();
		//hash.partition(graph, devices);
		HashSet<Node> nodes = new HashSet<Node>(graph.getNodes());
		
		for (Node node : nodes) {
			if(node.getId() == 0 || node.getId() == 5){
				hash.assignNode(node, 0, devices, graph);
			}else if(node.getId() == 1 || node.getId() == 6){
				hash.assignNode(node, 1, devices, graph);
			}else if(node.getId() == 2 || node.getId() == 7){
				hash.assignNode(node, 2, devices, graph);
			}else if(node.getId() == 3 || node.getId() == 8){
				hash.assignNode(node, 3, devices, graph);
			}else if(node.getId() == 4 || node.getId() == 9){
				hash.assignNode(node, 4, devices, graph);
			}
		}
		
		//System.out.println("test");
		Util.printGraphVisualisation(devices);
		
		Scheduler scheduler = Scheduler.strategyForString("FifoScheduler");
		PartitioningPostProcessor postProcessor = new PartitioningPostProcessor(graph, devices,scheduler);
		postProcessor.postProcess();
		Util.printGraphVisualisation(devices);

		Device device0 = devices.get(0);
		assertEquals(device0.getGraph().numberOfNodes(),3);
		Node node0 = device0.getGraph().getNodeForId(0);
		assertEquals(node0.getOutgoingNodes().size(),2);
		Node sendNode = null;
		
		for(Node node: node0.getOutgoingNodes()){
			if(node.getId() != 5){
				sendNode = node;
				assertTrue(sendNode.getClass().equals((SendNode.class)));
			}
		}
		assertEquals(sendNode.getIncomingNodes().size(),1);	
		for(Node node: sendNode.getIncomingNodes()){
			assertEquals(node.getId(),0);
		}
		
		assertEquals(sendNode.getOutgoingNodes().size(),1);	
		Node receiveNode = null;
		for(Node node: sendNode.getOutgoingNodes()){
			receiveNode = node;
		}
		assertTrue(receiveNode.getClass().equals((ReceiveNode.class)));

		assertEquals(receiveNode.getIncomingNodes().size(),1);	
		for(Node node: receiveNode.getIncomingNodes()){
			assertEquals(node.getId(),sendNode.getId());
		}
		
		assertEquals(receiveNode.getOutgoingNodes().size(),2);	
		for(Node node: receiveNode.getOutgoingNodes()){
			assertTrue((node.getId() == 1 || node.getId() == 6));
			if(node.getId() == 1){
				assertEquals(node.getIncomingNodes().size(),1);
				for(Node nodeIncoming: node.getIncomingNodes()){
					assertEquals(nodeIncoming.getId(),receiveNode.getId());
				}
			}else{
				assertEquals(node.getIncomingNodes().size(),2);
				for(Node nodeIncoming: node.getIncomingNodes()){
					assertTrue(nodeIncoming.getId() == receiveNode.getId() || nodeIncoming.getId() == 1);
				}
			}
		}	
		
		Device device1 = devices.get(1);
		assertEquals(device1.getGraph().numberOfNodes(),3);
		Device device2 = devices.get(2);
		assertEquals(device2.getGraph().numberOfNodes(),3);
		Device device3 = devices.get(3);
		assertEquals(device3.getGraph().numberOfNodes(),4);
		Device device4 = devices.get(4);
		assertEquals(device4.getGraph().numberOfNodes(),3);

	}

}