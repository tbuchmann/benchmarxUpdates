package org.benchmarx.sql.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.util.EcoreUtil;

import sql.Action;
import sql.Annotation;
import sql.Column;
import sql.Condition;
import sql.ForeignKey;
import sql.Property;
import sql.Schema;
import sql.Table;

public class SQLHelper {
	
	private SchemaBuilder builder;
	private Supplier<Schema> schemaSupplier;
	private BiConsumer<EAttribute, List<?>> changeAttribute;
	private Consumer<EObject> deleteNode;
	private BiConsumer<EReference, List<EObject>> deleteEdge;
	
	public SQLHelper(Supplier<Schema> schemaSupplier, Consumer<EObject> createNode,
			BiConsumer<EReference, List<EObject>> createEdge,
			BiConsumer<EAttribute, List<?>> changeAttribute, Consumer<EObject> deleteNode,
			BiConsumer<EObject, List<EObject>> moveTargetNode, BiConsumer<EReference, List<EObject>> deleteEdge) {
		builder = new SchemaBuilder(schemaSupplier, createNode, createEdge);
		this.schemaSupplier = schemaSupplier;
		this.changeAttribute = changeAttribute;
		this.deleteEdge = deleteEdge;
		this.deleteNode = deleteNode;
	}

	public void createListTable() {
		//Precondition: Table Node must exist.		
		//Create table List
		builder.table().name("List")
			.annotation().name("class").end(TableBuilder.class)
			.annotation().name("concrete").end(TableBuilder.class)
			.column().name("id").type("int").property(Property.NOT_NULL).end(TableBuilder.class)
			.column().name("start").type("int")
				.annotation().name("unidirectional").end(ColumnBuilder.class)
				.annotation().name("cross").end(ColumnBuilder.class)
				.annotation().name("single").end(ColumnBuilder.class)
				.end(TableBuilder.class)
			.column().name("length").type("int")
				.annotation().name("attribute").end(ColumnBuilder.class)
				.annotation().name("single").end(ColumnBuilder.class)
				.end(TableBuilder.class)
			.primaryKey().referencedColumn("id").end(TableBuilder.class)
			.foreignKey().referencedColumn("start").referencedTable("Node")
				.annotation().name("unidirectional").end(ForeignKeyBuilder.class)
				.annotation().name("cross").end(ForeignKeyBuilder.class)
				.annotation().name("single").end(ForeignKeyBuilder.class)
				.event().action(Action.SET_NULL).condition(Condition.DELETE).end(ForeignKeyBuilder.class).end(TableBuilder.class)
			.foreignKey().referencedColumn("id").referencedTable("EObject")
				.annotation().name("root").end(ForeignKeyBuilder.class)
				.event().condition(Condition.DELETE).action(Action.CASCADE);
		
		//Add List column to EObject table
		Optional<Table> t = schemaSupplier.get().getOwnedTables().stream().filter(f -> f.getName().equals("EObject")).findAny();
		if(t.isPresent()) {
			TableBuilder b = new TableBuilder(t.get(), builder);
			b.column().name("List").property(Property.UNIQUE).type("int").end(TableBuilder.class)
				.foreignKey().referencedColumn("List").referencedTable("List").event().condition(Condition.DELETE).action(Action.CASCADE);
		}
	}
	
	public void createNodeTable() {		
		//Create table Node
		builder.table().name("Node")
			.annotation().name("class").end(TableBuilder.class)
			.annotation().name("abstract").end(TableBuilder.class)
			.column().name("id").type("int").property(Property.NOT_NULL).end(TableBuilder.class)
			.primaryKey().referencedColumn("id").end(TableBuilder.class)
			.foreignKey().referencedColumn("id").referencedTable("EObject")
				.annotation().name("root").end(ForeignKeyBuilder.class)
				.event().condition(Condition.DELETE).action(Action.CASCADE);
		
		//Add Node column to EObject table
		Optional<Table> t = schemaSupplier.get().getOwnedTables().stream().filter(f -> f.getName().equals("EObject")).findAny();
		if(t.isPresent()) {
			TableBuilder b = new TableBuilder(t.get(), builder);
			b.column().name("Node").property(Property.UNIQUE).type("int").end(TableBuilder.class)
				.foreignKey().referencedColumn("Node").referencedTable("Node").event().condition(Condition.DELETE).action(Action.CASCADE);
		}
	}
	
