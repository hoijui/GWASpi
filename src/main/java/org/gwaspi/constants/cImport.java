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
public class cImport {

    public static String currentAnnotation = "";

    public static enum ImportFormat
    {
//        Affymetrix_GenomeWide6, BEAGLE, GWASpi, HAPMAP, HGDP1, Illumina_LGEN, PLINK, PLINK_Binary;
        Affymetrix_GenomeWide6, BEAGLE, GWASpi, HAPMAP, HGDP1, Illumina_LGEN, PLINK, PLINK_Binary, Sequenom;
        public static ImportFormat compareTo(String str)
        {
            try {
                return valueOf(str);
            }
            catch (Exception ex) {
                return GWASpi;
            }
        }
    }

    public static class Genotypes{

        public static class Affymetrix_GenomeWide6{
            //ProbesetID, Call, Confidence, Signal A, Signal B, Forced Call
            public static int markerId = 0;
            public static int alleles = 1; //Caution, using normal Call, not Forced Call!
            public static String missing = "NoCall";
            public static int score = 2;
            public static int intensity_A = 3;
            public static int intensity_B = 4;
        }

        public static class Beagle_Standard{

            public static int markerId=1;
            public static int genotypes=2;
            public static String missing="0";


        }


        public static class HGDP1_Standard{

            public static int markerId=0;
            public static int genotypes=1;
            public static String missing="--";

        }

        public static class Hapmap_Standard{

            public static int dataStartRow = 1;
            public static int sampleId = 11;
            public static int markerId = 0;
            public static int alleles = 1;
            public static int chr = 2;
            public static int pos = 3;
            public static int strand = 4;
            public static String missing = "NN";
            public static int score = 10;

        }

        public static class Illumina_LGEN{

            public static int familyId = 0;
            public static int sampleId = 1;
            public static int markerId=2;
            public static int allele1 = 3;
            public static int allele2 = 4;
            public static String missing="-";

        }


        public static class Sequenom {
            public static int sampleId = 0;
            public static int alleles = 1;
            public static int markerId = 2;
            public static int well = 3;
            public static int qa_desc = 4;
        }
    }

    
    public static class Annotation{

        public static class Affymetrix_GenomeWide6{

            /* Standard annotation since GenomeWideSNP_6.na30.annot
            0 Probe Set ID
            1 dbSNP RS ID
            2 Chromosome
            3 Physical Position
            4 Strand
            5 ChrX pseudo-autosomal region 1
            6 Cytoband
            7 Flank
            8 Allele A
            9 Allele B
            10 Associated Gene
            11 Genetic Map
            12 Microsatellite
            13 Fragment Enzyme Type Length Start Stop
            14 Allele Frequencies
            15 Heterozygous Allele Frequencies
            16 Number of individuals/Number of chromosomes
            17 In Hapmap
            18 Strand Versus dbSNP
            19 Copy Number Variation
            20 Probe Count
            21 ChrX pseudo-autosomal region 2
            22 In Final List
            23 Minor Allele
            24 Minor Allele Frequency
            25 % GC
            26 OMIM
            */

            public static int markerId=0;
            public static int rsId=1;
            public static int chr=2;
            public static int pos=3;
            public static int strand=4;
            public static int pseudo_a1=5;
            //public static int cytoband=6;
            //public static int flank=7;
            public static int alleleA=8;
            public static int alleleB=9;
            //public static int assoc_gene=10;
            //public static int gen_map=11;
            //public static int micro_sat=12;
            //public static int frag_enzyme_type_length_start_stop=13;
            public static int alleles_freq=14;
            //public static int het_alleles_freq=15;
            //public static int nb_hapmap_indiv_chr=16;
            //public static int in_hapmap=17;
            //public static int strand_vs_dbsnp=18;
            //public static int cnv=19;
            //public static int probe_count=20;
            public static int pseudo_a2=21;
            public static int in_final_list=22;
            //public static int minor_allele=23;
            //public static int minor_allele_freq=24;
            //public static int pcGC=25;
            //public static int OMIM = 26;
            
