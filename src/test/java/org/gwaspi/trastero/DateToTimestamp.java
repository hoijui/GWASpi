/*
 * Copyright (C) 2013 Universitat Pompeu Fabra
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.gwaspi.trastero;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DateToTimestamp {

	private static final Logger log = LoggerFactory.getLogger(DateToTimestamp.class);

	public static void main(final String[] args) {
		try {
			final Calendar cal = Calendar.getInstance();
			final SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyyhhmmssSSSS");
			final String str_date = sdf.format(cal.getTime());
			log.info("Raw Date: {}", str_date);

			final DateFormat formatter = new SimpleDateFormat("ddMMyyyyhhmmssSSSS");
			final Date date = formatter.parse(str_date);
			final Timestamp timeStampDate = new Timestamp(date.getTime());
			log.info("Today is {}", timeStampDate.toString());
			log.info("Today is {}", date.toString());
		} catch (final ParseException ex) {
			log.warn("Failed ot format date" + ex);
		}
	}
}