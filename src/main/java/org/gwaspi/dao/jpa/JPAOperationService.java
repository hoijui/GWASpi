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
import javax.persistence.Query;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.gwaspi.constants.NetCDFConstants.Defaults.OPType;
import org.gwaspi.dao.OperationService;
import org.gwaspi.dao.ReportService;
import org.gwaspi.model.DataSetKey;
import org.gwaspi.model.MatrixKey;
import org.gwaspi.model.OperationKey;
import org.gwaspi.model.OperationMetadata;
import org.gwaspi.model.ReportsList;
import org.gwaspi.model.StudyKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JPA implementation of a operation service.
 */
public class JPAOperationService implements OperationService {

	private static final Logger LOG
			= LoggerFactory.getLogger(JPAOperationService.class);

	private final JPAUtil jpaUtil;

	public JPAOperationService(EntityManagerFactory emf) {
		this.jpaUtil = new JPAUtil(emf);
	}

	private ReportService getReportService() {
		return ReportsList.getReportService();
	}

	@Override
	public OperationMetadata getOperationMetadata(OperationKey operationKey) throws IOException {

		OperationMetadata operationMetadata = null;

		EntityManager em = null;
		try {
			em = jpaUtil.open();
			operationMetadata = em.find(OperationMetadata.class, operationKey);
		} catch (Exception ex) {
			throw new IOException("Failed fetching operation-metadata by id: " + operationKey, ex);
		} finally {
			jpaUtil.close(em);
		}

		return operationMetadata;
	}

	@Override
	public List<OperationMetadata> getOffspringOperationsMetadata(DataSetKey root) throws IOException {

		if (root.isMatrix()) {
			return getOffspringOperationsMetadata(root.getMatrixParent());
		} else {
			return getOffspringOperationsMetadata(root.getOperationParent());
		}
	}

	@Override
	public List<OperationMetadata> getVisibleOffspringOperationsMetadata(DataSetKey root) throws IOException {

		if (root.isMatrix()) {
			return getVisibleOffspringOperationsMetadata(root.getMatrixParent());
		} else {
			return getVisibleOffspringOperationsMetadata(root.getOperationParent());
		}
	}

	@Override
	public List<OperationMetadata> getOffspringOperationsMetadata(DataSetKey root, OPType type) throws IOException {

		if (root.isMatrix()) {
			return getOffspringOperationsMetadata(root.getMatrixParent(), type);
		} else {
			return getOffspringOperationsMetadata(root.getOperationParent(), type);
		}
	}

	private List<OperationMetadata> getOffspringOperationsMetadata(MatrixKey root) throws IOException {

		List<OperationMetadata> operationsMetadata = Collections.EMPTY_LIST;

		EntityManager em = null;
		try {
			em = jpaUtil.open();
			Query query = em.createNamedQuery(
					"operationMetadata_listByStudyIdParentMatrixId");
			query.setParameter("studyId", root.getStudyId());
			query.setParameter("parentMatrixId", root.getMatrixId());
			operationsMetadata = query.getResultList();
		} catch (Exception ex) {
			LOG.error("Failed fetching operation-metadata", ex);
		} finally {
			jpaUtil.close(em);
		}

		return operationsMetadata;
	}

	private List<OperationMetadata> getVisibleOffspringOperationsMetadata(final MatrixKey root) throws IOException {

		final List<OperationMetadata> offspringOperations = new LinkedList<OperationMetadata>();

		final List<OperationMetadata> rootChildrenMetadata = getChildrenOperationsMetadata(root);
		for (final OperationMetadata rootChildMetadata : rootChildrenMetadata) {
			offspringOperations.addAll(getVisibleOffspringOperationsMetadata(OperationKey.valueOf(rootChildMetadata)));
		}

		return offspringOperations;
	}

	private List<OperationMetadata> getOffspringOperationsMetadata(MatrixKey root, OPType opType) throws IOException {

		List<OperationMetadata> operations = Collections.EMPTY_LIST;

		EntityManager em = null;
		try {
			em = jpaUtil.open();
			Query query = em.createNamedQuery(
					"operationMetadata_listByStudyIdParentMatrixIdOperationType");
			query.setParameter("studyId", root.getStudyId());
			query.setParameter("parentMatrixId", root.getMatrixId());
			query.setParameter("operationType", opType);
			operations = query.getResultList();
		} catch (Exception ex) {
			LOG.error("Failed fetching operations", ex);
		} finally {
			jpaUtil.close(em);
		}

		return operations;
	}

