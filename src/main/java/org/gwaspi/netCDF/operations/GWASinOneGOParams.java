package org.gwaspi.netCDF.operations;

import org.gwaspi.constants.cNetCDF.Defaults.GenotypeEncoding;
import org.gwaspi.constants.cNetCDF.Defaults.StrandType;

public class GWASinOneGOParams {

	private boolean proceed = false;
	private boolean performAllelicTests = true;
	private boolean performGenotypicTests = true;
	private boolean performTrendTests = true;
	private boolean discardGTMismatches = true;
	private boolean discardMarkerByMisRat = true;
	private double discardMarkerMisRatVal = 0;
	private boolean discardMarkerByHetzyRat = false;
	private double discardMarkerHetzyRatVal = 0;
	private boolean discardMarkerHWCalc = true;
	private boolean discardMarkerHWFree = false;
	private double discardMarkerHWTreshold = 0;
	private boolean discardSampleByMisRat = true;
	private double discardSampleMisRatVal = 0;
	private boolean discardSampleByHetzyRat = false;
	private double discardSampleHetzyRatVal = 0;
	private String chromosome = "";
	private StrandType strandType = StrandType.UNKNOWN;
	private GenotypeEncoding gtCode = GenotypeEncoding.UNKNOWN;
	private String friendlyName = "";

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

	public boolean isPerformAssociationTests() {
		return (isPerformAllelicTests() || isPerformGenotypicTests());
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

	public boolean isDiscardGTMismatches() {
		return discardGTMismatches;
	}

	public void setDiscardGTMismatches(boolean discardGTMismatches) {
		this.discardGTMismatches = discardGTMismatches;
	}

	public boolean isDiscardMarkerByMisRat() {
		return discardMarkerByMisRat;
	}

	public void setDiscardMarkerByMisRat(boolean discardMarkerByMisRat) {
		this.discardMarkerByMisRat = discardMarkerByMisRat;
	}

	public double getDiscardMarkerMisRatVal() {
		return discardMarkerMisRatVal;
	}

	public void setDiscardMarkerMisRatVal(double discardMarkerMisRatVal) {
		this.discardMarkerMisRatVal = discardMarkerMisRatVal;
	}

	public boolean isDiscardMarkerByHetzyRat() {
		return discardMarkerByHetzyRat;
	}

	public void setDiscardMarkerByHetzyRat(boolean discardMarkerByHetzyRat) {
		this.discardMarkerByHetzyRat = discardMarkerByHetzyRat;
	}

	public double getDiscardMarkerHetzyRatVal() {
		return discardMarkerHetzyRatVal;
	}

	public void setDiscardMarkerHetzyRatVal(double discardMarkerHetzyRatVal) {
		this.discardMarkerHetzyRatVal = discardMarkerHetzyRatVal;
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

	public boolean isDiscardSampleByMisRat() {
		return discardSampleByMisRat;
	}

	public void setDiscardSampleByMisRat(boolean discardSampleByMisRat) {
		this.discardSampleByMisRat = discardSampleByMisRat;
	}

	public double getDiscardSampleMisRatVal() {
		return discardSampleMisRatVal;
	}

	public void setDiscardSampleMisRatVal(double discardSampleMisRatVal) {
		this.discardSampleMisRatVal = discardSampleMisRatVal;
	}

	public boolean isDiscardSampleByHetzyRat() {
		return discardSampleByHetzyRat;
	}

	public void setDiscardSampleByHetzyRat(boolean discardSampleByHetzyRat) {
		this.discardSampleByHetzyRat = discardSampleByHetzyRat;
	}

	public double getDiscardSampleHetzyRatVal() {
		return discardSampleHetzyRatVal;
	}

	public void setDiscardSampleHetzyRatVal(double discardSampleHetzyRatVal) {
		this.discardSampleHetzyRatVal = discardSampleHetzyRatVal;
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

	public String getFriendlyName() {
		return friendlyName;
	}

	public void setFriendlyName(String friendlyName) {
		this.friendlyName = friendlyName;
	}
}
