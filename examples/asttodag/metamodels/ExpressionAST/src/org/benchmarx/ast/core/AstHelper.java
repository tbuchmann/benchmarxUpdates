package org.benchmarx.ast.core;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.benchmarx.ast.core.AstModelBuilder.Direction;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;

import ast.ArithmeticOperator;
import ast.Expression;
import ast.Model;

public class AstHelper {
	private AstModelBuilder builder;
	private Supplier<Model> rootSupplier;
	private BiConsumer<EAttribute, List<?>> changeAttribute;
	private Consumer<EObject> deleteNode;
	private BiConsumer<EReference, List<EObject>> deleteEdge;
	
	public AstHelper(Supplier<Model> rootSupplier, Consumer<EObject> createSourceNode,
			BiConsumer<EReference, List<EObject>> createSourceEdge,
			BiConsumer<EAttribute, List<?>> changeSourceAttribute, Consumer<EObject> deleteSourceNode,
			BiConsumer<EObject, List<EObject>> moveSourceNode, BiConsumer<EReference, List<EObject>> deleteSourceEdge) {
		builder = new AstModelBuilder(rootSupplier.get());
		this.rootSupplier = rootSupplier;
		this.changeAttribute = changeSourceAttribute;
		this.deleteEdge = deleteSourceEdge;
		this.deleteNode = deleteSourceNode;
	}
	
	public void create42() {		
		builder.number(Direction.UP).setValue(42);
	}
	
	public void createTextSum() {
		builder.operator(Direction.UP).setOp(ArithmeticOperator.ADD);
		builder.operator(Direction.LEFT).setOp(ArithmeticOperator.ADD);
		builder.variable(Direction.LEFT).setName("Answer to the Ultimate Question of Life, The Universe, and Everything");
		builder.navigate(Direction.UP).variable(Direction.RIGHT).setName("Deep Thought");
		builder.navigateToRoot().variable(Direction.RIGHT).setName("7.5 million years");
	}
	
	public void createComplexNumberExample() {
		builder.operator(Direction.UP).setOp(ArithmeticOperator.ADD);
		builder.operator(Direction.LEFT).setOp(ArithmeticOperator.SUBTRACT);
		builder.operator(Direction.LEFT).setOp(ArithmeticOperator.MULTIPLY);
		builder.operator(Direction.LEFT).setOp(ArithmeticOperator.ADD);
		builder.number(Direction.LEFT).setValue(10).navigate(Direction.UP);
		builder.number(Direction.RIGHT).setValue(1).navigate(Direction.UP, Direction.UP);
		builder.operator(Direction.RIGHT).setOp(ArithmeticOperator.DIVIDE);
		builder.number(Direction.LEFT).setValue(10).navigate(Direction.UP);
		builder.number(Direction.RIGHT).setValue(5).navigate(Direction.UP, Direction.UP, Direction.UP);
		builder.number(Direction.RIGHT).setValue(1).navigateToRoot();
		
		builder.operator(Direction.RIGHT).setOp(ArithmeticOperator.SUBTRACT);
		builder.operator(Direction.LEFT).setOp(ArithmeticOperator.MULTIPLY);
		builder.operator(Direction.RIGHT).setOp(ArithmeticOperator.ADD);
		builder.number(Direction.RIGHT).setValue(10).navigate(Direction.UP);
		builder.number(Direction.LEFT).setValue(1).navigate(Direction.UP, Direction.UP);
		builder.operator(Direction.LEFT).setOp(ArithmeticOperator.DIVIDE);
		builder.number(Direction.LEFT).setValue(10).navigate(Direction.UP);
		builder.number(Direction.RIGHT).setValue(5).navigate(Direction.UP, Direction.UP, Direction.UP);
		builder.number(Direction.RIGHT).setValue(1).navigateToRoot();
	}
	