	public void createLeafTable() {
		//Precondition: Table Node must exist
		
		//Create table Leaf
		builder.table().name("Leaf")
			.annotation().name("class").end(TableBuilder.class)
			.annotation().name("concrete").end(TableBuilder.class)
			.column().name("id").type("int").property(Property.NOT_NULL).end(TableBuilder.class)
			.primaryKey().referencedColumn("id").end(TableBuilder.class)
			.foreignKey().referencedColumn("id").referencedTable("Node")
				.event().condition(Condition.DELETE).action(Action.CASCADE).end(ForeignKeyBuilder.class)
				.annotation().name("superType");
		
		//Add Leaf column to EObject table
		Optional<Table> t = schemaSupplier.get().getOwnedTables().stream().filter(f -> f.getName().equals("EObject")).findAny();
		if(t.isPresent()) {
			TableBuilder b = new TableBuilder(t.get(), builder);
			b.column().name("Leaf").property(Property.UNIQUE).type("int").end(TableBuilder.class)
				.foreignKey().referencedColumn("Leaf").referencedTable("Leaf").event().condition(Condition.DELETE).action(Action.CASCADE);
		}
	}
	
	public void createDataNodeTable() {
		//Precondition: Table Node must exist
		
		//Create table DataNode
		builder.table().name("DataNode")
			.annotation().name("class").end(TableBuilder.class)
			.annotation().name("concrete").end(TableBuilder.class)
			.column().name("id").type("int").property(Property.NOT_NULL).end(TableBuilder.class)
			.column().name("follower").type("int")
				.annotation().name("single").end(ColumnBuilder.class)
				.annotation().name("unidirectional").end(ColumnBuilder.class)
				.annotation().name("cross").end(ColumnBuilder.class).end(TableBuilder.class)
			.column().name("data").type("int")
				.annotation().name("single").end(ColumnBuilder.class)
				.annotation().name("attribute").end(ColumnBuilder.class).end(TableBuilder.class)
			.primaryKey().referencedColumn("id").end(TableBuilder.class)
			.foreignKey().referencedColumn("id").referencedTable("Node")
				.event().condition(Condition.DELETE).action(Action.CASCADE).end(ForeignKeyBuilder.class)
				.annotation().name("superType").end(ForeignKeyBuilder.class).end(TableBuilder.class)
			.foreignKey().referencedColumn("follower").referencedTable("Node")
				.event().condition(Condition.DELETE).action(Action.SET_NULL).end(ForeignKeyBuilder.class)
				.annotation().name("single").end(ForeignKeyBuilder.class)
				.annotation().name("unidirectional").end(ForeignKeyBuilder.class)
				.annotation().name("cross").end(ForeignKeyBuilder.class);
		
		//Add DataNode column to EObject table
		Optional<Table> t = schemaSupplier.get().getOwnedTables().stream().filter(f -> f.getName().equals("EObject")).findAny();
		if(t.isPresent()) {
			TableBuilder b = new TableBuilder(t.get(), builder);
			b.column().name("DataNode").property(Property.UNIQUE).type("int").end(TableBuilder.class)
				.foreignKey().referencedColumn("DataNode").referencedTable("DataNode").event().condition(Condition.DELETE).action(Action.CASCADE);
		}
	}
	
	public void createDataElementTable() {
		
		//Create table DataElement
		builder.table().name("DataElement")
			.annotation().name("class").end(TableBuilder.class)
			.annotation().name("abstract").end(TableBuilder.class)
			.column().name("id").type("int").property(Property.NOT_NULL).end(TableBuilder.class)
			.primaryKey().referencedColumn("id").end(TableBuilder.class)
			.foreignKey().referencedColumn("id").referencedTable("EObject")
				.annotation().name("root").end(ForeignKeyBuilder.class)
				.event().condition(Condition.DELETE).action(Action.CASCADE);
		
		//Add DataElement column to EObject table
		Optional<Table> t = schemaSupplier.get().getOwnedTables().stream().filter(f -> f.getName().equals("EObject")).findAny();
		if(t.isPresent()) {
			TableBuilder b = new TableBuilder(t.get(), builder);
			b.column().name("DataElement").property(Property.UNIQUE).type("int").end(TableBuilder.class)
				.foreignKey().referencedColumn("DataElement").referencedTable("DataElement").event().condition(Condition.DELETE).action(Action.CASCADE);
		}
	}
	
