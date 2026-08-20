package org.benchmarx.examples.familiestopersons.testsuite;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.benchmarx.BXTool;
import org.benchmarx.edit.ChangeAttribute;
import org.benchmarx.edit.CreateEdge;
import org.benchmarx.edit.CreateNode;
import org.benchmarx.edit.DeleteEdge;
import org.benchmarx.edit.DeleteNode;
import org.benchmarx.edit.Edit;
import org.benchmarx.edit.IEdit;
import org.benchmarx.edit.MoveNode;
import org.benchmarx.examples.familiestopersons.implementations.bxagent.BXAgentF2p;
import org.benchmarx.examples.familiestopersons.implementations.medini.MediniQVTFamiliesToPersons;
import org.benchmarx.examples.familiestopersons.implementations.medini.MediniQVTFamiliesToPersonsConfig;
import org.benchmarx.families.core.FamiliesComparator;
import org.benchmarx.families.core.FamilyHelper;
import org.benchmarx.persons.core.PersonHelper;
import org.benchmarx.persons.core.PersonsComparator;
import org.benchmarx.util.BenchmarxUtil;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.junit.jupiter.api.AfterEach;

import Families.FamiliesPackage;
import Families.FamilyRegister;
import Persons.PersonRegister;
import Persons.PersonsPackage;
import org.benchmarx.examples.familiestopersons.implementations.directllm.DirectLLMF2p;

public abstract class FamiliesToPersonsTestCase {

	protected BXTool<FamilyRegister, PersonRegister, Decisions> tool;
	protected BiConsumer<FamilyRegister, FamilyRegister> familiesComparator;
	protected BiConsumer<PersonRegister, PersonRegister> personsComparator;
	protected BenchmarxUtil<FamilyRegister, PersonRegister, Decisions> util;
	protected FamilyHelper helperFamily;
	protected PersonHelper helperPerson;
	protected IEdit<FamilyRegister> sourceEdit;
	protected IEdit<PersonRegister> targetEdit;

	public void initialise() {
		Logger.getRootLogger().setLevel(Level.INFO);
		
		// Make sure packages are registered
		FamiliesPackage.eINSTANCE.getName();
		PersonsPackage.eINSTANCE.getName();

		// Initialise all helpers
		familiesComparator = new FamiliesComparator();
		personsComparator = new PersonsComparator();
		util = new BenchmarxUtil<>(tool);

		// Initialise the bx tool
		tool.initiateSynchronisationDialogue();

		helperFamily = createAndInitialiseHelperFamily(() -> tool.getSourceModel(), () -> sourceEdit);
		helperPerson = createAndInitialiseHelperPerson(() -> tool.getTargetModel(), () -> targetEdit);
	}

	public static FamilyHelper createAndInitialiseHelperFamily(Supplier<FamilyRegister> familyRegister,
			Supplier<IEdit<FamilyRegister>> sourceEdit) {
		Consumer<EObject> createSourceNode = (n) -> sourceEdit.get().getSteps().add(new CreateNode<FamilyRegister>(n));
		BiConsumer<EReference, List<EObject>> createSourceEdge = (ref, sourceTarget) -> {
			sourceEdit.get().getSteps()
					.add(new CreateEdge<FamilyRegister>(ref, sourceTarget.get(0), sourceTarget.get(1)));
		};
		BiConsumer<EAttribute, List<?>> changeSourceAttribute = (attr, nodeOldNew) -> {
			sourceEdit.get().getSteps().add(new ChangeAttribute<FamilyRegister>(attr, (EObject) nodeOldNew.get(0),
					nodeOldNew.get(1), nodeOldNew.get(2)));
		};
		Consumer<EObject> deleteSourceNode = (n) -> sourceEdit.get().getSteps().add(new DeleteNode<FamilyRegister>(n));
		BiConsumer<EReference, List<EObject>> deleteSourceEdge = (ref, sourceTarget) -> {
			sourceEdit.get().getSteps()
					.add(new DeleteEdge<FamilyRegister>(ref, sourceTarget.get(0), sourceTarget.get(1)));
		};

		BiConsumer<EObject, List<EObject>> moveSourceNode = (n, oldP_oldRef_newP_newRef) -> sourceEdit.get().getSteps()
				.add(new MoveNode<FamilyRegister>(n, //
						oldP_oldRef_newP_newRef.get(0), (EReference) oldP_oldRef_newP_newRef.get(1),
						oldP_oldRef_newP_newRef.get(2), (EReference) oldP_oldRef_newP_newRef.get(3)));

		return new FamilyHelper(familyRegister, createSourceNode, createSourceEdge, changeSourceAttribute,
				deleteSourceNode, moveSourceNode, deleteSourceEdge);
	}

