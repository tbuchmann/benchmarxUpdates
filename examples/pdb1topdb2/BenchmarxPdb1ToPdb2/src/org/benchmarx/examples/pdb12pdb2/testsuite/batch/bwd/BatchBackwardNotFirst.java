package org.benchmarx.examples.pdb12pdb2.testsuite.batch.bwd;

import java.util.Collection;

import org.benchmarx.BXTool;
import org.benchmarx.examples.pdb12pdb2.testsuite.BXToolParameterResolver;
import org.benchmarx.examples.pdb12pdb2.testsuite.Decisions;
import org.benchmarx.examples.pdb12pdb2.testsuite.Pdb12Pdb2TestCase;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(BXToolParameterResolver.class)
public class BatchBackwardNotFirst extends Pdb12Pdb2TestCase {

	public BatchBackwardNotFirst(BXTool<pdb1.Database, pdb2.Database, Decisions> tool) {
		super(tool);
	}

	public static Collection<BXTool<pdb1.Database, pdb2.Database, Decisions>> tools() {
		return Pdb12Pdb2TestCase.tools();
	}
	
	/**
	 * <b>Test</b> for creation of a single Person (Adenauer) in an empty Database.
	 * <br/>
	 * <b>Expect</b> Adenauer to be created in the source model, with multiple first names and single last name.
	 * <br/>
	 * <b>Features:</b>: bwd, runtime
	 */
	@ParameterizedTest @MethodSource("tools")
	public void testCreatePerson(BXTool<pdb1.Database, pdb2.Database, Decisions> tool) {
		this.tool = tool; initialise();
		// No precondition!
		//------------
		util.configure().makeDecision(Decisions.PREFER_USING_FIRST_SPACE_TO_LAST, false);
		tool.performAndPropagateTargetEdit(trgEdit(helperPerson2::createKonradAdenauer));
		//------------
		util.assertPostcondition("AdenauerPdb1", "AdenauerPdb2");
	}

	/**
	 * Analogous to @link {@link #testCreatePerson()}, but now for
	 * multiple Persons (first three chancellors).<br/>
	 * <b>Features:</b>: bwd, runtime
	 */
	@ParameterizedTest @MethodSource("tools")
	public void testCreateMultiplePersons(BXTool<pdb1.Database, pdb2.Database, Decisions> tool) { this.tool = tool; initialise();
		tool.performAndPropagateTargetEdit(trgEdit(helperPerson2::setDatabaseName));

		util.assertPrecondition("EmptyBundeskanzlerPdb1", "EmptyBundeskanzlerPdb2");
		//------------
		util.configure().makeDecision(Decisions.PREFER_USING_FIRST_SPACE_TO_LAST, false);
		tool.performAndPropagateTargetEdit(trgEdit(
				helperPerson2::createKonradAdenauer,
				helperPerson2::createLudwigErhard,
				helperPerson2::createKurtKiesinger));
		//------------
		util.assertPostcondition("PDB1FirstThreeChancellors", "Pre_IncrBwdPDB2FirstThreeChancellors");
		terminate();
	}
}
