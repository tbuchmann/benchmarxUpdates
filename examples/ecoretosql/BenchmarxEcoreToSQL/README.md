# BenchmarxEcoreToSQL – Test Suite Documentation

This project contains the **Benchmarx** test suite for the **Ecore-to-SQL** bidirectional transformation (bx). The transformation keeps an EMF `EPackage` (source / left model) consistent with a SQL `Schema` (target / right model).

---

## Transformation Overview

| Side | Model type | Root element |
|------|-----------|--------------|
| Source (left) | `org.eclipse.emf.ecore.EPackage` | an EPackage representing a composite-list meta-model |
| Target (right) | `sql.Schema` | a SQL schema with tables, columns and foreign keys |

The correspondence model is stored in `ecore_sql.corr.xmi`.

Tested tool implementations are registered in `EcoreToSQLTestCase#tools()`:
- **BXtendEcore2SQL** – BXtend-based implementation
- **BXAgentEcore2SQL** – BX-Agent-based implementation

---

## Test Classes and Individual Tests

### 1. `batch/fwd/BatchForward` – Batch Forward (source → target)

Tests that perform a single, complete source-model edit and check that the resulting target model is correct.

| Test method | What is tested | Precondition | Edit applied | Expected postcondition |
|---|---|---|---|---|
| `testInitialiseSynchronisation` | Starting state: after initiating the synchronisation dialogue both root elements exist. | none | none | `RootElementEcore` / `RootElementSQL` |
| `testEcoreNameChangeOfEmpty` | Renaming an otherwise empty EPackage propagates the new name to the SQL schema name. | `RootElementEcore` / `RootElementSQL` | `changePackageName` | `CompositeListPackageEcore` / `CompositeListPackageSQL` |
| `testCreateSimpleCompositeList` | Creating a simple composite-list EPackage (Node, Leaf, DataNode, List classes with basic attributes) produces the corresponding SQL schema. | none | `changePackageName` + `createSimpleCompositeList` | `CompositeListSimpleEcore` / `CompositeListSimpleSQL` |
| `testCreateComplexCompositeList` | Extending the simple model with all possible reference types (containment, non-containment, inverse) produces the full SQL schema. | none | `changePackageName` + `createSimpleCompositeList` + `addDataElementFeature` + `changeListAddParameter` | `CompositeListDataEcore` / `CompositeListDataSQL` |

---

### 2. `batch/bwd/BatchBackward` – Batch Backward (target → source)

Tests that perform a single, complete target-model edit and check that the resulting source model is correct.

| Test method | What is tested | Precondition | Edit applied | Expected postcondition |
|---|---|---|---|---|
| `testSQLNameChangeOfEmpty` | Renaming an otherwise empty SQL schema propagates the new name, URI, and prefix to the EPackage. | `RootElementEcore` / `RootElementSQL` | `changePackageName` (SQL) | `CompositeListPackageEcore` / `CompositeListPackageSQL` |
| `testCreateSimpleCompositeList` | Creating Node, Leaf, DataNode, and List tables in the SQL schema produces the corresponding simple composite-list EPackage. | none | `changePackageName` + `createNodeTable` + `createLeafTable` + `createDataNodeTable` + `createListTable` | `CompositeListSimple-OperationsEcore` / `CompositeListSimpleSQL` |
| `testCreateComplexCompositeList` | Creating all tables (including DataElement, Pair, Value, Key, association tables) and applying all column changes produces the full EPackage. After propagation, `setDataElementAsInterface` is applied as an idle source edit. | none | all table/column creation helpers | `CompositeListData-OperationsEcore` / `CompositeListDataSQL` |

---

### 3. `alignment_based/fwd/IncrementalForward` – Incremental Forward

Tests that start from a known consistent state and apply incremental source-model changes, verifying that the target model is updated correctly.

