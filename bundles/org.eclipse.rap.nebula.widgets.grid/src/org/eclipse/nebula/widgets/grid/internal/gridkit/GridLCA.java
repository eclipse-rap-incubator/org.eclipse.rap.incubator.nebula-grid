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

import org.eclipse.nebula.widgets.grid.Grid;
import org.eclipse.nebula.widgets.grid.internal.IGridAdapter;
import org.eclipse.rwt.internal.protocol.ClientObjectFactory;
import org.eclipse.rwt.internal.protocol.IClientObject;
import org.eclipse.rwt.lifecycle.AbstractWidgetLCA;
import org.eclipse.rwt.lifecycle.WidgetLCAUtil;
import org.eclipse.rwt.lifecycle.WidgetUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Widget;


@SuppressWarnings("restriction")
public class GridLCA extends AbstractWidgetLCA {

  private static final String TYPE = "rwt.widgets.Tree";
  private static final String[] ALLOWED_STYLES = new String[] {
    "SINGLE",
    "MULTI",
    "FULL_SELECTION",
    "CHECK",
    "VIRTUAL",
    "NO_FOCUS",
    "BORDER"
  };

  @Override
  public void renderInitialization( Widget widget ) throws IOException {
    Grid grid = ( Grid )widget;
    IClientObject clientObject = ClientObjectFactory.getClientObject( grid );
    clientObject.create( TYPE );
    clientObject.set( "parent", WidgetUtil.getId( grid.getParent() ) );
    clientObject.set( "style", WidgetLCAUtil.getStyles( grid, ALLOWED_STYLES ) );
    clientObject.set( "appearance", "tree" );
    IGridAdapter adapter = getGridAdapter( grid );
    clientObject.set( "indentionWidth", adapter.getIndentationWidth() );
    if( ( grid.getStyle() & SWT.CHECK ) != 0 ) {
      int[] checkMetrics = new int[] { adapter.getCheckLeft(), adapter.getCheckWidth() };
      clientObject.set( "checkBoxMetrics", checkMetrics );
    }
  }

  public void readData( Widget widget ) {
  }

  @Override
  public void preserveValues( Widget widget ) {
  }

  @Override
  public void renderChanges( Widget widget ) throws IOException {
  }

  @Override
  public void renderDispose( Widget widget ) throws IOException {
    ClientObjectFactory.getClientObject( widget ).destroy();
  }

  //////////////////
  // Helping methods

  private static IGridAdapter getGridAdapter( Grid grid ) {
    return grid.getAdapter( IGridAdapter.class );
  }

}
