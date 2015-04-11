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

package org.gwaspi.gui.utils;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.encoder.Encoder;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Automatically updates its-self to contain the log content.
 */
public class LogDocument extends PlainDocument {

	private static class LogBackAppender extends AppenderBase<ILoggingEvent> {

		private final ByteArrayOutputStream buffer;
		private final Document target;
		private Encoder<ILoggingEvent> encoder;

		LogBackAppender(final Document target) {

			this.encoder = null;
			this.buffer = new ByteArrayOutputStream();
			this.target = target;

			setName("Document appender");
			final LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
			setContext(loggerContext);
		}

		@Override
		public void start() {

			if (encoder == null) {
				try {
					final PatternLayoutEncoder tmpEncoder = new PatternLayoutEncoder();
					tmpEncoder.setContext(getContext());
					tmpEncoder.setPattern("%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n");
					encoder = tmpEncoder;

					encoder.init(buffer);
					encoder.start();
				} catch (final IOException ex) {
					throw new RuntimeException(ex);
				}
			}

			super.start();
		}

		@Override
		public void stop() {

			if (encoder != null) {
				try {
					encoder.close();
					encoder = null;
				} catch (final IOException ex) {
					throw new RuntimeException(ex);
				}
			}

			super.stop();
		}

		@Override
		public void append(final ILoggingEvent event) {

			if (encoder != null) {
				try {
					encoder.doEncode(event);
					buffer.flush();
					final String line = buffer.toString();
					buffer.reset();
					try {
						target.insertString(target.getLength(), line, null);
					} catch (final BadLocationException ex) {
						throw new RuntimeException(ex);
					}
				} catch (final IOException ex) {
					throw new RuntimeException(ex);
				}
			}
		}
	}

	private final LogBackAppender logBackAppender;

	public LogDocument() {

		this.logBackAppender = new LogBackAppender(this);
		this.logBackAppender.start();
		final LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
		loggerContext.getLogger(Logger.ROOT_LOGGER_NAME).addAppender(this.logBackAppender);
	}
}
