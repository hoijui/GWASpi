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

package org.gwaspi.netCDF.loader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.gwaspi.constants.cImport.ImportFormat;
import org.gwaspi.constants.cNetCDF;
import org.gwaspi.global.Text;
import org.gwaspi.gui.StartGWASpi;
import org.gwaspi.model.ChromosomeInfo;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.model.MarkerMetadata;
import org.gwaspi.model.MatricesList;
import org.gwaspi.model.MatrixKey;
import org.gwaspi.model.MatrixMetadata;
import org.gwaspi.model.SampleInfo;
import org.gwaspi.model.SampleInfoList;
import org.gwaspi.model.SampleKey;
import org.gwaspi.netCDF.matrices.MatrixFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.ma2.ArrayByte;
import ucar.ma2.ArrayChar;
import ucar.ma2.ArrayInt;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFileWriteable;

/**
 * Used when loading a data-set from an external source
 * directly into the NetCDF storage format.
 * For example, from a set of PLink files.
 */
public class LoadingNetCDFDataSetDestination extends AbstractNetCDFDataSetDestination {

	private final Logger log
			= LoggerFactory.getLogger(LoadingNetCDFDataSetDestination.class);

	private String startTime;
	private final GenotypesLoadDescription loadDescription;
	private AbstractLoadGTFromFiles gtLoader; // HACK

	public LoadingNetCDFDataSetDestination(
			GenotypesLoadDescription loadDescription)
	{
		super(loadDescription.getStudyKey());
		this.loadDescription = loadDescription;
		this.gtLoader = null;
	}

	public void setGTLoader(AbstractLoadGTFromFiles gtLoader) {
		this.gtLoader = gtLoader;
	}

