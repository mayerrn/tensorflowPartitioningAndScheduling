package graph_generator;

public class GraphConfig {
	static int MIN_PER_LEVEL = 1; //min amount of nodes per level
	static int MAX_PER_LEVEL = 80; //120;  //max amount of nodes per level
	static int NUMBER_OF_LEVELS = 100;
	//evtl wahrscheinlichkeit dynamisch machen, je mehr ausgehende Kanten ein Knoten schon besitzt
	//andere Idee mit tri-adjazenzmatrix
	static float EDGE_PROHABILITY = (float) 0.006;     /* Chance of having an Edge.  */
	static int EDGE_LEVEL_LIMIT = 5;
	static float EDGE_LEVEL_FUNCTION = -1.0f;//1.2f;     /* Chance of having an Edge.  */
	
	static float COLOCATION_PROHABILITY = 0.0f; //(float) 0.05;     /* Chance of having an Edge.  */	
	
	static int MIN_EDGE_WEIGHT = 1;
	static int MAX_EDGE_WEIGHT = 100;
	
	static int MIN_OP_WEIGHT = 1;
	static int MAX_OP_WEIGHT = 100;
	
	static float GPU_CONSTRAINT_PROHABILITY = (float) 0.01;
	static float CPU_CONSTRAINT_PROHABILITY = (float) 0.01;

	//relative to number of nodes
	static float SIZE_OF_BIGGEST_COLOCATION_GROUP = 10.0f;
	//other improtant variable with respect to colocation
	//int numberOfColocactionGroups = (int) (nodes.size() / 5 * Math.random() * Math.pow(2, -(i + 2)));

	static int MIN_RAM_FOR_NODE_STORAGE = 1;
	static int MAX_RAM_FOR_NODE_STORAGE = 100;
	
	static float PERCENTAGE_OF_LONG_RANGE_EDGES = (float) 0.1;

	
	public static String CATEGORY_SEPERATOR = ",";
	public static String ARRAY_SEPERATOR = ";";
	
	
	
	static String configToString(){
		StringBuilder buidler = new StringBuilder();
		buidler.append("Meta");
		buidler.append(CATEGORY_SEPERATOR);
		buidler.append(MIN_PER_LEVEL);
		buidler.append(CATEGORY_SEPERATOR);
		buidler.append(MAX_PER_LEVEL);
		buidler.append(CATEGORY_SEPERATOR);
		buidler.append(NUMBER_OF_LEVELS);
		buidler.append(CATEGORY_SEPERATOR);
		buidler.append(EDGE_PROHABILITY);
		buidler.append(CATEGORY_SEPERATOR);
		buidler.append(EDGE_LEVEL_LIMIT);
		buidler.append(CATEGORY_SEPERATOR);
		buidler.append(EDGE_LEVEL_FUNCTION);
		buidler.append(CATEGORY_SEPERATOR);
		buidler.append(COLOCATION_PROHABILITY);
		buidler.append(CATEGORY_SEPERATOR);
		buidler.append(MIN_EDGE_WEIGHT);
		buidler.append(CATEGORY_SEPERATOR);
		buidler.append(MAX_EDGE_WEIGHT);
		buidler.append(CATEGORY_SEPERATOR);
		buidler.append(MIN_OP_WEIGHT);
		buidler.append(CATEGORY_SEPERATOR);
		buidler.append(MAX_OP_WEIGHT);
		buidler.append(CATEGORY_SEPERATOR);
		buidler.append(GPU_CONSTRAINT_PROHABILITY);
		buidler.append(CATEGORY_SEPERATOR);
		buidler.append(CPU_CONSTRAINT_PROHABILITY);
		buidler.append(CATEGORY_SEPERATOR);
		buidler.append(SIZE_OF_BIGGEST_COLOCATION_GROUP);
		buidler.append(CATEGORY_SEPERATOR);
		buidler.append(MIN_RAM_FOR_NODE_STORAGE);
		buidler.append(CATEGORY_SEPERATOR);
		buidler.append(MAX_RAM_FOR_NODE_STORAGE);
		buidler.append(CATEGORY_SEPERATOR);
		buidler.append(PERCENTAGE_OF_LONG_RANGE_EDGES);
		return buidler.toString();

	}
	
}
