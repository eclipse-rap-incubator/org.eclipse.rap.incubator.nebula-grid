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
import static org.eclipse.nebula.widgets.grid.GridTestUtil.loadImage;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.rap.rwt.testfixture.Fixture;
import org.eclipse.rwt.internal.service.ContextProvider;
import org.eclipse.rwt.lifecycle.PhaseId;
import org.eclipse.rwt.service.IServiceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import junit.framework.TestCase;


@SuppressWarnings("restriction")
public class GridColumn_Test extends TestCase {

  private Display display;
  private Shell shell;
  private Grid grid;
  private List<Event> eventLog;

  @Override
  protected void setUp() throws Exception {
    Fixture.setUp();
    Fixture.fakePhase( PhaseId.PROCESS_ACTION );
    display = new Display();
    shell = new Shell( display );
    grid = new Grid( shell, SWT.H_SCROLL | SWT.V_SCROLL );
    eventLog = new ArrayList<Event>();
  }

  @Override
  protected void tearDown() throws Exception {
    Fixture.tearDown();
  }

  public void testGridColumnCreation_GridParent() {
    GridColumn column = new GridColumn( grid, SWT.NONE );

    assertSame( grid, column.getParent() );
    assertSame( column, grid.getColumn( 0 ) );
    assertEquals( 1, grid.getColumnCount() );
  }

  public void testGridColumnCreation_AtIndexWithGridParent() {
    createGridColumns( grid, 5, SWT.NONE );

    GridColumn column = new GridColumn( grid, SWT.NONE, 2 );

    assertSame( column, grid.getColumn( 2 ) );
    assertEquals( 2, grid.indexOf( column ) );
    assertEquals( 6, grid.getColumnCount() );
  }

  public void testDispose() {
    GridColumn column = new GridColumn( grid, SWT.NONE );

    column.dispose();

    assertTrue( column.isDisposed() );
    assertEquals( 0, grid.getColumnCount() );
  }

  public void testSendDisposeEvent() {
    final List<DisposeEvent> log = new ArrayList<DisposeEvent>();
    GridColumn column = new GridColumn( grid, SWT.NONE );
    column.addDisposeListener( new DisposeListener() {
      public void widgetDisposed( DisposeEvent event ) {
        log.add( event );
      }
    } );

    column.dispose();

    assertEquals( 1, log.size() );
    assertSame( column, log.get( 0 ).widget );
  }

  public void testSendDisposeEventOnGridDispose() {
    final List<DisposeEvent> log = new ArrayList<DisposeEvent>();
    GridColumn column = new GridColumn( grid, SWT.NONE );
    column.addDisposeListener( new DisposeListener() {
      public void widgetDisposed( DisposeEvent event ) {
        log.add( event );
      }
    } );

    grid.dispose();

    assertEquals( 1, log.size() );
    assertSame( column, log.get( 0 ).widget );
  }

  public void testIsCheck() {
    GridColumn column1 = new GridColumn( grid, SWT.NONE );
    GridColumn column2 = new GridColumn( grid, SWT.CHECK );
    GridColumn column3 = new GridColumn( grid, SWT.NONE );

    assertFalse( column1.isCheck() );
    assertTrue( column2.isCheck() );
    assertFalse( column3.isCheck() );
  }

  public void testIsCheck_TableCheck() {
    grid = new Grid( shell, SWT.CHECK );
    GridColumn column1 = new GridColumn( grid, SWT.NONE );
    GridColumn column2 = new GridColumn( grid, SWT.CHECK );
    GridColumn column3 = new GridColumn( grid, SWT.NONE );

    assertTrue( column1.isTableCheck() );
    assertTrue( column1.isCheck() );
    assertFalse( column2.isTableCheck() );
    assertTrue( column2.isCheck() );
    assertFalse( column3.isTableCheck() );
    assertFalse( column3.isCheck() );
  }

  public void testIsCheck_OnColumnAddRemove() {
    grid = new Grid( shell, SWT.CHECK );
    GridColumn[] columns = createGridColumns( grid, 3, SWT.NONE );

    GridColumn column = new GridColumn( grid, SWT.NONE, 0 );
    assertTrue( column.isCheck() );
    assertFalse( columns[ 0 ].isCheck() );

    column.dispose();
    assertTrue( columns[ 0 ].isCheck() );
  }

  public void testGetCheckable_Initial() {
    GridColumn column = new GridColumn( grid, SWT.NONE );

    assertTrue( column.getCheckable() );
  }

  public void testGetCheckable() {
    GridColumn column = new GridColumn( grid, SWT.NONE );

    column.setCheckable( false );

    assertFalse( column.getCheckable() );
  }

