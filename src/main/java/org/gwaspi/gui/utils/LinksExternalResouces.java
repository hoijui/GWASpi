package org.gwaspi.gui.utils;

public class LinksExternalResouces {

	private LinksExternalResouces() {
	}

	private static final String[][] LINKS_DB = new String[][]{
		{"Ensembl region overview, rsId",
			"http://www.ensembl.org/Homo_sapiens/Variation/Summary?source=dbSNP;v=$rsId$",
			"rsId"},
		{"Ensembl region overview, chr + position window",
			"http://www.ensembl.org/Homo_sapiens/Location/View?r=$chr$:$pos-start$-$pos-end$",
			"chr+pos"},
		{"UCSC Genome Browser, rsId",
			"http://genome.ucsc.edu/cgi-bin/hgTracks?position=$rsId$&hgFind.matches=$rsId$",
			"rsId"},
		{"UCSC Genome Browser, chr + position window",
			"http://genome.ucsc.edu/cgi-bin/hgTracks?db=hg19&position=Chr$chr$:$pos-start$-$pos-end$",
			"chr+pos"},
		{"Human Genome Variation Database, rsID",
			"http://www.hgvbaseg2p.org/search?q=$rsId$",
			"rsId"},
		{"Human Genome Variation Database, chr + position window",
			"http://www.hgvbaseg2p.org/search?q=chr$chr$:$pos-start$-$pos-end$&t=ZERO&l=asd&m=All",
			"chr+pos"},
		{"GVS: Genome Variation Server, rsID",
			"http://gvs.gs.washington.edu/GVS/PopStatsServlet?searchBy=dbsnp+rsID&target=$rsId$",
			"rsId"},
		{"GVS: Genome Variation Server,  chr + position window",
			"http://gvs.gs.washington.edu/GVS/PopStatsServlet?searchBy=chromosome&chromosome=$chr$&chromoStart=$pos-start$&chromoEnd=$pos-end$",
			"chr+pos"},
		{"NCBI Reference SNP Cluster Report, rsID",
			"http://www.ncbi.nlm.nih.gov/SNP/snp_ref.cgi?type=rs&rs=$rsId$",
			"rsId"},
		{"NCBI map viewer, chr + position window",
			"http://www.ncbi.nlm.nih.gov/mapview/maps.cgi?taxid=9606&CHR=$chr$&BEG=$pos-start$&END=$pos-end$",
			"chr+pos"},
		{"NCBI Homo sapiens genome view, rsID",
			"http://www.ncbi.nlm.nih.gov/mapview/map_search.cgi?taxid=9606&build=current&advsrch=off&query=$rsId$",
			"rsId"},
		{"NCBI Search across databases, rsID",
			"http://www.ncbi.nlm.nih.gov/sites/gquery?term=$rsId$",
			"rsId"},
		{"NCBI Search across databases, chr + position window",
			"http://www.ncbi.nlm.nih.gov/sites/gquery?term=$chr$:$pos-start$",
			"chr+pos"},
		{"1000 Genomes human variation, rsID",
			"http://browser.1000genomes.org/Homo_sapiens/Variation/Summary?v=$rsId$;vdb=variation",
			"rsId"},
		{"1000 Genomes human variation, chr + position window",
			"http://browser.1000genomes.org/Homo_sapiens/Location/View?contigviewbotom=variation_feature_variation=normal;r=$chr$:$pos-start$-$pos-end$;source=dbSNP;v=;vdb=variation;vf=",
			"chr+pos"},
		{"UniProt Proteine Knowledge Base, rsID",
			"http://www.uniprot.org/uniprot/?query=$rsId$&sort=score",
			"rsId"},
		{"UniProt Literature Citations, rsID",
			"http://www.uniprot.org/citations/?query=$rsId$&sort=score",
			"rsId"}};

	public static String[] getLinkNames() {
		String[] result = new String[LINKS_DB.length];
		int idx = 0;
		for (String[] tmp : LINKS_DB) {
			result[idx] = tmp[0];
			idx++;
		}
		return result;
	}

	public static String getLinkURL(int idx) {
		return LINKS_DB[idx][1];
	}

	public static boolean checkIfRsNecessary(int idx) {
		return LINKS_DB[idx][2].contains("rsId");
	}

	public static String getResourceLink(int idx, String chr, String rsId, Long position) {
		String baseUrl = getLinkURL(idx);

		Long startPos = (position - 1000);
		Long endPos = (position + 1000);
		if (startPos < 0) {
			startPos = 0L;
		}

		String processedURL = baseUrl.replace("$chr$", chr);
		processedURL = processedURL.replace("$rsId$", rsId);
		processedURL = processedURL.replace("$pos-middle$", position.toString());
		processedURL = processedURL.replace("$pos-start$", startPos.toString());
		processedURL = processedURL.replace("$pos-end$", endPos.toString());

		return (processedURL);
	}
}
