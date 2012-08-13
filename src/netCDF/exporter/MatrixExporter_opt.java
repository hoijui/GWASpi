package netCDF.exporter;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */

import constants.cExport.ExportFormat;
import java.io.IOException;
import java.util.LinkedHashMap;
import netCDF.markers.MarkerSet_opt;
import netCDF.matrices.*;
import samples.SampleSet;
import ucar.ma2.*;

public class MatrixExporter_opt {

    protected int studyId = Integer.MIN_VALUE;
    protected int rdMatrixId = Integer.MIN_VALUE;

    MatrixMetadata rdMatrixMetadata = null;
    MarkerSet_opt rdMarkerSet = null;
    SampleSet rdSampleSet = null;
    LinkedHashMap rdMarkerIdSetLHM = null;
    LinkedHashMap rdSampleSetLHM = null;


    public MatrixExporter_opt(int _rdMatrixId) throws IOException, InvalidRangeException{

        /////////// INIT EXTRACTOR OBJECTS //////////

        rdMatrixId = _rdMatrixId;
        rdMatrixMetadata = new MatrixMetadata(rdMatrixId);
        studyId = rdMatrixMetadata.getStudyId();

        rdMarkerSet = new MarkerSet_opt(rdMatrixMetadata.getStudyId(),rdMatrixId);

        rdSampleSet = new SampleSet(rdMatrixMetadata.getStudyId(),rdMatrixId);
        rdSampleSetLHM = rdSampleSet.getSampleIdSetLHM();

    }

    public boolean exportToFormat(String exportFormat, String phenotype) throws IOException{
        System.out.println(global.Text.All.processing);

        String exportPath = global.Config.getConfigValue("ExportDir", "");
        global.Utils.createFolder(exportPath, "STUDY_"+rdMatrixMetadata.getStudyId());
        exportPath = exportPath + "/STUDY_"+rdMatrixMetadata.getStudyId();
        boolean result=false;
        switch(ExportFormat.compareTo(exportFormat)){
            case PLINK:
                result=PlinkFormatter_opt.exportToPlink(exportPath,
                                                     rdMatrixMetadata,
                                                     rdMarkerSet,
                                                     rdSampleSetLHM);
                break;
            case PLINK_Transposed:
                result=PlinkFormatter_opt.exportToTransposedPlink(exportPath,
                                                     rdMatrixMetadata,
                                                     rdMarkerSet,
                                                     rdSampleSet,
                                                     rdSampleSetLHM);
                break;
            case PLINK_Binary:
                result=PlinkFormatter_opt.exportToBinaryPlink(exportPath,
                                                     rdMatrixMetadata,
                                                     rdMarkerSet,
                                                     rdSampleSet,
                                                     rdSampleSetLHM);
                break;
            case Eigensoft_Eigenstrat:
                result=EigensoftFormatter.exportToBinaryEigensoft(exportPath,
                                                     rdMatrixMetadata,
                                                     rdMarkerSet,
                                                     rdSampleSet,
                                                     rdSampleSetLHM,
                                                     phenotype);
                break;
            case BEAGLE:
                MarkerSet_opt rdMarkerSet_opt = new MarkerSet_opt(rdMatrixMetadata.getStudyId(),rdMatrixId);
                result=BeagleFormatter_opt.exportToBeagle(exportPath,
                                                     rdMatrixMetadata,
                                                     rdMarkerSet_opt,
                                                     rdSampleSet,
                                                     rdSampleSetLHM);
                break;
            case GWASpi:
                result=GWASpiFormatter_opt.exportGWASpiFiles(exportPath,
                                                     rdMatrixMetadata,
                                                     rdSampleSetLHM);
                break;
            case Spreadsheet:
                result=SpreadsheetFormatter_opt.exportToFleurFormat(exportPath,
                                                          rdMatrixMetadata,
                                                          rdMarkerSet,
                                                          rdSampleSetLHM);
                break;
            case MACH:
                result=MachFormatter_opt.exportToMach(exportPath,
                                                          rdMatrixMetadata,
                                                          rdMarkerSet,
                                                          rdSampleSetLHM);
                break;
        }

        if (rdMarkerSet.markerIdSetLHM!=null) {
            rdMarkerSet.markerIdSetLHM.clear();
        }

        global.Utils.sysoutCompleted("exporting Matrix"+exportPath);
        return result;
    }

}
