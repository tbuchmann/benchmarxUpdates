package org.benchmarx.examples.ecore2sql.implementations.bxagent;

import java.io.IOException;
import java.util.function.Supplier;

import org.benchmarx.config.Configurator;
import org.benchmarx.ecore.core.EcoreComparator;
import org.benchmarx.edit.IEdit;
import org.benchmarx.emf.BXToolForEMF;
import org.benchmarx.examples.ecore2sql.testsuite.Decisions;
import org.benchmarx.sql.core.SQLComparator;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;

import de.tbuchmann.bxagent.ecore2sql.Ecore2SqlTransformation;
import dev.emtagent.correspondence.CorrespondenceModel;
import dev.emtagent.correspondence.TransformationContext;
import sql.Schema;
import sql.SqlFactory;

public class BXAgentEcore2SQL extends BXToolForEMF<EPackage, Schema, Decisions> {

	private ResourceSet set = new ResourceSetImpl();
	private Resource source;
	private Resource target;
	private Resource corr;

private static final String RESULTPATH = "results/BXAgent";
	
	public BXAgentEcore2SQL() {
		super(new EcoreComparator(), new SQLComparator());
	}
	
	@Override
	public String getName() {
		return "BXAgent";
	}

	@Override
	public void initiateSynchronisationDialogue() {
		set.getResourceFactoryRegistry().getExtensionToFactoryMap().put("xmi", new XMIResourceFactoryImpl());
		set.getResourceFactoryRegistry().getExtensionToFactoryMap().put("ecore", new EcoreResourceFactoryImpl());
		
		source = set.createResource(URI.createURI("ecore.ecore"));
		target = set.createResource(URI.createURI("sql.xmi"));
		corr = set.createResource(URI.createURI("corr.xmi"));
		EPackage root = EcoreFactory.eINSTANCE.createEPackage();
		source.getContents().add(root);
		Schema targetRoot = SqlFactory.eINSTANCE.createSchema();
		target.getContents().add(targetRoot);
				
		// perform batch to establish consistent starting state
		Ecore2SqlTransformation.transform(source, target);
		org.eclipse.emf.common.util.URI corrURI = CorrespondenceModel.deriveCorrespondenceURI(
				source.getURI(), target.getURI());
		corr = CorrespondenceModel.loadOrCreate(corrURI, set);
	}
	
	@Override
	public void performAndPropagateSourceEdit(Supplier<IEdit<EPackage>> edit) {
		edit.get();
		Ecore2SqlTransformation.transform(source, target, corr, TransformationContext.DeletionPolicy.CASCADE);
	}

	@Override
	public void performAndPropagateTargetEdit(Supplier<IEdit<Schema>> edit) {
		edit.get();
		Ecore2SqlTransformation.transformBack(target, source, corr, TransformationContext.DeletionPolicy.CASCADE);
	}

	@Override
	public void performIdleSourceEdit(Supplier<IEdit<EPackage>> edit) {
		edit.get();
	}

	@Override
	public void performIdleTargetEdit(Supplier<IEdit<Schema>> edit) {
		edit.get();
	}

	@Override
	public void setConfigurator(Configurator<Decisions> configurator) {
		
	}

	@Override
	public EPackage getSourceModel() {
		return (EPackage) source.getContents().get(0);
	}

	@Override
	public Schema getTargetModel() {
		return (Schema) target.getContents().get(0);
	}

	@Override
	public void saveModels(String name) {
		ResourceSet set = new ResourceSetImpl();
		set.getResourceFactoryRegistry().getExtensionToFactoryMap().put(Resource.Factory.Registry.DEFAULT_EXTENSION, new XMIResourceFactoryImpl());
		set.getResourceFactoryRegistry().getExtensionToFactoryMap().put(".ecore", new EcoreResourceFactoryImpl());
		URI srcURI = URI.createFileURI(RESULTPATH + "/" + name + "ecore.ecore");
		URI trgURI = URI.createFileURI(RESULTPATH + "/" + name + "sql.xmi");
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
	public void performAndPropagateEdit(Supplier<IEdit<EPackage>> sourceEdit, Supplier<IEdit<Schema>> targetEdit) {
		// TODO Auto-generated method stub
		sourceEdit.get();
		targetEdit.get();
	}
}
