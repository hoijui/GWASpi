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
import org.slf4j.Logger;

/**
 * Logs all progress events to SLF4J.
 * @param <ST> the status type
 */
public class Slf4jProgressListener<ST> implements ProgressListener<ST> {

	private static final NumberFormat PERCENTAGE_FORMAT = new DecimalFormat("##0.00%");

	private final Logger logger;
	private final ProgressHandler progressHandler;

	public Slf4jProgressListener(final Logger logger, final ProgressHandler progressHandler) {

		this.logger = logger;
		this.progressHandler = progressHandler;
	}

	@Override
	public void processStarted() {
		logger.info("{}: started. now initializing ...", progressHandler.getShortName());
	}

	@Override
	public void processInitialized() {
		logger.info("{}: initialized. now progressing ...", progressHandler.getShortName());
	}

	@Override
	public void progressHappened(ProgressEvent<ST> evt) {

		if (logger.isInfoEnabled()) {
			logger.info("{}: progressed {}",
					progressHandler.getShortName(),
					PERCENTAGE_FORMAT.format(evt.getCompletionFraction().floatValue()));
		}
	}

	@Override
	public void processEnded() {
		logger.info("{}: ended. now finalizing ...", progressHandler.getShortName());
	}

	@Override
	public void processFinalized() {
		logger.info("{}: finalized.", progressHandler.getShortName());
	}
}
