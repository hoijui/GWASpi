/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package netCDF.operations;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class GWASinOneGOParams {
    public boolean proceed = false;

    public boolean performAllelicTests = true;
    public boolean performGenotypicTests = true;
    public boolean performTrendTests = true;

    public boolean discardGTMismatches = true;
    
    public boolean discardMarkerByMisRat = true;
    public double discardMarkerMisRatVal = 0;

    public boolean discardMarkerByHetzyRat = false;
    public double discardMarkerHetzyRatVal = 0;

    public boolean discardMarkerHWCalc = true;
    public boolean discardMarkerHWFree = false;
    public double discardMarkerHWTreshold = 0;

    public boolean discardSampleByMisRat = true;
    public double discardSampleMisRatVal = 0;

    public boolean discardSampleByHetzyRat = false;
    public double discardSampleHetzyRatVal = 0;

    public String chromosome = "";
    public String strandType = constants.cNetCDF.Defaults.StrandType.UNKNOWN.toString();
    public String gtCode = constants.cNetCDF.Defaults.GenotypeEncoding.UNKNOWN.toString();
    public String friendlyName = "";

}
