package org.gwaspi.netCDF.exporter;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */

import org.gwaspi.constants.cExport.ExportFormat;
import java.io.IOException;
import java.util.LinkedHashMap;
import org.gwaspi.netCDF.markers.MarkerSet;
import org.gwaspi.netCDF.matrices.*;
import org.gwaspi.samples.SampleSet;
import ucar.ma2.*;

public class MatrixExporter {

    protected int studyId = Integer.MIN_VALUE;
    protected int rdMatrixId = Integer.MIN_VALUE;

    MatrixMetadata rdMatrixMetadata = null;
    MarkerSet rdMarkerSet = null;
    SampleSet rdSampleSet = null;
    LinkedHashMap rdMarkerIdSetLHM = null;
    LinkedHashMap rdSampleSetLHM = null;


    public MatrixExporter(int _rdMatrixId) throws IOException, InvalidRangeException{

        /////////// INIT EXTRACTOR OBJECTS //////////

        rdMatrixId = _rdMatrixId;
        rdMatrixMetadata = new MatrixMetadata(rdMatrixId);
        studyId = rdMatrixMetadata.getStudyId();

        rdMarkerSet = new MarkerSet(rdMatrixMetadata.getStudyId(),rdMatrixId);
        rdMarkerIdSetLHM = rdMarkerSet.getMarkerIdSetLHM();

        rdSampleSet = new SampleSet(rdMatrixMetadata.getStudyId(),rdMatrixId);
        rdSampleSetLHM = rdSampleSet.getSampleIdSetLHM();

    }

    public boolean exportToFormat(String exportFormat) throws IOException{
        System.out.println(org.gwaspi.global.Text.All.processing);

        String exportPath = org.gwaspi.global.Config.getConfigValue("ExportDir", "");
        org.gwaspi.global.Utils.createFolder(exportPath, "STUDY_"+rdMatrixMetadata.getStudyId());
        exportPath = exportPath + "/STUDY_"+rdMatrixMetadata.getStudyId();
        boolean result=false;
        switch(ExportFormat.compareTo(exportFormat)){
            case PLINK:
                result=PlinkFormatter.exportToPlink(exportPath,
                                                     rdMatrixMetadata,
                                                     rdMarkerSet,
                                                     rdMarkerIdSetLHM,
                                                     rdSampleSetLHM);
                break;
            case PLINK_Transposed:
                result=PlinkFormatter.exportToTransposedPlink(exportPath,
                                                     rdMatrixMetadata,
                                                     rdMarkerSet,
                                                     rdSampleSet,
                                                     rdMarkerIdSetLHM,
                                                     rdSampleSetLHM);
                break;
            case BEAGLE:
                result=BeagleFormatter.exportToBeagle(exportPath,
                                                     rdMatrixMetadata,
                                                     rdMarkerSet,
                                                     rdSampleSet,
                                                     rdMarkerIdSetLHM,
                                                     rdSampleSetLHM);
                break;
            case GWASpi:
                result=GWASpiFormatter.exportGWASpiFiles(exportPath,
                                                     rdMatrixMetadata,
                                                     rdMarkerSet,
                                                     rdSampleSet,
                                                     rdMarkerIdSetLHM,
                                                     rdSampleSetLHM);
                break;
            case Spreadsheet:
                result=FleurFormatter.exportToFleurFormat(exportPath,
                                                          rdMatrixMetadata,
                                                          rdMarkerSet,
                                                          rdMarkerIdSetLHM,
                                                          rdSampleSetLHM);
                break;
            case MACH:
                result=MachFormatter.exportToMach(exportPath,
                                                          rdMatrixMetadata,
                                                          rdMarkerSet,
                                                          rdMarkerIdSetLHM,
                                                          rdSampleSetLHM);
                break;
        }

        org.gwaspi.global.Utils.sysoutCompleted("exporting Matrix"+exportPath);
        return result;
    }

}
