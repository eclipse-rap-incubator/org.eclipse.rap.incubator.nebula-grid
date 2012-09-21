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
package org.eclipse.nebula.widgets.grid.internal.griditemkit;

import static org.eclipse.nebula.widgets.grid.GridTestUtil.createGridColumns;
import static org.eclipse.nebula.widgets.grid.GridTestUtil.createGridItems;
import static org.eclipse.nebula.widgets.grid.GridTestUtil.loadImage;
import static org.eclipse.nebula.widgets.grid.internal.gridkit.GridLCATestUtil.jsonEquals;
import static org.eclipse.rap.rwt.internal.protocol.ClientMessageConst.EVENT_TREE_EXPANDED;
import static org.eclipse.rap.rwt.lifecycle.WidgetUtil.getId;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.nebula.widgets.grid.Grid;
import org.eclipse.nebula.widgets.grid.GridItem;
import org.eclipse.rap.rwt.graphics.Graphics;
import org.eclipse.rap.rwt.internal.protocol.ClientMessageConst;
import org.eclipse.rap.rwt.lifecycle.WidgetUtil;
import org.eclipse.rap.rwt.testfixture.Fixture;
import org.eclipse.rap.rwt.testfixture.Message;
import org.eclipse.rap.rwt.testfixture.Message.CreateOperation;
import org.eclipse.rap.rwt.testfixture.Message.DestroyOperation;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TreeAdapter;
import org.eclipse.swt.events.TreeEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import junit.framework.TestCase;


@SuppressWarnings("restriction")
public class GridItemLCA_Test extends TestCase {

  private Display display;
  private Shell shell;
  private Grid grid;
  private GridItem item;
  private GridItemLCA lca;

  @Override
  protected void setUp() throws Exception {
    Fixture.setUp();
    display = new Display();
    shell = new Shell( display );
    grid = new Grid( shell, SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL );
    item = new GridItem( grid, SWT.NONE );
    lca = new GridItemLCA();
    Fixture.fakeNewRequest( display );
  }

  @Override
  protected void tearDown() throws Exception {
    Fixture.tearDown();
  }

  public void testRenderCreate() throws IOException {
    GridItem[] items = createGridItems( grid, 3, 3 );

    lca.renderInitialization( items[ 8 ] );

    Message message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( items[ 8 ] );
    assertEquals( "rwt.widgets.GridItem", operation.getType() );
    assertEquals( Integer.valueOf( 3 ), operation.getProperty( "index" ) );
  }

  public void testRenderCreate_WithParentItem() throws IOException {
    GridItem[] items = createGridItems( grid, 3, 3 );

    lca.renderInitialization( items[ 10 ] );

    Message message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( items[ 10 ] );
    assertEquals( "rwt.widgets.GridItem", operation.getType() );
    assertEquals( Integer.valueOf( 1 ), operation.getProperty( "index" ) );
  }

  public void testRenderParent() throws IOException {
    lca.renderInitialization( item );

    Message message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( item );
    assertEquals( WidgetUtil.getId( item.getParent() ), operation.getParent() );
  }

  public void testRenderParent_WithParentItem() throws IOException {
    GridItem subitem = new GridItem( item, SWT.NONE );

    lca.renderInitialization( subitem );

    Message message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( subitem );
    assertEquals( WidgetUtil.getId( item ), operation.getParent() );
  }

  public void testRenderDispose() throws IOException {
    lca.renderDispose( item );

    Message message = Fixture.getProtocolMessage();
    DestroyOperation operation = ( DestroyOperation )message.getOperation( 0 );
    assertEquals( WidgetUtil.getId( item ), operation.getTarget() );
  }

  public void testRenderDispose_WithDisposedGrid() throws IOException {
    grid.dispose();

    lca.renderDispose( item );

    Message message = Fixture.getProtocolMessage();
    assertEquals( 0, message.getOperationCount() );
  }

  public void testRenderDispose_WithDisposedParentItem() throws IOException {
    GridItem[] items = createGridItems( grid, 3, 3 );
    items[ 0 ].dispose();

    lca.renderDispose( items[ 1 ] );

    Message message = Fixture.getProtocolMessage();
    assertEquals( 0, message.getOperationCount() );
  }

  public void testRenderInitialCustomVariant() throws IOException {
    lca.render( item );

    Message message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( item );
    assertTrue( operation.getPropertyNames().indexOf( "customVariant" ) == -1 );
  }

