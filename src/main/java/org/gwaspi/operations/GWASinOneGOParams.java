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

package org.gwaspi.operations;

import org.gwaspi.constants.NetCDFConstants.Defaults.GenotypeEncoding;
import org.gwaspi.constants.NetCDFConstants.Defaults.StrandType;
import org.gwaspi.operations.markercensus.MarkerCensusOperationParams;

public class GWASinOneGOParams {

	private boolean proceed = false;
	private boolean performAllelicTests = true;
	private boolean performGenotypicTests = true;
	private boolean performTrendTests = true;

	private boolean discardMarkerHWCalc = true;
	private boolean discardMarkerHWFree = false;
	private double discardMarkerHWTreshold = 0;

//	private boolean discardGTMismatches = true;
//	private boolean discardMarkerByMisRat = true;
//	private double discardMarkerMisRatVal = 0;
//	private boolean discardMarkerByHetzyRat = false;
//	private double discardMarkerHetzyRatVal = 0;
//	private boolean discardSampleByMisRat = true;
//	private double discardSampleMisRatVal = 0;
//	private boolean discardSampleByHetzyRat = false;
//	private double discardSampleHetzyRatVal = 0;
	private MarkerCensusOperationParams markerCensusOperationParams;


	private String chromosome = "";
	private StrandType strandType = StrandType.UNKNOWN;
	private GenotypeEncoding gtCode = GenotypeEncoding.UNKNOWN;
	private String hardyWeinbergOperationName = null;

	public boolean isProceed() {
		return proceed;
	}

	public void setProceed(boolean proceed) {
		this.proceed = proceed;
	}

	public boolean isPerformAllelicTests() {
		return performAllelicTests;
	}

	public void setPerformAllelicTests(boolean performAllelicTests) {
		this.performAllelicTests = performAllelicTests;
	}

	public boolean isPerformGenotypicTests() {
		return performGenotypicTests;
	}

	public void setPerformGenotypicTests(boolean performGenotypicTests) {
		this.performGenotypicTests = performGenotypicTests;
	}

	public boolean isPerformTrendTests() {
		return performTrendTests;
	}

	public void setPerformTrendTests(boolean performTrendTests) {
		this.performTrendTests = performTrendTests;
	}

	public boolean isDiscardMarkerHWCalc() {
		return discardMarkerHWCalc;
	}

	public void setDiscardMarkerHWCalc(boolean discardMarkerHWCalc) {
		this.discardMarkerHWCalc = discardMarkerHWCalc;
	}

	public boolean isDiscardMarkerHWFree() {
		return discardMarkerHWFree;
	}

	public void setDiscardMarkerHWFree(boolean discardMarkerHWFree) {
		this.discardMarkerHWFree = discardMarkerHWFree;
	}

	public double getDiscardMarkerHWTreshold() {
		return discardMarkerHWTreshold;
	}

	public void setDiscardMarkerHWTreshold(double discardMarkerHWTreshold) {
		this.discardMarkerHWTreshold = discardMarkerHWTreshold;
	}

//	public boolean isDiscardGTMismatches() {
//		return discardGTMismatches;
//	}
//
//	public void setDiscardGTMismatches(boolean discardGTMismatches) {
//		this.discardGTMismatches = discardGTMismatches;
//	}
//
//	public boolean isDiscardMarkerByMisRat() {
//		return discardMarkerByMisRat;
//	}
//
//	public void setDiscardMarkerByMisRat(boolean discardMarkerByMisRat) {
//		this.discardMarkerByMisRat = discardMarkerByMisRat;
//	}
//
//	public double getDiscardMarkerMisRatVal() {
//		return discardMarkerMisRatVal;
//	}
//
//	public void setDiscardMarkerMisRatVal(double discardMarkerMisRatVal) {
//		this.discardMarkerMisRatVal = discardMarkerMisRatVal;
//	}
//
//	public boolean isDiscardMarkerByHetzyRat() {
//		return discardMarkerByHetzyRat;
//	}
//
//	public void setDiscardMarkerByHetzyRat(boolean discardMarkerByHetzyRat) {
//		this.discardMarkerByHetzyRat = discardMarkerByHetzyRat;
//	}
//
//	public double getDiscardMarkerHetzyRatVal() {
//		return discardMarkerHetzyRatVal;
//	}
//
//	public void setDiscardMarkerHetzyRatVal(double discardMarkerHetzyRatVal) {
//		this.discardMarkerHetzyRatVal = discardMarkerHetzyRatVal;
//	}
//
//	public boolean isDiscardSampleByMisRat() {
//		return discardSampleByMisRat;
//	}
//
//	public void setDiscardSampleByMisRat(boolean discardSampleByMisRat) {
//		this.discardSampleByMisRat = discardSampleByMisRat;
//	}
//
//	public double getDiscardSampleMisRatVal() {
//		return discardSampleMisRatVal;
//	}
//
//	public void setDiscardSampleMisRatVal(double discardSampleMisRatVal) {
//		this.discardSampleMisRatVal = discardSampleMisRatVal;
//	}
//
//	public boolean isDiscardSampleByHetzyRat() {
//		return discardSampleByHetzyRat;
//	}
//
//	public void setDiscardSampleByHetzyRat(boolean discardSampleByHetzyRat) {
//		this.discardSampleByHetzyRat = discardSampleByHetzyRat;
//	}
//
//	public double getDiscardSampleHetzyRatVal() {
//		return discardSampleHetzyRatVal;
//	}
//
//	public void setDiscardSampleHetzyRatVal(double discardSampleHetzyRatVal) {
//		this.discardSampleHetzyRatVal = discardSampleHetzyRatVal;
//	}

	public MarkerCensusOperationParams getMarkerCensusOperationParams() {
		return markerCensusOperationParams;
	}

	public void setMarkerCensusOperationParams(MarkerCensusOperationParams markerCensusOperationParams) {
		this.markerCensusOperationParams = markerCensusOperationParams;
	}

	public String getChromosome() {
		return chromosome;
	}

	public void setChromosome(String chromosome) {
		this.chromosome = chromosome;
	}

	public StrandType getStrandType() {
		return strandType;
	}

	public void setStrandType(StrandType strandType) {
		this.strandType = strandType;
	}

	public GenotypeEncoding getGtCode() {
		return gtCode;
	}

	public void setGtCode(GenotypeEncoding gtCode) {
		this.gtCode = gtCode;
	}

	public String getHardyWeinbergOperationName() {
		return (hardyWeinbergOperationName == null)
				? ((getMarkerCensusOperationParams() == null)
					? ""
					: "H&W on " + getMarkerCensusOperationParams().getName())
				: hardyWeinbergOperationName;
	}

	public void setHardyWeinbergOperationName(final String hardyWeinbergOperationName) {
		this.hardyWeinbergOperationName = hardyWeinbergOperationName;
	}
}
