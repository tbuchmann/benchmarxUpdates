package org.benchmarx.examples.gantt2cpm.implementations.ibextgg;

import java.io.IOException;
import java.util.function.Supplier;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.benchmarx.config.Configurator;
import org.benchmarx.cpm.core.CPMComparator;
import org.benchmarx.edit.IEdit;
import org.benchmarx.emf.BXToolForEMF;
import org.benchmarx.examples.gantt2cpm.testsuite.Decisions;
import org.benchmarx.gantt.core.GanttComparator;
import org.emoflon.ibex.tgg.run.ibextgggantt2cpm.SYNC_App;

import cpm.CPMNetwork;
import gantt.GanttDiagram;;

public class IBeXTGGGantt2CPM extends BXToolForEMF<GanttDiagram, CPMNetwork, Decisions> {

	private SYNC_App sync;
	
	public IBeXTGGGantt2CPM() {
		super(new GanttComparator(), new CPMComparator());
		BasicConfigurator.configure();
		Logger.getRootLogger().setLevel(Level.ERROR);
	}

	@Override
	public String getName() {
		return "IBeX-TGG";
	}
	
	@Override
	public void initiateSynchronisationDialogue() {
		try {
			long ping = System.currentTimeMillis();

			sync = new SYNC_App();
			
			// Create initial src and trg models
			GanttDiagram gd = gantt.GanttFactory.eINSTANCE.createGanttDiagram();
			gd.setName("");
			sync.getSourceResource().getContents().add(gd);
			
			sync.forward();
			
			long pong = System.currentTimeMillis();
			System.out.println((pong - ping) / 1000.0 +  " s for init");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void performAndPropagateSourceEdit(Supplier<IEdit<GanttDiagram>> edit) {
		// Adapt source model
		GanttDiagram gd = (GanttDiagram) sync.getSourceResource().getContents().get(0);
		edit.get();
		
		// Invoke sync
		try {
			sync.forward();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void performAndPropagateTargetEdit(Supplier<IEdit<CPMNetwork>> edit) {
		// Adapt target model
		CPMNetwork cn = (CPMNetwork) sync.getTargetResource().getContents().get(0);
		edit.get();
		
 		// Invoke sync
		try {
			sync.backward();
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}

	@Override
	public void performIdleSourceEdit(Supplier<IEdit<GanttDiagram>> edit) {
		edit.get();
		performAndPropagateSourceEdit(edit);
		
	}

	@Override
	public void performIdleTargetEdit(Supplier<IEdit<CPMNetwork>> edit) {
		edit.get();
		performAndPropagateTargetEdit(edit);
	}

	@Override
	public void setConfigurator(Configurator<Decisions> configurator) {
		
	}

	@Override
	public GanttDiagram getSourceModel() {
		return (GanttDiagram) sync.getSourceResource().getContents().get(0);
	}

	@Override
	public CPMNetwork getTargetModel() {
		return (CPMNetwork) sync.getTargetResource().getContents().get(0);
	}

	@Override
	public void saveModels(String name) {
		try {
			sync.saveModels();
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
