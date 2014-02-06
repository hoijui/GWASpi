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
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.gwaspi.constants.cNetCDF.Defaults.OPType;
import org.gwaspi.dao.OperationService;
import org.gwaspi.model.MatrixKey;
import org.gwaspi.model.OperationKey;
import org.gwaspi.model.OperationMetadata;
import org.gwaspi.model.ReportsList;
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
	public List<OperationMetadata> getOperations(MatrixKey origin) throws IOException {

		List<OperationMetadata> operationsMetadata = Collections.EMPTY_LIST;

		EntityManager em = null;
		try {
			em = open();
			Query query = em.createNamedQuery(
					"operationMetadata_listByStudyIdParentMatrixId");
			query.setParameter("studyId", origin.getStudyId());
			query.setParameter("parentMatrixId", origin.getMatrixId());
			operationsMetadata = query.getResultList();
		} catch (Exception ex) {
			LOG.error("Failed fetching operation-metadata", ex);
		} finally {
			close(em);
		}

		return operationsMetadata;
	}

	@Override
	public List<OperationMetadata> getOperations(MatrixKey origin, OPType opType) throws IOException {

		List<OperationMetadata> operations = Collections.EMPTY_LIST;

		EntityManager em = null;
		try {
			em = open();
			Query query = em.createNamedQuery(
					"operationMetadata_listByStudyIdParentMatrixIdOperationType");
			query.setParameter("studyId", origin.getStudyId());
			query.setParameter("parentMatrixId", origin.getMatrixId());
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
	public List<OperationMetadata> getOperations(OperationKey parent) throws IOException {

		List<OperationMetadata> operations = Collections.EMPTY_LIST;

		EntityManager em = null;
		try {
			em = open();
			Query query = em.createNamedQuery(
					"operationMetadata_listStudyIdByParentMatrixIdParentOperationId");
			query.setParameter("studyId", parent.getStudyId());
			query.setParameter("parentMatrixId", parent.getParentMatrixId());
			query.setParameter("parentOperationId", parent.getId());
			operations = query.getResultList();
		} catch (Exception ex) {
			LOG.error("Failed fetching operations", ex);
		} finally {
			close(em);
		}

		return operations;
	}

	@Override
	public List<OperationMetadata> getOperations(OperationKey parent, OPType opType) throws IOException {

		List<OperationMetadata> operations = Collections.EMPTY_LIST;

		EntityManager em = null;
		try {
			em = open();
			Query query = em.createNamedQuery(
					"operationMetadata_listByStudyIdParentMatrixIdParentOperationIdOperationType");
			query.setParameter("studyId", parent.getStudyId());
			query.setParameter("parentMatrixId", parent.getParentMatrixId());
			query.setParameter("parentOperationId", parent.getId());
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
	public List<OperationMetadata> getOperationAndSubOperations(OperationKey rootOperationKey) throws IOException {

		List<OperationMetadata> operationsMetadata = Collections.EMPTY_LIST;

		EntityManager em = null;
		try {
			em = open();
			Query query = em.createNamedQuery(
					"operationMetadata_listByStudyIdParentMatrixIdOperationId");
			query.setParameter("studyId", rootOperationKey.getStudyId());
			query.setParameter("parentMatrixId", rootOperationKey.getParentMatrixId());
			query.setParameter("operationId", rootOperationKey.getId());
			operationsMetadata = query.getResultList();

			query = em.createNamedQuery(
					"operationMetadata_listByStudyIdParentMatrixIdParentOperationId");
			query.setParameter("studyId", rootOperationKey.getStudyId());
			query.setParameter("parentMatrixId", rootOperationKey.getParentMatrixId());
			query.setParameter("parentOperationId", rootOperationKey.getId());
			operationsMetadata.addAll(query.getResultList());
		} catch (Exception ex) {
			LOG.error("Failed fetching operation-metadata", ex);
		} finally {
			close(em);
		}

		return operationsMetadata;
	}

	public List<OPType> getAncestorOperationTypes(OperationKey operationKey) throws IOException {

		List<OPType> operationTypes = new LinkedList<OPType>();

		OperationKey curOperationKey = operationKey;
		while (curOperationKey != null) {
			EntityManager em = null;
			try {
				em = open();
				Query query = em.createNamedQuery(
						"operationMetadata_getTypeByStudyIdParentMatrixIdOperationId");
				query.setParameter("studyId", curOperationKey.getStudyId());
				query.setParameter("parentMatrixId", curOperationKey.getParentMatrixId());
				query.setParameter("id", curOperationKey.getId());
				Object[] operationTypeAndParentOperationId = (Object[]) query.getResultList().get(0);
				OPType type = (OPType) operationTypeAndParentOperationId[0];
				Integer parentOperationId = (Integer) operationTypeAndParentOperationId[1];
				operationTypes.add(type);
				if (parentOperationId == null) {
					curOperationKey = null;
				} else {
					curOperationKey = new OperationKey(curOperationKey.getParentMatrixKey(), parentOperationId);
				}
			} catch (Exception ex) {
				LOG.error("Failed fetching operation type", ex);
				return Collections.EMPTY_LIST;
			} finally {
				close(em);
			}
		}

		return operationTypes;
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

		final StudyKey studyKey = new StudyKey(operationKey.getStudyId());

		try {
			OperationMetadata op = getOperation(operationKey);

			// delete child operations
			List<OperationMetadata> childOperations = getOperations(operationKey);
			if (!childOperations.isEmpty()) {
				childOperations.add(op);
				for (int i = 0; i < childOperations.size(); i++) {
					org.gwaspi.global.Utils.tryToDeleteFile(OperationMetadata.generatePathToNetCdfFile(childOperations.get(i)));
					final OperationKey childOperationKey = OperationKey.valueOf(childOperations.get(i));
					if (deleteReports) {
						ReportsList.deleteReportByOperationKey(childOperationKey);
					}

					EntityManager em = null;
					try {
						em = open();
						begin(em);
						OperationMetadata operation = em.find(OperationMetadata.class, childOperationKey);
						if (operation == null) {
							throw new IllegalArgumentException("No operation found with this key: " + operationKey.toRawIdString());
						}
						em.remove(operation);
						commit(em);
					} catch (Exception ex) {
						rollback(em);
						throw new IOException("Failed deleting child operation by"
								+ ": operation-key: " + childOperationKey.toRawIdString(),
								ex);
					} finally {
						close(em);
					}
				}
			} else {
				org.gwaspi.global.Utils.tryToDeleteFile(OperationMetadata.generatePathToNetCdfFile(op));
				if (deleteReports) {
					ReportsList.deleteReportByOperationKey(operationKey);
				}

				EntityManager em = null;
				try {
					em = open();
					begin(em);
					OperationMetadata operation = em.find(OperationMetadata.class, operationKey);
					if (operation == null) {
						throw new IllegalArgumentException("No operation found with this key: " + operationKey.toRawIdString());
					}
					em.remove(operation);
					commit(em);
				} catch (Exception ex) {
					rollback(em);
					throw new IOException("Failed deleting operation by"
							+ ": operation-key: " + operationKey.toRawIdString(),
							ex);
				} finally {
					close(em);
				}
			}
		} catch (Exception ex) {
			// PURGE INEXISTING OPERATIONS FROM DB
			EntityManager em = null;
			try {
				em = open();
				begin(em);
				OperationMetadata operation = em.find(OperationMetadata.class, operationKey);
				if (operation == null) {
					throw new IllegalArgumentException("No operation found with this key: " + operationKey.toRawIdString());
				}
				em.remove(operation);
				commit(em);
			} catch (Exception exi) {
				rollback(em);
				throw new IOException("Failed deleting operation by"
						+ ": study-id: " + studyKey.getId()
						+ ": operation-key: " + operationKey.toRawIdString(),
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
			final int studyId = (Integer) operationKeyParts[0];
			final int originMatrixId = (Integer) operationKeyParts[1];
			final int operationId = (Integer) operationKeyParts[2];
			final StudyKey studyKey = new StudyKey(studyId);
			final MatrixKey originMatrixKey = new MatrixKey(studyKey, originMatrixId);
			final OperationKey operationKey = new OperationKey(originMatrixKey, operationId);
			operations.add(operationKey);
		}

		return operations;
	}

	@Override
	public List<OperationKey> getOperationKeysByName(StudyKey studyKey, String operationName) throws IOException {

		List<OperationKey> operations = Collections.EMPTY_LIST;

		EntityManager em = null;
		try {
			em = open();
			Query query = em.createNamedQuery(
					"operationMetadata_listByStudyIdFriendlyName");
			query.setParameter("studyId", studyKey.getId());
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
