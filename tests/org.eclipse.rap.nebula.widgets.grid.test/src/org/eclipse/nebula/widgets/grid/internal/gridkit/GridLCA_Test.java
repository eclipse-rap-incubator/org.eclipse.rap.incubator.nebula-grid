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

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.eclipse.nebula.widgets.grid.Grid;
import org.eclipse.rap.rwt.testfixture.Fixture;
import org.eclipse.rap.rwt.testfixture.Message;
import org.eclipse.rap.rwt.testfixture.Message.CreateOperation;
import org.eclipse.rwt.lifecycle.WidgetUtil;
import org.eclipse.swt.SWT;
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
    grid = new Grid( shell, SWT.NONE );
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
    assertTrue( GridLCATestUtil.jsonEquals( "[0,21]", actual ) );
  }

}