  public void testGetWidth_Initial() {
    GridColumn column = new GridColumn( grid, SWT.NONE );

    assertEquals( 10, column.getWidth() );
  }

  public void testGetWidth() {
    GridColumn column = new GridColumn( grid, SWT.NONE );

    column.setWidth( 100 );

    assertEquals( 100, column.getWidth() );
  }

  public void testSetWidth_BelowMinimumWidth() {
    GridColumn column = new GridColumn( grid, SWT.NONE );

    column.setMinimumWidth( 20 );
    column.setWidth( 10 );

    assertEquals( 20, column.getWidth() );
  }

  public void testGetMinimumWidth_Initial() {
    GridColumn column = new GridColumn( grid, SWT.NONE );

    assertEquals( 0, column.getMinimumWidth() );
  }

  public void testGetMinimumWidth() {
    GridColumn column = new GridColumn( grid, SWT.NONE );

    column.setMinimumWidth( 10 );

    assertEquals( 10, column.getMinimumWidth() );
  }

  public void testSetMinimumWidth_AdjustWidth() {
    GridColumn column = new GridColumn( grid, SWT.NONE );
    column.setWidth( 10 );

    column.setMinimumWidth( 20 );

    assertEquals( 20, column.getWidth() );
  }

  public void testGetSort_Initial() {
    GridColumn column = new GridColumn( grid, SWT.NONE );

    assertEquals( SWT.NONE, column.getSort() );
  }

  public void testGetSort() {
    GridColumn column = new GridColumn( grid, SWT.NONE );

    column.setSort( SWT.DOWN );

    assertEquals( SWT.DOWN, column.getSort() );
  }

  public void testSetSort_OnlyOneSortColumn() {
    GridColumn[] columns = createGridColumns( grid, 3, SWT.NONE );
    columns[ 0 ].setSort( SWT.UP );

    columns[ 1 ].setSort( SWT.DOWN );

    assertEquals( SWT.NONE, columns[ 0 ].getSort() );
  }

  public void testGetVisible_Initial() {
    GridColumn column = new GridColumn( grid, SWT.NONE );

    assertTrue( column.getVisible() );
    assertTrue( column.isVisible() );
  }

  public void testGetVisible() {
    GridColumn column = new GridColumn( grid, SWT.NONE );

    column.setVisible( false );

    assertFalse( column.getVisible() );
  }

  public void testSetVisible_FireHideEvent() {
    GridColumn column = new GridColumn( grid, SWT.NONE );
    column.addListener( SWT.Hide, new LoggingListener() );

    column.setVisible( false );

    assertEquals( 1, eventLog.size() );
    Event event = eventLog.get( 0 );
    assertEquals( SWT.Hide, event.type );
    assertSame( column, event.widget );
  }

  public void testSetVisible_FireShowEvent() {
    GridColumn column = new GridColumn( grid, SWT.NONE );
    column.setVisible( false );
    column.addListener( SWT.Show, new LoggingListener() );

    column.setVisible( true );

    assertEquals( 1, eventLog.size() );
    Event event = eventLog.get( 0 );
    assertEquals( SWT.Show, event.type );
    assertSame( column, event.widget );
  }

  public void testSetVisible_FireMoveEventOnNextColumns() {
    GridColumn[] columns = createGridColumns( grid, 3, SWT.NONE );
    columns[ 0 ].addListener( SWT.Move, new LoggingListener() );
    columns[ 1 ].addListener( SWT.Move, new LoggingListener() );
    columns[ 2 ].addListener( SWT.Move, new LoggingListener() );

    columns[ 1 ].setVisible( false );

    assertEquals( 1, eventLog.size() );
    Event event = eventLog.get( 0 );
    assertSame( columns[ 2 ], event.widget );
  }

  public void testSetVisible_FireEventOnlyOnce() {
    GridColumn column = new GridColumn( grid, SWT.NONE );
    column.addListener( SWT.Hide, new LoggingListener() );

    column.setVisible( false );
    column.setVisible( false );

    assertEquals( 1, eventLog.size() );
  }

  public void testIsTree_Initial() {
    GridColumn column = new GridColumn( grid, SWT.NONE );

    assertFalse( column.isTree() );
  }

  public void testIsTree() {
    GridColumn column = new GridColumn( grid, SWT.NONE );

    column.setTree( true );

    assertTrue( column.isTree() );
  }

  public void testGetAlignment_Initial() {
    GridColumn column = new GridColumn( grid, SWT.NONE );

    assertEquals( SWT.LEFT, column.getAlignment() );
  }

  public void testGetAlignment_WithStyleFlag() {
    GridColumn column = new GridColumn( grid, SWT.RIGHT );

    assertEquals( SWT.RIGHT, column.getAlignment() );
  }

