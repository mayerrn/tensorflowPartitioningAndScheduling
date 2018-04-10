package Partitioning;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import Device.Device;
import graph.ColocationComponent;
import graph.ConstraintInterface;
import graph.CreationGraph;
import graph.Node;
import graph.Path;

public class RangeBasedImportancePartitioning extends PartitioningStrategy {

	@Override
	public void partition(CreationGraph graph, HashMap<Integer, Device> devices) {
		List<Node> nodes = new ArrayList<Node>(graph.getNodes());
		// assignNode(node, deviceId, devices, graph);
		// Sorting
		Collections.sort(nodes, new Comparator<Node>() {
			@Override
			public int compare(Node node1, Node node2) {
				return node2.getSumOpsTillSinkNodeOpsFromSourceNode() - node1.getSumOpsTillSinkNodeOpsFromSourceNode();
			}
		});

		List<Device> devicesSorted = new ArrayList<Device>(devices.values());
		// assignNode(node, deviceId, devices, graph);
		// Sort devices based communication
		Collections.sort(devicesSorted, new Comparator<Device>() {
			@Override
			public int compare(Device device1, Device device2) {
				return device2.getNumberOfOperations() - device1.getNumberOfOperations();
			}
		});

		int deviceId = 0;
		float nodesPerMachine = (float) nodes.size() / devices.size(); 
		
		for (int i = 0; i < nodes.size(); i++) {
			Node node = nodes.get(i);
			if (node.getDeviceId() == -1) {
				deviceId = (int) ((int) i / nodesPerMachine);
				
				if (node.getColocationId() != -1) {
					ColocationComponent component = graph.getColocationMap().get(node.getColocationId());
					int assigmentId = getDeviceIdToAssignComponentTo(component, deviceId, devices);
					for (Node coloNodes : component.getNodes()) {
						assignNode(coloNodes, assigmentId, devices, graph);
					}
				} else {
					int assigmentId = getDeviceIdToAssignComponentTo(node, deviceId, devices);
					assignNode(node, assigmentId, devices, graph);
				}
			}
		}
	}
	
	public int getDeviceIdToAssignComponentTo(ConstraintInterface component, int startValue,
			HashMap<Integer, Device> devices) {
		int i = startValue;
		while (i < startValue + devices.size()) {
			int index = i % devices.size();
			//System.out.println(index);
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
