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

public class HeftPartitioning extends PartitioningStrategy{

	@Override
	public void partition(CreationGraph graph, HashMap<Integer, Device> devices) {
		List<Node> nodes = new ArrayList<Node>(graph.getNodes());
		Collections.sort(nodes, new Comparator<Node>() {
			@Override
			public int compare(Node node1, Node node2) {
				if(Math.abs(node1.getUpwardsRank() - node2.getUpwardsRank()) < 0.00005){
					if(node1.getTieBreaker() > node2.getTieBreaker()){
						return -1;
					}else{
						return 1;
					}
				}
				
				 if(node2.getUpwardsRank() > node1.getUpwardsRank()){
					 return 1;
				 }else{
					 return -1;
				 }
			}
		});
		
		//nodes sorted in decreasing order
		for(int i = 0; i<nodes.size(); i++){
			//System.out.println(nodes.get(i).getId() + " " + nodes.get(i).getUpwardsRank());
			Node node = nodes.get(i);
			//was not assigned due to a colocation
			if (node.getDeviceId() == -1) {				
				if (node.getColocationId() != -1) {
					ColocationComponent component = graph.getColocationMap().get(node.getColocationId());
					ScheduledTimeSlots slot = getScheduledTimeSlotToAssignComponentTo(component, devices, node);
					for (Node coloNodes : component.getNodes()) {
						assignNode(coloNodes, slot.getDeviceId(), devices, graph);
					}
					//only add the slot for the scheduled node
					devices.get(slot.getDeviceId()).addScheduledTimeSlot(slot);
					node.setFinishTime(slot.getEndTime());
				} else {
					ScheduledTimeSlots slot = getScheduledTimeSlotToAssignComponentTo(node, devices, node);
					assignNode(node, slot.getDeviceId(), devices, graph);
					devices.get(slot.getDeviceId()).addScheduledTimeSlot(slot);
					node.setFinishTime(slot.getEndTime());
				}
			}else{
				//Node is already assigned
				Device device = devices.get(node.getDeviceId());
				ScheduledTimeSlots slot = device.getScheduledTimeSlotForNode(node);
				device.addScheduledTimeSlot(slot);
				node.setFinishTime(slot.getEndTime());
			}
		}
	}
/*	public void assignNode(Node node, int deviceId, HashMap<Integer, Device> devices, CreationGraph graph) {
		super.assignNode(node, deviceId, devices, graph);
		//add finish time here
		
	}
	*/
	public ScheduledTimeSlots getScheduledTimeSlotToAssignComponentTo(ConstraintInterface component, HashMap<Integer, Device> devices, Node node) {
		
		//Device EFTdevice = null;
		ScheduledTimeSlots minEFTSlot = null;
		for(Device device: devices.values()){
			//feasible device
			if (device.canAddComponent(component)) {
				ScheduledTimeSlots slot = device.getScheduledTimeSlotForNode(node);
				if(minEFTSlot == null || slot.getEndTime() < minEFTSlot.getEndTime()){
					minEFTSlot = slot;
					//EFTdevice = device;
				}
				//System.out.println(device.getId() + " " + slot.getStartTime() + " " + slot.getEndTime());
			}

		}
		if(minEFTSlot != null){
			//System.out.println(minEFTSlot.getDeviceId());
			return minEFTSlot;
		}
		System.err.println("Component with constraint: " + component.getDeviceConstraint() + ", memory size: "
				+ component.getTotalRamDemand() + " could not be matched to any device");
		return null;
	}

}
