package org.benchmarx.examples.pdb12pdb2.testsuite.alignment_based.bwd;

import java.util.Collection;

import org.benchmarx.BXTool;
import org.benchmarx.examples.pdb12pdb2.testsuite.BXToolParameterResolver;
import org.benchmarx.examples.pdb12pdb2.testsuite.Decisions;
import org.benchmarx.examples.pdb12pdb2.testsuite.Pdb12Pdb2TestCase;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(BXToolParameterResolver.class)
public class IncrementalBackward extends Pdb12Pdb2TestCase {
	public IncrementalBackward(BXTool<pdb1.Database, pdb2.Database, Decisions> tool) {
		super(tool);
	}

	public static Collection<BXTool<pdb1.Database, pdb2.Database, Decisions>> tools() {
		return Pdb12Pdb2TestCase.tools();
	}
	
	/**
	 * <b>Test</b> for inserting persons into an existing person database. In this case the config is fixed. All persons lastName will be splitted of by the last space.<br/>
	 * <b>Expect</b> : New persons are added to the register, while the old persons
	 * remain unchanged. <br/>
	 * <b>Features</b>: bwd, add, fixed
	 */
	@ParameterizedTest @MethodSource("tools")
	public void testIncrementalInsertsFixedConfigLastSpace(BXTool<pdb1.Database, pdb2.Database, Decisions> tool) { this.tool = tool; initialise();
		tool.setConfigurator(util.configure().makeDecision(Decisions.PREFER_USING_FIRST_SPACE_TO_LAST , false));
		tool.performAndPropagateTargetEdit(trgEdit(
				helperPerson2::setDatabaseName,
				helperPerson2::createKonradAdenauer,
				helperPerson2::createLudwigErhard,
				helperPerson2::createKurtKiesinger,
				helperPerson2::createWillyBrandt,
				helperPerson2::createHelmutSchmidt,
				helperPerson2::createHelmutKohl));

		tool.performIdleSourceEdit(srcEdit(helperPerson1::changeIncrementalIDs));
		
		util.assertPrecondition("Pre_IncrBwdPDB1FirstSixChancellors", "Pre_IncrBwdPDB2FirstSixChancellors");

		//------------
		tool.performAndPropagateTargetEdit(trgEdit(
				helperPerson2::createGerhardSchroeder,
				helperPerson2::createAngelaMerkel));

		//------------
		util.assertPostcondition("IncrBwdPDB1AllChancellors", "IncrBwdPDB2AllChancellors");
		terminate();
	}
	
	/**
	 * <b>Test</b> for inserting persons into an existing person database. In this case the config is fixed. All persons lastName will be splitted of by the first space.<br/>
	 * <b>Expect</b> : New persons are added to the register, while the old persons
	 * remain unchanged. <br/>
	 * <b>Features</b>: bwd, add, fixed
	 */
	@ParameterizedTest @MethodSource("tools")
	public void testIncrementalInsertsFixedConfigFirstSpace(BXTool<pdb1.Database, pdb2.Database, Decisions> tool) { this.tool = tool; initialise();
		tool.setConfigurator(util.configure().makeDecision(Decisions.PREFER_USING_FIRST_SPACE_TO_LAST , true));
		tool.performAndPropagateTargetEdit(trgEdit(
				helperPerson2::setDatabaseName,
				helperPerson2::createKonradAdenauer,
				helperPerson2::createLudwigErhard,
				helperPerson2::createKurtKiesinger,
				helperPerson2::createWillyBrandt,
				helperPerson2::createHelmutSchmidt,
				helperPerson2::createHelmutKohl));

		
		tool.performIdleSourceEdit(srcEdit(helperPerson1::changeIncrementalIDs));
		
		util.assertPrecondition("Pre_IncrBwdPDB1FirstSixChancellorsFirstSpace", "Pre_IncrBwdPDB2FirstSixChancellors");

		//------------
		tool.performAndPropagateTargetEdit(trgEdit(
				helperPerson2::createGerhardSchroeder,
				helperPerson2::createAngelaMerkel));

		//------------
		util.assertPostcondition("IncrBwdPDB1AllChancellorsFirstSpace", "IncrBwdPDB2AllChancellors");
		terminate();
	}
	
