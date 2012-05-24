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
package org.eclipse.nebula.widgets.grid;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.eclipse.nebula.widgets.grid.internal.IScrollBarProxy;
import org.eclipse.nebula.widgets.grid.internal.NullScrollBarProxy;
import org.eclipse.nebula.widgets.grid.internal.ScrollBarProxyAdapter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TreeEvent;
import org.eclipse.swt.events.TreeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;


/**
 * <p>
 * NOTE:  THIS WIDGET AND ITS API ARE STILL UNDER DEVELOPMENT.  THIS IS A PRE-RELEASE ALPHA
 * VERSION.  USERS SHOULD EXPECT API CHANGES IN FUTURE VERSIONS.
 * </p>
 * Instances of this class implement a selectable user interface object that
 * displays a list of images and strings and issue notification when selected.
 * <p>
 * The item children that may be added to instances of this class must be of
 * type {@code GridItem}.
 * </p>
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>SWT.SINGLE, SWT.MULTI, SWT.NO_FOCUS, SWT.CHECK, SWT.VIRTUAL</dd>
 * <dt><b>Events:</b></dt>
 * <dd>Selection, DefaultSelection</dd>
 * </dl>
 */
public class Grid extends Canvas {

  /**
   * Vertical scrollbar proxy.
   * <p>
   * Note:
   * <ul>
   * <li>{@link Grid#getTopIndex()} is the only method allowed to call vScroll.getSelection()
   * (except #updateScrollbars() of course)</li>
   * <li>{@link Grid#setTopIndex(int)} is the only method allowed to call vScroll.setSelection(int)</li>
   * </ul>
   */
  private IScrollBarProxy vScroll;

  /**
   * Horizontal scrollbar proxy.
   */
  private IScrollBarProxy hScroll;

  /**
   * Tracks whether the scroll values are correct. If not they will be
   * recomputed in onPaint. This allows us to get a free ride on top of the
   * OS's paint event merging to assure that we don't perform this expensive
   * operation when unnecessary.
   */
  private boolean scrollValuesObsolete = false;

  /**
   * All items in the table, not just root items.
   */
  private List<GridItem> items = new ArrayList<GridItem>();

  /**
   * All root items.
   */
  private List<GridItem> rootItems = new ArrayList<GridItem>();

  /**
   * List of selected items.
   */
  private List<GridItem> selectedItems = new ArrayList<GridItem>();

  /**
   * Reference to the item in focus.
   */
  private GridItem focusItem;

  /**
   * List of selected cells.
   */
  private List<Point> selectedCells = new ArrayList<Point>();

  /**
   * List of table columns in creation/index order.
   */
  private List<GridColumn> columns = new ArrayList<GridColumn>();

  /**
   * List of the table columns in the order they are displayed.
   */
  private List<GridColumn> displayOrderedColumns = new ArrayList<GridColumn>();

  /**
   * True if there is at least one tree node.  This is used by accessibility and various
   * places for optimization.
   */
  private boolean isTree = false;

  /**
   * True if the widget is being disposed.  When true, events are not fired.
   */
  private boolean disposing = false;

  /**
   * True if three is at least one cell spanning columns.  This is used in various places for
   * optimizatoin.
   */
  private boolean hasSpanning = false;

  /**
   * Are column headers visible?
   */
  private boolean columnHeadersVisible = false;

  /**
   * Grid line color.
   */
  private Color lineColor;

  /**
   * Are the grid lines visible?
   */
  private boolean linesVisible = true;

  /**
   * Are tree lines visible?
   */
  private boolean treeLinesVisible = true;

  /**
   * The number of GridItems whose visible = true. Maintained for
   * performance reasons (rather than iterating over all items).
   */
  private int currentVisibleItems = 0;

  /**
   * Type of selection behavior. Valid values are SWT.SINGLE and SWT.MULTI.
   */
  private int selectionType = SWT.SINGLE;

  /**
   * True if selection highlighting is enabled.
   */
  private boolean selectionEnabled = true;

  private boolean cellSelectionEnabled = false;

  /**
   * Constructs a new instance of this class given its parent and a style
   * value describing its behavior and appearance.
   * <p>
   *
   * @param parent a composite control which will be the parent of the new
   * instance (cannot be null)
   * @param style the style of control to construct
   * @throws IllegalArgumentException
   * <ul>
   * <li>ERROR_NULL_ARGUMENT - if the parent is null</li>
   * </ul>
   * @throws org.eclipse.swt.SWTException
   * <ul>
   * <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that
   * created the parent</li>
   * </ul>
   * @see SWT#SINGLE
   * @see SWT#MULTI
   */
  public Grid( Composite parent, int style ) {
    super( parent, checkStyle( style ) );
    if( ( style & SWT.MULTI ) != 0 ) {
      selectionType = SWT.MULTI;
    }
    if( getVerticalBar() != null ) {
      getVerticalBar().setVisible( false );
      vScroll = new ScrollBarProxyAdapter( getVerticalBar() );
    } else {
      vScroll = new NullScrollBarProxy();
    }
    if( getHorizontalBar() != null ) {
      getHorizontalBar().setVisible( false );
      hScroll = new ScrollBarProxyAdapter( getHorizontalBar() );
    } else {
      hScroll = new NullScrollBarProxy();
    }
    scrollValuesObsolete = true;
    initListeners();
  }

  @Override
  public void dispose() {
    super.dispose();
    disposing = true;
    for( Iterator iterator = items.iterator(); iterator.hasNext(); ) {
      GridItem item = ( GridItem )iterator.next();
      item.dispose();
    }
    for( Iterator iterator = columns.iterator(); iterator.hasNext(); ) {
      GridColumn column = ( GridColumn )iterator.next();
      column.dispose();
    }
  }

  /**
   * Adds the listener to the collection of listeners who will be notified
   * when the receiver's selection changes, by sending it one of the messages
   * defined in the {@code SelectionListener} interface.
   * <p>
   * Cell selection events may have <code>Event.detail = SWT.DRAG</code> when the
   * user is drag selecting multiple cells.  A follow up selection event will be generated
   * when the drag is complete.
   *
   * @param listener the listener which should be notified
   * @throws IllegalArgumentException
   * <ul>
   * <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
   * </ul>
   * @throws org.eclipse.swt.SWTException
   * <ul>
   * <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   * <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that
   * created the receiver</li>
   * </ul>
   */
  public void addSelectionListener( SelectionListener listener ) {
    checkWidget();
    SelectionEvent.addListener( this, listener );
  }

  /**
   * Removes the listener from the collection of listeners who will be
   * notified when the receiver's selection changes.
   *
   * @param listener the listener which should no longer be notified
   * @see SelectionListener
   * @see #addSelectionListener(SelectionListener)
   * @throws org.eclipse.swt.SWTException
   * <ul>
   * <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   * <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that
   * created the receiver</li>
   * </ul>
   */
  public void removeSelectionListener( SelectionListener listener ) {
    checkWidget();
    SelectionEvent.removeListener( this, listener );
  }

