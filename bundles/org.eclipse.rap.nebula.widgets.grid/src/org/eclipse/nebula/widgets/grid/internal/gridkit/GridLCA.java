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
package org.eclipse.nebula.widgets.grid.internal.gridkit;

import static org.eclipse.rwt.lifecycle.WidgetLCAUtil.preserveListener;
import static org.eclipse.rwt.lifecycle.WidgetLCAUtil.preserveProperty;
import static org.eclipse.rwt.lifecycle.WidgetLCAUtil.renderListener;
import static org.eclipse.rwt.lifecycle.WidgetLCAUtil.renderProperty;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.nebula.widgets.grid.Grid;
import org.eclipse.nebula.widgets.grid.GridColumn;
import org.eclipse.nebula.widgets.grid.GridItem;
import org.eclipse.nebula.widgets.grid.internal.IGridAdapter;
import org.eclipse.rwt.internal.lifecycle.JSConst;
import org.eclipse.rwt.internal.protocol.ClientObjectFactory;
import org.eclipse.rwt.internal.protocol.IClientObject;
import org.eclipse.rwt.internal.service.ContextProvider;
import org.eclipse.rwt.internal.util.NumberFormatUtil;
import org.eclipse.rwt.lifecycle.AbstractWidgetLCA;
import org.eclipse.rwt.lifecycle.ControlLCAUtil;
import org.eclipse.rwt.lifecycle.WidgetLCAUtil;
import org.eclipse.rwt.lifecycle.WidgetUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.internal.events.EventLCAUtil;
import org.eclipse.swt.internal.widgets.CellToolTipUtil;
import org.eclipse.swt.internal.widgets.ICellToolTipAdapter;
import org.eclipse.swt.internal.widgets.ICellToolTipProvider;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Widget;


@SuppressWarnings("restriction")
public class GridLCA extends AbstractWidgetLCA {

  private static final String TYPE = "rwt.widgets.Grid";
  private static final String[] ALLOWED_STYLES = new String[] {
    "SINGLE",
    "MULTI",
    "FULL_SELECTION",
    "CHECK",
    "VIRTUAL",
    "NO_FOCUS",
    "BORDER"
  };

  private static final String PROP_ITEM_COUNT = "itemCount";
  private static final String PROP_ITEM_HEIGHT = "itemHeight";
  private static final String PROP_ITEM_METRICS = "itemMetrics";
  private static final String PROP_COLUMN_COUNT = "columnCount";
  private static final String PROP_TREE_COLUMN = "treeColumn";
  private static final String PROP_HEADER_HEIGHT = "headerHeight";
  private static final String PROP_HEADER_VISIBLE = "headerVisible";
  private static final String PROP_FOOTER_HEIGHT = "footerHeight";
  private static final String PROP_FOOTER_VISIBLE = "footerVisible";
  private static final String PROP_LINES_VISIBLE = "linesVisible";
  private static final String PROP_TOP_ITEM_INDEX = "topItemIndex";
  private static final String PROP_FOCUS_ITEM = "focusItem";
  private static final String PROP_SCROLL_LEFT = "scrollLeft";
  private static final String PROP_SELECTION = "selection";
  // TODO: [if] Sync sortDirection and sortColumn in GridColumnLCA when multiple sort columns are
  // possible on the client
  private static final String PROP_SORT_DIRECTION = "sortDirection";
  private static final String PROP_SORT_COLUMN = "sortColumn";
  private static final String PROP_SCROLLBARS_VISIBLE = "scrollBarsVisible";
  private static final String PROP_SCROLLBARS_SELECTION_LISTENER = "scrollBarsSelection";
  private static final String PROP_SELECTION_LISTENER = "selection";
  // TODO: [if] Sync toolTipText in GridItemLCA when it's possible on the client
  private static final String PROP_ENABLE_CELL_TOOLTIP = "enableCellToolTip";
  private static final String PROP_CELL_TOOLTIP_TEXT = "cellToolTipText";

  private static final int ZERO = 0 ;
  private static final String[] DEFAULT_SELECTION = new String[ 0 ];
  private static final String DEFAULT_SORT_DIRECTION = "none";
  private static final boolean[] DEFAULT_SCROLLBARS_VISIBLE = new boolean[] { false, false };

