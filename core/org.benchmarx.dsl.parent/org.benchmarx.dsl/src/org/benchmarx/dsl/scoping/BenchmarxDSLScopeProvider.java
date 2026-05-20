package org.benchmarx.dsl.scoping;

import java.util.ArrayList;
import java.util.List;

import org.benchmarx.dsl.benchmarxDSL.BxModel;
import org.benchmarx.dsl.benchmarxDSL.ProblemDecl;
import org.benchmarx.dsl.benchmarxDSL.StateDecl;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.xtext.resource.EObjectDescription;
import org.eclipse.xtext.resource.IEObjectDescription;
import org.eclipse.xtext.scoping.IScope;
import org.eclipse.xtext.scoping.impl.SimpleScope;

public class BenchmarxDSLScopeProvider extends AbstractBenchmarxDSLScopeProvider {

	/**
	 * Makes all ProblemDecl elements in the resource set visible for
	 * StateDecl.metamodel cross-references, enabling cross-file references.
	 */
	public IScope scope_StateDecl_Metamodel(StateDecl context, EReference ref) {
		Resource res = context.eResource();
		if (res == null) return IScope.NULLSCOPE;
		ResourceSet rs = res.getResourceSet();
		if (rs == null) return IScope.NULLSCOPE;

		List<IEObjectDescription> descs = new ArrayList<>();
		for (Resource r : new ArrayList<>(rs.getResources())) {
			for (EObject obj : r.getContents()) {
				if (obj instanceof BxModel model) {
					for (ProblemDecl p : model.getProblems()) {
						if (p.getName() != null)
							descs.add(EObjectDescription.create(p.getName(), p));
					}
				}
			}
		}
		return new SimpleScope(descs);
	}
}
