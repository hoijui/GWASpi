package org.gwaspi.constants;

import java.awt.Color;
import javax.swing.UIManager;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class cGlobal {

	public final static String APP_NAME = "GWASpi";
	public final static String OSNAME = System.getProperty("os.name");
	public final static String OSARCH = System.getProperty("os.arch");
	public final static String USERDIR = System.getProperty("user.dir");
	public final static String HOMEDIR = System.getProperty("user.home");
	public final static String JAVA_IO_TMPDIR = System.getProperty("java.io.tmpdir");
	public final static String LOCAL_VERSION_XML = "/resources/version.xml";
	public final static String REMOTE_VERSION_XML = "http://www.gwaspi.org/downloads/version.xml";
	//Interloped table row colors
	public static Color background = UIManager.getColor("Table.background");
	public static Color alternateRowColor = new Color(background.getRed() - 20, background.getGreen() - 20, background.getBlue() - 20);
	//public static Color alternateRowColor = UIManager.getColor("Table.dropLineColor");
	public static Color foreground = UIManager.getColor("Table.foreground");
	public static Color selectionBackground = UIManager.getColor("Table.selectionBackground");
	public static Color selectionForeground = UIManager.getColor("Table.selectionForeground");
}
