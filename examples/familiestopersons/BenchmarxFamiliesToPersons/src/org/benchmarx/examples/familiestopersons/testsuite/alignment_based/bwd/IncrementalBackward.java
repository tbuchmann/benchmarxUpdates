package org.benchmarx.examples.familiestopersons.testsuite.alignment_based.bwd;

import java.util.Collection;

import org.benchmarx.BXTool;
import org.benchmarx.examples.familiestopersons.testsuite.BXToolParameterResolver;
import org.benchmarx.examples.familiestopersons.testsuite.Decisions;
import org.benchmarx.examples.familiestopersons.testsuite.FamiliesToPersonsTestCase;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import Families.FamilyRegister;
import Persons.PersonRegister;

@ExtendWith(BXToolParameterResolver.class)
public class IncrementalBackward extends FamiliesToPersonsTestCase {

	public IncrementalBackward() { super(); }

	public static Collection<BXTool<FamilyRegister, PersonRegister, Decisions>> tools() {
		return FamiliesToPersonsTestCase.tools();
	}

	@ParameterizedTest @MethodSource("tools")
	public void testIncrementalInsertsFixedConfig(BXTool<FamilyRegister, PersonRegister, Decisions> tool) {
		this.tool = tool; initialise();
		util.configure().makeDecision(Decisions.PREFER_EXISTING_FAMILY_TO_NEW, true)
				.makeDecision(Decisions.PREFER_CREATING_PARENT_TO_CHILD, true);
		tool.performAndPropagateTargetEdit(trgEdit(helperPerson::createHomer, helperPerson::createMaggie));
		tool.performIdleTargetEdit(trgEdit(helperPerson::setBirthdaysOfSimpson));
		util.assertPrecondition("Pre_IncrBwdFamily", "Pre_IncrBwdPerson");
		tool.performAndPropagateTargetEdit(trgEdit(helperPerson::createSeymour));
		util.assertPostcondition("FamilyAfterBwdInsertion1", "PersonAfterBwdInsertion1");
		tool.performAndPropagateTargetEdit(trgEdit(helperPerson::createSeymour));
		util.assertPostcondition("FamilyAfterBwdInsertion2", "PersonAfterBwdInsertion2");
		terminate();
	}

	@ParameterizedTest @MethodSource("tools")
	public void testIncrementalInsertsDynamicConfig(BXTool<FamilyRegister, PersonRegister, Decisions> tool) {
		this.tool = tool; initialise();
		util.configure().makeDecision(Decisions.PREFER_EXISTING_FAMILY_TO_NEW, true)
				.makeDecision(Decisions.PREFER_CREATING_PARENT_TO_CHILD, true);
		tool.performAndPropagateTargetEdit(trgEdit(helperPerson::createHomer, helperPerson::createMaggie));
		tool.performIdleTargetEdit(trgEdit(helperPerson::setBirthdaysOfSimpson));
		util.assertPrecondition("Pre_IncrBwdFamily", "Pre_IncrBwdPerson");
		tool.performAndPropagateTargetEdit(trgEdit(helperPerson::createSeymour));
		util.assertPostcondition("FamilyAfterBwdInsertion1", "PersonAfterBwdInsertion1");
		tool.performAndPropagateTargetEdit(trgEdit(helperPerson::createSeymour));
		util.assertPostcondition("FamilyAfterBwdInsertion2", "PersonAfterBwdInsertion2");
		util.configure().makeDecision(Decisions.PREFER_EXISTING_FAMILY_TO_NEW, false)
				.makeDecision(Decisions.PREFER_CREATING_PARENT_TO_CHILD, true);
		tool.performAndPropagateTargetEdit(trgEdit(helperPerson::createSeymour));
		util.assertPostcondition("FamilyAfterBwdInsertion3", "PersonAfterBwdInsertion3");
		util.configure().makeDecision(Decisions.PREFER_EXISTING_FAMILY_TO_NEW, false)
				.makeDecision(Decisions.PREFER_CREATING_PARENT_TO_CHILD, false);
		tool.performAndPropagateTargetEdit(trgEdit(helperPerson::createSeymour));
		util.assertPostcondition("FamilyAfterBwdInsertion4", "PersonAfterBwdInsertion4");
		util.configure().makeDecision(Decisions.PREFER_EXISTING_FAMILY_TO_NEW, true)
				.makeDecision(Decisions.PREFER_CREATING_PARENT_TO_CHILD, true);
		tool.performAndPropagateTargetEdit(trgEdit(helperPerson::createSeymour));
		util.assertPostcondition("FamilyAfterBwdInsertion5", "PersonAfterBwdInsertion5");
		util.configure().makeDecision(Decisions.PREFER_EXISTING_FAMILY_TO_NEW, true)
				.makeDecision(Decisions.PREFER_CREATING_PARENT_TO_CHILD, false);
		tool.performAndPropagateTargetEdit(trgEdit(helperPerson::createBart, helperPerson::createLisa));
		util.assertPostcondition("FamilyAfterBwdInsertion6", "PersonAfterBwdInsertion6");
		terminate();
	}

