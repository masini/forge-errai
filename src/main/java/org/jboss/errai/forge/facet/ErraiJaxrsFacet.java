package org.jboss.errai.forge.facet;

import java.util.Arrays;
import java.util.List;

import org.jboss.forge.project.dependencies.Dependency;
import org.jboss.forge.project.dependencies.DependencyBuilder;
import org.jboss.forge.project.facets.DependencyFacet;
import org.jboss.forge.project.facets.WebResourceFacet;
import org.jboss.forge.shell.plugins.RequiresFacet;


@RequiresFacet({ DependencyFacet.class, WebResourceFacet.class })
public class ErraiJaxrsFacet extends ErraiBaseFacet{

	@Override
    protected void installErraiDeps() {
		  String erraiVersion = Versions.getInstance().getErrai_version();
	      List<? extends Dependency> dependencies = Arrays.asList(
	              DependencyBuilder.create("org.jboss.errai:errai-common:" + erraiVersion),
	              DependencyBuilder.create("org.jboss.errai:errai-jaxrs-client:" + erraiVersion).setScopeType("provided"),
	              DependencyBuilder.create("org.jboss.errai:errai-jaxrs-provider:" + erraiVersion),
	              DependencyBuilder.create("javax.enterprise:cdi-api:1.0-SP4"),
	              DependencyBuilder.create("org.jboss.resteasy:resteasy-jaxrs:2.2.3.GA")
	      );

		   
	      DependencyFacet deps = project.getFacet(DependencyFacet.class);
	      for (Dependency dependency : dependencies) {
	         deps.addDirectDependency(dependency);
	      }
		
	}

	@Override
    protected boolean isFacetInstalled() {
        if (!project.hasFacet(ErraiJaxrsFacet.class)) {
    		return false;
        }
		return true;
	}
}
