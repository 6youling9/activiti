package controller.workflow;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import common.util.AppReply;
import common.util.PageData;
import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.editor.constants.ModelDataJsonConstants;
import org.activiti.editor.language.json.converter.BpmnJsonConverter;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.Model;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.util.List;

/**
 * @project_name：bonc_ycioc_omp
 * @package_name：com.bonc.ioc.wmp.controller.workflow
 * @describe：模型控制器
 * @creater wangze (1215360909@qq.com)
 * @creat_time 2017-9-6 19:53
 * @changer   wangze
 * @change_time 2017-9-6 19:53
 * @remark
 * @version V0.1
 */
@Controller
@RequestMapping(value = "/workflow/model")
public class ModelController {

    protected Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    RepositoryService repositoryService;
    
    /**
     * 获取部署列表
     * @param request
     * @return
     */
    @RequestMapping(value = "list",method = RequestMethod.POST)
    @ResponseBody
    public AppReply<PageData> modelListJson(HttpServletRequest request) {
    	AppReply<PageData> reply = new AppReply<PageData>();
    	PageData result = new PageData();
        List<Model> list = repositoryService.createModelQuery().orderByLastUpdateTime().desc().list();
        result.put("total",list.size());
        result.put("rows",list);
        reply.setObj(result);
        return reply;
    }

    /**
     * 创建模型
     */
    @SuppressWarnings("deprecation")
	@RequestMapping(value = "create")
    @ResponseBody
    public AppReply<String> createJson(@RequestParam("name") String name, @RequestParam("key") String key, @RequestParam("description") String description , HttpServletRequest request , HttpServletResponse response) {
    	AppReply<String> reply = new AppReply<String>();
    	try {
            ObjectMapper objectMapper = new ObjectMapper();
            ObjectNode editorNode = objectMapper.createObjectNode();
            editorNode.put("id", "canvas");
            editorNode.put("resourceId", "canvas");
            ObjectNode stencilSetNode = objectMapper.createObjectNode();
            stencilSetNode.put("namespace", "http://b3mn.org/stencilset/bpmn2.0#");
            editorNode.put("stencilset", stencilSetNode);
            Model modelData = repositoryService.newModel();
            
            ObjectNode modelObjectNode = objectMapper.createObjectNode();
            modelObjectNode.put(ModelDataJsonConstants.MODEL_NAME, name);
            modelObjectNode.put(ModelDataJsonConstants.MODEL_REVISION, 1);
            description = StringUtils.defaultString(description);
            modelObjectNode.put(ModelDataJsonConstants.MODEL_DESCRIPTION, description);
            modelData.setMetaInfo(modelObjectNode.toString());
            modelData.setName(name);
            modelData.setKey(StringUtils.defaultString(key));
            
            repositoryService.saveModel(modelData);
            repositoryService.addModelEditorSource(modelData.getId(), editorNode.toString().getBytes("utf-8"));
            
            reply.setObj(request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+request.getContextPath()+"/" + "/views/process-editor/modeler.html?modelId=" + modelData.getId());
            reply.setMsg("创建流程模型成功");
            return reply;
        } catch (Exception e) {
            logger.error("创建模型失败：", e);
            reply.setMsg("创建模型失败");
            reply.setCode(AppReply.EORRO_CODE);
            return reply;
        }
    }

    /**
     * 根据Model部署流程_json
     */
    @RequestMapping(value = "deploy/{modelId}")
    @ResponseBody
    public AppReply<String> deploy(@PathVariable("modelId") String modelId, RedirectAttributes redirectAttributes) {
    	AppReply<String> reply=new AppReply<String>();
    	try {
            Model modelData = repositoryService.getModel(modelId);
            ObjectNode modelNode = (ObjectNode) new ObjectMapper().readTree(repositoryService.getModelEditorSource(modelData.getId()));
            byte[] bpmnBytes = null;

            BpmnModel model = new BpmnJsonConverter().convertToBpmnModel(modelNode);
            bpmnBytes = new BpmnXMLConverter().convertToXML(model);

            String processName = modelData.getName() + ".bpmn20.xml";
            Deployment deployment = repositoryService.createDeployment().name(modelData.getName()).addString(processName, new String(bpmnBytes,"utf-8")).deploy();
            reply.setObj("根据模型部署流程成功，部署id为"+deployment.getId());
            return reply;
        } catch (Exception e) {
            logger.error("根据模型部署流程失败：modelId={}", modelId, e);
            reply.setCode(AppReply.EORRO_CODE);
            reply.setObj("根据模型部署流程失败!!!");
            return reply;
        }
    }

    /**
     * 删除model-json
     */
    @RequestMapping(value = "delete/{modelId}")
    @ResponseBody
    public AppReply<String> deleteJson(@PathVariable("modelId") String modelId) {
    	AppReply<String> reply= new AppReply<String>();
    	try {
    		repositoryService.deleteModel(modelId);
    		reply.setObj("流程部署删除成功!!!");
		} catch (Exception e) {
			logger.error("流程部署删除失败{}",e);
			reply.setObj("流程部署删除失败!!!");
		}
        return reply;
    }
    
    /**
     * 导出model对象为指定类型
     *
     * @param modelId 模型ID
     * @param type    导出文件类型(bpmn\json)
     */
    @RequestMapping(value = "export/{modelId}/{type}")
    public void export(@PathVariable("modelId") String modelId, @PathVariable("type") String type, HttpServletResponse response) {
        try {
            Model modelData = repositoryService.getModel(modelId);
            BpmnJsonConverter jsonConverter = new BpmnJsonConverter();
            byte[] modelEditorSource = repositoryService.getModelEditorSource(modelData.getId());

            JsonNode editorNode = new ObjectMapper().readTree(modelEditorSource);
            BpmnModel bpmnModel = jsonConverter.convertToBpmnModel(editorNode);

            // 处理异常
            if (bpmnModel.getMainProcess() == null) {
                response.setStatus(HttpStatus.UNPROCESSABLE_ENTITY.value());
                response.getOutputStream().println("no main process, can't export for type: " + type);
                response.flushBuffer();
                return;
            }

            String filename = "";
            byte[] exportBytes = null;

            String mainProcessId = bpmnModel.getMainProcess().getId();

            if (type.equals("bpmn")) {

                BpmnXMLConverter xmlConverter = new BpmnXMLConverter();
                exportBytes = xmlConverter.convertToXML(bpmnModel);

                filename = mainProcessId + ".bpmn20.xml";
            } else if (type.equals("json")) {

                exportBytes = modelEditorSource;
                filename = mainProcessId + ".json";

            }

            ByteArrayInputStream in = new ByteArrayInputStream(exportBytes);
            IOUtils.copy(in, response.getOutputStream());

            response.setHeader("Content-Disposition", "attachment; filename=" + filename);
            response.flushBuffer();
        } catch (Exception e) {
            logger.error("导出model的xml文件失败:{}", e);
        }
    }
}
