/*
 * Copyright (C) 2015 Universitat Pompeu Fabra
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

package org.gwaspi.reports;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import org.gwaspi.constants.ExportConstants;
import org.gwaspi.global.Extractor;

public class ReportWriterRowWise {

	private static final String SEP = ExportConstants.SEPARATOR_REPORTS;

	private final File reportFile;
	private final List<Extractor<?, String>> extractors;

	public ReportWriterRowWise(final String reportPath, final String reportName, final List<Extractor<?, String>> extractors) {

		this.reportFile = new File(reportPath, reportName);
		this.extractors = extractors;
	}

	public ReportWriterRowWise(final File reportFile, final List<Extractor<?, String>> extractors) {

		this.reportFile = reportFile;
		this.extractors = extractors;
	}

	public void writeHeader(final List<String> headers) throws IOException {

		FileWriter outputFW = null;
		BufferedWriter outputBW = null;
		try {
			outputFW = new FileWriter(reportFile);
			outputBW = new BufferedWriter(outputFW);

			boolean firstColumn = true;
			for (final String header : headers) {
				if (firstColumn) {
					firstColumn = false;
				} else {
					outputBW.append(SEP);
				}
				outputBW.append(header);
			}
			outputBW.newLine();
		} finally {
			if (outputBW != null) {
				outputBW.close();
			} else if (outputFW != null) {
				outputFW.close();
			}
		}
	}

	public void appendEntry(final List<Object> values) throws IOException {

		FileWriter outputFW = null;
		BufferedWriter outputBW = null;
		try {
			outputFW = new FileWriter(reportFile, true);
			outputBW = new BufferedWriter(outputFW);

			boolean firstColumn = true;
			final Iterator<Extractor<?, String>> extractorsIt = extractors.iterator();
			for (final Object value : values) {
				if (firstColumn) {
					firstColumn = false;
				} else {
					outputBW.append(SEP);
				}
				outputBW.append(((Extractor<Object, String>) extractorsIt.next()).extract(value));
			}
			outputBW.newLine();
		} finally {
			if (outputBW != null) {
				outputBW.close();
			} else if (outputFW != null) {
				outputFW.close();
			}
		}
	}

	public void appendEntry(final Object... values) throws IOException {

		FileWriter outputFW = null;
		BufferedWriter outputBW = null;
		try {
			outputFW = new FileWriter(reportFile, true);
			outputBW = new BufferedWriter(outputFW);

			boolean firstColumn = true;
			final Iterator<Extractor<?, String>> extractorsIt = extractors.iterator();
			for (final Object value : values) {
				if (firstColumn) {
					firstColumn = false;
				} else {
					outputBW.append(SEP);
				}
				outputBW.append(((Extractor<Object, String>) extractorsIt.next()).extract(value));
			}
			outputBW.newLine();
		} finally {
			if (outputBW != null) {
				outputBW.close();
			} else if (outputFW != null) {
				outputFW.close();
			}
		}
	}

	public void close() throws IOException {
	}

}
