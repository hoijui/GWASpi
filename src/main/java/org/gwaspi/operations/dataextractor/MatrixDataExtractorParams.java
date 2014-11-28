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

package org.gwaspi.operations.dataextractor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.gwaspi.constants.DBSamplesConstants;
import org.gwaspi.constants.cNetCDF.Defaults.SetMarkerPickCase;
import org.gwaspi.constants.cNetCDF.Defaults.SetSamplePickCase;
import org.gwaspi.model.DataSetKey;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.model.StudyKey;
import org.gwaspi.operations.AbstractMatrixCreatingOperationParams;

public class MatrixDataExtractorParams extends AbstractMatrixCreatingOperationParams {

	private final File sampleCriteriaFile;
	private final SetSamplePickCase samplePickCase;
	private final String samplePickerVar;
	private final int sampleFilterPos;
	private final Set sampleCriteria;
	/**
	 * All the criteria to pick samples, including the directly supplied ones,
	 * and the ones read from the sample criteria file.
	 */
	private Set fullSampleCriteria;

	private final File markerCriteriaFile;
	private final SetMarkerPickCase markerPickCase;
	private final String markerPickerVar;
	private final Set markerCriteria;
	/**
	 * All the criteria to pick markers, including the directly supplied ones,
	 * and the ones read from the marker criteria file.
	 */
	private Set fullMarkerCriteria;

	public MatrixDataExtractorParams(
			DataSetKey parent,
			String matrixDescription,
			String matrixFriendlyName,
			File markerCriteriaFile,
			File sampleCriteriaFile,
			SetMarkerPickCase markerPickCase,
			String markerPickerVar,
			SetSamplePickCase samplePickCase,
			String samplePickerVar,
			int sampleFilterPos,
			Set<Object> markerCriteria,
			Set<Object> sampleCriteria)
	{
		super(parent, matrixDescription, matrixFriendlyName);

		this.markerCriteriaFile = markerCriteriaFile;
		this.sampleCriteriaFile = sampleCriteriaFile;
		this.markerPickCase = markerPickCase;
		this.markerPickerVar = markerPickerVar;
		this.markerCriteria = markerCriteria;
		this.samplePickCase = samplePickCase;
		this.samplePickerVar = samplePickerVar;
		this.sampleFilterPos = sampleFilterPos;
		this.sampleCriteria = sampleCriteria;
	}

	public Set<?> getFullMarkerCriteria() throws IOException {

		if (fullMarkerCriteria == null) {
			Set tmpFullMarkerCriteria = new HashSet();
			tmpFullMarkerCriteria.addAll(markerCriteria);
			// Pick markerId by criteria file
			tmpFullMarkerCriteria.addAll(parseMarkerPickerFile(markerCriteriaFile, markerPickCase));
			fullMarkerCriteria = tmpFullMarkerCriteria;
		}

		return fullMarkerCriteria;
	}

	private static List<?> parseMarkerPickerFile(File markerPickerFile, SetMarkerPickCase markerPickCase) throws IOException {

		List<?> markerCriteria = new LinkedList();

		// Pick markerId by criteria file
		if (!markerPickerFile.toString().isEmpty() && markerPickerFile.isFile()) {
			FileReader fr = new FileReader(markerPickerFile);
			BufferedReader br = new BufferedReader(fr);
			String l;
			while ((l = br.readLine()) != null) {
				if ((markerPickCase == SetMarkerPickCase.MARKERS_INCLUDE_BY_ID)
						|| (markerPickCase == SetMarkerPickCase.MARKERS_EXCLUDE_BY_ID))
				{
					((Collection<MarkerKey>) markerCriteria).add(MarkerKey.valueOf(l));
				} else {
					// markerPickerVar is one of:
					// - marker Chromosome  (cNetCDF.Variables.VAR_MARKERS_CHR)
					// - marker ID          (cNetCDF.Variables.VAR_MARKERSET)
					// - marker RS-ID       (cNetCDF.Variables.VAR_MARKERS_RSID)
					// which are all String types, and thus we can
					// always use char[] here
					((Collection<char[]>) markerCriteria).add(l.toCharArray());
				}
			}
			br.close();
		}

		return markerCriteria;
	}

	private static List<?> parseSamplePickerFile(File samplePickerFile, SetSamplePickCase samplePickCase, String samplePickerVar, StudyKey studyKey) throws IOException {

		List<?> sampleCriteria = new LinkedList();

		// USE cNetCDF Key and criteria or list file
		if (!samplePickerFile.toString().isEmpty() && samplePickerFile.isFile()) {
			FileReader fr = new FileReader(samplePickerFile);
			BufferedReader br = new BufferedReader(fr);
			String l;
			while ((l = br.readLine()) != null) {
//				if ((samplePickCase == SetSamplePickCase.SAMPLES_INCLUDE_BY_ID)
//						|| (samplePickCase == SetSamplePickCase.SAMPLES_EXCLUDE_BY_ID))
//				{
//					((Set<SampleKey>) sampleCriteria).add(SampleKey.valueOf(studyKey, l));
//				} else {
					// samplePickerVar is one of:
					// - sample affection   (cDBSamples.f_AFFECTION)
					// - sample age         (cDBSamples.f_AGE)
					// - sample category    (cDBSamples.f_CATEGORY)
					// - sample disease     (cDBSamples.f_DISEASE)
					// - sample family ID   (cDBSamples.f_FAMILY_ID)
					// - sample population  (DBSamples.f_POPULATION)
					// - sample ID          (cDBSamples.f_SAMPLE_ID)
					// - sample sex         (cDBSamples.f_SEX)
					// which use different types (String, Sex, Affection, int),
					// and thus we have to support multiple types here
					((Collection<Object>) sampleCriteria).add(DBSamplesConstants.parseFromField(samplePickerVar, l));
//				}
			}
			br.close();
		}

		return sampleCriteria;
	}

	public Set<?> getFullSampleCriteria() throws IOException {

		if (fullSampleCriteria == null) {
			Set tmpFullSampleCriteria = new HashSet();
			tmpFullSampleCriteria.addAll(sampleCriteria);
			tmpFullSampleCriteria.addAll(parseSamplePickerFile(sampleCriteriaFile, samplePickCase, samplePickerVar, getParent().getOrigin().getStudyKey()));
			fullSampleCriteria = tmpFullSampleCriteria;
		}

		return fullSampleCriteria;
	}

	public File getMarkerCriteriaFile() {
		return markerCriteriaFile;
	}

	public File getSampleCriteriaFile() {
		return sampleCriteriaFile;
	}

	public SetMarkerPickCase getMarkerPickCase() {
		return markerPickCase;
	}

	public String getMarkerPickerVar() {
		return markerPickerVar;
	}

	public SetSamplePickCase getSamplePickCase() {
		return samplePickCase;
	}

	public String getSamplePickerVar() {
		return samplePickerVar;
	}

	public int getSampleFilterPos() {
		return sampleFilterPos;
	}
}
