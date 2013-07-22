/*******************************************************************************
 * Copyright (c) 2012, 2013 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.grid.internal.gridcolumngroupkit;

import static org.eclipse.rap.rwt.internal.protocol.JsonUtil.createJsonArray;
import static org.eclipse.rap.rwt.internal.protocol.ProtocolUtil.getJsonForFont;
import static org.eclipse.rap.rwt.internal.protocol.RemoteObjectFactory.createRemoteObject;
import static org.eclipse.rap.rwt.internal.protocol.RemoteObjectFactory.getRemoteObject;
import static org.eclipse.rap.rwt.lifecycle.WidgetLCAUtil.getStyles;
import static org.eclipse.rap.rwt.lifecycle.WidgetLCAUtil.hasChanged;
import static org.eclipse.rap.rwt.lifecycle.WidgetLCAUtil.preserveListener;
import static org.eclipse.rap.rwt.lifecycle.WidgetLCAUtil.preserveProperty;
import static org.eclipse.rap.rwt.lifecycle.WidgetLCAUtil.renderListener;
import static org.eclipse.rap.rwt.lifecycle.WidgetLCAUtil.renderProperty;
import static org.eclipse.rap.rwt.lifecycle.WidgetLCAUtil.wasEventSent;
import static org.eclipse.rap.rwt.lifecycle.WidgetUtil.getId;
import static org.eclipse.swt.internal.events.EventLCAUtil.isListening;

import java.io.IOException;

import org.eclipse.nebula.widgets.grid.Grid;
import org.eclipse.nebula.widgets.grid.GridColumn;
import org.eclipse.nebula.widgets.grid.GridColumnGroup;
import org.eclipse.rap.rwt.internal.protocol.ClientMessageConst;
import org.eclipse.rap.rwt.lifecycle.AbstractWidgetLCA;
import org.eclipse.rap.rwt.lifecycle.ProcessActionRunner;
import org.eclipse.rap.rwt.lifecycle.WidgetLCAUtil;
import org.eclipse.rap.rwt.remote.RemoteObject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.internal.widgets.ItemLCAUtil;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Widget;


@SuppressWarnings("restriction")
public class GridColumnGroupLCA extends AbstractWidgetLCA {

  private static final String TYPE = "rwt.widgets.GridColumnGroup";
  private static final String[] ALLOWED_STYLES = new String[] { "TOGGLE" };

  private static final String PROP_LEFT = "left";
  private static final String PROP_WIDTH = "width";
  private static final String PROP_HEIGHT = "height";
  private static final String PROP_VISIBLE = "visibility";
  private static final String PROP_FONT = "font";
  private static final String PROP_EXPANDED = "expanded";
  private static final String PROP_EXPAND_LISTENER = "Expand";
  private static final String PROP_COLLAPSE_LISTENER = "Collapse";

  private static final int ZERO = 0;

  @Override
  public void renderInitialization( Widget widget ) throws IOException {
    GridColumnGroup group = ( GridColumnGroup )widget;
    RemoteObject remoteObject = createRemoteObject( group, TYPE );
    remoteObject.set( "parent", getId( group.getParent() ) );
    remoteObject.set( "style", createJsonArray( getStyles( group, ALLOWED_STYLES ) ) );
  }

  @Override
  public void readData( Widget widget ) {
    GridColumnGroup group = ( GridColumnGroup )widget;
    readExpanded( group );
    processTreeEvent( group, SWT.Expand, ClientMessageConst.EVENT_EXPAND );
    processTreeEvent( group, SWT.Collapse, ClientMessageConst.EVENT_COLLAPSE );
  }

  @Override
  public void preserveValues( Widget widget ) {
    GridColumnGroup group = ( GridColumnGroup )widget;
    WidgetLCAUtil.preserveCustomVariant( group );
    ItemLCAUtil.preserve( group );
    preserveProperty( group, PROP_LEFT, getLeft( group ) );
    preserveProperty( group, PROP_WIDTH, getWidth( group ) );
    preserveProperty( group, PROP_HEIGHT, getHeight( group ) );
    preserveProperty( group, PROP_VISIBLE, isVisible( group ) );
    preserveProperty( group, PROP_FONT, group.getHeaderFont() );
    preserveProperty( group, PROP_EXPANDED, group.getExpanded() );
    preserveListener( group, PROP_EXPAND_LISTENER, hasExpandListener( group ) );
    preserveListener( group, PROP_COLLAPSE_LISTENER, hasCollapseListener( group ) );
  }

  @Override
  public void renderChanges( Widget widget ) throws IOException {
    GridColumnGroup group = ( GridColumnGroup )widget;
    WidgetLCAUtil.renderCustomVariant( group );
    ItemLCAUtil.renderChanges( group );
    renderProperty( group, PROP_LEFT, getLeft( group ), ZERO );
    renderProperty( group, PROP_WIDTH, getWidth( group ), ZERO );
    renderProperty( group, PROP_HEIGHT, getHeight( group ), ZERO );
    renderProperty( group, PROP_VISIBLE, isVisible( group ), true );
    renderFont( group, PROP_FONT, group.getHeaderFont() );
    renderProperty( group, PROP_EXPANDED, group.getExpanded(), true );
    renderListener( group, PROP_EXPAND_LISTENER, hasExpandListener( group ), false );
    renderListener( group, PROP_COLLAPSE_LISTENER, hasCollapseListener( group ), false );
  }

  ////////////////////////////////////////////
  // Helping methods to read client-side state

  private static void readExpanded( final GridColumnGroup group ) {
    final String expanded = WidgetLCAUtil.readPropertyValue( group, PROP_EXPANDED );
    if( expanded != null ) {
      ProcessActionRunner.add( new Runnable() {
        public void run() {
          group.setExpanded( Boolean.valueOf( expanded ).booleanValue() );
//          preserveProperty( group, PROP_EXPANDED, group.getExpanded() );
        }
      } );
    }
  }

  private static void processTreeEvent( GridColumnGroup group, int eventType, String eventName ) {
    if( wasEventSent( group, eventName ) && isListening( group, eventType ) ) {
      group.notifyListeners( eventType, new Event() );
    }
  }

  //////////////////////////////////////////////
  // Helping methods to render widget properties

  private static void renderFont( GridColumnGroup group, String property, Font newValue ) {
    if( hasChanged( group, property, newValue, group.getParent().getFont() ) ) {
      getRemoteObject( group ).set( property, getJsonForFont( newValue ) );
    }
  }

  //////////////////
  // Helping methods

  private static int getLeft( GridColumnGroup group ) {
    int result = 0;
    Grid grid = group.getParent();
    int[] columnOrder = grid.getColumnOrder();
    boolean found = false;
    for( int i = 0; i < columnOrder.length && !found; i++ ) {
      GridColumn currentColumn = grid.getColumn( columnOrder[ i ] );
      if( currentColumn.getColumnGroup() == group ) {
        found = true;
      } else if( currentColumn.isVisible() ) {
        result += currentColumn.getWidth();
      }
    }
    return result;
  }

  private static int getWidth( GridColumnGroup group ) {
    int result = 0;
    GridColumn[] columns = group.getColumns();
    for( int i = 0; i < columns.length; i++ ) {
      if( columns[ i ].isVisible() ) {
        result += columns[ i ].getWidth();
      }
    }
    return result;
  }

  private static int getHeight( GridColumnGroup group ) {
    return group.getParent().getGroupHeaderHeight();
  }

  private static boolean isVisible( GridColumnGroup group ) {
    boolean result = false;
    GridColumn[] columns = group.getColumns();
    for( int i = 0; i < columns.length && !result; i++ ) {
      if( columns[ i ].isVisible() ) {
        result = true;
      }
    }
    return result;
  }

  private static boolean hasExpandListener( GridColumnGroup group ) {
    // Always render listen for Expand and Collapse, currently required for columns
    // visibility update.
    return true;
  }

  private static boolean hasCollapseListener( GridColumnGroup group ) {
    // Always render listen for Expand and Collapse, currently required for columns
    // visibility update.
    return true;
  }

}
