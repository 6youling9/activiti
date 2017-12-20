package controller.task;


import common.util.*;
import org.activiti.engine.*;
import org.activiti.engine.form.TaskFormData;
import org.activiti.engine.history.*;
import org.activiti.engine.identity.User;
import org.activiti.engine.identity.UserQuery;
import org.activiti.engine.impl.RepositoryServiceImpl;
import org.activiti.engine.impl.persistence.entity.CommentEntity;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Comment;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import service.task.WmpTaskService;
import service.workflow.WorkflowTraceService;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * @project_name：bonc_ycioc_omp
 * @package_name：AttachmentController
 * @describe：流程管理控制器
 * @creater wangze (1215360909@qq.com)
 * @creat_time 2017-9-6 19:53
 * @changer wangze
 * @change_time 2017-9-6 19:53
 * @remark
 * @version V0.1
 */
@RestController
@RequestMapping(value = "/taskcontroller")
public class TaskController {
    
    protected Logger logger = LoggerFactory.getLogger(getClass());
    
    @Autowired
    private WmpTaskService wmpTaskService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private HistoryService historyService;
	@Autowired
	private FormService formService;
    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private IdentityService identityService;
    @Autowired
    private WorkflowTraceService workflowTraceService;
    
    private static final Integer RUNNING_STATE=1;  //办理中
    private static final Integer FINISH_STATE=2;   //已完结
    private static final Integer REGECTED_STATE=3; //被驳回
    private static final String REJECTED_FLAG="rejected";//驳回标识
    
    /**
     * @Description: 根据各种条件查询审批节点信息
     * @method_name: approval
     * @author wangze
     * @param request
     * @date 2017/9/6 18:41
     * @return List<PageData>
     */
    @RequestMapping(value = "/approval")
    public AppReply<List<PageData>> approval(HttpServletRequest request) {
        logger.info("TaskController.approval:request");
        PageData pd = new PageData(request);
        AppReply<List<PageData>> appReply = new AppReply<List<PageData>>();
        try {
            List<PageData> approval = wmpTaskService.approval(pd);
            appReply.setObj(approval);
        } catch (Exception e) {
            appReply.setCode(AppReply.EORRO_CODE);
            appReply.setMsg("查询列表异常");
            logger.error("查询列表异常");
            e.printStackTrace();
        }
        return appReply;
    }
    
    /**
     * @Description: 待办(已签收或直接发配)
     * @method_name: backlogIsSigned
     * @author wangze
     * @param userId
     * @date 2017/9/6 18:41
     * @return List<PageData>
     */
    @RequestMapping(value = "/backlogIsSigned")
    public AppReply<List<String>> backlogIsSigned(@RequestParam("userId") String userId) {
        logger.info("TaskController.backlogIsSigned:userId=" + userId);
        AppReply<List<String>> appReply = new AppReply<List<String>>();
        PageData pd = new PageData();
        pd.put("isSigned", "isSigned");
        pd.put("userId", userId);
        if (userId == null || "".equals(userId)) {
            appReply.setCode(AppReply.EORRO_CODE);
            appReply.setMsg("用户id为空");
            logger.error("用户id为空");
            return appReply;
        }
        try {
            List<String> approval = wmpTaskService.backlog(pd);
            appReply.setObj(approval);
        } catch (Exception e) {
            appReply.setCode(AppReply.EORRO_CODE);
            appReply.setMsg("查询待办id列表异常");
            logger.error("查询待办id列表异常");
            e.printStackTrace();
        }
        return appReply;
    }
    
    
    /**
     * @Description: 待办(已签收或直接发配)数量
     * @method_name: backlogIsSigned
     * @author wangze
     * @param userId
     * @date 2017/9/6 18:41
     * @return List<PageData>
     */
    @RequestMapping(value = "/backlogIsSignedCount")
    public AppReply<Long> backlogIsSignedCount(@RequestParam("userId") String userId) {
        logger.info("TaskController.backlogIsSigned:userId=" + userId);
        AppReply<Long> appReply = new AppReply<Long>();
        PageData pd = new PageData();
        pd.put("isSigned", "isSigned");
        pd.put("userId", userId);
        if (userId == null || "".equals(userId)) {
            appReply.setCode(AppReply.EORRO_CODE);
            appReply.setMsg("用户id为空");
            logger.error("用户id为空");
            return appReply;
        }
        try {
            List<String> approval = wmpTaskService.backlog(pd);
            if(approval!=null){
            	appReply.setObj(approval.size());
            }else{
            	appReply.setObj(0);
            }
        } catch (Exception e) {
            appReply.setCode(AppReply.EORRO_CODE);
            appReply.setMsg("查询待办数量异常");
            logger.error("查询待办数量异常");
            e.printStackTrace();
        }
        return appReply;
    }
    
    /**
     * @Description: 待办(未签收)
     * @method_name: backlogNotSigned
     * @author wangze
     * @param userId
     * @date 2017/9/6 18:41
     * @return List<PageData>
     */
    @RequestMapping(value = "/backlogNotSigned")
    public AppReply<List<String>> backlogNotSigned(@RequestParam("userId") String userId) {
        logger.info("TaskController.backlogNotSigned:userId=" + userId);
        AppReply<List<String>> appReply = new AppReply<List<String>>();
        PageData pd = new PageData();
        pd.put("notSigned", "notSigned");
        pd.put("userId", userId);
        if (userId == null || "".equals(userId)) {
            appReply.setCode(AppReply.EORRO_CODE);
            appReply.setMsg("用户id为空");
            logger.error("用户id为空");
            return appReply;
        }
        try {
            List<String> approval = wmpTaskService.backlog(pd);
            appReply.setObj(approval);
        } catch (Exception e) {
            appReply.setCode(AppReply.EORRO_CODE);
            appReply.setMsg("查询待办id列表异常");
            logger.error("查询待办id列表异常");
            e.printStackTrace();
        }
        return appReply;
    }
    