	public void createMulitpleSubtrees() {
		builder.operator(Direction.UP).setOp(ArithmeticOperator.SUBTRACT)
			.operator(Direction.LEFT).setOp(ArithmeticOperator.MULTIPLY)
				.operator(Direction.LEFT).setOp(ArithmeticOperator.ADD)
					.operator(Direction.LEFT).setOp(ArithmeticOperator.ADD)
						.number(Direction.LEFT).setValue(1).navigate(Direction.UP)
						.number(Direction.RIGHT).setValue(1).navigate(Direction.UP, Direction.UP)
					.operator(Direction.RIGHT).setOp(ArithmeticOperator.ADD)
						.number(Direction.LEFT).setValue(1).navigate(Direction.UP)
						.operator(Direction.RIGHT).setOp(ArithmeticOperator.ADD)
							.operator(Direction.LEFT).setOp(ArithmeticOperator.ADD)
								.number(Direction.LEFT).setValue(1).navigate(Direction.UP)
								.number(Direction.RIGHT).setValue(1).navigate(Direction.UP, Direction.UP)
							.operator(Direction.RIGHT).setOp(ArithmeticOperator.ADD)
								.number(Direction.LEFT).setValue(1).navigate(Direction.UP)
								.number(Direction.RIGHT).setValue(1).navigateToRoot().navigate(Direction.LEFT)
				.operator(Direction.RIGHT).setOp(ArithmeticOperator.ADD)
					.operator(Direction.LEFT).setOp(ArithmeticOperator.ADD)
						.number(Direction.LEFT).setValue(1).navigate(Direction.UP)
						.number(Direction.RIGHT).setValue(1).navigate(Direction.UP, Direction.UP)
					.operator(Direction.RIGHT).setOp(ArithmeticOperator.ADD)
						.number(Direction.LEFT).setValue(1).navigate(Direction.UP)
						.operator(Direction.RIGHT).setOp(ArithmeticOperator.ADD)
							.operator(Direction.LEFT).setOp(ArithmeticOperator.ADD)
								.number(Direction.LEFT).setValue(1).navigate(Direction.UP)
								.number(Direction.RIGHT).setValue(1).navigate(Direction.UP, Direction.UP)
							.operator(Direction.RIGHT).setOp(ArithmeticOperator.ADD)
								.number(Direction.LEFT).setValue(1).navigate(Direction.UP)
								.number(Direction.RIGHT).setValue(1).navigateToRoot()
			.operator(Direction.RIGHT).setOp(ArithmeticOperator.DIVIDE)
				.operator(Direction.LEFT).setOp(ArithmeticOperator.ADD)
					.operator(Direction.LEFT).setOp(ArithmeticOperator.ADD)
						.operator(Direction.LEFT).setOp(ArithmeticOperator.ADD)
							.number(Direction.LEFT).setValue(1).navigate(Direction.UP)
							.number(Direction.RIGHT).setValue(1).navigate(Direction.UP, Direction.UP)
						.operator(Direction.RIGHT).setOp(ArithmeticOperator.ADD)
							.number(Direction.LEFT).setValue(1).navigate(Direction.UP)
							.operator(Direction.RIGHT).setOp(ArithmeticOperator.ADD)
								.operator(Direction.LEFT).setOp(ArithmeticOperator.ADD)
									.number(Direction.LEFT).setValue(1).navigate(Direction.UP)
									.number(Direction.RIGHT).setValue(1).navigate(Direction.UP, Direction.UP)
								.operator(Direction.RIGHT).setOp(ArithmeticOperator.ADD)
									.number(Direction.LEFT).setValue(1).navigate(Direction.UP)
									.number(Direction.RIGHT).setValue(1).navigateToRoot().navigate(Direction.RIGHT, Direction.LEFT)
					.operator(Direction.RIGHT).setOp(ArithmeticOperator.ADD)
						.operator(Direction.LEFT).setOp(ArithmeticOperator.ADD)
							.number(Direction.LEFT).setValue(1).navigate(Direction.UP)
							.number(Direction.RIGHT).setValue(1).navigate(Direction.UP, Direction.UP)
						.operator(Direction.RIGHT).setOp(ArithmeticOperator.ADD)
							.number(Direction.LEFT).setValue(1).navigate(Direction.UP)
							.operator(Direction.RIGHT).setOp(ArithmeticOperator.ADD)
								.operator(Direction.LEFT).setOp(ArithmeticOperator.ADD)
									.number(Direction.LEFT).setValue(1).navigate(Direction.UP)
									.number(Direction.RIGHT).setValue(1).navigate(Direction.UP, Direction.UP)
								.operator(Direction.RIGHT).setOp(ArithmeticOperator.ADD)
									.number(Direction.LEFT).setValue(1).navigate(Direction.UP)
									.number(Direction.RIGHT).setValue(1).navigateToRoot().navigate(Direction.RIGHT)
				.operator(Direction.RIGHT).setOp(ArithmeticOperator.ADD)
					.number(Direction.LEFT).setValue(1).navigate(Direction.UP)
					.number(Direction.RIGHT).setValue(1).navigateToRoot();
	}
	
	public void createBestDigit() {
		builder.operator(Direction.UP).setOp(ArithmeticOperator.SUBTRACT)
			.operator(Direction.LEFT).setOp(ArithmeticOperator.MULTIPLY)
				.number(Direction.LEFT).setValue(7).navigate(Direction.UP)
				.variable(Direction.RIGHT).setName("sieben").navigate(Direction.UP, Direction.UP)
			.number(Direction.RIGHT).setValue(7).navigateToRoot();
	}
	
