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

import org.eclipse.rap.rwt.testfixture.Fixture;
import org.eclipse.rwt.lifecycle.PhaseId;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import junit.framework.TestCase;


public class GridItem_Test extends TestCase {

  private Display display;
  private Shell shell;
  private Grid grid;

  @Override
  protected void setUp() throws Exception {
    Fixture.setUp();
    Fixture.fakePhase( PhaseId.PROCESS_ACTION );
    display = new Display();
    shell = new Shell( display );
    grid = new Grid( shell, SWT.H_SCROLL | SWT.V_SCROLL );
  }

  @Override
  protected void tearDown() throws Exception {
    Fixture.tearDown();
  }

  public void testGridItemCreation_GridParent() {
    GridItem item = new GridItem( grid, SWT.NONE );

    assertSame( grid, item.getParent() );
    assertSame( item, grid.getItem( 0 ) );
    assertSame( item, grid.getRootItem( 0 ) );
    assertNull( item.getParentItem() );
    assertEquals( 0, grid.indexOf( item ) );
    assertEquals( 1, grid.getItemCount() );
    assertEquals( 1, grid.getRootItemCount() );
  }

  public void testGridItemCreation_GridItemParent() {
    GridItem parentItem = new GridItem( grid, SWT.NONE );

    GridItem item = new GridItem( parentItem, SWT.NONE );

    assertSame( grid, item.getParent() );
    assertSame( item, grid.getItem( 1 ) );
    assertSame( parentItem, item.getParentItem() );
    assertEquals( 1, grid.indexOf( item ) );
    assertEquals( 2, grid.getItemCount() );
    assertEquals( 1, grid.getRootItemCount() );
    assertEquals( 1, parentItem.getItemCount() );
  }

  public void testGridItemCreation_AtIndexWithGridParent() {
    createGridItems( grid, 5, 5 );

    GridItem item = new GridItem( grid, SWT.NONE, 2 );

    assertSame( item, grid.getItem( 12 ) );
    assertSame( item, grid.getRootItem( 2 ) );
    assertEquals( 12, grid.indexOf( item ) );
    assertEquals( 31, grid.getItemCount() );
    assertEquals( 6, grid.getRootItemCount() );
  }

  public void testGridItemCreation_AtIndexWithGridItemParent() {
    createGridItems( grid, 5, 5 );
    GridItem parentItem = grid.getItem( 6 );

    GridItem item = new GridItem( parentItem, SWT.NONE, 2 );

    assertSame( item, grid.getItem( 9 ) );
    assertEquals( 9, grid.indexOf( item ) );
    assertEquals( 31, grid.getItemCount() );
    assertEquals( 5, grid.getRootItemCount() );
    assertEquals( 6, parentItem.getItemCount() );
  }

  public void testGetItemCount() {
    createGridItems( grid, 1, 10 );

    assertEquals( 10, grid.getItem( 0 ).getItemCount() );
  }

  public void testGetItemCount_AfterDispose() {
    GridItem[] items = createGridItems( grid, 1, 10 );

    items[ 5 ].dispose();

    assertEquals( 10, grid.getItemCount() );
    assertEquals( 9, grid.getItem( 0 ).getItemCount() );
  }

  public void testGetItems() {
    GridItem[] items = createGridItems( grid, 1, 10 );
    GridItem[] expected = new GridItem[ 10 ];
    System.arraycopy( items, 1, expected, 0, 10 );

    assertTrue( Arrays.equals( expected, items[ 0 ].getItems() ) );
  }

  public void testGetItem() {
    GridItem[] items = createGridItems( grid, 1, 10 );

    assertSame( items[ 5 ], items[ 0 ].getItem( 4 ) );
  }

  public void testGetItem_InvalidIndex() {
    GridItem[] items = createGridItems( grid, 1, 10 );

    try {
      items[ 0 ].getItem( 100 );
      fail();
    } catch( IndexOutOfBoundsException expected ) {
    }
  }

  public void testIndexOf() {
    GridItem[] items = createGridItems( grid, 1, 10 );

    assertEquals( 5, items[ 0 ].indexOf( items[ 6 ] ) );
  }

  public void testIndexOf_AfterDispose() {
    GridItem[] items = createGridItems( grid, 1, 10 );

    items[ 2 ].dispose();

    assertEquals( 4, items[ 0 ].indexOf( items[ 6 ] ) );
  }

