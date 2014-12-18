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

package org.gwaspi.progress;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import org.gwaspi.global.Utils;
import org.slf4j.Logger;

/**
 * Logs all progress events to SLF4J.
 * @param <S> the status type
 */
public class Slf4jProgressListener<S> extends AbstractProgressListener<S> {

	private static final NumberFormat PERCENTAGE_FORMAT = new DecimalFormat("##0.00%");

	private final Logger logger;
	private final ProcessInfo processInfo;
	private Long firstCompleetedEventTime;
	private Double firstCompleetedEventCompletionFraction;

	public Slf4jProgressListener(final Logger logger, final ProcessInfo processInfo) {

		this.logger = logger;
		this.processInfo = processInfo;
		firstCompleetedEventTime = null;
		firstCompleetedEventCompletionFraction = null;
	}

	@Override
	public void statusChanged(ProcessStatusChangeEvent evt) {

		final ProcessStatus newStatus = evt.getNewStatus();
		logger.info("{}: {}{}",
				processInfo.getShortName(),
				newStatus.name(),
				newStatus.isActive() ? " ..." : (newStatus.isEnd() ? "." : ""));
	}

	@Override
	public void progressHappened(ProgressEvent<S> evt) {

		if (firstCompleetedEventTime == null) {
			firstCompleetedEventTime = System.currentTimeMillis();
			firstCompleetedEventCompletionFraction = evt.getCompletionFraction();
		}

		if (logger.isInfoEnabled()) {
			final double completionFraction = evt.getCompletionFraction();
			final long timeSpan = System.currentTimeMillis() - firstCompleetedEventTime;
			final double compleetionSpan = completionFraction - firstCompleetedEventCompletionFraction;
			final long estimatedTotalTaskTime = (long) (timeSpan / compleetionSpan);
			final long estimatedRemainingTaskTime = (long) (estimatedTotalTaskTime * (1.0 - completionFraction));
			logger.info("{}: progressed {} (ETA: {} ETT: {})",
					processInfo.getShortName(),
					PERCENTAGE_FORMAT.format(completionFraction),
					Utils.toHumanReadableTime(estimatedRemainingTaskTime),
					Utils.toHumanReadableTime(estimatedTotalTaskTime));
		}
	}
}
