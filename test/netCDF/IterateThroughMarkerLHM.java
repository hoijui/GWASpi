/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package netCDF;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import samples.SampleSet;
import ucar.nc2.NetcdfFile;


/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class IterateThroughMarkerLHM {

    LinkedHashMap basesLHM = new LinkedHashMap();
    LinkedHashMap rdSampleSetLHM = new LinkedHashMap();
    LinkedHashMap wrSampleSetLHM = new LinkedHashMap();
    SampleSet rdSampleSet = null;
    public IterateThroughMarkerLHM() throws IOException{
        //Iterate through pmAllelesAndStrandsLHM, use marker item position to read correct GTs from all Samples into rdMarkerIdSetLHM.
        int markerNb = 0;
        NetcdfFile rdNcFile = NetcdfFile.open("pathToMatrix");
        for (Iterator it = basesLHM.keySet().iterator(); it.hasNext();) {
            Object markerId = it.next();
            String bases = basesLHM.get(markerId).toString();

            //Get alleles from read matrix
            rdSampleSetLHM = rdSampleSet.readAllSamplesGTsFromCurrentMarkerToLHM(rdNcFile, rdSampleSetLHM, markerNb);


            markerNb++;
        }
     }
}
