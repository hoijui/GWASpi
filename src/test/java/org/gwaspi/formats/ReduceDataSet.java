/*
 * Copyright (C) 2015 Universitat Pompeu Fabra
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

package org.gwaspi.formats;

import java.io.File;
import java.io.IOException;
import org.gwaspi.constants.DBSamplesConstants;
import org.gwaspi.constants.ExportConstants.ExportFormat;
import org.gwaspi.constants.ImportConstants.ImportFormat;
import org.gwaspi.constants.NetCDFConstants.Defaults.GenotypeEncoding;
import org.gwaspi.constants.NetCDFConstants.Defaults.StrandType;
import org.gwaspi.global.Config;
import org.gwaspi.model.DataSetKey;
import org.gwaspi.model.MatrixKey;
import org.gwaspi.model.OperationKey;
import org.gwaspi.model.Study;
import org.gwaspi.model.StudyKey;
import org.gwaspi.model.StudyList;
import org.gwaspi.netCDF.exporter.MatrixExporter;
import org.gwaspi.netCDF.exporter.MatrixExporterParams;
import org.gwaspi.netCDF.loader.GenotypesLoadDescription;
import org.gwaspi.operations.MatrixOperation;
import org.gwaspi.operations.OperationManager;
import org.gwaspi.operations.filter.ByValidAffectionFilterOperation;
import org.gwaspi.operations.filter.ByValidAffectionFilterOperationParams;
import org.gwaspi.threadbox.Threaded_Loader_GWASifOK;
import org.gwaspi.threadbox.Threaded_MatrixQA;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReduceDataSet {

	private static final Logger log
			= LoggerFactory.getLogger(ReduceDataSet.class);

	private static final String DIR_INPUT_DEFAULT = System.getProperty("user.home")
			+ "/Projects/GWASpi/var/data/tutorial/extractedNew"; // XXX system dependent path
	private static final String DIR_OUTPUT = System.getProperty("user.home")
			+ "/Projects/GWASpi/var/data/tutorial/generated"; // XXX system dependent path

	public static void main(final String[] args) throws IOException {

		final String filePathBed;
		final String filePathFam;
		final String filePathBim;
		final File exportDataDir;
		if (args.length == 0) {
			filePathBed = DIR_INPUT_DEFAULT + "/PLINK_format/Tutorial_Matrix.bed";
			filePathFam = DIR_INPUT_DEFAULT + "/PLINK_format/Tutorial_Matrix.fam";
			filePathBim = DIR_INPUT_DEFAULT + "/PLINK_format/Tutorial_Matrix.bim";
			exportDataDir = new File(DIR_OUTPUT, "gwaspi_" + (int)(Math.random() * Integer.MAX_VALUE));
		} else {
			filePathBed = args[0];
			filePathFam = filePathBed.substring(0, filePathBed.length() - 4) + ".fam";
			filePathBim = filePathBed.substring(0, filePathBed.length() - 4) + ".bim";
			exportDataDir = new File(filePathBed).getParentFile();
		}

		final double leftOverMarkersFraction;
		if (args.length >= 2) {
			leftOverMarkersFraction = Double.parseDouble(args[1]);
			if ((leftOverMarkersFraction < 0.0) || (leftOverMarkersFraction > 1.0)) {
				throw new IllegalArgumentException(
						"left-over markers fraction has to be in [0.0, 1.0], but is: "
						+ leftOverMarkersFraction);
			}
		} else {
			leftOverMarkersFraction = 0.1;
		}

		log.info("Reducing data from PLink Binary files:");
		log.info("\t{}", filePathBed);
		log.info("\t{}", filePathFam);
		log.info("\t{}", filePathBim);
		log.info("to a data-set containing only {} times the number of markers,",
				leftOverMarkersFraction);
		log.info("and savig it to {}", exportDataDir);

		Config.createSingleton(false); // HACK
		final Config config = Config.getSingleton();
		config.putBoolean(Config.PROPERTY_STORAGE_IN_MEMORY, true); // HACK

		// Get current size of heap in bytes
		final int maxHeapSize = (int) Math.round((double) Runtime.getRuntime().totalMemory() / 1048576); // heapSize in MB
		final int maxProcessMarkers = (int) Math.round((double) maxHeapSize * 625); // 1.6GB needed for 10^6 markers (safe, 1.4 - 1.5 real)
		config.putInteger(Config.PROPERTY_MAX_HEAP_MB, maxHeapSize);
		config.putInteger(Config.PROPERTY_MAX_PROCESS_MARKERS, maxProcessMarkers);

		ReducingFilterOperation.register();
		exportDataDir.mkdirs();
		Config.getSingleton().putString(Config.PROPERTY_DATA_DIR, exportDataDir.getAbsolutePath());

		// create new study
		final StudyKey newStudy = StudyList.insertNewStudy(new Study("TEMP-study-name", "TEMP-description"));

		// load all the data
		final GenotypesLoadDescription loadDescription = new GenotypesLoadDescription(
				filePathBed,
				filePathFam,
				filePathBim,
				newStudy,
				ImportFormat.PLINK_Binary,
				"TEMP-friendly-name",
				"TEMP-description",
				null,
				StrandType.UNKNOWN,
				GenotypeEncoding.UNKNOWN);
		final Threaded_Loader_GWASifOK loadGwasTask = new Threaded_Loader_GWASifOK(
				loadDescription, // Format
				false, // Dummy samples
				false, // Do GWAS
				null); // gwasParams (dummy)
		loadGwasTask.run();
		//MultiOperations.queueTask(loadGwasTask);
		final MatrixKey resultMatrixKey = loadGwasTask.getResultMatrixKey();

		// clean the data
		final MatrixOperation<?, OperationKey> byValidAffectionFilterOperation
				= new ByValidAffectionFilterOperation(new ByValidAffectionFilterOperationParams(
						new DataSetKey(resultMatrixKey), "TEMP-by-valid-affection-op-name"));
		final OperationKey byValidAffectionFilterOpKey
				= OperationManager.performOperationCreatingOperation(byValidAffectionFilterOperation);

		// reduce the data
		final MatrixOperation<?, OperationKey> reducingFilterOperation
				= new ReducingFilterOperation(new ReducingFilterOperationParams(
						new DataSetKey(byValidAffectionFilterOpKey), "TEMP-reducing-filter-op-name", 0.1));
		final OperationKey reducingFilterOpKey
				= OperationManager.performOperationCreatingOperation(reducingFilterOperation);
		final DataSetKey reducingFilterOpDataSetKey = new DataSetKey(reducingFilterOpKey);

		// run QA's on the reduced set
		final Threaded_MatrixQA threaded_MatrixQA = new Threaded_MatrixQA(reducingFilterOpDataSetKey, false);
		threaded_MatrixQA.run();
//		final OperationKey[] qaOpKeys = Threaded_MatrixQA.matrixCompleeted(parent.getMatrixParent(), progressSource);

		// export the data
		final MatrixExporterParams matrixExporterParams
				= new MatrixExporterParams(reducingFilterOpDataSetKey,
						ExportFormat.PLINK_Binary, DBSamplesConstants.F_AFFECTION);
		final MatrixExporter matrixExporter = new MatrixExporter(matrixExporterParams);
		matrixExporter.call();

		System.exit(0);
	}
}
