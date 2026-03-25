package org.benchmarx.examples.gantt2cpm.implementations.bxtend;

import java.io.IOException;
import java.util.function.Supplier;

import org.benchmarx.config.Configurator;
import org.benchmarx.cpm.core.CPMComparator;
import org.benchmarx.edit.IEdit;
import org.benchmarx.emf.BXToolForEMF;
import org.benchmarx.examples.gantt2cpm.testsuite.Decisions;
import org.benchmarx.gantt.core.GanttComparator;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;

import cpm.CPMNetwork;
import de.ubt.ai1.m2m.gantt2cpm.rules.Gantt2cpmTransformation;
import gantt.GanttDiagram;

public class BXtendGantt2CPM extends BXToolForEMF<GanttDiagram, CPMNetwork, Decisions> {

	private ResourceSet set = new ResourceSetImpl();
	private Resource source;
	private Resource target;
	private Resource corr;
	private Gantt2cpmTransformation gantt2cpm;
	
	private static final String RESULTPATH = "results/BXtend";
	
	public BXtendGantt2CPM() {
		super(new GanttComparator(), new CPMComparator());
	}
	
	@Override
	public String getName() {
		return "BXtend";
	}

	@Override
	public void initiateSynchronisationDialogue() {
		set.getResourceFactoryRegistry().getExtensionToFactoryMap().put("xmi", new XMIResourceFactoryImpl());		
		
		source = set.createResource(URI.createURI("gantt.xmi"));
		target = set.createResource(URI.createURI("cpm.xmi"));
		corr = set.createResource(URI.createURI("corr.xmi"));
		gantt.GanttDiagram root = gantt.GanttFactory.eINSTANCE.createGanttDiagram();
		source.getContents().add(root);
		gantt2cpm = new Gantt2cpmTransformation(source, target, corr);
				
		// perform batch to establish consistent starting state
		gantt2cpm.sourceToTarget();
	}

	@Override
	public void performAndPropagateSourceEdit(Supplier<IEdit<GanttDiagram>> edit) {
		edit.get();
		gantt2cpm.sourceToTarget();
	}

	@Override
	public void performAndPropagateTargetEdit(Supplier<IEdit<CPMNetwork>> edit) {
		edit.get();
		gantt2cpm.targetToSource();
	}

	@Override
	public void performIdleSourceEdit(Supplier<IEdit<GanttDiagram>> edit) {
		edit.get();
	}

	@Override
	public void performIdleTargetEdit(Supplier<IEdit<CPMNetwork>> edit) {
		edit.get();
	}
	
	

	@Override
	public void setConfigurator(Configurator<Decisions> configurator) {
		
	}

	@Override
	public GanttDiagram getSourceModel() {
		return (GanttDiagram) source.getContents().get(0);
	}

	@Override
	public CPMNetwork getTargetModel() {
		return (CPMNetwork) target.getContents().get(0);
	}

	@Override
	public void saveModels(String name) {
		ResourceSet set = new ResourceSetImpl();
		set.getResourceFactoryRegistry().getExtensionToFactoryMap().put(Resource.Factory.Registry.DEFAULT_EXTENSION, new XMIResourceFactoryImpl());
		URI srcURI = URI.createFileURI(RESULTPATH + "/" + name + "gantt.xmi");
		URI trgURI = URI.createFileURI(RESULTPATH + "/" + name + "cpm.xmi");
		Resource resSource = set.createResource(srcURI);
		Resource resTarget = set.createResource(trgURI);
		
		EObject colSource = EcoreUtil.copy(getSourceModel());
		EObject colTarget = EcoreUtil.copy(getTargetModel());
		
		resSource.getContents().add(colSource);
		resTarget.getContents().add(colTarget);
		
		try {
			resSource.save(null);
			resTarget.save(null);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void performAndPropagateEdit(Supplier<IEdit<GanttDiagram>> sourceEdit,
			Supplier<IEdit<CPMNetwork>> targetEdit) {
		// TODO Auto-generated method stub
		sourceEdit.get();
		targetEdit.get();
	}

}
