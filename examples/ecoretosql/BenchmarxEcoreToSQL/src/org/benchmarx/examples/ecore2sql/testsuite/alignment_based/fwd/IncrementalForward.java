package org.benchmarx.examples.ecore2sql.testsuite.alignment_based.fwd;

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
public class IncrementalForward extends EcoreToSQLTestCase {
	
	public IncrementalForward() {
		super();
	}
	
	public static Collection<BXTool<EPackage, Schema, Decisions>> tools() {
		return EcoreToSQLTestCase.tools();
	}
	
	/**
	 * <b>Test</b> for inserting a set of new classes and changing a attribute to a reference.
	 * Added one additional Annotation in the target model. <br/>
	 * <b>Expect</b> : New tables are added to the schema, while the old tables
	 * remain nearly unchanged. <br/>
	 * <b>Features</b>: fwd, add, fixed
	 */
	@ParameterizedTest
	@MethodSource("tools")
	public void testIncrementalInserts(BXTool<EPackage, Schema, Decisions> tool) {
		this.tool = tool;
		initialise();
		tool.performAndPropagateSourceEdit(srcEdit(
				helperEcore::createSimpleCompositeList,
				helperEcore::changePackageName));
		tool.performIdleTargetEdit(trgEdit(helperSQL::addAnnotationToDataNodeData));
		tool.performIdleTargetEdit(trgEdit(helperSQL::addAnnotationToDataNode));
		
		util.assertPrecondition("CompositeListSimpleEcore", "CompositeListSimpleWithDataAnnotationSQL");

		//------------
		tool.performAndPropagateSourceEdit(srcEdit(
				helperEcore::addDataElementFeature,
				helperEcore::changeListAddParameter));
		//------------
		util.assertPostcondition("CompositeListDataEcore", "CompositeListDataWithDataAnnotationSQL");
		terminate();
	}
	
	/**
	 * <b>Test</b> for deleting the data element structure.
	 * <b>Expect</b>: Delete the correct tables in the sql schema
	 * <b>Features</b>: fwd, del, corr-based, structural
	 */
	@ParameterizedTest
	@MethodSource("tools")
	public void testIncrementalDeletions(BXTool<EPackage, Schema, Decisions> tool) {
		this.tool = tool;
		initialise();
		tool.performAndPropagateSourceEdit(srcEdit(
				helperEcore::changePackageName,
				helperEcore::createSimpleCompositeList,
				helperEcore::addDataElementFeature,
				helperEcore::changeListAddParameter));
		tool.performIdleTargetEdit(trgEdit(helperSQL::addAnnotationToDataNode));
		
		util.assertPrecondition("CompositeListDataEcore", "CompositeListDataWithDataAnnotationSQL");
		//------------
		tool.performAndPropagateSourceEdit(srcEdit(
				helperEcore::deleteListLengthAttribute,
				helperEcore::deleteKeyKeyValuesAttribute));
		//------------
		util.assertPostcondition("CompositeListDataAttributeDeletionEcore", "CompositeListDataAttributeDeletionWithDataAnnotationSQL");
		//------------
		tool.performAndPropagateSourceEdit(srcEdit(
				helperEcore::deleteNodeStartOfReference,
				helperEcore::deletePairReferences,
				helperEcore::deleteDataNodeDataReference));
		//------------
		util.assertPostcondition("CompositeListDataAttributeReferenceDeletionEcore", "CompositeListDataAttributeReferenceDeletionWithDataAnnotationSQL");
		//------------
		tool.performAndPropagateSourceEdit(srcEdit(
				helperEcore::deleteDataElementFeature,
				helperEcore::changeBackListAddParameter));
		//------------
		util.assertPostcondition("CompositeListSimple-DataEcore", "CompositeListSimple-DataWithDataAnnotationSQL");
		terminate();
				
	}
	
	/**
	 * <b>Test</b> for renaming class, package, attribute and a reference.
	 * <b>Expect</b>: Change the name of the affected tables and columns in the SQL schema
	 * <b>Features</b>: fwd, attribute, fixed, structural, corr-based
	 */
	@ParameterizedTest
	@MethodSource("tools")
	public void testIncrementalRename(BXTool<EPackage, Schema, Decisions> tool) {
		this.tool = tool;
		initialise();
		tool.performAndPropagateSourceEdit(srcEdit(
				helperEcore::changePackageName,
				helperEcore::createSimpleCompositeList,
				helperEcore::addDataElementFeature,
				helperEcore::changeListAddParameter));
		tool.performIdleTargetEdit(trgEdit(helperSQL::addAnnotationToDataNode));
		
		util.assertPrecondition("CompositeListDataEcore", "CompositeListDataWithDataAnnotationSQL");
		//------------
		tool.performAndPropagateSourceEdit(srcEdit(
				helperEcore::renameListClass,
				helperEcore::renamePackage,
				helperEcore::renameDataNodeDataReference,
				helperEcore::renameValuesAttribute));
		//------------
		util.assertPostcondition("CompositeListDataAfterRenameEcore", "CompositeListDataAfterRenameWithDataAnnotationSQL");
		terminate();
	}
	