    /**
     * 获取代办数量
     * @param userId
     * @return
     */
    @RequestMapping(value = "/backlogNotSignedCount")
    public AppReply<Long> backlogNotSignedCount(@RequestParam("userId") String userId) {
        logger.info("TaskController.backlogNotSigned:userId=" + userId);
        AppReply<Long> appReply = new AppReply<Long>();
        PageData pd = new PageData();
        pd.put("notSigned", "notSigned");
        pd.put("userId", userId);
        if (userId == null || "".equals(userId)) {
            appReply.setCode(AppReply.EORRO_CODE);
            appReply.setMsg("用户id为空");
            logger.error("用户id为空");
            return appReply;
        }
        try {
            Long count = taskService.createTaskQuery().taskCandidateOrAssigned(userId).count();
            appReply.setObj(count);
        } catch (Exception e) {
            appReply.setCode(AppReply.EORRO_CODE);
            appReply.setMsg("查询待办数量异常");
            logger.error("查询待办数量异常");
            e.printStackTrace();
        }
        return appReply;
    }
    
    /**
     * @Description: 签收操作
     * @method_name: signfor
     * @author wangze
     * @param userId
     * @date 2017/9/6 18:41
     * @return List<PageData>
     */
    @RequestMapping(value = "/signfor")
    public AppReply<List<String>> signfor(@RequestParam("userId") String userId, @RequestParam("taskId") String taskId) {
        logger.info("TaskController.backlogNotSigned:userId=" + userId);
        AppReply<List<String>> appReply = new AppReply<List<String>>();
        if (userId == null || "".equals(userId)) {
            appReply.setCode(AppReply.EORRO_CODE);
            appReply.setMsg("用户id为空");
            logger.error("用户id为空");
            return appReply;
        }
        try {
            taskService.claim(taskId, userId);
            appReply.setCode(AppReply.SUCCESS_CODE);
        } catch (Exception e) {
            appReply.setCode(AppReply.EORRO_CODE);
            appReply.setMsg("签收失败");
            logger.error("查询待办id列表异常");
            e.printStackTrace();
        }
        return appReply;
    }
    
    /**
     * @Description: 已办
     * @method_name: haveDone
     * @author wangze
     * @param userId
     * @date 2017/9/6 18:41
     * @return List<PageData>
     */
    @RequestMapping(value = "/haveDone")
    public AppReply<List<String>> haveDone(@RequestParam("userId") String userId) {
        logger.info("TaskController.haveDone:userId=" + userId);
        AppReply<List<String>> appReply = new AppReply<List<String>>();
        PageData pd = new PageData();
        pd.put("userId", userId);
        if (userId == null || "".equals(userId)) {
            appReply.setCode(AppReply.EORRO_CODE);
            appReply.setMsg("用户id为空");
            logger.error("用户id为空");
            return appReply;
        }
        try {
            List<String> approval = wmpTaskService.haveDone(pd);
            appReply.setObj(approval);
        } catch (Exception e) {
            appReply.setCode(AppReply.EORRO_CODE);
            appReply.setMsg("查询已办id列表异常");
            logger.error("查询已办id列表异常");
            e.printStackTrace();
        }
        return appReply;
    }
    
    @RequestMapping(value = "/haveDoneCount")
    public AppReply<Long> haveDoneCount(@RequestParam("userId") String userId) {
        logger.info("TaskController.haveDone:userId=" + userId);
        AppReply<Long> appReply = new AppReply<Long>();
        PageData pd = new PageData();
        pd.put("userId", userId);
        if (userId == null || "".equals(userId)) {
            appReply.setCode(AppReply.EORRO_CODE);
            appReply.setMsg("用户id为空");
            logger.error("用户id为空");
            return appReply;
        }
        try {
            //Long conut = historyService.createHistoricTaskInstanceQuery().taskAssignee(userId).count();
            List<String> approval = wmpTaskService.haveDone(pd);
            appReply.setObj(approval.size());
        } catch (Exception e) {
            appReply.setCode(AppReply.EORRO_CODE);
            appReply.setMsg("查询已办数量异常");
            logger.error("查询已办数量异常");
            e.printStackTrace();
        }
        return appReply;
    }
    
    
    
    /**
     * @Description: 已提交
     * @method_name: submitted
     * @author wangze
     * @param userId
     * @date 2017/9/6 18:41
     * @return List<PageData>
     */
    @RequestMapping(value = "/submitted")
    public AppReply<List<String>> submitted(@RequestParam("userId") String userId) {
        logger.info("TaskController.submitted:userId=" + userId);
        AppReply<List<String>> appReply = new AppReply<List<String>>();
        List<String> taskIds = new ArrayList<String>();
        if (userId == null || "".equals(userId)) {
            appReply.setCode(AppReply.EORRO_CODE);
            appReply.setMsg("用户id为空");
            logger.error("用户id为空");
            return appReply;
        }
        List<HistoricProcessInstance> list = historyService.createHistoricProcessInstanceQuery()
                .startedBy(userId)
                .list();
        for (HistoricProcessInstance processInstance : list) {
            taskIds.add(processInstance.getBusinessKey());
        }
        appReply.setObj(taskIds);
        return appReply;
    }
    /**
     * @Description: 已提交 数量
     * @method_name: submitted
     * @author shy
     * @param userId
     * @date 2017/9/6 18:41
     * @return AppReply<Long>
     */
    @RequestMapping(value = "/submittedCount")
    public AppReply<Long> submittedCount(@RequestParam("userId") String userId) {
        logger.info("TaskController.submitted:userId=" + userId);
        AppReply<Long> appReply = new AppReply<Long>();
        if (userId == null || "".equals(userId)) {
            appReply.setCode(AppReply.EORRO_CODE);
            appReply.setMsg("用户id为空");
            logger.error("用户id为空");
            return appReply;
        }
        Long submittedCount = historyService.createHistoricProcessInstanceQuery()
                .startedBy(userId).count();
        appReply.setObj(submittedCount);
        return appReply;
    }
    
