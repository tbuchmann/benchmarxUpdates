package org.benchmarx.examples.ecore2sql.testsuite.alignment_based.bwd;

import java.util.Collection;

import org.benchmarx.BXTool;
import org.benchmarx.examples.ecore2sql.testsuite.BXToolParameterResolver;
import org.benchmarx.examples.ecore2sql.testsuite.Decisions;
import org.benchmarx.examples.ecore2sql.testsuite.EcoreToSQLTestCase;
import org.eclipse.emf.ecore.EPackage;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import sql.Schema;

@ExtendWith(BXToolParameterResolver.class)
public class IncrementalBackward extends EcoreToSQLTestCase {

	public IncrementalBackward() {
		super();
	}
	
	public static Collection<BXTool<EPackage, Schema, Decisions>> tools() {
		return EcoreToSQLTestCase.tools();
	}
	
	/**
	 * <b>Test</b> for inserting tables, columns and foreign keys in a SQL schema after the initial
	 * schema has been transformed into an EPackage. <br/>
	 * <b>Expect</b> : New classes, attributes and references should be added to the EPackage.<br/>
	 * <b>Features</b>: bwd, add, fixed
	 */
	@ParameterizedTest
	@MethodSource("tools")
	public void testIncrementalInserts(BXTool<EPackage, Schema, Decisions> tool) {
		this.tool = tool;
		initialise();
		tool.performAndPropagateTargetEdit(trgEdit(
				helperSQL::changePackageName,
				helperSQL::createNodeTable,
				helperSQL::createLeafTable,
				helperSQL::createDataNodeTable,
				helperSQL::createListTable));
		tool.performIdleSourceEdit(srcEdit(helperEcore::createMethodsSimple));
		tool.performIdleSourceEdit(srcEdit(helperEcore::changeListLengthAttribute));
		
		util.assertPrecondition("CompositeListSimpleEcore", "CompositeListSimpleSQL");
		//------------		
		tool.performAndPropagateTargetEdit(trgEdit(
				helperSQL::createDataElementTable,
				helperSQL::createPairTable,
				helperSQL::createValueTable,
				helperSQL::createKeyTable));
		tool.performIdleSourceEdit(srcEdit(helperEcore::setDataElementAsInterface));
		tool.performIdleSourceEdit(srcEdit(helperEcore::changeListAddParameter));
		//------------	
		util.assertPostcondition("CompositeListSimpleDataEcore", "CompositeListSimpleDataSQL");
		
		//------------	
		tool.performAndPropagateTargetEdit(trgEdit(
				helperSQL::createKey_keyValuesTable,
				helperSQL::createList_start_inverse_Node_startOfTable,
				helperSQL::changeDataNodeTable,
				helperSQL::changeListTable));
		//------------	
		util.assertPostcondition("CompositeListDataEcore", "CompositeListDataSQL");
		terminate();
	}
	
	/**
	 * <b>Test</b> for deleting columns and tables from the SQL schema.<br/>
	 * <b>Expect</b> : EPackage and SQL schema are structured as specified in the corresponding
	 * assertPostcondition statements.<br/>
	 * <b>Features</b>: bwd, del
	 */
	@ParameterizedTest
	@MethodSource("tools")
	public void testIncrementalDeletions(BXTool<EPackage, Schema, Decisions> tool) {
		this.tool = tool;
		initialise();
		tool.performAndPropagateTargetEdit(trgEdit(
				helperSQL::changePackageName,
				helperSQL::createNodeTable,
				helperSQL::createLeafTable,
				helperSQL::createDataNodeTable,
				helperSQL::createListTable,
				helperSQL::createDataElementTable,
				helperSQL::createPairTable,
				helperSQL::createValueTable,
				helperSQL::createKeyTable,
				helperSQL::createKey_keyValuesTable,
				helperSQL::createList_start_inverse_Node_startOfTable,
				helperSQL::changeDataNodeTable,
				helperSQL::changeListTable));
		tool.performIdleSourceEdit(srcEdit(helperEcore::setDataElementAsInterface));
		tool.performIdleSourceEdit(srcEdit(helperEcore::createMethods));
		tool.performIdleSourceEdit(srcEdit(helperEcore::changeListLengthAttribute));
		
		util.assertPrecondition("CompositeListDataEcore", "CompositeListDataSQL"); 
		
		//------------		
		tool.performAndPropagateTargetEdit(trgEdit(
				helperSQL::deleteListLengthColumn,
				helperSQL::deleteDataNodeDataColumn,
				helperSQL::deleteKeyKeyinverseColumn,
				helperSQL::deleteValuePairinverseValueColumn));
		//------------
		util.assertPostcondition("CompositeListDataColumnDeletionEcore", "CompositeListDataColumnDeletionSQL");
		//------------		
		tool.performAndPropagateTargetEdit(trgEdit(
				helperSQL::deleteKey_keyValuesTable,
				helperSQL::deleteList_start_inverse_Node_startOfTable));
		//------------
		util.assertPostcondition("CompositeListDataColumnATableDeletionEcore", "CompositeListDataColumnATableDeletionSQL");
		//------------		
		tool.performAndPropagateTargetEdit(trgEdit(
				helperSQL::deleteDataElementTable,
				helperSQL::deleteValueTable,
				helperSQL::deletePairTable,
				helperSQL::deleteKeyTable));
		tool.performIdleSourceEdit(srcEdit(helperEcore::changeBackListAddParameter));
		//------------
		util.assertPostcondition("CompositeListDataColumnTablesDeletionEcore", "CompositeListDataColumnTablesDeletionSQL");
		terminate();
	}
	
