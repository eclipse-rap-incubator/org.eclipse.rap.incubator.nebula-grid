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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Item;


/**
 * <p>
 * NOTE: THIS WIDGET AND ITS API ARE STILL UNDER DEVELOPMENT. THIS IS A
 * PRE-RELEASE ALPHA VERSION. USERS SHOULD EXPECT API CHANGES IN FUTURE
 * VERSIONS.
 * </p>
 * Instances of this class represent a column in a grid widget.
 * <p>
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>SWT.LEFT, SWT.RIGHT, SWT.CENTER, SWT.CHECK</dd>
 * <dt><b>Events:</b></dt>
 * <dd>Move, Resize, Selection, Show, Hide</dd>
 * </dl>
 */
public class GridColumn extends Item {

  /**
   * Default width of the column.
   */
  private static final int DEFAULT_WIDTH = 10;

  /**
   * Width of column.
   */
  private int width = DEFAULT_WIDTH;

  private int minimumWidth = 0;

  /**
   * Parent table.
   */
  private Grid parent;

  /**
   * Sort style of column. Only used to draw indicator, does not actually sort
   * data.
   */
  private int sortStyle = SWT.NONE;

  /**
   * Does this column contain check boxes? Did the user specify SWT.CHECK in
   * the constructor of the column.
   */
  private boolean check = false;

  /**
   * Specifies if this column should display a checkbox because SWT.CHECK was
   * passed to the parent table (not necessarily the column).
   */
  private boolean tableCheck = false;

  /**
   * Determines if this column shows toggles.
   */
  private boolean tree = false;

  /**
   * Is this column moveable?
   */
  private boolean moveable = false;

  /**
   * Is this column resizable?
   */
  private boolean resizeable = true;

  private boolean checkable = true;

  private boolean visible = true;

  private int alignment = SWT.LEFT;

  private Font headerFont;

  /**
   * Constructs a new instance of this class given its parent (which must be a
   * <code>Grid</code>) and a style value describing its behavior and
   * appearance. The item is added to the end of the items maintained by its
   * parent.
   *
   * @param parent
   *            an Grid control which will be the parent of the new instance
   *            (cannot be null)
   * @param style
   *            the style of control to construct
   * @throws IllegalArgumentException
   *             <ul>
   *             <li>ERROR_NULL_ARGUMENT - if the parent is null</li>
   *             </ul>
   * @throws org.eclipse.swt.SWTException
   *             <ul>
   *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *             thread that created the parent</li>
   *             <li>
   *             ERROR_INVALID_SUBCLASS - if this class is not an allowed
   *             subclass</li>
   *             </ul>
   */
  public GridColumn( Grid parent, int style ) {
    this( parent, style, -1 );
  }