    /**
     * @Description: 已完成（参与者）
     * @method_name: closed
     * @author wangze
     * @param userId
     * @date 2017/9/6 18:41
     * @return List<PageData>
     */
    @RequestMapping(value = "/finished")
    public AppReply<List<String>> finished(@RequestParam("userId") String userId) {
        logger.info("TaskController.submitted:userId=" + userId);
        AppReply<List<String>> appReply = new AppReply<List<String>>();
        List<String> businessIds = new ArrayList<String>();
        if (StringUtils.isEmpty(userId)) {
            appReply.setCode(AppReply.EORRO_CODE);
            appReply.setMsg("用户id为空");
            logger.error("用户id为空");
            return appReply;
        }
        List<HistoricProcessInstance> list = historyService.createHistoricProcessInstanceQuery().involvedUser(userId).finished().list();
        for (HistoricProcessInstance processInstance : list) {
            businessIds.add(processInstance.getBusinessKey());
        }
        appReply.setObj(businessIds);
        return appReply;
    }
    
    /**
     * @Description: 已关闭数量（参与者）
     * @method_name: closed
     * @author wangze
     * @param userId
     * @date 2017/9/6 18:41
     * @return List<PageData>
     */
    @RequestMapping(value = "/finishedCount")
    public AppReply<Long> finishedCount(@RequestParam("userId") String userId) {
        logger.info("TaskController.finishedCount:userId=" + userId);
        AppReply<Long> appReply = new AppReply<Long>();
        if (StringUtils.isEmpty(userId)) {
            appReply.setCode(AppReply.EORRO_CODE);
            appReply.setMsg("用户id为空");
            logger.error("用户id为空");
            return appReply;
        }
        appReply.setObj(historyService.createHistoricProcessInstanceQuery().involvedUser(userId).finished().count());
        return appReply;
    }
    
    /**
     * @Description: 已完成（发起者）
     * @method_name: finishedByStarter
     * @author wangze
     * @param userId
     * @date 2017/9/6 18:41
     * @return List<PageData>
     */
    @RequestMapping(value = "/finishedByStarter")
    public AppReply<List<String>> finishedByStarter(@RequestParam("userId") String userId) {
        logger.info("TaskController.finishedByStater:userId=" + userId);
        AppReply<List<String>> appReply = new AppReply<List<String>>();
        List<String> businessIds = new ArrayList<String>();
        if (StringUtils.isEmpty(userId)) {
            appReply.setCode(AppReply.EORRO_CODE);
            appReply.setMsg("用户id为空");
            logger.error("用户id为空");
            return appReply;
        }
        List<HistoricProcessInstance> list = historyService.createHistoricProcessInstanceQuery().startedBy(userId).finished().list();
        for (HistoricProcessInstance processInstance : list) {
            businessIds.add(processInstance.getBusinessKey());
        }
        appReply.setObj(businessIds);
        return appReply;
    }
    
    /**
     * @Description: 已关闭数量（参与者）
     * @method_name: finishedByStarterCount
     * @author wangze
     * @param userId
     * @date 2017/9/6 18:41
     * @return List<PageData>
     */
    @RequestMapping(value = "/finishedByStarterCount")
    public AppReply<Long> finishedByStarterCount(@RequestParam("userId") String userId) {
        logger.info("TaskController.finishedByStaterCount:userId=" + userId);
        AppReply<Long> appReply = new AppReply<Long>();
        if (StringUtils.isEmpty(userId)) {
            appReply.setCode(AppReply.EORRO_CODE);
            appReply.setMsg("用户id为空");
            logger.error("用户id为空");
            return appReply;
        }
        appReply.setObj(historyService.createHistoricProcessInstanceQuery().startedBy(userId).finished().count());
        return appReply;
    }
    
