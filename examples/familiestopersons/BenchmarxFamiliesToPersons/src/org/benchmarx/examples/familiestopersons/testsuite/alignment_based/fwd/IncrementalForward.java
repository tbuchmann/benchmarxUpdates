package org.benchmarx.examples.familiestopersons.testsuite.alignment_based.fwd;

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
public class IncrementalForward extends FamiliesToPersonsTestCase {

	public IncrementalForward() { super(); }

	public static Collection<BXTool<FamilyRegister, PersonRegister, Decisions>> tools() {
		return FamiliesToPersonsTestCase.tools();
	}

	@ParameterizedTest @MethodSource("tools")
	public void testIncrementalInserts(BXTool<FamilyRegister, PersonRegister, Decisions> tool) {
		this.tool = tool; initialise();
		tool.performAndPropagateSourceEdit(srcEdit(//
				helperFamily::createSkinnerFamily, helperFamily::createFlandersFamily,
				helperFamily::createSonRod, helperFamily::createSimpsonFamily, helperFamily::createFatherBart));
		tool.performIdleTargetEdit(trgEdit(helperPerson::setBirthdayOfRod));
		tool.performIdleTargetEdit(trgEdit(helperPerson::setBirthdayOfFatherBart));
		tool.performAndPropagateSourceEdit(srcEdit(helperFamily::createNewFamilySimpsonWithMembers));
		tool.performIdleTargetEdit(trgEdit(helperPerson::changeAllBirthdays));
		tool.performAndPropagateSourceEdit(srcEdit(helperFamily::createSonBart));
		tool.performIdleTargetEdit(trgEdit(helperPerson::setBirthdayOfYoungerBart));
		util.assertPrecondition("Pre_IncrFwdFamily", "Pre_IncrFwdPerson");
		tool.performAndPropagateSourceEdit(srcEdit(//
				helperFamily::createFatherNed, helperFamily::createMotherMaude, helperFamily::createSonTodd));
		util.assertPostcondition("FamilyAfterInsertion", "PersonAfterInsertion");
		terminate();
	}

	@ParameterizedTest @MethodSource("tools")
	public void testIncrementalDeletions(BXTool<FamilyRegister, PersonRegister, Decisions> tool) {
		this.tool = tool; initialise();
		tool.performAndPropagateSourceEdit(srcEdit(//
				helperFamily::createSkinnerFamily, helperFamily::createFlandersFamily,
				helperFamily::createSonRod, helperFamily::createSimpsonFamily, helperFamily::createFatherBart));
		tool.performIdleTargetEdit(trgEdit(helperPerson::setBirthdayOfRod));
		tool.performAndPropagateSourceEdit(srcEdit(helperFamily::createNewFamilySimpsonWithMembers));
		tool.performIdleTargetEdit(trgEdit(helperPerson::changeAllBirthdays));
		util.assertPrecondition("Pre_IncrFwdFamilyForDeletion", "Pre_IncrFwdPersonForDeletion");
		tool.performAndPropagateSourceEdit(srcEdit(helperFamily::createSonBart));
		tool.performAndPropagateSourceEdit(srcEdit(helperFamily::deleteFirstSonBart));
		util.assertPostcondition("FamilyAfterDeletion", "PersonAfterDeletion");
		terminate();
	}

	@ParameterizedTest @MethodSource("tools")
	public void testIncrementalRename(BXTool<FamilyRegister, PersonRegister, Decisions> tool) {
		this.tool = tool; initialise();
		tool.performAndPropagateSourceEdit(srcEdit(//
				helperFamily::createSkinnerFamily, helperFamily::createFlandersFamily,
				helperFamily::createSonRod, helperFamily::createSimpsonFamily, helperFamily::createFatherBart));
		tool.performIdleTargetEdit(trgEdit(helperPerson::setBirthdayOfRod));
		tool.performIdleTargetEdit(trgEdit(helperPerson::setBirthdayOfFatherBart));
		tool.performAndPropagateSourceEdit(srcEdit(helperFamily::createNewFamilySimpsonWithMembers));
		tool.performIdleTargetEdit(trgEdit(helperPerson::changeAllBirthdays));
		tool.performAndPropagateSourceEdit(srcEdit(helperFamily::createSonBart));
		tool.performIdleTargetEdit(trgEdit(helperPerson::setBirthdayOfYoungerBart));
		util.assertPrecondition("Pre_IncrFwdFamily", "Pre_IncrFwdPerson");
		tool.performAndPropagateSourceEdit(srcEdit(helperFamily::renameSimpsonToBouvier));
		util.assertPostcondition("FamilyAfterRename", "PersonAfterRename");
		terminate();
	}