	/**
	 * <b>Test</b> for inserting of a Person in a pdb2 database after the initial
	 * register has been transformed into a pdb1 database.<br/>
	 * <b>Expect</b> : pdb1 and pdb2 models are structured as specified in the corresponding
	 * assertPostcondition statements.<br/>
	 * <b>Question</b>: Is this test based on the assumption that the config change is only applied to the new delta?
	 * <b>Features</b>: bwd, add, runtime
	 */
	@ParameterizedTest @MethodSource("tools")
	public void testIncrementalInsertsDynamicConfig(BXTool<pdb1.Database, pdb2.Database, Decisions> tool) { this.tool = tool; initialise();
		tool.setConfigurator(util.configure().makeDecision(Decisions.PREFER_USING_FIRST_SPACE_TO_LAST , false));
		tool.performAndPropagateTargetEdit(trgEdit(
				helperPerson2::setDatabaseName,
				helperPerson2::createKonradAdenauer,
				helperPerson2::createLudwigErhard,
				helperPerson2::createKurtKiesinger));

		tool.performIdleSourceEdit(srcEdit(helperPerson1::changeIncrementalIDs));
		
		util.assertPrecondition("Pre_IncrBwdPDB1FirstThreeChancellors", "Pre_IncrBwdPDB2FirstThreeChancellors");

		//------------
		util.configure().makeDecision(Decisions.PREFER_USING_FIRST_SPACE_TO_LAST, true);
		tool.performAndPropagateTargetEdit(trgEdit(
				helperPerson2::createWillyBrandt,
				helperPerson2::createHelmutSchmidt,
				helperPerson2::createHelmutKohl));

		
		util.assertPostcondition("IncrBwdDynamicConfigPDB1_1", "Pre_IncrBwdPDB2FirstSixChancellors");
		
		// now setting last_space
		util.configure().makeDecision(Decisions.PREFER_USING_FIRST_SPACE_TO_LAST, false);
		tool.performAndPropagateTargetEdit(trgEdit(helperPerson2::createGerhardSchroeder));
		util.assertPostcondition("IncrBwdDynamicConfigPDB1_2", "IncrBwdPDB2FirstSevenChancellors");
		
		// now setting first_space
		util.configure().makeDecision(Decisions.PREFER_USING_FIRST_SPACE_TO_LAST, true);
		tool.performAndPropagateTargetEdit(trgEdit(helperPerson2::createAngelaMerkel));
		util.assertPostcondition("IncrBwdDynamicConfigPDB1_3", "IncrBwdPDB2AllChancellors");
		//------------			
		terminate();
	}
	
	/**
	 * <b>Test</b> for deleting persons. After creating the person register,
	 * set set 6 chancelors with all variables. Then delete Kurt Kiesinger from the pdb1 database.
	 * <b>Expect</b>: Delete the correct Person in the pdb2 database
	 * <b>Features</b>: bwd, del, corr-based, structural
	 */
	@ParameterizedTest @MethodSource("tools")
	public void testIncrementalDeletions(BXTool<pdb1.Database, pdb2.Database, Decisions> tool) { this.tool = tool; initialise();
		tool.performAndPropagateTargetEdit(trgEdit(
				helperPerson2::setDatabaseName,
				helperPerson2::createKonradAdenauer,
				helperPerson2::createLudwigErhard,
				helperPerson2::createKurtKiesinger,
				helperPerson2::createWillyBrandt,
				helperPerson2::createHelmutSchmidt,
				helperPerson2::createHelmutKohl));

		tool.performIdleSourceEdit(srcEdit(helperPerson1::changeIncrementalIDs));
		
		util.assertPrecondition("Pre_IncrBwdPDB1FirstSixChancellors", "Pre_IncrBwdPDB2FirstSixChancellors");
		//------------
		tool.performAndPropagateTargetEdit(trgEdit(helperPerson2::deleteKurtKiesinger));
		//------------
		util.assertPostcondition("IncrBwdPDB1FirstSixChancellorsWithoutKiesinger", "IncrBwdPDB2FirstSixChancellorsWithoutKiesinger");
		terminate();
	}
	
