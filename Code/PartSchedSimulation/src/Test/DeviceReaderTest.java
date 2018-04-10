package Test;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;

import org.junit.Test;

import Device.Device;
import Device.DeviceReader;
import evaluation.Evaluation;

public class DeviceReaderTest {

	@Test
	public void readDeviceTest(){
		Evaluation evaluation = new Evaluation();
		DeviceReader deviceReader = new DeviceReader("../../Sample_Data/Test/DeviceReaderTest.csv", evaluation);
		HashMap<Integer,Device> devices = deviceReader.readDevices();
		Device device0 = devices.get(0);
		assertEquals(device0.getCommunicationCost()[0],0);
		assertEquals(device0.getCommunicationCost()[1],184);
		assertEquals(device0.getCommunicationCost()[2],25);
		assertEquals(device0.getCommunicationCost()[3],138);		
		assertEquals(device0.getCommunicationCost()[4],119);
		
		Device device3 = devices.get(3);
		assertEquals(device3.getCommunicationCost()[0],138);
		assertEquals(device3.getCommunicationCost()[1],149);
		assertEquals(device3.getCommunicationCost()[2],105);
		assertEquals(device3.getCommunicationCost()[3],0);		
		assertEquals(device3.getCommunicationCost()[4],23);
		
		Device device4 = devices.get(4);
		assertEquals(device4.getCommunicationCost()[0],119);
		assertEquals(device4.getCommunicationCost()[1],20);
		assertEquals(device4.getCommunicationCost()[2],100);
		assertEquals(device4.getCommunicationCost()[3],23);		
		assertEquals(device4.getCommunicationCost()[4],0);
	}
}
