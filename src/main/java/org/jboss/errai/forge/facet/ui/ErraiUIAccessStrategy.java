/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.errai.forge.facet.ui;

import org.jboss.forge.project.Project;
import org.jboss.forge.resources.Resource;
import org.jboss.forge.scaffold.AccessStrategy;
import org.jboss.forge.spec.javaee.FacesFacet;

import java.util.List;

/**
 * Faces Scaffold Access Strategy
 *
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public class ErraiUIAccessStrategy implements AccessStrategy
{
   final FacesFacet faces;

   public ErraiUIAccessStrategy(final Project project)
   {
      this.faces = project.getFacet(FacesFacet.class);
   }

   @Override
   public List<String> getWebPaths(final Resource<?> resource)
   {
      return this.faces.getWebPaths(resource);
   }

   @Override
   public Resource<?> fromWebPath(final String path)
   {
      return this.faces.getResourceForWebPath(path);
   }

}
