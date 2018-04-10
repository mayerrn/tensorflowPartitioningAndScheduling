package Partitioning;

import java.util.HashMap;
import java.util.HashSet;

import Device.Device;
import Device.DeviceConstraint;
import graph.ColocationComponent;
import graph.ConstraintInterface;
import graph.CreationGraph;
import graph.Node;
import graph.Path;
import graph.Path.PathNotSplitableExection;

public class DividePathPartitioning extends PartitioningStrategy {

	HashSet<Node> sourceNodes;
	HashSet<Node> sinkNodes;
	// ist keine gute metric da ops till node immer neu berechnet werden ->
	// nimmt stark ab
	int maxPathImportance = -1;

	@Override
	public void partition(CreationGraph graph, HashMap<Integer, Device> devices) {

		sourceNodes = new HashSet<Node>(graph.getSourceNodes());
		sinkNodes = new HashSet<Node>(graph.getSinkNodes());

		for (Node node : graph.getNodes()) {
			node.copyNodes();
		}
		while (!sourceNodes.isEmpty()) {
			Path path = getLongestPath(graph);
			//System.out.println(path.getNodes().size());
			assignPath(path, graph, devices);
		}
	}

	public Path getLongestPath(CreationGraph graph) {
		for (Node node : sourceNodes) {
			node.calculateOpsTillNodeWithoutSendReceive(null, true);
		}

		Node maxSink = null;

		for (Node node : sinkNodes) {
			if (maxSink == null) {
				maxSink = node;
			} else {
				if (maxSink.getOpsTillNodeWithoutSendReceiveNodes() < node.getOpsTillNodeWithoutSendReceiveNodes()) {
					maxSink = node;
				}
			}
		}

		if (maxPathImportance == -1) {
			maxPathImportance = maxSink.getOpsTillNodeWithoutSendReceiveNodes()
					+ maxSink.getRemainedOperationsTillSinkNodeWithoutSendReceiveNodes();
		}

		// System.out.println("\n" +
		// maxSink.getOpsTillNodeWithoutSendReceiveNodes());
		// TODO id is probably not necessary anymore
		Path path = new Path(1);
		Node temp = null;
		while (maxSink != null) {
			path.addNode(maxSink, graph);
			if (maxSink.getPredecessorNode() != null) {
				maxSink.getPredecessorNode().outgoingNodesRemained.remove(maxSink);
				maxSink.incomingNodesRemained.remove(maxSink.getPredecessorNode());

				// sinkNodes und sourceNodes müssen bei Pfadentnahme modifiziert
				// werden
				// Knoten müssen ggf aus Graph entfernt werden
				// wenn Knoten keine ausgehenden Knoten mehr hat -> sinkNode
				// wenn Knoten keine eingehenden Knotem mehr hat -> sourceNode
				// wenn weder noch -> unerreichbar und entfernen -> war auf
				// einem Pfad, also zugewiesen
				boolean hasOutgoingNodes = (maxSink.outgoingNodesRemained.size() > 0);
				boolean hasIncomingNodes = (maxSink.incomingNodesRemained.size() > 0);

				if (!hasOutgoingNodes && hasIncomingNodes) {
					sinkNodes.add(maxSink);
				}
				if (hasOutgoingNodes && !hasIncomingNodes) {
					sourceNodes.add(maxSink);
				}
				// is first node -> sink node, check if we have to remove it
				// from sink nodes
				if (temp == null) {
					if (maxSink.incomingNodesRemained.size() == 0) {
						sinkNodes.remove(maxSink);
					}
				}

			}
			temp = maxSink;
			maxSink = maxSink.getPredecessorNode();
		}
		//System.out.println(path.getNodes().size());
		if (temp.outgoingNodesRemained.size() == 0) {
			sourceNodes.remove(temp);
		}

		return path;

	}

