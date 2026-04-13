package org.benchmarx.examples.pdb12pdb2.testsuite.alignment_based.fwd;

import java.util.Collection;

import org.benchmarx.BXTool;
import org.benchmarx.examples.pdb12pdb2.testsuite.BXToolParameterResolver;
import org.benchmarx.examples.pdb12pdb2.testsuite.Decisions;
import org.benchmarx.examples.pdb12pdb2.testsuite.Pdb12Pdb2TestCase;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(BXToolParameterResolver.class)
public class IncrementalForward extends Pdb12Pdb2TestCase {
	public IncrementalForward(BXTool<pdb1.Database, pdb2.Database, Decisions> tool) {
		super(tool);
	}

	public static Collection<BXTool<pdb1.Database, pdb2.Database, Decisions>> tools() {
		return Pdb12Pdb2TestCase.tools();
	}
	
	/**
	 * <b>Test</b> for inserting persons into an existing person database. <br/>
	 * <b>Expect</b> : New persons are added to the register, while the old persons
	 * remain unchanged. <br/>
	 * <b>Features</b>: fwd, add, fixed
	 */
	@ParameterizedTest @MethodSource("tools")
	public void testIncrementalInserts(BXTool<pdb1.Database, pdb2.Database, Decisions> tool) { this.tool = tool; initialise();
		tool.performAndPropagateSourceEdit(srcEdit(
				helperPerson1::setDatabaseName,
				helperPerson1::createKonradAdenauer,
				helperPerson1::createLudwigErhard,
				helperPerson1::createKurtKiesinger,
				helperPerson1::createWillyBrandt,
				helperPerson1::createHelmutSchmidt,
				helperPerson1::createHelmutKohl));
//		tool.performAndPropagateSourceEdit(util
//				.execute(helperPerson1::setDatabaseName)
//				.andThen(helperPerson1::createKonradAdenauer)
//				.andThen(helperPerson1::createLudwigErhard)
//				.andThen(helperPerson1::createKurtKiesinger)
//				.andThen(helperPerson1::createWillyBrandt)
//				.andThen(helperPerson1::createHelmutSchmidt)
//				.andThen(helperPerson1::createHelmutKohl));
		tool.performIdleTargetEdit(trgEdit(helperPerson2::changeIncrementalIDs));
		
		util.assertPrecondition("Pre_IncrFwdPDB1FirstSixChancellors", "Pre_IncrFwdPDB2FirstSixChancellors");

		//------------
		tool.performAndPropagateSourceEdit(srcEdit(
				helperPerson1::createGerhardSchroeder,
				helperPerson1::createAngelaMerkel));
//		tool.performAndPropagateSourceEdit(util
//				.execute(helperPerson1::createGerhardSchroeder)
//				.andThen(helperPerson1::createAngelaMerkel));
		//------------
		util.assertPostcondition("IncrFwdPDB1AllChancellors", "IncrFwdPDB2AllChancellors");
		terminate();
	}
	
	/**
	 * <b>Test</b> for deleting persons. After creating the person register,
	 * set set 6 chancelors with all variables. Then delete Kurt Kiesinger from the pdb1 database.
	 * <b>Expect</b>: Delete the correct Person in the pdb2 database
	 * <b>Features</b>: fwd, del, corr-based, structural
	 */
	@ParameterizedTest @MethodSource("tools")
	public void testIncrementalDeletions(BXTool<pdb1.Database, pdb2.Database, Decisions> tool) { this.tool = tool; initialise();
		tool.performAndPropagateSourceEdit(srcEdit(
				helperPerson1::setDatabaseName,
				helperPerson1::createKonradAdenauer,
				helperPerson1::createLudwigErhard,
				helperPerson1::createKurtKiesinger,
				helperPerson1::createWillyBrandt,
				helperPerson1::createHelmutSchmidt,
				helperPerson1::createHelmutKohl));
//		tool.performAndPropagateSourceEdit(util
//				.execute(helperPerson1::setDatabaseName)
//				.andThen(helperPerson1::createKonradAdenauer)
//				.andThen(helperPerson1::createLudwigErhard)
//				.andThen(helperPerson1::createKurtKiesinger)
//				.andThen(helperPerson1::createWillyBrandt)
//				.andThen(helperPerson1::createHelmutSchmidt)
//				.andThen(helperPerson1::createHelmutKohl));
		tool.performIdleTargetEdit(trgEdit(helperPerson2::changeIncrementalIDs));
		
		util.assertPrecondition("Pre_IncrFwdPDB1FirstSixChancellors", "Pre_IncrFwdPDB2FirstSixChancellors");
		//------------
		tool.performAndPropagateSourceEdit(srcEdit(helperPerson1::deleteKurtKiesinger));
//		tool.performAndPropagateSourceEdit(util
//				.execute(helperPerson1::deleteKurtKiesinger));
		//------------
		util.assertPostcondition("IncrFwdPDB1FirstSixChancellorsWithoutKiesinger", "IncrFwdPDB2FirstSixChancellorsWithoutKiesinger");
		terminate();
	}
	
