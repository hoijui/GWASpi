/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gwaspi.netCDF.loader;

import org.gwaspi.constants.cNetCDF;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedHashMap;
import ucar.ma2.InvalidRangeException;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class LoadManager {

	public static int dispatchLoadByFormat(String format,
			LinkedHashMap sampleInfoLHM,
			String txt_NewMatrixName,
			String txtA_NewMatrixDescription,
			String txt_File1,
			String txt_FileSampleInfo,
			String txt_File2,
			String chromosome,
			String strandType,
			String gtCode,
			int studyId) throws FileNotFoundException, IOException, InvalidRangeException, InterruptedException, NullPointerException {
		int newMatrixId = Integer.MIN_VALUE;

		switch (org.gwaspi.constants.cImport.ImportFormat.compareTo(format)) {
			case Affymetrix_GenomeWide6:
				org.gwaspi.samples.InsertSampleInfo.processData(studyId, sampleInfoLHM);
				LoadGTFromAffyFiles affyGTLoader = new LoadGTFromAffyFiles(txt_File2,
						txt_FileSampleInfo,
						txt_File1,
						studyId,
						format,
						txt_NewMatrixName,
						org.gwaspi.constants.cNetCDF.Defaults.GenotypeEncoding.AB0.toString(),
						txtA_NewMatrixDescription,
						sampleInfoLHM);

				newMatrixId = affyGTLoader.processData();
				break;
			case PLINK:
				org.gwaspi.samples.InsertSampleInfo.processData(studyId, sampleInfoLHM);
				LoadGTFromPlinkFlatFiles plinkFlatGTLoader = new LoadGTFromPlinkFlatFiles(txt_File1, //MAP file
						txt_FileSampleInfo, //Sample info file (optional)
						txt_File2, //PED file
						studyId,
						strandType, //Strand
						txt_NewMatrixName,
						gtCode, //Genotype encoding
						txtA_NewMatrixDescription,
						sampleInfoLHM);

				newMatrixId = plinkFlatGTLoader.processData();
				break;
			case PLINK_Binary:
				org.gwaspi.samples.InsertSampleInfo.processData(studyId, sampleInfoLHM);
				LoadGTFromPlinkBinaryFiles plinkBinaryGTLoader = new LoadGTFromPlinkBinaryFiles(txt_File1, //BED file
						txt_FileSampleInfo, //FAM or Sample info file (optional)
						txt_File2, //BIM file
						studyId,
						strandType, //Strand
						txt_NewMatrixName,
						gtCode, //Genotype encoding
						txtA_NewMatrixDescription,
						sampleInfoLHM);

				newMatrixId = plinkBinaryGTLoader.processData();
				break;
			case HAPMAP:
				org.gwaspi.samples.InsertSampleInfo.processData(studyId, sampleInfoLHM);
				LoadGTFromHapmapFiles hapmapGTLoader = new LoadGTFromHapmapFiles(txt_File1, //Genotypes file or folder
						txt_FileSampleInfo, //Sample info file (optional)
						studyId,
						strandType, //Strand
						txt_NewMatrixName,
						gtCode, //Genotype encoding
						txtA_NewMatrixDescription,
						sampleInfoLHM);

				//newMatrixId = hapmapGTLoader.processHapmapGTFile();
				newMatrixId = hapmapGTLoader.processHapmapGTFiles();
				break;
			case BEAGLE:
				org.gwaspi.samples.InsertSampleInfo.processData(studyId, sampleInfoLHM);
				LoadGTFromBeagleFiles beagleGTLoader = new LoadGTFromBeagleFiles(txt_File1, //Genotypes file
						txt_FileSampleInfo, //Sample info file (optional)
						txt_File2, //Marker file (markerId, pos, allele1, allele2)
						studyId,
						chromosome,
						strandType, //Strand
						txt_NewMatrixName,
						gtCode, //Genotype encoding
						txtA_NewMatrixDescription,
						sampleInfoLHM);

				newMatrixId = beagleGTLoader.processBeagleGTFiles();
				break;
			case HGDP1:
				org.gwaspi.samples.InsertSampleInfo.processData(studyId, sampleInfoLHM);
				LoadGTFromHGDP1Files hgdpGTLoader = new LoadGTFromHGDP1Files(txt_File1, //Genotypes file
						txt_FileSampleInfo, //Sample info file (optional)
						txt_File2, //Marker file (markerId, pos, allele1, allele2)
						studyId,
						chromosome,
						strandType, //Strand
						txt_NewMatrixName,
						gtCode, //Genotype encoding
						txtA_NewMatrixDescription,
						sampleInfoLHM);

				newMatrixId = hgdpGTLoader.processHGDP1GTFiles();
				break;
			case Illumina_LGEN:
				org.gwaspi.samples.InsertSampleInfo.processData(studyId, sampleInfoLHM);
				LoadGTFromIlluminaLGENFiles illuminaLgenGTLoader = new LoadGTFromIlluminaLGENFiles(txt_File1,
						txt_FileSampleInfo,
						txt_File2,
						studyId,
						format,
						txt_NewMatrixName,
						cNetCDF.Defaults.GenotypeEncoding.ACGT0.toString(),
						txtA_NewMatrixDescription,
						sampleInfoLHM);
				newMatrixId = illuminaLgenGTLoader.processData();
				break;
			case GWASpi:
				org.gwaspi.samples.InsertSampleInfo.processData(studyId, sampleInfoLHM);
				LoadGTFromGWASpiFiles gwaspiGTLoader = new LoadGTFromGWASpiFiles(txt_File1, //netCDF GT file
						txt_FileSampleInfo,
						studyId,
						txt_NewMatrixName,
						txtA_NewMatrixDescription,
						sampleInfoLHM);

				newMatrixId = gwaspiGTLoader.processGWASpiGTFiles();
				break;
			case Sequenom:
				org.gwaspi.samples.InsertSampleInfo.processData(studyId, sampleInfoLHM);
				LoadGTFromSequenomFiles sequenomGTLoader = new LoadGTFromSequenomFiles(txt_File1, //GT File
						txt_FileSampleInfo, //Sample info file (optional)
						txt_File2, //Annotation (MAP) file
						studyId,
						format, //format
						txt_NewMatrixName,
						cNetCDF.Defaults.GenotypeEncoding.ACGT0.toString(), //Genotype encoding
						txtA_NewMatrixDescription,
						sampleInfoLHM);

				newMatrixId = sequenomGTLoader.processData();
				break;
			default:

		}

		return newMatrixId;
	}
}
