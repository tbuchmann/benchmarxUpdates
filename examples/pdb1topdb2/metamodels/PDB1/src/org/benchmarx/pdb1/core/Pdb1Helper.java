package org.benchmarx.pdb1.core;

import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.util.EcoreUtil;

import pdb1.Person;

public class Pdb1Helper {
	
	private Pdb1DatabaseBuilder builder = null;
	private Supplier<pdb1.Database> db;
	private BiConsumer<EAttribute /* attribute type */, List<?> /* [owning node, old value, new value] */> changeAttribute;
	private Consumer<EObject> deleteNode;
	private BiConsumer<EObject, List<EObject>
	/*
	 * [old parent, old connection (ERef), new parent, new connection (ERef)]
	 */> moveNode;
	private BiConsumer<EReference, List<EObject> /* [source, target] */> deleteEdge;
	@SuppressWarnings("unused")
	private BiConsumer<EReference, List<EObject /* [source, target] */>> createEdge;
	
	public Pdb1Helper(Supplier<pdb1.Database> db, Consumer<EObject> createNode,
			BiConsumer<EReference, List<EObject>> createEdge, BiConsumer<EAttribute, List<?>> changeAttribute,
			Consumer<EObject> deleteNode, BiConsumer<EObject, List<EObject>> moveNode,
			BiConsumer<EReference, List<EObject>> deleteEdge) {
		builder = new Pdb1DatabaseBuilder(db, createNode, createEdge);
		this.db = db;
		this.changeAttribute = changeAttribute;
		this.deleteEdge = deleteEdge;
		this.deleteNode = deleteNode;
		this.moveNode = moveNode;
		this.createEdge = createEdge;
	}
	
	public void setDatabaseName() {
		db.get().setName("Bundeskanzler");
	}
	
	public void renameKanzlerDatabaseToPräsidenten() {
		if (!db.get().getName().equals("Bundeskanzler"))
			throw new IllegalStateException("Expected database name 'Bundeskanzler' but was: " + db.get().getName());
		db.get().setName("Bundespräsidenten");
	}
	
	public void createKonradAdenauer() {
		//builder = new Pdb1DatabaseBuilder(database);
		builder.addPerson("KA").firstName("Konrad Hermann Joseph").lastName("Adenauer").birthday("05.01.1876").placeOfBirth("Koeln");
	}
	
	public void createWrongKonradAdenauer() {
		//builder = new Pdb1DatabaseBuilder(database);
		builder.addPerson("KA").firstName("Konrad Hermann").lastName("Joseph Adenauer").birthday("05.01.1876").placeOfBirth("Koeln");
	}
	
	public void createLudwigErhard() {
		//builder = new Pdb1DatabaseBuilder(database);
		builder.addPerson("LE").firstName("Ludwig Wilhelm").lastName("Erhard").birthday("04.02.1897").placeOfBirth("Fuerth");
	}
	
	public void createKurtKiesinger() {
		//builder = new Pdb1DatabaseBuilder(database);
		builder.addPerson("KK").firstName("Kurt Georg").lastName("Kiesinger").birthday("06.04.1904").placeOfBirth("Ebingen");
	}
	
	public void createWillyBrandt() {
		//builder = new Pdb1DatabaseBuilder(database);
		builder.addPerson("WB").firstName("Willy").lastName("Brandt").birthday("18.12.1913").placeOfBirth("Luebeck");
	}
	
	public void createHelmutSchmidt() {
		//builder = new Pdb1DatabaseBuilder(database);
		builder.addPerson("HS").firstName("Helmut Heinrich Waldemar").lastName("Schmidt").birthday("23.12.1918").placeOfBirth("Hamburg");
	}
	
	public void createHelmutKohl() {
		//builder = new Pdb1DatabaseBuilder(database);
		builder.addPerson("HK").firstName("Helmut Josef Michael").lastName("Kohl").birthday("03.04.1930").placeOfBirth("Ludwigshafen am Rhein");
	}
	
	public void createGerhardSchroeder() {
		//builder = new Pdb1DatabaseBuilder(database);
		builder.addPerson("GS").firstName("Gerhard Fritz Kurt").lastName("Schroeder").birthday("07.04.1944").placeOfBirth("Mossenberg-Woehren");
	}
	
	public void createAngelaMerkel() {
		//builder = new Pdb1DatabaseBuilder(database);
		builder.addPerson("AM").firstName("Angela Dorothea").lastName("Merkel").birthday("17.07.1954").placeOfBirth("Hamburg");
	}
	
	public void deleteKurtKiesinger() {

		EcoreUtil.delete(getKurtKiesinger());
	}

	public void createPerson(String id) {
		builder.addPerson(id).firstName("First_" + id).lastName("Last_" + id).birthday("01.01.1900").placeOfBirth("Berlin");
	}

	public void createNPersons(int n, String prefix) {
		for (int i = 1; i <= n; i++) {
			createPerson(prefix + i);
		}
	}

	public void deleteNPersons(int n, String prefix) {
		for (int i = 1; i <= n; i++) {
			EcoreUtil.delete(getPerson(prefix + i));
		}
	}
	
	public void changeFirstNameOfKonradAdenauer() {
		getKonradAdenauer().setFirstName("Heinz");
	}
	
	public void changeLastNameOfLudwigErhard() {
		getLudwigErhard().setLastName("Meyer");
	}
	
	public void changeBirthdayOfKurtKiesinger() {
		getKurtKiesinger().setBirthday("01.01.1990");
	}
	