	/**
	 * <b>Test</b> for renaming tables and columns in a SQL schema after the initial
	 * schema has been transformed into an EPackage.<br/>
	 * <b>Expect</b> : Model states as described in the postcondition.<br/>
	 * <b>Features</b>: bwd, attribute, structural, corr-based, runtime
	 */
	@ParameterizedTest
	@MethodSource("tools")
	public void testIncrementalRenaming(BXTool<EPackage, Schema, Decisions> tool) {
		this.tool = tool;
		initialise();
		tool.performAndPropagateTargetEdit(trgEdit(
				helperSQL::changePackageName,
				helperSQL::createNodeTable,
				helperSQL::createLeafTable,
				helperSQL::createDataNodeTable,
				helperSQL::createListTable,
				helperSQL::createDataElementTable,
				helperSQL::createPairTable,
				helperSQL::createValueTable,
				helperSQL::createKeyTable,
				helperSQL::createKey_keyValuesTable,
				helperSQL::createList_start_inverse_Node_startOfTable,
				helperSQL::changeDataNodeTable,
				helperSQL::changeListTable));
		tool.performIdleSourceEdit(srcEdit(helperEcore::setDataElementAsInterface));
		tool.performIdleSourceEdit(srcEdit(helperEcore::createMethods));
		tool.performIdleSourceEdit(srcEdit(helperEcore::changeListLengthAttribute));
		
		util.assertPrecondition("CompositeListDataEcore", "CompositeListDataSQL");
		
		//----------------------
		tool.performAndPropagateTargetEdit(trgEdit(
				helperSQL::renameSchema,
				helperSQL::renameListTable,
				helperSQL::renameDataNodeDataColumn,
				helperSQL::renameKey_keyValuesTable,
				helperSQL::addAnnotationToDataNode));
		//----------------------
		util.assertPostcondition("CompositeListDataAfterRenameBWDEcore", "CompositeListDataAfterRenameBWDWithDataAnnotationSQL");
		terminate();
	}
	
