package org.benchmarx.examples.gantt2cpm.implementations.bxlang;

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
import dev.bxlang.generated.gantt2cpm.Gantt2CPMTransformation;
import gantt.GanttDiagram;
import gantt.GanttFactory;

public class BXLangGantt2Cpm extends BXToolForEMF<gantt.GanttDiagram, cpm.CPMNetwork, Decisions> {
	
	private ResourceSet set = new ResourceSetImpl();
	private Resource source;
	private Resource target;
	private Resource corr;
	
	private Configurator<Decisions> conf;
	private Configurator<Decisions> defaultConf;
	
	private Gantt2CPMTransformation gantt2cpm;
	
	private static final String RESULTPATH = "results/bxagent";
	
	public BXLangGantt2Cpm() {
		super(new GanttComparator(), new CPMComparator());
	}
	
	@Override
	public String getName() {
		return "BXLang";
	}
	
	@Override
	public void initiateSynchronisationDialogue() {
		set.getResourceFactoryRegistry().getExtensionToFactoryMap().put("xmi", new XMIResourceFactoryImpl());		
		
		source = set.createResource(URI.createURI("gantt.xmi"));
		target = set.createResource(URI.createURI("cpm.xmi"));
		corr = set.createResource(URI.createURI("corr.xmi"));
		gantt.GanttDiagram root = GanttFactory.eINSTANCE.createGanttDiagram();
		source.getContents().add(root);
		
		gantt2cpm = new Gantt2CPMTransformation();
		gantt2cpm.transformForward(source, target);
	}

	@Override
	public void performAndPropagateEdit(Supplier<IEdit<GanttDiagram>> sourceEdit,
			Supplier<IEdit<CPMNetwork>> targetEdit) {
		sourceEdit.get();
		targetEdit.get();
		
	}
	
	@Override
	public void performIdleSourceEdit(Supplier<IEdit<GanttDiagram>> sourceEdit) {
		sourceEdit.get();
	}
	
	@Override
	public void performIdleTargetEdit(Supplier<IEdit<CPMNetwork>> targetEdit) {
		targetEdit.get();
	}
	
	@Override
	public void performAndPropagateSourceEdit(Supplier<IEdit<GanttDiagram>> sourceEdit) {
		sourceEdit.get();
		gantt2cpm.transformForward(source, target);
	}
	
	@Override
	public void performAndPropagateTargetEdit(Supplier<IEdit<CPMNetwork>> targetEdit) {
		targetEdit.get();
		gantt2cpm.transformBackward(target, source);
	}

	@Override
	public void setConfigurator(Configurator<Decisions> configurator) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public GanttDiagram getSourceModel() {
		// TODO Auto-generated method stub
		return (GanttDiagram) source.getContents().get(0);
	}

	@Override
	public CPMNetwork getTargetModel() {
		// TODO Auto-generated method stub
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

}
