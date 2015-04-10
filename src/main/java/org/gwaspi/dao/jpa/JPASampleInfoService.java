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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import org.gwaspi.constants.NetCDFConstants.Variables;
import org.gwaspi.dao.SampleInfoService;
import org.gwaspi.model.SampleInfo;
import org.gwaspi.model.SampleKey;
import org.gwaspi.model.StudyKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JPASampleInfoService implements SampleInfoService {

	private static final Logger LOG
			= LoggerFactory.getLogger(JPASampleInfoService.class);

	private final JPAUtil jpaUtil;

	public JPASampleInfoService(EntityManagerFactory emf) {
		this.jpaUtil = new JPAUtil(emf);
	}

	@Override
	public List<SampleKey> getSampleKeys(StudyKey studyKey) throws IOException {

		List<SampleKey> sampleKeys = Collections.EMPTY_LIST;

		EntityManager em = null;
		try {
			em = jpaUtil.open();
			Query query;
			if (studyKey == null) {
				query = em.createNamedQuery("sampleInfo_listKeys");
			} else {
				query = em.createNamedQuery("sampleInfo_listKeysByStudyId");
				query.setParameter("studyId", studyKey.getId());
			}
			List<Object[]> sampleKeysParts = query.getResultList();
			sampleKeys = new ArrayList<SampleKey>(sampleKeysParts.size());
			for (Object[] sampleKeyParts : sampleKeysParts) {
				SampleKey sampleKey = new SampleKey(
						new StudyKey((Integer) sampleKeyParts[0]),
						(String) sampleKeyParts[1],
						(String) sampleKeyParts[2]);
				sampleKeys.add(sampleKey);
			}
		} catch (Exception ex) {
			throw new IOException("Failed fetching all matrices", ex);
		} finally {
			jpaUtil.close(em);
		}

		return sampleKeys;
	}

	@Override
	public List<SampleInfo> getSamples(StudyKey studyKey) throws IOException {

		List<SampleInfo> sampleInfos = Collections.EMPTY_LIST;

		EntityManager em = null;
		try {
			em = jpaUtil.open();
			Query query = em.createNamedQuery("sampleInfo_listByStudyId");
			query.setParameter("studyId", studyKey.getId());
			sampleInfos = (List<SampleInfo>) query.getResultList();
		} catch (NoResultException ex) {
			throw new IOException("Failed fetching a sample-info by study-id: "
					+ studyKey.getId() + " (not found)",
					ex);
		} catch (Exception ex) {
			throw new IOException("Failed fetching sample-info", ex);
		} finally {
			jpaUtil.close(em);
		}

		return sampleInfos;
	}

	@Override
	public SampleInfo getSample(SampleKey key) throws IOException {

		SampleInfo sampleInfo = null;

		EntityManager em = null;
		try {
			em = jpaUtil.open();
			sampleInfo = em.find(SampleInfo.class, key);
		} catch (Exception ex) {
			throw new IOException("Failed fetching sample-info: \"" + key.getStudyId() + "\" / \"" + key.getSampleId() + "\" / \"" + key.getFamilyId() + "\"", ex);
		} finally {
			jpaUtil.close(em);
		}

		return sampleInfo;
	}

	@Override
	public <T> Map<SampleKey, Integer> pickSamples(StudyKey studyKey, String variable, Collection<T> criteria, boolean include) throws IOException {

		// fetch the specified variable value for all the samples
		Collection<T> samplesVarValue;
		EntityManager em = null;
		try {
			em = jpaUtil.open();
			Query query;
			if (variable.equals(Variables.VAR_SAMPLE_KEY)) { // sample ID
				query = em.createNamedQuery("sampleInfo_listSampleIds");
			} else if (variable.equals(Variables.VAR_SAMPLES_AFFECTION)) {
				query = em.createNamedQuery("sampleInfo_listSampleAffections");
			} else if (variable.equals(Variables.VAR_SAMPLES_SEX)) {
				query = em.createNamedQuery("sampleInfo_listSampleSexes");
			} else {
				throw new UnsupportedOperationException("Unknown sample info variable: \"" + variable + "\"");
			}
			query.setParameter("studyId", studyKey.getId());
			samplesVarValue = (List<T>) query.getResultList();
		} catch (NoResultException ex) {
			throw new IOException(
					"Failed fetching sample-info variable by study-id: "
					+ studyKey.getId() + " (not found)",
					ex);
		} catch (Exception ex) {
			throw new IOException(ex);
		} finally {
			jpaUtil.close(em);
		}

		// do the picking
		Map<SampleKey, Integer> pickedSamples = new LinkedHashMap<SampleKey, Integer>();
		Iterator<SampleKey> sampleKeysIt = getSampleKeys(studyKey).iterator();
		int sampleIndex = 0;
		if (include) {
			for (T varValue : samplesVarValue) {
				SampleKey key = sampleKeysIt.next();
				if (criteria.contains(varValue)) {
					pickedSamples.put(key, sampleIndex);
				}
				sampleIndex++;
			}
		} else {
			for (T varValue : samplesVarValue) {
				SampleKey key = sampleKeysIt.next();
				if (!criteria.contains(varValue)) {
					pickedSamples.put(key, sampleIndex);
				}
				sampleIndex++;
			}
		}

		return pickedSamples;
	}

	@Override
	public void deleteSamples(StudyKey studyKey) throws IOException {

		EntityManager em = null;
		try {
			em = jpaUtil.open();
			jpaUtil.begin(em);
			Query query = em.createNamedQuery("sampleInfo_deleteByStudyId");
			query.setParameter("studyId", studyKey.getId());
			query.executeUpdate();
			jpaUtil.commit(em);
		} catch (Exception ex) {
			jpaUtil.rollback(em);
			throw new IOException("Failed deleting sample-infos by"
					+ ": study-id: " + studyKey.getId(),
					ex);
		} finally {
			jpaUtil.close(em);
		}
	}

	@Override
	public void insertSamples(Collection<SampleInfo> sampleInfos) throws IOException {

		for (SampleInfo sampleInfo : sampleInfos) {
			EntityManager em = null;
			try {
				em = jpaUtil.open();
				jpaUtil.begin(em);
				em.persist(sampleInfo);
				jpaUtil.commit(em);
			} catch (Exception ex) {
				EntityManager emInner = null;
				try {
					emInner = jpaUtil.open();
					jpaUtil.begin(emInner);
					emInner.merge(sampleInfo); // TODO rather check the id, and decide to do persist or merge
					jpaUtil.commit(emInner);
				} catch (Exception ex2) {
					LOG.error("Failed adding a sample-info", ex);
					LOG.error("Failed mergeing a sample-info", ex2);
					jpaUtil.rollback(emInner);
				} finally {
					jpaUtil.close(emInner);
				}
			} finally {
				jpaUtil.close(em);
			}
		}
	}
}
