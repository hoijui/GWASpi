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
import java.util.Collection;
import java.util.Map;
import org.gwaspi.constants.cNetCDF.Defaults.OPType;
import org.gwaspi.global.Text;
import org.gwaspi.model.Census;
import org.gwaspi.model.DataSetKey;
import org.gwaspi.model.OperationKey;
import org.gwaspi.operations.AbstractDefaultTypesOperationFactory;
import org.gwaspi.operations.AbstractNetCdfOperationDataSet;
import org.gwaspi.operations.OperationDataSet;
import org.gwaspi.operations.hardyweinberg.DefaultHardyWeinbergOperationEntry;
import org.gwaspi.operations.hardyweinberg.HardyWeinbergOperationDataSet;
import org.gwaspi.operations.hardyweinberg.HardyWeinbergOperationEntry;
import org.gwaspi.operations.hardyweinberg.HardyWeinbergOperationEntry.Category;
import org.gwaspi.operations.hardyweinberg.HardyWeinbergOperationParams;
import org.gwaspi.operations.hardyweinberg.NetCdfHardyWeinbergOperationDataSet;
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
		MarkerCensusOperationDataSet markerCensusOperationDataSet = (MarkerCensusOperationDataSet) OperationManager.generateOperationDataSet(markerCensusOPKey);

		HardyWeinbergOperationDataSet dataSet = generateFreshOperationDataSet();
		dataSet.setNumMarkers(markerCensusOperationDataSet.getNumMarkers());
		dataSet.setNumChromosomes(markerCensusOperationDataSet.getNumChromosomes());
		dataSet.setNumSamples(markerCensusOperationDataSet.getNumSamples());

		dataSet.setHardyWeinbergName(getParams().getName());

		//<editor-fold defaultstate="expanded" desc="GET CENSUS & PERFORM HW">
		Collection<MarkerCensusOperationEntry> markersCensus = markerCensusOperationDataSet.getEntries();

//		// PROCESS ALL SAMPLES
//		performHardyWeinberg(wrNcFile, markersCensus, "ALL");
//
//		// PROCESS CASE SAMPLES
//		performHardyWeinberg(wrNcFile, markersCensus, "CASE");

		// PROCESS CONTROL SAMPLES
		log.info("Perform Hardy-Weinberg test (Control)");
		performHardyWeinberg(dataSet, markersCensus, Category.CONTROL);

		// PROCESS ALTERNATE HW SAMPLES
		log.info("Perform Hardy-Weinberg test (HW-ALT)");
		performHardyWeinberg(dataSet, markersCensus, Category.ALTERNATE);
		//</editor-fold>

		dataSet.finnishWriting();
		resultOpId = ((AbstractNetCdfOperationDataSet) dataSet).getOperationKey().getId(); // HACK

		org.gwaspi.global.Utils.sysoutCompleted("Hardy-Weinberg Equilibrium Test");

		return resultOpId;
	}

	private void performHardyWeinberg(HardyWeinbergOperationDataSet dataSet, Collection<MarkerCensusOperationEntry> markersContingencyMap, Category category) throws IOException {

		for (MarkerCensusOperationEntry entry : markersContingencyMap) {
			// HARDY-WEINBERG
			Census census = entry.getCensus().getCategoryCensus().get(category);
			int obsAA = census.getAA();
			int obsAa = census.getAa();
			int obsaa = census.getaa();
			int sampleNb = obsAA + obsaa + obsAa;
			double obsHzy = (double) obsAa / sampleNb;

			double fA = StatisticsUtils.calculatePunnettFrequency(obsAA, obsAa, sampleNb);
			double fa = StatisticsUtils.calculatePunnettFrequency(obsaa, obsAa, sampleNb);

			double pAA = fA * fA;
			double pAa = 2 * fA * fa;
			double paa = fa * fa;

			double expAA = pAA * sampleNb;
			double expAa = pAa * sampleNb;
			double expaa = paa * sampleNb;
			double expHzy = pAa;

			double chiSQ = org.gwaspi.statistics.Chisquare.calculateHWChiSquare(obsAA, expAA, obsAa, expAa, obsaa, expaa);
			double pvalue = org.gwaspi.statistics.Pvalue.calculatePvalueFromChiSqr(chiSQ, 1);

			HardyWeinbergOperationEntry hwEntry = new DefaultHardyWeinbergOperationEntry(entry.getKey(), entry.getIndex(), category, pvalue, obsHzy, expHzy);
			dataSet.addEntry(hwEntry);
		}
	}
}
