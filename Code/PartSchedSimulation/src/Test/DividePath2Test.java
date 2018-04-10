package Test;

import java.util.HashMap;

import org.junit.Test;

import Device.Device;
import Device.DeviceReader;
import Partitioning.DividePath2Partitioning;
import Partitioning.PartitioningPreprocessor;
import Partitioning.PartitioningStrategy;
import evaluation.Evaluation;
import graph.CreationGraph;
import graph.GraphReader;

public class DividePath2Test {
	@Test
	public void readDeviceTest(){

	//String graphPath = "../../Test/LargestRemainedOperationFirstScheduler_Test_graph.csv";
		String graphPath =  "../../Sample_Data/TensorFlow sample code/dynamic_rnn_graph.csv";
	Evaluation evaluation = new Evaluation();
	GraphReader reader = new GraphReader(graphPath);

	CreationGraph graph = reader.readGraph();
	
	
	int numberOfNodes = graph.getNodes().size();
	
	//../../Test/Scheduling_Test_device.csv"
	DeviceReader deviceReader = new DeviceReader("../../Sample_Data/Device/Scheduling_Test_device.csv", evaluation);
	HashMap<Integer, Device> devices = deviceReader.readDevices();

	PartitioningStrategy partitioningStrategy = PartitioningStrategy.strategyForString("DividePath2Partitioning");

	PartitioningPreprocessor preprocessor = new PartitioningPreprocessor(graph,partitioningStrategy); 
	preprocessor.preprocess(devices);
	partitioningStrategy.partition(graph, devices);
	
	
	//partitioningStrategy.partition(graph, devices);
	//System.out.println("test");

/*
	if (graph.getNodes().size() != 0) {
		//System.err.println("Not all nodes partitioned, " + partitioningName + ", " + schedulingName + "\n number of not assigned nodes" + graph.getNodes().size() + " out of " + numberOfNodes );
		// exit and don't save results
		//return false; 
	}
	*/
	}
}
