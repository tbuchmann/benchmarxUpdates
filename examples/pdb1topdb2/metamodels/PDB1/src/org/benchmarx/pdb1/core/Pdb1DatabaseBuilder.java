package org.benchmarx.pdb1.core;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;

public class Pdb1DatabaseBuilder {
	private pdb1.Pdb1Factory f = pdb1.Pdb1Factory.eINSTANCE;
	private Supplier<pdb1.Database> database;
	private Consumer<EObject> createNode;
	private BiConsumer<EReference, List<EObject>> createEdge;
	private pdb1.Person lastAdded;
	
	Pdb1DatabaseBuilder(Supplier<pdb1.Database> database, Consumer<EObject> cn, 
			BiConsumer<EReference, List<EObject>> ce) {
		this.database = database;
		this.createNode = cn;
		this.createEdge = ce;
	}
	
	Pdb1DatabaseBuilder databaseName(String name) {
		database.get().setName(name);
		return this;
	}
	
	Pdb1DatabaseBuilder addPerson(String id) {
		lastAdded = f.createPerson();
		lastAdded.setDatabase(database.get());
		lastAdded.setId(id);
		return this;
	}
	
	Pdb1DatabaseBuilder birthday(String birthday) {
		lastAdded.setBirthday(birthday);
		return this;
	}
	
	Pdb1DatabaseBuilder placeOfBirth(String placeOfBirth) {
		lastAdded.setPlaceOfBirth(placeOfBirth);
		return this;
	}
	
	Pdb1DatabaseBuilder firstName(String firstName) {
		lastAdded.setFirstName(firstName);
		return this;
	}
	
	Pdb1DatabaseBuilder lastName(String lastName) {
		lastAdded.setLastName(lastName);
		return this;
	}
	
}
