package org.benchmarx.examples.gantt2cpm.testsuite;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.benchmarx.BXTool;
import org.benchmarx.cpm.core.CPMBuilder;
import org.benchmarx.cpm.core.CPMComparator;
import org.benchmarx.cpm.core.CPMHelper;
import org.benchmarx.edit.ChangeAttribute;
import org.benchmarx.edit.CreateEdge;
import org.benchmarx.edit.CreateNode;
import org.benchmarx.edit.Edit;
import org.benchmarx.edit.IEdit;
import org.benchmarx.edit.MoveNode;
import org.benchmarx.examples.gantt2cpm.implementations.bxagent.BXAgentGantt2Cpm;
import org.benchmarx.examples.gantt2cpm.implementations.bxtend.BXtendGantt2CPM;
import org.benchmarx.examples.gantt2cpm.implementations.medini.MediniQVTGantt2CPM;
import org.benchmarx.gantt.core.GanttComparator;
import org.benchmarx.gantt.core.GanttHelper;
import org.benchmarx.util.BenchmarxUtil;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;

import cpm.CPMNetwork;
import cpm.CpmPackage;
import gantt.GanttDiagram;
import gantt.GanttPackage;
import org.benchmarx.examples.gantt2cpm.implementations.directllm.DirectLLMGantt2Cpm;

@DisplayName("GanttToCPM Tests")
public abstract class GanttToCPMTestCase {
	protected BXTool<GanttDiagram, CPMNetwork, Decisions> tool;
	protected BiConsumer<GanttDiagram, GanttDiagram> ganttComparator;
	protected BiConsumer<CPMNetwork, CPMNetwork> cpmComparator;
	protected BenchmarxUtil<GanttDiagram, CPMNetwork, Decisions> util;
	protected GanttHelper helperGantt;
	protected CPMHelper helperCPM;
	protected IEdit<GanttDiagram> sourceEdit;
	protected IEdit<CPMNetwork> targetEdit;

	/** No-arg constructor for JUnit 5 instantiation. */
	protected GanttToCPMTestCase() {
	}

	/** Parameterised constructor kept for compatibility. */
	protected GanttToCPMTestCase(BXTool<GanttDiagram, CPMNetwork, Decisions> tool) {
		this.tool = tool;
	}

	/**
	 * Plain initialisation method — NOT annotated @BeforeEach.
	 * Each test method calls {@code this.tool = tool; initialise();} explicitly.
	 */
	public void initialise() {
		if (tool == null) return;

		// Make sure packages are registered
		GanttPackage.eINSTANCE.getName();
		CpmPackage.eINSTANCE.getName();

		// Initialise all helpers
		ganttComparator = new GanttComparator();
		cpmComparator = new CPMComparator();
		util = new BenchmarxUtil<>(tool);

		// Initialise the bx tool
		tool.initiateSynchronisationDialogue();

		helperGantt = createAndInitialiseHelperGantt(() -> tool.getSourceModel(), () -> sourceEdit);
		helperCPM = createAndInitialiseHelperCPM(() -> tool.getTargetModel(), () -> targetEdit);
	}

	public static GanttHelper createAndInitialiseHelperGantt(Supplier<GanttDiagram> rootSupplier, Supplier<IEdit<GanttDiagram>> sourceEdit) {
		Consumer<EObject> createSourceNode = (n) -> sourceEdit.get().getSteps().add(new CreateNode<GanttDiagram>(n));
		BiConsumer<EReference, List<EObject>> createSourceEdge = (r, ns) -> sourceEdit.get().getSteps().add(new CreateEdge<GanttDiagram>(r, ns.get(0), ns.get(1)));

		BiConsumer<EAttribute, List<?>> changeSourceAttribute = (a, v) -> sourceEdit.get().getSteps().add(new ChangeAttribute<GanttDiagram>(a, (EObject)v.get(0), v.get(1), v.get(2)));

		Consumer<EObject> deleteSourceNode = (n) -> sourceEdit.get().getSteps().add(new org.benchmarx.edit.DeleteNode<GanttDiagram>(n));
		BiConsumer<EReference, List<EObject>> deleteSourceEdge = (r, ns) -> sourceEdit.get().getSteps().add(new org.benchmarx.edit.DeleteEdge<GanttDiagram>(r, ns.get(0), ns.get(1)));

		BiConsumer<EObject, List<EObject>> moveSourceNode = (n, ns) -> sourceEdit.get().getSteps()
				.add(new MoveNode<GanttDiagram>(n, ns.get(0), (EReference)ns.get(1), ns.get(2), (EReference)ns.get(3)));

		return new GanttHelper(rootSupplier, createSourceNode, createSourceEdge, changeSourceAttribute, deleteSourceNode, moveSourceNode, deleteSourceEdge);
	}

