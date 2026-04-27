package org.benchmarx.examples.pdb12pdb2.testsuite.concurrent;

import static java.util.Map.entry;

import java.util.Collection;
import java.util.Map;

import org.benchmarx.BXTool;
import org.benchmarx.examples.pdb12pdb2.testsuite.BXToolParameterResolver;
import org.benchmarx.examples.pdb12pdb2.testsuite.Decisions;
import org.benchmarx.examples.pdb12pdb2.testsuite.Pdb12Pdb2TestCase;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(BXToolParameterResolver.class)
public class MonotonicCreating extends Pdb12Pdb2TestCase {

	public MonotonicCreating(BXTool<pdb1.Database, pdb2.Database, Decisions> tool) {
		super(tool);
	}

	public static Collection<BXTool<pdb1.Database, pdb2.Database, Decisions>> tools() {
		return Pdb12Pdb2TestCase.tools();
	}

	/**
	 * <b>Test</b> for concurrent creation of non-matching persons (different IDs)
	 * on both sides starting from an empty database. The source creates Adenauer
	 * while the target independently creates Schroeder. <br/>
	 * <b>Expect</b>: Both persons appear in both databases after synchronisation. <br/>
	 * <b>Features</b>: concurrent, add, non-matching
	 */
	@ParameterizedTest @MethodSource("tools")
	public void testNonMatchingCreate(BXTool<pdb1.Database, pdb2.Database, Decisions> tool) {
		this.tool = tool; initialise();
		tool.noPrecondition();
		tool.performAndPropagateEdit(
				srcEdit(helperPerson1::setDatabaseName, helperPerson1::createKonradAdenauer),
				trgEdit(helperPerson2::createGerhardSchroeder, helperPerson2::changeIncrementalIDs));
		util.assertPostcondition("ConcSyncNonMatchingCreatePDB1", "ConcSyncNonMatchingCreatePDB2");
		terminate();
	}

	/**
	 * <b>Test</b> for concurrent creation of the same person (matching ID "KA")
	 * on both sides starting from an empty database. Both sides create Adenauer
	 * with the same key. <br/>
	 * <b>Expect</b>: Only one person entry remains after synchronisation (entries merged). <br/>
	 * <b>Features</b>: concurrent, add, matching
	 */
	@ParameterizedTest @MethodSource("tools")
	public void testMatchingCreate(BXTool<pdb1.Database, pdb2.Database, Decisions> tool) {
		this.tool = tool; initialise();
		tool.noPrecondition();
		tool.performAndPropagateEdit(
				srcEdit(helperPerson1::setDatabaseName, helperPerson1::createKonradAdenauer),
				trgEdit(helperPerson2::createKonradAdenauer, helperPerson2::changeIncrementalIDs));
		util.assertPostcondition("ConcSyncMatchingCreatePDB1", "ConcSyncMatchingCreatePDB2");
		terminate();
	}

	/**
	 * <b>Test</b> for concurrent creation of multiple persons where some IDs
	 * match and some do not. The source creates Adenauer and Erhard; the target
	 * creates Adenauer (matching) and Kiesinger (non-matching). <br/>
	 * <b>Expect</b>: Adenauer is merged, Erhard and Kiesinger each appear once
	 * in both databases. <br/>
	 * <b>Features</b>: concurrent, add, matching, non-matching, combined
	 */
	@ParameterizedTest @MethodSource("tools")
	public void testCombinedCreate(BXTool<pdb1.Database, pdb2.Database, Decisions> tool) {
		this.tool = tool; initialise();
		tool.noPrecondition();
		tool.performAndPropagateEdit(
				srcEdit(helperPerson1::setDatabaseName,
						helperPerson1::createKonradAdenauer,
						helperPerson1::createLudwigErhard),
				trgEdit(helperPerson2::createKonradAdenauer,
						helperPerson2::createKurtKiesinger,
						helperPerson2::changeIncrementalIDs));
		util.assertAnyPostcondition(Map.ofEntries(
				entry("ConcSyncCombinedCreatePDB1", "ConcSyncCombinedCreatePDB2")));
		terminate();
	}
}