  /**
   * Adds the listener to the collection of listeners who will be notified
   * when the receiver's items changes, by sending it one of the messages
   * defined in the {@code TreeListener} interface.
   *
   * @param listener the listener which should be notified
   * @throws IllegalArgumentException
   * <ul>
   * <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
   * </ul>
   * @throws org.eclipse.swt.SWTException
   * <ul>
   * <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   * <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that
   * created the receiver</li>
   * </ul>
   * @see TreeListener
   * @see #removeTreeListener
   * @see org.eclipse.swt.events.TreeEvent
   */
  public void addTreeListener( TreeListener listener ) {
    checkWidget();
    TreeEvent.addListener( this, listener );
  }

  /**
   * Removes the listener from the collection of listeners who will be
   * notified when the receiver's items changes.
   *
   * @param listener the listener which should no longer be notified
   * @see TreeListener
   * @see #addTreeListener(TreeListener)
   * @throws org.eclipse.swt.SWTException
   * <ul>
   * <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   * <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that
   * created the receiver</li>
   * </ul>
   */
  public void removeTreeListener( TreeListener listener ) {
    checkWidget();
    TreeEvent.removeListener( this, listener );
  }

  /**
   * Returns the number of items contained in the receiver.
   *
   * @return the number of items
   * @throws org.eclipse.swt.SWTException
   * <ul>
   * <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   * <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that
   * created the receiver</li>
   * </ul>
   */
  public int getItemCount() {
    checkWidget();
    return items.size();
  }

  /**
   * Returns a (possibly empty) array of {@code GridItem}s which are the
   * items in the receiver.
   * <p>
   * Note: This is not the actual structure used by the receiver to maintain
   * its list of items, so modifying the array will not affect the receiver.
   * </p>
   *
   * @return the items in the receiver
   * @throws org.eclipse.swt.SWTException
   * <ul>
   * <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   * <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that
   * created the receiver</li>
   * </ul>
   */
  public GridItem[] getItems() {
    checkWidget();
    return items.toArray( new GridItem[ items.size() ] );
  }

  /**
   * Returns the item at the given, zero-relative index in the receiver.
   * Throws an exception if the index is out of range.
   *
   * @param index the index of the item to return
   * @return the item at the given index
   * @throws IllegalArgumentException
   * <ul>
   * <li>ERROR_INVALID_RANGE - if the index is not between 0 and the number of elements in the
   * list minus 1 (inclusive) </li>     *
   * </ul>
   * @throws org.eclipse.swt.SWTException
   * <ul>
   * <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   * <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that
   * created the receiver</li>
   * </ul>
   */
  public GridItem getItem( int index ) {
    checkWidget();
    if( index < 0 || index >= items.size() ) {
      SWT.error( SWT.ERROR_INVALID_RANGE );
    }
    return items.get( index );
  }

  /**
   * Searches the receiver's list starting at the first item (index 0) until
   * an item is found that is equal to the argument, and returns the index of
   * that item. If no item is found, returns -1.
   *
   * @param item the search item
   * @return the index of the item
   * @throws IllegalArgumentException
   * <ul>
   * <li>ERROR_NULL_ARGUMENT - if the item is null</li>
   * </ul>
   * @throws org.eclipse.swt.SWTException
   * <ul>
   * <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   * <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that
   * created the receiver</li>
   * </ul>
   */
  public int indexOf( GridItem item ) {
    checkWidget();
    if( item == null ) {
      SWT.error( SWT.ERROR_NULL_ARGUMENT );
    }
    int result = -1;
    if( item.getParent() == this ) {
      result = items.indexOf( item );
    }
    return result;
  }

  /**
   * Returns the number of root items contained in the receiver.
   *
   * @return the number of items
   * @throws org.eclipse.swt.SWTException
   * <ul>
   * <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   * <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that
   * created the receiver</li>
   * </ul>
   */
  public int getRootItemCount() {
    checkWidget();
    return rootItems.size();
  }

  /**
   * Returns a (possibly empty) array of {@code GridItem}s which are
   * the root items in the receiver.
   * <p>
   * Note: This is not the actual structure used by the receiver to maintain
   * its list of items, so modifying the array will not affect the receiver.
   * </p>
   *
   * @return the root items in the receiver
   * @throws org.eclipse.swt.SWTException
   * <ul>
   * <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   * <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that
   * created the receiver</li>
   * </ul>
   */
  public GridItem[] getRootItems() {
    checkWidget();
    return rootItems.toArray( new GridItem[ rootItems.size() ] );
  }

  /**
   * TODO: JavaDoc
   * @param index
   * @return the root item
   */
  public GridItem getRootItem( int index ) {
    checkWidget();
    if( index < 0 || index >= rootItems.size() ) {
      SWT.error( SWT.ERROR_INVALID_RANGE );
    }
    return rootItems.get( index );
  }

  /**
   * Returns the number of columns contained in the receiver. If no
   * {@code GridColumn}s were created by the programmer, this value is
   * zero, despite the fact that visually, one column of items may be visible.
   * This occurs when the programmer uses the table like a list, adding items
   * but never creating a column.
   *
   * @return the number of columns
   * @throws org.eclipse.swt.SWTException
   * <ul>
   * <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   * <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that
   * created the receiver</li>
   * </ul>
   */
  public int getColumnCount() {
    checkWidget();
    return columns.size();
  }

  /**
   * Returns an array of {@code GridColumn}s which are the columns in the
   * receiver. If no {@code GridColumn}s were created by the programmer,
   * the array is empty, despite the fact that visually, one column of items
   * may be visible. This occurs when the programmer uses the table like a
   * list, adding items but never creating a column.
   * <p>
   * Note: This is not the actual structure used by the receiver to maintain
   * its list of items, so modifying the array will not affect the receiver.
   * </p>
   *
   * @return the items in the receiver
   * @throws org.eclipse.swt.SWTException
   * <ul>
   * <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   * <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that
   * created the receiver</li>
   * </ul>
   */
  public GridColumn[] getColumns() {
    checkWidget();
    return columns.toArray( new GridColumn[ columns.size() ] );
  }

  /**
   * Returns the column at the given, zero-relative index in the receiver.
   * Throws an exception if the index is out of range. If no
   * {@code GridColumn}s were created by the programmer, this method will
   * throw {@code ERROR_INVALID_RANGE} despite the fact that a single column
   * of data may be visible in the table. This occurs when the programmer uses
   * the table like a list, adding items but never creating a column.
   *
   * @param index the index of the column to return
   * @return the column at the given index
   * @throws IllegalArgumentException
   * <ul>
   * <li>ERROR_INVALID_RANGE - if the index is not between 0 and the number
   * of elements in the list minus 1 (inclusive)</li>
   * </ul>
   * @throws org.eclipse.swt.SWTException
   * <ul>
   * <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   * <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that
   * created the receiver</li>
   * </ul>
   */
  public GridColumn getColumn( int index ) {
    checkWidget();
    if( index < 0 || index > getColumnCount() - 1 ) {
      SWT.error( SWT.ERROR_INVALID_RANGE );
    }
    return columns.get( index );
  }