            public static void init(String _currentAnnotation){

                currentAnnotation = _currentAnnotation;
                
                int versionStart = currentAnnotation.indexOf(".na");
                int versionEnd = currentAnnotation.indexOf(".", versionStart+1);
                int annotationVersion = 30; //Assuming newest annotation version
                try{    //Find out real annotation version
                    annotationVersion = Integer.parseInt(currentAnnotation.substring(versionStart+3, versionEnd));
                }catch(Exception e){}

                if(annotationVersion<30){
                    //Format for Annotation releases 26,27,28
                    markerId=0;
                    rsId=2;
                    chr=3;
                    pos=4;
                    strand=5;
                    pseudo_a1=6;
                    //cytoband=7;
                    //flank=8;
                    alleleA=9;
                    alleleB=10;
                    //assoc_gene=11;
                    //gen_map=12;
                    //micro_sat=13;
                    //frag_enzyme_type_length_start_stop=14;
                    alleles_freq=15;
                    //het_alleles_freq=16;
                    //nb_hapmap_indiv_chr=17;
                    //in_hapmap=18;
                    //strand_vs_dbsnp=19;
                    //cnv=20;
                    //probe_count=21;
                    pseudo_a2=22;
                    in_final_list=23;
                    //minor_allele=24;
                    //minor_allele_freq=25;
                    //pcGC=26;
                } 

            }



        }

        public static class GWASpi {
            public static int sampleId = 1;
            public static int familyId = 0;
            public static int fatherId = 2;
            public static int motherId = 3;
            public static int sex = 4;
            public static int affection = 5;
            public static int category = 6;
            public static int disease = 7;
            public static int population = 8;
            public static int age = 9;


        }


        public static class Plink_Standard{

            public static int map_chr=0;
            public static int map_markerId=1;
            public static int map_gendist=2;
            public static int map_pos=3;

            public static int ped_familyId = 0;
            public static int ped_sampleId = 1;
            public static int ped_fatherId = 2;
            public static int ped_motherId = 3;
            public static int ped_sex = 4;
            public static int ped_affection = 5;
            public static int ped_genotypes = 6;

        }

        public static class Plink_LGEN{

            public static int map_chr=0;
            public static int map_markerId=1;
            public static int map_gendist=2;
            public static int map_pos=3;

            public static int lgen_familyId = 0;
            public static int lgen_sampleId = 1;
            public static int lgen_markerId = 2;
            public static int lgen_allele1_fwd = 3;
            public static int lgen_allele2_fwd = 4;

        }


        public static class Plink_Binary{

            public static int bim_chr=0;
            public static int bim_markerId=1;
            public static int bim_gendist=2;
            public static int bim_pos=3;
            public static int bim_allele1=4;
            public static int bim_allele2=5;

            public static int ped_familyId = 0;
            public static int ped_sampleId = 1;
            public static int ped_fatherId = 2;
            public static int ped_motherId = 3;
            public static int ped_sex = 4;
            public static int ped_affection = 5;

        }


        public static class HapmapGT_Standard{

            public static int rsId=0;
            public static int alleles=1;
            public static int chr=2;
            public static int pos=3;
            public static  int strand=4;
            public static  int build=5;
            public static  int gt_center=6;
            public static  int qc_code=10;

        }

        public static class Beagle_Standard{

            public static int rsId=0;
            public static int pos=1;
            public static int allele1=2;
            public static int allele2=3;

        }

        public static class HGDP1_Standard{

            public static int rsId=0;
            public static int chr=1;
            public static int pos=2;

        }

        public static class Sequenom {
            public static int sampleId = 0;
            public static int alleles = 1;
            public static int markerId = 2;
            public static int well = 3;
            public static int qa_desc = 4;

            //Annotation same as PLINK MAP file
            public static int annot_chr = 0;
            public static int annot_markerId = 1;
            public static int annot_pos = 3;
        }

    }


    public static class SampleInfo{
        public static int familyId = 0;
        public static int sampleId = 1;
        public static int fatherId = 2;
        public static int motherId = 3;
        public static int sex = 4;
        public static int affection = 5;
        public static int category = 6;
        public static int disease = 7;
        public static int population = 8;
        public static int age = 9;

    }

    public static class StrandFlags {

        public static String strandPLS = "+";
        public static String strandMIN = "-";
        public static String strandPLSMIN = "+/-";
        public static String strandFWD = "fwd";
        public static String strandREV = "rev";
        public static String strandUNK = "unk";

    }

    public static class Separators{
        public static String separators_Spaces_rgxp="[ +]";
        public static String separators_CommaSpaceTab_rgxp = "[, \t]";
        public static String separators_CommaSpaceTabLf_rgxp = "[\n, \t]";
        public static String separators_CommaTab_rgxp = "[,\t]";
        public static String separators_SpaceTab_rgxp = "[ \t]";
        public static String separators_Tab_rgxp = "[\t]";
        public static String ops = " "; //separator used in output file

        public static String separator_PLINK = " ";
        public static String separator_BEAGLE = " ";
        public static String separator_REPORTS = "\t";


    }

}
