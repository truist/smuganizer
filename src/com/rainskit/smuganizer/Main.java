
package com.rainskit.smuganizer;

import com.rainskit.smuganizer.waitcursoreventqueue.WaitCursorEventQueue;
import java.awt.Toolkit;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class Main {
	public static final String VERSION = "0.91";

	private static File logFile;

    public static void main(String[] args) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {
		configureLogging();

		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		Toolkit.getDefaultToolkit().getSystemEventQueue().push(new WaitCursorEventQueue(170));
		new Smuganizer();
    }

	private static void configureLogging() throws IOException {
		logFile = new File(System.getProperty("java.io.tmpdir"), "smuganizer.log");
		FileHandler handler = new FileHandler(logFile.getAbsolutePath());
		handler.setFormatter(new Formatter() {
			private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss:SSS zzz");
			@Override
			public String format(LogRecord record) {
				StringBuffer result = new StringBuffer();
				result.append(dateFormat.format(new Date(record.getMillis()))).append(" [").append(record.getLevel().getLocalizedName()).append("] ");
				Throwable thrown = record.getThrown();
				if (thrown == null) {
					return result.append(record.getMessage()).append("\n").toString();
				} else {
					result.append(record.getSourceClassName()).append(' ').append(record.getSourceMethodName()).append('\n');
					result.append(record.getLevel().getLocalizedName()).append(": ").append(thrown.getLocalizedMessage()).append('\n');
					ByteArrayOutputStream stackTrace = new ByteArrayOutputStream();
					thrown.printStackTrace(new PrintStream(stackTrace));
					result.append(stackTrace.toString());
					return result.toString();
				}
			}
		});
		Logger.getLogger("").addHandler(handler);
		Logger.getLogger("com.rainskit.smuganizer").setLevel(Level.INFO);

		System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.Jdk14Logger");
	}

	public static File getLogFile() {
		return logFile;
	}
}
