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

import static org.eclipse.nebula.widgets.grid.GridTestUtil.createGridColumns;
import static org.eclipse.nebula.widgets.grid.GridTestUtil.createGridItems;
import static org.eclipse.nebula.widgets.grid.GridTestUtil.loadImage;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

import org.eclipse.nebula.widgets.grid.internal.IGridAdapter;
import org.eclipse.nebula.widgets.grid.internal.NullScrollBarProxy;
import org.eclipse.nebula.widgets.grid.internal.ScrollBarProxyAdapter;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.lifecycle.PhaseId;
import org.eclipse.rap.rwt.testfixture.Fixture;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TreeListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.internal.widgets.ICellToolTipAdapter;
import org.eclipse.swt.internal.widgets.IItemHolderAdapter;
import org.eclipse.swt.internal.widgets.MarkupValidator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Shell;


@SuppressWarnings("restriction")
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

  public void testGetItemByPoint_NullArgument() {
    createGridItems( grid, 3, 1 );

    try {
      grid.getItem( null );
      fail();
    } catch( IllegalArgumentException expected ) {
    }
  }

  public void testGetItemByPoint() {
    GridItem[] items = createGridItems( grid, 10, 0 );

    assertSame( items[ 2 ], grid.getItem( new Point( 10, 60 ) ) );
  }

  public void testGetItemByPoint_WithHeaderVisible() {
    grid.setHeaderVisible( true );
    createGridColumns( grid, 1, SWT.NONE );
    GridItem[] items = createGridItems( grid, 10, 0 );

    assertSame( items[ 1 ], grid.getItem( new Point( 10, 60 ) ) );
  }

  public void testGetItemByPoint_WithinHeader() {
    grid.setHeaderVisible( true );
    createGridColumns( grid, 1, SWT.NONE );
    createGridItems( grid, 10, 0 );

    assertNull( grid.getItem( new Point( 10, 20 ) ) );
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
    DisposeListener listener = mock( DisposeListener.class );
    grid.addDisposeListener( listener );

    grid.dispose();

    verify( listener ).widgetDisposed( any( DisposeEvent.class ) );
  }

  public void testAddRemoveSelectionListener() {
    SelectionListener listener = mock( SelectionListener.class );
    grid.addSelectionListener( listener );

    assertTrue( grid.isListening( SWT.Selection ) );
    assertTrue( grid.isListening( SWT.DefaultSelection ) );

    grid.removeSelectionListener( listener );
    assertFalse( grid.isListening( SWT.Selection ) );
    assertFalse( grid.isListening( SWT.DefaultSelection ) );
  }

  public void testAddRemoveTreeListener() {
    TreeListener listener = mock( TreeListener.class );
    grid.addTreeListener( listener );

    assertTrue( grid.isListening( SWT.Expand ) );
    assertTrue( grid.isListening( SWT.Collapse ) );

    grid.removeTreeListener( listener );
    assertFalse( grid.isListening( SWT.Expand ) );
    assertFalse( grid.isListening( SWT.Collapse ) );
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

  public void testGetFooterVisible_Initial() {
    assertFalse( grid.getFooterVisible() );
  }

  public void testSetFooterVisible() {
    grid.setFooterVisible( true );

    assertTrue( grid.getFooterVisible() );
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

  public void testSetColumnOrder_MoveToColumnGroup() {
    createGridColumns( grid, 2, SWT.NONE );
    GridColumnGroup group = new GridColumnGroup( grid, SWT.NONE );
    createGridColumns( group, 2, SWT.NONE );
    createGridColumns( grid, 2, SWT.NONE );

    try {
      grid.setColumnOrder( new int[]{ 1, 2, 0, 3, 4, 5 } );
      fail();
    } catch( IllegalArgumentException expected ) {
    }
  }

  public void testSetColumnOrder_MoveInSameColumnGroup() {
    createGridColumns( grid, 2, SWT.NONE );
    GridColumnGroup group = new GridColumnGroup( grid, SWT.NONE );
    createGridColumns( group, 2, SWT.NONE );
    createGridColumns( grid, 2, SWT.NONE );

    int[] order = new int[]{ 0, 1, 3, 2, 4, 5 };
    grid.setColumnOrder( order );

    assertTrue( Arrays.equals( order, grid.getColumnOrder() ) );
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

  public void testGetColumnOrder_UpdatePrimaryCheckColumn() {
    grid = new Grid( shell, SWT.CHECK );
    GridColumn[] columns = createGridColumns( grid, 3, SWT.NONE );

    grid.setColumnOrder( new int[] { 2, 0, 1 } );

    assertTrue( columns[ 2 ].isCheck() );
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
    assertEquals( 27, grid.getItemHeight() );
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

    assertEquals( 33, grid.getItemHeight() );
  }

  public void testGetItemHeight_MinHeight() {
    Font font = new Font( display, "Arial", 8, SWT.NORMAL );
    fakeCellPadding( grid, new Rectangle( 0, 0, 0, 0 ) );
    grid.setFont( font );

    assertEquals( 16, grid.getItemHeight() );
  }

  public void testGetItemHeight_WithGridCheck() {
    grid = new Grid( shell, SWT.CHECK );

    assertEquals( 30, grid.getItemHeight() );
  }

  public void testGetItemHeight_WithItemImage() {
    createGridColumns( grid, 3, SWT.NONE );
    GridItem item = new GridItem( grid, SWT.NONE );
    Image image = loadImage( display, Fixture.IMAGE_100x50 );
    // fill the cache
    grid.getItemHeight();

    item.setImage( 1, image );

    assertEquals( 63, grid.getItemHeight() );
  }

  public void testGetItemHeight_AfterClearAll() {
    createGridColumns( grid, 3, SWT.NONE );
    GridItem item = new GridItem( grid, SWT.NONE );
    Image image = loadImage( display, Fixture.IMAGE_100x50 );
    item.setImage( 1, image );

    grid.clearAll( true );

    assertEquals( 27, grid.getItemHeight() );
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

  public void testGetHeaderHeight_WithColumnGroup() {
    grid.setHeaderVisible( true );
    Image image = loadImage( display, Fixture.IMAGE_100x50 );
    GridColumn[] columns = createGridColumns( grid, 1, SWT.NONE );
    columns[ 0 ].setImage( image );
    columns[ 0 ].setText( "foo" );
    GridColumnGroup group = new GridColumnGroup( grid, SWT.NONE );
    group.setImage( image );
    group.setText( "foo" );
    createGridColumns( group, 1, SWT.NONE );

    assertEquals( 134, grid.getHeaderHeight() );
    assertEquals( 67, grid.getGroupHeaderHeight() );
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

  public void testGetFooterHeight_Initial() {
    createGridColumns( grid, 3, SWT.NONE );

    assertEquals( 0, grid.getFooterHeight() );
  }

  public void testGetFooterHeight() {
    grid.setFooterVisible( true );
    GridColumn[] columns = createGridColumns( grid, 3, SWT.NONE );
    Image image = loadImage( display, Fixture.IMAGE_100x50 );
    columns[ 0 ].setFooterImage( image );
    columns[ 1 ].setFooterText( "foo" );

    assertEquals( 67, grid.getFooterHeight() );
  }

  public void testGetFooterHeight_DifferentColumnFooterFonts() {
    grid.setFooterVisible( true );
    GridColumn[] columns = createGridColumns( grid, 3, SWT.NONE );
    columns[ 0 ].setFooterFont( new Font( display, "Arial", 10, SWT.NORMAL ) );
    columns[ 2 ].setFooterFont( new Font( display, "Arial", 20, SWT.NORMAL ) );

    assertEquals( 37, grid.getFooterHeight() );
  }

  public void testGetFooterHeight_AfterColumnDispose() {
    grid.setFooterVisible( true );
    GridColumn[] columns = createGridColumns( grid, 3, SWT.NONE );
    Image image = loadImage( display, Fixture.IMAGE_100x50 );
    columns[ 0 ].setFooterImage( image );
    columns[ 1 ].setFooterText( "foo" );
    // fill the cache
    grid.getFooterHeight();

    columns[ 0 ].dispose();

    assertEquals( 31, grid.getFooterHeight() );
  }

  public void testGetFooterHeight_AfterTextChange() {
    grid.setFooterVisible( true );
    GridColumn[] columns = createGridColumns( grid, 3, SWT.NONE );
    columns[ 1 ].setFooterText( "foo" );
    // fill the cache
    grid.getFooterHeight();

    columns[ 1 ].setFooterText( "foo\nbar" );

    assertEquals( 52, grid.getFooterHeight() );
  }

  public void testGetFooterHeight_AfterImageChange() {
    grid.setFooterVisible( true );
    GridColumn[] columns = createGridColumns( grid, 3, SWT.NONE );
    Image image = loadImage( display, Fixture.IMAGE_100x50 );
    columns[ 0 ].setFooterImage( image );
    columns[ 1 ].setFooterText( "foo" );
    // fill the cache
    grid.getFooterHeight();

    columns[ 0 ].setFooterImage( null );

    assertEquals( 31, grid.getFooterHeight() );
  }

  public void testGetFooterHeight_AfterFontChange() {
    grid.setFooterVisible( true );
    GridColumn[] columns = createGridColumns( grid, 3, SWT.NONE );
    columns[ 1 ].setFooterText( "foo" );
    // fill the cache
    grid.getFooterHeight();

    columns[ 1 ].setFooterFont( new Font( display, "Arial", 20, SWT.NORMAL ) );

    assertEquals( 37, grid.getFooterHeight() );
  }

  public void testGetGroupHeaderHeight_Initial() {
    createGridColumns( grid, 1, SWT.NONE );
    GridColumnGroup group = new GridColumnGroup( grid, SWT.NONE );
    createGridColumns( group, 1, SWT.NONE );

    assertEquals( 0, grid.getGroupHeaderHeight() );
  }

  public void testGetGroupHeaderHeight() {
    grid.setHeaderVisible( true );
    createGridColumns( grid, 1, SWT.NONE );
    GridColumnGroup group = new GridColumnGroup( grid, SWT.NONE );
    createGridColumns( group, 1, SWT.NONE );
    Image image = loadImage( display, Fixture.IMAGE_100x50 );
    group.setImage( image );
    group.setText( "foo" );

    assertEquals( 67, grid.getGroupHeaderHeight() );
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
    doFakeRedraw();

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

    doFakeRedraw();

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
    doFakeRedraw();
    assertTrue( horizontalBar.getVisible() );

    column.dispose();
    doFakeRedraw();
    assertFalse( horizontalBar.getVisible() );
  }

  public void testUpdateScrollBars_OnColumnWidthChange() {
    createGridColumns( grid, 4, SWT.NONE );

    grid.getColumn( 3 ).setWidth( 90 );
    doFakeRedraw();
    assertTrue( horizontalBar.getVisible() );

    grid.getColumn( 3 ).setWidth( 70 );
    doFakeRedraw();
    assertFalse( horizontalBar.getVisible() );
  }

  public void testUpdateScrollBars_OnItemExpandChange() {
    createGridItems( grid, 3, 10 );

    grid.getItem( 0 ).setExpanded( true );
    doFakeRedraw();
    assertTrue( verticalBar.getVisible() );

    grid.getItem( 0 ).setExpanded( false );
    doFakeRedraw();
    assertFalse( verticalBar.getVisible() );
  }

  public void testUpdateScrollBars_OnResize() {
    createGridColumns( grid, 5, SWT.NONE );
    createGridItems( grid, 10, 3 );

    grid.setSize( 500, 500 );
    doFakeRedraw();

    assertFalse( verticalBar.getVisible() );
    assertFalse( horizontalBar.getVisible() );
  }

  public void testUpdateScrollBars_OnHeaderVisible() {
    createGridColumns( grid, 1, SWT.NONE );
    createGridItems( grid, 7, 3 );

    grid.setHeaderVisible( true );
    doFakeRedraw();

    assertTrue( verticalBar.getVisible() );
  }

  public void testUpdateScrollBars_OnFooterVisible() {
    createGridColumns( grid, 1, SWT.NONE );
    createGridItems( grid, 7, 3 );

    grid.setFooterVisible( true );
    doFakeRedraw();

    assertTrue( verticalBar.getVisible() );
  }

  public void testUpdateScrollBars_OnCollapseColumnGroup() {
    grid.setSize( 90, 100 );
    GridColumnGroup group = new GridColumnGroup( grid, SWT.NONE );
    GridColumn[] columns = createGridColumns( group, 3, SWT.NONE );
    columns[ 0 ].setDetail( false );
    columns[ 1 ].setSummary( false );

    group.setExpanded( false );
    doFakeRedraw();

    assertFalse( horizontalBar.getVisible() );
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

    assertEquals( 13, grid.getTopIndex() );
  }

  public void testGetTopIndex_OnItemAdd() {
    createGridItems( grid, 20, 3 );
    grid.setTopIndex( 12 );

    new GridItem( grid, SWT.NONE, 0 );

    assertEquals( 9, grid.getTopIndex() );
  }

  public void testGetTopIndex_DifferentItemHeight() {
    GridItem[] items = createGridItems( grid, 20, 0 );
    items[ 16 ].setHeight( grid.getItemHeight() * 2  );

    grid.setTopIndex( 18 );

    assertEquals( 14, grid.getTopIndex() );
  }

  public void testAdjustTopIndexOnResize() {
    createGridItems( grid, 15, 3 );
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

  public void testShowItem_NoScroll() {
    GridItem[] items = createGridItems( grid, 20, 3 );
    grid.setTopIndex( 12 );

    grid.showItem( items[ 14 ] );

    assertEquals( 12, grid.getTopIndex() );
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

  public void testShowColumn_NullArgument() {
    try {
      grid.showColumn( null );
      fail();
    } catch( IllegalArgumentException expected ) {
    }
  }

  public void testShowColumn_DisposedColumn() {
    GridColumn column = new GridColumn( grid, SWT.NONE );
    column.dispose();

    try {
      grid.showColumn( column );
      fail();
    } catch( IllegalArgumentException expected ) {
    }
  }

  public void testShowColumn_ScrollRight() {
    GridColumn[] columns = createGridColumns( grid, 10, SWT.NONE );

    grid.showColumn( columns[ 4 ] );

    assertEquals( 100, horizontalBar.getSelection() );
  }

  public void testShowColumn_ScrollLeft() {
    GridColumn[] columns = createGridColumns( grid, 10, SWT.NONE );
    horizontalBar.setSelection( 150 );

    grid.showColumn( columns[ 2 ] );

    assertEquals( 60, horizontalBar.getSelection() );
  }

  public void testShowColumn_NoScroll() {
    GridColumn[] columns = createGridColumns( grid, 10, SWT.NONE );
    horizontalBar.setSelection( 30 );

    grid.showColumn( columns[ 2 ] );

    assertEquals( 30, horizontalBar.getSelection() );
  }

  public void testShowColumn_FireExpandEvent() {
    GridColumnGroup group = new GridColumnGroup( grid, SWT.NONE );
    group.addListener( SWT.Collapse, new LoggingListener() );
    GridColumn[] columns = createGridColumns( group, 10, SWT.NONE );
    columns[ 0 ].setDetail( false );
    columns[ 1 ].setSummary( false );

    grid.showColumn( columns[ 0 ] );

    assertEquals( 1, eventLog.size() );
    assertSame( group, eventLog.get( 0 ).widget );
  }

  public void testShowSelection() {
    grid = new Grid( shell, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL );
    grid.setSize( 200, 200 );
    createGridItems( grid, 20, 3 );
    grid.setSelection( new int[]{ 4, 8, 24 } );
    grid.setTopIndex( 12 );

    grid.showSelection();

    assertEquals( 4, grid.getTopIndex() );
  }

  public void testGetOrigin() {
    GridColumn[] columns = createGridColumns( grid, 10, SWT.NONE );
    GridItem[] items = createGridItems( grid, 20, 3 );
    horizontalBar.setSelection( 150 );
    grid.setTopIndex( 40 );

    Point expected = new Point( -30, 2 * grid.getItemHeight() );
    assertEquals( expected, grid.getOrigin( columns[ 3 ], items[ 48 ] ) );
  }

  public void testGetOrigin_SubItems() {
    GridColumn[] columns = createGridColumns( grid, 10, SWT.NONE );
    GridItem[] items = createGridItems( grid, 20, 3 );
    items[ 40 ].setExpanded( true );
    horizontalBar.setSelection( 150 );
    grid.setTopIndex( 40 );

    Point expected = new Point( -30, 5 * grid.getItemHeight() );
    assertEquals( expected, grid.getOrigin( columns[ 3 ], items[ 48 ] ) );
  }

  public void testGetOrigin_HeaderVisible() {
    GridColumn[] columns = createGridColumns( grid, 10, SWT.NONE );
    GridItem[] items = createGridItems( grid, 20, 3 );
    horizontalBar.setSelection( 150 );
    grid.setHeaderVisible( true );
    grid.setTopIndex( 40 );

    Point expected = new Point( -30, 2 * grid.getItemHeight() + grid.getHeaderHeight() );
    assertEquals( expected, grid.getOrigin( columns[ 3 ], items[ 48 ] ) );
  }

  public void testIsShown() {
    GridItem[] items = createGridItems( grid, 20, 0 );
    grid.setTopIndex( 5 );

    assertTrue( grid.isShown( items[ 6 ] ) );
  }

  public void testIsShown_HiddenItem() {
    GridItem[] items = createGridItems( grid, 20, 0 );
    grid.setTopIndex( 5 );

    assertFalse( grid.isShown( items[ 4 ] ) );
  }

  public void testIsShown_InvisibleItem() {
    GridItem[] items = createGridItems( grid, 20, 3 );
    grid.setTopIndex( 20 );

    assertFalse( grid.isShown( items[ 22 ] ) );
  }

  public void testIsShown_PartlyVisibleItem() {
    GridItem[] items = createGridItems( grid, 20, 0 );
    grid.setTopIndex( 7 );

    assertTrue( grid.isShown( items[ 13 ] ) );
    assertFalse( grid.isShown( items[ 14 ] ) );
  }

  public void testGetAdapter_IGridAdapter() {
    assertNotNull( grid.getAdapter( IGridAdapter.class ) );
  }

  public void testGetAdapter_IItemHolderAdapter() {
    assertNotNull( grid.getAdapter( IItemHolderAdapter.class ) );
  }

  public void testIItemHolderAdapter_GetItems() {
    GridColumnGroup group = new GridColumnGroup( grid, SWT.NONE );
    GridColumn column = new GridColumn( group, SWT.NONE );
    GridItem item = new GridItem( grid, SWT.NONE );

    Item[] items = grid.getAdapter( IItemHolderAdapter.class ).getItems();

    assertSame( group, items[ 0 ] );
    assertSame( column, items[ 1 ] );
    assertSame( item, items[ 2 ] );
  }

  public void testGetAdapter_ICellToolTipAdapter() {
    assertNotNull( grid.getAdapter( ICellToolTipAdapter.class ) );
  }

  public void testICellToolTipAdapter_hasCellToolTipProvider() {
    assertNotNull( grid.getAdapter( ICellToolTipAdapter.class ).getCellToolTipProvider() );
  }

  public void testICellToolTipAdapter_GetCellToolTipText() {
    createGridColumns( grid, 3, SWT.NONE );
    GridItem[] items = createGridItems( grid, 3, 0 );
    items[ 1 ].setToolTipText( 1, "foo" );

    ICellToolTipAdapter cellToolTipAdapter = grid.getAdapter( ICellToolTipAdapter.class );
    cellToolTipAdapter.getCellToolTipProvider().getToolTipText( items[ 1 ], 1 );

    assertEquals( "foo", cellToolTipAdapter.getCellToolTipText() );
  }

  public void testColumnGroup_Initial() {
    assertEquals( 0, grid.getColumnGroupCount() );
  }

  public void testGetColumnGroupCount_AddGroup() {
    new GridColumnGroup( grid, SWT.NONE );

    assertEquals( 1, grid.getColumnGroupCount() );
  }

  public void testGetColumnGroupCount_RemoveGroup() {
    GridColumnGroup group = new GridColumnGroup( grid, SWT.NONE );

    group.dispose();

    assertEquals( 0, grid.getColumnGroupCount() );
  }

  public void testGetColumnGroups() {
    GridColumnGroup group1 = new GridColumnGroup( grid, SWT.NONE );
    GridColumnGroup group2 = new GridColumnGroup( grid, SWT.NONE );

    GridColumnGroup[] expected = new GridColumnGroup[] { group1,  group2 };
    assertTrue( Arrays.equals( expected, grid.getColumnGroups() ) );
  }

  public void testGetColumnGroup() {
    GridColumnGroup group1 = new GridColumnGroup( grid, SWT.NONE );
    GridColumnGroup group2 = new GridColumnGroup( grid, SWT.NONE );

    assertSame( group1, grid.getColumnGroup( 0 ) );
    assertSame( group2, grid.getColumnGroup( 1 ) );
  }

  public void testGetColumnGroup_InvalidIndex() {
    try{
      grid.getColumnGroup( 3 );
      fail();
    } catch( IllegalArgumentException expected ) {
    }
  }

  public void testDisposeColumnGroupOnGridDispose() {
    GridColumnGroup group = new GridColumnGroup( grid, SWT.NONE );

    grid.dispose();

    assertTrue( group.isDisposed() );
  }

  public void testCheckBoxLeftOffset() {
    GridColumn[] columns = createGridColumns( grid, 2, SWT.CHECK );
    columns[ 0 ].setWidth( 100 );
    columns[ 1 ].setWidth( 100 );
    createGridItems( grid, 1, 1 );

    assertEquals( 0, getCheckBoxOffset( 0 ) );
    assertEquals( 6, getCheckBoxOffset( 1 ) );
  }

  public void testCheckBoxLeftOffset_CenteredWithoutContent() {
    GridColumn[] columns = createGridColumns( grid, 2, SWT.CHECK | SWT.CENTER );
    columns[ 0 ].setWidth( 100 );
    columns[ 1 ].setWidth( 100 );
    createGridItems( grid, 1, 1 );

    assertEquals( 0, getCheckBoxOffset( 0 ) );
    assertEquals( 39, getCheckBoxOffset( 1 ) );
  }

  public void testCheckBoxLeftOffset_CenteredWithContent() {
    GridColumn[] columns = createGridColumns( grid, 2, SWT.CHECK | SWT.CENTER );
    columns[ 0 ].setWidth( 100 );
    columns[ 1 ].setWidth( 100 );
    createGridItems( grid, 1, 1 );
    grid.getRootItem( 0 ).setText( 1, "foo" );

    assertEquals( 0, getCheckBoxOffset( 0 ) );
    assertEquals( 6, getCheckBoxOffset( 1 ) );
  }

  public void testGetBottomIndex_SameItemHeight() {
    createGridItems( grid, 20, 0 );

    grid.setTopIndex( 4 );

    assertEquals( 11, grid.getBottomIndex() );
  }

  public void testGetBottomIndex_DifferentItemHeight() {
    GridItem[] items = createGridItems( grid, 20, 0 );
    items[ 6 ].setHeight( grid.getItemHeight() * 2  );

    grid.setTopIndex( 4 );

    assertEquals( 10, grid.getBottomIndex() );
  }

  public void testMarkupTextWithoutMarkupEnabled() {
    grid.setData( RWT.MARKUP_ENABLED, Boolean.FALSE );
    GridItem item = new GridItem( grid, SWT.NONE );

    try {
      item.setText( "invalid xhtml: <<&>>" );
    } catch( IllegalArgumentException notExpected ) {
      fail();
    }
  }

  public void testMarkupTextWithMarkupEnabled() {
    grid.setData( RWT.MARKUP_ENABLED, Boolean.TRUE );
    GridItem item = new GridItem( grid, SWT.NONE );

    try {
      item.setText( "invalid xhtml: <<&>>" );
      fail();
    } catch( IllegalArgumentException expected ) {
    }
  }

  public void testMarkupTextWithMarkupEnabled_ValidationDisabled() {
    grid.setData( RWT.MARKUP_ENABLED, Boolean.TRUE );
    grid.setData( MarkupValidator.MARKUP_VALIDATION_DISABLED, Boolean.TRUE );
    GridItem item = new GridItem( grid, SWT.NONE );

    try {
      item.setText( "invalid xhtml: <<&>>" );
    } catch( IllegalArgumentException notExpected ) {
      fail();
    }
  }

  public void testDisableMarkupIsIgnored() {
    grid.setData( RWT.MARKUP_ENABLED, Boolean.TRUE );

    grid.setData( RWT.MARKUP_ENABLED, Boolean.FALSE );

    assertEquals( Boolean.TRUE, grid.getData( RWT.MARKUP_ENABLED ) );
  }

  //////////////////
  // Helping methods

  private void doFakeRedraw() {
    grid.getAdapter( IGridAdapter.class ).doRedraw();
  }

  private void fakeCellPadding( Grid grid, Rectangle padding ) {
    grid.layoutCache.cellPadding = padding;
  }

  private int getCheckBoxOffset( int index ) {
    return grid.getAdapter( IGridAdapter.class ).getCheckBoxOffset( index );
  }

  //////////////////
  // Helping classes

  private class LoggingListener implements Listener {

    public void handleEvent( Event event ) {
      eventLog.add( event );
    }
  }
}