| Test method | What is tested | Precondition | Incremental edit(s) | Expected postcondition(s) |
|---|---|---|---|---|
| `testIncrementalInserts` | After a simple model is established (with SQL annotations preserved as idle), adding DataElement classes and changing a List parameter adds the correct new tables to the SQL schema while leaving existing annotations intact. | `CompositeListSimpleEcore` / `CompositeListSimpleWithDataAnnotationSQL` | `addDataElementFeature` + `changeListAddParameter` | `CompositeListDataEcore` / `CompositeListDataWithDataAnnotationSQL` |
| `testIncrementalDeletions` | Deleting attributes, references, and finally entire classes from the EPackage removes the corresponding columns, foreign keys, and tables from the SQL schema while preserving unrelated annotations. Three incremental deletion steps are tested. | `CompositeListDataEcore` / `CompositeListDataWithDataAnnotationSQL` | Step 1: delete length attribute + keyValues attribute; Step 2: delete startOf reference, pair references, dataNode-data reference; Step 3: delete DataElement feature + revert list parameter | Three successive postconditions up to `CompositeListSimple-DataEcore` / `CompositeListSimple-DataWithDataAnnotationSQL` |
| `testIncrementalRename` | Renaming a class, the package, a reference and an attribute in the EPackage causes the corresponding tables and columns in the SQL schema to be renamed. Existing annotations are preserved. | `CompositeListDataEcore` / `CompositeListDataWithDataAnnotationSQL` | `renameListClass` + `renamePackage` + `renameDataNodeDataReference` + `renameValuesAttribute` | `CompositeListDataAfterRenameEcore` / `CompositeListDataAfterRenameWithDataAnnotationSQL` |
| `testIncrementalMove` | Moving generalizations, attributes, and containment references between classes (and renaming during the move) propagates the structural changes to the SQL schema. | `CompositeListDataEcore` / `CompositeListDataWithDataAnnotationSQL` | `changeGeneralizationDataElement` + `moveReferencePair` + `moveAttributeLengthAndRename` | `CompositeListDataAfterMoveEcore` / `CompositeListDataAfterMoveWithDataAnnotationSQL` |
| `testIncrementalMixed` | Deleting and re-creating an attribute, then deleting and re-creating a class, verifies that the SQL schema is rebuilt correctly. Annotations attached to the deleted/re-created elements are not restored (hippocraticness of annotations). Two incremental steps are tested. | `CompositeListSimpleEcore` / `CompositeListSimpleWithDataAnnotationSQL` | Step 1: `deleteDataAttribute` + `createDataAttribute`; Step 2: `deleteDataNode` + `createDataNode` | Step 1: `CompositeListSimpleWithDataNodeAnnotationSQL`; Step 2: `CompositeListSimpleSQL` |
| `testStability` | Performing an idle source delta (no semantic change) does not alter the SQL schema. | `CompositeListSimpleEcore` / `CompositeListSimpleWithDataAnnotationSQL` | `idleDelta` | `CompositeListSimpleEcore` / `CompositeListSimpleWithDataAnnotationSQL` (unchanged) |
| `testHippocraticness` | Performing a source delta that only creates an operation, deletes an operation, and changes non-mapped attribute values (i.e., changes that are not relevant for the transformation) does not change the SQL schema. | `CompositeListSimpleEcore` / `CompositeListSimpleWithDataAnnotationSQL` | `hippocraticDelta` | `CompositeListSimpleHippocraticEcore` / `CompositeListSimpleWithDataAnnotationSQL` |

---

### 4. `alignment_based/bwd/IncrementalBackward` – Incremental Backward

Tests that start from a known consistent state and apply incremental target-model changes, verifying that the source model is updated correctly.

