package graph_generator;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Set;

public class GraphWriter {
	
	
	public void write(String nodes, String graphPath) throws IOException{
		//File file = new File(GraphConfig.GRAPH_PATH + value + Util.currentTime() + ".csv");
		File file = new File(graphPath + ".csv");

		// if file doesnt exists, then create it
		if (!file.exists()) {
			file.createNewFile();
		}

		FileWriter fw = new FileWriter(file.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write("Id,(Outgoing) node,(Incoming) node,Colocation nodes,#tensorSize, #operations, RAM storage,Device constraint ,name\n");
		bw.write(nodes);
		bw.close();
		fw.close();
	}
	
	public void writeVisualisation(String string, String graphPath) throws IOException{
		//File file = new File(GraphConfig.GRAPH_PATH + Util.currentTime() + ".dot");
		File file = new File(graphPath + ".dot");
		FileWriter fw = new FileWriter(file.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write(string);
		bw.close();
		fw.close();
	}
}
