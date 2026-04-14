package org.benchmarx.sql.core;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;

import sql.Annotation;
import sql.Schema;
import sql.SqlFactory;
import sql.Table;

public class SchemaBuilder extends SQLBuilder {
	
	private Schema schema;
	private Consumer<EObject> createNode;
	private BiConsumer<EReference, List<EObject>> createEdge;	
	
	public SchemaBuilder(Supplier<Schema> schemaSupplier, Consumer<EObject> createNode,
			BiConsumer<EReference, List<EObject>> createEdge) {
		super(null);
		this.schema = schemaSupplier.get();
		this.createNode = createNode;
		this.createEdge = createEdge;
	}

	public SchemaBuilder name(String value) {
		schema.setName(value);
		return this;
	}
	
	public TableBuilder table() {
		Table table = SqlFactory.eINSTANCE.createTable();
		schema.getOwnedTables().add(table);
		return new TableBuilder(table, this);
	}
	
	public AnnotationBuilder annotation() {
		Annotation a = SqlFactory.eINSTANCE.createAnnotation();
		schema.getOwnedAnnotations().add(a);
		return new AnnotationBuilder(a, this);
	}
}
