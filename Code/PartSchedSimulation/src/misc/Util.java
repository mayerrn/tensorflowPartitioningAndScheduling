package misc;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import Device.Device;

public class Util {

	public static Set<Integer> stringToNodeSet(String input) {
		Set<Integer> nodes = new HashSet<Integer>();
		if (!input.equals("")) {
			String[] nodeIds = input.split(Config.ARRAY_SEPERATOR);
			for (int i = 0; i < nodeIds.length; i++) {
				nodes.add(Integer.parseInt(nodeIds[i]));
			}
		}
		return nodes;
	}

	public static void printGraphVisualisation(HashMap<Integer, Device> devices) {
		StringBuilder builder = new StringBuilder();
		builder.append("digraph {\nrankdir=LR;\nsplines=line;\n");
		for (int i = 0; i < devices.size(); i++) {
			Device device = devices.get(i);
			builder.append(device.graphVisualisation());
		}
		builder.append("}");
		System.out.println(builder.toString());
	}

	public static void printTimestamps(LinkedList<Device> devices) {
		for (int i = 0; i < devices.size(); i++) {
			System.out.println("Device " + devices.get(i).getId() + " : " + devices.get(i).getLocalTimestamp());
		}
	}

	public static String currentTime() {
		DateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
		Calendar cal = Calendar.getInstance();
		return dateFormat.format(cal.getTime());
	}
	
	public static String arrayToString(double[] arr){
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < arr.length; i++) {
			if(i != arr.length -1){
			builder.append(arr[i] + ";");
			}else{
				builder.append(arr[i]);
			}
		}
		return builder.toString();
	}
	
	public static String arrayToString(int[] arr){
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < arr.length; i++) {
			if(i != arr.length -1){
			builder.append(arr[i] + ";");
			}else{
				builder.append(arr[i]);
			}
		}
		return builder.toString();
	}
	
	
}