| Test method | What is tested | Precondition | Incremental edit(s) | Expected postcondition(s) |
|---|---|---|---|---|
| `testIncrementalInserts` | After a simple SQL schema has been propagated back to an EPackage (with idle Ecore edits to add methods and change an attribute), adding DataElement, Pair, Value, and Key tables extends the EPackage with the corresponding classes. A further step adds the remaining association tables and column changes. | `CompositeListSimpleEcore` / `CompositeListSimpleSQL` | Step 1: `createDataElementTable` + `createPairTable` + `createValueTable` + `createKeyTable`; Step 2: `createKey_keyValuesTable` + `createList_start_inverse_Node_startOfTable` + `changeDataNodeTable` + `changeListTable` | Step 1: `CompositeListSimpleDataEcore` / `CompositeListSimpleDataSQL`; Step 2: `CompositeListDataEcore` / `CompositeListDataSQL` |
| `testIncrementalDeletions` | Starting from the full model, deleting columns and then tables from the SQL schema removes the corresponding attributes, references, and classes from the EPackage. Three incremental deletion steps are tested. | `CompositeListDataEcore` / `CompositeListDataSQL` | Step 1: delete 4 columns; Step 2: delete 2 association tables; Step 3: delete DataElement, Value, Pair, Key tables | Three successive postconditions up to `CompositeListDataColumnTablesDeletionEcore` / `CompositeListDataColumnTablesDeletionSQL` |
| `testIncrementalRenaming` | Renaming the schema, a table, a column, and an association table in the SQL schema renames the corresponding EPackage, class, attribute/reference in the EPackage. Adding an annotation to a table is also propagated. | `CompositeListDataEcore` / `CompositeListDataSQL` | `renameSchema` + `renameListTable` + `renameDataNodeDataColumn` + `renameKey_keyValuesTable` + `addAnnotationToDataNode` | `CompositeListDataAfterRenameBWDEcore` / `CompositeListDataAfterRenameBWDWithDataAnnotationSQL` |
| `testIncrementalMixed` | Changing foreign key references, column types, and column names in the SQL schema (a mix of structural and attribute edits) correctly updates the EPackage. | `CompositeListDataEcore` / `CompositeListDataSQL` | `changePair_KeyValueReferences` + `changeDataNodeData` + `changeListLength` + `changeForeignKeyPair_Key` | `CompositeListDataAfterMixedEcore` / `CompositeListDataAfterMixedSQL` |
| `testStability` | Performing an idle target delta (no semantic change) does not alter the EPackage. | `CompositeListDataEcore` / `CompositeListDataSQL` | `idleDelta` | `CompositeListDataEcore` / `CompositeListDataSQL` (unchanged) |
| `testHippocraticness` | Adding annotations to SQL tables (which are not relevant to the Ecore model) does not change the EPackage, but is preserved in the SQL schema. | `CompositeListDataEcore` / `CompositeListDataSQL` | `hippocraticDelta` | `CompositeListDataEcore` / `CompositeListDataWithDataAnnotationsSQL` |

---

## Consistency-Check Helper Methods (Model Helpers)

### `EcoreHelper` – source-side edits
| Method | Description |
|---|---|
| `changePackageName` | Sets the EPackage name, nsURI, and nsPrefix to `CompositeList`. |
| `renamePackage` | Renames the EPackage to a different name. |
| `createSimpleCompositeList` | Creates Node, Leaf, DataNode, List EClasses with basic attributes and an inheritance hierarchy. |
| `addDataElementFeature` | Adds DataElement, Pair, Value, Key EClasses with cross-references. |
| `changeListAddParameter` | Changes the `add` operation parameter type in the List class. |
| `changeBackListAddParameter` | Reverts the `add` operation parameter type change. |
| `setDataElementAsInterface` | Marks the DataElement EClass as abstract/interface. |
| `createMethods` / `createMethodsSimple` | Adds EOperations (which are not mapped to SQL). |
| `changeListLengthAttribute` | Changes the type or name of the `length` attribute in List. |
| `deleteListLengthAttribute` | Deletes the `length` attribute from List. |
| `deleteKeyKeyValuesAttribute` | Deletes the `keyValues` attribute from Key. |
| `deleteNodeStartOfReference` | Deletes the `startOf` reference from Node. |
| `deletePairReferences` | Deletes references from Pair. |
| `deleteDataNodeDataReference` | Deletes the `data` reference from DataNode. |
| `deleteDataElementFeature` | Removes the DataElement class and its related features. |
| `deleteDataAttribute` | Deletes the `data` attribute from DataNode. |
| `deleteDataNode` | Deletes the DataNode EClass entirely. |
| `createDataAttribute` | Re-creates the `data` attribute in DataNode. |
| `createDataNode` | Re-creates the DataNode EClass. |
| `renameListClass` | Renames the List EClass. |
| `renameDataNodeDataReference` | Renames the `data` reference in DataNode. |
| `renameValuesAttribute` | Renames a values-related attribute. |
| `changeGeneralizationDataElement` | Moves the generalization of DataElement to a different supertype. |
| `moveReferencePair` | Moves a reference from one class to another. |
| `moveAttributeLengthAndRename` | Moves and renames the `length` attribute. |
| `idleDelta` | No semantic change (used for stability tests). |
| `hippocraticDelta` | Creates and deletes an EOperation and changes non-mapped attribute values. |

