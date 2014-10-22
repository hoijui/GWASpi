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
package org.gwaspi.operations.markercensus;

import java.io.File;
import org.gwaspi.constants.cNetCDF.Defaults.OPType;
import org.gwaspi.model.DataSetKey;
import org.gwaspi.model.OperationKey;
import org.gwaspi.operations.AbstractOperationParams;

/**
 * Parameters for the {@link org.gwaspi.operations.OP_MarkerCensus}.
 */
public class MarkerCensusOperationParams extends AbstractOperationParams {

	public static final boolean DEFAULT_DISCARD_MISMATCHES = true;
	public static final double DEFAULT_MARKER_MISSING_RATIO = 0.05;
	public static final double DEFAULT_SAMPLE_MISSING_RATIO = 0.05;
	public static final double DEFAULT_SAMPLE_HETZY_RATIO = 0.5;
	public static final double DISABLE_MARKER_MISSING_RATIO = 1.0;
	public static final double DISABLE_SAMPLE_MISSING_RATIO = 1.0;
	public static final double DISABLE_SAMPLE_HETZY_RATIO = 1.0;
	public static final File DISABLE_PHENOTYPE_FILE = null;

	private OperationKey sampleQAOpKey;
	private double sampleMissingRatio;
	private double sampleHetzygRatio;
	private OperationKey markerQAOpKey;
	private boolean discardMismatches;
	private double markerMissingRatio;
	private File phenotypeFile;

	public MarkerCensusOperationParams(
			final DataSetKey parent,
			final String name,
			final OperationKey sampleQAOpKey,
			final double sampleMissingRatio,
			final double sampleHetzygRatio,
			final OperationKey markerQAOpKey,
			final boolean discardMismatches,
			final double markerMissingRatio,
			final File phenotypeFile)
	{
		super((phenotypeFile == null) ? OPType.MARKER_CENSUS_BY_AFFECTION : OPType.MARKER_CENSUS_BY_PHENOTYPE, parent, name);

		this.sampleQAOpKey = sampleQAOpKey;
		this.sampleMissingRatio = sampleMissingRatio;
		this.sampleHetzygRatio = sampleHetzygRatio;
		this.markerQAOpKey = markerQAOpKey;
		this.discardMismatches = discardMismatches;
		this.markerMissingRatio = markerMissingRatio;
		this.phenotypeFile = phenotypeFile;
	}

	public MarkerCensusOperationParams(
			final DataSetKey parent,
			final OperationKey sampleQAOpKey,
			final OperationKey markerQAOpKey)
	{
		this(
				parent,
				null, // name - the default will be used later on
				sampleQAOpKey,
				DEFAULT_SAMPLE_MISSING_RATIO,
				DISABLE_SAMPLE_HETZY_RATIO,
				markerQAOpKey,
				DEFAULT_DISCARD_MISMATCHES,
				DEFAULT_MARKER_MISSING_RATIO,
				DISABLE_PHENOTYPE_FILE
				);
	}

	@Override
	protected String getNameDefault() {

		String nameDefault = "Marker-Census";
		if (getParent() != null) {
			nameDefault += " for matrix " + getParent().getOrigin().toString(); // TODO use nicer matrix name!
		}
		return nameDefault;
	}

	public OperationKey getSampleQAOpKey() {
		return sampleQAOpKey;
	}

	public void setSampleQAOpKey(OperationKey sampleQAOpKey) {
		this.sampleQAOpKey = sampleQAOpKey;
	}

	public double getSampleMissingRatio() {
		return sampleMissingRatio;
	}

	public void setSampleMissingRatio(double sampleMissingRatio) {
		this.sampleMissingRatio = sampleMissingRatio;
	}

	public double getSampleHetzygRatio() {
		return sampleHetzygRatio;
	}

	public void setSampleHetzygRatio(double sampleHetzygRatio) {
		this.sampleHetzygRatio = sampleHetzygRatio;
	}

	public OperationKey getMarkerQAOpKey() {
		return markerQAOpKey;
	}

	public void setMarkerQAOpKey(OperationKey markerQAOpKey) {
		this.markerQAOpKey = markerQAOpKey;
	}

	public boolean isDiscardMismatches() {
		return discardMismatches;
	}

	public void setDiscardMismatches(boolean discardMismatches) {
		this.discardMismatches = discardMismatches;
	}

	public double getMarkerMissingRatio() {
		return markerMissingRatio;
	}

	public void setMarkerMissingRatio(double markerMissingRatio) {
		this.markerMissingRatio = markerMissingRatio;
	}

	public File getPhenotypeFile() {
		return phenotypeFile;
	}

	public void setPhenotypeFile(File phenotypeFile) {
		this.phenotypeFile = phenotypeFile;
	}
}
