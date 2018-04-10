package misc;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;


public class Config {

	
	// combinations for partitioning and scheduling
	public static String[][] combinations = {

			//{"AssignLongestPath", "LargestRemainedOperationFirstScheduler"},
			{"AssignLongestPath", "FifoScheduler"},
//			{"AssignLongestPath", "HighDegreeVerticesFirstScheduler"},
//			{"AssignLongestPath", "LargestRemainedTimeToFinishFirst"},

			//{ "Hashing", "LargestRemainedOperationFirstScheduler" }, 
//			{ "Hashing", "FifoScheduler" },
//			{"Hashing", "LargestRemainedTimeToFinishFirst"},
//			{"Hashing", "HighDegreeVerticesFirstScheduler"},

			//{ "RangeBasedImportancePartitioning", "LargestRemainedOperationFirstScheduler" },
//			{ "RangeBasedImportancePartitioning", "FifoScheduler" },
//			{ "RangeBasedImportancePartitioning", "HighDegreeVerticesFirstScheduler"},
//			{"RangeBasedImportancePartitioning", "LargestRemainedTimeToFinishFirst"},

			
//			{ "DivideSuccessorNodesPartitioning", "FifoScheduler"},
		    //{ "DivideSuccessorNodesPartitioning", "LargestRemainedOperationFirstScheduler"},
		    //{ "DividePathPartitioning", "HighDegreeVerticesFirstScheduler" },
		    //{"DividePathPartitioning", "LargestRemainedTimeToFinishFirst"},
			
			{ "WeightFunctionPartitioning", "FifoScheduler" },
//			{"WeightFunctionPartitioning", "HighDegreeVerticesFirstScheduler"},
//			{"WeightFunctionPartitioning", "LargestRemainedTimeToFinishFirst"},

//			{"DFSPartitioning", "FifoScheduler"},
//			{ "DFSPartitioning", "HighDegreeVerticesFirstScheduler"},
//			{"DFSPartitioning", "LargestRemainedTimeToFinishFirst"},
			//HeftPartitioning sollte nur moeglich sein mit HeftScheduler
//			{"HeftPartitioning", "HeftScheduler"},
			
//			{"OptimalPartitioning", "FifoScheduler"},
//			{"OptimalPartitioning", "LargestRemainedTimeToFinishFirst"},
//			{"OptimalPartitioningCrazyHeuristic", "FifoScheduler"},
			// {"DividePath2Partitioning", "FifoScheduler"},
			// {"DividePath2Partitioning", "LargestRemainedOperationFirstScheduler"},
			
//			{"Basic_ILS", "FifoScheduler"},
//			{"Basic_ILS", "LargestRemainedTimeToFinishFirst"},
			
	};
	
	//maybe also add many devices
	public static String deviceFile = "../../Sample_Data/Device/large_heterogenous_devices.csv";


	public static String logPath = "output.txt";
	public static boolean printOutOnConsole = true;
	public static boolean printErrorOnConsole = true;	
	public static String ARRAY_SEPERATOR = ";";
	public static String CATEGORY_SEPERATOR = ",";
	
	
	public static void setPrintSettings(){
		PrintStream out;
		try {
			out = new PrintStream(new FileOutputStream(Config.logPath));
			if(!Config.printErrorOnConsole){
				System.setErr(out);
			}
			if(!Config.printOutOnConsole){
				System.setOut(out);
			}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
}
