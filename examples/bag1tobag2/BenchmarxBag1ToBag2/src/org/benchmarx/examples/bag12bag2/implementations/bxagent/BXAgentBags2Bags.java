package org.benchmarx.examples.bag12bag2.implementations.bxagent;

import java.io.IOException;
import java.util.function.Supplier;

import org.benchmarx.bags1.core.Bag1Comparator;
import org.benchmarx.bags2.core.Bag2Comparator;
import org.benchmarx.config.Configurator;
import org.benchmarx.edit.IEdit;
import org.benchmarx.emf.BXToolForEMF;
import org.benchmarx.examples.bag12bag2.testsuite.Decisions;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;

import bags1.MyBag;
import de.tbuchmann.bxagent.bags2bags.Bags12Bags2Transformation;
import dev.bxagent.correspondence.CorrespondenceModel;
import dev.bxagent.correspondence.SyncConflictPolicy;
import dev.bxagent.correspondence.TransformationContext;


public class BXAgentBags2Bags  extends BXToolForEMF<bags1.MyBag, bags2.MyBag, Decisions> {

	private ResourceSet set = new ResourceSetImpl();
	private Resource source;
	private Resource target;
	private Resource corr;		
	
	private static final String RESULTPATH = "results/bxagent";
	
	public BXAgentBags2Bags() {
		super(new Bag1Comparator(), new Bag2Comparator());
	}
	
	@Override
	public String getName() {
		return "BXAgent";
	}
	
	@Override
	public String toString() {
		return this.getName();
	}

	@Override
	public void initiateSynchronisationDialogue() {
		set.getResourceFactoryRegistry().getExtensionToFactoryMap().put("xmi", new XMIResourceFactoryImpl());		
		
		source = set.createResource(URI.createURI("bag1.xmi"));
		target = set.createResource(URI.createURI("bag2.xmi"));
		corr = set.createResource(URI.createURI("corr.xmi"));
		bags1.MyBag root = bags1.Bags1Factory.eINSTANCE.createMyBag();
		source.getContents().add(root);
		bags2.MyBag targetRoot = bags2.Bags2Factory.eINSTANCE.createMyBag();
		target.getContents().add(targetRoot);
				
		// perform batch to establish consistent starting state
		Bags12Bags2Transformation.transform(source, target);
		org.eclipse.emf.common.util.URI corrURI = CorrespondenceModel.deriveCorrespondenceURI(
				source.getURI(), target.getURI());
		corr = CorrespondenceModel.loadOrCreate(corrURI, set);
	}
	
	@Override
	public void performAndPropagateSourceEdit(Supplier<IEdit<bags1.MyBag>> edit) {
		edit.get();
		Bags12Bags2Transformation.transform(source, target, corr, TransformationContext.DeletionPolicy.CASCADE);
	}

	@Override
	public void performAndPropagateTargetEdit(Supplier<IEdit<bags2.MyBag>> edit) {
		edit.get();
		Bags12Bags2Transformation.transformBack(target, source, corr, TransformationContext.DeletionPolicy.CASCADE);
	}

	@Override
	public void performIdleSourceEdit(Supplier<IEdit<bags1.MyBag>> edit) {
		edit.get();
	}

	@Override
	public void performIdleTargetEdit(Supplier<IEdit<bags2.MyBag>> edit) {
		edit.get();
	}

	@Override
	public void setConfigurator(Configurator<Decisions> configurator) {

	}

	@Override
	public bags1.MyBag getSourceModel() {
		return (bags1.MyBag) source.getContents().get(0);
	}

	@Override
	public bags2.MyBag getTargetModel() {
		return (bags2.MyBag) target.getContents().get(0);
	}

	@Override
	public void saveModels(String name) {
		ResourceSet set = new ResourceSetImpl();
		set.getResourceFactoryRegistry().getExtensionToFactoryMap().put(Resource.Factory.Registry.DEFAULT_EXTENSION, new XMIResourceFactoryImpl());
		URI srcURI = URI.createFileURI(RESULTPATH + "/" + name + "bag1.xmi");
		URI trgURI = URI.createFileURI(RESULTPATH + "/" + name + "bag2.xmi");
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
	public void performAndPropagateEdit(Supplier<IEdit<MyBag>> sourceEdit, Supplier<IEdit<bags2.MyBag>> targetEdit) {
		// TODO Auto-generated method stub
		sourceEdit.get();
		targetEdit.get();
		Bags12Bags2Transformation.Options options = new Bags12Bags2Transformation.Options();
		Bags12Bags2Transformation.sync(source, target, corr, 
				SyncConflictPolicy.TARGET_WINS,
				TransformationContext.DeletionPolicy.CASCADE, options);
	}

}
