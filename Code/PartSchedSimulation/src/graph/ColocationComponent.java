package graph;

import java.util.HashSet;

import Device.DeviceConstraint;

public class ColocationComponent implements ConstraintInterface {

	private int id;
	private HashSet<Node> nodes = new HashSet<Node>();
	private int totalRamDemand = -1;
	private DeviceConstraint constraint = DeviceConstraint.NO;
	private int numberOfOperations = 0; 

	ColocationComponent(int id) {
		this.id = id;
	}

	// TODO evtl irgendwann schlauere merge Technik: z.B immer kleinere in
	// größere mergen, dabei sumOfOutput nodes und (RAM usage vereinigen: oft noch nicht berechnet)
	public void merge(ColocationComponent component) {
		for (Node node : component.nodes) {
			this.addNode(node);
		}
	}

	public void addNode(Node node) {
		node.setColocationId(this.id);
		nodes.add(node);
		//problem is that at the current nodes not all incoming and outgoing nodes are determined at this step
		//totalRamDemand += node.getTotalRamDemand();
		numberOfOperations += node.getNumberOfOperations();
		if (node.getDeviceConstraint() != DeviceConstraint.NO) {
			if (this.constraint == DeviceConstraint.NO) {
				this.constraint = node.getDeviceConstraint();
			} else if (this.constraint != node.getDeviceConstraint()) {
				try {
					System.err.println(node.getId());
					throw new AmbigiousColocationConstraint("Colocation constraints can not be fulfilled");
				} catch (AmbigiousColocationConstraint e) {
					e.printStackTrace();
				}
			}
		}
	}

	public HashSet<Node> getNodes() {
		return this.nodes;
	}

	public int getId() {
		return this.id;
	}

	@Override
	public int getTotalRamDemand() {
		if(this.totalRamDemand == -1){
			totalRamDemand = 0;
			for(Node node: this.getNodes()){
				this.totalRamDemand += node.getTotalRamDemand();
			}
		}
		return this.totalRamDemand;
	}
	

	@Override
	public DeviceConstraint getDeviceConstraint() {
		return this.constraint;
	}

	public class AmbigiousColocationConstraint extends Exception {
		private static final long serialVersionUID = 1L;

		public AmbigiousColocationConstraint(String message) {
			super(message);
		}
	}

	@Override
	public int getNumberOfOperations() {
		return this.numberOfOperations;
	}



}
