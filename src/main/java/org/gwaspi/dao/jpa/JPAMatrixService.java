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
import java.util.Date;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import org.gwaspi.constants.cImport.ImportFormat;
import org.gwaspi.constants.cNetCDF;
import org.gwaspi.constants.cNetCDF.Defaults.GenotypeEncoding;
import org.gwaspi.constants.cNetCDF.Defaults.StrandType;
import org.gwaspi.dao.MatrixService;
import org.gwaspi.global.Config;
import org.gwaspi.model.MatrixKey;
import org.gwaspi.model.MatrixMetadata;
import org.gwaspi.model.OperationMetadata;
import org.gwaspi.model.OperationsList;
import org.gwaspi.model.ReportsList;
import org.gwaspi.netCDF.matrices.MatrixFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.ma2.ArrayChar;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

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

	@Override
	public List<MatrixKey> getMatrixList() throws IOException {

		List<MatrixKey> matrices = Collections.EMPTY_LIST;

		EntityManager em = null;
		try {
			em = open();
			List<Object[]> matricesKeyParts = em.createNamedQuery("matrixMetadata_listKeys").getResultList();
			matrices = new ArrayList<MatrixKey>(matricesKeyParts.size());
			for (Object[] matrixKeyParts : matricesKeyParts) {
				matrices.add(new MatrixKey(
						(Integer) matrixKeyParts[0],
						(Integer) matrixKeyParts[1]));
			}
		} catch (Exception ex) {
			LOG.error("Failed fetching all matrices", ex);
		} finally {
			close(em);
		}

		return matrices;
	}

	@Override
	public List<MatrixKey> getMatrixList(int studyId) throws IOException {

		List<MatrixKey> matrices = Collections.EMPTY_LIST;

		EntityManager em = null;
		try {
			em = open();
			Query query = em.createNamedQuery("matrixMetadata_listIdsByStudyId");
			query.setParameter("studyId", studyId);
			List<Integer> matricesIds = query.getResultList();
			matrices = new ArrayList<MatrixKey>(matricesIds.size());
			for (Integer matrixId : matricesIds) {
				matrices.add(new MatrixKey(studyId, matrixId));
			}
		} catch (Exception ex) {
			LOG.error("Failed fetching all matrices", ex);
		} finally {
			close(em);
		}

		return matrices;
	}

	@Override
	public List<MatrixMetadata> getMatricesTable(int studyId) throws IOException {

		List<MatrixMetadata> matricesMetadata = Collections.EMPTY_LIST;

		EntityManager em = null;
		try {
			em = open();
			Query query = em.createNamedQuery("matrixMetadata_listByStudyId");
			query.setParameter("studyId", studyId);
			matricesMetadata = query.getResultList();
			for (int i = 0; i < matricesMetadata.size(); i++) {
				matricesMetadata.set(i, completeMatricesTable(matricesMetadata.get(i)));
			}
		} catch (Exception ex) {
			LOG.error("Failed fetching all matrices-metadata", ex);
		} finally {
			close(em);
		}

		return matricesMetadata;
	}

	@Override
	public void insertMatrixMetadata(MatrixMetadata matrixMetadata) throws IOException {

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
		} catch (Exception ex) {
			LOG.error("Failed adding a matrix-metadata", ex);
			rollback(em);
		} finally {
			close(em);
		}
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

		String genotypesFolder = Config.getConfigValue(Config.PROPERTY_GENOTYPES_DIR, "");
		genotypesFolder += "/STUDY_" + matrixMetadata.getStudyId() + "/";

		// DELETE OPERATION netCDFs FROM THIS MATRIX
		List<OperationMetadata> operations = OperationsList.getOperationsList(matrixKey.getMatrixId());
		for (OperationMetadata op : operations) {
			File opFile = new File(genotypesFolder + op.getMatrixCDFName()+ ".nc");
			org.gwaspi.global.Utils.tryToDeleteFile(opFile);
		}

		ReportsList.deleteReportByMatrixId(matrixKey.getMatrixId());

		// DELETE MATRIX NETCDF FILE
		File matrixFile = new File(genotypesFolder + matrixMetadata.getMatrixNetCDFName() + ".nc");
		org.gwaspi.global.Utils.tryToDeleteFile(matrixFile);
	}

	@Override
	public void saveMatrixDescription(int matrixId, String description) throws IOException {

		MatrixMetadata matrixMetadata = getMatrixMetadataById(matrixId);
		matrixMetadata.setDescription(description);

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
	public MatrixMetadata getLatestMatrixId() throws IOException {

		MatrixMetadata matrixMetadata = null;

		EntityManager em = null;
		try {
			em = open();
			Query query = em.createNamedQuery("matrixMetadata_listIds");
			List<Integer> matrixIds = query.getResultList();

			query = em.createNamedQuery("matrixMetadata_fetchById");
			query.setParameter("id", matrixIds.get(matrixIds.size() - 1));
			matrixMetadata = (MatrixMetadata) query.getSingleResult();
			matrixMetadata = completeMatricesTable(matrixMetadata);
		} catch (Exception ex) {
			LOG.error("Failed fetching latest-matrix-metadata", ex);
		} finally {
			close(em);
		}

		return matrixMetadata;
	}

	@Override
	public MatrixMetadata getMatrixMetadataById(int matrixId) throws IOException {

		MatrixMetadata matrixMetadata = null;

		EntityManager em = null;
		try {
			em = open();
			Query query = em.createNamedQuery("matrixMetadata_fetchById");
			query.setParameter("id", matrixId);
			matrixMetadata = (MatrixMetadata) query.getSingleResult();
			matrixMetadata = completeMatricesTable(matrixMetadata);
		} catch (NoResultException ex) {
			LOG.error("Failed fetching matrix-metadata by id: " + matrixId
					+ " (id not found)", ex);
			LOG.info("Available matrices:");
			List<MatrixKey> matrixList = getMatrixList();
			StringBuilder matrices = new StringBuilder();
			for (MatrixKey mat : matrixList) {
				matrices.append(" {study-id: ");
				matrices.append(mat.getStudyId()).append(", matrix-id: ");
				matrices.append(mat.getMatrixId()).append("}");
			}
			LOG.info(matrices.toString());
		} catch (Exception ex) {
			LOG.error("Failed fetching matrix-metadata by id: " + matrixId, ex);
		} finally {
			close(em);
		}

		return matrixMetadata;
	}

	@Override
	public MatrixMetadata getMatrixMetadataByNetCDFname(String netCDFName) throws IOException {

		MatrixMetadata matrixMetadata = null;

		EntityManager em = null;
		try {
			em = open();
			Query query = em.createNamedQuery("matrixMetadata_fetchByNetCDFName");
			query.setParameter("netCDFName", netCDFName);
			matrixMetadata = (MatrixMetadata) query.getSingleResult();
			matrixMetadata = completeMatricesTable(matrixMetadata);
		} catch (NoResultException ex) {
			LOG.error("Failed fetching matrix-metadata by netCDFname: " + netCDFName
					+ " (id not found)", ex);
		} catch (Exception ex) {
			LOG.error("Failed fetching matrix-metadata by netCDFname: " + netCDFName, ex);
		} finally {
			close(em);
		}

		return matrixMetadata;
	}

	@Override
	public MatrixMetadata getMatrixMetadata(String netCDFpath, int studyId, String newMatrixName) throws IOException {

		int matrixId = Integer.MIN_VALUE;
		String matrixFriendlyName = newMatrixName;
		String matrixNetCDFName = MatrixFactory.generateMatrixNetCDFNameByDate();
		String description = "";
		String matrixType = "";
		Date creationDate = null;

		String pathToMatrix = netCDFpath;
		return loadMatrixMetadataFromFile(matrixId, matrixFriendlyName, matrixNetCDFName, studyId, pathToMatrix, description, matrixType, creationDate);
	}

	private MatrixMetadata completeMatricesTable(MatrixMetadata toCompleteMatrixMetadata) throws IOException {
		String genotypesFolder = Config.getConfigValue(Config.PROPERTY_GENOTYPES_DIR, "");
		String pathToStudy = genotypesFolder + "/STUDY_" + toCompleteMatrixMetadata.getStudyId() + "/";
		String pathToMatrix = pathToStudy + toCompleteMatrixMetadata.getMatrixNetCDFName() + ".nc";
		return loadMatrixMetadataFromFile(
				toCompleteMatrixMetadata.getMatrixId(),
				toCompleteMatrixMetadata.getMatrixFriendlyName(),
				toCompleteMatrixMetadata.getMatrixNetCDFName(),
				toCompleteMatrixMetadata.getStudyId(),
				pathToMatrix,
				toCompleteMatrixMetadata.getDescription(),
				toCompleteMatrixMetadata.getMatrixType(),
				toCompleteMatrixMetadata.getCreationDate());
	}

	public static MatrixMetadata loadMatrixMetadataFromFile(int matrixId, String matrixFriendlyName, String matrixNetCDFName, int studyId, String pathToMatrix, String description, String matrixType, Date creationDate) throws IOException {

		String gwaspiDBVersion = "";
		ImportFormat technology = ImportFormat.UNKNOWN;
		GenotypeEncoding gtEncoding = GenotypeEncoding.UNKNOWN;
		StrandType strand = StrandType.UNKNOWN;
		boolean hasDictionray = false;
		int markerSetSize = Integer.MIN_VALUE;
		int sampleSetSize = Integer.MIN_VALUE;

//		String genotypesFolder = Config.getConfigValue(Config.PROPERTY_GENOTYPES_DIR, "");
//		String pathToStudy = genotypesFolder + "/STUDY_" + studyId + "/";
//		pathToMatrix = pathToStudy + matrixNetCDFName + ".nc";
		NetcdfFile ncfile = null;
		if (new File(pathToMatrix).exists()) {
			try {
				ncfile = NetcdfFile.open(pathToMatrix);

				technology = ImportFormat.compareTo(ncfile.findGlobalAttribute(cNetCDF.Attributes.GLOB_TECHNOLOGY).getStringValue());
				try {
					gwaspiDBVersion = ncfile.findGlobalAttribute(cNetCDF.Attributes.GLOB_GWASPIDB_VERSION).getStringValue();
				} catch (Exception ex) {
					LOG.error(null, ex);
				}

				Variable var = ncfile.findVariable(cNetCDF.Variables.GLOB_GTENCODING);
				if (var != null) {
					try {
						ArrayChar.D2 gtCodeAC = (ArrayChar.D2) var.read("(0:0:1, 0:7:1)");
//						gtEncoding = GenotypeEncoding.valueOf(gtCodeAC.getString(0));
						gtEncoding = GenotypeEncoding.compareTo(gtCodeAC.getString(0)); // HACK, the above was used before
					} catch (InvalidRangeException ex) {
						LOG.error(null, ex);
					}
				}

				strand = StrandType.valueOf(ncfile.findGlobalAttribute(cNetCDF.Attributes.GLOB_STRAND).getStringValue());
				hasDictionray = ((Integer) ncfile.findGlobalAttribute(cNetCDF.Attributes.GLOB_HAS_DICTIONARY).getNumericValue() != 0);

				Dimension markerSetDim = ncfile.findDimension(cNetCDF.Dimensions.DIM_MARKERSET);
				markerSetSize = markerSetDim.getLength();

				Dimension sampleSetDim = ncfile.findDimension(cNetCDF.Dimensions.DIM_SAMPLESET);
				sampleSetSize = sampleSetDim.getLength();
			} catch (IOException ex) {
				LOG.error("Cannot open file: " + ncfile, ex);
			} finally {
				if (null != ncfile) {
					try {
						ncfile.close();
					} catch (IOException ex) {
						LOG.warn("Cannot close file: " + ncfile, ex);
					}
				}
			}
		}

		MatrixMetadata matrixMetadata = new MatrixMetadata(
			matrixId,
			matrixFriendlyName,
			matrixNetCDFName,
			pathToMatrix,
			technology,
			gwaspiDBVersion,
			description,
			gtEncoding,
			strand,
			hasDictionray,
			markerSetSize,
			sampleSetSize,
			studyId,
			matrixType,
			creationDate);

		return matrixMetadata;
	}
}
