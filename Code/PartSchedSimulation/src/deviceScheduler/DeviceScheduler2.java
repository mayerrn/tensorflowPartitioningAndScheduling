package deviceScheduler;

import java.util.Collection;
import java.util.LinkedList;

import Device.Device;
import evaluation.Evaluation;
import misc.Util;
import scheduler.Scheduler;


//alternative for DeviceScheduler to compare resutls
public class DeviceScheduler2 {
	private Evaluation evaluation;
	private Scheduler scheduler;
	Collection<Device> devices;

	public DeviceScheduler2(Collection<Device> devices, Evaluation evaluation, Scheduler scheduler) {
		if (devices.size() < 2) {
			System.err
					.println("Please use at least 2 devices. If not scheduling and partitioning does not make sense.");
		}
		System.out.println("DeviceScheduler2");
		this.evaluation = evaluation;
		this.scheduler = scheduler;
		this.devices = devices;
	}
	
	public void execute() {

		execution: while (true) {
			Device lowestTimestampDevice = null;
			Device secondTimestampDevice = null;
			
			for(Device device: devices){
				if(lowestTimestampDevice == null){
					lowestTimestampDevice = device;
				}else{
					if(secondTimestampDevice == null){
						if(device.getLocalTimestamp() < lowestTimestampDevice.getLocalTimestamp()){
							secondTimestampDevice = lowestTimestampDevice;
						}else if(device.getLocalTimestamp() > lowestTimestampDevice.getLocalTimestamp()){
							secondTimestampDevice = device;
						}
					}
					
					if(device.getLocalTimestamp() < lowestTimestampDevice.getLocalTimestamp()){
						secondTimestampDevice = lowestTimestampDevice;
						lowestTimestampDevice = device;
					}
				}
			}
			//später vllt direkt in isSchedualbe aufrufen
			lowestTimestampDevice.receivePastData();
			if(lowestTimestampDevice.isScheduable()){
				lowestTimestampDevice.schedule(scheduler);
			}else{
							
				if(secondTimestampDevice != null){
					//man muss noch checken ob eins der gleiche großen schedualbe ist
					boolean foundScheduableDevice = false;
					for(Device device: devices){
						device.receivePastData();
						if(device.isScheduable() && device.getLocalTimestamp() == lowestTimestampDevice.getLocalTimestamp()){
							device.schedule(scheduler);
							foundScheduableDevice = true;
							break;
						}
					}
					if(!foundScheduableDevice){
						double minTimestamp = Math.min(lowestTimestampDevice.getNextReceiveMessageTimestamp(), secondTimestampDevice.getLocalTimestamp());
						lowestTimestampDevice.setLocalTimestamp(minTimestamp);
					}		
				
				}else{
					//alle haben den gleichen Zeitstempel
					//versuche eins zu ifnden das schedualbe ist.
					boolean foundScheduableDevice = false;
					for(Device device: devices){
						device.receivePastData();
						if(device.isScheduable()){
							device.schedule(scheduler);
							foundScheduableDevice = true;
							break;
						}
					}
					
					if(!foundScheduableDevice){
						//später nicht immer getNextReceiveMessageTimestamp aufrufen
						Device minReceiveTimeDevice = null;
						for(Device device: devices){
							if(minReceiveTimeDevice == null){
								if(device.getNextReceiveMessageTimestamp() < Double.MAX_VALUE){
								minReceiveTimeDevice = device;
								}
							}else{
								if(device.getNextReceiveMessageTimestamp() < minReceiveTimeDevice.getNextReceiveMessageTimestamp()){
									minReceiveTimeDevice = device;
								}
							}
						}
						if(minReceiveTimeDevice != null){
							minReceiveTimeDevice.setLocalTimestamp(minReceiveTimeDevice.getNextReceiveMessageTimestamp());
						}else{
							System.out.println(evaluation.getSchedules());
							//all devices have the same timestamp
							evaluation.setExecutionTime(devices.iterator().next().getLocalTimestamp());
							Util.printTimestamps(new LinkedList<Device>(devices));
							break execution;
						}
					}
				}
			}
			
		}
	}
}