	/**
	 * <b>Test</b> for moving generalizations, attributes and containment references to different classes and also changing their names. 
	 * <b>Expect</b>: Changes should be propagated to the SQL Schema
	 * <b>Features</b>: fwd, del+add, fixed, structural
	 */
	@ParameterizedTest
	@MethodSource("tools")
	public void testIncrementalMove(BXTool<EPackage, Schema, Decisions> tool) {
		this.tool = tool;
		initialise();
		tool.performAndPropagateSourceEdit(srcEdit(
				helperEcore::changePackageName,
				helperEcore::createSimpleCompositeList,
				helperEcore::addDataElementFeature,
				helperEcore::changeListAddParameter));
		tool.performIdleTargetEdit(trgEdit(helperSQL::addAnnotationToDataNode));
		
		util.assertPrecondition("CompositeListDataEcore", "CompositeListDataWithDataAnnotationSQL");
		//------------
		tool.performAndPropagateSourceEdit(srcEdit(
				helperEcore::changeGeneralizationDataElement,
				helperEcore::moveReferencePair,
				helperEcore::moveAttributeLengthAndRename));
		//------------
		util.assertPostcondition("CompositeListDataAfterMoveEcore", "CompositeListDataAfterMoveWithDataAnnotationSQL");
		terminate();
	}
	
	/**
	 * <b>Test</b> for deleting and re-creating an attribute and a class.
	 * After creating the SQL schema, set some other annotations. Then delete and re-create an attribute and a class with an additional annotation.
	 * <b>Expect</b>: SQL schema remains unchanged, except for the attribute and the class, who
	 * should be re-created without the additional annotation.
	 * <b>Features</b>: fwd, structural, add+del, fixed 
	 */
	@ParameterizedTest
	@MethodSource("tools")
	public void testIncrementalMixed(BXTool<EPackage, Schema, Decisions> tool) {
		this.tool = tool;
		initialise();
		tool.performAndPropagateSourceEdit(srcEdit(
				helperEcore::createSimpleCompositeList,
				helperEcore::changePackageName));
		tool.performIdleTargetEdit(trgEdit(helperSQL::addAnnotationToDataNodeData));
		tool.performIdleTargetEdit(trgEdit(helperSQL::addAnnotationToDataNode));
		
		util.assertPrecondition("CompositeListSimpleEcore", "CompositeListSimpleWithDataAnnotationSQL");
		//------------
		tool.performAndPropagateSourceEdit(srcEdit(
				helperEcore::deleteDataAttribute,
				helperEcore::createDataAttribute));
		//------------
		util.assertPostcondition("CompositeListSimpleEcore", "CompositeListSimpleWithDataNodeAnnotationSQL");
		//------------
		tool.performAndPropagateSourceEdit(srcEdit(
				helperEcore::deleteDataNode,
				helperEcore::createDataNode));
		//------------
		util.assertPostcondition("CompositeListSimpleEcore", "CompositeListSimpleSQL");
		terminate();
	}
	
	/**
	 * <b>Test</b> for stability of the transformation.<br/>
	 * <b>Expect</b> re-running the transformation after an idle source delta does not change the target model.<br/>
	 * <b>Features:</b>: fwd, fixed
	 */
	@ParameterizedTest
	@MethodSource("tools")
	public void testStability(BXTool<EPackage, Schema, Decisions> tool) {
		this.tool = tool;
		initialise();
		//------------
		tool.performAndPropagateSourceEdit(srcEdit(
				helperEcore::createSimpleCompositeList,
				helperEcore::changePackageName));
		tool.performIdleTargetEdit(trgEdit(helperSQL::addAnnotationToDataNodeData));
		tool.performIdleTargetEdit(trgEdit(helperSQL::addAnnotationToDataNode));
		
		util.assertPrecondition("CompositeListSimpleEcore", "CompositeListSimpleWithDataAnnotationSQL");
		//------------
		tool.performAndPropagateSourceEdit(srcEdit(helperEcore::idleDelta));
		//------------
		util.assertPostcondition("CompositeListSimpleEcore", "CompositeListSimpleWithDataAnnotationSQL");
		terminate();
	}
	
	/**
	 * <b>Test</b> for hippocraticness of the transformation.<br/>
	 * <b>Expect</b> re-running the transformation after creating a operation, deleting a operation and changing some EAttribute values
	 * does not change the SQL schema.<br/>
	 * <b>Features:</b>: fwd, fixed
	 */
	@ParameterizedTest
	@MethodSource("tools")
	public void testHippocraticness(BXTool<EPackage, Schema, Decisions> tool) {
		this.tool = tool;
		initialise();
		//------------
		tool.performAndPropagateSourceEdit(srcEdit(
				helperEcore::createSimpleCompositeList,
				helperEcore::changePackageName));
		tool.performIdleTargetEdit(trgEdit(helperSQL::addAnnotationToDataNodeData));
		tool.performIdleTargetEdit(trgEdit(helperSQL::addAnnotationToDataNode));
				
		util.assertPrecondition("CompositeListSimpleEcore", "CompositeListSimpleWithDataAnnotationSQL");
		//------------
		tool.performAndPropagateSourceEdit(srcEdit(helperEcore::hippocraticDelta));
		//------------
		util.assertPostcondition("CompositeListSimpleHippocraticEcore", "CompositeListSimpleWithDataAnnotationSQL");
		terminate();
	}

}
