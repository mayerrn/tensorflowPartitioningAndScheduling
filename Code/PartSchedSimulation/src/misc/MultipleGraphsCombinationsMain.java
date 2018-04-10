package misc;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;

import java.util.ArrayList;
import java.util.HashMap;

import Device.Device;
import Device.DeviceReader;
import Partitioning.PartitioningPostProcessor;
import Partitioning.PartitioningPreprocessor;
import Partitioning.PartitioningStrategy;
import deviceScheduler.DeviceScheduler;
import deviceScheduler.DeviceScheduler2;
import evaluation.Evaluation;
import evaluation.EvaluationParameter;
import graph.CreationGraph;
import graph.GraphReader;
import graph.Node;
import scheduler.Scheduler;

public class MultipleGraphsCombinationsMain {
	
	public static String[] graphPaths = {

			//in eval: 
//			"../../Sample_Data/Graph/Evaluation/convolutional_network.csv",
//			"../../Sample_Data/Graph/Evaluation/recurrent_network.csv",
//			"../../Sample_Data/Graph/Evaluation/dynamic_rnn.csv", 
//			"../../Sample_Data/Graph/toyGraph.csv",
			
			"../../Sample_Data/Graph/Evaluation/3528n_53253_2017_03_31_15_58_31.csv",
			"../../Sample_Data/Graph/Evaluation/3793n_6112_2017_03_31_15_58_59.csv",
			"../../Sample_Data/Graph/Evaluation/3872n_665137_2017_03_31_15_57_47.csv",
			"../../Sample_Data/Graph/Evaluation/4112n_14174_2017_03_31_15_58_50.csv",
			"../../Sample_Data/Graph/Evaluation/4122n_150905_2017_03_31_15_58_27.csv",
			"../../Sample_Data/Graph/Evaluation/4125n_3620_2017_03_31_15_59_08.csv",
			"../../Sample_Data/Graph/Evaluation/4185n_380243_2017_03_31_15_58_19.csv",
			"../../Sample_Data/Graph/Evaluation/4193n_1536_2017_03_31_15_59_16.csv",
			"../../Sample_Data/Graph/Evaluation/4278n_830_2017_03_31_15_59_23.csv",
			"../../Sample_Data/Graph/Evaluation/4641n_46065_2017_03_31_15_58_36.csv",
			
//			"../../Sample_Data/Graph/toyGraph2.csv",
			/*
			"../../Sample_Data/Graph/Evaluation/1.csv", //2017_02_18_19_36_28.csv", 
			
			"../../Sample_Data/Graph/Evaluation/2.csv",
			"../../Sample_Data/Graph/Evaluation/3.csv",
		    "../../Sample_Data/Graph/Evaluation/4.csv",
			"../../Sample_Data/Graph/Evaluation/5.csv",
			"../../Sample_Data/Graph/Evaluation/6.csv",

		*/
		
   };
	
	public static String[] devicePaths ={ 
		//"../../Sample_Data/Device/large_devices.csv",
		//"../../Sample_Data/Device/large_heterogenous_devices.csv"
			//"../../Sample_Data/Device/2017_02_02_09_46_39.csv"
			"../../Sample_Data/Device/2017_02_02_14_25_28-100-10-100-100000000-10-60-0.6.csv",
//			"../../Sample_Data/Device/toyDevices.csv",
			//"../../Sample_Data/Device/2017_02_02_09_57_09-10-10-100-100000000-10-60-0.6.csv"
			//"../../Sample_Data/Device/2017_02_03_11_22_48-50-10-100-100000000-10-60-0.6.csv"
			//"../../Sample_Data/Device/2017_02_02_09_57_09-7-10-100-100000000-10-60-0.6.csv"

			
	};

	
	public static EvaluationParameter[] evaluation_parameter = { EvaluationParameter.RamUsage,
			EvaluationParameter.SchedulingPossibilities, EvaluationParameter.Utilization };
	// utilization -> device utilization
	// 1 means that plot should be shown, 0 not
	public static String[] viszualisationParameter = { "executionTime 1", "communicationCost 0", "partitioningExecutionTime 0", "schedulingExecutionTime 0"};


	
	public static void main(String[] args) {
		Config.setPrintSettings();
		for(String devicePath: devicePaths){
			String time = Util.currentTime();
			multipleGraphCombinations(time, devicePath);
		}
	}


