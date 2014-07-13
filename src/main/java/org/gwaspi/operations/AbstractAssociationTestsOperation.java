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

package org.gwaspi.operations;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.gwaspi.model.Census;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.operations.genotypicassociationtest.AssociationTestOperationParams;
import org.gwaspi.operations.trendtest.CommonTestOperationDataSet;
import org.gwaspi.progress.ProcessStatus;
import org.gwaspi.progress.ProgressHandler;

public abstract class AbstractAssociationTestsOperation<DST extends CommonTestOperationDataSet> extends AbstractTestMatrixOperation<DST, AssociationTestOperationParams> {

	public AbstractAssociationTestsOperation(final AssociationTestOperationParams params) {
		super(params);
	}

	protected abstract void associationTest(
			final DST dataSet,
			final Integer markerOrigIndex,
			final MarkerKey markerKey,
			final int caseAA,
			final int caseAa,
			final int caseaa,
			final int ctrlAA,
			final int ctrlAa,
			final int ctrlaa)
			throws IOException;

	/**
	 * Performs the Allelic or Genotypic Association Tests.
	 * @param dataSet
	 * @param markerOrigIndicesKeys
	 * @param caseMarkersCensus
	 * @param ctrlMarkersCensus
	 * @param rawTestPH
	 * @throws IOException
	 */
	@Override
	protected void performTest(
			OperationDataSet dataSet,
			Map<Integer, MarkerKey> markerOrigIndicesKeys,
			List<Census> caseMarkersCensus,
			List<Census> ctrlMarkersCensus,
			ProgressHandler rawTestPH)
			throws IOException
	{
		rawTestPH.setNewStatus(ProcessStatus.INITIALIZING);
		Iterator<Census> caseMarkerCensusIt = caseMarkersCensus.iterator();
		Iterator<Census> ctrlMarkersCensusIt = ctrlMarkersCensus.iterator();
		int localMarkerIndex = 0;
		rawTestPH.setNewStatus(ProcessStatus.RUNNING);
		for (Map.Entry<Integer, MarkerKey> caseMarkerOrigIndexKey : markerOrigIndicesKeys.entrySet()) {
			final Integer origIndex = caseMarkerOrigIndexKey.getKey();
			final MarkerKey markerKey = caseMarkerOrigIndexKey.getValue();
			final Census caseCensus = caseMarkerCensusIt.next();
			final Census ctrlCensus = ctrlMarkersCensusIt.next();

			// INIT VALUES
			final int caseAA = caseCensus.getAA();
			final int caseAa = caseCensus.getAa();
			final int caseaa = caseCensus.getaa();

			final int ctrlAA = ctrlCensus.getAA();
			final int ctrlAa = ctrlCensus.getAa();
			final int ctrlaa = ctrlCensus.getaa();

			// XXX Genotypic is about 10 times faster then allelic, and the only difference between the two is the code here, so... find out why!!!
			associationTest((DST) dataSet, origIndex, markerKey, caseAA, caseAa, caseaa, ctrlAA, ctrlAa, ctrlaa);
			rawTestPH.setProgress(localMarkerIndex);
			localMarkerIndex++;
		}
		rawTestPH.setNewStatus(ProcessStatus.COMPLEETED);
	}
}
