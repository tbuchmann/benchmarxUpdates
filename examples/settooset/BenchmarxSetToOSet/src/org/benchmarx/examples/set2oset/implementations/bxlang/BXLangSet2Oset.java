package org.benchmarx.examples.set2oset.implementations.bxlang;

import dev.bxlang.generated.sets2orderedsets.Sets2OrderedSetsTransformation;
import java.io.IOException;
import java.util.function.Supplier;

import org.benchmarx.config.Configurator;
import org.benchmarx.edit.IEdit;
import org.benchmarx.emf.BXToolForEMF;
import org.benchmarx.examples.set2oset.testsuite.Decisions;
import org.benchmarx.osets.core.OsetComparator;
import org.benchmarx.sets.core.SetComparator;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;

import sets.MySet;
import sets.SetsFactory;
import osets.MyOrderedSet;


public class BXLangSet2Oset extends BXToolForEMF<sets.MySet, osets.MyOrderedSet, Decisions> {
	
	private ResourceSet set = new ResourceSetImpl();
	private Resource source;
	private Resource target;
	private Resource corr;
	
	private Sets2OrderedSetsTransformation s2ost;
	
	private Configurator<Decisions> conf;
	private Configurator<Decisions> defaultConf;
	
	private static final String RESULTPATH = "results/bxlang";

	public BXLangSet2Oset() {
		super(new SetComparator(), new OsetComparator());
		
	}

	@Override
	public void initiateSynchronisationDialogue() {
//		setConfigurator(new Configurator<Decisions>()
//				.makeDecision(Decisions., false));
				
		set.getResourceFactoryRegistry().getExtensionToFactoryMap().put("xmi", new XMIResourceFactoryImpl());		
				
		source = set.createResource(URI.createURI("pdb1.xmi"));
		target = set.createResource(URI.createURI("pdb2.xmi"));
		corr = set.createResource(URI.createURI("corr.xmi"));
		sets.MySet setRoot = SetsFactory.eINSTANCE.createMySet();
		//pdb2.Database pdb2Root = pdb2.Pdb2Factory.eINSTANCE.createDatabase();
		source.getContents().add(setRoot);
		//target.getContents().add(pdb2Root);
		
		// perform batch to establish consistent starting state
		//target = PersonToPersonTransformation.transform(source, target);
		s2ost = new Sets2OrderedSetsTransformation();
		target = s2ost.transformForward(source, target);
		
	}

	@Override
	public void performAndPropagateEdit(Supplier<IEdit<MySet>> sourceEdit,
			Supplier<IEdit<MyOrderedSet>> targetEdit) {
		// TODO Auto-generated method stub
		sourceEdit.get();
		targetEdit.get();
		
	}

	@Override
	public void setConfigurator(Configurator<Decisions> configurator) {
		if(defaultConf == null)
			defaultConf = configurator;
		conf = configurator;
	}

	@Override
	public MySet getSourceModel() {
		return (MySet) source.getContents().get(0);
	}

	@Override
	public MyOrderedSet getTargetModel() {
		return (MyOrderedSet) target.getContents().get(0);
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
	public void performAndPropagateSourceEdit(Supplier<IEdit<MySet>> sourceEdit) {
		sourceEdit.get();
		target = s2ost.transformForward(source, target);
	}
	
	@Override
	public void performAndPropagateTargetEdit(Supplier<IEdit<MyOrderedSet>> targetEdit) {
		targetEdit.get();
//		Pdb12Pdb2Transformation.Options options;
//		if (conf.decide(Decisions.PREFER_USING_FIRST_SPACE_TO_LAST)) 			
//			 options = new Pdb12Pdb2Transformation.Options("first");
//		else options = new Pdb12Pdb2Transformation.Options("last");
		//p2pt.splitFirst(conf.decide(Decisions.PREFER_USING_FIRST_SPACE_TO_LAST));
		source = s2ost.transformBackward(target, source);
	}
	
	@Override
	public void performIdleSourceEdit(Supplier<IEdit<MySet>> edit) {
		edit.get();
		//target = PersonToPersonTransformation.transform(source, target);
	}
	
	@Override
	public void performIdleTargetEdit(Supplier<IEdit<MyOrderedSet>> edit) {
		edit.get();
		//source = PersonToPersonTransformation.transformBack(target, source);
	}
	
	@Override
	public String toString() {
		return getName();
	}
	
	@Override
	public String getName() {
		return "BXLang";
	}

}
