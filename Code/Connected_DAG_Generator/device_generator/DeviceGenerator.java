package device_generator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import graph_generator.Util;

public class DeviceGenerator {
	public static void main(String[] args){
		
		//communication cost between devices!!
		ArrayList<Device> devices = new ArrayList<Device>();
		for(int i = 0; i< DeviceConfig.NUMBER_OF_DEVICES; i++){
			Device device = new Device(i, DeviceConfig.NUMBER_OF_DEVICES);
			devices.add(device);
		}
		write(devices);
	}
	
	public static void write(ArrayList<Device> devices){
		try{
		File file = new File(DeviceConfig.DEVICE_PATH + Util.currentTime() + DeviceConfig.getString() + ".csv");
		// if file doesnt exists, then create it
		if (!file.exists()) {
			file.createNewFile();
		}
		FileWriter fw = new FileWriter(file.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write("Id,RAM,#operations,commnication speed,type\n");
		for(Device device: devices){
			bw.write(device.toString());
			bw.write("\n");
		}
		bw.close();
		fw.close();
		}catch(Exception e){
			System.out.println(e.getStackTrace());
		}
	}
}
