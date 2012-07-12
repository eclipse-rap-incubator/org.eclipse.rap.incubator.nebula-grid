/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    rmcamara@us.ibm.com - initial API and implementation
 *    Tom Schindl <tom.schindl@bestsolution.at> - various significant contributions
 *    											  bug fix in: 191216
 *******************************************************************************/

package org.eclipse.nebula.jface.gridviewer;

import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.ViewerRow;
import org.eclipse.nebula.widgets.grid.GridItem;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Widget;

/**
 * GridViewerRow is the concrete implementation of the part that represents items in a
 * Grid.
 */
public class GridViewerRow extends ViewerRow
{
    private GridItem item;

    /**
     * Create a new instance of the receiver.
     *
     * @param item GridItem source.
     */
    GridViewerRow(GridItem item)
    {
        this.item = item;
    }

    /** {@inheritDoc} */
    @Override
    public Rectangle getBounds(int columnIndex)
    {
    	if( columnIndex == Integer.MAX_VALUE ) {
    		//TODO Provide implementation for GridItem
    		return null;
    	} else {
    		if( ! item.getParent().getColumn(columnIndex).isVisible() ) {
    			return new Rectangle(0,0,0,0);
    		} else {
    			return item.getBounds(columnIndex);
    		}

    	}
    }

    /** {@inheritDoc} */
    @Override
    public Rectangle getBounds()
    {
        // TODO This is not correct. Update once item returns the correct information.
        return item.getBounds(0);
    }


    /** {@inheritDoc} */
    @Override
    public int getColumnCount()
    {
        return item.getParent().getColumnCount();
    }

    /** {@inheritDoc} */
    @Override
    public Color getBackground(int columnIndex)
    {
    	if( columnIndex == Integer.MAX_VALUE ) {
    		//TODO Provide implementation for GridItem
    		return null;
    	} else {
    		return item.getBackground(columnIndex);
    	}
    }

    /** {@inheritDoc} */
    @Override
    public Font getFont(int columnIndex)
    {
    	if( columnIndex == Integer.MAX_VALUE ) {
    		//TODO Provide implementation for GridItem
    		return null;
    	} else {
    		return item.getFont(columnIndex);
    	}
    }

    /** {@inheritDoc} */
    @Override
    public Color getForeground(int columnIndex)
    {
    	if( columnIndex == Integer.MAX_VALUE ) {
    		//TODO Provide implementation for GridItem
    		return null;
    	} else {
    		return item.getForeground(columnIndex);
    	}
    }

    /** {@inheritDoc} */
    @Override
    public Image getImage(int columnIndex)
    {
    	if( columnIndex == Integer.MAX_VALUE ) {
    		//TODO Provide implementation for GridItem
    		return null;
    	} else {
    		return item.getImage(columnIndex);
    	}

    }

    /** {@inheritDoc} */
    @Override
    public String getText(int columnIndex)
    {
    	if( columnIndex == Integer.MAX_VALUE ) {
// RAP [if] Row headers not supported
//    		return item.getHeaderText();
    	  return null;
    	} else {
    		return item.getText(columnIndex);
    	}

    }

    /** {@inheritDoc} */
    @Override
    public void setBackground(int columnIndex, Color color)
    {
    	if( columnIndex == Integer.MAX_VALUE ) {
    		//TODO Provide implementation for GridItem
    	} else {
    		item.setBackground(columnIndex, color);
    	}
    }

    /** {@inheritDoc} */
    @Override
    public void setFont(int columnIndex, Font font)
    {
    	if( columnIndex == Integer.MAX_VALUE ) {
    		//TODO Provide implementation for GridItem
    	} else {
    		item.setFont(columnIndex, font);
    	}
    }

    /** {@inheritDoc} */
    @Override
    public void setForeground(int columnIndex, Color color)
    {
    	if( columnIndex == Integer.MAX_VALUE ) {
    		//TODO Provide implementation for GridItem
    	} else {
    		item.setForeground(columnIndex, color);
    	}
    }

    /** {@inheritDoc} */
    @Override
    public void setImage(int columnIndex, Image image)
    {
    	if( columnIndex == Integer.MAX_VALUE ) {
    		//TODO Provide implementation for GridItem
    	} else {
    		item.setImage(columnIndex, image);
    	}

    }

    /** {@inheritDoc} */
    @Override
    public void setText(int columnIndex, String text)
    {
    	if( columnIndex == Integer.MAX_VALUE ) {
// RAP [if] Row headers not supported
//    		item.setHeaderText(text);
    	} else {
    		item.setText(columnIndex, text == null ? "" : text); //$NON-NLS-1$
    	}
    }

    /** {@inheritDoc} */
    @Override
    public Control getControl()
    {
        return item.getParent();
    }

    @Override
    public ViewerRow getNeighbor(int direction, boolean sameLevel) {
		if( direction == ViewerRow.ABOVE ) {
			return getRowAbove();
		} else if( direction == ViewerRow.BELOW ) {
			return getRowBelow();
		} else {
			throw new IllegalArgumentException("Illegal value of direction argument."); //$NON-NLS-1$
		}
	}


	private ViewerRow getRowAbove() {
		int index = item.getParent().indexOf(item) - 1;

		if( index >= 0 ) {
			return new GridViewerRow(item.getParent().getItem(index));
		}

		return null;
	}

	private ViewerRow getRowBelow() {
		int index = item.getParent().indexOf(item) + 1;

		if( index < item.getParent().getItemCount() ) {
			GridItem tmp = item.getParent().getItem(index);
			if( tmp != null ) {
				return new GridViewerRow(tmp);
			}
		}

		return null;
	}

	@Override
  public TreePath getTreePath() {
		return new TreePath(new Object[] {item.getData()});
	}

	@Override
  public Object clone() {
		return new GridViewerRow(item);
	}

	@Override
  public Object getElement() {
		return item.getData();
	}

	void setItem(GridItem item) {
		this.item = item;
	}

    @Override
    public Widget getItem()
    {
        return item;
    }
}
