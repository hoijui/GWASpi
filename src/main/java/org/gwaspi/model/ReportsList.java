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

package org.gwaspi.model;

import java.io.IOException;
import java.util.List;
import org.gwaspi.constants.cNetCDF.Defaults.OPType;
import org.gwaspi.dao.ReportService;
import org.gwaspi.dao.jpa.JPAReportService;

/**
 * @deprecated use ReportService directly
 */
public class ReportsList {

	private static ReportService reportService = null;

	private ReportsList() {
	}

	static void clearInternalService() {
		reportService = null;
	}

	private static ReportService getReportService() {

		if (reportService == null) {
			reportService = new JPAReportService(StudyList.getEntityManagerFactory());
		}

		return reportService;
	}

	public static Report getReport(ReportKey reportKey) throws IOException {
		return getReportService().getReport(reportKey);
	}

	/** @deprecated use {@link #getReportsList(DataSetKey)} instead */
	public static List<Report> getReportsList(MatrixKey parentMatrixKey) throws IOException {
		return getReportService().getReports(new DataSetKey(parentMatrixKey));
	}

	/** @deprecated use {@link #getReportsList(DataSetKey)} instead */
	public static List<Report> getReportsList(OperationKey parentOperationKey) throws IOException {
		return getReportService().getReports(new DataSetKey(parentOperationKey));
	}

	public static List<Report> getReportsList(DataSetKey parentKey) throws IOException {
		return getReportService().getReports(parentKey);
	}

	public static List<Report> getReportsList(DataSetKey parentKey, OPType reportType) throws IOException {
		return getReportService().getReports(parentKey, reportType);
	}

	public static String getReportNamePrefix(OperationMetadata op) {
		return getReportService().getReportNamePrefix(op);
	}

	public static void insertRPMetadata(Report report) throws IOException {
		getReportService().insertReport(report);
	}

	public static void deleteReportByMatrixKey(MatrixKey parentMatrixKey) throws IOException {
		getReportService().deleteReportByMatrixKey(parentMatrixKey);
	}

	public static void deleteReportByOperationKey(OperationKey parentOperationKey) throws IOException {
		getReportService().deleteReportByOperationKey(parentOperationKey);
	}
}
