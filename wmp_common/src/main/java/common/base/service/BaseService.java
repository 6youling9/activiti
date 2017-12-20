/*
 * Copyright(C)2016-2020 BONC Software Co. Ltd. All rights reserved.
 * 系统名称：bonc_ioc_imp 1.0
 * 作者：左明强 && 手机：13522126905
 * 版本号                                                     日   期                       作     者                  变更说明
 * BaseService-V1.0     2016/09/06  左明强                  新建
 */
package common.base.service;

import java.text.SimpleDateFormat;
import java.util.Date;

 

/**
 * <p> 名       称：业务层操作基类
 * <p> 功       能：包含业务层操作公共功能操作方法
 * <p> 作       者： 左明强
 * <p> 联系方式：13522126905
 * <p> 创建时间：2016/09/06 10:00:00 
 * <p> 特殊情形： 无
 */
public class BaseService {

	//protected Logger logger = Logger.getLogger(this.getClass());
	/**
	 * 获取操作时间
	 * 格式为：yyyyMMdd HH:mi:ss
	 * @return
	 */
	protected String getDateTime(){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return sdf.format(new Date());
	}
	
	/**
	 * 获取操作时间
	 * 格式为：yyyyMMdd
	 * @return
	 */
	protected String getDate(){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		return sdf.format(new Date());
	}
	/**
	 * 得到64位的uuid
	 * @return
	 */
	/*public String get64UUID(){
		return UuidUtil.get64UUID();
	}*/
	
	/**
	 * shiro缓存当前操作用户信息
	 * @return
	 */
	/*protected User getCurrentUser(){
		return (User) SecurityUtils.getSubject().getSession().getAttribute(Constants.CURRENT_USER);
	}*/
}