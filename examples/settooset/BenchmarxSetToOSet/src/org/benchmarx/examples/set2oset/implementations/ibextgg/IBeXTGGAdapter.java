package org.benchmarx.examples.set2oset.implementations.ibextgg;

import java.io.IOException;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.benchmarx.config.Configurator;
import org.benchmarx.edit.IEdit;
import org.benchmarx.emf.BXToolForEMF;
import org.eclipse.emf.ecore.EObject;
import org.emoflon.ibex.tgg.operational.strategies.sync.SYNC;

public abstract class IBeXTGGAdapter<S extends EObject, T extends EObject, D, X extends SYNC>
		extends BXToolForEMF<S, T, D> {

	public IBeXTGGAdapter(BiConsumer<S, S> src, BiConsumer<T, T> trg) {
		super(src, trg);

		BasicConfigurator.configure();
		Logger.getRootLogger().setLevel(Level.ERROR);
	}

	protected SYNC sync;

	@Override
	public String getName() {
		return "IBeX-TGG";
	}

	@Override
	public void initiateSynchronisationDialogue() {
		try {
			sync = createSynchroniser();
			
			// Create initial src and trg models
			S o = createInitialSrc();
			sync.getSourceResource().getContents().add(o);

			sync.forward();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	protected abstract SYNC createSynchroniser() throws IOException;
	protected abstract S createInitialSrc();

	@Override
	public void performAndPropagateSourceEdit(Supplier<IEdit<S>> edit) {
		// Adapt source model
		@SuppressWarnings("unchecked")
		S o = (S) sync.getSourceResource().getContents().get(0);
		//edit.accept(o);
		edit.get();

		// Invoke sync
		try {
			sync.forward();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void performAndPropagateTargetEdit(Supplier<IEdit<T>> edit) {
		// Adapt target model
		@SuppressWarnings("unchecked")
		T o = (T) sync.getTargetResource().getContents().get(0);
		//edit.accept(o);
		edit.get();

		// Invoke sync
		try {
			sync.backward();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void performIdleSourceEdit(Supplier<IEdit<S>> edit) {
		performAndPropagateSourceEdit(edit);
	}

	@Override
	public void performIdleTargetEdit(Supplier<IEdit<T>> edit) {
		performAndPropagateTargetEdit(edit);
	}

	@Override
	public void setConfigurator(Configurator<D> configurator) {

	}

	@SuppressWarnings("unchecked")
	@Override
	public S getSourceModel() {
		return (S) sync.getSourceResource().getContents().get(0);
	}

	@SuppressWarnings("unchecked")
	@Override
	public T getTargetModel() {
		return (T) sync.getTargetResource().getContents().get(0);
	}

	@Override
	public void saveModels(String name) {
		try {
			sync.saveModels();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
