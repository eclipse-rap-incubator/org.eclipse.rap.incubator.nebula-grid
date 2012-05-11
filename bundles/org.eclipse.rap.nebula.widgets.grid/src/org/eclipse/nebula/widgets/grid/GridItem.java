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

import org.eclipse.swt.SWT;
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
    init();
    this.parent.newItem( this, index, false );
    level = parentItem.getLevel() + 1;
    parentItem.newItem( this, index );
//    if( parent.isVisible() && parent.isExpanded() ) {
//      setVisible( true );
//    } else {
//      setVisible( false );
//    }
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
   * Creates a new child item in this item at the given index.
   *
   * @param item
   *            new child item
   * @param index
   *            index
   */
  void newItem( GridItem item, int index ) {
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

  /**
   * Sets whether this item has children.
   *
   * @param hasChildren
   *            true if this item has children
   */
  void setHasChildren( boolean hasChildren ) {
    this.hasChildren = hasChildren;
  }

  private void init() {
  }

}
