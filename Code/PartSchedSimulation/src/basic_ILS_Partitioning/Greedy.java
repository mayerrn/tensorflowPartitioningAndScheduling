package basic_ILS_Partitioning;

import graph.Node;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import Device.Device;

/**
 * The main idea of the Greedy Local Search strategy is to
 * find and reassign vertices that can be placed on another
 * machine while reducing overall costs. This replacement
 * is repeated until no improvement can be found.
 * @author mayercn
 *
 */
public class Greedy implements LocalSearch {
	
	private Collection<Node> candidates = null;

	@Override
	public State generateSolution(State startState) {
		State state = startState.copy();
		boolean converged = false;
		while (!converged) {
			converged = true;
			double minCostDelta = Integer.MAX_VALUE;
			Node bestNode = null;
			Device bestDevice = null;
			if (candidates == null || candidates.isEmpty()) {
				candidates = this.getCandidates(state);
			}
//			System.out.println("Candidates: " + candidates);
			Iterator<Node> iter = candidates.iterator();
			while (iter.hasNext()) {
				Node node = iter.next();
				List<Device> deviceCandidates = this.getDeviceCandidates(node, state);
				boolean removeCandidate = true;
				for (Device device : deviceCandidates) {
					double c = state.getCostDelta(node,device);
					if (c<0) {
						removeCandidate = false;
					}
					if (c<minCostDelta) {
						minCostDelta = c;
						bestNode = node;
						bestDevice = device;
//						System.out.println("-");
//						System.out.println("New best node found!");
//						System.out.println("Best node: " + bestNode);
//						System.out.println("Best device: " + bestDevice);
//						System.out.println("Best costs delta: " + minCostDelta);
//						System.out.println();
					}
				}
				if (removeCandidate) {
					iter.remove();
				}
			}
			if (bestNode!=null && minCostDelta<0) {
				state.move(bestNode, bestDevice);
				converged = false;
//				System.out.println("State: " + state.toString());
			}
		}
		return state;
	}

	/**
	 * For a certain node gets the list of devices it could
	 * be assigned to.
	 * @param node
	 * @return
	 */
	private List<Device> getDeviceCandidates(Node node, State state) {
		List<Device> candidates = new ArrayList<Device>();
		for (Device device : Basic_ILS.devices) {
			if (state.deviceConstraint(node, device)
					&& state.colocationConstraint(node, device)
					&& state.memoryConstraint(node, device)) {
				candidates.add(device);
			}
		}
		return candidates;
	}

	/**
	 * Returns the vertices to be tested for potential placement improvements.
	 * In this function, all vertices are returned.
	 * @param state
	 * @return
	 */
	private Collection<Node> getCandidates(State state) {
		return new ArrayList<Node>(Basic_ILS.graph.getNodes());
	}


}
