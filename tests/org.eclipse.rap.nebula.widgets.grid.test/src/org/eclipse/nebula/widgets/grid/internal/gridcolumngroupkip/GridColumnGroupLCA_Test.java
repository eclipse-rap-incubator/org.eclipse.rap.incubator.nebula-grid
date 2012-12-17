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
package org.eclipse.nebula.widgets.grid.internal.gridcolumngroupkip;

import static org.eclipse.nebula.widgets.grid.GridTestUtil.createGridColumns;
import static org.eclipse.nebula.widgets.grid.GridTestUtil.loadImage;
import static org.eclipse.nebula.widgets.grid.internal.gridkit.GridLCATestUtil.jsonEquals;
import static org.eclipse.rap.rwt.lifecycle.WidgetUtil.getId;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.nebula.widgets.grid.Grid;
import org.eclipse.nebula.widgets.grid.GridColumnGroup;
import org.eclipse.nebula.widgets.grid.internal.gridcolumngroupkit.GridColumnGroupLCA;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.internal.protocol.ClientMessageConst;
import org.eclipse.rap.rwt.lifecycle.WidgetUtil;
import org.eclipse.rap.rwt.testfixture.Fixture;
import org.eclipse.rap.rwt.testfixture.Message;
import org.eclipse.rap.rwt.testfixture.Message.CreateOperation;
import org.eclipse.rap.rwt.testfixture.Message.DestroyOperation;
import org.eclipse.rap.rwt.testfixture.Message.Operation;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.internal.graphics.ImageFactory;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import junit.framework.TestCase;


@SuppressWarnings("restriction")
public class GridColumnGroupLCA_Test extends TestCase {

  private Display display;
  private Shell shell;
  private Grid grid;
  private GridColumnGroup group;
  private GridColumnGroupLCA lca;

  @Override
  protected void setUp() throws Exception {
    Fixture.setUp();
    display = new Display();
    shell = new Shell( display );
    grid = new Grid( shell, SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL );
    group = new GridColumnGroup( grid, SWT.NONE );
    lca = ( GridColumnGroupLCA )WidgetUtil.getLCA( group );
    Fixture.fakeNewRequest();
  }

  @Override
  protected void tearDown() throws Exception {
    Fixture.tearDown();
  }

  public void testRenderCreate() throws IOException {
    lca.renderInitialization( group );

    Message message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( group );
    assertEquals( "rwt.widgets.GridColumnGroup", operation.getType() );
  }

  public void testRenderCreateWithAligment() throws IOException {
    group = new GridColumnGroup( grid, SWT.TOGGLE );

    lca.render( group );

    Message message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( group );
    List<Object> styles = Arrays.asList( operation.getStyles() );
    assertTrue( styles.contains( "TOGGLE" ) );
  }

  public void testRenderParent() throws IOException {
    lca.renderInitialization( group );

    Message message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( group );
    assertEquals( WidgetUtil.getId( group.getParent() ), operation.getParent() );
  }

  public void testRenderDispose() throws IOException {
    lca.renderDispose( group );

    Message message = Fixture.getProtocolMessage();
    Operation operation = message.getOperation( 0 );
    assertTrue( operation instanceof DestroyOperation );
    assertEquals( WidgetUtil.getId( group ), operation.getTarget() );
  }

  public void testRenderInitialText() throws IOException {
    lca.render( group );

    Message message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( group );
    assertTrue( operation.getPropertyNames().indexOf( "text" ) == -1 );
  }

  public void testRenderText() throws IOException {
    group.setText( "foo" );
    lca.renderChanges( group );

    Message message = Fixture.getProtocolMessage();
    assertEquals( "foo", message.findSetProperty( group, "text" ) );
  }