    /**
     * @Description: 已驳回
     * @method_name: rejected
     * @author wangze
     * @param userId
     * @date 2017/9/6 18:41
     * @return List<PageData>
     */
    @RequestMapping(value = "/rejected")
    public AppReply<List<String>> rejected(@RequestParam("userId") String userId) {
        logger.info("TaskController.rejected:userId=" + userId);
        AppReply<List<String>> appReply = new AppReply<List<String>>();
        PageData pd = new PageData();
        pd.put("rejected", "rejected");
        pd.put("userId", userId);
        if (userId == null || "".equals(userId)) {
            appReply.setCode(AppReply.EORRO_CODE);
            appReply.setMsg("用户id为空");
            logger.error("用户id为空");
            return appReply;
        }
        try {
            List<String> approval = wmpTaskService.backlog(pd);
            appReply.setObj(approval);
        } catch (Exception e) {
            appReply.setCode(AppReply.EORRO_CODE);
            appReply.setMsg("查询已驳回id列表异常");
            logger.error("查询已驳回id列表异常");
            e.printStackTrace();
        }
        return appReply;
    }
    /**
     * 驳回数量
     * @param userId
     * @return
     */
    @RequestMapping(value = "/rejectedCount")
    public AppReply<Long> rejectedCount(@RequestParam("userId") String userId) {
        logger.info("TaskController.rejectedCount:userId=" + userId);
        AppReply<Long> appReply = new AppReply<Long>();
        PageData pd = new PageData();
        pd.put("rejected", "rejected");
        pd.put("userId", userId);
        if (userId == null || "".equals(userId)) {
            appReply.setCode(AppReply.EORRO_CODE);
            appReply.setMsg("用户id为空");
            logger.error("用户id为空");
            return appReply;
        }
        try {
            List<String> approval = wmpTaskService.backlog(pd);
            if(approval!=null){
            	appReply.setObj(approval.size());
            }else{
            	appReply.setObj(0);
            }
        } catch (Exception e) {
            appReply.setCode(AppReply.EORRO_CODE);
            appReply.setMsg("查询已驳回数量异常");
            logger.error("查询已驳回数量异常");
            e.printStackTrace();
        }
        return appReply;
    }
    
    
    /**
     * @Description: 查看详情(当前的)
     * @method_name: detailsForCurrent
     * @author wangze
     * @param executionId
     * @date 2017/9/6 18:41
     * @return List<PageData>
     */
    @RequestMapping(value = "/detailsForCurrent")
    public AppReply<Object> detailsForCurrent(@RequestParam("executionId") String executionId) {
        logger.info("TaskController.details:executionId=" + executionId);
        AppReply<Object> appReply = new AppReply<Object>();
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        if (executionId == null || "".equals(executionId)) {
            appReply.setCode(AppReply.EORRO_CODE);
            appReply.setMsg("流程id为空");
            logger.error("流程id为空");
            return appReply;
        }
        try {
            //获取流程的所有节点的信息
            List<Map<String, Object>> maps = workflowTraceService.traceProcess(executionId);
            //获取流程的所有评价意见
            List<Comment> comments = taskService.getProcessInstanceComments(executionId);
            //将评价意见匹配到对应节点上
            for (Map<String, Object> map : maps) {
                for (Comment comment : comments) {
                    if (map.get("taskId") != null && ((String) map.get("taskId")).equals(comment.getTaskId())) {
                        map.put("comment", Object2Map.Obj2Map(comment, 1));
                    }
                }
                result.add(map);
            }
            appReply.setObj(result);
        } catch (Exception e) {
            appReply.setCode(AppReply.EORRO_CODE);
            appReply.setMsg("查询事项详情异常");
            logger.error("查询事项详情异常");
            e.printStackTrace();
        }
        return appReply;
    }
    
    /**
     * @Description: 获取流程最新的评论信息
     * @method_name: commentInfo
     * @author wangze
     * @param pId
     * @date 2017/9/6 18:41
     * @return List<PageData>
     */
    @RequestMapping(value = "/commentInfo")
    public AppReply<Object> commentInfo(@RequestParam("pId") String pId) {
        logger.info("TaskController.CommentInfo:pId=" + pId);
        AppReply<Object> appReply = new AppReply<Object>();
        if (StringUtil.isEmpty(pId)) {
            appReply.setCode(AppReply.EORRO_CODE);
            appReply.setMsg("流程id为空");
            logger.error("流程id为空");
            return appReply;
        }
        try {
            //获取流程的所有节点的信息
            List<HistoricActivityInstance> activitys = historyService.createHistoricActivityInstanceQuery()
                    .executionId(pId).orderByHistoricActivityInstanceId().desc().list();
            //获取该流程的发起者
            String startUserId = historyService.createHistoricProcessInstanceQuery().processInstanceId(pId)
                    .singleResult().getStartUserId();
            //当前意见
            List<Comment> comments = null;
            //从倒数第二个节点找起，找到taskId不为空的节点
            for(int i=activitys.size()-2;i>=0;i--){
                String taskId = activitys.get(i).getTaskId();
                if(StringUtil.isNotEmpty(taskId)){
                    //增加一个判断，如果该节点为驳回节点就跳过
                    if(StringUtil.equals(startUserId,activitys.get(i).getAssignee())){
                        continue;
                    }else {
                        comments = taskService.getTaskComments(taskId);
                    }
                }
            }
            appReply.setObj(comments);
        } catch (Exception e) {
            appReply.setCode(AppReply.EORRO_CODE);
            appReply.setMsg("获取流程最新的评论信息异常");
            logger.error("获取流程最新的评论信息异常");
            e.printStackTrace();
        }
        return appReply;
    }
    
