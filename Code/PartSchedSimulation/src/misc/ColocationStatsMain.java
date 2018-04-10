package misc;

import java.io.IOException;

import graph.CreationGraph;
import graph.GraphReader;

public class ColocationStatsMain {

	//String graphName = "sample_graph2017_01_19_13_51_58";
	public static String graphName = "sample_graph2017_01_19_18_03_31";
	
	public static void main(String[] args) {
		Config.setPrintSettings();
		
		String time = Util.currentTime();	
		String colocationVisPath = "../../Visualisation/results/" + time + "colocation_" + graphName;
		//createColocationStats("../../Sample_Data/TensorFlow sample code/" + graphName + ".csv", graphName, colocationVisPath);
		createColocationStats("../../Sample_Data/Graph/" + graphName + ".csv", graphName, colocationVisPath);

		 
	}
	
	public static void createColocationStats(String graphPath, String graphName, String filePath) {
		GraphReader reader = new GraphReader(graphPath);
		CreationGraph graph = reader.readGraph();
		graph.exportColocationStatistics(filePath, graphName);
		try {
			Process p = Runtime.getRuntime().exec("python ../../Visualisation/graph_colocations.py " + filePath + " 1");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
