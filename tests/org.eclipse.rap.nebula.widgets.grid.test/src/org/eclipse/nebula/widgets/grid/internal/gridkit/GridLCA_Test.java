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
package org.eclipse.nebula.widgets.grid.internal.gridkit;

import static org.eclipse.nebula.widgets.grid.GridTestUtil.createGridColumns;
import static org.eclipse.nebula.widgets.grid.GridTestUtil.createGridItems;
import static org.eclipse.nebula.widgets.grid.internal.gridkit.GridLCATestUtil.jsonEquals;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.eclipse.nebula.widgets.grid.Grid;
import org.eclipse.nebula.widgets.grid.GridColumn;
import org.eclipse.nebula.widgets.grid.GridItem;
import org.eclipse.rap.rwt.testfixture.Fixture;
import org.eclipse.rap.rwt.testfixture.Message;
import org.eclipse.rap.rwt.testfixture.Message.CreateOperation;
import org.eclipse.rap.rwt.testfixture.Message.DestroyOperation;
import org.eclipse.rap.rwt.testfixture.Message.Operation;
import org.eclipse.rwt.lifecycle.WidgetUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.json.JSONArray;
import org.json.JSONException;

import junit.framework.TestCase;


public class GridLCA_Test extends TestCase {

  private Display display;
  private Shell shell;
  private Grid grid;
  private GridLCA lca;

  @Override
  protected void setUp() throws Exception {
    Fixture.setUp();
    display = new Display();
    shell = new Shell( display );
    grid = new Grid( shell, SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL );
    lca = ( GridLCA )WidgetUtil.getLCA( grid );
    Fixture.fakeNewRequest( display );
  }

  @Override
  protected void tearDown() throws Exception {
    Fixture.tearDown();
  }

  public void testRenderCreate() throws IOException {
    lca.renderInitialization( grid );

    Message message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( grid );
    List<Object> styles = Arrays.asList( operation.getStyles() );
    assertEquals( "rwt.widgets.Tree", operation.getType() );
    assertEquals( "tree", operation.getProperty( "appearance" ) );
    assertEquals( Integer.valueOf( 16 ), operation.getProperty( "indentionWidth" ) );
    assertFalse( operation.getPropertyNames().contains( "checkBoxMetrics" ) );
    assertTrue( styles.contains( "FULL_SELECTION" ) );
  }

  public void testRenderParent() throws IOException {
    lca.renderInitialization( grid );

    Message message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( grid );
    assertEquals( WidgetUtil.getId( grid.getParent() ), operation.getParent() );
  }

  public void testRenderCreateWithVirtualMulti() throws IOException {
    grid = new Grid( shell, SWT.VIRTUAL | SWT.MULTI );

    lca.renderInitialization( grid );

    Message message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( grid );
    List<Object> styles = Arrays.asList( operation.getStyles() );
    assertTrue( styles.contains( "VIRTUAL" ) );
    assertTrue( styles.contains( "MULTI" ) );
  }

  public void testRenderCreateWithCheck() throws IOException, JSONException {
    grid = new Grid( shell, SWT.CHECK );

    lca.renderInitialization( grid );

    Message message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( grid );
    List<Object> styles = Arrays.asList( operation.getStyles() );
    assertTrue( styles.contains( "CHECK" ) );
    JSONArray actual = ( JSONArray )operation.getProperty( "checkBoxMetrics" );
    assertTrue( jsonEquals( "[0,21]", actual ) );
  }

  public void testRenderDispose() throws IOException {
    lca.renderDispose( grid );

    Message message = Fixture.getProtocolMessage();
    Operation operation = message.getOperation( 0 );
    assertTrue( operation instanceof DestroyOperation );
    assertEquals( WidgetUtil.getId( grid ), operation.getTarget() );
  }

  public void testRenderInitialItemCount() throws IOException {
    lca.render( grid );

    Message message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( grid );
    assertTrue( operation.getPropertyNames().indexOf( "itemCount" ) == -1 );
  }

  public void testRenderItemCount() throws IOException {
    grid.setItemCount( 10 );
    lca.renderChanges( grid );

    Message message = Fixture.getProtocolMessage();
    assertEquals( Integer.valueOf( 10 ), message.findSetProperty( grid, "itemCount" ) );
  }