  /**
   * Searches the receiver's list starting at the first column (index 0) until
   * a column is found that is equal to the argument, and returns the index of
   * that column. If no column is found, returns -1.
   *
   * @param column the search column
   * @return the index of the column
   * @throws IllegalArgumentException
   * <ul>
   * <li>ERROR_NULL_ARGUMENT - if the column is null</li>
   * </ul>
   * @throws org.eclipse.swt.SWTException
   * <ul>
   * <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   * <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that
   * created the receiver</li>
   * </ul>
   */
  public int indexOf( GridColumn column ) {
    checkWidget();
    if( column == null ) {
      SWT.error( SWT.ERROR_NULL_ARGUMENT );
    }
    int result = -1;
    if( column.getParent() == this ) {
      result = columns.indexOf( column );
    }
    return result;
  }

  /**
   * Sets the order that the items in the receiver should be displayed in to
   * the given argument which is described in terms of the zero-relative
   * ordering of when the items were added.
   *
   * @param order the new order to display the items
   * @throws org.eclipse.swt.SWTException
   * <ul>
   * <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   * <li>ERROR_THREAD_INVALID_ACCESS -if not called from the thread that
   * created the receiver</li>
   * </ul>
   * @throws IllegalArgumentException
   * <ul>
   * <li>ERROR_NULL_ARGUMENT - if the item order is null</li>
   * <li>ERROR_INVALID_ARGUMENT - if the order is not the same length as the
   * number of items, or if an item is listed twice, or if the order splits a
   * column group</li>
   * </ul>
   */
  public void setColumnOrder( int[] order ) {
    checkWidget();
    if( order == null ) {
      SWT.error( SWT.ERROR_NULL_ARGUMENT );
    }
    if( order.length != displayOrderedColumns.size() ) {
      SWT.error( SWT.ERROR_INVALID_ARGUMENT );
    }
    boolean[] seen = new boolean[ displayOrderedColumns.size() ];
    for( int i = 0; i < order.length; i++ ) {
      if( order[ i ] < 0 || order[ i ] >= displayOrderedColumns.size() ) {
        SWT.error( SWT.ERROR_INVALID_ARGUMENT );
      }
      if( seen[ order[ i ] ] ) {
        SWT.error( SWT.ERROR_INVALID_ARGUMENT );
      }
      seen[ order[ i ] ] = true;
    }
// TODO: [if] Implement ColumnGroup
//    if( columnGroups.length != 0 ) {
//      GridColumnGroup currentGroup = null;
//      int columnsInGroup = 0;
//      for( int i = 0; i < order.length; i++ ) {
//        GridColumn column = getColumn( order[ i ] );
//        if( currentGroup != null ) {
//          if( column.getColumnGroup() != currentGroup && columnsInGroup > 0 ) {
//            SWT.error( SWT.ERROR_INVALID_ARGUMENT );
//          } else {
//            columnsInGroup--;
//            if( columnsInGroup <= 0 ) {
//              currentGroup = null;
//            }
//          }
//        } else if( column.getColumnGroup() != null ) {
//          currentGroup = column.getColumnGroup();
//          columnsInGroup = currentGroup.getColumns().length - 1;
//        }
//      }
//    }
    GridColumn[] columns = getColumns();
    displayOrderedColumns.clear();
    for( int i = 0; i < order.length; i++ ) {
      displayOrderedColumns.add( columns[ order[ i ] ] );
    }
  }

  /**
   * Returns an array of zero-relative integers that map the creation order of
   * the receiver's items to the order in which they are currently being
   * displayed.
   * <p>
   * Specifically, the indices of the returned array represent the current
   * visual order of the items, and the contents of the array represent the
   * creation order of the items.
   * </p>
   * <p>
   * Note: This is not the actual structure used by the receiver to maintain
   * its list of items, so modifying the array will not affect the receiver.
   * </p>
   *
   * @return the current visual order of the receiver's items
   * @throws org.eclipse.swt.SWTException
   * <ul>
   * <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   * <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that
   * created the receiver</li>
   * </ul>
   */
  public int[] getColumnOrder() {
    checkWidget();
    int[] result = new int[ columns.size() ];
    for( int i = 0; i < result.length; i++ ) {
      GridColumn column = displayOrderedColumns.get( i );
      result[ i ] = columns.indexOf( column );
    }
    return result;
  }

  /**
   * Clears the item at the given zero-relative index in the receiver.
   * The text, icon and other attributes of the item are set to the default
   * value.  If the table was created with the <code>SWT.VIRTUAL</code> style,
   * these attributes are requested again as needed.
   *
   * @param index the index of the item to clear
   * @param allChildren <code>true</code> if all child items of the indexed item should be
   * cleared recursively, and <code>false</code> otherwise
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_INVALID_RANGE - if the index is not between 0 and the number of elements in the list minus 1 (inclusive)</li>
   * </ul>
   * @exception org.eclipse.swt.SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   *
   * @see SWT#VIRTUAL
   * @see SWT#SetData
   */
  public void clear( int index, boolean allChildren ) {
    checkWidget();
    if( index < 0 || index >= items.size() ) {
      SWT.error( SWT.ERROR_INVALID_RANGE );
    }
    items.get( index ).clear( allChildren );
    redraw();
  }

  /**
   * Clears the items in the receiver which are between the given
   * zero-relative start and end indices (inclusive).  The text, icon
   * and other attributes of the items are set to their default values.
   * If the table was created with the <code>SWT.VIRTUAL</code> style,
   * these attributes are requested again as needed.
   *
   * @param start the start index of the item to clear
   * @param end the end index of the item to clear
   * @param allChildren <code>true</code> if all child items of the range of items should be
   * cleared recursively, and <code>false</code> otherwise
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_INVALID_RANGE - if either the start or end are not between 0 and the number of elements in the list minus 1 (inclusive)</li>
   * </ul>
   * @exception org.eclipse.swt.SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   *
   * @see SWT#VIRTUAL
   * @see SWT#SetData
   */
  public void clear( int start, int end, boolean allChildren ) {
    checkWidget();
    if( start <= end ) {
      if( !( 0 <= start && start <= end && end < items.size() ) ) {
        SWT.error( SWT.ERROR_INVALID_RANGE );
      }
      for( int i = start; i <= end; i++ ) {
        items.get( i ).clear( allChildren );
      }
      redraw();
    }
  }

