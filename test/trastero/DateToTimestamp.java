/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package trastero;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
import java.util.*;
import java.text.*;
import java.sql.Timestamp;
public class DateToTimestamp {
    public static void main(String[] args) {
        try {
            Calendar cal = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyyhhmmssSSSS");
            String str_date=sdf.format(cal.getTime());
            System.out.println("Raw Date: "+str_date);

            DateFormat formatter ;
            Date date ;
            formatter = new SimpleDateFormat("ddMMyyyyhhmmssSSSS");
            date = (Date)formatter.parse(str_date);
            java.sql.Timestamp timeStampDate = new Timestamp(date.getTime());
            System.out.println("Today is " +timeStampDate.toString());
            System.out.println("Today is " +date.toString());
        } catch (ParseException e){
            System.out.println("Exception :"+e);
        }
    }
}