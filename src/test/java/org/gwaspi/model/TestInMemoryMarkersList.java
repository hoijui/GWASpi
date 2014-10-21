/*
 * Copyright (C) 2014 Universitat Pompeu Fabra
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

package org.gwaspi.model;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.gwaspi.constants.ImportConstants.ImportFormat;
import org.gwaspi.constants.NetCDFConstants.Defaults.GenotypeEncoding;
import org.gwaspi.constants.NetCDFConstants.Defaults.OPType;
import org.gwaspi.constants.NetCDFConstants.Defaults.StrandType;
import org.gwaspi.datasource.inmemory.InMemoryMarkersKeysSource;
import org.gwaspi.global.Config;
import org.gwaspi.global.Utils;
import org.gwaspi.netCDF.loader.GenotypesLoadDescription;
import org.gwaspi.threadbox.CommonRunnable;
import org.gwaspi.threadbox.MultiOperations;
import org.gwaspi.threadbox.LoadAndGWASIfOKCombinedOperation;
import org.junit.Ignore;
import org.junit.Test;

public class TestInMemoryMarkersList {

	@Test
	@Ignore
	public void testChunkedList() throws IOException {

		Config.createSingleton(false); // HACK
		Config.getSingleton().putBoolean(Config.PROPERTY_STORAGE_IN_MEMORY, true); // HACK
		final File dataDir = File.createTempFile("gwaspiDD_", "");
		dataDir.delete();
		Utils.createFolder(dataDir);
		Utils.createFolder(new File(dataDir, "reports"));
		Config.getSingleton().putString(Config.PROPERTY_DATA_DIR, dataDir.getAbsolutePath());

		final int numMatrixKeys = 2000;
		final int numOperationKeys = 20;

		InMemoryMarkersKeysSource.clearStorage();

		StudyKey studyKey = StudyList.getStudyService().insertStudy(new Study("", ""));

final String localPath_HACK = System.getProperty("user.home") + "/Projects/GWASpi/var/data/tutorial/extractedNew/"; // HACK

		GenotypesLoadDescription loadDesc = new GenotypesLoadDescription(
				localPath_HACK + "c_2150SNPs.map",
				localPath_HACK + "c_sampleInfo_rndsex.txt",
				localPath_HACK + "c_2150SNPs.ped",
				studyKey,
				ImportFormat.PLINK,
				"freindlyName",
				"description",
				null,
				StrandType.UNKNOWN,
				GenotypeEncoding.UNKNOWN);
		final LoadAndGWASIfOKCombinedOperation loader = new LoadAndGWASIfOKCombinedOperation(loadDesc, false, false, null);
		loader.run();
		final MatrixKey matrixKey = loader.getResultMatrixKey();

		final MarkersKeysSource matrixKS = InMemoryMarkersKeysSource.createForMatrix(matrixKey, null);
		OperationKey operationKey = OperationsList.getOperationService().insertOperation(new OperationMetadata(new DataSetKey(matrixKey), "", "", OPType.MARKER_QA, numMatrixKeys, 999, 23, true));
//		final MarkersKeysSource operationKS = InMemoryMarkersKeysSource.createForOperation(operationKey, null, null);


//		StudyKey studyKey = StudyList.insertNewStudy(new Study("", ""));
//		MatrixKey matrixKey = MatricesList.insertMatrixMetadata(new MatrixMetadata(new MatrixKey(studyKey, MatrixKey.NULL_ID), "", "", ImportFormat.PLINK, "", "", cNetCDF.Defaults.GenotypeEncoding.UNKNOWN, cNetCDF.Defaults.StrandType.UNKNOWN, false, numMatrixKeys, 999, 23, "", new Date()));
//		OperationKey operationKey = OperationsList.insertOperation(new OperationMetadata(new DataSetKey(matrixKey), "", "", cNetCDF.Defaults.OPType.MARKER_QA, numMatrixKeys, 999, 23, true));
//
//		final List<MarkerKey> matrixMarkerKeys
//				= new ArrayList<MarkerKey>(numMatrixKeys);
//		for (int mmi = 0; mmi < numMatrixKeys; mmi++) {
//			matrixMarkerKeys.add(new MarkerKey("mk_" + mmi));
//		}
//		final MarkersKeysSource matrixKS = InMemoryMarkersKeysSource.createForMatrix(matrixKey, matrixMarkerKeys);

		final List<MarkerKey> matrixMarkerKeys = matrixKS;
		final List<Integer> origIndices
				= new ArrayList<Integer>(numOperationKeys);
//		final List<MarkerKey> operationMarkerKeys = null;
		final List<MarkerKey> operationMarkerKeys
				= new ArrayList<MarkerKey>(numOperationKeys);
		for (int omi = 0; omi < numOperationKeys; omi++) {
			final int origIndex = numMatrixKeys / numOperationKeys * omi;
			origIndices.add(origIndex);
			operationMarkerKeys.add(matrixMarkerKeys.get(origIndex));
		}
		final MarkersKeysSource operationKS = InMemoryMarkersKeysSource.createForOperation(operationKey, operationMarkerKeys, origIndices);

		for (MarkerKey matrixK : matrixKS) {
			;
		}

		for (int oki = 0; oki < operationKS.size(); oki++) {
			System.err.println("operationKS.get(" + oki + ") ...");
			operationKS.get(oki);
		}
		for (MarkerKey operationK : operationKS) {
			;
		}

		InMemoryMarkersKeysSource.clearStorage();
	}
}