	// genug memory, und dann besser entscheiden, wohin mappen
	// muss gesplittet werden? -> device constraints, oder schon zugewiesene
	// knoten
	public void assignPath2(Path path, CreationGraph graph, HashMap<Integer, Device> devices) {
		// sort devices based on queue

		// device.getSumOfOperationsOfAlreadyAssignedNodes()
		Device minDevice = devices.get(0);
		for (Device device : devices.values()) {
			if (device.getSumOfOperationsOfAlreadyAssignedNodes() < minDevice
					.getSumOfOperationsOfAlreadyAssignedNodes()) {
				minDevice = device;
			}
		}

		for (Node node : path.getNodes()) {
			if (node.getDeviceId() == -1) {
				if (node.getColocationId() != -1) {
					ColocationComponent component = graph.getColocationMap().get(node.getColocationId());
					for (Node coloNode : component.getNodes()) {
						assignNode(coloNode, minDevice.getId(), devices, graph);
					}
				} else {
					assignNode(node, minDevice.getId(), devices, graph);
				}
			}
		}

		// alreadyAssignedNodes, markieren Trennstellen, sofern auf anderem
		// device

		// get all colocation constrains, check necessary cut locations
		// check where to cut node
		// heurisitics to decide where to map path:
		// 1.) wo kann man zuweisen?: Memory, Device constraint
		// 2.) wie viel ist schon auf der Maschine (num of operations/speed),
		// (wie schnell ist die Maschine)
		// 3.) Wie wichtig ist der Pfad?
		// 4.) Wie viele sind schon auf der Maschine? (Outgoing/Incoming Nodes,
		// die nicht auf Pfad liegen)

		// Pfad immer weiter splitten (/2) bis er auf Maschine passt, zunächst
		// nach widersprüchlichen Constraints splitten
	}

	// genug memory, und dann besser entscheiden, wohin mappen
	// muss gesplittet werden? -> device constraints, oder schon zugewiesene
	// knoten
	public void assignPath(Path path, CreationGraph graph, HashMap<Integer, Device> devices) {
		// sort devices based on queue
		for (Path subPath : path.subpaths) {

			Device minDevice = getAssignmentDevice(devices, subPath);
			
			if(minDevice == null){
				//memory constraints berücksichtigen -> falls Subpfad nirgendwo
				// richtig hinpasst -> Pfad teilen.
				//split path, auf Subpfad neuen pfad mit eigenen subpfaden machen
				try {
					subPath.split(graph);
					for (Path subsubPath : subPath.subpaths){
						assignPath(subsubPath, graph, devices);
					}
				} catch (PathNotSplitableExection e) {
					System.err.println("path could not be assigned, no valid partitioning");
				}

			}else{
				//subpathes stimmen nur nicht mehr, wenn Knoten auf dem weg zugewiesen werden, könnten dann noch feingrunlarer aufgeteilt werden
				for (Node node : subPath.getNodes()) {

					if (node.getColocationId() != -1) {
						ColocationComponent component = graph.getColocationMap().get(node.getColocationId());
						for (Node coloNodes : component.getNodes()) {
							if (coloNodes.getDeviceId() == -1) {
								assignNode(coloNodes, minDevice.getId(), devices, graph);
							}
						}
						// only assign if not already assigned, auch wenn eigentlich
						// nur colocated node auf Pfad ist, könnten mehrere Knoten
						// einer colocation nodes
					} else {
						assignNode(node, minDevice.getId(), devices, graph);
					}
				}
			}

		}

	}

	// return null if path is too long to fit on a machine or there is no feasible device available
	private Device getAssignmentDevice(HashMap<Integer, Device> devices, Path subPath) {

		Device assigmentDevice = null;
		int weight = 0;
		for (Device device : devices.values()) {
			if (device.canAddComponent(subPath)) {
				if (assigmentDevice == null) {
					assigmentDevice = device;
				} else {
					// früher nur hier minimiert:
					// getSumOfOperationsOfAlreadyAssignedNodes
					int speed = device.getSumOfOperationsOfAlreadyAssignedNodes() / device.getNumberOfOperations();
					if (speed < assigmentDevice.getSumOfOperationsOfAlreadyAssignedNodes()
							/ assigmentDevice.getNumberOfOperations()) {
						assigmentDevice = device;
					}
				}
			}
		}

		return assigmentDevice;
	}

}
