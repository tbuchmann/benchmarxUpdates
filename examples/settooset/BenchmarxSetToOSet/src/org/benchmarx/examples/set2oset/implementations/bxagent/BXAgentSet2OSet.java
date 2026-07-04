package org.benchmarx.examples.set2oset.implementations.bxagent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

import de.tbuchmann.bxagent.set2oset.Sets2OsetsTransformation;
import de.tbuchmann.bxagent.set2oset.Sets2OsetsTransformation.PostProcessor;
import dev.bxagent.correspondence.CorrespondenceModel;
import dev.bxagent.correspondence.TransformationContext;
import osets.MyOrderedSet;
import osets.OsetsFactory;
import sets.MySet;
import sets.SetsFactory;



public class BXAgentSet2OSet extends BXToolForEMF<sets.MySet, osets.MyOrderedSet, Decisions> {
	
	private ResourceSet set = new ResourceSetImpl();
	private Resource source;
	private Resource target;
	private Resource corr;
	//private PersonToPersonTransformation p2pt;
	private PostProcessor linkedListHook;
	
	private Configurator<Decisions> conf;
	private Configurator<Decisions> defaultConf;
	
	private static final String RESULTPATH = "results/bxagent";

	public BXAgentSet2OSet() {
		super(new SetComparator(), new OsetComparator());
		
	}

	@Override
	public void initiateSynchronisationDialogue() {
		
				
		set.getResourceFactoryRegistry().getExtensionToFactoryMap().put("xmi", new XMIResourceFactoryImpl());		
				
		source = set.createResource(URI.createURI("pdb1.xmi"));
		target = set.createResource(URI.createURI("pdb2.xmi"));
		corr = set.createResource(URI.createURI("corr.xmi"));
		sets.MySet setRoot = SetsFactory.eINSTANCE.createMySet();
		osets.MyOrderedSet osetRoot = OsetsFactory.eINSTANCE.createMyOrderedSet();
		
		source.getContents().add(setRoot);
		target.getContents().add(osetRoot);
		
		// perform batch to establish consistent starting state
		Sets2OsetsTransformation.transform(source, target);
		org.eclipse.emf.common.util.URI corrURI = CorrespondenceModel.deriveCorrespondenceURI(
				source.getURI(), target.getURI());
		corr = CorrespondenceModel.loadOrCreate(corrURI, set);
		
		linkedListHook = new PostProcessor() {
			@Override
			public void afterTransform(Resource readFrom, Resource writeTo,
					Map<EObject, EObject> trace, List<EObject> created, List<EObject> deleted) {
				// after each transformation, we need to update the "next" references in the ordered set
				if (created.isEmpty())
					return;
				MyOrderedSet oset = (MyOrderedSet) writeTo.getContents().get(0);
				// Tail der bestehenden Liste finden:
				// letztes Element ohne next, das nicht neu ist
				osets.Element tail = null;
				for (osets.Element e : oset.getElements()) {
					if (!created.contains(e) && e.getNext() == null) {
						tail = e;
						break;
					}
				}
				// neue Elemente in Comtainment-Reihenfolge anhängen
				// (nicht über created iterieren - HashMap.-Reihenfolge ist undefiniert!)
				for (osets.Element e : oset.getElements()) {
					if (!created.contains(e)) continue; 
					if (tail != null) {
						tail.setNext(e);
						//e.setPrevious(tail);
					}
					tail = e;
					
				}
			}
			
			@Override
			public void beforeDeletions(List<EObject> toDelete) {
				// before deletions, we need to update the "next" references in the ordered set
				for (EObject e : toDelete) {
					if (e instanceof osets.Element) {
						osets.Element elem = (osets.Element) e;
						osets.Element prev = elem.getPrevious();
						osets.Element next = elem.getNext();
						if (prev != null && next != null) {
							prev.setNext(next);
							next.setPrevious(prev);
						} else if (prev != null) {
							prev.setNext(null);
						} else if (next != null) {
							next.setPrevious(null);
						}
					}
				}
			}
		};
	}

	@Override
	public void performAndPropagateEdit(Supplier<IEdit<sets.MySet>> sourceEdit,
			Supplier<IEdit<osets.MyOrderedSet>> targetEdit) {
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
		URI srcURI = URI.createFileURI(RESULTPATH + "/" + name + "set.xmi");
		URI trgURI = URI.createFileURI(RESULTPATH + "/" + name + "oset.xmi");
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
		Sets2OsetsTransformation.transform(source, target, corr, TransformationContext.DeletionPolicy.CASCADE, linkedListHook);
	}
	
	@Override
	public void performAndPropagateTargetEdit(Supplier<IEdit<MyOrderedSet>> targetEdit) {
		targetEdit.get();		
		
		Sets2OsetsTransformation.transformBack(target, source, corr, TransformationContext.DeletionPolicy.CASCADE);
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
		return "BXAgent";
	}


}