  /**
   * Clears the items at the given zero-relative indices in the receiver.
   * The text, icon and other attributes of the items are set to their default
   * values.  If the table was created with the <code>SWT.VIRTUAL</code> style,
   * these attributes are requested again as needed.
   *
   * @param indices the array of indices of the items
   * @param allChildren <code>true</code> if all child items of the indexed items should be
   * cleared recursively, and <code>false</code> otherwise
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_INVALID_RANGE - if the index is not between 0 and the number of elements in the list minus 1 (inclusive)</li>
   *    <li>ERROR_NULL_ARGUMENT - if the indices array is null</li>
   * </ul>
   * @exception org.eclipse.swt.SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   *
   * @see SWT#VIRTUAL
   * @see SWT#SetData
   */
  public void clear( int[] indices, boolean allChildren ) {
    checkWidget();
    if( indices == null ) {
      SWT.error( SWT.ERROR_NULL_ARGUMENT );
    }
    if( indices.length > 0 ) {
      for( int i = 0; i < indices.length; i++ ) {
        if( !( 0 <= indices[ i ] && indices[ i ] < items.size() ) ) {
          SWT.error( SWT.ERROR_INVALID_RANGE );
        }
      }
      for( int i = 0; i < indices.length; i++ ) {
        items.get( indices[ i ] ).clear( allChildren );
      }
      redraw();
    }
  }

  /**
   * Clears all the items in the receiver. The text, icon and other
   * attributes of the items are set to their default values. If the
   * table was created with the <code>SWT.VIRTUAL</code> style, these
   * attributes are requested again as needed.
   *
   * @param allChildren <code>true</code> if all child items of each item should be
   * cleared recursively, and <code>false</code> otherwise
   *
   * @exception org.eclipse.swt.SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   *
   * @see SWT#VIRTUAL
   * @see SWT#SetData
   */
  public void clearAll( boolean allChildren ) {
    checkWidget();
    int itemsCount = items.size();
    if( itemsCount > 0 ) {
      // [if] Note: The parameter allChildren has no effect as all items (not only rootItems)
      // are cleared
      clear( 0, itemsCount - 1, allChildren );
    }
  }

  /**
   * Enables selection highlighting if the argument is <code>true</code>.
   *
   * @param selectionEnabled the selection enabled state
   *
   * @throws org.eclipse.swt.SWTException
   * <ul>
   * <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   * <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that
   * created the receiver</li>
   * </ul>
   */
  public void setSelectionEnabled( boolean selectionEnabled ) {
    checkWidget();
    if( !selectionEnabled ) {
      selectedItems.clear();
      redraw();
    }
    this.selectionEnabled = selectionEnabled;
  }

  /**
   * Returns <code>true</code> if selection is enabled, false otherwise.
   *
   * @return the selection enabled state
   *
   * @throws org.eclipse.swt.SWTException
   * <ul>
   * <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   * <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that
   * created the receiver</li>
   * </ul>
   */
  public boolean getSelectionEnabled() {
    checkWidget();
    return selectionEnabled;
  }

  /**
   * Returns true if the cells are selectable in the reciever.
   *
   * @return cell selection enablement status.
   * @throws org.eclipse.swt.SWTException
   * <ul>
   * <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   * <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that
   * created the receiver</li>
   * </ul>
   */
  public boolean getCellSelectionEnabled() {
    checkWidget();
    return cellSelectionEnabled;
  }

  /**
   * Selects the item at the given zero-relative index in the receiver. If the
   * item at the index was already selected, it remains selected. Indices that
   * are out of range are ignored.
   * <p>
   * If cell selection is enabled, selects all cells at the given index.
   *
   * @param index the index of the item to select
   * @throws org.eclipse.swt.SWTException
   * <ul>
   * <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   * <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that
   * created the receiver</li>
   * </ul>
   */
  public void select( int index ) {
    checkWidget();
    if( selectionEnabled && index >= 0 && index < items.size() ) {
      if( !cellSelectionEnabled && selectionType == SWT.SINGLE ) {
        selectedItems.clear();
      }
      internalSelect( index );
      redraw();
    }
  }

  /**
   * Selects the items in the range specified by the given zero-relative
   * indices in the receiver. The range of indices is inclusive. The current
   * selection is not cleared before the new items are selected.
   * <p>
   * If an item in the given range is not selected, it is selected. If an item
   * in the given range was already selected, it remains selected. Indices
   * that are out of range are ignored and no items will be selected if start
   * is greater than end. If the receiver is single-select and there is more
   * than one item in the given range, then all indices are ignored.
   * <p>
   * If cell selection is enabled, all cells within the given range are selected.
   *
   * @param start the start of the range
   * @param end the end of the range
   * @throws org.eclipse.swt.SWTException
   * <ul>
   * <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   * <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that
   * created the receiver</li>
   * </ul>
   * @see Grid#setSelection(int,int)
   */
  public void select( int start, int end ) {
    checkWidget();
    if( selectionEnabled && !( selectionType == SWT.SINGLE && start != end ) ) {
      if( !cellSelectionEnabled && selectionType == SWT.SINGLE ) {
        selectedItems.clear();
      }
      for( int index = Math.max( 0, start ); index <= Math.min( items.size() - 1, end ); index++ ) {
        internalSelect( index );
      }
      redraw();
    }
  }

  /**
   * Selects the items at the given zero-relative indices in the receiver. The
   * current selection is not cleared before the new items are selected.
   * <p>
   * If the item at a given index is not selected, it is selected. If the item
   * at a given index was already selected, it remains selected. Indices that
   * are out of range and duplicate indices are ignored. If the receiver is
   * single-select and multiple indices are specified, then all indices are
   * ignored.
   * <p>
   * If cell selection is enabled, all cells within the given indices are
   * selected.
   *
   * @param indices the array of indices for the items to select
   * @throws IllegalArgumentException
   * <ul>
   * <li>ERROR_NULL_ARGUMENT - if the array of indices is null</li>
   * </ul>
   * @throws org.eclipse.swt.SWTException
   * <ul>
   * <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   * <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that
   * created the receiver</li>
   * </ul>
   * @see Grid#setSelection(int[])
   */
  public void select( int[] indices ) {
    checkWidget();
    if( indices == null ) {
      SWT.error( SWT.ERROR_NULL_ARGUMENT );
    }
    if( selectionEnabled && !( selectionType == SWT.SINGLE && indices.length > 1 ) ) {
      if( !cellSelectionEnabled && selectionType == SWT.SINGLE ) {
        selectedItems.clear();
      }
      for( int i = 0; i < indices.length; i++ ) {
        internalSelect( indices[ i ] );
      }
      redraw();
    }
  }

  /**
   * Selects all of the items in the receiver.
   * <p>
   * If the receiver is single-select, do nothing.  If cell selection is enabled,
   * all cells are selected.
   *
   * @throws org.eclipse.swt.SWTException
   * <ul>
   * <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   * <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that
   * created the receiver</li>
   * </ul>
   */
  public void selectAll() {
    checkWidget();
    if( selectionEnabled && selectionType != SWT.SINGLE ) {
      if( cellSelectionEnabled ) {
// TODO: [if] Implement cell selection
//        selectAllCells();
      } else {
        selectedItems.clear();
        selectedItems.addAll( items );
        redraw();
      }
    }
  }

