package org.gwaspi.model;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import org.gwaspi.constants.cNetCDF.Defaults.OPType;

@Entity
@Table(name = "matrixOperationSpec")
@NamedQueries({
	@NamedQuery(
		name = "matrixOperationSpecs_listById",
		query = "SELECT mos FROM MatrixOperationSpec mos WHERE mos.id = :id"),
})
public class MatrixOperationSpec implements Serializable {

	private Integer id;
	private final OPType type;

	protected MatrixOperationSpec() {
		this(null, null);
	}

	public MatrixOperationSpec(Integer id, OPType type) {

		this.id = id;
		this.type = type;
	}

	@Id
	@Column(
		name       = "id",
		unique     = false,
		nullable   = false,
		insertable = true,
		updatable  = false
		)
	public Integer getId() {
		return id;
	}

	protected void setId(Integer id) {
		this.id = id;
	}

	@Column(
		name       = "type",
		unique     = false,
		nullable   = false,
		insertable = true,
		updatable  = false
		)
	public OPType getType() {
		return type;
	}
}
