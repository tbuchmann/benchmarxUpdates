package org.benchmarx.examples.gantt2cpm.implementations.bxagent;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import cpm.CpmFactory;
import de.tbuchmann.bxagent.gantt2cpm.Gantt2CpmTransformation;
import de.tbuchmann.bxagent.gantt2cpm.Gantt2CpmTransformation.PostProcessor;
import dev.bxagent.correspondence.CorrespondenceModel;
import dev.bxagent.correspondence.TransformationContext;
import gantt.GanttDiagram;
import gantt.GanttFactory;

public class BXAgentGantt2Cpm extends BXToolForEMF<gantt.GanttDiagram, cpm.CPMNetwork, Decisions> {
	
	private ResourceSet set = new ResourceSetImpl();
	private Resource source;
	private Resource target;
	private Resource corr;
	
	private PostProcessor forwardHook;
	private PostProcessor backwardHook;
	
	private Configurator<Decisions> conf;
	private Configurator<Decisions> defaultConf;
	
	private static final String RESULTPATH = "results/bxagent";

	public BXAgentGantt2Cpm() {
		super(new GanttComparator(), new CPMComparator());
		
	}

	@Override
	public void initiateSynchronisationDialogue() {
		
				
		set.getResourceFactoryRegistry().getExtensionToFactoryMap().put("xmi", new XMIResourceFactoryImpl());		
				
		source = set.createResource(URI.createURI("gantt.xmi"));
		target = set.createResource(URI.createURI("cpm.xmi"));
		corr = set.createResource(URI.createURI("corr.xmi"));
		gantt.GanttDiagram ganttRoot = GanttFactory.eINSTANCE.createGanttDiagram();
		cpm.CPMNetwork cpmRoot = CpmFactory.eINSTANCE.createCPMNetwork();
		source.getContents().add(ganttRoot);
		target.getContents().add(cpmRoot);
		
		// perform batch to establish consistent starting state
		Gantt2CpmTransformation.transform(source, target);
		org.eclipse.emf.common.util.URI corrURI = CorrespondenceModel.deriveCorrespondenceURI(
				source.getURI(), target.getURI());
		corr = CorrespondenceModel.loadOrCreate(corrURI, set);
		
		forwardHook = new PostProcessor() {
			  private int eventCounter = 1; // Hilfszähler für eindeutige Event-Namen
		      @Override
		      public void afterTransform(Resource readFrom, Resource writeTo, Map<EObject, EObject> objectMap,
		    		  List<EObject> created, List<EObject> updated) {

		          gantt.GanttDiagram gantt =
		              (gantt.GanttDiagram) readFrom.getContents().get(0);
		          cpm.CPMNetwork network =
		              (cpm.CPMNetwork) writeTo.getContents().get(0);
		          CpmFactory factory = CpmFactory.eINSTANCE;

		          // Phase 1: Events für Activities erzeugen
		          // Für neue Activities (in `created`): neue Events anlegen
		          // Für bestehende (inkrementell): vorhandene Events lesen
		          Map<gantt.Activity, cpm.Event> startEvents = new HashMap<>();
		          Map<gantt.Activity, cpm.Event> endEvents   = new HashMap<>();

		          for (gantt.Element el : gantt.getElements()) {
		              if (!(el instanceof gantt.Activity a)) continue;
		              cpm.Activity cpmA = (cpm.Activity) objectMap.get(a);
		              if (cpmA == null) continue;

		              if (created.contains(cpmA)) {
		                  // Neue Activity: zwei Events erzeugen und verdrahten
		                  cpm.Event se = factory.createEvent();
		                  cpm.Event te = factory.createEvent();
		                  network.getElements().add(se);
		                  network.getElements().add(te);
		                  cpmA.setSourceEvent(se);
		                  cpmA.setTargetEvent(te);
		                  se.setNumber(eventCounter++); // Eindeutiger Name für Debugging
		                  te.setNumber(eventCounter++);
		              }
		              // In jedem Fall merken (auch für bestehende Activities)
		              startEvents.put(a, cpmA.getSourceEvent());
		              endEvents.put(a, cpmA.getTargetEvent());
		          }

		          // Phase 2: Dependency-Arcs verdrahten (idempotent — sicher bei jedem Aufruf)
		          for (gantt.Element el : gantt.getElements()) {
		              if (!(el instanceof gantt.Dependency d)) continue;
		              cpm.Activity cpmD = (cpm.Activity) objectMap.get(d);
		              if (cpmD == null) continue;

		              cpm.Event srcEvent = switch (d.getDependencyType()) {
		                  case START_START, START_END -> startEvents.get(d.getPredecessor());
		                  case END_START,   END_END   -> endEvents.get(d.getPredecessor());
		              };
		              cpm.Event tgtEvent = switch (d.getDependencyType()) {
		                  case START_START, END_START -> startEvents.get(d.getSuccessor());
		                  case START_END,   END_END   -> endEvents.get(d.getSuccessor());
		              };

		              cpmD.setSourceEvent(srcEvent);
		              cpmD.setTargetEvent(tgtEvent);
		          }
		      }

		      @Override
		      public void beforeDeletions(List<EObject> toDelete) {
		          // Nur relevant bei CASCADE-Policy
		          for (EObject obj : toDelete) {
		              if (!(obj instanceof cpm.Activity cpmA)) continue;
		              if (cpmA.getName().contains("->")) continue; // Dependency-Arcs überspringen

		              // Events der gelöschten Activity entfernen
		              // (Dependency-Arcs die diese Events nutzen bekommen		              
		              //  und werden beim nächsten afterTransform-Aufruf neu verdrahtet)
		              cpm.Event se = cpmA.getSourceEvent();
		              cpm.Event te = cpmA.getTargetEvent();
		              if (se != null) EcoreUtil.delete(se, true);
		              if (te != null) EcoreUtil.delete(te, true);
		          }
		      }
		  };
		  
		  backwardHook = new PostProcessor() {

		      @Override
		      public void afterTransform(Resource readFrom, Resource writeTo,
		                                 Map<EObject, EObject> objectMap,
		                                 List<EObject> created, List<EObject> updated) {

		          cpm.CPMNetwork cpmNetwork =
		              (cpm.CPMNetwork) readFrom.getContents().get(0);
		          gantt.GanttDiagram ganttDiagram =
		              (gantt.GanttDiagram) writeTo.getContents().get(0);

		          // Lookup: name → gantt.Activity
		          Map<String, gantt.Activity> ganttByName = new HashMap<>();
		          for (gantt.Element el : ganttDiagram.getElements()) {
		              if (el instanceof gantt.Activity a)
		                  ganttByName.put(a.getName(), a);
		          }

		          // Lookup: name → cpm.Activity (nur "echte" Activities, kein "->")
		          Map<String, cpm.Activity> cpmByName = new HashMap<>();
		          for (cpm.Element el : cpmNetwork.getElements()) {
		              if (el instanceof cpm.Activity a && !a.getName().contains("->"))
		                  cpmByName.put(a.getName(), a);
		          }

		          // Dependency-Objekte verdrahten
		          for (Map.Entry<EObject, EObject> entry : objectMap.entrySet()) {
		              if (!(entry.getKey()   instanceof cpm.Activity  cpmDep)) continue;
		              if (!cpmDep.getName().contains("->"))
		  continue;
		              if (!(entry.getValue() instanceof gantt.Dependency dep))
		  continue;

		              // Name parsen: "A->B" → pred="A", succ="B"
		              int arrow    = cpmDep.getName().indexOf("->");
		              String predN = cpmDep.getName().substring(0, arrow);
		              String succN = cpmDep.getName().substring(arrow + 2);

		              dep.setPredecessor(ganttByName.get(predN));
		              dep.setSuccessor(ganttByName.get(succN));

		              // DependencyType aus Event-Sharing rekonstruieren
		              cpm.Activity cpmPred = cpmByName.get(predN);
		              cpm.Activity cpmSucc = cpmByName.get(succN);
		              if (cpmPred != null && cpmSucc != null) {
		                  boolean fromPredStart = cpmDep.getSourceEvent() ==
		  cpmPred.getSourceEvent();
		                  boolean toSuccStart   = cpmDep.getTargetEvent() ==
		  cpmSucc.getSourceEvent();
		                  dep.setDependencyType(
		                      fromPredStart &&  toSuccStart ?
		  gantt.DependencyType.START_START :
		                     !fromPredStart &&  toSuccStart ?
		  gantt.DependencyType.END_START   :
		                      fromPredStart && !toSuccStart ?
		  gantt.DependencyType.START_END   :

		  gantt.DependencyType.END_END);
		              }
		          }
		      }
		  };
		  
		  //Gantt2CpmTransformation.transform(source, target, corr, TransformationContext.DeletionPolicy.CASCADE, forwardHook);
	}