	@ParameterizedTest @MethodSource("tools")
	public void testIncrementalMove(BXTool<FamilyRegister, PersonRegister, Decisions> tool) {
		this.tool = tool; initialise();
		tool.performAndPropagateSourceEdit(srcEdit(//
				helperFamily::createSkinnerFamily, helperFamily::createFlandersFamily,
				helperFamily::createSonRod, helperFamily::createSimpsonFamily, helperFamily::createFatherBart));
		tool.performIdleTargetEdit(trgEdit(helperPerson::setBirthdayOfRod));
		tool.performIdleTargetEdit(trgEdit(helperPerson::setBirthdayOfFatherBart));
		tool.performAndPropagateSourceEdit(srcEdit(helperFamily::createNewFamilySimpsonWithMembers));
		tool.performIdleTargetEdit(trgEdit(helperPerson::changeAllBirthdays));
		tool.performAndPropagateSourceEdit(srcEdit(helperFamily::createSonBart));
		tool.performIdleTargetEdit(trgEdit(helperPerson::setBirthdayOfYoungerBart));
		util.assertPrecondition("Pre_IncrFwdFamily", "Pre_IncrFwdPerson");
		tool.performAndPropagateSourceEdit(srcEdit(helperFamily::moveLisa, helperFamily::moveMarge));
		util.assertPostcondition("FamilyAfterMove", "PersonAfterMove");
		terminate();
	}

	@ParameterizedTest @MethodSource("tools")
	public void testIncrementalMixed(BXTool<FamilyRegister, PersonRegister, Decisions> tool) {
		this.tool = tool; initialise();
		tool.performAndPropagateSourceEdit(srcEdit(//
				helperFamily::createSkinnerFamily, helperFamily::createFlandersFamily,
				helperFamily::createSonRod, helperFamily::createSimpsonFamily, helperFamily::createFatherBart));
		tool.performIdleTargetEdit(trgEdit(helperPerson::setBirthdayOfRod));
		tool.performIdleTargetEdit(trgEdit(helperPerson::setBirthdayOfFatherBart));
		tool.performAndPropagateSourceEdit(srcEdit(helperFamily::createNewFamilySimpsonWithMembers));
		tool.performIdleTargetEdit(trgEdit(helperPerson::changeAllBirthdays));
		tool.performAndPropagateSourceEdit(srcEdit(helperFamily::createSonBart));
		tool.performIdleTargetEdit(trgEdit(helperPerson::setBirthdayOfYoungerBart));
		util.assertPrecondition("Pre_IncrFwdFamily", "Pre_IncrFwdPerson");
		tool.performAndPropagateSourceEdit(srcEdit(helperFamily::deleteFatherHomer));
		tool.performAndPropagateSourceEdit(srcEdit(helperFamily::createFatherHomer));
		util.assertPostcondition("FamilyAfterMixed", "PersonAfterMixed");
		terminate();
	}

	@ParameterizedTest @MethodSource("tools")
	public void testIncrementalMoveRoleChange(BXTool<FamilyRegister, PersonRegister, Decisions> tool) {
		this.tool = tool; initialise();
		tool.performAndPropagateSourceEdit(srcEdit(//
				helperFamily::createSkinnerFamily, helperFamily::createFlandersFamily,
				helperFamily::createSonRod, helperFamily::createSimpsonFamily, helperFamily::createFatherBart));
		tool.performIdleTargetEdit(trgEdit(helperPerson::setBirthdayOfRod));
		tool.performIdleTargetEdit(trgEdit(helperPerson::setBirthdayOfFatherBart));
		tool.performAndPropagateSourceEdit(srcEdit(helperFamily::createNewFamilySimpsonWithMembers));
		tool.performIdleTargetEdit(trgEdit(helperPerson::changeAllBirthdays));
		tool.performAndPropagateSourceEdit(srcEdit(helperFamily::createSonBart));
		tool.performIdleTargetEdit(trgEdit(helperPerson::setBirthdayOfYoungerBart));
		util.assertPrecondition("Pre_IncrFwdFamily", "Pre_IncrFwdPerson");
		tool.performAndPropagateSourceEdit(srcEdit(helperFamily::moveMaggieAndChangeRole));
		util.assertPostcondition("FamilyAfterMoveRoleChange", "PersonAfterMoveRoleChange");
		terminate();
	}

	@ParameterizedTest @MethodSource("tools")
	public void testStability(BXTool<FamilyRegister, PersonRegister, Decisions> tool) {
		this.tool = tool; initialise();
		tool.noPrecondition();
		tool.performAndPropagateSourceEdit(srcEdit(//
				helperFamily::createNewFamilySimpsonWithMembers, helperFamily::createSonBart));
		util.assertPostcondition("FamilyWithDuplicateMember", "PersonWithSameName");
		tool.performAndPropagateSourceEdit(srcEdit(helperFamily::idleDelta));
		util.assertPostcondition("FamilyWithDuplicateMember", "PersonWithSameName");
		terminate();
	}

	@ParameterizedTest @MethodSource("tools")
	public void testHippocraticness(BXTool<FamilyRegister, PersonRegister, Decisions> tool) {
		this.tool = tool; initialise();
		tool.noPrecondition();
		tool.performAndPropagateSourceEdit(srcEdit(//
				helperFamily::createNewFamilySimpsonWithMembers, helperFamily::createSonBart));
		util.assertPostcondition("FamilyWithDuplicateMember", "PersonWithSameName");
		tool.performAndPropagateSourceEdit(srcEdit(helperFamily::hippocraticDelta));
		util.assertPostcondition("FamilyWithDuplicateMember2", "PersonWithSameName");
		terminate();
	}
}