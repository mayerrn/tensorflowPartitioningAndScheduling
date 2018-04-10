package Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.HashMap;

import org.junit.Test;

import Device.Device;
import Device.DeviceConstraint;
import Device.DeviceReader;
import evaluation.Evaluation;
import graph.ColocationComponent;
import graph.CreationGraph;
import graph.GraphReader;
import graph.Node;

public class GraphPartitioningTest {
	//RAM test -> if hashing creates valid partitioning (don't go below RAM requirements)
	//test canAddComponent -> in device, RAM and device constraint test
	@Test
	public void partitionerTest(){
		String graphPath = "../../Sample_Data/Test/GraphPartitioner.csv";
		GraphReader reader = new GraphReader(graphPath);
		CreationGraph graph = reader.readGraph();
		Evaluation evaluation = new Evaluation();
		DeviceReader deviceReader = new DeviceReader("../../Sample_Data/Test/GraphPartitioner_device.csv", evaluation);
		HashMap<Integer,Device> devices = deviceReader.readDevices();
		
		assertEquals(graph.getColocationMap().size(),3);

		//public Map<Integer, ColocationComponent> getColocationMap(){
		for(ColocationComponent component: graph.getColocationMap().values()){
			if(component.getNodes().size() == 11){
				//device is GPU, should not be able to map device with CPU requirement
				assertEquals(devices.get(3).canAddComponent(component), false);
				//device is CPU but not enough memory left
				assertEquals(devices.get(0).canAddComponent(component), false);
				assertEquals(devices.get(4).canAddComponent(component), true);
			}else if(component.getNodes().size() == 5){
				assertEquals(component.getDeviceConstraint(), DeviceConstraint.GPU);
				assertEquals(devices.get(3).canAddComponent(component), true);
			}else if(component.getNodes().size() == 2){
				assertEquals(component.getDeviceConstraint(), DeviceConstraint.NO);
				assertEquals(component.getTotalRamDemand(),3555);
				assertEquals(devices.get(0).canAddComponent(component), true);
				assertEquals(devices.get(3).canAddComponent(component), true);
				assertEquals(devices.get(4).canAddComponent(component), true);
				
				Node node12 = graph.getNodeForId(12);
				assertEquals(devices.get(0).getFreeMemory(), 4000);
				assertEquals(devices.get(0).canAddComponent(node12), true);
				assertEquals(node12.getTotalRamDemand(), 2239);
				devices.get(0).addNode(node12);
				assertEquals(devices.get(0).getFreeMemory(), (4000 - node12.getTotalRamDemand()));
				//assert to device 0 is now not possible anymore as node 12 was assigned
				assertEquals(devices.get(0).canAddComponent(component), false);
				for (Node node : component.getNodes()) {
					devices.get(4).addNode(node);
				}
				assertEquals(devices.get(4).getFreeMemory(), 40000 - component.getTotalRamDemand());


			}
			else{
				fail("Wrong number of nodes");
			}
	}


}

}
