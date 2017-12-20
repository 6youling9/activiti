package common.util;

import java.util.Properties;

public class ConfigUtil {

	private static Properties pro = null;	
	
	private static Properties getProperties(String path) {
		pro = ConfigReader.getProperties(path);
		return pro;
	}
	
	public static Properties configProperties() {
		return getProperties("config.properties");
	}
	
	public static String configValue(String key) {
		
		return getProperties("config.properties").get(key).toString();
	}
    public static String dicValue(String key) {
		
		return getProperties("dic.properties").get(key).toString();
	}
}