  /**
   * Constructs a new instance of this class given its parent (which must be a
   * <code>Grid</code>), a style value describing its behavior and appearance,
   * and the index at which to place it in the items maintained by its parent.
   *
   * @param parent
   *            an Grid control which will be the parent of the new instance
   *            (cannot be null)
   * @param style
   *            the style of control to construct
   * @param index
   *            the index to store the receiver in its parent
   * @throws IllegalArgumentException
   *             <ul>
   *             <li>ERROR_NULL_ARGUMENT - if the parent is null</li>
   *             </ul>
   * @throws org.eclipse.swt.SWTException
   *             <ul>
   *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *             thread that created the parent</li>
   *             <li>
   *             ERROR_INVALID_SUBCLASS - if this class is not an allowed
   *             subclass</li>
   *             </ul>
   */
  public GridColumn( Grid parent, int style, int index ) {
    super( parent, style, index );
    init( parent, style, index );
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void dispose() {
    if( !parent.isDisposing() && !isDisposed() ) {
      parent.removeColumn( this );
    }
    super.dispose();
  }

  /**
   * Returns the parent grid.
   *
   * @return the parent grid.
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
   * Adds the listener to the collection of listeners who will be notified
   * when the receiver's is pushed, by sending it one of the messages defined
   * in the <code>SelectionListener</code> interface.
   *
   * @param listener
   *            the listener which should be notified
   * @throws IllegalArgumentException
   *             <ul>
   *             <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
   *             </ul>
   * @throws org.eclipse.swt.SWTException
   *             <ul>
   *             <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
   *             </li>
   *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *             thread that created the receiver</li>
   *             </ul>
   */
  public void addSelectionListener( SelectionListener listener ) {
    checkWidget();
    SelectionEvent.addListener( this, listener );
  }

  /**
   * Removes the listener from the collection of listeners who will be
   * notified when the receiver's selection changes.
   *
   * @param listener
   *            the listener which should no longer be notified
   * @see SelectionListener
   * @see #addSelectionListener(SelectionListener)
   * @throws org.eclipse.swt.SWTException
   *             <ul>
   *             <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
   *             </li>
   *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *             thread that created the receiver</li>
   *             </ul>
   */
  public void removeSelectionListener( SelectionListener listener ) {
    checkWidget();
    SelectionEvent.removeListener( this, listener );
  }

  /**
   * Adds a listener to the list of listeners notified when the column is
   * moved or resized.
   *
   * @param listener
   *            listener
   * @throws IllegalArgumentException
   *             <ul>
   *             <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
   *             </ul>
   * @throws org.eclipse.swt.SWTException
   *             <ul>
   *             <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
   *             </li>
   *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *             thread that created the receiver</li>
   *             </ul>
   */
  public void addControlListener( ControlListener listener ) {
    checkWidget();
    ControlEvent.addListener( this, listener );
  }

  /**
   * Removes the given control listener.
   *
   * @param listener
   *            listener.
   * @throws IllegalArgumentException
   *             <ul>
   *             <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
   *             </ul>
   * @throws org.eclipse.swt.SWTException
   *             <ul>
   *             <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
   *             </li>
   *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *             thread that created the receiver</li>
   *             </ul>
   */
  public void removeControlListener( ControlListener listener ) {
    checkWidget();
    ControlEvent.removeListener( this, listener );
  }

  /**
   * Sets the width of the column.
   *
   * @param width
   *            new width
   * @throws org.eclipse.swt.SWTException
   *             <ul>
   *             <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
   *             </li>
   *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *             thread that created the receiver</li>
   *             </ul>
   */
  public void setWidth( int width ) {
    checkWidget();
    setWidth( width, true );
  }

  /**
   * Returns the width of the column.
   *
   * @return width of column
   * @throws org.eclipse.swt.SWTException
   *             <ul>
   *             <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
   *             </li>
   *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *             thread that created the receiver</li>
   *             </ul>
   */
  public int getWidth() {
    checkWidget();
    return width;
  }

  /**
   * Set the minimum width of the column
   *
   * @param minimumWidth
   *            the minimum width
   */
  public void setMinimumWidth( int minimumWidth ) {
    checkWidget();
    this.minimumWidth = Math.max( 0, minimumWidth );
    if( minimumWidth > width ) {
      setWidth( minimumWidth, true );
    }
  }

  /**
   * @return the minimum width
   */
  public int getMinimumWidth() {
    checkWidget();
    return minimumWidth;
  }

  @Override
  public void setText( String text ) {
    super.setText( text );
    parent.layoutCache.invalidateHeaderHeight();
  }

  @Override
  public void setImage( Image image ) {
    super.setImage( image );
    parent.layoutCache.invalidateHeaderHeight();
  }

  /**
   * Sets the sort indicator style for the column. This method does not actual
   * sort the data in the table. Valid values include: SWT.UP, SWT.DOWN,
   * SWT.NONE.
   *
   * @param style
   *            SWT.UP, SWT.DOWN, SWT.NONE
   * @throws org.eclipse.swt.SWTException
   *             <ul>
   *             <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
   *             </li>
   *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *             thread that created the receiver</li>
   *             </ul>
   */
  public void setSort( int style ) {
    checkWidget();
    sortStyle = style;
    parent.redraw();
  }

  /**
   * Returns the sort indicator value.
   *
   * @return SWT.UP, SWT.DOWN, SWT.NONE
   * @throws org.eclipse.swt.SWTException
   *             <ul>
   *             <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
   *             </li>
   *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *             thread that created the receiver</li>
   *             </ul>
   */
  public int getSort() {
    checkWidget();
    return sortStyle;
  }

  /**
   * Sets the column moveable or fixed.
   *
   * @param moveable
   *            true to enable column moving
   * @throws org.eclipse.swt.SWTException
   *             <ul>
   *             <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
   *             </li>
   *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *             thread that created the receiver</li>
   *             </ul>
   */
  public void setMoveable( boolean moveable ) {
    checkWidget();
    this.moveable = moveable;
    parent.redraw();
  }

  /**
   * Returns true if this column is moveable.
   *
   * @return true if moveable.
   * @throws org.eclipse.swt.SWTException
   *             <ul>
   *             <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
   *             </li>
   *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *             thread that created the receiver</li>
   *             </ul>
   */
  public boolean getMoveable() {
    checkWidget();
    return moveable;
  }

  /**
   * Sets the column resizeable.
   *
   * @param resizeable
   *            true to make the column resizeable
   * @throws org.eclipse.swt.SWTException
   *             <ul>
   *             <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
   *             </li>
   *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *             thread that created the receiver</li>
   *             </ul>
   */
  public void setResizeable( boolean resizeable ) {
    checkWidget();
    this.resizeable = resizeable;
  }

  /**
   * Returns true if the column is resizeable.
   *
   * @return true if the column is resizeable.
   * @throws org.eclipse.swt.SWTException
   *             <ul>
   *             <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
   *             </li>
   *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *             thread that created the receiver</li>
   *             </ul>
   */
  public boolean getResizeable() {
    checkWidget();
    return resizeable;
  }

  /**
   * Sets the column's visibility.
   *
   * @param visible
   *            the visible to set
   * @throws org.eclipse.swt.SWTException
   *             <ul>
   *             <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
   *             </li>
   *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *             thread that created the receiver</li>
   *             </ul>
   */
  public void setVisible( boolean visible ) {
    checkWidget();
    boolean before = isVisible();
    this.visible = visible;
    if( isVisible() != before ) {
      if( visible ) {
        notifyListeners( SWT.Show, new Event() );
      } else {
        notifyListeners( SWT.Hide, new Event() );
      }
      GridColumn[] orderedColumns = parent.getColumnsInOrder();
      boolean fire = false;
      for( int i = 0; i < orderedColumns.length; i++ ) {
        GridColumn column = orderedColumns[ i ];
        if( column == this ) {
          fire = true;
        } else if( fire && column.isVisible() ) {
          column.fireMoved();
        }
      }
      parent.redraw();
    }
  }

  /**
   * Returns the visibility state as set with {@code setVisible}.
   *
   * @return the visible
   * @throws org.eclipse.swt.SWTException
   *             <ul>
   *             <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
   *             </li>
   *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *             thread that created the receiver</li>
   *             </ul>
   */
  public boolean getVisible() {
    checkWidget();
    return visible;
  }

  /**
   * Returns true if the column is visible, false otherwise. If the column is
   * in a group and the group is not expanded and this is a detail column,
   * returns false (and vice versa).
   *
   * @return true if visible, false otherwise
   *
   * @throws org.eclipse.swt.SWTException
   *             <ul>
   *             <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
   *             </li>
   *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *             thread that created the receiver</li>
   *             </ul>
   */
  public boolean isVisible() {
    checkWidget();
    boolean result = visible;
//    if( group != null ) {
//      if( ( group.getExpanded() && !isDetail() ) || ( !group.getExpanded() && !isSummary() ) ) {
//        result = false;
//      }
//    }
    return result;
  }

  /**
   * Returns true if the column includes a check box.
   *
   * @return true if the column includes a check box.
   * @throws org.eclipse.swt.SWTException
   *             <ul>
   *             <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
   *             </li>
   *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *             thread that created the receiver</li>
   *             </ul>
   */
  public boolean isCheck() {
    checkWidget();
    return check || tableCheck;
  }

  /**
   * Sets the checkable state. If false the checkboxes in the column cannot be
   * checked.
   *
   * @param checkable
   *            the new checkable state.
   * @throws org.eclipse.swt.SWTException
   *             <ul>
   *             <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
   *             </li>
   *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *             thread that created the receiver</li>
   *             </ul>
   */
  public void setCheckable( boolean checkable ) {
    checkWidget();
    this.checkable = checkable;
  }

  /**
   * Returns the checkable state. If false the checkboxes in the column cannot
   * be checked.
   *
   * @return true if the column is checkable (only applicable when style is
   *         SWT.CHECK).
   * @throws org.eclipse.swt.SWTException
   *             <ul>
   *             <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
   *             </li>
   *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *             thread that created the receiver</li>
   *             </ul>
   */
  public boolean getCheckable() {
    checkWidget();
    return checkable;
  }

  /**
   * Adds or removes the columns tree toggle.
   *
   * @param tree
   *            true to add toggle.
   * @throws org.eclipse.swt.SWTException
   *             <ul>
   *             <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
   *             </li>
   *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *             thread that created the receiver</li>
   *             </ul>
   */
  public void setTree( boolean tree ) {
    checkWidget();
    this.tree = tree;
    parent.redraw();
  }

  /**
   * Returns true if this column includes a tree toggle.
   *
   * @return true if the column includes the tree toggle.
   * @throws org.eclipse.swt.SWTException
   *             <ul>
   *             <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
   *             </li>
   *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *             thread that created the receiver</li>
   *             </ul>
   */
  public boolean isTree() {
    checkWidget();
    return tree;
  }

  /**
   * Sets the column alignment.
   *
   * @param alignment
   *            SWT.LEFT, SWT.RIGHT, SWT.CENTER
   * @throws org.eclipse.swt.SWTException
   *             <ul>
   *             <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
   *             </li>
   *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *             thread that created the receiver</li>
   *             </ul>
   */
  public void setAlignment( int alignment ) {
    checkWidget();
    if( alignment == SWT.LEFT || alignment == SWT.CENTER || alignment == SWT.RIGHT ) {
      this.alignment = alignment;
    }
  }

  /**
   * Returns the column alignment.
   *
   * @return SWT.LEFT, SWT.RIGHT, SWT.CENTER
   * @throws org.eclipse.swt.SWTException
   *             <ul>
   *             <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
   *             </li>
   *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *             thread that created the receiver</li>
   *             </ul>
   */
  public int getAlignment() {
    checkWidget();
    return alignment;
  }

  /**
   * Sets the Font to be used when displaying the Header text.
   *
   * @param font
   *            the new header font (or null)
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
  public void setHeaderFont( Font font ) {
    checkWidget();
    if( font != null && font.isDisposed() ) {
      SWT.error( SWT.ERROR_INVALID_ARGUMENT );
    }
    parent.layoutCache.invalidateHeaderHeight();
    headerFont = font;
  }

  /**
   * Returns the font that the receiver will use to paint textual information
   * for the header.
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
  public Font getHeaderFont() {
    checkWidget();
    return headerFont == null ? parent.getFont() : headerFont;
  }

  void setWidth( int width, boolean redraw ) {
    this.width = Math.max( minimumWidth, width );
    if( redraw ) {
      parent.setScrollValuesObsolete();
      parent.redraw();
    }
  }

  void fireMoved() {
    Event event = new Event();
    event.display = this.getDisplay();
    event.item = this;
    event.widget = parent;
    notifyListeners( SWT.Move, event );
  }

  protected boolean isTableCheck() {
    return tableCheck;
  }

  protected void setTableCheck( boolean tableCheck ) {
    this.tableCheck = tableCheck;
  }

  private void init( Grid parent, int style, int index ) {
    this.parent = parent;
    if( ( style & SWT.CHECK ) == SWT.CHECK ) {
      check = true;
    }
    if( ( style & SWT.RIGHT ) == SWT.RIGHT ) {
      alignment = SWT.RIGHT;
    }
    if( ( style & SWT.CENTER ) == SWT.CENTER ) {
      alignment = SWT.CENTER;
    }
    parent.newColumn( this, index );
  }
}
