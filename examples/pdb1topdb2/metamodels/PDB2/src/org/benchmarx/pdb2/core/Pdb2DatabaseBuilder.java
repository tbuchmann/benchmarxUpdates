package org.benchmarx.pdb2.core;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;

import pdb2.Database;

public class Pdb2DatabaseBuilder {
	private pdb2.Pdb2Factory f = pdb2.Pdb2Factory.eINSTANCE;
	private Supplier<pdb2.Database> database;
	private Consumer<EObject> createNode;
	private BiConsumer<EReference, List<EObject>> createEdge;
	private pdb2.Person lastAdded;

	
	public Pdb2DatabaseBuilder(Supplier<Database> db, Consumer<EObject> createNode,
			BiConsumer<EReference, List<EObject>> createEdge) {
		this.database = db;
		this.createNode = createNode;
		this.createEdge = createEdge;
	}

	Pdb2DatabaseBuilder databaseName(String name) {
		database.get().setName(name);
		return this;
	}
	
	Pdb2DatabaseBuilder addPerson(String id) {
		lastAdded = f.createPerson();
		lastAdded.setDatabase(database.get());
		lastAdded.setId(id);
		return this;
	}
	
	Pdb2DatabaseBuilder birthday(String birthday) {
		lastAdded.setBirthday(birthday);
		return this;
	}
	
	Pdb2DatabaseBuilder placeOfBirth(String placeOfBirth) {
		lastAdded.setPlaceOfBirth(placeOfBirth);
		return this;
	}
	
	Pdb2DatabaseBuilder name(String name) {
		lastAdded.setName(name);
		return this;
	}
	
}
