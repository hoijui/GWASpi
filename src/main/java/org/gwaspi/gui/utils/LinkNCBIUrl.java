package org.gwaspi.gui.utils;

public class LinkNCBIUrl {

	private LinkNCBIUrl() {
	}

	private static final String NCBI_BASE_URL = "http://www.ncbi.nlm.nih.gov/sites/entrez?db=snp&cmd=search&term=";

	public static String getRsLink(String rs) {
		String baseUrl = NCBI_BASE_URL;
		return (baseUrl + rs);
	}
}
