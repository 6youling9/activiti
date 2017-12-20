package controller.editor;

/**
 * <p>项目名称：bonc_kmioc_pbp   </p>
 * <p>类名称：***Controller   </p>
 * <p>类描述：  ***  </p>
 * <p>创建人：王泽(1215360909@qq.com)  </p>
 * <p>创建时间：${date} ${time}   </p>
 * <p>修改人：  ***   </p>
 * <p>修改时间：${date} ${time}   </p>
 * <p>修改备注：   </p>
 * <p>@version V0.1   </p>   
 */
//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.activiti.editor.constants.ModelDataJsonConstants;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Model;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class ModelEditorJsonRestResource implements ModelDataJsonConstants {
    //  protected static final Logger LOGGER = LoggerFactory.getLogger(org.activiti.rest.editor.model.ModelEditorJsonRestResource.class);
    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private ObjectMapper objectMapper;
    
    public ModelEditorJsonRestResource() {
    }
    
    //打开模型编辑页面
    @RequestMapping(value = {"/service/openEditor/{modelId}"})
    public String openEditor(@PathVariable String modelId) {
        return "redirect:../../views/process-editor/modeler.html?modelId=" + modelId;
    }
    
    @RequestMapping(
            value = {"/service/model/{modelId}/json"},
            method = {RequestMethod.GET},
            produces = {"application/json"}
    )
    @ResponseBody
    public ObjectNode getEditorJson(@PathVariable String modelId) {
        ObjectNode modelNode = null;
        Model model = this.repositoryService.getModel(modelId);
        if (model != null) {
            try {
                if (StringUtils.isNotEmpty(model.getMetaInfo())) {
                    modelNode = (ObjectNode) this.objectMapper.readTree(model.getMetaInfo());
                } else {
                    modelNode = this.objectMapper.createObjectNode();
                    modelNode.put("name", model.getName());
                }
                
                modelNode.put("modelId", model.getId());
                ObjectNode editorJsonNode = (ObjectNode) this.objectMapper.readTree(new String(this.repositoryService.getModelEditorSource(model.getId()), "utf-8"));
                modelNode.put("model", editorJsonNode);
            } catch (Exception var5) {
                //  LOGGER.error("Error creating model JSON", var5);
                throw new ActivitiException("Error creating model JSON", var5);
            }
        }
        
        return modelNode;
    }
}