	static void multipleGraphCombinations(String time, String devicePath) {
		String filePath = ("../../Visualisation/results/" + time + ".csv");
		String filePathEvaluationParameter = ("../../Visualisation/results/" + time);
		String filePathDeviceEvals = "../../Visualisation/visualisation_plots/" + time;

		boolean generatePlots = true;

		// test all given graphs
		graphLoop: for (int i = 0; i < graphPaths.length; i++) {
			// at these partitioning and scheduling combinations
			for (int j = 0; j < Config.combinations.length; j++) {
				String graphName = graphPaths[i];
				String partitioningName = Config.combinations[j][0];
				String schedulingName = Config.combinations[j][1];
				// hier vllt noch über bestimmte Devices iterieren
				// z.B Geräteanzahl, compute costs variation zu
				// commuikationskosten
				// for(int s = 0; s< 3; s++){
				boolean successfulRun = repeatExecution(5, graphName, partitioningName, schedulingName, filePath,
						devicePath, true, filePathEvaluationParameter, filePathDeviceEvals);

				if (!successfulRun) {
					generatePlots = false;
					break graphLoop;
				}
				// }
			}
		}
		if (generatePlots) {
			try {
				// generate plots for each visualization parameter
				for (String parameter : viszualisationParameter) {
					//System.out.println(parameter);
					Process p = Runtime.getRuntime()
							.exec("python ../../Visualisation/visualizeGraphs.py " + filePath + " " + parameter + " " + devicePath);
					//This prevents later diagrams to not be generated.
					String line;  
					BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()) );
				       while ((line = in.readLine()) != null) {
				         System.out.println(line);
				       }
				       in.close();
				       
				}
				// Process p = Runtime.getRuntime().exec("python yourapp.py");

				for (EvaluationParameter parameter : evaluation_parameter) {
					String readPath = filePathEvaluationParameter + "_" + parameter.toString() + ".csv";
					if (parameter == EvaluationParameter.Utilization) {
						Process p = Runtime.getRuntime()
								.exec("python ../../Visualisation/visualizeUtilization.py " + readPath);
						p.waitFor();
					} else if (parameter == EvaluationParameter.SchedulingPossibilities) {
						Process p = Runtime.getRuntime()
								.exec("python ../../Visualisation/visualizeSchedulingPossibilitiesOrRamUsage.py "
										+ readPath + " " + EvaluationParameter.SchedulingPossibilities.toString());
					} else if (parameter == EvaluationParameter.RamUsage) {
						Process p = Runtime.getRuntime()
								.exec("python ../../Visualisation/visualizeSchedulingPossibilitiesOrRamUsage.py "
										+ readPath + " " + EvaluationParameter.RamUsage.toString());
					}

				}
			} catch (Exception e) {
				System.err.println(e.toString());
			}
		}
	}
	
	public static String getGraphNameFromPath(String graphPath){
		String[] path = graphPath.split("/");
		return path[path.length-1].split(".csv")[0];
	}

	public static boolean repeatExecution(int n, String graphName, String partitioningName, String schedulingName,
			String plotPath, String devicePath, boolean firstDeviceScheduler, String parameterEvaluationFilePath, String filePathDeviceEvals) {

		ArrayList<Evaluation> evaluations = new ArrayList<Evaluation>();
		double executionTime = 0;
		int communicationCost = 0;
		long schedulingTime = 0;
		long partitioningTime = 0;
		
		for (int i = 0; i < n; i++) {
			Evaluation eval = execute(graphName, partitioningName, schedulingName, plotPath, devicePath,
					firstDeviceScheduler, parameterEvaluationFilePath);
			if (eval != null) {
				evaluations.add(eval);
				executionTime += eval.getExecutionTime();
				communicationCost += eval.getCommunicationCost();
				schedulingTime += eval.getSchedulingExecutionTime();
				partitioningTime += eval.getPartitioningExecutionTime();
			} else {
				return false;
			}
		}
		double avrExecutionTime = executionTime / n;
		double avrCommunicationCosts = communicationCost / n;
		double avrSchedulingTime = schedulingTime/n;
		double avrPartitioningTime = partitioningTime/n;

		String abbrPartitioningName = PartitioningStrategy.abbrevationForPartitioningName(partitioningName);
		String abbrSchedulingName = Scheduler.abbreavatonForSchedulingName(schedulingName);

		double stdExecutionTime = 0;
		double stdCommunicationCost = 0;
		double stdSchedulingTime = 0;
		double stdPartitioningTime  = 0;
		Evaluation closestEval = null;

		for (Evaluation evaluation : evaluations) {
			if (closestEval == null) {
				closestEval = evaluation;
			} else {
				if (Math.abs(closestEval.getExecutionTime() - avrExecutionTime) > Math
						.abs(evaluation.getExecutionTime() - avrExecutionTime)) {
					closestEval = evaluation;
				}
			}

			stdExecutionTime += Math.pow((evaluation.getExecutionTime() - avrExecutionTime), 2);
			stdCommunicationCost += Math.pow((evaluation.getCommunicationCost() - avrCommunicationCosts), 2);
			stdSchedulingTime += Math.pow((evaluation.getSchedulingExecutionTime() - avrSchedulingTime), 2);
			stdPartitioningTime += Math.pow((evaluation.getPartitioningExecutionTime() - avrPartitioningTime), 2);
		}

		stdExecutionTime = Math.sqrt(stdExecutionTime / n);
		stdCommunicationCost = Math.sqrt(stdCommunicationCost / n);
		stdSchedulingTime = Math.sqrt(stdSchedulingTime / n);
		stdPartitioningTime = Math.sqrt(stdPartitioningTime / n);
		
		closestEval.setExecutionTime(avrExecutionTime);
		closestEval.setCommunicationCost(avrCommunicationCosts);
		closestEval.setSchedulingExecutionTime(avrSchedulingTime);
		closestEval.setPartitioningExecutionTime(avrPartitioningTime);

		closestEval.save(plotPath, graphName, abbrPartitioningName, abbrSchedulingName, closestEval.getNumberOfNodes(),
				closestEval.getDevices().size(), stdExecutionTime, stdCommunicationCost, stdPartitioningTime, stdSchedulingTime);
		closestEval.saveNodeDeviceDistribution(filePathDeviceEvals +"/"+ getGraphNameFromPath(graphName) , abbrPartitioningName + abbrSchedulingName+ ".csv");
		closestEval.print();

		for (EvaluationParameter parameter : evaluation_parameter) {
			closestEval.saveParameterEvaluation(parameterEvaluationFilePath + "_" + parameter.toString() + ".csv",
					graphName, abbrPartitioningName, abbrSchedulingName, closestEval.getDevices(), parameter);
		}

		System.out.println(graphName + " " + partitioningName + " " + schedulingName);
		//System.out.println("Exece" + stdExecutionTime);
		//System.out.println("Exece" + stdCommunicationCost);

		System.out.println("\n\n\n");
		return true;
	}

	public static Evaluation execute(String graphName, String partitioningName, String schedulingName, String plotPath,
			String devicePath, boolean firstDeviceScheduler, String parameterEvaluationFilePath) {

		System.out.println("Evaluation for graph" + graphName + ", partitioning: " + partitioningName + ", scheduling: "
				+ schedulingName);

		Evaluation evaluation = new Evaluation();
		GraphReader reader = new GraphReader(graphName);

		CreationGraph graph = reader.readGraph();

		int numberOfNodes = graph.getNodes().size();
		int numberOfEdges = 0;
		for(Node node: graph.getNodes()){
			numberOfEdges += node.getOutgoingNodes().size();
		}
		System.out.println("NumberOfEdges " + numberOfEdges);
		//graph.exportColocationStatistics(graphName.split(".csv")[0] + "colocation_stats.txt" ,graphName);

		DeviceReader deviceReader = new DeviceReader(devicePath, evaluation);
		HashMap<Integer, Device> devices = deviceReader.readDevices();

		PartitioningStrategy partitioningStrategy = PartitioningStrategy.strategyForString(partitioningName);

		//start measuring time
		evaluation.setStartTimePartitioning(System.currentTimeMillis());
		PartitioningPreprocessor preprocessor = new PartitioningPreprocessor(graph, partitioningStrategy);
		preprocessor.preprocess(devices);

		partitioningStrategy.partition(graph, devices);

		if (graph.getNodes().size() != 0) {
			System.err.println("Not all nodes partitioned, " + partitioningName + ", " + schedulingName
					+ "\n number of not assigned nodes" + graph.getNodes().size() + " out of " + numberOfNodes);
			// exit and don't save results
			return null;
		}
		Scheduler scheduler = Scheduler.strategyForString(schedulingName);
		scheduler.setDevices(devices);
		
		
		PartitioningPostProcessor postProcessor = new PartitioningPostProcessor(graph, devices, scheduler);
		postProcessor.postProcess();
		evaluation.setStartTimeScheduling(System.currentTimeMillis());

		// Util.printGraphVisualisation(devices);
		if (firstDeviceScheduler) {
			DeviceScheduler deviceScheduler = new DeviceScheduler(devices.values(), evaluation, scheduler);
			deviceScheduler.execute();
		} else {
			// alternative implementatiton for correctness analysis
			DeviceScheduler2 deviceScheduler = new DeviceScheduler2(devices.values(), evaluation, scheduler);
			deviceScheduler.execute();
		}
		//end saving the time
		evaluation.setEndTimeScheduling(System.currentTimeMillis());
		/*
		for (Device device : devices.values()) {
			System.out.println(device.toString());
		}
*/
		if (evaluation.getSchedules() != numberOfNodes) {
			System.err.println("Send schedules " + evaluation.getSendSchedules() + "," + "receive schedules "
					+ evaluation.getReceiveSchedules());
			System.err.println("Not scheduled all nodes, " + partitioningName + ", " + schedulingName
					+ "\nScheduled nodes: " + evaluation.getSchedules() + " out of " + numberOfNodes);
			System.err.println(evaluation.getSchedules() + ", " + numberOfNodes);
			// exit and don't save results
			return null;
		}
		evaluation.setNumberOfNodes(numberOfNodes);
		evaluation.setDevices(devices);

		return evaluation;
	}

	

}
