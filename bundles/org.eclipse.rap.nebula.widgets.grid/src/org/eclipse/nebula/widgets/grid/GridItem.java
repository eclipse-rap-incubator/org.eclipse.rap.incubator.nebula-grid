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
package org.eclipse.nebula.widgets.grid;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.nebula.widgets.grid.internal.IGridItemAdapter;
import org.eclipse.rap.rwt.internal.textsize.TextSizeUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.internal.SerializableCompatibility;
import org.eclipse.swt.internal.widgets.IWidgetColorAdapter;
import org.eclipse.swt.internal.widgets.IWidgetFontAdapter;
import org.eclipse.swt.internal.widgets.MarkupValidator;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Widget;


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

  private Grid parent;
  private GridItem parentItem;
  private ArrayList<Data> data;
  private ArrayList<GridItem> children = new ArrayList<GridItem>();
  private boolean hasChildren;
  private int level;
  private int customHeight = -1;
  private boolean visible = true;
  private boolean expanded;
  private boolean hasSetData;
  private Font defaultFont;
  private Color defaultBackground;
  private Color defaultForeground;
  private transient IGridItemAdapter gridItemAdapter;

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
    parent.setScrollValuesObsolete();
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
    this.parent.setScrollValuesObsolete();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void dispose() {
    if( !parent.isDisposing() && !isDisposed() ) {
      for( int i = 0; i < parent.getColumnCount(); i++ ) {
        Data itemData = getItemData( i );
        updateColumnImageCount( i, itemData.image, null );
        updateColumnTextCount( i, itemData.text, "" );
      }
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
    boolean unselected = false;
    for( Iterator itemIterator = children.iterator(); itemIterator.hasNext(); ) {
      GridItem item = ( GridItem )itemIterator.next();
      item.setVisible( expanded && visible );
      if( !expanded ) {
        if( parent.isSelected( item ) ) {
          parent.deselect( parent.indexOf( item ) );
          unselected = true;
        }
        if( deselectChildren( item ) ) {
          unselected = true;
        }
      }
    }
    parent.invalidateTopBottomIndex();
    parent.setScrollValuesObsolete();
    if( unselected ) {
      Event event = new Event();
      event.item = this;
      parent.notifyListeners( SWT.Selection, event );
    }
    if( parent.getFocusItem() != null && !parent.getFocusItem().isVisible() ) {
      parent.setFocusItem( this );
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
   * Sets the font that the receiver will use to paint textual information for
   * this item to the font specified by the argument, or to the default font
   * for that kind of control if the argument is null.
   *
   * @param font
   *            the new font (or null)
   * @throws IllegalArgumentException
   *             <ul>
   *             <li>ERROR_INVALID_ARGUMENT - if the argument has been
   *             disposed</li>
   *             </ul>
   * @throws org.eclipse.swt.SWTException
   *             <ul>
   *             <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
   *             </li>
   *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *             thread that created the receiver</li>
   *             </ul>
   */
  public void setFont( Font font ) {
    checkWidget();
    if( font != null && font.isDisposed() ) {
      SWT.error( SWT.ERROR_INVALID_ARGUMENT );
    }
    defaultFont = font;
    parent.redraw();
  }

  /**
   * Returns the font that the receiver will use to paint textual information
   * for this item.
   *
   * @return the receiver's font
   * @throws org.eclipse.swt.SWTException
   *             <ul>
   *             <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
   *             </li>
   *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *             thread that created the receiver</li>
   *             </ul>
   */
  public Font getFont() {
    checkWidget();
    return defaultFont == null ? parent.getFont() : defaultFont;
  }

  /**
   * Sets the font that the receiver will use to paint textual information for
   * the specified cell in this item to the font specified by the argument, or
   * to the default font for that kind of control if the argument is null.
   *
   * @param index
   *            the column index
   * @param font
   *            the new font (or null)
   * @throws IllegalArgumentException
   *             <ul>
   *             <li>ERROR_INVALID_ARGUMENT - if the argument has been
   *             disposed</li>
   *             </ul>
   * @throws org.eclipse.swt.SWTException
   *             <ul>
   *             <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
   *             </li>
   *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *             thread that created the receiver</li>
   *             </ul>
   */
  public void setFont( int index, Font font ) {
    checkWidget();
    if( font != null && font.isDisposed() ) {
      SWT.error( SWT.ERROR_INVALID_ARGUMENT );
    }
    getItemData( index ).font = font;
    parent.redraw();
  }

  /**
   * Returns the font that the receiver will use to paint textual information
   * for the specified cell in this item.
   *
   * @param index
   *            the column index
   * @return the receiver's font
   * @throws org.eclipse.swt.SWTException
   *             <ul>
   *             <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
   *             </li>
   *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *             thread that created the receiver</li>
   *             </ul>
   */
  public Font getFont( int index ) {
    checkWidget();
    handleVirtual();
    return internalGetFont( index );
  }

  /**
   * Sets the receiver's background color to the color specified by the
   * argument, or to the default system color for the item if the argument is
   * null.
   *
   * @param background
   *            the new color (or null)
   * @throws IllegalArgumentException
   *             <ul>
   *             <li>ERROR_INVALID_ARGUMENT - if the argument has been
   *             disposed</li>
   *             </ul>
   * @throws org.eclipse.swt.SWTException
   *             <ul>
   *             <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
   *             </li>
   *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *             thread that created the receiver</li>
   *             </ul>
   */
  public void setBackground( Color background ) {
    checkWidget();
    if( background != null && background.isDisposed() ) {
      SWT.error( SWT.ERROR_INVALID_ARGUMENT );
    }
    defaultBackground = background;
    parent.redraw();
  }

  /**
   * Returns the receiver's background color.
   *
   * @return the background color
   * @throws org.eclipse.swt.SWTException
   *             <ul>
   *             <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
   *             </li>
   *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *             thread that created the receiver</li>
   *             </ul>
   */
  public Color getBackground() {
    checkWidget();
    return defaultBackground == null ? parent.getBackground() : defaultBackground;
  }

  /**
   * Sets the background color at the given column index in the receiver to
   * the color specified by the argument, or to the default system color for
   * the item if the argument is null.
   *
   * @param index
   *            the column index
   * @param background
   *            the new color (or null)
   * @throws IllegalArgumentException
   *             <ul>
   *             <li>ERROR_INVALID_ARGUMENT - if the argument has been
   *             disposed</li>
   *             </ul>
   * @throws org.eclipse.swt.SWTException
   *             <ul>
   *             <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
   *             </li>
   *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *             thread that created the receiver</li>
   *             </ul>
   */
  public void setBackground( int index, Color background ) {
    checkWidget();
    if( background != null && background.isDisposed() ) {
      SWT.error( SWT.ERROR_INVALID_ARGUMENT );
    }
    getItemData( index ).background = background;
    parent.redraw();
  }

  /**
   * Returns the background color at the given column index in the receiver.
   *
   * @param index
   *            the column index
   * @return the background color
   * @throws org.eclipse.swt.SWTException
   *             <ul>
   *             <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
   *             </li>
   *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *             thread that created the receiver</li>
   *             </ul>
   */
  public Color getBackground( int index ) {
    checkWidget();
    handleVirtual();
    Color result = getItemData( index ).background;
     if( result == null ) {
       result = getBackground();
     }
    return result;
  }

  /**
   * Sets the receiver's foreground color to the color specified by the
   * argument, or to the default system color for the item if the argument is
   * null.
   *
   * @param foreground
   *            the new color (or null)
   * @throws IllegalArgumentException
   *             <ul>
   *             <li>ERROR_INVALID_ARGUMENT - if the argument has been
   *             disposed</li>
   *             </ul>
   * @throws org.eclipse.swt.SWTException
   *             <ul>
   *             <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
   *             </li>
   *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *             thread that created the receiver</li>
   *             </ul>
   */
  public void setForeground( Color foreground ) {
    checkWidget();
    if( foreground != null && foreground.isDisposed() ) {
      SWT.error( SWT.ERROR_INVALID_ARGUMENT );
    }
    defaultForeground = foreground;
    parent.redraw();
  }

  /**
   * Returns the foreground color that the receiver will use to draw.
   *
   * @return the receiver's foreground color
   * @throws org.eclipse.swt.SWTException
   *             <ul>
   *             <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
   *             </li>
   *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *             thread that created the receiver</li>
   *             </ul>
   */
  public Color getForeground() {
    checkWidget();
    return defaultForeground == null ? parent.getForeground() : defaultForeground;
  }

  /**
   * Sets the foreground color at the given column index in the receiver to
   * the color specified by the argument, or to the default system color for
   * the item if the argument is null.
   *
   * @param index
   *            the column index
   * @param foreground
   *            the new color (or null)
   * @throws IllegalArgumentException
   *             <ul>
   *             <li>ERROR_INVALID_ARGUMENT - if the argument has been
   *             disposed</li>
   *             </ul>
   * @throws org.eclipse.swt.SWTException
   *             <ul>
   *             <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
   *             </li>
   *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *             thread that created the receiver</li>
   *             </ul>
   */
  public void setForeground( int index, Color foreground ) {
    checkWidget();
    if( foreground != null && foreground.isDisposed() ) {
      SWT.error( SWT.ERROR_INVALID_ARGUMENT );
    }
    getItemData( index ).foreground = foreground;
    parent.redraw();
  }

  /**
   * Returns the foreground color at the given column index in the receiver.
   *
   * @param index
   *            the column index
   * @return the foreground color
   * @throws org.eclipse.swt.SWTException
   *             <ul>
   *             <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
   *             </li>
   *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *             thread that created the receiver</li>
   *             </ul>
   */
  public Color getForeground( int index ) {
    checkWidget();
    handleVirtual();
    Color result = getItemData( index ).foreground;
    if( result == null ) {
      result = getForeground();
    }
    return result;
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
    if( parent.markupEnabled && !parent.markupValidationDisabled ) {
      MarkupValidator.getInstance().validate( text );
    }
    Data itemData = getItemData( index );
    updateColumnTextCount( index, itemData.text, text );
    itemData.text = text;
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
   * Sets the tooltip for the given column index.
   *
   * @param index
   *            the column index
   * @param tooltip
   *            the tooltip text
   * @throws org.eclipse.swt.SWTException
   *             <ul>
   *             <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
   *             </li>
   *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *             thread that created the receiver</li>
   *             </ul>
   */
  public void setToolTipText( int index, String tooltip ) {
    checkWidget();
    getItemData( index ).tooltip = tooltip;
    if( tooltip != null && tooltip.length() > 0 ) {
      parent.setCellToolTipsEnabled( true );
    }
  }

  /**
   * Returns the tooltip for the given cell.
   *
   * @param index
   *            the column index
   * @return the tooltip
   * @throws org.eclipse.swt.SWTException
   *             <ul>
   *             <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
   *             </li>
   *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *             thread that created the receiver</li>
   *             </ul>
   */
  public String getToolTipText( int index ) {
    checkWidget();
    handleVirtual();
    return getItemData( index ).tooltip;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setImage( Image image ) {
    setImage( 0, image );
    parent.redraw();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Image getImage() {
    checkWidget();
    return getImage( 0 );
  }

  /**
   * Sets the receiver's image at a column.
   *
   * @param index
   *            the column index
   * @param image
   *            the new image
   * @throws IllegalArgumentException
   *             <ul>
   *             <li>ERROR_INVALID_ARGUMENT - if the image has been disposed</li>
   *             </ul>
   * @throws org.eclipse.swt.SWTException
   *             <ul>
   *             <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
   *             </li>
   *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *             thread that created the receiver</li>
   *             </ul>
   */
  public void setImage( int index, Image image ) {
    checkWidget();
    if( image != null && image.isDisposed() ) {
      SWT.error( SWT.ERROR_INVALID_ARGUMENT );
    }
    Data itemData = getItemData( index );
    updateColumnImageCount( index, itemData.image, image );
    itemData.image = image;
    parent.imageSetOnItem( index, this );
    parent.redraw();
  }

  /**
   * Returns the image stored at the given column index in the receiver, or
   * null if the image has not been set or if the column does not exist.
   *
   * @param index
   *            the column index
   * @return the image stored at the given column index in the receiver
   * @throws org.eclipse.swt.SWTException
   *             <ul>
   *             <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
   *             </li>
   *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *             thread that created the receiver</li>
   *             </ul>
   */
  public Image getImage( int index ) {
    checkWidget();
    handleVirtual();
    return getItemData( index ).image;
  }

  /**
   * Sets the checked state at the first column in the receiver.
   *
   * @param checked
   *            the new checked state
   * @throws org.eclipse.swt.SWTException
   *             <ul>
   *             <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
   *             </li>
   *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *             thread that created the receiver</li>
   *             </ul>
   */
  public void setChecked( boolean checked ) {
    checkWidget();
    setChecked( 0, checked );
    parent.redraw();
  }

  /**
   * Returns the checked state at the first column in the receiver.
   *
   * @return the checked state
   * @throws org.eclipse.swt.SWTException
   *             <ul>
   *             <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
   *             </li>
   *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *             thread that created the receiver</li>
   *             </ul>
   */
  public boolean getChecked() {
    checkWidget();
    return getChecked( 0 );
  }

  /**
   * Sets the checked state at the given column index in the receiver.
   *
   * @param index
   *            the column index
   * @param checked
   *            the new checked state
   * @throws org.eclipse.swt.SWTException
   *             <ul>
   *             <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
   *             </li>
   *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *             thread that created the receiver</li>
   *             </ul>
   */
  public void setChecked( int index, boolean checked ) {
    checkWidget();
    // [if] TODO: probably need a check for parent.getColumn( index ).isCheck() ?
    getItemData( index ).checked = checked;
    parent.redraw();
  }

  /**
   * Returns the checked state at the given column index in the receiver.
   *
   * @param index
   *            the column index
   * @return the checked state
   * @throws org.eclipse.swt.SWTException
   *             <ul>
   *             <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
   *             </li>
   *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *             thread that created the receiver</li>
   *             </ul>
   */
  public boolean getChecked( int index ) {
    checkWidget();
    handleVirtual();
    return getItemData( index ).checked;
  }

  /**
   * Sets the grayed state of the checkbox for the first column. This state
   * change only applies if the GridColumn was created with the SWT.CHECK
   * style.
   *
   * @param grayed
   *            the new grayed state of the checkbox;
   * @throws org.eclipse.swt.SWTException
   *             <ul>
   *             <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
   *             </li>
   *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *             thread that created the receiver</li>
   *             </ul>
   */
  public void setGrayed( boolean grayed ) {
    checkWidget();
    setGrayed( 0, grayed );
    parent.redraw();
  }

  /**
   * Returns <code>true</code> if the first column in the receiver is grayed,
   * and false otherwise. When the GridColumn does not have the
   * <code>CHECK</code> style, return false.
   *
   * @return the grayed state of the checkbox
   * @throws org.eclipse.swt.SWTException
   *             <ul>
   *             <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
   *             </li>
   *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *             thread that created the receiver</li>
   *             </ul>
   */
  public boolean getGrayed() {
    checkWidget();
    return getGrayed( 0 );
  }

  /**
   * Sets the grayed state of the checkbox for the given column index. This
   * state change only applies if the GridColumn was created with the
   * SWT.CHECK style.
   *
   * @param index
   *            the column index
   * @param grayed
   *            the new grayed state of the checkbox;
   * @throws org.eclipse.swt.SWTException
   *             <ul>
   *             <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
   *             </li>
   *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *             thread that created the receiver</li>
   *             </ul>
   */
  public void setGrayed( int index, boolean grayed ) {
    checkWidget();
    // [if] TODO: probably need a check for parent.getColumn( index ).isCheck() ?
    getItemData( index ).grayed = grayed;
    parent.redraw();
  }

  /**
   * Returns <code>true</code> if the column at the given index in the
   * receiver is grayed, and false otherwise. When the GridColumn does not
   * have the <code>CHECK</code> style, return false.
   *
   * @param index
   *            the column index
   * @return the grayed state of the checkbox
   * @throws org.eclipse.swt.SWTException
   *             <ul>
   *             <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
   *             </li>
   *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *             thread that created the receiver</li>
   *             </ul>
   */
  public boolean getGrayed( int index ) {
    checkWidget();
    handleVirtual();
    return getItemData( index ).grayed;
  }

  /**
   * Sets the height of this <code>GridItem</code>.
   *
   * @param height
   *            new height in pixels
   * @throws org.eclipse.swt.SWTException
   *             <ul>
   *             <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
   *             </li>
   *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *             thread that created the receiver</li>
   *             </ul>
   */
  public void setHeight( int height ) {
    checkWidget();
    if( height < 1 ) {
      SWT.error( SWT.ERROR_INVALID_ARGUMENT );
    }
    if( customHeight != height ) {
      customHeight = height;
      parent.hasDifferingHeights = true;
      parent.invalidateTopBottomIndex();
      parent.setScrollValuesObsolete();
    }
  }

  /**
   * Returns the height of this <code>GridItem</code>.
   *
   * @return height of this <code>GridItem</code>
   */
  public int getHeight() {
    checkWidget();
    return customHeight != -1 ? customHeight : parent.getItemHeight();
  }

  /**
   * Sets this <code>GridItem</code> to its preferred height.
   *
   * @throws org.eclipse.swt.SWTException
   *             <ul>
   *             <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
   *             </li>
   *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *             thread that created the receiver</li>
   *             </ul>
   */
  public void pack() {
    checkWidget();
    // [if] As different item heights are not supported, we only invalidate the cache here
    parent.layoutCache.invalidateItemHeight();
  }

  /**
   * Returns a rectangle describing the receiver's size and location relative
   * to its parent at a column in the table.
   *
   * @param columnIndex
   *            the index that specifies the column
   * @return the receiver's bounding column rectangle
   * @throws org.eclipse.swt.SWTException
   *             <ul>
   *             <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
   *             </li>
   *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *             thread that created the receiver</li>
   *             </ul>
   */
  public Rectangle getBounds( int columnIndex ) {
    checkWidget();
    // [if] -1000 is used in the original implementation
    Rectangle result = new Rectangle( -1000, -1000, 0, 0 );
    if( isVisible() && parent.isShown( this ) ) {
      Point origin = parent.getOrigin( parent.getColumn( columnIndex ), this );
      Point cellSize = getCellSize( columnIndex );
      result = new Rectangle( origin.x, origin.y, cellSize.x, cellSize.y );
    }
    return result;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T getAdapter( Class<T> adapter ) {
    T result;
    if(    adapter == IWidgetFontAdapter.class
        || adapter == IWidgetColorAdapter.class
        || adapter == IGridItemAdapter.class )
    {
      if( gridItemAdapter == null ) {
        gridItemAdapter = new GridItemAdapter();
      }
      result = ( T )gridItemAdapter;
    } else {
      result = super.getAdapter( adapter );
    }
    return result;
  }

  void setHasChildren( boolean hasChildren ) {
    this.hasChildren = hasChildren;
  }

  boolean isVisible() {
    return visible;
  }

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

  private void newItem( GridItem item, int index ) {
    setHasChildren( true );
    if( index == -1 ) {
      children.add( item );
    } else {
      children.add( index, item );
    }
  }

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

  void clear( boolean allChildren ) {
    for( int i = 0; i < parent.getColumnCount(); i++ ) {
      Data itemData = getItemData( i );
      updateColumnImageCount( i, itemData.image, null );
      updateColumnTextCount( i, itemData.text, "" );
    }
    init();
    defaultFont = null;
    defaultBackground = null;
    defaultForeground = null;
    hasSetData = false;
    // Recursively clear children if requested.
    if( allChildren ) {
      for( int i = children.size() - 1; i >= 0; i-- ) {
        children.get( i ).clear( true );
      }
    }
  }

  int getPreferredWidth( int index ) {
    int result = getIndentationWidth( index );
    result += getPaddingWidth( index );
    result += getCheckBoxWidth( index );
    result += getImageWidth( index );
    result += getSpacing( index );
    result += getTextWidth( index );
    return result;
  }

  private int getIndentationWidth( int index ) {
    int result = 0;
    if( parent.isTreeColumn( index ) ) {
      result = ( level + 1 ) * parent.getIndentationWidth();
    }
    return result;
  }

  private int getPaddingWidth( int index ) {
    int result = parent.getCellPadding().width;
    if( parent.isTreeColumn( index ) ) {
      result -= parent.getCellPadding().x;
    }
    return result;
  }

  private int getCheckBoxWidth( int index ) {
    return parent.getColumn( index ).isCheck() ? parent.getCheckBoxImageOuterSize().x : 0;
  }

  private int getImageWidth( int index ) {
    int result = 0;
    if( parent.hasColumnImages( index ) ) {
      result = parent.getItemImageSize().x;
    }
    return result;
  }

  private int getSpacing( int index ) {
    int result = 0;
    String text = getItemData( index ).text;
    if( parent.hasColumnImages( index ) && text.length() > 0 ) {
      result = parent.getCellSpacing();
    }
    return result;
  }

  private int getTextWidth( int index ) {
    int result = 0;
    String text = getItemData( index ).text;
    if( text.length() > 0 ) {
      result += TextSizeUtil.stringExtent( internalGetFont( index ), text ).x;
    }
    return result;
  }

  int getTextOffset( int index ) {
    int result = getIndentationWidth( index );
    if( !parent.isTreeColumn( index ) ) {
      result += parent.getCellPadding().x;
    }
    result += getCheckBoxWidth( index );
    result += getImageWidth( index );
    result += getSpacing( index );
    return result;
  }

  private Font internalGetFont( int index ) {
    Font result = getItemData( index ).font;
    if( result == null ) {
      result = getFont();
    }
    return result;
  }

  protected Point getCellSize( int index ) {
    int width = 0;
    int span = 0; // getColumnSpan( index );
    for( int i = 0; i <= span && i < parent.getColumnCount() - index; i++ ) {
      width += parent.getColumn( index + i ).getWidth();
    }
    GridItem item = this;
    int itemIndex = parent.indexOf( item );
    int height = getHeight();
    span = 0; // getRowSpan( index );
    for( int i = 1; i <= span && i < parent.getItemCount() - itemIndex; i++ ) {
      item = parent.getItem( itemIndex + i );
      if( item.isVisible() ) {
        height += item.getHeight();
      }
    }
    return new Point( width, height );
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

  private boolean deselectChildren( GridItem item ) {
    boolean flag = false;
    GridItem[] kids = item.getItems();
    for( int i = 0; i < kids.length; i++ ) {
      if( parent.isSelected( kids[ i ] ) ) {
        flag = true;
      }
      parent.deselect( parent.indexOf( kids[ i ] ) );
      if( deselectChildren( kids[ i ] ) ) {
        flag = true;
      }
    }
    return flag;
  }

  private void updateColumnImageCount( int index, Image oldImage, Image newImage ) {
    int delta = 0;
    if( oldImage == null && newImage != null ) {
      delta = +1;
    } else if( oldImage != null && newImage == null ) {
      delta = -1;
    }
    if( delta != 0 && index >= 0 && index < parent.getColumnCount() ) {
      parent.getColumn( index ).imageCount += delta;
    }
  }

  private void updateColumnTextCount( int index, String oldText, String newText ) {
    int delta = 0;
    if( oldText.length() == 0 && newText.length() > 0 ) {
      delta = +1;
    } else if( oldText.length() > 0 && newText.length() == 0 ) {
      delta = -1;
    }
    if( delta != 0 && index >= 0 && index < parent.getColumnCount() ) {
      parent.getColumn( index ).textCount += delta;
    }
  }

  ////////////////
  // Inner classes

  private static final class Data implements SerializableCompatibility {
    public Font font;
    public Color background;
    public Color foreground;
    public String text = "";
    public String tooltip;
    public Image image;
    public boolean checked;
    public boolean grayed;
  }

  private final class GridItemAdapter
    implements IGridItemAdapter, IWidgetFontAdapter, IWidgetColorAdapter
  {

    public boolean isParentDisposed() {
      Widget itemParent = parentItem == null ? parent : parentItem;
      return itemParent.isDisposed();
    }

    public Color getUserBackground() {
      return defaultBackground;
    }

    public Color getUserForeground() {
      return defaultForeground;
    }

    public Font getUserFont() {
      return defaultFont;
    }

    public Color[] getCellBackgrounds() {
      int columnCount = Math.max( 1, getParent().getColumnCount() );
      Color[] result = new Color[ columnCount ];
      for( int i = 0; i < columnCount; i++ ) {
        result[ i ] = getItemData( i ).background;
      }
      return result;
    }

    public Color[] getCellForegrounds() {
      int columnCount = Math.max( 1, getParent().getColumnCount() );
      Color[] result = new Color[ columnCount ];
      for( int i = 0; i < columnCount; i++ ) {
        result[ i ] = getItemData( i ).foreground;
      }
      return result;
    }

    public Font[] getCellFonts() {
      int columnCount = Math.max( 1, getParent().getColumnCount() );
      Font[] result = new Font[ columnCount ];
      for( int i = 0; i < columnCount; i++ ) {
        result[ i ] = getItemData( i ).font;
      }
      return result;
    }
  }

}
