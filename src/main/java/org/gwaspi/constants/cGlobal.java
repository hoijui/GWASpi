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

package org.gwaspi.constants;

import java.awt.Color;
import javax.swing.UIManager;

public class cGlobal {

	public static final String OSNAME = System.getProperty("os.name");
	public static final String OSARCH = System.getProperty("os.arch");
	public static final String SORT_SINGLE_DIR_CONFIG = "SSdir";
	public static final String SORT_EXEC_DIR_CONFIG = "ESdir";
	public static final String USER_DIR_DEFAULT = System.getProperty("user.dir");
	public static final String HOMEDIR = System.getProperty("user.home");
	public static final String JAVA_IO_TMPDIR = System.getProperty("java.io.tmpdir");
	public static final String LOCAL_VERSION_XML = "/version.xml";
	public static final String REMOTE_VERSION_XML = "http://www.gwaspi.org/downloads/version.xml";
	// Interloped table row colors
	public static final Color background = UIManager.getColor("Table.background");
	public static final Color alternateRowColor = new Color(background.getRed() - 20, background.getGreen() - 20, background.getBlue() - 20);
	//public static final Color alternateRowColor = UIManager.getColor("Table.dropLineColor");
	public static final Color foreground = UIManager.getColor("Table.foreground");
	public static final Color selectionBackground = UIManager.getColor("Table.selectionBackground");
	public static final Color selectionForeground = UIManager.getColor("Table.selectionForeground");

	private cGlobal() {
	}
}
