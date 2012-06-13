package org.eclipse.rap.nebula.widgets.grid.snippets;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import org.eclipse.nebula.widgets.grid.Grid;
import org.eclipse.nebula.widgets.grid.GridColumn;
import org.eclipse.nebula.widgets.grid.GridItem;
import org.eclipse.rwt.lifecycle.IEntryPoint;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TreeEvent;
import org.eclipse.swt.events.TreeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class GridSnippet1 implements IEntryPoint {

  private static int COLUMN_COUNT = 3;
  private static int ROOT_ITEM_COUNT = 20;
  private static int SUB_ITEM_COUNT = 20;

  public int createUI() {
    Display display = new Display();
    Shell shell = new Shell( display );
    shell.setText( "Nebula Grid Snippets" );
    createContents( shell );
    shell.setSize( 800, 600 );
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

  @SuppressWarnings("serial")
  private void createContents( Composite parent ) {
    parent.setLayout( new GridLayout() );
    Image image = loadImage( parent.getDisplay(), "icons/sample.gif" );
    final Grid grid = new Grid( parent, SWT.BORDER
                                        | SWT.V_SCROLL
                                        | SWT.H_SCROLL
                                        | SWT.CHECK
                                        | SWT.MULTI );
    grid.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true, 1, 1 ) );
    grid.setHeaderVisible( true );
    for( int i = 0; i < COLUMN_COUNT; i++ ) {
      GridColumn column = new GridColumn( grid, SWT.NONE );
      column.setText( "Column " + i );
      column.setWidth( 200 );
      column.setMoveable( true );
      column.addControlListener( new ControlListener() {

        public void controlResized( ControlEvent event ) {
          System.out.println( "column controlResized: " + event );
        }

        public void controlMoved( ControlEvent event ) {
          System.out.println( "column controlMoved: " + event );
        }
      } );
      column.addSelectionListener( new SelectionListener() {

        public void widgetSelected( SelectionEvent event ) {
          System.out.println( "column widgetSelected: " + event );
        }

        public void widgetDefaultSelected( SelectionEvent event ) {
          System.out.println( "column widgetDefaultSelected: " + event );
        }
      } );
      switch( i ) {
        case 0:
          column.setImage( image );
          column.setSort( SWT.DOWN );
        break;
        case 1:
          column.setAlignment( SWT.CENTER );
          column.setImage( image );
        break;
        case 2:
          column.setAlignment( SWT.RIGHT );
          column.setMinimumWidth( 100 );
        break;
      }

      if( i == 0 ) {
      } else if( i == 1 ) {
      }
    }
    for( int i = 0; i < ROOT_ITEM_COUNT; i++ ) {
      GridItem item = new GridItem( grid, SWT.NONE );
      item.setImage( image );
      for( int k = 0; k < COLUMN_COUNT; k++ ) {
        item.setText( k, "Item " + i + "." + k );
      }
      for( int j = 0; j < SUB_ITEM_COUNT; j++ ) {
        GridItem subitem = new GridItem( item, SWT.NONE );
        subitem.setImage( 1, image );
        for( int k = 0; k < COLUMN_COUNT; k++ ) {
          subitem.setText( k, "Subitem " + i + "." + j + "." + k );
        }
      }
    }
    grid.addTreeListener( new TreeListener() {

      public void treeExpanded( TreeEvent event ) {
        System.out.println( "grid treeExpanded: " + event );
      }

      public void treeCollapsed( TreeEvent event ) {
        System.out.println( "grid treeExpanded: " + event );
      }
    } );
    grid.addSelectionListener( new SelectionListener() {

      public void widgetSelected( SelectionEvent event ) {
        System.out.println( "grid widgetSelected: " + event );
        System.out.println( "selection: " + Arrays.toString( grid.getSelection() ) );
      }

      public void widgetDefaultSelected( SelectionEvent event ) {
        System.out.println( "grid widgetDefaultSelected: " + event );
        System.out.println( "selection: " + Arrays.toString( grid.getSelection() ) );
      }
    } );
  }

  private Image loadImage( Display display, String name ) {
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

}
