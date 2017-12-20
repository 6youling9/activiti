package service.editor;

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

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProcessInstanceDiagramLayoutResource extends BaseProcessDefinitionDiagramLayoutResource {
    public ProcessInstanceDiagramLayoutResource() {
    }
    
    @RequestMapping(
            value = {"/process-instance/{processInstanceId}/diagram-layout"},
            method = {RequestMethod.GET},
            produces = {"application/json"}
    )
    public ObjectNode getDiagram(@PathVariable String processInstanceId) {
        return this.getDiagramNode(processInstanceId, (String)null);
    }
}

