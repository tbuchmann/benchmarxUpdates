package org.benchmarx.pdb2.core;

import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.util.EcoreUtil;

import pdb2.Database;
import pdb2.Person;

public class Pdb2Helper {
	
	private Pdb2DatabaseBuilder builder = null;
	private Supplier<Database> db;
	private BiConsumer<EAttribute /* attribute type */, List<?> /* [owning node, old value, new value] */> changeAttribute;
	private Consumer<EObject> deleteNode;
	private BiConsumer<EObject, List<EObject>> moveNode;
	private BiConsumer<EReference, List<EObject>> deleteEdge;
	private BiConsumer<EReference, List<EObject>> createEdge;
	
	public Pdb2Helper(Supplier<Database> db, Consumer<EObject> createNode,
			BiConsumer<EReference, List<EObject>> createEdge, BiConsumer<EAttribute, List<?>> changeAttribute,
			Consumer<EObject> deleteNode, BiConsumer<EObject, List<EObject>> moveNode,
			BiConsumer<EReference, List<EObject>> deleteEdge) {
		builder = new Pdb2DatabaseBuilder(db, createNode, createEdge);
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
		assertTrue(db.get().getName().equals("Bundeskanzler"));
		db.get().setName("Bundespräsidenten");
	}
	
	public void createKonradAdenauer() {
		//builder = new Pdb2DatabaseBuilder(database);
		builder.addPerson("KA").name("Konrad Hermann Joseph Adenauer").birthday("05.01.1876").placeOfBirth("Koeln");
	}
	
	public void createLudwigErhard() {
		//builder = new Pdb2DatabaseBuilder(database);
		builder.addPerson("LE").name("Ludwig Wilhelm Erhard").birthday("04.02.1897").placeOfBirth("Fuerth");
	}
	
	public void createKurtKiesinger() {
		//builder = new Pdb2DatabaseBuilder(database);
		builder.addPerson("KK").name("Kurt Georg Kiesinger").birthday("06.04.1904").placeOfBirth("Ebingen");
	}
	
	public void createWillyBrandt() {
		//builder = new Pdb2DatabaseBuilder(database);
		builder.addPerson("WB").name("Willy Brandt").birthday("18.12.1913").placeOfBirth("Luebeck");
	}
	
	public void createHelmutSchmidt() {
		//builder = new Pdb2DatabaseBuilder(database);
		builder.addPerson("HS").name("Helmut Heinrich Waldemar Schmidt").birthday("23.12.1918").placeOfBirth("Hamburg");
	}
	
	public void createHelmutKohl() {
		//builder = new Pdb2DatabaseBuilder(database);
		builder.addPerson("HK").name("Helmut Josef Michael Kohl").birthday("03.04.1930").placeOfBirth("Ludwigshafen am Rhein");
	}
	
	public void createGerhardSchroeder() {
		//builder = new Pdb2DatabaseBuilder(database);
		builder.addPerson("GS").name("Gerhard Fritz Kurt Schroeder").birthday("07.04.1944").placeOfBirth("Mossenberg-Woehren");
	}
	
	public void createAngelaMerkel() {
		//builder = new Pdb2DatabaseBuilder(database);
		builder.addPerson("AM").name("Angela Dorothea Merkel").birthday("17.07.1954").placeOfBirth("Hamburg");
	}
	
	public void deleteKurtKiesinger() {
		EcoreUtil.delete(getKurtKiesinger());
	}
	
	public void changeFirstNameOfKonradAdenauer() {
		getKonradAdenauer().setName("Heinz Jochen Adenauer");
	}
	
	public void changeLastNameOfLudwigErhard() {
		getLudwigErhard().setName("Ludwig Wilhelm Meyer");
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
		helmut.setId("Helmut2");
		helmut.setName("Heinz Meyer");
		helmut.setPlaceOfBirth("Germany");
	}
	
	public void changeIncrementalIDs() {
		db.get().getPersons().stream().forEach(p -> p.setIncrementalID("incrTestValue"));
	}
	
	public void idleDelta() {
		
	}
	
	public void hippocraticDelta() {
		
	}
	
	private Person getKonradAdenauer() {
		Optional<Person> ka = db.get().getPersons().stream().filter(p -> p.getId().equals("KA")).findAny();
		
		assertTrue(ka.isPresent());
		Person konrad = ka.get();
		assertTrue(konrad.getName().equals("Konrad Hermann Joseph Adenauer"));
		return konrad;		
	}
	
	private Person getKurtKiesinger() {
		Optional<Person> ka = db.get().getPersons().stream().filter(p -> p.getId().equals("KK")).findAny();
		
		assertTrue(ka.isPresent());
		Person konrad = ka.get();
		assertTrue(konrad.getName().equals("Kurt Georg Kiesinger"));
		return konrad;		
	}
	
	private Person getLudwigErhard() {
		Optional<Person> ka = db.get().getPersons().stream().filter(p -> p.getId().equals("LE")).findAny();
		
		assertTrue(ka.isPresent());
		Person konrad = ka.get();
		assertTrue(konrad.getName().equals("Ludwig Wilhelm Erhard"));
		return konrad;		
	}
	
	private Person getWillyBrandt() {
		Optional<Person> ka = db.get().getPersons().stream().filter(p -> p.getId().equals("WB")).findAny();
		
		assertTrue(ka.isPresent());
		Person konrad = ka.get();
		assertTrue(konrad.getName().equals("Willy Brandt"));
		return konrad;		
	}
	
	private Person getHelmutSchmidt() {
		Optional<Person> ka = db.get().getPersons().stream().filter(p -> p.getId().equals("HS")).findAny();
		
		assertTrue(ka.isPresent());
		Person konrad = ka.get();
		assertTrue(konrad.getName().equals("Helmut Heinrich Waldemar Schmidt"));
		return konrad;		
	}
	
	private Person getHelmutKohl() {
		Optional<Person> ka = db.get().getPersons().stream().filter(p -> p.getId().equals("HK")).findAny();
		
		assertTrue(ka.isPresent());
		Person konrad = ka.get();
		assertTrue(konrad.getName().equals("Helmut Josef Michael Kohl"));
		return konrad;		
	}
}
