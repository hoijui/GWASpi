/*
 * Copyright (C) 2014 Universitat Pompeu Fabra
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

package org.gwaspi.operations;

import java.util.Collections;
import java.util.Set;
import org.gwaspi.constants.NetCDFConstants.Defaults.OPType;
import org.gwaspi.model.DataSetKey;
import org.gwaspi.model.MatrixKey;

public abstract class AbstractOperationParams implements OperationParams {

	private final OPType type;
	private DataSetKey parent;
	private String name;

	protected AbstractOperationParams(final OPType type, final DataSetKey parent, final String name) {

		this.type = type;
		this.parent = parent;
		this.name = (name == null) ? getNameDefault() : name;
	}

	@Override
	public OPType getType() {
		return type;
	}

	@Override
	public DataSetKey getParent() {
		return parent;
	}

	public void setParent(DataSetKey parent) {
		this.parent = parent;
	}

	@Override
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public Set<MatrixKey> getParticipatingMatrices() {
		return Collections.singleton(getParent().getOrigin());
	}

	@Override
	public boolean isHidden() {
		return false;
	}

	protected abstract String getNameDefault();
}
