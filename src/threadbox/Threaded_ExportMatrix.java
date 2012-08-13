/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package threadbox;

import constants.cExport.ExportFormat;
import global.Text;
import java.util.logging.Level;
import java.util.logging.Logger;
import netCDF.exporter.MatrixExporter_opt;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class Threaded_ExportMatrix  implements Runnable {

    Thread runner;
    protected String timeStamp="";
    protected boolean startWithGUI=gui.StartGWASpi.guiMode;

    public int matrixId;
    public ExportFormat format;
    public String phenotype;

    public Threaded_ExportMatrix(String threadName, 
                                 String _timeStamp,
                                 int _matrixId,
                                 ExportFormat _format,
                                 String _phenotype) {
        try {
            timeStamp=_timeStamp;
            global.Utils.sysoutStart("Exporting Matrix");
            global.Config.initPreferences(false, null);
            matrixId = _matrixId;
            format = _format;
            phenotype = _phenotype;
            runner = new Thread(this, threadName); // (1) Create a new thread.
            runner.start(); // (2) Start the thread.
            runner.join();
        } catch (InterruptedException ex) {
            //Logger.getLogger(Threaded_ExportMatrix.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @SuppressWarnings("static-access")
    public void run() {
        SwingWorkerItem thisSwi = SwingWorkerItemList.getSwingWorkerItemByTimeStamp(timeStamp);

        if (format!=null) {
            try {
                switch (constants.cExport.ExportFormat.compareTo(format.toString())) {
                    case PLINK:
                        if(startWithGUI){gui.ProcessTab.showTab();}
                        MatrixExporter_opt mEx = new MatrixExporter_opt(matrixId);
                        mEx.exportToFormat(constants.cExport.ExportFormat.PLINK.toString(), phenotype);
                        break;
                    case PLINK_Binary:
                        if(startWithGUI){gui.ProcessTab.showTab();}
                        mEx = new MatrixExporter_opt(matrixId);
                        mEx.exportToFormat(constants.cExport.ExportFormat.PLINK_Binary.toString(), phenotype);
                        break;
                    case Eigensoft_Eigenstrat:
                        if(startWithGUI){gui.ProcessTab.showTab();}
                        mEx = new MatrixExporter_opt(matrixId);
                        mEx.exportToFormat(constants.cExport.ExportFormat.Eigensoft_Eigenstrat.toString(), phenotype);
                        break;
                    case PLINK_Transposed:
                        if(startWithGUI){gui.ProcessTab.showTab();}
                        mEx = new MatrixExporter_opt(matrixId);
                        mEx.exportToFormat(constants.cExport.ExportFormat.PLINK_Transposed.toString(), phenotype);
                        break;
                    case BEAGLE:
                        if(startWithGUI){gui.ProcessTab.showTab();}
                        mEx = new MatrixExporter_opt(matrixId);
                        mEx.exportToFormat(constants.cExport.ExportFormat.BEAGLE.toString(), phenotype);
                        break;
                    case GWASpi:
                        if(startWithGUI){gui.ProcessTab.showTab();}
                        mEx = new MatrixExporter_opt(matrixId);
                        mEx.exportToFormat(constants.cExport.ExportFormat.GWASpi.toString(), phenotype);
                        break;
                    case Spreadsheet:
                        if(startWithGUI){gui.ProcessTab.showTab();}
                        mEx = new MatrixExporter_opt(matrixId);
                        mEx.exportToFormat(constants.cExport.ExportFormat.Spreadsheet.toString(), phenotype);
                        break;
                    case MACH:
                        if(startWithGUI){gui.ProcessTab.showTab();}
                        mEx = new MatrixExporter_opt(matrixId);
                        mEx.exportToFormat(constants.cExport.ExportFormat.MACH.toString(), phenotype);
                        break;
                }
                MultiOperations.printFinished("Exporting Matrix");
                MultiOperations.swingWorkerItemList.flagCurrentItemDone(timeStamp);
                MultiOperations.updateTree();
                MultiOperations.updateProcessOverviewStartNext();
            } catch (OutOfMemoryError e) {
                System.out.println(Text.App.outOfMemoryError);
            } catch (Exception ex) {
                Logger.getLogger(Threaded_ExportMatrix.class.getName()).log(Level.SEVERE, null, ex);
                MultiOperations.printError("Exporting Matrix");
                try {
                    MultiOperations.swingWorkerItemList.flagCurrentItemError(timeStamp);
                    MultiOperations.updateTree();
                    MultiOperations.updateProcessOverviewStartNext();
                } catch (Exception ex1) {
                }
            }
        }

    }

}
