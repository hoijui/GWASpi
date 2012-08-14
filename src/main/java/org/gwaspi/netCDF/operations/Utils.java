package org.gwaspi.netCDF.operations;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import ucar.ma2.*;
import ucar.nc2.*;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class Utils {

    //////// HELPER METHODS ////////

    //<editor-fold defaultstate="collapsed" desc="SAVERS">

        public static boolean saveCharLHMKeyToWrMatrix(NetcdfFileWriteable wrNcFile, LinkedHashMap wrLHM, String variable, int varStride){
            boolean result=false;

            try {
                ArrayChar.D2 markersD2 = org.gwaspi.netCDF.operations.Utils.writeLHMKeysToD2ArrayChar(wrLHM, varStride);

                int[] markersOrig = new int[]{0, 0};
                try {
                    wrNcFile.write(variable, markersOrig, markersD2);
                    System.out.println("Done writing "+variable+" at " + org.gwaspi.global.Utils.getMediumDateTimeAsString());
                    result=true;
                } catch (IOException e) {
                    System.err.println("ERROR writing file");
                } catch (InvalidRangeException e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                System.out.println("ERROR writing "+variable+" at " + org.gwaspi.global.Utils.getMediumDateTimeAsString());
            }

            return result;
        }

        public static boolean saveCharLHMValueToWrMatrix(NetcdfFileWriteable wrNcFile, LinkedHashMap wrLHM, String variable, int varStride){
            boolean result=false;

            try {
                ArrayChar.D2 markersD2 = org.gwaspi.netCDF.operations.Utils.writeLHMValueToD2ArrayChar(wrLHM, varStride);

                int[] markersOrig = new int[]{0, 0};
                try {
                    wrNcFile.write(variable, markersOrig, markersD2);
                    System.out.println("Done writing "+variable+" at " + org.gwaspi.global.Utils.getMediumDateTimeAsString());
                    result=true;
                } catch (IOException e) {
                    System.err.println("ERROR writing file");
                } catch (InvalidRangeException e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                System.out.println("ERROR writing "+variable+" at " + org.gwaspi.global.Utils.getMediumDateTimeAsString());
            }

            return result;
        }

        public static boolean saveCharLHMItemToWrMatrix(NetcdfFileWriteable wrNcFile, LinkedHashMap wrLHM, String variable, int itemIndex, int varStride){
            boolean result=false;

            try {
                ArrayChar.D2 markersD2 = Utils.writeLHMValueItemToD2ArrayChar(wrLHM, itemIndex, varStride);
                int[] markersOrig = new int[]{0, 0};
                try {
                    wrNcFile.write(variable, markersOrig, markersD2);
                    System.out.println("Done writing "+variable+" at " + org.gwaspi.global.Utils.getMediumDateTimeAsString());
                    result=true;
                } catch (IOException e) {
                    System.err.println("ERROR writing file");
                } catch (InvalidRangeException e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                System.out.println("ERROR writing "+variable+" at " + org.gwaspi.global.Utils.getMediumDateTimeAsString());
            }

            return result;
        }

        //<editor-fold defaultstate="collapsed" desc="GENOTYPE SAVERS">
        public static boolean saveSingleSampleGTsToMatrix(NetcdfFileWriteable wrNcFile, LinkedHashMap wrLhm, int sampleIndex){
            boolean result=false;
            ArrayByte.D3 genotypes = org.gwaspi.netCDF.operations.Utils.writeLHMToSingleSampleArrayByteD3(wrLhm, org.gwaspi.constants.cNetCDF.Strides.STRIDE_GT);
//            ArrayByte.D3 genotypes = org.gwaspi.netCDF.operations.Utils.writeLHMToCurrentSampleArrayByteD3(wrLhm, org.gwaspi.constants.cNetCDF.Strides.STRIDE_GT);

            int[] origin = new int[]{sampleIndex,0,0};
            try {
                wrNcFile.write(org.gwaspi.constants.cNetCDF.Variables.VAR_GENOTYPES, origin, genotypes);
//                System.out.println("Done writing Sample "+samplePos+" genotypes at " + org.gwaspi.global.Utils.getMediumDateTimeAsString());
                result=true;
            } catch (IOException e) {
                System.err.println("ERROR writing genotypes to netCDF in MAtrixDataExtractor");
            } catch (InvalidRangeException e) {
                e.printStackTrace();
            }
            return result;
        }

        public static boolean saveSingleMarkerGTsToMatrix(NetcdfFileWriteable wrNcFile, LinkedHashMap wrLhm, int markerIndex){
            boolean result=false;
            ArrayByte.D3 genotypes = org.gwaspi.netCDF.operations.Utils.writeLHMToSingleMarkerArrayByteD3(wrLhm, org.gwaspi.constants.cNetCDF.Strides.STRIDE_GT);

            int[] origin = new int[]{0,markerIndex,0};
            try {
                wrNcFile.write(org.gwaspi.constants.cNetCDF.Variables.VAR_GENOTYPES, origin, genotypes);
//                System.out.println("Done writing genotypes at " + org.gwaspi.global.Utils.getMediumDateTimeAsString());
                result=true;
            } catch (IOException e) {
                System.err.println("ERROR writing genotypes to netCDF in MAtrixDataExtractor");
            } catch (InvalidRangeException e) {
                e.printStackTrace();
            }
            return result;
        }
        //</editor-fold>


        //<editor-fold defaultstate="collapsed" desc="D1 SAVERS">
        public static boolean saveDoubleLHMD1ToWrMatrix(NetcdfFileWriteable wrNcFile, LinkedHashMap wrLHM, String variable){
            boolean result=false;

            try {
                ArrayDouble.D1 arrayDouble = Utils.writeLHMValueToD1ArrayDouble(wrLHM);
                int[] origin1 = new int[1];
                try {
                    wrNcFile.write(variable, origin1, arrayDouble);
                    System.out.println("Done writing "+variable+" at " + org.gwaspi.global.Utils.getMediumDateTimeAsString());
                    result=true;
                } catch (IOException e) {
                    System.err.println("ERROR writing "+variable+" to netCDF");
                } catch (InvalidRangeException e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                System.out.println("ERROR writing "+variable+" at " + org.gwaspi.global.Utils.getMediumDateTimeAsString());
            }

            return result;
        }

        public static boolean saveDoubleLHMItemD1ToWrMatrix(NetcdfFileWriteable wrNcFile, LinkedHashMap wrLHM, int itemNb, String variable){
            boolean result=false;

            try {
                ArrayDouble.D1 arrayDouble = Utils.writeLHMValueItemToD1ArrayDouble(wrLHM, itemNb);
                int[] origin1 = new int[1];
                try {
                    wrNcFile.write(variable, origin1, arrayDouble);
                    System.out.println("Done writing "+variable+" at " + org.gwaspi.global.Utils.getMediumDateTimeAsString());
                    result=true;
                } catch (IOException e) {
                    System.err.println("ERROR writing "+variable+" to netCDF");
                } catch (InvalidRangeException e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                System.out.println("ERROR writing "+variable+" at " + org.gwaspi.global.Utils.getMediumDateTimeAsString());
            }

            return result;
        }

        public static boolean saveIntLHMD1ToWrMatrix(NetcdfFileWriteable wrNcFile, LinkedHashMap wrLHM, String variable){
            boolean result=false;

            try {
                ArrayInt.D1 arrayInt = Utils.writeLHMValueToD1ArrayInt(wrLHM);
                int[] origin1 = new int[1];
                try {
                    wrNcFile.write(variable, origin1, arrayInt);
                    System.out.println("Done writing "+variable+" at " + org.gwaspi.global.Utils.getMediumDateTimeAsString());
                    result=true;
                } catch (IOException e) {
                    System.err.println("ERROR writing "+variable+" to netCDF");
                } catch (InvalidRangeException e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                System.out.println("ERROR writing "+variable+" at " + org.gwaspi.global.Utils.getMediumDateTimeAsString());
            }

            return result;
        }

        public static boolean saveIntLHMItemD1ToWrMatrix(NetcdfFileWriteable wrNcFile, LinkedHashMap wrLHM, int itemNb, String variable){
            boolean result=false;

            try {
                ArrayInt.D1 arrayInt = Utils.writeLHMValueItemToD1ArrayInt(wrLHM, itemNb);
                int[] origin1 = new int[1];
                try {
                    wrNcFile.write(variable, origin1, arrayInt);
                    System.out.println("Done writing "+variable+" at " + org.gwaspi.global.Utils.getMediumDateTimeAsString());
                    result=true;
                } catch (IOException e) {
                    System.err.println("ERROR writing "+variable+" to netCDF");
                } catch (InvalidRangeException e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                System.out.println("ERROR writing "+variable+" at " + org.gwaspi.global.Utils.getMediumDateTimeAsString());
            }

            return result;
        }
        //</editor-fold>

        //<editor-fold defaultstate="collapsed" desc="D2 SAVERS">
        public static boolean saveIntLHMD2ToWrMatrix(NetcdfFileWriteable wrNcFile, LinkedHashMap wrLHM, int[] columns, String variable) {
            boolean result=false;

            try {
                ArrayInt.D2 arrayIntD2 = Utils.writeLHMValueItemToD2ArrayInt(wrLHM, columns);
                int[] origin1 = new int[2];
                try {
                    wrNcFile.write(variable, origin1, arrayIntD2);
                    System.out.println("Done writing "+variable+" at " + org.gwaspi.global.Utils.getMediumDateTimeAsString());
                    result=true;
                } catch (IOException e) {
                    System.err.println("ERROR writing "+variable+" to netCDF");
                } catch (InvalidRangeException e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                System.out.println("ERROR writing "+variable+" at " + org.gwaspi.global.Utils.getMediumDateTimeAsString());
                e.printStackTrace();
            }

            return result;
        }

        public static boolean saveDoubleLHMD2ToWrMatrix(NetcdfFileWriteable wrNcFile, LinkedHashMap wrLHM, int[] columns, String variable) {
            boolean result=false;

            try {
                ArrayDouble.D2 arrayDoubleD2 = Utils.writeLHMValueItemToD2ArrayDouble(wrLHM, columns);
                int[] origin1 = new int[2];
                try {
                    wrNcFile.write(variable, origin1, arrayDoubleD2);
                    System.out.println("Done writing "+variable+" at " + org.gwaspi.global.Utils.getMediumDateTimeAsString());
                    result=true;
                } catch (IOException e) {
                    System.err.println("ERROR writing "+variable+" to netCDF");
                } catch (InvalidRangeException e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                System.out.println("ERROR writing "+variable+" at " + org.gwaspi.global.Utils.getMediumDateTimeAsString());
                e.printStackTrace();
            }

            return result;
        }

        //</editor-fold>

    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="CHUNKED SAVERS">

        public static boolean saveCharChunkedLHMToWrMatrix(NetcdfFileWriteable wrNcFile, 
                                                           LinkedHashMap wrLHM,
                                                           String variable,
                                                           int varStride,
                                                           int offset){
            boolean result=false;

            try {
                ArrayChar.D2 markersD2 = org.gwaspi.netCDF.operations.Utils.writeLHMValueToD2ArrayChar(wrLHM, varStride);

                int[] markersOrig = new int[]{offset, 0}; //first origin is the initial markerset position, second is the original allele position
                try {
                    wrNcFile.write(variable, markersOrig, markersD2);
                    System.out.println("Done writing "+variable+" at " + org.gwaspi.global.Utils.getMediumDateTimeAsString());
                    result=true;
                } catch (IOException e) {
                    System.err.println("ERROR writing file");
                } catch (InvalidRangeException e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                System.out.println("ERROR writing "+variable+" at " + org.gwaspi.global.Utils.getMediumDateTimeAsString());
            }

            return result;
        }

        public static boolean saveCharChunkedLHMItemToWrMatrix(NetcdfFileWriteable wrNcFile,
                                                               LinkedHashMap wrLHM,
                                                               String variable,
                                                               int itemNb,
                                                               int varStride,
                                                               int offset){
            boolean result=false;

            try {
                ArrayChar.D2 markersD2 = Utils.writeLHMValueItemToD2ArrayChar(wrLHM, itemNb, varStride);
                int[] markersOrig = new int[]{offset, 0};
                try {
                    wrNcFile.write(variable, markersOrig, markersD2);
                    System.out.println("Done writing "+variable+" at " + org.gwaspi.global.Utils.getMediumDateTimeAsString());
                    result=true;
                } catch (IOException e) {
                    System.err.println("ERROR writing file");
                } catch (InvalidRangeException e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                System.out.println("ERROR writing "+variable+" at " + org.gwaspi.global.Utils.getMediumDateTimeAsString());
            }

            return result;
        }

        //<editor-fold defaultstate="collapsed" desc="GENOTYPE SAVERS">
        public static boolean saveChunkedCurrentSampleGTsToMatrix(NetcdfFileWriteable wrNcFile,
                                                                  LinkedHashMap wrLhm,
                                                                  int samplePos,
                                                                  int offset){
            boolean result=false;
            ArrayChar.D3 genotypes = org.gwaspi.netCDF.operations.Utils.writeLHMToCurrentSampleArrayCharD3(wrLhm, org.gwaspi.constants.cNetCDF.Strides.STRIDE_GT);

            int[] origin = new int[]{samplePos,offset,0};
            try {
                wrNcFile.write(org.gwaspi.constants.cNetCDF.Variables.VAR_GENOTYPES, origin, genotypes);
                System.out.println("Done writing Sample "+samplePos+" genotypes at " + org.gwaspi.global.Utils.getMediumDateTimeAsString());
                result=true;
            } catch (IOException e) {
                System.err.println("ERROR writing genotypes to netCDF in MAtrixDataExtractor");
            } catch (InvalidRangeException e) {
                e.printStackTrace();
            }
            return result;
        }

        public static boolean saveChunkedCurrentMarkerGTsToMatrix(NetcdfFileWriteable wrNcFile,
                                                                  LinkedHashMap wrLhm,
                                                                  int markerPos,
                                                                  int offset){
            boolean result=false;
            ArrayChar.D3 genotypes = org.gwaspi.netCDF.operations.Utils.writeLHMToCurrentMarkerArrayCharD3(wrLhm, org.gwaspi.constants.cNetCDF.Strides.STRIDE_GT);

            int[] origin = new int[]{offset,markerPos,0};
            try {
                wrNcFile.write(org.gwaspi.constants.cNetCDF.Variables.VAR_GENOTYPES, origin, genotypes);
                System.out.println("Done writing genotypes at " + org.gwaspi.global.Utils.getMediumDateTimeAsString());
                result=true;
            } catch (IOException e) {
                System.err.println("ERROR writing genotypes to netCDF in MAtrixDataExtractor");
            } catch (InvalidRangeException e) {
                e.printStackTrace();
            }
            return result;
        }
        //</editor-fold>

        //<editor-fold defaultstate="collapsed" desc="D1 SAVERS">
        public static boolean saveDoubleChunkedLHMD1ToWrMatrix(NetcdfFileWriteable wrNcFile,
                                                               LinkedHashMap wrLHM,
                                                               String variable,
                                                               int offset){
            boolean result=false;

            try {
                ArrayDouble.D1 arrayDouble = Utils.writeLHMValueToD1ArrayDouble(wrLHM);
                int[] origin1 = new int[]{offset};
                try {
                    wrNcFile.write(variable, origin1, arrayDouble);
                    System.out.println("Done writing "+variable+" at " + org.gwaspi.global.Utils.getMediumDateTimeAsString());
                    result=true;
                } catch (IOException e) {
                    System.err.println("ERROR writing "+variable+" to netCDF");
                } catch (InvalidRangeException e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                System.out.println("ERROR writing "+variable+" at " + org.gwaspi.global.Utils.getMediumDateTimeAsString());
            }

            return result;
        }

        public static boolean saveDoubleChunkedLHMItemD1ToWrMatrix(NetcdfFileWriteable wrNcFile, 
                                                                   LinkedHashMap wrLHM,
                                                                   int itemNb,
                                                                   String variable,
                                                                   int offset){
            boolean result=false;

            try {
                ArrayDouble.D1 arrayDouble = Utils.writeLHMValueItemToD1ArrayDouble(wrLHM, itemNb);
                int[] origin1 = new int[]{offset};
                try {
                    wrNcFile.write(variable, origin1, arrayDouble);
                    System.out.println("Done writing "+variable+" at " + org.gwaspi.global.Utils.getMediumDateTimeAsString());
                    result=true;
                } catch (IOException e) {
                    System.err.println("ERROR writing "+variable+" to netCDF");
                } catch (InvalidRangeException e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                System.out.println("ERROR writing "+variable+" at " + org.gwaspi.global.Utils.getMediumDateTimeAsString());
            }

            return result;
        }

        public static boolean saveIntChunkedLHMD1ToWrMatrix(NetcdfFileWriteable wrNcFile,
                                                            LinkedHashMap wrLHM,
                                                            String variable,
                                                            int offset){
            boolean result=false;

            try {
                ArrayInt.D1 arrayInt = Utils.writeLHMValueToD1ArrayInt(wrLHM);
                int[] origin1 = new int[]{offset};
                try {
                    wrNcFile.write(variable, origin1, arrayInt);
                    System.out.println("Done writing "+variable+" at " + org.gwaspi.global.Utils.getMediumDateTimeAsString());
                    result=true;
                } catch (IOException e) {
                    System.err.println("ERROR writing "+variable+" to netCDF");
                } catch (InvalidRangeException e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                System.out.println("ERROR writing "+variable+" at " + org.gwaspi.global.Utils.getMediumDateTimeAsString());
            }

            return result;
        }

        public static boolean saveIntChunkedLHMItemD1ToWrMatrix(NetcdfFileWriteable wrNcFile, 
                                                                LinkedHashMap wrLHM,
                                                                int itemNb,
                                                                String variable,
                                                                int offset){
            boolean result=false;

            try {
                ArrayInt.D1 arrayInt = Utils.writeLHMValueItemToD1ArrayInt(wrLHM, itemNb);
                int[] origin1 = new int[]{offset};
                try {
                    wrNcFile.write(variable, origin1, arrayInt);
                    System.out.println("Done writing "+variable+" at " + org.gwaspi.global.Utils.getMediumDateTimeAsString());
                    result=true;
                } catch (IOException e) {
                    System.err.println("ERROR writing "+variable+" to netCDF");
                } catch (InvalidRangeException e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                System.out.println("ERROR writing "+variable+" at " + org.gwaspi.global.Utils.getMediumDateTimeAsString());
            }

            return result;
        }
        //</editor-fold>

        //<editor-fold defaultstate="collapsed" desc="D2 SAVERS">
        public static boolean saveIntChunkedLHMD2ToWrMatrix(NetcdfFileWriteable wrNcFile, 
                                                            LinkedHashMap wrLHM,
                                                            int[] columns,
                                                            String variable,
                                                            int offset) {
            boolean result=false;

            try {
                ArrayInt.D2 arrayIntD2 = Utils.writeLHMValueItemToD2ArrayInt(wrLHM, columns);
                int[] origin1 = new int[]{offset,0};
                try {
                    wrNcFile.write(variable, origin1, arrayIntD2);
                    System.out.println("Done writing "+variable+" at " + org.gwaspi.global.Utils.getMediumDateTimeAsString());
                    result=true;
                } catch (IOException e) {
                    System.err.println("ERROR writing "+variable+" to netCDF");
                } catch (InvalidRangeException e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                System.out.println("ERROR writing "+variable+" at " + org.gwaspi.global.Utils.getMediumDateTimeAsString());
                e.printStackTrace();
            }

            return result;
        }

        public static boolean saveDoubleChunkedD2ToWrMatrix(NetcdfFileWriteable wrNcFile, 
                                                            LinkedHashMap wrLHM,
                                                            int[] columns,
                                                            String variable,
                                                            int offset) {
            boolean result=false;

            try {
                ArrayDouble.D2 arrayDoubleD2 = Utils.writeLHMValueItemToD2ArrayDouble(wrLHM, columns);
                int[] origin1 = new int[]{offset,0};
                try {
                    wrNcFile.write(variable, origin1, arrayDoubleD2);
                    System.out.println("Done writing "+variable+" at " + org.gwaspi.global.Utils.getMediumDateTimeAsString());
                    result=true;
                } catch (IOException e) {
                    System.err.println("ERROR writing "+variable+" to netCDF");
                } catch (InvalidRangeException e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                System.out.println("ERROR writing "+variable+" at " + org.gwaspi.global.Utils.getMediumDateTimeAsString());
                e.printStackTrace();
            }

            return result;
        }

        //</editor-fold>

    //</editor-fold>

        
    //<editor-fold defaultstate="collapsed" desc="POJOs TO netCDFJOs">

        //<editor-fold defaultstate="collapsed" desc="ArrayChar.D3">
        public static ArrayChar.D3 writeLHMToCurrentSampleArrayCharD3(LinkedHashMap lhm, int stride){
            ArrayChar.D3 charArray = new ArrayChar.D3(1, lhm.size(), stride);
            Index ima = charArray.getIndex();

            int markerCounter=0;
            for (Iterator itWr = lhm.keySet().iterator(); itWr.hasNext();) {
                String key = itWr.next().toString();
                String value = lhm.get(key).toString();
                charArray.setString(ima.set(0,markerCounter,0),value.trim()); //1 Sample at a time, iterating through markers, starting at gtSpan 0
                markerCounter++;
            }

            
            return charArray;
        }

        public static ArrayChar.D3 writeLHMToCurrentMarkerArrayCharD3(LinkedHashMap lhm, int stride){
            ArrayChar.D3 charArray = new ArrayChar.D3(lhm.size(), 1, stride);
            Index ima = charArray.getIndex();

            int sampleCounter=0;
            for (Iterator itWr = lhm.keySet().iterator(); itWr.hasNext();) {
                String key = itWr.next().toString();
                String value = lhm.get(key).toString();
                charArray.setString(ima.set(sampleCounter,0,0),value.trim()); //1 Marker at a time, iterating through samples, starting at gtSpan 0
                sampleCounter++;
            }

            
            return charArray;
        }
        //</editor-fold>

        //<editor-fold defaultstate="collapsed" desc="ArrayChar.D2">
        public static ArrayChar.D2 writeALToD2ArrayChar(ArrayList al, int stride){
            ArrayChar.D2 charArray = new ArrayChar.D2(al.size(), stride);
            Index ima = charArray.getIndex();

            for (int i=0;i<al.size();i++) {
                String value = al.get(i).toString();
                charArray.setString(ima.set(i,0),value.trim());
            }

            
            return charArray;
        }

        public static ArrayChar.D2 writeLHMValueToD2ArrayChar(LinkedHashMap lhm, int stride){
            ArrayChar.D2 charArray = new ArrayChar.D2(lhm.size(), stride);
            Index ima = charArray.getIndex();

            int count=0;
            for (Iterator itWr = lhm.keySet().iterator(); itWr.hasNext();) {
                String key = itWr.next().toString();
                String value = lhm.get(key).toString();
                charArray.setString(ima.set(count,0),value.trim());
                count++;
            }

            
            return charArray;
        }

        public static ArrayChar.D2 writeLHMKeysToD2ArrayChar(LinkedHashMap lhm, int stride) {
            ArrayChar.D2 charArray = new ArrayChar.D2(lhm.size(), stride);
            Index ima = charArray.getIndex();

            int count=0;
            for (Iterator itWr = lhm.keySet().iterator(); itWr.hasNext();) {
                String key = itWr.next().toString();
                charArray.setString(ima.set(count,0),key.trim());
                count++;
            }

            
            return charArray;
        }

        public static ArrayChar.D2 writeLHMValueItemToD2ArrayChar(LinkedHashMap lhm, int itemNb, int stride){
            ArrayChar.D2 charArray = new ArrayChar.D2(lhm.size(), stride);
            Index index = charArray.getIndex();

            int count=0;
            for (Iterator itWr = lhm.keySet().iterator(); itWr.hasNext();) {
                String key = itWr.next().toString();
                Object[] values = (Object[]) lhm.get(key);
                String value = values[itemNb].toString();
                charArray.setString(index.set(count,0),value.trim());
                count++;
            }

            
            return charArray;
        }
        //</editor-fold>


        //<editor-fold defaultstate="collapsed" desc="ArrayDouble.D1 & D2">

        public static ArrayDouble.D1 writeLHMValueToD1ArrayDouble(LinkedHashMap lhm){
            ArrayDouble.D1 doubleArray = new ArrayDouble.D1(lhm.size());
            Index index = doubleArray.getIndex();

            int count=0;
            for (Iterator itWr = lhm.keySet().iterator(); itWr.hasNext();) {
                String key = itWr.next().toString();
                double value = (Double) lhm.get(key);
                doubleArray.setDouble(index.set(count), value);
                count++;
            }

            
            return doubleArray;
        }

        private static ArrayDouble.D1 writeLHMValueItemToD1ArrayDouble(LinkedHashMap lhm, int itemNb) {
            ArrayDouble.D1 doubleArray = new ArrayDouble.D1(lhm.size());
            Index index = doubleArray.getIndex();

            int count=0;
            for (Iterator itWr = lhm.keySet().iterator(); itWr.hasNext();) {
                String key = itWr.next().toString();
                Object[] values = (Object[]) lhm.get(key);
                doubleArray.setDouble(index.set(count), (Double) values[itemNb]);
                count++;
            }

            return doubleArray;
        }

        private static ArrayDouble.D2 writeLHMValueItemToD2ArrayDouble(LinkedHashMap lhm, int[] columns) {
            ArrayDouble.D2 doubleArray = new ArrayDouble.D2(lhm.size(), columns.length);
            Index ima = doubleArray.getIndex();

            int i=0;
            for (Iterator itWr = lhm.keySet().iterator(); itWr.hasNext();) {
                String key = itWr.next().toString();
                Object[] values = (Object[]) lhm.get(key);
                for (int j=0; j<columns.length; j++) {
                    doubleArray.setDouble(ima.set(i, j), (Double) values[columns[j]]);
                }
                i++;
            }

            
            return doubleArray;
        }
        //</editor-fold>


        //<editor-fold defaultstate="collapsed" desc="ArrayInt.D1 & D2">

        public static ArrayInt.D1 writeLHMValueToD1ArrayInt(LinkedHashMap lhm){
            ArrayInt.D1 intArray = new ArrayInt.D1(lhm.size());
            Index index = intArray.getIndex();

            int count=0;
            for (Iterator itWr = lhm.keySet().iterator(); itWr.hasNext();) {
                String key = itWr.next().toString();
                int value = (Integer) lhm.get(key);
                intArray.setInt(index.set(count), value);
                count++;
            }

            
            return intArray;
        }

        public static ArrayInt.D1 writeLHMValueItemToD1ArrayInt(LinkedHashMap lhm, int itemNb){
            ArrayInt.D1 intArray = new ArrayInt.D1(lhm.size());
            Index index = intArray.getIndex();

            int count=0;
            for (Iterator itWr = lhm.keySet().iterator(); itWr.hasNext();) {
                String key = itWr.next().toString();
                Object[] values = (Object[]) lhm.get(key);
                intArray.setInt(index.set(count), (Integer) values[itemNb]);
                count++;
            }

            
            return intArray;
        }

        //TODO: can be optimized with arraycopy?
        public static ArrayInt.D2 writeLHMValueItemToD2ArrayInt(LinkedHashMap lhm, int[] columns) {
            ArrayInt.D2 intArray = new ArrayInt.D2(lhm.size(), columns.length);
            Index ima = intArray.getIndex();

            int i=0;
            for (Iterator itWr = lhm.keySet().iterator(); itWr.hasNext();) {
                String key = itWr.next().toString();
                int[] values = (int[]) lhm.get(key);
//                Object[] values = (Object[]) lhm.get(key);
                for (int j=0; j<columns.length; j++) {
                    intArray.setInt(ima.set(i, j), values[columns[j]]);
                }
                i++;
            }

            
            return intArray;
        }

        //</editor-fold>


        //<editor-fold defaultstate="collapsed" desc="ArrayByte.D3">
        public static ArrayByte.D3 writeALValuesToSamplesHyperSlabArrayByteD3(ArrayList<byte[]> genotypesAL, int sampleNb, int stride){
            int markerNb=genotypesAL.size()/sampleNb;
            int alCounter = 0;
            
            //samplesDim, markersDim, gtStrideDim
            ArrayByte.D3 byteArray = new ArrayByte.D3(sampleNb, markerNb, stride);
            Index ima = byteArray.getIndex();

            for (int markerCounter = 0; markerCounter < markerNb; markerCounter++) {
                for (int sampleCounter = 0; sampleCounter < sampleNb; sampleCounter++) {
                
                    byte[] value = (byte[]) genotypesAL.get(alCounter);
                    byteArray.setByte(ima.set(sampleCounter, markerCounter, 0),value[0]); //1 Sample at a time, iterating through markers, first byte
                    byteArray.setByte(ima.set(sampleCounter, markerCounter, 1),value[1]); //1 Sample at a time, iterating through markers, second byte
                    alCounter++;
                }
            }

            return byteArray;
        }

        public static ArrayByte.D3 writeLHMToSingleSampleArrayByteD3(LinkedHashMap lhm, int stride){
            //samplesDim, markersDim, gtStrideDim
            ArrayByte.D3 byteArray = new ArrayByte.D3(1, lhm.size(), stride);
            Index ima = byteArray.getIndex();

            int markerCount=0;
            for (Iterator itWr = lhm.keySet().iterator(); itWr.hasNext();) {
                String key = itWr.next().toString();
                byte[] value = (byte[]) lhm.get(key);
                byteArray.setByte(ima.set(0, markerCount, 0),value[0]); //1 Sample at a time, iterating through markers, first byte
                byteArray.setByte(ima.set(0, markerCount, 1),value[1]); //1 Sample at a time, iterating through markers, second byte
                markerCount++;
            }

            return byteArray;
        }

        //TODO CHECK ALL USAGES
        /**
         * This writeLHMToCurrentSampleArrayByteD3 has now been deprecated in favor of writeLHMToSingleSampleArrayByteD3
         *  Method is probably INCORRECT!
         * @deprecated Use writeLHMToSingleSampleArrayByteD3 instead
         */
        public static ArrayByte.D3 writeLHMToCurrentSampleArrayByteD3(LinkedHashMap lhm, int stride){
            ArrayByte.D3 byteArray = new ArrayByte.D3(lhm.size(), 1, stride);
            Index ima = byteArray.getIndex();

            int markerCount=0;
            for (Iterator itWr = lhm.keySet().iterator(); itWr.hasNext();) {
                String key = itWr.next().toString();
                byte[] value = (byte[]) lhm.get(key);
                byteArray.setByte(ima.set(markerCount,0,0),value[0]); //1 Sample at a time, iterating through markers, first byte
                byteArray.setByte(ima.set(markerCount,0,1),value[1]); //1 Sample at a time, iterating through markers, second byte
                markerCount++;
            }


            return byteArray;
        }


        public static ArrayByte.D3 writeLHMToSingleMarkerArrayByteD3(LinkedHashMap lhm, int stride){
            ArrayByte.D3 byteArray = new ArrayByte.D3(lhm.size(), 1, stride);
            Index ima = byteArray.getIndex();

            int markerCounter=0;
            for (Iterator itWr = lhm.keySet().iterator(); itWr.hasNext();) {
                String key = itWr.next().toString();
                byte[] value = (byte[]) lhm.get(key);
                byteArray.setByte(ima.set(markerCounter, 0 ,0),value[0]); //1 Marker at a time, iterating through samples, first byte
                byteArray.setByte(ima.set(markerCounter, 0, 1),value[1]); //1 Marker at a time, iterating through samples, second byte
                markerCounter++;
            }

            return byteArray;
        }

        //TODO CHECK ALL USAGES
        /**
         * This writeLHMToCurrentMarkerArrayByteD3 has now been deprecated in favor of writeLHMToSingleMarkerArrayByteD3
         *  Method is probably INCORRECT!
         * @deprecated Use writeLHMToSingleMarkerArrayByteD3 instead
         */
        public static ArrayByte.D3 writeLHMToCurrentMarkerArrayByteD3(LinkedHashMap lhm, int stride){
            ArrayByte.D3 byteArray = new ArrayByte.D3(1, lhm.size(), stride);
            Index ima = byteArray.getIndex();

            int markerCounter=0;
            for (Iterator itWr = lhm.keySet().iterator(); itWr.hasNext();) {
                String key = itWr.next().toString();
                byte[] value = (byte[]) lhm.get(key);
                byteArray.setByte(ima.set(0, markerCounter,0),value[0]); //1 Marker at a time, iterating through samples, first byte
                byteArray.setByte(ima.set(0, markerCounter,1),value[1]); //1 Marker at a time, iterating through samples, second byte
                markerCounter++;
            }


            return byteArray;
        }
        //</editor-fold>


    //</editor-fold>


    //<editor-fold defaultstate="collapsed" desc="netCDFJOs TO POJOs">

        //<editor-fold defaultstate="collapsed" desc="ArrayChar.D2">
        public static LinkedHashMap writeD2ArrayCharToLHMKeys(ArrayChar inputArray){
            LinkedHashMap result = new LinkedHashMap();
            StringBuilder key = new StringBuilder("");

            int[] shape = inputArray.getShape();
            Index index = inputArray.getIndex();
            for (int i=0; i<shape[0]; i++) {
                ArrayChar wrCharArray = new ArrayChar ( new int[] {1, shape[1]} );
                ArrayChar.D2.arraycopy(inputArray, i*shape[1], wrCharArray, 0, shape[1]);
                char[] values = (char[]) wrCharArray.copyTo1DJavaArray();
                result.put(String.valueOf(values).trim(), "");

//                key = new StringBuilder("");
//                for (int j=0; j<shape[1]; j++) {
//                    key.append(inputArray.getChar(index.set(i,j)));
//                }
//                result.put(key.toString().trim(), "");
            }

            
            return result;
        }

        public static LinkedHashMap writeD2ArrayCharToLHMValues(ArrayChar inputArray, LinkedHashMap lhm){

            int[] shape = inputArray.getShape();
            Iterator it = lhm.keySet().iterator();
            for (int i=0; i<shape[0]; i++) {
                Object key = it.next();

                ArrayChar wrCharArray = new ArrayChar ( new int[] {1, shape[1]} );
                ArrayChar.D2.arraycopy(inputArray, i*shape[1], wrCharArray, 0, shape[1]);
                char[] values = (char[]) wrCharArray.copyTo1DJavaArray();
                lhm.put(key, String.valueOf(values).trim());

            }

            
            return lhm;
        }

        public static ArrayList writeD2ArrayCharToAL(ArrayChar inputArray){
            ArrayList als = new ArrayList();
            Long expectedSize = inputArray.getSize();
            als.ensureCapacity(expectedSize.intValue());

            int[] shape = inputArray.getShape();
            for (int i=0; i<shape[0]; i++) {
                ArrayChar wrCharArray = new ArrayChar ( new int[] {1, shape[1]} );
                ArrayChar.D2.arraycopy(inputArray, i*shape[1], wrCharArray, 0, shape[1]);
                char[] values = (char[]) wrCharArray.copyTo1DJavaArray();
                als.add(String.valueOf(values).trim());
            }

            
            return als;
        }
        //</editor-fold>

        //<editor-fold defaultstate="collapsed" desc="ArrayChar.D1">
        public static LinkedHashMap writeD1ArrayCharToLHMKeys(ArrayChar inputArray){
            LinkedHashMap resultLHM = new LinkedHashMap();
            StringBuilder key = new StringBuilder("");
            Index index = inputArray.getIndex();

            int[] shape = inputArray.getShape();
            for (int j=0; j<shape[0]; j++) {
                key.append(inputArray.getChar(index.set(j)));
            }
            resultLHM.put(key.toString().trim(),"");

            return resultLHM;

        }

        public static LinkedHashMap writeD1ArrayCharToLHMValues(ArrayChar inputArray, LinkedHashMap lhm){
            StringBuilder value = new StringBuilder("");
            Index index = inputArray.getIndex();

            int[] shape = inputArray.getShape();
            Iterator it = lhm.keySet().iterator();
            Object key = it.next();

            for (int j=0; j<shape[0]; j++) {
                value.append(inputArray.getChar(index.set(j)));
            }
            lhm.put(key, value.toString().trim());

            return lhm;
        }


        //</editor-fold>


        //<editor-fold defaultstate="collapsed" desc="ArrayDouble.D1">

        public static LinkedHashMap writeD1ArrayDoubleToLHMValues(ArrayDouble inputArray, LinkedHashMap lhm){
            Double value = Double.NaN;

            int[] shape = inputArray.getShape();
            Index index = inputArray.getIndex();
            Iterator it = lhm.keySet().iterator();
            for (int i=0; i<shape[0]; i++) {
                Object key = it.next();
                value = inputArray.getDouble(index.set(i));
                lhm.put(key, value);
            }

            
            return lhm;
        }

        public static ArrayList<Double> writeD1ArrayDoubleToAL(ArrayDouble.D1 inputArray){
            ArrayList<Double> alf = new ArrayList<Double>();
            Long expectedSize = inputArray.getSize();
            alf.ensureCapacity(expectedSize.intValue());
            Double value = Double.NaN;

            int[] shape = inputArray.getShape();
            Index index = inputArray.getIndex();
            for (int i=0; i<shape[0]; i++) {
                value = inputArray.getDouble(index.set(i));
                alf.add(value);
            }

            
            return alf;
        }

        //</editor-fold>

        //<editor-fold defaultstate="collapsed" desc="ArrayDouble.D2">
        public static LinkedHashMap writeD2ArrayDoubleToLHMValues(ArrayDouble.D2 inputArray, LinkedHashMap lhm) {
            int[] shape = inputArray.getShape();
            Iterator it = lhm.keySet().iterator();

            for (int i=0; i<(shape[0]*shape[1]); i=i+shape[1]) {
                String key = it.next().toString();

                ArrayDouble wrDoubleArray = new ArrayDouble ( new int[] {1, shape[1]} );
                ArrayDouble.D2.arraycopy(inputArray, i, wrDoubleArray, 0, shape[1]);
                double[] values = (double[]) wrDoubleArray.copyTo1DJavaArray();

                lhm.put(key, values);
            }

            
            return lhm;
        }

        public static ArrayList<double[]> writeD2ArrayDoubleToAL(ArrayDouble.D2 inputArray){
            ArrayList<double[]> alf = new ArrayList<double[]>();
            Long expectedSize = inputArray.getSize();
            alf.ensureCapacity(expectedSize.intValue());

            int[] shape = inputArray.getShape();
            for (int i=0; i<(shape[0]*shape[1]); i=i+shape[1]) {
                ArrayDouble wrDoubleArray = new ArrayDouble ( new int[] {1, shape[1]} );
                ArrayDouble.D2.arraycopy(inputArray, i, wrDoubleArray, 0, shape[1]);
                double[] values = (double[]) wrDoubleArray.copyTo1DJavaArray();
                alf.add(values);
            }

            
            return alf;
        }
        //</editor-fold>


        //<editor-fold defaultstate="collapsed" desc="ArrayInt.D1">

        public static LinkedHashMap writeD1ArrayIntToLHMValues(ArrayInt inputArray, LinkedHashMap lhm){
            Integer value = 0;

            int[] shape = inputArray.getShape();
            Index index = inputArray.getIndex();
            Iterator it = lhm.keySet().iterator();
            for (int i=0; i<shape[0]; i++) {
                Object key = it.next();
                value = inputArray.getInt(index.set(i));
                lhm.put(key, value);
            }

            
            return lhm;
        }

        public static ArrayList<Integer> writeD1ArrayIntToAL(ArrayInt.D1 inputArray){
            ArrayList<Integer> ali = new ArrayList<Integer>();
            Long expectedSize = inputArray.getSize();
            ali.ensureCapacity(expectedSize.intValue());
            int value = 0;

            int[] shape = inputArray.getShape();
            Index index = inputArray.getIndex();
            for (int i=0; i<shape[0]; i++) {
                value = inputArray.getInt(index.set(i));
                ali.add(value);
            }

            
            return ali;
        }

        //</editor-fold>

        //<editor-fold defaultstate="collapsed" desc="ArrayInt.D2">

        public static LinkedHashMap writeD2ArrayIntToLHMValues(ArrayInt.D2 inputArray, LinkedHashMap lhm){
            int[] shape = inputArray.getShape();
            Index index = inputArray.getIndex();
            Iterator it = lhm.keySet().iterator();

            for (int i=0; i<(shape[0]*shape[1]); i=i+shape[1]) {
                String key = it.next().toString();

                ArrayInt wrIntArray = new ArrayInt ( new int[] {1, shape[1]} );
                ArrayInt.D2.arraycopy(inputArray, i, wrIntArray, 0, shape[1]);
                int[] values = (int[]) wrIntArray.copyTo1DJavaArray();

                lhm.put(key, values);
            }

            
            return lhm;
        }

        //</editor-fold>


        //<editor-fold defaultstate="collapsed" desc="ArrayByte.D2">
        public static LinkedHashMap writeD2ArrayByteToLHMValues(ArrayByte inputArray, LinkedHashMap lhm){
            StringBuilder value = new StringBuilder("");

            int[] shape = inputArray.getShape();
            Index index = inputArray.getIndex();
            Iterator it = lhm.keySet().iterator();
            for (int i=0; i<shape[0]; i++) {
                Object key = it.next();

                ArrayByte wrArray = new ArrayByte ( new int[] {1, shape[1]} );
                ArrayByte.D2.arraycopy(inputArray, i*shape[1], wrArray, 0, shape[1]);
                byte[] values = (byte[]) wrArray.copyTo1DJavaArray();
                lhm.put(key, values);

            }


            return lhm;
        }

        public static ArrayList writeD2ArrayByteToAL(ArrayByte inputArray){
            ArrayList als = new ArrayList();
            Long expectedSize = inputArray.getSize();
            als.ensureCapacity(expectedSize.intValue());
            StringBuilder value = new StringBuilder("");

            int[] shape = inputArray.getShape();
            Index index = inputArray.getIndex();
            for (int i=0; i<shape[0]; i++) {
                ArrayByte wrArray = new ArrayByte ( new int[] {1, shape[1]} );
                ArrayByte.D2.arraycopy(inputArray, i*shape[1], wrArray, 0, shape[1]);
                byte[] values = (byte[]) wrArray.copyTo1DJavaArray();
                als.add(values);

            }


            return als;
        }
        //</editor-fold>

        //<editor-fold defaultstate="collapsed" desc="ArrayByte.D1">

        public static LinkedHashMap writeD1ArrayByteToLHMValues(ArrayByte inputArray, LinkedHashMap lhm){
            StringBuilder value = new StringBuilder("");
            Index index = inputArray.getIndex();

            int[] shape = inputArray.getShape();
            Iterator it = lhm.keySet().iterator();
            Object key = it.next();

            for (int j=0; j<shape[0]; j++) {
                value.append(inputArray.getChar(index.set(j)));
            }
            lhm.put(key, value.toString().trim());

            return lhm;
        }


        //</editor-fold>


    //</editor-fold>

}
