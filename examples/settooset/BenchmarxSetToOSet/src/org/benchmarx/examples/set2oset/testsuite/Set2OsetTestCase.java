package org.benchmarx.examples.set2oset.testsuite;

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
import org.benchmarx.examples.set2oset.implementations.bxagent.BXAgentSet2OSet;
import org.benchmarx.examples.set2oset.implementations.bxlang.BXLangSet2Oset;
import org.benchmarx.examples.set2oset.implementations.bxtend.BXtendSet2Oset;
import org.benchmarx.examples.set2oset.implementations.medini.MediniQVTSetToOSet;
import org.benchmarx.osets.core.OsetComparator;
import org.benchmarx.osets.core.OsetHelper;
import org.benchmarx.sets.core.SetComparator;
import org.benchmarx.sets.core.SetHelper;
import org.benchmarx.util.BenchmarxUtil;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;

@DisplayName("Set2Oset Tests")
public class Set2OsetTestCase {
	protected BXTool<sets.MySet, osets.MyOrderedSet, Decisions> tool;
	protected BiConsumer<sets.MySet, sets.MySet> setComparator;
	protected BiConsumer<osets.MyOrderedSet, osets.MyOrderedSet> osetComparator;
	protected BenchmarxUtil<sets.MySet, osets.MyOrderedSet, Decisions> util;
	protected SetHelper helperSet;
	protected OsetHelper helperOset;
	protected IEdit<sets.MySet> sourceEdit;
	protected IEdit<osets.MyOrderedSet> targetEdit;

	public Set2OsetTestCase(BXTool<sets.MySet, osets.MyOrderedSet, Decisions> tool) {
		this.tool = tool;
	}

	public Set2OsetTestCase() {
	}

	public void initialise() {
		if (tool == null) return;

		// Make sure packages are registered
		sets.SetsPackage.eINSTANCE.getSetsFactory();
		osets.OsetsPackage.eINSTANCE.getOsetsFactory();

		// Initialise all helpers
		setComparator = new SetComparator();
		osetComparator = new OsetComparator();
		util = new BenchmarxUtil<>(tool);

		// Initialise the bx tool
		tool.initiateSynchronisationDialogue();

		helperSet =
				createAnInitialiseHelperSet(() -> tool.getSourceModel(), () -> sourceEdit);
		helperOset =
				createAnInitialiseHelperOset(() -> tool.getTargetModel(), () -> targetEdit);
	}
	
	public static SetHelper createAnInitialiseHelperSet(Supplier<sets.MySet> mySet, Supplier<IEdit<sets.MySet>> sourceEdit) {
		Consumer<EObject> createSourceNode = (n) -> sourceEdit.get().getSteps().add(new CreateNode<sets.MySet>(n));
		BiConsumer<EReference, List<EObject>> createSourceEdge = (r, ns) -> sourceEdit.get().getSteps().add(new CreateEdge<sets.MySet>(r, ns.get(0), ns.get(1)));
		
		BiConsumer<EAttribute, List<?>> changeSourceAttribute = (a, v) -> sourceEdit.get().getSteps().add(new ChangeAttribute<sets.MySet>(a, (EObject)v.get(0), v.get(1), v.get(2)));
		
		Consumer<EObject> deleteSourceNode = (n) -> sourceEdit.get().getSteps().add(new org.benchmarx.edit.DeleteNode<sets.MySet>(n));
		BiConsumer<EReference, List<EObject>> deleteSourceEdge = (r, ns) -> sourceEdit.get().getSteps().add(new org.benchmarx.edit.DeleteEdge<sets.MySet>(r, ns.get(0), ns.get(1)));
		
		BiConsumer<EObject, List<EObject>> moveSourceNode = (n, ns) -> sourceEdit.get().getSteps()
				.add(new MoveNode<sets.MySet>(n, ns.get(0), (EReference)ns.get(1), ns.get(2), (EReference)ns.get(3)));
		
		return new SetHelper(mySet, createSourceNode, createSourceEdge, changeSourceAttribute, deleteSourceNode, moveSourceNode, deleteSourceEdge);
	}
	
	public static OsetHelper createAnInitialiseHelperOset(Supplier<osets.MyOrderedSet> myOSet, Supplier<IEdit<osets.MyOrderedSet>> targetEdit) {
		Consumer<EObject> createTargetNode = (n) -> targetEdit.get().getSteps().add(new CreateNode<osets.MyOrderedSet>(n));
		BiConsumer<EReference, List<EObject>> createTargetEdge = (r, ns) -> targetEdit.get().getSteps().add(new CreateEdge<osets.MyOrderedSet>(r, ns.get(0), ns.get(1)));
		
		BiConsumer<EAttribute, List<?>> changeTargetAttribute = (a, v) -> targetEdit.get().getSteps().add(new ChangeAttribute<osets.MyOrderedSet>(a, (EObject)v.get(0), v.get(1), v.get(2)));
		
		Consumer<EObject> deleteTargetNode = (n) -> targetEdit.get().getSteps().add(new org.benchmarx.edit.DeleteNode<osets.MyOrderedSet>(n));
		BiConsumer<EReference, List<EObject>> deleteTargetEdge = (r, ns) -> targetEdit.get().getSteps().add(new org.benchmarx.edit.DeleteEdge<osets.MyOrderedSet>(r, ns.get(0), ns.get(1)));
		
		BiConsumer<EObject, List<EObject>> moveTargetNode = (n, ns) -> targetEdit.get().getSteps()
				.add(new MoveNode<osets.MyOrderedSet>(n, ns.get(0), (EReference)ns.get(1), ns.get(2), (EReference)ns.get(3)));
		
		return new OsetHelper(myOSet, createTargetNode, createTargetEdge, changeTargetAttribute, deleteTargetNode, moveTargetNode, deleteTargetEdge);
	}

	@AfterEach
	public void terminate() {
		if (tool == null) return;
		tool.terminateSynchronisationDialogue();
		tool = null;  // prevent double-termination when tests also call terminate() explicitly
	}
	
	public static Collection<BXTool<sets.MySet, osets.MyOrderedSet, Decisions>> tools() {
		List<BXTool<sets.MySet, osets.MyOrderedSet, Decisions>> allTools = Arrays.asList(
				new BXtendSet2Oset(),
				//new PlainJavaUbtSet2Oset(),
				new MediniQVTSetToOSet(),
				//new IBeXTGGSetToOSet(),
				new BXLangSet2Oset(),
				new BXAgentSet2OSet()
		);
		String toolName = System.getProperty("benchmarx.tool");
		if (toolName != null && !toolName.isEmpty()) {
			return allTools.stream()
					.filter(t -> t.getClass().getSimpleName().equals(toolName))
					.collect(java.util.stream.Collectors.toList());
		}
		return allTools;
	}
	
	protected Supplier<IEdit<sets.MySet>> srcEdit(Runnable... ops) {
		return () -> {
			sourceEdit = new Edit<sets.MySet>();
			for (var op : ops) {
				op.run();
			}
			return sourceEdit;
		};
	}

	protected Supplier<IEdit<osets.MyOrderedSet>> trgEdit(Runnable... ops) {
		return () -> {
			targetEdit = new Edit<osets.MyOrderedSet>();
			for (var op : ops) {
				op.run();
			}
			return targetEdit;
		};
	}
}

