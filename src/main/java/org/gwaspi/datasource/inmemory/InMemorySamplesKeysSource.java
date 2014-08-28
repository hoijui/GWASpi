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
import org.gwaspi.model.KeyFactory;
import org.gwaspi.model.MatrixKey;
import org.gwaspi.model.OperationKey;
import org.gwaspi.model.SampleKey;
import org.gwaspi.model.SampleKeyFactory;
import org.gwaspi.model.SamplesKeysSource;
import org.gwaspi.model.StudyKey;

public class InMemorySamplesKeysSource extends AbstractInMemoryKeysSource<SampleKey> implements SamplesKeysSource {

	private static final Map<MatrixKey, SamplesKeysSource> KEY_TO_DATA
			= new HashMap<MatrixKey, SamplesKeysSource>();
	private static final Map<OperationKey, SamplesKeysSource> KEY2_TO_DATA
			= new HashMap<OperationKey, SamplesKeysSource>();

	private final StudyKey studyKey;
	private SamplesKeysSource originSource;

	private InMemorySamplesKeysSource(MatrixKey key, StudyKey studyKey, List<SampleKey> items, List<Integer> originalIndices) {
		super(key, items, originalIndices);

		this.studyKey = studyKey;
		this.originSource = null;
	}

	public static SamplesKeysSource createForMatrix(MatrixKey key, StudyKey studyKey, List<SampleKey> items) throws IOException {
//		return createForOperation(key, studyKey, items, null);

		SamplesKeysSource data = KEY_TO_DATA.get(key);
		if (data == null) {
			if (items == null) {
				throw new IllegalStateException("Tried to fetch data that is not available, or tried to create a data-set without giving data");
			}
			data = new InMemorySamplesKeysSource(key, studyKey, items, null);
			KEY_TO_DATA.put(key, data);
		} else if (items != null) {
			throw new IllegalStateException("Tried to store data under a key that is already present. key: " + key.toRawIdString());
		}

		return data;
	}

	public static SamplesKeysSource createForOperation(OperationKey key, StudyKey studyKey, List<SampleKey> items, List<Integer> originalIndices) throws IOException {

		SamplesKeysSource data = KEY2_TO_DATA.get(key);
		if (data == null) {
			if (items == null) {
				throw new IllegalStateException("Tried to fetch data that is not available, or tried to create a data-set without giving data");
			}
			data = new InMemorySamplesKeysSource(key.getParentMatrixKey(), studyKey, items, originalIndices);
			KEY2_TO_DATA.put(key, data);
		} else if (items != null) {
			throw new IllegalStateException("Tried to store data under a key that is already present. key: " + key.toRawIdString());
		}

		return data;
	}

	public static void clearStorage() {

		KEY_TO_DATA.clear();
		KEY2_TO_DATA.clear();
	}

	@Override
	public SamplesKeysSource getOrigSource() throws IOException {

		if (originSource == null) {
			if (getOrigin() == null) {
				originSource = this;
			} else {
				originSource = getOrigDataSetSource().getSamplesKeysSource();
			}
		}

		return originSource;
	}

	@Override
	protected KeyFactory<SampleKey> createKeyFactory() {
		return new SampleKeyFactory(studyKey);
	}
}
