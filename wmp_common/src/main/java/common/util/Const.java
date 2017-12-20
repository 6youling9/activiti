package common.util;

import org.springframework.context.ApplicationContext;
/**
 * 项目名称：
 * @author:fh
 * 
*/
public class Const {
	public static final String SESSION_SECURITY_CODE = "sessionSecCode";
	public static final String SESSION_USER = "sessionUser";
	public static final String SESSION_ROLE_RIGHTS = "sessionRoleRights";
	public static final String SESSION_menuList = "menuList";			//当前菜单
	public static final String SESSION_allmenuList = "allmenuList";		//全部菜单
	public static final String SESSION_QX = "QX";
	public static final String SESSION_userpds = "userpds";			
	public static final String SESSION_USERROL = "USERROL";				//用户对象
	public static final String SESSION_USERNAME = "USERNAME";			//用户名
	public static final String TRUE = "T";
	public static final String FALSE = "F";
	public static final String LOGIN = "/login_toLogin.do";				//登录地址
	public static final String SYSNAME_CH = "global/SYSNAME_CH.txt";	//系统名称路径
	public static final String SYSNAME_EN = "global/SYSNAME_EN.txt";	//系统名称路径
	public static final String PAGE	= "global/PAGE.txt";			//分页条数配置路径
	public static final String EMAIL = "global/EMAIL.txt";		//邮箱服务器配置路径
	public static final String SMS1 = "global/SMS1.txt";			//短信账户配置路径1
	public static final String SMS2 = "global/SMS2.txt";			//短信账户配置路径2
	public static final String FWATERM = "global/FWATERM.txt";	//文字水印配置路径
	public static final String IWATERM = "global/IWATERM.txt";	//图片水印配置路径
	public static final String WEIXIN	= "global/WEIXIN.txt";	//微信配置路径
	public static final String FILEPATHIMG = "uploadFiles/uploadImgs/";	//图片上传路径
	
	
	/**
	 * 自定义上传路径
	 */
	public static final String FILE_UPLOAD_IMGPATH = "D:\\uploadFiles\\uploadImages";	//图片上传至磁盘的根路径
	public static final String FILE_UPLOAD_EXLPATH = "D:\\uploadFiles\\uploadExcels";	//ecxel文件上传至磁盘的根路径
	public static final String FILE_UPLOAD_IMGPATH_LINUX = "uploadFiles/uploadExcels";	//图片上传至服务器的根路径
	public static final String PORTAL_ZTSC = "dgioc_portal/ztsc";	//IOC门户 主题市场内容的子路径
	public static final String PORTAL_ZHCSYY = "dgioc_portal/zhcsyy";	//IOC门户 城市智慧应用的子路径
	public static final String PORTAL_DSJPTYY = "dgioc_portal/dsjptyy";	//IOC门户大数据应用平台的子路径
	public static final String IND_ZHCSYY = "dgioc_ind/zhcsyy";	//工业与经济 智慧城市应用上传文件子路径
	
	
	
	public static final String FILEPATHFILE = "uploadFiles/file/";		//文件上传路径
	public static final String FILEPATHTWODIMENSIONCODE = "uploadFiles/twoDimensionCode/"; //二维码存放路径
	public static final String NO_INTERCEPTOR_PATH = ".*/((login)|(logout)|(code)|(app)|(weixin)|(static)|(main)|(websocket)).*";	//不对匹配该值的访问路径拦截（正则）
	
	
	public static ApplicationContext WEB_APP_CONTEXT = null; //该值会在web容器启动时由WebAppContextListener初始化
	
	/**
	 * APP Constants
	 */
	//app注册接口_请求协议参数)
	public static final String[] APP_REGISTERED_PARAM_ARRAY = new String[]{"countries","uname","passwd","title","full_name","company_name","countries_code","area_code","telephone","mobile"};
	public static final String[] APP_REGISTERED_VALUE_ARRAY = new String[]{"国籍","邮箱帐号","密码","称谓","名称","公司名称","国家编号","区号","电话","手机号"};
	
	//app根据用户名获取会员信息接口_请求协议中的参数
	public static final String[] APP_GETAPPUSER_PARAM_ARRAY = new String[]{"USERNAME"};
	public static final String[] APP_GETAPPUSER_VALUE_ARRAY = new String[]{"用户名"};
	

	

	
}