	public static PersonHelper createAndInitialiseHelperPerson(Supplier<PersonRegister> personRegister,
			Supplier<IEdit<PersonRegister>> targetEdit) {
		Consumer<EObject> createTargetNode = (n) -> targetEdit.get().getSteps().add(new CreateNode<PersonRegister>(n));
		BiConsumer<EReference, List<EObject>> createTargetEdge = (ref, sourceTarget) -> {
			targetEdit.get().getSteps()
					.add(new CreateEdge<PersonRegister>(ref, sourceTarget.get(0), sourceTarget.get(1)));
		};
		BiConsumer<EAttribute, List<?>> changeTargetAttribute = (attr, nodeOldNew) -> {
			targetEdit.get().getSteps().add(new ChangeAttribute<PersonRegister>(attr, (EObject) nodeOldNew.get(0),
					nodeOldNew.get(1), nodeOldNew.get(2)));
		};
		Consumer<EObject> deleteTargetNode = (n) -> targetEdit.get().getSteps().add(new DeleteNode<PersonRegister>(n));
		BiConsumer<EReference, List<EObject>> deleteTargetEdge = (ref, sourceTarget) -> {
			targetEdit.get().getSteps()
					.add(new DeleteEdge<PersonRegister>(ref, sourceTarget.get(0), sourceTarget.get(1)));
		};

		return new PersonHelper(personRegister, createTargetNode, createTargetEdge, changeTargetAttribute,
				deleteTargetNode, deleteTargetEdge);
	}

	@AfterEach
	public void terminate() {
		if (tool != null) {
			tool.terminateSynchronisationDialogue();
			tool = null;
		}
	}

	// Solutions requiring additional setup are commented out.
	public static Collection<BXTool<FamilyRegister, PersonRegister, Decisions>> tools() {
		List<BXTool<FamilyRegister, PersonRegister, Decisions>> allTools = Arrays.asList(//
				// new UbtXtendFamiliesToPersons(),
				// new IBeXTGGFamiliesToPersons(),
				// new BiGULFamiliesToPersons(),        // See setup: implementations/bigul/README-SETUP
				// new FunnyQTFamiliesToPerson(),       // Excluded: problems with Closure
				// new NMFFamiliesToPersonsIncremental(), // See setup: implementations/nmf/README-SETUP
				// new JTLFamiliesToPersons(),          // Excluded: problems with Emftext
				// new EMoflonFamiliesToPersons(),
				new MediniQVTFamiliesToPersons(),
				new MediniQVTFamiliesToPersonsConfig(),
				// new BXtendFamiliesToPersons(),       // No failures
				// new WrapperOverBXtendWithMerge(),    // No failures
				// new BXtendDSLFamiliesToPersons(),    // 3 failures
				new BXAgentF2p()                        // 4 failures
				// new ENeoFamiliesToPersons(),         // See setup: implementations/eneo/README-SETUP
				// new IBeXTGGIntegrateFamiliesToPersons() // See setup: implementations/ibextgg/integrate/README-SETUP
		,
				new DirectLLMF2p());
		String toolName = System.getProperty("benchmarx.tool");
		if (toolName != null && !toolName.isEmpty()) {
			return allTools.stream()
					.filter(t -> t.getClass().getSimpleName().equals(toolName))
					.collect(java.util.stream.Collectors.toList());
		}
		return allTools;
	}

	protected FamiliesToPersonsTestCase(BXTool<FamilyRegister, PersonRegister, Decisions> tool) {
		this.tool = tool;
	}

	protected FamiliesToPersonsTestCase() {
		// no-arg constructor for JUnit 5 — tool is set by each test method
	}

	protected Supplier<IEdit<FamilyRegister>> srcEdit(Runnable... ops) {
		return () -> {
			sourceEdit = new Edit<FamilyRegister>();
			for (var op : ops) {
				op.run();
			}
			return sourceEdit;
		};
	}

	protected Supplier<IEdit<PersonRegister>> trgEdit(Runnable... ops) {
		return () -> {
			targetEdit = new Edit<PersonRegister>();
			for (var op : ops) {
				op.run();
			}
			return targetEdit;
		};
	}
}
