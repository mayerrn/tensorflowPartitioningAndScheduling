package evaluation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

import Device.Device;
import misc.Util;

public class Evaluation {

	private int communicationCost = 0;
	private int schedules = 0;
	private int sendSchedules = 0;
	private int receiveSchedules = 0;
	private double executionTime = 0;

	private long startTimePartitioning;
	private long startTimeScheduling;
	private long endTimeScheduling;

	private double schedulingExecutionTime;
	private double partitioningExecutionTime;

	// for repeating
	private HashMap<Integer, Device> devices;
	private int numberOfNodes;

	public void print() {
		System.out.println("");
		System.out.println("Evaluation: ");
		System.out.println("Execution time: " + executionTime);
		System.out.println("Communication cost: " + communicationCost);
		System.out.println("Scheduled nodes (without send and receive): " + schedules);
		System.out.println("Scheduled nodes (send): " + sendSchedules);
		System.out.println("Scheduled nodes (receive): " + receiveSchedules);

		System.out.println("");
	}

	// Graph, Partitioning, Scheduling, execution time, communication cost,
	// graph size, #devices
	public void save(String filePath, String graph, String partitioning, String scheduling, int graphSize,
			int deviceSize, double stdExecutionTime, double stdCommunicationCost, double stdPartitioningExecutionTime,
			double stdSchedulingExecutionTime) {
		File file = new File(filePath);
		// if file doesnt exists, then create it
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				System.err.println(e.toString());
			}
		}

		FileWriter fw;
		String csvSeperator = ",";
		try {
			fw = new FileWriter(file.getAbsoluteFile(), true);
			BufferedWriter bw = new BufferedWriter(fw);
			StringBuilder builder = new StringBuilder();
			builder.append(graph);
			builder.append(csvSeperator);
			builder.append(partitioning);
			builder.append(csvSeperator);
			builder.append(scheduling);
			builder.append(csvSeperator);
			builder.append(executionTime);
			builder.append(csvSeperator);
			builder.append(communicationCost);
			builder.append(csvSeperator);
			builder.append(graphSize);
			builder.append(csvSeperator);
			builder.append(deviceSize);
			builder.append(csvSeperator);
			builder.append(stdExecutionTime);
			builder.append(csvSeperator);
			builder.append(stdCommunicationCost);
			builder.append(csvSeperator);
			builder.append(this.partitioningExecutionTime);
			builder.append(csvSeperator);
			builder.append(stdPartitioningExecutionTime);
			builder.append(csvSeperator);
			builder.append(this.schedulingExecutionTime);
			builder.append(csvSeperator);
			builder.append(stdSchedulingExecutionTime);

			bw.write(builder.toString() + "\n");
			bw.close();
			fw.close();
		} catch (IOException e) {
			System.err.println(e.toString());
		}

	}

	public void saveParameterEvaluation(String filePath, String graph, String partitioning, String scheduling,
			HashMap<Integer, Device> devices, EvaluationParameter parameter) {
		File file = new File(filePath);
		// if file doesnt exists, then create it
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				System.err.println(e.toString());
			}
		}

		// Graph,Partitioning,Scheduling,Device number, [DeviceId,
		// schedulingStartTime, executionTime]^n
		FileWriter fw;
		String csvSeperator = ",";
		try {
			fw = new FileWriter(file.getAbsoluteFile(), true);
			BufferedWriter bw = new BufferedWriter(fw);
			StringBuilder builder = new StringBuilder();
			builder.append(graph);
			builder.append(csvSeperator);
			builder.append(partitioning);
			builder.append(csvSeperator);
			builder.append(scheduling);
			builder.append(csvSeperator);
			builder.append(devices.size());

			for (Device device : devices.values()) {
				builder.append(csvSeperator);
				builder.append(device.getId());
				if (parameter == EvaluationParameter.Utilization) {
					builder.append(csvSeperator);
					builder.append(Util.arrayToString(device.getEvaluation().getStartTimeScheduling()));
					builder.append(csvSeperator);
					builder.append(Util.arrayToString(device.getEvaluation().getExecutionDuration()));
				} else if (parameter == EvaluationParameter.SchedulingPossibilities) {
					builder.append(csvSeperator);
					builder.append(Util.arrayToString(device.getEvaluation().getSchedulingPossibilities()));
				} else if (parameter == EvaluationParameter.RamUsage) {
					builder.append(csvSeperator);
					builder.append(Util.arrayToString(device.getEvaluation().getRamUsage()));
				} else {
					System.err.println("Wrong evaluation type");
				}
			}
			bw.write(builder.toString() + "\n");
			bw.close();
			fw.close();
		} catch (IOException e) {
			System.err.println(e.toString());
		}
	}

	public void saveNodeDeviceDistribution(String directory, String fileName) {
		File dir = new File(directory);
		dir.mkdirs();

		File file = new File(directory + "/"+ fileName);
		// if file doesnt exists, then create it
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				System.err.println(e.toString());
			}
		}

		// Graph,Partitioning,Scheduling,Device number, [DeviceId,
		// schedulingStartTime, executionTime]^n
		FileWriter fw;
		try {
			fw = new FileWriter(file.getAbsoluteFile(), true);
			BufferedWriter bw = new BufferedWriter(fw);
			StringBuilder builder = new StringBuilder();

			for (Device device : this.devices.values()) {
				builder.append(device.getGraph().getNodes().size());
				builder.append(";");
				builder.append(device.getLastNodeFinishedTimestamp());
				builder.append(";");
				builder.append(device.getSumOfOperationsOfAlreadyAssignedNodes());
				builder.append("\n");
			}
			bw.write(builder.toString() + "\n");
			bw.close();
			fw.close();
		} catch (Exception e) {
			System.err.println(e.toString());
		}
	}

	public int getCommunicationCost() {
		return communicationCost;
	}

	public void setCommunicationCost(double communicationCost) {
		this.communicationCost = (int) communicationCost;
	}

	public void addCommunicationCost(int communicationCost) {
		this.communicationCost += communicationCost;
	}

	public void increaseSchedulingCounter() {
		schedules++;
	}

	public int getSchedules() {
		return this.schedules;
	}

	public double getExecutionTime() {
		return executionTime;
	}

	public void setExecutionTime(double executionTime) {
		this.executionTime = executionTime;
	}

	// execution time in real time
	public long getSchedulingExecutionTime() {
		return this.endTimeScheduling - this.startTimeScheduling;
	}

	public long getPartitioningExecutionTime() {
		return this.startTimeScheduling - this.startTimePartitioning;
	}

	public void setSchedulingExecutionTime(double schedulingExecutionTime) {
		this.schedulingExecutionTime = schedulingExecutionTime;
	}

	public void setPartitioningExecutionTime(double partitioningExecutionTime) {
		this.partitioningExecutionTime = partitioningExecutionTime;
	}

	public int getNumberOfNodes() {
		return this.numberOfNodes;
	}

	public HashMap<Integer, Device> getDevices() {
		return this.devices;
	}

	public void setNumberOfNodes(int numberOfNodes) {
		this.numberOfNodes = numberOfNodes;
	}

	public void setDevices(HashMap<Integer, Device> devices) {
		this.devices = devices;
	}

	public int getSendSchedules() {
		return sendSchedules;
	}

	public int getReceiveSchedules() {
		return receiveSchedules;
	}

	public void increaseSendSchedules() {
		sendSchedules++;
	}

	public void increaseReceiveSchedules() {
		receiveSchedules++;
	}

	public void setStartTimePartitioning(long startTimePartitioning) {
		this.startTimePartitioning = startTimePartitioning;
	}

	public void setStartTimeScheduling(long startTimeScheduling) {
		this.startTimeScheduling = startTimeScheduling;
	}

	public void setEndTimeScheduling(long endTimeScheduling) {
		this.endTimeScheduling = endTimeScheduling;
	}

}
