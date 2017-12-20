package common.util;

import java.io.InputStream;
import java.util.Properties;

public class ConfigReader {
	public static Properties getProperties(String propertiesName) {
		ClassLoader classLoader = getDefaultClassLoader();
		InputStream is = classLoader.getResourceAsStream(propertiesName);
		if (null == is) {
			classLoader = ConfigReader.class.getClassLoader();
			is = classLoader.getResourceAsStream(propertiesName);
			if(null == is){
				return null;
			}
		}
		Properties prop = new Properties();
		try {
			prop.load(is);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return prop;
	}

	private static ClassLoader getDefaultClassLoader() {
		ClassLoader cl = null;
		try {
			cl = Thread.currentThread().getContextClassLoader();
		} catch (Throwable localThrowable) {
		}
		if (cl == null) {
			cl = ConfigReader.class.getClassLoader();
		}
		return cl;
	}

	public static String getPropertiesValue(String propertiesName, String key) {
		Properties prop = getProperties(propertiesName);
		if (null != prop) {
			if (prop.containsKey(key)) {
				return prop.getProperty(key);
			}
		}
		return "";
	}
}