package netCDF;


import ucar.ma2.*;
import ucar.nc2.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;



public class PrototypeReadNetcdf {
    
    public static void main(String[] arg) throws InvalidRangeException, IOException {

        String filename = "/media/data/work/moapi/genotypes/prototype.nc";
        NetcdfFile ncfile = null;

        int gtSpan = 2;
        LinkedHashMap lhm = new LinkedHashMap();
        for(int i=0;i<10;i++){
            lhm.put(i, "00");
        }
        
        try{
            ncfile = NetcdfFile.open(filename);
            
            String varName = "genotypes"; 
            Variable genotypes = ncfile.findVariable(varName);
            if (null == genotypes) return;
            try {
                //Array gt = genotypes.read("0:0:1, 0:9:1, 0:1:1"); //sample 1, snp 0 - 10, alleles 0+1
                ArrayChar.D3 gt = (ArrayChar.D3) genotypes.read("0:0:1, 0:9:1, 0:1:1");
                NCdump.printArray(gt, varName, System.out, null);
                
                LinkedHashMap filledLhm = fillLinkedHashMap(lhm, gt, gtSpan);
                
                int stopme = 0;

                
            } catch (IOException ioe) {
                System.out.println("Cannot read data: "+ioe);
            } catch (InvalidRangeException e) {
                System.out.println("Cannot read data: "+e);
            }
            
            
            
        } catch (IOException ioe) {
            System.out.println("Cannot open file: "+ioe);
        } finally { 
            if (null != ncfile) try {
                ncfile.close();
            } catch (IOException ioe) {
                System.out.println("Cannot close file: "+ioe);
            }
        }

    }
    
    public static LinkedHashMap fillLinkedHashMap(LinkedHashMap lhm, Array inputArray, int gtSpan){
        StringBuffer alleles = new StringBuffer("");
        int lhmIndex=0;
        int alleleCount=0;
        for (int i=0;i<inputArray.getSize();i++){
            if(alleleCount == gtSpan){
                lhm.put(lhmIndex, alleles);
                alleles = new StringBuffer("");
                alleleCount = 0;
                lhmIndex++;
            }
            char c = inputArray.getChar(i);
            alleles.append(c);
            alleleCount++;
        }
        lhm.put(lhmIndex, alleles);
        return lhm;
    }
}
