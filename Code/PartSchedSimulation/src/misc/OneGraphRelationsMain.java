package misc;

import java.io.IOException;

public class OneGraphRelationsMain {
	
	private static String evaluationParameter = "Edge Probability"; //RatioComputeCostToTensorSize
	
	//// String graphNameComputeCost = "../../Sample_Data/TensorFlow sample code/Test_relations/recurrent_network_graph_";
	private static String graphNameComputeCost = "../../Sample_Data/TensorFlow sample code/Test_relations/dynamic_rnn_graph_";
	// String[] computationPrefactor = {"0.05","0.1","0.2","0.4", "0.6", "0.8","1", "2"}; //,"10", "50", "100", "200"};
	private static String[] computationPrefactor = { "0.05", "0.1", "0.2", "0.4", "0.6", "0.8", "1", "2", "10", "50", "100","200" };
	
	private static String graphNameEdgeProb = "../../Sample_Data/Graph/sample_graph2017_01_20_09_47_22_";
	private static String[] edge_probability = {"0.05" ,"0.1", "0.2","0.3"}; //,"0.4" ,"0.7"};
	
	
	
	public static void main(String[] args) {
		Config.setPrintSettings();
		String time = Util.currentTime();
		oneGraphRelations(time, evaluationParameter);
	}
	
	
	static void oneGraphRelations(String time, String parameter) {
		String filePath = ("../../Visualisation/results/" + time + "_" + parameter +"_relations.csv");
		String filePathEvaluationParameter = ("../../Visualisation/toDelete/" + time);
		String graphName = null;
		if(parameter.equals("RatioComputeCostToTensorSize")){
			 graphName = graphNameComputeCost;
		}else if(parameter.equals("Edge Probability")){
			 graphName = graphNameEdgeProb;
		}

		for (int j = 0; j < Config.combinations.length; j++) {
			
			String partitioningName = Config.combinations[j][0];
			String schedulingName = Config.combinations[j][1];

			// hier vllt noch über bestimmte Devices iterieren
			// z.B Geräteanzahl, compute costs variation zu commuikationskosten,
			// Colocation constraints, edge prohability, level limitation for
			// edges

			String[] factors = null;
			if(parameter.equals("RatioComputeCostToTensorSize")){
				factors = computationPrefactor;
			}else if(parameter.equals("Edge Probability")){
				factors = edge_probability;
			}
			
			for (String factor : factors) {
				boolean successfulRun = MultipleGraphsCombinationsMain.repeatExecution(1, graphName + factor + ".csv", partitioningName,
						schedulingName, filePath, Config.deviceFile, true, filePathEvaluationParameter, filePathEvaluationParameter);
				if (!successfulRun) {
					System.err.println("An error occured");
					break;
				}
			}
		}
		try {
			Process p = Runtime.getRuntime().exec("python ../../Visualisation/line_diagram_relations_one_graph.py "
					+ filePath + " executionTime " + parameter);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
