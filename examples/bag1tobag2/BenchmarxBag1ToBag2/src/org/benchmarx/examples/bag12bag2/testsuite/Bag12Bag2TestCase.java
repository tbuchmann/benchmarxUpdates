package org.benchmarx.examples.bag12bag2.testsuite;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.benchmarx.BXTool;
import org.benchmarx.bags1.core.Bag1Comparator;
import org.benchmarx.bags1.core.Bag1Helper;
import org.benchmarx.bags2.core.Bag2Comparator;
import org.benchmarx.bags2.core.Bag2Helper;
import org.benchmarx.edit.ChangeAttribute;
import org.benchmarx.edit.CreateEdge;
import org.benchmarx.edit.CreateNode;
import org.benchmarx.edit.Edit;
import org.benchmarx.edit.IEdit;
import org.benchmarx.edit.MoveNode;
import org.benchmarx.examples.bag12bag2.implementations.bxtend.BXtendBag12Bag2;
import org.benchmarx.examples.bag12bag2.implementations.medini.MediniQVTBag12Bag2;
import org.benchmarx.util.BenchmarxUtil;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;

public abstract class Bag12Bag2TestCase {
	protected BXTool<bags1.MyBag, bags2.MyBag, Decisions> tool;
	protected BiConsumer<bags1.MyBag, bags1.MyBag> bag1Comparator;
	protected BiConsumer<bags2.MyBag, bags2.MyBag> bag2Comparator;
	protected BenchmarxUtil<bags1.MyBag, bags2.MyBag, Decisions> util;
	protected Bag1Helper helperBag1;
	protected Bag2Helper helperBag2;
	protected IEdit<bags1.MyBag> sourceEdit;
	protected IEdit<bags2.MyBag> targetEdit;

	public void initialise() {
		// Make sure packages are registered
		bags1.Bags1Package.eINSTANCE.getBags1Factory();
		bags2.Bags2Package.eINSTANCE.getBags2Factory();
		
		// Initialise all helpers
		bag1Comparator = new Bag1Comparator();
		bag2Comparator = new Bag2Comparator();
		util = new BenchmarxUtil<>(tool);
		
		// Initialise the bx tool
		tool.initiateSynchronisationDialogue();
		
		helperBag1 = createAndInitialiseHelperBag1(() -> tool.getSourceModel(), () -> sourceEdit);
		helperBag2 = createAndInitialiseHelperBag2(() -> tool.getTargetModel(), () -> targetEdit);
		
	}
	
	public static Bag1Helper createAndInitialiseHelperBag1(Supplier<bags1.MyBag> rootSupplier, 
			Supplier<IEdit<bags1.MyBag>> sourceEdit) {
		Consumer<EObject> createSourceNode = (n) -> sourceEdit.get().getSteps().add(new CreateNode<bags1.MyBag>(n));
		BiConsumer<EReference, List<EObject>> createSourceEdge = (r, ns) -> sourceEdit.get().getSteps().add(new CreateEdge<bags1.MyBag>(r, ns.get(0), ns.get(1)));
		
		BiConsumer<EAttribute, List<?>> changeSourceAttribute = (a, v) -> sourceEdit.get().getSteps().add(new ChangeAttribute<bags1.MyBag>(a, (EObject)v.get(0), v.get(1), v.get(2)));
		
		Consumer<EObject> deleteSourceNode = (n) -> sourceEdit.get().getSteps().add(new org.benchmarx.edit.DeleteNode<bags1.MyBag>(n));
		BiConsumer<EReference, List<EObject>> deleteSourceEdge = (r, ns) -> sourceEdit.get().getSteps().add(new org.benchmarx.edit.DeleteEdge<bags1.MyBag>(r, ns.get(0), ns.get(1)));
		
		BiConsumer<EObject, List<EObject>> moveSourceNode = (n, ns) -> sourceEdit.get().getSteps()
				.add(new MoveNode<bags1.MyBag>(n, ns.get(0), (EReference)ns.get(1), ns.get(2), (EReference)ns.get(3)));
		
		return new Bag1Helper(rootSupplier, createSourceNode, createSourceEdge, changeSourceAttribute, deleteSourceNode, moveSourceNode, deleteSourceEdge);
	}
	
	public static Bag2Helper createAndInitialiseHelperBag2(Supplier<bags2.MyBag> rootSupplier, 
			Supplier<IEdit<bags2.MyBag>> targetEdit) {
		
		Consumer<EObject> createTargetNode = (n) -> targetEdit.get().getSteps().add(new CreateNode<bags2.MyBag>(n));
		BiConsumer<EReference, List<EObject>> createTargetEdge = (r, ns) -> targetEdit.get().getSteps().add(new CreateEdge<bags2.MyBag>(r, ns.get(0), ns.get(1)));
		
		BiConsumer<EAttribute, List<?>> changeTargetAttribute = (a, v) -> targetEdit.get().getSteps().add(new ChangeAttribute<bags2.MyBag>(a, (EObject)v.get(0), v.get(1), v.get(2)));
		
		Consumer<EObject> deleteTargetNode = (n) -> targetEdit.get().getSteps().add(new org.benchmarx.edit.DeleteNode<bags2.MyBag>(n));
		BiConsumer<EReference, List<EObject>> deleteTargetEdge = (r, ns) -> targetEdit.get().getSteps().add(new org.benchmarx.edit.DeleteEdge<bags2.MyBag>(r, ns.get(0), ns.get(1)));
		
		BiConsumer<EObject, List<EObject>> moveTargetNode = (n, ns) -> targetEdit.get().getSteps()
				.add(new MoveNode<bags2.MyBag>(n, ns.get(0), (EReference)ns.get(1), ns.get(2), (EReference)ns.get(3)));
		
		return new Bag2Helper(rootSupplier, createTargetNode, createTargetEdge, changeTargetAttribute, deleteTargetNode, moveTargetNode, deleteTargetEdge);
	}
	

	public void terminate(){
		tool.terminateSynchronisationDialogue();
	}
	
	public static Collection<BXTool<bags1.MyBag, bags2.MyBag, Decisions>> tools() {
		return Arrays.asList(
				new BXtendBag12Bag2(),
				//new PlainJavaUbtBag12Bag2(),
				new MediniQVTBag12Bag2()
			);
	}
	
	protected Bag12Bag2TestCase(BXTool<bags1.MyBag, bags2.MyBag, Decisions> tool) {
		this.tool = tool; 
	}

	protected Bag12Bag2TestCase() {
	}
	
	protected Supplier<IEdit<bags1.MyBag>> srcEdit(Runnable... ops) {
		return () -> {
			sourceEdit = new Edit<bags1.MyBag>();
			for (var op : ops) {
				op.run();
			}
			return sourceEdit;
		};
	}
	
	protected Supplier<IEdit<bags2.MyBag>> trgEdit(Runnable... ops) {
		return () -> {
			targetEdit = new Edit<bags2.MyBag>();
			for (var op : ops) {
				op.run();
			}
			return targetEdit;
		};
	}
}
