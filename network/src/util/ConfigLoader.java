package util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * A application global configuration provider. This class entirely static and can only
 * load a configuration file once. After which the loaded configuration can be statically
 * accessed throughout the application.
 */
public final class ConfigLoader {
	private static String FILENAME;
	private static Properties PROPERTIES;

	private ConfigLoader() {}


	/** Loads the specified .properties config file. */
	public static Properties loadConfig(final String file) {
		if (PROPERTIES != null || FILENAME != null) // second check should be redundant
			throw new IllegalStateException("Config already loaded: " + FILENAME);

		final Properties config = new Properties();
		final Path path = Paths.get(file).toAbsolutePath();

		try {
			config.load(Files.newInputStream(path));
		}
		catch (IOException e) {
			System.err.println("Failed to read config file: " + path);
			e.printStackTrace();
			return null;
		}

		FILENAME = path.toString();
		PROPERTIES = config;

		return config;
	}


	public static String getConfigFilename() {
		return FILENAME;
	}

	public static Properties getConfig() {
		return PROPERTIES;
	}
}
