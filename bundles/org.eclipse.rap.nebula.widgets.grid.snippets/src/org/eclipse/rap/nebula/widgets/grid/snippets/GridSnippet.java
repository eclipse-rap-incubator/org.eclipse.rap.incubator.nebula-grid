package org.eclipse.rap.nebula.widgets.grid.snippets;

import java.util.Arrays;
import org.eclipse.nebula.widgets.grid.Grid;
import org.eclipse.nebula.widgets.grid.GridColumn;
import org.eclipse.nebula.widgets.grid.GridItem;
import org.eclipse.rap.nebula.widgets.grid.snippets.internal.GridSnippetBase;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TreeEvent;
import org.eclipse.swt.events.TreeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

@SuppressWarnings("serial")
public class GridSnippet extends GridSnippetBase {

  private static int COLUMN_COUNT = 3;
  private static int ROOT_ITEM_COUNT = 20;
  private static int SUB_ITEM_COUNT = 20;

  private Image image;
  private Grid grid;

  @Override
  protected void createContents( Composite parent ) {
    parent.setLayout( new GridLayout( 2, false ) );
    image = loadImage( parent.getDisplay(), "icons/shell.gif" );
    createGrid( parent );
    createAddRemoveItemButton( parent );
    createTopIndexButton( parent );
  }

  @Override
  protected boolean isLoggingEnabled() {
    return false;
  }

  private void createGrid( Composite parent ) {
    int style = SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.CHECK | SWT.MULTI;
    grid = new Grid( parent, style );
    grid.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true, 1, 20 ) );
    grid.setHeaderVisible( true );
    addGridListeners();
    createGridColumns();
    createGridItems();
  }

  private void addGridListeners() {
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

  private void createGridColumns() {
    for( int i = 0; i < COLUMN_COUNT; i++ ) {
      GridColumn column = new GridColumn( grid, SWT.NONE );
      column.setText( "Column " + i );
      column.setWidth( 250 );
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
    }
  }

  private void createGridItems() {
    for( int i = 0; i < ROOT_ITEM_COUNT; i++ ) {
      GridItem item = new GridItem( grid, SWT.NONE );
      item.setImage( image );
      int gridItemIndex = grid.indexOf( item );
      for( int k = 0; k < COLUMN_COUNT; k++ ) {
        item.setText( k, "Item " + i + "." + k + " (" + gridItemIndex + ")" );
      }
      for( int j = 0; j < SUB_ITEM_COUNT; j++ ) {
        GridItem subitem = new GridItem( item, SWT.NONE );
        gridItemIndex = grid.indexOf( subitem );
        subitem.setImage( 1, image );
        for( int k = 0; k < COLUMN_COUNT; k++ ) {
          subitem.setText( k, "Subitem " + i + "." + j + "." + k + " (" + gridItemIndex + ")" );
        }
      }
    }
  }

  private void createAddRemoveItemButton( Composite parent ) {
    Composite composite = new Composite( parent, SWT.NONE );
    composite.setLayout( new GridLayout( 2, false ) );
    Button addButton = new Button( composite, SWT.PUSH );
    addButton.setText( "Add item" );
    addButton.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent event ) {
        GridItem selectedItem = grid.getSelectionCount() > 0 ? grid.getSelection()[ 0 ] : null;
        if( selectedItem == null ) {
          GridItem item = new GridItem( grid, SWT.NONE );
          item.setImage( image );
          int gridItemIndex = grid.indexOf( item );
          int itemIndex = getItemIndex( item );
          for( int k = 0; k < COLUMN_COUNT; k++ ) {
            item.setText( k, "Item " + itemIndex + "." + k + " (" + gridItemIndex + ")" );
          }
        } else {
          GridItem item = new GridItem( selectedItem, SWT.NONE );
          item.setImage( 1, image );
          int gridItemIndex = grid.indexOf( item );
          int itemIndex = getItemIndex( item );
          int parentItemIndex = getItemIndex( selectedItem );
          for( int k = 0; k < COLUMN_COUNT; k++ ) {
            String text = "Subitem " + parentItemIndex + "." + itemIndex + "." + k
                        + " (" + gridItemIndex + ")";
            item.setText( k, text );
          }
        }
      }
    } );
    Button removeButton = new Button( composite, SWT.PUSH );
    removeButton.setText( "Remove item" );
    removeButton.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent event ) {
        GridItem selectedItem = grid.getSelectionCount() > 0 ? grid.getSelection()[ 0 ] : null;
        if( selectedItem != null ) {
          selectedItem.dispose();
        }
      }
    } );
  }

  private void createTopIndexButton( Composite parent ) {
    Composite composite = new Composite( parent, SWT.NONE );
    composite.setLayout( new GridLayout( 3, false ) );
    Label topItemLabel = new Label( composite, SWT.NONE );
    topItemLabel.setText( "Top index" );
    final Text topItemText = new Text( composite, SWT.BORDER );
    topItemText.setLayoutData( new GridData( 100, SWT.DEFAULT ) );
    Button button = new Button( composite, SWT.PUSH );
    button.setText( "Change" );
    button.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent event ) {
        int topIndex = -1;
        try {
          topIndex = Integer.parseInt( topItemText.getText() );
        } catch( NumberFormatException e ) {
        }
        grid.setTopIndex( topIndex );
      }
    } );
  }

  private int getItemIndex( GridItem item ) {
    int result = -1;
    GridItem parentItem = item.getParentItem();
    if( parentItem == null ) {
      result = Arrays.asList( grid.getRootItems() ).indexOf( item );
    } else {
      result = parentItem.indexOf( item );
    }
    return result;
  }
}