	public void createPairTable() {
		//Precondition: Table DataElement must exist
		
		//Create table Pair
		builder.table().name("Pair")
			.annotation().name("class").end(TableBuilder.class)
			.annotation().name("concrete").end(TableBuilder.class)
			.column().name("id").type("int").property(Property.NOT_NULL).end(TableBuilder.class)
			.primaryKey().referencedColumn("id").end(TableBuilder.class)
			.foreignKey().referencedColumn("id").referencedTable("DataElement")
				.event().condition(Condition.DELETE).action(Action.CASCADE).end(ForeignKeyBuilder.class)
				.annotation().name("superType").end(ForeignKeyBuilder.class);
		
		//Add Pair column to EObject table
		Optional<Table> t = schemaSupplier.get().getOwnedTables().stream().filter(f -> f.getName().equals("EObject")).findAny();
		if(t.isPresent()) {
			TableBuilder b = new TableBuilder(t.get(), builder);
			b.column().name("Pair").property(Property.UNIQUE).type("int").end(TableBuilder.class)
				.foreignKey().referencedColumn("Pair").referencedTable("Pair").event().condition(Condition.DELETE).action(Action.CASCADE);
		}
	}
	
	public void createValueTable() {
		//Precondition: Table Pair must exist
		
		//Create table Value
		builder.table().name("Value")
			.annotation().name("class").end(TableBuilder.class)
			.annotation().name("concrete").end(TableBuilder.class)
			.column().name("id").type("int").property(Property.NOT_NULL).end(TableBuilder.class)
			.column().name("pair_inverse_values").type("int")
				.annotation().name("bidirectional").end(ColumnBuilder.class)
				.annotation().name("containment").end(ColumnBuilder.class)
				.annotation().name("multi").end(ColumnBuilder.class)
				.end(TableBuilder.class)
			.primaryKey().referencedColumn("id").end(TableBuilder.class)
			.foreignKey().referencedColumn("pair_inverse_values").referencedTable("Pair")
				.event().condition(Condition.DELETE).action(Action.CASCADE).end(ForeignKeyBuilder.class)
				.annotation().name("bidirectional").end(ForeignKeyBuilder.class)
				.annotation().name("containment").end(ForeignKeyBuilder.class)
				.annotation().name("multi").end(ForeignKeyBuilder.class).end(TableBuilder.class)
			.foreignKey().referencedColumn("id").referencedTable("EObject")
				.annotation().name("root").end(ForeignKeyBuilder.class)
				.event().condition(Condition.DELETE).action(Action.CASCADE);
		
		//Add Value column to EObject table
		Optional<Table> t = schemaSupplier.get().getOwnedTables().stream().filter(f -> f.getName().equals("EObject")).findAny();
		if(t.isPresent()) {
			TableBuilder b = new TableBuilder(t.get(), builder);
			b.column().name("Value").property(Property.UNIQUE).type("int").end(TableBuilder.class)
				.foreignKey().referencedColumn("Value").referencedTable("Value").event().condition(Condition.DELETE).action(Action.CASCADE);
		}
	}
	
	public void createKeyTable() {
		//Precondition: Table Pair must exist
		
		//Create table Key
		builder.table().name("Key")
			.annotation().name("class").end(TableBuilder.class)
			.annotation().name("concrete").end(TableBuilder.class)
			.column().name("id").type("int").property(Property.NOT_NULL).end(TableBuilder.class)
			.column().name("key_inverse").type("int")
				.annotation().name("single").end(ColumnBuilder.class)
				.annotation().name("unidirectional").end(ColumnBuilder.class)
				.annotation().name("containment").end(ColumnBuilder.class).end(TableBuilder.class)
			.primaryKey().referencedColumn("id").end(TableBuilder.class)
			.foreignKey().referencedColumn("key_inverse").referencedTable("Pair")
				.event().condition(Condition.DELETE).action(Action.CASCADE).end(ForeignKeyBuilder.class)
				.annotation().name("single").end(ForeignKeyBuilder.class)
				.annotation().name("unidirectional").end(ForeignKeyBuilder.class)
				.annotation().name("containment").end(ForeignKeyBuilder.class).end(TableBuilder.class)
			.foreignKey().referencedColumn("id").referencedTable("EObject")
				.annotation().name("root").end(ForeignKeyBuilder.class)
				.event().condition(Condition.DELETE).action(Action.CASCADE);
		
		//Add Key column to EObject table
		Optional<Table> t = schemaSupplier.get().getOwnedTables().stream().filter(f -> f.getName().equals("EObject")).findAny();
		if(t.isPresent()) {
			TableBuilder b = new TableBuilder(t.get(), builder);
			b.column().name("Key").property(Property.UNIQUE).type("int").end(TableBuilder.class)
				.foreignKey().referencedColumn("Key").referencedTable("Key").event().condition(Condition.DELETE).action(Action.CASCADE);
		}
	}
	
