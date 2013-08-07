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
package org.eclipse.nebula.widgets.grid.internal.griditemkit;

import static org.eclipse.nebula.widgets.grid.GridTestUtil.createGridColumns;
import static org.eclipse.nebula.widgets.grid.GridTestUtil.createGridItems;
import static org.eclipse.nebula.widgets.grid.GridTestUtil.loadImage;
import static org.eclipse.rap.rwt.lifecycle.WidgetUtil.getId;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import junit.framework.TestCase;

import org.eclipse.nebula.widgets.grid.Grid;
import org.eclipse.nebula.widgets.grid.GridItem;
import org.eclipse.rap.json.JsonArray;
import org.eclipse.rap.json.JsonObject;
import org.eclipse.rap.json.JsonValue;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.internal.protocol.ClientMessageConst;
import org.eclipse.rap.rwt.internal.remote.RemoteObjectImpl;
import org.eclipse.rap.rwt.internal.remote.RemoteObjectRegistry;
import org.eclipse.rap.rwt.lifecycle.WidgetUtil;
import org.eclipse.rap.rwt.testfixture.Fixture;
import org.eclipse.rap.rwt.testfixture.Message;
import org.eclipse.rap.rwt.testfixture.Message.CreateOperation;
import org.eclipse.rap.rwt.testfixture.Message.DestroyOperation;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.junit.Test;


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
    Fixture.fakeNewRequest();
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
    assertEquals( 3, operation.getProperty( "index" ).asInt() );
  }

  public void testRenderCreate_WithParentItem() throws IOException {
    GridItem[] items = createGridItems( grid, 3, 3 );

    lca.renderInitialization( items[ 10 ] );

    Message message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( items[ 10 ] );
    assertEquals( "rwt.widgets.GridItem", operation.getType() );
    assertEquals( 1, operation.getProperty( "index" ).asInt() );
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

  @Test
  public void testRenderDispose_withDisposedParent_destroysRemoteObjects() throws IOException {
    lca.renderInitialization( item );
    RemoteObjectImpl remoteObject = RemoteObjectRegistry.getInstance().get( getId( item ) );
    grid.dispose();

    lca.renderDispose( item );

    assertTrue( remoteObject.isDestroyed() );
  }

  public void testRenderInitialCustomVariant() throws IOException {
    lca.render( item );

    Message message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( item );
    assertTrue( operation.getPropertyNames().indexOf( "customVariant" ) == -1 );
  }

  public void testRenderCustomVariant() throws IOException {
    item.setData( RWT.CUSTOM_VARIANT, "blue" );
    lca.renderChanges( item );

    Message message = Fixture.getProtocolMessage();
    assertEquals( "variant_blue", message.findSetProperty( item, "customVariant" ).asString() );
  }

  public void testRenderCustomVariantUnchanged() throws IOException {
    Fixture.markInitialized( display );
    Fixture.markInitialized( item );

    item.setData( RWT.CUSTOM_VARIANT, "blue" );
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
    assertEquals( 10, message.findSetProperty( items[ 0 ], "itemCount" ).asInt() );
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
    assertEquals( 10, message.findSetProperty( item, "height" ).asInt() );
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

  public void testRenderTexts() throws IOException {
    createGridColumns( grid, 2, SWT.NONE );

    item.setText( 0, "item 0.0" );
    item.setText( 1, "item 0.1" );
    lca.renderChanges( item );

    Message message = Fixture.getProtocolMessage();
    JsonArray expected = JsonArray.readFrom( "[\"item 0.0\", \"item 0.1\"]" );
    assertEquals( expected, message.findSetProperty( item, "texts" ) );
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

  public void testRenderImages() throws IOException {
    createGridColumns( grid, 2, SWT.NONE );
    Image image = loadImage( display, Fixture.IMAGE1 );

    item.setImage( 1, image );
    lca.renderChanges( item );

    Message message = Fixture.getProtocolMessage();
    JsonValue actual = message.findSetProperty( item, "images" );
    String expected = "[null, [\"rwt-resources/generated/90fb0bfe.gif\",58,12]]";
    assertEquals( JsonArray.readFrom( expected ), actual );
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

  public void testRenderBackground() throws IOException {
    item.setBackground( display.getSystemColor( SWT.COLOR_GREEN ) );
    lca.renderChanges( item );

    Message message = Fixture.getProtocolMessage();
    JsonValue actual = message.findSetProperty( item, "background" );
    assertEquals( JsonArray.readFrom( "[0,255,0,255]" ), actual );
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

  public void testRenderForeground() throws IOException {
    item.setForeground( display.getSystemColor( SWT.COLOR_GREEN ) );
    lca.renderChanges( item );

    Message message = Fixture.getProtocolMessage();
    JsonValue actual = message.findSetProperty( item, "foreground" );
    assertEquals( JsonArray.readFrom( "[0,255,0,255]" ), actual );
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

  public void testRenderFont() throws IOException {
    item.setFont( new Font( display, "Arial", 20, SWT.BOLD ) );
    lca.renderChanges( item );

    Message message = Fixture.getProtocolMessage();
    JsonValue actual = message.findSetProperty( item, "font" );
    assertEquals( JsonArray.readFrom( "[[\"Arial\"], 20, true, false]" ), actual );
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

  public void testRenderCellBackgrounds() throws IOException {
    createGridColumns( grid, 2, SWT.NONE );

    item.setBackground( 1, display.getSystemColor( SWT.COLOR_GREEN ) );
    lca.renderChanges( item );

    Message message = Fixture.getProtocolMessage();
    JsonValue actual = message.findSetProperty( item, "cellBackgrounds" );
    assertEquals( JsonArray.readFrom( "[null, [0,255,0,255]]" ), actual );
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

  public void testRenderCellForegrounds() throws IOException {
    createGridColumns( grid, 2, SWT.NONE );

    item.setForeground( 1, display.getSystemColor( SWT.COLOR_GREEN ) );
    lca.renderChanges( item );

    Message message = Fixture.getProtocolMessage();
    JsonValue actual = message.findSetProperty( item, "cellForegrounds" );
    assertEquals( JsonArray.readFrom( "[null, [0,255,0,255]]" ), actual );
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

  public void testRenderCellFonts() throws IOException {
    createGridColumns( grid, 2, SWT.NONE );

    item.setFont( 1, new Font( display, "Arial", 20, SWT.BOLD ) );
    lca.renderChanges( item );

    Message message = Fixture.getProtocolMessage();
    JsonValue actual = message.findSetProperty( item, "cellFonts" );
    assertEquals( JsonArray.readFrom( "[null, [[\"Arial\"], 20, true, false]]" ), actual );
  }

  public void testRenderCellFontsUnchanged() throws IOException {
    createGridColumns( grid, 2, SWT.NONE );
    Fixture.markInitialized( display );
    Fixture.markInitialized( item );

    item.setFont( 1, new Font( display, "Arial", 20, SWT.BOLD ) );
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
    assertEquals( JsonValue.TRUE, message.findSetProperty( item, "expanded" ) );
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

  public void testRenderCellChecked() throws IOException {
    grid = new Grid( shell, SWT.CHECK );
    createGridColumns( grid, 2, SWT.NONE );
    item = new GridItem( grid, SWT.NONE );

    item.setChecked( 1, true );
    lca.renderChanges( item );

    Message message = Fixture.getProtocolMessage();
    JsonArray expected = JsonArray.readFrom( "[false,true]" );
    assertEquals( expected, message.findSetProperty( item, "cellChecked" ) );
  }

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

  public void testRenderCellGrayed() throws IOException {
    grid = new Grid( shell, SWT.CHECK );
    createGridColumns( grid, 2, SWT.NONE );
    item = new GridItem( grid, SWT.NONE );

    item.setGrayed( 1, true );
    lca.renderChanges( item );

    Message message = Fixture.getProtocolMessage();
    JsonArray expected = JsonArray.readFrom( "[false, true]" );
    assertEquals( expected, message.findSetProperty( item, "cellGrayed" ) );
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

  public void testRenderInitialCellCheckable() throws IOException {
    grid = new Grid( shell, SWT.CHECK );
    createGridColumns( grid, 2, SWT.NONE );
    item = new GridItem( grid, SWT.NONE );

    lca.render( item );

    Message message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( item );
    assertTrue( operation.getPropertyNames().indexOf( "cellCheckable" ) == -1 );
  }

  public void testRenderCellCheckable() throws IOException {
    grid = new Grid( shell, SWT.CHECK );
    createGridColumns( grid, 2, SWT.NONE );
    item = new GridItem( grid, SWT.NONE );

    item.setCheckable( 1, false );
    lca.renderChanges( item );

    Message message = Fixture.getProtocolMessage();
    JsonArray expected = JsonArray.readFrom( "[true, false]" );
    assertEquals( expected, message.findSetProperty( item, "cellCheckable" ) );
  }

  public void testRenderCellCheckableUnchanged() throws IOException {
    grid = new Grid( shell, SWT.CHECK );
    createGridColumns( grid, 2, SWT.NONE );
    item = new GridItem( grid, SWT.NONE );
    Fixture.markInitialized( display );
    Fixture.markInitialized( item );

    item.setCheckable( 1, false );
    Fixture.preserveWidgets();
    lca.renderChanges( item );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( item, "cellCheckable" ) );
  }

  public void testReadChecked() {
    grid = new Grid( shell, SWT.CHECK );
    createGridColumns( grid, 3, SWT.NONE );
    item = new GridItem( grid, SWT.NONE );

    JsonArray cellChecked = new JsonArray()
      .add( true )
      .add( false )
      .add( true );
    Fixture.fakeSetProperty( getId( item ), "cellChecked", cellChecked );
    Fixture.readDataAndProcessAction( item );

    assertTrue( item.getChecked( 0 ) );
    assertFalse( item.getChecked( 1 ) );
    assertTrue( item.getChecked( 2 ) );
  }

  public void testProcessTreeEvent_Expanded() {
    List<Event> events = new LinkedList<Event>();
    grid.addListener( SWT.Expand, new LoggingTreeListener( events ) );
    new GridItem( item, SWT.NONE );

    Fixture.fakeSetProperty( getId( item ), "expanded", true );
    fakeTreeEvent( item, ClientMessageConst.EVENT_EXPAND );
    Fixture.readDataAndProcessAction( display );

    assertEquals( 1, events.size() );
    Event event = events.get( 0 );
    assertEquals( grid, event.widget );
    assertEquals( item, event.item );
    assertTrue( item.isExpanded() );
  }

  public void testProcessTreeEvent_Collapsed() {
    List<Event> events = new LinkedList<Event>();
    grid.addListener( SWT.Collapse, new LoggingTreeListener( events ) );
    new GridItem( item, SWT.NONE );
    item.setExpanded( true );

    Fixture.fakeSetProperty( getId( item ), "expanded", false );
    fakeTreeEvent( item, ClientMessageConst.EVENT_COLLAPSE );
    Fixture.readDataAndProcessAction( display );

    assertEquals( 1, events.size() );
    Event event = events.get( 0 );
    assertEquals( grid, event.widget );
    assertEquals( item, event.item );
    assertFalse( item.isExpanded() );
  }

  public void testRenderScrollbarsVisibleAfterExpanded() {
    grid.setSize( 200, 200 );
    createGridColumns( grid, 3, SWT.NONE );
    GridItem[] items = createGridItems( grid, 5, 10 );

    Fixture.markInitialized( grid );
    Fixture.fakeSetProperty( getId( items[ 0 ] ), "expanded", true );
    Fixture.executeLifeCycleFromServerThread();

    Message message = Fixture.getProtocolMessage();
    assertNotNull( message.findSetOperation( grid.getVerticalBar(), "visibility" ) );
  }

  @Test
  public void testRenderData() throws IOException {
    WidgetUtil.registerDataKeys( new String[]{ "foo", "bar" } );
    item.setData( "foo", "string" );
    item.setData( "bar", Integer.valueOf( 1 ) );

    lca.renderChanges( item );

    Message message = Fixture.getProtocolMessage();
    JsonObject data = ( JsonObject )message.findSetProperty( item, "data" );
    assertEquals( "string", data.get( "foo" ).asString() );
    assertEquals( 1, data.get( "bar" ).asInt() );
  }

  @Test
  public void testRenderDataUnchanged() throws IOException {
    WidgetUtil.registerDataKeys( new String[]{ "foo" } );
    item.setData( "foo", "string" );
    Fixture.markInitialized( display );
    Fixture.markInitialized( item );

    Fixture.preserveWidgets();
    lca.renderChanges( item );

    Message message = Fixture.getProtocolMessage();
    assertEquals( 0, message.getOperationCount() );
  }

  private static void fakeTreeEvent( GridItem item, String eventName ) {
    JsonObject parameters = new JsonObject()
      .add( ClientMessageConst.EVENT_PARAM_ITEM, getId( item ) );
    Fixture.fakeNotifyOperation( getId( item.getParent() ), eventName, parameters );
  }

  //////////////////
  // Helping classes

  private static class LoggingTreeListener implements Listener {
    private final List<Event> events;
    private LoggingTreeListener( List<Event> events ) {
      this.events = events;
    }
    public void handleEvent( Event event ) {
      events.add( event );
    }
  }

}
