package controller.identity;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bonc.ioc.common.util.ConvertObject2Json;
import org.activiti.engine.IdentityService;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.GroupQuery;
import org.activiti.engine.identity.User;
import org.activiti.engine.identity.UserQuery;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.List;

/**
 * 用户相关控制器
 *
 * @author HenryYan
 */
@Controller
@RequestMapping("/user")
public class UseController {

    private static Logger logger = LoggerFactory.getLogger(UseController.class);

    // Activiti Identify Service
    private IdentityService identityService;

    /**
     * 登录系统
     *
     * @param userName
     * @param password
     * @param session
     * @return
     */
    @RequestMapping(value = "/logon")
    public String logon(@RequestParam("username") String userName, @RequestParam("password") String password, HttpSession session) {
        logger.debug("logon request: {username={}, password={}}", userName, password);
        boolean checkPassword = identityService.checkPassword(userName, password);
        if (checkPassword) {

            // read user from database
            User user = identityService.createUserQuery().userId(userName).singleResult();
             //  UserUtil.saveUserToSession(session, user);

            List<Group> groupList = identityService.createGroupQuery().groupMember(userName).list();
            session.setAttribute("groups", groupList);

            String[] groupNames = new String[groupList.size()];
            for (int i = 0; i < groupNames.length; i++) {
                System.out.println(groupList.get(i).getName());
                groupNames[i] = groupList.get(i).getName();
            }

            session.setAttribute("groupNames", ArrayUtils.toString(groupNames));

            return "redirect:/main/index";
        } else {
            return "redirect:/login?error=true";
        }
    }
    /**
     * 用户列表
     * @param request
     * @param response
     */
    @RequestMapping(value = "/userlist")
    public void userList(HttpServletRequest request, HttpServletResponse response) {
    	   JSONArray resultJsonArray=new JSONArray();
           logger.debug("logon request: {request={}, response={}}", request, response);
           String params=request.getParameter("params");
           UserQuery userquery=null;
           if(!StringUtils.isEmpty(params)){
        	      userquery=identityService.createUserQuery();
        		  userquery.userId(params);
        		  if(userquery.list().size()==0){
        			  userquery=identityService.createUserQuery();
        			  userquery.userFirstNameLike("%"+params+"%");
        		   }
        	 }else{
        		 userquery=identityService.createUserQuery();
        	 }
           List<User> userList = userquery.list();
            for (User user:userList) {
               JSONObject jsonObject=new JSONObject();
	               jsonObject.put("id", user.getId());
	               jsonObject.put("name", user.getLastName()+user.getFirstName());
               List<Group> groupNameList=identityService.createGroupQuery().groupMember(user.getId()).list();
           	   StringBuffer groupNameBuffer=new StringBuffer();
               for( Group groupName:groupNameList){
            	   groupNameBuffer.append(groupName.getName()).append(",");
               }
               if(groupNameBuffer.length()>0){
            	   jsonObject.put("group", groupNameBuffer.toString().substring(0, groupNameBuffer.length()-1));
               }else{
            	   jsonObject.put("group","");
               }
                   
               resultJsonArray.add(jsonObject);
			}
            ConvertObject2Json.writeJson(resultJsonArray, request, response);
    }
    /**
     * 用户组列表
     * @param request
     * @param response
     */
    @RequestMapping(value = "/grouplist")
    public void grouplist(HttpServletRequest request, HttpServletResponse response) {
    	   JSONArray resultJsonArray=new JSONArray();
           logger.debug("logon request: {request={}, response={}}", request, response);
           //获取查询变量
           String params=request.getParameter("params");
           GroupQuery groupQuery=null;
           if(!StringUtils.isEmpty(params)){
        	   groupQuery= identityService.createGroupQuery();
        	   groupQuery.groupId(params);
        	    if(groupQuery.list().size()==0){
        	    	groupQuery=identityService.createGroupQuery();
        	    	groupQuery.groupNameLike("%"+params+"%");
                }
        	 }else{
        	    groupQuery= identityService.createGroupQuery(); 
        	 }
           
           List<Group> groupList = groupQuery.list();
	            for (Group group:groupList) {
	               JSONObject jsonObject=new JSONObject();
		               jsonObject.put("id", group.getId());
		               jsonObject.put("name", group.getName());
		               jsonObject.put("type", group.getType());
	               resultJsonArray.add(jsonObject);
				}
           ConvertObject2Json.writeJson(resultJsonArray, request, response);
    }
    

    @RequestMapping(value = "/logout")
    public String logout(HttpSession session) {
        session.removeAttribute("user");
        return "/login";
    }

    @Autowired
    public void setIdentityService(IdentityService identityService) {
        this.identityService = identityService;
    }

}
