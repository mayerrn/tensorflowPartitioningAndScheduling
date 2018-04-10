package Test;

import static org.junit.Assert.*;

import java.util.HashMap;

import org.junit.Test;

import Device.Device;
import Device.DeviceReader;
import Partitioning.PartitioningPreprocessor;
import Partitioning.PartitioningStrategy;
import evaluation.Evaluation;
import graph.CreationGraph;
import graph.GraphReader;

public class DividPathTest {
	@Test
	public void readDeviceTest(){

	//String graphPath = "../../Test/LargestRemainedOperationFirstScheduler_Test_graph.csv";
		String graphPath =  "../../Sample_Data/TensorFlow sample code/dynamic_rnn_graph.csv";
		/*{ "../../TensorFlow sample code/convolutional_network_graph.csv",
			"../../TensorFlow sample code/recurrent_network.csv",
			 "../../TensorFlow sample code/dynamic_rnn_graph.csv"
			 */
	Evaluation evaluation = new Evaluation();
	GraphReader reader = new GraphReader(graphPath);

	CreationGraph graph = reader.readGraph();
	
	
	int numberOfNodes = graph.getNodes().size();
	
	//../../Test/Scheduling_Test_device.csv"
	DeviceReader deviceReader = new DeviceReader("../../Sample_Data/Device/Scheduling_Test_device.csv", evaluation);
	HashMap<Integer, Device> devices = deviceReader.readDevices();

	PartitioningStrategy partitioningStrategy = PartitioningStrategy.strategyForString("DividePathPartitioning");

	PartitioningPreprocessor preprocessor = new PartitioningPreprocessor(graph,partitioningStrategy); 
	preprocessor.preprocess(devices);
	partitioningStrategy.partition(graph, devices);
	assertTrue(graph.getNodes().size() == 0);

	}
}

