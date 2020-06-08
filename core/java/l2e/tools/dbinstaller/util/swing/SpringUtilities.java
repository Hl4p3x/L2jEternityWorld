/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://eternity-world.ru/>.
 */
package l2e.tools.dbinstaller.util.swing;

import java.awt.Component;
import java.awt.Container;

import javax.swing.Spring;
import javax.swing.SpringLayout;

public class SpringUtilities
{
	public static void printSizes(Component c)
	{
		System.out.println("minimumSize = " + c.getMinimumSize());
		System.out.println("preferredSize = " + c.getPreferredSize());
		System.out.println("maximumSize = " + c.getMaximumSize());
	}
	
	public static void makeGrid(Container parent, int rows, int cols, int initialX, int initialY, int xPad, int yPad)
	{
		SpringLayout layout;
		try
		{
			layout = (SpringLayout) parent.getLayout();
		}
		catch (ClassCastException exc)
		{
			System.err.println("The first argument to makeGrid must use SpringLayout.");
			return;
		}
		
		Spring xPadSpring = Spring.constant(xPad);
		Spring yPadSpring = Spring.constant(yPad);
		Spring initialXSpring = Spring.constant(initialX);
		Spring initialYSpring = Spring.constant(initialY);
		int max = rows * cols;
		
		Spring maxWidthSpring = layout.getConstraints(parent.getComponent(0)).getWidth();
		Spring maxHeightSpring = layout.getConstraints(parent.getComponent(0)).getWidth();
		for (int i = 1; i < max; i++)
		{
			SpringLayout.Constraints cons = layout.getConstraints(parent.getComponent(i));
			
			maxWidthSpring = Spring.max(maxWidthSpring, cons.getWidth());
			maxHeightSpring = Spring.max(maxHeightSpring, cons.getHeight());
		}
		
		for (int i = 0; i < max; i++)
		{
			SpringLayout.Constraints cons = layout.getConstraints(parent.getComponent(i));
			
			cons.setWidth(maxWidthSpring);
			cons.setHeight(maxHeightSpring);
		}
		
		SpringLayout.Constraints lastCons = null;
		SpringLayout.Constraints lastRowCons = null;
		for (int i = 0; i < max; i++)
		{
			SpringLayout.Constraints cons = layout.getConstraints(parent.getComponent(i));
			if ((i % cols) == 0)
			{
				lastRowCons = lastCons;
				cons.setX(initialXSpring);
			}
			else
			{
				if (lastCons != null)
				{
					cons.setX(Spring.sum(lastCons.getConstraint(SpringLayout.EAST), xPadSpring));
				}
			}
			
			if ((i / cols) == 0)
			{
				cons.setY(initialYSpring);
			}
			else
			{
				if (lastRowCons != null)
				{
					cons.setY(Spring.sum(lastRowCons.getConstraint(SpringLayout.SOUTH), yPadSpring));
				}
			}
			lastCons = cons;
		}
		
		SpringLayout.Constraints pCons = layout.getConstraints(parent);
		if (lastCons != null)
		{
			pCons.setConstraint(SpringLayout.SOUTH, Spring.sum(Spring.constant(yPad), lastCons.getConstraint(SpringLayout.SOUTH)));
			pCons.setConstraint(SpringLayout.EAST, Spring.sum(Spring.constant(xPad), lastCons.getConstraint(SpringLayout.EAST)));
		}
	}
	
	private static SpringLayout.Constraints getConstraintsForCell(int row, int col, Container parent, int cols)
	{
		SpringLayout layout = (SpringLayout) parent.getLayout();
		Component c = parent.getComponent((row * cols) + col);
		return layout.getConstraints(c);
	}
	
	public static void makeCompactGrid(Container parent, int rows, int cols, int initialX, int initialY, int xPad, int yPad)
	{
		SpringLayout layout;
		try
		{
			layout = (SpringLayout) parent.getLayout();
		}
		catch (ClassCastException exc)
		{
			System.err.println("The first argument to makeCompactGrid must use SpringLayout.");
			return;
		}
		
		Spring x = Spring.constant(initialX);
		for (int c = 0; c < cols; c++)
		{
			Spring width = Spring.constant(0);
			for (int r = 0; r < rows; r++)
			{
				width = Spring.max(width, getConstraintsForCell(r, c, parent, cols).getWidth());
			}
			for (int r = 0; r < rows; r++)
			{
				SpringLayout.Constraints constraints = getConstraintsForCell(r, c, parent, cols);
				constraints.setX(x);
				constraints.setWidth(width);
			}
			x = Spring.sum(x, Spring.sum(width, Spring.constant(xPad)));
		}
		
		Spring y = Spring.constant(initialY);
		for (int r = 0; r < rows; r++)
		{
			Spring height = Spring.constant(0);
			for (int c = 0; c < cols; c++)
			{
				height = Spring.max(height, getConstraintsForCell(r, c, parent, cols).getHeight());
			}
			for (int c = 0; c < cols; c++)
			{
				SpringLayout.Constraints constraints = getConstraintsForCell(r, c, parent, cols);
				constraints.setY(y);
				constraints.setHeight(height);
			}
			y = Spring.sum(y, Spring.sum(height, Spring.constant(yPad)));
		}
		SpringLayout.Constraints pCons = layout.getConstraints(parent);
		pCons.setConstraint(SpringLayout.SOUTH, y);
		pCons.setConstraint(SpringLayout.EAST, x);
	}
}