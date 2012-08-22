package org.gwaspi.trastero;

import java.sql.Timestamp;
import java.text.*;
import java.util.*;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class DateToTimestamp {

	public static void main(String[] args) {
		try {
			Calendar cal = Calendar.getInstance();
			SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyyhhmmssSSSS");
			String str_date = sdf.format(cal.getTime());
			System.out.println("Raw Date: " + str_date);

			DateFormat formatter = new SimpleDateFormat("ddMMyyyyhhmmssSSSS");
			Date date = formatter.parse(str_date);
			java.sql.Timestamp timeStampDate = new Timestamp(date.getTime());
			System.out.println("Today is " + timeStampDate.toString());
			System.out.println("Today is " + date.toString());
		} catch (ParseException e) {
			System.out.println("Exception :" + e);
		}
	}
}