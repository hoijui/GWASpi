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

package org.gwaspi.dao;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.model.MarkerMetadata;
import org.gwaspi.model.MatrixKey;

public interface MarkerService {

	List<MarkerKey> getMatrixKeys(MatrixKey matrixKey) throws IOException;

	MarkerMetadata getMarker(MatrixKey matrixKey, MarkerKey key) throws IOException;

	List<MarkerMetadata> getMarkers(MatrixKey matrixKey) throws IOException;

	<T> Map<MarkerKey, Integer> pickMarkers(MatrixKey matrixKey, String variable, Collection<T> criteria, boolean include) throws IOException;

	<T> Collection<T> getMarkersVariable(MatrixKey matrixKey, String variable, boolean include) throws IOException;

	void deleteMarkers(MatrixKey matrixKey) throws IOException;

	void insertMarkers(Collection<MarkerMetadata> markerMetadatas) throws IOException;
}
