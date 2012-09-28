package org.gwaspi.netCDF.loader;

import org.gwaspi.constants.cImport.ImportFormat;
import org.gwaspi.constants.cNetCDF.Defaults.GenotypeEncoding;
import org.gwaspi.constants.cNetCDF.Defaults.StrandType;

public class GenotypesLoadDescription {

	/**
	 * The main genotypes file or directory.
	 * The MAP file in the case of format PLINK.
	 * The BED file in the case of format PLINK_Binary.
	 * The netCDF genotypes file in the case of format GWASpi.
	 */
	private final String gtDirPath;
	/**
	 * The sample info file.
	 * The FAM file (optional) in the case of format PLINK_Binary.
	 */
	private final String sampleFilePath;
	/**
	 * The annotation file.
	 * The PED file in the case of format PLINK.
	 * The BIM file in the case of format PLINK_Binary.
	 * The marker file (markerId, pos, allele1, allele2) in the case of
	 * formats BEAGLE and HGDP1.
	 */
	private final String annotationFilePath;
	private final int studyId;
	private final ImportFormat format;
	private final String friendlyName;
	private final String description;
	private final GenotypeEncoding gtCode;
	private final StrandType strand;
	private final String chromosome;

	public GenotypesLoadDescription(
			String gtDirPath,
			String sampleFilePath,
			String annotationFilePath,
			int studyId,
			ImportFormat format,
			String friendlyName,
			String description,
			String chromosome,
			StrandType strand,
			GenotypeEncoding gtCode)
	{
		this.gtDirPath = gtDirPath;
		this.sampleFilePath = sampleFilePath;
		this.annotationFilePath = annotationFilePath;
		this.studyId = studyId;
		this.format = format;
		this.friendlyName = friendlyName;
		this.description = description;
		if (format == ImportFormat.Affymetrix_GenomeWide6) { // HACK
			this.gtCode = GenotypeEncoding.AB0;
		} else if (format == ImportFormat.Illumina_LGEN) { // HACK
			this.gtCode = GenotypeEncoding.ACGT0;
		} else if (format == ImportFormat.Sequenom) { // HACK
			this.gtCode = GenotypeEncoding.ACGT0;
		} else {
			this.gtCode = gtCode;
		}
		this.strand = strand;
		this.chromosome = chromosome;
	}

	public String getGtDirPath() {
		return gtDirPath;
	}

	public String getSampleFilePath() {
		return sampleFilePath;
	}

	public String getAnnotationFilePath() {
		return annotationFilePath;
	}

	public int getStudyId() {
		return studyId;
	}

	public ImportFormat getFormat() {
		return format;
	}

	public String getFriendlyName() {
		return friendlyName;
	}

	public String getDescription() {
		return description;
	}

	public GenotypeEncoding getGtCode() {
		return gtCode;
	}

	public StrandType getStrand() {
		return strand;
	}

	public String getChromosome() {
		return chromosome;
	}
}
