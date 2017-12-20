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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;


@Controller
public class ModelSaveRestResource implements ModelDataJsonConstants {
   // protected static final Logger LOGGER = LoggerFactory.getLogger(org.activiti.rest.editor.model.ModelSaveRestResource.class);
    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private ObjectMapper objectMapper;
    
    public ModelSaveRestResource() {
    }
    
    @RequestMapping(
            value = {"/service/model/{modelId}/save"},
            method = {RequestMethod.PUT}
    )
    @ResponseStatus(HttpStatus.OK)
    public void saveModel(@PathVariable String modelId, @RequestBody MultiValueMap<String, String> values) {
        try {
            Model model = this.repositoryService.getModel(modelId);
            ObjectNode modelJson = (ObjectNode)this.objectMapper.readTree(model.getMetaInfo());
            modelJson.put("name", (String)values.getFirst("name"));
            modelJson.put("description", (String)values.getFirst("description"));
            model.setMetaInfo(modelJson.toString());
            model.setName((String)values.getFirst("name"));
            this.repositoryService.saveModel(model);
            this.repositoryService.addModelEditorSource(model.getId(), ((String)values.getFirst("json_xml")).getBytes("utf-8"));
            InputStream svgStream = new ByteArrayInputStream(((String)values.getFirst("svg_xml")).getBytes("utf-8"));
            TranscoderInput input = new TranscoderInput(svgStream);
            PNGTranscoder transcoder = new PNGTranscoder();
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            TranscoderOutput output = new TranscoderOutput(outStream);
            transcoder.transcode(input, output);
            byte[] result = outStream.toByteArray();
            this.repositoryService.addModelEditorSourceExtra(model.getId(), result);
            outStream.close();
        } catch (Exception var11) {
          //  LOGGER.error("Error saving model", var11);
            throw new ActivitiException("Error saving model", var11);
        }
    }
}

