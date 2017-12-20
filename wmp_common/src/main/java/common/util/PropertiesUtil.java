package common.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class PropertiesUtil {
	private static PropertiesUtil util = null;
	private static Map<String,Properties> props = null;
	private PropertiesUtil(){
		
	}
	
	public static void main(String[] args) {
		//resources/fullScreen
		
		Properties prop = new Properties();
		InputStream in = PropertiesUtil.class.getResourceAsStream("/fullScreen.properties"); 
		try {
			prop.load(in);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(prop.get("JJZL_ZYYWSR"));
	}
	
	public static PropertiesUtil getInstance() {
		if(util==null) {
			props = new HashMap<String, Properties>();
			util = new PropertiesUtil();
		}
		return util;
	}
	
	
	public Properties load(String name) {
		if(props.get(name)!=null) {
			return props.get(name);
		} else {
			Properties prop = new Properties();
			try {
				//prop.load(PropertiesUtil.class.getResourceAsStream("/"+name+".properties"));
				prop.load(new InputStreamReader(PropertiesUtil.class.getResourceAsStream("/"+name+".properties"), "UTF-8"));
				props.put(name, prop);
				return prop;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
}
