package org.benchmarx.examples.set2oset.implementations.plainjavaubt;

import java.util.function.Supplier;

import org.benchmarx.config.Configurator;
import org.benchmarx.edit.IEdit;
import org.benchmarx.emf.BXToolForEMF;
import org.benchmarx.examples.set2oset.testsuite.Decisions;

import org.benchmarx.osets.core.OsetComparator;
import org.benchmarx.sets.core.SetComparator;

import osets.MyOrderedSet;
import osets.OsetsFactory;
import plainjavaubt.oset2set.Oset2Set;
import plainjavaubt.set2oset.Set2Oset;
import plainjavaubt.util.test.BXToolForPlainJavaUbt;
import plainjavaubt.util.trafo.Transformation;
import sets.MySet;
import sets.SetsFactory;

public class PlainJavaUbtSet2Oset extends BXToolForEMF<sets.MySet, osets.MyOrderedSet, Decisions> {
	public PlainJavaUbtSet2Oset() {
		super(new SetComparator(), new OsetComparator());
	}

	@Override
	public void performAndPropagateEdit(Supplier<IEdit<MySet>> sourceEdit, Supplier<IEdit<MyOrderedSet>> targetEdit) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setConfigurator(Configurator<Decisions> configurator) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void initiateSynchronisationDialogue() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public MySet getSourceModel() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MyOrderedSet getTargetModel() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void saveModels(String name) {
		// TODO Auto-generated method stub
		
	}
}
