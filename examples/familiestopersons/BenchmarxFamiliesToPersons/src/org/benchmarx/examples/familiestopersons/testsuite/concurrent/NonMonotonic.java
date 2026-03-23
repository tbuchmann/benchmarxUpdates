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
public class NonMonotonic extends FamiliesToPersonsTestCase {

	public NonMonotonic(BXTool<FamilyRegister, PersonRegister, Decisions> tool) {
		super(tool);
	}

	public static Collection<BXTool<FamilyRegister, PersonRegister, Decisions>> tools() {
		return FamiliesToPersonsTestCase.tools();
	}

	@ParameterizedTest @MethodSource("tools")
	public void testCombinedDeletionAndCreation(BXTool<FamilyRegister, PersonRegister, Decisions> tool) {
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
				srcEdit(helperFamily::deleteFatherHomer, helperFamily::createFatherNed), //
				trgEdit(helperPerson::deleteMarge, helperPerson::deleteHomer));
		util.assertPostcondition("FamiliesAfterConcSyncCombinedNonMonotonicCases", "PersonsAfterConcSyncCombinedNonMonotonicCases");
		terminate();
	}

	@ParameterizedTest @MethodSource("tools")
	public void testCombinedRenameDelete(BXTool<FamilyRegister, PersonRegister, Decisions> tool) {
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
				srcEdit(helperFamily::deleteFatherHomer), trgEdit(helperPerson::nameChangeOfLisa));
		util.assertPostcondition("FamiliesAfterConcSyncCombinedNonMonotonicRenameDelete", "PersonsAfterConcSyncCombinedNonMonotonicRenameDelete");
		terminate();
	}
}