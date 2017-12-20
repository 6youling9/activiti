package common.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLDecoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.json.JSONException;
import org.json.JSONObject;

public class HttpUtils {
	public static byte[] getImage(HttpClient httpclient,String url){
		try {

			// 利用HTTP GET向服务器发起请求
			HttpGet get = new HttpGet(url);
			
			// 获得服务器响应的的所有信息
			HttpResponse response = httpclient.execute(get);
			// 获得服务器响应回来的消息体（不包括HTTP HEAD）
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				InputStream is = entity.getContent();
				return IOUtils.toByteArray(is);
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
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
			String str=URLDecoder.decode(sb.toString(),"utf-8");
			System.out.println("发送过来的参数为："+str);
			return new JSONObject(str.substring(str.indexOf("{"),str.indexOf("}")+1));
		}
	}
}
