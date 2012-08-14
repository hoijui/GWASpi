/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.gwaspi.constants;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class cExport {

    public static String separator_PLINK = " ";
    public static String separator_BEAGLE = " ";
    public static String separator_MACH = " ";
    public static String separator_REPORTS = "\t";
    public static String separator_SAMPLE_INFO = "\t";

    public static enum ExportFormat
    {
        BEAGLE, Eigensoft_Eigenstrat, GWASpi, MACH, PLINK, PLINK_Binary, PLINK_Transposed, Spreadsheet ;
        public static ExportFormat compareTo(String str)
        {
            try {
                return valueOf(str);
            }
            catch (Exception ex) {
                return PLINK;
            }
        }
    }

}
