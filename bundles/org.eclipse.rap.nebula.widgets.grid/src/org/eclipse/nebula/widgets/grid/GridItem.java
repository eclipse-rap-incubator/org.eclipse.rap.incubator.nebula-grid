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

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.internal.SerializableCompatibility;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Item;


/**
 * <p>
 * NOTE: THIS WIDGET AND ITS API ARE STILL UNDER DEVELOPMENT. THIS IS A
 * PRE-RELEASE ALPHA VERSION. USERS SHOULD EXPECT API CHANGES IN FUTURE
 * VERSIONS.
 * </p>
 * Instances of this class represent a selectable user interface object that
 * represents an item in a grid.
 * <p>
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>(none)</dd>
 * <dt><b>Events:</b></dt>
 * <dd>(none)</dd>
 * </dl>
 */
@SuppressWarnings("restriction")
public class GridItem extends Item {

  /**
   * Parent grid instance.
   */
  private Grid parent;

  /**
   * Parent item (if a child item).
   */
  private GridItem parentItem;

  /**
   * List of item data for each column.
   */
  private ArrayList<Data> data;

  /**
   * List of children.
   */
  private ArrayList<GridItem> children = new ArrayList<GridItem>();

  /**
   * True if has children.
   */
  private boolean hasChildren;

  /**
   * Level of item in a tree.
   */
  private int level = 0;

  /**
   * Is visible?
   */
  private boolean visible = true;

  /**
   * Is expanded?
   */
  private boolean expanded = false;

  /**
   * (SWT.VIRTUAL only) Flag that specifies whether the client has already
   * been sent a SWT.SetData event.
   */
  private boolean hasSetData = false;

  /**
   * Creates a new instance of this class and places the item at the end of
   * the grid.
   *
   * @param parent
   *            parent grid
   * @param style
   *            item style
   * @throws IllegalArgumentException
   *             <ul>
   *             <li>ERROR_NULL_ARGUMENT - if the parent is null</li>
   *             </ul>
   * @throws org.eclipse.swt.SWTException
   *             <ul>
   *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *             thread that created the parent</li>
   *             <li>ERROR_INVALID_SUBCLASS - if this class is not an allowed
   *             subclass</li>
   *             </ul>
   */
  public GridItem( Grid parent, int style ) {
    this( parent, style, -1 );
  }

  /**
   * Creates a new instance of this class and places the item in the grid at
   * the given index.
   *
   * @param parent
   *            parent grid
   * @param style
   *            item style
   * @param index
   *            index where to insert item
   * @throws IllegalArgumentException
   *             <ul>
   *             <li>ERROR_NULL_ARGUMENT - if the parent is null</li>
   *             </ul>
   * @throws org.eclipse.swt.SWTException
   *             <ul>
   *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *             thread that created the parent</li>
   *             <li>ERROR_INVALID_SUBCLASS - if this class is not an allowed
   *             subclass</li>
   *             </ul>
   */
  public GridItem( Grid parent, int style, int index ) {
    super( parent, style, index );
    this.parent = parent;
    init();
    parent.newItem( this, index, true );
    parent.newRootItem( this, index );
  }

  /**
   * Creates a new instance of this class as a child node of the given
   * GridItem and places the item at the end of the parents items.
   *
   * @param parent
   *            parent item
   * @param style
   *            item style
   * @throws IllegalArgumentException
   *             <ul>
   *             <li>ERROR_NULL_ARGUMENT - if the parent is null</li>
   *             </ul>
   * @throws org.eclipse.swt.SWTException
   *             <ul>
   *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *             thread that created the parent</li>
   *             <li>ERROR_INVALID_SUBCLASS - if this class is not an allowed
   *             subclass</li>
   *             </ul>
   */
  public GridItem( GridItem parent, int style ) {
    this( parent, style, -1 );
  }

