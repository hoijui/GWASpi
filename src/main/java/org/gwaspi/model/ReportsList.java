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
import org.gwaspi.dao.ReportService;
import org.gwaspi.dao.jpa.JPAReportService;

/**
 * @deprecated use ReportService directly
 */
public class ReportsList {

	private static final ReportService reportService
			= new JPAReportService(StudyList.getEntityManagerFactory());

	private ReportsList() {
	}

	public static Report getReport(ReportKey reportKey) throws IOException {
		return reportService.getReport(reportKey);
	}

	// XXX split into two methods, with either parentOperationId or parentMatrixId param, as we do not allow to fetch by specifying both anyway (on has to be (MatrixKey.)NULL_ID as it works now)
	public static List<Report> getReportsList(int parentOperationId, int parentMatrixId) throws IOException {
		return reportService.getReports(parentOperationId, parentMatrixId);
	}

	public static String getReportNamePrefix(OperationMetadata op) {
		return reportService.getReportNamePrefix(op);
	}

	public static void insertRPMetadata(Report report) throws IOException {
		reportService.insertReport(report);
	}

	public static void deleteReportByMatrixId(MatrixKey matrixKey) throws IOException {
		reportService.deleteReportByMatrixId(matrixKey);
	}

	public static void deleteReportByOperationId(int opId) throws IOException {
		reportService.deleteReportByOperationId(opId);
	}
}
