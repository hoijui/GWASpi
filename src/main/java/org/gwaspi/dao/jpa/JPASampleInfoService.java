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
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import org.gwaspi.dao.SampleInfoService;
import org.gwaspi.model.SampleInfo;
import org.gwaspi.model.SampleKey;
import org.gwaspi.model.StudyKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JPASampleInfoService implements SampleInfoService {

	private static final Logger LOG
			= LoggerFactory.getLogger(JPASampleInfoService.class);

	private final EntityManagerFactory emf;

	public JPASampleInfoService(EntityManagerFactory emf) {
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
	public List<SampleInfo> getSamples() throws IOException {

		List<SampleInfo> sampleInfos = Collections.EMPTY_LIST;

		EntityManager em = null;
		try {
			em = open();
			sampleInfos = em.createNamedQuery("sampleInfo_list").getResultList();
		} catch (Exception ex) {
			LOG.error("Failed fetching all sample-infos", ex);
		} finally {
			close(em);
		}

		return sampleInfos;
	}

	@Override
	public List<SampleInfo> getSamples(StudyKey studyKey) throws IOException {

		List<SampleInfo> sampleInfos = Collections.EMPTY_LIST;

		EntityManager em = null;
		try {
			em = open();
			Query query = em.createNamedQuery("sampleInfo_listByStudyId");
			query.setParameter("studyId", studyKey.getId());
			sampleInfos = (List<SampleInfo>) query.getResultList();
		} catch (NoResultException ex) {
			LOG.error("Failed fetching a sample-info by study-id: " + studyKey.getId()
					+ " (not found)", ex);
		} catch (Exception ex) {
			LOG.error("Failed fetching sample-info", ex);
		} finally {
			close(em);
		}

		return sampleInfos;
	}

	@Override
	public SampleInfo getSample(SampleKey key) throws IOException {

		SampleInfo sampleInfo = null;

		EntityManager em = null;
		try {
			em = open();
			sampleInfo = em.find(SampleInfo.class, key);
		} catch (Exception ex) {
			LOG.error("Failed fetching sample-info: " + key, ex);
		} finally {
			close(em);
		}

		return sampleInfo;
	}

	@Override
	public void deleteSamples(StudyKey studyKey) throws IOException {

		EntityManager em = null;
		try {
			em = open();
			begin(em);
			Query query = em.createNamedQuery("sampleInfo_deleteByStudyId");
			query.setParameter("studyId", studyKey.getId());
			query.executeUpdate();
			commit(em);
		} catch (Exception ex) {
			rollback(em);
			throw new IOException("Failed deleting sample-infos by"
					+ ": study-id: " + studyKey.getId(),
					ex);
		} finally {
			close(em);
		}
	}

	@Override
	public void insertSamples(Collection<SampleInfo> sampleInfos) throws IOException {

		for (SampleInfo sampleInfo : sampleInfos) {
			EntityManager em = null;
			try {
				em = open();
				begin(em);
				em.persist(sampleInfo);
				commit(em);
			} catch (Exception ex) {
				EntityManager emInner = null;
				try {
					emInner = open();
					begin(emInner);
					emInner.merge(sampleInfo); // TODO rather check the id, and decide to do persist or merge
					commit(emInner);
				} catch (Exception ex2) {
					LOG.error("Failed adding a sample-info", ex);
					LOG.error("Failed mergeing a sample-info", ex2);
					rollback(emInner);
				} finally {
					close(emInner);
				}
			} finally {
				close(em);
			}
		}
	}
}
