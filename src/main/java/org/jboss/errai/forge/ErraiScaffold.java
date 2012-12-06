/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc., and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.errai.forge;

import org.jboss.errai.forge.facet.ui.ErraiUIFacet;
import org.jboss.errai.forge.metawidget.config.ForgeConfigReader;
import org.jboss.forge.env.Configuration;
import org.jboss.forge.parser.JavaParser;
import org.jboss.forge.parser.java.JavaClass;
import org.jboss.forge.project.Project;
import org.jboss.forge.project.facets.*;
import org.jboss.forge.project.facets.events.InstallFacets;
import org.jboss.forge.resources.Resource;
import org.jboss.forge.scaffold.AccessStrategy;
import org.jboss.forge.scaffold.ScaffoldProvider;
import org.jboss.forge.scaffold.TemplateStrategy;
import org.jboss.forge.scaffold.util.ScaffoldUtil;
import org.jboss.forge.shell.ShellPrompt;
import org.jboss.forge.shell.plugins.Alias;
import org.jboss.forge.shell.plugins.Help;
import org.jboss.forge.shell.plugins.RequiresFacet;
import org.jboss.forge.shell.util.Streams;
import org.jboss.forge.spec.javaee.CDIFacet;
import org.jboss.forge.spec.javaee.EJBFacet;
import org.jboss.forge.spec.javaee.PersistenceFacet;
import org.jboss.seam.render.TemplateCompiler;
import org.jboss.seam.render.spi.TemplateResolver;
import org.jboss.seam.render.template.CompiledTemplateResource;
import org.jboss.seam.render.template.resolver.ClassLoaderTemplateResolver;
import org.metawidget.statically.html.StaticHtmlMetawidget;
import org.metawidget.statically.javacode.StaticJavaMetawidget;
import org.metawidget.util.CollectionUtils;
import org.metawidget.util.simple.StringUtils;

import javax.enterprise.event.Event;
import javax.inject.Inject;
import java.io.File;
import java.io.StringWriter;
import java.util.*;

/**
 * @author Richard Kennard
 */
@Alias("errai-ui")
@Help("Errai scaffolding")
@RequiresFacet({ WebResourceFacet.class,
         DependencyFacet.class,
         PersistenceFacet.class,
         EJBFacet.class,
         CDIFacet.class,
         ErraiUIFacet.class})
public class ErraiScaffold extends BaseFacet implements ScaffoldProvider
{
   //
   // Private statics
   //

   private static final String FORM_BEAN_TEMPLATE = "scaffold/errai/FormBean.jv";
   private static final String FORM_TEMPLATE = "scaffold/errai/form.xhtml";
   //
   // Protected members (nothing is private, to help subclassing)
   //
   protected CompiledTemplateResource formBeanTemplate;
   protected int formBeanTemplateMetawidgetIndent;
   protected CompiledTemplateResource formTemplate;
   protected int formTemplateMetawidgetIndent;
   protected TemplateResolver<ClassLoader> resolver;
   protected final ShellPrompt prompt;
   protected final TemplateCompiler compiler;
   protected final Event<InstallFacets> install;
   protected StaticHtmlMetawidget formMetawidget;
   protected StaticJavaMetawidget formBeanMetawidget;

   private Configuration config;

   //
   // Constructor
   //

   @Inject
   public ErraiScaffold(final Configuration config,
            final ShellPrompt prompt,
            final TemplateCompiler compiler,
            final Event<InstallFacets> install)
   {
      this.config = config;
      this.prompt = prompt;
      this.compiler = compiler;
      this.install = install;

      this.resolver = new ClassLoaderTemplateResolver(ErraiScaffold.class.getClassLoader());

      if (this.compiler != null)
      {
         this.compiler.getTemplateResolverFactory().addResolver(this.resolver);
      }
   }

   //
   // Public methods
   //
   @Override
   public List<Resource<?>> setup(String targetDir, final Resource<?> template, final boolean overwrite)
   {
      return generateIndex(targetDir, template, overwrite);
   }

   /**
    * Overridden to setup the Metawidgets.
    * <p>
    * Metawidgets must be configured per project <em>and per Forge invocation</em>. It is not sufficient to simply
    * configure them in <code>setup</code> because the user may restart Forge and not run <code>scaffold setup</code> a
    * second time.
    */
   @Override
   public void setProject(Project project)
   {
      super.setProject(project);

      ForgeConfigReader configReader = new ForgeConfigReader(this.config, this.project);

      this.formMetawidget = new StaticHtmlMetawidget();
      this.formMetawidget.setConfigReader(configReader);
      this.formMetawidget.setConfig("/scaffold/errai/metawidget-form.xml");

      this.formBeanMetawidget = new StaticJavaMetawidget();
      this.formBeanMetawidget.setConfigReader(configReader);
      this.formBeanMetawidget.setConfig("/scaffold/errai/metawidget-formbean.xml");

   }

