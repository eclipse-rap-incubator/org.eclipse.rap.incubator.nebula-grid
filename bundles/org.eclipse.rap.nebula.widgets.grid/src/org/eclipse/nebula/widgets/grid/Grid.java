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

import org.eclipse.nebula.widgets.grid.internal.IScrollBarProxy;
import org.eclipse.nebula.widgets.grid.internal.NullScrollBarProxy;
import org.eclipse.nebula.widgets.grid.internal.ScrollBarProxyAdapter;
import org.eclipse.swt.SWT;
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