	public void createList_start_inverse_Node_startOfTable() {
		//Precondition: Tables Node and List must exist
		
		//Create table List_start_inverse_Node_startOf
		builder.table().name("List_start_inverse_Node_startOf")
			.annotation().name("backwardSingle").end(TableBuilder.class)
			.annotation().name("forwardSingle").end(TableBuilder.class)
			.annotation().name("bidirectional").end(TableBuilder.class)
			.annotation().name("cross").end(TableBuilder.class)
			.column().name("source").type("int").property(Property.NOT_NULL).end(TableBuilder.class)
			.column().name("target").type("int").property(Property.NOT_NULL).end(TableBuilder.class)
			.foreignKey().referencedColumn("source").referencedTable("List")
				.event().condition(Condition.DELETE).action(Action.CASCADE).end(ForeignKeyBuilder.class).end(TableBuilder.class)
			.foreignKey().referencedColumn("target").referencedTable("Node")
				.event().condition(Condition.DELETE).action(Action.CASCADE);
	}
	
	public void createKey_keyValuesTable() {
		//Precondition: Table Key must exist
		
		//Create table Key_keyValues
		builder.table().name("Key_keyValues")
			.annotation().name("attribute").end(TableBuilder.class)
			.annotation().name("multi").end(TableBuilder.class)
			.column().name("id").type("int").property(Property.NOT_NULL).end(TableBuilder.class)
			.column().name("value").type("varchar(30)").property(Property.NOT_NULL).end(TableBuilder.class)
			.foreignKey().referencedColumn("id").referencedTable("Key")
				.event().condition(Condition.DELETE).action(Action.CASCADE);
	}
	
	public void changePackageName() {
		schemaSupplier.get().setName("CompositeList");
	}
	
	public void changeDataNodeTable() {
		Optional<Table> ot = schemaSupplier.get().getOwnedTables().stream().filter(t -> t.getName().equals("DataNode")).findAny();
		if(!ot.isPresent()) return;
		Table t = ot.get();
		Optional<Column> oc = t.getOwnedColumns().stream().filter(c -> c.getName().equals("data")).findAny();
		if(oc.isPresent()) {
			List<Annotation>fl = new ArrayList<Annotation>(oc.get().getOwnedAnnotations());
			while(!fl.isEmpty()) {
				EcoreUtil.delete(fl.remove(0),true);
			}
			ColumnBuilder builder = new ColumnBuilder(oc.get(), null);
			builder
			.annotation().name("single").end(ColumnBuilder.class)
			.annotation().name("unidirectional").end(ColumnBuilder.class)
			.annotation().name("cross");
		}
		TableBuilder builder = new TableBuilder(t, null);
		builder.foreignKey().referencedColumn("data").referencedTable("DataElement")
			.event().condition(Condition.DELETE).action(Action.SET_NULL).end(ForeignKeyBuilder.class)
			.annotation().name("single").end(ForeignKeyBuilder.class)
			.annotation().name("unidirectional").end(ForeignKeyBuilder.class)
			.annotation().name("cross");
	}
	
	public void changeListTable() {
		Optional<Table> ot = schemaSupplier.get().getOwnedTables().stream().filter(t -> t.getName().equals("List")).findAny();
		if(!ot.isPresent()) return;
		Table t = ot.get();
		Optional<Column> oc = t.getOwnedColumns().stream().filter(c -> c.getName().equals("start")).findAny();
		if(oc.isPresent()) {
			EcoreUtil.delete(oc.get().getKeys().get(0), true);
			EcoreUtil.delete(oc.get(), true);
		}
	}
	
