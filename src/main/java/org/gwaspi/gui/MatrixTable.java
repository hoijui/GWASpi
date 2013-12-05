/*
 * Copyright (C) 2013 Universitat Pompeu Fabra
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.gwaspi.gui;

import java.awt.Component;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

public class MatrixTable extends JTable {

	@Override
	public boolean isCellEditable(int row, int col) {
		return false; // Renders column 0 uneditable.
	}

	@Override
	public Component prepareRenderer(TableCellRenderer renderer, int rowIndex, int vColIndex) {

		Component tooltipComp = super.prepareRenderer(renderer, rowIndex, vColIndex);

		if (tooltipComp instanceof JComponent && getValueAt(rowIndex, vColIndex) != null) {
			JComponent jTooltipComp = (JComponent) tooltipComp;
			String plainTooltip = getValueAt(rowIndex, vColIndex).toString();
			String htmlTooltip = "<html>" + plainTooltip.replaceAll("\n", "<br>") + "</html>";
			jTooltipComp.setToolTipText(htmlTooltip);
		}

		return tooltipComp;
	}
}