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
import org.eclipse.nebula.widgets.grid.internal.gridkit.GridThemeAdapter;
import org.eclipse.rwt.graphics.Graphics;
import org.eclipse.rwt.internal.textsize.TextSizeUtil;
import org.eclipse.rwt.internal.theme.IThemeAdapter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TreeEvent;
import org.eclipse.swt.events.TreeListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.internal.SerializableCompatibility;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ScrollBar;


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
@SuppressWarnings("restriction")
public class Grid extends Canvas {

  private static final int MIN_ITEM_HEIGHT = 16;
  private static final int GRID_WIDTH = 1;

  private List<GridItem> items = new ArrayList<GridItem>();
  private List<GridItem> rootItems = new ArrayList<GridItem>();
  private List<GridItem> selectedItems = new ArrayList<GridItem>();
  private List<Point> selectedCells = new ArrayList<Point>();
  private List<GridColumn> columns = new ArrayList<GridColumn>();
  private List<GridColumn> displayOrderedColumns = new ArrayList<GridColumn>();
  private GridItem focusItem;
  private boolean isTree;
  private boolean disposing;
  private boolean columnHeadersVisible;
  private boolean linesVisible = true;
  private int currentVisibleItems;
  private int selectionType = SWT.SINGLE;
  private boolean selectionEnabled = true;
  private boolean cellSelectionEnabled;
  private int customItemHeight = -1;
  private Point itemImageSize;
  private ControlListener resizeListener;
  LayoutCache layoutCache;
  private boolean isTemporaryResize;
  private IScrollBarProxy vScroll;
  private IScrollBarProxy hScroll;
  private int topIndex = -1;

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
    layoutCache = new LayoutCache();
    initListeners();
  }

  @Override
  public void dispose() {
    disposing = true;
    removeControlListener( resizeListener );
    super.dispose();
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
   * {@inheritDoc}
   */
  @Override
  public Point computeSize( int wHint, int hHint, boolean changed ) {
    checkWidget();
    Point rreferredSize = null;
    if( wHint == SWT.DEFAULT || hHint == SWT.DEFAULT ) {
      rreferredSize = getTableSize();
      rreferredSize.x += 2 * getBorderWidth();
      rreferredSize.y += 2 * getBorderWidth();
    }
    int width = 0;
    int height = 0;
    if( wHint == SWT.DEFAULT ) {
      width += rreferredSize.x;
      if( getVerticalBar() != null ) {
        width += getVerticalBar().getSize().x;
      }
    } else {
      width = wHint;
    }
    if( hHint == SWT.DEFAULT ) {
      height += rreferredSize.y;
      if( getHorizontalBar() != null ) {
        height += getHorizontalBar().getSize().y;
      }
    } else {
      height = hHint;
    }
    return new Point( width, height );
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
   * Sets the number of items contained in the receiver.
   *
   * @param count the number of items
   *
   * @exception org.eclipse.swt.SWTException
   * <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public void setItemCount( int count ) {
    checkWidget();
    int itemCount = Math.max( 0, count );
    while( itemCount < items.size() ) {
      items.get( items.size() - 1 ).dispose();
    }
    while( itemCount > items.size() ) {
      new GridItem( this, SWT.NONE );
    }
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
   * Returns the next visible item in the table.
   *
   * @param item item
   * @return next visible item or null
   * @throws org.eclipse.swt.SWTException
   * <ul>
   * <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   * <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that
   * created the receiver</li>
   * </ul>
   */
  public GridItem getNextVisibleItem( GridItem item ) {
    checkWidget();
    GridItem result = null;
    int index = items.indexOf( item );
    if( index != items.size() - 1 ) {
      result = items.get( index + 1 );
      while( result != null && !result.isVisible() ) {
        index++;
        if( index != items.size() - 1 ) {
          result = items.get( index + 1 );
        } else {
          result = null;
        }
      }
    }
    return result;
  }

  /**
   * Returns the previous visible item in the table. Passing null for the item
   * will return the last visible item in the table.
   *
   * @param item item or null
   * @return previous visible item or if item==null last visible item
   * @throws org.eclipse.swt.SWTException
   * <ul>
   * <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   * <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that
   * created the receiver</li>
   * </ul>
   */
  public GridItem getPreviousVisibleItem( GridItem item ) {
    checkWidget();
    GridItem result = null;
    int index = 0;
    if( item == null ) {
      index = items.size();
    } else {
      index = items.indexOf( item );
    }
    if( index > 0 ) {
      result = items.get( index - 1 );
      while( result != null && !result.isVisible() ) {
        index--;
        if( index > 0 ) {
          result = items.get( index - 1 );
        } else {
          result = null;
        }
      }
    }
    return result;
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
   * Returns the next visible column in the table.
   *
   * @param column column
   * @return next visible column or null
   * @throws org.eclipse.swt.SWTException
   * <ul>
   * <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   * <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that
   * created the receiver</li>
   * </ul>
   */
  public GridColumn getNextVisibleColumn( GridColumn column ) {
    checkWidget();
    GridColumn result = null;
    int index = displayOrderedColumns.indexOf( column );
    if( index != displayOrderedColumns.size() - 1 ) {
      result = displayOrderedColumns.get( index + 1 );
      while( result != null && !result.isVisible() ) {
        index++;
        if( index != displayOrderedColumns.size() - 1 ) {
          result = displayOrderedColumns.get( index + 1 );
        } else {
          result = null;
        }
      }
    }
    return result;
  }

  /**
   * Returns the previous visible column in the table.
   *
   * @param column column
   * @return previous visible column or null
   * @throws org.eclipse.swt.SWTException
   * <ul>
   * <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   * <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that
   * created the receiver</li>
   * </ul>
   */
  public GridColumn getPreviousVisibleColumn( GridColumn column ) {
    checkWidget();
    GridColumn result = null;
    int index = 0;
    if( column == null ) {
      index = displayOrderedColumns.size();
    } else {
      index = displayOrderedColumns.indexOf( column );
    }
    if( index > 0 ) {
      result = displayOrderedColumns.get( index - 1 );
      while( result != null && !result.isVisible() ) {
        index--;
        if( index > 0 ) {
          result = displayOrderedColumns.get( index - 1 );
        } else {
          result = null;
        }
      }
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
      itemImageSize = null;
      layoutCache.invalidateItemHeight();
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
    if( columnHeadersVisible != show ) {
      columnHeadersVisible = show;
      layoutCache.invalidateHeaderHeight();
      updateScrollBars();
      redraw();
    }
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
   * Returns the height of the column headers. If this table has column
   * groups, the returned value includes the height of group headers.
   *
   * @return height of the column header row
   * @throws org.eclipse.swt.SWTException
   * <ul>
   * <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   * <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that
   * created the receiver</li>
   * </ul>
   */
  public int getHeaderHeight() {
    checkWidget();
    if( !layoutCache.hasHeaderHeight() ) {
      layoutCache.headerHeight = computeHeaderHeight();
    }
    return layoutCache.headerHeight;
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
   * Sets the default height for this <code>Grid</code>'s items.  When
   * this method is called, all existing items are resized
   * to the specified height and items created afterwards will be
   * initially sized to this height.
   * <p>
   * As long as no default height was set by the client through this method,
   * the preferred height of the first item in this <code>Grid</code> is
   * used as a default for all items (and is returned by {@link #getItemHeight()}).
   *
   * @param height  default height in pixels
   * @throws IllegalArgumentException
   * <ul>
   * <li>ERROR_INVALID_ARGUMENT - if the height is < 1</li>
   * </ul>
   * @throws org.eclipse.swt.SWTException
   * <ul>
   * <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   * <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that
   * created the receiver</li>
   * </ul>
   *
   * @see GridItem#getHeight()
   */
  public void setItemHeight( int height ) {
    checkWidget();
    if( height < 1 ) {
      SWT.error( SWT.ERROR_INVALID_ARGUMENT );
    }
    customItemHeight = height;
    updateScrollBars();
    redraw();
  }

  /**
   * Returns the default height of the items
   * in this <code>Grid</code>. See {@link #setItemHeight(int)}
   * for details.
   *
   * @return default height of items
   * @throws org.eclipse.swt.SWTException
   * <ul>
   * <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   * <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that
   * created the receiver</li>
   * </ul>
   * @see #setItemHeight(int)
   */
  public int getItemHeight() {
    checkWidget();
    int result = customItemHeight;
    if( result == -1 ) {
      if( !layoutCache.hasItemHeight() ) {
        layoutCache.itemHeight = computeItemHeight();
      }
      result = layoutCache.itemHeight;
    }
    return result;
  }

  @Override
  public void setFont( Font font ) {
    super.setFont( font );
    layoutCache.invalidateItemHeight();
    updateScrollBars();
    redraw();
  }

  /**
   * Sets the zero-relative index of the item which is currently at the top of
   * the receiver. This index can change when items are scrolled or new items
   * are added and removed.
   *
   * @param index the index of the top item
   * @throws org.eclipse.swt.SWTException
   * <ul>
   * <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   * <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that
   * created the receiver</li>
   * </ul>
   */
  public void setTopIndex( int index ) {
    checkWidget();
    if( index >= 0 && index < items.size() ) {
      GridItem item = items.get( index );
      if( item.isVisible() && vScroll.getVisible() ) {
        int vScrollAmount = 0;
        for( int i = 0; i < index; i++ ) {
          if( items.get( i ).isVisible() ) {
            vScrollAmount++;
          }
        }
        vScroll.setSelection( vScrollAmount );
        invalidateTopIndex();
      }
    }
  }

  /**
   * Returns the zero-relative index of the item which is currently at the top
   * of the receiver. This index can change when items are scrolled or new
   * items are added or removed.
   *
   * @return the index of the top item
   * @throws org.eclipse.swt.SWTException
   * <ul>
   * <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   * <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that
   * created the receiver</li>
   * </ul>
   */
  public int getTopIndex() {
    checkWidget();
    if( topIndex == -1 ) {
      if( vScroll.getVisible() ) {
        int firstVisibleIndex = vScroll.getSelection();
        if( isTree ) {
          Iterator iterator = items.iterator();
          int row = firstVisibleIndex + 1;
          while( row > 0 && iterator.hasNext() ) {
            GridItem item = ( GridItem )iterator.next();
            if( item.isVisible() ) {
              row--;
              if( row == 0 ) {
                firstVisibleIndex = items.indexOf( item );
              }
            }
          }
        }
        topIndex = firstVisibleIndex;
      } else {
        topIndex = 0;
      }
    }
    return topIndex;
  }

  /**
   * Shows the item. If the item is already showing in the receiver, this
   * method simply returns. Otherwise, the items are scrolled until the item
   * is visible.
   *
   * @param item the item to be shown
   * @throws org.eclipse.swt.SWTException
   * <ul>
   * <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   * <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that
   * created the receiver</li>
   * <li>ERROR_INVALID_ARGUMENT - if 'item' is not contained in the receiver</li>
   * </ul>
   */
  public void showItem( GridItem item ) {
    checkWidget();
    if( item == null ) {
      SWT.error( SWT.ERROR_NULL_ARGUMENT );
    }
    if( item.isDisposed() ) {
      SWT.error( SWT.ERROR_INVALID_ARGUMENT );
    }
    if( item.getParent() == this ) {
      int visibleGridHeight = getVisibleGridHeight();
      if( visibleGridHeight >= 1 ) {
        updateScrollBars();
        GridItem parent = item.getParentItem();
        while( parent != null ) {
          if( !parent.isExpanded() ) {
            parent.setExpanded( true );
            parent.fireEvent( SWT.Expand );
          }
          parent = parent.getParentItem();
        }
        int counter = 0;
        int itemFlatIndex = -1;
        int topItemFlatIndex = -1;
        int topIndex = getTopIndex();
        for( int i = 0; i < items.size() && ( itemFlatIndex == -1 || topItemFlatIndex == -1 ); i++ )
        {
          GridItem currentItem = items.get( i );
          if( item == currentItem ) {
            itemFlatIndex = counter;
          }
          if( topIndex == i ) {
            topItemFlatIndex = counter;
          }
          if( currentItem.isVisible() ) {
            counter++;
          }
        }
        int newTopIndex = items.indexOf( item );
        if( itemFlatIndex <= topItemFlatIndex ) {
          setTopIndex( newTopIndex );
        } else {
          int rows = ( int )Math.floor( visibleGridHeight / getItemHeight() );
          if( itemFlatIndex >= topItemFlatIndex + rows ) {
            setTopIndex( newTopIndex );
          }
        }
      }
    }
  }

  /**
   * Shows the column. If the column is already showing in the receiver, this
   * method simply returns. Otherwise, the columns are scrolled until the
   * column is visible.
   *
   * @param column the column to be shown
   * @throws org.eclipse.swt.SWTException
   * <ul>
   * <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   * <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that
   * created the receiver</li>
   * </ul>
   */
  public void showColumn( GridColumn column ) {
    checkWidget();
    if( column == null ) {
      SWT.error( SWT.ERROR_NULL_ARGUMENT );
    }
    if( column.isDisposed() ) {
      SWT.error( SWT.ERROR_INVALID_ARGUMENT );
    }
    if( column.getParent() == this ) {
      updateScrollBars();
      // TODO: [if] Enable this code when GridColumnGroup is implemented
//      if( !column.isVisible() ) {
//        GridColumnGroup group = column.getColumnGroup();
//        group.setExpanded( !group.getExpanded() );
//        if( group.getExpanded() ) {
//          group.notifyListeners( SWT.Expand, new Event() );
//        } else {
//          group.notifyListeners( SWT.Collapse, new Event() );
//        }
//      }
      if( hScroll.getVisible() ) {
        int offset = hScroll.getSelection();
        int x = getColumnHeaderXPosition( column );
        if( x < 0 || x + column.getWidth() > getClientArea().width ) {
          if( x < 0 ) {
            hScroll.setSelection( offset + x );
          } else {
            if( column.getWidth() > getClientArea().width ) {
              hScroll.setSelection( offset + x );
            } else {
              x -= getClientArea().width - column.getWidth();
              hScroll.setSelection( offset + x );
            }
          }
        }
      }
    }
  }

  /**
   * Shows the selection. If the selection is already showing in the receiver,
   * this method simply returns. Otherwise, the items are scrolled until the
   * selection is visible.
   *
   * @throws org.eclipse.swt.SWTException
   * <ul>
   * <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   * <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that
   * created the receiver</li>
   * </ul>
   */
  public void showSelection() {
    checkWidget();
    GridItem item = null;
    if( cellSelectionEnabled ) {
      if( selectedCells.size() != 0 ) {
        Point cell = selectedCells.get( 0 );
        item = getItem( cell.y );
        showItem( item );
        GridColumn column = getColumn( cell.x );
        showColumn( column );
      }
    } else {
      if( selectedItems.size() != 0 ) {
        item = selectedItems.get( 0 );
        showItem( item );
      }
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
    invalidateTopIndex();
    currentVisibleItems++;
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
      invalidateTopIndex();
      if( item.isVisible() ) {
        currentVisibleItems--;
      }
      updateScrollBars();
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
    if( column.isCheck() ) {
      layoutCache.invalidateItemHeight();
    }
    layoutCache.invalidateHeaderHeight();
    updateScrollBars();
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
    if( column.isCheck() ) {
      layoutCache.invalidateItemHeight();
    }
    layoutCache.invalidateHeaderHeight();
    updateScrollBars();
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
   * Updates the row height when the first image is set on an item.
   * @param column the column the image is change
   * @param item item which images has just been set on.
   */
  void imageSetOnItem( int column, GridItem item ) {
    Image image = item.getImage( column );
    if( image != null && itemImageSize == null ) {
      Rectangle imageBounds = image.getBounds();
      itemImageSize = new Point( imageBounds.width, imageBounds.height );
      layoutCache.invalidateItemHeight();
      updateScrollBars();
      redraw();
    }
  }

  int getMaxContentWidth( GridColumn column ) {
    return getMaxInnerWidth( getRootItems(), indexOf( column ) );
  }

  private int getMaxInnerWidth( GridItem[] items, int index ) {
    int maxInnerWidth = 0;
    for( int i = 0; i < items.length; i++ ) {
      GridItem item = items[ i ];
      maxInnerWidth = Math.max( maxInnerWidth, item.getPreferredWidth( index ) );
      if( item.isExpanded() ) {
        int innerWidth = getMaxInnerWidth( item.getItems(), index );
        maxInnerWidth = Math.max( maxInnerWidth, innerWidth );
      }
    }
    return maxInnerWidth;
  }

  boolean isTreeColumn( int index ) {
    boolean result = false;
    if( isTree ) {
      int columnCount = getColumnCount();
      result = columnCount == 0 && index == 0 || columnCount > 0 && index == getColumnOrder()[ 0 ];
    }
    return result;
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

  void invalidateTopIndex() {
    topIndex = -1;
  }

  void updateScrollBars() {
    Point preferredSize = getTableSize();
    Rectangle clientArea = getClientArea();
    for( int doublePass = 1; doublePass <= 2; doublePass++ ) {
      if( preferredSize.y > clientArea.height ) {
        vScroll.setVisible( true );
      } else {
        vScroll.setVisible( false );
        vScroll.setValues( 0, 0, 1, 1, 1, 1 );
      }
      if( preferredSize.x > clientArea.width ) {
        hScroll.setVisible( true );
      } else {
        hScroll.setVisible( false );
        hScroll.setValues( 0, 0, 1, 1, 1, 1 );
      }
      clientArea = getClientArea();
    }
    if( vScroll.getVisible() ) {
      int thumb = getVisibleGridHeight() / getItemHeight();
      int selection = Math.min( vScroll.getSelection(), currentVisibleItems );
      vScroll.setValues( selection, 0, currentVisibleItems, thumb, 1, thumb );
    }
    if( hScroll.getVisible() ) {
      int hiddenArea = preferredSize.x - clientArea.width;
      int selection = Math.min( hScroll.getSelection(), hiddenArea );
      hScroll.setValues( selection, 0, preferredSize.x, clientArea.width, 5, clientArea.width );
    }
  }

  private int getVisibleGridHeight() {
    int headerHeight = columnHeadersVisible ? getHeaderHeight() : 0;
    return getClientArea().height - headerHeight;
  }

  /**
   * Initialize all listeners.
   */
  private void initListeners() {
    resizeListener = new ResizeListener();
    addControlListener( resizeListener );
    ScrollBar verticalBar = getVerticalBar();
    if( verticalBar != null ) {
      verticalBar.addSelectionListener( new SelectionAdapter() {
        @Override
        public void widgetSelected( SelectionEvent eevent ) {
          invalidateTopIndex();
        }
      } );
    }
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

  private Point getTableSize() {
    int width = 0;
    int height = 0;
    if( columnHeadersVisible ) {
      height += getHeaderHeight();
    }
    height += getGridHeight();
    for( Iterator iterator = columns.iterator(); iterator.hasNext(); ) {
      GridColumn column = ( GridColumn )iterator.next();
      if( column.isVisible() ) {
        width += column.getWidth();
      }
    }
    return new Point( width, height );
  }

  private int getGridHeight() {
    return currentVisibleItems * getItemHeight();
  }

  private int computeItemHeight() {
    int result = Math.max( getItemImageSize().y, Graphics.getCharHeight( getFont() ) );
    if( hasCheckBoxes() ) {
      result = Math.max( getCheckBoxImageOuterSize().y, result );
    }
    result += getCellPadding().y;
    result += GRID_WIDTH;
    result = Math.max( result, MIN_ITEM_HEIGHT );
    return result;
  }

  private int computeHeaderHeight() {
    int result = 0;
    if( columnHeadersVisible ) {
      int columnHeaderHeight = 0;
      for( int i = 0; i < getColumnCount(); i++ ) {
        columnHeaderHeight = Math.max( columnHeaderHeight, computeColumnHeaderHeight( i ) );
      }
      result = columnHeaderHeight + getThemeAdapter().getHeaderBorderBottomWidth( this );
    }
    return result;
  }

  private int computeColumnHeaderHeight( int index ) {
    GridColumn column = columns.get( index );
    int textHeight = 0;
    Font headerFont = column.getHeaderFont();
    String text = column.getText();
    if( text.contains( "\n" ) ) {
      textHeight = Graphics.textExtent( headerFont, text, 0 ).y;
    } else {
      textHeight = Graphics.getCharHeight( headerFont );
    }
    Image image = column.getImage();
    int imageHeight = image == null ? 0 : image.getBounds().height;
    int result = Math.max( textHeight, imageHeight );
    result += getHeaderPadding().height;
    return result;
  }

  private void repackColumns() {
    for( int i = 0; i < getColumnCount(); i++ ) {
      columns.get( i ).repack();
    }
  }

  private int getColumnHeaderXPosition( GridColumn column ) {
    int result = -1;
    if( column.isVisible() ) {
      result = -hScroll.getSelection();
      boolean found = false;
      for( Iterator iterator = displayOrderedColumns.iterator(); iterator.hasNext() && !found; ) {
        GridColumn currentColumn = ( GridColumn )iterator.next();
        if( currentColumn.isVisible() ) {
          if( currentColumn == column ) {
            found = true;
          } else {
            result += currentColumn.getWidth();
          }
        }
      }
    }
    return result;
  }

  private Point getItemImageSize() {
    Point result = new Point( 0, 0 );
    if( itemImageSize != null ) {
      result.x = itemImageSize.x;
      result.y = itemImageSize.y;
    }
    return result;
  }

  private boolean hasCheckBoxes() {
    boolean result = ( getStyle() & SWT.CHECK ) != 0;
    for( int i = 0; i < getColumnCount() && !result; i++ ) {
      GridColumn column = columns.get( i );
      if( column.isCheck() ) {
        result = true;
      }
    }
    return result;
  }

  Point getCheckBoxImageOuterSize() {
    Point imageSize = getCheckBoxImageSize();
    Rectangle margin = getCheckBoxMargin();
    return new Point( imageSize.x + margin.width, imageSize.y + margin.y );
  }

  private Point getCheckBoxImageSize() {
    if( !layoutCache.hasCheckBoxImageSize() ) {
      layoutCache.checkBoxImageSize = getThemeAdapter().getCheckBoxImageSize( this );
    }
    return layoutCache.checkBoxImageSize;
  }

  private Rectangle getCheckBoxMargin() {
    if( !layoutCache.hasCheckBoxMargin() ) {
      layoutCache.checkBoxMargin = getThemeAdapter().getCheckBoxMargin( this );
    }
    return layoutCache.checkBoxMargin;
  }

  Rectangle getCellPadding() {
    if( !layoutCache.hasCellPadding() ) {
      layoutCache.cellPadding = getThemeAdapter().getCellPadding( this );
    }
    return layoutCache.cellPadding;
  }

  Rectangle getHeaderPadding() {
    if( !layoutCache.hasHeaderPadding() ) {
      layoutCache.headerPadding = getThemeAdapter().getHeaderPadding( this );
    }
    return layoutCache.headerPadding;
  }

  int getIndentationWidth() {
    if( !layoutCache.hasIndentationWidth() ) {
      layoutCache.indentationWidth = getThemeAdapter().getIndentationWidth( this );
    }
    return layoutCache.indentationWidth;
  }

  int getCellSpacing() {
    if( !layoutCache.hasCellSpacing() ) {
      layoutCache.cellSpacing = getThemeAdapter().getCellSpacing( this );
    }
    return layoutCache.cellSpacing;
  }

  private GridThemeAdapter getThemeAdapter() {
    return ( GridThemeAdapter )getAdapter( IThemeAdapter.class );
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

  ////////////////
  // Inner classes

  private final class ResizeListener extends ControlAdapter {
    @Override
    public void controlResized( ControlEvent event ) {
      if( TextSizeUtil.isTemporaryResize() ) {
        isTemporaryResize = true;
        layoutCache.invalidateHeaderHeight();
        layoutCache.invalidateItemHeight();
      } else {
        if( isTemporaryResize) {
          isTemporaryResize = false;
          repackColumns();
        }
        invalidateTopIndex();
        updateScrollBars();
        redraw();
      }
    }
  }

  static final class LayoutCache implements SerializableCompatibility {
    private static final int UNKNOWN = -1;

    int headerHeight = UNKNOWN;
    int itemHeight = UNKNOWN;
    int cellSpacing = UNKNOWN;
    int indentationWidth = UNKNOWN;
    Rectangle cellPadding;
    Rectangle checkBoxMargin;
    Point checkBoxImageSize;
    Rectangle headerPadding;

    public boolean hasHeaderPadding() {
      return headerPadding != null;
    }

    public void invalidateHeaderPadding() {
      headerPadding = null;
    }

    public boolean hasHeaderHeight() {
      return headerHeight != UNKNOWN;
    }

    public void invalidateHeaderHeight() {
      headerHeight = UNKNOWN;
    }

    public boolean hasItemHeight() {
      return itemHeight != UNKNOWN;
    }

    public void invalidateItemHeight() {
      itemHeight = UNKNOWN;
    }

    public boolean hasCellSpacing() {
      return cellSpacing != UNKNOWN;
    }

    public void invalidateCellSpacing() {
      cellSpacing = UNKNOWN;
    }

    public boolean hasIndentationWidth() {
      return indentationWidth != UNKNOWN;
    }

    public void invalidateIndentationWidth() {
      indentationWidth = UNKNOWN;
    }

    public boolean hasCellPadding() {
      return cellPadding != null;
    }

    public void invalidateCellPadding() {
      cellPadding = null;
    }

    public boolean hasCheckBoxMargin() {
      return checkBoxMargin != null;
    }

    public void invalidateCheckBoxMargin() {
      checkBoxMargin = null;
    }

    public boolean hasCheckBoxImageSize() {
      return checkBoxImageSize != null;
    }

    public void invalidateCheckBoxImageSize() {
      checkBoxImageSize = null;
    }

    public void invalidateAll() {
      invalidateHeaderPadding();
      invalidateHeaderHeight();
      invalidateItemHeight();
      invalidateCellSpacing();
      invalidateCellPadding();
      invalidateCheckBoxMargin();
      invalidateCheckBoxImageSize();
      invalidateIndentationWidth();
    }
  }
}
