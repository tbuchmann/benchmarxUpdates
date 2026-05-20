package org.benchmarx.examples.familiestopersons.testsuite.batch.fwd;

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
public class BatchForward extends FamiliesToPersonsTestCase {

	public BatchForward() { super(); }

	public static Collection<BXTool<FamilyRegister, PersonRegister, Decisions>> tools() {
		return FamiliesToPersonsTestCase.tools();
	}

	@ParameterizedTest @MethodSource("tools")
	public void testInitialiseSynchronisation(BXTool<FamilyRegister, PersonRegister, Decisions> tool) {
		this.tool = tool; initialise();
		tool.noPrecondition();
		util.assertPostcondition("RootElementFamilies", "RootElementPersons");
		terminate();
	}

	@ParameterizedTest @MethodSource("tools")
	public void testFamilyNameChangeOfEmpty(BXTool<FamilyRegister, PersonRegister, Decisions> tool) {
		this.tool = tool; initialise();
		tool.performAndPropagateSourceEdit(srcEdit(helperFamily::createSimpsonFamily));
		util.assertPrecondition("Pre_NameChangeFamilyEmpty", "Pre_NameChangePersonEmpty");
		tool.performAndPropagateSourceEdit(srcEdit(helperFamily::renameEmptySimpsonToBouvier));
		util.assertPostcondition("NameChangeFamilyEmpty", "NameChangePersonEmpty");
		terminate();
	}

	@ParameterizedTest @MethodSource("tools")
	public void testCreateFamily(BXTool<FamilyRegister, PersonRegister, Decisions> tool) {
		this.tool = tool; initialise();
		tool.noPrecondition();
		tool.performAndPropagateSourceEdit(srcEdit(helperFamily::createSkinnerFamily));
		util.assertPostcondition("OneFamily", "PersonsForOneFamily");
		terminate();
	}

	@ParameterizedTest @MethodSource("tools")
	public void testCreateFamilyMember(BXTool<FamilyRegister, PersonRegister, Decisions> tool) {
		this.tool = tool; initialise();
		tool.noPrecondition();
		tool.performAndPropagateSourceEdit(srcEdit(helperFamily::createFlandersFamily, helperFamily::createSonRod));
		util.assertPostcondition("OneFamilyWithOneFamilyMemberSon", "PersonOneMaleMember");
		terminate();
	}

	@ParameterizedTest @MethodSource("tools")
	public void testNewFamilyWithMultiMembers(BXTool<FamilyRegister, PersonRegister, Decisions> tool) {
		this.tool = tool; initialise();
		tool.noPrecondition();
		tool.performAndPropagateSourceEdit(srcEdit(//
				helperFamily::createFlandersFamily, //
				helperFamily::createSonRod, //
				helperFamily::createNewFamilySimpsonWithMembers));
		util.assertPostcondition("NewFamilyWithMembers", "PersonsMulti");
		terminate();
	}

	@ParameterizedTest @MethodSource("tools")
	public void testNewDuplicateFamilyNames(BXTool<FamilyRegister, PersonRegister, Decisions> tool) {
		this.tool = tool; initialise();
		tool.noPrecondition();
		tool.performAndPropagateSourceEdit(srcEdit(//
				helperFamily::createNewFamilySimpsonWithMembers, //
				helperFamily::createSimpsonFamily, //
				helperFamily::createFatherBart));
		util.assertPostcondition("FamiliesWithSameName", "PersonWithSameName");
		terminate();
	}

	@ParameterizedTest @MethodSource("tools")
	public void testDuplicateFamilyMemberNames(BXTool<FamilyRegister, PersonRegister, Decisions> tool) {
		this.tool = tool; initialise();
		tool.noPrecondition();
		tool.performAndPropagateSourceEdit(srcEdit(//
				helperFamily::createNewFamilySimpsonWithMembers, //
				helperFamily::createSonBart));
		util.assertPostcondition("FamilyWithDuplicateMember", "PersonWithSameName");
		terminate();
	}
}