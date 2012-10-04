package org.gwaspi.trastero;

import java.sql.Timestamp;
import java.text.*;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class DateToTimestamp {

	private static final Logger log = LoggerFactory.getLogger(DateToTimestamp.class);

	public static void main(String[] args) {
		try {
			Calendar cal = Calendar.getInstance();
			SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyyhhmmssSSSS");
			String str_date = sdf.format(cal.getTime());
			log.info("Raw Date: {}", str_date);

			DateFormat formatter = new SimpleDateFormat("ddMMyyyyhhmmssSSSS");
			Date date = formatter.parse(str_date);
			Timestamp timeStampDate = new Timestamp(date.getTime());
			log.info("Today is {}", timeStampDate.toString());
			log.info("Today is {}", date.toString());
		} catch (ParseException ex) {
			log.warn("Failed ot format date" + ex);
		}
	}
}