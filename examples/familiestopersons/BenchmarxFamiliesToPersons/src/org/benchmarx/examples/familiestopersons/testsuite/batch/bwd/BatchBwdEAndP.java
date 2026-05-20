package org.benchmarx.examples.familiestopersons.testsuite.batch.bwd;

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
public class BatchBwdEAndP extends FamiliesToPersonsTestCase {

	public BatchBwdEAndP() { super(); }

	public static Collection<BXTool<FamilyRegister, PersonRegister, Decisions>> tools() {
		return FamiliesToPersonsTestCase.tools();
	}

	@ParameterizedTest @MethodSource("tools")
	public void testCreateMalePersonAsSon(BXTool<FamilyRegister, PersonRegister, Decisions> tool) {
		this.tool = tool; initialise();
		tool.noPrecondition();
		util.configure()//
				.makeDecision(Decisions.PREFER_EXISTING_FAMILY_TO_NEW, true)
				.makeDecision(Decisions.PREFER_CREATING_PARENT_TO_CHILD, true);
		tool.performAndPropagateTargetEdit(trgEdit(helperPerson::createRod));
		util.assertPostcondition("OneFamilyWithOneFamilyMember", "PersonOneMaleMember");
		terminate();
	}

	@ParameterizedTest @MethodSource("tools")
	public void testCreateFamilyMembersInExistingFamilyAsParents(BXTool<FamilyRegister, PersonRegister, Decisions> tool) {
		this.tool = tool; initialise();
		tool.noPrecondition();
		util.configure()//
				.makeDecision(Decisions.PREFER_EXISTING_FAMILY_TO_NEW, true)//
				.makeDecision(Decisions.PREFER_CREATING_PARENT_TO_CHILD, true);
		tool.performAndPropagateTargetEdit(trgEdit(//
				helperPerson::createRod, //
				helperPerson::createHomer, //
				helperPerson::createMarge));
		util.assertPostcondition("FamilyWithParentsOnly", "PersonsMultiDeterministic");
		terminate();
	}
}