package org.benchmarx.examples.pdb12pdb2.implementations.bxagent;

import java.io.IOException;
import java.util.function.Supplier;

import org.benchmarx.config.Configurator;
import org.benchmarx.edit.IEdit;
import org.benchmarx.emf.BXToolForEMF;
import org.benchmarx.examples.pdb12pdb2.testsuite.Decisions;
import org.benchmarx.pdb1.core.Pdb1Comparator;
import org.benchmarx.pdb2.core.Pdb2Comparator;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;

import de.tbuchmann.bxagent.pdb12pdb2.Pdb12Pdb2Transformation;
import pdb1.Database;
import pdb1.Pdb1Factory;

public class BXAgentPdb12Pdb2 extends BXToolForEMF<pdb1.Database, pdb2.Database, Decisions> {
	
	private ResourceSet set = new ResourceSetImpl();
	private Resource source;
	private Resource target;
	private Resource corr;
	//private PersonToPersonTransformation p2pt;
	
	private Configurator<Decisions> conf;
	private Configurator<Decisions> defaultConf;
	
	private static final String RESULTPATH = "results/bxagent";

	public BXAgentPdb12Pdb2() {
		super(new Pdb1Comparator(), new Pdb2Comparator());
		
	}

	@Override
	public void initiateSynchronisationDialogue() {
		setConfigurator(new Configurator<Decisions>()
				.makeDecision(Decisions.PREFER_USING_FIRST_SPACE_TO_LAST, false));
				
		set.getResourceFactoryRegistry().getExtensionToFactoryMap().put("xmi", new XMIResourceFactoryImpl());		
				
		source = set.createResource(URI.createURI("pdb1.xmi"));
		target = set.createResource(URI.createURI("pdb2.xmi"));
		corr = set.createResource(URI.createURI("corr.xmi"));
		pdb1.Database pdb1Root = Pdb1Factory.eINSTANCE.createDatabase();
		pdb2.Database pdb2Root = pdb2.Pdb2Factory.eINSTANCE.createDatabase();
		source.getContents().add(pdb1Root);
		target.getContents().add(pdb2Root);
		
		// perform batch to establish consistent starting state
		//target = PersonToPersonTransformation.transform(source, target);
	}

	@Override
	public void performAndPropagateEdit(Supplier<IEdit<Database>> sourceEdit,
			Supplier<IEdit<pdb2.Database>> targetEdit) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setConfigurator(Configurator<Decisions> configurator) {
		if(defaultConf == null)
			defaultConf = configurator;
		conf = configurator;
	}

	@Override
	public Database getSourceModel() {
		return (pdb1.Database) source.getContents().get(0);
	}

	@Override
	public pdb2.Database getTargetModel() {
		return (pdb2.Database) target.getContents().get(0);
	}

	@Override
	public void saveModels(String name) {
		ResourceSet set = new ResourceSetImpl();
		set.getResourceFactoryRegistry().getExtensionToFactoryMap().put(Resource.Factory.Registry.DEFAULT_EXTENSION, new XMIResourceFactoryImpl());
		URI srcURI = URI.createFileURI(RESULTPATH + "/" + name + "pdb1.xmi");
		URI trgURI = URI.createFileURI(RESULTPATH + "/" + name + "pdb2.xmi");
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
	public void performAndPropagateSourceEdit(Supplier<IEdit<Database>> sourceEdit) {
		sourceEdit.get();
		Pdb12Pdb2Transformation.transform(source, target);
	}
	
	@Override
	public void performAndPropagateTargetEdit(Supplier<IEdit<pdb2.Database>> targetEdit) {
		targetEdit.get();
		Pdb12Pdb2Transformation.Options options;
		if (conf.decide(Decisions.PREFER_USING_FIRST_SPACE_TO_LAST)) 			
			 options = new Pdb12Pdb2Transformation.Options("first");
		else options = new Pdb12Pdb2Transformation.Options("last");
		Pdb12Pdb2Transformation.transformBack(target, source, options);
	}
	
	@Override
	public void performIdleSourceEdit(Supplier<IEdit<pdb1.Database>> edit) {
		edit.get();
		//target = PersonToPersonTransformation.transform(source, target);
	}
	
	@Override
	public void performIdleTargetEdit(Supplier<IEdit<pdb2.Database>> edit) {
		edit.get();
		//source = PersonToPersonTransformation.transformBack(target, source);
	}
	
	@Override
	public String toString() {
		return getName();
	}
	
	@Override
	public String getName() {
		return "BXAgent";
	}

}
