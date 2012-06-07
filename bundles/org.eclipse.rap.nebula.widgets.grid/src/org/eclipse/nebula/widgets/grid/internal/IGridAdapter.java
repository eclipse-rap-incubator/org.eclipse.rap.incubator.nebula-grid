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
package org.eclipse.nebula.widgets.grid.internal;


public interface IGridAdapter {

  void invalidateTopIndex();
  int getIndentationWidth();
  int getCheckLeft();
  int getCheckWidth();

  int getCellLeft( int index );
  int getCellWidth( int index );
  int getImageOffset( int index );
  int getImageWidth( int index );
  int getTextOffset( int index );
  int getTextWidth( int index );

}