	/**
	 * <b>Test</b> for deleting and recreating a Person in a PersonRegister after the initial
	 * register has been transformed into a family model.<br/>
	 * <b>Expect</b> : Model states as described in the postcondition.<br/>
	 * <b>Features</b>: bwd, del+add, structural, runtime
	 */
	@ParameterizedTest
	@MethodSource("tools")
	public void testIncrementalMixed(BXTool<EPackage, Schema, Decisions> tool) {
		this.tool = tool;
		initialise();
		tool.performAndPropagateTargetEdit(trgEdit(
				helperSQL::changePackageName,
				helperSQL::createNodeTable,
				helperSQL::createLeafTable,
				helperSQL::createDataNodeTable,
				helperSQL::createListTable,
				helperSQL::createDataElementTable,
				helperSQL::createPairTable,
				helperSQL::createValueTable,
				helperSQL::createKeyTable,
				helperSQL::createKey_keyValuesTable,
				helperSQL::createList_start_inverse_Node_startOfTable,
				helperSQL::changeDataNodeTable,
				helperSQL::changeListTable));
		tool.performIdleSourceEdit(srcEdit(helperEcore::setDataElementAsInterface));
		tool.performIdleSourceEdit(srcEdit(helperEcore::createMethods));
		tool.performIdleSourceEdit(srcEdit(helperEcore::changeListLengthAttribute));
		
		util.assertPrecondition("CompositeListDataEcore", "CompositeListDataSQL");

		//------------
		tool.performAndPropagateTargetEdit(trgEdit(
				helperSQL::changePair_KeyValueReferences,
				helperSQL::changeDataNodeData,
				helperSQL::changeListLength,
				helperSQL::changeForeignKeyPair_Key));
		//------------
		util.assertPostcondition("CompositeListDataAfterMixedEcore", "CompositeListDataAfterMixedSQL");
		terminate();
	}
	
	/**
	 * <b>Test</b> for stability of the transformation.<br/>
	 * <b>Expect</b> Nothing should be changed after an idle target delta.<br/>
	 * <b>Features</b>: bwd, runtime
	 */
	@ParameterizedTest
	@MethodSource("tools")
	public void testStability(BXTool<EPackage, Schema, Decisions> tool) {
		this.tool = tool;
		initialise();
		//------------
		tool.performAndPropagateTargetEdit(trgEdit(
				helperSQL::changePackageName,
				helperSQL::createNodeTable,
				helperSQL::createLeafTable,
				helperSQL::createDataNodeTable,
				helperSQL::createListTable,
				helperSQL::createDataElementTable,
				helperSQL::createPairTable,
				helperSQL::createValueTable,
				helperSQL::createKeyTable,
				helperSQL::createKey_keyValuesTable,
				helperSQL::createList_start_inverse_Node_startOfTable,
				helperSQL::changeDataNodeTable,
				helperSQL::changeListTable));
		tool.performIdleSourceEdit(srcEdit(helperEcore::setDataElementAsInterface));
		tool.performIdleSourceEdit(srcEdit(helperEcore::createMethods));
		tool.performIdleSourceEdit(srcEdit(helperEcore::changeListLengthAttribute));
		
		util.assertPrecondition("CompositeListDataEcore", "CompositeListDataSQL"); 
		//------------
		tool.performAndPropagateTargetEdit(trgEdit(helperSQL::idleDelta));
		//------------
		util.assertPostcondition("CompositeListDataEcore", "CompositeListDataSQL");
		terminate();
	}
	
	
	/**
	 * <b>Test</b> for hippocraticness of the transformation.<br/>
	 * <b>Expect</b> re-running the transformation after creating annotations does not change the EPackage.<br/>
	 * <b>Features:</b>: bwd, fixed
	 */
	@ParameterizedTest
	@MethodSource("tools")
	public void testHippocraticness(BXTool<EPackage, Schema, Decisions> tool) {
		this.tool = tool;
		initialise();
		//------------
		tool.performAndPropagateTargetEdit(trgEdit(
				helperSQL::changePackageName,
				helperSQL::createNodeTable,
				helperSQL::createLeafTable,
				helperSQL::createDataNodeTable,
				helperSQL::createListTable,
				helperSQL::createDataElementTable,
				helperSQL::createPairTable,
				helperSQL::createValueTable,
				helperSQL::createKeyTable,
				helperSQL::createKey_keyValuesTable,
				helperSQL::createList_start_inverse_Node_startOfTable,
				helperSQL::changeDataNodeTable,
				helperSQL::changeListTable));
		tool.performIdleSourceEdit(srcEdit(helperEcore::setDataElementAsInterface));
		tool.performIdleSourceEdit(srcEdit(helperEcore::createMethods));
		tool.performIdleSourceEdit(srcEdit(helperEcore::changeListLengthAttribute));
		
		util.assertPrecondition("CompositeListDataEcore", "CompositeListDataSQL"); 
		//------------
		tool.performAndPropagateTargetEdit(trgEdit(helperSQL::hippocraticDelta));
		//------------
		util.assertPostcondition("CompositeListDataEcore", "CompositeListDataWithDataAnnotationsSQL");
		terminate();
	}

}
