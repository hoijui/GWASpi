package org.gwaspi.model;

import java.io.Serializable;
import org.gwaspi.constants.cNetCDF.Defaults.OPType;

public class MatrixOperationSpec implements Serializable {

	private final Integer id;
	private final OPType type;

	protected MatrixOperationSpec() {
		this(null, null);
	}

	public MatrixOperationSpec(Integer id, OPType type) {

		this.id = id;
		this.type = type;
	}

	public Integer getId() {
		return id;
	}

	public OPType getType() {
		return type;
	}
}
