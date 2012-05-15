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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.rap.rwt.testfixture.Fixture;
import org.eclipse.rwt.lifecycle.PhaseId;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import junit.framework.TestCase;


public class GridColumn_Test extends TestCase {

  private Display display;
  private Shell shell;
  private Grid grid;

  @Override
  protected void setUp() throws Exception {
    Fixture.setUp();
    Fixture.fakePhase( PhaseId.PROCESS_ACTION );
    display = new Display();
    shell = new Shell( display );
    grid = new Grid( shell, SWT.H_SCROLL | SWT.V_SCROLL );
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

  //////////////////
  // Helping methods

  private static GridColumn[] createGridColumns( Grid grid, int columns, int style ) {
    GridColumn[] result = new GridColumn[ columns ];
    for( int i = 0; i < columns; i++ ) {
      GridColumn column = new GridColumn( grid, style );
      result[ i ] = column;
    }
    return result;
  }
}
