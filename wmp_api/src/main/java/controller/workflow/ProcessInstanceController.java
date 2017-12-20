package controller.workflow;

import common.util.AppReply;
import common.util.PageData;
import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.editor.constants.ModelDataJsonConstants;
import org.activiti.editor.language.json.converter.BpmnJsonConverter;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.repository.Model;
import org.activiti.engine.repository.ProcessDefinition;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import service.workflow.WorkflowProcessDefinitionService;

import javax.servlet.http.HttpServletRequest;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * @project_name：bonc_ycioc_omp
 * @package_name：AttachmentController
 * @describe：流程控制器
 * @creater wangze (1215360909@qq.com)
 * @creat_time 2017-9-6 19:53
 * @changer   wangze
 * @change_time 2017-9-6 19:53
 * @remark
 * @version V0.1
 */
@Controller
@RequestMapping(value = "/workflow/processinstance")
public class ProcessInstanceController {
	
	@Autowired
	private RepositoryService repositoryService;
    @Autowired
    private RuntimeService runtimeService;
	@Autowired
	private HistoryService historyService;
	@Autowired
	private WorkflowProcessDefinitionService workflowProcessDefinitionService;
	
	private static Logger logger = LoggerFactory.getLogger(ProcessInstanceController.class);
	
	/**
	 * <p>@Description: 挂起/激活流程,返回json</p>
	 * <p>@author wangze </p>
	 * <p>@param state
	 * <p>@param processDefinitionId
	 * <p>@param cascadeProcessInstances
	 * <p>@param strEffectiveDate
	 * <p>@param redirectAttributes
	 * <p>@return    设定文件 </p>
	 * <p>返回类型 </p>
	 * <p>@throws </p>
	 */
	@RequestMapping(value = "/changeStateJson")
	@ResponseBody
	public AppReply<Object> changeStateJson(
			@RequestParam(value = "state") String state,
			@RequestParam(value = "processDefinitionId") String processDefinitionId,
			@RequestParam(value = "effectiveDate", required = false) String strEffectiveDate) {// 可以选择的日期
		logger.info("request/workflow/processinstance/changeStateJson:state"+state+";processDefinitionId"+processDefinitionId);
		AppReply<Object> reply =new AppReply<Object>();
		Date effectiveDate = null;
		//转换时间格式
		if (StringUtils.isBlank(strEffectiveDate)) {
			try {
				effectiveDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(strEffectiveDate);
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		if (StringUtils.equals("active", state)) {
			repositoryService
					.activateProcessDefinitionById(processDefinitionId,
							true, effectiveDate);
			reply.setCode(AppReply.SUCCESS_CODE);
			reply.setObj("active");
			return reply;
		} else if (StringUtils.equals("suspend", state)) {
			repositoryService.suspendProcessDefinitionById(processDefinitionId,
					true, effectiveDate);
			reply.setCode(AppReply.SUCCESS_CODE);
			reply.setObj("suspend");
			return reply;
		}
		reply.setCode(AppReply.EORRO_CODE);
		reply.setObj("");
		return reply;
	}
	
    /**
     * 激活挂起接口
     * @param state
     * @param state
     * @param processDefinitionId
     * @return
     */
    @RequestMapping(value = "/update")
    @ResponseBody
    public AppReply<String> updateState(@RequestParam("state") String state,
                                        @RequestParam("processDefinitionId") String processDefinitionId) {
    	logger.info("request /workflow/processinstance/update/"+state+"/"+processDefinitionId);
    	AppReply<String> reply =new AppReply<String>();
    	if (state.equals("active")) {
            runtimeService.activateProcessInstanceById(processDefinitionId);
        	reply.setObj("已激活ID为[" + processDefinitionId + "]的流程实例。");
        } else if (state.equals("suspend")) {
            runtimeService.suspendProcessInstanceById(processDefinitionId);
            reply.setObj("已挂起ID为[" + processDefinitionId + "]的流程实例。");
        }
    	return reply;
    }
	
	/**
	 * <p>@Description: 删除已经部署流程</p>
	 * <p>@author wangze </p>
	 * <p>@param processInstanceId
	 * <p>@param deleteReason
	 * <p>@return    设定文件 </p>
	 * <p>返回类型 </p>
	 * <p>@throws </p>
	 */
	@RequestMapping(value = "deleteDeployment")
	@ResponseBody
	public AppReply<String> deleteDeployment(@RequestParam("deploymentId") String deploymentId) {
		logger.info("request /workflow/processinstance/deleteDeployment:deploymentId"+deploymentId);
		AppReply<String> reply =new AppReply<String>();
		try {
			Long runCount= historyService.createHistoricProcessInstanceQuery().deploymentId(deploymentId).count();
			if(runCount==0){
				repositoryService.deleteDeployment(deploymentId);
				reply.setCode(AppReply.SUCCESS_CODE);
				reply.setObj("true");
			}else{
				 throw new Exception("当前流程实例流程正在运行");
			}
		}catch (Exception e){
			reply.setCode(AppReply.EORRO_CODE);
			reply.setObj("false");
		}
		return reply;
	}
	
	/**
	 * <p>@Description: TODO(查看所有已部署的流程) </p>
	 * <p>@author 王泽</p>
	 * <p>@param @param request
	 * <p>@param @return    设定文件 </p>
	 * <p>@return ModelAndView    返回类型 </p>
	 * <p>@throws </p>
	 */
	@RequestMapping(value = "/getDeploied")
	@ResponseBody
	public AppReply<Object> getDeploied(HttpServletRequest request) {
		logger.info("workflow/processinstance/getDeploied/");
		AppReply<Object> reply = null;
		try {
			reply = new AppReply<Object>();
			PageData pd = new PageData(request);
			Map<String, Object> result = workflowProcessDefinitionService.getDeploied(pd);
			reply.setCode(AppReply.SUCCESS_CODE);
			reply.setObj(result);
			return reply;
		}catch (Exception e){
			reply.setCode(AppReply.EORRO_CODE);
			reply.setMsg("查看所有已部署的流程异常");
			logger.error("查看所有已部署的流程异常");
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * <p>@Description: 将已经部署的流程转换为model</p>
	 * <p>@author wangze </p>
	 * <p>@param processDefinitionId
	 * <p>@return
	 * <p>@throws UnsupportedEncodingException
	 * <p>@throws XMLStreamException    设定文件 </p>
	 * <p>返回类型 </p>
	 * <p>@throws </p>
	 */
	@RequestMapping(value = "/process/convert-to-model")
	@ResponseBody
	public AppReply<String> convertToModel(@RequestParam("processDefinitionId") String processDefinitionId,
	                                       RedirectAttributes redirectAttributes)
			throws UnsupportedEncodingException, XMLStreamException {
		AppReply<String> reply = new AppReply<String>();
		ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
				.processDefinitionId(processDefinitionId).singleResult();
		
		if(processDefinition == null) {
			redirectAttributes.addAttribute("message", "该流程不支持转换！");
			reply.setCode(AppReply.EORRO_CODE);
			return reply;
		}
		
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
		
		return reply;
	}
}
