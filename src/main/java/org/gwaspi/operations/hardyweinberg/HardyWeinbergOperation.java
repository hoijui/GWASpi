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
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import org.gwaspi.global.Text;
import org.gwaspi.model.Census;
import org.gwaspi.model.OperationKey;
import org.gwaspi.operations.AbstractNetCdfOperationDataSet;
import org.gwaspi.operations.AbstractOperationCreatingOperation;
import org.gwaspi.operations.OperationManager;
import org.gwaspi.operations.OperationTypeInfo;
import org.gwaspi.operations.hardyweinberg.HardyWeinbergOperationEntry.Category;
import org.gwaspi.operations.markercensus.MarkerCensusOperationDataSet;
import org.gwaspi.operations.markercensus.MarkerCensusOperationEntry;
import org.gwaspi.progress.DefaultProcessInfo;
import org.gwaspi.progress.IntegerProgressHandler;
import org.gwaspi.progress.ProcessInfo;
import org.gwaspi.progress.ProcessStatus;
import org.gwaspi.progress.ProgressHandler;
import org.gwaspi.progress.ProgressSource;
import org.gwaspi.progress.SubProcessInfo;
import org.gwaspi.progress.SuperProgressSource;
import org.gwaspi.statistics.StatisticsUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HardyWeinbergOperation extends AbstractOperationCreatingOperation<HardyWeinbergOperationDataSet, HardyWeinbergOperationParams> {

	private final Logger log = LoggerFactory.getLogger(HardyWeinbergOperation.class);

	public static final ProcessInfo PROCESS_INFO = new DefaultProcessInfo(
			Text.Operation.hardyWeiberg,
			Text.Operation.hardyWeiberg); // TODO We need a more elaborate description of this operation!

	public static void register() {
		// NOTE When converting to OSGi, this would be done in bundle init,
		//   or by annotations.
		OperationManager.registerOperationFactory(new HardyWeinbergOperationFactory());
	}

//	private ProgressHandler hwAllPH;
//	private ProgressHandler hwCasePH;
	private ProgressHandler hwControlPH;
	private ProgressHandler hwAlternatePH;
	private SuperProgressSource progressSource;

	public HardyWeinbergOperation(HardyWeinbergOperationParams params) {
		super(params);

//		this.hwAllPH = null;
//		this.hwCasePH = null;
		this.hwControlPH = null;
		this.hwAlternatePH = null;
		this.progressSource = null;
	}

	@Override
	public OperationTypeInfo getTypeInfo() {
		return HardyWeinbergOperationFactory.OPERATION_TYPE_INFO;
	}

	private SuperProgressSource getSuperProgressHandler() throws IOException {

		if (progressSource == null) {
			final int numItems = getNumItems();
//			this.hwAllPH = new IntegerProgressHandler(
//					new SubProcessInfo(PROCESS_INFO, "H&W all", "Run the Hardy & Weinberg test over the 'all' category"),
//					0, numItems - 1);
//			this.hwCasePH = new IntegerProgressHandler(
//					new SubProcessInfo(PROCESS_INFO, "H&W case", "Run the Hardy & Weinberg test over the 'case' category"),
//					0, numItems - 1);
			this.hwControlPH = new IntegerProgressHandler(
					new SubProcessInfo(PROCESS_INFO, "H&W control", "Run the Hardy & Weinberg test over the 'control' category"),
					0, numItems - 1);
			this.hwAlternatePH = new IntegerProgressHandler(
					new SubProcessInfo(PROCESS_INFO, "H&W alternate", "Run the Hardy & Weinberg test over the 'alternate' category"),
					0, numItems - 1);

			final Map<ProgressSource, Double> subProgressSourcesAndWeights;
			final LinkedHashMap<ProgressSource, Double> tmpSubProgressSourcesAndWeights
					= new LinkedHashMap<ProgressSource, Double>(2);
	//		tmpSubProgressSourcesAndWeights.put(hwAllPH, 0.25);
	//		tmpSubProgressSourcesAndWeights.put(hwCasePH, 0.25);
			tmpSubProgressSourcesAndWeights.put(hwControlPH, 0.5);
			tmpSubProgressSourcesAndWeights.put(hwAlternatePH, 0.5);
			subProgressSourcesAndWeights = Collections.unmodifiableMap(tmpSubProgressSourcesAndWeights);

			progressSource = new SuperProgressSource(PROCESS_INFO, subProgressSourcesAndWeights);
		}

		return progressSource;
	}

	@Override
	public ProgressSource getProgressSource() throws IOException {
		return getSuperProgressHandler();
	}

	@Override
	public ProcessInfo getProcessInfo() {
		return PROCESS_INFO;
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

		progressSource.setNewStatus(ProcessStatus.INITIALIZING);

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

		progressSource.setNewStatus(ProcessStatus.RUNNING);

//		log.info("Perform Hardy-Weinberg test (All)");
//		performHardyWeinberg(wrNcFile, markersCensus, Category.ALL);
//
//		log.info("Perform Hardy-Weinberg test (Case)");
//		performHardyWeinberg(wrNcFile, markersCensus, Category.CASE);

		log.info("Perform Hardy-Weinberg test (Control)");
		performHardyWeinberg(dataSet, markersCensus, Category.CONTROL, hwControlPH);

		log.info("Perform Hardy-Weinberg test (Alternate Hardy&Weinberg)");
		performHardyWeinberg(dataSet, markersCensus, Category.ALTERNATE, hwAlternatePH);

		progressSource.setNewStatus(ProcessStatus.FINALIZING);
		dataSet.finnishWriting();
		resultOpId = ((AbstractNetCdfOperationDataSet) dataSet).getOperationKey().getId(); // HACK

		org.gwaspi.global.Utils.sysoutCompleted("Hardy-Weinberg Equilibrium Test");
		progressSource.setNewStatus(ProcessStatus.COMPLEETED);

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
	private static void performHardyWeinberg(HardyWeinbergOperationDataSet dataSet, Collection<MarkerCensusOperationEntry> markersContingencyMap, Category category, ProgressHandler progressHandler) throws IOException {

		int localMarkerIndex = 0;
		progressHandler.setNewStatus(ProcessStatus.RUNNING);
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
			final double pValue = org.gwaspi.statistics.Pvalue.calculatePvalueFromChiSqr(chiSQ, 1);

			final HardyWeinbergOperationEntry hwEntry = new DefaultHardyWeinbergOperationEntry(entry.getKey(), entry.getIndex(), category, pValue, obsHzy, expHzy);
			dataSet.addEntry(hwEntry);
			progressHandler.setProgress(localMarkerIndex);
			localMarkerIndex++;
		}
		progressHandler.setNewStatus(ProcessStatus.COMPLEETED);
	}
}
