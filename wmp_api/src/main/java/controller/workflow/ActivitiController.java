package controller.workflow;

import com.alibaba.fastjson.JSONObject;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import common.util.AppReply;
import common.util.PageData;
import common.util.StringUtil;
import common.util.WorkflowUtils;
import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.editor.constants.ModelDataJsonConstants;
import org.activiti.editor.language.json.converter.BpmnJsonConverter;
import org.activiti.engine.*;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.impl.RepositoryServiceImpl;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.Model;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.repository.ProcessDefinitionQuery;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.image.ProcessDiagramGenerator;
import org.activiti.spring.ProcessEngineFactoryBean;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import service.workflow.WorkflowProcessDefinitionService;
import service.workflow.WorkflowTraceService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipInputStream;

/**
 * 流程管理控制器
 * @author shy
 */
@RestController
@RequestMapping(value = "/workflow")
public class ActivitiController {
    
    protected Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    protected WorkflowProcessDefinitionService workflowProcessDefinitionService;
    @Autowired
    protected RepositoryService repositoryService;
    @Autowired
    protected RuntimeService runtimeService;
    @Autowired
    protected TaskService taskService;
    @Autowired
    protected WorkflowTraceService traceService;

    @Autowired
    ManagementService managementService;
    
    @Autowired
    private HistoryService historyService;
    
    protected static Map<String, ProcessDefinition> PROCESS_DEFINITION_CACHE = new HashMap<String, ProcessDefinition>();

    @Autowired
    ProcessEngineFactoryBean processEngine;

    @Autowired
    ProcessEngineConfiguration processEngineConfiguration;
    