  @Override
  public void renderInitialization( Widget widget ) throws IOException {
    Grid grid = ( Grid )widget;
    IClientObject clientObject = ClientObjectFactory.getClientObject( grid );
    clientObject.create( TYPE );
    clientObject.set( "parent", WidgetUtil.getId( grid.getParent() ) );
    clientObject.set( "style", WidgetLCAUtil.getStyles( grid, ALLOWED_STYLES ) );
    clientObject.set( "appearance", "tree" );
    IGridAdapter adapter = getGridAdapter( grid );
    clientObject.set( "indentionWidth", adapter.getIndentationWidth() );
    if( ( grid.getStyle() & SWT.CHECK ) != 0 ) {
      int[] checkMetrics = new int[] { adapter.getCheckLeft(), adapter.getCheckWidth() };
      clientObject.set( "checkBoxMetrics", checkMetrics );
    }
  }

  public void readData( Widget widget ) {
    Grid grid = ( Grid )widget;
    readSelection( grid );
    readScrollLeft( grid );
    readTopItemIndex( grid );
    readCellToolTipTextRequested( grid );
    processSelectionEvent( grid, JSConst.EVENT_WIDGET_SELECTED );
    processSelectionEvent( grid, JSConst.EVENT_WIDGET_DEFAULT_SELECTED );
    ControlLCAUtil.processMouseEvents( grid );
    ControlLCAUtil.processKeyEvents( grid );
    ControlLCAUtil.processMenuDetect( grid );
    WidgetLCAUtil.processHelp( grid );
  }

  @Override
  public void preserveValues( Widget widget ) {
    Grid grid = ( Grid )widget;
    ControlLCAUtil.preserveValues( ( Control )widget );
    WidgetLCAUtil.preserveCustomVariant( grid );
    preserveProperty( grid, PROP_ITEM_COUNT, grid.getRootItemCount() );
    preserveProperty( grid, PROP_ITEM_HEIGHT, grid.getItemHeight() );
    preserveProperty( grid, PROP_ITEM_METRICS, getItemMetrics( grid ) );
    preserveProperty( grid, PROP_COLUMN_COUNT, grid.getColumnCount() );
    preserveProperty( grid, PROP_TREE_COLUMN, getTreeColumn( grid ) );
    preserveProperty( grid, PROP_HEADER_HEIGHT, grid.getHeaderHeight() );
    preserveProperty( grid, PROP_HEADER_VISIBLE, grid.getHeaderVisible() );
    preserveProperty( grid, PROP_FOOTER_HEIGHT, grid.getFooterHeight() );
    preserveProperty( grid, PROP_FOOTER_VISIBLE, grid.getFooterVisible() );
    preserveProperty( grid, PROP_LINES_VISIBLE, grid.getLinesVisible() );
    preserveProperty( grid, PROP_TOP_ITEM_INDEX, getTopItemIndex( grid ) );
    preserveProperty( grid, PROP_FOCUS_ITEM, grid.getFocusItem() );
    preserveProperty( grid, PROP_SCROLL_LEFT, getScrollLeft( grid ) );
    preserveProperty( grid, PROP_SELECTION, getSelection( grid ) );
    preserveProperty( grid, PROP_SORT_DIRECTION, getSortDirection( grid ) );
    preserveProperty( grid, PROP_SORT_COLUMN, getSortColumn( grid ) );
    preserveProperty( grid, PROP_SCROLLBARS_VISIBLE, getScrollBarsVisible( grid ) );
    preserveListener( grid,
                      PROP_SCROLLBARS_SELECTION_LISTENER,
                      hasScrollBarsSelectionListener( grid ) );
    preserveListener( grid, PROP_SELECTION_LISTENER, SelectionEvent.hasListener( grid ) );
    preserveProperty( grid, PROP_ENABLE_CELL_TOOLTIP, CellToolTipUtil.isEnabledFor( grid ) );
    preserveProperty( grid, PROP_CELL_TOOLTIP_TEXT, null );
  }

