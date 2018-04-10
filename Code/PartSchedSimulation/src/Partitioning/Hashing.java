package Partitioning;

import java.util.HashMap;
import java.util.HashSet;

import Device.Device;
import graph.ColocationComponent;
import graph.ConstraintInterface;
import graph.CreationGraph;
import graph.Node;

public class Hashing extends PartitioningStrategy {

	@Override
	public void partition(CreationGraph graph, HashMap<Integer, Device> devices) {
		int deviceHash = 0;
		
		// assign colocation components
		for (Integer componentId : graph.getColocationMap().keySet()) {
			ColocationComponent component = graph.getColocationMap().get(componentId);
			int deviceId = getDeviceIdToAssignComponentTo(component, deviceHash, devices);
			for (Node node : component.getNodes()) {
				assignNode(node, deviceId, devices, graph);
			}
			deviceHash = (deviceHash + 1) % devices.size();
		}

		// assign nodes (which are not a member of the colocation components)
		HashSet<Node> nodesNotColocated = new HashSet<Node>(graph.getNodes());
		for (Node node : nodesNotColocated) {
			int deviceId = getDeviceIdToAssignComponentTo(node, deviceHash, devices);
			assignNode(node, deviceId, devices, graph);
			deviceHash = (deviceHash + 1) % devices.size();
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
				//System.err.println("Can't assign it"); //does not have to be a problem
				i++;
			}
		}
		System.err.println("Component with constraint: " + component.getDeviceConstraint() + ", memory size: "
				+ component.getTotalRamDemand() + " could not be matched to any device");
		return -1;
	}

}
