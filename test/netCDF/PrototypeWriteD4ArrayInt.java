package netCDF;


import java.util.List;
import ucar.ma2.*;
import ucar.nc2.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;


public class PrototypeWriteD4ArrayInt {
    
    public static void main(String[] arg) throws InvalidRangeException, IOException {

        String filename = "/media/data/work/moapi/genotypes/prototype.nc";
        NetcdfFileWriteable ncfile = NetcdfFileWriteable.createNew(filename, false);

        // add dimensions
        Dimension markersDim = ncfile.addDimension("markers", 100);
        Dimension boxesDim = ncfile.addDimension("boxes", 4); //0=>AA, 1=>Aa, 2=>aa, 3=>00
        ArrayList punettSpace = new ArrayList();
        punettSpace.add(markersDim);
        punettSpace.add(boxesDim);
        
        // define Variable
        ncfile.addVariable("contingencies", DataType.INT, punettSpace);

                
        // create the file
        try {
            ncfile.create();
        } catch (IOException e) {
            System.err.println("ERROR creating file "+ncfile.getLocation()+"\n"+e);
        }
        
        
        ////////////// FILL'ER UP! ////////////////
        
        ArrayInt intArray = new ArrayInt.D2(markersDim.getLength(),boxesDim.getLength());
        int i,j;
        Index ima = intArray.getIndex();
        
        
        int method = 1;
        switch (method) {
            case 1: 
                // METHOD 1: Feed the complete genotype in one go
                Random generator = new Random();
                for (i=0; i<markersDim.getLength(); i++) {
                    for (j=0; j<boxesDim.getLength(); j++) {
                        int rnd = generator.nextInt(50*(j+1));
                        intArray.setInt(ima.set(i, j), rnd);
                        //System.out.println("SNP: "+i);
                    }
                }
                break;
            case 3: 
                //METHOD 3: One sample at a time -> feed in all snps
                break;
        }
        
        
        
        int[] offsetOrigin = new int[2]; //0,0
        try {
            ncfile.write("contingencies", offsetOrigin, intArray);
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
