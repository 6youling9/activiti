package common.config;

import common.util.PropertiesUtil;

import java.util.Properties;


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
