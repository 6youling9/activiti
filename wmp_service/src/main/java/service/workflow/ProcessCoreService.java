package service.workflow;

import common.util.UuidUtil;
import org.activiti.engine.*;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.impl.RepositoryServiceImpl;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.pvm.process.ProcessDefinitionImpl;
import org.activiti.engine.impl.pvm.process.TransitionImpl;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.NativeTaskQuery;
import org.activiti.engine.task.Task;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/** 
 * 流程操作核心类<br> 
 * 此核心类主要处理：流程通过、驳回、会签、转办、中止、挂起等核心操作<br> 
 *  
 * @author Shy 
 *  
 */ 
@Service
public class ProcessCoreService{
	
	@Autowired
    protected RepositoryService repositoryService;
	@Autowired
    protected RuntimeService runtimeService;
	@Autowired
    protected TaskService taskService;
	@Autowired
    protected FormService formService;
	@Autowired
    protected HistoryService historyService;

  
   /* protected ProcessOtherService processOtherService;  */
  
    /** 
     * 根据当前任务ID，查询可以驳回的任务节点 
     *  
     * @param taskId 
     *            当前任务ID 
     */  
    public List<ActivityImpl> findBackAvtivity(String taskId) throws Exception {
        List<ActivityImpl> rtnList = null;
        if (isJoinTask(taskId)) {// 会签任务节点，不允许驳回  
            rtnList = new ArrayList<ActivityImpl>();
        } else {  
            rtnList = iteratorBackActivity(taskId, findActivitiImpl(taskId,  
                    null), new ArrayList<ActivityImpl>(),
                    new ArrayList<ActivityImpl>());
        }  
        return reverList(rtnList);  
    }  
    
    /**
     * 判断节点是否是会签节点
     */
    public boolean isJoinTask(String taskId){
    	   if(taskQueryByparentTaskId(taskId).size()>0)
    		   return true;
    	   return false;
    }
    
    /**
     * 查询会签节点的所有任务
     * @param taskId
     * @return
     */
    public List<Task> taskQueryByparentTaskId(String taskId) {
            String sql="select distinct RES.* from ACT_RU_TASK RES WHERE 1=1  AND RES.PARENT_TASK_ID_=#{taskId}";
    	 NativeTaskQuery query= taskService.createNativeTaskQuery().sql(sql);
    	    query.parameter("taskId", taskId);
    	 List<Task> tasks = query.list();
    	 return tasks;
	}
    
  
    /** 
     * 审批通过(驳回直接跳回功能需后续扩展) 
     *  
     * @param taskId 
     *            当前任务ID 
     * @param variables 
     *            流程存储参数 
     * @throws Exception
     */  
    public void passProcess(String taskId, Map<String, Object> variables)
            throws Exception {
    /*    List<Task> tasks = taskService.createTaskQuery().parentTaskId(taskId)  
                .taskDescription("jointProcess").list();*/
    	 List<Task> tasks = taskQueryByparentTaskId(taskId);
        for (Task task : tasks) {// 级联结束本节点发起的会签任务
            commitProcess(task.getId(), null, null);  
        }  
        commitProcess(taskId, variables, null);  
    }  
  
    /** 
     * 驳回流程 
     *  
     * @param taskId 
     *            当前任务ID 
     * @param activityId 
     *            驳回节点ID 
     * @param variables 
     *            流程存储参数 
     * @throws Exception
     */  
    public void backProcess(String taskId, String activityId,
                            Map<String, Object> variables) throws Exception {
        if (StringUtils.isEmpty(activityId)) {
            throw new Exception("驳回目标节点ID为空！");
        }  
  
        // 查询本节点发起的会签任务，并结束  
    /*    List<Task> tasks = taskService.createTaskQuery()..parentTaskId(taskId)  
                .taskDescription("jointProcess").list();  */
        List<Task> tasks = taskQueryByparentTaskId(taskId);
        for (Task task : tasks) {
            commitProcess(task.getId(), null, null);  
        }  
  
        // 查找所有并行任务节点，同时驳回  
        List<Task> taskList = findTaskListByKey(findProcessInstanceByTaskId(
                taskId).getId(), findTaskById(taskId).getTaskDefinitionKey());  
        for (Task task : taskList) {
            commitProcess(task.getId(), variables, activityId);  
        }  
    }  
  
