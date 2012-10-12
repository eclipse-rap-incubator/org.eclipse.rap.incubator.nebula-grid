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
import static org.eclipse.nebula.widgets.grid.GridTestUtil.loadImage;

import org.eclipse.rap.rwt.lifecycle.PhaseId;
import org.eclipse.rap.rwt.testfixture.Fixture;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.TreeAdapter;
import org.eclipse.swt.events.TreeListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import junit.framework.TestCase;


public class GridColumnGroup_Test extends TestCase {

  private Display display;
  private Shell shell;
  private Grid grid;
  private GridColumnGroup group;

  @Override
  protected void setUp() throws Exception {
    Fixture.setUp();
    Fixture.fakePhase( PhaseId.PROCESS_ACTION );
    display = new Display();
    shell = new Shell( display );
    grid = new Grid( shell, SWT.H_SCROLL | SWT.V_SCROLL );
    group = new GridColumnGroup( grid, SWT.NONE );
  }

  @Override
  protected void tearDown() throws Exception {
    Fixture.tearDown();
  }

  public void testGridColumnGroupCreation() {
    assertSame( grid, group.getParent() );
    assertSame( group, grid.getColumnGroup( 0 ) );
    assertEquals( 0, group.getColumns().length );
  }

  public void testDispose() {
    group.dispose();

    assertTrue( group.isDisposed() );
    assertEquals( 0, grid.getColumnGroupCount() );
  }

  public void testDispose_DisposeColumns() {
    GridColumn[] columns = createGridColumns( group, 2, SWT.NONE );

    group.dispose();

    assertTrue( columns[ 0 ].isDisposed() );
    assertTrue( columns[ 1 ].isDisposed() );
  }

  public void testGetExpanded_Initial() {
    assertTrue( group.getExpanded() );
  }

  public void testSetExpanded() {
    group.setExpanded( false );

    assertFalse( group.getExpanded() );
  }

  public void testAddRemoveTreeListener() {
    TreeListener listener = new TreeAdapter() {};
    group.addTreeListener( listener );

    assertTrue( group.isListening( SWT.Expand ) );
    assertTrue( group.isListening( SWT.Collapse ) );

    group.removeTreeListener( listener );
    assertFalse( group.isListening( SWT.Expand ) );
    assertFalse( group.isListening( SWT.Collapse ) );
  }

  public void testGetHeaderText_Initial() {
    assertEquals( "", group.getText() );
  }

  public void testGetHeaderText() {
    group.setText( "foo" );

    assertEquals( "foo", group.getText() );
  }

  public void testSetHeaderText_NullArgument() {
    try {
      group.setText( null );
      fail();
    } catch( IllegalArgumentException expected ) {
    }
  }

  public void testGetHeaderImage_Initial() {
    assertNull( group.getImage() );
  }

  public void testGetHeaderImage() {
    Image image = loadImage( display, Fixture.IMAGE1 );

    group.setImage( image );

    assertSame( image, group.getImage() );
  }

  public void testSetHeaderImage_DisposedImage() {
    Image image = loadImage( display, Fixture.IMAGE1 );
    image.dispose();

    try {
      group.setImage( image );
      fail();
    } catch( IllegalArgumentException expected ) {
    }
  }

  public void testGetHeaderFont_Initial() {
    assertSame( grid.getFont(), group.getHeaderFont() );
  }

  public void testGetHeaderFont() {
    Font font = new Font( display, "Arial", 20, SWT.BOLD );

    group.setHeaderFont( font );

    assertSame( font, group.getHeaderFont() );
  }

  public void testSetHeaderFont_DisposedFont() {
    Font font = new Font( display, "Arial", 20, SWT.BOLD );
    font.dispose();

    try {
      group.setHeaderFont( font );
      fail();
    } catch( IllegalArgumentException expected ) {
    }
  }

}
