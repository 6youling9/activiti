package common.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;
/*
 *对http请求头做处理的工具类
 */
public class HTTPUtil {
	/*
	 *将post请求的json字符串格式的参数转为JSONObject 
	 */
	public static JSONObject httpProcess(HttpServletRequest request,HttpServletResponse response) throws IOException, JSONException{
		StringBuffer sb = new StringBuffer() ; 
		InputStream is = request.getInputStream(); 
		BufferedReader br = new BufferedReader(new InputStreamReader(is,"utf-8")); 
		String s = "" ; 
		while((s=br.readLine())!=null){ 
			sb.append(s) ; 
		} 
		if(sb.toString().length()<=0){
			return null;
		}else {
			return new JSONObject(sb.toString());
		}
	}
}
