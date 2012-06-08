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

import java.io.IOException;

import org.eclipse.nebula.widgets.grid.GridColumn;
import org.eclipse.rwt.internal.protocol.ClientObjectFactory;
import org.eclipse.rwt.internal.protocol.IClientObject;
import org.eclipse.rwt.lifecycle.AbstractWidgetLCA;
import org.eclipse.rwt.lifecycle.WidgetUtil;
import org.eclipse.swt.widgets.Widget;


@SuppressWarnings("restriction")
public class GridColumnLCA extends AbstractWidgetLCA {

  private static final String TYPE = "rwt.widgets.TableColumn";

  @Override
  public void renderInitialization( Widget widget ) throws IOException {
    GridColumn column = ( GridColumn )widget;
    IClientObject clientObject = ClientObjectFactory.getClientObject( column );
    clientObject.create( TYPE );
    clientObject.set( "parent", WidgetUtil.getId( column.getParent() ) );
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
}
