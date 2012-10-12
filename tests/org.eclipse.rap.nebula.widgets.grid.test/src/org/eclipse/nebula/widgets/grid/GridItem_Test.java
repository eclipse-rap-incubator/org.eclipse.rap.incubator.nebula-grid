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

import org.eclipse.rap.rwt.lifecycle.PhaseId;
import org.eclipse.rap.rwt.testfixture.Fixture;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import junit.framework.TestCase;


public class GridItem_Test extends TestCase {

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
    grid.setSize( 200, 200 );
    eventLog = new ArrayList<Event>();
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
    GridItem[] items = createGridItems( grid, 1, 1 );
    items[ 0 ].addListener( SWT.Dispose, new LoggingListener() );

    items[ 0 ].dispose();

    assertEquals( 1, eventLog.size() );
    assertSame( items[ 0 ], eventLog.get( 0 ).widget );
  }

  public void testSendDisposeEventOnGridDispose() {
    GridItem[] items = createGridItems( grid, 1, 1 );
    items[ 0 ].addListener( SWT.Dispose, new LoggingListener() );

    grid.dispose();

    assertEquals( 1, eventLog.size() );
    assertSame( items[ 0 ], eventLog.get( 0 ).widget );
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

  public void testSetExpanded_ChangedFocusItem() {
    GridItem[] items = createGridItems( grid, 1, 1 );
    items[ 0 ].setExpanded( true );
    grid.setFocusItem( items[ 1 ] );

    items[ 0 ].setExpanded( false );

    assertSame( items[ 0 ], grid.getFocusItem() );
  }

  public void testSetExpanded_ChangeSelection() {
    GridItem[] items = createGridItems( grid, 1, 1 );
    items[ 0 ].setExpanded( true );
    grid.setSelection( 1 );

    items[ 0 ].setExpanded( false );

    assertFalse( grid.isSelected( 1 ) );
  }

  public void testSetExpanded_FireSelectionEvent() {
    GridItem[] items = createGridItems( grid, 1, 1 );
    items[ 0 ].setExpanded( true );
    grid.setSelection( 1 );
    grid.addListener( SWT.Selection, new LoggingListener() );

    items[ 0 ].setExpanded( false );

    assertEquals( 1, eventLog.size() );
    Event event = eventLog.get( 0 );
    assertSame( items[ 0 ], event.item );
  }

  public void testIsVisibleOnCreation() {
    GridItem[] items = createGridItems( grid, 1, 1 );
    items[ 0 ].setExpanded( true );

    new GridItem( items[ 0 ], SWT.NONE );

    assertTrue( items[ 1 ].isVisible() );
  }

  public void testFireEvent() {
    GridItem item = new GridItem( grid, SWT.NONE );
    grid.addListener( SWT.Expand, new LoggingListener() );

    item.fireEvent( SWT.Expand );

    assertEquals( 1, eventLog.size() );
    Event event = eventLog.get( 0 );
    assertSame( item.getDisplay(), event.display );
    assertSame( grid, event.widget );
    assertSame( item, event.item );
  }

  public void testFireCheckEvent() {
    GridItem item = new GridItem( grid, SWT.NONE );
    grid.addListener( SWT.Selection, new LoggingListener() );

    item.fireCheckEvent( 3 );

    assertEquals( 1, eventLog.size() );
    Event event = eventLog.get( 0 );
    assertSame( item.getDisplay(), event.display );
    assertSame( grid, event.widget );
    assertSame( item, event.item );
    assertEquals( SWT.CHECK, event.detail );
    assertEquals( 3, event.index );
  }

  public void testGetText_Inital() {
    GridItem item = new GridItem( grid, SWT.NONE );

    assertEquals( "", item.getText() );
  }

  public void testGetText_InvalidColumn() {
    GridItem item = new GridItem( grid, SWT.NONE );

    try {
      item.getText( 5 );
      fail();
    } catch( IndexOutOfBoundsException expected ) {
    }
  }

  public void testGetText_AfterSet() {
    GridItem item = new GridItem( grid, SWT.NONE );
    item.setText( "foo" );

    assertEquals( "foo", item.getText() );
  }

  public void testGetText_WithColumns() {
    GridItem item = new GridItem( grid, SWT.NONE );
    createGridColumns( grid, 3, SWT.NONE );
    item.setText( 0, "0" );
    item.setText( 1, "1" );
    item.setText( 2, "2" );

    assertEquals( "0", item.getText( 0 ) );
    assertEquals( "1", item.getText( 1 ) );
    assertEquals( "2", item.getText( 2 ) );
  }

  public void testGetText_AfterAddColumn() {
    GridItem item = new GridItem( grid, SWT.NONE );
    createGridColumns( grid, 1, SWT.NONE );
    item.setText( "foo" );
    new GridColumn( grid, SWT.NONE, 0 );

    assertEquals( "", item.getText( 0 ) );
    assertEquals( "foo", item.getText( 1 ) );
  }

  public void testGetText_AfterRemoveColumn() {
    GridItem item = new GridItem( grid, SWT.NONE );
    GridColumn[] columns = createGridColumns( grid, 2, SWT.NONE );
    item.setText( 1, "foo" );

    columns[ 0 ].dispose();

    assertEquals( "foo", item.getText( 0 ) );
  }

  public void testGetText_AfterRemoveAllColumns() {
    GridItem item = new GridItem( grid, SWT.NONE );
    GridColumn[] columns = createGridColumns( grid, 2, SWT.NONE );
    item.setText( 1, "foo" );

    columns[ 0 ].dispose();
    columns[ 1 ].dispose();

    assertEquals( "foo", item.getText( 0 ) );
  }

  public void testSetText_InvalidColumn() {
    GridItem item = new GridItem( grid, SWT.NONE );

    try {
      item.setText( 5, "foo" );
      fail();
    } catch( IndexOutOfBoundsException expected ) {
    }
  }

  public void testSetText_NullArgument() {
    GridItem item = new GridItem( grid, SWT.NONE );

    try {
      item.setText( 5, null );
      fail();
    } catch( IllegalArgumentException expected ) {
    }
  }

  public void testHandleVirtual_RootItem() {
    grid = new Grid( shell, SWT.VIRTUAL );
    GridItem[] items = createGridItems( grid, 3, 3 );
    grid.addListener( SWT.SetData, new LoggingListener() );

    items[ 4 ].getText();

    assertEquals( 1, eventLog.size() );
    Event event = eventLog.get( 0 );
    assertSame( grid, event.widget );
    assertSame( items[ 4 ], event.item );
    assertEquals( 4, event.index );
  }

  public void testHandleVirtual_SubItem() {
    grid = new Grid( shell, SWT.VIRTUAL );
    GridItem[] items = createGridItems( grid, 3, 3 );
    grid.addListener( SWT.SetData, new LoggingListener() );

    items[ 2 ].getText();

    assertEquals( 1, eventLog.size() );
    Event event = eventLog.get( 0 );
    assertSame( grid, event.widget );
    assertSame( items[ 2 ], event.item );
    assertEquals( 1, event.index );
  }

  public void testHandleVirtual_Twice() {
    grid = new Grid( shell, SWT.VIRTUAL );
    GridItem[] items = createGridItems( grid, 3, 3 );
    grid.addListener( SWT.SetData, new LoggingListener() );

    items[ 2 ].getText();
    items[ 2 ].getText();

    assertEquals( 1, eventLog.size() );
  }

  public void testGetFont_Inital() {
    GridItem item = new GridItem( grid, SWT.NONE );

    assertSame( grid.getFont(), item.getFont() );
  }

  public void testGetFont() {
    GridItem item = new GridItem( grid, SWT.NONE );
    Font font = new Font( display, "Arial", 20, SWT.BOLD );

    item.setFont( font );

    assertSame( font, item.getFont() );
  }

  public void testSetFont_DisposedFont() {
    GridItem item = new GridItem( grid, SWT.NONE );
    Font font = new Font( display, "Arial", 20, SWT.BOLD );
    font.dispose();

    try {
      item.setFont( font );
      fail();
    } catch( IllegalArgumentException expected ) {
    }
  }

  public void testGetFontByIndex() {
    createGridColumns( grid, 3, SWT.NONE );
    GridItem item = new GridItem( grid, SWT.NONE );
    Font font = new Font( display, "Arial", 20, SWT.BOLD );

    item.setFont( 1, font );

    assertSame( grid.getFont(), item.getFont( 0 ) );
    assertSame( font, item.getFont( 1 ) );
    assertSame( grid.getFont(), item.getFont( 2 ) );
  }

  public void testGetFontByIndex_InvalidIndex() {
    createGridColumns( grid, 3, SWT.NONE );
    GridItem item = new GridItem( grid, SWT.NONE );

    try {
      item.getFont( 10 );
    } catch( IndexOutOfBoundsException expected ) {
    }
  }

  public void testSetFontByIndex_DisposedFont() {
    createGridColumns( grid, 3, SWT.NONE );
    GridItem item = new GridItem( grid, SWT.NONE );
    Font font = new Font( display, "Arial", 20, SWT.BOLD );
    font.dispose();

    try {
      item.setFont( 1, font );
    } catch( IllegalArgumentException expected ) {
    }
  }

  public void testGetBackground_Initial() {
    GridItem item = new GridItem( grid, SWT.NONE );

    assertSame( grid.getBackground(), item.getBackground() );
  }

  public void testGetBackground() {
    GridItem item = new GridItem( grid, SWT.NONE );
    Color background = new Color( display, 0, 0, 255 );

    item.setBackground( background );

    assertSame( background, item.getBackground() );
  }

  public void testSetBackground_DisposedFont() {
    GridItem item = new GridItem( grid, SWT.NONE );
    Color background = new Color( display, 0, 0, 255 );
    background.dispose();

    try {
      item.setBackground( background );
      fail();
    } catch( IllegalArgumentException expected ) {
    }
  }

  public void testGetBackgroundByIndex() {
    createGridColumns( grid, 3, SWT.NONE );
    GridItem item = new GridItem( grid, SWT.NONE );
    Color background = new Color( display, 0, 0, 255 );

    item.setBackground( 1, background );

    assertSame( grid.getBackground(), item.getBackground( 0 ) );
    assertSame( background, item.getBackground( 1 ) );
    assertSame( grid.getBackground(), item.getBackground( 2 ) );
  }

  public void testGetBackgroundByIndex_InvalidIndex() {
    createGridColumns( grid, 3, SWT.NONE );
    GridItem item = new GridItem( grid, SWT.NONE );

    try {
      item.getBackground( 10 );
    } catch( IndexOutOfBoundsException expected ) {
    }
  }

  public void testSetBackgroundByIndex_DisposedFont() {
    createGridColumns( grid, 3, SWT.NONE );
    GridItem item = new GridItem( grid, SWT.NONE );
    Color background = new Color( display, 0, 0, 255 );
    background.dispose();

    try {
      item.setBackground( 1, background );
    } catch( IllegalArgumentException expected ) {
    }
  }

  public void testGetForeground_Initial() {
    GridItem item = new GridItem( grid, SWT.NONE );

    assertSame( grid.getForeground(), item.getForeground() );
  }

  public void testGetForeground() {
    GridItem item = new GridItem( grid, SWT.NONE );
    Color foreground = new Color( display, 0, 0, 255 );

    item.setForeground( foreground );

    assertSame( foreground, item.getForeground() );
  }

  public void testSetForeground_DisposedFont() {
    GridItem item = new GridItem( grid, SWT.NONE );
    Color foreground = new Color( display, 0, 0, 255 );
    foreground.dispose();

    try {
      item.setForeground( foreground );
      fail();
    } catch( IllegalArgumentException expected ) {
    }
  }

  public void testGetForegroundByIndex() {
    createGridColumns( grid, 3, SWT.NONE );
    GridItem item = new GridItem( grid, SWT.NONE );
    Color foreground = new Color( display, 0, 0, 255 );

    item.setForeground( 1, foreground );

    assertSame( grid.getForeground(), item.getForeground( 0 ) );
    assertSame( foreground, item.getForeground( 1 ) );
    assertSame( grid.getForeground(), item.getForeground( 2 ) );
  }

  public void testGetForegroundByIndex_InvalidIndex() {
    createGridColumns( grid, 3, SWT.NONE );
    GridItem item = new GridItem( grid, SWT.NONE );

    try {
      item.getForeground( 10 );
    } catch( IndexOutOfBoundsException expected ) {
    }
  }

  public void testSetForegroundByIndex_DisposedFont() {
    createGridColumns( grid, 3, SWT.NONE );
    GridItem item = new GridItem( grid, SWT.NONE );
    Color foreground = new Color( display, 0, 0, 255 );
    foreground.dispose();

    try {
      item.setForeground( 1, foreground );
    } catch( IllegalArgumentException expected ) {
    }
  }

  public void testClear() {
    GridItem item = new GridItem( grid, SWT.NONE );
    Font font = new Font( display, "Arial", 20, SWT.BOLD );
    Color background = new Color( display, 0, 255, 0 );
    Color foreground = new Color( display, 0, 0, 255 );
    item.setFont( font );
    item.setBackground( background );
    item.setForeground( foreground );

    grid.clear( 0, false );

    assertSame( grid.getFont(), item.getFont() );
    assertSame( grid.getBackground(), item.getBackground() );
    assertSame( grid.getForeground(), item.getForeground() );
  }

  public void testGetToolTipText() {
    GridItem item = new GridItem( grid, SWT.NONE );

    item.setToolTipText( 0, "foo" );

    assertEquals( "foo", item.getToolTipText( 0 ) );
  }

  public void testGetToolTipText_WithColumns() {
    createGridColumns( grid, 3, SWT.NONE );
    GridItem item = new GridItem( grid, SWT.NONE );

    item.setToolTipText( 1, "foo" );

    assertNull( item.getToolTipText( 0 ) );
    assertEquals( "foo", item.getToolTipText( 1 ) );
    assertNull( item.getToolTipText( 2 ) );
  }

  public void testGetImage_Inital() {
    GridItem item = new GridItem( grid, SWT.NONE );

    assertNull( item.getImage() );
  }

  public void testGetImage() {
    GridItem item = new GridItem( grid, SWT.NONE );
    Image image = loadImage( display, Fixture.IMAGE1 );

    item.setImage( image );

    assertSame( image, item.getImage() );
  }

  public void testSetImage_DisposedImage() {
    GridItem item = new GridItem( grid, SWT.NONE );
    Image image = loadImage( display, Fixture.IMAGE1 );
    image.dispose();

    try {
      item.setImage( image );
      fail();
    } catch( IllegalArgumentException expected ) {
    }
  }

  public void testGetImageByIndex() {
    createGridColumns( grid, 3, SWT.NONE );
    GridItem item = new GridItem( grid, SWT.NONE );
    Image image = loadImage( display, Fixture.IMAGE1 );

    item.setImage( 1, image );

    assertNull( item.getImage( 0 ) );
    assertSame( image, item.getImage( 1 ) );
    assertNull( item.getImage( 2 ) );
  }

  public void testGetImagetByIndex_InvalidIndex() {
    createGridColumns( grid, 3, SWT.NONE );
    GridItem item = new GridItem( grid, SWT.NONE );

    try {
      item.getImage( 10 );
    } catch( IndexOutOfBoundsException expected ) {
    }
  }

  public void testSetImageByIndex_DisposedFont() {
    createGridColumns( grid, 3, SWT.NONE );
    GridItem item = new GridItem( grid, SWT.NONE );
    Image image = loadImage( display, Fixture.IMAGE1 );
    image.dispose();

    try {
      item.setImage( 1, image );
    } catch( IllegalArgumentException expected ) {
    }
  }

  public void testGetChecked_Inital() {
    GridItem item = new GridItem( grid, SWT.NONE );

    assertFalse( item.getChecked() );
  }

  public void testGetChecked() {
    GridItem item = new GridItem( grid, SWT.NONE );

    item.setChecked( true );

    assertTrue( item.getChecked() );
  }

  public void testGetCheckedByIndex() {
    createGridColumns( grid, 3, SWT.NONE );
    GridItem item = new GridItem( grid, SWT.NONE );

    item.setChecked( 1, true );

    assertFalse( item.getChecked( 0 ) );
    assertTrue( item.getChecked( 1 ) );
    assertFalse( item.getChecked( 2 ) );
  }

  public void testGetCheckedByIndex_InvalidIndex() {
    createGridColumns( grid, 3, SWT.NONE );
    GridItem item = new GridItem( grid, SWT.NONE );

    try {
      item.getChecked( 10 );
    } catch( IndexOutOfBoundsException expected ) {
    }
  }

  public void testGetGrayed_Inital() {
    GridItem item = new GridItem( grid, SWT.NONE );

    assertFalse( item.getGrayed() );
  }

  public void testGetGrayed() {
    GridItem item = new GridItem( grid, SWT.NONE );

    item.setGrayed( true );

    assertTrue( item.getGrayed() );
  }

  public void testGetGrayedByIndex() {
    createGridColumns( grid, 3, SWT.NONE );
    GridItem item = new GridItem( grid, SWT.NONE );

    item.setGrayed( 1, true );

    assertFalse( item.getGrayed( 0 ) );
    assertTrue( item.getGrayed( 1 ) );
    assertFalse( item.getGrayed( 2 ) );
  }

  public void testGetGrayedByIndex_InvalidIndex() {
    createGridColumns( grid, 3, SWT.NONE );
    GridItem item = new GridItem( grid, SWT.NONE );

    try {
      item.getGrayed( 10 );
    } catch( IndexOutOfBoundsException expected ) {
    }
  }

  public void testGetHeight_Initial() {
    GridItem item = new GridItem( grid, SWT.NONE );

    assertEquals( 27, item.getHeight() );
  }

  public void testGetHeight() {
    GridItem item = new GridItem( grid, SWT.NONE );

    grid.setItemHeight( 30 );

    assertEquals( 30, item.getHeight() );
  }

  public void testGetHeight_CustomHeight() {
    GridItem item = new GridItem( grid, SWT.NONE );

    item.setHeight( 30 );

    assertEquals( 30, item.getHeight() );
    assertTrue( grid.getItemHeight() != item.getHeight() );
  }

  public void testGetCellSize_WithoutSpan() {
    createGridColumns( grid, 5, SWT.NONE );
    createGridItems( grid, 5, 5 );
    GridItem item = grid.getRootItem( 2 );

    assertEquals( new Point( 60, 27 ), item.getCellSize( 2 ) );
  }

  public void testGetPreferredWidth_Initial() {
    createGridColumns( grid, 2, SWT.NONE );
    GridItem item = new GridItem( grid, SWT.NONE );

    // padding left (6) + padding right (6) = 12
    assertEquals( 12, item.getPreferredWidth( 0 ) );
    // padding left (6) + padding right (6) = 12
    assertEquals( 12, item.getPreferredWidth( 1 ) );
  }

  public void testGetPreferredWidth_InitialTree() {
    createGridColumns( grid, 2, SWT.NONE );
    GridItem item = new GridItem( grid, SWT.NONE );
    GridItem subitem = new GridItem( item, SWT.NONE );

    // indentation (16) + padding right (6) = 22
    assertEquals( 22, item.getPreferredWidth( 0 ) );
    // padding left (6) + padding right (6) = 12
    assertEquals( 12, item.getPreferredWidth( 1 ) );
    // 2 * indentation (16) + padding right (6) = 38
    assertEquals( 38, subitem.getPreferredWidth( 0 ) );
  }

  public void testGetPreferredWidth_WithCheck() {
    grid = new Grid( shell, SWT.CHECK );
    createGridColumns( grid, 2, SWT.NONE );
    GridItem item = new GridItem( grid, SWT.NONE );
    GridItem subitem = new GridItem( item, SWT.NONE );

    // indentation (16) + check width (23) + padding right (6) = 45
    assertEquals( 45, item.getPreferredWidth( 0 ) );
    // padding left (6) + padding right (6) = 12
    assertEquals( 12, item.getPreferredWidth( 1 ) );
    // 2 * indentation (16) + check width (23) + padding right (6) = 61
    assertEquals( 61, subitem.getPreferredWidth( 0 ) );
  }

  public void testGetPreferredWidth_WithImage() {
    createGridColumns( grid, 2, SWT.NONE );
    Image image = loadImage( display, Fixture.IMAGE1 );
    GridItem item = new GridItem( grid, SWT.NONE );
    item.setImage( 0, image );
    item.setImage( 1, image );
    GridItem subitem = new GridItem( item, SWT.NONE );
    subitem.setImage( 0, image );
    subitem.setImage( 1, image );

    // indentation (16) + image width (58) + padding right (6) = 80
    assertEquals( 80, item.getPreferredWidth( 0 ) );
    // padding left (6) + image width (58) + padding right (6) = 70
    assertEquals( 70, item.getPreferredWidth( 1 ) );
    // 2 * indentation (16) + image width (58) + padding right (6) = 96
    assertEquals( 96, subitem.getPreferredWidth( 0 ) );
  }

  public void testGetPreferredWidth_WithText() {
    createGridColumns( grid, 2, SWT.NONE );
    GridItem item = new GridItem( grid, SWT.NONE );
    item.setText( 0, "foo" );
    item.setText( 1, "foo" );
    GridItem subitem = new GridItem( item, SWT.NONE );
    subitem.setText( 0, "foo" );
    subitem.setText( 1, "foo" );

    // indentation (16) + text width (20) + padding right (6) = 42
    assertEquals( 42, item.getPreferredWidth( 0 ) );
    // padding left (6) + text width (20) + padding right (6) = 32
    assertEquals( 32, item.getPreferredWidth( 1 ) );
    // 2 * indentation (16) + text width (20) + padding right (6) = 58
    assertEquals( 58, subitem.getPreferredWidth( 0 ) );
  }

  public void testGetPreferredWidth_WithImageAndText() {
    createGridColumns( grid, 2, SWT.NONE );
    Image image = loadImage( display, Fixture.IMAGE1 );
    GridItem item = new GridItem( grid, SWT.NONE );
    item.setImage( 0, image );
    item.setImage( 1, image );
    item.setText( 0, "foo" );
    item.setText( 1, "foo" );
    GridItem subitem = new GridItem( item, SWT.NONE );
    subitem.setImage( 0, image );
    subitem.setImage( 1, image );
    subitem.setText( 0, "foo" );
    subitem.setText( 1, "foo" );
    fakeSpacing( grid, 3 );

    // indentation (16) + image width (58) + spacing (3) + text width (20) + padding right (6) = 103
    assertEquals( 103, item.getPreferredWidth( 0 ) );
    // padding left (6) + image width (58) + spacing (3) + text width (20) + padding right (6) = 93
    assertEquals( 93, item.getPreferredWidth( 1 ) );
    // 2 * indentation (16) + image width (58) + spacing (3) + text width (20) + padding right (6) = 119
    assertEquals( 119, subitem.getPreferredWidth( 0 ) );
  }

  public void testGetBounds() {
    createGridColumns( grid, 3, SWT.NONE );
    createGridItems( grid, 3, 3 );

    assertEquals( new Rectangle( 0, 27, 20, 27 ), grid.getItem( 4 ).getBounds( 0 ) );
    assertEquals( new Rectangle( 20, 27, 40, 27 ), grid.getItem( 4 ).getBounds( 1 ) );
    assertEquals( new Rectangle( 60, 27, 60, 27 ), grid.getItem( 4 ).getBounds( 2 ) );
  }

  public void testGetBounds_WithOffset() {
    createGridColumns( grid, 5, SWT.NONE );
    createGridItems( grid, 20, 3 );
    grid.getHorizontalBar().setSelection( 30 );
    grid.setTopIndex( 12 );

    assertEquals( new Rectangle( -30, 54, 20, 27 ), grid.getItem( 20 ).getBounds( 0 ) );
    assertEquals( new Rectangle( -10, 54, 40, 27 ), grid.getItem( 20 ).getBounds( 1 ) );
    assertEquals( new Rectangle( 30, 54, 60, 27 ), grid.getItem( 20 ).getBounds( 2 ) );
  }

  public void testGetBounds_InvisibleItem() {
    createGridColumns( grid, 3, SWT.NONE );
    createGridItems( grid, 3, 3 );

    Rectangle expected = new Rectangle( -1000, -1000, 0, 0 );
    assertEquals( expected, grid.getItem( 1 ).getBounds( 0 ) );
  }

  public void testGetBounds_HiddenItem() {
    createGridColumns( grid, 3, SWT.NONE );
    createGridItems( grid, 20, 3 );
    grid.setTopIndex( 12 );

    Rectangle expected = new Rectangle( -1000, -1000, 0, 0 );
    assertEquals( expected, grid.getItem( 8 ).getBounds( 0 ) );
  }

  public void testUpdateColumnImageCount_AddRemoveImage() {
    GridColumn[] columns = createGridColumns( grid, 3, SWT.NONE );
    GridItem[] items = createGridItems( grid, 3, 0 );
    Image image = loadImage( display, Fixture.IMAGE1 );

    items[ 0 ].setImage( 1, image );
    items[ 2 ].setImage( 1, image );
    assertEquals( 2, columns[ 1 ].imageCount );

    items[ 0 ].setImage( 1, null );
    assertEquals( 1, columns[ 1 ].imageCount );
  }

  public void testUpdateColumnImageCount_ClearItem() {
    GridColumn[] columns = createGridColumns( grid, 3, SWT.NONE );
    GridItem[] items = createGridItems( grid, 3, 0 );
    Image image = loadImage( display, Fixture.IMAGE1 );
    items[ 0 ].setImage( 1, image );
    items[ 2 ].setImage( 1, image );

    items[ 0 ].clear( false );

    assertEquals( 1, columns[ 1 ].imageCount );
  }

  public void testUpdateColumnImageCount_DisposeItem() {
    GridColumn[] columns = createGridColumns( grid, 3, SWT.NONE );
    GridItem[] items = createGridItems( grid, 3, 0 );
    Image image = loadImage( display, Fixture.IMAGE1 );
    items[ 0 ].setImage( 1, image );
    items[ 2 ].setImage( 1, image );

    items[ 0 ].dispose();

    assertEquals( 1, columns[ 1 ].imageCount );
  }

  //////////////////
  // Helping methods

  private void fakeSpacing( Grid grid, int spacing ) {
    grid.layoutCache.cellSpacing = spacing;
  }

  //////////////////
  // Helping classes

  private class LoggingListener implements Listener {
    public void handleEvent( Event event ) {
      eventLog.add( event );
    }
  }
}
