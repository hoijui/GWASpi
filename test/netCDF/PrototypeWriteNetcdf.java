package netCDF;


import java.util.List;
import ucar.ma2.*;
import ucar.nc2.*;
import java.io.IOException;


public class PrototypeWriteNetcdf {
    
    public static void main(String[] arg) throws InvalidRangeException, IOException {
        
        NetcdfFileWriteable ncfile = netCDF.CreateNetcdf.setDimsAndAttributes(0, 
                                                                          "INTERNAL", 
                                                                          "test in PrototypeWriteNetcdf", 
                                                                          "+/-", 
                                                                          5,
                                                                          10);
                
        // create the file
        try {
            ncfile.create();
        } catch (IOException e) {
            System.err.println("ERROR creating file "+ncfile.getLocation()+"\n"+e);
        }
        
        
        ////////////// FILL'ER UP! ////////////////
        List<Dimension> dims = ncfile.getDimensions();
        Dimension samplesDim = dims.get(0);
        Dimension markersDim = dims.get(1);
        Dimension markerSpanDim = dims.get(2);
        
        ArrayChar charArray = new ArrayChar.D3(samplesDim.getLength(),markersDim.getLength(),markerSpanDim.getLength());
        int i,j;
        Index ima = charArray.getIndex();
        
        
        int method = 1;
        switch (method) {
            case 1: 
                // METHOD 1: Feed the complete genotype in one go
                for (i=0; i<samplesDim.getLength(); i++) {
                    for (j=0; j<markersDim.getLength(); j++) {
                        char c = (char) ((char) j + 65);
                        String s = Character.toString(c) + Character.toString(c);
                        charArray.setString(ima.set(i,j,0),s);
                        System.out.println("SNP: "+i);
                    }
                }
                break;
            case 2: 
                //METHOD 2: One snp at a time -> feed in all samples
                for (i=0; i<markersDim.getLength(); i++) {
                    charArray.setString(ima.set(i,0), "s"+i+"I0");
                    System.out.println("SNP: "+i);
                }
                break;
            case 3: 
                //METHOD 3: One sample at a time -> feed in all snps
                break;
        }
        
        
        
        int[] offsetOrigin = new int[3]; //0,0
        try {
            ncfile.write("genotypes", offsetOrigin, charArray);
            //ncfile.write("genotype", origin, A);
        } catch (IOException e) {
            System.err.println("ERROR writing file");
        } catch (InvalidRangeException e) {
            e.printStackTrace();
        }
        
        // close the file
        try {
            ncfile.close();
        } catch (IOException e) {
            System.err.println("ERROR creating file "+ncfile.getLocation()+"\n"+e);
        }

    }
    
    
}