    /** 
     * 取回流程 
     *  
     * @param taskId 
     *            当前任务ID 
     * @param activityId 
     *            取回节点ID 
     * @throws Exception
     */  
    public void callBackProcess(String taskId, String activityId)
            throws Exception {
        if (StringUtils.isEmpty(activityId)) {
            throw new Exception("目标节点ID为空！");
        }  
  
        // 查找所有并行任务节点，同时取回  
        List<Task> taskList = findTaskListByKey(findProcessInstanceByTaskId(
                taskId).getId(), findTaskById(taskId).getTaskDefinitionKey());  
        for (Task task : taskList) {
            commitProcess(task.getId(), null, activityId);  
        }  
    }  
  
    /** 
     * 中止流程(特权人直接审批通过等) 
     *  
     * @param taskId 
     */  
    public void endProcess(String taskId) throws Exception {
        ActivityImpl endActivity = findActivitiImpl(taskId, "end");
        commitProcess(taskId, null, endActivity.getId());  
    }  
  
    /** 
     * 会签操作 
     *  
     * @param taskId 
     *            当前任务ID 
     * @param userCodes 
     *            会签人账号集合 
     * @throws Exception
     */  
    public void jointProcess(String taskId, List<String> userCodes)
            throws Exception {
        for (String userCode : userCodes) {
            /*TaskEntity task = (TaskEntity) taskService.newTask(IDGenerator  
                    .generateID());  */
        	TaskEntity task = (TaskEntity) taskService.newTask(UuidUtil.get32UUID());
            task.setAssignee(userCode);  
            task.setName(findTaskById(taskId).getName() + "-会签");  
            task.setProcessDefinitionId(findProcessDefinitionEntityByTaskId(  
                    taskId).getId());  
            task.setProcessInstanceId(findProcessInstanceByTaskId(taskId)  
                    .getId());  
            task.setParentTaskId(taskId);  
            task.setDescription("jointProcess");  
            taskService.saveTask(task);  
        }  
    }  
  
    /** 
     * 转办流程 
     *  
     * @param taskId 
     *            当前任务节点ID 
     * @param userCode 
     *            被转办人Code 
     */  
    public void transferAssignee(String taskId, String userCode) {
        taskService.setAssignee(taskId, userCode);  
    }  
  
    /** 
     * ***************************************************************************************************************************************************<br> 
     * ************************************************以下为流程会签操作核心逻辑******************************************************************************<br> 
     * ***************************************************************************************************************************************************<br> 
     */  
  
    /** 
     * ***************************************************************************************************************************************************<br> 
     * ************************************************以上为流程会签操作核心逻辑******************************************************************************<br> 
     * ***************************************************************************************************************************************************<br> 
     */  
  
    /** 
     * ***************************************************************************************************************************************************<br> 
     * ************************************************以下为流程转向操作核心逻辑******************************************************************************<br> 
     * ***************************************************************************************************************************************************<br> 
     */  
  
    /** 
     * @param taskId 
     *            当前任务ID 
     * @param variables 
     *            流程变量 
     * @param activityId 
     *            流程转向执行任务节点ID<br> 
     *            此参数为空，默认为提交操作 
     * @throws Exception
     */  
    private void commitProcess(String taskId, Map<String, Object> variables,
                               String activityId) throws Exception {
        if (variables == null) {  
            variables = new HashMap<String, Object>();
        }  
        // 跳转节点为空，默认提交操作  
        if (StringUtils.isEmpty(activityId)) {
            taskService.complete(taskId, variables);  
        } else {// 流程转向操作  
            turnTransition(taskId, activityId, variables);  
        }  
    }  
  