	@Override
	public void init() throws Exception {
		super.init();

		startTime = org.gwaspi.global.Utils.getMediumDateTimeAsString();
	}

//	//<editor-fold defaultstate="expanded" desc="PROCESS GENOTYPES">
	@Override
	public void finishedLoadingMarkerMetadatas() throws IOException {
		XXX; see in parent class;
//
//		List<SampleKey> sampleKeys = LoadingNetCDFDataSetDestination.extractKeys(getDataSet().getSampleInfos());
//
//		// CREATE netCDF-3 FILE
//		StringBuilder descSB = new StringBuilder(Text.Matrix.descriptionHeader1);
//		descSB.append(org.gwaspi.global.Utils.getShortDateTimeAsString());
//		if (!loadDescription.getDescription().isEmpty()) {
//			descSB.append("\nDescription: ");
//			descSB.append(loadDescription.getDescription());
//			descSB.append("\n");
//		}
////		descSB.append("\nStrand: ");
////		descSB.append(strand);
////		descSB.append("\nGenotype encoding: ");
////		descSB.append(gtCode);
//		descSB.append("\n");
//		descSB.append("Markers: ").append(getDataSet().getMarkerMetadatas().size());
//		descSB.append(", Samples: ").append(getDataSet().getSampleInfos().size());
//		descSB.append("\n");
//		descSB.append(Text.Matrix.descriptionHeader2);
//		descSB.append(loadDescription.getFormat().toString());
//		descSB.append("\n");
//		descSB.append(Text.Matrix.descriptionHeader3);
//		descSB.append("\n");
//		gtLoader.addAdditionalBigDescriptionProperties(descSB, loadDescription);
//		if (new File(loadDescription.getSampleFilePath()).exists()) {
//			descSB.append(loadDescription.getSampleFilePath()); // the FAM file, in case of PLink Binary
//			descSB.append(" (Sample Info file)\n");
//		}
//
//		// RETRIEVE CHROMOSOMES INFO
//		Map<MarkerKey, ChromosomeInfo> chrSetMap = org.gwaspi.netCDF.matrices.Utils.aggregateChromosomeInfo(getDataSet().getMarkerMetadatas(), 2, 3);
//
//		try {
//			matrixFactory = new MatrixFactory(
//					loadDescription.getStudyKey(),
//					loadDescription.getFormat(),
//					loadDescription.getFriendlyName(),
//					descSB.toString(), // description
//					loadDescription.getGtCode(),
//					(gtLoader.getMatrixStrand() != null) ? gtLoader.getMatrixStrand() : loadDescription.getStrand(),
//					gtLoader.isHasDictionary(),
//					getDataSet().getSampleInfos().size(),
//					getDataSet().getMarkerMetadatas().size(),
//					chrSetMap.size(),
//					loadDescription.getGtDirPath());
//
//			ncfile = matrixFactory.getNetCDFHandler();
//
//			// create the file
//			ncfile.create();
//			log.trace("Done creating netCDF handle: " + ncfile.toString());
//			//</editor-fold>
//
//			//<editor-fold defaultstate="expanded" desc="WRITE MATRIX METADATA">
//			// WRITE SAMPLESET TO MATRIX FROM SAMPLES LIST
//			ArrayChar.D2 samplesD2 = org.gwaspi.netCDF.operations.Utils.writeCollectionToD2ArrayChar(sampleKeys, cNetCDF.Strides.STRIDE_SAMPLE_NAME);
//
//			int[] sampleOrig = new int[] {0, 0};
//			ncfile.write(cNetCDF.Variables.VAR_SAMPLESET, sampleOrig, samplesD2);
//			samplesD2 = null;
//			log.info("Done writing SampleSet to matrix");
//
//			// WRITE RSID & MARKERID METADATA FROM METADATAMap
//			ArrayChar.D2 markersD2 = org.gwaspi.netCDF.operations.Utils.writeValuesToD2ArrayChar(getDataSet().getMarkerMetadatas().values(), MarkerMetadata.TO_RS_ID, cNetCDF.Strides.STRIDE_MARKER_NAME);
//
//			int[] markersOrig = new int[] {0, 0};
//			ncfile.write(cNetCDF.Variables.VAR_MARKERS_RSID, markersOrig, markersD2);
//
//			markersD2 = org.gwaspi.netCDF.operations.Utils.writeValuesToD2ArrayChar(getDataSet().getMarkerMetadatas().values(), MarkerMetadata.TO_MARKER_ID, cNetCDF.Strides.STRIDE_MARKER_NAME);
//			ncfile.write(cNetCDF.Variables.VAR_MARKERSET, markersOrig, markersD2);
//			log.info("Done writing MarkerId and RsId to matrix");
//
//			// WRITE CHROMOSOME METADATA FROM ANNOTATION FILE
//			// Chromosome location for each marker
//			markersD2 = org.gwaspi.netCDF.operations.Utils.writeValuesToD2ArrayChar(getDataSet().getMarkerMetadatas().values(), MarkerMetadata.TO_CHR, cNetCDF.Strides.STRIDE_CHR);
//			ncfile.write(cNetCDF.Variables.VAR_MARKERS_CHR, markersOrig, markersD2);
//			log.info("Done writing chromosomes to matrix");
//
//			// Set of chromosomes found in matrix along with number of markersinfo
//			org.gwaspi.netCDF.operations.Utils.saveObjectsToStringToMatrix(ncfile, chrSetMap.keySet(), cNetCDF.Variables.VAR_CHR_IN_MATRIX, 8);
//
//			// Number of marker per chromosome & max pos for each chromosome
//			int[] columns = new int[] {0, 1, 2, 3};
//			org.gwaspi.netCDF.operations.Utils.saveChromosomeInfosD2ToWrMatrix(ncfile, chrSetMap.values(), columns, cNetCDF.Variables.VAR_CHR_INFO);
//
//
//			// WRITE POSITION METADATA FROM ANNOTATION FILE
//			//markersD2 = org.gwaspi.netCDF.operations.Utils.writeValuesToD2ArrayChar(sortedMarkerSetMap, 3, cNetCDF.Strides.STRIDE_POS);
//			ArrayInt.D1 markersPosD1 = org.gwaspi.netCDF.operations.Utils.writeValuesToD1ArrayInt(getDataSet().getMarkerMetadatas().values(), MarkerMetadata.TO_POS);
//			int[] posOrig = new int[1];
//			ncfile.write(cNetCDF.Variables.VAR_MARKERS_POS, posOrig, markersPosD1);
//			log.info("Done writing positions to matrix");
//
//			// WRITE CUSTOM ALLELES METADATA FROM ANNOTATION FILE
//			if (gtLoader.getMarkersD2Variables() != null) {
//				markersD2 = org.gwaspi.netCDF.operations.Utils.writeValuesToD2ArrayChar(getDataSet().getMarkerMetadatas().values(), gtLoader.getBaseDictPropertyExtractor(), cNetCDF.Strides.STRIDE_GT);
//				ncfile.write(gtLoader.getMarkersD2Variables(), markersOrig, markersD2);
//				log.info("Done writing forward alleles to matrix");
//			}
//
//			// WRITE GT STRAND FROM ANNOTATION FILE
//			int[] gtOrig = new int[] {0, 0};
//			if (gtLoader.isHasStrandInfo()) {
//				// TODO Strand info is buggy in Hapmap bulk download!
//				markersD2 = org.gwaspi.netCDF.operations.Utils.writeValuesToD2ArrayChar(getDataSet().getMarkerMetadatas().values(), MarkerMetadata.TO_STRAND, cNetCDF.Strides.STRIDE_STRAND);
//			} else {
//				String strandFlag = gtLoader.getStrandFlag(loadDescription);
//				markersD2 = org.gwaspi.netCDF.operations.Utils.writeSingleValueToD2ArrayChar(strandFlag, cNetCDF.Strides.STRIDE_STRAND, getDataSet().getMarkerMetadatas().size());
//			}
//
//			ncfile.write(cNetCDF.Variables.VAR_GT_STRAND, gtOrig, markersD2);
//			markersD2 = null;
//			log.info("Done writing strand info to matrix");
//			//</editor-fold>
//		} catch (InvalidRangeException ex) {
//			throw new IOException(ex);
//		}
	}