  public void testSetAlignment() {
    GridColumn column = new GridColumn( grid, SWT.NONE );

    column.setAlignment( SWT.CENTER );

    assertEquals( SWT.CENTER, column.getAlignment() );
  }

  public void testSetAlignment_InvalidValue() {
    GridColumn column = new GridColumn( grid, SWT.NONE );

    column.setAlignment( SWT.UP );

    assertEquals( SWT.LEFT, column.getAlignment() );
  }

  public void testGetMoveable_Initial() {
    GridColumn column = new GridColumn( grid, SWT.NONE );

    assertFalse( column.getMoveable() );
  }

  public void testGetMoveable() {
    GridColumn column = new GridColumn( grid, SWT.NONE );

    column.setMoveable( true );

    assertTrue( column.getMoveable() );
  }

  public void testGetResizeable_Initial() {
    GridColumn column = new GridColumn( grid, SWT.NONE );

    assertTrue( column.getResizeable() );
  }

  public void testGetResizeable() {
    GridColumn column = new GridColumn( grid, SWT.NONE );

    column.setResizeable( false );

    assertFalse( column.getResizeable() );
  }

  public void testAddRemoveSelectionListener() {
    GridColumn column = new GridColumn( grid, SWT.NONE );
    SelectionListener listener = new SelectionAdapter() { };
    assertFalse( SelectionEvent.hasListener( column ) );

    column.addSelectionListener( listener );
    assertTrue( SelectionEvent.hasListener( column ) );

    column.removeSelectionListener( listener );
    assertFalse( SelectionEvent.hasListener( column ) );
  }

  public void testAddSelectionListener_NullArgument() {
    GridColumn column = new GridColumn( grid, SWT.NONE );

    try {
      column.addSelectionListener( null );
      fail();
    } catch( IllegalArgumentException expected ) {
    }
  }

  public void testRemoveSelectionListener_NullArgument() {
    GridColumn column = new GridColumn( grid, SWT.NONE );

    try {
      column.removeSelectionListener( null );
      fail();
    } catch( IllegalArgumentException expected ) {
    }
  }

  public void testAddRemoveControlListener() {
    GridColumn column = new GridColumn( grid, SWT.NONE );
    ControlListener listener = new ControlAdapter() { };
    assertFalse( ControlEvent.hasListener( column ) );

    column.addControlListener( listener );
    assertTrue( ControlEvent.hasListener( column ) );

    column.removeControlListener( listener );
    assertFalse( ControlEvent.hasListener( column ) );
  }

  public void testAddControlListener_NullArgument() {
    GridColumn column = new GridColumn( grid, SWT.NONE );

    try {
      column.addControlListener( null );
      fail();
    } catch( IllegalArgumentException expected ) {
    }
  }

  public void testRemoveControlListener_NullArgument() {
    GridColumn column = new GridColumn( grid, SWT.NONE );

    try {
      column.removeControlListener( null );
      fail();
    } catch( IllegalArgumentException expected ) {
    }
  }

  public void testGetHeaderText_Initial() {
    GridColumn column = new GridColumn( grid, SWT.NONE );

    assertEquals( "", column.getText() );
  }

  public void testGetHeaderText() {
    GridColumn column = new GridColumn( grid, SWT.NONE );

    column.setText( "foo" );

    assertEquals( "foo", column.getText() );
  }

  public void testSetHeaderText_NullArgument() {
    GridColumn column = new GridColumn( grid, SWT.NONE );

    try {
      column.setText( null );
      fail();
    } catch( IllegalArgumentException expected ) {
    }
  }

  public void testGetHeaderImage_Initial() {
    GridColumn column = new GridColumn( grid, SWT.NONE );

    assertNull( column.getImage() );
  }

  public void testGetHeaderImage() {
    GridColumn column = new GridColumn( grid, SWT.NONE );
    Image image = loadImage( display, Fixture.IMAGE1 );

    column.setImage( image );

    assertSame( image, column.getImage() );
  }

  public void testSetHeaderImage_DisposedImage() {
    GridColumn column = new GridColumn( grid, SWT.NONE );
    Image image = loadImage( display, Fixture.IMAGE1 );
    image.dispose();

    try {
      column.setImage( image );
      fail();
    } catch( IllegalArgumentException expected ) {
    }
  }

  public void testGetHeaderFont_Initial() {
    GridColumn column = new GridColumn( grid, SWT.NONE );

    assertSame( grid.getFont(), column.getHeaderFont() );
  }

  public void testGetHeaderFont() {
    GridColumn column = new GridColumn( grid, SWT.NONE );
    Font font = new Font( display, "Arial", 20, SWT.BOLD );

    column.setHeaderFont( font );

    assertSame( font, column.getHeaderFont() );
  }

