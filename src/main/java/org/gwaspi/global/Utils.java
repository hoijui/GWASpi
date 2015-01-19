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

package org.gwaspi.global;

import java.io.Closeable;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.Serializable;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.channels.FileChannel;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Semaphore;
import org.gwaspi.constants.GlobalConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Utils {

	private static final Logger log = LoggerFactory.getLogger(Utils.class);
	private static final Locale timeLocale = new Locale("es", "ES");
	private static final Locale dateLocale = new Locale("en", "US");
	private static final DateFormat mediumTimeFormatter = DateFormat.getTimeInstance(DateFormat.MEDIUM, timeLocale);
	private static final DateFormat longTimeFormatter = DateFormat.getTimeInstance(DateFormat.LONG, timeLocale);
	private static final DateFormat shortDateFormatter = DateFormat.getDateInstance(DateFormat.SHORT, dateLocale);
	private static final DateFormat mediumDateFormatter = DateFormat.getDateInstance(DateFormat.MEDIUM, dateLocale);
	private static final DateFormat timeStampFormat = new SimpleDateFormat("ddMMyyyyhhmmssSSSS");
	private static final DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
	private static final String LICENSE_LOCATION = "/META-INF/gwaspi/LICENSE";
	private static final String MANIFEST_LOCATION = "/META-INF/MANIFEST.MF";
	public static final String MANIFEST_PROPERTY_VERSION = "Implementation-Version";
	public static final String MANIFEST_PROPERTY_BUILD = "Implementation-Build";
	public static final String MANIFEST_PROPERTY_BUILD_TIMESTAMP = "Build-Timestamp";
	public static final String MANIFEST_PROPERTY_JDK_VERSION = "Build-Jdk";

	private static Properties manifestProperties = null;
	private static Semaphore manifestPropertiesInit = new Semaphore(1);

	/**
	 * This filter only returns files, not directories
	 */
	private static final FileFilter FILES_ONLY_FILTER = new FileFilter() {
		@Override
		public boolean accept(File file) {
			return !file.isDirectory();
		}
	};

	private Utils() {
	}

	/**
	 * Tries to convert an Object to a meaningful String representation.
	 * We primarily use this to not convert char[] or byte[]
	 * to something like "C[@g63dfg" or "B[@g63dfg".
	 */
	public static String toMeaningfullRep(Object obj) {

		String rep;

		if (obj instanceof char[]) {
			rep = new String((char[]) obj);
		} else if (obj instanceof byte[]) {
			rep = new String((byte[]) obj);
		} else {
			rep = obj.toString();
		}

		return rep;
	}

	// <editor-fold defaultstate="expanded" desc="File and directory methods">
	private static String currentAppPath = "";

	public static String getAppPath() {
		currentAppPath = GlobalConstants.USER_DIR_DEFAULT;
		//JOptionPane.showMessageDialog(base.ApipelineGUI.getFrames()[0], currentAppPath);
		return currentAppPath;
	}

	/**
	 * Reads the LICENSE file from the class-path.
	 * @return the content of the LICENSE file
	 */
	public static String readLicense() {

		InputStream in = null;
		InputStreamReader reader = null;
		LineNumberReader lineReader = null;
		try {
			in = Utils.class.getResourceAsStream(LICENSE_LOCATION);
			if (in == null) {
				throw new IOException("Failed locating LICENSE file in the classpath: " + LICENSE_LOCATION);
			}

			reader = new InputStreamReader(in);
			lineReader = new LineNumberReader(reader);

			final String lineSeparator = System.getProperty("line.separator");
			final StringBuilder licenseFileContents = new StringBuilder();
			String line = lineReader.readLine();
			while (line != null) {
				licenseFileContents.append(line).append(lineSeparator);
				line = lineReader.readLine();
			}
			return licenseFileContents.toString();
		} catch (final IOException ex) {
			log.warn("Failed reading the LICENSE file", ex);
			return "Error when reading LICENSE file";
		} finally {
			final Closeable topCloseable;
			if (lineReader != null) {
				topCloseable = lineReader;
			} else if (reader != null) {
				topCloseable = reader;
			} else if (in != null) {
				topCloseable = in;
			} else {
				topCloseable = null;
			}
			if (topCloseable != null) {
				try {
					topCloseable.close();
				} catch (final IOException ioex) {
					log.warn("Failed closing stream to the LICENSE file", ioex);
				}
			}
		}
	}

	/**
	 * Reads all (JAR archive) manifest properties from the class-path.
	 * @return properties read from MANIFEST.MF
	 */
	private static Properties readManifest() {

		Properties manifestProps = null;

		InputStream manifestFileIn = null;
		InputStreamReader manifestFileReader = null;
		LineNumberReader manifestFileLineReader = null;
		try {
			manifestFileIn = Utils.class.getResourceAsStream(MANIFEST_LOCATION);
			if (manifestFileIn == null) {
				throw new IOException("Failed locating resource in the classpath: " + MANIFEST_LOCATION);
			}
			manifestFileReader = new InputStreamReader(manifestFileIn);
			manifestFileLineReader = new LineNumberReader(manifestFileReader);

			final Properties tmpProps = new Properties();
			// As the manifest file uses ": " as separatortains of key and value,
			// instead of "=" (as it is in "*.properties" files),
			// we can not use <code>tmpProps.load(manifestFileIn);</code>,
			// but have to parse the file directly.
			String manifestLine = manifestFileLineReader.readLine();
			while (manifestLine != null) {
				manifestLine = manifestLine.trim();
				if (!manifestLine.isEmpty() && !manifestLine.startsWith("#")) {
					final String[] keyAndValue = manifestLine.split(": ", 2);
					tmpProps.put(keyAndValue[0], keyAndValue[1]);
				}
				manifestLine = manifestFileLineReader.readLine();
			}
			manifestProps = tmpProps;
		} catch (final IOException ex) {
			log.warn("Failed reading the manifest file", ex);
			manifestProps = new Properties();
		} finally {
			final Closeable topCloseable;
			if (manifestFileLineReader != null) {
				topCloseable = manifestFileLineReader;
			} else if (manifestFileReader != null) {
				topCloseable = manifestFileReader;
			} else if (manifestFileIn != null) {
				topCloseable = manifestFileIn;
			} else {
				topCloseable = null;
			}
			if (topCloseable != null) {
				try {
					topCloseable.close();
				} catch (final IOException ioex) {
					log.warn("Failed closing stream to manifest file", ioex);
				}
			}
		}

		return manifestProps;
	}

	/**
	 * Returns the (buffered) properties from this applications MANIFEST.MF file in the
	 * META-INF directory of the class-path.
	 * @return properties from MANIFEST.MF
	 */
	public static Properties getManifestProperties() {

		if (manifestProperties == null) {
			try {
				manifestPropertiesInit.acquire();
				if (manifestProperties == null) {
					manifestProperties = readManifest();
				}
			} catch (final InterruptedException ex) {
				// do nothing
			} finally {
				manifestPropertiesInit.release();
			}
		}

		return manifestProperties;
	}

	public static File createFolder(File folder) throws IOException {

		if (log.isDebugEnabled()) {
			File parent = folder.getParentFile();
			List<File> systemRoots = null;
			while (!parent.exists()) {
				log.debug("parent folder does not exist: {}", parent.getAbsolutePath());
				if (systemRoots == null) {
					systemRoots = Arrays.asList(File.listRoots());
				}
				if (systemRoots.contains(parent)) {
					log.debug("given file-system-root does not exist: {}", parent.getAbsolutePath());
					break;
				}
				parent = parent.getParentFile();
			}
		}

		if (!folder.exists() && !folder.mkdir()) {
			throw new IOException("Failed to create directory " + folder.getPath());
		}

		return folder;
	}

	public static File createFolder(String path, String folderName) throws IOException {
		return createFolder(new File(path, folderName));
	}

	/**
	 * Deletes all files and subdirectories under dir.
	 * Returns true if all deletions were successful.
	 * If a deletion fails, the method stops attempting to delete and returns false.
	 */
	public static boolean deleteFolder(File dir) {

		if (dir.isDirectory()) {
			String[] children = dir.list();
			for (String child : children) {
				boolean success = deleteFolder(new File(dir, child));
				if (!success) {
					return false;
				}
			}
		}

		// The directory is now empty so delete it
		return dir.delete();
	}

	public static File createFile(String path, String fileName) throws IOException {
		String spoonFeeding = path + "/" + fileName;
		File f = new File(spoonFeeding);
		if (!f.exists() && !f.createNewFile()) {
			throw new IOException("Failed to create file " + f.getPath());
		}
		return f;
	}

	public static File[] listFiles(String path) {

		File[] files;

		File dir = new File(path);
		if (dir.isDirectory()) {
			files = dir.listFiles(FILES_ONLY_FILTER);
		} else {
			File[] tmpF = new File[1];
			tmpF[0] = dir;
			files = tmpF;
		}

		return files;
	}

	public static void copyFile(File in, File out) throws IOException {

		FileChannel inChannel = new FileInputStream(in).getChannel();
		FileChannel outChannel = new FileOutputStream(out).getChannel();
		try {
			inChannel.transferTo(0, inChannel.size(), outChannel);
		} catch (IOException ex) {
			throw ex;
		} finally {
			if (inChannel != null) {
				inChannel.close();
			}
			if (outChannel != null) {
				outChannel.close();
			}
		}
	}

	/**
	 * This function will copy files or directories from one location to
	 * another. note that the source and the destination must be mutually
	 * exclusive. This function can not be used to copy a directory to a sub
	 * directory of itself. The function will also have problems if the
	 * destination files already exist.
	 *
	 * @param src -- A File object that represents the source for the copy
	 * @param dest -- A File object that represents the destination for the
	 * copy.
	 * @throws IOException if unable to copy.
	 */
	public static void copyFileRecursive(File src, File dest) throws IOException {
		// Check to ensure that the source is valid...
		if (!src.exists()) {
			throw new IOException("copyFiles: Can not find source: " + src.getAbsolutePath() + ".");
		} else if (!src.canRead()) { //check to ensure we have rights to the source...
			throw new IOException("copyFiles: No right to source: " + src.getAbsolutePath() + ".");
		}
		// is this a directory copy?
		if (src.isDirectory()) {
			// does the destination already exist?
			// if not we need to make it exist if possible
			// (NOTE this is mkdirs not mkdir)
			if (!dest.exists() && !dest.mkdirs()) {
				throw new IOException("copyFiles: Could not create direcotry: " + dest.getAbsolutePath() + ".");
			}
			// get a listing of files...
			String[] list = src.list();
			for (String listEntry : list) {
				File dest1 = new File(dest, listEntry);
				File src1 = new File(src, listEntry);
				copyFileRecursive(src1, dest1);
			}
		} else {
			// This was not a directory, so lets just copy the file
			FileInputStream fin = null;
			FileOutputStream fout = null;
			byte[] buffer = new byte[4096]; // Buffer 4K at a time (you can change this).
			int bytesRead;
			try {
				// open the files for input and output
				fin = new FileInputStream(src);
				fout = new FileOutputStream(dest);
				// while bytesRead indicates a successful read, lets write...
				while ((bytesRead = fin.read(buffer)) >= 0) {
					fout.write(buffer, 0, bytesRead);
				}
			} catch (IOException ex) { // Error copying file...
				IOException wrapper = new IOException("copyFiles: Unable to copy file: "
						+ src.getAbsolutePath() + "to" + dest.getAbsolutePath() + ".");
				wrapper.initCause(ex);
				wrapper.setStackTrace(ex.getStackTrace());
				throw wrapper;
			} finally { // Ensure that the files are closed (if they were open).
				if (fin != null) {
					fin.close();
				}
				if (fout != null) {
					fout.close();
				}
			}
		}
	}

	public static void tryToDeleteFile(File toDelete) throws IOException {

		if (toDelete.exists()) {
			if (!toDelete.canWrite()) {
				throw new IOException("Failed to delete file; write protected: " + toDelete.getPath());
			}

			// If it is a directory, make sure it is empty
			if (toDelete.isDirectory()) {
				final String[] files = toDelete.list();
				if (files.length > 0) {
					throw new IllegalArgumentException("Failed to delete file; directory not empty: " + toDelete);
				}
			}

			boolean success = toDelete.delete();
			if (!success) {
				throw new IOException("Failed to delete file; reason unknown: " + toDelete.getPath());
			}
		}
	}

	public static void move(final File source, final File destination) throws IOException {

		Utils.copyFile(source, destination);
		Utils.tryToDeleteFile(source);
	}
	// </editor-fold>

	// <editor-fold defaultstate="expanded" desc="Date Time methods">
	public static String getShortDateTimeForFileName() {

		Date now = new Date();
		return getShortDateTimeForFileName(now);
	}

	public static String getShortDateTimeForFileName(Date date) {

		final String dateStr;
		synchronized (shortDateFormatter) {
			dateStr = shortDateFormatter.format(date);
		}
		final String timeStr;
		synchronized (longTimeFormatter) {
			timeStr = longTimeFormatter.format(date);
		}
		final String dateOut = dateStr + timeStr;

		return dateOut;
	}

	public static String getShortDateTimeAsString(final Date time) {

		final String dateStr;
		synchronized (mediumDateFormatter) {
			dateStr = mediumDateFormatter.format(time);
		}
		final String timeStr;
		synchronized (longTimeFormatter) {
			timeStr = longTimeFormatter.format(time);
		}
		final String dateOut = dateStr + " " + timeStr;

		return dateOut;
	}

	public static String getShortDateTimeAsString() {

		final Date now = new Date();
		return getShortDateTimeAsString(now);
	}

	public static String getMediumDateTimeAsString() {

		Date now = new Date();
		final String dateStr;
		synchronized (mediumDateFormatter) {
			dateStr = mediumDateFormatter.format(now);
		}
		final String timeStr;
		synchronized (mediumTimeFormatter) {
			timeStr = mediumTimeFormatter.format(now);
		}
		final String dateOut = dateStr + " " + timeStr;
//		dateOut = dateOut.replace(":", "-");
//		dateOut = dateOut.replace(" ", "-");
//		dateOut = dateOut.replace(",", "");

		return dateOut;
	}

	public static String getMediumDateAsString() {

		Date now = new Date();
		synchronized (mediumDateFormatter) {
			return mediumDateFormatter.format(now);
		}
	}

	public static String getTimeStamp() {

		Calendar now = Calendar.getInstance();
		synchronized (timeStampFormat) {
			return timeStampFormat.format(now.getTime());
		}
	}

	public static String toHumanReadableTime(final long milliseconds) {
		return milliseconds + "ms"; // XXX make nicer (days, hours, minutes, seconds, millieconds)
	}

	public static Date stringToDate(String txtDate, String format) {

		Date dateDate = null;

		DateFormat df = new SimpleDateFormat(format);
		try {
			dateDate = df.parse(txtDate);
		} catch (ParseException ex) {
			log.error("Failed to convert to a dat: " + txtDate, ex);
		}

		return dateDate;
	}

	public static String now(String dateFormat) {
		Calendar now = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
		return sdf.format(now.getTime());
	}
	// </editor-fold>

	// <editor-fold defaultstate="expanded" desc="String manipulation methods">
	public static String stripNonAlphaNumeric(String s) {
		String good =
				"abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

		StringBuilder result = new StringBuilder();
		for (int i = 0; i < s.length(); i++) {
			if (good.indexOf(s.charAt(i)) >= 0) {
				result.append(s.charAt(i));
			}
		}

		return result.toString();
	}

	public static String stripNonAlphaNumericDashUndscr(String s) {
		String good =
				"abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-_";

		StringBuilder result = new StringBuilder();
		for (int i = 0; i < s.length(); i++) {
			if (good.indexOf(s.charAt(i)) >= 0) {
				result.append(s.charAt(i));
			}
		}

		return result.toString();
	}
	// </editor-fold>

	//<editor-fold defaultstate="expanded" desc="SYSTEM MANAGEMENT">
	public static void takeOutTheGarbage() {
		collectGarbageWithThreadSleep(0); // Poke system to try to Garbage Collect!
	}

	public static void collectGarbageWithThreadSleep(int millisecs) {
//		try {
//			System.gc(); //Poke system to try to Garbage Collect!
//			if (millisecs>0) {
//				Thread.sleep(millisecs);
//				System.gc(); //Poke system to try to Garbage Collect!
//			}
//			log.info("Garbage collected");
//		} catch (InterruptedException ex) {
//			log.error(null, ex);
//		}
	}

	/**
	 * FIXME This is actually only checking for a network connection,
	 * not for internet connection.
	 */
	public static boolean checkInternetConnection() {

		boolean isConnected = false;

		try {
			Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
			while (interfaces.hasMoreElements()) {
				NetworkInterface interf = interfaces.nextElement();
				if (interf.isUp() && !interf.isLoopback()) {
					isConnected = true;
				}
			}
		} catch (SocketException ex) {
			log.warn(null, ex);
		}

//		try {
//			InetAddress address = InetAddress.getByName("java.sun.com");
//			if (address != null) {
//				isConnected = true;
//			}
//		} catch (UnknownHostException ex) {
//			isConnected = false;
//		} catch (IOException ex) {
//			isConnected = false;
//		}

		return isConnected;
	}
	//</editor-fold>

	// <editor-fold defaultstate="expanded" desc="Logging methods">
	public static String createActualMessage(String message) {
		return ((message == null) || message.isEmpty())
				? "Operation"
				: message;
	}

	public static void sysoutStart(String message) {
		log.info("******* Started {} *******", createActualMessage(message));
	}

	public static void sysoutCompleted(String message) {
		log.info("===> Completed {} <===", createActualMessage(message));
	}

	public static void sysoutFinish(String message) {
		log.info("################# Finished {} #################", createActualMessage(message));
		log.info("");
		log.info("");
	}

	public static void sysoutError(String message) {
		String actualMessage = ((message == null) || message.isEmpty())
				? ""
				: " perfoming " + message;
		log.error("!!!!! Error encountered{} !!!!!", actualMessage);
	}

	/**
	 * This logOperationInStudyDesc has now been deprecated in favor of
	 * ProcessTab
	 *
	 * @deprecated Use ProcessTab instead
	 */
	public static void logOperationInStudyDesc(String operation, int studyId) throws IOException {
//		StringBuffer result = new StringBuffer();
//		try {
//			String fileDir = Config.getConfigValue(Config.PROPERTY_LOG_DIR,"")+"/";
//			String fileName = "Study_"+ studyId + ".log";
//			File logFile = new File(fileDir+fileName);
//			if(!logFile.exists()){
//				createFile(fileDir, fileName);
//			}
//			StringBuffer description = new StringBuffer(org.gwaspi.framework.util.IOUtils.readFile(new FileReader(fileDir+fileName)));
//
//			FileWriter fw = new FileWriter(fileDir+fileName);
//			BufferedWriter bw = new BufferedWriter(fw);
//
//			bw.append(description.toString());
//			bw.append(operation);
//			bw.append("\nEnd Time: ");
//			bw.append(Utils.getMediumDateTimeAsString());
//			if(description.length()!=0){
//				bw.append("\n\n");
//			}
//			bw.close();
//
//			result = description;
//		} catch (IOException ex) {
//			log.error(null, ex);
//		}
//
//		//gui.LogTab_old.refreshLogInfo();
	}

	/**
	 * This logStartMessageEnd has now been deprecated in favor of ProcessTab
	 *
	 * @deprecated Use ProcessTab instead
	 */
	public static void logStartMessageEnd(String startTime, String operation, String endTime, String studyId) throws IOException {
//		StringBuffer result = new StringBuffer();
//		try {
//			String fileDir = Config.getConfigValue(Config.PROPERTY_LOG_DIR,"")+"/";
//			String fileName = "Study_"+ studyId + ".log";
//			File logFile = new File(fileDir+fileName);
//			if(!logFile.exists()){
//				createFile(fileDir, fileName);
//			}
//			StringBuffer description = new StringBuffer(org.gwaspi.framework.util.IOUtils.readFile(new FileReader(fileDir+fileName)));
//
//			FileWriter fw = new FileWriter(fileDir+fileName);
//			BufferedWriter bw = new BufferedWriter(fw);
//
//			if(description.length()!=0){
//				bw.append("\n");
//			}
//			bw.write(description.toString());
//			bw.append("\nStart Time: "+startTime + "\n");
//			bw.append(operation);
//			bw.append("\nEnd Time: ");
//			bw.append(Utils.getMediumDateTimeAsString());
//			bw.close();
//
//			result = description;
//		} catch (IOException ex) {
//			log.error(null, ex);
//		}
	}

	/**
	 * This logBlockInStudyDesc has now been deprecated in favor of ProcessTab
	 *
	 * @deprecated Use ProcessTab instead
	 */
	public static void logBlockInStudyDesc(String operation, int studyId) throws IOException {
//		StringBuffer result = new StringBuffer();
//		try {
//			String fileDir = Config.getConfigValue(Config.PROPERTY_LOG_DIR,"")+"/";
//			String fileName = "Study_"+ studyId + ".log";
//			File logFile = new File(fileDir+fileName);
//			if(!logFile.exists()){
//				createFile(fileDir, fileName);
//			}
//
//			StringBuffer description = new StringBuffer(org.gwaspi.framework.util.IOUtils.readFile(new FileReader(fileDir+fileName)));
//
//			FileWriter fw = new FileWriter(fileDir+fileName);
//			BufferedWriter bw = new BufferedWriter(fw);
//
//			bw.append(description);
//			bw.append(operation);
//			bw.close();
//
//			result = description;
//		} catch (IOException ex) {
//			log.error(null, ex);
//		}
//
//		//gui.LogTab_old.refreshLogInfo();
	}
	// </editor-fold>


	//<editor-fold defaultstate="expanded" desc="Collections methods">
	/**
	 * Creates a new list with the order of entries equal to the order
	 * of entries in the collection 'order',
	 * regarding the indices of the collection 'values',
	 * and the values copied from the collection 'values'.
	 */
	public static <V> List<V> createIndicesOrderedList(final Collection<Integer> order, final Collection<V> values) {

		List<V> result = new ArrayList<V>(order.size());

		if (values instanceof List) {
			List<V> valuesList = (List<V>) values;
			for (Integer orderIndex : order) {
				result.add(valuesList.get(orderIndex));
			}
		} else {
			int curIndex = 0;
			for (V value : values) {
				if (order.contains(curIndex)) {
					result.add(value);
				}
				curIndex++;
			}
		}

		return result;
	}

	/**
	 * Creates a new map, with the order of entries equal to the order
	 * of entries in the Map order (regarding the keys),
	 * and the values copied from the Map values.
	 */
	public static <K, V> Map<K, V> createOrderedMap(final Collection<K> order, final Map<K, V> values) {

		Map<K, V> result = new LinkedHashMap<K, V>(order.size());

		for (K key : order) {
			result.put(key, values.get(key));
		}

		return result;
	}

	public static <K, V> Map<K, V> createMapSortedByValue(Map<K, V> map) {
		return createSortedMap(map, new MapValueComparator<K, V>(true));
	}

	public static <K, V> Map<K, V> createMapSortedByValueDescending(Map<K, V> map) {
		return createSortedMap(map, new MapValueComparator<K, V>(false));
	}

	private static class MapValueComparator<K, V> implements Comparator<Map.Entry<K, V>>, Serializable {

		private final int multiplier;

		MapValueComparator(boolean ascending) {
			this.multiplier = ascending ? 1 : -1;
		}

		@Override
		public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
			return multiplier * ((Comparable) o1.getValue()).compareTo(o2.getValue());
		}
	}

	private static <K, V> Map<K, V> createSortedMap(Map<K, V> map, Comparator<Map.Entry<K, V>> comparator) {

		Map<K, V> result = new LinkedHashMap<K, V>(map.size());

		List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>(map.entrySet());
		Collections.sort(list, comparator);

		for (Map.Entry<K, V> entry : list) {
			result.put(entry.getKey(), entry.getValue());
		}

		return result;
	}
	// </editor-fold>
}
