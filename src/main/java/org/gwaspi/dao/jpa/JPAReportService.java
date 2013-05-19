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
import javax.persistence.NoResultException;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import org.gwaspi.constants.cNetCDF.Defaults.OPType;
import org.gwaspi.dao.ReportService;
import org.gwaspi.model.Operation;
import org.gwaspi.model.OperationsList;
import org.gwaspi.model.Report;
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
	public Report getById(int reportId) throws IOException {

		Report report = null;

		EntityManager em = null;
		try {
			em = open();
			Query query = em.createNamedQuery("report_fetchById");
			query.setParameter("id", reportId);
			report = (Report) query.getSingleResult();
		} catch (NoResultException ex) {
			LOG.error("Failed fetching a report by id: " + reportId
					+ " (id not found)", ex);
		} catch (Exception ex) {
			LOG.error("Failed fetching a report by id: " + reportId, ex);
		} finally {
			close(em);
		}

		return report;
	}

	@Override
	public List<Report> getReportsList(int parentOperationId, int parentMatrixId) throws IOException {

		List<Report> reports = Collections.EMPTY_LIST;

		EntityManager em = null;
		try {
			em = open();
			Query query;
			if (parentMatrixId == Integer.MIN_VALUE) {
				query = em.createNamedQuery(
						"report_fetchByParentOperationId");
				query.setParameter("parentOperationId", parentOperationId);
			} else {
				query = em.createNamedQuery(
						"report_fetchByParentMatrixId");
				query.setParameter("parentMatrixId", parentMatrixId);
			}
			reports = (List<Report>) query.getResultList();
		} catch (NoResultException ex) {
			LOG.error("Failed fetching a report by"
					+ ": parent-operation-id: " + parentOperationId
					+ ", parent-matrix-id: " + parentMatrixId
					+ "; (not found)", ex);
		} catch (Exception ex) {
			LOG.error("Failed fetching report", ex);
		} finally {
			close(em);
		}

		return reports;
	}

	@Override
	public String getReportNamePrefix(Operation op) {
		return extractReportNamePrefix(op);
	}

	public static String extractReportNamePrefix(Operation op) {
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
				Operation parentOp = OperationsList.getById(op.getParentOperationId());
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
	public String createReportsMetadataTable() {
//		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
		return "1";
	}

	@Override
	public void insertRPMetadata(Report report) {

		EntityManager em = null;
		try {
			em = open();
			begin(em);
			if (report.getId() == Integer.MIN_VALUE) {
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
	public void deleteReportByMatrixId(int parentMatrixId) throws IOException {

		int deleted = 0;

		EntityManager em = null;
		try {
			em = open();
			Query query = em.createNamedQuery("report_deleteByParentMatrixId");
			query.setParameter("parentMatrixId", parentMatrixId);
			deleted = query.executeUpdate();
		} catch (NoResultException ex) {
			LOG.error("Failed deleting reports by"
					+ ": parent-matrix-id: " + parentMatrixId
					+ "; (not found)", ex);
		} catch (Exception ex) {
			LOG.error("Failed deleting reports", ex);
		} finally {
			close(em);
		}
	}

	@Override
	public void deleteReportByOperationId(int parentOperationId) throws IOException {

		int deleted = 0;

		EntityManager em = null;
		try {
			em = open();
			Query query = em.createNamedQuery("report_deleteByParentOperationId");
			query.setParameter("parentOperationId", parentOperationId);
			deleted = query.executeUpdate();
		} catch (NoResultException ex) {
			LOG.error("Failed deleting reports by"
					+ ": parent-operation-id: " + parentOperationId
					+ "; (not found)", ex);
		} catch (Exception ex) {
			LOG.error("Failed deleting reports", ex);
		} finally {
			close(em);
		}
	}
}
