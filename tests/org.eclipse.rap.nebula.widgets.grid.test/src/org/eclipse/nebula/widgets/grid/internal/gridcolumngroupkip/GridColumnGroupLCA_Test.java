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

import static org.eclipse.nebula.widgets.grid.internal.gridkit.GridLCATestUtil.jsonEquals;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.nebula.widgets.grid.Grid;
import org.eclipse.nebula.widgets.grid.GridColumnGroup;
import org.eclipse.nebula.widgets.grid.internal.gridcolumngroupkit.GridColumnGroupLCA;
import org.eclipse.rap.rwt.testfixture.Fixture;
import org.eclipse.rap.rwt.testfixture.Message;
import org.eclipse.rap.rwt.testfixture.Message.CreateOperation;
import org.eclipse.rap.rwt.testfixture.Message.DestroyOperation;
import org.eclipse.rap.rwt.testfixture.Message.Operation;
import org.eclipse.rwt.internal.lifecycle.JSConst;
import org.eclipse.rwt.lifecycle.WidgetUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TreeAdapter;
import org.eclipse.swt.events.TreeEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.json.JSONArray;
import org.json.JSONException;

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
    Fixture.fakeNewRequest( display );
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
    List<TreeEvent> events = new LinkedList<TreeEvent>();
    group.addTreeListener( new LoggingTreeListener( events ) );
    group.setExpanded( false );
    String groupId = WidgetUtil.getId( group );

    Fixture.fakeRequestParam( JSConst.EVENT_TREE_EXPANDED, groupId );
    Fixture.readDataAndProcessAction( group );

    assertEquals( 1, events.size() );
    SelectionEvent event = events.get( 0 );
    assertEquals( SWT.Expand, event.getID() );
    assertEquals( group, event.getSource() );
    assertTrue( group.getExpanded() );
  }

  public void testProcessTreeEvent_Collapsed() {
    List<TreeEvent> events = new LinkedList<TreeEvent>();
    group.addTreeListener( new LoggingTreeListener( events ) );
    String groupId = WidgetUtil.getId( group );

    Fixture.fakeRequestParam( JSConst.EVENT_TREE_COLLAPSED, groupId );
    Fixture.readDataAndProcessAction( group );

    assertEquals( 1, events.size() );
    SelectionEvent event = events.get( 0 );
    assertEquals( SWT.Collapse, event.getID() );
    assertEquals( group, event.getSource() );
    assertFalse( group.getExpanded() );
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
