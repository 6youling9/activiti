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

import org.activiti.engine.ActivitiException;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.InputStream;

@Controller
public class StencilsetRestResource {
    public StencilsetRestResource() {
    }
    
    @RequestMapping(
            value = {"/service/editor/stencilset"},
            method = {RequestMethod.GET},
            produces = {"application/json;charset=utf-8"}
    )
    @ResponseBody
    public String getStencilset() {
        InputStream stencilsetStream = this.getClass().getClassLoader().getResourceAsStream("stencilset.json");
        
        try {
            return IOUtils.toString(stencilsetStream, "utf-8");
        } catch (Exception var3) {
            throw new ActivitiException("Error while loading stencil set", var3);
        }
    }
}

