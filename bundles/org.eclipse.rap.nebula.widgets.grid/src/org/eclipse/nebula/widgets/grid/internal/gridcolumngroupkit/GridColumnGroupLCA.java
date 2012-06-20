/*******************************************************************************
 * Copyright (c) 2012 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.grid.internal.gridcolumngroupkit;

import static org.eclipse.rwt.lifecycle.WidgetLCAUtil.preserveProperty;
import static org.eclipse.rwt.lifecycle.WidgetLCAUtil.renderProperty;

import java.io.IOException;

import org.eclipse.nebula.widgets.grid.GridColumnGroup;
import org.eclipse.rwt.internal.lifecycle.JSConst;
import org.eclipse.rwt.internal.protocol.ClientObjectFactory;
import org.eclipse.rwt.internal.protocol.IClientObject;
import org.eclipse.rwt.internal.protocol.ProtocolUtil;
import org.eclipse.rwt.lifecycle.AbstractWidgetLCA;
import org.eclipse.rwt.lifecycle.WidgetLCAUtil;
import org.eclipse.rwt.lifecycle.WidgetUtil;
import org.eclipse.swt.events.TreeEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.internal.widgets.ItemLCAUtil;
import org.eclipse.swt.widgets.Widget;


@SuppressWarnings("restriction")
public class GridColumnGroupLCA extends AbstractWidgetLCA {

  private static final String TYPE = "rwt.widgets.GridColumnGroup";
  private static final String[] ALLOWED_STYLES = new String[] { "TOGGLE" };

  private static final String PROP_FONT = "font";
  private static final String PROP_EXPANDED = "expanded";

  @Override
  public void renderInitialization( Widget widget ) throws IOException {
    GridColumnGroup group = ( GridColumnGroup )widget;
    IClientObject clientObject = ClientObjectFactory.getClientObject( group );
    clientObject.create( TYPE );
    clientObject.set( "parent", WidgetUtil.getId( group.getParent() ) );
    clientObject.set( "style", WidgetLCAUtil.getStyles( group, ALLOWED_STYLES ) );
  }

  public void readData( Widget widget ) {
    GridColumnGroup group = ( GridColumnGroup )widget;
    processTreeEvent( group, JSConst.EVENT_TREE_EXPANDED );
    processTreeEvent( group, JSConst.EVENT_TREE_COLLAPSED );
  }

  @Override
  public void preserveValues( Widget widget ) {
    GridColumnGroup group = ( GridColumnGroup )widget;
    ItemLCAUtil.preserve( group );
    preserveProperty( group, PROP_FONT, group.getHeaderFont() );
    preserveProperty( group, PROP_EXPANDED, group.getExpanded() );
  }

  @Override
  public void renderChanges( Widget widget ) throws IOException {
    GridColumnGroup group = ( GridColumnGroup )widget;
    ItemLCAUtil.renderChanges( group );
    renderFont( group, PROP_FONT, group.getHeaderFont() );
    renderProperty( group, PROP_EXPANDED, group.getExpanded(), true );
  }

  @Override
  public void renderDispose( Widget widget ) throws IOException {
    ClientObjectFactory.getClientObject( widget ).destroy();
  }

  ////////////////////////////////////////////
  // Helping methods to read client-side state

  private static void processTreeEvent( GridColumnGroup group, String eventName ) {
    if( WidgetLCAUtil.wasEventSent( group, eventName ) ) {
      boolean expanded = eventName.equals( JSConst.EVENT_TREE_EXPANDED );
      group.setExpanded( expanded );
      if( TreeEvent.hasListener( group ) ) {
        int eventType = expanded ? TreeEvent.TREE_EXPANDED : TreeEvent.TREE_COLLAPSED;
        TreeEvent event = new TreeEvent( group, null, eventType );
        event.processEvent();
      }
    }
  }

  //////////////////////////////////////////////
  // Helping methods to render widget properties

  private static void renderFont( GridColumnGroup group, String property, Font newValue ) {
    if( WidgetLCAUtil.hasChanged( group, property, newValue, group.getParent().getFont() ) ) {
      IClientObject clientObject = ClientObjectFactory.getClientObject( group );
      clientObject.set( property, ProtocolUtil.getFontAsArray( newValue ) );
    }
  }
}
