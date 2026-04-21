package org.benchmarx.examples.ecore2sql.testsuite;


import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.benchmarx.BXTool;
import org.benchmarx.ecore.core.EcoreComparator;
import org.benchmarx.ecore.core.EcoreHelper;
import org.benchmarx.edit.ChangeAttribute;
import org.benchmarx.edit.CreateEdge;
import org.benchmarx.edit.CreateNode;
import org.benchmarx.edit.Edit;
import org.benchmarx.edit.IEdit;
import org.benchmarx.edit.MoveNode;
import org.benchmarx.examples.ecore2sql.implementations.bxagent.BXAgentEcore2SQL;
import org.benchmarx.examples.ecore2sql.implementations.bxtend.BXtendEcore2SQL;
import org.benchmarx.sql.core.SQLComparator;
import org.benchmarx.sql.core.SQLHelper;
import org.benchmarx.util.BenchmarxUtil;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EcorePackage;
import org.junit.runners.Parameterized.Parameters;

import sql.Schema;
import sql.SqlPackage;

public abstract class EcoreToSQLTestCase {
	protected BXTool<EPackage, Schema, Decisions> tool;
	protected BiConsumer<EPackage, EPackage> ecoreComparator;
	protected BiConsumer<Schema, Schema> sqlComparator;
	protected BenchmarxUtil<EPackage, Schema, Decisions> util;
	protected EcoreHelper helperEcore;
	protected SQLHelper helperSQL;
	protected IEdit<EPackage> sourceEdit;
	protected IEdit<Schema> targetEdit;
		
	public void initialise() {
		// Make sure packages are registered
		EcorePackage.eINSTANCE.getName();
		SqlPackage.eINSTANCE.getName();
		
		// Initialise all helpers
		ecoreComparator = new EcoreComparator();
		sqlComparator = new SQLComparator();
		util = new BenchmarxUtilForEcoreToSQL(tool);//new BenchmarxUtilForEcoreToSQL(tool);
//		helperEcore = new EcoreHelper();
//		helperSQL = new SQLHelper();
		
		// Initialise the bx tool
		tool.initiateSynchronisationDialogue();
		helperEcore = createAndInitialiseHelperEcore(() -> tool.getSourceModel(), () -> sourceEdit);
		helperSQL = createAndInitialiseHelperSQL(() -> tool.getTargetModel(), () -> targetEdit);
	}
	
	protected static EcoreHelper createAndInitialiseHelperEcore(Supplier<EPackage> rootSupplier, Supplier<IEdit<EPackage>> sourceEdit) {
		Consumer<EObject> createSourceNode = (n) -> sourceEdit.get().getSteps().add(new CreateNode<EPackage>(n));
		BiConsumer<EReference, List<EObject>> createSourceEdge = (r, ns) -> sourceEdit.get().getSteps().add(new CreateEdge<EPackage>(r, ns.get(0), ns.get(1)));
		
		BiConsumer<EAttribute, List<?>> changeSourceAttribute = (a, v) -> sourceEdit.get().getSteps().add(new ChangeAttribute<EPackage>(a, (EObject)v.get(0), v.get(1), v.get(2)));
		
		Consumer<EObject> deleteSourceNode = (n) -> sourceEdit.get().getSteps().add(new org.benchmarx.edit.DeleteNode<EPackage>(n));
		BiConsumer<EReference, List<EObject>> deleteSourceEdge = (r, ns) -> sourceEdit.get().getSteps().add(new org.benchmarx.edit.DeleteEdge<EPackage>(r, ns.get(0), ns.get(1)));
		
		BiConsumer<EObject, List<EObject>> moveSourceNode = (n, ns) -> sourceEdit.get().getSteps()
				.add(new MoveNode<EPackage>(n, ns.get(0), (EReference)ns.get(1), ns.get(2), (EReference)ns.get(3)));
		
		return new EcoreHelper(rootSupplier, createSourceNode, createSourceEdge, changeSourceAttribute, deleteSourceNode, moveSourceNode, deleteSourceEdge);
	}
	
	protected static SQLHelper createAndInitialiseHelperSQL(Supplier<Schema> rootSupplier, Supplier<IEdit<Schema>> targetEdit) {
		Consumer<EObject> createTargetNode = (n) -> targetEdit.get().getSteps().add(new CreateNode<Schema>(n));
		BiConsumer<EReference, List<EObject>> createTargetEdge = (r, ns) -> targetEdit.get().getSteps().add(new CreateEdge<Schema>(r, ns.get(0), ns.get(1)));
		
		BiConsumer<EAttribute, List<?>> changeTargetAttribute = (a, v) -> targetEdit.get().getSteps().add(new ChangeAttribute<Schema>(a, (EObject)v.get(0), v.get(1), v.get(2)));
		
		Consumer<EObject> deleteTargetNode = (n) -> targetEdit.get().getSteps().add(new org.benchmarx.edit.DeleteNode<Schema>(n));
		BiConsumer<EReference, List<EObject>> deleteTargetEdge = (r, ns) -> targetEdit.get().getSteps().add(new org.benchmarx.edit.DeleteEdge<Schema>(r, ns.get(0), ns.get(1)));
		
		BiConsumer<EObject, List<EObject>> moveTargetNode = (n, ns) -> targetEdit.get().getSteps()
				.add(new MoveNode<Schema>(n, ns.get(0), (EReference)ns.get(1), ns.get(2), (EReference)ns.get(3)));
		
		return new SQLHelper(rootSupplier, createTargetNode, createTargetEdge, changeTargetAttribute, deleteTargetNode, moveTargetNode, deleteTargetEdge);
	}
	
	public void terminate(){
		tool.terminateSynchronisationDialogue();
	}
	
	@Parameters(name = "{0}")
	public static Collection<BXTool<EPackage, Schema, Decisions>> tools() {
		return Arrays.asList(
				new BXtendEcore2SQL() ,  // Currently 0 failures				
//				new MediniQVTEcore2SQL(),
//				new IBeXTGGEcoreToSQL()
				new BXAgentEcore2SQL()
			);
	}
	
	protected EcoreToSQLTestCase() {
		
	}
	
	protected Supplier<IEdit<EPackage>> srcEdit(Runnable... ops) {
		return () -> {
			sourceEdit = new Edit<EPackage>();
			for (var op : ops) {
				op.run();
			}
			return sourceEdit;
		};
	}
	
	protected Supplier<IEdit<Schema>> trgEdit(Runnable... ops) {
		return () -> {
			targetEdit = new Edit<Schema>();
			for (var op : ops) {
				op.run();
			}
			return targetEdit;
		};
	}
}
