/*
 * Copyright (C) 2015 Universitat Pompeu Fabra
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

package org.gwaspi.gui.utils;

import java.awt.Dimension;
import javax.swing.JTable;

/**
 * Utilities for GUI layouts.
 */
public class LayoutUtils {

	public static void configureReasonableHeight(final JTable table) {

		// this returns a reasonable width (based on the actual columns),
		// but 0 height (only the header would be visible)
		final Dimension preferredSize = table.getPreferredSize();
		// so we set a sane minimum height
		preferredSize.height = 100;
		table.setPreferredScrollableViewportSize(preferredSize);
		table.setFillsViewportHeight(true);
		table.setAutoCreateColumnsFromModel(false);
		// ... we do all this, because otherwise
		// the default preffered height is way too big, especially
		// for netbook displays
	}
}
