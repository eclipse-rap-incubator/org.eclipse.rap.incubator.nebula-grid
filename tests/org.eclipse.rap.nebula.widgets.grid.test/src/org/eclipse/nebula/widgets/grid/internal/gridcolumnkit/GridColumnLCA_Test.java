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
package org.eclipse.nebula.widgets.grid.internal.gridcolumnkit;

import static org.eclipse.nebula.widgets.grid.GridTestUtil.loadImage;
import static org.eclipse.nebula.widgets.grid.internal.gridkit.GridLCATestUtil.jsonEquals;

import java.io.IOException;

import org.eclipse.nebula.widgets.grid.Grid;
import org.eclipse.nebula.widgets.grid.GridColumn;
import org.eclipse.rap.rwt.testfixture.Fixture;
import org.eclipse.rap.rwt.testfixture.Message;
import org.eclipse.rap.rwt.testfixture.Message.CreateOperation;
import org.eclipse.rap.rwt.testfixture.Message.DestroyOperation;
import org.eclipse.rap.rwt.testfixture.Message.Operation;
import org.eclipse.rwt.lifecycle.WidgetUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.internal.graphics.ImageFactory;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import junit.framework.TestCase;


@SuppressWarnings("restriction")
public class GridColumnLCA_Test extends TestCase {

  private Display display;
  private Shell shell;
  private Grid grid;
  private GridColumn column;
  private GridColumnLCA lca;

  @Override
  protected void setUp() throws Exception {
    Fixture.setUp();
    display = new Display();
    shell = new Shell( display );
    grid = new Grid( shell, SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL );
    column = new GridColumn( grid, SWT.NONE );
    lca = ( GridColumnLCA )WidgetUtil.getLCA( column );
    Fixture.fakeNewRequest( display );
  }

  @Override
  protected void tearDown() throws Exception {
    Fixture.tearDown();
  }

  public void testRenderCreate() throws IOException {
    lca.renderInitialization( column );

    Message message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( column );
    assertEquals( "rwt.widgets.TableColumn", operation.getType() );
  }

  public void testRenderCreateWithAligment() throws IOException {
    column = new GridColumn( grid, SWT.RIGHT );

    lca.render( column );

    Message message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( column );
    assertTrue( operation.getPropertyNames().indexOf( "style" ) == -1 );
    assertEquals( "right", message.findCreateProperty( column, "alignment" ) );
  }

  public void testRenderParent() throws IOException {
    lca.renderInitialization( column );

    Message message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( column );
    assertEquals( WidgetUtil.getId( column.getParent() ), operation.getParent() );
  }

  public void testRenderDispose() throws IOException {
    lca.renderDispose( column );

    Message message = Fixture.getProtocolMessage();
    Operation operation = message.getOperation( 0 );
    assertTrue( operation instanceof DestroyOperation );
    assertEquals( WidgetUtil.getId( column ), operation.getTarget() );
  }

  public void testRenderInitialToolTip() throws IOException {
    lca.render( column );

    Message message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( column );
    assertTrue( operation.getPropertyNames().indexOf( "toolTip" ) == -1 );
  }

  public void testRenderToolTip() throws IOException {
    column.setHeaderTooltip( "foo" );
    lca.renderChanges( column );

    Message message = Fixture.getProtocolMessage();
    assertEquals( "foo", message.findSetProperty( column, "toolTip" ) );
  }

