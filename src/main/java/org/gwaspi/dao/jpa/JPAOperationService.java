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

import java.util.Collections;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.gwaspi.constants.cNetCDF.Defaults.OPType;
import org.gwaspi.dao.OperationService;
import org.gwaspi.model.MatrixKey;
import org.gwaspi.model.OperationKey;
import org.gwaspi.model.OperationMetadata;
import org.gwaspi.model.ReportsList;
import org.gwaspi.model.Study;
import org.gwaspi.model.StudyKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JPA implementation of a operation service.
 * Uses abstracted DB access to store data,
 * see persistence.xml for DB settings.
 */
public class JPAOperationService implements OperationService {

	private static final Logger LOG
			= LoggerFactory.getLogger(JPAOperationService.class);

	private final EntityManagerFactory emf;


	public JPAOperationService(EntityManagerFactory emf) {
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
	public OperationMetadata getOperation(OperationKey operationKey) throws IOException {

		OperationMetadata operationMetadata = null;

		EntityManager em = null;
		try {
			em = open();
			operationMetadata = em.find(OperationMetadata.class, operationKey);
		} catch (Exception ex) {
			throw new IOException("Failed fetching a operation-metadata by id: " + operationKey, ex);
		} finally {
			close(em);
		}

		return operationMetadata;
	}

	@Override
	public OperationMetadata getOperation(int operationId) throws IOException {

		OperationMetadata operationMetadata = null;

		EntityManager em = null;
		try {
			em = open();
			Query query = em.createNamedQuery("operationMetadata_fetchById");
			query.setParameter("id", operationId);
			operationMetadata = (OperationMetadata) query.getSingleResult();
		} catch (NoResultException ex) {
			LOG.error("Failed fetching a operation-metadata by id: " + operationId
					+ " (id not found)", ex);
		} catch (Exception ex) {
			LOG.error("Failed fetching a operation-metadata by id: " + operationId, ex);
		} finally {
			close(em);
		}

		return operationMetadata;
	}

	@Override
	public List<OperationMetadata> getOperations(int parentMatrixId, int parentOpId) throws IOException {

		List<OperationMetadata> operations = Collections.EMPTY_LIST;

		EntityManager em = null;
		try {
			em = open();
			Query query = em.createNamedQuery(
					"operationMetadata_listByParentMatrixIdParentOperationId");
			query.setParameter("parentMatrixId", parentMatrixId);
			query.setParameter("parentOperationId", parentOpId);
			operations = query.getResultList();
		} catch (Exception ex) {
			LOG.error("Failed fetching operations", ex);
		} finally {
			close(em);
		}

		return operations;
	}

	@Override
	public List<OperationMetadata> getOperations(int parentMatrixId, int parentOpId, OPType opType) throws IOException {

		List<OperationMetadata> operations = Collections.EMPTY_LIST;

		EntityManager em = null;
		try {
			em = open();
			Query query = em.createNamedQuery(
					"operationMetadata_listByParentMatrixIdParentOperationIdOperationType");
			query.setParameter("parentMatrixId", parentMatrixId);
			query.setParameter("parentOperationId", parentOpId);
			query.setParameter("operationType", opType);
			operations = query.getResultList();
		} catch (Exception ex) {
			LOG.error("Failed fetching operations", ex);
		} finally {
			close(em);
		}

		return operations;
	}

	@Override
	public List<OperationMetadata> getOperations(MatrixKey parentMatrixKey) throws IOException {

		List<OperationMetadata> operationsMetadata = Collections.EMPTY_LIST;

		EntityManager em = null;
		try {
			em = open();
			Query query = em.createNamedQuery(
					"operationMetadata_listByParentMatrixId");
			query.setParameter("parentMatrixId", parentMatrixKey.getMatrixId());
			operationsMetadata = query.getResultList();
		} catch (Exception ex) {
			LOG.error("Failed fetching operation-metadata", ex);
		} finally {
			close(em);
		}

		return operationsMetadata;
	}

	@Override
	public List<OperationMetadata> getOperationAndSubOperations(OperationKey operationKey) throws IOException {

		List<OperationMetadata> operationsMetadata = Collections.EMPTY_LIST;

		EntityManager em = null;
		try {
			em = open();
			Query query = em.createNamedQuery(
					"operationMetadata_listByParentMatrixIdOperationId");
			query.setParameter("parentMatrixId", operationKey.getParentMatrixId());
			query.setParameter("operationId", operationKey.getId());
			operationsMetadata = query.getResultList();

			query = em.createNamedQuery(
					"operationMetadata_listByParentMatrixIdParentOperationId");
			query.setParameter("parentMatrixId", operationKey.getParentMatrixId());
			query.setParameter("parentOperationId", operationKey.getId());
			operationsMetadata.addAll(query.getResultList());
		} catch (Exception ex) {
			LOG.error("Failed fetching operation-metadata", ex);
		} finally {
			close(em);
		}

		return operationsMetadata;
	}

	@Override
	public OperationKey insertOperation(OperationMetadata operationMetadata) throws IOException {

		EntityManager em = null;
		try {
			em = open();
			begin(em);
			em.persist(operationMetadata);
			commit(em);
		} catch (Exception ex) {
			LOG.error("Failed persisting operation-metadata", ex);
			rollback(em);
		} finally {
			close(em);
		}

		return OperationKey.valueOf(operationMetadata);
	}

	@Override
	public void deleteOperation(OperationKey operationKey, boolean deleteReports) throws IOException {

		final int opId = operationKey.getId();
		final StudyKey studyKey = new StudyKey(operationKey.getStudyId());

		try {
			OperationMetadata op = getOperation(operationKey);

			// delete child operations
			List<OperationMetadata> operations = getOperations(op.getParentMatrixId(), opId);
			if (!operations.isEmpty()) {
				operations.add(op);
				for (int i = 0; i < operations.size(); i++) {
					org.gwaspi.global.Utils.tryToDeleteFile(OperationMetadata.generatePathToNetCdfFile(operations.get(i)));
					if (deleteReports) {
						ReportsList.deleteReportByOperationId(operations.get(i).getId());
					}

					EntityManager em = null;
					int operationId = OperationKey.NULL_ID;
					try {
						em = open();
						begin(em);
						operationId = operations.get(i).getId();
						OperationMetadata operation = em.find(OperationMetadata.class, operationKey);
						if (operation == null) {
							throw new IllegalArgumentException("No operation found with this ID: " + operationKey.getId());
						}
						em.remove(operation);
						commit(em);
					} catch (Exception ex) {
						rollback(em);
						throw new IOException("Failed deleting operation by"
								+ ": study-id: " + studyKey.getId()
								+ ", operation-id: " + operationId,
								ex);
					} finally {
						close(em);
					}
				}
			} else {
				org.gwaspi.global.Utils.tryToDeleteFile(OperationMetadata.generatePathToNetCdfFile(op));
				if (deleteReports) {
					ReportsList.deleteReportByOperationId(opId);
				}

				EntityManager em = null;
				try {
					em = open();
					begin(em);
					OperationMetadata operation = em.find(OperationMetadata.class, operationKey);
					if (operation == null) {
						throw new IllegalArgumentException("No operation found with this ID: " + operationKey.getId());
					}
					em.remove(operation);
					commit(em);
				} catch (Exception ex) {
					rollback(em);
					throw new IOException("Failed deleting operation by"
							+ ": operation-id: " + opId,
							ex);
				} finally {
					close(em);
				}
			}
		} catch (Exception ex) {
			// PURGE INEXISTING OPERATIONS FROM DB
			OperationMetadata op = getOperation(opId);
			EntityManager em = null;
			try {
				em = open();
				begin(em);
				OperationMetadata operation = em.find(OperationMetadata.class, operationKey);
				if (operation == null) {
					throw new IllegalArgumentException("No operation found with this ID: " + operationKey.getId());
				}
				em.remove(operation);
				commit(em);
			} catch (Exception exi) {
				rollback(em);
				throw new IOException("Failed deleting operation by"
						+ ": study-id: " + studyKey.getId()
						+ ", operation-id: " + opId,
						exi);
			} finally {
				close(em);
			}
			throw new IOException(ex);
		}
	}

	private static List<OperationKey> convertFieldsToOperationKeys(List<Object[]> studyIdMatrixIdOperationIds) {

		List<OperationKey> operations = new ArrayList<OperationKey>(studyIdMatrixIdOperationIds.size());
		for (Object[] operationKeyParts : studyIdMatrixIdOperationIds) {
			operations.add(
					new OperationKey(
							new MatrixKey(
									new StudyKey((Integer) operationKeyParts[0]),
									(Integer) operationKeyParts[1]),
							(Integer) operationKeyParts[2]));
		}

		return operations;
	}

	@Override
	public List<OperationKey> getOperationKeysByName(String operationName) throws IOException {

		List<OperationKey> operations = Collections.EMPTY_LIST;

		EntityManager em = null;
		try {
			em = open();
			Query query = em.createNamedQuery(
					"operationMetadata_listByFriendlyName");
			query.setParameter("name", operationName);
			operations = convertFieldsToOperationKeys(query.getResultList());
		} catch (NoResultException ex) {
			LOG.error("Failed fetching operation-keys operation-name: " + operationName
					+ " (id not found)", ex);
		} catch (Exception ex) {
			LOG.error("Failed fetching operation-keys operation-name: " + operationName, ex);
		} finally {
			close(em);
		}

		return operations;
	}

//	/**
//	 * loads:
//	 * - int opSetSize = cNetCDF.Dimensions.DIM_OPSET
//	 * - int implicitDim = cNetCDF.Dimensions.DIM_IMPLICITSET
//	 */
//	public static OperationMetadata completeOperationMetadata(OperationMetadata toComplete) throws IOException {
//
//		int opSetSize = Integer.MIN_VALUE;
//		int implicitSetSize = Integer.MIN_VALUE;
//
//		String pathToStudy = Study.constructGTPath(new StudyKey(toComplete.getStudyId()));
//		String pathToMatrix = pathToStudy + toComplete.getMatrixCDFName() + ".nc";
//		if (new File(pathToMatrix).exists()) {
//			NetcdfFile ncfile = null;
//			try {
//				ncfile = NetcdfFile.open(pathToMatrix);
////				gtCode = ncfile.findGlobalAttribute(cNetCDF.Attributes.GLOB_GTCODE).getStringValue();
//
//				Dimension markerSetDim = ncfile.findDimension(cNetCDF.Dimensions.DIM_OPSET);
//				opSetSize = markerSetDim.getLength();
//
//				Dimension implicitDim = ncfile.findDimension(cNetCDF.Dimensions.DIM_IMPLICITSET);
//				implicitSetSize = implicitDim.getLength();
//			} catch (IOException ex) {
//				LOG.error("Cannot open file: " + pathToMatrix, ex);
//			} finally {
//				if (null != ncfile) {
//					try {
//						ncfile.close();
//					} catch (IOException ex) {
//						LOG.warn("Cannot close file: " + ncfile.getLocation(), ex);
//					}
//				}
//			}
//		}
//
//		toComplete.setPathToMatrix(pathToMatrix);
//		toComplete.setOpSetSize(opSetSize);
//		toComplete.setImplicitSetSize(implicitSetSize);
//
//		return toComplete;
//	}
}
