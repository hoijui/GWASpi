/* Copyright (c) Dem Pilafian (public domain) */

package org.gwaspi.gui.utils;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import org.gwaspi.global.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

	private static final Logger log = LoggerFactory.getLogger(URLInDefaultBrowser.class);

//	private static final String[] BROWSERS = {
//		"firefox",
//		"opera",
//		"konqueror",
//		"epiphany",
//		"seamonkey",
//		"galeon",
//		"kazehakase",
//		"mozilla",
//		"chromium-browser",
//		"netscape"};

	private URLInDefaultBrowser() {
	}

	/**
	 * Opens the specified web page in a web browser
	 *
	 * @param url A web address (URL) of a web page (ex:
	 * "http://www.google.com/")
	 */
	public static void browseGenericURL(final String url) throws IOException {

		if (!Desktop.isDesktopSupported()) {
			log.error("Desktop is not supported (fatal)");
//			System.exit(1);
		}

		final Desktop desktop = Desktop.getDesktop();

		if (!desktop.isSupported(Desktop.Action.BROWSE)) {
			log.error("Desktop doesn't support the browse action (fatal)");
//			System.exit(1);
		}

		try {
			if (Utils.isInternetReachable() && desktop.isSupported(Desktop.Action.BROWSE)) {
				final URI uri = new URI(url);
				desktop.browse(uri);
			} else {
				File file = new File(url);
				desktop.open(file);
			}
		} catch (Exception ex) {
			log.error("Failed browsing " + url, ex);
		}
	}

	public static void browseHelpURL(String helpFile) throws IOException {

		//String helpDir = Config.getConfigValue("HelpDir", "");
		String url;

		if (!Utils.isInternetReachable()) {
			String[] resourceURL = URLInDefaultBrowser.class.getResource("/img/logo/logo_white.png").toString().split("/");
			StringBuilder urlBuilder = new StringBuilder();
			urlBuilder.append('/');
			for (int i = 1; i < resourceURL.length - 4; i++) {
				urlBuilder
						.append(resourceURL[i])
						.append('/');
			}
			urlBuilder.append(HelpURLs.QryURL.helpIndex);
			url = urlBuilder.toString();
		} else {
			url = HelpURLs.baseURL + helpFile;
		}

		browseGenericURL(url);
	}
}
