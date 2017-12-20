package common.util;

import java.io.IOException;
import java.util.HashMap;

import javax.xml.namespace.QName;

import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.rpc.client.RPCServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class IMPWSUtil {
	
	private static final Logger logger = LoggerFactory.getLogger(IMPWSUtil.class);
	
	/**
	 * 统一运维平台接口调用
	 * @param service 发布服务名称
	 * @param method  请求调用方法
	 * @param params  请求参数 json格式
	 * @param timeOutInMilliSeconds 请求超时时长
	 * @return 接口返回值json格式
	 */
	public static String invoke(String service,String method,String params,Long timeOutInMilliSeconds){
		logger.info("接口："+service+","+method+"方法被调用");
		EndpointReference targetEPR = new EndpointReference(ConfigUtil.configValue("ws.endpointReference")+"/"+service);
		try {
			RPCServiceClient sender = new RPCServiceClient();
			Options options = sender.getOptions();
			options.setTimeOutInMilliSeconds(timeOutInMilliSeconds);//超时时间20s//
			options.setTo(targetEPR);
			
			QName qname = new QName(ConfigUtil.configValue("ws.QName"), method);
			Object[] param = new Object[]{params};
			Class<?>[] types = new Class[]{String.class};  //这是针对返值类型的
			Object[] response = sender.invokeBlocking(qname, param, types);
			ObjectMapper mapper = new ObjectMapper();
			mapper.configure(SerializationFeature.INDENT_OUTPUT, Boolean.TRUE);
			logger.info("接口："+service+","+method+"方法调用完成");
			return (String) response[0];
		} catch (AxisFault e) {
			logger.info("接口："+service+","+method+"方法调用异常！参数："+params+"\n错误信息："+e.getStackTrace());
		}
		return null;
	}
	
	/**
	 * 统一运维平台接口调用
	 * @param service 发布服务名称
	 * @param method  请求调用方法
	 * @param params  请求参数 json格式
	 * @return 接口返回值json格式
	 */
	public static String invoke(String service,String method,String params) {
		return invoke(service,method,params,ConfigUtil.configValue("ws.timeOutInMilliSeconds")==null?2*20000L:Long.parseLong(ConfigUtil.configValue("ws.timeOutInMilliSeconds")));
	}
	
	/**
	 * 解析统一运维平台接口调用返回报文为MAP对象
	 * @param service 发布服务名称
	 * @param method  请求调用方法
	 * @param params  请求参数 json格式
	 * @return 返回将接口返回值的解析的MAP
	 */
	public static HashMap getInvokeReturnMap(String service,String method,String params){
		String json = invoke(service,method,params);
		if(json!=null){
			ObjectMapper mapper = new ObjectMapper();
			mapper.configure(SerializationFeature.INDENT_OUTPUT, Boolean.TRUE);
			HashMap map = null;
			try {
				map = mapper.readValue(json,HashMap.class);
			} catch (JsonParseException e) {
				e.printStackTrace();
			} catch (JsonMappingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} //解析数组
			return map;
		}
		return null;
	}
	
	/**
	 * 验证是否请求成功
	 * @param map 请求返回对象
	 * @return
	 */
	public static boolean isInvokeSuccess(HashMap map){
		if(map!=null){
			if("true".equalsIgnoreCase(map.get("result").toString())){
				return true;
			}
		}
		return false;
	}
}