   @Override
   public List<Resource<?>> generateFromEntity(String targetDir, final Resource<?> template, final JavaClass entity,
            final boolean overwrite)
   {
      // Track the list of resources generated

      List<Resource<?>> result = new ArrayList<Resource<?>>();
      try
      {
         JavaSourceFacet java = this.project.getFacet(JavaSourceFacet.class);
         ResourceFacet resource = this.project.getFacet(ResourceFacet.class);
         WebResourceFacet web = this.project.getFacet(WebResourceFacet.class);

         loadTemplates();
         Map<Object, Object> context = CollectionUtils.newHashMap();
         context.put("entity", entity);
         String ccEntity = StringUtils.decapitalize(entity.getName());
         context.put("ccEntity", ccEntity);

         // Prepare formBeanMetawidget
         this.formBeanMetawidget.setPath(entity.getQualifiedName());
         StringWriter stringWriter = new StringWriter();
         this.formBeanMetawidget.write(stringWriter, this.formBeanTemplateMetawidgetIndent);

         context.put("formBeanMetawidget", stringWriter.toString().trim());
         Set<String> formBeanMetawidgetImports = this.formBeanMetawidget.getImports();
         formBeanMetawidgetImports.remove(entity.getQualifiedName());
         context.put("formBeanMetawidgetImports",
                  CollectionUtils.toString(formBeanMetawidgetImports, ";\r\nimport ", true, false));

         // Create formBean
         JavaClass viewBean = JavaParser.parse(JavaClass.class, this.formBeanTemplate.render(context));
         viewBean.setPackage(java.getBasePackage() + ".client.view");
         result.add(ScaffoldUtil.createOrOverwrite(this.prompt, java.getJavaResource(viewBean), viewBean.toString(),
                 overwrite));

         // Set new context for view generation
         context = getTemplateContext(targetDir, template);
         String beanName = StringUtils.decapitalize(viewBean.getName());
         context.put("beanName", beanName);
         context.put("ccEntity", ccEntity);
         context.put("entityName", StringUtils.uncamelCase(entity.getName()));

         // Prepare formMetawidget
         this.formMetawidget.setPath(entity.getQualifiedName());

         // Generate form
         stringWriter = new StringWriter();
         this.formMetawidget.write(stringWriter, this.formTemplateMetawidgetIndent);
         context.put("metawidget", stringWriter.toString().trim());
         result.add(ScaffoldUtil.createOrOverwrite(this.prompt,
                 resource.getResource(viewBean.getQualifiedName().replace('.', File.separatorChar)+".html"),
                 this.formTemplate.render(context), overwrite));

         this.project.getFacet(JavaSourceFacet.class).saveJavaSource(entity);

      }
      catch (Exception e)
      {
         throw new RuntimeException("Error generating default scaffolding: " + e.getMessage(), e);
      }
      return result;
   }

   @Override
   @SuppressWarnings("unchecked")
   public boolean install()
   {
      if (!(this.project.hasFacet(WebResourceFacet.class) && this.project.hasFacet(PersistenceFacet.class)
               && this.project.hasFacet(CDIFacet.class)))
      {
         this.install.fire(new InstallFacets(WebResourceFacet.class, PersistenceFacet.class, CDIFacet.class));
      }

      return true;
   }

   @Override
   public boolean isInstalled()
   {
      return true;
   }

   @Override
   public List<Resource<?>> generateIndex(String targetDir, final Resource<?> template, final boolean overwrite)
   {
      return new ArrayList<Resource<?>>();
   }

   @Override
   public List<Resource<?>> getGeneratedResources(String targetDir)
   {
      throw new RuntimeException("Not yet implemented!");
   }

   @Override
   public AccessStrategy getAccessStrategy()
   {
      return null;
   }

   @Override
   public TemplateStrategy getTemplateStrategy()
   {
      return new ErraiTemplateStrategy(this.project);
   }

   @Override
   public List<Resource<?>> generateTemplates(String targetDir, final boolean overwrite)
   {
      List<Resource<?>> result = new ArrayList<Resource<?>>();

      try
      {
         WebResourceFacet web = this.project.getFacet(WebResourceFacet.class);

         result.add(ScaffoldUtil.createOrOverwrite(this.prompt,
                 web.getWebResource("/resources/scaffold/paginator.xhtml"),
                 getClass().getResourceAsStream("/scaffold/errai/paginator.xhtml"),
                 overwrite));
      }
      catch (Exception e)
      {
         throw new RuntimeException("Error generating default templates", e);
      }

      return result;
   }

   //
   // Protected methods (nothing is private, to help subclassing)
   //
   protected void loadTemplates()
   {
      if (this.formBeanTemplate == null)
      {
         this.formBeanTemplate = this.compiler.compile(FORM_BEAN_TEMPLATE);
         String template = Streams.toString(this.formBeanTemplate.getSourceTemplateResource().getInputStream());
         this.formBeanTemplateMetawidgetIndent = parseIndent(template, "@{formBeanMetawidget}");
      }
      if (this.formTemplate == null)
      {
         this.formTemplate = this.compiler.compile(FORM_TEMPLATE);
         String template = Streams.toString(this.formTemplate.getSourceTemplateResource().getInputStream());
         this.formTemplateMetawidgetIndent = parseIndent(template, "@{metawidget}");
      }
   }

   protected HashMap<Object, Object> getTemplateContext(String targetDir, final Resource<?> template)
   {
      HashMap<Object, Object> context;
      context = new HashMap<Object, Object>();
      context.put("template", template);
      context.put("templateStrategy", getTemplateStrategy());
      context.put("targetDir", targetDir);
      return context;
   }

   /**
    * Parses the given XML and determines the indent of the given String namespaces that Metawidget introduces.
    */
   protected int parseIndent(final String template, final String indentOf)
   {
      int indent = 0;
      int indexOf = template.indexOf(indentOf);

      while ((indexOf >= 0) && (template.charAt(indexOf) != '\n'))
      {
         if (template.charAt(indexOf) == '\t')
         {
            indent++;
         }

         indexOf--;
      }

      return indent;
   }
}
