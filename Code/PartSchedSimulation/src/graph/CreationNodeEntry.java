package graph;

import java.util.HashSet;

import Device.DeviceConstraint;
import misc.Config;
import misc.Util;

public class CreationNodeEntry {
	
	String[] components;
	
	public CreationNodeEntry(String csvLine){
		components = csvLine.split(Config.CATEGORY_SEPERATOR);
	}
	
	public int getId(){
		return Integer.parseInt(components[0]);
	}
	public HashSet<Integer> getOutgoingNodes(){
		return (HashSet<Integer>) Util.stringToNodeSet(components[1]);
	}
	public HashSet<Integer> getIncomingNodes(){
		return (HashSet<Integer>) Util.stringToNodeSet(components[2]);
	}
	public HashSet<Integer> getColocationNodes(){
		return (HashSet<Integer>) Util.stringToNodeSet(components[3]);
	}
	public int getOutgoingTensorSize(){
		return Integer.parseInt(components[4]);
	}
	public int getNumberOfOperations(){
		return Integer.parseInt(components[5]);
	}
	public int getRamUsage(){
		return Integer.parseInt(components[6]);
	}
	public DeviceConstraint getConstraint(){
		return DeviceConstraint.valueOf(components[7]);
	}
}
