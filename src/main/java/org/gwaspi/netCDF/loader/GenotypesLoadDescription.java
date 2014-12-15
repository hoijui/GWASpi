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

import org.gwaspi.constants.ImportConstants.ImportFormat;
import org.gwaspi.constants.NetCDFConstants.Defaults.GenotypeEncoding;
import org.gwaspi.constants.NetCDFConstants.Defaults.StrandType;
import org.gwaspi.model.StudyKey;

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
	private final StudyKey studyKey;
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
			StudyKey studyKey,
			ImportFormat format,
			String friendlyName,
			String description,
			String chromosome, // NOTE this is only used for the Beagle format
			StrandType strand,
			GenotypeEncoding gtCode)
	{
		this.gtDirPath = gtDirPath;
		this.sampleFilePath = sampleFilePath;
		this.annotationFilePath = annotationFilePath;
		this.studyKey = studyKey;
		this.format = format;
		this.friendlyName = friendlyName;
		this.description = description;
		if (format == ImportFormat.Affymetrix_GenomeWide6) {
			this.gtCode = GenotypeEncoding.AB0;
		} else if (format == ImportFormat.Illumina_LGEN) {
			this.gtCode = GenotypeEncoding.ACGT0;
		} else if (format == ImportFormat.Sequenom) {
			this.gtCode = GenotypeEncoding.ACGT0;
		} else {
			this.gtCode = gtCode; // NOTE this will usually be GenotypeEncoding.UNKNOWN
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

	public StudyKey getStudyKey() {
		return studyKey;
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

	/**
	 * Returns the strand flag that applies to all markers of the data set.
	 * This value only makes sense if the format does not supply
	 * a per marker strand.
	 * @see MetadataLoader#isHasStrandInfo
	 */
	public String getStrandFlag() {

		String strandFlag;

		StrandType localStrand = getStrand();
		// NOTE This if statement may not make sense,
		//   it was only introduced to lead to exactly
		//   the same strand-flag as the old code
		if (localStrand == StrandType.FWDREV) {
			localStrand = StrandType.UNKNOWN;
		}
		strandFlag = localStrand.toString();

		return strandFlag;
	}

	public String getChromosome() {
		return chromosome;
	}
}