	@Override
	public void finishedLoadingAlleles() throws Exception {
		super.finishedLoadingAlleles();

		logAsWhole(
				startTime,
				loadDescription.getStudyKey().getId(),
				loadDescription.getGtDirPath(),
				loadDescription.getFormat(),
				loadDescription.getFriendlyName(),
				loadDescription.getDescription());

		org.gwaspi.global.Utils.sysoutCompleted("writing Genotypes to Matrix");
	}

	@Override
	protected String getGuessedGTCode() {
		return gtLoader.getGuessedGTCode().toString();
	}

	private static void logAsWhole(String startTime, int studyId, String dirPath, ImportFormat format, String matrixName, String description) throws IOException {
		// LOG OPERATION IN STUDY HISTORY
		StringBuilder operation = new StringBuilder("\nLoaded raw " + format + " genotype data in path " + dirPath + ".\n");
		operation.append("Start Time: ").append(startTime).append("\n");
		operation.append("End Time: ").append(org.gwaspi.global.Utils.getMediumDateTimeAsString()).append(".\n");
		operation.append("Data stored in matrix ").append(matrixName).append(".\n");
		operation.append("Description: ").append(description).append(".\n");
		org.gwaspi.global.Utils.logOperationInStudyDesc(operation.toString(), studyId);
	}

	static List<SampleKey> extractKeys(Collection<SampleInfo> sampleInfos) {

		List<SampleKey> sampleKeys = new ArrayList<SampleKey>(sampleInfos.size());

		for (SampleInfo sampleInfo : sampleInfos) {
			sampleKeys.add(sampleInfo.getKey());
		}

		return sampleKeys;
	}
}
