/*******************************************************************************
 * Copyright (c) 2013 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.grid.internal.griditemkit;

import static org.eclipse.rap.rwt.lifecycle.WidgetLCAUtil.preserveProperty;

import org.eclipse.nebula.widgets.grid.GridItem;
import org.eclipse.rap.json.JsonArray;
import org.eclipse.rap.json.JsonObject;
import org.eclipse.rap.json.JsonValue;
import org.eclipse.rap.rwt.internal.protocol.WidgetOperationHandler;
import org.eclipse.rap.rwt.lifecycle.ProcessActionRunner;


@SuppressWarnings( "restriction" )
public class GridItemOperationHandler extends WidgetOperationHandler<GridItem> {

  private static final String PROP_CELL_CHECKED = "cellChecked";
  private static final String PROP_EXPANDED = "expanded";

  public GridItemOperationHandler( GridItem item ) {
    super( item );
  }

  @Override
  public void handleSet( GridItem item, JsonObject properties ) {
    handleSetChecked( item, properties );
    handleSetExpanded( item, properties );
  }

  /*
   * PROTOCOL SET checked
   *
   * @param checked ([boolean]) array with item checked states (by column)
   */
  public void handleSetChecked( GridItem item, JsonObject properties ) {
    JsonValue value = properties.get( PROP_CELL_CHECKED );
    if( value != null ) {
      JsonArray arrayValue = value.asArray();
      for( int i = 0; i < arrayValue.size(); i++ ) {
        item.setChecked( i, arrayValue.get( i ).asBoolean() );
      }
    }
  }

  /*
   * PROTOCOL SET expanded
   *
   * @param expanded (boolean) true if the item was expanded, false otherwise
   */
  public void handleSetExpanded( final GridItem item, JsonObject properties ) {
    final JsonValue expanded = properties.get( PROP_EXPANDED );
    if( expanded != null ) {
      ProcessActionRunner.add( new Runnable() {
        public void run() {
          item.setExpanded( expanded.asBoolean() );
          preserveProperty( item, PROP_EXPANDED, item.isExpanded() );
        }
      } );
    }
  }

}
