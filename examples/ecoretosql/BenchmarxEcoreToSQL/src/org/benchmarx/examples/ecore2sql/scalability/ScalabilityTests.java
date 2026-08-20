package org.benchmarx.examples.ecore2sql.scalability;

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
import org.benchmarx.ecore.core.EcoreComparator;
import org.benchmarx.examples.ecore2sql.testsuite.BenchmarxUtilForEcoreToSQL;
import org.benchmarx.examples.ecore2sql.testsuite.Decisions;
import org.benchmarx.examples.ecore2sql.testsuite.EcoreToSQLTestCase;
import org.benchmarx.sql.core.SQLComparator;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcorePackage;

import sql.Schema;
import sql.SqlPackage;

public abstract class ScalabilityTests extends EcoreToSQLTestCase {
	private static final String DELIMITER = "\n";
	protected static final int REPEAT = 1;
	protected static final int TIMEOUT = 120; // seconds (temp: MediniQVT comparison run)
	private static final String resultFolder = "scalability_results";

	protected static Map<String, Map<Integer, Double>> results;
	protected static java.util.Set<String> timedOut;
	protected static String label;

	@Override
	public void initialise() {
		Logger.getRootLogger().setLevel(Level.INFO);

		// Make sure packages are registered
		EcorePackage.eINSTANCE.getName();
		SqlPackage.eINSTANCE.getName();

		// Initialise all helpers
		ecoreComparator = new EcoreComparator();
		sqlComparator = new SQLComparator();
		util = new BenchmarxUtilForEcoreToSQL(tool);

		// we overwrite the super method to avoid initialising the synchronisationDialog
		// this happens within each test

		helperEcore = createAndInitialiseHelperEcore(() -> tool.getSourceModel(), () -> sourceEdit);
		helperSQL = createAndInitialiseHelperSQL(() -> tool.getTargetModel(), () -> targetEdit);
	}

	@Override
	public void terminate() {
		// we overwrite the super method to avoid terminating the synchronisationDialog
		// this happens within each test
	}

	protected static void recordResult(BXTool<EPackage, Schema, Decisions> tool, int size, double timeInS)
			throws FileNotFoundException {
		results.computeIfAbsent(tool.getName(), k -> new HashMap<>()).put(size, timeInS);
		saveResults(tool);
	}

	private static void saveResults(BXTool<EPackage, Schema, Decisions> tool)
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

	protected ScalabilityTests(String l) {
		super();
		label = l;
	}

	protected ScalabilityTests() {
		super();
	}

	public static Collection<BXTool<EPackage, Schema, Decisions>> tools() {
		return EcoreToSQLTestCase.tools();
	}
}
