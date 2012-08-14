/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.gwaspi.netCDF.loader;

import org.gwaspi.constants.cNetCDF;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.TreeMap;
import org.gwaspi.constants.cImport.Annotation.HapmapGT_Standard;
import org.gwaspi.constants.cNetCDF.*;

/* Imports Hapmap genotype files as found on
 * http://hapmap.ncbi.nlm.nih.gov/downloads/genotypes/?N=D
 */


/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class MetadataLoaderHapmap_old {

    private String hapmapPath;
    private String format;
    private int studyId;
    protected enum Bases
    {
        A, C, T, G;
    }
    private static String tabulator= cNetCDF.Defaults.TMP_SEPARATOR;

    public MetadataLoaderHapmap_old(String _hapmapPath, String _format, int _studyId) throws FileNotFoundException{

        hapmapPath = _hapmapPath;
        studyId = _studyId;
        format = _format;

    }

    //ACCESSORS
    public LinkedHashMap getSortedMarkerSetWithMetaData() throws IOException {
        String startTime= org.gwaspi.global.Utils.getMediumDateTimeAsString();

        TreeMap tempTM = parseAnnotationBRFile(hapmapPath); //rsId, alleles [A/T], chr, pos, strand, genome_build, center, protLSID, assayLSID, panelLSID, QC_code, ensue GTs by SampleId

        org.gwaspi.global.Utils.sysoutStart("initilaizing marker info");
        System.out.println(org.gwaspi.global.Text.All.processing);

        LinkedHashMap markerMetadataLHM = new LinkedHashMap();
        for (Iterator it=tempTM.keySet().iterator(); it.hasNext();) {
            Object key = it.next();
            String[] values = tempTM.get(key).toString().split(cNetCDF.Defaults.TMP_SEPARATOR);

            Object[] markerInfo = new Object[values.length];
            markerInfo[0] = values[0];  //0 => markerid
            markerInfo[1] = values[1];  //1 => rsId
            markerInfo[2] = fixChrData(values[2]);  //2 => chr
            markerInfo[3] = Integer.parseInt(values[3]);  //3 => pos
            markerInfo[4] = values[4];  //4 => strand
            markerInfo[5] = values[5];  //5 => alleles

            markerMetadataLHM.put(values[0], markerInfo);
        }

        String description="Generated sorted MarkerIdSet LHM sorted by chromosome and position";
        logAsWhole(startTime, hapmapPath, description, studyId);
        return markerMetadataLHM;
    }



    public static TreeMap parseAnnotationBRFile(String path) throws FileNotFoundException, IOException{
        FileReader fr = new FileReader(path);
        BufferedReader inputAnnotationBr = new BufferedReader(fr);
        TreeMap sortedMetadataTM = new TreeMap(new ComparatorChrAutPosMarkerIdAsc());

        String header=inputAnnotationBr.readLine();

        String l;
        String[] hapmapVals = null;
        String chr="";

        int count=0;
        while ((l = inputAnnotationBr.readLine()) != null) {

            hapmapVals = l.split(org.gwaspi.constants.cImport.Separators.separators_SpaceTab_rgxp);
            String alleles = hapmapVals[HapmapGT_Standard.alleles].replace("/","");

            StringBuilder sbVal = new StringBuilder(hapmapVals[HapmapGT_Standard.rsId]); //0 => markerId = rsId
            sbVal.append(cNetCDF.Defaults.TMP_SEPARATOR);
            sbVal.append(hapmapVals[HapmapGT_Standard.rsId]);   //1 => rsId
            sbVal.append(cNetCDF.Defaults.TMP_SEPARATOR);
            chr = hapmapVals[HapmapGT_Standard.chr];    //2 => chr
            if(chr.length()>3){chr = chr.substring(3);}       //Probably contains "chr" in front of number
            sbVal.append(chr); 
            sbVal.append(cNetCDF.Defaults.TMP_SEPARATOR);
            sbVal.append(hapmapVals[HapmapGT_Standard.pos]);    //3 => pos
            sbVal.append(cNetCDF.Defaults.TMP_SEPARATOR);
            sbVal.append(hapmapVals[HapmapGT_Standard.strand]); //4 => strand
            sbVal.append(cNetCDF.Defaults.TMP_SEPARATOR);
            sbVal.append(alleles);                              //5 => alleles

            StringBuffer sbKey = new StringBuffer(chr);
            sbKey.append(cNetCDF.Defaults.TMP_SEPARATOR);
            sbKey.append(hapmapVals[HapmapGT_Standard.pos]);
            sortedMetadataTM.put(sbKey.toString(), sbVal.toString());
            
            count++;
            if(count==1){
                System.out.println(org.gwaspi.global.Text.All.processing);
            } else if(count%100000==0){
                System.out.println("Parsed annotation lines: "+count);
            }
        }
        System.out.println("Parsed annotation lines: "+count);
        return sortedMetadataTM;
    }

    public String fixChrData(String chr) throws IOException{
        if(chr.equals("23")){
            chr = "X";
        }
        if(chr.equals("24")){
            chr = "Y";
        }
        if(chr.equals("25")){
            chr = "XY";
        }
        if(chr.equals("26")){
            chr = "MT";
        }
        return chr;
    }

    //METHODS
    private static void logAsWhole(String startTime, String dirPath, String description, int studyId) throws IOException {
         //LOG OPERATION IN STUDY HISTORY
         StringBuffer operation = new StringBuffer("\nLoaded Annotation metadata in path "+dirPath+".\n");
         operation.append("Start Time: " + startTime + "\n");
         operation.append("End Time: "+global.Utils.getMediumDateTimeAsString()+".\n");
         operation.append("Description: "+description+".\n");
         org.gwaspi.global.Utils.logOperationInStudyDesc(operation.toString(), studyId);
         ////////////////////////////////
    }

}
