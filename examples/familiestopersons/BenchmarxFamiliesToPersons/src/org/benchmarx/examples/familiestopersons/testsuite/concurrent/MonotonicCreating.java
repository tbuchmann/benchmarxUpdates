package org.benchmarx.examples.familiestopersons.testsuite.concurrent;

import static java.util.Map.entry;

import java.util.Collection;
import java.util.Map;

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
public class MonotonicCreating extends FamiliesToPersonsTestCase {

	public MonotonicCreating() { super(); }

	public static Collection<BXTool<FamilyRegister, PersonRegister, Decisions>> tools() {
		return FamiliesToPersonsTestCase.tools();
	}

	@ParameterizedTest @MethodSource("tools")
	public void testSuitableFamilyNonMatchingMember(BXTool<FamilyRegister, PersonRegister, Decisions> tool) {
		this.tool = tool; initialise();
		tool.noPrecondition();
		tool.performAndPropagateEdit(srcEdit(helperFamily::createSimpsonFamily, helperFamily::createFatherHomer),
				trgEdit(helperPerson::createBart, helperPerson::changeAllBirthdays));
		util.assertPostcondition("FamilyAfterBasicConcurrentEdit1", "PersonsAfterBasicConcurrentEdit1");
		terminate();
	}

	@ParameterizedTest @MethodSource("tools")
	public void testSuitableFamilyMatchingMember(BXTool<FamilyRegister, PersonRegister, Decisions> tool) {
		this.tool = tool; initialise();
		tool.noPrecondition();
		util.configure().makeDecision(Decisions.PREFER_CREATING_PARENT_TO_CHILD, true)
				.makeDecision(Decisions.PREFER_EXISTING_FAMILY_TO_NEW, true);
		tool.performAndPropagateEdit(//
				srcEdit(helperFamily::createSimpsonFamily, helperFamily::createFatherHomer), //
				trgEdit(helperPerson::createHomer, helperPerson::changeAllBirthdays));
		util.assertPostcondition("FamilyAfterBasicConcurrentEdit2", "PersonsAfterBasicConcurrentEdit2");
		terminate();
	}

	@ParameterizedTest @MethodSource("tools")
	public void testNonSuitableFamily(BXTool<FamilyRegister, PersonRegister, Decisions> tool) {
		this.tool = tool; initialise();
		tool.noPrecondition();
		util.configure().makeDecision(Decisions.PREFER_CREATING_PARENT_TO_CHILD, true)
				.makeDecision(Decisions.PREFER_EXISTING_FAMILY_TO_NEW, true);
		tool.performAndPropagateEdit(//
				srcEdit(helperFamily::createSimpsonFamily, helperFamily::createFatherHomer), //
				trgEdit(helperPerson::createSeymour, helperPerson::setBirthdayOfSeymour));
		util.assertPostcondition("FamilyAfterBasicConcurrentEdit3", "PersonsAfterBasicConcurrentEdit3");
		terminate();
	}

	@ParameterizedTest @MethodSource("tools")
	public void testCombinedCases(BXTool<FamilyRegister, PersonRegister, Decisions> tool) {
		this.tool = tool; initialise();
		tool.noPrecondition();
		util.configure().makeDecision(Decisions.PREFER_CREATING_PARENT_TO_CHILD, false)
				.makeDecision(Decisions.PREFER_EXISTING_FAMILY_TO_NEW, true);
		tool.performAndPropagateEdit(//
				srcEdit(helperFamily::createSimpsonFamily, helperFamily::createFatherHomer, helperFamily::createSonBart), //
				trgEdit(helperPerson::createHomer, helperPerson::createSeymour,
						helperPerson::changeAllBirthdays, helperPerson::setBirthdayOfSeymour));
		util.assertAnyPostcondition(Map.ofEntries(//
				entry("FamilyAfterBasicConcurrentEdit4", "PersonsAfterBasicConcurrentEdit4"),
				entry("FamilyAfterBasicConcurrentEdit5", "PersonsAfterBasicConcurrentEdit5")));
		terminate();
	}
}