  @Override
  public void renderChanges( Widget widget ) throws IOException {
    Grid grid = ( Grid )widget;
    ControlLCAUtil.renderChanges( grid );
    WidgetLCAUtil.renderCustomVariant( grid );
    renderProperty( grid, PROP_ITEM_COUNT, grid.getRootItemCount(), ZERO );
    renderProperty( grid, PROP_ITEM_HEIGHT, grid.getItemHeight(), ZERO );
    renderItemMetrics( grid );
    renderProperty( grid, PROP_COLUMN_COUNT, grid.getColumnCount(), ZERO );
    renderProperty( grid, PROP_TREE_COLUMN, getTreeColumn( grid ), ZERO );
    renderProperty( grid, PROP_HEADER_HEIGHT, grid.getHeaderHeight(), ZERO );
    renderProperty( grid, PROP_HEADER_VISIBLE, grid.getHeaderVisible(), false );
    renderProperty( grid, PROP_FOOTER_HEIGHT, grid.getFooterHeight(), ZERO );
    renderProperty( grid, PROP_FOOTER_VISIBLE, grid.getFooterVisible(), false );
    renderProperty( grid, PROP_LINES_VISIBLE, grid.getLinesVisible(), false );
    renderProperty( grid, PROP_TOP_ITEM_INDEX, getTopItemIndex( grid ), ZERO );
    renderProperty( grid, PROP_FOCUS_ITEM, grid.getFocusItem(), null );
    renderProperty( grid, PROP_SCROLL_LEFT, getScrollLeft( grid ), ZERO );
    renderProperty( grid, PROP_SELECTION, getSelection( grid ), DEFAULT_SELECTION );
    renderProperty( grid, PROP_SORT_DIRECTION, getSortDirection( grid ), DEFAULT_SORT_DIRECTION );
    renderProperty( grid, PROP_SORT_COLUMN, getSortColumn( grid ), null );
    renderProperty( grid,
                    PROP_SCROLLBARS_VISIBLE,
                    getScrollBarsVisible( grid ),
                    DEFAULT_SCROLLBARS_VISIBLE );
    renderListener( grid,
                    PROP_SCROLLBARS_SELECTION_LISTENER,
                    hasScrollBarsSelectionListener( grid ),
                    false );
    renderListener( grid, PROP_SELECTION_LISTENER, SelectionEvent.hasListener( grid ), false );
    renderProperty( grid, PROP_ENABLE_CELL_TOOLTIP, CellToolTipUtil.isEnabledFor( grid ), false );
    renderProperty( grid, PROP_CELL_TOOLTIP_TEXT, getCellToolTipText( grid ), null );
  }

  @Override
  public void renderDispose( Widget widget ) throws IOException {
    ClientObjectFactory.getClientObject( widget ).destroy();
  }

  ////////////////////////////////////////////
  // Helping methods to read client-side state

  private static void readSelection( Grid grid ) {
    String value = WidgetLCAUtil.readPropertyValue( grid, "selection" );
    if( value != null ) {
      String[] values = value.split( "," );
      GridItem[] selectedItems = new GridItem[ values.length ];
      boolean validItemFound = false;
      for( int i = 0; i < values.length; i++ ) {
        selectedItems[ i ] = getItem( grid, values[ i ] );
        if( selectedItems[ i ] != null ) {
          validItemFound = true;
        }
      }
      if( !validItemFound ) {
        selectedItems = new GridItem[ 0 ];
      }
      grid.setSelection( selectedItems );
    }
  }

  private static void readScrollLeft( Grid grid ) {
    String left = WidgetLCAUtil.readPropertyValue( grid, "scrollLeft" );
    if( left != null ) {
      int leftOffset = NumberFormatUtil.parseInt( left );
      processScrollBarSelection( grid.getHorizontalBar(), leftOffset );
    }
  }

  private static void readTopItemIndex( Grid grid ) {
    String topItemIndex = WidgetLCAUtil.readPropertyValue( grid, "topItemIndex" );
    if( topItemIndex != null ) {
      int topOffset = NumberFormatUtil.parseInt( topItemIndex );
      getGridAdapter( grid ).invalidateTopIndex();
      processScrollBarSelection( grid.getVerticalBar(), topOffset );
    }
  }

