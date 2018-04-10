package Device;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;

import evaluation.Evaluation;

public class DeviceReader {

	private FileReader fileReader;
	private BufferedReader bufferedReader;
	private Evaluation evaluation;

	HashMap<Integer, Device> devices = new HashMap<Integer, Device>();

	public DeviceReader(String devicePath, Evaluation evaluation) {
		this.evaluation = evaluation;
		
		try {
			fileReader = new FileReader(devicePath);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		bufferedReader = new BufferedReader(fileReader);
	}

	public HashMap<Integer, Device> readDevices() {
		try {
			// skip the first line (description in csv)
			String input = bufferedReader.readLine();

			while ((input = bufferedReader.readLine()) != null) {
				Device device = new Device(input, devices, evaluation);
				devices.put(device.getId(), device);
			}
			fileReader.close();

		} catch (Exception e) {
			System.err.println(e.toString());
			System.err.println("Maybe memory size is to big (e.g. 4300000000 was too big to be converted to an int)");
		}
		return this.devices;
	}

}
