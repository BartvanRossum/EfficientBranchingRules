package util;

import java.io.FileInputStream;
import java.time.LocalDate;
import java.util.Properties;

public class Configuration
{
	private final static String DEFAULT_PROPERTIES = "default.properties";
	private final static String CUSTOM_PROPERTIES = "custom.properties";

	private static Configuration configuration;
	private Properties properties;

	private Configuration(String defaultPropertiesFile, String customPropertiesFile)
	{
		// Load default properties.
		Properties defaultProperties = new Properties();
		try
		{
			defaultProperties.load(new FileInputStream(defaultPropertiesFile));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		// Override default properties with custom settings.
		this.properties = new Properties(defaultProperties);
		try
		{
			properties.load(new FileInputStream(customPropertiesFile));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public Properties getProperties()
	{
		return properties;
	}

	public String getStringProperty(String key)
	{
		return properties.getProperty(key);
	}

	public LocalDate getDateProperty(String key)
	{
		return LocalDate.parse(properties.getProperty(key));
	}

	public double getDoubleProperty(String key)
	{
		return Double.parseDouble(properties.getProperty(key));
	}

	public int getIntProperty(String key)
	{
		return Integer.parseInt(properties.getProperty(key));
	}

	public boolean getBooleanProperty(String key)
	{
		return Boolean.parseBoolean(properties.getProperty(key));
	}

	public static Configuration getConfiguration()
	{
		return (configuration == null)
				? Configuration.configuration = new Configuration(DEFAULT_PROPERTIES, CUSTOM_PROPERTIES)
				: configuration;
	}

	public static void initialiseConfiguration(String defaultPropertiesFile, String customPropertiesFile)
	{
		Configuration.configuration = new Configuration(defaultPropertiesFile, customPropertiesFile);
	}

	public String getAllProperties()
	{
		StringBuilder sb = new StringBuilder();
		for (String key : properties.stringPropertyNames())
		{
			sb.append(key + " = " + properties.getProperty(key) + "\n");
		}
		return sb.toString();
	}
}