  ////////////////
  // Cell tooltips

  private static void readCellToolTipTextRequested( Grid grid ) {
    ICellToolTipAdapter adapter = CellToolTipUtil.getAdapter( grid );
    adapter.setCellToolTipText( null );
    String event = JSConst.EVENT_CELL_TOOLTIP_REQUESTED;
    if( WidgetLCAUtil.wasEventSent( grid, event ) ) {
      ICellToolTipProvider provider = adapter.getCellToolTipProvider();
      if( provider != null ) {
        HttpServletRequest request = ContextProvider.getRequest();
        String cell = request.getParameter( JSConst.EVENT_CELL_TOOLTIP_DETAILS );
        String[] details = cell.split( "," );
        String itemId = details[ 0 ];
        int columnIndex = NumberFormatUtil.parseInt( details[ 1 ] );
        GridItem item = getItem( grid, itemId );
        if( item != null && ( columnIndex >= 0 && columnIndex < grid.getColumnCount() ) ) {
          provider.getToolTipText( item, columnIndex );
        }
      }
    }
  }

  private static String getCellToolTipText( Grid grid ) {
    return CellToolTipUtil.getAdapter( grid ).getCellToolTipText();
  }

  //////////////////
  // Helping methods

  private static int getTreeColumn( Grid grid ) {
    int[] order = grid.getColumnOrder();
    return order.length > 0 ? order[ 0 ] : 0;
  }

  private static int getTopItemIndex( Grid grid ) {
    int result = 0;
    ScrollBar verticalBar = grid.getVerticalBar();
    if( verticalBar != null ) {
      result = verticalBar.getSelection();
    }
    return result;
  }

  private static int getScrollLeft( Grid grid ) {
    int result = 0;
    ScrollBar horizontalBar = grid.getHorizontalBar();
    if( horizontalBar != null ) {
      result = horizontalBar.getSelection();
    }
    return result;
  }

  private static String[] getSelection( Grid grid ) {
    GridItem[] selection = grid.getSelection();
    String[] result = new String[ selection.length ];
    for( int i = 0; i < result.length; i++ ) {
      result[ i ] = WidgetUtil.getId( selection[ i ] );
    }
    return result;
  }

  private static String getSortDirection( Grid grid ) {
    String result = "none";
    for( int i = 0; i < grid.getColumnCount() && result.equals( "none" ); i++ ) {
      int sort = grid.getColumn( i ).getSort();
      if( sort == SWT.UP ) {
        result = "up";
      } else if( sort == SWT.DOWN ) {
        result = "down";
      }
    }
    return result;
  }

  private static GridColumn getSortColumn( Grid grid ) {
    GridColumn result = null;
    for( int i = 0; i < grid.getColumnCount() && result == null; i++ ) {
      GridColumn column = grid.getColumn( i );
      if( column.getSort() != SWT.NONE ) {
        result = column;
      }
    }
    return result;
  }

  private static boolean[] getScrollBarsVisible( Grid grid ) {
    boolean horizontalBarVisible = false;
    ScrollBar horizontalBar = grid.getHorizontalBar();
    if( horizontalBar != null ) {
      horizontalBarVisible = horizontalBar.getVisible();
    }
    boolean verticalBarVisible = false;
    ScrollBar verticalBar = grid.getVerticalBar();
    if( verticalBar != null ) {
      verticalBarVisible = verticalBar.getVisible();
    }
    return new boolean[] { horizontalBarVisible, verticalBarVisible };
  }

  private static boolean hasScrollBarsSelectionListener( Grid grid ) {
    boolean result = false;
    ScrollBar horizontalBar = grid.getHorizontalBar();
    if( horizontalBar != null ) {
      result = result || SelectionEvent.hasListener( horizontalBar );
    }
    ScrollBar verticalBar = grid.getVerticalBar();
    if( verticalBar != null ) {
      result = result || SelectionEvent.hasListener( verticalBar );
    }
    return result;
  }

  private static GridItem getItem( Grid grid, String itemId ) {
    return ( GridItem )WidgetUtil.find( grid, itemId );
  }

