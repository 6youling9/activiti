package common.base.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import common.util.Page;
import common.util.PageData;
import common.util.PageResult;
import common.util.UuidUtil;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.ModelAndView;


public class BaseController {

	protected String id;
	protected String ids;
	protected HttpServletRequest request;
	protected HttpServletResponse response;
	protected HttpSession session;
	protected PageResult pageResult;

	@ModelAttribute
	public void setReqAndRes(HttpServletRequest request, HttpServletResponse response){
		this.request = request;
		this.response = response;
		this.session = request.getSession();
	}

	/**
	 * 得到PageData
	 */
	public PageData getPageData() {
		return new PageData(request);
	}

	/**
	 * 得到ModelAndView
	 */
	public ModelAndView getModelAndView() {
		return new ModelAndView();
	}
	
	/**
	 * 得到request对象
	 */
	public HttpServletRequest getRequest() {
		HttpServletRequest request = ((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getRequest();
		return request;
	}

	/**
	 * 得到32位的uuid
	 * @return
	 */
	public String get32UUID(){
		return UuidUtil.get32UUID();
	}

	/**
	 * 获取登录用户的IP
	 * 
	 * @throws Exception
	 */
	public String getRemoteIP() throws Exception {
//		HttpServletRequest request = getHttpServletRequest();
		String ip = "";
		if (request.getHeader("x-forwarded-for") == null) {
			ip = request.getRemoteAddr();
		} else {
			ip = request.getHeader("x-forwarded-for");
		}
		return ip;
	}

	/**
	 * 得到分页列表的信息
	 */
	public Page getPage() {
		return new Page();
	}
	
	protected HttpServletRequest getHttpServletRequest(){
		return ((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getRequest();
	}

	protected HttpServletResponse getHttpServletResponse(){
		return ((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getResponse();
	}

}