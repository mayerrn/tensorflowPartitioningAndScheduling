package Test;


import org.junit.Test;

import evaluation.Evaluation;

import static org.junit.Assert.*;

import java.util.ArrayList;

import misc.MultipleGraphsCombinationsMain;

public class SameCombinations {
	@Test
	public void testIfSameCombinationLeadToSameResults() {
		assertTrue(false);
		//ToDo check if executionTime and communication costs are the same in every run
		ArrayList<Double> executionTime = new ArrayList<Double>();
		ArrayList<Integer> communicationCost = new ArrayList<Integer>();

		String[] graphPaths = { 
				"../../Sample_Data/TensorFlow sample code/recurrent_network.csv"
		};

		// combinations for partitioning and scheduling
		String[][] combinations = {
				{ "Hashing", "FifoScheduler" },
				 {"Hashing", "FifoScheduler"},
				 {"Hashing", "FifoScheduler"},
				 {"Hashing", "FifoScheduler"},
				 {"Hashing", "FifoScheduler"}
		};
	
		// test all given graphs
		graphLoop: for (int i = 0; i < graphPaths.length; i++) {
			// at these partitioning and scheduling combinations
			for (int j = 0; j < combinations.length; j++) {

				String graphName = graphPaths[i];
				String partitioningName = combinations[j][0];
				String schedulingName = combinations[j][1];
				Evaluation eval = MultipleGraphsCombinationsMain.execute(graphName, partitioningName, schedulingName, "test",
						"../../Sample_Data/Device/Scheduling_Test_device.csv", true, "../../Visualisation/results/");
				assertTrue(eval != null);
				
			}
		}

	}
}