  private static void processScrollBarSelection( ScrollBar scrollBar, int selection ) {
    if( scrollBar != null ) {
      scrollBar.setSelection( selection );
      if( SelectionEvent.hasListener( scrollBar ) ) {
        int eventId = SelectionEvent.WIDGET_SELECTED;
        SelectionEvent evt = new SelectionEvent( scrollBar, null, eventId );
        evt.stateMask = EventLCAUtil.readStateMask( JSConst.EVENT_WIDGET_SELECTED_MODIFIER );
        evt.processEvent();
      }
    }
  }

  private static void processSelectionEvent( Grid grid, String eventName ) {
    if( WidgetLCAUtil.wasEventSent( grid, eventName ) ) {
      HttpServletRequest request = ContextProvider.getRequest();
      GridItem item = getItem( grid, request.getParameter( eventName + ".item" ) );
      if( item != null ) {
        if( eventName.equals( JSConst.EVENT_WIDGET_SELECTED ) ) {
          String detail = request.getParameter( eventName + ".detail" );
          if( "check".equals( detail ) ) {
            String index = request.getParameter( eventName + ".index" );
            if( index == null ) {
              index = "0";
            }
            item.fireCheckEvent( Integer.valueOf( index ).intValue()  );
          } else {
            item.fireEvent( SWT.Selection );
          }
        } else {
          item.fireEvent( SWT.DefaultSelection );
        }
      }
    }
  }

  ///////////////
  // Item Metrics

  private static void renderItemMetrics( Grid grid ) {
    ItemMetrics[] itemMetrics = getItemMetrics( grid );
    if( WidgetLCAUtil.hasChanged( grid, PROP_ITEM_METRICS, itemMetrics ) ) {
      int[][] metrics = new int[ itemMetrics.length ][ 7 ];
      for( int i = 0; i < itemMetrics.length; i++ ) {
        metrics[ i ] = new int[] {
          i,
          itemMetrics[ i ].left,
          itemMetrics[ i ].width,
          itemMetrics[ i ].imageLeft,
          itemMetrics[ i ].imageWidth,
          itemMetrics[ i ].textLeft,
          itemMetrics[ i ].textWidth
        };
      }
      IClientObject clientObject = ClientObjectFactory.getClientObject( grid );
      clientObject.set( PROP_ITEM_METRICS, metrics );
    }
  }

  static ItemMetrics[] getItemMetrics( Grid grid ) {
    int columnCount = grid.getColumnCount();
    ItemMetrics[] result = new ItemMetrics[ columnCount ];
    IGridAdapter adapter = getGridAdapter( grid );
    for( int i = 0; i < columnCount; i++ ) {
      result[ i ] = new ItemMetrics();
      result[ i ].left = adapter.getCellLeft( i );
      result[ i ].width = adapter.getCellWidth( i );
      result[ i ].imageLeft = result[ i ].left + adapter.getImageOffset( i );
      result[ i ].imageWidth = adapter.getImageWidth( i );
      result[ i ].textLeft = result[ i ].left + adapter.getTextOffset( i );
      result[ i ].textWidth = adapter.getTextWidth( i );
    }
    return result;
  }

  private static IGridAdapter getGridAdapter( Grid grid ) {
    return grid.getAdapter( IGridAdapter.class );
  }

  ////////////////
  // Inner classes

  static final class ItemMetrics {
    int left;
    int width;
    int imageLeft;
    int imageWidth;
    int textLeft;
    int textWidth;

    @Override
    public boolean equals( Object obj ) {
      boolean result;
      if( obj == this ) {
        result = true;
      } else  if( obj instanceof ItemMetrics ) {
        ItemMetrics other = ( ItemMetrics )obj;
        result =  other.left == left
               && other.width == width
               && other.imageLeft == imageLeft
               && other.imageWidth == imageWidth
               && other.textLeft == textLeft
               && other.textWidth == textWidth;
      } else {
        result = false;
      }
      return result;
    }

    @Override
    public int hashCode() {
      String msg = "ItemMetrics#hashCode() not implemented";
      throw new UnsupportedOperationException( msg );
    }
  }

}
