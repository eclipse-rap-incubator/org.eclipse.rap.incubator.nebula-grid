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
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TreeAdapter;
import org.eclipse.swt.events.TreeEvent;
import org.eclipse.swt.events.TreeListener;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
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
    createGridColumns( grid, 5 );

    assertEquals( 5, grid.getColumnCount() );
  }

  public void testGetColumns() {
    GridColumn[] columns = createGridColumns( grid, 5 );

    assertTrue( Arrays.equals( columns, grid.getColumns() ) );
  }

  public void testGetColumn() {
    GridColumn[] columns = createGridColumns( grid, 5 );

    assertSame( columns[ 1 ], grid.getColumn( 1 ) );
    assertSame( columns[ 4 ], grid.getColumn( 4 ) );
  }

  public void testGetColumn_InvalidIndex() {
    createGridColumns( grid, 3 );

    try {
      grid.getColumn( 10 );
      fail();
    } catch( IllegalArgumentException expected ) {
    }
  }

  public void testIndexOfColumn() {
    GridColumn[] columns = createGridColumns( grid, 5 );

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
    GridColumn[] columns = createGridColumns( grid, 5 );

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
    GridColumn[] columns = createGridColumns( grid, 2 );

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
    createGridColumns( grid, 3 );
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

  public void testGetCellSelectionEnabled_Initial() {
    assertFalse( grid.getCellSelectionEnabled() );
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

  public void testSetHeaderVisible_Initial() {
    assertFalse( grid.getHeaderVisible() );
  }

  public void testSetHeaderVisible() {
    grid.setHeaderVisible( true );

    assertTrue( grid.getHeaderVisible() );
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

  private static GridColumn[] createGridColumns( Grid grid, int columns ) {
    GridColumn[] result = new GridColumn[ columns ];
    for( int i = 0; i < columns; i++ ) {
      GridColumn column = new GridColumn( grid, SWT.NONE );
      result[ i ] = column;
    }
    return result;
  }
}
