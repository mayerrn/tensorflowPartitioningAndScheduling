package Partitioning;


public class ScheduledTimeSlots implements Comparable<ScheduledTimeSlots>{
	private double startTime;
	private double endTime;
	private int deviceId ;

	public ScheduledTimeSlots(double startTime, double endTime, int deviceId){
		this.startTime = startTime;
		this.endTime = endTime;
		this.deviceId = deviceId;
	}

	public double getStartTime() {
		return startTime;
	}

	public double getEndTime() {
		return endTime;
	}
	public int getDeviceId() {
		return deviceId;
	}
	
	public int compareTo(ScheduledTimeSlots o) {
		if(this.startTime - o.startTime < 0){
			return -1;
		}else if (this.startTime - o.startTime == 0){
			return 0;
		}else{
			return 1;
		}
	}

	
}