	public static CPMHelper createAndInitialiseHelperCPM(Supplier<CPMNetwork> rootSupplier, Supplier<IEdit<CPMNetwork>> targetEdit) {
		Consumer<EObject> createTargetNode = (n) -> targetEdit.get().getSteps().add(new CreateNode<CPMNetwork>(n));
		BiConsumer<EReference, List<EObject>> createTargetEdge = (r, ns) -> targetEdit.get().getSteps().add(new CreateEdge<CPMNetwork>(r, ns.get(0), ns.get(1)));

		BiConsumer<EAttribute, List<?>> changeTargetAttribute = (a, v) -> targetEdit.get().getSteps().add(new ChangeAttribute<CPMNetwork>(a, (EObject)v.get(0), v.get(1), v.get(2)));

		Consumer<EObject> deleteTargetNode = (n) -> targetEdit.get().getSteps().add(new org.benchmarx.edit.DeleteNode<CPMNetwork>(n));
		BiConsumer<EReference, List<EObject>> deleteTargetEdge = (r, ns) -> targetEdit.get().getSteps().add(new org.benchmarx.edit.DeleteEdge<CPMNetwork>(r, ns.get(0), ns.get(1)));

		BiConsumer<EObject, List<EObject>> moveTargetNode = (n, ns) -> targetEdit.get().getSteps()
				.add(new MoveNode<CPMNetwork>(n, ns.get(0), (EReference)ns.get(1), ns.get(2), (EReference)ns.get(3)));

		return new CPMHelper(rootSupplier, createTargetNode, createTargetEdge, changeTargetAttribute, deleteTargetNode, moveTargetNode, deleteTargetEdge);
	}

	@AfterEach
	public void terminate() {
		if (tool == null) return;
		tool.terminateSynchronisationDialogue();
		CPMBuilder.reset();
		tool = null; // prevent double-termination
	}

	public static Collection<BXTool<GanttDiagram, CPMNetwork, Decisions>> tools() {
		List<BXTool<GanttDiagram, CPMNetwork, Decisions>> allTools = Arrays.asList(
				new BXtendGantt2CPM(),     // Currently 0 failures
				//new PlainJavaUbtGantt2Cpm(),
				new MediniQVTGantt2CPM(),
				//new BXLangGantt2Cpm(),
				new BXAgentGantt2Cpm()
		,
				new DirectLLMGantt2Cpm());
		String toolName = System.getProperty("benchmarx.tool");
		if (toolName != null && !toolName.isEmpty()) {
			return allTools.stream()
					.filter(t -> t.getClass().getSimpleName().equals(toolName))
					.collect(java.util.stream.Collectors.toList());
		}
		return allTools;
	}

	protected Supplier<IEdit<GanttDiagram>> srcEdit(Runnable... ops) {
		return () -> {
			sourceEdit = new Edit<GanttDiagram>();
			for (var op : ops) {
				op.run();
			}
			return sourceEdit;
		};
	}

	protected Supplier<IEdit<CPMNetwork>> trgEdit(Runnable... ops) {
		return () -> {
			targetEdit = new Edit<CPMNetwork>();
			for (var op : ops) {
				op.run();
			}
			return targetEdit;
		};
	}
}