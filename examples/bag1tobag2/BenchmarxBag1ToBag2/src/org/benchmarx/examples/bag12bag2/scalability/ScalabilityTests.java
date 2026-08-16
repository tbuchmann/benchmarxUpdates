package org.benchmarx.examples.bag12bag2.scalability;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.benchmarx.BXTool;
import org.benchmarx.bags1.core.Bag1Comparator;
import org.benchmarx.bags2.core.Bag2Comparator;
import org.benchmarx.examples.bag12bag2.testsuite.Bag12Bag2TestCase;
import org.benchmarx.examples.bag12bag2.testsuite.Decisions;
import org.benchmarx.util.BenchmarxUtil;

import bags1.Bags1Package;
import bags1.MyBag;
import bags2.Bags2Package;

public abstract class ScalabilityTests extends Bag12Bag2TestCase {
	private static final String DELIMITER = "\n";
	protected static final int REPEAT = 1;
	protected static final int TIMEOUT = 180; // seconds
	private static final String resultFolder = "scalability_results";

	protected static Map<String, Map<Integer, Double>> results;
	protected static java.util.Set<String> timedOut;
	protected static String label;

	@Override
	public void initialise() {
		Logger.getRootLogger().setLevel(Level.INFO);

		// Make sure packages are registered
		Bags1Package.eINSTANCE.getBags1Factory();
		Bags2Package.eINSTANCE.getBags2Factory();

		// Initialise all helpers
		bag1Comparator = new Bag1Comparator();
		bag2Comparator = new Bag2Comparator();
		util = new BenchmarxUtil<>(tool);

		// we overwrite the super method to avoid initialising the synchronisationDialog
		// this happens within each test

		helperBag1 = createAndInitialiseHelperBag1(() -> tool.getSourceModel(), () -> sourceEdit);
		helperBag2 = createAndInitialiseHelperBag2(() -> tool.getTargetModel(), () -> targetEdit);
	}

	@Override
	public void terminate() {
		// we overwrite the super method to avoid terminating the synchronisationDialog
		// this happens within each test
	}

	protected static void recordResult(BXTool<MyBag, bags2.MyBag, Decisions> tool, int size, double timeInS)
			throws FileNotFoundException {
		results.computeIfAbsent(tool.getName(), k -> new HashMap<>()).put(size, timeInS);
		saveResults(tool);
	}

	private static void saveResults(BXTool<MyBag, bags2.MyBag, Decisions> tool)
			throws FileNotFoundException {
		Map<Integer, Double> perTool = results.get(tool.getName());
		if (perTool == null || perTool.isEmpty())
			return;

		new File(resultFolder).mkdirs();
		try (PrintWriter out = new PrintWriter(resultFolder + "/" + label + tool.getName() + ".txt")) {
			out.println(perTool.keySet().stream()//
					.sorted()//
					.map(k -> k + ", " + perTool.get(k))//
					.collect(Collectors.joining(DELIMITER)));
		}
	}

	public static void initResults() {
		results = new HashMap<>();
		timedOut = new java.util.HashSet<>();
	}

	public ScalabilityTests(BXTool<MyBag, bags2.MyBag, Decisions> tool, String l) {
		super(tool);
		label = l;
	}

	protected ScalabilityTests(String l) {
		super();
		label = l;
	}

	protected ScalabilityTests() {
		super();
	}

	public static Collection<BXTool<MyBag, bags2.MyBag, Decisions>> tools() {
		return Bag12Bag2TestCase.tools();
	}
}
