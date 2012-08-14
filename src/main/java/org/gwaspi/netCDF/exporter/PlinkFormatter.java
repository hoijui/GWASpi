/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.gwaspi.netCDF.exporter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import org.gwaspi.netCDF.markers.MarkerSet;
import org.gwaspi.netCDF.matrices.*;
import org.gwaspi.samples.SampleSet;
import ucar.nc2.*;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class PlinkFormatter {
    

    public static boolean exportToPlink(String exportPath,
                                 MatrixMetadata rdMatrixMetadata,
                                 MarkerSet rdMarkerSet,
                                 LinkedHashMap rdMarkerIdSetLHM,
                                 LinkedHashMap rdSampleSetLHM) throws IOException {

        File exportDir = new File(exportPath);
        if(!exportDir.exists() || !exportDir.isDirectory()){
            return false;
        }

        boolean result=false;
        String sep = org.gwaspi.constants.cExport.separator_PLINK;
        NetcdfFile rdNcFile = NetcdfFile.open(rdMatrixMetadata.getPathToMatrix());

        try {

            //<editor-fold defaultstate="collapsed" desc="PED FILE">
            FileWriter pedFW = new FileWriter(exportDir.getPath()+"/"+rdMatrixMetadata.getMatrixFriendlyName()+".ped");
            BufferedWriter pedBW = new BufferedWriter(pedFW);

            //Iterate through all samples
            int sampleNb = 0;
            for (Iterator it = rdSampleSetLHM.keySet().iterator(); it.hasNext();) {
                String sampleId = it.next().toString();
                HashMap sampleInfo = Utils.getCurrentSampleFormattedInfo(sampleId, rdMatrixMetadata.getStudyId());

                String familyId = sampleInfo.get(org.gwaspi.constants.cDBSamples.f_FAMILY_ID).toString();
                String fatherId = sampleInfo.get(org.gwaspi.constants.cDBSamples.f_FATHER_ID).toString();
                String motherId = sampleInfo.get(org.gwaspi.constants.cDBSamples.f_MOTHER_ID).toString();
                String sex = sampleInfo.get(org.gwaspi.constants.cDBSamples.f_SEX).toString();
                String affection = sampleInfo.get(org.gwaspi.constants.cDBSamples.f_AFFECTION).toString();


                //Iterate through all markers
                rdMarkerIdSetLHM = rdMarkerSet.readAllMarkersGTsForCurrentSampleToLHM(rdNcFile, rdMarkerIdSetLHM, sampleNb);
                StringBuilder genotypes = new StringBuilder();
                for (Iterator it2 = rdMarkerIdSetLHM.keySet().iterator(); it2.hasNext();) {
                    Object key = it2.next();
                    byte[] tempGT = (byte[]) rdMarkerIdSetLHM.get(key);
                    genotypes.append(sep);
                    genotypes.append(new String(new byte[]{tempGT[0]}));
                    genotypes.append(sep);
                    genotypes.append(new String(new byte[]{tempGT[1]}));
                }


                //Family ID
                //Individual ID
                //Paternal ID
                //Maternal ID
                //Sex (1=male; 2=female; other=unknown)
                //Affection
                //Genotypes

                StringBuilder line = new StringBuilder();
                line.append(familyId);
                line.append(sep);
                line.append(sampleId);
                line.append(sep);
                line.append(fatherId);
                line.append(sep);
                line.append(motherId);
                line.append(sep);
                line.append(sex);
                line.append(sep);
                line.append(affection);
                line.append(genotypes);

                pedBW.append(line);
                pedBW.append("\n");
                pedBW.flush();

                sampleNb++;
                if(sampleNb%100==0){
                    System.out.println("Samples exported to PED file:"+sampleNb);
                }
                
            }
            System.out.println("Samples exported to PED file:"+sampleNb);
            pedBW.close();
            pedFW.close();

            //</editor-fold>

            //<editor-fold defaultstate="collapsed" desc="MAP FILE">
            FileWriter mapFW = new FileWriter(exportDir.getPath()+"/"+rdMatrixMetadata.getMatrixFriendlyName()+".map");
            BufferedWriter mapBW = new BufferedWriter(mapFW);

            //MAP files
            //     chromosome (1-22, X(23), Y(24), XY(25), MT(26) or 0 if unplaced)
            //     rs# or snp identifier
            //     Genetic distance (morgans)
            //     Base-pair position (bp units)

            //PURGE MARKERSET
            rdMarkerIdSetLHM = rdMarkerSet.fillLHMWithDefaultValue(rdMarkerIdSetLHM, "");

            //MARKERSET CHROMOSOME
            rdMarkerIdSetLHM = rdMarkerSet.appendVariableToMarkerSetLHMValue(rdNcFile, org.gwaspi.constants.cNetCDF.Variables.VAR_MARKERS_CHR, sep);

            //MARKERSET RSID
            rdMarkerIdSetLHM = rdMarkerSet.appendVariableToMarkerSetLHMValue(rdNcFile, org.gwaspi.constants.cNetCDF.Variables.VAR_MARKERS_RSID, sep);

            //DEFAULT GENETIC DISTANCE = 0
            for (Iterator it = rdMarkerIdSetLHM.keySet().iterator(); it.hasNext();) {
                Object key = it.next();
                StringBuilder value = new StringBuilder(rdMarkerIdSetLHM.get(key).toString());
                value.append(sep);
                value.append("0");
                rdMarkerIdSetLHM.put(key, value);
            }

            //MARKERSET POSITION
            rdMarkerIdSetLHM = rdMarkerSet.appendVariableToMarkerSetLHMValue(rdNcFile, org.gwaspi.constants.cNetCDF.Variables.VAR_MARKERS_POS, sep);
            int markerNb=0;
            for (Iterator it = rdMarkerIdSetLHM.keySet().iterator(); it.hasNext();) {
                Object key = it.next();
                Object pos = rdMarkerIdSetLHM.get(key);
                mapBW.append(pos.toString());
                mapBW.append("\n");
                markerNb++;
            }

            System.out.println("Markers exported to MAP file:"+markerNb);

            mapBW.close();
            mapFW.close();

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

    public static boolean exportToTransposedPlink(String exportPath,
                                         MatrixMetadata rdMatrixMetadata,
                                         MarkerSet rdMarkerSet,
                                         SampleSet rdSampleSet,
                                         LinkedHashMap rdMarkerIdSetLHM,
                                         LinkedHashMap rdSampleSetLHM) throws IOException {
        File exportDir = new File(exportPath);
        if(!exportDir.exists() || !exportDir.isDirectory()){
            return false;
        }

        boolean result=false;
        String sep = org.gwaspi.constants.cExport.separator_PLINK;
        NetcdfFile rdNcFile = NetcdfFile.open(rdMatrixMetadata.getPathToMatrix());

        try {

            //<editor-fold defaultstate="collapsed" desc="TPED FILE">
            FileWriter tpedFW = new FileWriter(exportDir.getPath()+"/"+rdMatrixMetadata.getMatrixFriendlyName()+".tped");
            BufferedWriter tpedBW = new BufferedWriter(tpedFW);

            //TPED files:
            //chromosome (1-22, X(23), Y(24), XY(25), MT(26) or 0 if unplaced)
            //rs# or snp identifier
            //Genetic distance (morgans)
            //Base-pair position (bp units)
            //Genotypes

            //PURGE MARKERSET
            rdMarkerIdSetLHM = rdMarkerSet.fillLHMWithDefaultValue(rdMarkerIdSetLHM, "");

            //MARKERSET CHROMOSOME
            rdMarkerIdSetLHM = rdMarkerSet.appendVariableToMarkerSetLHMValue(rdNcFile, org.gwaspi.constants.cNetCDF.Variables.VAR_MARKERS_CHR, sep);

            //MARKERSET RSID
            rdMarkerIdSetLHM = rdMarkerSet.appendVariableToMarkerSetLHMValue(rdNcFile, org.gwaspi.constants.cNetCDF.Variables.VAR_MARKERS_RSID, sep);

            //DEFAULT GENETIC DISTANCE = 0
            for (Iterator it = rdMarkerIdSetLHM.keySet().iterator(); it.hasNext();) {
                Object key = it.next();
                StringBuilder value = new StringBuilder(rdMarkerIdSetLHM.get(key).toString());
                value.append(sep);
                value.append("0");
                rdMarkerIdSetLHM.put(key, value);
            }

            //MARKERSET POSITION
            rdMarkerIdSetLHM = rdMarkerSet.appendVariableToMarkerSetLHMValue(rdNcFile, org.gwaspi.constants.cNetCDF.Variables.VAR_MARKERS_POS, sep);

            //Iterate through markerset
            int markerNb=0;
            for (Iterator it = rdMarkerIdSetLHM.keySet().iterator(); it.hasNext();) {
                StringBuilder line = new StringBuilder();
                Object markerId = it.next();
                Object pos = rdMarkerIdSetLHM.get(markerId);

                //Iterate through sampleset
                StringBuilder genotypes = new StringBuilder();
                rdSampleSetLHM = rdSampleSet.readAllSamplesGTsFromCurrentMarkerToLHM(rdNcFile, rdSampleSetLHM, markerNb);
                for (Iterator it2 = rdSampleSetLHM.keySet().iterator(); it2.hasNext();) {
                    Object sampleId = it2.next();
                    byte[] tempGT = (byte[]) rdSampleSetLHM.get(sampleId);
                    genotypes.append(sep);
                    genotypes.append(new String(new byte[]{tempGT[0]}));
                    genotypes.append(sep);
                    genotypes.append(new String(new byte[]{tempGT[1]}));
                }

                line.append(pos.toString());
                line.append(genotypes);

                tpedBW.append(line);
                tpedBW.append("\n");
                markerNb++;
            }

            System.out.println("Markers exported to tped:"+markerNb);

            tpedBW.close();
            tpedFW.close();

            //</editor-fold>


            //<editor-fold defaultstate="collapsed" desc="TFAM FILE">
            FileWriter tfamFW = new FileWriter(exportDir.getPath()+"/"+rdMatrixMetadata.getMatrixFriendlyName()+".tfam");
            BufferedWriter tfamBW = new BufferedWriter(tfamFW);

            //Iterate through all samples
            int sampleNb = 0;
            for (Iterator it = rdSampleSetLHM.keySet().iterator(); it.hasNext();) {
                StringBuilder line = new StringBuilder();
                String sampleId = it.next().toString();

                HashMap sampleInfo = Utils.getCurrentSampleFormattedInfo(sampleId, rdMatrixMetadata.getStudyId());

                String familyId = sampleInfo.get(org.gwaspi.constants.cDBSamples.f_FAMILY_ID).toString();
                String fatherId = sampleInfo.get(org.gwaspi.constants.cDBSamples.f_FATHER_ID).toString();
                String motherId = sampleInfo.get(org.gwaspi.constants.cDBSamples.f_MOTHER_ID).toString();
                String sex = sampleInfo.get(org.gwaspi.constants.cDBSamples.f_SEX).toString();
                String affection = sampleInfo.get(org.gwaspi.constants.cDBSamples.f_AFFECTION).toString();

                //TFAM files
                //Family ID
                //Individual ID
                //Paternal ID
                //Maternal ID
                //Sex (1=male; 2=female; other=unknown)
                //Affection

                line = new StringBuilder();
                line.append(familyId);
                line.append(sep);
                line.append(sampleId);
                line.append(sep);
                line.append(fatherId);
                line.append(sep);
                line.append(motherId);
                line.append(sep);
                line.append(sex);
                line.append(sep);
                line.append(affection);


                tfamBW.append(line);
                tfamBW.append("\n");
                tfamBW.flush();

                sampleNb++;
                if(sampleNb%100==0){
                    System.out.println("Samples exported:"+sampleNb);
                }
                
            }
            tfamBW.close();
            tfamFW.close();

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
