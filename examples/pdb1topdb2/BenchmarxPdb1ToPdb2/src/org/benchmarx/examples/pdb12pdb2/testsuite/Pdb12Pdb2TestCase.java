package org.benchmarx.examples.pdb12pdb2.testsuite;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.benchmarx.BXTool;
import org.benchmarx.edit.ChangeAttribute;
import org.benchmarx.edit.CreateEdge;
import org.benchmarx.edit.CreateNode;
import org.benchmarx.edit.Edit;
import org.benchmarx.edit.IEdit;
import org.benchmarx.edit.MoveNode;
import org.benchmarx.examples.pdb12pdb2.implementations.bxagent.BXAgentPdb12Pdb2;
import org.benchmarx.examples.pdb12pdb2.implementations.bxtend.BXtendPdb12Pdb2;
import org.benchmarx.pdb1.core.Pdb1Comparator;
import org.benchmarx.pdb1.core.Pdb1Helper;
import org.benchmarx.pdb2.core.Pdb2Comparator;
import org.benchmarx.pdb2.core.Pdb2Helper;
import org.benchmarx.util.BenchmarxUtil;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public abstract class Pdb12Pdb2TestCase {

	protected BXTool<pdb1.Database, pdb2.Database, Decisions> tool;
	protected BiConsumer<pdb1.Database, pdb1.Database> person1Comparator;
	protected BiConsumer<pdb2.Database, pdb2.Database> person2Comparator;
	protected BenchmarxUtil<pdb1.Database, pdb2.Database, Decisions> util;
	protected Pdb1Helper helperPerson1;
	protected Pdb2Helper helperPerson2;
	protected IEdit<pdb1.Database> sourceEdit;
	protected IEdit<pdb2.Database> targetEdit;

	@Before
	public void initialise() {
		// Make sure packages are registered
		pdb1.Pdb1Package.eINSTANCE.getPdb1Factory();
		pdb2.Pdb2Package.eINSTANCE.getPdb2Factory();
		
		// Initialise all helpers
		person1Comparator = new Pdb1Comparator();
		person2Comparator = new Pdb2Comparator();
		util = new BenchmarxUtil<>(tool);
		//helperPerson1 = new Pdb1Helper(null, null, null, null, null, null, null);
		//helperPerson2 = new Pdb2Helper();
		
		// Initialise the bx tool
		tool.initiateSynchronisationDialogue();
		
		helperPerson1 = 
				createAndInitialiseHelperPerson1(() -> tool.getSourceModel(), () -> sourceEdit);
		helperPerson2 = 
				createAndInitialiseHelperPerson2(() -> tool.getTargetModel(), () -> targetEdit);
	
	}
	
	public static Pdb1Helper createAndInitialiseHelperPerson1(Supplier<pdb1.Database> personRegister,
			Supplier<IEdit<pdb1.Database>> sourceEdit) {
		Consumer<EObject> createSourceNode = (n) -> sourceEdit.get().getSteps().add(new CreateNode<pdb1.Database>(n));
		BiConsumer<EReference, List<EObject>> createSourceEdge = (r, ns) -> sourceEdit.get().getSteps().add(new CreateEdge<pdb1.Database>(r, ns.get(0), ns.get(1)));
		
		BiConsumer<EAttribute, List<?>> changeSourceAttribute = (a, v) -> sourceEdit.get().getSteps().add(new ChangeAttribute<pdb1.Database>(a, (EObject)v.get(0), v.get(1), v.get(2)));
		
		Consumer<EObject> deleteSourceNode = (n) -> sourceEdit.get().getSteps().add(new org.benchmarx.edit.DeleteNode<pdb1.Database>(n));
		BiConsumer<EReference, List<EObject>> deleteSourceEdge = (r, ns) -> sourceEdit.get().getSteps().add(new org.benchmarx.edit.DeleteEdge<pdb1.Database>(r, ns.get(0), ns.get(1)));
		
		BiConsumer<EObject, List<EObject>> moveSourceNode = (n, ns) -> sourceEdit.get().getSteps()
				.add(new MoveNode<pdb1.Database>(n, ns.get(0), (EReference)ns.get(1), ns.get(2), (EReference)ns.get(3)));
		
		return new Pdb1Helper(personRegister, createSourceNode, createSourceEdge, changeSourceAttribute, deleteSourceNode, moveSourceNode, deleteSourceEdge);
	}
	
	public static Pdb2Helper createAndInitialiseHelperPerson2(Supplier<pdb2.Database> personRegister,
			Supplier<IEdit<pdb2.Database>> targetEdit) {
		Consumer<EObject> createTargetNode = (n) -> targetEdit.get().getSteps().add(new CreateNode<pdb2.Database>(n));
		BiConsumer<EReference, List<EObject>> createTargetEdge = (r, ns) -> targetEdit.get().getSteps().add(new CreateEdge<pdb2.Database>(r, ns.get(0), ns.get(1)));
		
		BiConsumer<EAttribute, List<?>> changeTargetAttribute = (a, v) -> targetEdit.get().getSteps().add(new ChangeAttribute<pdb2.Database>(a, (EObject)v.get(0), v.get(1), v.get(2)));
		
		Consumer<EObject> deleteTargetNode = (n) -> targetEdit.get().getSteps().add(new org.benchmarx.edit.DeleteNode<pdb2.Database>(n));
		BiConsumer<EReference, List<EObject>> deleteTargetEdge = (r, ns) -> targetEdit.get().getSteps().add(new org.benchmarx.edit.DeleteEdge<pdb2.Database>(r, ns.get(0), ns.get(1)));
		
		BiConsumer<EObject, List<EObject>> moveTargetNode = (n, ns) -> targetEdit.get().getSteps()
				.add(new MoveNode<pdb2.Database>(n, ns.get(0), (EReference)ns.get(1), ns.get(2), (EReference)ns.get(3)));
		
		return new Pdb2Helper(personRegister, createTargetNode, createTargetEdge, changeTargetAttribute, deleteTargetNode, moveTargetNode, deleteTargetEdge);
	}

	@After
	public void terminate(){
		tool.terminateSynchronisationDialogue();
	}
	
	@Parameters(name = "{0}")
	public static Collection<BXTool<pdb1.Database, pdb2.Database, Decisions>> tools() {
		return Arrays.asList(
				//new BXtendPdb12Pdb2()				
				//new MediniQVTPdb12Pdb2(),
				//new IBeXTGGPDB1ToPDB2()
				new BXAgentPdb12Pdb2()
			);
	}
	
	protected Pdb12Pdb2TestCase(BXTool<pdb1.Database, pdb2.Database, Decisions> tool) {
		this.tool = tool; 
	}
	
	protected Supplier<IEdit<pdb1.Database>> srcEdit(Runnable... ops) {
		return () -> {
			sourceEdit = new Edit<pdb1.Database>();
			for (var op : ops) {
				op.run();
			}
			return sourceEdit;
		};
	}

	protected Supplier<IEdit<pdb2.Database>> trgEdit(Runnable... ops) {
		return () -> {
			targetEdit = new Edit<pdb2.Database>();
			for (var op : ops) {
				op.run();
			}
			return targetEdit;
		};
	}
}