  /**
   * Deselects the item at the given zero-relative index in the receiver. If
   * the item at the index was already deselected, it remains deselected.
   * Indices that are out of range are ignored.
   * <p>
   * If cell selection is enabled, all cells in the specified item are deselected.
   *
   * @param index the index of the item to deselect
   * @throws org.eclipse.swt.SWTException
   * <ul>
   * <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   * <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that
   * created the receiver</li>
   * </ul>
   */
  public void deselect( int index ) {
    checkWidget();
    if( index >= 0 && index < items.size() ) {
      internalDeselect( index );
      redraw();
    }
  }

  /**
   * Deselects the items at the given zero-relative indices in the receiver.
   * If the item at the given zero-relative index in the receiver is selected,
   * it is deselected. If the item at the index was not selected, it remains
   * deselected. The range of the indices is inclusive. Indices that are out
   * of range are ignored.
   * <p>
   * If cell selection is enabled, all cells in the given range are deselected.
   *
   * @param start the start index of the items to deselect
   * @param end the end index of the items to deselect
   * @throws org.eclipse.swt.SWTException
   * <ul>
   * <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   * <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that
   * created the receiver</li>
   * </ul>
   */
  public void deselect( int start, int end ) {
    checkWidget();
    for( int index = Math.max( 0, start ); index <= Math.min( items.size() - 1, end ); index++ ) {
      internalDeselect( index );
    }
    redraw();
  }

  /**
   * Deselects the items at the given zero-relative indices in the receiver.
   * If the item at the given zero-relative index in the receiver is selected,
   * it is deselected. If the item at the index was not selected, it remains
   * deselected. Indices that are out of range and duplicate indices are
   * ignored.
   * <p>
   * If cell selection is enabled, all cells in the given items are deselected.
   *
   * @param indices the array of indices for the items to deselect
   * @throws IllegalArgumentException
   * <ul>
   * <li>ERROR_NULL_ARGUMENT - if the set of indices is null</li>
   * </ul>
   * @throws org.eclipse.swt.SWTException
   * <ul>
   * <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   * <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that
   * created the receiver</li>
   * </ul>
   */
  public void deselect( int[] indices ) {
    checkWidget();
    if( indices == null ) {
      SWT.error( SWT.ERROR_NULL_ARGUMENT );
    }
    for( int i = 0; i < indices.length; i++ ) {
      internalDeselect( indices[ i ] );
    }
    redraw();
  }

  /**
   * Deselects all selected items in the receiver.  If cell selection is enabled,
   * all cells are deselected.
   *
   * @throws org.eclipse.swt.SWTException
   * <ul>
   * <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   * <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that
   * created the receiver</li>
   * </ul>
   */
  public void deselectAll() {
    checkWidget();
    internalDeselectAll();
    redraw();
  }

  /**
   * Selects the item at the given zero-relative index in the receiver. The
   * current selection is first cleared, then the new item is selected.
   * <p>
   * If cell selection is enabled, all cells within the item at the given index
   * are selected.
   *
   * @param index the index of the item to select
   * @throws org.eclipse.swt.SWTException
   * <ul>
   * <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   * <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that
   * created the receiver</li>
   * </ul>
   */
  public void setSelection( int index ) {
    checkWidget();
    if( selectionEnabled && index >= 0 && index < items.size() ) {
      internalDeselectAll();
      internalSelect( index );
      redraw();
    }
  }

  /**
   * Selects the items in the range specified by the given zero-relative
   * indices in the receiver. The range of indices is inclusive. The current
   * selection is cleared before the new items are selected.
   * <p>
   * Indices that are out of range are ignored and no items will be selected
   * if start is greater than end. If the receiver is single-select and there
   * is more than one item in the given range, then all indices are ignored.
   * <p>
   * If cell selection is enabled, all cells within the given range are selected.
   *
   * @param start the start index of the items to select
   * @param end the end index of the items to select
   * @throws org.eclipse.swt.SWTException
   * <ul>
   * <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   * <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that
   * created the receiver</li>
   * </ul>
   * @see Grid#deselectAll()
   * @see Grid#select(int,int)
   */
  public void setSelection( int start, int end ) {
    checkWidget();
    if( selectionEnabled && !( selectionType == SWT.SINGLE && start != end ) ) {
      internalDeselectAll();
      for( int index = Math.max( 0, start ); index <= Math.min( items.size() - 1, end ); index++ ) {
        internalSelect( index );
      }
      redraw();
    }
  }

  /**
   * Selects the items at the given zero-relative indices in the receiver. The
   * current selection is cleared before the new items are selected.
   * <p>
   * Indices that are out of range and duplicate indices are ignored. If the
   * receiver is single-select and multiple indices are specified, then all
   * indices are ignored.
   * <p>
   * If cell selection is enabled, all cells within the given indices are selected.
   *
   * @param indices the indices of the items to select
   * @throws IllegalArgumentException
   * <ul>
   * <li>ERROR_NULL_ARGUMENT - if the array of indices is null</li>
   * </ul>
   * @throws org.eclipse.swt.SWTException
   * <ul>
   * <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   * <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that
   * created the receiver</li>
   * </ul>
   * @see Grid#deselectAll()
   * @see Grid#select(int[])
   */
  public void setSelection( int[] indices ) {
    checkWidget();
    if( indices == null ) {
      SWT.error( SWT.ERROR_NULL_ARGUMENT );
    }
    if( selectionEnabled && !( selectionType == SWT.SINGLE && indices.length > 1 ) ) {
      internalDeselectAll();
      for( int i = 0; i < indices.length; i++ ) {
        internalSelect( indices[ i ] );
      }
      redraw();
    }
  }

  /**
   * Sets the receiver's selection to be the given array of items. The current
   * selection is cleared before the new items are selected.
   * <p>
   * Items that are not in the receiver are ignored. If the receiver is
   * single-select and multiple items are specified, then all items are
   * ignored.  If cell selection is enabled, all cells within the given items
   * are selected.
   *
   * @param items the array of items
   * @throws IllegalArgumentException
   * <ul>
   * <li>ERROR_NULL_ARGUMENT - if the array of items is null</li>
   * <li>ERROR_INVALID_ARGUMENT - if one of the items has been disposed</li>
   * </ul>
   * @throws org.eclipse.swt.SWTException
   * <ul>
   * <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   * <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that
   * created the receiver</li>
   * </ul>
   * @see Grid#deselectAll()
   * @see Grid#select(int[])
   * @see Grid#setSelection(int[])
   */
  public void setSelection( GridItem[] items ) {
    checkWidget();
    if( items == null ) {
      SWT.error( SWT.ERROR_NULL_ARGUMENT );
    }
    if( selectionEnabled && !( selectionType == SWT.SINGLE && items.length > 1 ) ) {
      internalDeselectAll();
      for( int i = 0; i < items.length; i++ ) {
        GridItem item = items[ i ];
        if( item != null ) {
          if( item.isDisposed() ) {
            SWT.error( SWT.ERROR_INVALID_ARGUMENT );
          }
          internalSelect( indexOf( item ) );
        }
      }
      redraw();
    }
  }

