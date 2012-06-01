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

import static org.eclipse.nebula.widgets.grid.GridTestUtil.createGridColumns;
import static org.eclipse.nebula.widgets.grid.GridTestUtil.createGridItems;
import static org.eclipse.nebula.widgets.grid.GridTestUtil.loadImage;

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
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TreeAdapter;
import org.eclipse.swt.events.TreeEvent;
import org.eclipse.swt.events.TreeListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Shell;
import junit.framework.TestCase;


public class Grid_Test extends TestCase {

  private Display display;
  private Shell shell;
  private Grid grid;
  private ScrollBar verticalBar;
  private ScrollBar horizontalBar;
  private List<Event> eventLog;

  @Override
  protected void setUp() {
    Fixture.setUp();
    Fixture.fakePhase( PhaseId.PROCESS_ACTION );
    display = new Display();
    shell = new Shell( display );
    grid = new Grid( shell, SWT.H_SCROLL | SWT.V_SCROLL );
    grid.setSize( 200, 200 );
    verticalBar = grid.getVerticalBar();
    horizontalBar = grid.getHorizontalBar();
    eventLog = new ArrayList<Event>();
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

  public void testSetItemCount_MoreItems() {
    createGridItems( grid, 3, 3 );

    grid.setItemCount( 15 );

    assertEquals( 15, grid.getItemCount() );
    assertEquals( 6, grid.getRootItemCount() );
  }

  public void testSetItemCount_LessItems() {
    GridItem[] items = createGridItems( grid, 3, 3 );

    grid.setItemCount( 6 );

    assertEquals( 6, grid.getItemCount() );
    assertEquals( 2, grid.getRootItemCount() );
    assertEquals( 1, items[ 4 ].getItemCount() );
  }

  public void testSetItemCount_NoChange() {
    GridItem[] items = createGridItems( grid, 3, 3 );

    grid.setItemCount( 12 );

    assertTrue( Arrays.equals( items, grid.getItems() ) );
  }

  public void testGetRootItems() {
    GridItem[] items = createGridItems( grid, 3, 1 );

    GridItem[] rootItems = grid.getRootItems();
    assertSame( items[ 0 ], rootItems[ 0 ] );
    assertSame( items[ 2 ], rootItems[ 1 ] );
    assertSame( items[ 4 ], rootItems[ 2 ] );
  }

  public void testGetItems() {
    GridItem[] items = createGridItems( grid, 3, 1 );

    assertTrue( Arrays.equals( items, grid.getItems() ) );
  }

  public void testGetRootItem() {
    GridItem[] items = createGridItems( grid, 3, 1 );

    assertSame( items[ 2 ], grid.getRootItem( 1 ) );
    assertSame( items[ 4 ], grid.getRootItem( 2 ) );
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
    GridItem[] items = createGridItems( grid, 3, 1 );

    assertSame( items[ 1 ], grid.getItem( 1 ) );
    assertSame( items[ 4 ], grid.getItem( 4 ) );
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
    GridItem[] items = createGridItems( grid, 3, 1 );

    assertEquals( 1, grid.indexOf( items[ 1 ] ) );
    assertEquals( 4, grid.indexOf( items[ 4 ] ) );
  }

  public void testIndexOf_NullArgument() {
    try {
      grid.indexOf( ( GridItem )null );
      fail();
    } catch( IllegalArgumentException expected ) {
    }
  }

  public void testIndexOf_DifferentParent() {
    Grid otherGrid = new Grid( shell, SWT.NONE );
    GridItem item = new GridItem( otherGrid, SWT.NONE );

    assertEquals( -1, grid.indexOf( item ) );
  }

  public void testIndexOf_AfterDispose() {
    GridItem[] items = createGridItems( grid, 3, 1 );

    items[ 2 ].dispose();

    assertEquals( 1, grid.indexOf( items[ 1 ] ) );
    assertEquals( 2, grid.indexOf( items[ 4 ] ) );
  }

  public void testGetColumnCount() {
    createGridColumns( grid, 5, SWT.NONE );

    assertEquals( 5, grid.getColumnCount() );
  }

  public void testGetColumns() {
    GridColumn[] columns = createGridColumns( grid, 5, SWT.NONE );

    assertTrue( Arrays.equals( columns, grid.getColumns() ) );
  }

  public void testGetColumn() {
    GridColumn[] columns = createGridColumns( grid, 5, SWT.NONE );

    assertSame( columns[ 1 ], grid.getColumn( 1 ) );
    assertSame( columns[ 4 ], grid.getColumn( 4 ) );
  }

  public void testGetColumn_InvalidIndex() {
    createGridColumns( grid, 3, SWT.NONE );

    try {
      grid.getColumn( 10 );
      fail();
    } catch( IllegalArgumentException expected ) {
    }
  }

  public void testIndexOfColumn() {
    GridColumn[] columns = createGridColumns( grid, 5, SWT.NONE );

    assertEquals( 1, grid.indexOf( columns[ 1 ] ) );
    assertEquals( 4, grid.indexOf( columns[ 4 ] ) );
  }

  public void testIndexOfColumn_NullArgument() {
    try {
      grid.indexOf( ( GridColumn )null );
      fail();
    } catch( IllegalArgumentException expected ) {
    }
  }

  public void testIndexOfColumn_DifferentParent() {
    Grid otherGrid = new Grid( shell, SWT.NONE );
    GridColumn column = new GridColumn( otherGrid, SWT.NONE );

    assertEquals( -1, grid.indexOf( column ) );
  }

  public void testIndexOfColumn_AfterDispose() {
    GridColumn[] columns = createGridColumns( grid, 5, SWT.NONE );

    columns[ 2 ].dispose();

    assertEquals( 3, grid.indexOf( columns[ 4 ] ) );
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

  public void testDispose_WithColumns() {
    GridColumn[] columns = createGridColumns( grid, 2, SWT.NONE );

    grid.dispose();

    assertTrue( columns[ 0 ].isDisposed() );
    assertTrue( columns[ 1 ].isDisposed() );
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
    assertSame( grid, log.get( 0 ).widget );
  }

  public void testAddRemoveSelectionListener() {
    SelectionListener listener = new SelectionAdapter() {};
    grid.addSelectionListener( listener );

    assertTrue( SelectionEvent.hasListener( grid ) );

    grid.removeSelectionListener( listener );
    assertFalse( SelectionEvent.hasListener( grid ) );
  }

  public void testAddRemoveTreeListener() {
    TreeListener listener = new TreeAdapter() {};
    grid.addTreeListener( listener );

    assertTrue( TreeEvent.hasListener( grid ) );

    grid.removeTreeListener( listener );
    assertFalse( TreeEvent.hasListener( grid ) );
  }

  public void testClearAll() {
    GridItem[] items = createGridItems( grid, 3, 3 );
    items[ 0 ].setText( "foo" );
    items[ 1 ].setText( "bar" );
    items[ 4 ].setText( "root" );

    // Note: The parameter allChildren has no effect as all items (not only rootItems) are cleared
    grid.clearAll( false );

    assertEquals( "", items[ 0 ].getText() );
    assertEquals( "", items[ 1 ].getText() );
    assertEquals( "", items[ 4 ].getText() );
  }

  public void testClearByIndex() {
    GridItem[] items = createGridItems( grid, 3, 3 );
    items[ 0 ].setText( "foo" );
    items[ 1 ].setText( "bar" );
    items[ 4 ].setText( "root" );

    grid.clear( 0, false );

    assertEquals( "", items[ 0 ].getText() );
    assertEquals( "bar", items[ 1 ].getText() );
    assertEquals( "root", items[ 4 ].getText() );
  }

  public void testClearByIndex_AllChildren() {
    GridItem[] items = createGridItems( grid, 3, 3 );
    items[ 0 ].setText( "foo" );
    items[ 1 ].setText( "bar" );
    items[ 4 ].setText( "root" );

    grid.clear( 0, true );

    assertEquals( "", items[ 0 ].getText() );
    assertEquals( "", items[ 1 ].getText() );
    assertEquals( "root", items[ 4 ].getText() );
  }

  public void testClearByIndex_InvalidIndex() {
    createGridItems( grid, 3, 3 );

    try {
      grid.clear( 20, false );
      fail();
    } catch( IllegalArgumentException expected ) {
    }
  }

  public void testClearByIndexRange() {
    GridItem[] items = createGridItems( grid, 3, 3 );
    items[ 0 ].setText( "foo" );
    items[ 1 ].setText( "bar" );
    items[ 3 ].setText( "sub" );
    items[ 4 ].setText( "root" );

    grid.clear( 0, 2, false );

    assertEquals( "", items[ 0 ].getText() );
    assertEquals( "", items[ 1 ].getText() );
    assertEquals( "sub", items[ 3 ].getText() );
    assertEquals( "root", items[ 4 ].getText() );
  }

  public void testClearByIndexRange_AllChildren() {
    GridItem[] items = createGridItems( grid, 3, 3 );
    items[ 0 ].setText( "first" );
    items[ 1 ].setText( "foo" );
    items[ 4 ].setText( "root" );
    items[ 5 ].setText( "bar" );
    items[ 7 ].setText( "sub" );

    grid.clear( 1, 4, true );

    assertEquals( "first", items[ 0 ].getText() );
    assertEquals( "", items[ 1 ].getText() );
    assertEquals( "", items[ 4 ].getText() );
    assertEquals( "", items[ 5 ].getText() );
    assertEquals( "", items[ 7 ].getText() );
  }

  public void testClearByIndexRange_InvalidIndex1() {
    createGridItems( grid, 3, 3 );

    try {
      grid.clear( -1, 4, false );
      fail();
    } catch( IllegalArgumentException expected ) {
    }
  }

  public void testClearByIndexRange_InvalidIndex2() {
    createGridItems( grid, 3, 3 );

    try {
      grid.clear( 1, 20, false );
      fail();
    } catch( IllegalArgumentException expected ) {
    }
  }

  public void testClearByIndexRange_InvalidIndex3() {
    GridItem[] items = createGridItems( grid, 3, 3 );
    items[ 0 ].setText( "foo" );
    items[ 1 ].setText( "bar" );
    items[ 3 ].setText( "sub" );
    items[ 4 ].setText( "root" );

    grid.clear( 4, 1, false );

    assertEquals( "foo", items[ 0 ].getText() );
    assertEquals( "bar", items[ 1 ].getText() );
    assertEquals( "sub", items[ 3 ].getText() );
    assertEquals( "root", items[ 4 ].getText() );
  }

  public void testClearByIndices() {
    GridItem[] items = createGridItems( grid, 3, 3 );
    items[ 0 ].setText( "foo" );
    items[ 1 ].setText( "bar" );
    items[ 3 ].setText( "sub" );
    items[ 4 ].setText( "root" );

    grid.clear( new int[] { 0, 1 }, false );

    assertEquals( "", items[ 0 ].getText() );
    assertEquals( "", items[ 1 ].getText() );
    assertEquals( "sub", items[ 3 ].getText() );
    assertEquals( "root", items[ 4 ].getText() );
  }

  public void testClearByIndices_AllChildren() {
    GridItem[] items = createGridItems( grid, 3, 3 );
    items[ 0 ].setText( "foo" );
    items[ 1 ].setText( "bar" );
    items[ 3 ].setText( "sub" );
    items[ 4 ].setText( "root" );

    grid.clear( new int[] { 0, 1 }, true );

    assertEquals( "", items[ 0 ].getText() );
    assertEquals( "", items[ 1 ].getText() );
    assertEquals( "", items[ 3 ].getText() );
    assertEquals( "root", items[ 4 ].getText() );
  }

  public void testClearByIndices_NullArgument() {
    createGridItems( grid, 3, 3 );

    try {
      grid.clear( null, false );
    } catch( IllegalArgumentException expected ) {
    }
  }

  public void testClearByIndices_InvalidIndex() {
    createGridItems( grid, 3, 3 );

    try {
      grid.clear( new int[] { 0, 20 }, false );
    } catch( IllegalArgumentException expected ) {
    }
  }

  public void testSendSetDataEventAfterClear() {
    grid = new Grid( shell, SWT.VIRTUAL );
    GridItem[] items = createGridItems( grid, 3, 3 );
    items[ 0 ].setText( "foo" );
    // Mark SetData event as fired
    items[ 0 ].getText();
    grid.addListener( SWT.SetData, new Listener() {
      public void handleEvent( Event event ) {
        GridItem item = ( GridItem )event.item;
        item.setText( "bar" );
      }
    } );

    grid.clear( 0, false );

    assertEquals( "bar", items[ 0 ].getText() );
  }

  public void testClearWithColumns() {
    grid = new Grid( shell, SWT.VIRTUAL );
    GridItem[] items = createGridItems( grid, 3, 3 );
    createGridColumns( grid, 3, SWT.NONE );
    items[ 1 ].setText( 0, "item 1.0" );
    items[ 1 ].setText( 1, "item 1.1" );
    items[ 1 ].setText( 2, "item 1.2" );

    grid.clear( 1, false );

    assertEquals( "", items[ 1 ].getText( 0 ) );
    assertEquals( "", items[ 1 ].getText( 1 ) );
    assertEquals( "", items[ 1 ].getText( 2 ) );
  }

  public void testGetSelectionEnabled_Initial() {
    assertTrue( grid.getSelectionEnabled() );
  }

  public void testGetSelectionEnabled() {
    grid.setSelectionEnabled( false );

    assertFalse( grid.getSelectionEnabled() );
  }

  public void testSetSelectionEnabled_ClearSelectedItems() {
    createGridItems( grid, 3, 0 );
    grid.select( 0 );

    grid.setSelectionEnabled( false );

    assertEquals( 0, grid.getSelectionCount() );
  }

  public void testGetSelectionCount_Initial() {
    assertEquals( 0, grid.getSelectionCount() );
  }

  public void testGetSelectionCount() {
    createGridItems( grid, 3, 0 );

    grid.select( 0 );

    assertEquals( 1, grid.getSelectionCount() );
  }

  public void testGetSelection_Initial() {
    assertTrue( Arrays.equals( new GridItem[ 0 ], grid.getSelection() ) );
  }

  public void testGetSelection() {
    createGridItems( grid, 3, 0 );

    grid.select( 0 );

    assertSame( grid.getItem( 0 ), grid.getSelection()[ 0 ] );
  }

  public void testGetSelection_AfterDisposeItem() {
    grid = new Grid( shell, SWT.MULTI );
    GridItem[] items = createGridItems( grid, 5, 0 );
    grid.select( 1, 3 );

    items[ 2 ].dispose();

    GridItem[] expected = new GridItem[]{ items[ 1 ], items[ 3 ] };
    assertTrue( Arrays.equals( expected, grid.getSelection() ) );
  }

  public void testSelectByIndex_Single() {
    grid = new Grid( shell, SWT.SINGLE );
    GridItem[] items = createGridItems( grid, 3, 0 );

    grid.select( 0 );
    grid.select( 2 );

    GridItem[] expected = new GridItem[]{ items[ 2 ] };
    assertTrue( Arrays.equals( expected, grid.getSelection() ) );
  }

  public void testSelectByIndex_Multi() {
    grid = new Grid( shell, SWT.MULTI );
    GridItem[] items = createGridItems( grid, 3, 0 );

    grid.select( 0 );
    grid.select( 2 );

    GridItem[] expected = new GridItem[]{ items[ 0 ], items[ 2 ] };
    assertTrue( Arrays.equals( expected, grid.getSelection() ) );
  }

  public void testSelectByIndex_WithSelectionDisabled() {
    grid.setSelectionEnabled( false );
    createGridItems( grid, 3, 0 );

    grid.select( 0 );

    assertTrue( Arrays.equals( new GridItem[ 0 ], grid.getSelection() ) );
  }

  public void testSelectByIndex_WithInvalidIndex() {
    grid = new Grid( shell, SWT.MULTI );
    GridItem[] items = createGridItems( grid, 3, 0 );

    grid.select( 0 );
    grid.select( 5 );

    GridItem[] expected = new GridItem[]{ items[ 0 ] };
    assertTrue( Arrays.equals( expected, grid.getSelection() ) );
  }

  public void testSelectByIndex_Twice() {
    grid = new Grid( shell, SWT.MULTI );
    GridItem[] items = createGridItems( grid, 3, 0 );

    grid.select( 0 );
    grid.select( 0 );

    GridItem[] expected = new GridItem[]{ items[ 0 ] };
    assertTrue( Arrays.equals( expected, grid.getSelection() ) );
  }

  public void testSelectByRange_Single() {
    grid = new Grid( shell, SWT.SINGLE );
    GridItem[] items = createGridItems( grid, 5, 0 );

    grid.select( 1, 1 );

    GridItem[] expected = new GridItem[]{ items[ 1 ] };
    assertTrue( Arrays.equals( expected, grid.getSelection() ) );
  }

  public void testSelectByRange_SingleWithDifferentSrartEnd() {
    grid = new Grid( shell, SWT.SINGLE );
    createGridItems( grid, 5, 0 );

    grid.select( 1, 3 );

    assertTrue( Arrays.equals( new GridItem[ 0 ], grid.getSelection() ) );
  }

  public void testSelectByRange_Multi() {
    grid = new Grid( shell, SWT.MULTI );
    GridItem[] items = createGridItems( grid, 5, 0 );

    grid.select( 1, 3 );

    GridItem[] expected = new GridItem[]{ items[ 1 ], items[ 2 ], items[ 3 ] };
    assertTrue( Arrays.equals( expected, grid.getSelection() ) );
  }

  public void testSelectByRange_WithSelectionDisabled() {
    grid = new Grid( shell, SWT.MULTI );
    grid.setSelectionEnabled( false );
    createGridItems( grid, 5, 0 );

    grid.select( 1, 3 );

    assertTrue( Arrays.equals( new GridItem[ 0 ], grid.getSelection() ) );
  }

  public void testSelectByRange_StartBiggerThanEnd() {
    grid = new Grid( shell, SWT.MULTI );
    createGridItems( grid, 5, 0 );

    grid.select( 3, 1 );

    assertTrue( Arrays.equals( new GridItem[ 0 ], grid.getSelection() ) );
  }

  public void testSelectByIndices_Single() {
    grid = new Grid( shell, SWT.SINGLE );
    GridItem[] items = createGridItems( grid, 5, 0 );

    grid.select( new int[] { 1 } );

    GridItem[] expected = new GridItem[]{ items[ 1 ] };
    assertTrue( Arrays.equals( expected, grid.getSelection() ) );
  }

  public void testSelectByIndices_SingleWithMultipleIndices() {
    grid = new Grid( shell, SWT.SINGLE );
    createGridItems( grid, 5, 0 );

    grid.select( new int[] { 1, 2, 3 } );

    assertTrue( Arrays.equals( new GridItem[ 0 ], grid.getSelection() ) );
  }

  public void testSelectByIndices_Multi() {
    grid = new Grid( shell, SWT.MULTI );
    GridItem[] items = createGridItems( grid, 5, 0 );

    grid.select( new int[] { 1, 2, 3 } );

    GridItem[] expected = new GridItem[]{ items[ 1 ], items[ 2 ], items[ 3 ] };
    assertTrue( Arrays.equals( expected, grid.getSelection() ) );
  }

  public void testSelectByIndices_NullArgument() {
    grid = new Grid( shell, SWT.MULTI );

    try {
      grid.select( null );
      fail();
    } catch( IllegalArgumentException expected ) {
    }
  }

  public void testSelectByIndices_InvalidIndex() {
    grid = new Grid( shell, SWT.MULTI );
    GridItem[] items = createGridItems( grid, 5, 0 );

    grid.select( new int[] { 1, 55, 2, 3 } );

    GridItem[] expected = new GridItem[]{ items[ 1 ], items[ 2 ], items[ 3 ] };
    assertTrue( Arrays.equals( expected, grid.getSelection() ) );
  }

  public void testSelectByIndices_DuplicateIndex() {
    grid = new Grid( shell, SWT.MULTI );
    GridItem[] items = createGridItems( grid, 5, 0 );

    grid.select( new int[] { 1, 2, 2, 3 } );

    GridItem[] expected = new GridItem[]{ items[ 1 ], items[ 2 ], items[ 3 ] };
    assertTrue( Arrays.equals( expected, grid.getSelection() ) );
  }

  public void testSelectAll_Single() {
    grid = new Grid( shell, SWT.SINGLE );
    createGridItems( grid, 5, 0 );

    grid.selectAll();

    assertTrue( Arrays.equals( new GridItem[ 0 ], grid.getSelection() ) );
  }

  public void testSelectAll_Multi() {
    grid = new Grid( shell, SWT.MULTI );
    GridItem[] items = createGridItems( grid, 3, 0 );

    grid.selectAll();

    GridItem[] expected = new GridItem[]{ items[ 0 ], items[ 1 ], items[ 2 ] };
    assertTrue( Arrays.equals( expected, grid.getSelection() ) );
  }

  public void testSelectAll_AfterSelect() {
    grid = new Grid( shell, SWT.MULTI );
    GridItem[] items = createGridItems( grid, 3, 0 );

    grid.select( 1 );
    grid.selectAll();

    GridItem[] expected = new GridItem[]{ items[ 0 ], items[ 1 ], items[ 2 ] };
    assertTrue( Arrays.equals( expected, grid.getSelection() ) );
  }

  public void testSetSelectionByIndex() {
    GridItem[] items = createGridItems( grid, 3, 0 );

    grid.setSelection( 1 );

    GridItem[] expected = new GridItem[]{ items[ 1 ] };
    assertTrue( Arrays.equals( expected, grid.getSelection() ) );
  }

  public void testSetSelectionByIndex_ClearPreviousSelection() {
    GridItem[] items = createGridItems( grid, 3, 0 );
    grid.select( 0 );

    grid.setSelection( 1 );

    GridItem[] expected = new GridItem[]{ items[ 1 ] };
    assertTrue( Arrays.equals( expected, grid.getSelection() ) );
  }

  public void testSetSelectionByIndex_WithSelectionDisabled() {
    grid.setSelectionEnabled( false );
    createGridItems( grid, 3, 0 );

    grid.setSelection( 0 );

    assertTrue( Arrays.equals( new GridItem[ 0 ], grid.getSelection() ) );
  }

  public void testSetSelectionByIndex_WithInvalidIndex() {
    createGridItems( grid, 3, 0 );

    grid.setSelection( 5 );

    assertTrue( Arrays.equals( new GridItem[ 0 ], grid.getSelection() ) );
  }

  public void testSetSelectionByRange_Single() {
    grid = new Grid( shell, SWT.SINGLE );
    GridItem[] items = createGridItems( grid, 5, 0 );
    grid.select( 0 );

    grid.setSelection( 1, 1 );

    GridItem[] expected = new GridItem[]{ items[ 1 ] };
    assertTrue( Arrays.equals( expected, grid.getSelection() ) );
  }

  public void testSetSelectionByRange_SingleWithDifferentSrartEnd() {
    grid = new Grid( shell, SWT.SINGLE );
    GridItem[] items = createGridItems( grid, 5, 0 );
    grid.select( 0 );

    grid.setSelection( 1, 3 );

    GridItem[] expected = new GridItem[]{ items[ 0 ] };
    assertTrue( Arrays.equals( expected, grid.getSelection() ) );
  }

  public void testSetSelectionByRange_Multi() {
    grid = new Grid( shell, SWT.MULTI );
    GridItem[] items = createGridItems( grid, 5, 0 );
    grid.select( 0 );

    grid.setSelection( 1, 3 );

    GridItem[] expected = new GridItem[]{ items[ 1 ], items[ 2 ], items[ 3 ] };
    assertTrue( Arrays.equals( expected, grid.getSelection() ) );
  }

  public void testSetSelectionByRange_WithSelectionDisabled() {
    grid = new Grid( shell, SWT.MULTI );
    grid.setSelectionEnabled( false );
    createGridItems( grid, 5, 0 );

    grid.setSelection( 1, 3 );

    assertTrue( Arrays.equals( new Grid[ 0 ], grid.getSelection() ) );
  }

  public void testSetSelectionByRange_StartBiggerThanEnd() {
    grid = new Grid( shell, SWT.MULTI );
    createGridItems( grid, 5, 0 );
    grid.select( 0 );

    grid.setSelection( 3, 1 );

    assertTrue( Arrays.equals( new Grid[ 0 ], grid.getSelection() ) );
  }

  public void testSetSelectionByIndices_Single() {
    grid = new Grid( shell, SWT.SINGLE );
    GridItem[] items = createGridItems( grid, 5, 0 );
    grid.select( 0 );

    grid.setSelection( new int[] { 1 } );

    GridItem[] expected = new GridItem[]{ items[ 1 ] };
    assertTrue( Arrays.equals( expected, grid.getSelection() ) );
  }

  public void testSetSelectionByIndices_SingleWithMultipleIndices() {
    grid = new Grid( shell, SWT.SINGLE );
    GridItem[] items = createGridItems( grid, 5, 0 );
    grid.select( 0 );

    grid.setSelection( new int[] { 1, 2, 3 } );

    GridItem[] expected = new GridItem[]{ items[ 0 ] };
    assertTrue( Arrays.equals( expected, grid.getSelection() ) );
  }

  public void testSetSelectionByIndices_Multi() {
    grid = new Grid( shell, SWT.MULTI );
    GridItem[] items = createGridItems( grid, 5, 0 );
    grid.select( 0 );

    grid.setSelection( new int[] { 1, 2, 3 } );

    GridItem[] expected = new GridItem[]{ items[ 1 ], items[ 2 ], items[ 3 ] };
    assertTrue( Arrays.equals( expected, grid.getSelection() ) );
  }

  public void testSetSelectionByIndices_NullArgument() {
    try {
      grid.setSelection( ( int[] )null );
      fail();
    } catch( IllegalArgumentException expected ) {
    }
  }

  public void testSetSelectionByIndices_InvalidIndex() {
    grid = new Grid( shell, SWT.MULTI );
    GridItem[] items = createGridItems( grid, 5, 0 );

    grid.setSelection( new int[] { 1, 55, 2, 3 } );

    GridItem[] expected = new GridItem[]{ items[ 1 ], items[ 2 ], items[ 3 ] };
    assertTrue( Arrays.equals( expected, grid.getSelection() ) );
  }

  public void testSetSelectionByIndices_DuplicateIndex() {
    grid = new Grid( shell, SWT.MULTI );
    GridItem[] items = createGridItems( grid, 5, 0 );

    grid.setSelection( new int[] { 1, 2, 2, 3 } );

    GridItem[] expected = new GridItem[]{ items[ 1 ], items[ 2 ], items[ 3 ] };
    assertTrue( Arrays.equals( expected, grid.getSelection() ) );
  }

  public void testSetSelectionByItems_Single() {
    grid = new Grid( shell, SWT.SINGLE );
    GridItem[] items = createGridItems( grid, 5, 0 );

    grid.setSelection( new GridItem[]{ items[ 1 ] } );

    GridItem[] expected = new GridItem[]{ items[ 1 ] };
    assertTrue( Arrays.equals( expected, grid.getSelection() ) );
  }

  public void testSetSelectionByItems_SingleWithMultipleItems() {
    grid = new Grid( shell, SWT.SINGLE );
    GridItem[] items = createGridItems( grid, 5, 0 );

    grid.setSelection( new GridItem[]{ items[ 1 ], items[ 2 ], items[ 3 ] } );

    assertTrue( Arrays.equals( new GridItem[ 0 ], grid.getSelection() ) );
  }

  public void testSetSelectionByItems_WithSelectionDisabled() {
    grid.setSelectionEnabled( false );
    GridItem[] items = createGridItems( grid, 5, 0 );

    grid.setSelection( new GridItem[]{ items[ 1 ], items[ 2 ], items[ 3 ] } );

    assertTrue( Arrays.equals( new GridItem[ 0 ], grid.getSelection() ) );
  }

  public void testSetSelectionByItems_Multi() {
    grid = new Grid( shell, SWT.MULTI );
    GridItem[] items = createGridItems( grid, 5, 0 );
    grid.select( 0 );

    grid.setSelection( new GridItem[]{ items[ 1 ], items[ 2 ], items[ 3 ] } );

    GridItem[] expected = new GridItem[]{ items[ 1 ], items[ 2 ], items[ 3 ] };
    assertTrue( Arrays.equals( expected, grid.getSelection() ) );
  }

  public void testSetSelectionByItems_NullArgument() {
    try {
      grid.setSelection( ( GridItem[] )null );
      fail();
    } catch( IllegalArgumentException expected ) {
    }
  }

  public void testSetSelectionByItems_NullItem() {
    grid = new Grid( shell, SWT.MULTI );
    GridItem[] items = createGridItems( grid, 5, 0 );

    grid.setSelection( new GridItem[]{ items[ 1 ], null, items[ 3 ] } );

    GridItem[] expected = new GridItem[]{ items[ 1 ], items[ 3 ] };
    assertTrue( Arrays.equals( expected, grid.getSelection() ) );
  }

  public void testSetSelectionByItems_ItemWithDifferentParent() {
    grid = new Grid( shell, SWT.MULTI );
    GridItem[] items = createGridItems( grid, 5, 0 );
    Grid otherGrid = new Grid( shell, SWT.NONE );
    GridItem otherItem = new GridItem( otherGrid, SWT.NONE );

    grid.setSelection( new GridItem[]{ items[ 1 ], otherItem, items[ 3 ] } );

    GridItem[] expected = new GridItem[]{ items[ 1 ], items[ 3 ] };
    assertTrue( Arrays.equals( expected, grid.getSelection() ) );
  }

  public void testSetSelectionByItems_DisposedItem() {
    grid = new Grid( shell, SWT.MULTI );
    GridItem[] items = createGridItems( grid, 5, 0 );
    items[ 2 ].dispose();

    try {
      grid.setSelection( new GridItem[]{ items[ 1 ], items[ 2 ], items[ 3 ] } );
      fail();
    } catch( IllegalArgumentException expected ) {
    }
  }

  public void testDeselectByIndex() {
    grid = new Grid( shell, SWT.MULTI );
    GridItem[] items = createGridItems( grid, 5, 0 );
    grid.select( 1, 3 );

    grid.deselect( 2 );

    GridItem[] expected = new GridItem[]{ items[ 1 ], items[ 3 ] };
    assertTrue( Arrays.equals( expected, grid.getSelection() ) );
  }

  public void testDeselectByIndex_InvalidIndex() {
    grid = new Grid( shell, SWT.MULTI );
    GridItem[] items = createGridItems( grid, 5, 0 );
    grid.select( 1, 3 );

    grid.deselect( 10 );

    GridItem[] expected = new GridItem[]{ items[ 1 ], items[ 2 ], items[ 3 ] };
    assertTrue( Arrays.equals( expected, grid.getSelection() ) );
  }

  public void testDeselectByRange() {
    grid = new Grid( shell, SWT.MULTI );
    GridItem[] items = createGridItems( grid, 5, 0 );
    grid.select( 1, 4 );

    grid.deselect( 2, 3 );

    GridItem[] expected = new GridItem[]{ items[ 1 ], items[ 4 ] };
    assertTrue( Arrays.equals( expected, grid.getSelection() ) );
  }

  public void testDeselectByRange_OutOfItemsSize() {
    grid = new Grid( shell, SWT.MULTI );
    GridItem[] items = createGridItems( grid, 5, 0 );
    grid.select( 1, 4 );

    grid.deselect( 2, 12 );

    GridItem[] expected = new GridItem[]{ items[ 1 ] };
    assertTrue( Arrays.equals( expected, grid.getSelection() ) );
  }

  public void testDeselectByIndices() {
    grid = new Grid( shell, SWT.MULTI );
    GridItem[] items = createGridItems( grid, 5, 0 );
    grid.select( 1, 4 );

    grid.deselect( new int[]{ 2, 3 } );

    GridItem[] expected = new GridItem[]{ items[ 1 ], items[ 4 ] };
    assertTrue( Arrays.equals( expected, grid.getSelection() ) );
  }

  public void testDeselectByIndices_NullArgument() {
    try {
      grid.deselect( null );
    } catch( IllegalArgumentException expected ) {
    }
  }

  public void testDeselectByIndices_DuplicateIndex() {
    grid = new Grid( shell, SWT.MULTI );
    GridItem[] items = createGridItems( grid, 5, 0 );
    grid.select( 1, 4 );

    grid.deselect( new int[]{ 2, 3, 2 } );

    GridItem[] expected = new GridItem[]{ items[ 1 ], items[ 4 ] };
    assertTrue( Arrays.equals( expected, grid.getSelection() ) );
  }

  public void testDeselectByIndices_InvalidIndex() {
    grid = new Grid( shell, SWT.MULTI );
    GridItem[] items = createGridItems( grid, 5, 0 );
    grid.select( 1, 4 );

    grid.deselect( new int[]{ 2, 3, 14 } );

    GridItem[] expected = new GridItem[]{ items[ 1 ], items[ 4 ] };
    assertTrue( Arrays.equals( expected, grid.getSelection() ) );
  }

  public void testDeselectAll() {
    grid = new Grid( shell, SWT.MULTI );
    createGridItems( grid, 5, 0 );
    grid.select( 1, 4 );

    grid.deselectAll();

    assertTrue( Arrays.equals( new GridItem[ 0 ], grid.getSelection() ) );
  }

  public void testRemoveByIndex() {
    createGridItems( grid, 3, 3 );

    grid.remove( 4 );

    assertEquals( 8, grid.getItemCount() );
    assertEquals( 2, grid.getRootItemCount() );
  }

  public void testRemoveByIndex_InvalidIndex() {
    try {
      grid.remove( 50 );
      fail();
    } catch( IllegalArgumentException expected ) {
    }
  }

  public void testRemoveByIndex_RemoveFromSelection() {
    createGridItems( grid, 3, 3 );
    grid.select( 6 );

    grid.remove( 4 );

    assertEquals( 0, grid.getSelectionCount() );
  }

  public void testRemoveByRange() {
    createGridItems( grid, 3, 3 );

    grid.remove( 3, 9 );

    assertEquals( 3, grid.getItemCount() );
    assertEquals( 1, grid.getRootItemCount() );
  }

  public void testRemoveByRange_InvalidRange() {
    createGridItems( grid, 3, 3 );

    try {
      grid.remove( 3, 60 );
      fail();
    } catch( IllegalArgumentException expected ) {
    }
  }

  public void testRemoveByIndices() {
    createGridItems( grid, 3, 3 );

    grid.remove( new int[]{ 3, 5, 8 } );

    assertEquals( 6, grid.getItemCount() );
    assertEquals( 2, grid.getRootItemCount() );
  }

  public void testRemoveByIndices_NullArgument() {
    try {
      grid.remove( null );
      fail();
    } catch( IllegalArgumentException expected ) {
    }
  }

  public void testRemoveByIndices_DuplicateIndex() {
    createGridItems( grid, 3, 3 );

    grid.remove( new int[]{ 3, 5, 3 } );

    assertEquals( 10, grid.getItemCount() );
    assertEquals( 3, grid.getRootItemCount() );
  }

  public void testRemoveByIndices_InvalidIndex() {
    createGridItems( grid, 3, 3 );

    try {
      grid.remove(new int[]{ 3, 5, 100 } );
      fail();
    } catch( IllegalArgumentException expected ) {
    }
  }

  public void testRemoveAll() {
    createGridItems( grid, 3, 3 );

    grid.removeAll();

    assertEquals( 0, grid.getItemCount() );
    assertEquals( 0, grid.getRootItemCount() );
  }

  public void testGetSelectionIndex() {
    grid = new Grid( shell, SWT.MULTI );
    createGridItems( grid, 3, 3 );
    int indicies[] = new int[]{ 3, 4, 1, 7 };

    grid.setSelection( indicies );

    assertEquals( 3, grid.getSelectionIndex() );
  }

  public void testGetSelectionIndex_WithoutSelection() {
    assertEquals( -1, grid.getSelectionIndex() );
  }

  public void testGetSelectionIndicies() {
    grid = new Grid( shell, SWT.MULTI );
    createGridItems( grid, 3, 3 );
    int indicies[] = new int[]{ 3, 4, 1, 7 };

    grid.setSelection( indicies );

    assertTrue( Arrays.equals( indicies, grid.getSelectionIndices() ) );
  }

  public void testGetSelectionIndicies_WithoutSelection() {
    assertTrue( Arrays.equals( new int[ 0 ], grid.getSelectionIndices() ) );
  }

  public void testIsSelectedByIndex_Initial() {
    createGridItems( grid, 3, 0 );

    assertFalse( grid.isSelected( 0 ) );
    assertFalse( grid.isSelected( 1 ) );
    assertFalse( grid.isSelected( 2 ) );
  }

  public void testIsSelectedByIndex() {
    createGridItems( grid, 3, 0 );

    grid.select( 1 );

    assertFalse( grid.isSelected( 0 ) );
    assertTrue( grid.isSelected( 1 ) );
    assertFalse( grid.isSelected( 2 ) );
  }

  public void testIsSelectedByIndex_InvalidIndex() {
    createGridItems( grid, 3, 0 );

    assertFalse( grid.isSelected( 5 ) );
  }

  public void testIsSelectedByItem_Initial() {
    GridItem[] items = createGridItems( grid, 3, 0 );

    assertFalse( grid.isSelected( items[ 0 ] ) );
    assertFalse( grid.isSelected( items[ 1 ] ) );
    assertFalse( grid.isSelected( items[ 2 ] ) );
  }

  public void testIsSelectedByItem() {
    GridItem[] items = createGridItems( grid, 3, 0 );

    grid.select( 1 );

    assertFalse( grid.isSelected( items[ 0 ] ) );
    assertTrue( grid.isSelected( items[ 1 ] ) );
    assertFalse( grid.isSelected( items[ 2 ] ) );
  }

  public void testIsSelectedByItem_NullArgument() {
    try {
      grid.isSelected( null );
      fail();
    } catch( IllegalArgumentException expected ) {
    }
  }

  public void testIsSelectedByItem_DisposedItem() {
    GridItem[] items = createGridItems( grid, 3, 0 );
    grid.select( 1 );

    items[ 1 ].dispose();

    assertFalse( grid.isSelected( items[ 1 ] ) );
  }

  public void testGetHeaderVisible_Initial() {
    assertFalse( grid.getHeaderVisible() );
  }

  public void testSetHeaderVisible() {
    grid.setHeaderVisible( true );

    assertTrue( grid.getHeaderVisible() );
  }

  public void testGetLinesVisible_Initial() {
    assertTrue( grid.getLinesVisible() );
  }

  public void testGetLinesVisible() {
    grid.setLinesVisible( false );

    assertFalse( grid.getLinesVisible() );
  }

  public void testGetFocusItem_Initial() {
    assertNull( grid.getFocusItem() );
  }

  public void testSetFocusItem() {
    GridItem[] items = createGridItems( grid, 3, 3 );

    grid.setFocusItem( items[ 4 ] );

    assertSame( items[ 4 ], grid.getFocusItem() );
  }

  public void testSetFocusItem_NullArgument() {
    try {
      grid.setFocusItem( null );
      fail();
    } catch( IllegalArgumentException expected ) {
    }
  }

  public void testSetFocusItem_DisposedItem() {
    GridItem item = new GridItem( grid, SWT.NONE );
    item.dispose();

    try {
      grid.setFocusItem( item );
      fail();
    } catch( IllegalArgumentException expected ) {
    }
  }

  public void testSetFocusItem_WithOtherParent() {
    Grid otherGrid = new Grid( shell, SWT.NONE );
    GridItem item = new GridItem( otherGrid, SWT.NONE );

    try {
      grid.setFocusItem( item );
      fail();
    } catch( IllegalArgumentException expected ) {
    }
  }

  public void testSetFocusItem_InvisibleItem() {
    GridItem[] items = createGridItems( grid, 3, 3 );

    try {
      grid.setFocusItem( items[ 2 ] );
      fail();
    } catch( IllegalArgumentException expected ) {
    }
  }

  public void testGetColumnOrder_Initial() {
    createGridColumns( grid, 5, SWT.NONE );

    assertTrue( Arrays.equals( new int[]{ 0, 1, 2, 3, 4 }, grid.getColumnOrder() ) );
  }

  public void testSetColumnOrder() {
    createGridColumns( grid, 5, SWT.NONE );
    int[] order = new int[]{ 4, 1, 3, 2, 0 };

    grid.setColumnOrder( order );

    assertTrue( Arrays.equals( order, grid.getColumnOrder() ) );
  }

  public void testSetColumnOrder_NullArgument() {
    try {
      grid.setColumnOrder( null );
      fail();
    } catch( IllegalArgumentException expected ) {
    }
  }

  public void testSetColumnOrder_DifferentArraySize() {
    createGridColumns( grid, 5, SWT.NONE );
    int[] order = new int[]{ 4, 1, 3, 2, 0, 6 };

    try {
      grid.setColumnOrder( order );
      fail();
    } catch( IllegalArgumentException expected ) {
    }
  }

  public void testSetColumnOrder_InvalidColumnIndex() {
    createGridColumns( grid, 5, SWT.NONE );
    int[] order = new int[]{ 4, 1, 33, 2, 0 };

    try {
      grid.setColumnOrder( order );
      fail();
    } catch( IllegalArgumentException expected ) {
    }
  }

  public void testSetColumnOrder_DuplicateColumnIndex() {
    createGridColumns( grid, 5, SWT.NONE );
    int[] order = new int[]{ 3, 1, 3, 2, 0 };

    try {
      grid.setColumnOrder( order );
      fail();
    } catch( IllegalArgumentException expected ) {
    }
  }

  public void testGetColumnOrder_AfterColumnAdd() {
    createGridColumns( grid, 5, SWT.NONE );
    grid.setColumnOrder( new int[]{ 4, 1, 3, 2, 0 } );

    new GridColumn( grid, SWT.NONE, 2 );

    int[] expected = new int[]{ 5, 1, 2, 4, 3, 0 };
    assertTrue( Arrays.equals( expected, grid.getColumnOrder() ) );
  }

  public void testGetColumnOrder_AfterColumnRemove() {
    GridColumn[] columns = createGridColumns( grid, 5, SWT.NONE );
    grid.setColumnOrder( new int[]{ 4, 1, 3, 2, 0 } );

    columns[ 3 ].dispose();

    int[] expected = new int[]{ 3, 1, 2, 0 };
    assertTrue( Arrays.equals( expected, grid.getColumnOrder() ) );
  }

  public void testGetNextVisibleItem_CollapsedItem() {
    GridItem[] items = createGridItems( grid, 3, 3 );

    assertSame( items[ 8 ], grid.getNextVisibleItem( items[ 4 ] ) );
  }

  public void testGetNextVisibleItem_ExpandedItem() {
    GridItem[] items = createGridItems( grid, 3, 3 );
    items[ 4 ].setExpanded( true );

    assertSame( items[ 5 ], grid.getNextVisibleItem( items[ 4 ] ) );
  }

  public void testGetNextVisibleItem_NullArgument() {
    GridItem[] items = createGridItems( grid, 3, 3 );

    assertSame( items[ 0 ], grid.getNextVisibleItem( null ) );
  }

  public void testGetNextVisibleItem_LastItem() {
    GridItem[] items = createGridItems( grid, 3, 3 );

    assertNull( grid.getNextVisibleItem( items[ 11 ] ) );
  }

  public void testGetNextVisibleItem_AllNextNotVisible() {
    GridItem[] items = createGridItems( grid, 3, 3 );

    assertNull( grid.getNextVisibleItem( items[ 8 ] ) );
  }

  public void testGetPreviousVisibleItem_CollapsedItem() {
    GridItem[] items = createGridItems( grid, 3, 3 );

    assertSame( items[ 0 ], grid.getPreviousVisibleItem( items[ 4 ] ) );
  }

  public void testGetPreviousVisibleItem_ExpandedItem() {
    GridItem[] items = createGridItems( grid, 3, 3 );
    items[ 0 ].setExpanded( true );

    assertSame( items[ 3 ], grid.getPreviousVisibleItem( items[ 4 ] ) );
  }

  public void testGetPreviousVisibleItem_NullArgument() {
    GridItem[] items = createGridItems( grid, 3, 3 );

    assertSame( items[ 8 ], grid.getPreviousVisibleItem( null ) );
  }

  public void testGetPreviousVisibleItem_FirstItem() {
    GridItem[] items = createGridItems( grid, 3, 3 );

    assertNull( grid.getPreviousVisibleItem( items[ 0 ] ) );
  }

  public void testGetNextVisibleColumn_NextNotVisible() {
    GridColumn[] columns = createGridColumns( grid, 5, SWT.NONE );
    columns[ 3 ].setVisible( false );

    assertSame( columns[ 4 ], grid.getNextVisibleColumn( columns[ 2 ] ) );
  }

  public void testGetNextVisibleColumn_NextVisible() {
    GridColumn[] columns = createGridColumns( grid, 5, SWT.NONE );

    assertSame( columns[ 3 ], grid.getNextVisibleColumn( columns[ 2 ] ) );
  }

  public void testGetNextVisibleColumn_NullArgument() {
    GridColumn[] columns = createGridColumns( grid, 5, SWT.NONE );

    assertSame( columns[ 0 ], grid.getNextVisibleColumn( null ) );
  }

  public void testGetNextVisibleColumn_LastColumn() {
    GridColumn[] columns = createGridColumns( grid, 5, SWT.NONE );

    assertNull( grid.getNextVisibleColumn( columns[ 4 ] ) );
  }

  public void testGetNextVisibleColumn_AllNextNotVisible() {
    GridColumn[] columns = createGridColumns( grid, 5, SWT.NONE );
    columns[ 3 ].setVisible( false );
    columns[ 4 ].setVisible( false );

    assertNull( grid.getNextVisibleColumn( columns[ 2 ] ) );
  }

  public void testGetNextVisibleColumn_WithColumnOrder() {
    GridColumn[] columns = createGridColumns( grid, 5, SWT.NONE );
    grid.setColumnOrder( new int[]{ 4, 0, 2, 1, 3 } );

    assertSame( columns[ 1 ], grid.getNextVisibleColumn( columns[ 2 ] ) );
  }

  public void testGetPreviousVisibleColumn_PreviousNotVisible() {
    GridColumn[] columns = createGridColumns( grid, 5, SWT.NONE );
    columns[ 1 ].setVisible( false );

    assertSame( columns[ 0 ], grid.getPreviousVisibleColumn( columns[ 2 ] ) );
  }

  public void testGetPreviousVisibleColumn_PreviousVisible() {
    GridColumn[] columns = createGridColumns( grid, 5, SWT.NONE );

    assertSame( columns[ 1 ], grid.getPreviousVisibleColumn( columns[ 2 ] ) );
  }

  public void testGetPreviousVisibleColumn_NullArgument() {
    GridColumn[] columns = createGridColumns( grid, 5, SWT.NONE );

    assertSame( columns[ 4 ], grid.getPreviousVisibleColumn( null ) );
  }

  public void testGetPreviousVisibleColumn_FirstColumn() {
    GridColumn[] columns = createGridColumns( grid, 5, SWT.NONE );

    assertNull( grid.getPreviousVisibleColumn( columns[ 0 ] ) );
  }

  public void testGetPreviousVisibleColumn_WithColumnOrder() {
    GridColumn[] columns = createGridColumns( grid, 5, SWT.NONE );
    grid.setColumnOrder( new int[]{ 4, 0, 2, 1, 3 } );

    assertSame( columns[ 0 ], grid.getPreviousVisibleColumn( columns[ 2 ] ) );
  }

  public void testGetItemHeight_Initial() {
    assertEquals( 21, grid.getItemHeight() );
  }

  public void testGetItemHeight() {
    grid.setItemHeight( 30 );

    assertEquals( 30, grid.getItemHeight() );
  }

  public void testGetItemHeight_AfterFontChange() {
    // fill the cache
    grid.getItemHeight();
    Font font = new Font( display, "Arial", 20, SWT.BOLD );

    grid.setFont( font );

    assertEquals( 27, grid.getItemHeight() );
  }

  public void testGetItemHeight_MinHeight() {
    Font font = new Font( display, "Arial", 8, SWT.NORMAL );

    grid.setFont( font );

    assertEquals( 16, grid.getItemHeight() );
  }

  public void testGetItemHeight_WithGridCheck() {
    grid = new Grid( shell, SWT.CHECK );

    assertEquals( 24, grid.getItemHeight() );
  }

  public void testGetItemHeight_WithColumnCheck() {
    new GridColumn( grid, SWT.CHECK );

    assertEquals( 24, grid.getItemHeight() );
  }

  public void testGetItemHeight_AddCheckColumn() {
    // fill the cache
    grid.getItemHeight();

    new GridColumn( grid, SWT.CHECK );

    assertEquals( 24, grid.getItemHeight() );
  }

  public void testGetItemHeight_RemoveCheckColumn() {
    GridColumn column = new GridColumn( grid, SWT.CHECK );
    // fill the cache
    grid.getItemHeight();

    column.dispose();

    assertEquals( 21, grid.getItemHeight() );
  }

  public void testGetItemHeight_WithItemImage() {
    createGridColumns( grid, 3, SWT.NONE );
    GridItem item = new GridItem( grid, SWT.NONE );
    Image image = loadImage( display, Fixture.IMAGE_100x50 );
    // fill the cache
    grid.getItemHeight();

    item.setImage( 1, image );

    assertEquals( 57, grid.getItemHeight() );
  }

  public void testGetItemHeight_AfterClearAll() {
    createGridColumns( grid, 3, SWT.NONE );
    GridItem item = new GridItem( grid, SWT.NONE );
    Image image = loadImage( display, Fixture.IMAGE_100x50 );
    item.setImage( 1, image );

    grid.clearAll( true );

    assertEquals( 21, grid.getItemHeight() );
  }

  public void testGetHeaderHeight_Initial() {
    createGridColumns( grid, 3, SWT.NONE );

    assertEquals( 0, grid.getHeaderHeight() );
  }

  public void testGetHeaderHeight() {
    grid.setHeaderVisible( true );
    GridColumn[] columns = createGridColumns( grid, 3, SWT.NONE );
    Image image = loadImage( display, Fixture.IMAGE_100x50 );
    columns[ 0 ].setImage( image );
    columns[ 1 ].setText( "foo" );

    assertEquals( 67, grid.getHeaderHeight() );
  }

  public void testGetHeaderHeight_DifferentColumnHeaderFonts() {
    grid.setHeaderVisible( true );
    GridColumn[] columns = createGridColumns( grid, 3, SWT.NONE );
    columns[ 0 ].setHeaderFont( new Font( display, "Arial", 10, SWT.NORMAL ) );
    columns[ 2 ].setHeaderFont( new Font( display, "Arial", 20, SWT.NORMAL ) );

    assertEquals( 37, grid.getHeaderHeight() );
  }

  public void testGetHeaderHeight_AfterColumnDispose() {
    grid.setHeaderVisible( true );
    GridColumn[] columns = createGridColumns( grid, 3, SWT.NONE );
    Image image = loadImage( display, Fixture.IMAGE_100x50 );
    columns[ 0 ].setImage( image );
    columns[ 1 ].setText( "foo" );
    // fill the cache
    grid.getHeaderHeight();

    columns[ 0 ].dispose();

    assertEquals( 31, grid.getHeaderHeight() );
  }

  public void testGetHeaderHeight_AfterTextChange() {
    grid.setHeaderVisible( true );
    GridColumn[] columns = createGridColumns( grid, 3, SWT.NONE );
    columns[ 1 ].setText( "foo" );
    // fill the cache
    grid.getHeaderHeight();

    columns[ 1 ].setText( "foo\nbar" );

    assertEquals( 52, grid.getHeaderHeight() );
  }

  public void testGetHeaderHeight_AfterImageChange() {
    grid.setHeaderVisible( true );
    GridColumn[] columns = createGridColumns( grid, 3, SWT.NONE );
    Image image = loadImage( display, Fixture.IMAGE_100x50 );
    columns[ 0 ].setImage( image );
    columns[ 1 ].setText( "foo" );
    // fill the cache
    grid.getHeaderHeight();

    columns[ 0 ].setImage( null );

    assertEquals( 31, grid.getHeaderHeight() );
  }

  public void testGetHeaderHeight_AfterFontChange() {
    grid.setHeaderVisible( true );
    GridColumn[] columns = createGridColumns( grid, 3, SWT.NONE );
    columns[ 1 ].setText( "foo" );
    // fill the cache
    grid.getHeaderHeight();

    columns[ 1 ].setHeaderFont( new Font( display, "Arial", 20, SWT.NORMAL ) );

    assertEquals( 37, grid.getHeaderHeight() );
  }

  public void testComputeSize() {
    grid = new Grid( shell, SWT.NONE );
    createGridColumns( grid, 3, SWT.NONE );
    createGridItems( grid, 3, 3 );
    int itemHeight = grid.getItemHeight();

    Point preferredSize = grid.computeSize( SWT.DEFAULT, SWT.DEFAULT );

    assertEquals( 120, preferredSize.x );
    assertEquals( 3 * itemHeight, preferredSize.y );
  }

  public void testComputeSize_WithScrollBars() {
    createGridColumns( grid, 3, SWT.NONE );
    createGridItems( grid, 3, 3 );
    int itemHeight = grid.getItemHeight();
    int scrollbarSize = 10;

    Point preferredSize = grid.computeSize( SWT.DEFAULT, SWT.DEFAULT );

    assertEquals( 120 + scrollbarSize, preferredSize.x );
    assertEquals( 3 * itemHeight + scrollbarSize, preferredSize.y );
  }

  public void testComputeSize_WithBorder() {
    grid = new Grid( shell, SWT.BORDER );
    createGridColumns( grid, 3, SWT.NONE );
    createGridItems( grid, 3, 3 );
    int itemHeight = grid.getItemHeight();
    int borderWidth = grid.getBorderWidth();

    Point preferredSize = grid.computeSize( SWT.DEFAULT, SWT.DEFAULT );

    assertEquals( 120 + 2 * borderWidth, preferredSize.x );
    assertEquals( 3 * itemHeight + 2 * borderWidth, preferredSize.y );
  }

  public void testComputeSize_WithExpandedItems() {
    grid = new Grid( shell, SWT.NONE );
    createGridColumns( grid, 3, SWT.NONE );
    GridItem[] items = createGridItems( grid, 3, 3 );
    items[ 0 ].setExpanded( true );
    items[ 4 ].setExpanded( true );
    int itemHeight = grid.getItemHeight();

    Point preferredSize = grid.computeSize( SWT.DEFAULT, SWT.DEFAULT );

    assertEquals( 120, preferredSize.x );
    assertEquals( 9 * itemHeight, preferredSize.y );
  }

  public void testUpdateScrollBars_Initial() {
    assertFalse( verticalBar.getVisible() );
    assertEquals( 0, verticalBar.getSelection() );
    assertEquals( 1, verticalBar.getMaximum() );
    assertFalse( horizontalBar.getVisible() );
    assertEquals( 0, horizontalBar.getSelection() );
    assertEquals( 1, horizontalBar.getMaximum() );
  }

  public void testUpdateScrollBars() {
    createGridColumns( grid, 5, SWT.NONE );
    createGridItems( grid, 20, 3 );

    assertTrue( verticalBar.getVisible() );
    assertEquals( 0, verticalBar.getSelection() );
    assertEquals( 20, verticalBar.getMaximum() );
    assertTrue( horizontalBar.getVisible() );
    assertEquals( 0, horizontalBar.getSelection() );
    assertEquals( 300, horizontalBar.getMaximum() );
  }

  public void testUpdateScrollBars_OnColumnChange() {
    createGridColumns( grid, 4, SWT.NONE );

    GridColumn column = new GridColumn( grid, SWT.NONE );
    assertTrue( horizontalBar.getVisible() );

    column.dispose();
    assertFalse( horizontalBar.getVisible() );
  }

  public void testUpdateScrollBars_OnColumnWidthChange() {
    createGridColumns( grid, 4, SWT.NONE );

    grid.getColumn( 3 ).setWidth( 90 );
    assertTrue( horizontalBar.getVisible() );

    grid.getColumn( 3 ).setWidth( 70 );
    assertFalse( horizontalBar.getVisible() );
  }

  public void testUpdateScrollBars_OnItemExpandChange() {
    createGridItems( grid, 8, 3 );

    grid.getItem( 0 ).setExpanded( true );
    assertTrue( verticalBar.getVisible() );

    grid.getItem( 0 ).setExpanded( false );
    assertFalse( verticalBar.getVisible() );
  }

  public void testUpdateScrollBars_OnResize() {
    createGridColumns( grid, 5, SWT.NONE );
    createGridItems( grid, 20, 3 );

    grid.setSize( 500, 500 );

    assertFalse( verticalBar.getVisible() );
    assertFalse( horizontalBar.getVisible() );
  }

  public void testUpdateScrollBars_OnHeaderVisible() {
    createGridColumns( grid, 1, SWT.NONE );
    createGridItems( grid, 9, 3 );

    grid.setHeaderVisible( true );

    assertTrue( verticalBar.getVisible() );
  }

  public void testGetTopIndex_Initial() {
    createGridItems( grid, 20, 3 );

    assertEquals( 0, grid.getTopIndex() );
  }

  public void testSetTopIndex() {
    createGridItems( grid, 20, 3 );

    grid.setTopIndex( 4 );

    assertEquals( 4, grid.getTopIndex() );
  }

  public void testSetTopIndex_InvisibleSubItem() {
    createGridItems( grid, 20, 3 );

    grid.setTopIndex( 3 );

    assertEquals( 0, grid.getTopIndex() );
  }

  public void testSetTopIndex_VisibleSubItem() {
    createGridItems( grid, 20, 3 );
    grid.getItem( 4 ).setExpanded( true );

    grid.setTopIndex( 6 );

    assertEquals( 6, grid.getTopIndex() );
  }

  public void testSetTopIndex_AdjustTopIndex() {
    createGridItems( grid, 20, 0 );

    grid.setTopIndex( 18 );

    assertEquals( 11, grid.getTopIndex() );
  }

  public void testAdjustTopIndexOnResize() {
    createGridItems( grid, 20, 3 );
    grid.setTopIndex( 4 );

    grid.setSize( 500, 500 );

    assertEquals( 0, grid.getTopIndex() );
  }

  public void testShowItem_NullArgument() {
    try {
      grid.showItem( null );
      fail();
    } catch( IllegalArgumentException expected ) {
    }
  }

  public void testShowItem_DisposedItem() {
    GridItem item = new GridItem( grid, SWT.NONE );
    item.dispose();

    try {
      grid.showItem( item );
      fail();
    } catch( IllegalArgumentException expected ) {
    }
  }

  public void testShowItem_ScrollDown() {
    GridItem[] items = createGridItems( grid, 20, 3 );

    grid.showItem( items[ 40 ] );

    assertEquals( 40, grid.getTopIndex() );
  }

  public void testShowItem_ScrollUp() {
    GridItem[] items = createGridItems( grid, 20, 3 );
    grid.setTopIndex( 12 );

    grid.showItem( items[ 4 ] );

    assertEquals( 4, grid.getTopIndex() );
  }

  public void testShowItem_SubItemScrollDown() {
    GridItem[] items = createGridItems( grid, 20, 3 );

    grid.showItem( items[ 41 ] );

    assertEquals( 41, grid.getTopIndex() );
    assertTrue( items[ 40 ].isExpanded() );
  }

  public void testShowItem_SubItemScrollUp() {
    GridItem[] items = createGridItems( grid, 20, 3 );
    grid.setTopIndex( 12 );

    grid.showItem( items[ 5 ] );

    assertEquals( 5, grid.getTopIndex() );
    assertTrue( items[ 4 ].isExpanded() );
  }

  public void testShowItem_FireExpandEvent() {
    grid.addListener( SWT.Expand, new LoggingListener() );
    GridItem[] items = createGridItems( grid, 20, 3 );

    grid.showItem( items[ 41 ] );

    assertEquals( 1, eventLog.size() );
    assertSame( items[ 40 ], eventLog.get( 0 ).item );
  }

  //////////////////
  // Helping classes

  private class LoggingListener implements Listener {

    public void handleEvent( Event event ) {
      eventLog.add( event );
    }
  }
}
