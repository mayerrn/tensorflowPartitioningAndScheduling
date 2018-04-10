package device_generator;

import graph_generator.GraphConfig;
import graph_generator.Util;

public class Device {
	private int id;
	private int availableRAM;
	private DeviceType type;
	private int numberOfOperations;
	private String communicationSpeed;
	
	public Device(int id, int numberOfDevices){
		this.id = id;
		if(Math.random()< DeviceConfig.CPU_PROBAPILITY){
			this.type = DeviceType.CPU;
		}else{
			this.type = DeviceType.GPU;
		}
		this.numberOfOperations = Util.intFromRange(DeviceConfig.MIN_OPS, DeviceConfig.MAX_OPS);
		
		double speedup = (double) this.numberOfOperations/(double) DeviceConfig.MIN_OPS;
		//this.availableRAM = Util.intFromRange(DeviceConfig.MIN_DEVICE_RAM, DeviceConfig.MAX_DEVICE_RAM);
		this.availableRAM = (int) ((double) DeviceConfig.MAX_DEVICE_RAM/speedup);
		this.calculateCommunicationSpeed(numberOfDevices);
	}

	public int getId() {
		return id;
	}

	public int getAvailableRAM() {
		return availableRAM;
	}

	public DeviceType getType() {
		return type;
	}

	public int getNumberOfOperations() {
		return numberOfOperations;
	}
	
	private void calculateCommunicationSpeed(int numberOfDevices){
		StringBuilder builder = new StringBuilder();
		/*for(int i = 0; i < this.id; i++){
			builder.append(Util.intFromRange(DeviceConfig.MIN_COMMUNICATION_SPEED, DeviceConfig.MAX_COMMUNICATION_SPEED));
			if(i!= this.id -1){
			builder.append(GraphConfig.ARRAY_SEPERATOR);
			}
		}*/
		for(int i = this.id +1 ; i < numberOfDevices; i++){
			builder.append(Util.intFromRange(DeviceConfig.MIN_COMMUNICATION_SPEED, DeviceConfig.MAX_COMMUNICATION_SPEED));
			if(i!= numberOfDevices -1){
			builder.append(GraphConfig.ARRAY_SEPERATOR);
			}
		}
		this.communicationSpeed = builder.toString();
	}
	
	public String toString(){
		StringBuilder builder = new StringBuilder();
		builder.append(this.getId());
		builder.append(GraphConfig.CATEGORY_SEPERATOR);
		builder.append(this.getAvailableRAM());
		builder.append(GraphConfig.CATEGORY_SEPERATOR);
		builder.append(this.getNumberOfOperations());
		builder.append(GraphConfig.CATEGORY_SEPERATOR);
		builder.append(this.communicationSpeed);
		builder.append(GraphConfig.CATEGORY_SEPERATOR);
		builder.append(this.getType());
		return builder.toString();
	}
}