  public void testRenderToolTipUnchanged() throws IOException {
    Fixture.markInitialized( display );
    Fixture.markInitialized( column );

    column.setHeaderTooltip( "foo" );
    Fixture.preserveWidgets();
    lca.renderChanges( column );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( column, "toolTip" ) );
  }

  public void testRenderInitialCustomVariant() throws IOException {
    lca.render( column );

    Message message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( column );
    assertTrue( operation.getPropertyNames().indexOf( "customVariant" ) == -1 );
  }

  public void testRenderCustomVariant() throws IOException {
    column.setData( WidgetUtil.CUSTOM_VARIANT, "blue" );
    lca.renderChanges( column );

    Message message = Fixture.getProtocolMessage();
    assertEquals( "variant_blue", message.findSetProperty( column, "customVariant" ) );
  }

  public void testRenderCustomVariantUnchanged() throws IOException {
    Fixture.markInitialized( display );
    Fixture.markInitialized( column );

    column.setData( WidgetUtil.CUSTOM_VARIANT, "blue" );
    Fixture.preserveWidgets();
    lca.renderChanges( column );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( column, "customVariant" ) );
  }

  public void testRenderInitialText() throws IOException {
    lca.render( column );

    Message message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( column );
    assertTrue( operation.getPropertyNames().indexOf( "text" ) == -1 );
  }

  public void testRenderText() throws IOException {
    column.setText( "foo" );
    lca.renderChanges( column );

    Message message = Fixture.getProtocolMessage();
    assertEquals( "foo", message.findSetProperty( column, "text" ) );
  }

  public void testRenderTextUnchanged() throws IOException {
    Fixture.markInitialized( display );
    Fixture.markInitialized( column );

    column.setText( "foo" );
    Fixture.preserveWidgets();
    lca.renderChanges( column );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( column, "text" ) );
  }

  public void testRenderInitialImage() throws IOException {
    lca.renderChanges( column );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( column, "image" ) );
  }

  public void testRenderImage() throws IOException, JSONException {
    Image image = loadImage( display, Fixture.IMAGE_100x50 );

    column.setImage( image );
    lca.renderChanges( column );

    Message message = Fixture.getProtocolMessage();
    String imageLocation = ImageFactory.getImagePath( image );
    String expected = "[\"" + imageLocation + "\", 100, 50 ]";
    JSONArray actual = ( JSONArray )message.findSetProperty( column, "image" );
    assertTrue( jsonEquals( expected, actual ) );
  }

  public void testRenderImageUnchanged() throws IOException {
    Fixture.markInitialized( display );
    Fixture.markInitialized( column );
    Image image = loadImage( display, Fixture.IMAGE_100x50 );

    column.setImage( image );
    Fixture.preserveWidgets();
    lca.renderChanges( column );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( column, "image" ) );
  }

  public void testRenderImageReset() throws IOException {
    Fixture.markInitialized( display );
    Fixture.markInitialized( column );
    Image image = loadImage( display, Fixture.IMAGE_100x50 );
    column.setImage( image );

    Fixture.preserveWidgets();
    column.setImage( null );
    lca.renderChanges( column );

    Message message = Fixture.getProtocolMessage();
    assertEquals( JSONObject.NULL, message.findSetProperty( column, "image" ) );
  }

  public void testRenderInitialIndex() throws IOException {
    lca.render( column );

    Message message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( column );
    assertTrue( operation.getPropertyNames().indexOf( "index" ) == -1 );
  }

  public void testRenderIndex() throws IOException {
    new GridColumn( grid, SWT.NONE, 0 );
    lca.renderChanges( column );

    Message message = Fixture.getProtocolMessage();
    assertEquals( Integer.valueOf( 1 ), message.findSetProperty( column, "index" ) );
  }

  public void testRenderIndexUnchanged() throws IOException {
    Fixture.markInitialized( display );
    Fixture.markInitialized( column );

    new GridColumn( grid, SWT.NONE, 0 );
    Fixture.preserveWidgets();
    lca.renderChanges( column );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( column, "index" ) );
  }

  public void testRenderInitialLeft() throws IOException {
    lca.render( column );

    Message message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( column );
    assertTrue( operation.getPropertyNames().indexOf( "left" ) == -1 );
  }

  public void testRenderLeft() throws IOException {
    GridColumn column2 = new GridColumn( grid, SWT.NONE, 0 );
    column2.setWidth( 50 );
    lca.renderChanges( column );

    Message message = Fixture.getProtocolMessage();
    assertEquals( Integer.valueOf( 50 ), message.findSetProperty( column, "left" ) );
  }

  public void testRenderLeftUnchanged() throws IOException {
    Fixture.markInitialized( display );
    Fixture.markInitialized( column );

    GridColumn column2 = new GridColumn( grid, SWT.NONE, 0 );
    column2.setWidth( 50 );
    Fixture.preserveWidgets();
    lca.renderChanges( column );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( column, "left" ) );
  }

  public void testRenderInitialWidth() throws IOException {
    lca.render( column );

    Message message = Fixture.getProtocolMessage();
    assertEquals( Integer.valueOf( 10 ), message.findCreateProperty( column, "width" ) );
  }

  public void testRenderWidth() throws IOException {
    column.setWidth( 50 );
    lca.renderChanges( column );

    Message message = Fixture.getProtocolMessage();
    assertEquals( Integer.valueOf( 50 ), message.findSetProperty( column, "width" ) );
  }

  public void testRenderWidthUnchanged() throws IOException {
    Fixture.markInitialized( display );
    Fixture.markInitialized( column );

    column.setWidth( 50 );
    Fixture.preserveWidgets();
    lca.renderChanges( column );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( column, "width" ) );
  }

  public void testRenderInitialAlignment() throws IOException {
    lca.render( column );

    Message message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( column );
    assertTrue( operation.getPropertyNames().indexOf( "alignment" ) == -1 );
  }

  public void testRenderAlignment() throws IOException {
    column.setAlignment( SWT.RIGHT );
    lca.renderChanges( column );

    Message message = Fixture.getProtocolMessage();
    assertEquals( "right", message.findSetProperty( column, "alignment" ) );
  }

  public void testRenderAlignmentUnchanged() throws IOException {
    Fixture.markInitialized( display );
    Fixture.markInitialized( column );

    column.setAlignment( SWT.RIGHT );
    Fixture.preserveWidgets();
    lca.renderChanges( column );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( column, "alignment" ) );
  }
}
