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
package org.jboss.errai.forge.metawidget.config;

import org.jboss.forge.env.Configuration;
import org.jboss.forge.project.Project;
import org.metawidget.config.iface.ResourceResolver;
import org.metawidget.config.impl.BaseConfigReader;
import org.metawidget.inspector.iface.InspectorException;
import org.metawidget.util.ClassUtils;

import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * ConfigReader with Forge-specific features.
 *
 * @author Richard Kennard
 */

public class ForgeConfigReader
         extends BaseConfigReader
{

    public static class LocalResourceResolver 	implements ResourceResolver {

        public InputStream openResource( String resource ) {

            if ( resource == null || "".equals( resource.trim() ) ) {
                throw InspectorException.newException(new FileNotFoundException("No resource specified"));
            }

            // Thread's ClassLoader

            ClassLoader loaderContext = Thread.currentThread().getContextClassLoader();

            if ( loaderContext != null ) {
                InputStream stream = loaderContext.getResourceAsStream( resource );

                if ( stream != null ) {
                    return stream;
                }
            }

            // Our ClassLoader

            InputStream stream = getClass().getResourceAsStream( resource );

            if ( stream != null ) {
                return stream;
            }

            throw InspectorException.newException(new FileNotFoundException("Unable to locate " + resource + " on CLASSPATH"));
        }
    }

    //
   // Private statics
   //

   private static final String CONFIG_ELEMENT_NAME = "forgeConfig";

   private static final String PROJECT_ELEMENT_NAME = "forgeProject";

   //
   // Private members
   //

   private Configuration config;

   private Project project;

   //
   // Constructor
   //

   public ForgeConfigReader(Configuration config, Project project)
   {
       super(new LocalResourceResolver());

      this.config = config;
      this.project = project;
   }

   //
   // Protected methods
   //

   @Override
   protected boolean isNative(String name)
   {
      if (PROJECT_ELEMENT_NAME.equals(name))
      {
         return true;
      }

      if (CONFIG_ELEMENT_NAME.equals(name))
      {
         return true;
      }

      return super.isNative(name);
   }

   @Override
   protected Object createNative(String name, Class<?> namespace, String recordedText) throws Exception
   {
      if (PROJECT_ELEMENT_NAME.equals(name))
      {
         return this.project;
      }

      if (CONFIG_ELEMENT_NAME.equals(name))
      {
         return this.config;
      }

      return super.createNative(name, namespace, recordedText);
   }

    @Override
    protected Class<?> lookupClass( String className, ClassLoader classLoader ) {

        Class<?> aClass = ClassUtils.niceForName(className, classLoader);

        if( aClass==null ) {
            aClass = ClassUtils.niceForName(className, this.getClass().getClassLoader());
        }
        return aClass;
    }

}