	private List<OperationMetadata> getOffspringOperationsMetadata(OperationKey root) throws IOException {

		List<OperationMetadata> offspringOperations = new LinkedList<OperationMetadata>();

		List<OperationKey> possibleParents = new LinkedList<OperationKey>();
		possibleParents.add(root);
		while (!possibleParents.isEmpty()) {
			OperationKey possibleParent = possibleParents.remove(0);
			final List<OperationMetadata> childrenOperations = getChildrenOperationsMetadata(possibleParent);
			offspringOperations.addAll(childrenOperations);
			for (OperationMetadata childOperation : childrenOperations) {
				possibleParents.add(OperationKey.valueOf(childOperation));
			}
		}

		return offspringOperations;
	}

	private List<OperationMetadata> getVisibleOffspringOperationsMetadata(final OperationKey root) throws IOException {

		final List<OperationMetadata> offspringOperations = new LinkedList<OperationMetadata>();

		final OperationMetadata rootMetadata = getOperationMetadata(root);
		if (!rootMetadata.isHidden()) {
			final List<OperationKey> possibleParents = new LinkedList<OperationKey>();
			possibleParents.add(root);
			while (!possibleParents.isEmpty()) {
				final OperationKey possibleParent = possibleParents.remove(0);
				final List<OperationMetadata> childrenOperations = getChildrenOperationsMetadata(possibleParent);
				for (final OperationMetadata childOperation : childrenOperations) {
					if (!childOperation.isHidden()) {
						offspringOperations.add(childOperation);
						possibleParents.add(OperationKey.valueOf(childOperation));
					}
				}
			}
		}

		return offspringOperations;
	}

	private List<OperationMetadata> getOffspringOperationsMetadata(OperationKey root, OPType type) throws IOException {

		List<OperationMetadata> offspringOperations = new LinkedList<OperationMetadata>();

		List<OperationKey> possibleParents = new LinkedList<OperationKey>();
		possibleParents.add(root);
		while (!possibleParents.isEmpty()) {
			OperationKey possibleParent = possibleParents.remove(0);
			final List<OperationMetadata> childrenOperations = getChildrenOperationsMetadata(possibleParent);
			for (OperationMetadata childOperation : childrenOperations) {
				if (childOperation.getOperationType() == type) {
					offspringOperations.add(childOperation);
				}
				possibleParents.add(OperationKey.valueOf(childOperation));
			}
		}

		return offspringOperations;
	}

	private List<OperationMetadata> getChildrenOperationsMetadata(OperationKey parent) throws IOException {

		List<OperationMetadata> operations = Collections.EMPTY_LIST;

		EntityManager em = null;
		try {
			em = jpaUtil.open();
			Query query = em.createNamedQuery(
					"operationMetadata_listByStudyIdParentMatrixIdParentOperationId");
			query.setParameter("studyId", parent.getStudyId());
			query.setParameter("parentMatrixId", parent.getParentMatrixId());
			query.setParameter("parentOperationId", parent.getId());
			operations = query.getResultList();
		} catch (Exception ex) {
			LOG.error("Failed fetching operations", ex);
		} finally {
			jpaUtil.close(em);
		}

		return operations;
	}

	private List<OperationMetadata> getChildrenOperationsMetadata(OperationKey parent, OPType opType) throws IOException {

		List<OperationMetadata> operations = Collections.EMPTY_LIST;

		EntityManager em = null;
		try {
			em = jpaUtil.open();
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
			jpaUtil.close(em);
		}

		return operations;
	}

	public List<OperationMetadata> getChildrenOperationsMetadata(MatrixKey parent) throws IOException {
		return getChildrenOperationsMetadata(new OperationKey(parent, OperationKey.NULL_ID));
	}

	public List<OperationMetadata> getChildrenOperationsMetadata(MatrixKey parent, OPType opType) throws IOException {
		return getChildrenOperationsMetadata(new OperationKey(parent, OperationKey.NULL_ID), opType);
	}

	@Override
	public List<OperationMetadata> getChildrenOperationsMetadata(DataSetKey parent) throws IOException {

		if (parent.isMatrix()) {
			return getChildrenOperationsMetadata(parent.getMatrixParent());
		} else {
			return getChildrenOperationsMetadata(parent.getOperationParent());
		}
	}

	@Override
	public List<OperationMetadata> getChildrenOperationsMetadata(DataSetKey parent, OPType opType) throws IOException {

		if (parent.isMatrix()) {
			return getChildrenOperationsMetadata(parent.getMatrixParent(), opType);
		} else {
			return getChildrenOperationsMetadata(parent.getOperationParent(), opType);
		}
	}

