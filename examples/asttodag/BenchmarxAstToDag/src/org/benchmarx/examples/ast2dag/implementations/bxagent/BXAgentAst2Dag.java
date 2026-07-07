package org.benchmarx.examples.ast2dag.implementations.bxagent;

import java.io.IOException;
import java.util.function.Supplier;

import org.benchmarx.ast.core.AstComparator;
import org.benchmarx.config.Configurator;
import org.benchmarx.dag.core.DagComparator;
import org.benchmarx.edit.IEdit;
import org.benchmarx.emf.BXToolForEMF;
import org.benchmarx.examples.ast2dag.testsuite.Decisions;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;

import ast.Model;
import de.tbuchmann.bxagent.ast2dag.Ast2DagTransformation;
import dev.bxagent.correspondence.CorrespondenceModel;
import dev.bxagent.correspondence.TransformationContext;

public class BXAgentAst2Dag extends BXToolForEMF<ast.Model, dag.Model, Decisions> {

	private ResourceSet set = new ResourceSetImpl();
	private Resource source;
	private Resource target;
	private Resource corr;
	
	private static final String RESULTPATH = "results/BXAgent";
	
	public BXAgentAst2Dag() {
		super(new AstComparator(), new DagComparator());
	}
	
	@Override
	public String getName() {
		return "BXAgent";
	}
	
	@Override
	public void initiateSynchronisationDialogue() {
		set.getResourceFactoryRegistry().getExtensionToFactoryMap().put("xmi", new XMIResourceFactoryImpl());		
		
		source = set.createResource(URI.createURI("ast.xmi"));
		target = set.createResource(URI.createURI("dag.xmi"));
		corr = set.createResource(URI.createURI("corr.xmi"));
		ast.Model root = ast.AstFactory.eINSTANCE.createModel();
		source.getContents().add(root);
		dag.Model targetRoot = dag.DagFactory.eINSTANCE.createModel();
		target.getContents().add(targetRoot);
		
		// perform batch to establish consistent starting state
		Ast2DagTransformation.transform(source, target);
		org.eclipse.emf.common.util.URI corrURI = org.eclipse.emf.common.util.URI.createURI("memory://ast-dag-corr.xmi");
		corr = CorrespondenceModel.loadOrCreate(corrURI, set);
	}
	

	@Override
	public void performAndPropagateEdit(Supplier<IEdit<Model>> sourceEdit, Supplier<IEdit<dag.Model>> targetEdit) {
		sourceEdit.get();
		targetEdit.get();
		Ast2DagTransformation.transform(source, target, corr, TransformationContext.DeletionPolicy.CASCADE);
	}
	
	@Override
	public void performAndPropagateSourceEdit(Supplier<IEdit<Model>> edit) {
		edit.get();
		Ast2DagTransformation.transform(source, target, corr, TransformationContext.DeletionPolicy.CASCADE);
	}
	
	@Override
	public void performAndPropagateTargetEdit(Supplier<IEdit<dag.Model>> edit) {
		edit.get();
		Ast2DagTransformation.transformBack(target, source, corr, TransformationContext.DeletionPolicy.CASCADE);
	}
	
	@Override
	public void performIdleSourceEdit(Supplier<IEdit<Model>> edit) {
		edit.get();
	}
	
	@Override
	public void performIdleTargetEdit(Supplier<IEdit<dag.Model>> edit) {
		edit.get();
	}

	@Override
	public void setConfigurator(Configurator<Decisions> configurator) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Model getSourceModel() {
		// TODO Auto-generated method stub
		return (Model) source.getContents().get(0);
	}

	@Override
	public dag.Model getTargetModel() {
		// TODO Auto-generated method stub
		return (dag.Model) target.getContents().get(0);
	}
	
	@Override
	public void saveModels(String name) {
		ResourceSet set = new ResourceSetImpl();
		set.getResourceFactoryRegistry().getExtensionToFactoryMap().put(Resource.Factory.Registry.DEFAULT_EXTENSION, new XMIResourceFactoryImpl());
		URI srcURI = URI.createFileURI(RESULTPATH + "/" + name + "ast.xmi");
		URI trgURI = URI.createFileURI(RESULTPATH + "/" + name + "dag.xmi");
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
