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

package org.gwaspi.gui;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.gwaspi.threadbox.SwingDeleterItemList;
import org.gwaspi.threadbox.SwingWorkerItemList;
import org.junit.AfterClass;
import org.junit.BeforeClass;

public abstract class AbstractTestScripts {

	private static Setup setup = null;

	protected static void copyFile(URL srcFile, File dstFile, Map<String, String> substitutions) throws IOException {

		BufferedReader src = new BufferedReader(new InputStreamReader(srcFile.openStream()));

		OutputStreamWriter dst = new OutputStreamWriter(new FileOutputStream(dstFile));

		String line = src.readLine();
		while (line != null) {
			for (Map.Entry<String, String> substitution : substitutions.entrySet()) {
				line = line.replaceAll(substitution.getKey(), substitution.getValue());
			}
			dst.write(line, 0, line.length());
			dst.write('\n');

			line = src.readLine();
		}

		src.close();
		dst.close();
	}

	protected static void compareFiles(File origFile, File compareFile) throws IOException {

		BufferedReader orig = new BufferedReader(new FileReader(origFile));
		BufferedReader compare = new BufferedReader(new FileReader(compareFile));

		String origLine = orig.readLine();
		String compareLine = compare.readLine();
		while (origLine != null) {
			if (compareLine == null) {
				throw new IOException("the file to compare with is shorter then the original file");
			}
			if (!origLine.equals(compareLine)) {
				throw new IOException("the files are not equal");
			}

			origLine = orig.readLine();
			compareLine = compare.readLine();
		}
		if (compareLine != null) {
			throw new IOException("the file to compare with is longer then the original file");
		}

		orig.close();
		compare.close();
	}

	protected static class Setup {

		/**
		 * Temporary data-directory; only valid during a single
		 * unit-test-suite run.
		 */
		private final File dbDataDir;
		/**
		 * Where the application exports data to.
		 */
		private final File exportDir;
		/**
		 * Where we store temporary files to.
		 */
		private final File tmpDir;
		/**
		 * Where we store temporary script files to.
		 */
		private final File scriptsDir;

		private int lastLoadedMatrixId;
		private final Map<String, Integer> fileNameToLoadedMatrixId;

		private int studyId;

		Setup(File dbDataDir, File exportDir, File tmpDir, File scriptsDir) {

			this.dbDataDir = dbDataDir;
			this.exportDir = exportDir;
			this.tmpDir = tmpDir;
			this.scriptsDir = scriptsDir;
			this.lastLoadedMatrixId = -1;
			this.fileNameToLoadedMatrixId = new HashMap<String, Integer>();
			this.studyId = -1;
		}

		public static Setup createTemp() throws IOException {

			File dbDataDir = File.createTempFile("gwaspi_dbData_dir_", null);
			dbDataDir.delete();
			File exportDir = new File(dbDataDir, "export");

			File tmpDataDir = File.createTempFile("gwaspi_tmpData_dir_", null);
			tmpDataDir.delete();
			tmpDataDir.mkdir();
			File scriptsDir = new File(tmpDataDir, "scripts");
			scriptsDir.mkdir();

			return new Setup(dbDataDir, exportDir, tmpDataDir, scriptsDir);
		}

		public static void deleteDirRecursively(File toBeDeleted) throws IOException {

			if (toBeDeleted.isFile()) {
				toBeDeleted.delete();
			} else if (toBeDeleted.isDirectory()) {
				for (File containedFile : toBeDeleted.listFiles()) {
					deleteDirRecursively(containedFile);
				}
				toBeDeleted.delete();
			}
		}

		public void cleanupTemp() throws IOException {

			deleteDirRecursively(getDbDataDir());
			deleteDirRecursively(getTmpDir());
		}

		public void setStudyId(int studyId) {
			if (this.studyId != -1) {
				throw new IllegalStateException("The study-ID was already set");
			}
			this.studyId = studyId;
		}

		public int getStudyId() {
			return studyId;
		}

		public File getDbDataDir() {
			return dbDataDir;
		}

		public File getExportDir() {
			return exportDir;
		}

		public File getTmpDir() {
			return tmpDir;
		}

		public File getScriptsDir() {
			return scriptsDir;
		}

		public void addLoadedFileName(String matrixName) {

			if (!fileNameToLoadedMatrixId.containsKey(matrixName)) {
				fileNameToLoadedMatrixId.put(matrixName, ++lastLoadedMatrixId + 1);
			}
		}

		public Map<String, Integer> getMatrixIds() {
			return Collections.unmodifiableMap(fileNameToLoadedMatrixId);
		}
	}

	protected Setup getSetup() {
		return setup;
	}

	@BeforeClass
	public static void createTempDataDirs() throws IOException {

		setup = Setup.createTemp();
		setup.setStudyId(1); // XXX We should create a new study and use it
	}

	@AfterClass
	public static void cleanupTempDataDirs() throws IOException {

		setup.cleanupTemp();
		setup = null;
	}

	protected static void startGWASpi(String[] args) throws Exception {

//		StartGWASpi.main(args); // NOTE overrides all args!!

// FIXME BAD THREADDING!!! fix it first, and then all will resolve into wohlgefallen!
		StartGWASpi startGWASpi = new StartGWASpi();
		startGWASpi.start(Arrays.asList(args));

		int sum = 999;
		do {
			try {
				Thread.sleep(250);
			} catch (InterruptedException ex) {
				ex.printStackTrace();
			}
			sum = SwingWorkerItemList.sizePending() + SwingDeleterItemList.size();
		} while (sum > 0);
	}

	protected static String[] createArgs(String scriptPath, String logPath) {

		String[] args = ("script " + scriptPath + " log " + logPath).split(" ");

		return args;
	}
}
