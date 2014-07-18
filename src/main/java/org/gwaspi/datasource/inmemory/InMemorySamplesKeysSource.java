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
import java.util.List;
import org.gwaspi.model.KeyFactory;
import org.gwaspi.model.MatrixKey;
import org.gwaspi.model.SampleKey;
import org.gwaspi.model.SampleKeyFactory;
import org.gwaspi.model.SamplesKeysSource;
import org.gwaspi.model.StudyKey;

public class InMemorySamplesKeysSource extends AbstractInMemoryKeysSource<SampleKey> implements SamplesKeysSource {

	private final StudyKey studyKey;
	private SamplesKeysSource originSource;

	private InMemorySamplesKeysSource(MatrixKey origin, StudyKey studyKey, List<SampleKey> items, List<Integer> originalIndices) {
		super(origin, items, originalIndices);

		this.studyKey = studyKey;
		this.originSource = null;
	}

	public static SamplesKeysSource createForMatrix(MatrixKey origin, StudyKey studyKey, List<SampleKey> items) throws IOException {
		return new InMemorySamplesKeysSource(origin, studyKey, items, null);
	}

	public static SamplesKeysSource createForOperation(MatrixKey origin, StudyKey studyKey, List<SampleKey> items, List<Integer> originalIndices) throws IOException {
		return new InMemorySamplesKeysSource(origin, studyKey, items, originalIndices);
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
