package netCDF;


import java.util.List;
import ucar.ma2.*;
import ucar.nc2.*;
import java.io.IOException;
import java.util.Date;
import java.util.LinkedHashMap;



public class BenchmarkReadCompareD3Array {
    
    public static void main(String[] arg) throws InvalidRangeException, IOException {
        int method = 2; //1=int, 2=byte, 3=char
        int markerNb = 100000;
        String filename = "/media/data/work/GWASpi/genotypes/method"+method+"mk"+markerNb+".nc";
        NetcdfFile ncfile = null;

        long timeAverage=0;
        
        try{
            ncfile = NetcdfFile.open(filename);
            List<Dimension> dims= ncfile.getDimensions();
            Dimension samplesDim = dims.get(0);
            Dimension markersDim = dims.get(1);
            Dimension allelesDim = dims.get(2);
                    
            String varName = "genotypes";
            Variable genotypes = ncfile.findVariable(varName);
            if (null == genotypes) return;

            try {
                switch (method) {
                    case 1: //int
                        for(int sampleNb=0;sampleNb<samplesDim.getLength();sampleNb++){
                            Date start = new Date();
                            ArrayInt.D3 rdArray = (ArrayInt.D3) genotypes.read(sampleNb+":"+sampleNb+":1,"
                                                                                  + " 0:"+(markersDim.getLength()-1)+":1,"
                                                                                  + " 0:"+(allelesDim.getLength()-1)+":1");

                            Date end = new Date();
                            long tmpTime = end.getTime() - start.getTime();
                            //System.out.println("tmpTime: "+tmpTime);
                            timeAverage = ((timeAverage*sampleNb)+tmpTime)/(sampleNb+1);
                            if(sampleNb%10==0){
                                System.out.println("Processing "+sampleNb+" at "+global.Utils.getMediumDateTimeAsString());
                            }
                        }
                        System.out.println("Time average with int: "+timeAverage+"");
                        break;
                    case 2: //byte
                        for(int sampleNb=0;sampleNb<samplesDim.getLength();sampleNb++){
                            Date start = new Date();
                            ArrayByte.D3 rdArray = (ArrayByte.D3) genotypes.read(sampleNb+":"+sampleNb+":1,"
                                                                                  + " 0:"+(markersDim.getLength()-1)+":1,"
                                                                                  + " 0:"+(allelesDim.getLength()-1)+":1");



                            int[] shape = rdArray.getShape();
                            Index index = rdArray.getIndex();
                            LinkedHashMap stringLHM = new LinkedHashMap();
                            for (int i=0; i<shape[0]; i++) {
                                //ArrayChar wrCharArray = new ArrayChar ( new int[] {1, shape[1]} );
                                //ArrayChar.D2.arraycopy(rdArray, i*shape[1], wrCharArray, 0, shape[1]);

                                ArrayByte rdByteArray = new ArrayByte ( new int[] {1, shape[1]} );
                                ArrayByte.D2.arraycopy(rdArray, i*shape[1], rdByteArray, 0, shape[1]);

                                //byte[] values = (byte[]) rdByteArray.copyTo1DJavaArray();
                                byte[] values = (byte[]) rdByteArray.copyTo1DJavaArray();
                                for(int h=0;h<values.length;h=h+2){
                                    stringLHM.put(h/2, Byte.toString(values[h])+Byte.toString(values[h+1]));
                                }
                                
                                

                            }

                            Date end = new Date();
                            long tmpTime = end.getTime() - start.getTime();
                            //System.out.println("tmpTime: "+tmpTime);
                            timeAverage = ((timeAverage*sampleNb)+tmpTime)/(sampleNb+1);
                            if(sampleNb%10==0){
                                System.out.println("Processing "+sampleNb+" at "+global.Utils.getMediumDateTimeAsString());
                            }
                        }
                        System.out.println("Time average with byte: "+timeAverage+"");
                        break;
                    case 3: //char
                        for(int sampleNb=0;sampleNb<samplesDim.getLength();sampleNb++){
                            Date start = new Date();
                            ArrayChar.D3 gt_ACD3 = (ArrayChar.D3) genotypes.read("("+sampleNb+":"+sampleNb+":1, "
                                                                                + "0:"+(markersDim.getLength()-1)+":1, "
                                                                                + "0:"+(allelesDim.getLength()-1)+":1)");
//                            ArrayChar.D3 rdArray = (ArrayChar.D3) genotypes.read("("+sampleNb+":"+sampleNb+":1,"
//                                                                                  + " 0:"+(markersDim.getLength()-1)+":1,"
//                                                                                  + " 0:"+(allelesDim.getLength()-1)+":1)");
                            Date end = new Date();
                            long tmpTime = end.getTime() - start.getTime();
                            //System.out.println("tmpTime: "+tmpTime);
                            timeAverage = ((timeAverage*sampleNb)+tmpTime)/(sampleNb+1);
                            if(sampleNb%10==0){
                                System.out.println("Processing "+sampleNb+" at "+global.Utils.getMediumDateTimeAsString());
                            }
                        }
                        System.out.println("Time average with char: "+timeAverage+"");
                        break;
                }
                //Array gt = genotypes.read("0:0:1, 0:9:1, 0:1:1"); //sample 1, snp 0 - 10, alleles 0+1



                
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

}