	public void insertMoreBestDigits() { // in BestDigit
		builder.navigate(Direction.RIGHT).delete().operator(Direction.RIGHT).setOp(ArithmeticOperator.SUBTRACT)
			.operator(Direction.LEFT).setOp(ArithmeticOperator.MULTIPLY)
				.number(Direction.LEFT).setValue(7).navigate(Direction.UP)
				.variable(Direction.RIGHT).setName("sieben").navigate(Direction.UP, Direction.UP)
			.variable(Direction.RIGHT).setName("zweiundvierzig").navigateToRoot();	
	}
	
	public void createBestDigitRef() {
		builder.operator(Direction.UP).setOp(ArithmeticOperator.ADD)
			.operator(Direction.LEFT).setOp(ArithmeticOperator.SUBTRACT)
				.operator(Direction.LEFT).setOp(ArithmeticOperator.MULTIPLY)
					.number(Direction.LEFT).setValue(7).navigate(Direction.UP)
					.variable(Direction.RIGHT).setName("sieben").navigate(Direction.UP, Direction.UP)
				.number(Direction.RIGHT).setValue(7).navigate(Direction.UP, Direction.UP)
			.operator(Direction.RIGHT).setOp(ArithmeticOperator.MULTIPLY)
				.operator(Direction.LEFT).setOp(ArithmeticOperator.SUBTRACT)
					.number(Direction.LEFT).setValue(14).navigate(Direction.UP)
					.number(Direction.RIGHT).setValue(14).navigate(Direction.UP, Direction.UP)
				.operator(Direction.RIGHT).setOp(ArithmeticOperator.DIVIDE)
					.number(Direction.LEFT).setValue(7).navigate(Direction.UP)
					.number(Direction.RIGHT).setValue(2).navigateToRoot();
	}	
	
	public void modifyBestDigitRef() { // in BestDigitRef
		builder.navigate(Direction.LEFT)
			.setOp(ArithmeticOperator.MULTIPLY)
				.navigate(Direction.LEFT).setOp(ArithmeticOperator.SUBTRACT)
					.navigate(Direction.LEFT).setValue(8).navigate(Direction.UP)
					.navigate(Direction.RIGHT).setName("zwei").navigate(Direction.UP, Direction.UP, Direction.UP)
			.navigate(Direction.RIGHT)
				.navigate(Direction.RIGHT).setOp(ArithmeticOperator.MULTIPLY)
					.navigate(Direction.LEFT).setValue(14).navigate(Direction.UP)
					.navigate(Direction.RIGHT).setValue(14).navigateToRoot();
	}
	
	public void createMoreBestDigits() {
		createBestDigit();
		insertMoreBestDigits();
	}
	
	public void removeSomeBestDigits() { // from MoreBestDigits
		builder.navigate(Direction.RIGHT).delete().navigateToRoot();
		builder.number(Direction.RIGHT).setValue(7).navigateToRoot();
	}
	
	public void createSimpleASTRef() {
		builder.operator(Direction.UP).setOp(ArithmeticOperator.MULTIPLY)
			.operator(Direction.LEFT).setOp(ArithmeticOperator.ADD)
				.variable(Direction.LEFT).setName("a").navigate(Direction.UP)
				.number(Direction.RIGHT).setValue(5).navigate(Direction.UP, Direction.UP)
			.operator(Direction.RIGHT).setOp(ArithmeticOperator.ADD)
				.variable(Direction.LEFT).setName("a").navigate(Direction.UP)
				.number(Direction.RIGHT).setValue(7).navigateToRoot();
	}
	
	public void modifySimpleASTRef() {
		builder.navigate(Direction.RIGHT).navigate(Direction.RIGHT).setValue(5).navigateToRoot();
	}
	
	public void changeIncrementalID() {
		rootSupplier.get().eAllContents().forEachRemaining(e -> {if(e instanceof Expression) ((Expression)e).setIncrementalID("incrTestValue");});
	}
	
//	public void createStillBestDigit(Model model) {
//		builder = new AstModelBuilder(model);
//		builder.operator(Direction.UP).setOp(ArithmeticOperator.ADD)
//			.operator(Direction.LEFT).setOp(ArithmeticOperator.SUBTRACT)
//				.operator(Direction.LEFT).setOp(ArithmeticOperator.MULTIPLY)
//					.number(Direction.LEFT).setValue(7).navigate(Direction.UP)
//					.variable(Direction.RIGHT).setName("sieben").navigate(Direction.UP, Direction.UP)
//				.operator(Direction.RIGHT).setOp(ArithmeticOperator.MULTIPLY)
//					.number(Direction.LEFT).setValue(7).navigate(Direction.UP)
//					.variable(Direction.RIGHT).setName("sieben").navigate(Direction.UP, Direction.UP, Direction.UP)
//			.variable(Direction.RIGHT).setName("zweiundvierzig").navigateToRoot();
//	}
	
	public void idleDelta() {
	}
}
