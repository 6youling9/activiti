package common.config;

import java.util.Properties;

import org.apache.commons.lang3.SystemUtils;

import com.bonc.ioc.common.util.PropertiesUtil;

public class Config
{
    private static Properties prop;
    public static String CITY;//当前城市名称
    static 
    {
		prop=new Properties();
		try{   
		    prop = PropertiesUtil.getInstance().load("config");
		    CITY=prop.getProperty("city");
		}catch (Exception e){
		    e.printStackTrace();
		}
		
    }

}
