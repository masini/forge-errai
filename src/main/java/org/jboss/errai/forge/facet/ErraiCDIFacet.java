package org.jboss.errai.forge.facet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import org.jboss.forge.maven.MavenPluginFacet;
import org.jboss.forge.maven.plugins.ExecutionBuilder;
import org.jboss.forge.maven.plugins.MavenPluginBuilder;
import org.jboss.forge.project.dependencies.Dependency;
import org.jboss.forge.project.dependencies.DependencyBuilder;
import org.jboss.forge.project.facets.DependencyFacet;
import org.jboss.forge.project.facets.WebResourceFacet;
import org.jboss.forge.shell.ShellPrintWriter;
import org.jboss.forge.shell.plugins.RequiresFacet;


@RequiresFacet({ DependencyFacet.class, WebResourceFacet.class })
public class ErraiCDIFacet extends ErraiBaseFacet{
    @Inject
    private ShellPrintWriter writer;


	@Override
    protected void installErraiDeps() {
		  String erraiVersion = Versions.getInstance().getErrai_version();
		//TODO list here all the CDI dependencies
	      List<? extends Dependency> dependencies = Arrays.asList(
	              DependencyBuilder.create("javax.servlet:servlet-api:2.5").setScopeType("provided"),
	              DependencyBuilder.create("javax.servlet:jsp-api:2.0").setScopeType("provided"),
	              DependencyBuilder.create("log4j:log4j:1.2.16"),
	              DependencyBuilder.create("junit:junit:4.10").setScopeType("test"),
	              DependencyBuilder.create("org.jboss.ejb3:jboss-ejb3-api:3.1.0").setScopeType("provided"),
	              DependencyBuilder.create("javax.enterprise:cdi-api:1.0-SP4").setScopeType("provided"),
	              DependencyBuilder.create("org.mvel:mvel2:2.1.Beta8"),

	              // CDI Integration
	              DependencyBuilder.create("org.jboss.errai:errai-cdi-client:" + erraiVersion),	              
	              DependencyBuilder.create("org.jboss.errai:errai-javax-enterprise:" + erraiVersion),	              
	              DependencyBuilder.create("org.jboss.errai:errai-weld-integration:" + erraiVersion),	              
	              DependencyBuilder.create("org.jboss.errai:errai-cdi-jetty:" + erraiVersion),

	              //Jetty & Weld
	              DependencyBuilder.create("org.mortbay.jetty:jetty:6.1.25"),
	              DependencyBuilder.create("org.mortbay.jetty:jetty-plus:6.1.25"),
	              DependencyBuilder.create("org.mortbay.jetty:jetty-naming:6.1.25"),
	              DependencyBuilder.create("org.jboss.weld.se:weld-se-core:1.1.6.Final"),
	              DependencyBuilder.create("org.jboss.weld.servlet:weld-servlet:1.1.6.Final"),
	              DependencyBuilder.create("org.jboss.logging:jboss-logging:3.0.0.Beta4")
	              
	      );
	      
        DependencyFacet deps = project.getFacet(DependencyFacet.class);
	      
        List<Dependency> exDependencies = new ArrayList<Dependency>();        
		
		// add dependency exclusions
        Dependency ex1 = DependencyBuilder.create("javax.inject:javax.inject");
        Dependency ex2 = DependencyBuilder.create("javax.annotation:jsr250-api");
	    for(Dependency dep : project.getFacet(DependencyFacet.class).getDependencies()){
	    	writer.println("artifacts: " + dep.getArtifactId());
		    if(dep.getArtifactId().equals("errai-bus") || dep.getArtifactId().equals("errai-ioc")){
		 	   dep.getExcludedDependencies().add(ex1);
		 	   //writer.print("exclusions: " + dep.getExcludedDependencies().toString());
		 	   dep.getExcludedDependencies().add(ex2);
		 	   writer.print("exclusions: " + dep.getExcludedDependencies());
	           deps.removeDependency(dep);
	           exDependencies.add(dep);

		    }
	    } 
		
       for (Dependency dependency : dependencies) {
         deps.addDirectDependency(dependency);
       }
       for (Dependency dependency : exDependencies) {
         deps.addDirectDependency(dependency);
       }
	}

	@Override
    protected boolean isFacetInstalled() {
        if (!project.hasFacet(ErraiCDIFacet.class)) {
    		return false;
        }
		return true;
	}
	@Override
	public void installGWTPlugin() {
		   String gwtVersion = "";
		   for(Dependency dep : project.getFacet(DependencyFacet.class).getDependencies()){
			   if(dep.getGroupId().startsWith("com.google.gwt")){
				   gwtVersion = dep.getVersion();
			   }
		   } 
		    
		    DependencyBuilder gwtDependencyBuilder = DependencyBuilder.create()
				    .setGroupId("org.codehaus.mojo")
				    .setArtifactId("gwt-maven-plugin")
				    .setVersion(gwtVersion);
				     
		    ExecutionBuilder execution1 = ExecutionBuilder.create()
		    		.setId("gwt-clean")
		    		.setPhase("clean")
		    		.addGoal("clean");
		    
		    ExecutionBuilder execution2 = ExecutionBuilder.create()
		    		.setId("gwt-compile")
		    		.addGoal("resources")
		    		.addGoal("compile");
		    
		    MavenPluginBuilder gwtPlugin = MavenPluginBuilder.create()
		    		.setDependency(gwtDependencyBuilder)
				    .createConfiguration()
				    .createConfigurationElement("logLevel")
				    .setText("INFO")
				    .getParentPluginConfig().getOrigin()
				    .createConfiguration()
				    .createConfigurationElement("runTarget")
				    .setText("App.html")
				    .getParentPluginConfig().getOrigin()
				    .createConfiguration()
				    .createConfigurationElement("extraJvmArgs")
				    .setText("-Xmx512m")
				    .getParentPluginConfig().getOrigin()
				    .createConfiguration()
				    .createConfigurationElement("soyc")
				    .setText("false")
				    .getParentPluginConfig().getOrigin()
				    .createConfiguration()
				    .createConfigurationElement("hostedWebapp")
				    .setText("src/main/webapp/")
				    .getParentPluginConfig().getOrigin()
				    .createConfiguration()
				    .createConfigurationElement("server")
				    .setText("org.jboss.errai.cdi.server.gwt.JettyLauncher")
				    .getParentPluginConfig().getOrigin()
				    .addExecution(execution1)	    
		    		.addExecution(execution2);	    
		    
		    MavenPluginFacet pluginFacet = project.getFacet(MavenPluginFacet.class);
		    pluginFacet.addPlugin(gwtPlugin);
	}
}