  /**
   * Returns a array of {@code GridItem}s that are currently selected in the
   * receiver. The order of the items is unspecified. An empty array indicates
   * that no items are selected.
   * <p>
   * Note: This is not the actual structure used by the receiver to maintain
   * its selection, so modifying the array will not affect the receiver.
   * <p>
   * If cell selection is enabled, any items which contain at least one selected
   * cell are returned.
   *
   * @return an array representing the selection
   * @throws org.eclipse.swt.SWTException
   * <ul>
   * <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   * <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that
   * created the receiver</li>
   * </ul>
   */
  public GridItem[] getSelection() {
    checkWidget();
    GridItem[] result = new GridItem[ 0 ];
    if( cellSelectionEnabled ) {
// TODO: [if] Implement cell selection
//      List<GridItem> items = new ArrayList<GridItem>();
//      int itemCount = getItemCount();
//      for( Iterator iterator = selectedCells.iterator(); iterator.hasNext(); ) {
//        Point cell = ( Point )iterator.next();
//        if( cell.y >= 0 && cell.y < itemCount ) {
//          GridItem item = getItem( cell.y );
//          if( !items.contains( item ) ) {
//            items.add( item );
//          }
//        }
//      }
//      result = items.toArray( new GridItem[ 0 ] );
    } else {
      result = selectedItems.toArray( new GridItem[ selectedItems.size() ] );
    }
    return result;
  }

  /**
   * Returns the number of selected items contained in the receiver.  If cell selection
   * is enabled, the number of items with at least one selected cell are returned.
   *
   * @return the number of selected items
   * @throws org.eclipse.swt.SWTException
   * <ul>
   * <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   * <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that
   * created the receiver</li>
   * </ul>
   */
  public int getSelectionCount() {
    checkWidget();
    int result = 0;
    if( cellSelectionEnabled ) {
// TODO: [if] Implement cell selection
//      List<GridItem> items = new ArrayList<GridItem>();
//      for( Iterator iterator = selectedCells.iterator(); iterator.hasNext(); ) {
//        Point cell = ( Point )iterator.next();
//        GridItem item = getItem( cell.y );
//        if( !items.contains( item ) ) {
//          items.add( item );
//        }
//      }
//      result = items.size();
    } else {
      result = selectedItems.size();
    }
    return result;
  }

  /**
   * Returns the zero-relative index of the item which is currently selected
   * in the receiver, or -1 if no item is selected.  If cell selection is enabled,
   * returns the index of first item that contains at least one selected cell.
   *
   * @return the index of the selected item
   * @throws org.eclipse.swt.SWTException
   * <ul>
   * <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   * <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that
   * created the receiver</li>
   * </ul>
   */
  public int getSelectionIndex() {
    checkWidget();
    int result = -1;
    if( cellSelectionEnabled ) {
      if( selectedCells.size() != 0 ) {
        result = selectedCells.get( 0 ).y;
      }
    } else {
      if( selectedItems.size() != 0 ) {
        result = items.indexOf( selectedItems.get( 0 ) );
      }
    }
    return result;
  }

  /**
   * Returns the zero-relative indices of the items which are currently
   * selected in the receiver. The order of the indices is unspecified. The
   * array is empty if no items are selected.
   * <p>
   * Note: This is not the actual structure used by the receiver to maintain
   * its selection, so modifying the array will not affect the receiver.
   * <p>
   * If cell selection is enabled, returns the indices of any items which
   * contain at least one selected cell.
   *
   * @return the array of indices of the selected items
   * @throws org.eclipse.swt.SWTException
   * <ul>
   * <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   * <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that
   * created the receiver</li>
   * </ul>
   */
  public int[] getSelectionIndices() {
    checkWidget();
    int[] result = new int[ 0 ];
    if( cellSelectionEnabled ) {
      List<GridItem> selectedRows = new ArrayList<GridItem>();
      for( Iterator iterator = selectedCells.iterator(); iterator.hasNext(); ) {
        Point cell = ( Point )iterator.next();
        GridItem item = getItem( cell.y );
        if( !selectedRows.contains( item ) ) {
          selectedRows.add( item );
        }
      }
      result = new int[ selectedRows.size() ];
      for( int i = 0; i < result.length; i++ ) {
        GridItem item = selectedRows.get( i );
        result[ i ] = items.indexOf( item );
      }
    } else {
      result = new int[ selectedItems.size() ];
      for( int i = 0; i < result.length; i++ ) {
        GridItem item = selectedItems.get( i );
        result[ i ] = items.indexOf( item );
      }
    }
    return result;
  }

  /**
   * Returns {@code true} if the item is selected, and {@code false}
   * otherwise. Indices out of range are ignored.  If cell selection is
   * enabled, returns true if the item at the given index contains at
   * least one selected cell.
   *
   * @param index the index of the item
   * @return the visibility state of the item at the index
   * @throws org.eclipse.swt.SWTException
   * <ul>
   * <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   * <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that
   * created the receiver</li>
   * </ul>
   */
  public boolean isSelected( int index ) {
    checkWidget();
    boolean result = false;
    if( index >= 0 && index < items.size() ) {
      if( cellSelectionEnabled ) {
        for( Iterator iterator = selectedCells.iterator(); iterator.hasNext(); ) {
          Point cell = ( Point )iterator.next();
          if( cell.y == index ) {
            result = true;
          }
        }
      } else {
        result = isSelected( items.get( index ) );
      }
    }
    return result;
  }

  /**
   * Returns true if the given item is selected.  If cell selection is enabled,
   * returns true if the given item contains at least one selected cell.
   *
   * @param item item
   * @return true if the item is selected.
   * @throws IllegalArgumentException
   * <ul>
   * <li>ERROR_NULL_ARGUMENT - if the item is null</li>
   * </ul>
   * @throws org.eclipse.swt.SWTException
   * <ul>
   * <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   * <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that
   * created the receiver</li>
   * </ul>
   */
  public boolean isSelected( GridItem item ) {
    checkWidget();
    if( item == null ) {
      SWT.error( SWT.ERROR_NULL_ARGUMENT );
    }
    boolean result = false;
    if( cellSelectionEnabled ) {
      int index = indexOf( item );
      if( index != -1 ) {
        for( Iterator iterатор = selectedCells.iterator(); iterатор.hasNext(); ) {
          Point cell = ( Point )iterатор.next();
          if( cell.y == index ) {
            result = true;
          }
        }
      }
    } else {
      result = selectedItems.contains( item );
    }
    return result;
  }

  /**
   * Removes the item from the receiver at the given zero-relative index.
   *
   * @param index the index for the item
   * @throws IllegalArgumentException
   * <ul>
   * <li>ERROR_INVALID_RANGE - if the index is not between 0 and the number
   * of elements in the list minus 1 (inclusive)</li>
   * </ul>
   * @throws org.eclipse.swt.SWTException
   * <ul>
   * <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   * <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that
   * created the receiver</li>
   * </ul>
   */
  public void remove( int index ) {
    checkWidget();
    if( index < 0 || index > items.size() - 1 ) {
      SWT.error( SWT.ERROR_INVALID_RANGE );
    }
    items.get( index ).dispose();
    redraw();
  }