    /**
     * @Description: 查看详情(所有的)
     * @method_name: detailsForAll
     * @author wangze
     * @param executionId
     * @date 2017/9/6 18:41
     * @return List<PageData>
     */
    @RequestMapping(value = "/detailsForAll")
    public AppReply<Object> detailsForAll(@RequestParam("executionId") String executionId) {
        logger.info("TaskController.details:executionId=" + executionId);
        AppReply<Object> appReply = new AppReply<Object>();
        List<Map<String, Object>> taskInfos = new ArrayList<Map<String, Object>>();
        Map<String, Object> resultMap = new HashMap<String, Object>();
        if (executionId == null || "".equals(executionId)) {
            appReply.setCode(AppReply.EORRO_CODE);
            appReply.setMsg("流程id为空");
            logger.error("流程id为空");
            return appReply;
        }
        try {
            //获取流程的所有节点的信息
            List<HistoricActivityInstance> activitys = historyService.createHistoricActivityInstanceQuery()
                    .executionId(executionId).orderByHistoricActivityInstanceStartTime().asc().list();
            
            //获取流程的所有用户操作
            List<Map<String, Object>> userOperates = getUserOperate(executionId);
    
            //获取流程的所有评价意见
            List<Comment> comments = taskService.getProcessInstanceComments(executionId);
    
            //获取流程的所有节点参数
            List<HistoricVariableInstance> variables = historyService.createHistoricVariableInstanceQuery().executionId(executionId).list();
    
            //获取需要所有节点画框的信息
            List<Map<String, Object>> activitieInfos = getActivitieInfoAll(executionId);
            
            for (HistoricActivityInstance activity : activitys) {
                if("exclusiveGateway".equals(activity.getActivityType())){
                    continue;
                }
                Map<String, Object> activitytMap = new HashMap<String, Object>();
                if(StringUtil.isNotEmpty(activity.getAssignee())) {
                    User assignee = identityService.createUserQuery().userId(activity.getAssignee()).singleResult();
                    activitytMap.put("assignee", assignee!=null?assignee.getFirstName():"");
                }
                activitytMap.put("assigneeId", activity.getAssignee());
                activitytMap.put("taskId", activity.getTaskId());
                if(StringUtils.isNotEmpty(activity.getTaskId())&& activity.getEndTime()==null){
                    TaskFormData taskFormData = formService.getTaskFormData(activity.getTaskId());
                    activitytMap.put("formKey", taskFormData.getFormKey());
                }else{
                	activitytMap.put("formKey", "");
                }
                activitytMap.put("type", activity.getActivityType());
                activitytMap.put("name", activity.getActivityName());
                activitytMap.put("activitiId", activity.getActivityId());
                activitytMap.put("startTime", DateUtil.getTimeStr(activity.getStartTime()));
                activitytMap.put("endTime", activity.getEndTime()==null?"":DateUtil.getTimeStr(activity.getEndTime()));
    
                //将任务变量匹配到对应节点上
                for (HistoricVariableInstance variable : variables) {
                    if (activity.getTaskId()!=null && activity.getTaskId().equals(variable.getTaskId())) {
                        activitytMap.put(variable.getVariableName(), variable.getValue());
                    }
                }
    
                //将用户操作匹配到对应节点上
                for (Map<String, Object> userOperate : userOperates) {
                    if (activity.getTaskId()!=null && activity.getTaskId().equals((String)userOperate.get("taskId"))) {
                        activitytMap.put("operate", userOperate.get("operate"));
                        break;
                    }
                }
                
                //将评价意见匹配到对应节点上
                for (Comment comment : comments) {
                    if (activity.getTaskId()!=null && activity.getTaskId().equals(comment.getTaskId())) {
                        activitytMap.put("comment", ((CommentEntity) comment).getMessage());
                        activitytMap.put("time", DateUtil.getTimeStr(((CommentEntity) comment).getTime()));
                        activitytMap.put("appraiser", activity.getAssignee());
                    }
                }
    
                //将评价画框匹配到对应节点上
                for(Map<String, Object> activitieInfo:activitieInfos){
                    if(activity.getActivityId()!=null && activity.getActivityId().equals(activitieInfo.get("activityId"))){
                        activitytMap.put("frameInfo",activitieInfo);
                        break;
                    }
                }
                taskInfos.add(activitytMap);
            }
            resultMap.put("taskInfos", taskInfos);
            appReply.setObj(resultMap);
        } catch (Exception e) {
            appReply.setCode(AppReply.EORRO_CODE);
            appReply.setMsg("查询事项详情异常");
            logger.error("查询事项详情异常");
            e.printStackTrace();
        }
        return appReply;
    }
    /**获取需要各节点人员操作**/
    private List<Map<String, Object>> getUserOperate(String executionId) {
        //用户信息查询
        UserQuery userQuery = identityService.createUserQuery();
        //获取历史变量详情
        List<HistoricDetail> variables = historyService.createHistoricDetailQuery().executionId(executionId).list();
        //获取流程的所有节点的信息
        List<HistoricActivityInstance> activitys = historyService.createHistoricActivityInstanceQuery()
                .executionId(executionId).orderByHistoricActivityInstanceId().desc().list();
        //获取该流程的发起者
        String startUserId = historyService.createHistoricProcessInstanceQuery().processInstanceId(executionId)
                .singleResult().getStartUserId();
        //储存用户操作的容器
        List<Map<String,Object>> resultList = new ArrayList<Map<String,Object>>();
        //从倒数第二个节点找起，找到taskId不为空的节点
        for(HistoricActivityInstance activity:activitys){
            String taskId = activity.getTaskId();
            if(StringUtil.isNotEmpty(taskId)){
                String activityId = activity.getId();
                Map<String,Object> resultMap = new HashMap<String,Object>();
                for(HistoricDetail variable:variables){
                    if(activityId.equals(variable.getActivityInstanceId())){
                        resultMap.put("taskId",taskId);//代理人id
                        resultMap.put("operate",((HistoricVariableUpdate)variable).getValue());//操作
                    }
                }
                resultList.add(resultMap);
            }
        }
        return resultList;
    }
    /**获取需要所有节点画框的信息**/
    private List<Map<String, Object>> getActivitieInfoAll(String executionId) {
        ProcessDefinitionEntity processDefinitionEntity = null;
        //获取当前流程实例
        ExecutionEntity executionEntityion = (ExecutionEntity) runtimeService.createExecutionQuery().executionId(executionId).singleResult();
        //如果当前没有，去历史表中查
        if(executionEntityion == null){
            HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery().processInstanceId(executionId).singleResult();
            //获取流程定义
            processDefinitionEntity = (ProcessDefinitionEntity) ((RepositoryServiceImpl) repositoryService)
                    .getDeployedProcessDefinition(historicProcessInstance.getProcessDefinitionId());
        }else {
            //获取当前的流程定义
            processDefinitionEntity = (ProcessDefinitionEntity) ((RepositoryServiceImpl) repositoryService)
                    .getDeployedProcessDefinition(executionEntityion.getProcessDefinitionId());
        }
        //获取当前节点的定义
        List<ActivityImpl> activities = processDefinitionEntity.getActivities();
        List<Map<String, Object>> activitieInfos = new ArrayList<Map<String, Object>>();
        for (ActivityImpl activitie : activities) {
            Map<String, Object> activitieInfo = new HashMap<String, Object>();
            activitieInfo.put("activityId", activitie.getId());
            activitieInfo.put("height", activitie.getHeight());
            activitieInfo.put("width", activitie.getWidth());
            activitieInfo.put("y", activitie.getY());
            activitieInfo.put("x", activitie.getX());
            if (executionEntityion!=null&&StringUtil.equals(activitie.getId(), executionEntityion.getCurrentActivityId())) {
                activitieInfo.put("currentActiviti", true);
            }else {
                activitieInfo.put("currentActiviti", false);
            }
            activitieInfos.add(activitieInfo);
        }
        return activitieInfos;
    }
    /**获取需要当前节点画框的信息**/
    @SuppressWarnings("unused")
	private Map<String, Object> getActivitieInfo(String executionId) {
        //获取当前流程实例
        ExecutionEntity executionEntityion = (ExecutionEntity) runtimeService.createExecutionQuery().executionId(executionId).singleResult();
        //获取当前的流程定义
        ProcessDefinitionEntity processDefinitionEntity = (ProcessDefinitionEntity) ((RepositoryServiceImpl) repositoryService)
                .getDeployedProcessDefinition(executionEntityion.getProcessDefinitionId());
        //获取当前节点的定义
        List<ActivityImpl> activities = processDefinitionEntity.getActivities();
        Map<String, Object> activitieInfo = new HashMap<String, Object>();
        activitieInfo.put("currentActiviti", false);
        for (ActivityImpl activitie : activities) {
            if (StringUtil.equals(activitie.getId(), executionEntityion.getCurrentActivityId())) {
                activitieInfo.put("height", activitie.getHeight());
                activitieInfo.put("width", activitie.getWidth());
                activitieInfo.put("y", activitie.getY());
                activitieInfo.put("x", activitie.getX());
                activitieInfo.put("currentActiviti", true);
                break;
            }
        }
        return activitieInfo;
    }
    
