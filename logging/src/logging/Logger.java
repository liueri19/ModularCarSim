package logging;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Utility class for debug logs.
 */
public final class Logger {
	private static final Queue<Runnable> TASKS = new ConcurrentLinkedQueue<>();
	private static boolean running = true;
	private static final Thread EXECUTOR = new Thread(() -> {
		while (running) {
			try {
				while (TASKS.peek() == null)
					Thread.onSpinWait();
				TASKS.poll().run();
			}
//			catch (InterruptedException e) {
//				Thread.currentThread().interrupt();
//				running = false;
//			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	});
	static {
		EXECUTOR.setDaemon(true);
		EXECUTOR.setName("Logger");
	}


	private final String absolutePathStr;
	private final BufferedWriter writer;

	/**
	 * Constructs a new Logger that logs to the file at the specified path.
	 */
	public Logger(final String pathStr) {
		final Path path;
		try {
			path = Path.of(pathStr);
			absolutePathStr = path.toAbsolutePath().toString();
		}
		catch (InvalidPathException e) {
			System.err.println("Invalid path: " + pathStr);
			throw e;
		}

		try {
			this.writer = Files.newBufferedWriter(path);
		}
		catch (IOException e) {
			System.err.println("Could not write to file: " + absolutePathStr);
			throw new IllegalArgumentException();
		}

		EXECUTOR.start();
	}

	/**
	 * Constructs a new Logger that logs to the specified OutputStream.
	 */
	public Logger(final OutputStream stream) {
		writer = new BufferedWriter(new OutputStreamWriter(stream));
		absolutePathStr = stream.toString();

		EXECUTOR.start();
	}


	public void log(final String message) {
		TASKS.offer(() -> {
			try {
				writer.append(message);
			}
			catch (IOException e) {
				System.err.println("Failed to write log to file: " + absolutePathStr);
				e.printStackTrace();
			}
		});
	}

	/**
	 * Logs the message and appends a new line.
	 */
	public void logln(final String message) {
		log(message + System.lineSeparator());
	}

	public void logf(final String message, final Object... args) {
		log(String.format(message, args));
	}

}