	public void changePair_KeyValueReferences() {
		Optional<Table> ot = schemaSupplier.get().getOwnedTables().stream().filter(t -> t.getName().equals("Key")).findAny();
		if(!ot.isPresent()) return;
		Table t = ot.get();
		Optional<Column> oc = t.getOwnedColumns().stream().filter(c -> c.getName().equals("key_inverse")).findAny();
		if(oc.isPresent()) {
			oc.get().setName("pair_inverse_key");
			oc.get().getOwnedAnnotations().stream().forEach(a -> {
				if(a.getAnnotation().equals("unidirectional")) a.setAnnotation("bidirectional");
			});
			oc.get().getKeys().get(0).getOwnedAnnotations().stream().forEach(a -> {
				if(a.getAnnotation().equals("unidirectional")) a.setAnnotation("bidirectional");
			});
		}
		ot = schemaSupplier.get().getOwnedTables().stream().filter(tab -> tab.getName().equals("Value")).findAny();
		if(!ot.isPresent()) return;
		t = ot.get();
		oc = t.getOwnedColumns().stream().filter(c -> c.getName().equals("pair_inverse_values")).findAny();
		if(oc.isPresent()) {
			oc.get().setName("values_inverse");
			oc.get().getOwnedAnnotations().stream().forEach(a -> {
				if(a.getAnnotation().equals("bidirectional")) a.setAnnotation("unidirectional");
			});
			oc.get().getKeys().get(0).getOwnedAnnotations().stream().forEach(a -> {
				if(a.getAnnotation().equals("bidirectional")) a.setAnnotation("unidirectional");
			});
		}
	}
	
	public void changeDataNodeData() {
		Optional<Table> ot = schemaSupplier.get().getOwnedTables().stream().filter(t -> t.getName().equals("DataNode")).findAny();
		if(!ot.isPresent()) return;
		Table t = ot.get();
		Optional<Column> oc = t.getOwnedColumns().stream().filter(c -> c.getName().equals("data")).findAny();
		if(!oc.isPresent()) return;
		Column c = oc.get();
		EcoreUtil.delete(c.getOwnedAnnotations().stream().filter(a -> a.getAnnotation().equals("unidirectional")).findAny().get(), true);
		c.getOwnedAnnotations().stream().filter(a -> a.getAnnotation().equals("cross")).findAny().get().setAnnotation("attribute");
		c.setOwningTable(schemaSupplier.get().getOwnedTables().stream().filter(tab -> tab.getName().equals("Node")).findAny().get());
		c.setName("number");
		EcoreUtil.delete(c.getKeys().get(0),true);
	}
	
	public void changeListLength() {
		Optional<Table> ot = schemaSupplier.get().getOwnedTables().stream().filter(t -> t.getName().equals("List")).findAny();
		if(!ot.isPresent()) return;
		Table t = ot.get();
		Optional<Column> oc = t.getOwnedColumns().stream().filter(c -> c.getName().equals("length")).findAny();
		if(!oc.isPresent()) return;
		Column c = oc.get();
		c.setName("end");
		c.getOwnedAnnotations().stream().filter(a -> a.getAnnotation().equals("attribute")).findAny().get().setAnnotation("cross");
		ColumnBuilder b = new ColumnBuilder(c, null);
		b.annotation().name("unidirectional");
		
		TableBuilder builder = new TableBuilder(t, null);
		builder.foreignKey()
			.referencedColumn("end")
			.referencedTable("Node")
			.event().condition(Condition.DELETE).action(Action.SET_NULL).end(ForeignKeyBuilder.class)
			.annotation().name("cross").end(ForeignKeyBuilder.class)
			.annotation().name("unidirectional").end(ForeignKeyBuilder.class)
			.annotation().name("single");
	}
	
	public void changeForeignKeyPair_Key() {
		Optional<Table> ot = schemaSupplier.get().getOwnedTables().stream().filter(t -> t.getName().equals("Value")).findAny();
		if(!ot.isPresent()) return;
		Table value = ot.get();
		ot = schemaSupplier.get().getOwnedTables().stream().filter(t -> t.getName().equals("Pair")).findAny();
		if(!ot.isPresent()) return;
		Table pair = ot.get();
		ForeignKey dataElement = pair.getOwnedForeignKeys().get(0);
		ForeignKey eObject = value.getOwnedForeignKeys().stream().filter(k -> k.getColumn().getName().equals("id")).findAny().get();
		dataElement.setOwningTable(value);
		Column c = dataElement.getColumn();
		dataElement.setColumn(eObject.getColumn());
		eObject.setColumn(c);
		eObject.setOwningTable(pair);
	}
	
