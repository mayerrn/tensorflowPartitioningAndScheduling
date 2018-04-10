package Test;

import java.util.HashMap;
import java.util.HashSet;

import org.junit.Test;
import static org.junit.Assert.*;

import Device.Device;
import Device.DeviceReader;
import Partitioning.Hashing;
import Partitioning.PartitioningPostProcessor;
import deviceScheduler.DeviceScheduler;
import evaluation.Evaluation;
import graph.CreationGraph;
import graph.GraphReader;
import graph.Node;
import scheduler.LargestRemainedOperationFirstScheduler;
import scheduler.Scheduler;

//Idee eigentlich koennte man die Kommunikationskosten auch mitberechnen
public class LargestRemainedOperationFirstSchedulerTest {
	@Test
	public void testRemainedOperationsExecution() {
		String graphPath = "../../Sample_Data/Test/LargestRemainedOperationFirstScheduler_Test_graph.csv";
		GraphReader reader = new GraphReader(graphPath);
		CreationGraph graph = reader.readGraph();
		
		Evaluation evaluation = new Evaluation();
		DeviceReader deviceReader = new DeviceReader("../../Sample_Data/Test/Scheduling_Test_device.csv",evaluation);
		HashMap<Integer,Device> devices = deviceReader.readDevices();
		
		Hashing hash = new Hashing();
		//hash.partition(graph, devices);
		//partition manually as hasing depends on incoming order
		HashSet<Node> nodes = new HashSet<Node>(graph.getNodes());
		for (Node node : nodes) {
			if(node.getId() == 1 || node.getId() == 4 || node.getId() == 7){
				hash.assignNode(node, 0, devices, graph);
			}else if(node.getId() == 2 || node.getId() == 5){
				hash.assignNode(node, 1, devices, graph);
			}else{
				hash.assignNode(node, 2, devices, graph);
			}
		}
		
		Scheduler scheduler = Scheduler.strategyForString("LargestRemainedOperationFirstScheduler");
		PartitioningPostProcessor postProcessor = new PartitioningPostProcessor(graph, devices,scheduler);
		postProcessor.postProcess();
		
		assertEquals(graph.getSinkNodes().size(), 3);
		Device device1 = devices.get(1);
		
		System.out.println("Device 1");
		for(Node node: device1.getGraph().getNodes()){
			System.out.println(node.getId());
		}
		
		Device device2 = devices.get(2);
		
		System.out.println("Device 2");
		for(Node node: device2.getGraph().getNodes()){
			System.out.println(node.getId());
		}
		
		System.out.println("Device 0");
		Device device0 = devices.get(0);
		for(Node node: device0.getGraph().getNodes()){
			System.out.println(node.getId());
		}
		
		assertEquals(device1.getGraph().getNodeForId(5).getRemainedOperationsTillSinkNode(),80);
		assertEquals(device1.getGraph().getNodeForId(2).getRemainedOperationsTillSinkNode(),40);
		assertEquals(device1.getGraph().getNodeForId(11).getRemainedOperationsTillSinkNode(),80 + 10);

		

		assertEquals(device2.getGraph().getNodeForId(6).getRemainedOperationsTillSinkNode(),20);
		assertEquals(device2.getGraph().getNodeForId(9).getRemainedOperationsTillSinkNode(),20 + 10);
		assertEquals(device2.getGraph().getNodeForId(12).getRemainedOperationsTillSinkNode(),100);
		assertEquals(device2.getGraph().getNodeForId(3).getRemainedOperationsTillSinkNode(),140);
		
		

		assertEquals(device0.getGraph().getNodeForId(8).getRemainedOperationsTillSinkNode(),30 + 10);
		assertEquals(device0.getGraph().getNodeForId(10).getRemainedOperationsTillSinkNode(),100);
		assertEquals(device0.getGraph().getNodeForId(4).getRemainedOperationsTillSinkNode(),80);
		assertEquals(device0.getGraph().getNodeForId(7).getRemainedOperationsTillSinkNode(),180);
		assertEquals(device0.getGraph().getNodeForId(1).getRemainedOperationsTillSinkNode(),340);
		assertEquals(device0.getGraph().getNodeForId(13).getRemainedOperationsTillSinkNode(),90);
		
		DeviceScheduler deviceScheduler = new DeviceScheduler(devices.values(), evaluation, scheduler);
		deviceScheduler.execute();
	}
		
}