  public void testRenderCustomVariant() throws IOException {
    item.setData( WidgetUtil.CUSTOM_VARIANT, "blue" );
    lca.renderChanges( item );

    Message message = Fixture.getProtocolMessage();
    assertEquals( "variant_blue", message.findSetProperty( item, "customVariant" ) );
  }

  public void testRenderCustomVariantUnchanged() throws IOException {
    Fixture.markInitialized( display );
    Fixture.markInitialized( item );

    item.setData( WidgetUtil.CUSTOM_VARIANT, "blue" );
    Fixture.preserveWidgets();
    lca.renderChanges( item );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( item, "customVariant" ) );
  }

  public void testRenderInitialItemCount() throws IOException {
    lca.render( item );

    Message message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( item );
    assertTrue( operation.getPropertyNames().indexOf( "itemCount" ) == -1 );
  }

  public void testRenderItemCount() throws IOException {
    GridItem[] items = createGridItems( grid, 1, 10 );
    lca.renderChanges( items[ 0 ] );

    Message message = Fixture.getProtocolMessage();
    assertEquals( Integer.valueOf( 10 ), message.findSetProperty( items[ 0 ], "itemCount" ) );
  }

  public void testRenderItemCountUnchanged() throws IOException {
    GridItem[] items = createGridItems( grid, 1, 10 );
    Fixture.markInitialized( display );
    Fixture.markInitialized( items[ 0 ] );

    Fixture.preserveWidgets();
    lca.renderChanges( items[ 0 ] );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( items[ 0 ], "itemCount" ) );
  }

  public void testRenderInitialHeight() throws IOException {
    lca.render( item );

    Message message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( item );
    assertTrue( operation.getPropertyNames().indexOf( "height" ) == -1 );
  }

  public void testRenderHeight() throws IOException {
    item.setHeight( 10 );

    lca.renderChanges( item );

    Message message = Fixture.getProtocolMessage();
    assertEquals( Integer.valueOf( 10 ), message.findSetProperty( item, "height" ) );
  }

  public void testRenderHeightUnchanged() throws IOException {
    Fixture.markInitialized( display );
    Fixture.markInitialized( item );

    item.setHeight( 10 );
    Fixture.preserveWidgets();
    lca.renderChanges( item );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( item, "height" ) );
  }

  public void testRenderInitialTexts() throws IOException {
    createGridColumns( grid, 2, SWT.NONE );

    lca.render( item );

    Message message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( item );
    assertTrue( operation.getPropertyNames().indexOf( "texts" ) == -1 );
  }

  public void testRenderTexts() throws IOException, JSONException {
    createGridColumns( grid, 2, SWT.NONE );

    item.setText( 0, "item 0.0" );
    item.setText( 1, "item 0.1" );
    lca.renderChanges( item );

    Message message = Fixture.getProtocolMessage();
    JSONArray actual = ( JSONArray )message.findSetProperty( item, "texts" );
    assertTrue( jsonEquals( "[\"item 0.0\",\"item 0.1\"]", actual ) );
  }

  public void testRenderTextsUnchanged() throws IOException {
    createGridColumns( grid, 2, SWT.NONE );
    Fixture.markInitialized( display );
    Fixture.markInitialized( item );

    item.setText( 0, "item 0.0" );
    item.setText( 1, "item 0.1" );
    Fixture.preserveWidgets();
    lca.renderChanges( item );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( item, "texts" ) );
  }

  public void testRenderInitialImages() throws IOException {
    createGridColumns( grid, 2, SWT.NONE );

    lca.render( item );

    Message message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( item );
    assertTrue( operation.getPropertyNames().indexOf( "images" ) == -1 );
  }

  public void testRenderImages() throws IOException, JSONException {
    createGridColumns( grid, 2, SWT.NONE );
    Image image = loadImage( display, Fixture.IMAGE1 );

    item.setImage( 1, image );
    lca.renderChanges( item );

    Message message = Fixture.getProtocolMessage();
    JSONArray actual = ( JSONArray )message.findSetProperty( item, "images" );
    String expected = "[\"rwt-resources/generated/90fb0bfe\",58,12]";
    assertEquals( JSONObject.NULL, actual.get( 0 ) );
    assertTrue( jsonEquals( expected, actual.getJSONArray( 1 ) ) );
  }

  public void testRenderImagesUnchanged() throws IOException {
    createGridColumns( grid, 2, SWT.NONE );
    Fixture.markInitialized( display );
    Fixture.markInitialized( item );
    Image image = loadImage( display, Fixture.IMAGE1 );

    item.setImage( 1, image );
    Fixture.preserveWidgets();
    lca.renderChanges( item );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( item, "images" ) );
  }

  public void testRenderInitialBackground() throws IOException {
    lca.render( item );

    Message message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( item );
    assertTrue( operation.getPropertyNames().indexOf( "background" ) == -1 );
  }

  public void testRenderBackground() throws IOException, JSONException {
    item.setBackground( display.getSystemColor( SWT.COLOR_GREEN ) );
    lca.renderChanges( item );

    Message message = Fixture.getProtocolMessage();
    JSONArray actual = ( JSONArray )message.findSetProperty( item, "background" );
    assertTrue( jsonEquals( "[0,255,0,255]", actual ) );
  }

  public void testRenderBackgroundUnchanged() throws IOException {
    Fixture.markInitialized( display );
    Fixture.markInitialized( item );

    item.setBackground( display.getSystemColor( SWT.COLOR_GREEN ) );
    Fixture.preserveWidgets();
    lca.renderChanges( item );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( item, "background" ) );
  }

  public void testRenderInitialForeground() throws IOException {
    lca.render( item );

    Message message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( item );
    assertTrue( operation.getPropertyNames().indexOf( "foreground" ) == -1 );
  }

  public void testRenderForeground() throws IOException, JSONException {
    item.setForeground( display.getSystemColor( SWT.COLOR_GREEN ) );
    lca.renderChanges( item );

    Message message = Fixture.getProtocolMessage();
    JSONArray actual = ( JSONArray )message.findSetProperty( item, "foreground" );
    assertTrue( jsonEquals( "[0,255,0,255]", actual ) );
  }

  public void testRenderForegroundUnchanged() throws IOException {
    Fixture.markInitialized( display );
    Fixture.markInitialized( item );

    item.setForeground( display.getSystemColor( SWT.COLOR_GREEN ) );
    Fixture.preserveWidgets();
    lca.renderChanges( item );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( item, "foreground" ) );
  }

  public void testRenderInitialFont() throws IOException {
    lca.render( item );

    Message message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( item );
    assertTrue( operation.getPropertyNames().indexOf( "font" ) == -1 );
  }

  public void testRenderFont() throws IOException, JSONException {
    item.setFont( new Font( display, "Arial", 20, SWT.BOLD ) );
    lca.renderChanges( item );

    Message message = Fixture.getProtocolMessage();
    JSONArray actual = ( JSONArray )message.findSetProperty( item, "font" );
    assertTrue( jsonEquals( "[\"Arial\"]", actual.getJSONArray( 0 ) ) );
    assertEquals( Integer.valueOf( 20 ), actual.get( 1 ) );
    assertEquals( Boolean.TRUE, actual.get( 2 ) );
    assertEquals( Boolean.FALSE, actual.get( 3 ) );
  }

  public void testRenderFontUnchanged() throws IOException {
    Fixture.markInitialized( display );
    Fixture.markInitialized( item );

    item.setFont( new Font( display, "Arial", 20, SWT.BOLD ) );
    Fixture.preserveWidgets();
    lca.renderChanges( item );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( item, "font" ) );
  }

  public void testRenderInitialCellBackgrounds() throws IOException {
    createGridColumns( grid, 2, SWT.NONE );

    lca.render( item );

    Message message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( item );
    assertTrue( operation.getPropertyNames().indexOf( "cellBackgrounds" ) == -1 );
  }

  public void testRenderCellBackgrounds() throws IOException, JSONException {
    createGridColumns( grid, 2, SWT.NONE );

    item.setBackground( 1, display.getSystemColor( SWT.COLOR_GREEN ) );
    lca.renderChanges( item );

    Message message = Fixture.getProtocolMessage();
    JSONArray actual = ( JSONArray )message.findSetProperty( item, "cellBackgrounds" );
    assertEquals( JSONObject.NULL, actual.get( 0 ) );
    assertTrue( jsonEquals( "[0,255,0,255]", actual.getJSONArray( 1 ) ) );
  }

  public void testRenderCellBackgroundsUnchanged() throws IOException {
    createGridColumns( grid, 2, SWT.NONE );
    Fixture.markInitialized( display );
    Fixture.markInitialized( item );

    item.setBackground( 1, display.getSystemColor( SWT.COLOR_GREEN ) );
    Fixture.preserveWidgets();
    lca.renderChanges( item );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( item, "cellBackgrounds" ) );
  }

  public void testRenderInitialCellForegrounds() throws IOException {
    createGridColumns( grid, 2, SWT.NONE );

    lca.render( item );

    Message message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( item );
    assertTrue( operation.getPropertyNames().indexOf( "cellForegrounds" ) == -1 );
  }

  public void testRenderCellForegrounds() throws IOException, JSONException {
    createGridColumns( grid, 2, SWT.NONE );

    item.setForeground( 1, display.getSystemColor( SWT.COLOR_GREEN ) );
    lca.renderChanges( item );

    Message message = Fixture.getProtocolMessage();
    JSONArray actual = ( JSONArray )message.findSetProperty( item, "cellForegrounds" );
    assertEquals( JSONObject.NULL, actual.get( 0 ) );
    assertTrue( jsonEquals( "[0,255,0,255]", actual.getJSONArray( 1 ) ) );
  }

  public void testRenderCellForegroundsUnchanged() throws IOException {
    createGridColumns( grid, 2, SWT.NONE );
    Fixture.markInitialized( display );
    Fixture.markInitialized( item );

    item.setForeground( 1, display.getSystemColor( SWT.COLOR_GREEN ) );
    Fixture.preserveWidgets();
    lca.renderChanges( item );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( item, "cellForegrounds" ) );
  }

  public void testRenderInitialCellFonts() throws IOException {
    createGridColumns( grid, 2, SWT.NONE );

    lca.render( item );

    Message message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( item );
    assertTrue( operation.getPropertyNames().indexOf( "cellFonts" ) == -1 );
  }

  public void testRenderCellFonts() throws IOException, JSONException {
    createGridColumns( grid, 2, SWT.NONE );

    item.setFont( 1, Graphics.getFont( "Arial", 20, SWT.BOLD ) );
    lca.renderChanges( item );

    Message message = Fixture.getProtocolMessage();
    JSONArray actual = ( JSONArray )message.findSetProperty( item, "cellFonts" );
    assertEquals( JSONObject.NULL, actual.get( 0 ) );
    JSONArray cellFont = actual.getJSONArray( 1 );
    assertTrue( jsonEquals( "[\"Arial\"]", cellFont.getJSONArray( 0 ) ) );
    assertEquals( Integer.valueOf( 20 ), cellFont.get( 1 ) );
    assertEquals( Boolean.TRUE, cellFont.get( 2 ) );
    assertEquals( Boolean.FALSE, cellFont.get( 3 ) );
  }

  public void testRenderCellFontsUnchanged() throws IOException {
    createGridColumns( grid, 2, SWT.NONE );
    Fixture.markInitialized( display );
    Fixture.markInitialized( item );

    item.setFont( 1, Graphics.getFont( "Arial", 20, SWT.BOLD ) );
    Fixture.preserveWidgets();
    lca.renderChanges( item );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( item, "cellFonts" ) );
  }

  public void testRenderInitialExpanded() throws IOException {
    lca.render( item );

    Message message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( item );
    assertTrue( operation.getPropertyNames().indexOf( "expanded" ) == -1 );
  }

  public void testRenderExpanded() throws IOException {
    new GridItem( item, SWT.NONE );

    item.setExpanded( true );
    lca.renderChanges( item );

    Message message = Fixture.getProtocolMessage();
    assertEquals( Boolean.TRUE, message.findSetProperty( item, "expanded" ) );
  }

  public void testRenderExpandedUnchanged() throws IOException {
    new GridItem( item, SWT.NONE );
    Fixture.markInitialized( display );
    Fixture.markInitialized( item );

    item.setExpanded( true );
    Fixture.preserveWidgets();
    lca.renderChanges( item );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( item, "expanded" ) );
  }

  public void testRenderInitialCellChecked() throws IOException {
    grid = new Grid( shell, SWT.CHECK );
    createGridColumns( grid, 2, SWT.NONE );
    item = new GridItem( grid, SWT.NONE );

    lca.render( item );

    Message message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( item );
    assertTrue( operation.getPropertyNames().indexOf( "cellChecked" ) == -1 );
  }

  public void testRenderCellChecked() throws IOException, JSONException {
    grid = new Grid( shell, SWT.CHECK );
    createGridColumns( grid, 2, SWT.NONE );
    item = new GridItem( grid, SWT.NONE );

    item.setChecked( 1, true );
    lca.renderChanges( item );

    Message message = Fixture.getProtocolMessage();
    JSONArray actual = ( JSONArray )message.findSetProperty( item, "cellChecked" );
    assertTrue( jsonEquals( "[false,true]", actual ) );  }

  public void testRenderCellCheckedUnchanged() throws IOException {
    grid = new Grid( shell, SWT.CHECK );
    createGridColumns( grid, 2, SWT.NONE );
    item = new GridItem( grid, SWT.NONE );
    Fixture.markInitialized( display );
    Fixture.markInitialized( item );

    item.setChecked( 1, true );
    Fixture.preserveWidgets();
    lca.renderChanges( item );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( item, "cellChecked" ) );
  }

  public void testRenderInitialCellGrayed() throws IOException {
    grid = new Grid( shell, SWT.CHECK );
    createGridColumns( grid, 2, SWT.NONE );
    item = new GridItem( grid, SWT.NONE );

    lca.render( item );

    Message message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( item );
    assertTrue( operation.getPropertyNames().indexOf( "cellGrayed" ) == -1 );
  }

  public void testRenderCellGrayed() throws IOException, JSONException {
    grid = new Grid( shell, SWT.CHECK );
    createGridColumns( grid, 2, SWT.NONE );
    item = new GridItem( grid, SWT.NONE );

    item.setGrayed( 1, true );
    lca.renderChanges( item );

    Message message = Fixture.getProtocolMessage();
    JSONArray actual = ( JSONArray )message.findSetProperty( item, "cellGrayed" );
    assertTrue( jsonEquals( "[false,true]", actual ) );
  }

  public void testRenderGrayedUnchanged() throws IOException {
    grid = new Grid( shell, SWT.CHECK );
    createGridColumns( grid, 2, SWT.NONE );
    item = new GridItem( grid, SWT.NONE );
    Fixture.markInitialized( display );
    Fixture.markInitialized( item );

    item.setGrayed( 1, true );
    Fixture.preserveWidgets();
    lca.renderChanges( item );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( item, "cellGrayed" ) );
  }

  public void testReadChecked() {
    grid = new Grid( shell, SWT.CHECK );
    createGridColumns( grid, 3, SWT.NONE );
    item = new GridItem( grid, SWT.NONE );

    Fixture.fakeSetParameter( getId( item ), "cellChecked", "[ true, false, true ]" );
    Fixture.readDataAndProcessAction( item );

    assertTrue( item.getChecked( 0 ) );
    assertFalse( item.getChecked( 1 ) );
    assertTrue( item.getChecked( 2 ) );
  }

  public void testProcessTreeEvent_Expanded() {
    List<TreeEvent> events = new LinkedList<TreeEvent>();
    grid.addTreeListener( new LoggingTreeListener( events ) );
    new GridItem( item, SWT.NONE );

    Fixture.fakeNotifyOperation( getId( item ), ClientMessageConst.EVENT_TREE_EXPANDED, null );
    Fixture.readDataAndProcessAction( item );

    assertEquals( 1, events.size() );
    SelectionEvent event = events.get( 0 );
    assertEquals( SWT.Expand, event.getID() );
    assertEquals( grid, event.getSource() );
    assertEquals( item, event.item );
    assertTrue( item.isExpanded() );
  }

  public void testProcessTreeEvent_Collapsed() {
    List<TreeEvent> events = new LinkedList<TreeEvent>();
    grid.addTreeListener( new LoggingTreeListener( events ) );
    new GridItem( item, SWT.NONE );
    item.setExpanded( true );

    Fixture.fakeNotifyOperation( getId( item ), ClientMessageConst.EVENT_TREE_COLLAPSED, null );
    Fixture.readDataAndProcessAction( item );

    assertEquals( 1, events.size() );
    SelectionEvent event = events.get( 0 );
    assertEquals( SWT.Collapse, event.getID() );
    assertEquals( grid, event.getSource() );
    assertEquals( item, event.item );
    assertFalse( item.isExpanded() );
  }

  public void testRenderScrollbarsVisibleAfterExpanded() throws JSONException {
    grid.setSize( 200, 200 );
    createGridColumns( grid, 3, SWT.NONE );
    GridItem[] items = createGridItems( grid, 5, 10 );

    Fixture.markInitialized( grid );
    Fixture.fakeNotifyOperation( getId( items[ 0 ] ), EVENT_TREE_EXPANDED, null );
    Fixture.executeLifeCycleFromServerThread();

    Message message = Fixture.getProtocolMessage();
    JSONArray actual = ( JSONArray )message.findSetProperty( grid, "scrollBarsVisible" );
    assertTrue( jsonEquals( "[ false, true ]", actual ) );
  }

  //////////////////
  // Helping classes

  private static class LoggingTreeListener extends TreeAdapter {
    private final List<TreeEvent> events;
    private LoggingTreeListener( List<TreeEvent> events ) {
      this.events = events;
    }
    @Override
    public void treeExpanded( TreeEvent event ) {
      events.add( event );
    }
    @Override
    public void treeCollapsed( TreeEvent event ) {
      events.add( event );
    }
  }
}
