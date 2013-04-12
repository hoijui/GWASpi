/* Copyright (c) Marco Schmidt (UNKOWN) */

/*
 * Other works by Marco Schmidt, like the JUI application or ImageInfo.java,
 * are published under public domain or under the GPL.
 */

package org.gwaspi.global;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Download data from URLs and save it to local files.
 * @author Marco Schmidt
 * @author Fernando Muñiz Fernandez
 */
public class FileDownload {

	private static final Logger log = LoggerFactory.getLogger(FileDownload.class);

	private FileDownload() {
	}

	public static boolean download(String address, String localFileName) {
		boolean result = false;
		OutputStream out = null;
		InputStream in = null;
		try {
			URL url = new URL(address);
			out = new BufferedOutputStream(
					new FileOutputStream(localFileName));
			URLConnection conn = url.openConnection();
			in = conn.getInputStream();
			byte[] buffer = new byte[1024];
			int numRead;
			long numWritten = 0;
			while ((numRead = in.read(buffer)) != -1) {
				out.write(buffer, 0, numRead);
				numWritten += numRead;
			}
			result = true;
			log.info("{}\t{}", localFileName, numWritten);
		} catch (Exception ex) {
			log.warn("CFailed to download from " + address, ex);
		} finally {
			try {
				if (in != null) {
					in.close();
				}
				if (out != null) {
					out.close();
				}
			} catch (IOException ex) {
				log.warn(null, ex);
			}
		}
		return result;
	}

	public static void download(String address) {
		int lastSlashIndex = address.lastIndexOf('/');
		if (lastSlashIndex >= 0
				&& lastSlashIndex < address.length() - 1) {
			download(address, address.substring(lastSlashIndex + 1));
		} else {
			log.warn("Could not figure out local file name for {}", address);
		}
	}
}
