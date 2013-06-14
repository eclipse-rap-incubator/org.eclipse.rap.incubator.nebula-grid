/*******************************************************************************
 * Copyright (c) 2013 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.rap.nebula.widget.grid.demo.examples;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.nebula.widgets.grid.Grid;
import org.eclipse.nebula.widgets.grid.GridColumn;
import org.eclipse.nebula.widgets.grid.GridColumnGroup;
import org.eclipse.nebula.widgets.grid.GridItem;
import org.eclipse.rap.examples.ExampleUtil;
import org.eclipse.rap.examples.IExamplePage;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;


public class NebulaGridExamplePage implements IExamplePage {

  private static final String EURO = "€ ";
  private static final String[] MONTHS = new String[] {
    "January", "February", "March", "Apil", "May", "June"
  };

  private Image euro;
  private List<Expenses> expenses;

  public void createControl( Composite parent ) {
    initData();
    parent.setLayout( ExampleUtil.createGridLayout( 1, true, true, true ) );
    parent.setLayoutData( ExampleUtil.createFillData() );
    Grid grid = createGrid( parent );
    GridColumn category = createColumn( grid, null, "Company Expenses", 215, SWT.CENTER );
    category.setFooterText( "Grand Total" );
    GridColumnGroup group = createGridColumnGroup( grid, "Period ( six monts )", SWT.CENTER );
    for( int i = 0; i < MONTHS.length; i++ ) {
      createMonthColumn( grid, group, MONTHS[ i ] );
    }
    createTotalColumn( grid, group );
    createColumn( grid, null, "", 55, SWT.CENTER | SWT.CHECK );
    createItems( grid );
    calcTotals( grid );
  }

  private void initData() {
    expenses = new ArrayList<Expenses>();
    expenses.add( new Expenses( "Business Property<br/>Rent/Lease", 450, 85, 1200, 140, 450, 235 ) );
    expenses.add( new Expenses( "Salaries and Wages of<br/>Employees", 12970, 14075, 13560, 15980, 15780, 14120 ) );
    expenses.add( new Expenses( "Employee Benefits", 1200, 700, 880, 3200, 2430, 1750 ) );
    expenses.add( new Expenses( "Equipment Lease<br/>Payments", 0, 0, 0, 0, 0, 0 ) );
    expenses.add( new Expenses( "Secured Debt Payments", 0, 0, 0, 0, 0, 0 ) );
    expenses.add( new Expenses( "Supplies", 0, 0, 0, 0, 0, 0 ) );
    expenses.add( new Expenses( "Utilities", 0, 0, 0, 0, 0, 0 ) );
    expenses.add( new Expenses( "Telephone", 420, 340, 200, 235, 210, 180 ) );
    expenses.add( new Expenses( "Repairs and Maintenance", 0, 0, 0, 0, 0, 0 ) );
    expenses.add( new Expenses( "Miscellaneous Office<br/>Expense", 0, 0, 0, 0, 0, 0 ) );
    expenses.add( new Expenses( "Advertising", 0, 0, 0, 0, 0, 0 ) );
    expenses.add( new Expenses( "Travel and Entertainment", 0, 0, 0, 0, 0, 0 ) );
    expenses.add( new Expenses( "Professional Fees", 0, 0, 0, 0, 0, 0 ) );
  }

  private Grid createGrid( Composite parent ) {
    Grid grid = new Grid( parent, SWT.V_SCROLL | SWT.BORDER );
    grid.setData( RWT.MARKUP_ENABLED, Boolean.TRUE );
    GridData tableLayoutData = ExampleUtil.createFillData();
    tableLayoutData.verticalIndent = 10;
    grid.setLayoutData( tableLayoutData );
    grid.setHeaderVisible( true );
    grid.setFooterVisible( true );
    grid.setLinesVisible( true );
    grid.addListener( SWT.Selection, new Listener() {
      public void handleEvent( Event event ) {
        calcTotals( ( Grid )event.widget );
      }
    } );
    return grid;
  }

  private GridColumnGroup createGridColumnGroup( Grid grid, String name, int style ) {
    GridColumnGroup group = new GridColumnGroup( grid, style );
    group.setHeaderFont( new Font( group.getDisplay(), "Verdana", 14, SWT.BOLD ) );
    group.setText( name );
    return group;
  }

  private void createMonthColumn( Grid grid, GridColumnGroup group, String text ) {
    GridColumn column = createColumn( grid, group, text, 120, SWT.CENTER );
    column.setSummary( false );
    column.setFooterImage( euro );
    column.setData( "month" );
  }

  private void createTotalColumn( Grid grid, GridColumnGroup columnGroup ) {
    GridColumn totalColumn = createColumn( grid, columnGroup, "Total", 720, SWT.CENTER );
    totalColumn.setDetail( false );
    totalColumn.setData( "total" );
  }

  private static GridColumn createColumn( Grid grid,
                                          GridColumnGroup group,
                                          String name,
                                          int width,
                                          int style )
  {
    GridColumn column;
    if( group == null ) {
      column = new GridColumn( grid, style );
    } else {
      column = new GridColumn( group, style );
    }
    Font font = new Font( column.getDisplay(), "Verdana", 12, SWT.BOLD );
    column.setHeaderFont( font );
    column.setFooterFont( font );
    column.setText( name );
    column.setWidth( width );
    return column;
  }

  private void createItems( Grid grid ) {
    for( int i = 0; i < expenses.size(); i++ ) {
      Expenses current = expenses.get( i );
      GridItem item = new GridItem( grid, SWT.NONE );
      if( current.name.indexOf( "<br/>" ) != -1 ) {
        item.setHeight( 2 * grid.getItemHeight() );
      }
      item.setText( current.name );
      for( int j = 0; j < current.amount.length; j++ ) {
        String text = EURO + String.valueOf( current.amount[ j ] );
        item.setText( j + 1, text );
      }
      item.setChecked( 8, true );
    }
  }

  private void calcTotals( Grid grid ) {
    for( int i = 0; i < MONTHS.length + 1; i++ ) {
      int total = 0;
      for( int j = 0; j < expenses.size(); j++ ) {
        Expenses current = expenses.get( j );
        if( grid.getItem( j ).getChecked( 8 ) ) {
          total += current.amount[ i ];
        }
      }
      String text = EURO + String.valueOf( total );
      grid.getColumn( i + 1 ).setFooterText( text );
    }
  }

  private class Expenses {

    public final String name;
    public final int[] amount = new int[ MONTHS.length + 1 ];

    public Expenses( String name, int... amount ) {
      this.name = name;
      int sum = 0;
      for( int i = 0; i < this.amount.length - 1; i++ ) {
        this.amount[ i ] = amount[ i ];
        sum += amount[ i ];
      }
      this.amount[ this.amount.length - 1 ] = sum;
    }

  }

}