  /**
   * Creates a new instance of this class as a child node of the given Grid
   * and places the item at the given index in the parent items list.
   *
   * @param parent
   *            parent item
   * @param style
   *            item style
   * @param index
   *            index to place item
   * @throws IllegalArgumentException
   *             <ul>
   *             <li>ERROR_NULL_ARGUMENT - if the parent is null</li>
   *             </ul>
   * @throws org.eclipse.swt.SWTException
   *             <ul>
   *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *             thread that created the parent</li>
   *             <li>ERROR_INVALID_SUBCLASS - if this class is not an allowed
   *             subclass</li>
   *             </ul>
   */
  public GridItem( GridItem parent, int style, int index ) {
    super( parent, style, index );
    parentItem = parent;
    this.parent = parentItem.getParent();
    this.parent.newItem( this, index, false );
    init();
    level = parentItem.getLevel() + 1;
    parentItem.newItem( this, index );
    if( parent.isVisible() && parent.isExpanded() ) {
      setVisible( true );
    } else {
      setVisible( false );
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void dispose() {
    if( !parent.isDisposing() ) {
      parent.removeItem( this );
      if( parentItem != null ) {
        parentItem.remove( this );
      } else {
        parent.removeRootItem( this );
      }
      for( int i = children.size() - 1; i >= 0; i-- ) {
        children.get( i ).dispose();
      }
    }
    super.dispose();
  }

  /**
   * Fires the given event type on the parent Grid instance. This method
   * should only be called from within a cell renderer. Any other use is not
   * intended.
   *
   * @param eventId
   *            SWT event constant
   * @throws org.eclipse.swt.SWTException
   *             <ul>
   *             <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
   *             </li>
   *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *             thread that created the receiver</li>
   *             </ul>
   */
  public void fireEvent( int eventId ) {
    checkWidget();
    Event event = new Event();
    event.display = getDisplay();
    event.widget = this;
    event.item = this;
    event.type = eventId;
    getParent().notifyListeners( eventId, event );
  }

  /**
   * Fires the appropriate events in response to a user checking/unchecking an
   * item. Checking an item fires both a selection event (with event.detail of
   * SWT.CHECK) if the checkbox is in the first column and the seperate check
   * listener (all columns). This method manages that behavior. This method
   * should only be called from within a cell renderer. Any other use is not
   * intended.
   *
   * @param column
   *            the column where the checkbox resides
   * @throws org.eclipse.swt.SWTException
   *             <ul>
   *             <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
   *             </li>
   *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *             thread that created the receiver</li>
   *             </ul>
   */
  public void fireCheckEvent( int column ) {
    checkWidget();
    Event event = new Event();
    event.display = getDisplay();
    event.widget = this;
    event.item = this;
    event.type = SWT.Selection;
    event.detail = SWT.CHECK;
    event.index = column;
    getParent().notifyListeners( SWT.Selection, event );
  }

  /**
   * Returns the receiver's parent, which must be a <code>Grid</code>.
   *
   * @return the receiver's parent
   * @throws org.eclipse.swt.SWTException
   *             <ul>
   *             <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
   *             </li>
   *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *             thread that created the receiver</li>
   *             </ul>
   */
  public Grid getParent() {
    checkWidget();
    return parent;
  }

  /**
   * Returns the receiver's parent item, which must be a <code>GridItem</code>
   * or null when the receiver is a root.
   *
   * @return the receiver's parent item
   * @throws org.eclipse.swt.SWTException
   *             <ul>
   *             <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
   *             </li>
   *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *             thread that created the receiver</li>
   *             </ul>
   */
  public GridItem getParentItem() {
    checkWidget();
    return parentItem;
  }

  /**
   * Returns the number of items contained in the receiver that are direct
   * item children of the receiver.
   *
   * @return the number of items
   *
   * @throws org.eclipse.swt.SWTException
   *             <ul>
   *             <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
   *             </li>
   *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *             thread that created the receiver</li>
   *             </ul>
   */
  public int getItemCount() {
    checkWidget();
    return children.size();
  }

  /**
   * Returns a (possibly empty) array of <code>GridItem</code>s which are the
   * direct item children of the receiver.
   * <p>
   * Note: This is not the actual structure used by the receiver to maintain
   * its list of items, so modifying the array will not affect the receiver.
   * </p>
   *
   * @return the receiver's items
   * @throws org.eclipse.swt.SWTException
   *             <ul>
   *             <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
   *             </li>
   *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *             thread that created the receiver</li>
   *             </ul>
   */
  public GridItem[] getItems() {
    return children.toArray( new GridItem[ children.size() ] );
  }

  /**
   * Returns the item at the given, zero-relative index in the receiver.
   * Throws an exception if the index is out of range.
   *
   * @param index
   *            the index of the item to return
   * @return the item at the given index
   * @throws IllegalArgumentException
   *             <ul>
   *             <li>ERROR_INVALID_RANGE - if the index is not between 0 and
   *             the number of elements in the list minus 1 (inclusive)</li>
   *             </ul>
   * @throws org.eclipse.swt.SWTException
   *             <ul>
   *             <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
   *             </li>
   *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *             thread that created the receiver</li>
   *             </ul>
   */
  public GridItem getItem( int index ) {
    checkWidget();
    return children.get( index );
  }

  /**
   * Searches the receiver's list starting at the first item (index 0) until
   * an item is found that is equal to the argument, and returns the index of
   * that item. If no item is found, returns -1.
   *
   * @param item
   *            the search item
   * @return the index of the item
   *
   * @exception IllegalArgumentException
   *                <ul>
   *                <li>ERROR_NULL_ARGUMENT - if the item is null</li>
   *                <li>ERROR_INVALID_ARGUMENT - if the item has been disposed
   *                </li>
   *                </ul>
   * @exception org.eclipse.swt.SWTException
   *                <ul>
   *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
   *                disposed</li>
   *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *                thread that created the receiver</li>
   *                </ul>
   */
  public int indexOf( GridItem item ) {
    checkWidget();
    if( item == null ) {
      SWT.error( SWT.ERROR_NULL_ARGUMENT );
    }
    if( item.isDisposed() ) {
      SWT.error( SWT.ERROR_INVALID_ARGUMENT );
    }
    return children.indexOf( item );
  }

  /**
   * Returns true if this item has children.
   *
   * @return true if this item has children
   * @throws org.eclipse.swt.SWTException
   *             <ul>
   *             <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
   *             </li>
   *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *             thread that created the receiver</li>
   *             </ul>
   */
  public boolean hasChildren() {
    checkWidget();
    return hasChildren;
  }

  /**
   * Returns <code>true</code> if the receiver is expanded, and false
   * otherwise.
   * <p>
   *
   * @return the expanded state
   * @throws org.eclipse.swt.SWTException
   *             <ul>
   *             <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
   *             </li> <li>ERROR_THREAD_INVALID_ACCESS - if not called from
   *             the thread that created the receiver</li>
   *             </ul>
   */
  public boolean isExpanded() {
    checkWidget();
    return expanded;
  }

  /**
   * Sets the expanded state of the receiver.
   * <p>
   *
   * @param expanded
   *            the new expanded state
   * @throws org.eclipse.swt.SWTException
   *             <ul>
   *             <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
   *             </li> <li>ERROR_THREAD_INVALID_ACCESS - if not called from
   *             the thread that created the receiver</li>
   *             </ul>
   */
  public void setExpanded( boolean expanded ) {
    checkWidget();
    this.expanded = expanded;
    for( Iterator itemIterator = children.iterator(); itemIterator.hasNext(); ) {
      GridItem item = ( GridItem )itemIterator.next();
      item.setVisible( expanded && visible );
    }
  }

  /**
   * Returns the level of this item in the tree.
   *
   * @return the level of the item in the tree
   * @throws org.eclipse.swt.SWTException
   *             <ul>
   *             <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
   *             </li>
   *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *             thread that created the receiver</li>
   *             </ul>
   */
  public int getLevel() {
    checkWidget();
    return level;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setText( String string ) {
    setText( 0, string );
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getText() {
    return getText( 0 );
  }

  /**
   * Sets the receiver's text at a column.
   *
   * @param index
   *            the column index
   * @param text
   *            the new text
   * @throws IllegalArgumentException
   *             <ul>
   *             <li>ERROR_NULL_ARGUMENT - if the text is null</li>
   *             </ul>
   * @throws org.eclipse.swt.SWTException
   *             <ul>
   *             <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
   *             </li>
   *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *             thread that created the receiver</li>
   *             </ul>
   */
  public void setText( int index, String text ) {
    checkWidget();
    if( text == null ) {
      SWT.error( SWT.ERROR_NULL_ARGUMENT );
    }
    getItemData( index ).text = text;
    parent.redraw();
  }

  /**
   * Returns the text stored at the given column index in the receiver, or
   * empty string if the text has not been set.
   *
   * @param index
   *            the column index
   * @return the text stored at the given column index in the receiver
   * @throws org.eclipse.swt.SWTException
   *             <ul>
   *             <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
   *             </li>
   *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *             thread that created the receiver</li>
   *             </ul>
   */
  public String getText( int index ) {
    checkWidget();
    handleVirtual();
    return getItemData( index ).text;
  }

  /**
   * Sets whether this item has children.
   *
   * @param hasChildren
   *            true if this item has children
   */
  void setHasChildren( boolean hasChildren ) {
    this.hasChildren = hasChildren;
  }

  /**
   * Returns true if the item is visible because its parent items are all
   * expanded. This method does not determine if the item is in the currently
   * visible range.
   *
   * @return Returns the visible.
   */
  boolean isVisible() {
    return visible;
  }

  /**
   * Sets the visible state of this item. The visible state is determined by
   * the expansion state of all of its parent items. If all parent items are
   * expanded it is visible.
   *
   * @param visible
   *            The visible to set.
   */
  void setVisible( boolean visible ) {
    if( this.visible != visible ) {
      this.visible = visible;
      if( visible ) {
        parent.updateVisibleItems( 1 );
      } else {
        parent.updateVisibleItems( -1 );
      }
      if( hasChildren ) {
        boolean childrenVisible = visible;
        if( visible ) {
          childrenVisible = expanded;
        }
        for( Iterator itemIterator = children.iterator(); itemIterator.hasNext(); ) {
          GridItem item = ( GridItem )itemIterator.next();
          item.setVisible( childrenVisible );
        }
      }
    }
  }

  /**
   * Creates a new child item in this item at the given index.
   *
   * @param item
   *            new child item
   * @param index
   *            index
   */
  private void newItem( GridItem item, int index ) {
    setHasChildren( true );
    if( index == -1 ) {
      children.add( item );
    } else {
      children.add( index, item );
    }
  }

  /**
   * Removes the given child item from the list of children.
   *
   * @param child
   *            child to remove
   */
  private void remove( GridItem child ) {
    children.remove( child );
    hasChildren = children.size() > 0;
  }

  void columnAdded( int index ) {
    if( parent.getColumnCount() > 1 ) {
      if( index == -1 ) {
        data.add( null );
      } else {
        data.add( index, null );
      }
      hasSetData = false;
    }
  }

  void columnRemoved( int index ) {
    if( parent.getColumnCount() > 0 ) {
      if( data.size() > index ) {
        data.remove( index );
      }
    }
  }

  /**
   * Clears all properties of this item and resets values to their defaults.
   *
   * @param allChildren
   *            <code>true</code> if all child items should be cleared
   *            recursively, and <code>false</code> otherwise
   */
  void clear( boolean allChildren ) {
    init();
//    defaultFont = null;
//    defaultBackground = null;
//    defaultForeground = null;
    hasSetData = false;
    // Recursively clear children if requested.
    if( allChildren ) {
      for( int i = children.size() - 1; i >= 0; i-- ) {
        children.get( i ).clear( true );
      }
    }
  }

  private void init() {
    if( data == null ) {
      data = new ArrayList<Data>();
    } else {
      data.clear();
    }
    data.add( null );
    for( int i = 1; i < parent.getColumnCount(); i++ ) {
      data.add( null );
    }
  }

  private Data getItemData( int index ) {
    if( data.get( index ) == null ) {
      data.set( index, new Data() );
    }
    return data.get( index );
  }

  private void handleVirtual() {
    if( ( getParent().getStyle() & SWT.VIRTUAL ) != 0 && !hasSetData ) {
      hasSetData = true;
      Event event = new Event();
      event.item = this;
      if( parentItem == null ) {
        event.index = getParent().indexOf( this );
      } else {
        event.index = parentItem.indexOf( this );
      }
      getParent().notifyListeners( SWT.SetData, event );
    }
  }

  ////////////////
  // Inner classes

  private static final class Data implements SerializableCompatibility {
    public Font font;
    public Color background;
    public Color foreground;
    public boolean checked;
    public boolean checkable = true;
    public boolean grayed;
    public Image image;
    public String text = "";
    public String tooltip;
    public int columnSpan;
    public int rowSpan;
  }

}
