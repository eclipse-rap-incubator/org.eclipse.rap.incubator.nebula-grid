/*******************************************************************************
 * Copyright (c) 2012, 2014 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.grid.internal.gridcolumngroupkit;

import static org.eclipse.nebula.widgets.grid.GridTestUtil.createGridColumns;
import static org.eclipse.nebula.widgets.grid.GridTestUtil.loadImage;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetUtil.getId;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.eclipse.nebula.widgets.grid.Grid;
import org.eclipse.nebula.widgets.grid.GridColumnGroup;
import org.eclipse.nebula.widgets.grid.internal.gridcolumngroupkit.GridColumnGroupLCA;
import org.eclipse.nebula.widgets.grid.internal.gridcolumngroupkit.GridColumnGroupOperationHandler;
import org.eclipse.rap.json.JsonArray;
import org.eclipse.rap.json.JsonObject;
import org.eclipse.rap.json.JsonValue;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.internal.remote.RemoteObjectRegistry;
import org.eclipse.rap.rwt.internal.lifecycle.WidgetUtil;
import org.eclipse.rap.rwt.remote.OperationHandler;
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
import org.eclipse.swt.widgets.Shell;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


@SuppressWarnings("restriction")
public class GridColumnGroupLCA_Test {

  private Display display;
  private Shell shell;
  private Grid grid;
  private GridColumnGroup group;
  private GridColumnGroupLCA lca;

  @Before
  public void setUp() {
    Fixture.setUp();
    display = new Display();
    shell = new Shell( display );
    grid = new Grid( shell, SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL );
    group = new GridColumnGroup( grid, SWT.NONE );
    lca = ( GridColumnGroupLCA )WidgetUtil.getLCA( group );
    Fixture.fakeNewRequest();
  }

  @After
  public void tearDown() {
    Fixture.tearDown();
  }

  @Test
  public void testRenderCreate() throws IOException {
    lca.renderInitialization( group );

    Message message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( group );
    assertEquals( "rwt.widgets.GridColumnGroup", operation.getType() );
  }

  @Test
  public void testRenderCreateWithAligment() throws IOException {
    group = new GridColumnGroup( grid, SWT.TOGGLE );

    lca.render( group );

    Message message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( group );
    List<Object> styles = Arrays.asList( operation.getStyles() );
    assertTrue( styles.contains( "TOGGLE" ) );
  }

  @Test
  public void testRenderInitialization_setsOperationHandler() throws IOException {
    String id = getId( group );

    lca.renderInitialization( group );

    OperationHandler handler = RemoteObjectRegistry.getInstance().get( id ).getHandler();
    assertTrue( handler instanceof GridColumnGroupOperationHandler );
  }

  @Test
  public void testRenderParent() throws IOException {
    lca.renderInitialization( group );

    Message message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( group );
    assertEquals( WidgetUtil.getId( group.getParent() ), operation.getParent() );
  }

  @Test
  public void testRenderDispose() throws IOException {
    lca.renderDispose( group );

    Message message = Fixture.getProtocolMessage();
    Operation operation = message.getOperation( 0 );
    assertTrue( operation instanceof DestroyOperation );
    assertEquals( WidgetUtil.getId( group ), operation.getTarget() );
  }

  @Test
  public void testRenderInitialText() throws IOException {
    lca.render( group );

    Message message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( group );
    assertTrue( operation.getPropertyNames().indexOf( "text" ) == -1 );
  }

  @Test
  public void testRenderText() throws IOException {
    group.setText( "foo" );
    lca.renderChanges( group );

    Message message = Fixture.getProtocolMessage();
    assertEquals( "foo", message.findSetProperty( group, "text" ).asString() );
  }

  @Test
  public void testRenderTextUnchanged() throws IOException {
    Fixture.markInitialized( display );
    Fixture.markInitialized( group );

    group.setText( "foo" );
    Fixture.preserveWidgets();
    lca.renderChanges( group );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( group, "text" ) );
  }

  @Test
  public void testRenderInitialImage() throws IOException {
    lca.renderChanges( group );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( group, "image" ) );
  }

  @Test
  public void testRenderImage() throws IOException {
    Image image = loadImage( display, Fixture.IMAGE_100x50 );

    group.setImage( image );
    lca.renderChanges( group );

    Message message = Fixture.getProtocolMessage();
    String imageLocation = ImageFactory.getImagePath( image );
    String expected = "[\"" + imageLocation + "\", 100, 50 ]";
    JsonArray actual = message.findSetProperty( group, "image" ).asArray();
    assertEquals( JsonArray.readFrom( expected ), actual );
  }

  @Test
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

  @Test
  public void testRenderImageReset() throws IOException {
    Fixture.markInitialized( display );
    Fixture.markInitialized( group );
    Image image = loadImage( display, Fixture.IMAGE_100x50 );
    group.setImage( image );

    Fixture.preserveWidgets();
    group.setImage( null );
    lca.renderChanges( group );

    Message message = Fixture.getProtocolMessage();
    assertEquals( JsonObject.NULL, message.findSetProperty( group, "image" ) );
  }

  @Test
  public void testRenderInitialFont() throws IOException {
    lca.render( group );

    Message message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( group );
    assertTrue( operation.getPropertyNames().indexOf( "font" ) == -1 );
  }

  @Test
  public void testRenderFont() throws IOException {
    group.setHeaderFont( new Font( display, "Arial", 20, SWT.BOLD ) );
    lca.renderChanges( group );

    Message message = Fixture.getProtocolMessage();
    JsonArray expected = JsonArray.readFrom( "[[\"Arial\"], 20, true, false]" );
    assertEquals( expected, message.findSetProperty( group, "font" ) );
  }

  @Test
  public void testRenderFontUnchanged() throws IOException {
    Fixture.markInitialized( display );
    Fixture.markInitialized( group );

    group.setHeaderFont( new Font( display, "Arial", 20, SWT.BOLD ) );
    Fixture.preserveWidgets();
    lca.renderChanges( group );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( group, "font" ) );
  }

  @Test
  public void testRenderInitialExpanded() throws IOException {
    lca.render( group );

    Message message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( group );
    assertTrue( operation.getPropertyNames().indexOf( "expanded" ) == -1 );
  }

  @Test
  public void testRenderExpanded() throws IOException {
    group.setExpanded( false );
    lca.renderChanges( group );

    Message message = Fixture.getProtocolMessage();
    assertEquals( JsonValue.FALSE, message.findSetProperty( group, "expanded" ) );
  }

  @Test
  public void testRenderExpandedUnchanged() throws IOException {
    Fixture.markInitialized( display );
    Fixture.markInitialized( group );

    group.setExpanded( false );
    Fixture.preserveWidgets();
    lca.renderChanges( group );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( group, "expanded" ) );
  }

  @Test
  public void testRenderInitialLeft() throws IOException {
    lca.render( group );

    Message message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( group );
    assertTrue( operation.getPropertyNames().indexOf( "left" ) == -1 );
  }

  @Test
  public void testRenderLeft() throws IOException {
    createGridColumns( grid, 3, SWT.NONE );
    grid.getColumn( 1 ).setVisible( false );
    createGridColumns( group, 3, SWT.NONE );

    grid.getColumn( 0 ).setWidth( 30 );
    lca.renderChanges( group );

    Message message = Fixture.getProtocolMessage();
    assertEquals( 90, message.findSetProperty( group, "left" ).asInt() );
  }

  @Test
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

  @Test
  public void testRenderInitialWidth() throws IOException {
    lca.render( group );

    Message message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( group );
    assertTrue( operation.getPropertyNames().indexOf( "width" ) == -1 );
  }

  @Test
  public void testRenderWidth() throws IOException {
    createGridColumns( group, 1, SWT.NONE );

    lca.renderChanges( group );

    Message message = Fixture.getProtocolMessage();
    assertEquals( 20, message.findSetProperty( group, "width" ).asInt() );
  }

  @Test
  public void testRenderWidthUnchanged() throws IOException {
    Fixture.markInitialized( display );
    Fixture.markInitialized( group );

    createGridColumns( group, 1, SWT.NONE );
    Fixture.preserveWidgets();
    lca.renderChanges( group );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( group, "width" ) );
  }

  @Test
  public void testRenderInitialHeight() throws IOException {
    lca.render( group );

    Message message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( group );
    assertTrue( operation.getPropertyNames().indexOf( "height" ) == -1 );
  }

  @Test
  public void testRenderHeight() throws IOException {
    createGridColumns( group, 1, SWT.NONE );
    grid.setHeaderVisible( true );

    lca.renderChanges( group );

    Message message = Fixture.getProtocolMessage();
    assertEquals( 31, message.findSetProperty( group, "height" ).asInt() );
  }

  @Test
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

  @Test
  public void testRenderInitialVisible() throws IOException {
    createGridColumns( group, 1, SWT.NONE );

    lca.render( group );

    Message message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( group );
    assertTrue( operation.getPropertyNames().indexOf( "visibility" ) == -1 );
  }

  @Test
  public void testRenderVisible() throws IOException {
    createGridColumns( group, 1, SWT.NONE );

    grid.getColumn( 0 ).setVisible( false );
    lca.renderChanges( group );

    Message message = Fixture.getProtocolMessage();
    assertEquals( JsonValue.FALSE, message.findSetProperty( group, "visibility" ) );
  }

  @Test
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

  @Test
  public void testRenderInitialCustomVariant() throws IOException {
    lca.render( group );

    Message message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( group );
    assertTrue( operation.getPropertyNames().indexOf( "customVariant" ) == -1 );
  }

  @Test
  public void testRenderCustomVariant() throws IOException {
    group.setData( RWT.CUSTOM_VARIANT, "blue" );
    lca.renderChanges( group );

    Message message = Fixture.getProtocolMessage();
    assertEquals( "variant_blue", message.findSetProperty( group, "customVariant" ).asString() );
  }

  @Test
  public void testRenderCustomVariantUnchanged() throws IOException {
    Fixture.markInitialized( display );
    Fixture.markInitialized( group );

    group.setData( RWT.CUSTOM_VARIANT, "blue" );
    Fixture.preserveWidgets();
    lca.renderChanges( group );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( group, "customVariant" ) );
  }

}
