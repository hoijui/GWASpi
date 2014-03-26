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

package org.gwaspi.operations.hardyweinberg;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import org.gwaspi.constants.cNetCDF.Defaults.OPType;
import org.gwaspi.global.Text;
import org.gwaspi.model.Census;
import org.gwaspi.model.DataSetKey;
import org.gwaspi.model.OperationKey;
import org.gwaspi.operations.AbstractOperation;
import org.gwaspi.operations.DefaultOperationTypeInfo;
import org.gwaspi.operations.OperationManager;
import org.gwaspi.operations.OperationTypeInfo;
import org.gwaspi.operations.AbstractDefaultTypesOperationFactory;
import org.gwaspi.operations.AbstractNetCdfOperationDataSet;
import org.gwaspi.operations.OperationDataSet;
import org.gwaspi.operations.hardyweinberg.HardyWeinbergOperationEntry.Category;
import org.gwaspi.operations.markercensus.MarkerCensusOperationDataSet;
import org.gwaspi.operations.markercensus.MarkerCensusOperationEntry;
import org.gwaspi.statistics.StatisticsUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HardyWeinbergOperation extends AbstractOperation<HardyWeinbergOperationDataSet, HardyWeinbergOperationParams> {

	private final Logger log = LoggerFactory.getLogger(HardyWeinbergOperation.class);

	private static final OperationTypeInfo OPERATION_TYPE_INFO
			= new DefaultOperationTypeInfo(
					false,
					Text.Operation.hardyWeiberg,
					Text.Operation.hardyWeiberg, // TODO We need a more elaborate description of this operation!
					OPType.HARDY_WEINBERG);
	public static void register() {
		// NOTE When converting to OSGi, this would be done in bundle init,
		//   or by annotations.
		OperationManager.registerOperationFactory(new AbstractDefaultTypesOperationFactory(
				HardyWeinbergOperation.class, OPERATION_TYPE_INFO) {
					@Override
					protected OperationDataSet generateReadOperationDataSetNetCdf(OperationKey operationKey, DataSetKey parent, Map<String, Object> properties) throws IOException {
						return new NetCdfHardyWeinbergOperationDataSet(parent.getOrigin(), parent, operationKey);
					}
				});
	}

	public HardyWeinbergOperation(HardyWeinbergOperationParams params) {
		super(params);
	}

	@Override
	public boolean isValid() {
		return true;
	}

	@Override
	public String getProblemDescription() {
		return null;
	}

	@Override
	public int processMatrix() throws IOException {

		int resultOpId;

		final OperationKey markerCensusOPKey = getParams().getParent().getOperationParent();
		final MarkerCensusOperationDataSet markerCensusOperationDataSet
				= (MarkerCensusOperationDataSet) OperationManager.generateOperationDataSet(markerCensusOPKey);

		final HardyWeinbergOperationDataSet dataSet = generateFreshOperationDataSet();
		dataSet.setNumMarkers(markerCensusOperationDataSet.getNumMarkers());
		dataSet.setNumChromosomes(markerCensusOperationDataSet.getNumChromosomes());
		dataSet.setNumSamples(markerCensusOperationDataSet.getNumSamples());

		dataSet.setHardyWeinbergName(getParams().getName());

		final Collection<MarkerCensusOperationEntry> markersCensus
				= markerCensusOperationDataSet.getEntries();

//		log.info("Perform Hardy-Weinberg test (All)");
//		performHardyWeinberg(wrNcFile, markersCensus, Category.ALL);
//
//		log.info("Perform Hardy-Weinberg test (Case)");
//		performHardyWeinberg(wrNcFile, markersCensus, Category.CASE);

		log.info("Perform Hardy-Weinberg test (Control)");
		performHardyWeinberg(dataSet, markersCensus, Category.CONTROL);

		log.info("Perform Hardy-Weinberg test (Alternate Hardy&Weinberg)");
		performHardyWeinberg(dataSet, markersCensus, Category.ALTERNATE);

		dataSet.finnishWriting();
		resultOpId = ((AbstractNetCdfOperationDataSet) dataSet).getOperationKey().getId(); // HACK

		org.gwaspi.global.Utils.sysoutCompleted("Hardy-Weinberg Equilibrium Test");

		return resultOpId;
	}

	/**
	 * Performs the Hardy & Weinberg test.
	 * {@see https://en.wikipedia.org/wiki/Hardy%E2%80%93Weinberg_principle}
	 * @param dataSet where to store the results
	 * @param markersContingencyMap where to read input-data from
	 * @param category process only this category
	 * @throws IOException
	 */
	private static void performHardyWeinberg(HardyWeinbergOperationDataSet dataSet, Collection<MarkerCensusOperationEntry> markersContingencyMap, Category category) throws IOException {

		for (MarkerCensusOperationEntry entry : markersContingencyMap) {
			final Census census = entry.getCensus().getCategoryCensus().get(category);
			final int obsAA = census.getAA();
			final int obsAa = census.getAa();
			final int obsaa = census.getaa();
			final int numSamples = obsAA + obsaa + obsAa;
			final double obsHzy = (double) obsAa / numSamples;

			final double fA = StatisticsUtils.calculatePunnettFrequency(obsAA, obsAa, numSamples);
			final double fa = StatisticsUtils.calculatePunnettFrequency(obsaa, obsAa, numSamples);

			final double pAA = fA * fA;
			final double pAa = 2 * fA * fa;
			final double paa = fa * fa;

			final double expAA = pAA * numSamples;
			final double expAa = pAa * numSamples;
			final double expaa = paa * numSamples;
			final double expHzy = pAa;

			final double chiSQ = org.gwaspi.statistics.Chisquare.calculateHWChiSquare(obsAA, expAA, obsAa, expAa, obsaa, expaa);
			final double pvalue = org.gwaspi.statistics.Pvalue.calculatePvalueFromChiSqr(chiSQ, 1);

			final HardyWeinbergOperationEntry hwEntry = new DefaultHardyWeinbergOperationEntry(entry.getKey(), entry.getIndex(), category, pvalue, obsHzy, expHzy);
			dataSet.addEntry(hwEntry);
		}
	}
}