	/**
	 * <b>Test</b> for changing all variable-values in different persons. After creating the pdb2 database. 
	 * Then change values of each variable in another person and all variables of one person. Using different configs for bwd Transformation.
	 * <b>Expect</b>: Change the values of the affected variables in Persons of the pdb1 database.
	 * <b>Question</b>: Is this test based on the assumption that the config change is only applied to the new delta?
	 * <b>Features</b>: bwd, attribute, structural, corr-based
	 */
	@ParameterizedTest @MethodSource("tools")
	public void testIncrementalValueChange(BXTool<pdb1.Database, pdb2.Database, Decisions> tool) { this.tool = tool; initialise();
		util.configure().makeDecision(Decisions.PREFER_USING_FIRST_SPACE_TO_LAST, false);
		tool.performAndPropagateTargetEdit(trgEdit(
				helperPerson2::setDatabaseName,
				helperPerson2::createKonradAdenauer,
				helperPerson2::createLudwigErhard,
				helperPerson2::createKurtKiesinger,
				helperPerson2::createWillyBrandt,
				helperPerson2::createHelmutSchmidt,
				helperPerson2::createHelmutKohl));

		tool.performIdleSourceEdit(srcEdit(helperPerson1::changeIncrementalIDs));
		
		util.assertPrecondition("Pre_IncrBwdPDB1FirstSixChancellors", "Pre_IncrBwdPDB2FirstSixChancellors");
		//------------
		tool.performAndPropagateTargetEdit(trgEdit(
				helperPerson2::changeAllOfHelmutKohl,
				helperPerson2::changeBirthdayOfKurtKiesinger,
				helperPerson2::changeFirstNameOfKonradAdenauer));

		//------------
		util.assertPostcondition("IncrBwdPDB1FirstSixChancellorsAfterValueChange_1", "IncrBwdPDB2FirstSixChancellorsAfterValueChange_1");
		
		util.configure().makeDecision(Decisions.PREFER_USING_FIRST_SPACE_TO_LAST, true);
		tool.performAndPropagateTargetEdit(trgEdit(
				helperPerson2::changeIDOfHelmutSchmidt,
				helperPerson2::changeLastNameOfLudwigErhard,
				helperPerson2::changePlaceOfBirthOfWillyBrandt));

		//------------
		util.assertPostcondition("IncrBwdPDB1FirstSixChancellorsAfterValueChange_2", "IncrBwdPDB2FirstSixChancellorsAfterValueChange_2");
		terminate();
	}

	/**
	 * <b>Test</b> for stability of the transformation.<br/>
	 * <b>Expect</b> re-running the transformation after an idle source delta does not change the target model.<br/>
	 * <b>Features:</b>: bwd, fixed
	 */
	@ParameterizedTest @MethodSource("tools")
	public void testStability(BXTool<pdb1.Database, pdb2.Database, Decisions> tool) { this.tool = tool; initialise();
		tool.performAndPropagateTargetEdit(trgEdit(
				helperPerson2::setDatabaseName,
				helperPerson2::createKonradAdenauer,
				helperPerson2::createLudwigErhard,
				helperPerson2::createKurtKiesinger,
				helperPerson2::createWillyBrandt,
				helperPerson2::createHelmutSchmidt,
				helperPerson2::createHelmutKohl,
				helperPerson2::createGerhardSchroeder,
				helperPerson2::createAngelaMerkel));

		tool.performIdleSourceEdit(srcEdit(helperPerson1::changeIncrementalIDs));

		util.assertPrecondition("IncrBwdPDB1AllChancellorsIDs", "IncrBwdPDB2AllChancellors");
		//------------
		tool.performAndPropagateTargetEdit(trgEdit(helperPerson2::idleDelta));
		//------------
		util.assertPostcondition("IncrBwdPDB1AllChancellorsIDs", "IncrBwdPDB2AllChancellors");
		terminate();
	}
	
	/**
	 * <b>Test</b> for hippocraticness of the transformation.<br/>
	 * <b>Expect</b> re-running the transformation after changing the configuration should not change anything in the pdb1 Database.<br/>
	 * <b>Question</b> not sure about this anymore. If the config is changed, the transformation might change the pdb1 database, because the way how the name is splitted is changed. So maybe we can only expect that the names are splitted in a different way, but not that the values of the variables are changed. What do you think? <br/>
	 * <b>Features:</b>: bwd, fixed
	 */
	@ParameterizedTest @MethodSource("tools")
	public void testHippocraticness(BXTool<pdb1.Database, pdb2.Database, Decisions> tool) { this.tool = tool; initialise();
		util.configure().makeDecision(Decisions.PREFER_USING_FIRST_SPACE_TO_LAST, false);
		tool.performAndPropagateTargetEdit(trgEdit(
				helperPerson2::setDatabaseName,
				helperPerson2::createKonradAdenauer,
				helperPerson2::createLudwigErhard,
				helperPerson2::createKurtKiesinger,
				helperPerson2::createWillyBrandt,
				helperPerson2::createHelmutSchmidt,
				helperPerson2::createHelmutKohl,
				helperPerson2::createGerhardSchroeder,
				helperPerson2::createAngelaMerkel));

		tool.performIdleSourceEdit(srcEdit(helperPerson1::changeIncrementalIDs));

		util.assertPrecondition("IncrBwdPDB1AllChancellorsIDs", "IncrBwdPDB2AllChancellors");
		//------------
		util.configure().makeDecision(Decisions.PREFER_USING_FIRST_SPACE_TO_LAST, true);
		tool.performAndPropagateTargetEdit(trgEdit(helperPerson2::hippocraticDelta));
		//------------
		util.assertPostcondition("IncrBwdPDB1AllChancellorsIDs", "IncrBwdPDB2AllChancellors");
		terminate();
	}
}
