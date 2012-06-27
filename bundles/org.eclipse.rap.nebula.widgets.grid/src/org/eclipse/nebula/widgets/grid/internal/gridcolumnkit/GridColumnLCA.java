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
package org.eclipse.nebula.widgets.grid.internal.gridcolumnkit;

import static org.eclipse.rwt.lifecycle.WidgetLCAUtil.preserveListener;
import static org.eclipse.rwt.lifecycle.WidgetLCAUtil.preserveProperty;
import static org.eclipse.rwt.lifecycle.WidgetLCAUtil.renderListener;
import static org.eclipse.rwt.lifecycle.WidgetLCAUtil.renderProperty;

import java.io.IOException;
import java.util.Arrays;

import org.eclipse.nebula.widgets.grid.Grid;
import org.eclipse.nebula.widgets.grid.GridColumn;
import org.eclipse.nebula.widgets.grid.GridColumnGroup;
import org.eclipse.nebula.widgets.grid.internal.IGridAdapter;
import org.eclipse.rwt.internal.protocol.ClientObjectFactory;
import org.eclipse.rwt.internal.protocol.IClientObject;
import org.eclipse.rwt.internal.protocol.ProtocolUtil;
import org.eclipse.rwt.internal.util.NumberFormatUtil;
import org.eclipse.rwt.lifecycle.AbstractWidgetLCA;
import org.eclipse.rwt.lifecycle.ControlLCAUtil;
import org.eclipse.rwt.lifecycle.IWidgetAdapter;
import org.eclipse.rwt.lifecycle.ProcessActionRunner;
import org.eclipse.rwt.lifecycle.WidgetLCAUtil;
import org.eclipse.rwt.lifecycle.WidgetUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.internal.widgets.ItemLCAUtil;
import org.eclipse.swt.widgets.Widget;


@SuppressWarnings("restriction")
public class GridColumnLCA extends AbstractWidgetLCA {

  private static final String TYPE = "rwt.widgets.GridColumn";

  private static final String PROP_INDEX = "index";
  private static final String PROP_LEFT = "left";
  private static final String PROP_WIDTH = "width";
  private static final String PROP_ALIGNMENT = "alignment";
  private static final String PROP_RESIZABLE = "resizable";
  private static final String PROP_MOVEABLE = "moveable";
  private static final String PROP_VISIBLE = "visibility";
  private static final String PROP_FONT = "font";
  private static final String PROP_FOOTER_FONT = "footerFont";
  private static final String PROP_FOOTER_TEXT = "footerText";
  private static final String PROP_FOOTER_IMAGE = "footerImage";
  private static final String PROP_SELECTION_LISTENER = "selection";

  private static final int ZERO = 0;
  private static final String DEFAULT_ALIGNMENT = "left";

  @Override
  public void renderInitialization( Widget widget ) throws IOException {
    GridColumn column = ( GridColumn )widget;
    IClientObject clientObject = ClientObjectFactory.getClientObject( column );
    clientObject.create( TYPE );
    clientObject.set( "parent", WidgetUtil.getId( column.getParent() ) );
    GridColumnGroup group = column.getColumnGroup();
    if( group != null ) {
      clientObject.set( "group", WidgetUtil.getId( group ) );
    }
  }

  public void readData( Widget widget ) {
    GridColumn column = ( GridColumn )widget;
    readLeft( column );
    readWidth( column );
    ControlLCAUtil.processSelection( column, null, false );
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
    preserveProperty( column, PROP_FONT, column.getHeaderFont() );
    preserveProperty( column, PROP_FOOTER_FONT, column.getFooterFont() );
    preserveProperty( column, PROP_FOOTER_TEXT, column.getFooterText() );
    preserveProperty( column, PROP_FOOTER_IMAGE, column.getFooterImage() );
    preserveListener( column, PROP_SELECTION_LISTENER, SelectionEvent.hasListener( column ) );
  }

  @Override
  public void renderChanges( Widget widget ) throws IOException {
    GridColumn column = ( GridColumn )widget;
    WidgetLCAUtil.renderToolTip( column, column.getHeaderTooltip() );
    WidgetLCAUtil.renderCustomVariant( column );
    ItemLCAUtil.renderChanges( column );
    renderProperty( column, PROP_INDEX, getIndex( column ), ZERO );
    renderProperty( column, PROP_LEFT, getLeft( column ), ZERO );
    renderProperty( column, PROP_WIDTH, column.getWidth(), ZERO );
    renderProperty( column, PROP_ALIGNMENT, getAlignment( column ), DEFAULT_ALIGNMENT );
    renderProperty( column, PROP_RESIZABLE, column.getResizeable(), true );
    renderProperty( column, PROP_MOVEABLE, column.getMoveable(), false );
    renderProperty( column, PROP_VISIBLE, column.isVisible(), true );
    renderFont( column, PROP_FONT, column.getHeaderFont() );
    renderFont( column, PROP_FOOTER_FONT, column.getFooterFont() );
    renderProperty( column, PROP_FOOTER_TEXT, column.getFooterText(), "" );
    renderProperty( column, PROP_FOOTER_IMAGE, column.getFooterImage(), null );
    renderListener( column, PROP_SELECTION_LISTENER, SelectionEvent.hasListener( column ), false );
  }

