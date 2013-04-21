package org.gwaspi.model;

import org.gwaspi.constants.cNetCDF.Defaults.OPType;

public class MatrixOperationSpec {

	private final Integer id;
	private final OPType type;

	public MatrixOperationSpec(Integer id, OPType type) {

		this.id = id;
		this.type = type;
	}

	public Integer getId() {
		return id;
	}

	public OPType geType() {
		return type;
	}
}
