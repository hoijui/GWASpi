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
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.net.URLConnection;
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
	public static final String DEFAULT_INTERNET_CONNECTION_CHECK_URL = "http://www.gwaspi.org";

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
	 * Tries to convert any number into an easily readable String representation.
	 * for example: 123456789000 -> "123'456'789'000"
	 */
	public static String toHumanReadableNum(final Number number) {

		String rep = number.toString();

		// split into parts
		final String signPart = rep.startsWith("-") ? "-" : "";
		final String integerPart = rep.substring(signPart.length()).replaceFirst("[^0-9].*$", "");
		final String restPart = rep.substring(signPart.length() + integerPart.length());

		// beautify the integer part
		final int n = integerPart.length();
		String integerPartNice = integerPart.substring(0, n % 3);
		for (int i = n % 3; i < n; i += 3) {
			if (!integerPartNice.isEmpty()) {
				integerPartNice += '\'';
			}
			integerPartNice += integerPart.substring(i, Math.min(i + 3, integerPart.length()));
		}

		rep = signPart + integerPartNice + restPart;

		return rep;
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

	public static File createFolder(final File folder) throws IOException {

		// strangely, java needs this to be able to fetch the parent and to check if the file exists
		final File folderCanonical = folder.getCanonicalFile();

		if (log.isDebugEnabled()) {
			File parent = folderCanonical.getCanonicalFile().getParentFile();
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

		if (!folderCanonical.exists() && !folderCanonical.mkdir()) {
			throw new IOException("Failed to create directory \"" + folder.getCanonicalPath() + "\"");
		}

		return folderCanonical;
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

		final StringBuilder humanReadableTime = new StringBuilder();
		long leftover = milliseconds;

		humanReadableTime.insert(0, "ms").insert(0, leftover % 1000);
		leftover = leftover / 1000;

		if (leftover > 0) {
			humanReadableTime.insert(0, "s ").insert(0, leftover % 60);
			leftover = leftover / 60;
		}

		if (leftover > 0) {
			humanReadableTime.insert(0, "min ").insert(0, leftover % 60);
			leftover = leftover / 60;
		}

		if (leftover > 0) {
			humanReadableTime.insert(0, "h ").insert(0, leftover % 24);
			leftover = leftover / 24;
		}

		if (leftover > 0) {
			humanReadableTime.insert(0, "d ").insert(0, leftover % 365);
			leftover = leftover / 365;
		}

		if (leftover > 0) {
			humanReadableTime.insert(0, "y ").insert(0, leftover);
			leftover = 0;
		}

		return humanReadableTime.toString();
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
	public static String stripNonAlphaNumeric(final String str) {
		return str.replaceAll("[^a-zA-Z0-9]", "");
	}

	public static String stripNonAlphaNumericDashUndscr(final String str) {
		return str.replaceAll("[^a-zA-Z0-9_-]", "");
	}
	// </editor-fold>

	//<editor-fold defaultstate="expanded" desc="SYSTEM MANAGEMENT">
	/**
	 * Checks whether this computer is connected to a network.
	 */
	public static boolean isNetworkConnected() {

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

	/**
	 * Checks for a connection to the internet through a dummy request.
	 * @return whether the supplied address was reachable or not
	 */
	public static boolean isURLReachable(final URL url) {

		boolean connected;
		try {
			// open a connection to that source
			final URLConnection urlConnect = url.openConnection();

			// Try to retrieve data from the source.
			// If there is no connection, this line will throw an exception.
			final Object objData = urlConnect.getContent();
			connected = true;
		} catch (final IOException ex) {
			log.warn("Internet connection attempt resulted in: negative");
			log.trace("... detailed problem", ex);
			connected = false;
		}

		return connected;
	}

	public static boolean isInternetReachable() {

		boolean reachable;
		try {
			reachable = isURLReachable(new URL(DEFAULT_INTERNET_CONNECTION_CHECK_URL));
		} catch (final MalformedURLException ex) {
			log.error("Internet connection attempt resulted in: negative", ex);
			reachable = false;
		}

		return reachable;
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

		private static final long serialVersionUID = 1L;

		private final int multiplier;

		MapValueComparator(boolean ascending) {
			this.multiplier = ascending ? 1 : -1;
		}

		@Override
		public int compare(Map.Entry<K, V> entry1, Map.Entry<K, V> entry2) {
			return multiplier * ((Comparable) entry1.getValue()).compareTo(entry2.getValue());
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
