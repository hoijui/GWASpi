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
import java.util.HashMap;
import java.util.Map;

public class SuperProgressSource extends AbstractProgressSource<Double> {

	private final Map<ProgressSource, Double> subProgressSourcesAndWeights;
	private final Map<ProgressSource, Double> subProgressSourcesAndLastCompletionFraction;
	private final ProgressListener progressListener;
	private final double lastCompletionFraction;

	private class SubProgressListener implements ProgressListener {

		@Override
		public void processStarted() {}

		@Override
		public void processInitialized() {}

		@Override
		public void progressHappened(ProgressEvent evt) {

			Double currentSubCompletionFraction = evt.getCompletionFraction();
			if (currentSubCompletionFraction != null) {
				ProgressSource progressSource = (ProgressSource) evt.getSource();
				final double weight = subProgressSourcesAndWeights.get(progressSource);
				double lastSubCompletionFraction = subProgressSourcesAndLastCompletionFraction.get(progressSource);
				fireAdditionalProgressHappened((currentSubCompletionFraction - lastSubCompletionFraction) * weight);
				subProgressSourcesAndLastCompletionFraction.put(progressSource, currentSubCompletionFraction);
			}
		}

		@Override
		public void processEnded() {}

		@Override
		public void processFinalized() {}
	}

	public SuperProgressSource(Map<ProgressSource, Double> subProgressSourcesAndWeights) {
		super(calculateNumIntervalls(subProgressSourcesAndWeights));

		this.subProgressSourcesAndWeights = subProgressSourcesAndWeights;
		this.subProgressSourcesAndLastCompletionFraction = new HashMap<ProgressSource, Double>();
		this.progressListener = new SubProgressListener();
		this.lastCompletionFraction = 0.0;

		for (ProgressSource progressSource : subProgressSourcesAndWeights.keySet()) {
			progressSource.addProgressListener(progressListener);
			subProgressSourcesAndLastCompletionFraction.put(progressSource, 0.0);
		}
	}

	public SuperProgressSource(Collection<ProgressSource> subProgressSources) {
		this(createEvenlyDistributedWeights(subProgressSources));
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
			Integer numSubIntervalls = progressSource.getNumIntervalls();
			if (numSubIntervalls == null) {
				numSubIntervalls = 1;
			}
			numIntervalls += numSubIntervalls;
		}

		return numIntervalls;
	}

	private void fireAdditionalProgressHappened(Double additionalCompletionFraction) {

		final double newCompletionFraction = lastCompletionFraction + additionalCompletionFraction;
		fireProgressHappened(newCompletionFraction, newCompletionFraction);
	}
}
