package optimalPartitioning;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import Device.Device;
import Partitioning.PartitioningStrategy;
import graph.ColocationComponent;
import graph.ConstraintInterface;
import graph.CreationGraph;
import graph.Node;

public class OptimalPartitioning extends PartitioningStrategy {

	private int i = 0;
	private List<Node> nodes;
	
	@Override
	public void partition(CreationGraph graph, HashMap<Integer, Device> devices) {
		
		nodes = new ArrayList<Node>(graph.getNodes());
		SearchSpaceState.devices = devices.values();
		SearchSpaceState.nodes = nodes;
		SearchSpaceState.graph = graph;
		
		SearchSpaceState start = new SearchSpaceState();
		List<SearchSpaceState> fringe = new LinkedList<SearchSpaceState>();
		fringe.add(start);
		SearchSpaceState bestState = null;
		double minCosts = Integer.MAX_VALUE;
		Random random = new Random();
		
		i = 0;
		while (!fringe.isEmpty()) {
			SearchSpaceState sss = getNextState(fringe);
//			System.out.println("Fringe: " + fringe.size() + "\tEnd? " + sss.isEndState());
//			System.out.println(sss.toString());
//			System.out.println(sss.toString() + "\t" + sss.getCosts() + "\t\t\t" + bestState + "\t\t" + minCosts);
			if (sss.isEndState()) {
				double c = sss.getCosts();
//				System.out.println("costs: " + c);
//				System.out.println();
				if (c<minCosts) {
					minCosts = c;
					bestState = sss;
					System.out.println("New best state found: " + minCosts);
					System.out.println("Best state: " + sss.toString());
					System.out.println("-----------------------");
				}
			} else {
				Node nextNode = selectNextNode(sss);
				fringe.addAll(sss.getSuccessorStates(nextNode));
			}
//			System.out.println("Fringe: " + fringe.size());
//			if (random.nextDouble()>0.9) {
				prune(fringe, minCosts);
//			}
			i++;
		}
		
		flush(bestState, graph, devices);
		
	}

	/**
	 * This method selects the next node to expand the search space.
	 * A good heuristic would probably select the most promising nodes first.
	 * @param sss 
	 * @return
	 */
	private Node selectNextNode(SearchSpaceState sss) {
		return nodes.get(sss.numberOfAssignedNodes());
	}

	/**
	 * Performs the actual partitioning (finalize)
	 * @param bestState
	 * @param graph
	 * @param devices
	 */
	private void flush(SearchSpaceState bestState, CreationGraph graph, HashMap<Integer, Device> devices) {
		
		for (Node node : nodes) {
			assignNode(node, bestState.getDevice(node), devices, graph);
		}
	}

	private void prune(List<SearchSpaceState> fringe, double minCosts) {
		Iterator<SearchSpaceState> iter = fringe.iterator();
		while (iter.hasNext()) {
			SearchSpaceState sss = iter.next();
			if (sss.getCosts()>=minCosts) {
				iter.remove();
			}
		}
	}

	/**
	 * Returns the next state to expand.
	 * TODO: should expand in a DFS manner because of pruning and memory efficiency.
	 * @param fringe
	 * @return
	 */
	private SearchSpaceState getNextState(List<SearchSpaceState> fringe) {
		List<SearchSpaceState> candidates = new LinkedList<SearchSpaceState>();
		int max = 0;
		for (SearchSpaceState sss : fringe) {
			int tmp = sss.numberOfAssignedNodes();
			if (tmp>max) {
				max = tmp;
				candidates = new LinkedList<SearchSpaceState>();
				candidates.add(sss);
				
			} else if (tmp==max) {
				candidates.add(sss);
			}
		}
		// TODO: remove candidate with lowest makespan to prune early
		SearchSpaceState next = candidates.remove(0);
		fringe.remove(next);
		return next;
	}

//	public int getDeviceIdToAssignComponentTo(ConstraintInterface component, int startValue,
//			HashMap<Integer, Device> devices) {
//		int i = startValue;
//		while (i < startValue + devices.size()) {
//			int index = i % devices.size();
//			if (devices.get(index).canAddComponent(component)) {
//				return index;
//			} else {
//				//System.err.println("Can't assign it"); //does not have to be a problem
//				i++;
//			}
//		}
//		System.err.println("Component with constraint: " + component.getDeviceConstraint() + ", memory size: "
//				+ component.getTotalRamDemand() + " could not be matched to any device");
//		return -1;
//	}

}
