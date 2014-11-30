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

package org.gwaspi.netCDF.exporter;

import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.gwaspi.constants.ExportConstants;
import org.gwaspi.constants.NetCDFConstants.Defaults.OPType;
import org.gwaspi.model.DataSetKey;
import org.gwaspi.model.DataSetMetadata;
import org.gwaspi.model.DataSetSource;
import org.gwaspi.model.GenotypesList;
import org.gwaspi.model.MarkerMetadata;
import org.gwaspi.model.OperationKey;
import org.gwaspi.model.OperationMetadata;
import org.gwaspi.model.OperationsList;
import org.gwaspi.model.SampleInfo;
import static org.gwaspi.netCDF.exporter.Formatter.PLACEHOLDER_PS_EXPORT;
import org.gwaspi.operations.OperationManager;
import org.gwaspi.operations.qamarkers.QAMarkersOperationDataSet;
import org.gwaspi.progress.IntegerProgressHandler;
import org.gwaspi.progress.ProcessInfo;
import org.gwaspi.progress.ProcessStatus;
import org.gwaspi.progress.SubProcessInfo;
import org.gwaspi.progress.SuperProgressSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlinkBinaryFormatter implements Formatter {

	private final Logger log = LoggerFactory.getLogger(PlinkBinaryFormatter.class);

	/**
	 * If true, then we write the Eigensoft format, else PLink Binary.
	 */
	private final boolean eigensoft;

	/**
	 * Creates a new PLink Binary or Eigensoft formatter.
	 * @param eigensoft if true, then we write the Eigensoft format,
	 *   else PLink Binary.
	 */
	public PlinkBinaryFormatter(boolean eigensoft) {
		this.eigensoft = eigensoft;
	}

	/**
	 * Creates a new PLink Binary formatter.
	 */
	public PlinkBinaryFormatter() {
		this(false);
	}

	@Override
	public void export(
			String exportPath,
			DataSetMetadata rdDataSetMetadata,
			DataSetSource dataSetSource,
			final SuperProgressSource superProgressSource,
			String phenotype)
			throws IOException
	{
		final ProcessInfo exportPI = new SubProcessInfo(
				superProgressSource.getInfo(),
				"export",
				"export in the PLink-Binary format");
		final ProcessInfo exportSamplesPI = new SubProcessInfo(
				exportPI,
				"export samples",
				"format and export sample infos");
		final ProcessInfo exportGenotypesPI = new SubProcessInfo(
				exportPI,
				"export genotypes",
				"format and export genotypes");
		final ProcessInfo exportMarkersPI = new SubProcessInfo(
				exportPI,
				"export markers",
				"export marker infos");

		final SuperProgressSource exportPS = new SuperProgressSource(exportPI);
		superProgressSource.replaceSubProgressSource(PLACEHOLDER_PS_EXPORT, exportPS, null);
		exportPS.setNewStatus(ProcessStatus.INITIALIZING);

		final IntegerProgressHandler exportMarkersPS = new IntegerProgressHandler(exportMarkersPI, 0, dataSetSource.getNumMarkers()- 1);
		exportPS.addSubProgressSource(exportMarkersPS, 0.08);

		final IntegerProgressHandler exportGenotypesPS = new IntegerProgressHandler(exportGenotypesPI, 0, dataSetSource.getNumMarkers()- 1);
		exportPS.addSubProgressSource(exportGenotypesPS, 0.9);

		final IntegerProgressHandler exportSamplesPS = new IntegerProgressHandler(exportSamplesPI, 0, dataSetSource.getNumSamples() - 1);
		exportPS.addSubProgressSource(exportSamplesPS, 0.02);

		final File exportDir = Utils.checkDirPath(exportPath);

		String sep = ExportConstants.separator_PLINK;

		final DataSetKey dataSetKey = rdDataSetMetadata.getDataSetKey();

		// ALLELES
		final List<OperationMetadata> operations = OperationsList.getOffspringOperationsMetadata(dataSetKey);
		OperationKey markersQAOpKey = OperationsList.getIdOfLastOperationTypeOccurance(operations, OPType.MARKER_QA, rdDataSetMetadata.getNumMarkers());

		QAMarkersOperationDataSet qaMarkersOpDS = (QAMarkersOperationDataSet) OperationManager.generateOperationDataSet(markersQAOpKey);

		Collection<MarkerMetadata> markersMetadata = dataSetSource.getMarkersMetadatasSource();
		// NOTE We can use these from the Markers-QA-Operation directly,
		//   assuming the same indices as with the parent matrix,
		//   because the Markers-QA-Operation uses all markers
		//   of the parent matrix.
		final List<Byte> minorAlleles = qaMarkersOpDS.getKnownMinorAllele(-1, -1);
		final List<Double> minorAllelesFrequencies = qaMarkersOpDS.getKnownMinorAlleleFrequencies(-1, -1);
		final List<Byte> majorAlleles = qaMarkersOpDS.getKnownMajorAllele(-1, -1);

		//<editor-fold defaultstate="expanded" desc="BIM FILE">
		exportMarkersPS.setNewStatus(ProcessStatus.INITIALIZING);
		File bimFile = new File(exportDir.getPath(),
				rdDataSetMetadata.getFriendlyName() + ".bim");
		BufferedWriter bimBW = null;
		try {
			FileWriter mapFW = new FileWriter(bimFile);
			bimBW = new BufferedWriter(mapFW);

			// BIM files
			//     chromosome (1-22, X(23), Y(24), XY(25), MT(26) or 0 if unplaced)
			//     rs# or snp identifier
			//     Genetic distance (morgans)
			//     Base-pair position (bp units)
			//     Allele 1
			//     Allele 2

			int markerIndex = 0;
			Iterator<Double> minorAllelesFrequenciesIt = minorAllelesFrequencies.iterator();
			exportMarkersPS.setNewStatus(ProcessStatus.RUNNING);
			for (MarkerMetadata curMarkerMetadata : markersMetadata) {
				bimBW.write(curMarkerMetadata.getChr());
				bimBW.write(sep);
				bimBW.write(curMarkerMetadata.getRsId()); // XXX Maybe has to be marker-id instead?
				bimBW.write(sep);
				bimBW.write('0'); // DEFAULT GENETIC DISTANCE
				bimBW.write(sep);
				bimBW.write(Integer.toString(curMarkerMetadata.getPos())); // NOTE This conversion is required, because Writer#write(int) actually writes a char, not the int value.

				Byte minorAllele = minorAlleles.get(markerIndex);
				Byte majorAllele = majorAlleles.get(markerIndex);
				Double minAlleleFreq = minorAllelesFrequenciesIt.next();
				if ((minAlleleFreq == 0.5) && (majorAllele.compareTo(majorAllele) > 0)) {
					// if both alleles are equally common, use alphabetic order
					Byte tmpMinorAllele = majorAllele;
					majorAllele = minorAllele;
					minorAllele = tmpMinorAllele;

					majorAlleles.set(markerIndex, majorAllele);
					minorAlleles.set(markerIndex, minorAllele);
				}

				bimBW.write(sep);
				bimBW.write((char) (byte) minorAllele);
				bimBW.write(sep);
				bimBW.write((char) (byte) majorAllele);

				bimBW.write('\n');
				exportMarkersPS.setProgress(markerIndex);
				markerIndex++;
			}
			exportMarkersPS.setNewStatus(ProcessStatus.FINALIZING);
			org.gwaspi.global.Utils.sysoutCompleted("Exporting BIM file to " + bimFile.getAbsolutePath());
		} finally {
			if (bimBW != null) {
				bimBW.close();
			}
		}
		exportMarkersPS.setNewStatus(ProcessStatus.COMPLEETED);
		//</editor-fold>

		//<editor-fold defaultstate="expanded" desc="BED FILE">
		exportGenotypesPS.setNewStatus(ProcessStatus.INITIALIZING);
		// THIS SHOULD BE MULTIPLE OF SAMPLE SET LENGTH
		int nbOfSamples = dataSetSource.getSamplesKeysSource().size();
		int bytesPerSampleSet = ((int) Math.ceil((double) nbOfSamples / 8)) * 2;
		int nbOfMarkers = dataSetSource.getMarkersKeysSource().size();
		int nbRowsPerChunk = Math.round((float) org.gwaspi.gui.StartGWASpi.maxProcessMarkers / nbOfSamples);
		if (nbRowsPerChunk > nbOfMarkers) {
			nbRowsPerChunk = nbOfMarkers;
		}
		int byteChunkSize = bytesPerSampleSet * nbRowsPerChunk;

		// Create an output stream to the file.
		File bedFile = new File(exportDir.getPath(),
				rdDataSetMetadata.getFriendlyName() + ".bed");
		DataOutputStream bedBW = null;
		try {
			FileOutputStream bedFW = new FileOutputStream(bedFile);
			bedBW = new DataOutputStream(bedFW);

//	          |----magic number---|  |---mode--|  |----------genotype data-----------|
//	          01101100 00011011   00000001   11011100 00001111 11100111
//	                                              (SNP-major)

			bedBW.writeByte(108); // TODO document magic number
			bedBW.writeByte(27); // TODO document magic number
			bedBW.writeByte(1); // mode SNP-major

			int byteCount = 0;
			byte[] wrBytes = new byte[byteChunkSize];
			// ITERATE THROUGH ALL MARKERS, ONE SAMPLESET AT A TIME
			Iterator<GenotypesList> markersGenotypesIt = dataSetSource.getMarkersGenotypesSource().iterator();
			exportGenotypesPS.setNewStatus(ProcessStatus.RUNNING);
			for (int markerIndex = 0; markerIndex < nbOfMarkers; markerIndex++) {
				byte tmpMinorAllele = minorAlleles.get(markerIndex);
				byte tmpMajorAllele = majorAlleles.get(markerIndex);

				for (Iterator<byte[]> rdSampleGts = markersGenotypesIt.next().iterator(); rdSampleGts.hasNext();) {
					// ONE BYTE AT A TIME (4 SAMPLES)
					StringBuilder tetraGTs = new StringBuilder("");
					for (int i = 0; i < 4; i++) {
						if (rdSampleGts.hasNext()) {
							byte[] tempGT = rdSampleGts.next();
							byte[] translatedByte = translateTo00011011Byte(tempGT, tmpMinorAllele, tmpMajorAllele);
							tetraGTs.insert(0, translatedByte[0]); // REVERSE ORDER, AS PER PLINK SPECS http://pngu.mgh.harvard.edu/~purcell/plink/binary.shtml
							tetraGTs.insert(0, translatedByte[1]);
						}
					}

					int number = Integer.parseInt(tetraGTs.toString(), 2);
					byte[] tetraGT = new byte[] {(byte) number};

					System.arraycopy(
							tetraGT,
							0,
							wrBytes,
							byteCount,
							1);
					byteCount++;

					if (byteCount == byteChunkSize) {
						// WRITE TO FILE
						bedBW.write(wrBytes, 0, byteChunkSize);

						// INIT NEW CHUNK
						wrBytes = new byte[byteChunkSize];
						byteCount = 0;
					}
				}
				exportGenotypesPS.setProgress(markerIndex);
			}
			exportGenotypesPS.setNewStatus(ProcessStatus.FINALIZING);

			// WRITE LAST BITES TO FILE
			bedBW.write(wrBytes, 0, byteCount);

			org.gwaspi.global.Utils.sysoutCompleted("Exporting BED file to " + bedFile.getAbsolutePath());
		} finally {
			if (bedBW != null) {
				bedBW.close();
			}
		}
		exportGenotypesPS.setNewStatus(ProcessStatus.COMPLEETED);
		//</editor-fold>

		//<editor-fold defaultstate="expanded" desc="FAM FILE">
		exportSamplesPS.setNewStatus(ProcessStatus.INITIALIZING);
		File famFile = new File(exportDir.getPath(),
				rdDataSetMetadata.getFriendlyName() + ".fam");
		BufferedWriter tfamBW = null;
		try {
			FileWriter tfamFW = new FileWriter(famFile);
			tfamBW = new BufferedWriter(tfamFW);

			// Iterate through all samples
			int sampleNb = 0;
			exportSamplesPS.setNewStatus(ProcessStatus.RUNNING);
			for (SampleInfo sampleInfo : dataSetSource.getSamplesInfosSource()) {
				sampleInfo = org.gwaspi.netCDF.exporter.Utils.formatSampleInfo(sampleInfo);

				String familyId = sampleInfo.getFamilyId();
				String fatherId = sampleInfo.getFatherId();
				String motherId = sampleInfo.getMotherId();
				String sex = sampleInfo.getSexStr();
				String affectionOrExpPhenotype;
				if (eigensoft) {
					affectionOrExpPhenotype = sampleInfo.getField(phenotype).toString();
				} else {
					affectionOrExpPhenotype = sampleInfo.getAffectionStr();
				}

				// FAM files
				// Family ID
				// Individual ID
				// Paternal ID
				// Maternal ID
				// Sex (1=male; 2=female; other=unknown)
				// Affection (PLink Binary) / Phenotype (Eigensoft)

				tfamBW.append(familyId);
				tfamBW.append(sep);
				tfamBW.append(sampleInfo.getSampleId());
				tfamBW.append(sep);
				tfamBW.append(fatherId);
				tfamBW.append(sep);
				tfamBW.append(motherId);
				tfamBW.append(sep);
				tfamBW.append(sex);
				tfamBW.append(sep);
				tfamBW.append(affectionOrExpPhenotype);

				tfamBW.append("\n");
				tfamBW.flush();

				exportSamplesPS.setProgress(sampleNb);
				sampleNb++;
			}
			exportSamplesPS.setNewStatus(ProcessStatus.FINALIZING);
			org.gwaspi.global.Utils.sysoutCompleted("Exporting FAM file to " + famFile.getAbsolutePath());
		} finally {
			if (tfamBW != null) {
				tfamBW.close();
			}
		}
		exportSamplesPS.setNewStatus(ProcessStatus.COMPLEETED);
		//</editor-fold>
		exportPS.setNewStatus(ProcessStatus.COMPLEETED);
	}

	private static byte[] translateTo00011011Byte(byte[] tempGT, byte tmpMinorAllele, byte tmpMajorAllele) {

		byte[] result;

		if (tempGT[0] == 48
				|| tempGT[1] == 48) {
			// SOME MISSING ALLELES => SET ALL TO MISSING
			result = new byte[]{1, 0};
		} else {
			if (tempGT[0] == tmpMinorAllele) {
				if (tempGT[1] == tmpMinorAllele) {
					// HOMOZYGOUS FOR MINOR ALLELE
					result = new byte[] {0, 0};
				} else {
					// HETEROZYGOUS
					result = new byte[] {0, 1};
				}
			} else {
				if (tempGT[1] == tmpMajorAllele) {
					// HOMOZYGOUS FOR MAJOR ALLELE
					result = new byte[] {1, 1};
				} else {
					// HETEROZYGOUS
					result = new byte[] {0, 1};
				}
			}
		}

		return result;
	}
}