    /**
     * @Description: 批量通过
     * @method_name: doAgreeAll
     * @author wangze
     * @param request
     * @date 2017/9/6 18:41
     * @return List<PageData>
     */
    @SuppressWarnings("unchecked")
	@RequestMapping(value = "/doAgreeAll")
    public AppReply<Object> doAgreeAll(HttpServletRequest request) {
        PageData pd = new PageData(request);
        String[] taskIds = (String[]) pd.get("taskIds");
        pd.remove("taskIds");
        
        logger.info("TaskController.doAgreeAll:taskIds=" + taskIds.toString());
        AppReply<Object> appReply = new AppReply<Object>();
        if (taskIds == null || taskIds.length == 0) {
            appReply.setCode(AppReply.EORRO_CODE);
            appReply.setMsg("节点id为空");
            logger.error("节点id为空");
            return appReply;
        }
        try {
            for (String taskId : taskIds) {
                taskService.complete(taskId, pd);
            }
            appReply.setCode(AppReply.SUCCESS_CODE);
        } catch (Exception e) {
            appReply.setCode(AppReply.EORRO_CODE);
            appReply.setMsg("批量通过异常");
            logger.error("批量通过异常");
            e.printStackTrace();
        }
        return appReply;
    }
    
    /**
     * @Description: 启动流程
     * @method_name: startWorkflow
     * @author wangze
     * @param userId
     * @param pdId
     * @param businessId
     * @param request
     * @date 2017/9/6 18:41
     * @return List<PageData>
     */
    @RequestMapping(value = "/startWorkflow")
    public AppReply<Object> startWorkflow(@RequestParam("userId") String userId,
                                          @RequestParam("pdId") String pdId,
                                          @RequestParam("businessId") String businessId, HttpServletRequest request) {
        logger.info("TaskController.startWorkflow:PDId=" + pdId + ";businessId=" + businessId);
        AppReply<Object> appReply = new AppReply<Object>();
        if (StringUtils.isEmpty(userId)||"null".equals(userId)) {
            appReply.setCode(AppReply.EORRO_CODE);
            appReply.setMsg("未获得申请人信息");
            logger.error("发起人id为空");
            return appReply;
        }
        if (StringUtils.isEmpty(pdId)||"null".equals(pdId)) {
            appReply.setCode(AppReply.EORRO_CODE);
            appReply.setMsg("事项未设定流程id");
            logger.error("流程定义id为空");
            return appReply;
        }
        if (StringUtils.isEmpty(businessId)||"null".equals(businessId)) {
            appReply.setCode(AppReply.EORRO_CODE);
            appReply.setMsg("缺少业务id");
            logger.error("业务id为空");
            return appReply;
        }
        try {
            // 用来设置启动流程的人员ID，引擎会自动把用户ID保存到activiti:initiator中
            identityService.setAuthenticatedUserId(userId);
            // 从request中读取参数然后转换
            PageData taskVar= new PageData();
            taskVar.put("applyUserId", userId);
            Map<String, String[]> parameterMap = request.getParameterMap();
            Set<Map.Entry<String, String[]>> entrySet = parameterMap.entrySet();
            for (Map.Entry<String, String[]> entry : entrySet) {
                String key = entry.getKey();
                if (StringUtils.defaultString(key).startsWith("fp_")) {
                    taskVar.put(key.split("_")[1], entry.getValue()[0]);
                }
            }
            logger.debug("task parameters: {}", taskVar);
            @SuppressWarnings("unchecked")
			ProcessInstance processInstance = runtimeService.startProcessInstanceById(pdId, businessId, taskVar);
            appReply.setCode(AppReply.SUCCESS_CODE);
            appReply.setObj(processInstance.getId());
        } catch (Exception e) {
            appReply.setCode(AppReply.EORRO_CODE);
            appReply.setMsg("流程启动异常");
            logger.error("查询事项详情异常");
            e.printStackTrace();
        }
        return appReply;
    }
    
