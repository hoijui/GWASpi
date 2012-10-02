package org.gwaspi.gui.utils;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.encoder.EchoEncoder;
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

		private Encoder<ILoggingEvent> encoder;
		private ByteArrayOutputStream buffer;
		private Document target;

		LogBackAppender(Document target) {

			this.encoder = null;
			this.buffer = new ByteArrayOutputStream();
			this.target = target;

			setName("Document appender");
			LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
			setContext(lc);
		}

		@Override
		public void start() {

			if (encoder == null) {
				try {
					encoder = new EchoEncoder<ILoggingEvent>();
					encoder.init(buffer);
				} catch (IOException ex) {
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
				} catch (IOException ex) {
					throw new RuntimeException(ex);
				}
			}

			super.stop();
		}

		@Override
		public void append(ILoggingEvent event) {

			if (encoder != null) {
				try {
					encoder.doEncode(event);
					buffer.flush();
					String line = buffer.toString();
					buffer.reset();
					try {
						target.insertString(target.getLength(), line, null);
					} catch (BadLocationException ex) {
						throw new RuntimeException(ex);
					}
				} catch (IOException ex) {
					throw new RuntimeException(ex);
				}
			}
		}
	}

	private LogBackAppender logBackAppender;

	public LogDocument() {

		this.logBackAppender = new LogBackAppender(this);
		this.logBackAppender.start();
		LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
		lc.getLogger(Logger.ROOT_LOGGER_NAME).addAppender(this.logBackAppender);
	}
}
