package org.benchmarx.examples.set2oset.testsuite.alignment_based.roundtrip;

import java.util.Collection;

import org.benchmarx.BXTool;
import org.benchmarx.examples.set2oset.testsuite.BXToolParameterResolver;
import org.benchmarx.examples.set2oset.testsuite.Decisions;
import org.benchmarx.examples.set2oset.testsuite.Set2OsetTestCase;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Round-trip tests for the Set-to-OrderedSet transformation.
 * Each test performs a sequence of forward and backward propagation steps and
 * verifies consistency at each intermediate point.
 */
@ExtendWith(BXToolParameterResolver.class)
public class RoundtripTests extends Set2OsetTestCase {

	public RoundtripTests() {
		super();
	}

	public static Collection<BXTool<sets.MySet, osets.MyOrderedSet, Decisions>> tools() {
		return Set2OsetTestCase.tools();
	}

	/**
	 * <b>Test</b> for a round-trip: forward create A/B/C, backward invert order, then forward delete A and B.<br/>
	 * <b>Expect</b>: After invert, source is unchanged (sets have no order) and OSet is C→B→A.
	 * After deleting A and B from the source, both models contain only C.<br/>
	 * <b>Features</b>: roundtrip, fwd+bwd, order, del
	 */
	@ParameterizedTest
	@MethodSource("tools")
	public void testRoundtripInvertThenDelete(BXTool<sets.MySet, osets.MyOrderedSet, Decisions> tool) {
		this.tool = tool;
		initialise();
		tool.performAndPropagateSourceEdit(srcEdit(helperSet::setSetName, helperSet::createA, helperSet::createB, helperSet::createC));
		util.assertPrecondition("FirstThreeLettersSet", "FirstThreeLettersOset");
		// Backward: invert OSet order — source must remain unchanged
		tool.performAndPropagateTargetEdit(trgEdit(helperOset::invert));
		util.assertPostcondition("FirstThreeLettersSet", "CbaOset");
		// Forward: delete A and B from the source set
		tool.performAndPropagateSourceEdit(srcEdit(helperSet::deleteA, helperSet::deleteB));
		util.assertPostcondition("CSet", "COset");
	}

	/**
	 * <b>Test</b> for round-trip stability followed by extension.<br/>
	 * <b>Expect</b>: Backward idle delta leaves both models unchanged. Forward addition of D
	 * appends D to both the Set and the OSet.<br/>
	 * <b>Features</b>: roundtrip, stability, fwd+bwd, add
	 */
	@ParameterizedTest
	@MethodSource("tools")
	public void testRoundtripIdleBwdThenAddD(BXTool<sets.MySet, osets.MyOrderedSet, Decisions> tool) {
		this.tool = tool;
		initialise();
		tool.performAndPropagateSourceEdit(srcEdit(helperSet::setSetName, helperSet::createA, helperSet::createB, helperSet::createC));
		util.assertPrecondition("FirstThreeLettersSet", "FirstThreeLettersOset");
		// Backward idle: nothing must change
		tool.performAndPropagateTargetEdit(trgEdit(helperOset::idleDelta));
		util.assertPostcondition("FirstThreeLettersSet", "FirstThreeLettersOset");
		// Forward: add D to the source set
		tool.performAndPropagateSourceEdit(srcEdit(helperSet::createD));
		util.assertPostcondition("abcdSet", "AbcdOset");
	}
}
