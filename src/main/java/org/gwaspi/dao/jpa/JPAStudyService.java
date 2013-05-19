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

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import org.gwaspi.dao.StudyService;
import org.gwaspi.global.Config;
import org.gwaspi.gui.GWASpiExplorerPanel;
import org.gwaspi.model.MatricesList;
import org.gwaspi.model.Matrix;
import org.gwaspi.model.SampleInfoList;
import org.gwaspi.model.Study;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JPA implementation of a study service.
 * Uses abstracted DB access to store data,
 * see persistence.xml for DB settings.
 */
public class JPAStudyService implements StudyService {

	private static final Logger LOG
			= LoggerFactory.getLogger(JPAStudyService.class);

	private final EntityManagerFactory emf;


	public JPAStudyService(EntityManagerFactory emf) {
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
	public Study getById(int studyId) throws IOException {

		Study study = null;

		EntityManager em = null;
		try {
			em = open();
			Query query = em.createNamedQuery("study_fetchById");
			query.setParameter("id", studyId);
			study = (Study) query.getSingleResult();
		} catch (NoResultException ex) {
			LOG.error("Failed fetching a study by id: " + studyId
					+ " (id not found)", ex);
		} catch (Exception ex) {
			LOG.error("Failed fetching a study by id: " + studyId, ex);
		} finally {
			close(em);
		}

		return study;
	}

	@Override
	public List<Study> getAll() throws IOException {

		List<Study> studies = Collections.EMPTY_LIST;

		EntityManager em = null;
		try {
			em = open();
			studies = em.createNamedQuery("study_list").getResultList();
		} catch (Exception ex) {
			LOG.error("Failed fetching all studies", ex);
		} finally {
			close(em);
		}

		return studies;
	}

	@Override
	public String createStudyManagementTable(Object[] insertValues) {

		int newStudyId = insertNewStudy((String) insertValues[0], (String) insertValues[1]);
		return "1";
	}

	@Override
	public int insertNewStudy(String name, String description) {

		Study study = new Study(name, description);

		EntityManager em = null;
		try {
			em = open();
			begin(em);
			if (study.getId() == Integer.MIN_VALUE) {
				em.persist(study);
			} else {
				em.merge(study);
			}
			commit(em);
		} catch (Exception ex) {
			LOG.error("Failed adding a study", ex);
			rollback(em);
		} finally {
			close(em);
		}

		return study.getId();
	}

	@Override
	public void deleteStudy(int studyId, boolean deleteReports) throws IOException {

		Study study = getById(studyId);

		List<Matrix> matrixList = MatricesList.getMatrixList(studyId);

		for (int i = 0; i < matrixList.size(); i++) {
			try {
				MatricesList.deleteMatrix(matrixList.get(i).getId(), deleteReports);
				GWASpiExplorerPanel.getSingleton().updateTreePanel(true);
			} catch (IOException ex) {
				LOG.warn(null, ex);
			}
		}

		// DELETE METADATA INFO FROM DB
		boolean removed = false;
		EntityManager em = null;
		try {
			em = open();
			begin(em);
			em.remove(study);
			commit(em);
			removed = true;
		} catch (Exception ex) {
			LOG.error("Failed removing a study", ex);
			rollback(em);
		} finally {
			close(em);
		}

		// DELETE STUDY FOLDERS
		String genotypesFolder = Config.getConfigValue(Config.PROPERTY_GENOTYPES_DIR, "");
		File gtStudyFolder = new File(genotypesFolder + "/STUDY_" + studyId);
		org.gwaspi.global.Utils.deleteFolder(gtStudyFolder);

		if (deleteReports) {
			String reportsFolder = Config.getConfigValue(Config.PROPERTY_REPORTS_DIR, "");
			File repStudyFolder = new File(reportsFolder + "/STUDY_" + studyId);
			org.gwaspi.global.Utils.deleteFolder(repStudyFolder);
		}

		// DELETE STUDY POOL SAMPLES
		SampleInfoList.deleteSamplesByPoolId(studyId);
	}

	@Override
	public String createStudyLogFile(Integer studyId) throws IOException {
//		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
		return "";
	}
}
