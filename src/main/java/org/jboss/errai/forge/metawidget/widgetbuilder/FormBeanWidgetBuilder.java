// Metawidget
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

package org.jboss.errai.forge.metawidget.widgetbuilder;

import org.metawidget.statically.javacode.JavaStatement;
import org.metawidget.statically.javacode.StaticJavaMetawidget;
import org.metawidget.statically.javacode.StaticJavaStub;
import org.metawidget.statically.javacode.StaticJavaWidget;
import org.metawidget.widgetbuilder.iface.WidgetBuilder;

import java.util.Map;

import static org.metawidget.inspector.InspectionResultConstants.*;

public class FormBeanWidgetBuilder
         implements WidgetBuilder<StaticJavaWidget, StaticJavaMetawidget>
{
   //
   // Public methods
   //

   @Override
   public StaticJavaWidget buildWidget(String elementName, Map<String, String> attributes,
            StaticJavaMetawidget metawidget)
   {
      if ( ENTITY.equals( elementName )) {
         return null;
      }

      if ( TRUE.equals(attributes.get(HIDDEN))) {
         return new StaticJavaStub();
      }

      return new JavaStatement("@Inject @DataField @Bound private TextBox " + attributes.get(NAME));
   }
}
