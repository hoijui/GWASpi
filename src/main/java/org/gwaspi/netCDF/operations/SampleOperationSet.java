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

package org.gwaspi.netCDF.operations;

import java.io.IOException;
import org.gwaspi.model.OperationKey;
import org.gwaspi.model.SampleKey;
import org.gwaspi.model.SampleKeyFactory;

public class SampleOperationSet<V> extends AbstractOperationSet<SampleKey, V> {

	public SampleOperationSet(OperationKey operationKey) throws IOException {
		super(operationKey, new SampleKeyFactory(operationKey.getParentMatrixKey().getStudyKey()));
	}

	public SampleOperationSet(OperationKey operationKey, int from, int to) throws IOException {
		super(operationKey, new SampleKeyFactory(operationKey.getParentMatrixKey().getStudyKey()), from, to);
	}
}