    /** 
     * 清空指定活动节点流向 
     *  
     * @param activityImpl 
     *            活动节点 
     * @return 节点流向集合 
     */  
    private List<PvmTransition> clearTransition(ActivityImpl activityImpl) {
        // 存储当前节点所有流向临时变量  
        List<PvmTransition> oriPvmTransitionList = new ArrayList<PvmTransition>();
        // 获取当前节点所有流向，存储到临时变量，然后清空  
        List<PvmTransition> pvmTransitionList = activityImpl
                .getOutgoingTransitions();  
        for (PvmTransition pvmTransition : pvmTransitionList) {
            oriPvmTransitionList.add(pvmTransition);  
        }  
        pvmTransitionList.clear();  
  
        return oriPvmTransitionList;  
    }  
  
    /** 
     * 还原指定活动节点流向 
     *  
     * @param activityImpl 
     *            活动节点 
     * @param oriPvmTransitionList 
     *            原有节点流向集合 
     */  
    private void restoreTransition(ActivityImpl activityImpl,
            List<PvmTransition> oriPvmTransitionList) {
        // 清空现有流向  
        List<PvmTransition> pvmTransitionList = activityImpl
                .getOutgoingTransitions();  
        pvmTransitionList.clear();  
        // 还原以前流向  
        for (PvmTransition pvmTransition : oriPvmTransitionList) {
            pvmTransitionList.add(pvmTransition);  
        }  
    }  
  
    /** 
     * 流程转向操作 
     *  
     * @param taskId 
     *            当前任务ID 
     * @param activityId 
     *            目标节点任务ID 
     * @param variables 
     *            流程变量 
     * @throws Exception
     */  
    private void turnTransition(String taskId, String activityId,
                                Map<String, Object> variables) throws Exception {
        // 当前节点  
        ActivityImpl currActivity = findActivitiImpl(taskId, null);
        // 清空当前流向  
        List<PvmTransition> oriPvmTransitionList = clearTransition(currActivity);
  
        // 创建新流向  
        TransitionImpl newTransition = currActivity.createOutgoingTransition();
        // 目标节点  
        ActivityImpl pointActivity = findActivitiImpl(taskId, activityId);
        // 设置新流向的目标节点  
        newTransition.setDestination(pointActivity);  
  
        // 执行转向任务  
        taskService.complete(taskId, variables);  
        // 删除目标节点新流入  
        pointActivity.getIncomingTransitions().remove(newTransition);  
  
        // 还原以前流向  
        restoreTransition(currActivity, oriPvmTransitionList);  
    }  
  
    /** 
     * ***************************************************************************************************************************************************<br> 
     * ************************************************以上为流程转向操作核心逻辑******************************************************************************<br> 
     * ***************************************************************************************************************************************************<br> 
     */  
  
    /** 
     * ***************************************************************************************************************************************************<br> 
     * ************************************************以下为查询流程驳回节点核心逻辑***************************************************************************<br> 
     * ***************************************************************************************************************************************************<br> 
     */  
  