  @Override
  public void renderDispose( Widget widget ) throws IOException {
    ClientObjectFactory.getClientObject( widget ).destroy();
  }

  ////////////////////////////////////////////
  // Helping methods to read client-side state

  private static void readLeft( final GridColumn column ) {
    String value = WidgetLCAUtil.readPropertyValue( column, "left" );
    if( value != null ) {
      final int newLeft = NumberFormatUtil.parseInt( value );
      ProcessActionRunner.add( new Runnable() {
        public void run() {
          moveColumn( column, newLeft );
        }
      } );
    }
  }

  private static void readWidth( final GridColumn column ) {
    String value = WidgetLCAUtil.readPropertyValue( column, "width" );
    if( value != null ) {
      final int newWidth = NumberFormatUtil.parseInt( value );
      ProcessActionRunner.add( new Runnable() {
        public void run() {
          column.setWidth( newWidth );
        }
      } );
    }
  }

  //////////////////////////////////////////////
  // Helping methods to render widget properties

  private static void renderFont( GridColumn column, String property, Font newValue ) {
    if( WidgetLCAUtil.hasChanged( column, property, newValue, column.getParent().getFont() ) ) {
      IClientObject clientObject = ClientObjectFactory.getClientObject( column );
      clientObject.set( property, ProtocolUtil.getFontAsArray( newValue ) );
    }
  }

  /////////////////////////////////
  // Helping methods to move column

  static void moveColumn( GridColumn column, int newLeft ) {
    Grid grid = column.getParent();
    int index = grid.indexOf( column );
    int targetColumn = findMoveTarget( grid, newLeft );
    int[] columnOrder = grid.getColumnOrder();
    int orderIndex = arrayIndexOf( columnOrder, index );
    columnOrder = arrayRemove( columnOrder, orderIndex );
    if( orderIndex < targetColumn ) {
      targetColumn--;
    }
    columnOrder = arrayInsert( columnOrder, targetColumn, index );
    if( Arrays.equals( columnOrder, grid.getColumnOrder() ) ) {
      GridColumn[] columns = grid.getColumns();
      for( int i = 0; i < columns.length; i++ ) {
        IWidgetAdapter adapter = WidgetUtil.getAdapter( columns[ i ] );
        adapter.preserve( PROP_LEFT, null );
      }
    } else {
      grid.setColumnOrder( columnOrder );
      IWidgetAdapter adapter = WidgetUtil.getAdapter( column );
      adapter.preserve( PROP_LEFT, null );
    }
  }

  private static int findMoveTarget( Grid grid, int newLeft ) {
    int result = -1;
    GridColumn[] columns = grid.getColumns();
    int[] columnOrder = grid.getColumnOrder();
    if( newLeft < 0 ) {
      result = 0;
    } else {
      for( int i = 0; result == -1 && i < columns.length; i++ ) {
        GridColumn column = columns[ columnOrder[ i ] ];
        int left = getLeft( column );
        int width = column.getWidth();
        if( newLeft >= left && newLeft <= left + width ) {
          result = i;
          if( newLeft >= left + width / 2 && result < columns.length ) {
            result++;
          }
        }
      }
    }
    if( result == -1 ) {
      result = columns.length;
    }
    return result;
  }

  //////////////////
  // Helping methods

  private static int getIndex( GridColumn column ) {
    return column.getParent().indexOf( column );
  }

  private static int getLeft( GridColumn column ) {
    Grid grid = column.getParent();
    IGridAdapter adapter = grid.getAdapter( IGridAdapter.class );
    return adapter.getCellLeft( grid.indexOf( column ) );
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

  private static int arrayIndexOf( int[] array, int value ) {
    int result = -1;
    for( int i = 0; result == -1 && i < array.length; i++ ) {
      if( array[ i ] == value ) {
        result = i;
      }
    }
    return result;
  }

  private static int[] arrayRemove( int[] array, int index ) {
    int length = array.length;
    int[] result = new int[ length - 1 ];
    System.arraycopy( array, 0, result, 0, index );
    if( index < length - 1 ) {
      System.arraycopy( array, index + 1, result, index, length - index - 1 );
    }
    return result;
  }

  private static int[] arrayInsert( int[] array, int index, int value ) {
    int length = array.length;
    int[] result = new int[ length + 1 ];
    System.arraycopy( array, 0, result, 0, length );
    System.arraycopy( result, index, result, index + 1, length - index );
    result[ index ] = value;
    return result;
  }
}