	public void changePlaceOfBirthOfWillyBrandt() {
		getWillyBrandt().setPlaceOfBirth("Germany");
	}
	
	public void changeIDOfHelmutSchmidt() {
		getHelmutSchmidt().setId("Helmut");
	}
	
	public void changeAllOfHelmutKohl() {
		Person helmut = getHelmutKohl();
		helmut.setBirthday("01.01.1990");
		helmut.setFirstName("Heinz");
		helmut.setId("Helmut2");
		helmut.setLastName("Meyer");
		helmut.setPlaceOfBirth("Germany");
	}
	
	public void changeIncrementalIDs() {
		db.get().getPersons().stream().forEach(p -> p.setIncrementalID("incrTestValue"));
	}
	
	public void idleDelta() {
		
	}
	
	public void hippocraticDelta() {
		Person k = getWrongKonradAdenauer();
		k.setFirstName("Konrad Hermann Joseph");
		k.setLastName("Adenauer");
	}
	
	private Person getPerson(String id) {
		Optional<Person> p = db.get().getPersons().stream().filter(x -> x.getId().equals(id)).findAny();
		if (!p.isPresent()) throw new IllegalStateException("Expected person " + id + " to be present");
		return p.get();
	}

	private Person getWrongKonradAdenauer() {
		Optional<Person> ka = db.get().getPersons().stream().filter(p -> p.getId().equals("KA")).findAny();
		if (!ka.isPresent()) throw new IllegalStateException("Expected person KA to be present");
		Person konrad = ka.get();
		if (!konrad.getFirstName().equals("Konrad Hermann")) throw new IllegalStateException("Unexpected firstName: " + konrad.getFirstName());
		if (!konrad.getLastName().equals("Joseph Adenauer")) throw new IllegalStateException("Unexpected lastName: " + konrad.getLastName());
		return konrad;		
	}
	
	private Person getKonradAdenauer() {
		Optional<Person> ka = db.get().getPersons().stream().filter(p -> p.getId().equals("KA")).findAny();
		if (!ka.isPresent()) throw new IllegalStateException("Expected person KA to be present");
		Person konrad = ka.get();
		if (!konrad.getFirstName().equals("Konrad Hermann Joseph")) throw new IllegalStateException("Unexpected firstName: " + konrad.getFirstName());
		if (!konrad.getLastName().equals("Adenauer")) throw new IllegalStateException("Unexpected lastName: " + konrad.getLastName());
		return konrad;		
	}
	
	private Person getKurtKiesinger() {
		Optional<Person> ka = db.get().getPersons().stream().filter(p -> p.getId().equals("KK")).findAny();
		if (!ka.isPresent()) throw new IllegalStateException("Expected person KK to be present");
		Person konrad = ka.get();
		if (!konrad.getFirstName().equals("Kurt Georg")) throw new IllegalStateException("Unexpected firstName: " + konrad.getFirstName());
		if (!konrad.getLastName().equals("Kiesinger")) throw new IllegalStateException("Unexpected lastName: " + konrad.getLastName());
		return konrad;		
	}
	
	private Person getLudwigErhard() {
		Optional<Person> ka = db.get().getPersons().stream().filter(p -> p.getId().equals("LE")).findAny();
		if (!ka.isPresent()) throw new IllegalStateException("Expected person LE to be present");
		Person konrad = ka.get();
		if (!konrad.getFirstName().equals("Ludwig Wilhelm")) throw new IllegalStateException("Unexpected firstName: " + konrad.getFirstName());
		if (!konrad.getLastName().equals("Erhard")) throw new IllegalStateException("Unexpected lastName: " + konrad.getLastName());
		return konrad;		
	}
	
	private Person getWillyBrandt() {
		Optional<Person> ka = db.get().getPersons().stream().filter(p -> p.getId().equals("WB")).findAny();
		if (!ka.isPresent()) throw new IllegalStateException("Expected person WB to be present");
		Person konrad = ka.get();
		if (!konrad.getFirstName().equals("Willy")) throw new IllegalStateException("Unexpected firstName: " + konrad.getFirstName());
		if (!konrad.getLastName().equals("Brandt")) throw new IllegalStateException("Unexpected lastName: " + konrad.getLastName());
		return konrad;		
	}
	
	private Person getHelmutSchmidt() {
		Optional<Person> ka = db.get().getPersons().stream().filter(p -> p.getId().equals("HS")).findAny();
		if (!ka.isPresent()) throw new IllegalStateException("Expected person HS to be present");
		Person konrad = ka.get();
		if (!konrad.getFirstName().equals("Helmut Heinrich Waldemar")) throw new IllegalStateException("Unexpected firstName: " + konrad.getFirstName());
		if (!konrad.getLastName().equals("Schmidt")) throw new IllegalStateException("Unexpected lastName: " + konrad.getLastName());
		return konrad;		
	}
	
	private Person getHelmutKohl() {
		Optional<Person> ka = db.get().getPersons().stream().filter(p -> p.getId().equals("HK")).findAny();
		if (!ka.isPresent()) throw new IllegalStateException("Expected person HK to be present");
		Person konrad = ka.get();
		if (!konrad.getFirstName().equals("Helmut Josef Michael")) throw new IllegalStateException("Unexpected firstName: " + konrad.getFirstName());
		if (!konrad.getLastName().equals("Kohl")) throw new IllegalStateException("Unexpected lastName: " + konrad.getLastName());
		return konrad;		
	}
}