    /**
     * @Description: 获取所有已激活的流程定义
     * @method_name: pdList_active
     * @author wangze
     * @param request
     * @date 2017/9/6 18:41
     * @return List<PageData>
     */
    @RequestMapping(value = "/procDef_active")
    public AppReply<Object> procDef_active(HttpServletRequest request) {
        logger.info("TaskController.procDef_active");
        AppReply<Object> appReply = new AppReply<Object>();
        try {
            ProcessDefinitionQuery processDefinitionQuery = repositoryService.createProcessDefinitionQuery().orderByProcessDefinitionName().asc();
            List<ProcessDefinition> list = processDefinitionQuery.active().list();
            List<Map<String,String>> result = new ArrayList<Map<String,String>>();
            for(ProcessDefinition procDef : list){
                Map<String,String> map = new HashMap<String,String>();
                if(!StringUtil.isBlank(procDef.getName())) {
                    map.put("name", procDef.getName() + ":V" + procDef.getVersion());
                    map.put("procDefId", procDef.getId());
                    result.add(map);
                }
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
     * 根据流程实例id获取流程名称
     * @param pdId
     * @return
     */
    @RequestMapping(value = "getpdName")
    @ResponseBody
    public AppReply<Object> getpdName(String pdId) {
        logger.info("TaskController.procDef_active");
        AppReply<Object> appReply = new AppReply<Object>();
        try {
            ProcessDefinitionQuery processDefinitionQuery = repositoryService.createProcessDefinitionQuery().orderByProcessDefinitionName().asc();
            ProcessDefinition procDef = processDefinitionQuery.active().processDefinitionId(pdId).singleResult();
            Map<String,String> result = new HashMap<String,String>();
            if(procDef!=null&&!StringUtil.isBlank(procDef.getName())) {
                result.put("name", procDef.getName() + ":V" + procDef.getVersion());
                result.put("procDefId", procDef.getId());
            }
            appReply.setObj(result);
        } catch (Exception e) {
            appReply.setCode(AppReply.EORRO_CODE);
            appReply.setMsg("查询事项名称异常");
            logger.error("查询事项名称异常");
            e.printStackTrace();
        }
        return appReply;
    }
    
    /**
     * 流程定义列表
     *
     * @return
     */
    @RequestMapping(value = "/process-list")
    public AppReply<PageData> processList(HttpServletRequest request) {
	    /*
	     * 保存两个对象，一个是ProcessDefinition（流程定义），一个是Deployment（流程部署）
	     */
        List<Object[]> objects = new ArrayList<Object[]>();
        AppReply<PageData> reply =new AppReply<PageData>();
        PageData rowsData=new PageData();

        List<ProcessDefinition> processDefinitionList = repositoryService.createProcessDefinitionQuery().orderByDeploymentId().desc().list();

        for (ProcessDefinition processDefinition : processDefinitionList) {
            String deploymentId = processDefinition.getDeploymentId();
            Deployment deployment = repositoryService.createDeploymentQuery().deploymentId(deploymentId).singleResult();
            objects.add(new Object[]{processDefinition, deployment});
        }
        
        rowsData.put("total", processDefinitionList.size());
        rowsData.put("rows", objects);
        reply.setObj(rowsData);

        return reply;
    }

    
    /**
     * 获取部署列表key和部署id名字 拼接下拉
     * @param request
     * @param response
     */
    @RequestMapping(value = "/process-list-select")
    public AppReply<List<JSONObject>> processListSelect(HttpServletRequest request, HttpServletResponse response) {
    	AppReply<List<JSONObject>> appReply=new AppReply<List<JSONObject>>();
    	List<JSONObject> list= new ArrayList<JSONObject>();
        JSONObject jsonObject;
        ProcessDefinitionQuery processDefinitionQuery = repositoryService.createProcessDefinitionQuery().orderByDeploymentId().desc();
        List<ProcessDefinition> processDefinitionList = processDefinitionQuery.list();
        for (ProcessDefinition processDefinition : processDefinitionList) {
        	jsonObject=new JSONObject();
            jsonObject.put("key",processDefinition.getKey()+":"+processDefinition.getDeploymentId());
            list.add(jsonObject);
        }
        appReply.setObj(list);
        return appReply;
    }
    
    /**
     * 读取资源，通过部署ID
     *
     * @param processDefinitionId 流程定义
     * @param resourceType         资源类型(xml|image)
     * @throws Exception
     */
    @RequestMapping(value = "/resource/read")
    public void loadByDeployment(@RequestParam("processDefinitionId") String processDefinitionId, @RequestParam("resourceType") String resourceType,
                                 HttpServletResponse response) throws Exception {
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionId(processDefinitionId).singleResult();
        String resourceName = "";
        if (resourceType.equals("image")) {
            resourceName = processDefinition.getDiagramResourceName();
        } else if (resourceType.equals("xml")) {
            resourceName = processDefinition.getResourceName();
        }
        InputStream resourceAsStream = repositoryService.getResourceAsStream(processDefinition.getDeploymentId(), resourceName);
        byte[] b = new byte[1024];
        int len = -1;
        while ((len = resourceAsStream.read(b, 0, 1024)) != -1) {
            response.getOutputStream().write(b, 0, len);
        }
    }

    /**
     * 读取资源，通过流程ID
     *
     * @param resourceType      资源类型(xml|image)
     * @param processInstanceId 流程实例ID
     * @param response
     * @throws Exception
     */
    @RequestMapping(value = "/resource/process-instance")
    public void loadByProcessInstance(@RequestParam("type") String resourceType, @RequestParam("pid") String processInstanceId, HttpServletResponse response)
            throws Exception {
        InputStream resourceAsStream = null;
        ProcessInstance processInstance=null;
        String processDefinitionId;
        boolean flag=isProcessEnd(processInstanceId);
        if(flag){
       	     HistoricProcessInstance hpi=(HistoricProcessInstance)historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
       	     processDefinitionId=hpi.getProcessDefinitionId();
         }else{
       	     processInstance= runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
             processDefinitionId = processInstance.getProcessDefinitionId();
        }                                         
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionId(processDefinitionId)
                .singleResult();

        String resourceName = "";
        if (resourceType.equals("image")) {
            resourceName = processDefinition.getDiagramResourceName();
        } else if (resourceType.equals("xml")) {
            resourceName = processDefinition.getResourceName();
        }
        resourceAsStream = repositoryService.getResourceAsStream(processDefinition.getDeploymentId(), resourceName);
        byte[] b = new byte[1024];
        int len = -1;
        while ((len = resourceAsStream.read(b, 0, 1024)) != -1) {
            response.getOutputStream().write(b, 0, len);
        }
    }
    
    /*判断当前流程是否结束*/
    public  boolean isProcessEnd(String processInstanceId){
 	    ProcessInstance pi = runtimeService//表示正在执行的流程实例和执行对象  
 	            .createProcessInstanceQuery()//创建流程实例查询  
 	            .processInstanceId(processInstanceId)//使用流程实例ID查询  
 	            .singleResult();  
 	    if(pi==null){  
 	        System.out.println("流程已经结束");
 	        return true;
 	    }else{  
 	        System.out.println("流程没有结束");
 	        return false;
 	    }  
 	}  
    
    /**
     * 删除部署的流程，级联删除流程实例
     *
     * @param deploymentId 流程部署ID
     */
    @RequestMapping(value = "/process/delete")
    public String delete(@RequestParam("deploymentId") String deploymentId) {
        repositoryService.deleteDeployment(deploymentId, true);
        return "redirect:/workflow/process-list";
    }
    
    /**
     * 接口删除部署的流程，级联删除流程实例
     *
     * @param deploymentId 流程部署ID
     */
    @RequestMapping(value = "jk/process/delete")
    public void jkdelete(@RequestParam("deploymentId") String deploymentId, PrintWriter out) {
        repositoryService.deleteDeployment(deploymentId, true);
        out.write("流程已删除！！！");
    }

    /**
     * 输出跟踪流程信息
     *
     * @param processInstanceId
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/process/trace")
    @ResponseBody
    public List<Map<String, Object>> traceProcess(@RequestParam("pid") String processInstanceId) throws Exception {
    	List<Map<String, Object>> activityInfos = traceService.traceProcess(processInstanceId);
        return activityInfos;
    }
    
    /**
     * 获取历史节点信息
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    @RequestMapping(value="/history/activiti")
    @ResponseBody
    public List<Map<String, Object>> getHistoryActiviti(@RequestParam("pid") String processInstanceId) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
    	List<Map<String, Object>> activityInfos = new ArrayList<Map<String, Object>>();
    	String processDefinitionId;
    	ProcessInstance  processInstance=null;
    	List<HistoricActivityInstance> historicActivityInstanceList = historyService.createHistoricActivityInstanceQuery()
				.processInstanceId(processInstanceId).orderByHistoricActivityInstanceId().asc().list();
    	
        Execution execution = runtimeService.createExecutionQuery().executionId(processInstanceId).singleResult();//执行实例
        String activityId = "";
        if(execution!=null){
        	 Object property = PropertyUtils.getProperty(execution, "activityId");
             if (property != null) {
                 activityId = property.toString();
             }
        }
        boolean flag=isProcessEnd(processInstanceId);
        if(flag){
        	 HistoricProcessInstance hpi=(HistoricProcessInstance)historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
        	 processDefinitionId=hpi.getProcessDefinitionId();
          }else{
        	  processInstance= runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
              processDefinitionId = processInstance.getProcessDefinitionId();
         } 
        ProcessDefinitionEntity processDefinition = (ProcessDefinitionEntity) ((RepositoryServiceImpl) repositoryService)
                .getDeployedProcessDefinition(processDefinitionId);
    	for (int i = 0; i < historicActivityInstanceList.size(); i++) {
            String historyActivitiid=historicActivityInstanceList.get(i).getActivityId();
            ActivityImpl activityImpl = processDefinition.findActivity(historyActivitiid);//获得当前任务的所有节点
            try {
            	if(!activityId.equals(historyActivitiid)){
            		Map<String, Object> map= traceService.packageSingleActivitiInfo(activityImpl, processInstance, false);
            	    activityInfos.add(map);
            	}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
    	return activityInfos;
    }
    
    

    /**
     * 读取带跟踪的图片
     */
    @RequestMapping(value = "/process/trace/auto/{executionId}")
    public void readResource(@PathVariable("executionId") String executionId, HttpServletResponse response)
            throws Exception {
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(executionId).singleResult();
        BpmnModel bpmnModel = repositoryService.getBpmnModel(processInstance.getProcessDefinitionId());
        List<String> activeActivityIds = runtimeService.getActiveActivityIds(executionId);
        // 不使用spring请使用下面的两行代码
//    ProcessEngineImpl defaultProcessEngine = (ProcessEngineImpl) ProcessEngines.getDefaultProcessEngine();
//    Context.setProcessEngineConfiguration(defaultProcessEngine.getProcessEngineConfiguration());

        // 使用spring注入引擎请使用下面的这行代码
        processEngineConfiguration = processEngine.getProcessEngineConfiguration();
        Context.setProcessEngineConfiguration((ProcessEngineConfigurationImpl) processEngineConfiguration);

        ProcessDiagramGenerator diagramGenerator = processEngineConfiguration.getProcessDiagramGenerator();
        InputStream imageStream = diagramGenerator.generateDiagram(bpmnModel, "png", activeActivityIds);

        // 输出资源内容到相应对象
        byte[] b = new byte[1024];
        int len;
        while ((len = imageStream.read(b, 0, 1024)) != -1) {
            response.getOutputStream().write(b, 0, len);
        }
    }
   
    @RequestMapping(value = "/deploy")
    public String deploy(@Value("#{APP_PROPERTIES['export.diagram.path']}") String exportDir, @RequestParam(value = "file", required = false) MultipartFile file) {

        String fileName = file.getOriginalFilename();

        try {
            InputStream fileInputStream = file.getInputStream();
            Deployment deployment = null;

            String extension = FilenameUtils.getExtension(fileName);
            if (extension.equals("zip") || extension.equals("bar")) {
                ZipInputStream zip = new ZipInputStream(fileInputStream);
                deployment = repositoryService.createDeployment().addZipInputStream(zip).deploy();
            } else {
                deployment = repositoryService.createDeployment().addInputStream(fileName, fileInputStream).deploy();
            }

            List<ProcessDefinition> list = repositoryService.createProcessDefinitionQuery().deploymentId(deployment.getId()).list();

            for (ProcessDefinition processDefinition : list) {
                WorkflowUtils.exportDiagramToFile(repositoryService, processDefinition, exportDir);
            }

        } catch (Exception e) {
            logger.error("error on deploy process, because of file input stream", e);
        }

        return "redirect:/workflow/process-list";
    }

    @RequestMapping(value = "/process/convert-to-model/{processDefinitionId}")
    public String convertToModel(@PathVariable("processDefinitionId") String processDefinitionId)
            throws UnsupportedEncodingException, XMLStreamException {
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionId(processDefinitionId).singleResult();
        InputStream bpmnStream = repositoryService.getResourceAsStream(processDefinition.getDeploymentId(),
                processDefinition.getResourceName());
        XMLInputFactory xif = XMLInputFactory.newInstance();
        InputStreamReader in = new InputStreamReader(bpmnStream, "UTF-8");
        XMLStreamReader xtr = xif.createXMLStreamReader(in);
        BpmnModel bpmnModel = new BpmnXMLConverter().convertToBpmnModel(xtr);

        BpmnJsonConverter converter = new BpmnJsonConverter();
        com.fasterxml.jackson.databind.node.ObjectNode modelNode = converter.convertToJson(bpmnModel);
        Model modelData = repositoryService.newModel();
        modelData.setKey(processDefinition.getKey());
        modelData.setName(processDefinition.getResourceName());
        modelData.setCategory(processDefinition.getDeploymentId());

        ObjectNode modelObjectNode = new ObjectMapper().createObjectNode();
        modelObjectNode.put(ModelDataJsonConstants.MODEL_NAME, processDefinition.getName());
        modelObjectNode.put(ModelDataJsonConstants.MODEL_REVISION, 1);
        modelObjectNode.put(ModelDataJsonConstants.MODEL_DESCRIPTION, processDefinition.getDescription());
        modelData.setMetaInfo(modelObjectNode.toString());

        repositoryService.saveModel(modelData);

        repositoryService.addModelEditorSource(modelData.getId(), modelNode.toString().getBytes("utf-8"));

        return "redirect:/workflow/model/list";
    }

    

    /**
     * 导出图片文件到硬盘
     *
     * @return
     */
    @RequestMapping(value = "export/diagrams")
    @ResponseBody
    public List<String> exportDiagrams(@Value("#{APP_PROPERTIES['export.diagram.path']}") String exportDir) throws IOException {
        List<String> files = new ArrayList<String>();
        List<ProcessDefinition> list = repositoryService.createProcessDefinitionQuery().list();

        for (ProcessDefinition processDefinition : list) {
            files.add(WorkflowUtils.exportDiagramToFile(repositoryService, processDefinition, exportDir));
        }

        return files;
    }



    @RequestMapping(value = "bpmn/model/{processDefinitionId}")
    @ResponseBody
    public BpmnModel queryBpmnModel(@PathVariable("processDefinitionId") String processDefinitionId) {
        BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinitionId);
        return bpmnModel;
    }


}