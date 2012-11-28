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

package org.jboss.errai.forge.metawidget.widgetprocessor;

import org.metawidget.statically.StaticXmlMetawidget;
import org.metawidget.statically.StaticXmlWidget;
import org.metawidget.statically.html.widgetbuilder.HtmlInput;
import org.metawidget.widgetprocessor.iface.WidgetProcessor;

import java.util.Map;

import static org.metawidget.inspector.InspectionResultConstants.NAME;

/**
 * WidgetProcessor to add 'data-field' attributes.
 *
 * @author Richard Kennard
 */

public class DataFieldWidgetProcessor
         implements WidgetProcessor<StaticXmlWidget, StaticXmlMetawidget>
{
   //
   // Public methods
   //

   @Override
   public StaticXmlWidget processWidget(StaticXmlWidget widget, String elementName, Map<String, String> attributes,
            StaticXmlMetawidget metawidget)
   {
      if (widget instanceof HtmlInput)
      {
         widget.putAttribute("data-field", attributes.get(NAME));
      }

      return widget;
   }
}
