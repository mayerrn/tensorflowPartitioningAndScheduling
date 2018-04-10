package graph;
import Device.DeviceConstraint;

public interface ConstraintInterface {
	public int getTotalRamDemand();
	public DeviceConstraint getDeviceConstraint();
	public int getNumberOfOperations();

}
