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

package org.gwaspi.datasource.inmemory;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.gwaspi.model.DataSetSource;
import org.gwaspi.model.MarkerMetadata;
import org.gwaspi.model.MarkersKeysSource;
import org.gwaspi.model.MarkersMetadataSource;
import org.gwaspi.model.MatrixKey;

public class InMemoryMarkersMetadataSource extends AbstractInMemoryListSource<MarkerMetadata> implements MarkersMetadataSource {

	private static final Map<MatrixKey, MarkersMetadataSource> KEY_TO_DATA
			= new HashMap<MatrixKey, MarkersMetadataSource>();

	private MarkersMetadataSource originSource;
	private final DataSetSource dataSetSource;

	private InMemoryMarkersMetadataSource(final DataSetSource dataSetSource, MatrixKey key, final List<MarkerMetadata> items, List<Integer> originalIndices) {
		super(key, items, originalIndices);

		this.dataSetSource = dataSetSource;
	}

	public static MarkersMetadataSource createForMatrix(final DataSetSource dataSetSource, MatrixKey key, final List<MarkerMetadata> items) throws IOException {
		return createForOperation(dataSetSource, key, items, null);
	}

	private static MarkersMetadataSource createForOperation(final DataSetSource dataSetSource, MatrixKey key, final List<MarkerMetadata> items, List<Integer> originalIndices) throws IOException {

		MarkersMetadataSource data = KEY_TO_DATA.get(key);
		if (data == null) {
			if (items == null) {
				throw new IllegalStateException("Tried to fetch data that is not available, or tried to create a data-set without giving data");
			}
			data = new InMemoryMarkersMetadataSource(dataSetSource, key, items, originalIndices);
			KEY_TO_DATA.put(key, data);
		} else if (items != null) {
			throw new IllegalStateException("Tried to store data under a key that is already present. key: " + key.toRawIdString());
		}

		return data;
	}

	@Override
	public MarkerMetadata get(int index) {
		return super.get(index);
	}

	// XXX same code as in the NetCDF counterpart!
	@Override
	public MarkersMetadataSource getOrigSource() throws IOException {

		if (originSource == null) {
			if (getOrigin() == null) {
				originSource = this;
			} else {
				originSource = getOrigDataSetSource().getMarkersMetadatasSource();
			}
		}

		return originSource;
	}

	private DataSetSource getDataSetSource() {
		return dataSetSource;
	}

	@Override
	public MarkersKeysSource getKeysSource() throws IOException {
		return getDataSetSource().getMarkersKeysSource();
	}

	@Override
	public List<String> getMarkerIds() throws IOException {
		return extractProperty(getItems(), MarkerMetadata.TO_MARKER_ID);
	}

	@Override
	public List<String> getRsIds() throws IOException {
		return extractProperty(getItems(), MarkerMetadata.TO_RS_ID);
	}

	@Override
	public List<String> getChromosomes() throws IOException {
		return extractProperty(getItems(), MarkerMetadata.TO_CHR);
	}

	@Override
	public List<Integer> getPositions() throws IOException {
		return extractProperty(getItems(), MarkerMetadata.TO_POS);
	}

	@Override
	public List<String> getAlleles() throws IOException {
		return extractProperty(getItems(), MarkerMetadata.TO_ALLELES);
	}

	@Override
	public List<String> getStrands() throws IOException {
		return extractProperty(getItems(), MarkerMetadata.TO_STRAND);
	}
}
