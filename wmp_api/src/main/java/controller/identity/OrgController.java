package controller.identity;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bonc.ioc.common.util.ConvertObject2Json;
import com.bonc.ioc.common.util.ZTreeNode;
import com.bonc.ioc.common.util.ZTreeUtil;
import org.activiti.engine.IdentityService;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.GroupQuery;
import org.activiti.engine.identity.User;
import org.activiti.engine.identity.UserQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;

/**
 * 组织机构控制层
 * @author Shy
 * @tel    18210175120
 * @date 2016-10-19
 */
@Controller
@RequestMapping("/Org")
public class OrgController {

    private static Logger logger = LoggerFactory.getLogger(OrgController.class);

    // Activiti Identify Service
    private IdentityService identityService;
    
    @RequestMapping(value = "/getBpmOrgZtree")
    @ResponseBody
    public List<ZTreeNode> getBpmOrgZtree() {
        logger.debug("/Org/getOrgZtree");
        try{
			List<ZTreeNode> tree = new ArrayList<ZTreeNode>();
			List<Group> groups=identityService.createGroupQuery().list();
			/*ZTreeNode root = new ZTreeNode();
			root.setId("0");
			root.setPid("");
			root.setName("组织机构");
			root.setType("root"); //虚节点
			tree.add(root);*/
			ZTreeNode node = null;
			for (Group group : groups) {
				node = new ZTreeNode();
				node.setId(group.getId());
				node.setPid(group.getType());
				node.setName(group.getName());
				node.setType("org"); //单位
				tree.add(node);
			}
			return ZTreeUtil.encapsulate(tree);
		} catch(Exception e){
			logger.error(e.toString(), e);
			return null;
		}
    }
    
    
    @RequestMapping(value = "/getBpmUserZtree")
    @ResponseBody
    public List<ZTreeNode> getBpmUserZtree() {
        logger.debug("/Org/getOrgZtree");
        try{
			List<ZTreeNode> tree = new ArrayList<ZTreeNode>();
			List<Group> groups=identityService.createGroupQuery().list();
			ZTreeNode node = null;
			for (Group group : groups) {
				node = new ZTreeNode();
				node.setId(group.getId());
				node.setPid(group.getType());
				node.setName(group.getName());
				node.setType("org"); //单位
				tree.add(node);
				List<User> users =identityService.createUserQuery().memberOfGroup(group.getId()).list();
				if(users.size()>0){
					for (User user:users) {
						node = new ZTreeNode();
						node.setId(user.getId());
						node.setPid(group.getId());
						node.setName(user.getId());
						node.setType("user"); //用户
						tree.add(node);
					}
				}
			}
			return ZTreeUtil.encapsulate(tree);
		} catch(Exception e){
			logger.error(e.toString(), e);
			return null;
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
