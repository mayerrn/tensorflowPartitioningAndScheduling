package graph;

import java.util.ArrayList;
import java.util.HashSet;

import Device.DeviceConstraint;

public class Path implements ConstraintInterface{

	public class PathNotSplitableExection extends Exception {

		private static final long serialVersionUID = 1L;

		public PathNotSplitableExection(String message) {
	        super(message);
	    }
	}
	
	private int id;
	// ggf ArrayList zum Zerschneiden
	private ArrayList<Node> nodes = new ArrayList<Node>();
	private int totalRamDemand = 0;
	private DeviceConstraint constraint = DeviceConstraint.NO;
	//private int priority = 0;
	//private int numberOfOperations = 0;
	public ArrayList<Path> subpaths = new ArrayList<Path>();
	// helper for subpaths
	private boolean closed = false;

	// GPUNode or colocated with GPUNode
	// public HashSet<Node> GPUNodes = new HashSet<Node>();
	// CPUNode or colocated with CPUNode
	// public HashSet<Node> CPUNodes = new HashSet<Node>();
	// natürliche Trennstellen
	// public HashSet<Node> alreadyAssignedNodes = new HashSet<Node>();
	// irgendwo muss berücksichtigt werden, dass Knoten auf Pfad schon
	// zugewiesen -> wegen Colocation, oder Knoten liegt auf mehreren Pfaden

	// bei Colocation immer alle auf einmal zuweisen oder weil anderer Pfad da
	// schon langlief

	public Path(int id) {
		this.id = id;
	}

	// hier etwas überlegen, nimmt man einfach den Erstmöglichen?
	// oder Pfade erstmal unabhängig davon bilder, falls unterschiedlich
	// einzelne Knoten auslagern
	/*
	 * if (node.getDeviceConstraint() != DeviceConstraint.NO) { if
	 * (this.constraint == DeviceConstraint.NO) { this.constraint =
	 * node.getDeviceConstraint(); } else if (this.constraint !=
	 * node.getDeviceConstraint()) {
	 * 
	 * } }
	 * 
	 */

	public void addNode(Node node, CreationGraph graph) {
		node.setPathId(this.id);
		nodes.add(node);
		// ggf. hier auch colocations prüfen und hinzufügen
		// TODO prüfen ob die auch immer gleich bleibt
		// theoretische Pfadlänge, wenn alles was sich auf diesem großen Pfad
		// (enthalt knoten von source bis sink) so schnell wie möglich
		// ausgeführt wird
		// priority = node.getOpsTillNodeWithoutSendReceiveNodes() +
		// node.getRemainedOperationsTillSinkNode();
		// numberOfOperations += node.getNumberOfOperations();
		/*
		 * if(node.getDeviceConstraint() == DeviceConstraint.CPU){
		 * this.CPUNodes.add(node); }else if(node.getDeviceConstraint() ==
		 * DeviceConstraint.GPU){ this.GPUNodes.add(node); }
		 * if(node.getDeviceId() != -1){ this.alreadyAssignedNodes.add(node); }
		 */

		if (node.getDeviceId() == -1) {

			if (subpaths.size() == 0 || subpaths.get(0).closed) {
				Path path = new Path(-1);
				subpaths.add(0, path);
			}
			Path actualPath = subpaths.get(0);

			if (node.getColocationId() != -1) {
				ColocationComponent component = graph.getColocationMap().get(node.getColocationId());
				if (component.getDeviceConstraint() == DeviceConstraint.NO
						|| component.getDeviceConstraint() == constraint || constraint == DeviceConstraint.NO) {
					actualPath.addSubPathNode(node, graph);
				}else{
					//erstelle neuen Pfad wenn constraints nicht auf alten passen
					actualPath.closed = true;
					Path path = new Path(-1);
					path.addSubPathNode(node, graph);
					subpaths.add(0, path);
				}
			} else {
				if (node.getDeviceConstraint() == DeviceConstraint.NO || node.getDeviceConstraint() == constraint) {
					actualPath.addSubPathNode(node, graph);
				}else{
					actualPath.closed = true;
					Path path = new Path(-1);
					path.addSubPathNode(node, graph);
					subpaths.add(0, path);
				}
			}
		} else {
			if (subpaths.size() > 0) {
				Path actualPath = subpaths.get(0);
				actualPath.closed = true;
			} else {
				// sollte nicht auftreten
			}

		}
	}
	
	//split the subpath (was probably to large (memory) to fit on one device onto a device)
	//alternative koennte man auch über die arraylist nodes splitten, aber so einfacher
	public void split(CreationGraph graph) throws PathNotSplitableExection{
		
		if(this.getNodes().size() < 2){
			System.err.println("Path is not big enough to be splited");
			throw new PathNotSplitableExection("Path is not big enough to be splited");
		}
		
		//Path path = new Path(-1); //maybe use subpath instead of creating a new path -> is possible very easily
		//change back if this leads to problems
		Path firstSubpath = new Path(-1);
		this.subpaths.add(firstSubpath);
		Path secondSubpath = new Path(-1);
		this.subpaths.add(secondSubpath);
		//does not work with addNode (as there are subpath created but not considering because of the memory limitation)
		boolean foundAlreadyAssignedNode = false;
		for(Node node: this.getNodes()){
			if(node.getDeviceId() != -1){
				foundAlreadyAssignedNode = true;
			}
			//test if RAM demand is halfed
			//einziges Problem: wenn vorher beim iteriieren über die Pfade colocated nodes zugewiesen wurde, könnte sich wieder eine natürliche Trennstelle ergeben haben
			//firstSubpath.getNodes().size() == 0: wichtig damit immer mindestens ein Knoten abgespaltet wird.
			if((firstSubpath.getTotalRamDemand() < this.getTotalRamDemand()/2 && !foundAlreadyAssignedNode) || firstSubpath.getNodes().size() == 0){
				firstSubpath.addSubPathNode(node, graph);
			}else{
				secondSubpath.addSubPathNode(node, graph);
			}
		}
		//return subpath;
		
	}
	
	//the difference to add node is that in this method no subpathes are created
	private void addSubPathNode(Node node, CreationGraph graph) {
		nodes.add(node);
		if (node.getColocationId() != -1) {
			ColocationComponent component = graph.getColocationMap().get(node.getColocationId());
			for (Node coloNode : component.getNodes()) {
				totalRamDemand += coloNode.getTotalRamDemand();
			}
		} else {
			totalRamDemand += node.getTotalRamDemand();
		}
		if (node.getDeviceConstraint() != DeviceConstraint.NO) {
			this.constraint = node.getDeviceConstraint();
		}
	}

	public ArrayList<Node> getNodes() {
		return this.nodes;
	}

	public int getId() {
		return this.id;
	}

	// assigend Nodes sind schon rausgeworfen in subnodes
	public int getTotalRamDemand() {
		// TODO Auto-generated method stub
		return this.totalRamDemand;
	}

	public DeviceConstraint getDeviceConstraint() {
		// TODO Auto-generated method stub
		return this.constraint;
	}

	@Override
	public int getNumberOfOperations() {
		// TODO Auto-generated method stub
		System.err.println("Not implemented");
		return 0;
	}

}