  /**
   * Removes the items from the receiver which are between the given
   * zero-relative start and end indices (inclusive).
   *
   * @param start the start of the range
   * @param end the end of the range
   * @throws IllegalArgumentException
   * <ul>
   * <li>ERROR_INVALID_RANGE - if either the start or end are not between 0
   * and the number of elements in the list minus 1 (inclusive)</li>
   * </ul>
   * @throws org.eclipse.swt.SWTException
   * <ul>
   * <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   * <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that
   * created the receiver</li>
   * </ul>
   */
  public void remove( int start, int end ) {
    checkWidget();
    for( int i = end; i >= start; i-- ) {
      if( i < 0 || i > items.size() - 1 ) {
        SWT.error( SWT.ERROR_INVALID_RANGE );
      }
      items.get( i ).dispose();
    }
    redraw();
  }

  /**
   * Removes the items from the receiver's list at the given zero-relative
   * indices.
   *
   * @param indices the array of indices of the items
   * @throws IllegalArgumentException
   * <ul>
   * <li>ERROR_INVALID_RANGE - if the index is not between 0 and the number
   * of elements in the list minus 1 (inclusive)</li>
   * <li>ERROR_NULL_ARGUMENT - if the indices array is null</li>
   * </ul>
   * @throws org.eclipse.swt.SWTException
   * <ul>
   * <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   * <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that
   * created the receiver</li>
   * </ul>
   */
  public void remove( int[] indices ) {
    checkWidget();
    if( indices == null ) {
      SWT.error( SWT.ERROR_NULL_ARGUMENT );
    }
    GridItem[] removeThese = new GridItem[ indices.length ];
    for( int i = 0; i < indices.length; i++ ) {
      int index = indices[ i ];
      if( index >= 0 && index < items.size() ) {
        removeThese[ i ] = items.get( index );
      } else {
        SWT.error( SWT.ERROR_INVALID_RANGE );
      }
    }
    for( int i = 0; i < removeThese.length; i++ ) {
      removeThese[ i ].dispose();
    }
    redraw();
  }

  /**
   * Removes all of the items from the receiver.
   *
   * @throws org.eclipse.swt.SWTException
   * <ul>
   * <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   * <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that
   * created the receiver</li>
   * </ul>
   */
  public void removeAll() {
    checkWidget();
    while( items.size() > 0 ) {
      items.get( 0 ).dispose();
    }
    redraw();
  }

  /**
   * Marks the receiver's header as visible if the argument is {@code true},
   * and marks it invisible otherwise.
   *
   * @param show the new visibility state
   * @throws org.eclipse.swt.SWTException
   * <ul>
   * <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   * <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that
   * created the receiver</li>
   * </ul>
   */
  public void setHeaderVisible( boolean show ) {
    checkWidget();
    columnHeadersVisible = show;
    redraw();
  }

  /**
   * Returns {@code true} if the receiver's header is visible, and
   * {@code false} otherwise.
   *
   * @return the receiver's header's visibility state
   * @throws org.eclipse.swt.SWTException
   * <ul>
   * <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   * <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that
   * created the receiver</li>
   * </ul>
   */
  public boolean getHeaderVisible() {
    checkWidget();
    return columnHeadersVisible;
  }

  /**
   * Sets the line color.
   *
   * @param lineColor The lineColor to set.
   * @throws org.eclipse.swt.SWTException
   * <ul>
   * <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   * <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that
   * created the receiver</li>
   * </ul>
   */
  public void setLineColor( Color lineColor ) {
    checkWidget();
    if( lineColor != null && lineColor.isDisposed() ) {
      SWT.error( SWT.ERROR_INVALID_ARGUMENT );
    }
    this.lineColor = lineColor;
  }

  /**
   * Returns the line color.
   *
   * @return Returns the lineColor.
   * @throws org.eclipse.swt.SWTException
   * <ul>
   * <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   * <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that
   * created the receiver</li>
   * </ul>
   */
  public Color getLineColor() {
    checkWidget();
    return lineColor;
  }

  /**
   * Sets the line visibility.
   *
   * @param linesVisible The linesVisible to set.
   * @throws org.eclipse.swt.SWTException
   * <ul>
   * <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   * <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that
   * created the receiver</li>
   * </ul>
   */
  public void setLinesVisible( boolean linesVisible ) {
    checkWidget();
    this.linesVisible = linesVisible;
    redraw();
  }

  /**
   * Returns true if the lines are visible.
   *
   * @return Returns the linesVisible.
   * @throws org.eclipse.swt.SWTException
   * <ul>
   * <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   * <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that
   * created the receiver</li>
   * </ul>
   */
  public boolean getLinesVisible() {
    checkWidget();
    return linesVisible;
  }

  /**
   * Sets the tree line visibility.
   *
   * @param treeLinesVisible
   * @throws org.eclipse.swt.SWTException
     * <ul>
     * <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     * <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that
     * created the receiver</li>
     * </ul>
   */
  public void setTreeLinesVisible( boolean treeLinesVisible ) {
    checkWidget();
    this.treeLinesVisible = treeLinesVisible;
    redraw();
  }

  /**
   * Returns true if the tree lines are visible.
   *
   * @return Returns the treeLinesVisible.
   * @throws org.eclipse.swt.SWTException
   * <ul>
   * <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   * <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that
   * created the receiver</li>
   * </ul>
   */
  public boolean getTreeLinesVisible() {
    checkWidget();
    return treeLinesVisible;
  }

  /**
   * Sets the focused item to the given item.
   *
   * @param item item to focus.
   * @throws IllegalArgumentException
   * <ul>
   * <li>ERROR_INVALID_ARGUMENT - if item is disposed</li>
   * </ul>
   * @throws org.eclipse.swt.SWTException
   * <ul>
   * <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   * <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that
   * created the receiver</li>
   * </ul>
   */
  public void setFocusItem( GridItem item ) {
    checkWidget();
    if( item == null || item.isDisposed() || item.getParent() != this || !item.isVisible() ) {
      SWT.error( SWT.ERROR_INVALID_ARGUMENT );
    }
    focusItem = item;
  }

  /**
   * Returns the current item in focus.
   *
   * @return item in focus or {@code null}.
   * @throws org.eclipse.swt.SWTException
   * <ul>
   * <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   * <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that
   * created the receiver</li>
   * </ul>
   */
  public GridItem getFocusItem() {
    checkWidget();
    return focusItem;
  }