	@Override
	public void setConfigurator(Configurator<Decisions> configurator) {
		if(defaultConf == null)
			defaultConf = configurator;
		conf = configurator;
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
	public void performAndPropagateSourceEdit(Supplier<IEdit<GanttDiagram>> sourceEdit) {
		sourceEdit.get();
		Gantt2CpmTransformation.transform(source, target, corr, TransformationContext.DeletionPolicy.CASCADE, forwardHook);
	}
	
	@Override
	public void performAndPropagateTargetEdit(Supplier<IEdit<CPMNetwork>> targetEdit) {
		targetEdit.get();		
		Gantt2CpmTransformation.transformBack(target, source, corr, TransformationContext.DeletionPolicy.CASCADE, backwardHook);
	}
	
	@Override
	public void performIdleSourceEdit(Supplier<IEdit<GanttDiagram>> edit) {
		edit.get();
		//target = PersonToPersonTransformation.transform(source, target);
	}
	
	@Override
	public void performIdleTargetEdit(Supplier<IEdit<CPMNetwork>> edit) {
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

	@Override
	public void performAndPropagateEdit(Supplier<IEdit<GanttDiagram>> sourceEdit,
			Supplier<IEdit<CPMNetwork>> targetEdit) {
		sourceEdit.get();
		targetEdit.get();
		Gantt2CpmTransformation.transform(source, target, corr,
				TransformationContext.DeletionPolicy.CASCADE, forwardHook);
	}

}
