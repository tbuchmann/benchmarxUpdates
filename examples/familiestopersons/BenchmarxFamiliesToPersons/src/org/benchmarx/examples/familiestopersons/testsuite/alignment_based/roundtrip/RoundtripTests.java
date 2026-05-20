package org.benchmarx.examples.familiestopersons.testsuite.alignment_based.roundtrip;

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
public class RoundtripTests extends FamiliesToPersonsTestCase {

	public RoundtripTests() { super(); }

	public static Collection<BXTool<FamilyRegister, PersonRegister, Decisions>> tools() {
		return FamiliesToPersonsTestCase.tools();
	}

	@ParameterizedTest @MethodSource("tools")
	public void testRoundtripEdit(BXTool<FamilyRegister, PersonRegister, Decisions> tool) {
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
		tool.performAndPropagateTargetEdit(trgEdit(helperPerson::firstNameChangeOfHomer));
		tool.performAndPropagateSourceEdit(srcEdit(helperFamily::renameFlandersFamilyToBouvier));
		util.assertPostcondition("FamilyAfterRoundtripEdit", "PersonAfterRoundtripEdit");
		terminate();
	}

	@ParameterizedTest @MethodSource("tools")
	public void testRoundtripAdd(BXTool<FamilyRegister, PersonRegister, Decisions> tool) {
		this.tool = tool; initialise();
		util.configure().makeDecision(Decisions.PREFER_EXISTING_FAMILY_TO_NEW, true)
				.makeDecision(Decisions.PREFER_CREATING_PARENT_TO_CHILD, false);
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
		tool.performAndPropagateTargetEdit(trgEdit(helperPerson::createSeymour));
		tool.performAndPropagateSourceEdit(srcEdit(helperFamily::createSonTodd));
		util.assertPostcondition("FamilyAfterRoundtripAdd", "PersonAfterRoundtripAdd");
		terminate();
	}

	@ParameterizedTest @MethodSource("tools")
	public void testRoundtripDelete(BXTool<FamilyRegister, PersonRegister, Decisions> tool) {
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
		tool.performAndPropagateTargetEdit(trgEdit(helperPerson::deleteMarge));
		tool.performAndPropagateSourceEdit(srcEdit(helperFamily::deleteRodAsSon));
		util.assertPostcondition("FamilyAfterRoundtripDelete", "PersonAfterRoundtripDelete");
		terminate();
	}
}