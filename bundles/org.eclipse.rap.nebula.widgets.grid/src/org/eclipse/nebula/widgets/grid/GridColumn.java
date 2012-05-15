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
   * Parent table.
   */
  private Grid parent;

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

  private boolean checkable = true;

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
    if( !parent.isDisposing() ) {
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

  private void init( Grid parent, int style, int index ) {
    this.parent = parent;
    parent.newColumn( this, index );
    if( ( style & SWT.CHECK ) == SWT.CHECK ) {
      check = true;
    }
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

  protected boolean isTableCheck() {
    return tableCheck;
  }

  protected void setTableCheck( boolean tableCheck ) {
    this.tableCheck = tableCheck;
  }
}
