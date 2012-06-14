package org.eclipse.rap.nebula.widgets.grid.snippets;

import java.util.Arrays;

import org.eclipse.nebula.widgets.grid.Grid;
import org.eclipse.nebula.widgets.grid.GridColumn;
import org.eclipse.nebula.widgets.grid.GridItem;
import org.eclipse.rap.nebula.widgets.grid.snippets.internal.GridSnippetBase;
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

public class GridSnippet extends GridSnippetBase {

  private static int COLUMN_COUNT = 3;
  private static int ROOT_ITEM_COUNT = 20;
  private static int SUB_ITEM_COUNT = 20;

  @Override
  @SuppressWarnings("serial")
  protected void createContents( Composite parent ) {
    parent.setLayout( new GridLayout() );
    Image image = loadImage( parent.getDisplay(), "icons/shell.gif" );
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
          log( "column controlResized: " + event );
        }

        public void controlMoved( ControlEvent event ) {
          log( "column controlMoved: " + event );
        }
      } );
      column.addSelectionListener( new SelectionListener() {

        public void widgetSelected( SelectionEvent event ) {
          log( "column widgetSelected: " + event );
        }

        public void widgetDefaultSelected( SelectionEvent event ) {
          log( "column widgetDefaultSelected: " + event );
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
        log( "grid treeExpanded: " + event );
      }

      public void treeCollapsed( TreeEvent event ) {
        log( "grid treeExpanded: " + event );
      }
    } );
    grid.addSelectionListener( new SelectionListener() {

      public void widgetSelected( SelectionEvent event ) {
        log( "grid widgetSelected: " + event );
        log( "selection: " + Arrays.toString( grid.getSelection() ) );
      }

      public void widgetDefaultSelected( SelectionEvent event ) {
        log( "grid widgetDefaultSelected: " + event );
        log( "selection: " + Arrays.toString( grid.getSelection() ) );
      }
    } );
  }

  @Override
  protected boolean isLoggingEnabled() {
    return false;
  }

}
