package org.gwaspi.global;


import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * Download data from URLs and save it to local files.
 * @author Marco Schmidt
 * modified by me
 * FIXME Who is me?
 */
public class FileDownload {

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
			System.out.println(localFileName + "\t" + numWritten);
		} catch (Exception exception) {
			exception.printStackTrace();
		} finally {
			try {
				if (in != null) {
					in.close();
				}
				if (out != null) {
					out.close();
				}
			} catch (IOException ioe) {
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
			System.err.println("Could not figure out local file name for "
					+ address);
		}
	}
}