	/**
	 * <b>Test</b> for changing all variable-values in different persons. After creating the pdb2 database. 
	 * Then change values of each variable in another person and all variables of one person.
	 * <b>Expect</b>: Change the values of the affected variables in Persons of the pdb2 database.
	 * <b>Features</b>: fwd, attribute, fixed, structural, corr-based
	 */
	@ParameterizedTest @MethodSource("tools")
	public void testIncrementalValueChange(BXTool<pdb1.Database, pdb2.Database, Decisions> tool) { this.tool = tool; initialise();
		tool.performAndPropagateSourceEdit(srcEdit(
				helperPerson1::setDatabaseName,
				helperPerson1::createKonradAdenauer,
				helperPerson1::createLudwigErhard,
				helperPerson1::createKurtKiesinger,
				helperPerson1::createWillyBrandt,
				helperPerson1::createHelmutSchmidt,
				helperPerson1::createHelmutKohl));
//		tool.performAndPropagateSourceEdit(util
//				.execute(helperPerson1::setDatabaseName)
//				.andThen(helperPerson1::createKonradAdenauer)
//				.andThen(helperPerson1::createLudwigErhard)
//				.andThen(helperPerson1::createKurtKiesinger)
//				.andThen(helperPerson1::createWillyBrandt)
//				.andThen(helperPerson1::createHelmutSchmidt)
//				.andThen(helperPerson1::createHelmutKohl));
		tool.performIdleTargetEdit(trgEdit(helperPerson2::changeIncrementalIDs));
		
		util.assertPrecondition("Pre_IncrFwdPDB1FirstSixChancellors", "Pre_IncrFwdPDB2FirstSixChancellors");
		//------------
		tool.performAndPropagateSourceEdit(srcEdit(
				helperPerson1::changeAllOfHelmutKohl,
				helperPerson1::changeBirthdayOfKurtKiesinger,
				helperPerson1::changeFirstNameOfKonradAdenauer,
				helperPerson1::changeIDOfHelmutSchmidt,
				helperPerson1::changeLastNameOfLudwigErhard,
				helperPerson1::changePlaceOfBirthOfWillyBrandt));
//		tool.performAndPropagateSourceEdit(util
//				.execute(helperPerson1::changeAllOfHelmutKohl)
//				.andThen(helperPerson1::changeBirthdayOfKurtKiesinger)
//				.andThen(helperPerson1::changeFirstNameOfKonradAdenauer)
//				.andThen(helperPerson1::changeIDOfHelmutSchmidt)
//				.andThen(helperPerson1::changeLastNameOfLudwigErhard)
//				.andThen(helperPerson1::changePlaceOfBirthOfWillyBrandt));
		//------------
		util.assertPostcondition("IncrFwdPDB1FirstSixChancellorsAfterValueChange", "IncrFwdPDB2FirstSixChancellorsAfterValueChange");
		terminate();
	}