  public void testRenderTextUnchanged() throws IOException {
    Fixture.markInitialized( display );
    Fixture.markInitialized( group );

    group.setText( "foo" );
    Fixture.preserveWidgets();
    lca.renderChanges( group );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( group, "text" ) );
  }

  public void testRenderInitialImage() throws IOException {
    lca.renderChanges( group );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( group, "image" ) );
  }

  public void testRenderImage() throws IOException, JSONException {
    Image image = loadImage( display, Fixture.IMAGE_100x50 );

    group.setImage( image );
    lca.renderChanges( group );

    Message message = Fixture.getProtocolMessage();
    String imageLocation = ImageFactory.getImagePath( image );
    String expected = "[\"" + imageLocation + "\", 100, 50 ]";
    JSONArray actual = ( JSONArray )message.findSetProperty( group, "image" );
    assertTrue( jsonEquals( expected, actual ) );
  }

  public void testRenderImageUnchanged() throws IOException {
    Fixture.markInitialized( display );
    Fixture.markInitialized( group );
    Image image = loadImage( display, Fixture.IMAGE_100x50 );

    group.setImage( image );
    Fixture.preserveWidgets();
    lca.renderChanges( group );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( group, "image" ) );
  }

  public void testRenderImageReset() throws IOException {
    Fixture.markInitialized( display );
    Fixture.markInitialized( group );
    Image image = loadImage( display, Fixture.IMAGE_100x50 );
    group.setImage( image );

    Fixture.preserveWidgets();
    group.setImage( null );
    lca.renderChanges( group );

    Message message = Fixture.getProtocolMessage();
    assertEquals( JSONObject.NULL, message.findSetProperty( group, "image" ) );
  }

  public void testRenderInitialFont() throws IOException {
    lca.render( group );

    Message message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( group );
    assertTrue( operation.getPropertyNames().indexOf( "font" ) == -1 );
  }

  public void testRenderFont() throws IOException, JSONException {
    group.setHeaderFont( new Font( display, "Arial", 20, SWT.BOLD ) );
    lca.renderChanges( group );

    Message message = Fixture.getProtocolMessage();
    JSONArray actual = ( JSONArray )message.findSetProperty( group, "font" );
    assertTrue( jsonEquals( "[\"Arial\"]", actual.getJSONArray( 0 ) ) );
    assertEquals( Integer.valueOf( 20 ), actual.get( 1 ) );
    assertEquals( Boolean.TRUE, actual.get( 2 ) );
    assertEquals( Boolean.FALSE, actual.get( 3 ) );
  }

  public void testRenderFontUnchanged() throws IOException {
    Fixture.markInitialized( display );
    Fixture.markInitialized( group );

    group.setHeaderFont( new Font( display, "Arial", 20, SWT.BOLD ) );
    Fixture.preserveWidgets();
    lca.renderChanges( group );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( group, "font" ) );
  }

  public void testRenderInitialExpanded() throws IOException {
    lca.render( group );

    Message message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( group );
    assertTrue( operation.getPropertyNames().indexOf( "expanded" ) == -1 );
  }

  public void testRenderExpanded() throws IOException {
    group.setExpanded( false );
    lca.renderChanges( group );

    Message message = Fixture.getProtocolMessage();
    assertEquals( Boolean.FALSE, message.findSetProperty( group, "expanded" ) );
  }

  public void testRenderExpandedUnchanged() throws IOException {
    Fixture.markInitialized( display );
    Fixture.markInitialized( group );

    group.setExpanded( false );
    Fixture.preserveWidgets();
    lca.renderChanges( group );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( group, "expanded" ) );
  }

  public void testProcessTreeEvent_Expanded() {
    List<Event> events = new LinkedList<Event>();
    group.addListener( SWT.Expand, new LoggingTreeListener( events ) );
    group.setExpanded( false );

    Fixture.fakeNotifyOperation( getId( group ), ClientMessageConst.EVENT_EXPAND, null );
    Fixture.readDataAndProcessAction( group );

    assertEquals( 1, events.size() );
    Event event = events.get( 0 );
    assertEquals( group, event.widget );
  }

  public void testProcessTreeEvent_Collapsed() {
    List<Event> events = new LinkedList<Event>();
    group.addListener( SWT.Collapse, new LoggingTreeListener( events ) );

    Fixture.fakeNotifyOperation( getId( group ), ClientMessageConst.EVENT_COLLAPSE, null );
    Fixture.readDataAndProcessAction( group );

    assertEquals( 1, events.size() );
    Event event = events.get( 0 );
    assertEquals( group, event.widget );
  }

  public void testReadExpanded_Expanded() {
    group.setExpanded( false );

    Fixture.fakeSetParameter( getId( group ), "expanded", Boolean.TRUE );
    Fixture.readDataAndProcessAction( group );

    assertTrue( group.getExpanded() );
  }

  public void testReadExpanded_Collapsed() {
    Fixture.fakeSetParameter( getId( group ), "expanded", Boolean.FALSE );
    Fixture.readDataAndProcessAction( group );

    assertFalse( group.getExpanded() );
  }

  public void testRenderInitialLeft() throws IOException {
    lca.render( group );

    Message message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( group );
    assertTrue( operation.getPropertyNames().indexOf( "left" ) == -1 );
  }

  public void testRenderLeft() throws IOException {
    createGridColumns( grid, 3, SWT.NONE );
    grid.getColumn( 1 ).setVisible( false );
    createGridColumns( group, 3, SWT.NONE );

    grid.getColumn( 0 ).setWidth( 30 );
    lca.renderChanges( group );

    Message message = Fixture.getProtocolMessage();
    assertEquals( Integer.valueOf( 90 ), message.findSetProperty( group, "left" ) );
  }

  public void testRenderLeftUnchanged() throws IOException {
    createGridColumns( grid, 3, SWT.NONE );
    grid.getColumn( 1 ).setVisible( false );
    createGridColumns( group, 3, SWT.NONE );
    Fixture.markInitialized( display );
    Fixture.markInitialized( group );

    grid.getColumn( 0 ).setWidth( 30 );
    Fixture.preserveWidgets();
    lca.renderChanges( group );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( group, "left" ) );
  }

  public void testRenderInitialWidth() throws IOException {
    lca.render( group );

    Message message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( group );
    assertTrue( operation.getPropertyNames().indexOf( "width" ) == -1 );
  }

  public void testRenderWidth() throws IOException {
    createGridColumns( group, 1, SWT.NONE );

    lca.renderChanges( group );

    Message message = Fixture.getProtocolMessage();
    assertEquals( Integer.valueOf( 20 ), message.findSetProperty( group, "width" ) );
  }

  public void testRenderWidthUnchanged() throws IOException {
    Fixture.markInitialized( display );
    Fixture.markInitialized( group );

    createGridColumns( group, 1, SWT.NONE );
    Fixture.preserveWidgets();
    lca.renderChanges( group );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( group, "width" ) );
  }

  public void testRenderInitialHeight() throws IOException {
    lca.render( group );

    Message message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( group );
    assertTrue( operation.getPropertyNames().indexOf( "height" ) == -1 );
  }

  public void testRenderHeight() throws IOException {
    createGridColumns( group, 1, SWT.NONE );
    grid.setHeaderVisible( true );

    lca.renderChanges( group );

    Message message = Fixture.getProtocolMessage();
    assertEquals( Integer.valueOf( 31 ), message.findSetProperty( group, "height" ) );
  }

  public void testRenderHeightUnchanged() throws IOException {
    createGridColumns( group, 1, SWT.NONE );
    Fixture.markInitialized( display );
    Fixture.markInitialized( group );

    grid.setHeaderVisible( true );
    Fixture.preserveWidgets();
    lca.renderChanges( group );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( group, "height" ) );
  }

  public void testRenderInitialVisible() throws IOException {
    createGridColumns( group, 1, SWT.NONE );

    lca.render( group );

    Message message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( group );
    assertTrue( operation.getPropertyNames().indexOf( "visibility" ) == -1 );
  }

  public void testRenderVisible() throws IOException {
    createGridColumns( group, 1, SWT.NONE );

    grid.getColumn( 0 ).setVisible( false );
    lca.renderChanges( group );

    Message message = Fixture.getProtocolMessage();
    assertEquals( Boolean.FALSE, message.findSetProperty( group, "visibility" ) );
  }

  public void testRenderVisibleUnchanged() throws IOException {
    createGridColumns( group, 1, SWT.NONE );
    Fixture.markInitialized( display );
    Fixture.markInitialized( group );

    grid.getColumn( 0 ).setVisible( false );
    Fixture.preserveWidgets();
    lca.renderChanges( group );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( group, "visibility" ) );
  }

  public void testRenderInitialCustomVariant() throws IOException {
    lca.render( group );

    Message message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( group );
    assertTrue( operation.getPropertyNames().indexOf( "customVariant" ) == -1 );
  }

  public void testRenderCustomVariant() throws IOException {
    group.setData( RWT.CUSTOM_VARIANT, "blue" );
    lca.renderChanges( group );

    Message message = Fixture.getProtocolMessage();
    assertEquals( "variant_blue", message.findSetProperty( group, "customVariant" ) );
  }

  public void testRenderCustomVariantUnchanged() throws IOException {
    Fixture.markInitialized( display );
    Fixture.markInitialized( group );

    group.setData( RWT.CUSTOM_VARIANT, "blue" );
    Fixture.preserveWidgets();
    lca.renderChanges( group );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( group, "customVariant" ) );
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