  /**
   * Creates the new item at the given index. Only called from GridItem
   * constructor.
   *
   * @param item new item
   * @param index index to insert the item at
   * @return the index where the item was insert
   */
  int newItem( GridItem item, int index, boolean root ) {
    int row = 0;
    GridItem parentItem = item.getParentItem();
    if( !isTree && parentItem != null ) {
      isTree = true;
    }
    int flatIndex = index;
    // Have to convert indexes, this method needs a flat index, the method is called with indexes
    // that are relative to the level
    if( root && index != -1 ) {
      if( index >= rootItems.size() ) {
        flatIndex = -1;
      } else {
        flatIndex = items.indexOf( rootItems.get( index ) );
      }
    } else if( !root ) {
      if( index >= parentItem.getItems().length || index == -1 ) {
        GridItem rightMostDescendent = parentItem;
        while( rightMostDescendent.getItems().length > 0 ) {
          GridItem[] rightMostDescendentItems = rightMostDescendent.getItems();
          rightMostDescendent = rightMostDescendentItems[ rightMostDescendentItems.length - 1 ];
        }
        flatIndex = indexOf( rightMostDescendent ) + 1;
      } else {
        flatIndex = indexOf( parentItem.getItems()[ index ] );
      }
    }
    if( flatIndex == -1 ) {
      items.add( item );
      row = items.size() - 1;
    } else {
      items.add( flatIndex, item );
      row = flatIndex;
    }
    scrollValuesObsolete = true;
    currentVisibleItems++;
    redraw();
    return row;
  }

  /**
   * Removes the given item from the table. This method is only called from
   * the item's dispose method.
   *
   * @param item item to remove
   */
  void removeItem( GridItem item ) {
    items.remove( item );
    if( !disposing ) {
      selectedItems.remove (item );
// TODO: [if] Implement cell selection
//      Point[] cells = getCells( item );
//      for( int i = 0; i < cells.length; i++ ) {
//        selectedCells.remove( cells[ i ] );
//      }
      if( focusItem == item ) {
        focusItem = null;
      }
      scrollValuesObsolete = true;
      if( item.isVisible() ) {
        currentVisibleItems--;
      }
      redraw();
    }
  }

  void newRootItem( GridItem item, int index ) {
    if( index == -1 || index >= rootItems.size() ) {
      rootItems.add( item );
    } else {
      rootItems.add( index, item );
    }
  }

  void removeRootItem( GridItem item ) {
    rootItems.remove( item );
  }

  /**
   * Inserts a new column into the table.
   *
   * @param column new column
   * @param index index to insert new column
   * @return current number of columns
   */
  int newColumn( GridColumn column, int index ) {
    if( index == -1 ) {
      columns.add( column );
      displayOrderedColumns.add( column );
    } else {
      columns.add( index, column );
      displayOrderedColumns.add( index, column );
    }
    updatePrimaryCheckColumn();
    for( Iterator iterator = items.iterator(); iterator.hasNext(); ) {
      GridItem item = ( GridItem )iterator.next();
      item.columnAdded( index );
    }
    scrollValuesObsolete = true;
    redraw();
    return columns.size() - 1;
  }

  /**
   * Removes the given column from the table.
   *
   * @param column column to remove
   */
  void removeColumn( GridColumn column ) {
    int index = indexOf( column );
    columns.remove( column );
    displayOrderedColumns.remove( column );
    updatePrimaryCheckColumn();
    for( Iterator iterator = items.iterator(); iterator.hasNext(); ) {
      GridItem item = ( GridItem )iterator.next();
      item.columnRemoved( index );
    }
    scrollValuesObsolete = true;
    redraw();
  }

  /**
   * @return the disposing
   */
  boolean isDisposing() {
    return disposing;
  }

  /**
   * Updates the cached number of visible items by the given amount.
   *
   * @param amount amount to update cached total
   */
  void updateVisibleItems( int amount ) {
    currentVisibleItems += amount;
  }

  void setHasSpanning( boolean hasSpanning ) {
    this.hasSpanning = hasSpanning;
  }

  /**
   * Returns an array of the columns in their display order.
   *
   * @return columns in display order
   */
  GridColumn[] getColumnsInOrder() {
    checkWidget();
    return displayOrderedColumns.toArray( new GridColumn[ columns.size() ] );
  }

  /**
   * Returns the externally managed horizontal scrollbar.
   *
   * @return the external horizontal scrollbar.
   * @see #setHorizontalScrollBarProxy(IScrollBarProxy)
   * @throws org.eclipse.swt.SWTException
   * <ul>
   * <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   * <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that
   * created the receiver</li>
   * </ul>
   */
  protected IScrollBarProxy getHorizontalScrollBarProxy() {
    checkWidget();
    return hScroll;
  }

  /**
   * Returns the externally managed vertical scrollbar.
   *
   * @return the external vertical scrollbar.
   * @see #setlVerticalScrollBarProxy(IScrollBarProxy)
   * @throws org.eclipse.swt.SWTException
   * <ul>
   * <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   * <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that
   * created the receiver</li>
   * </ul>
   */
  protected IScrollBarProxy getVerticalScrollBarProxy() {
    checkWidget();
    return vScroll;
  }

  /**
   * Marks the scroll values obsolete so they will be recalculated.
   */
  protected void setScrollValuesObsolete() {
    this.scrollValuesObsolete = true;
    redraw();
  }

  /**
   * Initialize all listeners.
   */
  private void initListeners() {
  }

  private void internalSelect( int index ) {
    if( index >= 0 && index < items.size() ) {
      GridItem item = items.get( index );
      if( cellSelectionEnabled ) {
// TODO: [if] Implement cell selection
//        selectCells( getCells( item ) );
      } else if( !selectedItems.contains( item ) ) {
        selectedItems.add( item );
      }
    }
  }
  private void internalDeselect( int index ) {
    if( index >= 0 && index < items.size() ) {
      GridItem item = items.get( index );
      if( cellSelectionEnabled ) {
// TODO: [if] Implement cell selection
//        deselectCells( getCells( item ) );
      } else if( selectedItems.contains( item ) ) {
        selectedItems.remove( item );
      }
    }
  }

  private void internalDeselectAll() {
    if( cellSelectionEnabled ) {
// TODO: [if] Implement cell selection
//      selectedCells.clear();
    } else {
      selectedItems.clear();
    }
  }

  /**
   * Manages the setting of the checkbox column when the SWT.CHECK style was given to the
   * table.  This method will ensure that the first column of the table always has a checkbox
   * when SWT.CHECK is given to the table.
   */
  private void updatePrimaryCheckColumn() {
    if( ( getStyle() & SWT.CHECK ) == SWT.CHECK ) {
      boolean firstCol = true;
      for( Iterator iter = columns.iterator(); iter.hasNext(); ) {
        GridColumn col = ( GridColumn )iter.next();
        col.setTableCheck( firstCol );
        firstCol = false;
      }
    }
  }

  /**
   * Filters out unnecessary styles, adds mandatory styles and generally
   * manages the style to pass to the super class.
   *
   * @param style user specified style.
   * @return style to pass to the super class.
   */
  private static int checkStyle( int style ) {
    int mask =   SWT.BORDER
               | SWT.LEFT_TO_RIGHT
//               | SWT.RIGHT_TO_LEFT
               | SWT.H_SCROLL
               | SWT.V_SCROLL
               | SWT.SINGLE
               | SWT.MULTI
               | SWT.NO_FOCUS
               | SWT.CHECK
               | SWT.VIRTUAL;
    int result = style & mask;
    result |= SWT.DOUBLE_BUFFERED;
    return result;
  }
}
