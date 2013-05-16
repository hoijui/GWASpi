package org.gwaspi.model;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public interface Matrix {

	int getId();

	int getStudyId();

	MatrixMetadata getMatrixMetadata();
}