    /** 
     * 迭代循环流程树结构，查询当前节点可驳回的任务节点 
     *  
     * @param taskId 
     *            当前任务ID 
     * @param currActivity 
     *            当前活动节点 
     * @param rtnList 
     *            存储回退节点集合 
     * @param tempList 
     *            临时存储节点集合（存储一次迭代过程中的同级userTask节点） 
     * @return 回退节点集合 
     */  
    private List<ActivityImpl> iteratorBackActivity(String taskId,
                                                    ActivityImpl currActivity, List<ActivityImpl> rtnList,
                                                    List<ActivityImpl> tempList) throws Exception {
        // 查询流程定义，生成流程树结构  
        ProcessInstance processInstance = findProcessInstanceByTaskId(taskId);
  
        // 当前节点的流入来源  
        List<PvmTransition> incomingTransitions = currActivity
                .getIncomingTransitions();  
        // 条件分支节点集合，userTask节点遍历完毕，迭代遍历此集合，查询条件分支对应的userTask节点  
        List<ActivityImpl> exclusiveGateways = new ArrayList<ActivityImpl>();
        // 并行节点集合，userTask节点遍历完毕，迭代遍历此集合，查询并行节点对应的userTask节点  
        List<ActivityImpl> parallelGateways = new ArrayList<ActivityImpl>();
        // 遍历当前节点所有流入路径  
        for (PvmTransition pvmTransition : incomingTransitions) {
            TransitionImpl transitionImpl = (TransitionImpl) pvmTransition;
            ActivityImpl activityImpl = transitionImpl.getSource();
            String type = (String) activityImpl.getProperty("type");
            /** 
             * 并行节点配置要求：<br> 
             * 必须成对出现，且要求分别配置节点ID为:XXX_start(开始)，XXX_end(结束) 
             */  
            if ("parallelGateway".equals(type)) {// 并行路线  
                String gatewayId = activityImpl.getId();
                String gatewayType = gatewayId.substring(gatewayId
                        .lastIndexOf("_") + 1);  
                if ("START".equals(gatewayType.toUpperCase())) {// 并行起点，停止递归  
                    return rtnList;  
                } else {// 并行终点，临时存储此节点，本次循环结束，迭代集合，查询对应的userTask节点  
                    parallelGateways.add(activityImpl);  
                }  
            } else if ("startEvent".equals(type)) {// 开始节点，停止递归  
                return rtnList;  
            } else if ("userTask".equals(type)) {// 用户任务  
                tempList.add(activityImpl);  
            } else if ("exclusiveGateway".equals(type)) {// 分支路线，临时存储此节点，本次循环结束，迭代集合，查询对应的userTask节点  
                currActivity = transitionImpl.getSource();  
                exclusiveGateways.add(currActivity);  
            }  
        }  
  
        /** 
         * 迭代条件分支集合，查询对应的userTask节点 
         */  
        for (ActivityImpl activityImpl : exclusiveGateways) {
            iteratorBackActivity(taskId, activityImpl, rtnList, tempList);  
        }  
  
        /** 
         * 迭代并行集合，查询对应的userTask节点 
         */  
        for (ActivityImpl activityImpl : parallelGateways) {
            iteratorBackActivity(taskId, activityImpl, rtnList, tempList);  
        }  
  
        /** 
         * 根据同级userTask集合，过滤最近发生的节点 
         */  
        currActivity = filterNewestActivity(processInstance, tempList);  
        if (currActivity != null) {  
            // 查询当前节点的流向是否为并行终点，并获取并行起点ID  
            String id = findParallelGatewayId(currActivity);
            if (StringUtils.isEmpty(id)) {// 并行起点ID为空，此节点流向不是并行终点，符合驳回条件，存储此节点
                rtnList.add(currActivity);  
            } else {// 根据并行起点ID查询当前节点，然后迭代查询其对应的userTask任务节点  
                currActivity = findActivitiImpl(taskId, id);  
            }  
  
            // 清空本次迭代临时集合  
            tempList.clear();  
            // 执行下次迭代  
            iteratorBackActivity(taskId, currActivity, rtnList, tempList);  
        }  
        return rtnList;  
    }  
  
    /** 
     * 反向排序list集合，便于驳回节点按顺序显示 
     *  
     * @param list 
     * @return 
     */  
    private List<ActivityImpl> reverList(List<ActivityImpl> list) {
        List<ActivityImpl> rtnList = new ArrayList<ActivityImpl>();
        // 由于迭代出现重复数据，排除重复  
        for (int i = list.size(); i > 0; i--) {  
            if (!rtnList.contains(list.get(i - 1)))  
                rtnList.add(list.get(i - 1));  
        }  
        return rtnList;  
    }  
  
