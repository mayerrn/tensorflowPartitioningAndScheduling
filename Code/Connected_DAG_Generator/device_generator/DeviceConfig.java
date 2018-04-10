package device_generator;

public class DeviceConfig {
	static int NUMBER_OF_DEVICES = 50;
	static int MIN_OPS = 10;
	static int MAX_OPS = 100;	
	
	//static int MIN_DEVICE_RAM = 1000000;
	//1500000000
	static int MAX_DEVICE_RAM = 100000000;

	static int MIN_COMMUNICATION_SPEED = 10;
	static int MAX_COMMUNICATION_SPEED = 60;
	static float CPU_PROBAPILITY = (float) 0.6;
	static String DEVICE_PATH = "../../Sample_Data/Device/";

	
	static String getString(){
		StringBuilder builder = new StringBuilder();
		builder.append("-");
		builder.append(NUMBER_OF_DEVICES);
		builder.append("-");
		builder.append(MIN_OPS);
		builder.append("-");
		builder.append(MAX_OPS);
		builder.append("-");
		builder.append(MAX_DEVICE_RAM);
		builder.append("-");
		builder.append(MIN_COMMUNICATION_SPEED);
		builder.append("-");
		builder.append(MAX_COMMUNICATION_SPEED);
		builder.append("-");
		builder.append(CPU_PROBAPILITY);
		return builder.toString();
	}
}
