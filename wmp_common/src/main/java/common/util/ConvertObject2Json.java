package common.util;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;

public final class ConvertObject2Json {
	
	
	
	private static final Log log = LogFactory.getLog(ConvertObject2Json.class);
	
	/**
	 * 方法功能：将对象转换成JSON字符串，并响应回前台
	 * 参数：object
	 * 返回值：void
	 * 异常：IOException
	 */
	public static void writeJson(Object object,HttpServletRequest request, HttpServletResponse response) {
		PrintWriter out = null;
		try {
			String datePattern = "yyyy-MM-dd HH:mm:ss";
			String json = JSON.toJSONStringWithDateFormat(object, datePattern);
			response.setContentType("text/html;charset=utf-8");
			out = response.getWriter();
			out.write(json);
			out.flush();
		} catch (IOException e) {
			log.error("响应json输出异常：" + e);
		} finally {
			if (out != null) {
				out.close();
			}
		}
	}
	
	
	/**
	 * 方法功能：将对象转换成JSONObject字符串，并响应回前台
	 * 参数：object
	 * 返回值：void
	 * 异常：IOException
	 */
	public static void writeJsonObject(Object object,HttpServletRequest request, HttpServletResponse response){
		
		PrintWriter out = null;
		
		try {
			String datePattern = "yyyy-MM-dd HH:mm:ss";
			String json=JSONObject.toJSONStringWithDateFormat(object,datePattern,SerializerFeature.WriteMapNullValue);
		    json=json.replaceAll("null", "\"\"");
			response.setContentType("application/json;charset=UTF-8");
			out = response.getWriter();
			out.write(json);
			out.flush();
		} catch (IOException e) {
			log.error("响应json输出异常：" + e);
		} finally {
			if (out != null) {
				out.close();
			}
		}
	}

}