	@Override
	public List<OPType> getAncestorOperationTypes(OperationKey operationKey) throws IOException {

		List<OPType> operationTypes = new LinkedList<OPType>();

		OperationKey curOperationKey = operationKey;
		while (curOperationKey != null) {
			EntityManager em = null;
			try {
				em = jpaUtil.open();
				Query query = em.createNamedQuery(
						"operationMetadata_getTypeByStudyIdParentMatrixIdOperationId");
				query.setParameter("studyId", curOperationKey.getStudyId());
				query.setParameter("parentMatrixId", curOperationKey.getParentMatrixId());
				query.setParameter("id", curOperationKey.getId());
				Object[] operationTypeAndParentOperationId = (Object[]) query.getResultList().get(0);
				OPType type = (OPType) operationTypeAndParentOperationId[0];
				Integer parentOperationId = (Integer) operationTypeAndParentOperationId[1];
				operationTypes.add(type);
				if (parentOperationId == OperationKey.NULL_ID) {
					curOperationKey = null;
				} else {
					curOperationKey = new OperationKey(curOperationKey.getParentMatrixKey(), parentOperationId);
				}
			} catch (Exception ex) {
				LOG.error("Failed fetching operation type", ex);
				return Collections.EMPTY_LIST;
			} finally {
				jpaUtil.close(em);
			}
		}

		return operationTypes;
	}

	@Override
	public OperationKey insertOperation(OperationMetadata operationMetadata) throws IOException {

		EntityManager em = null;
		try {
			em = jpaUtil.open();
			jpaUtil.begin(em);
			em.persist(operationMetadata);
			jpaUtil.commit(em);
		} catch (Exception ex) {
			LOG.error("Failed persisting operation-metadata", ex);
			jpaUtil.rollback(em);
		} finally {
			jpaUtil.close(em);
		}

		return OperationKey.valueOf(operationMetadata);
	}

	@Override
	public void deleteOperation(OperationKey operationKey, boolean deleteReports) throws IOException {

		final StudyKey studyKey = new StudyKey(operationKey.getStudyId());

		try {
			OperationMetadata op = getOperationMetadata(operationKey);

			// delete child operations
			List<OperationMetadata> childOperations = getChildrenOperationsMetadata(operationKey);
			if (!childOperations.isEmpty()) {
				childOperations.add(op);
				for (final OperationMetadata childOperation : childOperations) {
					org.gwaspi.global.Utils.tryToDeleteFile(OperationMetadata.generatePathToNetCdfFile(childOperation));
					final OperationKey childOperationKey = OperationKey.valueOf(childOperation);
					if (deleteReports) {
						getReportService().deleteReports(new DataSetKey(childOperationKey));
					}
					EntityManager em = null;
					try {
						em = jpaUtil.open();
						jpaUtil.begin(em);
						OperationMetadata operation = em.find(OperationMetadata.class, childOperationKey);
						if (operation == null) {
							throw new IllegalArgumentException("No operation found with this key: " + operationKey.toRawIdString());
						}
						em.remove(operation);
						jpaUtil.commit(em);
					} catch (Exception ex) {
						jpaUtil.rollback(em);
						throw new IOException("Failed deleting child operation by"
								+ ": operation-key: " + childOperationKey.toRawIdString(),
								ex);
					} finally {
						jpaUtil.close(em);
					}
				}
			} else {
				org.gwaspi.global.Utils.tryToDeleteFile(OperationMetadata.generatePathToNetCdfFile(op));
				if (deleteReports) {
					getReportService().deleteReports(new DataSetKey(operationKey));
				}

				EntityManager em = null;
				try {
					em = jpaUtil.open();
					jpaUtil.begin(em);
					OperationMetadata operation = em.find(OperationMetadata.class, operationKey);
					if (operation == null) {
						throw new IllegalArgumentException("No operation found with this key: " + operationKey.toRawIdString());
					}
					em.remove(operation);
					jpaUtil.commit(em);
				} catch (Exception ex) {
					jpaUtil.rollback(em);
					throw new IOException("Failed deleting operation by"
							+ ": operation-key: " + operationKey.toRawIdString(),
							ex);
				} finally {
					jpaUtil.close(em);
				}
			}
		} catch (Exception ex) {
			// PURGE INEXISTING OPERATIONS FROM DB
			EntityManager em = null;
			try {
				em = jpaUtil.open();
				jpaUtil.begin(em);
				OperationMetadata operation = em.find(OperationMetadata.class, operationKey);
				if (operation == null) {
					throw new IllegalArgumentException("No operation found with this key: " + operationKey.toRawIdString());
				}
				em.remove(operation);
				jpaUtil.commit(em);
			} catch (Exception exi) {
				jpaUtil.rollback(em);
				throw new IOException("Failed deleting operation by"
						+ ": study-id: " + studyKey.getId()
						+ ": operation-key: " + operationKey.toRawIdString(),
						exi);
			} finally {
				jpaUtil.close(em);
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
			em = jpaUtil.open();
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
			jpaUtil.close(em);
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
