package Test;

import graph.ColocationComponent;
import graph.CreationGraph;
import graph.GraphReader;
import graph.Node;

import static org.junit.Assert.*;
import org.junit.Test;

import Device.DeviceConstraint;


public class ColocationTests {
	@Test
		public void colocationTest(){
			String graphPath = "../../Sample_Data/Test/ColocationTest.csv";
			GraphReader reader = new GraphReader(graphPath);
			CreationGraph graph = reader.readGraph();
			assertEquals(graph.getColocationMap().size(),3);
			assertEquals(graph.getNodeForId(7).getTotalRamDemand(), 1531);

			//public Map<Integer, ColocationComponent> getColocationMap(){
			for(ColocationComponent component: graph.getColocationMap().values()){
				if(component.getNodes().size() == 11){
					assertEquals(component.getDeviceConstraint(), DeviceConstraint.CPU);
				}else if(component.getNodes().size() == 5){
					assertEquals(component.getDeviceConstraint(), DeviceConstraint.GPU);
				}else if(component.getNodes().size() == 2){
					assertEquals(component.getDeviceConstraint(), DeviceConstraint.NO);
					assertEquals(component.getTotalRamDemand(),3555);
				}
				else{
					fail("Wrong number of nodes");
				}
			}
		}

}
