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
import java.util.Vector;

import org.eclipse.nebula.widgets.grid.internal.IScrollBarProxy;
import org.eclipse.nebula.widgets.grid.internal.NullScrollBarProxy;
import org.eclipse.nebula.widgets.grid.internal.ScrollBarProxyAdapter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TreeEvent;
import org.eclipse.swt.events.TreeListener;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;


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
   * Type of selection behavior. Valid values are SWT.SINGLE and SWT.MULTI.
   */
  private int selectionType = SWT.SINGLE;

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
   * The number of GridItems whose visible = true. Maintained for
   * performance reasons (rather than iterating over all items).
   */
  private int currentVisibleItems = 0;

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