    /**
     *
     * @Description: 批量办理
     * @method_name: completeAllTask
     * @author Shy
     * @param pIds
     * @param userId
     * @date 2017/11/07 10:48
     * @return AppReply<String>
     */
    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/completeAll")
    @Rollback()
    public AppReply<String> completeAllTask(@RequestParam("pIds") String pIds,
                                            @RequestParam("userId") String userId,
                                            @RequestParam(value="message",required=false) String message,
                                            HttpServletRequest request) {
        AppReply<String> reply= new AppReply<String>();
        PageData taskVar= new PageData();
        ProcessInstance pi=null;
        HashMap<String,Integer> result= new HashMap<>(16);
        String[] pIdsArr=null;
        if(pIds!=null){
        	pIdsArr=pIds.split(",");
        }
        try {
            if(pIdsArr==null || pIdsArr.length==0){
                reply.setCode(AppReply.EORRO_CODE);
                reply.setMsg("流程实例id为空");
                return reply;
            }
            if(StringUtils.isEmpty(userId)){
                reply.setCode(AppReply.EORRO_CODE);
                reply.setMsg("用户id为空");
                return reply;
            }
            //获取任务id
            List<Map<String,String>> taskAndPids = new ArrayList<Map<String,String>>();
            Map<String,String> map = null;
            for(String pId:pIdsArr) {
                map= new HashMap<String,String>();
                String taskId=taskService.createTaskQuery().processInstanceId(pId).taskCandidateOrAssigned(userId).singleResult().getId();
                map.put("taskId",taskId);
                map.put("pId",pId);
                taskAndPids.add(map);
            }
            if(taskAndPids.size()==0){
                reply.setCode(AppReply.EORRO_CODE);
                reply.setMsg("任务id查询失败");
                return reply;
            }
            // 从request中读取参数然后转换
            Map<String, String[]> parameterMap = request.getParameterMap();
            Set<Map.Entry<String, String[]>> entrySet = parameterMap.entrySet();
            for (Map.Entry<String, String[]> entry : entrySet) {
                String key = entry.getKey();
                if (StringUtils.defaultString(key).startsWith("fp_")) {
                    taskVar.put(key.split("_")[1], entry.getValue()[0]);
                }
            }
            logger.debug("task parameters: {}", taskVar);
            
            identityService.setAuthenticatedUserId(userId);
            Map<String, Object> pvar=new HashMap<String, Object>();//专用来存流程变量
            pvar.put("check",taskVar.getString("check"));
            
            for(Map<String,String> taskAndPid:taskAndPids) {
                String taskId = taskAndPid.get("taskId");
                String pId = taskAndPid.get("pId");
                if (StringUtils.isNotEmpty(message)) {
                    taskService.addComment(taskId,pId, message);
                }
                //设置代理人
                taskService.setAssignee(taskId, userId);
                //办理任务
                taskService.setVariablesLocal(taskId,taskVar);
                taskService.complete(taskId, pvar);
    
                pi = runtimeService//表示正在执行的流程实例和执行对象
                        .createProcessInstanceQuery()//创建流程实例查询
                        .processInstanceId(pId)//使用流程实例ID查询
                        .singleResult();
                
                //taskService.complete(taskId, taskVar);
                //pi = runtimeService.createProcessInstanceQuery().processInstanceId(pId).singleResult();
                if(pi==null){
                    result.put(pId,FINISH_STATE);
                }else{
                    if(REJECTED_FLAG.equals(pi.getActivityId())){
                        String startUserId = historyService.createHistoricProcessInstanceQuery().processInstanceId(pId).singleResult().getStartUserId();
                        String nextTaskId=taskService.createTaskQuery().processInstanceId(pId).taskAssignee(startUserId).singleResult().getId();
                        Map<String, Object> var=new HashMap<String, Object>();
                        var.put("check", "关闭");
                        taskService.addComment(nextTaskId, pId, "驳回关闭");
                        //设置代理人
                        taskService.setAssignee(nextTaskId, startUserId);
                        //办理任务
                        taskService.complete(nextTaskId, var);
                        result.put(pId,REGECTED_STATE);
                    }else{
                        result.put(pId,RUNNING_STATE);
                    }
                }
            }
            reply.setCode(AppReply.SUCCESS_CODE);
            reply.setObj(result);
            reply.setMsg("办理成功  obj是一个Map结构返回值说明：1、办理中  2、已完结  3、已驳回");
            return reply;
        }catch (Exception e) {
            e.printStackTrace();
            reply.setCode(AppReply.EORRO_CODE);
            reply.setMsg("办理失败");
            return reply;
        } finally {
            identityService.setAuthenticatedUserId(null);
        }
    }
    
