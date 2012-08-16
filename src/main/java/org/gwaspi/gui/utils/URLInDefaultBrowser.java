package org.gwaspi.gui.utils;

import java.io.File;
import java.io.IOException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Enumeration;

/**
 * <b>Bare Bones Browser Launch for Java</b><br> Utility class to open a web
 * page from a Swing application in the user's default browser.<br> Supports:
 * Mac OS X, GNU/Linux, Unix, Windows XP/Vista<br> Example Usage:
 * <code><br> &nbsp; &nbsp;
 *    String url = "http://www.google.com/";<br> &nbsp; &nbsp;
 *    BareBonesBrowserLaunch.openHelpURL(url);<br></code> Latest Version: <a
 * href="http://www.centerkey.com/java/browser/">www.centerkey.com/java/browser</a><br>
 * Author: Dem Pilafian<br> Public Domain Software -- Free to Use as You Like
 *
 * @version 2.0, May 26, 2009
 */
public class URLInDefaultBrowser {

	static final String[] browsers = {"firefox", "opera", "konqueror", "epiphany",
		"seamonkey", "galeon", "kazehakase", "mozilla", "chromium-browser", "netscape"};

	/**
	 * Opens the specified web page in a web browser
	 *
	 * @param url A web address (URL) of a web page (ex:
	 * "http://www.google.com/")
	 */
	public static void browseGenericURL(String url) throws IOException {
		if (!java.awt.Desktop.isDesktopSupported()) {
			System.err.println("Desktop is not supported (fatal)");
//            System.exit(1);
		}

		java.awt.Desktop desktop = java.awt.Desktop.getDesktop();

		if (!desktop.isSupported(java.awt.Desktop.Action.BROWSE)) {
			System.err.println("Desktop doesn't support the browse action (fatal)");
//            System.exit(1);
		}


		try {
			if (org.gwaspi.global.Utils.checkIntenetConnection() && desktop.isSupported(java.awt.Desktop.Action.BROWSE)) {
				java.net.URI uri = new java.net.URI(url);
				desktop.browse(uri);
			} else {
				File file = new File(url);
				desktop.open(file);
			}

		} catch (Exception e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
	}

	public static void browseHelpURL(String helpFile) throws IOException {
		//String helpDir = org.gwaspi.global.Config.getConfigValue("HelpDir", "");
		String url = "";

		if (!org.gwaspi.global.Utils.checkIntenetConnection()) {
			String[] resourceURL = URLInDefaultBrowser.class.getClass().getResource("/resources/logo_white.png").toString().split("/");
			StringBuilder sb = new StringBuilder("/");
			for (int i = 1; i < resourceURL.length - 4; i++) {
				sb.append(resourceURL[i]);
				sb.append("/");
			}
			sb.append(HelpURLs.QryURL.helpIndex);
			url = sb.toString();
		} else {
			url = HelpURLs.baseURL + helpFile;
		}

		browseGenericURL(url);
	}
}