  public void testRenderItemCountUnchanged() throws IOException {
    Fixture.markInitialized( display );
    Fixture.markInitialized( grid );

    grid.setItemCount( 10 );
    Fixture.preserveWidgets();
    lca.renderChanges( grid );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( grid, "itemCount" ) );
  }

  public void testRenderInitialItemHeight() throws IOException {
    lca.render( grid );

    Message message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( grid );
    assertTrue( operation.getPropertyNames().contains( "itemHeight" ) );
  }

  public void testRenderItemHeight() throws IOException {
    grid.setItemHeight( 40 );
    lca.renderChanges( grid );

    Message message = Fixture.getProtocolMessage();
    assertEquals( Integer.valueOf( 40 ), message.findSetProperty( grid, "itemHeight" ) );
  }

  public void testRenderItemHeightUnchanged() throws IOException {
    Fixture.markInitialized( display );
    Fixture.markInitialized( grid );

    grid.setItemHeight( 40 );
    Fixture.preserveWidgets();
    lca.renderChanges( grid );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( grid, "itemHeight" ) );
  }

  public void testRenderInitialColumnCount() throws IOException {
    lca.render( grid );

    Message message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( grid );
    assertTrue( operation.getPropertyNames().indexOf( "columnCount" ) == -1 );
  }

  public void testRenderColumnCount() throws IOException {
    new GridColumn( grid, SWT.NONE );
    lca.renderChanges( grid );

    Message message = Fixture.getProtocolMessage();
    assertEquals( Integer.valueOf( 1 ), message.findSetProperty( grid, "columnCount" ) );
  }

  public void testRenderColumnCountUnchanged() throws IOException {
    Fixture.markInitialized( display );
    Fixture.markInitialized( grid );

    new GridColumn( grid, SWT.NONE );
    Fixture.preserveWidgets();
    lca.renderChanges( grid );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( grid, "columnCount" ) );
  }

  public void testRenderInitialTreeColumn() throws IOException {
    createGridColumns( grid, 2, SWT.NONE );

    lca.render( grid );

    Message message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( grid );
    assertTrue( operation.getPropertyNames().indexOf( "treeColumn" ) == -1 );
  }

  public void testRenderTreeColumn() throws IOException {
    createGridColumns( grid, 2, SWT.NONE );

    grid.setColumnOrder( new int[]{ 1, 0 } );
    lca.renderChanges( grid );

    Message message = Fixture.getProtocolMessage();
    assertEquals( Integer.valueOf( 1 ), message.findSetProperty( grid, "treeColumn" ) );
  }

  public void testRenderTreeColumnUnchanged() throws IOException {
    createGridColumns( grid, 2, SWT.NONE );
    Fixture.markInitialized( display );
    Fixture.markInitialized( grid );

    grid.setColumnOrder( new int[]{ 1, 0 } );
    Fixture.preserveWidgets();
    lca.renderChanges( grid );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( grid, "treeColumn" ) );
  }

  public void testRenderInitialHeaderHeight() throws IOException {
    new GridColumn( grid, SWT.NONE );

    lca.render( grid );

    Message message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( grid );
    assertTrue( operation.getPropertyNames().indexOf( "headerHeight" ) == -1 );
  }

  public void testRenderHeaderHeight() throws IOException {
    new GridColumn( grid, SWT.NONE );
    grid.setHeaderVisible( true );

    lca.renderChanges( grid );

    Message message = Fixture.getProtocolMessage();
    assertEquals( Integer.valueOf( 31 ), message.findSetProperty( grid, "headerHeight" ) );
  }

  public void testRenderHeaderHeightUnchanged() throws IOException {
    new GridColumn( grid, SWT.NONE );
    Fixture.markInitialized( display );
    Fixture.markInitialized( grid );

    grid.setHeaderVisible( true );
    Fixture.preserveWidgets();
    lca.renderChanges( grid );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( grid, "headerHeight" ) );
  }

  public void testRenderInitialHeaderVisible() throws IOException {
    lca.render( grid );

    Message message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( grid );
    assertTrue( operation.getPropertyNames().indexOf( "headerVisible" ) == -1 );
  }

  public void testRenderHeaderVisible() throws IOException {
    grid.setHeaderVisible( true );
    lca.renderChanges( grid );

    Message message = Fixture.getProtocolMessage();
    assertEquals( Boolean.TRUE, message.findSetProperty( grid, "headerVisible" ) );
  }

  public void testRenderHeaderVisibleUnchanged() throws IOException {
    Fixture.markInitialized( display );
    Fixture.markInitialized( grid );

    grid.setHeaderVisible( true );
    Fixture.preserveWidgets();
    lca.renderChanges( grid );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( grid, "headerVisible" ) );
  }

  public void testRenderInitialLinesVisible() throws IOException {
    lca.render( grid );

    Message message = Fixture.getProtocolMessage();
    assertEquals( Boolean.TRUE, message.findCreateProperty( grid, "linesVisible" ) );
  }

  public void testRenderLinesVisible() throws IOException {
    Fixture.markInitialized( grid );
    grid.setLinesVisible( false );
    lca.renderChanges( grid );

    Message message = Fixture.getProtocolMessage();
    assertEquals( Boolean.FALSE, message.findSetProperty( grid, "linesVisible" ) );
  }

  public void testRenderLinesVisibleUnchanged() throws IOException {
    Fixture.markInitialized( display );
    Fixture.markInitialized( grid );

    grid.setLinesVisible( true );
    Fixture.preserveWidgets();
    lca.renderChanges( grid );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( grid, "linesVisible" ) );
  }

  public void testRenderInitialTopItemIndex() throws IOException {
    grid.setSize( 100, 100 );

    lca.render( grid );

    Message message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( grid );
    assertTrue( operation.getPropertyNames().indexOf( "topItemIndex" ) == -1 );
  }

  public void testRenderTopItemIndex() throws IOException {
    grid.setSize( 100, 100 );
    createGridItems( grid, 10, 3 );
    grid.getItem( 4 ).setExpanded( true );

    grid.setTopIndex( 5 );
    lca.renderChanges( grid );

    Message message = Fixture.getProtocolMessage();
    assertEquals( Integer.valueOf( 2 ), message.findSetProperty( grid, "topItemIndex" ) );
  }

  public void testRenderTopItemIndexUnchanged() throws IOException {
    grid.setSize( 100, 100 );
    createGridItems( grid, 10, 3 );
    Fixture.markInitialized( display );
    Fixture.markInitialized( grid );

    grid.setTopIndex( 2 );
    Fixture.preserveWidgets();
    lca.renderChanges( grid );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( grid, "topItemIndex" ) );
  }

  public void testRenderInitialFocusItem() throws IOException {
    lca.render( grid );

    Message message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( grid );
    assertTrue( operation.getPropertyNames().indexOf( "focusItem" ) == -1 );
  }

  public void testRenderFocusItem() throws IOException {
    GridItem[] items = createGridItems( grid, 3, 0 );

    grid.setFocusItem( items[ 1 ] );
    lca.renderChanges( grid );

    Message message = Fixture.getProtocolMessage();
    assertEquals( WidgetUtil.getId( items[ 1 ] ), message.findSetProperty( grid, "focusItem" ) );
  }

  public void testRenderFocusItemUnchanged() throws IOException {
    GridItem[] items = createGridItems( grid, 3, 0 );
    Fixture.markInitialized( display );
    Fixture.markInitialized( grid );

    grid.setFocusItem( items[ 1 ] );
    Fixture.preserveWidgets();
    lca.renderChanges( grid );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( grid, "focusItem" ) );
  }

  public void testRenderInitialScrollLeft() throws IOException {
    createGridColumns( grid, 3, SWT.NONE );
    lca.render( grid );

    Message message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( grid );
    assertTrue( operation.getPropertyNames().indexOf( "scrollLeft" ) == -1 );
  }

  public void testRenderScrollLeft() throws IOException {
    createGridColumns( grid, 3, SWT.NONE );
    grid.getHorizontalBar().setSelection( 10 );
    lca.renderChanges( grid );

    Message message = Fixture.getProtocolMessage();
    assertEquals( Integer.valueOf( 10 ), message.findSetProperty( grid, "scrollLeft" ) );
  }

  public void testRenderScrollLeftUnchanged() throws IOException {
    createGridColumns( grid, 3, SWT.NONE );
    Fixture.markInitialized( display );
    Fixture.markInitialized( grid );

    grid.getHorizontalBar().setSelection( 10 );
    Fixture.preserveWidgets();
    lca.renderChanges( grid );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( grid, "scrollLeft" ) );
  }

  public void testRenderInitialSelection() throws IOException {
    lca.render( grid );

    Message message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( grid );
    assertTrue( operation.getPropertyNames().indexOf( "selection" ) == -1 );
  }

  public void testRenderSelection() throws IOException, JSONException {
    GridItem[] items = createGridItems( grid, 3, 3 );

    grid.setSelection( new int[] { 0, 4 } );
    lca.renderChanges( grid );

    Message message = Fixture.getProtocolMessage();
    JSONArray actual = ( JSONArray )message.findSetProperty( grid, "selection" );
    StringBuilder expected = new StringBuilder();
    expected.append( "[" );
    expected.append( WidgetUtil.getId( items[ 0 ] ) );
    expected.append( "," );
    expected.append( WidgetUtil.getId( items[ 4 ] ) );
    expected.append( "]" );
    assertTrue( jsonEquals( expected.toString(), actual ) );
  }

  public void testRenderSelectionUnchanged() throws IOException {
    createGridItems( grid, 3, 3 );
    Fixture.markInitialized( display );
    Fixture.markInitialized( grid );

    grid.setSelection( new int[] { 0, 4 } );
    Fixture.preserveWidgets();
    lca.renderChanges( grid );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( grid, "selection" ) );
  }

  public void testRenderInitialSortDirection() throws IOException {
    lca.render( grid );

    Message message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( grid );
    assertTrue( operation.getPropertyNames().indexOf( "sortDirection" ) == -1 );
  }

  public void testRenderSortDirection() throws IOException {
    GridColumn[] columns = createGridColumns( grid, 3, SWT.NONE );

    columns[ 1 ].setSort( SWT.UP );
    lca.renderChanges( grid );

    Message message = Fixture.getProtocolMessage();
    assertEquals( "up", message.findSetProperty( grid, "sortDirection" ) );
  }

  public void testRenderSortDirectionUnchanged() throws IOException {
    GridColumn[] columns = createGridColumns( grid, 3, SWT.NONE );
    Fixture.markInitialized( display );
    Fixture.markInitialized( grid );

    columns[ 1 ].setSort( SWT.UP );
    Fixture.preserveWidgets();
    lca.renderChanges( grid );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( grid, "sortDirection" ) );
  }

  public void testRenderInitialSortColumn() throws IOException {
    lca.render( grid );

    Message message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( grid );
    assertTrue( operation.getPropertyNames().indexOf( "sortColumn" ) == -1 );
  }

  public void testRenderSortColumn() throws IOException {
    GridColumn[] columns = createGridColumns( grid, 3, SWT.NONE );

    columns[ 1 ].setSort( SWT.UP );
    lca.renderChanges( grid );

    Message message = Fixture.getProtocolMessage();
    assertEquals( WidgetUtil.getId( columns[ 1 ] ), message.findSetProperty( grid, "sortColumn" ) );
  }

  public void testRenderSortColumnUnchanged() throws IOException {
    GridColumn[] columns = createGridColumns( grid, 3, SWT.NONE );
    Fixture.markInitialized( display );
    Fixture.markInitialized( grid );

    columns[ 1 ].setSort( SWT.UP );
    Fixture.preserveWidgets();
    lca.renderChanges( grid );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( grid, "sortColumn" ) );
  }

  public void testRenderInitialScrollBarsVisible() throws IOException {
    lca.render( grid );

    Message message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( grid );
    assertTrue( operation.getPropertyNames().indexOf( "scrollBarsVisible" ) == -1 );
  }

  public void testRenderScrollBarsVisible_Horizontal() throws IOException, JSONException {
    grid.setSize( 200, 200 );
    GridColumn[] columns = createGridColumns( grid, 3, SWT.NONE );

    columns[ 0 ].setWidth( 150 );
    lca.renderChanges( grid );

    Message message = Fixture.getProtocolMessage();
    JSONArray actual = ( JSONArray )message.findSetProperty( grid, "scrollBarsVisible" );
    assertTrue( jsonEquals( "[ true, false ]", actual ) );
  }

  public void testRenderScrollBarsVisible_Vertical() throws IOException, JSONException {
    grid.setSize( 200, 200 );
    createGridColumns( grid, 3, SWT.NONE );

    createGridItems( grid, 20, 0 );
    lca.renderChanges( grid );

    Message message = Fixture.getProtocolMessage();
    JSONArray actual = ( JSONArray )message.findSetProperty( grid, "scrollBarsVisible" );
    assertTrue( jsonEquals( "[ false, true ]", actual ) );
  }

  public void testRenderScrollBarsVisibleUnchanged() throws IOException {
    grid.setSize( 200, 200 );
    GridColumn[] columns = createGridColumns( grid, 3, SWT.NONE );
    Fixture.markInitialized( display );
    Fixture.markInitialized( grid );

    columns[ 0 ].setWidth( 150 );
    createGridItems( grid, 20, 0 );
    Fixture.preserveWidgets();
    lca.renderChanges( grid );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( grid, "scrollBarsVisible" ) );
  }

  public void testRenderAddScrollBarsSelectionListener_Horizontal() throws Exception {
    Fixture.markInitialized( display );
    Fixture.markInitialized( grid );
    Fixture.preserveWidgets();

    grid.getHorizontalBar().addSelectionListener( new SelectionAdapter() { } );
    lca.renderChanges( grid );

    Message message = Fixture.getProtocolMessage();
    assertEquals( Boolean.TRUE, message.findListenProperty( grid, "scrollBarsSelection" ) );
  }

  public void testRenderRemoveScrollBarsSelectionListener_Horizontal() throws Exception {
    SelectionListener listener = new SelectionAdapter() { };
    grid.getHorizontalBar().addSelectionListener( listener );
    Fixture.markInitialized( display );
    Fixture.markInitialized( grid );
    Fixture.preserveWidgets();

    grid.getHorizontalBar().removeSelectionListener( listener );
    lca.renderChanges( grid );

    Message message = Fixture.getProtocolMessage();
    assertEquals( Boolean.FALSE, message.findListenProperty( grid, "scrollBarsSelection" ) );
  }

  public void testRenderScrollBarsSelectionListenerUnchanged_Horizontal() throws Exception {
    Fixture.markInitialized( display );
    Fixture.markInitialized( grid );
    Fixture.preserveWidgets();

    grid.getHorizontalBar().addSelectionListener( new SelectionAdapter() { } );
    Fixture.preserveWidgets();
    lca.renderChanges( grid );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findListenOperation( grid, "scrollBarsSelection" ) );
  }

  public void testRenderAddScrollBarsSelectionListener_Vertical() throws Exception {
    Fixture.markInitialized( display );
    Fixture.markInitialized( grid );
    Fixture.preserveWidgets();

    grid.getVerticalBar().addSelectionListener( new SelectionAdapter() { } );
    lca.renderChanges( grid );

    Message message = Fixture.getProtocolMessage();
    assertEquals( Boolean.TRUE, message.findListenProperty( grid, "scrollBarsSelection" ) );
  }

  public void testRenderRemoveScrollBarsSelectionListener_Vertical() throws Exception {
    SelectionListener listener = new SelectionAdapter() { };
    grid.getVerticalBar().addSelectionListener( listener );
    Fixture.markInitialized( display );
    Fixture.markInitialized( grid );
    Fixture.preserveWidgets();

    grid.getVerticalBar().removeSelectionListener( listener );
    lca.renderChanges( grid );

    Message message = Fixture.getProtocolMessage();
    assertEquals( Boolean.FALSE, message.findListenProperty( grid, "scrollBarsSelection" ) );
  }

  public void testRenderScrollBarsSelectionListenerUnchanged_Vertical() throws Exception {
    Fixture.markInitialized( display );
    Fixture.markInitialized( grid );
    Fixture.preserveWidgets();

    grid.getVerticalBar().addSelectionListener( new SelectionAdapter() { } );
    Fixture.preserveWidgets();
    lca.renderChanges( grid );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findListenOperation( grid, "scrollBarsSelection" ) );
  }

  public void testRenderAddSelectionListener() throws Exception {
    Fixture.markInitialized( display );
    Fixture.markInitialized( grid );
    Fixture.preserveWidgets();

    grid.addSelectionListener( new SelectionAdapter() { } );
    lca.renderChanges( grid );

    Message message = Fixture.getProtocolMessage();
    assertEquals( Boolean.TRUE, message.findListenProperty( grid, "selection" ) );
  }

  public void testRenderRemoveSelectionListener() throws Exception {
    SelectionListener listener = new SelectionAdapter() { };
    grid.addSelectionListener( listener );
    Fixture.markInitialized( display );
    Fixture.markInitialized( grid );
    Fixture.preserveWidgets();

    grid.removeSelectionListener( listener );
    lca.renderChanges( grid );

    Message message = Fixture.getProtocolMessage();
    assertEquals( Boolean.FALSE, message.findListenProperty( grid, "selection" ) );
  }

  public void testRenderSelectionListenerUnchanged() throws Exception {
    Fixture.markInitialized( display );
    Fixture.markInitialized( grid );
    Fixture.preserveWidgets();

    grid.addSelectionListener( new SelectionAdapter() { } );
    Fixture.preserveWidgets();
    lca.renderChanges( grid );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findListenOperation( grid, "selection" ) );
  }

}
