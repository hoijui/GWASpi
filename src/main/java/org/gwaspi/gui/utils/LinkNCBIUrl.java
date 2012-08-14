/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.gwaspi.gui.utils;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class LinkNCBIUrl {

    public static String NCBI_BASE_URL = "http://www.ncbi.nlm.nih.gov/sites/entrez?db=snp&cmd=search&term=";

    public static String getRsLink(String rs){
        String baseUrl = NCBI_BASE_URL;
        return (baseUrl+rs);
    }


}
