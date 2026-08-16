package org.benchmarx.examples.gantt2cpm.scalability;

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
import org.benchmarx.cpm.core.CPMComparator;
import org.benchmarx.examples.gantt2cpm.testsuite.Decisions;
import org.benchmarx.examples.gantt2cpm.testsuite.GanttToCPMTestCase;
import org.benchmarx.gantt.core.GanttComparator;
import org.benchmarx.util.BenchmarxUtil;

import cpm.CPMNetwork;
import cpm.CpmPackage;
import gantt.GanttDiagram;
import gantt.GanttPackage;

public abstract class ScalabilityTests extends GanttToCPMTestCase {
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
		GanttPackage.eINSTANCE.getName();
		CpmPackage.eINSTANCE.getName();

		// Initialise all helpers
		ganttComparator = new GanttComparator();
		cpmComparator = new CPMComparator();
		util = new BenchmarxUtil<>(tool);

		// we overwrite the super method to avoid initialising the synchronisationDialog
		// this happens within each test

		helperGantt = createAndInitialiseHelperGantt(() -> tool.getSourceModel(), () -> sourceEdit);
		helperCPM = createAndInitialiseHelperCPM(() -> tool.getTargetModel(), () -> targetEdit);
	}

	@Override
	public void terminate() {
		// we overwrite the super method to avoid terminating the synchronisationDialog
		// this happens within each test
	}

	protected static void recordResult(BXTool<GanttDiagram, CPMNetwork, Decisions> tool, int size, double timeInS)
			throws FileNotFoundException {
		results.computeIfAbsent(tool.getName(), k -> new HashMap<>()).put(size, timeInS);
		saveResults(tool);
	}

	private static void saveResults(BXTool<GanttDiagram, CPMNetwork, Decisions> tool)
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

	public ScalabilityTests(BXTool<GanttDiagram, CPMNetwork, Decisions> tool, String l) {
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

	public static Collection<BXTool<GanttDiagram, CPMNetwork, Decisions>> tools() {
		return GanttToCPMTestCase.tools();
	}
}
