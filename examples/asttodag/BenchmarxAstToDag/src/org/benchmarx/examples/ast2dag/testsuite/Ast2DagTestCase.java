package org.benchmarx.examples.ast2dag.testsuite;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.benchmarx.BXTool;
import org.benchmarx.ast.core.AstComparator;
import org.benchmarx.ast.core.AstHelper;
import org.benchmarx.dag.core.DagComparator;
import org.benchmarx.dag.core.DagHelper;
import org.benchmarx.edit.ChangeAttribute;
import org.benchmarx.edit.CreateEdge;
import org.benchmarx.edit.CreateNode;
import org.benchmarx.edit.Edit;
import org.benchmarx.edit.IEdit;
import org.benchmarx.edit.MoveNode;
import org.benchmarx.examples.ast2dag.implementations.bxagent.BXAgentAst2Dag;
import org.benchmarx.examples.ast2dag.implementations.bxlang.BXLangAst2Dag;
import org.benchmarx.examples.ast2dag.implementations.bxtend.BXtendAst2Dag;
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
public abstract class Ast2DagTestCase {
	protected BXTool<ast.Model, dag.Model, Decisions> tool;
	protected BiConsumer<ast.Model, ast.Model> astComparator;
	protected BiConsumer<dag.Model, dag.Model> dagComparator;
	protected BenchmarxUtil<ast.Model, dag.Model, Decisions> util;
	protected AstHelper helperAst;
	protected DagHelper helperDag;
	protected IEdit<ast.Model> sourceEdit;
	protected IEdit<dag.Model> targetEdit;

	@Before
	public void initialise() {
		// Make sure packages are registered
		ast.AstPackage.eINSTANCE.getAstFactory();
		dag.DagPackage.eINSTANCE.getDagFactory();
		
		// Initialise all helpers
		astComparator = new AstComparator();
		dagComparator = new DagComparator();
		util = new BenchmarxUtil<>(tool);		
		
		// Initialise the bx tool
		tool.initiateSynchronisationDialogue();
		
		helperAst = createAndInitialiseHelperAst(() -> tool.getSourceModel(), () -> sourceEdit);
		helperDag = createAndInitialiseHelperDag(() -> tool.getTargetModel(), () -> targetEdit);
	}
	
	public static AstHelper createAndInitialiseHelperAst(Supplier<ast.Model> rootSupplier, 
			Supplier<IEdit<ast.Model>> sourceEdit) {
		Consumer<EObject> createSourceNode = (n) -> sourceEdit.get().getSteps().add(new CreateNode<ast.Model>(n));
		BiConsumer<EReference, List<EObject>> createSourceEdge = (r, ns) -> sourceEdit.get().getSteps().add(new CreateEdge<ast.Model>(r, ns.get(0), ns.get(1)));
		
		BiConsumer<EAttribute, List<?>> changeSourceAttribute = (a, v) -> sourceEdit.get().getSteps().add(new ChangeAttribute<ast.Model>(a, (EObject)v.get(0), v.get(1), v.get(2)));
		
		Consumer<EObject> deleteSourceNode = (n) -> sourceEdit.get().getSteps().add(new org.benchmarx.edit.DeleteNode<ast.Model>(n));
		BiConsumer<EReference, List<EObject>> deleteSourceEdge = (r, ns) -> sourceEdit.get().getSteps().add(new org.benchmarx.edit.DeleteEdge<ast.Model>(r, ns.get(0), ns.get(1)));
		
		BiConsumer<EObject, List<EObject>> moveSourceNode = (n, ns) -> sourceEdit.get().getSteps()
				.add(new MoveNode<ast.Model>(n, ns.get(0), (EReference)ns.get(1), ns.get(2), (EReference)ns.get(3)));
		
		return new AstHelper(rootSupplier, createSourceNode, createSourceEdge, changeSourceAttribute, deleteSourceNode, moveSourceNode, deleteSourceEdge);
	}
	
	public static DagHelper createAndInitialiseHelperDag(Supplier<dag.Model> rootSupplier, 
			Supplier<IEdit<dag.Model>> targetEdit) {
		
		Consumer<EObject> createTargetNode = (n) -> targetEdit.get().getSteps().add(new CreateNode<dag.Model>(n));
		BiConsumer<EReference, List<EObject>> createTargetEdge = (r, ns) -> targetEdit.get().getSteps().add(new CreateEdge<dag.Model>(r, ns.get(0), ns.get(1)));
		
		BiConsumer<EAttribute, List<?>> changeTargetAttribute = (a, v) -> targetEdit.get().getSteps().add(new ChangeAttribute<dag.Model>(a, (EObject)v.get(0), v.get(1), v.get(2)));
		
		Consumer<EObject> deleteTargetNode = (n) -> targetEdit.get().getSteps().add(new org.benchmarx.edit.DeleteNode<dag.Model>(n));
		BiConsumer<EReference, List<EObject>> deleteTargetEdge = (r, ns) -> targetEdit.get().getSteps().add(new org.benchmarx.edit.DeleteEdge<dag.Model>(r, ns.get(0), ns.get(1)));
		
		BiConsumer<EObject, List<EObject>> moveTargetNode = (n, ns) -> targetEdit.get().getSteps()
				.add(new MoveNode<dag.Model>(n, ns.get(0), (EReference)ns.get(1), ns.get(2), (EReference)ns.get(3)));
		
		return new DagHelper(rootSupplier, createTargetNode, createTargetEdge, changeTargetAttribute, deleteTargetNode, moveTargetNode, deleteTargetEdge);
	}

	@After
	public void terminate(){
		tool.terminateSynchronisationDialogue();
	}
	
	@Parameters(name = "{0}")
	public static Collection<BXTool<ast.Model, dag.Model, Decisions>> tools() {
		return Arrays.asList(
				new BXtendAst2Dag(),				
//				, new MediniQVTAst2Dag(),
				new BXLangAst2Dag(),
				new BXAgentAst2Dag()
			);
	}
	
	protected Ast2DagTestCase(BXTool<ast.Model, dag.Model, Decisions> tool) {
		this.tool = tool; 
	}
	
	protected Supplier<IEdit<ast.Model>> srcEdit(Runnable... ops) {
		return () -> {
			sourceEdit = new Edit<ast.Model>();
			for (var op : ops) {
				op.run();
			}
			return sourceEdit;
		};
	}
	
	protected Supplier<IEdit<dag.Model>> trgEdit(Runnable... ops) {
		return () -> {
			targetEdit = new Edit<dag.Model>();
			for (var op : ops) {
				op.run();
			}
			return targetEdit;
		};
	}
}