    /** 
     * 根据当前节点，查询输出流向是否为并行终点，如果为并行终点，则拼装对应的并行起点ID 
     *  
     * @param activityImpl 
     *            当前节点 
     * @return 
     */  
    private String findParallelGatewayId(ActivityImpl activityImpl) {
        List<PvmTransition> incomingTransitions = activityImpl
                .getOutgoingTransitions();  
        for (PvmTransition pvmTransition : incomingTransitions) {
            TransitionImpl transitionImpl = (TransitionImpl) pvmTransition;
            activityImpl = transitionImpl.getDestination();  
            String type = (String) activityImpl.getProperty("type");
            if ("parallelGateway".equals(type)) {// 并行路线  
                String gatewayId = activityImpl.getId();
                String gatewayType = gatewayId.substring(gatewayId
                        .lastIndexOf("_") + 1);  
                if ("END".equals(gatewayType.toUpperCase())) {  
                    return gatewayId.substring(0, gatewayId.lastIndexOf("_"))  
                            + "_start";  
                }  
            }  
        }  
        return null;  
    }  
  
    /** 
     * 根据流入任务集合，查询最近一次的流入任务节点 
     *  
     * @param processInstance 
     *            流程实例 
     * @param tempList 
     *            流入任务集合 
     * @return 
     */  
    private ActivityImpl filterNewestActivity(ProcessInstance processInstance,
                                              List<ActivityImpl> tempList) {
        while (tempList.size() > 0) {  
            ActivityImpl activity_1 = tempList.get(0);
            HistoricActivityInstance activityInstance_1 = findHistoricUserTask(
                    processInstance, activity_1.getId());  
            if (activityInstance_1 == null) {  
                tempList.remove(activity_1);  
                continue;  
            }  
  
            if (tempList.size() > 1) {  
                ActivityImpl activity_2 = tempList.get(1);
                HistoricActivityInstance activityInstance_2 = findHistoricUserTask(
                        processInstance, activity_2.getId());  
                if (activityInstance_2 == null) {  
                    tempList.remove(activity_2);  
                    continue;  
                }  
  
                if (activityInstance_1.getEndTime().before(  
                        activityInstance_2.getEndTime())) {  
                    tempList.remove(activity_1);  
                } else {  
                    tempList.remove(activity_2);  
                }  
            } else {  
                break;  
            }  
        }  
        if (tempList.size() > 0) {  
            return tempList.get(0);  
        }  
        return null;  
    }  
  
    /** 
     * 查询指定任务节点的最新记录 
     *  
     * @param processInstance 
     *            流程实例 
     * @param activityId 
     * @return 
     */  
    private HistoricActivityInstance findHistoricUserTask(
            ProcessInstance processInstance, String activityId) {
        HistoricActivityInstance rtnVal = null;
        // 查询当前流程实例审批结束的历史节点  
        List<HistoricActivityInstance> historicActivityInstances = historyService
                .createHistoricActivityInstanceQuery().activityType("userTask")  
                .processInstanceId(processInstance.getId()).activityId(  
                        activityId).finished()  
                .orderByHistoricActivityInstanceEndTime().desc().list();  
        if (historicActivityInstances.size() > 0) {  
            rtnVal = historicActivityInstances.get(0);  
        }  
  
        return rtnVal;  
    }  
  
    /** 
     * *******************************************************************************************************<br> 
     * ********************************以上为查询流程驳回节点核心逻辑***********************************************<br> 
     * ********************************************************************************************************<br> 
     */  
  
    /** 
     * ********************************************************************************<br> 
     * **********************以下为activiti 核心service 
     * set方法***************************<br> 
     * *********************************************************************************<br> 
     */  
    public void setFormService(FormService formService) {
        this.formService = formService;  
    }  
  
    public void setHistoryService(HistoryService historyService) {
        this.historyService = historyService;  
    }  
  
