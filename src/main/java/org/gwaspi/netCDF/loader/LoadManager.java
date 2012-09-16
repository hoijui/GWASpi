package org.gwaspi.netCDF.loader;

import org.gwaspi.constants.cImport;
import org.gwaspi.constants.cNetCDF;
import java.io.IOException;
import java.util.Map;
import ucar.ma2.InvalidRangeException;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class LoadManager {

	private LoadManager() {
	}

	public static int dispatchLoadByFormat(String format,
			Map<String, Object> sampleInfoLHM,
			String txt_NewMatrixName,
			String txtA_NewMatrixDescription,
			String txt_File1,
			String txt_FileSampleInfo,
			String txt_File2,
			String chromosome,
			String strandType,
			String gtCode,
			int studyId)
			throws IOException, InvalidRangeException, InterruptedException
	{
		int newMatrixId = Integer.MIN_VALUE;

		GTFilesLoader gtFilesLoader;
		switch (cImport.ImportFormat.compareTo(format)) {
			case Affymetrix_GenomeWide6:
				org.gwaspi.samples.InsertSampleInfo.processData(studyId, sampleInfoLHM);
				gtFilesLoader = new LoadGTFromAffyFiles(txt_File2,
						txt_FileSampleInfo,
						txt_File1,
						studyId,
						format,
						txt_NewMatrixName,
						org.gwaspi.constants.cNetCDF.Defaults.GenotypeEncoding.AB0.toString(),
						txtA_NewMatrixDescription,
						sampleInfoLHM);
				break;
			case PLINK:
				org.gwaspi.samples.InsertSampleInfo.processData(studyId, sampleInfoLHM);
				gtFilesLoader = new LoadGTFromPlinkFlatFiles(txt_File1, //MAP file
						txt_FileSampleInfo, //Sample info file (optional)
						txt_File2, //PED file
						studyId,
						strandType, //Strand
						txt_NewMatrixName,
						gtCode, //Genotype encoding
						txtA_NewMatrixDescription,
						sampleInfoLHM);
				break;
			case PLINK_Binary:
				org.gwaspi.samples.InsertSampleInfo.processData(studyId, sampleInfoLHM);
				gtFilesLoader = new LoadGTFromPlinkBinaryFiles(txt_File1, //BED file
						txt_FileSampleInfo, //FAM or Sample info file (optional)
						txt_File2, //BIM file
						studyId,
						strandType, //Strand
						txt_NewMatrixName,
						gtCode, //Genotype encoding
						txtA_NewMatrixDescription,
						sampleInfoLHM);
				break;
			case HAPMAP:
				org.gwaspi.samples.InsertSampleInfo.processData(studyId, sampleInfoLHM);
				gtFilesLoader = new LoadGTFromHapmapFiles(txt_File1, //Genotypes file or folder
						txt_FileSampleInfo, //Sample info file (optional)
						studyId,
						strandType, //Strand
						txt_NewMatrixName,
						gtCode, //Genotype encoding
						txtA_NewMatrixDescription,
						sampleInfoLHM);
				break;
			case BEAGLE:
				org.gwaspi.samples.InsertSampleInfo.processData(studyId, sampleInfoLHM);
				gtFilesLoader = new LoadGTFromBeagleFiles(txt_File1, //Genotypes file
						txt_FileSampleInfo, //Sample info file (optional)
						txt_File2, //Marker file (markerId, pos, allele1, allele2)
						studyId,
						chromosome,
						strandType, //Strand
						txt_NewMatrixName,
						gtCode, //Genotype encoding
						txtA_NewMatrixDescription,
						sampleInfoLHM);
				break;
			case HGDP1:
				org.gwaspi.samples.InsertSampleInfo.processData(studyId, sampleInfoLHM);
				gtFilesLoader = new LoadGTFromHGDP1Files(txt_File1, //Genotypes file
						txt_FileSampleInfo, //Sample info file (optional)
						txt_File2, //Marker file (markerId, pos, allele1, allele2)
						studyId,
						chromosome,
						strandType, //Strand
						txt_NewMatrixName,
						gtCode, //Genotype encoding
						txtA_NewMatrixDescription,
						sampleInfoLHM);
				break;
			case Illumina_LGEN:
				org.gwaspi.samples.InsertSampleInfo.processData(studyId, sampleInfoLHM);
				gtFilesLoader = new LoadGTFromIlluminaLGENFiles(txt_File1,
						txt_FileSampleInfo,
						txt_File2,
						studyId,
						format,
						txt_NewMatrixName,
						cNetCDF.Defaults.GenotypeEncoding.ACGT0.toString(),
						txtA_NewMatrixDescription,
						sampleInfoLHM);
				break;
			case GWASpi:
				org.gwaspi.samples.InsertSampleInfo.processData(studyId, sampleInfoLHM);
				gtFilesLoader = new LoadGTFromGWASpiFiles(txt_File1, //netCDF GT file
						txt_FileSampleInfo,
						studyId,
						txt_NewMatrixName,
						txtA_NewMatrixDescription,
						sampleInfoLHM);
				break;
			case Sequenom:
				org.gwaspi.samples.InsertSampleInfo.processData(studyId, sampleInfoLHM);
				gtFilesLoader = new LoadGTFromSequenomFiles(txt_File1, //GT File
						txt_FileSampleInfo, //Sample info file (optional)
						txt_File2, //Annotation (MAP) file
						studyId,
						format, //format
						txt_NewMatrixName,
						cNetCDF.Defaults.GenotypeEncoding.ACGT0.toString(), //Genotype encoding
						txtA_NewMatrixDescription,
						sampleInfoLHM);
				break;
			default:
				gtFilesLoader = null;
		}

		if (gtFilesLoader != null) {
			newMatrixId = gtFilesLoader.processData();
		}

		return newMatrixId;
	}
}
