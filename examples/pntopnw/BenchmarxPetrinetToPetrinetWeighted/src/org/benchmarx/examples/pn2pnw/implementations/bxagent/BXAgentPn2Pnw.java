package org.benchmarx.examples.pn2pnw.implementations.bxagent;

import java.io.IOException;
import java.util.function.Supplier;

import org.benchmarx.config.Configurator;
import org.benchmarx.edit.IEdit;
import org.benchmarx.emf.BXToolForEMF;
import org.benchmarx.examples.pn2pnw.testsuite.Decisions;
import org.benchmarx.petrinet.core.PNComparator;
import org.benchmarx.petrinetweighted.core.PNWComparator;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;

import de.tbuchmann.bxagent.pn2pnw.Pn2PnwTransformation;
import dev.bxagent.correspondence.CorrespondenceModel;
import dev.bxagent.correspondence.SyncConflictPolicy;
import dev.bxagent.correspondence.TransformationContext;
import pn.Net;

public class BXAgentPn2Pnw extends BXToolForEMF<pn.Net, pnw.Net, Decisions> {

	private ResourceSet set = new ResourceSetImpl();
	private Resource source;
	private Resource target;
	private Resource corr;
	
	private static final String RESULTPATH = "results/BXAgent";
	
	public BXAgentPn2Pnw() {		
		super(new PNComparator(), new PNWComparator());
	}
	
	@Override
	public String getName() {
		return "BXAgent";
	}
	
	@Override
	public void initiateSynchronisationDialogue() {
		set.getResourceFactoryRegistry().getExtensionToFactoryMap().put("xmi", new XMIResourceFactoryImpl());		
		
		source = set.createResource(URI.createURI("pn.xmi"));
		target = set.createResource(URI.createURI("pnw.xmi"));
		corr = set.createResource(URI.createURI("corr.xmi"));
		pn.Net root = pn.PnFactory.eINSTANCE.createNet();
		pnw.Net targetRoot = pnw.PnwFactory.eINSTANCE.createNet();
		source.getContents().add(root);
		target.getContents().add(targetRoot);
				
		org.eclipse.emf.common.util.URI corrURI = CorrespondenceModel.deriveCorrespondenceURI(
				source.getURI(), target.getURI());
		corr = CorrespondenceModel.loadOrCreate(corrURI, set);
		
		Pn2PnwTransformation.transform(source, target, corr);
	}

	@Override
	public void performAndPropagateEdit(Supplier<IEdit<Net>> sourceEdit, Supplier<IEdit<pnw.Net>> targetEdit) {
		sourceEdit.get();
		targetEdit.get();
		Pn2PnwTransformation.sync(source, target, corr,
				SyncConflictPolicy.TARGET_WINS,
				TransformationContext.DeletionPolicy.CASCADE, Pn2PnwTransformation.Options.defaults());
	}
	
	@Override
	public void performAndPropagateSourceEdit(Supplier<IEdit<pn.Net>> edit) {
		edit.get();
		Pn2PnwTransformation.transform(source, target, corr, TransformationContext.DeletionPolicy.CASCADE);
	}

	@Override
	public void performAndPropagateTargetEdit(Supplier<IEdit<pnw.Net>> edit) {
		edit.get();
		Pn2PnwTransformation.transformBack(target, source, corr, TransformationContext.DeletionPolicy.CASCADE);
	}

	@Override
	public void performIdleSourceEdit(Supplier<IEdit<pn.Net> >edit) {
		edit.get();
	}

	@Override
	public void performIdleTargetEdit(Supplier<IEdit<pnw.Net>> edit) {
		edit.get();
	}


	@Override
	public void setConfigurator(Configurator<Decisions> configurator) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Net getSourceModel() {
		return (Net) (source.getContents().get(0));
	}

	@Override
	public pnw.Net getTargetModel() {
		return (pnw.Net) (target.getContents().get(0));
	}

	@Override
	public void saveModels(String name) {
		ResourceSet set = new ResourceSetImpl();
		set.getResourceFactoryRegistry().getExtensionToFactoryMap().put(Resource.Factory.Registry.DEFAULT_EXTENSION, new XMIResourceFactoryImpl());
		URI srcURI = URI.createFileURI(RESULTPATH + "/" + name + "pn.xmi");
		URI trgURI = URI.createFileURI(RESULTPATH + "/" + name + "pnw.xmi");
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