  public void testSetHeaderFont_DisposedFont() {
    GridColumn column = new GridColumn( grid, SWT.NONE );
    Font font = new Font( display, "Arial", 20, SWT.BOLD );
    font.dispose();

    try {
      column.setHeaderFont( font );
      fail();
    } catch( IllegalArgumentException expected ) {
    }
  }

  public void testPack_TreeColumnEmpty() {
    GridColumn[] columns = createGridColumns( grid, 2, SWT.NONE );
    GridItem item = new GridItem( grid, SWT.NONE );
    item.setExpanded( true );
    new GridItem( item, SWT.NONE );

    columns[ 0 ].pack();

    assertEquals( 38, columns[ 0 ].getWidth() );
  }

  public void testPack_NonTreeColumnEmpty() {
    GridColumn[] columns = createGridColumns( grid, 2, SWT.NONE );
    GridItem item = new GridItem( grid, SWT.NONE );
    item.setExpanded( true );
    new GridItem( item, SWT.NONE );

    columns[ 1 ].pack();

    assertEquals( 12, columns[ 1 ].getWidth() );
  }

  public void testPack_TreeColumn() {
    GridColumn[] columns = createGridColumns( grid, 2, SWT.CHECK );
    Image image = loadImage( display, Fixture.IMAGE1 );
    GridItem item = new GridItem( grid, SWT.NONE );
    item.setExpanded( true );
    item.setImage( 0, image );
    item.setText( 0, "foo" );
    GridItem subitem = new GridItem( item, SWT.NONE );
    subitem.setImage( 0, image );
    subitem.setText( 0, "foo" );

    columns[ 0 ].pack();

    assertEquals( 139, columns[ 0 ].getWidth() );
  }

  public void testPack_NonTreeColumn() {
    GridColumn[] columns = createGridColumns( grid, 2, SWT.CHECK );
    Image image = loadImage( display, Fixture.IMAGE1 );
    GridItem item = new GridItem( grid, SWT.NONE );
    item.setExpanded( true );
    item.setImage( 1, image );
    item.setText( 1, "foo" );
    GridItem subitem = new GridItem( item, SWT.NONE );
    subitem.setImage( 1, image );
    subitem.setText( 1, "foo" );

    columns[ 1 ].pack();

    assertEquals( 113, columns[ 1 ].getWidth() );
  }

  public void testPack_WithHeaderVisible() {
    grid.setHeaderVisible( true );
    GridColumn[] columns = createGridColumns( grid, 2, SWT.CHECK );
    Image image = loadImage( display, Fixture.IMAGE1 );
    GridItem item = new GridItem( grid, SWT.NONE );
    item.setExpanded( true );
    item.setImage( 0, image );
    item.setText( 0, "foo" );
    GridItem subitem = new GridItem( item, SWT.NONE );
    subitem.setImage( 0, image );
    subitem.setText( 0, "foo" );
    columns[ 0 ].setImage( image );
    columns[ 0 ].setText( "Column header text wider than its content" );

    columns[ 0 ].pack();

    assertEquals( 353, columns[ 0 ].getWidth() );
  }

  public void testRepackAfterTextSizeDetermination() {
    grid.setHeaderVisible( true );
    GridColumn column = new GridColumn( grid, SWT.NONE );
    column.setText( "foo" );
    column.pack();
    int packedWidth = column.getWidth();

    // simulate TSD resize
    markTemporaryResize( true);
    grid.setSize( 1000, 1000 );
    // change preferred size
    column.setText( "foo bar" );
    markTemporaryResize( false);
    grid.setSize( 100, 100 );
    int repackedWidth = column.getWidth();

    assertTrue( repackedWidth > packedWidth );
  }

  public void testGetHeaderTooltip_Initial() {
    GridColumn column = new GridColumn( grid, SWT.NONE );

    assertNull( column.getHeaderTooltip() );
  }

  public void testGetHeaderTooltip() {
    GridColumn column = new GridColumn( grid, SWT.NONE );

    column.setHeaderTooltip( "foo" );

    assertEquals( "foo", column.getHeaderTooltip() );
  }

  //////////////////
  // Helping methods

  private void markTemporaryResize( boolean value ) {
    IServiceStore serviceStore = ContextProvider.getServiceStore();
    String key = "org.eclipse.rwt.internal.textsize.TextSizeRecalculation#temporaryResize";
    serviceStore.setAttribute( key, Boolean.valueOf( value ) );
  }

  //////////////////
  // Helping classes

  private class LoggingListener implements Listener {
    public void handleEvent( Event event ) {
      eventLog.add( event );
    }
  }
}
