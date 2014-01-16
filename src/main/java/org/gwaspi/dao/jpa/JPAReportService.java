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

package org.gwaspi.dao.jpa;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import org.gwaspi.constants.cNetCDF.Defaults.OPType;
import org.gwaspi.dao.ReportService;
import org.gwaspi.model.MatrixKey;
import org.gwaspi.model.OperationKey;
import org.gwaspi.model.OperationMetadata;
import org.gwaspi.model.OperationsList;
import org.gwaspi.model.Report;
import org.gwaspi.model.ReportKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JPA implementation of a report service.
 * Uses abstracted DB access to store data,
 * see persistence.xml for DB settings.
 */
public class JPAReportService implements ReportService {

	private static final Logger LOG
			= LoggerFactory.getLogger(JPAReportService.class);

	private final EntityManagerFactory emf;


	public JPAReportService(EntityManagerFactory emf) {
		this.emf = emf;
	}

	private EntityManager open() {

		EntityManager em = emf.createEntityManager();
		return em;
	}
	private void begin(EntityManager em) {
		em.getTransaction().begin();
	}
	private void commit(EntityManager em) {
		em.getTransaction().commit();
	}
	private void rollback(EntityManager em) {

		if (em == null) {
			LOG.error("Failed to create an entity manager");
		} else {
			try {
				if (em.isOpen() && em.getTransaction().isActive()) {
					em.getTransaction().rollback();
				} else {
					LOG.error("Failed to rollback a transaction: no active"
							+ " connection or transaction");
				}
			} catch (PersistenceException ex) {
				LOG.error("Failed to rollback a transaction", ex);
			}
		}
	}
	private void close(EntityManager em) {

		if (em == null) {
			LOG.error("Failed to create an entity manager");
		} else {
			try {
				if (em.isOpen()) {
					em.close();
				}
			} catch (IllegalStateException ex) {
				LOG.error("Failed to close an entity manager", ex);
			}
		}
	}

	@Override
	public Report getReport(ReportKey reportKey) throws IOException {

		Report report = null;

		EntityManager em = null;
		try {
			em = open();
			report = em.find(Report.class, reportKey);
		} catch (Exception ex) {
			throw new IOException("Failed fetching a report by: " + reportKey, ex);
		} finally {
			close(em);
		}

		return report;
	}

	@Override
	public List<Report> getReports(int parentOperationId, int parentMatrixId) throws IOException {

		List<Report> reports = Collections.EMPTY_LIST;

		EntityManager em = null;
		try {
			em = open();
			Query query;
			if (parentMatrixId == MatrixKey.NULL_ID) {
				if (parentOperationId == OperationKey.NULL_ID) {
					throw new IllegalArgumentException("You have to specify at least one of either parentOperationId or parentMatrixId");
				}
				query = em.createNamedQuery(
						"report_fetchByParentOperationId");
				query.setParameter("parentOperationId", parentOperationId);
			} else if (parentOperationId == OperationKey.NULL_ID) {
				query = em.createNamedQuery(
						"report_fetchByParentMatrixId");
				query.setParameter("parentMatrixId", parentMatrixId);
			} else {
				query = em.createNamedQuery(
						"report_fetchByParentMatrixIdParentOperationId");
				query.setParameter("parentMatrixId", parentMatrixId);
				query.setParameter("parentOperationId", parentOperationId);
			}
			reports = (List<Report>) query.getResultList();
		} catch (Exception ex) {
			throw new IOException("Failed fetching a report by"
					+ ": parent-operation-id: " + parentOperationId
					+ ", parent-matrix-id: " + parentMatrixId
					+ "; (not found)", ex);
		} finally {
			close(em);
		}

		return reports;
	}

	@Override
	public String getReportNamePrefix(OperationMetadata op) {
		return extractReportNamePrefix(op);
	}

	public static String extractReportNamePrefix(OperationMetadata op) {
		StringBuilder prefix = new StringBuilder();
		prefix.append("mx-");
		prefix.append(op.getParentMatrixId());

		prefix.append("_").append(op.getOperationType().toString()).append("-");
		prefix.append(op.getId());

		// Get Genotype Freq. assigned name. Pry out the part inserted by user only
		try {
			final OPType operationType = op.getOperationType();
			if (operationType.equals(OPType.ALLELICTEST)
					|| operationType.equals(OPType.GENOTYPICTEST)
					|| operationType.equals(OPType.TRENDTEST))
			{
				OperationMetadata parentOp = OperationsList.getById(op.getParentOperationId());
				String[] tmp = parentOp.getFriendlyName().split("-", 2);
				tmp = tmp[1].split("using");
				prefix.append("_");
				prefix.append(org.gwaspi.global.Utils.stripNonAlphaNumericDashUndscr(tmp[0].trim()));
				prefix.append("_");
			}
		} catch (IOException ex) {
			LOG.error(null, ex);
		}

		return prefix.toString();
	}

	@Override
	public void insertReport(Report report) {

		EntityManager em = null;
		try {
			em = open();
			begin(em);
			if (report.getId() == ReportKey.NULL_ID) {
				em.persist(report);
			} else {
				em.merge(report);
			}
			commit(em);
		} catch (Exception ex) {
			LOG.error("Failed adding a report", ex);
			rollback(em);
		} finally {
			close(em);
		}
	}

	@Override
	public void deleteReportByMatrixId(MatrixKey parentMatrixKey) throws IOException {

		EntityManager em = null;
		try {
			em = open();
			begin(em);
			Query query = em.createNamedQuery("report_deleteByParentMatrixId");
			query.setParameter("parentMatrixId", parentMatrixKey.getMatrixId());
			query.executeUpdate();
			commit(em);
		} catch (Exception ex) {
			rollback(em);
			throw new IOException("Failed deleting reports by"
					+ ": parent-matrix-id: " + parentMatrixKey,
					ex);
		} finally {
			close(em);
		}
	}

	@Override
	public void deleteReportByOperationId(int parentOperationId) throws IOException {

		EntityManager em = null;
		try {
			em = open();
			Query query = em.createNamedQuery("report_deleteByParentOperationId");
			query.setParameter("parentOperationId", parentOperationId);
			query.executeUpdate();
		} catch (Exception ex) {
			rollback(em);
			throw new IOException("Failed deleting reports by"
					+ ": parent-operation-id: " + parentOperationId,
					ex);
		} finally {
			close(em);
		}
	}
}
