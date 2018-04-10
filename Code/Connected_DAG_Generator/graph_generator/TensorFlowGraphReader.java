package graph_generator;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class TensorFlowGraphReader {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String graphString;
		try {
			graphString = new String(Files.readAllBytes(Paths.get("../../Sample_Data/TensorFlow sample code/dynamic_rnn_graph")), StandardCharsets.UTF_8);
			//graphString = new String(Files.readAllBytes(Paths.get("../../TensorFlow sample code/TensorFlowWebsite/simple_mnist_example_graph")), StandardCharsets.UTF_8);

			//graphString = new String(Files.readAllBytes(Paths.get("../../Graph/Graphs_Raw_TensorFlow/lal.txt")), StandardCharsets.UTF_8);
			String[] nodes = graphString.split("node");
			HashMap<String, Node> hmap = new HashMap<String, Node>();
		    
			//wegen Sink
			int numberOfNode = 1;
			for(int i=1; i< nodes.length; i++){
				String[] lines = nodes[i].split("\n");
				String name = lines[1].split("\"")[1];
				hmap.put(name, new Node(numberOfNode, name));
				numberOfNode ++;
			}
			int numberOfEdges = 0;
			int numberOfColocatedNodes = 0;
			StringBuilder graphVis = new StringBuilder();
			graphVis.append("digraph {\n");
			for(int i=1; i< nodes.length; i++){
				String[] lines = nodes[i].split("\n");
				String name = lines[1].split("\"")[1];
				
				int inputNodeOffset = 3;
				
				while(lines[inputNodeOffset].contains("input")){
					//remove : (valid, what is there use case?)
					String inputNodeName = lines[inputNodeOffset].split("\"")[1].split(":")[0];
					inputNodeName = inputNodeName.replace("^","");
					
					Node inputNode = hmap.get(inputNodeName);
					Node currentNode = hmap.get(name);
					
					if(currentNode == null){
						System.out.println("Current node does not exist: " + name);
						inputNodeOffset++;
						continue;
					}
					if(inputNode == null){
						System.out.println("Input node does not exist: " + inputNodeName);
						inputNodeOffset++;
						continue;
					}
					
					currentNode.getIncomingNodes().add(inputNode);
					inputNode.getOutgoingNodes().add(currentNode);
					numberOfEdges ++;
					graphVis.append("  " + inputNode.getId() + "->"+ currentNode.getId() +";\n");
					inputNodeOffset++;
				}
				
				for(int j = inputNodeOffset; j<lines.length; j++){
					String line = lines[j];
					if(line.contains("\"loc:@")){
						String colocationNodeName = line.split("\"loc:@")[1].replace("\"","");
						System.out.println(name);
						System.out.println("colocation:"+ colocationNodeName + "\n");
						Node coloationNode = hmap.get(colocationNodeName);
						Node currentNode = hmap.get(name);
						
						if(currentNode == null){
							System.out.println("Current node does not exist: " + name);
							continue;
						}
						if(coloationNode == null){
							System.out.println("coloationNode node does not exist: " + colocationNodeName);
							continue;
						}
						currentNode.getColocationNodes().add(coloationNode);
						coloationNode.getColocationNodes().add(currentNode);
						numberOfColocatedNodes ++;
						graphVis.append("  " + coloationNode.getId() + "->" + currentNode.getId() +"[color=blue];\n");

					}
				}
			}
			graphVis.append("}\n");
			
			
			StringBuilder content = new StringBuilder();
			for(Node node: hmap.values()){
				content.append(node.toString());
			}
			System.out.println("Number of nodes:" + numberOfNode);
			System.out.println("Number of colocated nodes:" + numberOfColocatedNodes);
			System.out.println("edges:" + numberOfEdges);
			//String graphPath = "../../Sample_Data/Graph/recurrent_network_graph";
			
			String graphPath = "../../Sample_Data/Graph/Evaluation/dynamic_rnn";
			GraphWriter writer = new GraphWriter();
			writer.write(content.toString(),graphPath);
			writer.writeVisualisation(graphVis.toString(),graphPath);
			//GraphConfig.GRAPH_PATH + value + Util.currentTime() + ".csv
			//static String GRAPH_PATH = "../../Sample_Data/Graph/sample_graph";

			//add other buildes
		/*	String[] computationPrefactor = {"0.01" , "0.03" , "0.05", "0.08" , "0.1", "0.2","0.4", "0.6", "0.8","1", "2"};//"10", "50", "100", "200"};
			for(String factor: computationPrefactor){
				content = new StringBuilder();
				for(Node node: hmap.values()){
					int oldNumberOfOperations = node.getNumberOfOperations();
					node.setNumberOfOperations((int) (oldNumberOfOperations* Float.parseFloat(factor)));
					content.append(node.toString());
					node.setNumberOfOperations(oldNumberOfOperations);
				}
				writer.write(content.toString(),graphPath + "_" + factor);
			}
			*/

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	      
	}

}