### `SQLHelper` – target-side edits
| Method | Description |
|---|---|
| `changePackageName` | Sets the SQL schema name. |
| `renameSchema` | Renames the SQL schema. |
| `createNodeTable` | Creates the `Node` table with its columns. |
| `createLeafTable` | Creates the `Leaf` table. |
| `createDataNodeTable` | Creates the `DataNode` table. |
| `createListTable` | Creates the `List` table with `length` and `add` columns. |
| `createDataElementTable` | Creates the `DataElement` table. |
| `createPairTable` | Creates the `Pair` table. |
| `createValueTable` | Creates the `Value` table. |
| `createKeyTable` | Creates the `Key` table. |
| `createKey_keyValuesTable` | Creates the `Key_keyValues` association table. |
| `createList_start_inverse_Node_startOfTable` | Creates the `List_start_inverse_Node_startOf` association table. |
| `changeDataNodeTable` | Adds/changes columns in the DataNode table. |
| `changeListTable` | Adds/changes columns in the List table. |
| `deleteListLengthColumn` | Deletes the `length` column from List. |
| `deleteDataNodeDataColumn` | Deletes the `data` column from DataNode. |
| `deleteKeyKeyinverseColumn` | Deletes the `keyinverse` column from Key. |
| `deleteValuePairinverseValueColumn` | Deletes the `pairinverseValue` column from Value. |
| `deleteKey_keyValuesTable` | Deletes the `Key_keyValues` table. |
| `deleteList_start_inverse_Node_startOfTable` | Deletes the `List_start_inverse_Node_startOf` table. |
| `deleteDataElementTable` | Deletes the DataElement table. |
| `deleteValueTable` | Deletes the Value table. |
| `deletePairTable` | Deletes the Pair table. |
| `deleteKeyTable` | Deletes the Key table. |
| `renameListTable` | Renames the List table. |
| `renameDataNodeDataColumn` | Renames the `data` column in DataNode. |
| `renameKey_keyValuesTable` | Renames the `Key_keyValues` table. |
| `addAnnotationToDataNode` | Adds an EAnnotation to the DataNode table (not mapped to Ecore). |
| `addAnnotationToDataNodeData` | Adds an EAnnotation to the DataNode.data column. |
| `changePair_KeyValueReferences` | Changes the foreign-key references in the Pair table. |
| `changeDataNodeData` | Changes the `data` column in DataNode. |
| `changeListLength` | Changes the `length` column in List. |
| `changeForeignKeyPair_Key` | Changes the foreign key from Pair to Key. |
| `idleDelta` | No semantic change (used for stability tests). |
| `hippocraticDelta` | Adds annotations that are not relevant to the EPackage. |

---

## Test Naming Conventions for Expected States

Model state identifiers used in `assertPrecondition` / `assertPostcondition` follow the pattern:

- **`RootElement{Ecore|SQL}`** – freshly initialised model with only the root element.
- **`CompositeListPackage{Ecore|SQL}`** – only the package name / schema name has been set.
- **`CompositeListSimple{Ecore|SQL}`** – simple composite-list structure (Node, Leaf, DataNode, List).
- **`CompositeListSimpleWithDataAnnotation{SQL}`** – simple SQL schema with extra annotations on DataNode and DataNode.data.
- **`CompositeListData{Ecore|SQL}`** – full model including DataElement, Pair, Value, Key and association tables.
- **`CompositeListDataWithDataAnnotation{SQL}`** – full SQL model with DataNode annotation.
- **`CompositeListDataColumnDeletion{Ecore|SQL}`** – after deleting four columns.
- **`CompositeListDataColumnATableDeletion{Ecore|SQL}`** – after additionally deleting two association tables.
- **`CompositeListDataColumnTablesDeletion{Ecore|SQL}`** – after additionally deleting four class tables.
- **`CompositeListDataAfterRename{Ecore|SQL}`** / **`CompositeListDataAfterRenameBWD{Ecore|SQL}`** – after renaming operations.
- **`CompositeListDataAfterMove{Ecore|SQL}`** – after moving structural features.
- **`CompositeListDataAfterMixed{Ecore|SQL}`** – after a mixed set of structural and attribute changes.
- **`CompositeListSimpleHippocratic{Ecore}`** – simple EPackage after a hippocratic source delta (operations added/deleted, non-mapped attributes changed).
- **`CompositeListSimpleWithDataNodeAnnotation{SQL}`** – simple SQL schema with annotation only on DataNode table (DataNode.data annotation removed).