	@ParameterizedTest @MethodSource("tools")
	public void testIncrementalDeletions(BXTool<FamilyRegister, PersonRegister, Decisions> tool) {
		this.tool = tool; initialise();
		util.configure().makeDecision(Decisions.PREFER_EXISTING_FAMILY_TO_NEW, true)
				.makeDecision(Decisions.PREFER_CREATING_PARENT_TO_CHILD, true);
		tool.performAndPropagateTargetEdit(trgEdit(helperPerson::createHomer));
		util.configure().makeDecision(Decisions.PREFER_EXISTING_FAMILY_TO_NEW, true)
				.makeDecision(Decisions.PREFER_CREATING_PARENT_TO_CHILD, false);
		tool.performAndPropagateTargetEdit(trgEdit(helperPerson::createMaggie));
		tool.performIdleTargetEdit(trgEdit(helperPerson::setBirthdaysOfSimpson));
		util.assertPrecondition("Pre_IncrBwdFamilyFatherChild", "Pre_IncrBwdPerson");
		tool.performAndPropagateTargetEdit(trgEdit(helperPerson::deleteHomer));
		tool.performAndPropagateTargetEdit(trgEdit(helperPerson::deleteMaggie));
		util.assertPostcondition("FamilyAfterBwdDeletion", "PersonAfterBwdDeletion");
		terminate();
	}

	@ParameterizedTest @MethodSource("tools")
	public void testIncrementalRenamingDynamic(BXTool<FamilyRegister, PersonRegister, Decisions> tool) {
		this.tool = tool; initialise();
		util.configure().makeDecision(Decisions.PREFER_EXISTING_FAMILY_TO_NEW, true)
				.makeDecision(Decisions.PREFER_CREATING_PARENT_TO_CHILD, false);
		tool.performAndPropagateTargetEdit(trgEdit(helperPerson::createRod));
		tool.performIdleTargetEdit(trgEdit(helperPerson::setBirthdayOfRod));
		util.configure().makeDecision(Decisions.PREFER_EXISTING_FAMILY_TO_NEW, false)
				.makeDecision(Decisions.PREFER_CREATING_PARENT_TO_CHILD, true);
		tool.performAndPropagateTargetEdit(trgEdit(helperPerson::createHomer));
		util.configure().makeDecision(Decisions.PREFER_EXISTING_FAMILY_TO_NEW, true)
				.makeDecision(Decisions.PREFER_CREATING_PARENT_TO_CHILD, true);
		tool.performAndPropagateTargetEdit(trgEdit(helperPerson::createMarge, helperPerson::createBart));
		tool.performAndPropagateTargetEdit(trgEdit(helperPerson::createLisa, helperPerson::createMaggie));
		tool.performIdleTargetEdit(trgEdit(helperPerson::changeAllBirthdays));
		tool.performAndPropagateTargetEdit(trgEdit(helperPerson::createBart));
		tool.performIdleTargetEdit(trgEdit(helperPerson::setBirthdayOfYoungerBart));
		util.configure().makeDecision(Decisions.PREFER_EXISTING_FAMILY_TO_NEW, false)
				.makeDecision(Decisions.PREFER_CREATING_PARENT_TO_CHILD, true);
		tool.performAndPropagateTargetEdit(trgEdit(helperPerson::createBart));
		tool.performIdleTargetEdit(trgEdit(helperPerson::setBirthdayOfFatherBart));
		util.assertPrecondition("Pre_IncrBwdFamilyRenameDynamic", "Pre_IncrBwdPersonRenameDynamic");
		tool.performAndPropagateTargetEdit(trgEdit(helperPerson::firstNameChangeOfBart));
		util.configure().makeDecision(Decisions.PREFER_EXISTING_FAMILY_TO_NEW, true)
				.makeDecision(Decisions.PREFER_CREATING_PARENT_TO_CHILD, true);
		tool.performAndPropagateTargetEdit(trgEdit(helperPerson::fullNameChangeOfOtherBart));
		util.configure().makeDecision(Decisions.PREFER_EXISTING_FAMILY_TO_NEW, true)
				.makeDecision(Decisions.PREFER_CREATING_PARENT_TO_CHILD, false);
		tool.performAndPropagateTargetEdit(trgEdit(helperPerson::fullNameChangeOfFatherBart));
		util.configure().makeDecision(Decisions.PREFER_EXISTING_FAMILY_TO_NEW, false)
				.makeDecision(Decisions.PREFER_CREATING_PARENT_TO_CHILD, false);
		tool.performAndPropagateTargetEdit(trgEdit(helperPerson::familyNameChangeOfLisa));
		util.configure().makeDecision(Decisions.PREFER_EXISTING_FAMILY_TO_NEW, false)
				.makeDecision(Decisions.PREFER_CREATING_PARENT_TO_CHILD, true);
		tool.performAndPropagateTargetEdit(trgEdit(helperPerson::fullNameChangeOfMarge));
		util.assertPostcondition("FamilyAfterBwdIncrRenameDynamic", "PersonAfterBwdIncrRenameDynamic");
		terminate();
	}

