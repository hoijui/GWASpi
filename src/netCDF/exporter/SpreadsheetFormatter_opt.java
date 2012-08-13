/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package netCDF.exporter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import netCDF.markers.MarkerSet_opt;
import netCDF.matrices.*;
import ucar.nc2.*;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class SpreadsheetFormatter_opt {
    

    public static boolean exportToFleurFormat(String exportPath,
                                 MatrixMetadata rdMatrixMetadata,
                                 MarkerSet_opt rdMarkerSet,
                                 LinkedHashMap rdSampleSetLHM) throws IOException {

        File exportDir = new File(exportPath);
        if(!exportDir.exists() || !exportDir.isDirectory()){
            return false;
        }

        boolean result=false;
        String sep = constants.cExport.separator_REPORTS;
        NetcdfFile rdNcFile = NetcdfFile.open(rdMatrixMetadata.getPathToMatrix());
        rdMarkerSet.initFullMarkerIdSetLHM();
        
        try {

            //<editor-fold defaultstate="collapsed" desc="SPREADSHEET FILE">
            FileWriter pedFW = new FileWriter(exportDir.getPath()+"/"+rdMatrixMetadata.getMatrixFriendlyName()+".csv");
            BufferedWriter pedBW = new BufferedWriter(pedFW);

            //HEADER CONTAINING MARKER IDs
            StringBuilder line = new StringBuilder();
            for (Iterator it = rdMarkerSet.markerIdSetLHM.keySet().iterator(); it.hasNext();) {
                Object key = it.next();
                line.append(sep);
                line.append(key);
            }
            pedBW.append(line);
            pedBW.append("\n");
            pedBW.flush();

            //Iterate through all samples
            int sampleNb = 0;
            for (Iterator it = rdSampleSetLHM.keySet().iterator(); it.hasNext();) {

                String sampleId = it.next().toString();

                //Iterate through all markers
                rdMarkerSet.fillGTsForCurrentSampleIntoInitLHM(sampleNb);
                StringBuilder genotypes = new StringBuilder();
                for (Iterator it2 = rdMarkerSet.markerIdSetLHM.keySet().iterator(); it2.hasNext();) {
                    Object key = it2.next();
                    byte[] tempGT = (byte[]) rdMarkerSet.markerIdSetLHM.get(key);
                    genotypes.append(sep);
                    genotypes.append(new String(new byte[]{tempGT[0]}));
                    genotypes.append(new String(new byte[]{tempGT[1]}));
                }

                //Individual ID
                //Genotypes
                line = new StringBuilder();
                line.append(sampleId);
                line.append(genotypes);

                pedBW.append(line);
                pedBW.append("\n");
                pedBW.flush();

                sampleNb++;
                if(sampleNb%100==0){
                    System.out.println("Samples exported to Fleur file:"+sampleNb);
                }
                
            }
            System.out.println("Samples exported to Fleur file:"+sampleNb);
            pedBW.close();
            pedFW.close();

            //</editor-fold>

            result=true;
        } catch (IOException iOException) {
        } finally {
           if (null != rdNcFile) try {
                rdNcFile.close();
           } catch (IOException ioe) {
                System.out.println("Cannot close file: "+ioe);
           }
        }

        return result;
    }

 

}
