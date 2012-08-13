package trastero;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Random;

public class SingeFileSort {


    public static String sortFile(String filePath, int compareIndex) throws IOException
    {
         File tempSorted = null;
         
         try
         {
             FileReader fr = new FileReader(filePath);
             BufferedReader br = new BufferedReader(fr);
             ArrayList<String[]> filerows = new ArrayList<String[]>();
             Random generator = new Random();
             int rnd = Math.abs(generator.nextInt());
             tempSorted = new File(global.Config.getConfigValue("SSdir", constants.cGlobal.USERDIR).toString()+"/"+rnd+".csv");
             FileWriter fw = new FileWriter(tempSorted);
             BufferedWriter bw = new BufferedWriter(fw);
             String [] header;

             boolean someFileStillHasRows = false;

             // get each one past the header
             header = br.readLine().split(constants.cImport.Separators.separators_CommaTab_rgxp);
             bw.write(flattenArray(header,",")+"\n");

             // get the first row
             String line = br.readLine();
             if (line != null)
             {
                 filerows.add(line.split(constants.cImport.Separators.separators_CommaTab_rgxp));
                 someFileStillHasRows = true;
             }
             else
             {
                 filerows.add(null);
             }


             String[] row;
             int cnt = 0;
             while (someFileStillHasRows)
             {
                 String min;
                 int minIndex = 0;

                 row = filerows.get(0);
                 if (row!=null) {
                     min = row[compareIndex];
                     minIndex = 0;
                 }
                 else {
                     min = null;
                     minIndex = -1;
                 }

                 // check which one is min
                 for(int i=1; i<filerows.size(); i++)
                 {
                     row = filerows.get(i);
                     if (min!=null) {

                         if(row!=null && row[compareIndex].compareTo(min) <= 0)
                         {
                             minIndex = i;
                             min = filerows.get(i)[compareIndex];
                         }
                     }
                     else
                     {
                         if(row!=null)
                         {
                             min = row[compareIndex];
                             minIndex = i;
                         }
                     }
                 }

                 if (minIndex < 0) {
                     someFileStillHasRows=false;
                 }
                 else
                 {
                     // write to the sorted file
                     bw.append(flattenArray(filerows.get(minIndex),",")+"\n");

                     // get another row from the file that had the min
                     line = br.readLine();
                     if (line != null)
                     {
                         filerows.set(minIndex,line.split(constants.cImport.Separators.separators_CommaTab_rgxp));
                     }
                     else
                     {
                         filerows.set(minIndex,null);
                     }
                 }                                
                 // check if one still has rows
                 for(int i=0; i<filerows.size(); i++)
                 {

                     someFileStillHasRows = false;
                     if(filerows.get(i)!=null)
                     {
                         if (minIndex < 0)
                         {
                             //System.out ("mindex < 0 and found row not null" + filerows.get(i).toString());
                             System.exit(-1);
                         }
                         someFileStillHasRows = true;
                         break;
                     }
                 }

                 // check the actual files one more time
                 if (!someFileStillHasRows)
                 {

                     //write the last one not covered above
                     for(int i=0; i<filerows.size(); i++)
                     {
                         if (filerows.get(i) == null)
                         {
                             line = br.readLine();
                             if (line!=null)
                             {
                                 someFileStillHasRows=true;
                                 filerows.set(i,line.split(constants.cImport.Separators.separators_CommaTab_rgxp));
                             }
                         }

                     }
                 }

             }

             // close all the files
             bw.close();
             fw.close();
             br.close();
             fr.close();
         }
         catch (Exception ex)
         {
             ex.printStackTrace();
             System.exit(-1);
         }
         
         return tempSorted.getPath();
    }


    private static String flattenArray(String[] a, String separator)
    {
        StringBuffer result = new StringBuffer(a[0]);
        for(int i=1; i<Array.getLength(a); i++){
            result.append(separator);
            result.append(a[i]);
        }
        return result.toString();
    }
    
}
