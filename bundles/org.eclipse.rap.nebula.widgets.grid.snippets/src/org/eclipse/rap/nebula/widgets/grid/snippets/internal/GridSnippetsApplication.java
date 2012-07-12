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
package org.eclipse.rap.nebula.widgets.grid.snippets.internal;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.rap.nebula.jface.gridviewer.snippets.GridViewerSnippet;
import org.eclipse.rap.nebula.widgets.grid.snippets.GridSnippet;
import org.eclipse.rwt.application.Application;
import org.eclipse.rwt.application.Application.OperationMode;
import org.eclipse.rwt.application.ApplicationConfiguration;
import org.eclipse.rwt.client.WebClient;


public class GridSnippetsApplication implements ApplicationConfiguration {

  public void configure( Application application ) {
    Map<String, String> properties = new HashMap<String, String>();
    properties.put( WebClient.PAGE_TITLE, "Nebula Grid Snippets" );
    application.addEntryPoint( "/grid-widget", GridSnippet.class, properties );
    application.addEntryPoint( "/grid-viewer", GridViewerSnippet.class, properties );
    application.setOperationMode( OperationMode.SWT_COMPATIBILITY );
  }

}