    /**
     *
     * @Description: 办理
     * @method_name: completeTask
     * @author wangze
     * @param pId
     * @param userId
     * @date 2017/9/6 18:41
     * @return List<PageData>
     */
    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/complete")
    public AppReply<String> completeTask(@RequestParam("pId") String pId,
                                         @RequestParam("userId") String userId,
                                         @RequestParam(value="message",required=false) String message,
                                         HttpServletRequest request) {
        AppReply<String> reply= new AppReply<String>();
        PageData taskVar= new PageData();
        try {
            if(StringUtils.isEmpty(pId)){
                reply.setCode(AppReply.EORRO_CODE);
                reply.setMsg("流程实例id为空");
                return reply;
            }
            if(StringUtils.isEmpty(userId)){
                reply.setCode(AppReply.EORRO_CODE);
                reply.setMsg("用户id为空");
                return reply;
            }
            //获取任务id
            String taskId=taskService.createTaskQuery().processInstanceId(pId).taskCandidateOrAssigned(userId).singleResult().getId();
            if(StringUtils.isEmpty(taskId)){
            	 reply.setCode(AppReply.EORRO_CODE);
                 reply.setMsg("任务id查询失败");
                 return reply;
            }
            // 从request中读取参数然后转换
            Map<String, String[]> parameterMap = request.getParameterMap();
            Set<Map.Entry<String, String[]>> entrySet = parameterMap.entrySet();
            for (Map.Entry<String, String[]> entry : entrySet) {
                String key = entry.getKey();
                if (StringUtils.defaultString(key).startsWith("fp_")) {
                    taskVar.put(key.split("_")[1], entry.getValue()[0]);
                }
            }
            logger.debug("task parameters: {}", taskVar);
            identityService.setAuthenticatedUserId(userId);
            
            if(StringUtils.isNotEmpty(message)){
                taskService.addComment(taskId, pId, message);
            }
            //设置代理人
            taskService.setAssignee(taskId, userId);
            //办理任务
            taskService.setVariablesLocal(taskId,taskVar);
            Map<String, Object> pvar=new HashMap<String, Object>();//专用来存流程变量
            pvar.put("check",taskVar.getString("check"));
            taskService.complete(taskId, pvar);
            
    	    ProcessInstance pi = runtimeService//表示正在执行的流程实例和执行对象
                    .createProcessInstanceQuery()//创建流程实例查询
                    .processInstanceId(pId)//使用流程实例ID查询
                    .singleResult();
            if(pi==null){
     	    	reply.setObj(FINISH_STATE);
     	    }else{
     	    	if(REJECTED_FLAG.equals(pi.getActivityId())){
     	    		String startUserId = historyService.createHistoricProcessInstanceQuery().processInstanceId(pId).singleResult().getStartUserId();
     	    		String nextTaskId=taskService.createTaskQuery().processInstanceId(pId).taskAssignee(startUserId).singleResult().getId();
     	    		Map<String, Object> var=new HashMap<String, Object>();
     	    		var.put("check", "关闭");
     	    		taskService.addComment(nextTaskId, pId, "驳回关闭");
     	    	    //设置代理人
     	            taskService.setAssignee(nextTaskId, startUserId);
     	            //办理任务
     	            taskService.complete(nextTaskId, var);
     	            
     	    		reply.setObj(REGECTED_STATE);
     	    	}else{
     	    		reply.setObj(RUNNING_STATE);
     	    	}
     	    }
            reply.setCode(AppReply.SUCCESS_CODE);
            reply.setMsg("办理成功  obj返回值说明：1、办理中  2、已完结  3、已驳回");
            return reply;
        }catch (Exception e) {
            e.printStackTrace();
            reply.setCode(AppReply.EORRO_CODE);
            reply.setMsg("办理失败");
            return reply;
        } finally {
            identityService.setAuthenticatedUserId(null);
        }
    }
    
    /**
     * @Description: 获取某一流程当前办理人的id
     * @method_name: detailsForAll
     * @author wangze
     * @param executionId
     * @date 2017/9/6 18:41
     * @return List<PageData>
     */
    @RequestMapping(value = "/getApplayer")
    public AppReply<Object> getApplayer(@RequestParam("executionId") String executionId) {
        logger.info("TaskController.details:executionId=" + executionId);
        AppReply<Object> appReply = new AppReply<Object>();
        Map<String, Object> resultMap = new HashMap<String, Object>();
        if (executionId == null || "".equals(executionId)) {
            appReply.setCode(AppReply.EORRO_CODE);
            appReply.setMsg("流程id为空");
            logger.error("流程id为空");
            return appReply;
        }
        try {
            PageData pd = new PageData();
            //获取流程的所有节点的信息
            String taskId = taskService.createTaskQuery().executionId(executionId).singleResult().getId();
            pd.put("taskId",taskId);
            List<String> applayers = wmpTaskService.getApplayerByTask(pd);
            appReply.setObj(applayers);
        } catch (Exception e) {
            appReply.setCode(AppReply.EORRO_CODE);
            appReply.setMsg("查询事项详情异常");
            logger.error("查询事项详情异常");
            e.printStackTrace();
        }
        return appReply;
    }
}
