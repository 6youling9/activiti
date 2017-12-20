package common.util;

import com.bonc.ioc.common.base.exception.BusinessException;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * <p> 名       称：接口JSON包装类
 * <p> 功       能：用于封装格式化的接口信息
 * <p> 作       者：左明强
 * <p> 联系方式：zuomingqiang@bonc.com.cn
 * <p> 创建时间：2017-7-20 下午4:48:35 
 * <p> 特殊情形： 无
 */
public class RetJson {

	public static final String EORRO_CODE = "0"; 					// 请求失败
	public static final String SUCCESS_CODE = "1"; 					// 请求成功
	public static final String SUCCESS_MSG = "成功"; 			// 请求成功
	public static final String ERROR_MSG = "失败"; 				// 请求失败
	
	private String retCode;
	
	private String retMsg;
	
	private Object data;
	
	public RetJson(){
	}
	
	public RetJson(String retCode, String retMsg, Object data) {
		super();
		this.retCode = retCode;
		this.retMsg = retMsg;
		setData(data);
	}

	public static RetJson success(Object obj){
		return new RetJson(RetJson.SUCCESS_CODE, RetJson.SUCCESS_MSG,obj);
	}
	
	public static RetJson success(String msg){
		return new RetJson(RetJson.SUCCESS_CODE, msg,null);
	}
	
	public static RetJson success(){
		return success(RetJson.SUCCESS_MSG);
	}
	
	public static RetJson error(String msg){
		return new RetJson(RetJson.EORRO_CODE, msg,null);
	}
	
	public static RetJson error(BusinessException businessException){
		return new RetJson(RetJson.EORRO_CODE, businessException.getCode(),null);
	}
	
	public static RetJson error(){
		return error(RetJson.ERROR_MSG);
	}
	
	public String getRetCode() {
		return retCode;
	}

	public void setRetCode(String retCode) {
		this.retCode = retCode;
	}

	public String getRetMsg() {
		return retMsg;
	}

	public void setRetMsg(String retMsg) {
		this.retMsg = retMsg;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		if( data instanceof PageResult) {
			Map<String,Object> map = new HashMap<>();
			map.put("total", ((PageResult) data).getTotal());
			map.put("rows", ((PageResult) data).getRows());
			HttpServletRequest request = HttpServletUtil.getHttpServletRequest();
			map.put("pageNumber", request.getParameter("pageNumber"));
			map.put("pageSize", request.getParameter("pageSize"));
			this.data = map;
		} else {
			this.data = data == null ? "" : data;
		}
	}
	
}
