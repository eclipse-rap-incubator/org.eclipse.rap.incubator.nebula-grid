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
package org.eclipse.rap.nebula.jface.gridviewer.snippets;

import java.util.ArrayList;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ColumnViewerEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerColumn;
import org.eclipse.nebula.jface.gridviewer.GridTreeViewer;
import org.eclipse.nebula.jface.gridviewer.GridViewerColumn;
import org.eclipse.nebula.jface.gridviewer.GridViewerEditor;
import org.eclipse.rap.nebula.widgets.grid.snippets.internal.GridSnippetBase;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

@SuppressWarnings("serial")
public class GridViewerSnippet extends GridSnippetBase {

  private static int COLUMN_COUNT = 3;

  private GridTreeViewer viewer;

  @Override
  protected void createContents( Composite parent ) {
    parent.setLayout( new FillLayout() );
    createGridViewer( parent );
    createGridViewerEditor();
  }

  @Override
  protected boolean isLoggingEnabled() {
    return false;
  }

  private void createGridViewer( Composite parent ) {
    viewer = new GridTreeViewer( parent, SWT.BORDER | SWT.FULL_SELECTION );
    viewer.getGrid().setHeaderVisible( true );
    createGridColumns();
    viewer.setContentProvider( new ContentProvider() );
    viewer.setInput( createModel() );
  }

  private void createGridColumns() {
    final TextCellEditor textCellEditor = new TextCellEditor( viewer.getGrid() );
    for( int i = 0; i < COLUMN_COUNT; i++ ) {
      final int index = i;
      GridViewerColumn column = new GridViewerColumn( viewer, SWT.NONE );
      column.getColumn().setWidth( 260 );
      column.getColumn().setText( "Column " + index );
      column.setLabelProvider( new ColumnLabelProvider() {
        @Override
        protected void initialize(ColumnViewer viewer, ViewerColumn column) {
          // CellToolTipProvider is not compatible with Grid viewers
        }
        @Override
        public String getText( Object element ) {
          return "Column " + index + " => " + element.toString();
        }
      } );
      column.setEditingSupport( new EditingSupport( viewer ) {

        @Override
        protected boolean canEdit( Object element ) {
          return true;
        }

        @Override
        protected CellEditor getCellEditor( Object element ) {
          return textCellEditor;
        }

        @Override
        protected Object getValue( Object element ) {
          return ( ( Model )element ).counter + "";
        }

        @Override
        protected void setValue( Object element, Object value ) {
          try {
            ( ( Model )element ).counter = Integer.parseInt( value.toString() );
            viewer.update( element, null );
          } catch( NumberFormatException exception ) {
            // ignore if not a number
          }
        }
      } );
    }
  }

  private void createGridViewerEditor() {
    ColumnViewerEditorActivationStrategy strategy
      = new ColumnViewerEditorActivationStrategy( viewer ) {
        @Override
        protected boolean isEditorActivationEvent( ColumnViewerEditorActivationEvent event ) {
          return    event.eventType == ColumnViewerEditorActivationEvent.TRAVERSAL
                 || event.eventType == ColumnViewerEditorActivationEvent.MOUSE_DOUBLE_CLICK_SELECTION
                 || ( event.eventType == ColumnViewerEditorActivationEvent.KEY_PRESSED && event.keyCode == SWT.CR )
                 || event.eventType == ColumnViewerEditorActivationEvent.PROGRAMMATIC;
        }
    };
    int feature = ColumnViewerEditor.TABBING_HORIZONTAL | ColumnViewerEditor.KEYBOARD_ACTIVATION;
    GridViewerEditor.create( viewer, strategy, feature );
  }

  private Model createModel() {
    Model root = new Model( 0, null );
    root.counter = 0;
    for( int i = 1; i < 10; i++ ) {
      Model item = new Model( i, root );
      root.child.add( item );
      for( int j = 1; j < i; j++ ) {
        Model subItem = new Model( j, item );
        subItem.child.add( new Model( j * 100, subItem ) );
        item.child.add( subItem );
      }
    }
    return root;
  }

  private class ContentProvider implements ITreeContentProvider {

    public Object[] getElements( Object inputElement ) {
      return ( ( Model )inputElement ).child.toArray();
    }

    public void dispose() {
    }

    public void inputChanged( Viewer viewer, Object oldInput, Object newInput ) {
    }

    public Object[] getChildren( Object parentElement ) {
      return getElements( parentElement );
    }

    public Object getParent( Object element ) {
      return element == null ? null : ( ( Model )element ).parent;
    }

    public boolean hasChildren( Object element ) {
      return ( ( Model )element ).child.size() > 0;
    }
  }

  private class Model {

    public Model parent;
    public ArrayList<Model> child = new ArrayList<Model>();
    public int counter;

    public Model( int counter, Model parent ) {
      this.parent = parent;
      this.counter = counter;
    }

    @Override
    public String toString() {
      String result = "Item ";
      if( parent != null ) {
        result = parent.toString() + ".";
      }
      result += counter;
      return result;
    }
  }
}
