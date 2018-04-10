package Test;

import static org.junit.Assert.*;

import org.junit.Test;

import graph.CreationGraph;
import graph.GraphReader;

public class GraphReaderTest {
	@Test
	public void graphReaderTest(){
		String graphPath = "../../Sample_Data/Test/GraphReader.csv";
		GraphReader reader = new GraphReader(graphPath);
		CreationGraph graph = reader.readGraph();
		
		assertEquals(graph.getNodeForId(5).getIncomingNodes().size(),1);
		assertEquals(graph.getNodeForId(0).getIncomingNodes().size(),0);
		assertEquals(graph.getNodeForId(14).getIncomingNodes().size(),9);
		assertEquals(graph.getNodeForId(12).getIncomingNodes().size(),4);
		assertEquals(graph.getNodeForId(7).getIncomingNodes().size(),2);
		assertEquals(graph.getNodeForId(8).getIncomingNodes().size(),2);
		
		assertEquals(graph.getNodeForId(6).getOutgoingNodes().size(),4);
		assertEquals(graph.getNodeForId(8).getOutgoingNodes().size(),1);
		assertEquals(graph.getNodeForId(7).getOutgoingNodes().size(),5);

		assertEquals(graph.getNodeForId(1).getOutgoingTensorSize(),629);
		assertEquals(graph.getNodeForId(12).getNumberOfOperations(),371);
		assertEquals(graph.getNodeForId(5).getRamToStore(),27);

		

	}	
}