  public void testIndexOf_NullArgument() {
    GridItem[] items = createGridItems( grid, 1, 10 );

    try {
      items[ 0 ].indexOf( null );
      fail();
    } catch( IllegalArgumentException expected ) {
    }
  }

  public void testIndexOf_WithDisposedItem() {
    GridItem[] items = createGridItems( grid, 1, 10 );
    items[ 6 ].dispose();

    try {
      items[ 0 ].indexOf( items[ 6 ] );
      fail();
    } catch( IllegalArgumentException expected ) {
    }
  }

  public void testHasChildren() {
    GridItem[] items = createGridItems( grid, 1, 10 );

    assertTrue( items[ 0 ].hasChildren() );
  }

  public void testHasChildren_NoChildren() {
    GridItem[] items = createGridItems( grid, 1, 0 );

    assertFalse( items[ 0 ].hasChildren() );
  }

  public void testHasChildren_AfterItemRemove() {
    GridItem[] items = createGridItems( grid, 1, 1 );

    items[ 1 ].dispose();

    assertFalse( items[ 0 ].hasChildren() );
  }

  public void testDispose() {
    GridItem[] items = createGridItems( grid, 1, 1 );

    items[ 0 ].dispose();

    assertTrue( items[ 0 ].isDisposed() );
    assertTrue( items[ 1 ].isDisposed() );
    assertEquals( 0, grid.getItemCount() );
    assertEquals( 0, grid.getRootItemCount() );
  }

  public void testSendDisposeEvent() {
    final List<DisposeEvent> log = new ArrayList<DisposeEvent>();
    GridItem[] items = createGridItems( grid, 1, 1 );
    items[ 0 ].addDisposeListener( new DisposeListener() {
      public void widgetDisposed( DisposeEvent event ) {
        log.add( event );
      }
    } );

    items[ 0 ].dispose();

    assertEquals( 1, log.size() );
    assertSame( items[ 0 ], log.get( 0 ).widget );
  }

  public void testSendDisposeEventOnGridDispose() {
    final List<DisposeEvent> log = new ArrayList<DisposeEvent>();
    GridItem[] items = createGridItems( grid, 1, 1 );
    items[ 0 ].addDisposeListener( new DisposeListener() {
      public void widgetDisposed( DisposeEvent event ) {
        log.add( event );
      }
    } );

    grid.dispose();

    assertEquals( 1, log.size() );
    assertSame( items[ 0 ], log.get( 0 ).widget );
  }

  public void testGetLevel() {
    GridItem[] items = createGridItems( grid, 1, 1 );

    assertEquals( 0, items[ 0 ].getLevel() );
    assertEquals( 1, items[ 1 ].getLevel() );
  }

  public void testSetExpanded() {
    GridItem[] items = createGridItems( grid, 1, 1 );

    assertFalse( items[ 0 ].isExpanded() );
    assertFalse( items[ 1 ].isVisible() );

    items[ 0 ].setExpanded( true );

    assertTrue( items[ 0 ].isExpanded() );
    assertTrue( items[ 1 ].isVisible() );
  }

  public void testIsVisibleOnCreation() {
    GridItem[] items = createGridItems( grid, 1, 1 );
    items[ 0 ].setExpanded( true );

    new GridItem( items[ 0 ], SWT.NONE );

    assertTrue( items[ 1 ].isVisible() );
  }

  public void testFireEvent() {
    final List<Event> log = new ArrayList<Event>();
    GridItem item = new GridItem( grid, SWT.NONE );
    grid.addListener( SWT.Expand, new Listener() {
      public void handleEvent( Event event ) {
        log.add( event );
      }
    } );

    item.fireEvent( SWT.Expand );

    assertEquals( 1, log.size() );
    Event event = log.get( 0 );
    assertSame( item.getDisplay(), event.display );
    assertSame( grid, event.widget );
    assertSame( item, event.item );
  }

  public void testFireCheckEvent() {
    final List<Event> log = new ArrayList<Event>();
    GridItem item = new GridItem( grid, SWT.NONE );
    grid.addListener( SWT.Selection, new Listener() {
      public void handleEvent( Event event ) {
        log.add( event );
      }
    } );

    item.fireCheckEvent( 3 );

    assertEquals( 1, log.size() );
    Event event = log.get( 0 );
    assertSame( item.getDisplay(), event.display );
    assertSame( grid, event.widget );
    assertSame( item, event.item );
    assertEquals( SWT.CHECK, event.detail );
    // FIXME: As untyped event is wrapped around typed SelectionEvent this field is lost
    // assertEquals( 3, event.index );
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