	public void addAnnotationToDataNodeData() {
		Optional<Table> ot = schemaSupplier.get().getOwnedTables().stream().filter(t -> t.getName().equals("DataNode")).findAny();
		if(!ot.isPresent()) return;
		Table t = ot.get();
		Optional<Column> oc = t.getOwnedColumns().stream().filter(c -> c.getName().equals("data")).findAny();
		if(oc.isPresent()) {
			ColumnBuilder builder = new ColumnBuilder(oc.get(), null);
			builder.annotation().name("Just an example of the possible type");
		}
	}
	
	public void addAnnotationToDataNode() {
		Optional<Table> ot = schemaSupplier.get().getOwnedTables().stream().filter(t -> t.getName().equals("DataNode")).findAny();
		if(!ot.isPresent()) return;
		Table t = ot.get();
		TableBuilder builder = new TableBuilder(t, null);
		builder.annotation().name("saves the data");
	}
	
	public void deleteListLengthColumn() {
		Optional<Table> ot = schemaSupplier.get().getOwnedTables().stream().filter(t -> t.getName().equals("List")).findAny();
		if(!ot.isPresent()) return;
		Table t = ot.get();
		Optional<Column> oc = t.getOwnedColumns().stream().filter(c -> c.getName().equals("length")).findAny();
		if(oc.isPresent()) {
			EcoreUtil.delete(oc.get(), true);
		}
	}
	
	public void deleteDataNodeDataColumn() {
		Optional<Table> ot = schemaSupplier.get().getOwnedTables().stream().filter(t -> t.getName().equals("DataNode")).findAny();
		if(!ot.isPresent()) return;
		Table t = ot.get();
		Optional<Column> oc = t.getOwnedColumns().stream().filter(c -> c.getName().equals("data")).findAny();
		if(oc.isPresent()) {
			EcoreUtil.delete(oc.get().getKeys().get(0), true);
			EcoreUtil.delete(oc.get(), true);
		}
	}
	
	public void deleteKeyKeyinverseColumn() {
		Optional<Table> ot = schemaSupplier.get().getOwnedTables().stream().filter(t -> t.getName().equals("Key")).findAny();
		if(!ot.isPresent()) return;
		Table t = ot.get();
		Optional<Column> oc = t.getOwnedColumns().stream().filter(c -> c.getName().equals("key_inverse")).findAny();
		if(oc.isPresent()) {
			EcoreUtil.delete(oc.get().getKeys().get(0), true);
			EcoreUtil.delete(oc.get(), true);
		}
	}
	
	public void deleteValuePairinverseValueColumn() {
		Optional<Table> ot = schemaSupplier.get().getOwnedTables().stream().filter(t -> t.getName().equals("Value")).findAny();
		if(!ot.isPresent()) return;
		Table t = ot.get();
		Optional<Column> oc = t.getOwnedColumns().stream().filter(c -> c.getName().equals("pair_inverse_values")).findAny();
		if(oc.isPresent()) {
			EcoreUtil.delete(oc.get().getKeys().get(0), true);
			EcoreUtil.delete(oc.get(), true);
		}
	}
	
	public void deleteKey_keyValuesTable() {
		Optional<Table> ot = schemaSupplier.get().getOwnedTables().stream().filter(t -> t.getName().equals("Key_keyValues")).findAny();
		if(ot.isPresent()) EcoreUtil.delete(ot.get(), true);
	}
	
	public void deleteList_start_inverse_Node_startOfTable() {
		Optional<Table> ot = schemaSupplier.get().getOwnedTables().stream().filter(t -> t.getName().equals("List_start_inverse_Node_startOf")).findAny();
		if(ot.isPresent()) EcoreUtil.delete(ot.get(), true);
	}
	
