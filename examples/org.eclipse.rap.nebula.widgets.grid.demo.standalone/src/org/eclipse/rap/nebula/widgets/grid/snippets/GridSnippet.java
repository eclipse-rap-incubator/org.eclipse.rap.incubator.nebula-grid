/*******************************************************************************
 * Copyright (c) 2012, 2014 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.rap.nebula.widgets.grid.snippets;

import java.util.Arrays;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.nebula.widgets.grid.Grid;
import org.eclipse.nebula.widgets.grid.GridColumn;
import org.eclipse.nebula.widgets.grid.GridColumnGroup;
import org.eclipse.nebula.widgets.grid.GridItem;
import org.eclipse.rap.nebula.widgets.grid.snippets.internal.GridSnippetBase;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TreeEvent;
import org.eclipse.swt.events.TreeListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

@SuppressWarnings("serial")
public class GridSnippet extends GridSnippetBase {

  private static int COLUMN_COUNT = 5;
  private static int ROOT_ITEM_COUNT = 20;
  private static int SUB_ITEM_COUNT = 10;

  private Grid grid;
  private Image image;

  @Override
  protected void createContents( Composite parent ) {
    parent.setLayout( new GridLayout( 2, false ) );
    image = loadImage( parent.getDisplay(), "icons/shell.gif" );
    createGrid( parent );
    createAddRemoveItemButton( parent );
    createTopIndexButton( parent );
    createShowItemGroup( parent );
    createShowColumnGroup( parent );
    createShowHeaderButton( parent );
    createShowFooterButton( parent );
    createWordWrapButton( parent );
    createHeaderWordWrapButton( parent );
    createQueryFocusItem( parent );
  }

  @Override
  protected boolean isLoggingEnabled() {
    return false;
  }

  private void createGrid( Composite parent ) {
    int style = SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.MULTI | SWT.CHECK;
    grid = new Grid( parent, style );
    grid.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true, 1, 20 ) );
    grid.setHeaderVisible( true );
    grid.setFooterVisible( true );
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
    grid.getHorizontalBar().addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent event ) {
        log( "horizontal croll widgetSelected: " + event );
        log( "selection: " + grid.getHorizontalBar().getSelection() );
      }
    } );
    grid.getVerticalBar().addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent event ) {
        log( "vertical scroll widgetSelected: " + event );
        log( "selection: " + grid.getVerticalBar().getSelection() );
      }
    } );
  }

  private void createGridColumns() {
    GridColumnGroup group = new GridColumnGroup( grid, SWT.TOGGLE );
    group.setText( "Column Group" );
    group.setImage( image );
    group.setHeaderFont( new Font( group.getDisplay(), "Verdana", 16, SWT.BOLD ) );
    for( int i = 0; i < COLUMN_COUNT; i++ ) {
      GridColumn column;
      if( i > 0 && i < 4 ) {
        column = new GridColumn( group, i == 1 ? SWT.CHECK : SWT.NONE );
      } else {
        column = new GridColumn( grid, i == 4 ? SWT.CHECK : SWT.NONE );
      }
      column.setData( RWT.TOOLTIP_MARKUP_ENABLED, Boolean.TRUE );
      column.setText( "Column " + i );
      column.setHeaderTooltip( "<span style='font-weight:bold'><i>Column " + i + "</i></span>" );
      column.setFooterText( "Footer " + i );
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
          column.setFooterImage( image );
          break;
        case 1:
          column.setWidth( 100 );
          column.setAlignment( SWT.CENTER );
          column.setImage( image );
          column.setHeaderFont( new Font( column.getDisplay(), "Comic Sans MS", 16, SWT.NORMAL ) );
          column.setFooterFont( new Font( column.getDisplay(), "Segoe Script", 16, SWT.NORMAL ) );
          column.setSummary( false );
          break;
        case 2:
          column.setMinimumWidth( 100 );
          column.setSummary( false );
          break;
        case 3:
          column.setAlignment( SWT.RIGHT );
          column.setDetail( false );
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
        if( k != 1 ) {
          item.setText( k, "Item (" + gridItemIndex + "." + k + ")" );
        }
      }
      if( gridItemIndex % 5 == 0 ) {
        grid.getItem( gridItemIndex ).setHeight( 2 * grid.getItemHeight() );
      }
      for( int j = 0; j < SUB_ITEM_COUNT; j++ ) {
        GridItem subitem = new GridItem( item, SWT.NONE );
        gridItemIndex = grid.indexOf( subitem );
        subitem.setImage( 2, image );
        for( int k = 0; k < COLUMN_COUNT; k++ ) {
          if( k != 1 ) {
            subitem.setText( k, "Subitem (" + gridItemIndex + "." + k + ")" );
          }
        }
        if( gridItemIndex % 5 == 0 ) {
          grid.getItem( gridItemIndex ).setHeight( 2 * grid.getItemHeight() );
        }
      }
    }
  }

  private void createAddRemoveItemButton( Composite parent ) {
    Composite composite = new Composite( parent, SWT.NONE );
    composite.setLayout( new GridLayout( 2, false ) );
    Button addButton = new Button( composite, SWT.PUSH );
    addButton.setLayoutData( new GridData( 100, SWT.DEFAULT ) );
    addButton.setText( "Add item" );
    addButton.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent event ) {
        GridItem selectedItem = grid.getSelectionCount() > 0 ? grid.getSelection()[ 0 ] : null;
        GridItem item;
        int gridItemIndex;
        if( selectedItem == null ) {
          item = new GridItem( grid, SWT.NONE );
          item.setImage( image );
          gridItemIndex = grid.indexOf( item );
          for( int k = 0; k < COLUMN_COUNT; k++ ) {
            item.setText( k, "Item (" + gridItemIndex + "." + k + ")" );
          }
        } else {
          item = new GridItem( selectedItem, SWT.NONE );
          item.setImage( 1, image );
          gridItemIndex = grid.indexOf( item );
          for( int k = 0; k < COLUMN_COUNT; k++ ) {
            item.setText( k, "Subitem (" + gridItemIndex + "." + k + ")" );
          }
        }
        updateItemsText( gridItemIndex + 1 );
      }
    } );
    Button removeButton = new Button( composite, SWT.PUSH );
    removeButton.setLayoutData( new GridData( 100, SWT.DEFAULT ) );
    removeButton.setText( "Remove item" );
    removeButton.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent event ) {
        GridItem selectedItem = grid.getSelectionCount() > 0 ? grid.getSelection()[ 0 ] : null;
        if( selectedItem != null ) {
          int selectedItemIndex = grid.indexOf( selectedItem );
          grid.remove( selectedItemIndex );
          updateItemsText( selectedItemIndex );
        }
      }
    } );
  }

  private void createTopIndexButton( Composite parent ) {
    Composite composite = new Composite( parent, SWT.NONE );
    composite.setLayout( new GridLayout( 3, false ) );
    Label topIndexLabel = new Label( composite, SWT.NONE );
    topIndexLabel.setLayoutData( new GridData( 100, SWT.DEFAULT ) );
    topIndexLabel.setText( "Top index" );
    final Text topIndexText = new Text( composite, SWT.BORDER );
    topIndexText.setLayoutData( new GridData( 50, SWT.DEFAULT ) );
    Button button = new Button( composite, SWT.PUSH );
    button.setLayoutData( new GridData( 100, SWT.DEFAULT ) );
    button.setText( "Change" );
    button.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent event ) {
        int topIndex = -1;
        try {
          topIndex = Integer.parseInt( topIndexText.getText() );
        } catch( NumberFormatException e ) {
        }
        grid.setTopIndex( topIndex );
      }
    } );
  }

  private void createShowItemGroup( Composite parent ) {
    Composite composite = new Composite( parent, SWT.NONE );
    composite.setLayout( new GridLayout( 3, false ) );
    Label showItemLabel = new Label( composite, SWT.NONE );
    showItemLabel.setLayoutData( new GridData( 100, SWT.DEFAULT ) );
    showItemLabel.setText( "Show item" );
    final Text showItemText = new Text( composite, SWT.BORDER );
    showItemText.setLayoutData( new GridData( 50, SWT.DEFAULT ) );
    Button button = new Button( composite, SWT.PUSH );
    button.setLayoutData( new GridData( 100, SWT.DEFAULT ) );
    button.setText( "Show" );
    button.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent event ) {
        int index = -1;
        try {
          index = Integer.parseInt( showItemText.getText() );
        } catch( NumberFormatException e ) {
        }
        if( index >= 0 && index < grid.getItemCount() ) {
          grid.showItem( grid.getItem( index ) );
        }
      }
    } );
  }

  private void createShowColumnGroup( Composite parent ) {
    Composite composite = new Composite( parent, SWT.NONE );
    composite.setLayout( new GridLayout( 3, false ) );
    Label showColumnLabel = new Label( composite, SWT.NONE );
    showColumnLabel.setLayoutData( new GridData( 100, SWT.DEFAULT ) );
    showColumnLabel.setText( "Show column" );
    final Text showColumnText = new Text( composite, SWT.BORDER );
    showColumnText.setLayoutData( new GridData( 50, SWT.DEFAULT ) );
    Button button = new Button( composite, SWT.PUSH );
    button.setLayoutData( new GridData( 100, SWT.DEFAULT ) );
    button.setText( "Show" );
    button.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent event ) {
        int index = -1;
        try {
          index = Integer.parseInt( showColumnText.getText() );
        } catch( NumberFormatException e ) {
        }
        if( index >= 0 && index < grid.getColumnCount() ) {
          grid.showColumn( grid.getColumn( index ) );
        }
      }
    } );
  }

  private void createShowHeaderButton( Composite parent ) {
    final Button button = new Button( parent, SWT.CHECK );
    button.setText( "Show header" );
    button.setSelection( true );
    button.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent event ) {
        grid.setHeaderVisible( button.getSelection() );
      }
    } );
  }

  private void createShowFooterButton( Composite parent ) {
    final Button button = new Button( parent, SWT.CHECK );
    button.setText( "Show footer" );
    button.setSelection( true );
    button.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent event ) {
        grid.setFooterVisible( button.getSelection() );
      }
    } );
  }

  private void createWordWrapButton( Composite parent ) {
    final Button button = new Button( parent, SWT.CHECK );
    button.setText( "Word wrap cells text" );
    button.setSelection( false );
    button.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent event ) {
        for( GridColumn column : grid.getColumns() ) {
          column.setWordWrap( button.getSelection() );
        }
      }
    } );
  }

  private void createHeaderWordWrapButton( Composite parent ) {
    final Button button = new Button( parent, SWT.CHECK );
    button.setText( "Word wrap headers text" );
    button.setSelection( false );
    button.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent event ) {
        for( GridColumn column : grid.getColumns() ) {
          column.setHeaderWordWrap( button.getSelection() );
        }
      }
    } );
  }

  private void createQueryFocusItem( Composite parent ) {
    Button button = new Button( parent, SWT.PUSH );
    button.setText( "Query focusItem" );
    button.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent e ) {
        Shell shell = grid.getShell();
        String msg = "Current focusItem: " + grid.getFocusItem();
        MessageDialog.openInformation( shell, "Information", msg );
      }
    } );
  }

  private void updateItemsText( int startIndex ) {
    for( int index = startIndex; index < grid.getItemCount(); index++ ) {
      GridItem item = grid.getItem( index );
      String text = item.getText();
      text = text.substring( 0, text.indexOf( "(" ) + 1 );
      for( int k = 0; k < COLUMN_COUNT; k++ ) {
        item.setText( k, text + grid.indexOf( item ) + "." + k + ")" );
      }
    }
  }

}
