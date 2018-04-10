package basic_ILS_Partitioning;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import optimalPartitioning.SearchSpaceState;
import Device.Device;
import Partitioning.PartitioningStrategy;
import graph.ColocationComponent;
import graph.ConstraintInterface;
import graph.CreationGraph;
import graph.Node;

public class Basic_ILS extends PartitioningStrategy {

	public static Collection<Device> devices;
	public static LocalSearch localSearch;
	public static CreationGraph graph;
	
	private int i = 0;
	private History history;
	
	@Override
	public void partition(CreationGraph graph, HashMap<Integer, Device> devices) {
		
//		System.out.println("Graph: " + graph.graphVisualisation());
		init(graph, devices);
		
		State s_0 = generateInitialSolution();
		State s_star = localSearch(s_0);
		do {
			State s_tick = pertubation(s_star, history);
			State s_star_tick = localSearch(s_tick);
			s_star = acceptanceCriterion(s_star,s_star_tick,history);
		} while (!terminated(10));
		
		flush(s_star, graph, devices);
	}

	private void init(CreationGraph graph, HashMap<Integer, Device> devices) {
		Basic_ILS.devices = devices.values();
		Basic_ILS.localSearch = new Greedy();
		Basic_ILS.graph = graph;
	}

	/**
	 * First, we should swap assignments between devices as the local search might be stuck
	 * in local optimum.
	 * @param s_star
	 * @param history
	 * @return
	 */
	private State pertubation(State s_star, History history) {
		swapAssignments(s_star);
		return s_star;
	}

	// TODO: pertubation
	private void swapAssignments(State s_star) {
//		double rand = Math.random();
//		HashMap<Integer,Double> pro
	}

	/**
	 * Take state with minimal costs.
	 * @param s_star
	 * @param s_star_tick
	 * @param history
	 * @return
	 */
	private State acceptanceCriterion(State s_star, State s_star_tick,
			History history) {
		if (s_star_tick.getCosts()<s_star.getCosts()) {
			return s_star_tick;
		}
		return s_star;
	}

	private boolean terminated(int maxIter) {
		i++;
		if (i==maxIter) {
			return true;
		}
		return false;
	}

	private State localSearch(State s_0) {
		return localSearch.generateSolution(s_0);
	}

	private State generateInitialSolution() {
		return State.getRandomState();
	}
	
	/**
	 * Performs the actual partitioning (finalize)
	 * @param bestState
	 * @param graph
	 * @param devices
	 */
	private void flush(State state, CreationGraph graph, HashMap<Integer, Device> devices) {
		Collection<Node> nodes = new ArrayList<Node>(graph.getNodes());
//		System.out.println(".sjdflsjfd");
		for (Node node : nodes) {
//			System.out.println(state.getDevice(node));
			assignNode(node, state.getDevice(node).getId(), devices, graph);
		}
	}

}