    public void setRepositoryService(RepositoryService repositoryService) {
        this.repositoryService = repositoryService;  
    }  
  
    public void setRuntimeService(RuntimeService runtimeService) {
        this.runtimeService = runtimeService;  
    }  
  
    public void setTaskService(TaskService taskService) {
        this.taskService = taskService;  
    }  
  
    /** 
     * ********************************************************************************<br> 
     * **********************以上为activiti 核心service 
     * set方法***************************<br> 
     * *********************************************************************************<br> 
     */  
  
    /** 
     * ********************************************************************************<br> 
     * **********************以下为根据 任务节点ID 获取流程各对象查询方法**********************<br> 
     * *********************************************************************************<br> 
     */  
  
/*    public void setProcessOtherService(ProcessOtherService processOtherService) {  
        this.processOtherService = processOtherService;  
    }  */
  
    /** 
     * 根据任务ID获得任务实例 
     *  
     * @param taskId 
     *            任务ID 
     * @return 
     * @throws Exception
     */  
    private TaskEntity findTaskById(String taskId) throws Exception {
        TaskEntity task = (TaskEntity) taskService.createTaskQuery().taskId(
                taskId).singleResult();  
        if (task == null) {  
            throw new Exception("任务实例未找到!");
        }  
        return task;  
    }  
  
    /** 
     * 根据流程实例ID和任务key值查询所有同级任务集合 
     *  
     * @param processInstanceId 
     * @param key 
     * @return 
     */  
    private List<Task> findTaskListByKey(String processInstanceId, String key) {
        return taskService.createTaskQuery().processInstanceId(  
                processInstanceId).taskDefinitionKey(key).list();  
    }  
  
    /** 
     * 根据任务ID获取对应的流程实例 
     *  
     * @param taskId 
     *            任务ID 
     * @return 
     * @throws Exception
     */  
    private ProcessInstance findProcessInstanceByTaskId(String taskId)
            throws Exception {
        // 找到流程实例  
        ProcessInstance processInstance = runtimeService
                .createProcessInstanceQuery().processInstanceId(  
                        findTaskById(taskId).getProcessInstanceId())  
                .singleResult();  
        if (processInstance == null) {  
            throw new Exception("流程实例未找到!");
        }  
        return processInstance;  
    }  
  
    /** 
     * 根据任务ID获取流程定义 
     *  
     * @param taskId 
     *            任务ID 
     * @return 
     * @throws Exception
     */  
    private ProcessDefinitionEntity findProcessDefinitionEntityByTaskId(
            String taskId) throws Exception {
        // 取得流程定义  
        ProcessDefinitionEntity processDefinition = (ProcessDefinitionEntity) ((RepositoryServiceImpl) repositoryService)
                .getDeployedProcessDefinition(findTaskById(taskId)  
                        .getProcessDefinitionId());  
  
        if (processDefinition == null) {  
            throw new Exception("流程定义未找到!");
        }  
  
        return processDefinition;  
    }  
  
