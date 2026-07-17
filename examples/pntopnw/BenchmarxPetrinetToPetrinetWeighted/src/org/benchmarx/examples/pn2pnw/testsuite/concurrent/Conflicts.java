package org.benchmarx.examples.pn2pnw.testsuite.concurrent;

import static java.util.Map.entry;

import java.util.Collection;
import java.util.Map;

import org.benchmarx.BXTool;
import org.benchmarx.examples.pn2pnw.testsuite.BXToolParameterResolver;
import org.benchmarx.examples.pn2pnw.testsuite.Decisions;
import org.benchmarx.examples.pn2pnw.testsuite.Pn2PnwTestCase;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Concurrent conflict tests for PetriNet-to-PetriNetWeighted: both sides edit the same
 * element incompatibly in the same concurrent step. Resolution is tool-dependent, so
 * {@code util.assertAnyPostcondition} is used to accept every outcome the tool could
 * reasonably produce.
 */
@ExtendWith(BXToolParameterResolver.class)
public class Conflicts extends Pn2PnwTestCase {

	public Conflicts() {
		super();
	}

	public static Collection<BXTool<pn.Net, pnw.Net, Decisions>> tools() {
		return Pn2PnwTestCase.tools();
	}

	/**
	 * <b>Test</b> for a conflict on the net name: source renames it to "FactoryModel"
	 * while target independently renames it to "AlternativeModel" in the same
	 * concurrent step (CF-NetNameRename).<br/>
	 * <b>Expect</b>: The tool resolves the naming conflict one way or another; both
	 * models end up consistent with whichever side won.<br/>
	 * <b>Features</b>: concurrent, conflict, rename
	 */
	@ParameterizedTest
	@MethodSource("tools")
	public void testConcurrentRenameNetNameConflict(BXTool<pn.Net, pnw.Net, Decisions> tool) {
		this.tool = tool;
		initialise();
		tool.performAndPropagateSourceEdit(srcEdit(helperPn::createPTPLettersDigits));
		tool.performIdleTargetEdit(trgEdit(helperPnw::weightA1BWith42));
		util.assertPrecondition("ConflictsPreLettersDigitsPn", "ConflictsPreLettersDigitsWeightedPnw");
		// Concurrent: SRC renames net to "FactoryModel"; TRG independently renames the
		// same net to "AlternativeModel" - a genuine incompatible edit to the same element.
		tool.performAndPropagateEdit(
				srcEdit(helperPn::renameToFactoryModel),
				trgEdit(helperPnw::renameToAlternativeModel));
		// Source-wins was captured directly from a real BXAgent run via tool.saveModels(...).
		// Target-wins is not independently observed, but derived from that verified
		// structure by substituting only the contested net name - both are accepted
		// since target-wins is just as valid a conflict resolution policy as source-wins.
		util.assertAnyPostcondition(Map.ofEntries(
				entry("ConflictsNetNameSrcWinsPn", "ConflictsNetNameSrcWinsWeightedPnw"),
				entry("ConflictsNetNameTrgWinsPn", "ConflictsNetNameTrgWinsWeightedPnw")));
		terminate();
	}
}
