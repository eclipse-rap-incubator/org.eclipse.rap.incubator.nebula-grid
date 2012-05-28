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

import org.eclipse.swt.graphics.*;
import org.eclipse.swt.internal.widgets.controlkit.ControlThemeAdapter;
import org.eclipse.swt.widgets.Control;


@SuppressWarnings("restriction")
public final class GridThemeAdapter extends ControlThemeAdapter {

  public Rectangle getCheckBoxMargin( Control control ) {
    return getCssBoxDimensions( "Tree-Checkbox", "margin", control );
  }

  public Point getCheckBoxImageSize( Control control ) {
    return getCssImageDimension( "Tree-Checkbox", "background-image", control );
  }

  public Rectangle getCellPadding( Control control ) {
    return getCssBoxDimensions( "Tree-Cell", "padding", control );
  }

}
