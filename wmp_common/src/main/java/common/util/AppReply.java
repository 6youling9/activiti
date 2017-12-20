package common.util;


import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;

/**
 * app接口返回数据实体
 * @author shy
 * @date   18210175120
 * @param <T>
 */
public class AppReply<T> {

    
	public static final String EORRO_CODE ="1";              //请求失败
	public static final String SUCCESS_CODE ="0";            //请求成功
	public static final String TYPE_ERROR_CODE ="100";       //类型错误
	public static final String LACK_PARAMENT_CODE ="103";    //缺少参数
	public static final String SERVICE_ERROR_CODE ="200";    //服务器错误
	public static final String SERVICE_DISABLED_CODE ="201"; //服务器不可用
	public static final String SERVICE_RESET_CODE ="202";    //服务器正在重启
    		
	private String code = SUCCESS_CODE;  //状态编号

	private Object obj;   //单条对象
	
	private List<T> data; //数组对象
	
	private String msg;  //附加消息
	
	private String url;  //附加地址
	
	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public Object getObj() {
		return obj;
	}

	public void setObj(Object obj) {
		this.obj = obj;
	}

	public List<T> getData(List<PageData> approval) {
		return data;
	}

	public void setData(List<T> data) {
		this.data = data;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
	
    @Override
	public String toString() {
    	
    	 String datePattern = "yyyy-MM-dd HH-mm-ss";
    	 
    	 String json=JSONObject.toJSONStringWithDateFormat(this,datePattern,SerializerFeature.WriteMapNullValue);
		 
    	 json=json.replaceAll("null", "\"\"");
    	 
		return json;
	}

}
