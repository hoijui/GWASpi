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

package org.gwaspi.progress;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class SuperProgressSource extends AbstractProgressHandler<Double> {

	private final Map<ProgressSource, Double> subProgressSourcesAndWeights;
	private final Map<ProgressSource, Double> subProgressSourcesAndLastCompletionFraction;
	private final ProgressListener progressListener;
	private double lastCompletionFraction;
	private double weightSum;

	private class SubProgressListener<ST> extends AbstractProgressListener<ST> {

		@Override
		public void progressHappened(ProgressEvent evt) {

			Double currentSubCompletionFraction = evt.getCompletionFraction();
			if (currentSubCompletionFraction != null) {
				ProgressSource progressSource = (ProgressSource) evt.getSource();
				final double weight = subProgressSourcesAndWeights.get(progressSource);
				double lastSubCompletionFraction = subProgressSourcesAndLastCompletionFraction.get(progressSource);
				fireAdditionalProgressHappened((currentSubCompletionFraction - lastSubCompletionFraction) * weight / weightSum);
				subProgressSourcesAndLastCompletionFraction.put(progressSource, currentSubCompletionFraction);
			}
		}
	}

	public SuperProgressSource(ProcessInfo processInfo, Map<ProgressSource, Double> subProgressSourcesAndWeights) {
		super(processInfo, calculateNumIntervalls(subProgressSourcesAndWeights));

		this.subProgressSourcesAndWeights = subProgressSourcesAndWeights;
		this.subProgressSourcesAndLastCompletionFraction = new HashMap<ProgressSource, Double>();
		this.progressListener = new SubProgressListener();
		this.lastCompletionFraction = 0.0;
		this.weightSum = 0.0;

		for (Map.Entry<ProgressSource, Double> subProgressSourceAndWeight : subProgressSourcesAndWeights.entrySet()) {
			final ProgressSource progressSource = subProgressSourceAndWeight.getKey();
			final double weight = subProgressSourceAndWeight.getValue();
			progressSource.addProgressListener(progressListener);
			subProgressSourcesAndLastCompletionFraction.put(progressSource, 0.0);
			weightSum += weight;
		}
	}

	public SuperProgressSource(ProcessInfo processInfo, Collection<ProgressSource> subProgressSources) {
		this(processInfo, createEvenlyDistributedWeights(subProgressSources));
	}

	public SuperProgressSource(ProcessInfo processInfo) {
		this(processInfo, Collections.EMPTY_MAP);
	}

	private static Map<ProgressSource, Double> createEvenlyDistributedWeights(Collection<ProgressSource> subProgressSources) {

		Map<ProgressSource, Double> subProgressSourcesAndWeights
				= new HashMap<ProgressSource, Double>(subProgressSources.size());

		final double weight = 1.0 / subProgressSources.size();
		for (ProgressSource progressSource : subProgressSources) {
			subProgressSourcesAndWeights.put(progressSource, weight);
		}

		return subProgressSourcesAndWeights;
	}

	private static Integer calculateNumIntervalls(Map<ProgressSource, Double> subProgressSourcesAndWeights) {

		int numIntervalls = 0;
		for (ProgressSource progressSource : subProgressSourcesAndWeights.keySet()) {
			Integer numSubIntervalls = progressSource.getNumIntervals();
			if (numSubIntervalls == null) {
				numSubIntervalls = 1;
			}
			numIntervalls += numSubIntervalls;
		}

		return numIntervalls;
	}

	public void addSubProgressSource(ProgressSource progressSource, Double weight) {

		subProgressSourcesAndWeights.put(progressSource, weight);
		progressSource.addProgressListener(progressListener);
		subProgressSourcesAndLastCompletionFraction.put(progressSource, 0.0);
		weightSum += weight;
		fireProcessDetailsChanged();
	}

	public Map<ProgressSource, Double> getSubProgressSourcesAndWeights() {
		return subProgressSourcesAndWeights;
	}

	@Override
	public void setProgress(Double currentState) {
		throw new UnsupportedOperationException("progress should never be set directly on the super process, but only on its child pocesses");
	}

	private void fireAdditionalProgressHappened(Double additionalCompletionFraction) {

		final double newCompletionFraction = lastCompletionFraction + additionalCompletionFraction;
		fireProgressHappened(newCompletionFraction, newCompletionFraction);
		lastCompletionFraction = newCompletionFraction;
	}
}
