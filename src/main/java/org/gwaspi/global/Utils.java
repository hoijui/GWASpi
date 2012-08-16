package org.gwaspi.global;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.channels.FileChannel;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.Locale;
import javax.swing.*;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class Utils {

	// <editor-fold defaultstate="collapsed" desc="File and directory methods">
	public static String currentAppPath = "";
	private static JFileChooser fc;

	public static String GetAppPath() {
		currentAppPath = org.gwaspi.constants.cGlobal.USERDIR;
		//JOptionPane.showMessageDialog(base.ApipelineGUI.getFrames()[0], currentAppPath);
		return currentAppPath;
	}

	public static File createFolder(String path, String folderName) {
		String spoonFeeding = path + "/" + folderName;
		File f = new File(spoonFeeding);
		if (!f.exists()) {
			f.mkdir();
		}
		return f;
	}

	// Deletes all files and subdirectories under dir.
	// Returns true if all deletions were successful.
	// If a deletion fails, the method stops attempting to delete and returns false.
	public static boolean deleteFolder(File dir) {
		if (dir.isDirectory()) {
			String[] children = dir.list();
			for (int i = 0; i < children.length; i++) {
				boolean success = deleteFolder(new File(dir, children[i]));
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
		f.createNewFile();
		return f;
	}

	public static File[] listFiles(String path, final boolean foldersToo) {
		File dir = new File(path);
		File[] files = null;

		if (dir.isDirectory()) {
			files = dir.listFiles();

			// This filter only returns files, not directories
			java.io.FileFilter fileFilter = new java.io.FileFilter() {
				public boolean accept(File file) {
					if (foldersToo) {
						return file.isDirectory();
					} else {
						return !file.isDirectory();
					}
				}
			};

			files = dir.listFiles(fileFilter);
		} else {
			File[] tmpF = new File[1];
			tmpF[0] = dir;
			files = tmpF;
		}
		return files;
	}

	public static void copyFile(File in, File out) throws Exception {

		FileChannel inChannel = new FileInputStream(in).getChannel();
		FileChannel outChannel = new FileOutputStream(out).getChannel();
		try {
			inChannel.transferTo(0, inChannel.size(), outChannel);
		} catch (IOException e) {
			throw e;
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
		//Check to ensure that the source is valid...
		if (!src.exists()) {
			throw new IOException("copyFiles: Can not find source: " + src.getAbsolutePath() + ".");
		} else if (!src.canRead()) { //check to ensure we have rights to the source...
			throw new IOException("copyFiles: No right to source: " + src.getAbsolutePath() + ".");
		}
		//is this a directory copy?
		if (src.isDirectory()) {
			if (!dest.exists()) { //does the destination already exist?
				//if not we need to make it exist if possible (note this is mkdirs not mkdir)
				if (!dest.mkdirs()) {
					throw new IOException("copyFiles: Could not create direcotry: " + dest.getAbsolutePath() + ".");
				}
			}
			//get a listing of files...
			String list[] = src.list();
			//copy all the files in the list.
			for (int i = 0; i < list.length; i++) {
				File dest1 = new File(dest, list[i]);
				File src1 = new File(src, list[i]);
				copyFileRecursive(src1, dest1);
			}
		} else {
			//This was not a directory, so lets just copy the file
			FileInputStream fin = null;
			FileOutputStream fout = null;
			byte[] buffer = new byte[4096]; //Buffer 4K at a time (you can change this).
			int bytesRead;
			try {
				//open the files for input and output
				fin = new FileInputStream(src);
				fout = new FileOutputStream(dest);
				//while bytesRead indicates a successful read, lets write...
				while ((bytesRead = fin.read(buffer)) >= 0) {
					fout.write(buffer, 0, bytesRead);
				}
			} catch (IOException e) { //Error copying file...
				IOException wrapper = new IOException("copyFiles: Unable to copy file: "
						+ src.getAbsolutePath() + "to" + dest.getAbsolutePath() + ".");
				wrapper.initCause(e);
				wrapper.setStackTrace(e.getStackTrace());
				throw wrapper;
			} finally { //Ensure that the files are closed (if they were open).
				if (fin != null) {
					fin.close();
				}
				if (fout != null) {
					fout.close();
				}
			}
		}
	}

	// </editor-fold>
	// <editor-fold defaultstate="collapsed" desc="Date Time methods">
	public static String getShortDateTimeForFileName() {
		Date today;
		String dateOut;

		DateFormat timeFormatter = DateFormat.getTimeInstance(DateFormat.LONG, new Locale("es", "ES"));
		DateFormat dateFormatter = DateFormat.getDateInstance(DateFormat.SHORT, new Locale("en", "US"));

		today = new Date();
		dateOut = dateFormatter.format(today);
		dateOut = dateOut + timeFormatter.format(today);
		return dateOut;
	}

	public static String getShortDateTimeAsString() {
		Date today;
		String dateOut;

		DateFormat timeFormatter = DateFormat.getTimeInstance(DateFormat.LONG, new Locale("es", "ES"));
		DateFormat dateFormatter = DateFormat.getDateInstance(DateFormat.MEDIUM, new Locale("en", "ES"));

		today = new Date();
		dateOut = dateFormatter.format(today);
		dateOut = dateOut + " - " + timeFormatter.format(today);
		return dateOut;
	}

	public static String getMediumDateTimeAsString() {
		Date today;
		String dateOut;

		DateFormat timeFormatter = DateFormat.getTimeInstance(DateFormat.MEDIUM, new Locale("es", "ES"));
		DateFormat dateFormatter = DateFormat.getDateInstance(DateFormat.MEDIUM, new Locale("en", "US"));


		today = new Date();
		dateOut = dateFormatter.format(today);
		dateOut = dateOut + " " + timeFormatter.format(today);
//        dateOut = dateOut.replace(":", "-");
//        dateOut = dateOut.replace(" ", "-");
//        dateOut = dateOut.replace(",", "");
		return dateOut;
	}

	public static String getMediumDateAsString() {
		Date today;
		String dateOut;
		DateFormat dateFormatter = DateFormat.getDateInstance(DateFormat.MEDIUM, new Locale("en", "US"));
		today = new Date();
		dateOut = dateFormatter.format(today);
		return dateOut;
	}

	public static String getURIDate() {
		String matrixName = org.gwaspi.global.Utils.getMediumDateTimeAsString();
		matrixName = matrixName.replace(",", "");
		matrixName = matrixName.replace(":", "-");
		matrixName = matrixName.replace(" ", "_");
		matrixName = matrixName.replace("/", "-");
		matrixName.replaceAll("[a-zA-Z]", "");

		//matrixName = matrixName.substring(0, matrixName.length()-3); //Remove "CET" from name
		return matrixName;
	}

	public static String getSQLDateAsString() {
		Date today = new Date();
		java.sql.Date jsqlD = new java.sql.Date(today.getTime());
		return jsqlD.toString();
	}

	public static String getTimeStamp() {
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyyhhmmssSSSS");
		return sdf.format(cal.getTime());
	}

	public static String dateToString(Date date) {
		DateFormat formatter;
		formatter = new SimpleDateFormat("dd-MM-yyyy");
		String s = formatter.format(date);
		return s;
	}

	public static Date stringToDate(String txtDate) throws ParseException {
		Date date = null;
		DateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
		date = (Date) formatter.parse(txtDate);
		return date;
	}

	public static Date stringToDate(String txtDate, String format) {
		Date dateDate = null;
		DateFormat df = new SimpleDateFormat(format);
		try {
			dateDate = df.parse(txtDate);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return dateDate;
	}

	public static String now(String dateFormat) {
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
		return sdf.format(cal.getTime());
	}

	// </editor-fold>
	// <editor-fold defaultstate="collapsed" desc="String manipulation methods">
	public static String stripNonAlphaNumeric(String s) {
		String good =
				"abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
		String result = "";
		for (int i = 0; i < s.length(); i++) {
			if (good.indexOf(s.charAt(i)) >= 0) {
				result += s.charAt(i);
			}
		}
		return result;
	}

	public static String stripNonAlphaNumericDashUndscr(String s) {
		String good =
				"abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-_";
		String result = "";
		for (int i = 0; i < s.length(); i++) {
			if (good.indexOf(s.charAt(i)) >= 0) {
				result += s.charAt(i);
			}
		}
		return result;
	}

	// </editor-fold>
	//<editor-fold defaultstate="collapsed" desc="SYSTEM MANAGEMENT">
	public static void takeOutTheGarbage() {
		collectGarbageWithThreadSleep(0);    //Poke system to try to Garbage Collect!
	}

	public static void collectGarbageWithThreadSleep(int millisecs) {
//        try {
//            System.gc(); //Poke system to try to Garbage Collect!
//            if (millisecs>0) {
//                Thread.sleep(millisecs);
//                System.gc(); //Poke system to try to Garbage Collect!
//            }
//            System.out.println("Garbage collected at "+global.Utils.getMediumDateTimeAsString());
//        } catch (InterruptedException ex) {
//            Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
//        }
	}

	public static boolean checkIntenetConnection() {
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
		}

//        try {
//            InetAddress address = InetAddress.getByName("java.sun.com");
//            if(address != null){
//                isConnected = true;
//            }
//        } catch (UnknownHostException e) {
//            isConnected = false;
//        }
//        catch (IOException e) {
//            isConnected = false;
//        }


		return isConnected;
	}

	//</editor-fold>
	// <editor-fold defaultstate="collapsed" desc="Logging methods">
	public static String sysoutStart(String message) {
		if (message == null || message.isEmpty()) {
			message = "******* Started Operation at " + org.gwaspi.global.Utils.getMediumDateTimeAsString() + " *******";
		} else {
			message = "******* Started " + message + " at " + org.gwaspi.global.Utils.getMediumDateTimeAsString() + " *******";
		}
		System.out.println(message);
		return message;
	}

	public static String sysoutCompleted(String message) {
		if (message == null || message.isEmpty()) {
			message = "===> Completed Operation at " + org.gwaspi.global.Utils.getMediumDateTimeAsString() + " <===";
		} else {
			message = "===> Completed " + message + " at " + org.gwaspi.global.Utils.getMediumDateTimeAsString() + " <===";
		}
		System.out.println(message);
		return message;
	}

	public static String sysoutFinish(String message) {
		if (message == null || message.isEmpty()) {
			message = "################# Finished Operation at " + org.gwaspi.global.Utils.getMediumDateTimeAsString() + " #################\n\n";
		} else {
			message = "################# Finished " + message + " at " + org.gwaspi.global.Utils.getMediumDateTimeAsString() + " #################\n\n";
		}
		System.out.println(message);
		return message;
	}

	public static String sysoutError(String message) {
		if (message == null || message.isEmpty()) {
			message = "!!!!! Error encountered at " + org.gwaspi.global.Utils.getMediumDateTimeAsString() + " !!!!!";
		} else {
			message = "!!!!! Error encountered perfoming " + message + " at " + org.gwaspi.global.Utils.getMediumDateTimeAsString() + " !!!!!";
		}
		System.out.println(message);
		return message;
	}

	/**
	 * This logOperationInStudyDesc has now been deprecated in favor of
	 * ProcessTab
	 *
	 * @deprecated Use ProcessTab instead
	 */
	public static String logOperationInStudyDesc(String operation, int studyId) throws IOException {
//        StringBuffer result = new StringBuffer();
//        try {
//            String fileDir = org.gwaspi.global.Config.getConfigValue("LogDir","")+"/";
//            String fileName = "Study_"+ studyId + ".log";
//            File logFile = new File(fileDir+fileName);
//            if(!logFile.exists()){
//                createFile(fileDir, fileName);
//            }
//            StringBuffer description = new StringBuffer(org.gwaspi.framework.util.IOUtils.readFile(new FileReader(fileDir+fileName)));
//
//            FileWriter fw = new FileWriter(fileDir+fileName);
//            BufferedWriter bw = new BufferedWriter(fw);
//
//            bw.append(description.toString());
//            bw.append(operation);
//            bw.append("\nEnd Time: ");
//            bw.append(org.gwaspi.global.Utils.getMediumDateTimeAsString());
//            if(description.length()!=0){
//                bw.append("\n\n");
//            }
//            bw.close();
//
//            result = description;
//        } catch (IOException ex) {
//            Logger.getLogger(org.gwaspi.global.Utils.class.getName()).log(Level.SEVERE, null, ex);
//        }

		//gui.LogTab_old.refreshLogInfo();

		return operation;
	}

	/**
	 * This logStartMessageEnd has now been deprecated in favor of ProcessTab
	 *
	 * @deprecated Use ProcessTab instead
	 */
	public static String logStartMessageEnd(String startTime, String operation, String endTime, String studyId) throws IOException {
//        StringBuffer result = new StringBuffer();
//        try {
//            String fileDir = org.gwaspi.global.Config.getConfigValue("LogDir","")+"/";
//            String fileName = "Study_"+ studyId + ".log";
//            File logFile = new File(fileDir+fileName);
//            if(!logFile.exists()){
//                createFile(fileDir, fileName);
//            }
//            StringBuffer description = new StringBuffer(org.gwaspi.framework.util.IOUtils.readFile(new FileReader(fileDir+fileName)));
//
//            FileWriter fw = new FileWriter(fileDir+fileName);
//            BufferedWriter bw = new BufferedWriter(fw);
//
//            if(description.length()!=0){
//                bw.append("\n");
//            }
//            bw.write(description.toString());
//            bw.append("\nStart Time: "+startTime + "\n");
//            bw.append(operation);
//            bw.append("\nEnd Time: ");
//            bw.append(org.gwaspi.global.Utils.getMediumDateTimeAsString());
//            bw.close();
//
//            result = description;
//        } catch (IOException ex) {
//            Logger.getLogger(org.gwaspi.global.Utils.class.getName()).log(Level.SEVERE, null, ex);
//        }

		return operation;
	}

	/**
	 * This logBlockInStudyDesc has now been deprecated in favor of ProcessTab
	 *
	 * @deprecated Use ProcessTab instead
	 */
	public static String logBlockInStudyDesc(String operation, int studyId) throws IOException {
//        StringBuffer result = new StringBuffer();
//        try {
//            String fileDir = org.gwaspi.global.Config.getConfigValue("LogDir","")+"/";
//            String fileName = "Study_"+ studyId + ".log";
//            File logFile = new File(fileDir+fileName);
//            if(!logFile.exists()){
//                createFile(fileDir, fileName);
//            }
//
//            StringBuffer description = new StringBuffer(org.gwaspi.framework.util.IOUtils.readFile(new FileReader(fileDir+fileName)));
//
//            FileWriter fw = new FileWriter(fileDir+fileName);
//            BufferedWriter bw = new BufferedWriter(fw);
//
//            bw.append(description);
//            bw.append(operation);
//            bw.close();
//
//            result = description;
//        } catch (IOException ex) {
//            Logger.getLogger(org.gwaspi.global.Utils.class.getName()).log(Level.SEVERE, null, ex);
//        }
//
//        //gui.LogTab_old.refreshLogInfo();

		return operation;
	}
	// </editor-fold>
}