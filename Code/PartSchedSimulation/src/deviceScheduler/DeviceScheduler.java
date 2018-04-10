package deviceScheduler;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

import Device.Device;
import Test.GetOutgoingNodesTest;
import evaluation.Evaluation;
import graph.Node;
import scheduler.Scheduler;

public class DeviceScheduler {

	// linked list, sorted based on timestamp, minimum timestamp is on position 0
	LinkedList<Device> deviceTimestampOrdered = new LinkedList<Device>();
	private Evaluation evaluation;
	private Scheduler scheduler;

	//for debugging
	//static HashSet<Integer> test = new HashSet<Integer>();
	//static boolean second = false;

	public DeviceScheduler(Collection<Device> devices, Evaluation evaluation, Scheduler scheduler) {
		deviceTimestampOrdered.addAll(devices);
		if (deviceTimestampOrdered.size() < 2) {
			System.err
					.println("Please use at least 2 devices. If not scheduling and partitioning does not make sense.");
		}
		this.evaluation = evaluation;
		this.scheduler = scheduler;
	}

	public void execute() {
		//debugging
		/* HashSet<Node> addNodesSources = new HashSet<Node>();

		System.out.println(evaluation.getSchedules());
		int numberOfNodes = 0;
		for(Device deviceX: deviceTimestampOrdered){
			numberOfNodes += deviceX.getGraph().getNodes().size();
			System.out.println( deviceX.readyToScheduleNodes.size() + "  " + deviceX.receiveNodes.size());
				if(!second){
					for(Node node: deviceX.readyToScheduleNodes){
						test.add(node.getId());
					}
				}else{
					for(Node node: deviceX.readyToScheduleNodes){
						test.remove(node.getId());
					}
				
			}
				addNodesSources.addAll(deviceX.readyToScheduleNodes);
		}
		GetOutgoingNodesTest.reachableNodes(addNodesSources);
		System.out.println(numberOfNodes);
		
		System.out.println("lala" + "MUMUM" + test.size());
		if(second){
		for(Integer test1: test){
			System.out.println("lala" + test1);
		}
		}
		second = !second;
		*/
		//debugging
		execution: while (true) {
			//orderedListValidator();
			Device device = deviceTimestampOrdered.peekFirst();

			if (device.isScheduable()) {
				this.scheduleDevice(device);
			} else {
				//device with lowest timestamp is not scheduable -> update timestamp (save as it can't get any messages from the other devices which change current scheduling possibilities)
				// jump to min(receiveTimeNextMessage, localTimestamp other devices)
				// find the next scheduable process or jump till next is message received
				boolean foundScheduableDevice = searchSchedualbeDevicesOrReceiveEventsAndModifyTimestamps();
				
				if (!foundScheduableDevice) {
					// if not found any devices which can be scheduled in next loop, check if there are any receiving events bigger than all devices' local timestamp
					if (!this.receivingDataLargerThanAllLocalTimestamps()) {
						break execution;
					}
				}
			}
		}

		double lastTimestamp =  deviceTimestampOrdered.getLast().getLocalTimestamp();
		evaluation.setExecutionTime(lastTimestamp);
		
		for(Device device: deviceTimestampOrdered){
			device.getEvaluation().setTotalExecutionTime(lastTimestamp);
			device.getEvaluation().addRamUsage(device.getCurrentlyUsedMemory());
		}
	}
	
	
	private boolean searchSchedualbeDevicesOrReceiveEventsAndModifyTimestamps(){
		//ggf leichter, alle devices auf minimum receive message durchsuchen, und dann beim scheduling prüfem wann der nächste scheduable ist
		for (int j = 1; j < deviceTimestampOrdered.size(); j++) {
			// found minimum message and reordered linked list
			// check if a message is received smaller than the current timestamp of device on position j
			if (this.searchReceivingDataTimestamps(j)) {
				return true;
			}
			
			if (deviceTimestampOrdered.get(j).isScheduable()) {
				double timestamp = deviceTimestampOrdered.get(j).getLocalTimestamp();
				for (int n = 0; n < j; n++) {
					deviceTimestampOrdered.get(n).setLocalTimestamp(timestamp);
				}
				Device device = deviceTimestampOrdered.get(j);
				deviceTimestampOrdered.remove(j);
				deviceTimestampOrdered.add(0, device);
				return true;
			}
		}
		return false;
	}

