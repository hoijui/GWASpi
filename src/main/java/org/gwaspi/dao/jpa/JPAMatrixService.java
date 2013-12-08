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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import org.gwaspi.dao.MatrixService;
import org.gwaspi.model.MatrixKey;
import org.gwaspi.model.MatrixMetadata;
import org.gwaspi.model.OperationMetadata;
import org.gwaspi.model.OperationsList;
import org.gwaspi.model.ReportsList;
import org.gwaspi.model.Study;
import org.gwaspi.model.StudyKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JPA implementation of a matrix service.
 * Uses abstracted DB access to store data,
 * see persistence.xml for DB settings.
 */
public class JPAMatrixService implements MatrixService {

	private static final Logger LOG
			= LoggerFactory.getLogger(JPAMatrixService.class);

	private final EntityManagerFactory emf;


	public JPAMatrixService(EntityManagerFactory emf) {
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

	private static List<MatrixKey> convertFieldsToMatrixKeys(List<Object[]> studyIdMatrixIds) {

		List<MatrixKey> matrices = new ArrayList<MatrixKey>(studyIdMatrixIds.size());
		for (Object[] matrixKeyParts : studyIdMatrixIds) {
			matrices.add(new MatrixKey(
					new StudyKey((Integer) matrixKeyParts[0]),
					(Integer) matrixKeyParts[1]));
		}

		return matrices;
	}

	@Override
	public List<MatrixKey> getMatrixKeys() throws IOException {

		List<MatrixKey> matrices = Collections.EMPTY_LIST;

		EntityManager em = null;
		try {
			em = open();
			List<Object[]> matricesKeyParts = em.createNamedQuery("matrixMetadata_listKeys").getResultList();
			matrices = convertFieldsToMatrixKeys(matricesKeyParts);
//		} catch (Exception ex) {
//			LOG.error("Failed fetching all matrices", ex);
		} finally {
			close(em);
		}

		return matrices;
	}

	@Override
	public List<MatrixKey> getMatrixKeys(StudyKey studyKey) throws IOException {

		List<MatrixKey> matrices = Collections.EMPTY_LIST;

		EntityManager em = null;
		try {
			em = open();
			Query query = em.createNamedQuery("matrixMetadata_listIdsByStudyId");
			query.setParameter("studyId", studyKey.getId());
			List<Integer> matricesIds = query.getResultList();
			matrices = new ArrayList<MatrixKey>(matricesIds.size());
			for (Integer matrixId : matricesIds) {
				matrices.add(new MatrixKey(studyKey, matrixId));
			}
//		} catch (Exception ex) {
//			LOG.error("Failed fetching all matrices", ex);
		} finally {
			close(em);
		}

		return matrices;
	}

	@Override
	public List<MatrixMetadata> getMatrices(StudyKey studyKey) throws IOException {

		List<MatrixMetadata> matricesMetadata = Collections.EMPTY_LIST;

		EntityManager em = null;
		try {
			em = open();
			Query query = em.createNamedQuery("matrixMetadata_listByStudyId");
			query.setParameter("studyId", studyKey.getId());
			matricesMetadata = query.getResultList();
//		} catch (IOException ex) {
//			LOG.error("Failed fetching all matrices-metadata", ex);
		} finally {
			close(em);
		}

		return matricesMetadata;
	}

	@Override
	public MatrixKey insertMatrix(MatrixMetadata matrixMetadata) throws IOException {

		EntityManager em = null;
		try {
			em = open();
			begin(em);
			if (matrixMetadata.getMatrixId() == Integer.MIN_VALUE) {
				em.persist(matrixMetadata);
			} else {
				em.merge(matrixMetadata);
			}
			commit(em);
//		} catch (Exception ex) {
//			LOG.error("Failed adding a matrix-metadata", ex);
//			rollback(em);
		} finally {
			close(em);
		}

		return MatrixKey.valueOf(matrixMetadata);
	}

	@Override
	public void deleteMatrix(MatrixKey matrixKey, boolean deleteReports) throws IOException {

		MatrixMetadata matrixMetadata = null;

		// DELETE METADATA INFO FROM DB
		EntityManager em = null;
		try {
			em = open();
			begin(em);
			matrixMetadata = em.find(MatrixMetadata.class, matrixKey);
			if (matrixMetadata == null) {
				throw new IllegalArgumentException("No matrix found with this ID: (" + matrixKey.getStudyId() + ") " + matrixKey.getMatrixId());
			}
			em.remove(matrixMetadata); // This is done implicitly by remove(matrix)
			commit(em);
		} catch (Exception ex) {
			rollback(em);
			throw new IOException("Failed deleting matrix by"
					+ ": study-id: " + matrixKey.getStudyId()
					+ ", matrix-id: " + matrixKey.getMatrixId(),
					ex);
		} finally {
			close(em);
		}

		String genotypesFolder = Study.constructGTPath(matrixMetadata.getKey().getStudyKey());

		// DELETE OPERATION netCDFs FROM THIS MATRIX
		List<OperationMetadata> operations = OperationsList.getOperationsList(matrixKey);
		for (OperationMetadata op : operations) {
			org.gwaspi.global.Utils.tryToDeleteFile(OperationMetadata.generatePathToNetCdfFile(op));
		}

		ReportsList.deleteReportByMatrixId(matrixKey);

		// DELETE MATRIX NETCDF FILE
		File matrixFile = new File(genotypesFolder + matrixMetadata.getMatrixNetCDFName() + ".nc");
		org.gwaspi.global.Utils.tryToDeleteFile(matrixFile);
	}

	@Override
	public void updateMatrix(MatrixMetadata matrixMetadata) throws IOException {

		EntityManager em = null;
		try {
			em = open();
			begin(em);
			em.merge(matrixMetadata);
			commit(em);
		} catch (Exception ex) {
			LOG.error("Failed adding a matrix-metadata", ex);
			rollback(em);
		} finally {
			close(em);
		}
	}

	@Override
	public MatrixMetadata getMatrix(MatrixKey matrixKey) throws IOException {

		MatrixMetadata matrixMetadata = null;

		EntityManager em = null;
		try {
			em = open();
			Query query = em.createNamedQuery("matrixMetadata_fetchById");
			query.setParameter("id", matrixKey.getMatrixId());
			matrixMetadata = (MatrixMetadata) query.getSingleResult();
		} catch (NoResultException ex) {
			LOG.error("Failed fetching matrix-metadata by id: " + matrixKey.toRawIdString()
					+ " (id not found)", ex);
			close(em);
			LOG.info("Available matrices:");
			List<MatrixKey> matrixList = getMatrixKeys();
			StringBuilder matrices = new StringBuilder();
			for (MatrixKey mat : matrixList) {
				matrices.append(" {study-id: ");
				matrices.append(mat.getStudyId()).append(", matrix-id: ");
				matrices.append(mat.getMatrixId()).append("}");
			}
			LOG.info(matrices.toString());
		} catch (Exception ex) {
			LOG.error("Failed fetching matrix-metadata by id: " + matrixKey.toRawIdString(), ex);
		} finally {
			close(em);
		}

		return matrixMetadata;
	}

	@Override
	public List<MatrixKey> getMatrixKeysByNetCdfName(String netCDFName) throws IOException {

		List<MatrixKey> matrices = Collections.EMPTY_LIST;

		EntityManager em = null;
		try {
			em = open();
			Query query = em.createNamedQuery("matrixMetadata_listKeysByNetCDFName");
			query.setParameter("netCDFName", netCDFName);
			matrices = convertFieldsToMatrixKeys(query.getResultList());
		} catch (NoResultException ex) {
			LOG.error("Failed fetching matrix-keys by netCDFname: " + netCDFName
					+ " (id not found)", ex);
		} catch (Exception ex) {
			LOG.error("Failed fetching matrix-keys by netCDFname: " + netCDFName, ex);
		} finally {
			close(em);
		}

		return matrices;
	}

	@Override
	public List<MatrixKey> getMatrixKeysByName(String matrixFriendlyName) throws IOException {

		List<MatrixKey> matrices = Collections.EMPTY_LIST;

		EntityManager em = null;
		try {
			em = open();
			Query query = em.createNamedQuery("matrixMetadata_listKeysByFriendlyName");
			query.setParameter("matrixFriendlyName", matrixFriendlyName);
			matrices = convertFieldsToMatrixKeys(query.getResultList());
		} catch (NoResultException ex) {
			LOG.error("Failed fetching matrix-keys by matrixFriendlyName: " + matrixFriendlyName
					+ " (id not found)", ex);
		} catch (Exception ex) {
			LOG.error("Failed fetching matrix-keys by matrixFriendlyName: " + matrixFriendlyName, ex);
		} finally {
			close(em);
		}

		return matrices;
	}
}
