package graph;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import misc.Config;

public class GraphReader {

	private FileReader fileReader;
	private BufferedReader bufferedReader;

	public GraphReader(String graphPath) {
		try {
			fileReader = new FileReader(graphPath);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		bufferedReader = new BufferedReader(fileReader);
	}

	public CreationGraph readGraph() {
		CreationGraph graph = new CreationGraph();
		try {
			// skip the first line (csv description)
			String input = bufferedReader.readLine();
			if(input.split(",")[0].equals("Meta")){
				bufferedReader.readLine();
			}
			
			while ((input = bufferedReader.readLine()) != null) {
				CreationNodeEntry nodeEntry = new CreationNodeEntry(input);
				graph.addEntry(nodeEntry);
			}
			fileReader.close();
		} catch (Exception e) {
			System.out.println(e.toString());
		}
		
		return graph;
	}

}
