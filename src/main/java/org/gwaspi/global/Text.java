/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.gwaspi.global;

import org.gwaspi.constants.cNetCDF;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class Text {

    public static class App {
        public static String appName = "GWASpi";
        public static String memoryAvailable1 = "MB have been allocated to run "+Text.App.appName+".";
        public static String memoryAvailable2 = "This will allow you to operate on +/- ";
        public static String memoryAvailable3 = " markers.";
        public static String memoryAvailable4 = "Caution! This amount of memory is likely not to be sufficient for\n"+Text.App.appName+" to perform optimally!";
        public static String initText = "You are running "+Text.App.appName+" for the fist time!\nYou will now be asked to choose a directory to store your data in.\nPlease provide a valid location on your local hard-drive with sufficient free disk space.";
        public static String appDescription = appName+" provides the tools and integrated know-how to quickly and safley process your GWAS data, all in a single, easy to use application.\n\nOnce your genotype data is loaded into "+appName+"'s hierarchical database (netCDF), you will be able to manage, manipulate and transform it, as well as perform basic quality controls and association studies.\n"+appName+" will also generate charts, plots and reports for you.";
        public static String contact = "Contact information: fernando.muniz@upf.edu";

        public static String newVersionAvailable = "A new version of "+appName+" is available at http://www.gwaspi.org/.";
        public static String newVersionIsCompatible = "This version is backward compatible with your local one. ";
        public static String newVersionIsUnCompatible = "This version is NOT backward compatible with your local one and may cause your older\ndatabases not to function.";

        public static String license = "";//"License agreement: GPL";
        public static String cite = "Please cite Genome-wide Association Studies Pipeline (GWASpi): a desktop application for genome-wide SNP analysis and management, as in http://bioinformatics.oxfordjournals.org/content/early/2011/05/16/bioinformatics.btr301.abstract";
        public static String warnUnableToInitForFirstTime = org.gwaspi.constants.cGlobal.APP_NAME + " was unable to create the database on the \nspecified path. Please check if the write rights are enabled on \nthe given path or if there is enough space available";
        public static String Tab0 = Text.App.appName+" Management";
        public static String Tab1 = "Process Overview";
        public static String Tab2 = "Study Log";
        public static String processOutput = "Process Output";
        public static String warnProcessInterrupted = "Process has been interrupted!";
        public static String author = "Author: Fernando Muñiz Fernandez, Angel Carreño Torres, Carlos Morcillo-Suarez and Arcadi Navarro\nINB-Node8, UPF BioEvo Dept.";
        public static String whitePaper = "White paper: Genome-wide Association Studies Pipeline (GWASpi): a desktop application for genome-wide SNP analysis and management (unpublished)";
        public static String propertiesPaths = "Properties & Paths";
        public static String propertyName = "Property Name";
        public static String propertyValue = "Value";
        public static String warnPropertyRGB="You must insert an RGB (Red, Green Blue) value like '0,0,0' (black), '255,255,255' (white) or '150,0,0' (dark red).\nMinimum value for each element is 0, maximum is 255.";
        public static String warnMustBeNumeric="Field must contain valid numeric value (like 0.006 or 5E-7)!";
        public static String exit = "Exit";
        public static String preferences = "Preferences";
        public static String changeDataDir = "Change data directory";
        public static String confirmCopyDataDir = "This will copy all databases and genotypes to a new directory! Proceed?";
        public static String warnErrorCopyData = "Couldn't perform data copy!\nDo you have write permission?";
        public static String infoDataDirCopyOK = "The Data directory was copied successfully!\nYou may now remove the old directory.";
        public static String start = "Start";
        public static String warnOnlyOneInstance=Text.App.appName+" could not be started!\nIs there another instance of the program running?";
        public static String jobsStillPending = "There are still pending jobs in the processing queue!\nShut down anyway?";
        public static String outOfMemoryError = "ERROR: Out Of Memory!\nAre you trying to process to many SNPs for the currently allocated RAM?\nRefer to http://www.gwaspi.org/?page_id=632 for instructions on how to allocate more RAM.";

        public static String treeStudyManagement = "Study Management";
        public static String treeStudy = "Study";
        public static String treeSampleInfo = "Sample Info";
        public static String treeMatrix = "Matrix";
        public static String treeOperation = "Operation";
        public static String treeReport = "Report";
        public static String treeReferenceDBs = "Ref. Databases";
        public static String close="Close";
        public static String thanx="Special thanks to Angel Carreño";

    }

    public static class All {
        public static String optional = "[Optional]";
        public static String description = "Description";
        public static String nameAndDescription = "Name and Description";
        public static String insertDescription = "Insert Description...";
        public static String saveDescription = "Save Description";
        public static String mainProject = "Main project:";
        public static String Back = "Back";
        public static String browse = "Browse";
        public static String get = "Get";
        public static String go = "Go!";
        public static String yes= "Yes";
        public static String no = "No";
        public static String ok = "OK!";
        public static String save = "Save";
        public static String reset = "Reset";
        public static String cancel = "Cancel";
        public static String abort = "Abort";
        public static String next = "Next";
        public static String processing = "Processing...";
        public static String file1 = "File 1";
        public static String file2 = "File 2";
        public static String file3 = "File 3";
        public static String folder = "Folder";
        public static String createDate = "Create Date";

        public static String warnLoadError = "An error has ocurred while loading the data!";
        public static String warnWrongFormat = "Did you provide a file with the correct stated format?";
      
    }

    public static class Cli {
        public static String wrongScriptFilePath = "Unable to read start-up file!\nMake sure the specified file exists and try again.";
        public static String studyNotExist="Provided Study Id does not exist!";
        public static String availableStudies="Available Studies:\n";
        public static String doneExiting="\n"+Text.App.appName+" has finished processing your script.\nExiting....\nGood-bye!\n########################";
        
    }

    public static class Study {
        public static String study = "Study";
        public static String studies = "Studies";
        public static String addStudy = "Add Study";
        public static String createNewStudy = "Create New Study";
        public static String warnNoStudyName = "You must insert a Study name!";

        public static String studyName = "Study Name";
        public static String currentStudy = "Current Study:";
        public static String insertNewStudyName = "Insert Name of new Study...";
        public static String availableStudies = "Available Studies";
        public static String studyID = "StudyID";
        public static String deleteStudy = "Delete Study";
        public static String confirmDelete1 = "Do you really want to delete Studies as well as it's corresponding Sample Info?";
        public static String confirmDelete2 = "\nThis action cannot be undone!";

        public static String sampleInfo = "Sample Info";
        public static String loadSampleInfo = "Load Sample Info";
        public static String infoSampleInfo = "Next you must provide a valid Sample Info file.";
        public static String updateSampleInfo = "Update Sample Info";
        public static String warnMissingSampleInfo = "Warning! Your genotype files contains Samples that are not in your Sample Info file!\nGWASpi will fill dummy info for this Sample...";
    }

    public static class Matrix {
        public static String lblMatrix = "Matrix:";
        public static String matrix = "Matrix";
        public static String matrices = "Matrices";
        public static String exportMatrix = "Export Matrix";
        public static String deleteMatrix = "Delete Matrix";
        public static String trafoMatrix = "Transform Matrix";
        public static String confirmDelete1 = "Do you really want to delete Matrices? ";
        public static String confirmDelete2 = "\nThis action cannot be undone!";
        public static String loadGenotypes = "Load Genotype Data";
        public static String scanAffectionStandby = "Scanning Affection Status, standby...";
        public static String importGenotypes = "Import Genotypes";
        public static String newMatrixName = "New Matrix Name: ";
        public static String parentMatrix = "Parent Matrix: ";
        public static String matrixID  = "MatrixID";
        public static String currentMatrix  = "Current Matrix: ";
        public static String input  = "Input";
        public static String format = "Format";
        public static String annotationFile = "Annotation File";
        public static String lgenFile = "LGEN File";
        public static String sampleInfo = "Sample Info";
        public static String genotypes = "Genotypes";
        public static String mapFile = "MAP File";
        public static String pedFile = "PED File";
        public static String bedFile = "BED File";
        public static String sampleInfoOrFam = "FAM or Sample Info";;
        public static String bimFile = "BIM File";
        public static String findComplementaryPlink = "Should "+App.appName+" search for complementary map/ped files in the same directory?";
        public static String findComplementaryPlinkBinary = "Should "+App.appName+" search for complementary bed/bim/fam files in the same directory?";
        public static String markerFile = "Marker File";
        public static String warnCantLoadData1 = "Data could not be loaded!";
        public static String warnCantLoadData2 = "Data could not be loaded!\nDo all Samples listed in your Sample Info File have a corresponding\ngenotype file under the specified path?";
        public static String warnInputNewMatrixName = "You must insert a name for the Imported Matrix!";
        public static String warnInputFileInField = "You must insert a valid path in field ";
        public static String pleaseInsertMatrixName = "Please insert the name of the resulting Matrix!";
        public static String infoScanSampleAffection = App.appName+" will now scan your Samples for Case/Control Affection state.\nStandby...";
        public static String caseCtrlDetected = "You have provided Case/Control Affection info.\nShould "+App.appName+" perform a complete GWAS study now?";
        public static String ifCaseCtrlDetected = "In case your data contains Case/Control Affection info,\nshould "+App.appName+" perform a complete GWAS study after loading data?";
        public static String noJustLoad = "No, just load data";
        public static String gwasInOne = "GWAS in one?";

        public static String descriptionHeader1 = "Matrix created at: "; // add time
        public static String descriptionHeader2 = "Loaded using format: "; // add format
        public static String descriptionHeader3 = "From files: "; // add file path
        public static String descriptionHeader4 = "From Matrices: "; // add file path


        //public static String  = ;
    }

    public static class Reports {
        public static String doneReport = "===>Done writing report files!<===";
        public static String makeChartsAndReport = "Make Charts and Report";
        public static String viewReportSummary = "View Report Summary";
        public static String report = "Report";
        public static String summary = "Summary";
        public static String confirmDelete = "Should corresponding Report and Chart files in 'reports' folder be deleted as well?";
        public static String selectSaveMode = "Choose report to be saved";
        public static String completeReport = "Complete Report";
        public static String currentReportView = "Current Report View";
        public static String warnCantOpenFile = "Cannot open file!";

        public static String radio1Prefix = "Show";
        public static String radio1Suffix_pVal = "most significant p-Values.";
        public static String radio1Suffix_gen = "Most significant";
        public static String radio2Prefix_pVal = "Show p-Values below";

        public static String cannotOpenEnsembl = "Couldn't connect to Ensemble website";

        public static String chr = "Chr";
        public static String markerId = "MarkerID";
        public static String chiSqr = "X²";
        public static String pVal = "p-Value";
        public static String pos = "Pos";
        public static String rsId = "RsID";

        public static String externalResourceDB = "External Database Resources";
        public static String externalResource = "Ext. Resource";
        public static String warnExternalResource = "You need to click on a specific SNP for the selected\nexternal database resource to return data!";
        public static String ensemblLink = "Ensembl";
        public static String NCBILink = "NCBI";
        public static String zoom = "Zoom";
        public static String queryDB = "Query DB";

        public static String alleles = "Alleles";
        public static String majAallele = "Maj. Allele";
        public static String minAallele = "Min. Allele";
        public static String oddsRatio = "Odds Ratio";
        public static String ORAAaa = "OR AA/aa";
        public static String ORAaaa = "OR Aa/aa";
        public static String missRatio = "Missing-Ratio";
        public static String genQA = "QA";
        public static String hwObsHetzy = "OBS_HETZY_";
        public static String hwExpHetzy = "EXP_HETZY_";
        public static String hwPval = "HW_p-Value_";
        public static String trendTest = "Trend X²";

        public static String CASE = "CASE";
        public static String CTRL = "CTRL";
        public static String ALL = "ALL";

        public static String sampleId = "SampleID";
        public static String familyId = "FamilyID";
        public static String sex = "Sex";
        public static String affection = "Affection";
        public static String age = "Age";
        public static String category = "Category";
        public static String disease = "Disease";
        public static String population = "Population";
        public static String fatherId = "FatherID";
        public static String motherId = "MotherID";
        public static String smplHetzyRat = "Hetzyg. ratio";

        public static String smplHetzyVsMissingRat = "Sample QA, Heterozygosity vs Missing Ratio";

        public static String backToTable = "Back to Table";
        public static String backToManhattanPlot = "Back to Manhattan Plot";
        public static String threshold = "Threshold";
        public static String thresholds = "Thresholds";
        public static String redraw = "Redraw";
        public static String heterozygosity = "Heterozygosity";


        


       


    }

    public static class Operation {
        public static String operation = "Operation";
        public static String newOperation = "New Operation";
        public static String analyseData = "Analyse Data";
        public static String generateReports = "Generate Reports";
        public static String htmlPerformQA = "<html><div align='center'>Missingnes QA<br>Heterozygosity<div></html>";

        public static String infoPerformQA = "The Case/Control Census will be performed according to the initial Sample Affection you provided at load-time.";
        public static String htmlPhenoFileCensus = "<html><div align='center'>Genotypes freq. based on external Phenotype file<div></html>";
        public static String infoPhenotypeCensus = "Next you must provide a file specifying the Phenotype status of every Sample.";
        public static String htmlAffectionCensus = "<html><div align='center'>Genotypes freq. based on Affection status<div></html>";
        public static String htmlPhenoMatrixCensus = "<html><div align='center'>Genotypes freq. based on Phenotype DB<div></html>";

        public static String htmlGTFreqAndHW = "<html><div align='center'>Genotype freq. & Hardy-Weingerg QA<div></html>";
        public static String GTFreqAndHW = "Genotype freq. & Hardy-Weingerg QA";
        public static String GTFreqAndHWFriendlyName = "Type in a name for this Genotype frequency count";
        public static String htmlPerformHW = "<html><div align='center'>Perfom Hardy-Weinberg equilibrium test<div></html>";

        //public static String htmlAllelicAssocTest = "<html><div align='center'>Perform Allelic, Genotypic &<br>Armitage Trend Tests<div></html>";
        public static String htmlAllelicAssocTest = "<html><div align='center'>Allelic Association Test<div></html>";
        public static String htmlGenotypicTest = "<html><div align='center'>Genotypic Association Test<div></html>";
        public static String htmlTrendTest = "<html><div align='center'>Cochran-Armitage Trend Test<div></html>";
        public static String htmlBlank = "<html><div align='center'><div></html>";
        public static String warnQABeforeAnything = "You must perform a Samples & Markers Quality Assurance before making a GWAS!";
        public static String warnOperationsMissing = "Some neccesary previous Operations are missing!";
        public static String willPerformOperation = App.appName+" will now complete these missing Operation(s)...";
        public static String warnAffectionMissing = "You must provide a valid Case/Control input!\nUse the '"+Study.updateSampleInfo+"' feature in the Study management section.";
        public static String warnCensusBeforeHW = "You must perform the Census and QA tests before the Hardy-Weinberg test!\nShould "+App.appName+" perform these now instead?";
        public static String warnAllreadyExists = "The Operation you are trying to perform already exists!\nDelete existing operation and Try again.";
        public static String warnAllreadyExistsProceed = "The Operation you are trying to perform already exists!\nProceed anyway?";
        public static String warnOperationError="There has been an error while performing operation!";
        public static String warnNoDataLeftAfterPicking = "There was no data left to operate on after applying threshold filters!\nNo further Operations performed.";
        public static String operationId = "OperationID";
        public static String operationName = "Operation Name";
        public static String deleteOperation = "Delete Operation";

        public static String hardyWeiberg = "Hardy Weinberg";
        public static String qaData = "QA Data";
        public static String confirmDelete1 = "Do you really want to delete the selected Operation(s)?";
        public static String confirmDelete2 = "?\nThis action cannot be undone!";

        public static String allelicAssocTest = "Allelic Association Test";
        public static String genoAssocTest = "Genotypic Association Test";
        public static String trendTest = "Cochran-Armitage Trend Test";

        public static String chosePhenotype = "Affection or Phenotype File";
        public static String genotypeFreqAndHW = "Do you want to use current Case/Conrol Affection info from the Samples DB\nor update the Samples DB Affection from an external Sample Info file?";
        public static String htmlCurrentAffectionFromDB = "<html><div align='center'> Current Case/Control <br> Affection from DB </div></html>";
        public static String htmlAffectionFromFile = "<html><div align='center'> Update Case/Control <br> Affection from File </div></html>";
        public static String addPhenotypes = "Add Phenotypes";

        public static String gwasInOneGo = "GWAS In One Go";
        public static String performAllelicTests = "Allelic tests";
        public static String performGenotypicTests = "Genotypic tests";
        public static String performTrendTests = "Trend tests";
        public static String discardMismatch = "Discard mismatching Markers?";
        public static String discardMarkerMissing = "Discard Markers with missingness    >";
        public static String discardMarkerHetzy = "Discard Markers with heterozygosity    >";
        public static String discardMarkerHWCalc1 = "Discard Markers with Hardy-Weinberg p-Value  <";
        public static String discardMarkerHWCalc2 = "(0.05 / Markers Nb)";
        public static String discardMarkerHWFree = "Discard Markers with Hardy-Weinberg p-Value  <";
        public static String discardSampleMissing = "Discard Samples with missingness   >";
        public static String discardSampleHetzy = "Discard Samples with heterozygosity    >";






    }

    public static class Trafo {
        public static String doneExtracting = "===>Done extracting to new Matrix!<===";
        public static String extratedMatrixDetails = "New Matrix Details";
        public static String trafoMatrixDetails = "New Matrix Details";
        public static String criteriaReturnsNoResults = "The provided criteria has returned 0 matches!";
        public static String markerSelectZone = "Marker Selection";
        public static String filterMarkersBy = "Filter Markers By:";
        public static String variable = "Property / Variable: ";
        public static String criteria = "Criteria:";
        public static String criteriaFile = "Criteria File:";
        public static String sampleSelectZone = "Sample Selection";
        public static String filterSamplesBy = "Filter Samples By:";
        public static String inputMatrix = "Input Matrix:";
        public static String htmlTranslate1 = "<html><div align='center'>Translate AB or 12 to ACGT<div></html>";
        public static String htmlTranslate2 = "<html><div align='center'>Translate 1234 to ACGT<div></html>";
        public static String htmlTranslate3 = "<html><div align='center'>Translate 12 to ACGT<div></html>";
        public static String htmlForceAlleleStrand = "Force Alleles Strand";

        public static final String mergeMatrices = "Merge Matrices";
        public static final String mergeWithMatrix = "Matrix to be added";
        public static final String selectMatrix = "Select Matrix: ";
        public static final String mergeMarkersOnly = "Merge new Markers only";
        public static final String mergeSamplesOnly = "Merge new Samples only";
        public static final String mergeAll = "Merge Markers & Samples";
        public static final String merge = "Merge";
        public static final String warnSelectMergeMethod = "You must choose the merge method to use!";

        public static final String mergedFrom = "Matrix merged from parent Matrices:";
        public static final String mergeMethodMarkerJoin = "· The SampleSet from the 1st Matrix will be used in the result Matrix.\n· No new Samples from the 2nd Matrix will be added.\n· Markers from the 2nd Matrix will be merged in chromosome and position order to the\n  MarkersSet from the 1st Matrix.\n· Duplicate genotypes from the 2nd Matrix will overwrite genotypes from the 1st Matrix.";
        public static final String mergeMethodSampleJoin = "· The MarkerSet from the 1st Matrix will be used in the result Matrix.\n· No new Markers from the 2nd Matrix will be added.\n· Samples from the 2nd Matrix will be appended to the end of the SampleSet from the 1st Matrix.\n· Duplicate genotypes from the 2nd Matrix will overwrite genotypes in the 1st Matrix.";
        public static final String mergeMethodMergeAll = "· MarkerSets from the 1st and 2nd Matrix will be merged in the result Matrix.\n· Samples from the 2nd Matrix will be appended to the end of the SampleSet from the 1st Matrix.\n· Duplicate Genotypes from the 1st and 2nd matrix will be resolved by overwriting the 1st Matrix\n values by the 2nd Matrix values.";
        public static String htmlAddMatrixSamples = "<html><div align='center'>Add Samples To Matrix.<div></html>";
        public static String htmlAddMatrixMarkers = "<html><div align='center'>Add Markers To Matrix.<div></html>";
        public static String successMatricesJoined = "Matrix joined correctly!";
        public static String warnMatrixEncMismatch = "Matrix genotypes are encoded differently.\nTranslate first!";
        public static String warnMatrixEncUnknown = "Matrix genotypes encoding is unknown.\nCannot proceed!";
        public static String warnStrandUndet = "Part of the genotypes have an undetermined strand-placement\nContinue anyway?";
        public static String warnStrandMismatch = "The genotypes are placed on different strands.\nFlip strands first!";
        public static String warnExcessMismatch = "Matrix joined. \nThe acceptable number of mismatching genotypes has been crossed!";

        public static String warnNoDictionary = "The parent matrix you are trying to translate has no dictionnary!";
        public static String warnNotACGT = "The parent matrix you are trying to modify is not encoded as "+cNetCDF.Defaults.GenotypeEncoding.ACGT0.toString();
        public static String warnNot1234 = "The parent matrix you are trying to modify is not encoded as "+cNetCDF.Defaults.GenotypeEncoding.O1234;
        public static String warnNotAB12 = "The parent matrix you are trying to modify is not encoded as "+cNetCDF.Defaults.GenotypeEncoding.AB0+" or "+cNetCDF.Defaults.GenotypeEncoding.O12;
        public static String warnNotACGTor1234 = "The parent matrix you are trying to modify is not encoded as "+cNetCDF.Defaults.GenotypeEncoding.ACGT0.toString()+" or "+cNetCDF.Defaults.GenotypeEncoding.O1234;

        public static String extract = "Extract";
        public static String extractToNewMatrix = "Extract data to new Matrix";
        public static String exportMatrix = "Export Matrix";
        public static String translateMatrix = "Translate Matrices";
        public static String transformMatrix = "Translation & Stranding";
        public static String extractData = "Extract Data";
        public static String flipStrand = "Flip Strand";

    }

    public static class Processes {
        public static String processes = "Processes";
        public static String processOverview = "Process Overview";
        public static String processLog = "Process Log";
        public static String launchTime = "Launch Time";
        public static String startTime = "Start Time";
        public static String id = "ID";
        public static String processeName = "Process Name";
        public static final String endTime = "End Time";
        public static final String queueState = "Activity";
        public static final String cantDeleteRequiredItem = App.appName+" can't delete this Item!\nThis Item is required for a process in the pending processes queue.";
        public static final String abortingProcess = "********* Process Aborted! **********";
    }


    public static class Help {
        public static String riddle = "?";
        public static String launchError = "Error attempting to launch web browser\n";
        public static String help = "Help";
        public static String aboutHelp = "Pick a link to open a Help subject";
    }

    public static class Dialog {
        public static String chromosome = "Chromosome";
        public static String strand = "Strand";
        public static String genotypeEncoding = "Genotype encoding";
    }

}