	@ParameterizedTest @MethodSource("tools")
	public void testIncrementalMixedDynamic(BXTool<FamilyRegister, PersonRegister, Decisions> tool) {
		this.tool = tool; initialise();
		util.configure().makeDecision(Decisions.PREFER_EXISTING_FAMILY_TO_NEW, true)
				.makeDecision(Decisions.PREFER_CREATING_PARENT_TO_CHILD, true);
		tool.performAndPropagateTargetEdit(trgEdit(helperPerson::createMaggie));
		tool.performAndPropagateTargetEdit(trgEdit(helperPerson::createHomer));
		tool.performIdleTargetEdit(trgEdit(helperPerson::setBirthdaysOfSimpson));
		util.assertPrecondition("Pre_IncrBwdFamily", "Pre_IncrBwdPerson");
		util.configure().makeDecision(Decisions.PREFER_EXISTING_FAMILY_TO_NEW, true)
				.makeDecision(Decisions.PREFER_CREATING_PARENT_TO_CHILD, false);
		tool.performAndPropagateTargetEdit(trgEdit(helperPerson::deleteHomer, helperPerson::createHomer));
		util.assertPostcondition("FamilyAfterBwdMixed", "PersonAfterBwdMixed");
		terminate();
	}

	@ParameterizedTest @MethodSource("tools")
	public void testIncrementalOperational(BXTool<FamilyRegister, PersonRegister, Decisions> tool) {
		this.tool = tool; initialise();
		util.configure().makeDecision(Decisions.PREFER_EXISTING_FAMILY_TO_NEW, true)
				.makeDecision(Decisions.PREFER_CREATING_PARENT_TO_CHILD, false);
		tool.performAndPropagateTargetEdit(trgEdit(helperPerson::createMaggie));
		tool.performIdleTargetEdit(trgEdit(helperPerson::setBirthdayOfMaggie));
		util.assertPrecondition("Pre_IncrBwdOpFamily", "Pre_IncrBwdOpPerson");
		util.configure().makeDecision(Decisions.PREFER_EXISTING_FAMILY_TO_NEW, true)
				.makeDecision(Decisions.PREFER_CREATING_PARENT_TO_CHILD, true);
		tool.performAndPropagateTargetEdit(trgEdit(//
				helperPerson::createMarge, helperPerson::createLisa, helperPerson::createHomer,
				helperPerson::createBart, helperPerson::createMaggie, helperPerson::createLisa));
		util.configure().makeDecision(Decisions.PREFER_EXISTING_FAMILY_TO_NEW, false)
				.makeDecision(Decisions.PREFER_CREATING_PARENT_TO_CHILD, true);
		tool.performAndPropagateTargetEdit(trgEdit(helperPerson::createLisa));
		util.assertPostcondition("FamilyAfterIncrOp", "PersonAfterIncrOp");
		terminate();
	}

	@ParameterizedTest @MethodSource("tools")
	public void testStability(BXTool<FamilyRegister, PersonRegister, Decisions> tool) {
		this.tool = tool; initialise();
		tool.noPrecondition();
		util.configure().makeDecision(Decisions.PREFER_EXISTING_FAMILY_TO_NEW, true)
				.makeDecision(Decisions.PREFER_CREATING_PARENT_TO_CHILD, true);
		tool.performAndPropagateTargetEdit(trgEdit(//
				helperPerson::createRod, helperPerson::createHomer, helperPerson::createMarge));
		util.assertPostcondition("FamilyWithParentsOnly", "PersonsMultiDeterministic");
		tool.performAndPropagateTargetEdit(trgEdit(helperPerson::idleDelta));
		util.assertPostcondition("FamilyWithParentsOnly", "PersonsMultiDeterministic");
		terminate();
	}

	@ParameterizedTest @MethodSource("tools")
	public void testHippocraticness(BXTool<FamilyRegister, PersonRegister, Decisions> tool) {
		this.tool = tool; initialise();
		tool.noPrecondition();
		util.configure().makeDecision(Decisions.PREFER_EXISTING_FAMILY_TO_NEW, true)
				.makeDecision(Decisions.PREFER_CREATING_PARENT_TO_CHILD, true);
		tool.performAndPropagateTargetEdit(trgEdit(//
				helperPerson::createRod, helperPerson::createHomer, helperPerson::createMarge));
		util.assertPostcondition("FamilyWithParentsOnly", "PersonsMultiDeterministic");
		tool.performAndPropagateTargetEdit(trgEdit(helperPerson::hippocraticDelta));
		util.assertPostcondition("FamilyWithParentsOnly", "PersonsMultiDeterministic2");
		terminate();
	}
}