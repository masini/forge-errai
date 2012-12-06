package org.jboss.errai.forge.facet.ui;

import java.util.*;

import org.jboss.errai.forge.facet.ErraiBaseFacet;
import org.jboss.errai.forge.facet.Versions;
import org.jboss.forge.env.Configuration;
import org.jboss.forge.parser.java.JavaClass;
import org.jboss.forge.project.dependencies.Dependency;
import org.jboss.forge.project.dependencies.DependencyBuilder;
import org.jboss.forge.project.facets.DependencyFacet;
import org.jboss.forge.project.facets.JavaSourceFacet;
import org.jboss.forge.project.facets.ResourceFacet;
import org.jboss.forge.project.facets.WebResourceFacet;
import org.jboss.forge.project.facets.events.InstallFacets;
import org.jboss.forge.resources.Resource;
import org.jboss.forge.scaffold.AccessStrategy;
import org.jboss.forge.scaffold.ScaffoldProvider;
import org.jboss.forge.scaffold.TemplateStrategy;
import org.jboss.forge.scaffold.util.ScaffoldUtil;
import org.jboss.forge.shell.ShellColor;
import org.jboss.forge.shell.ShellPrintWriter;
import org.jboss.forge.shell.ShellPrompt;
import org.jboss.forge.shell.plugins.Alias;
import org.jboss.forge.shell.plugins.Help;
import org.jboss.forge.shell.plugins.RequiresFacet;
import org.jboss.forge.shell.util.Streams;
import org.jboss.forge.spec.javaee.PersistenceFacet;
import org.jboss.seam.render.TemplateCompiler;
import org.jboss.seam.render.spi.TemplateResolver;
import org.jboss.seam.render.template.CompiledTemplateResource;
import org.jboss.seam.render.template.resolver.ClassLoaderTemplateResolver;
import org.metawidget.util.CollectionUtils;

import javax.enterprise.event.Event;
import javax.inject.Inject;


@RequiresFacet({ ErraiBaseFacet.class, DependencyFacet.class, WebResourceFacet.class, PersistenceFacet.class})
public class ErraiUIFacet extends ErraiBaseFacet implements ScaffoldProvider {

    private static final String ERRAI_APP_PROPERTIES_TEMPLATE = "/errai-ui/resources/ErraiApp.properties";

    protected final ShellPrompt prompt;
    protected final TemplateCompiler compiler;
    protected final Event<InstallFacets> install;
    private final Configuration config;
    protected TemplateResolver<ClassLoader> resolver;

    protected CompiledTemplateResource erraiAppPropertiesTemplate;

    @Inject
    private ShellPrintWriter writer;

    @Inject
    public ErraiUIFacet(final Configuration config,
                         final ShellPrompt prompt,
                         final TemplateCompiler compiler,
                         final Event<InstallFacets> install)
    {
        this.config = config;
        this.prompt = prompt;
        this.compiler = compiler;
        this.install = install;

        this.resolver = new ClassLoaderTemplateResolver(ErraiUIFacet.class.getClassLoader());

        if (this.compiler != null)
        {
            this.compiler.getTemplateResolverFactory().addResolver(this.resolver);
        }
    }

	@Override
    protected void  installErraiDeps() {
		  String erraiVersion = Versions.getInstance().getErrai_version();
		  String javaeeVersion = Versions.getInstance().getJavaee_version();
		  
	      List<? extends Dependency> dependencies = Arrays.asList(
	              DependencyBuilder.create("org.jboss.errai:errai-javaee-all:" + erraiVersion),
	              DependencyBuilder.create("org.jboss.spec:jboss-javaee-6.0:" + javaeeVersion).setScopeType("provided").setPackagingType("pom")
	      );

		   
	      DependencyFacet deps = project.getFacet(DependencyFacet.class);
	      for (Dependency dependency : dependencies) {
	         deps.addDirectDependency(dependency);
	      }
		
	}

	@Override
    protected boolean isFacetInstalled() {
        if (!project.hasFacet(ErraiUIFacet.class)) {
    		return false;
        }
		return true;
	}

    public List<Resource<?>> setup(String targetDir, final Resource<?> template, final boolean overwrite) {
        writer.println(ShellColor.RED, "setup");

        List<Resource<?>> resources = generateIndex(targetDir, template, overwrite);

        return resources;
    }

    public List<Resource<?>> generateTemplates(String s, boolean b) {
        System.out.println("generateTemplates");
        return Collections.emptyList();  //To change body of implemented methods use File | Settings | File Templates.
    }

    public List<Resource<?>> generateIndex(String targetDir, final Resource<?> template, final boolean overwrite) {
        writer.println(ShellColor.RED, "generateIndex");

        WebResourceFacet web = this.project.getFacet(WebResourceFacet.class);
        JavaSourceFacet java = this.project.getFacet(JavaSourceFacet.class);
        ResourceFacet resourceFacet = this.project.getFacet(ResourceFacet.class);
        Map<Object, Object> context = CollectionUtils.newHashMap();

        loadTemplates();

        List<Resource<?>> result = new ArrayList<Resource<?>>();
        result.add(ScaffoldUtil.createOrOverwrite(this.prompt, web.getWebResource("/App.html"),
                getClass().getResourceAsStream("/errai-ui/webapp/App.html"), overwrite));
        result.add(ScaffoldUtil.createOrOverwrite(this.prompt, web.getWebResource("/App.css"),
                getClass().getResourceAsStream("/errai-ui/webapp/App.css"), overwrite));

        result.add(ScaffoldUtil.createOrOverwrite(this.prompt, resourceFacet.getResource("ErraiApp.properties"), this.erraiAppPropertiesTemplate.render(context),
                true));

        return result;
    }

    public List<Resource<?>> generateFromEntity(String s, Resource<?> resource, JavaClass javaClass, boolean b) {
        throw new RuntimeException("Not yet implemented!");
    }

    public List<Resource<?>> getGeneratedResources(String s) {
        throw new RuntimeException("Not yet implemented!");
    }

    public AccessStrategy getAccessStrategy() {
        return new ErraiUIAccessStrategy(project);
    }

    public TemplateStrategy getTemplateStrategy() {
        return new ErraiUITemplateStrategy(project);
    }

    protected void loadTemplates()
    {
        if (this.erraiAppPropertiesTemplate == null)
        {
            this.erraiAppPropertiesTemplate = this.compiler.compile(ERRAI_APP_PROPERTIES_TEMPLATE);
            String template = Streams.toString(this.erraiAppPropertiesTemplate.getSourceTemplateResource().getInputStream());
        }
    }

}
