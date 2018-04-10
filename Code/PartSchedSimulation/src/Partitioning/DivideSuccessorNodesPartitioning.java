package Partitioning;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import Device.Device;
import graph.ColocationComponent;
import graph.ConstraintInterface;
import graph.CreationGraph;
import graph.Node;

public class DivideSuccessorNodesPartitioning extends PartitioningStrategy {

	class NodeBundle {
		public NodeBundle(Set<Node> nodes, int proposedDeviceId) {
			this.proposedDeviceId = proposedDeviceId;
			this.nodes = nodes;
		}

		public int proposedDeviceId;
		public Set<Node> nodes;
	}

	@Override
	public void partition(CreationGraph graph, HashMap<Integer, Device> devices) {

		Queue<NodeBundle> queue = new LinkedList<NodeBundle>();
		NodeBundle firstBundle = new NodeBundle(graph.getSourceNodes(), 0);
		queue.add(firstBundle);
		LinkedList<Node> assignOrder = new LinkedList<Node>();
		while (!queue.isEmpty()) {

			NodeBundle bundle = queue.poll();
			int deviceHash = bundle.proposedDeviceId;
			Set<Node> nodes = bundle.nodes;

			// define which node to keep and which to split
			for (Node nodeToAssign : nodes) {
				if (nodeToAssign.getDeviceId() == -1) {
					int insertionPosition = 0;
					for (int j = 0; j < assignOrder.size(); j++) {
						if (nodeToAssign.getRemainedOperationsTillSinkNodeWithoutSendReceiveNodes() < assignOrder.get(j)
								.getRemainedOperationsTillSinkNodeWithoutSendReceiveNodes()) {
							insertionPosition++;
						} else {
							break;
						}
					}
					assignOrder.add(insertionPosition, nodeToAssign);
				}
			}

			// nodes contains all nodes that should be splited
			for (Node nodeToAssign : assignOrder) {

				// if not outgoing nodes have already been added and node is
				// assigned
				// with previous for-loop unnecessary
				if (nodeToAssign.getDeviceId() == -1) {

					int colocationId = nodeToAssign.getColocationId();

					ConstraintInterface component = null;
					
					if (colocationId != -1) {
						component = graph.getColocationMap().get(colocationId);
					} else {
						component = nodeToAssign;
					}
					
					int deviceId = getDeviceIdToAssignComponentTo(nodeToAssign, deviceHash, devices);

					if (colocationId != -1) {

						for (Node node : ((ColocationComponent) component).getNodes()) {
							assignNode(node, deviceId, devices, graph);
							queue.add(new NodeBundle(node.getOutgoingNodes(), node.getDeviceId()));
						}
					} else {
						assignNode(nodeToAssign, deviceId, devices, graph);
						queue.add(new NodeBundle(nodeToAssign.getOutgoingNodes(), nodeToAssign.getDeviceId()));
					}

				}

				deviceHash = (deviceHash + 1) % devices.size();
			}

		}

	}

	public int getDeviceIdToAssignComponentTo(ConstraintInterface component, int startValue,
			HashMap<Integer, Device> devices) {
		int i = startValue;
		while (i < startValue + devices.size()) {
			int index = i % devices.size();
			if (devices.get(index).canAddComponent(component)) {
				return index;
			} else {
				i++;
			}
		}
		System.err.println("Component with constraint: " + component.getDeviceConstraint() + ", memory size: "
				+ component.getTotalRamDemand() + " could not be matched to any device");
		return -1;
	}

}