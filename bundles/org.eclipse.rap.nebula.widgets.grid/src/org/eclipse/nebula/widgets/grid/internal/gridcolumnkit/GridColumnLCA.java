/*******************************************************************************
 * Copyright (c) 2012, 2014 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.grid.internal.gridcolumnkit;

import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.preserveListener;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.preserveProperty;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.renderListener;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.renderProperty;
import static org.eclipse.rap.rwt.internal.protocol.RemoteObjectFactory.createRemoteObject;
import static org.eclipse.rap.rwt.internal.protocol.RemoteObjectFactory.getRemoteObject;
import static org.eclipse.rap.rwt.remote.JsonMapping.toJson;
import static org.eclipse.swt.internal.events.EventLCAUtil.isListening;

import java.io.IOException;

import org.eclipse.nebula.widgets.grid.GridColumn;
import org.eclipse.nebula.widgets.grid.GridColumnGroup;
import org.eclipse.nebula.widgets.grid.internal.IGridAdapter;
import org.eclipse.rap.rwt.internal.lifecycle.AbstractWidgetLCA;
import org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil;
import org.eclipse.rap.rwt.internal.lifecycle.WidgetUtil;
import org.eclipse.rap.rwt.remote.RemoteObject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.internal.widgets.ItemLCAUtil;
import org.eclipse.swt.widgets.Widget;


@SuppressWarnings( "restriction" )
public class GridColumnLCA extends AbstractWidgetLCA {

  private static final String TYPE = "rwt.widgets.GridColumn";

  private static final String PROP_INDEX = "index";
  private static final String PROP_LEFT = "left";
  private static final String PROP_WIDTH = "width";
  private static final String PROP_ALIGNMENT = "alignment";
  private static final String PROP_RESIZABLE = "resizable";
  private static final String PROP_MOVEABLE = "moveable";
  private static final String PROP_VISIBLE = "visibility";
  private static final String PROP_CHECK = "check";
  private static final String PROP_FONT = "font";
  private static final String PROP_FOOTER_FONT = "footerFont";
  private static final String PROP_FOOTER_TEXT = "footerText";
  private static final String PROP_FOOTER_IMAGE = "footerImage";
  private static final String PROP_WORD_WRAP = "wordWrap";
  private static final String PROP_HEADER_WORD_WRAP = "headerWordWrap";
  private static final String PROP_SELECTION_LISTENER = "Selection";

  private static final int ZERO = 0;
  private static final String DEFAULT_ALIGNMENT = "left";

  @Override
  public void renderInitialization( Widget widget ) throws IOException {
    GridColumn column = ( GridColumn )widget;
    RemoteObject remoteObject = createRemoteObject( column, TYPE );
    remoteObject.setHandler( new GridColumnOperationHandler( column ) );
    remoteObject.set( "parent", WidgetUtil.getId( column.getParent() ) );
    GridColumnGroup group = column.getColumnGroup();
    if( group != null ) {
      remoteObject.set( "group", WidgetUtil.getId( group ) );
    }
  }

  @Override
  public void preserveValues( Widget widget ) {
    GridColumn column = ( GridColumn )widget;
    WidgetLCAUtil.preserveToolTipText( column, column.getHeaderTooltip() );
    WidgetLCAUtil.preserveCustomVariant( column );
    ItemLCAUtil.preserve( column );
    preserveProperty( column, PROP_INDEX, getIndex( column ) );
    preserveProperty( column, PROP_LEFT, getLeft( column ) );
    preserveProperty( column, PROP_WIDTH, column.getWidth() );
    preserveProperty( column, PROP_ALIGNMENT, getAlignment( column ) );
    preserveProperty( column, PROP_RESIZABLE, column.getResizeable() );
    preserveProperty( column, PROP_MOVEABLE, column.getMoveable() );
    preserveProperty( column, PROP_VISIBLE, column.isVisible() );
    preserveProperty( column, PROP_CHECK, column.isCheck() );
    preserveProperty( column, PROP_FONT, column.getHeaderFont() );
    preserveProperty( column, PROP_FOOTER_FONT, column.getFooterFont() );
    preserveProperty( column, PROP_FOOTER_TEXT, column.getFooterText() );
    preserveProperty( column, PROP_FOOTER_IMAGE, column.getFooterImage() );
    preserveProperty( column, PROP_WORD_WRAP, column.getWordWrap() );
    preserveProperty( column, PROP_HEADER_WORD_WRAP, column.getHeaderWordWrap() );
    preserveListener( column, PROP_SELECTION_LISTENER, isListening( column, SWT.Selection ) );
  }

  @Override
  public void renderChanges( Widget widget ) throws IOException {
    GridColumn column = ( GridColumn )widget;
    WidgetLCAUtil.renderToolTip( column, column.getHeaderTooltip() );
    WidgetLCAUtil.renderCustomVariant( column );
    ItemLCAUtil.renderChanges( column );
    renderProperty( column, PROP_INDEX, getIndex( column ), -1 );
    renderProperty( column, PROP_LEFT, getLeft( column ), ZERO );
    renderProperty( column, PROP_WIDTH, column.getWidth(), ZERO );
    renderProperty( column, PROP_ALIGNMENT, getAlignment( column ), DEFAULT_ALIGNMENT );
    renderProperty( column, PROP_RESIZABLE, column.getResizeable(), true );
    renderProperty( column, PROP_MOVEABLE, column.getMoveable(), false );
    renderProperty( column, PROP_VISIBLE, column.isVisible(), true );
    renderProperty( column, PROP_CHECK, column.isCheck(), false );
    renderFont( column, PROP_FONT, column.getHeaderFont() );
    renderFont( column, PROP_FOOTER_FONT, column.getFooterFont() );
    renderProperty( column, PROP_FOOTER_TEXT, column.getFooterText(), "" );
    renderProperty( column, PROP_FOOTER_IMAGE, column.getFooterImage(), null );
    renderProperty( column, PROP_WORD_WRAP, column.getWordWrap(), false );
    renderProperty( column, PROP_HEADER_WORD_WRAP, column.getHeaderWordWrap(), false );
    renderListener( column, PROP_SELECTION_LISTENER, isListening( column, SWT.Selection ), false );
  }

  //////////////////////////////////////////////
  // Helping methods to render widget properties

  private static void renderFont( GridColumn column, String property, Font newValue ) {
    if( WidgetLCAUtil.hasChanged( column, property, newValue, column.getParent().getFont() ) ) {
      RemoteObject remoteObject = getRemoteObject( column );
      remoteObject.set( property, toJson( newValue ) );
    }
  }

  //////////////////
  // Helping methods

  private static int getLeft( GridColumn column ) {
    return getGridAdapter( column ).getCellLeft( getIndex( column ) );
  }

  private static String getAlignment( GridColumn column ) {
    int alignment = column.getAlignment();
    String result = "left";
    if( ( alignment & SWT.CENTER ) != 0 ) {
      result = "center";
    } else if( ( alignment & SWT.RIGHT ) != 0 ) {
      result = "right";
    }
    return result;
  }

  private static int getIndex( GridColumn column ) {
    return column.getParent().indexOf( column );
  }

  private static IGridAdapter getGridAdapter( GridColumn column ) {
    return column.getParent().getAdapter( IGridAdapter.class );
  }

}