    /** 
     * 根据任务ID和节点ID获取活动节点 <br> 
     *  
     * @param taskId 
     *            任务ID 
     * @param activityId 
     *            活动节点ID <br> 
     *            如果为null或""，则默认查询当前活动节点 <br> 
     *            如果为"end"，则查询结束节点 <br> 
     *  
     * @return 
     * @throws Exception
     */  
    private ActivityImpl findActivitiImpl(String taskId, String activityId)
            throws Exception {
        // 取得流程定义  
        ProcessDefinitionEntity processDefinition = findProcessDefinitionEntityByTaskId(taskId);
  
        // 获取当前活动节点ID  
        if (StringUtils.isEmpty(activityId)) {
            activityId = findTaskById(taskId).getTaskDefinitionKey();  
        }  
  
        // 根据流程定义，获取该流程实例的结束节点  
        if (activityId.toUpperCase().equals("END")) {  
            for (ActivityImpl activityImpl : processDefinition.getActivities()) {
                List<PvmTransition> pvmTransitionList = activityImpl
                        .getOutgoingTransitions();  
                if (pvmTransitionList.isEmpty()) {  
                    return activityImpl;  
                }  
            }  
        }  
  
        // 根据节点ID，获取对应的活动节点  
        ActivityImpl activityImpl = ((ProcessDefinitionImpl) processDefinition)
                .findActivity(activityId);  
  
        return activityImpl;  
    }  
    
  
    /** 
     * ********************************************************************************<br> 
     * **********************以上为根据 任务节点ID 获取流程各对象查询方法**********************<br> 
     * *********************************************************************************<br> 
     */  
    
    
    
    
    
    
    
    
    
    
    public void rejectTask(String procInstId, String destTaskKey, String rejectMessage)
    		throws Exception {
    		//获得当前任务的对应实列
    		Task taskEntity = taskService.createTaskQuery().processInstanceId(procInstId).singleResult();
    		//当前任务key
    		String taskDefKey = taskEntity.getTaskDefinitionKey();
    		//获得当前流程的定义模型
    		ProcessDefinitionEntity processDefinition =(ProcessDefinitionEntity) ((RepositoryServiceImpl)repositoryService)
    		.getDeployedProcessDefinition(taskEntity.getProcessDefinitionId()); 

    		//获得当前流程定义模型的所有任务节点
    		List<ActivityImpl> activitilist = processDefinition.getActivities();
    		//获得当前活动节点和驳回的目标节点"draft"
    		ActivityImpl currActiviti = null;//当前活动节点
    		ActivityImpl destActiviti = null;//驳回目标节点
    		int sign = 0;
    		for(ActivityImpl activityImpl : activitilist){
    		//确定当前活动activiti节点
    		if(taskDefKey.equals(activityImpl.getId())){
    		currActiviti = activityImpl;

    		sign++;
    		}else if(destTaskKey.equals(activityImpl.getId())){
    		destActiviti = activityImpl;
    		sign++;
    		}
    		//System.out.println("//-->activityImpl.getId():"+activityImpl.getId());
    		if(sign==2){
    		break;//如果两个节点都获得,退出跳出循环
    		}
    		}
    		System.out.println("//-->currActiviti activityImpl.getId():"+currActiviti.getId());
    		System.out.println("//-->destActiviti activityImpl.getId():"+destActiviti.getId());
    		//保存当前活动节点的流程想参数
    		List<PvmTransition> hisPvmTransitionList = new ArrayList<PvmTransition>(0);

    		for(PvmTransition pvmTransition:currActiviti.getOutgoingTransitions()){
    		       hisPvmTransitionList.add(pvmTransition);
    		}
    		//清空当前活动节点的所有流出项
    		currActiviti.getOutgoingTransitions().clear();
    		System.out.println("//-->currActiviti.getOutgoingTransitions().clear():"+currActiviti.getOutgoingTransitions().size());
    		//为当前节点动态创建新的流出项
    		TransitionImpl newTransitionImpl = currActiviti.createOutgoingTransition();
    		//为当前活动节点新的流出目标指定流程目标
    		newTransitionImpl.setDestination(destActiviti);
    		//保存驳回意见
    		taskEntity.setDescription(rejectMessage);//设置驳回意见
    		
    		taskService.saveTask(taskEntity);
    		//设定驳回标志 
    		Map<String, Object> variables = new HashMap<String, Object>();
    		
    		variables.put("HT","HT");
    		//执行当前任务驳回到目标任务draft
    		taskService.complete(taskEntity.getId(), variables);
    		//清除目标节点的新流入项
    		destActiviti.getIncomingTransitions().remove(newTransitionImpl);
    		//清除原活动节点的临时流程项
    		currActiviti.getOutgoingTransitions().clear();
    		//还原原活动节点流出项参数
    		currActiviti.getOutgoingTransitions().addAll(hisPvmTransitionList);
    		}
    
    
}  
