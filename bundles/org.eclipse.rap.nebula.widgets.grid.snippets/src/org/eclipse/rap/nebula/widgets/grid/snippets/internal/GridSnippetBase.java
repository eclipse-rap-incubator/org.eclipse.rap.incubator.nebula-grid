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

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.rap.rwt.application.EntryPoint;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;


public abstract class GridSnippetBase implements EntryPoint {

  public int createUI() {
    Display display = new Display();
    Shell shell = new Shell( display );
    shell.setText( "Nebula Grid Snippets" );
    createContents( shell );
    shell.setSize( 1024, 768 );
    shell.setLocation( 20, 20 );
    shell.open();
    while( !shell.isDisposed() ) {
      if( !display.readAndDispatch() ) {
        display.sleep();
      }
    }
    display.dispose();
    return 0;
  }

  protected abstract void createContents( Composite parent );

  protected Image loadImage( Display display, String name ) {
    Image result = null;
    InputStream stream = getClass().getClassLoader().getResourceAsStream( name );
    if( stream != null ) {
      try {
        result = new Image( display, stream );
      } finally {
        try {
          stream.close();
        } catch( IOException unexpected ) {
          throw new RuntimeException( "Failed to close image input stream", unexpected );
        }
      }
    }
    return result;
  }

  protected abstract boolean isLoggingEnabled();

  protected void log( String message ) {
    if( isLoggingEnabled() ) {
      System.out.println( message );
    }
  }

}
