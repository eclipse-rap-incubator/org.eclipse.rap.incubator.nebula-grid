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
import java.util.Arrays;
import java.util.List;

import org.eclipse.nebula.widgets.grid.internal.NullScrollBarProxy;
import org.eclipse.nebula.widgets.grid.internal.ScrollBarProxyAdapter;
import org.eclipse.rap.rwt.testfixture.Fixture;
import org.eclipse.rwt.lifecycle.PhaseId;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import junit.framework.TestCase;


public class Grid_Test extends TestCase {

  private Display display;
  private Shell shell;
  private Grid grid;

  @Override
  protected void setUp() {
    Fixture.setUp();
    Fixture.fakePhase( PhaseId.PROCESS_ACTION );
    display = new Display();
    shell = new Shell( display );
    grid = new Grid( shell, SWT.H_SCROLL | SWT.V_SCROLL );
  }

  @Override
  protected void tearDown() {
    Fixture.tearDown();
  }

  public void testGridCreation() {
    grid = new Grid( shell, SWT.NONE );
    assertNotNull( grid );
    assertTrue( grid.getHorizontalScrollBarProxy() instanceof NullScrollBarProxy );
    assertNull( grid.getHorizontalBar() );
    assertTrue( grid.getVerticalScrollBarProxy() instanceof NullScrollBarProxy );
    assertNull( grid.getVerticalBar() );
    assertEquals( 0, grid.getRootItemCount() );
  }

  public void testGridCreationWithScrollBars() {
    grid = new Grid( shell, SWT.H_SCROLL | SWT.V_SCROLL );
    assertTrue( grid.getHorizontalScrollBarProxy() instanceof ScrollBarProxyAdapter );
    assertFalse( grid.getHorizontalBar().isVisible() );
    assertTrue( grid.getVerticalScrollBarProxy() instanceof ScrollBarProxyAdapter );
    assertFalse( grid.getVerticalBar().isVisible() );
  }

  public void testStyle() {
    Grid grid = new Grid( shell, SWT.NONE );
    assertTrue( ( grid.getStyle() & SWT.DOUBLE_BUFFERED ) != 0 );

    grid = new Grid( shell, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER );
    assertTrue( ( grid.getStyle() & SWT.H_SCROLL ) != 0 );
    assertTrue( ( grid.getStyle() & SWT.V_SCROLL ) != 0 );
    assertTrue( ( grid.getStyle() & SWT.BORDER ) != 0 );

    grid = new Grid( shell, SWT.SINGLE | SWT.MULTI );
    assertTrue( ( grid.getStyle() & SWT.SINGLE ) != 0 );
    assertTrue( ( grid.getStyle() & SWT.MULTI ) != 0 );

    grid = new Grid( shell, SWT.VIRTUAL | SWT.CHECK );
    assertTrue( ( grid.getStyle() & SWT.VIRTUAL ) != 0 );
    assertTrue( ( grid.getStyle() & SWT.CHECK ) != 0 );
  }

  public void testGetRootItemCount() {
    createGridItems( grid, 5, 1 );

    assertEquals( 5, grid.getRootItemCount() );
  }

  public void testGetItemCount() {
    createGridItems( grid, 5, 1 );

    assertEquals( 10, grid.getItemCount() );
  }

  public void testGetRootItems() {
    GridItem[] createdItems = createGridItems( grid, 3, 1 );

    GridItem[] rootItems = grid.getRootItems();
    assertSame( createdItems[ 0 ], rootItems[ 0 ] );
    assertSame( createdItems[ 2 ], rootItems[ 1 ] );
    assertSame( createdItems[ 4 ], rootItems[ 2 ] );
  }

  public void testGetItems() {
    GridItem[] createdItems = createGridItems( grid, 3, 1 );

    GridItem[] items = grid.getItems();
    assertTrue( Arrays.equals( createdItems, items ) );
  }

  public void testGetRootItem() {
    GridItem[] createdItems = createGridItems( grid, 3, 1 );

    assertSame( createdItems[ 2 ], grid.getRootItem( 1 ) );
    assertSame( createdItems[ 4 ], grid.getRootItem( 2 ) );
  }

  public void testGetRootItem_InvalidIndex() {
    createGridItems( grid, 3, 1 );

    try {
      grid.getRootItem( 10 );
      fail();
    } catch( IllegalArgumentException expected ) {
    }
  }

  public void testGetItem() {
    GridItem[] createdItems = createGridItems( grid, 3, 1 );

    assertSame( createdItems[ 1 ], grid.getItem( 1 ) );
    assertSame( createdItems[ 4 ], grid.getItem( 4 ) );
  }

  public void testGetItem_InvalidIndex() {
    createGridItems( grid, 3, 1 );

    try {
      grid.getItem( 10 );
      fail();
    } catch( IllegalArgumentException expected ) {
    }
  }

  public void testIndexOf() {
    GridItem[] createdItems = createGridItems( grid, 3, 1 );

    assertEquals( 1, grid.indexOf( createdItems[ 1 ] ) );
    assertEquals( 4, grid.indexOf( createdItems[ 4 ] ) );
  }

  public void testIndexOf_NullArgument() {
    try {
      grid.indexOf( null );
      fail();
    } catch( IllegalArgumentException expected ) {
    }
  }

  public void testIndexOf_DifferentParent() {
    Grid otherGrid = new Grid( shell, SWT.NONE );
    GridItem item = new GridItem( otherGrid, SWT.NONE );

    assertEquals( -1, grid.indexOf( item ) );
  }

  public void testDispose() {
    grid.dispose();

    assertTrue( grid.isDisposing() );
    assertTrue( grid.isDisposed() );
  }

  public void testDispose_WithItems() {
    GridItem[] items = createGridItems( grid, 1, 1 );

    grid.dispose();

    assertTrue( items[ 0 ].isDisposed() );
    assertTrue( items[ 1 ].isDisposed() );
  }

  public void testSendDisposeEvent() {
    final List<DisposeEvent> log = new ArrayList<DisposeEvent>();
    grid.addDisposeListener( new DisposeListener() {
      public void widgetDisposed( DisposeEvent event ) {
        log.add( event );
      }
    } );

    grid.dispose();

    assertEquals( 1, log.size() );
    assertSame(grid, log.get( 0 ).widget );
  }

  //////////////////
  // Helping methods

  private static GridItem[] createGridItems( Grid grid, int rootItems, int childItems ) {
    GridItem[] result = new GridItem[ rootItems * ( childItems + 1 ) ];
    int counter = 0;
    for( int i = 0; i < rootItems; i++ ) {
      GridItem rootItem = new GridItem( grid, SWT.NONE );
      result[ counter ] = rootItem;
      counter++;
      for( int j = 0; j < childItems; j++ ) {
        GridItem childItem = new GridItem( rootItem, SWT.NONE );
        result[ counter ] = childItem;
        counter++;
      }
    }
    return result;
  }
}
