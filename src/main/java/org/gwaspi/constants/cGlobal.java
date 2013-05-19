package org.gwaspi.constants;

import java.awt.Color;
import javax.swing.UIManager;

public class cGlobal {

	public static final String APP_NAME = "GWASpi";
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