	/**
	 * <b>Test</b> for stability of the transformation.<br/>
	 * <b>Expect</b> re-running the transformation after an idle source delta does not change the target model.<br/>
	 * <b>Features:</b>: fwd, fixed
	 */
	@ParameterizedTest @MethodSource("tools")
	public void testStability(BXTool<pdb1.Database, pdb2.Database, Decisions> tool) { this.tool = tool; initialise();
		tool.performAndPropagateSourceEdit(srcEdit(
				helperPerson1::setDatabaseName,
				helperPerson1::createKonradAdenauer,
				helperPerson1::createLudwigErhard,
				helperPerson1::createKurtKiesinger,
				helperPerson1::createWillyBrandt,
				helperPerson1::createHelmutSchmidt,
				helperPerson1::createHelmutKohl,
				helperPerson1::createGerhardSchroeder,
				helperPerson1::createAngelaMerkel));
//		tool.performAndPropagateSourceEdit(util
//				.execute(helperPerson1::setDatabaseName)
//				.andThen(helperPerson1::createKonradAdenauer)
//				.andThen(helperPerson1::createLudwigErhard)
//				.andThen(helperPerson1::createKurtKiesinger)
//				.andThen(helperPerson1::createWillyBrandt)
//				.andThen(helperPerson1::createHelmutSchmidt)
//				.andThen(helperPerson1::createHelmutKohl)
//				.andThen(helperPerson1::createGerhardSchroeder)
//				.andThen(helperPerson1::createAngelaMerkel));
		tool.performIdleTargetEdit(trgEdit(helperPerson2::changeIncrementalIDs));

		util.assertPrecondition("IncrFwdPDB1AllChancellors", "IncrFwdPDB2AllChancellorsIDs");
		//------------
		tool.performAndPropagateSourceEdit(srcEdit(helperPerson1::idleDelta));
		//------------
		util.assertPostcondition("IncrFwdPDB1AllChancellors", "IncrFwdPDB2AllChancellorsIDs");
		terminate();
	}
	
	/**
	 * <b>Test</b> for hippocraticness of the transformation.<br/>
	 * <b>Expect</b> re-running the transformation after getting the first part of the last name of a person to the firstname does not change the pdb2 Database<br/>
	 * <b>Features:</b>: fwd, fixed
	 */
	@ParameterizedTest @MethodSource("tools")
	public void testHippocraticness(BXTool<pdb1.Database, pdb2.Database, Decisions> tool) { this.tool = tool; initialise();
		tool.performAndPropagateSourceEdit(srcEdit(
				helperPerson1::setDatabaseName,
				helperPerson1::createWrongKonradAdenauer,
				helperPerson1::createLudwigErhard,
				helperPerson1::createKurtKiesinger,
				helperPerson1::createWillyBrandt,
				helperPerson1::createHelmutSchmidt,
				helperPerson1::createHelmutKohl,
				helperPerson1::createGerhardSchroeder,
				helperPerson1::createAngelaMerkel));
//		tool.performAndPropagateSourceEdit(util
//				.execute(helperPerson1::setDatabaseName)
//				.andThen(helperPerson1::createWrongKonradAdenauer)
//				.andThen(helperPerson1::createLudwigErhard)
//				.andThen(helperPerson1::createKurtKiesinger)
//				.andThen(helperPerson1::createWillyBrandt)
//				.andThen(helperPerson1::createHelmutSchmidt)
//				.andThen(helperPerson1::createHelmutKohl)
//				.andThen(helperPerson1::createGerhardSchroeder)
//				.andThen(helperPerson1::createAngelaMerkel));
		tool.performIdleTargetEdit(trgEdit(helperPerson2::changeIncrementalIDs));

		util.assertPrecondition("IncrFwdPDB1AllChancellorsWrongAdenauer", "IncrFwdPDB2AllChancellorsIDs");
		//------------
		tool.performAndPropagateSourceEdit(srcEdit(helperPerson1::hippocraticDelta));
		//------------
		util.assertPostcondition("IncrFwdPDB1AllChancellors", "IncrFwdPDB2AllChancellorsIDs");
		terminate();
	}
}