	// schedule device and sort it in the linked list according to its timestamp
	private void scheduleDevice(Device device) {
		//System.out.println("Device " + device.getId() + " is scheduable");
		device.schedule(scheduler);

		// sort into the correct position after scheduling
		if (deviceTimestampOrdered.get(1).getLocalTimestamp() < device.getLocalTimestamp()) {
			int insertionPosition = 1;
			for (int j = 2; j < deviceTimestampOrdered.size(); j++) {
				if (deviceTimestampOrdered.get(j).getLocalTimestamp() < device.getLocalTimestamp()) {
					insertionPosition++;
				} else {
					break;
				}
			}
			deviceTimestampOrdered.removeFirst();
			deviceTimestampOrdered.add(insertionPosition, device);
		}
		//Util.printTimestamps(deviceTimestampOrdered);
	}

	// returns true if a device is found
	private boolean searchReceivingDataTimestamps(int position) {
	
		double minNextScheduleEventTime = deviceTimestampOrdered.get(position).getLocalTimestamp();
		int devicePosition = -1;
		// find minimum for next receive data message
		// receivePast data mit Minimum, Trick evtl. nur den key übergeben und
		// den kleinsten receiven (wenn man schon weiß was der kleinste ist)
		for (int r = 0; r <= position; r++) {
			double timestamp = deviceTimestampOrdered.get(r).getNextReceiveMessageTimestamp();

			if (timestamp <= minNextScheduleEventTime) {
				minNextScheduleEventTime = timestamp;
				devicePosition = r;
			}
			// found a device where the receiving of a message is bigger than
			// the local timestamps of all devices
		}

		if (devicePosition != -1) {
			//do not change anything as we just have to receive the data
			if(minNextScheduleEventTime < deviceTimestampOrdered.get(devicePosition).getLocalTimestamp()){
				deviceTimestampOrdered.get(devicePosition).receivePastData();
			}else{
			for (int n = 0; n < position; n++) {
				deviceTimestampOrdered.get(n).setLocalTimestamp(minNextScheduleEventTime);
			}
			Device device1 = deviceTimestampOrdered.get(devicePosition);
			device1.receivePastData();
			deviceTimestampOrdered.remove(devicePosition);
			deviceTimestampOrdered.add(0, device1);
			}
			return true;
		} else {
			// no device found
			return false;
		}
	}

	// try to find another receiving data packet
	private boolean receivingDataLargerThanAllLocalTimestamps() {
		// check if there will messages received in the future
		double minNextMessageTime = Double.MAX_VALUE;
		int devicePosition = -1;
		for (int r = 0; r < deviceTimestampOrdered.size(); r++) {
			double timestamp = deviceTimestampOrdered.get(r).getNextReceiveMessageTimestamp();
			if (timestamp < minNextMessageTime) {
				devicePosition = r;
				minNextMessageTime = timestamp;
			}
		}
		// found a device where the receiving of a message is bigger than the
		// local timestamps of all devices
		if (devicePosition != -1) {
			for (int n = 0; n < deviceTimestampOrdered.size(); n++) {
				deviceTimestampOrdered.get(n).setLocalTimestamp(minNextMessageTime);
			}
			Device device1 = deviceTimestampOrdered.get(devicePosition);
			device1.receivePastData();
			deviceTimestampOrdered.remove(devicePosition);
			deviceTimestampOrdered.add(0, device1);
			return true;
		} else {
			return false;
		}
	}
	
	private void orderedListValidator(){
		 Iterator<Device> it = deviceTimestampOrdered.iterator();
		 double timestampLowerDevice = it.next().getLocalTimestamp();
		 
		    while(it.hasNext()){
		    double timestamp = it.next().getLocalTimestamp();
		       if(timestamp < timestampLowerDevice){
		    	   System.err.println("Violated");
		       }
		       timestampLowerDevice = timestamp;
		    }
		    
	}
}
