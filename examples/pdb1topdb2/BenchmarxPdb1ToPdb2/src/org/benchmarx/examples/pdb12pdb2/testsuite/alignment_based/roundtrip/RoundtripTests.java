package org.benchmarx.examples.pdb12pdb2.testsuite.alignment_based.roundtrip;

import java.util.Collection;

import org.benchmarx.BXTool;
import org.benchmarx.examples.pdb12pdb2.testsuite.BXToolParameterResolver;
import org.benchmarx.examples.pdb12pdb2.testsuite.Decisions;
import org.benchmarx.examples.pdb12pdb2.testsuite.Pdb12Pdb2TestCase;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(BXToolParameterResolver.class)
public class RoundtripTests extends Pdb12Pdb2TestCase {

	public RoundtripTests(BXTool<pdb1.Database, pdb2.Database, Decisions> tool) {
		super(tool);
	}

	public static Collection<BXTool<pdb1.Database, pdb2.Database, Decisions>> tools() {
		return Pdb12Pdb2TestCase.tools();
	}

	/**
	 * <b>Test</b> for a rename round-trip: a name change is first propagated
	 * backward (target → source), then a different name change is propagated
	 * forward (source → target). <br/>
	 * <b>Expect</b>: Both renames are reflected consistently in both databases. <br/>
	 * <b>Features</b>: roundtrip, rename
	 */
	@ParameterizedTest @MethodSource("tools")
	public void testRoundtripEdit(BXTool<pdb1.Database, pdb2.Database, Decisions> tool) {
		this.tool = tool; initialise();
		tool.performAndPropagateSourceEdit(srcEdit(
				helperPerson1::setDatabaseName,
				helperPerson1::createKonradAdenauer,
				helperPerson1::createLudwigErhard,
				helperPerson1::createKurtKiesinger,
				helperPerson1::createWillyBrandt,
				helperPerson1::createHelmutSchmidt,
				helperPerson1::createHelmutKohl));
		tool.performIdleTargetEdit(trgEdit(helperPerson2::changeIncrementalIDs));
		util.assertPrecondition("Pre_IncrFwdPDB1FirstSixChancellors", "Pre_IncrFwdPDB2FirstSixChancellors");
		// ------------
		tool.performAndPropagateTargetEdit(trgEdit(helperPerson2::changeFirstNameOfKonradAdenauer));
		tool.performAndPropagateSourceEdit(srcEdit(helperPerson1::changeLastNameOfLudwigErhard));
		tool.performIdleSourceEdit(srcEdit(helperPerson1::changeIncrementalIDs));
		// ------------
		util.assertPostcondition("RoundtripEditPDB1", "RoundtripEditPDB2");
		terminate();
	}

	/**
	 * <b>Test</b> for an add round-trip: a new person is first added via the
	 * target (backward propagation), then another new person is added via the
	 * source (forward propagation). <br/>
	 * <b>Expect</b>: Both persons appear in both databases. <br/>
	 * <b>Features</b>: roundtrip, add
	 */
	@ParameterizedTest @MethodSource("tools")
	public void testRoundtripAdd(BXTool<pdb1.Database, pdb2.Database, Decisions> tool) {
		this.tool = tool; initialise();
		tool.performAndPropagateSourceEdit(srcEdit(
				helperPerson1::setDatabaseName,
				helperPerson1::createKonradAdenauer,
				helperPerson1::createLudwigErhard,
				helperPerson1::createKurtKiesinger,
				helperPerson1::createWillyBrandt,
				helperPerson1::createHelmutSchmidt,
				helperPerson1::createHelmutKohl));
		tool.performIdleTargetEdit(trgEdit(helperPerson2::changeIncrementalIDs));		
		util.assertPrecondition("Pre_IncrFwdPDB1FirstSixChancellors", "Pre_IncrFwdPDB2FirstSixChancellors");
		// ------------
		tool.performIdleSourceEdit(srcEdit(helperPerson1::changeIncrementalIDs));
		tool.performAndPropagateTargetEdit(trgEdit(helperPerson2::createGerhardSchroeder));
		tool.performAndPropagateSourceEdit(srcEdit(helperPerson1::createAngelaMerkel));
		// ------------
		util.assertPostcondition("IncrBwdPDB1AllChancellors", "IncrFwdPDB2AllChancellors");
		terminate();
	}

	/**
	 * <b>Test</b> for a delete round-trip: a person is deleted via the target
	 * (backward propagation), then an attribute is changed via the source
	 * (forward propagation). <br/>
	 * <b>Expect</b>: The deleted person is removed from both databases and the
	 * renamed person is updated in both. <br/>
	 * <b>Features</b>: roundtrip, delete, rename
	 */
	@ParameterizedTest @MethodSource("tools")
	public void testRoundtripDelete(BXTool<pdb1.Database, pdb2.Database, Decisions> tool) {
		this.tool = tool; initialise();
		tool.performAndPropagateSourceEdit(srcEdit(
				helperPerson1::setDatabaseName,
				helperPerson1::createKonradAdenauer,
				helperPerson1::createLudwigErhard,
				helperPerson1::createKurtKiesinger,
				helperPerson1::createWillyBrandt,
				helperPerson1::createHelmutSchmidt,
				helperPerson1::createHelmutKohl));
		tool.performIdleTargetEdit(trgEdit(helperPerson2::changeIncrementalIDs));
		util.assertPrecondition("Pre_IncrFwdPDB1FirstSixChancellors", "Pre_IncrFwdPDB2FirstSixChancellors");
		// ------------
		tool.performAndPropagateTargetEdit(trgEdit(helperPerson2::deleteKurtKiesinger));
		tool.performAndPropagateSourceEdit(srcEdit(helperPerson1::changeLastNameOfLudwigErhard));
		tool.performIdleSourceEdit(srcEdit(helperPerson1::changeIncrementalIDs));
		// ------------
		util.assertPostcondition("RoundtripDeletePDB1", "RoundtripDeletePDB2");
		terminate();
	}
}