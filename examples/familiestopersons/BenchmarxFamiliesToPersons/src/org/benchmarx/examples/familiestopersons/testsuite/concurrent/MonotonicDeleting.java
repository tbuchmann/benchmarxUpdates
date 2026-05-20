package org.benchmarx.examples.familiestopersons.testsuite.concurrent;

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
public class MonotonicDeleting extends FamiliesToPersonsTestCase {

	public MonotonicDeleting() { super(); }

	public static Collection<BXTool<FamilyRegister, PersonRegister, Decisions>> tools() {
		return FamiliesToPersonsTestCase.tools();
	}

	@ParameterizedTest @MethodSource("tools")
	public void testMatchingDeletion(BXTool<FamilyRegister, PersonRegister, Decisions> tool) {
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
		tool.performAndPropagateEdit(//
				srcEdit(helperFamily::deleteFatherHomer), trgEdit(helperPerson::deleteHomer));
		util.assertPostcondition("FamiliesAfterConcSyncMatchingDeletion", "PersonsAfterConcSyncMatchingDeletion");
		terminate();
	}

	@ParameterizedTest @MethodSource("tools")
	public void testNonMatchingDeletion(BXTool<FamilyRegister, PersonRegister, Decisions> tool) {
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
		tool.performAndPropagateEdit(//
				srcEdit(helperFamily::deleteFatherHomer), trgEdit(helperPerson::deleteMaggie));
		util.assertPostcondition("FamiliesAfterConcSyncNonMatchingDeletion", "PersonsAfterConcSyncNonMatchingDeletion");
		terminate();
	}

	@ParameterizedTest @MethodSource("tools")
	public void testCombinedCases(BXTool<FamilyRegister, PersonRegister, Decisions> tool) {
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
		tool.performAndPropagateEdit(//
				srcEdit(helperFamily::deleteFatherHomer, helperFamily::deleteRodAsSon), //
				trgEdit(helperPerson::deleteHomer, helperPerson::deleteMaggie));
		util.assertPostcondition("FamiliesAfterConcSyncCombinedCases", "PersonsAfterConcSyncCombinedCases");
		terminate();
	}
}