	public void deleteDataElementTable() {
		Optional<Table> ot = schemaSupplier.get().getOwnedTables().stream().filter(t -> t.getName().equals("DataElement")).findAny();
		if(ot.isPresent()) {
			EcoreUtil.delete(ot.get().getReferencingForeignKeys().get(0).getColumn(), true);
			EcoreUtil.delete(ot.get().getReferencingForeignKeys().get(0), true);
			EcoreUtil.delete(ot.get(), true);
		}
	}
	
	public void deleteKeyTable() {
		Optional<Table> ot = schemaSupplier.get().getOwnedTables().stream().filter(t -> t.getName().equals("Key")).findAny();
		if(ot.isPresent()) {
			EcoreUtil.delete(ot.get().getReferencingForeignKeys().get(0).getColumn(), true);
			EcoreUtil.delete(ot.get().getReferencingForeignKeys().get(0), true);
			EcoreUtil.delete(ot.get(), true);
		}
	}
	
	public void deleteValueTable() {
		Optional<Table> ot = schemaSupplier.get().getOwnedTables().stream().filter(t -> t.getName().equals("Value")).findAny();
		if(ot.isPresent()) {
			EcoreUtil.delete(ot.get().getReferencingForeignKeys().get(0).getColumn(), true);
			EcoreUtil.delete(ot.get().getReferencingForeignKeys().get(0), true);
			EcoreUtil.delete(ot.get(), true);
		}
	}
	
	public void deletePairTable() {
		Optional<Table> ot = schemaSupplier.get().getOwnedTables().stream().filter(t -> t.getName().equals("Pair")).findAny();
		if(ot.isPresent()) {
			EcoreUtil.delete(ot.get().getReferencingForeignKeys().get(0).getColumn(), true);
			EcoreUtil.delete(ot.get().getReferencingForeignKeys().get(0), true);
			EcoreUtil.delete(ot.get(), true);
		}
	}
	
	public void renameSchema() {
		schemaSupplier.get().setName("CompositeQueue");
	}
	
	public void renameListTable() {
		Optional<Table> ot = schemaSupplier.get().getOwnedTables().stream().filter(t -> t.getName().equals("List")).findAny();
		if(!ot.isPresent()) return;
		Table list = ot.get();
		list.setName("Queue");
		Optional<ForeignKey> key = list.getReferencingForeignKeys().stream().filter(k -> k.getOwningTable().getName().equals("EObject")).findAny();
		if(key.isPresent()) key.get().getColumn().setName("Queue");
		
		
		
		ot = schemaSupplier.get().getOwnedTables().stream().filter(t -> t.getName().equals("List_start_inverse_Node_startOf")).findAny();
		if(!ot.isPresent()) return;
		Table lsins = ot.get();
		lsins.setName("Node_startOf_inverse_Queue_start");
		Optional<ForeignKey> oc = lsins.getOwnedForeignKeys().stream().filter(c -> c.getColumn().getName().equals("source")).findAny();
		if(oc.isPresent()) {
			ot = schemaSupplier.get().getOwnedTables().stream().filter(t -> t.getName().equals("Node")).findAny();
			if(ot.isPresent()) oc.get().setReferencedTable(ot.get());
		}
		oc = lsins.getOwnedForeignKeys().stream().filter(c -> c.getColumn().getName().equals("target")).findAny();
		if(oc.isPresent()) oc.get().setReferencedTable(list);
	}
	
	public void renameDataNodeDataColumn() {
		Optional<Table> ot = schemaSupplier.get().getOwnedTables().stream().filter(t -> t.getName().equals("DataNode")).findAny();
		if(!ot.isPresent()) return;
		Table t = ot.get();
		Optional<Column> oc = t.getOwnedColumns().stream().filter(c -> c.getName().equals("data")).findAny();
		if(oc.isPresent()) {
			oc.get().setName("savedInformation");
		}
	}
	
	public void renameKey_keyValuesTable() {
		Optional<Table> ot = schemaSupplier.get().getOwnedTables().stream().filter(t -> t.getName().equals("Key_keyValues")).findAny();
		if(ot.isPresent()) {
			ot.get().setName("Value_vals");
			ot.get().getOwnedForeignKeys().get(0).setReferencedTable(schemaSupplier.get().getOwnedTables().stream().filter(t -> t.getName().equals("Value")).findAny().get());
		}
	}
	
	public void idleDelta() {
		
	}
	
	public void hippocraticDelta() {
		addAnnotationToDataNode();
		addAnnotationToDataNodeData();
	}
}
