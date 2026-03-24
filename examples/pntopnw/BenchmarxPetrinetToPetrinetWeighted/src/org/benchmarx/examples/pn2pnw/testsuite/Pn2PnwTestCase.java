package org.benchmarx.examples.pn2pnw.testsuite;

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
import org.benchmarx.examples.pn2pnw.implementations.bxtend.BXtendPn2Pnw;
import org.benchmarx.examples.pn2pnw.implementations.ibextgg.IBeXTGGPetrinets;
import org.benchmarx.examples.pn2pnw.implementations.medini.MediniQVTPn2Pnw;
import org.benchmarx.examples.pn2pnw.implementations.plainjavaubt.PlainJavaUbtPn2Pnw;
import org.benchmarx.petrinet.core.PNComparator;
import org.benchmarx.petrinet.core.PNHelper;
import org.benchmarx.petrinetweighted.core.PNWComparator;
import org.benchmarx.petrinetweighted.core.PNWHelper;
import org.benchmarx.util.BenchmarxUtil;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;

public abstract class Pn2PnwTestCase {
	protected BXTool<pn.Net, pnw.Net, Decisions> tool;
	protected BiConsumer<pn.Net, pn.Net> pnComparator;
	protected BiConsumer<pnw.Net, pnw.Net> pnwComparator;
	protected BenchmarxUtil<pn.Net, pnw.Net, Decisions> util;
	protected PNHelper helperPn;
	protected PNWHelper helperPnw;
	protected IEdit<pn.Net> sourceEdit;
	protected IEdit<pnw.Net> targetEdit;

	public void initialise() {
		// Make sure packages are registered
		pn.PnPackage.eINSTANCE.getPnFactory();
		pnw.PnwPackage.eINSTANCE.getPnwFactory();
		
		// Initialise all helpers
		pnComparator = new PNComparator();
		pnwComparator = new PNWComparator();
		util = new BenchmarxUtil<>(tool);
		
		// Initialise the bx tool
		tool.initiateSynchronisationDialogue();
		
		helperPn = createAndInitialiseHelperPn(() -> tool.getSourceModel(), () -> sourceEdit);
		helperPnw = createAndInitialiseHelperPnw(() -> tool.getTargetModel(), () -> targetEdit);
	}
	
	public static PNHelper createAndInitialiseHelperPn(Supplier<pn.Net> rootSupplier,
			Supplier<IEdit<pn.Net>> sourceEdit) {
		Consumer<EObject> createSourceNode = (n) -> sourceEdit.get().getSteps().add(new CreateNode<pn.Net>(n));
		BiConsumer<EReference, List<EObject>> createSourceEdge = (r, ns) -> sourceEdit.get().getSteps().add(new CreateEdge<pn.Net>(r, ns.get(0), ns.get(1)));
		
		BiConsumer<EAttribute, List<?>> changeSourceAttribute = (a, v) -> sourceEdit.get().getSteps().add(new ChangeAttribute<pn.Net>(a, (EObject)v.get(0), v.get(1), v.get(2)));
		
		Consumer<EObject> deleteSourceNode = (n) -> sourceEdit.get().getSteps().add(new org.benchmarx.edit.DeleteNode<pn.Net>(n));
		BiConsumer<EReference, List<EObject>> deleteSourceEdge = (r, ns) -> sourceEdit.get().getSteps().add(new org.benchmarx.edit.DeleteEdge<pn.Net>(r, ns.get(0), ns.get(1)));
		
		BiConsumer<EObject, List<EObject>> moveSourceNode = (n, ns) -> sourceEdit.get().getSteps()
				.add(new MoveNode<pn.Net>(n, ns.get(0), (EReference)ns.get(1), ns.get(2), (EReference)ns.get(3)));
		
		return new PNHelper(rootSupplier, createSourceNode, createSourceEdge, changeSourceAttribute, deleteSourceNode, moveSourceNode, deleteSourceEdge);
	}
	
	public static PNWHelper createAndInitialiseHelperPnw(Supplier<pnw.Net> rootSupplier,
			Supplier<IEdit<pnw.Net>> targetEdit) {
		Consumer<EObject> createTargetNode = (n) -> targetEdit.get().getSteps().add(new CreateNode<pnw.Net>(n));
		BiConsumer<EReference, List<EObject>> createTargetEdge = (r, ns) -> targetEdit.get().getSteps().add(new CreateEdge<pnw.Net>(r, ns.get(0), ns.get(1)));
		
		BiConsumer<EAttribute, List<?>> changeTargetAttribute = (a, v) -> targetEdit.get().getSteps().add(new ChangeAttribute<pnw.Net>(a, (EObject)v.get(0), v.get(1), v.get(2)));
		
		Consumer<EObject> deleteTargetNode = (n) -> targetEdit.get().getSteps().add(new org.benchmarx.edit.DeleteNode<pnw.Net>(n));
		BiConsumer<EReference, List<EObject>> deleteTargetEdge = (r, ns) -> targetEdit.get().getSteps().add(new org.benchmarx.edit.DeleteEdge<pnw.Net>(r, ns.get(0), ns.get(1)));
		
		BiConsumer<EObject, List<EObject>> moveTargetNode = (n, ns) -> targetEdit.get().getSteps()
				.add(new MoveNode<pnw.Net>(n, ns.get(0), (EReference)ns.get(1), ns.get(2), (EReference)ns.get(3)));
		
		return new PNWHelper(rootSupplier, createTargetNode, createTargetEdge, changeTargetAttribute, deleteTargetNode, moveTargetNode, deleteTargetEdge);
	}

	
	public void terminate(){
		tool.terminateSynchronisationDialogue();
	}
	
	
	public static Collection<BXTool<pn.Net, pnw.Net, Decisions>> tools() {
		return Arrays.asList(
				new BXtendPn2Pnw(),				
				new MediniQVTPn2Pnw()
				//new IBeXTGGPetrinets()
			);
	}
	
	protected Pn2PnwTestCase(BXTool<pn.Net, pnw.Net, Decisions> tool) {
		this.tool = tool; 
	}
	
	protected Pn2PnwTestCase() {
		
	}
	
	protected Supplier<IEdit<pn.Net>> srcEdit(Runnable... ops) {
		return () -> {
			sourceEdit = new Edit<pn.Net>();
			for (var op : ops) {
				op.run();
			}
			return sourceEdit;
		};
	}
	
	protected Supplier<IEdit<pnw.Net>> trgEdit(Runnable... ops) {
		return () -> {
			targetEdit = new Edit<pnw.Net>();
			for (var op : ops) {
				op.run();
			}
			return targetEdit;
		};
	}
}
