package controller.workflow;

import com.bonc.ioc.common.base.web.BaseController;
import org.activiti.engine.IdentityService;
import org.activiti.engine.TaskService;
import org.activiti.engine.task.Attachment;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * @project_name：bonc_ycioc_omp
 * @package_name：AttachmentController
 * @describe：附件控制器
 * @creater wangze (1215360909@qq.com)
 * @creat_time 2017-9-6 19:53
 * @changer wangze
 * @change_time 2017-9-6 19:53
 * @remark
 * @version V0.1
 */
@Controller
@RequestMapping(value = "/AttachmentController")
public class AttachmentController extends BaseController {
    
    @Autowired
    TaskService taskService;
    
    @Autowired
    IdentityService identityService;
    
    /**
     * @Description: TODO(非启动阶段文件类型的附件)
     * @method_name: newFile
     * @author wangze
     * @param taskId
     * @param processInstanceId
     * @param attachmentName
     * @param attachmentDescription
     * @param file
     * @param session
     * @date 2017/9/6 19:55
     * @return int
     */
    @RequestMapping(value = "new/file")
    @ResponseBody
    public int newFile(@RequestParam("taskId") String taskId,
                       @RequestParam(value = "processInstanceId", required = false) String processInstanceId,
                       @RequestParam("attachmentName") String attachmentName,
                       @RequestParam(value = "attachmentDescription", required = false) String attachmentDescription,
                       @RequestParam("file") CommonsMultipartFile file, HttpSession session) {
        
        try {
            uploadFile(taskId, processInstanceId, attachmentName, attachmentDescription, file,"admin");
            return 1;
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
        
    }
    
    /**
     * @Description: TODO(启动阶段上传文件，支持多文件上传，保存网址)
     * @method_name: startUploadFileAndSaveWeb
     * @author wangze
     * @param redirectAttributes
     * @date 2017/9/6 19:55
     * @return int
     */
    @RequestMapping(value = "/uploadFileAndSaveWeb")
    @ResponseBody
    public int startUploadFileAndSaveWeb(RedirectAttributes redirectAttributes) {
        
        try {
            String taskId = (String) request.getAttribute("taskId");
            String processInstanceId = (String) request.getAttribute("processInstanceId");
            
            String attachmentNameFile = (String) request.getAttribute("attachmentNameFile");
            String attachmentDescriptionFile = (String) request.getAttribute("attachmentDescriptionFile");
            
            String attachmentNameWeb = (String) request.getAttribute("attachmentNameWeb");
            String attachmentDescriptionWeb = (String) request.getAttribute("attachmentDescriptionWeb");
            
            CommonsMultipartFile[] files = (CommonsMultipartFile[]) request.getAttribute("file");
            String url = (String) request.getAttribute("url");
            String userId = (String) request.getAttribute("userId");
            
            if (files == null && url == null) {//没有附件
                return 0;
            }
            //Map<String, String> attachmentNamesMap = new HashMap<String, String>();
            for (CommonsMultipartFile file : files) {
                
                uploadFile(taskId, processInstanceId, attachmentNameFile,
                        attachmentDescriptionFile, file, userId);
                
            }
            //保存网址
            String attachmentType = "url";
            identityService.setAuthenticatedUserId("admin");
        /* Attachment attachment = */
            Attachment attachment = taskService.createAttachment(attachmentType, taskId,
                    processInstanceId, attachmentNameWeb, attachmentDescriptionWeb,
                    url);
            taskService.saveAttachment(attachment);//保存网址
            redirectAttributes.addAttribute("message", request.getAttribute("message"));
        
    /*
     * 如果要更新附件内容，先读取附件对象，然后设置属性（只能更新name和description），最后保存附件对象
     */
//    taskService.saveAttachment(attachment);
            return 1;
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
    }
    
    /**
     * @Description: TODO(URL类型的附件)
     * @method_name: newUrl
     * @author wangze
     * @param taskId
     * @param processInstanceId
     * @param attachmentName
     * @param attachmentDescription
     * @param url
     * @param session
     * @date 2017/9/6 19:57
     * @return int
     */
    @RequestMapping(value = "new/url")
    @ResponseBody
    public int newUrl(@RequestParam("taskId") String taskId,
                      @RequestParam(value = "processInstanceId", required = false) String processInstanceId,
                      @RequestParam("attachmentName") String attachmentName,
                      @RequestParam(value = "attachmentDescription", required = false) String attachmentDescription,
                      @RequestParam("url") String url, HttpSession session) {
        try {
            String attachmentType = "url";
            identityService.setAuthenticatedUserId("admin");
    /*Attachment attachment = */
            taskService.createAttachment(attachmentType, taskId, processInstanceId, attachmentName, attachmentDescription, url);
    /*
     * 如果要更新附件内容，先读取附件对象，然后设置属性（只能更新name和description），最后保存附件对象
     */
//    taskService.saveAttachment(attachment);
            return 1;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
    
    /**
     * @Description: TODO(删除附件或网址)
     * @method_name: delete
     * @author wangze
     * @param attachmentId
     * @date 2017/9/6 19:58
     * @return int
     */
    @RequestMapping(value = "delete")
    @ResponseBody
    public int delete(@RequestParam("attachmentId") String attachmentId) {
        try {
            taskService.deleteAttachment(attachmentId);
            return 1;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
    
    /**
     * @Description: TODO(下载附件)
     * @method_name: downloadFile
     * @author wangze
     * @param attachmentId
     * @param response
     * @date 2017/9/6 19:59
     * @return void
     * @throws Exception
     */
    @RequestMapping(value = "download")
    public void downloadFile(@RequestParam("attachmentId") String attachmentId, HttpServletResponse response) throws IOException {
        Attachment attachment = taskService.getAttachment(attachmentId);
        InputStream attachmentContent = taskService.getAttachmentContent(attachmentId);
        String contentType = StringUtils.substringBefore(attachment.getType(), ";");
        response.addHeader("Content-Type", contentType + ";charset=UTF-8");
        String extensionFileName = StringUtils.substringAfter(attachment.getType(), ";");
        String fileName = attachment.getName() + "." + extensionFileName;
        response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
        IOUtils.copy(new BufferedInputStream(attachmentContent), response.getOutputStream());
    }
    
    /**
     * @Description: TODO(上传单个文件)
     * @method_name: uploadFile
     * @author wangze
     * @date 2017/9/6 20:00
     * @return void
     * @throws IOException
     */
    private void uploadFile(String taskId, String processInstanceId,
                            String attachmentName, String attachmentDescription,
                            CommonsMultipartFile file, String userId) throws IOException {
        String attachmentType = file.getContentType() + ";" +
                FilenameUtils.getExtension(file.getOriginalFilename());
        identityService.setAuthenticatedUserId(userId);
        if (attachmentName == null || attachmentName == "" || attachmentName.length() <= 0)
            attachmentName = file.getOriginalFilename();
        Attachment attachment = taskService.createAttachment(attachmentType, taskId, processInstanceId, attachmentName, attachmentDescription,
                file.getInputStream());
        taskService.saveAttachment(attachment);
    }
    
    /**
     * @Description: TODO(数据分割)
     * @method_name: splitFileDesc
     * @author wangze
     * @date 2017/9/6 20:01
     * @return java.util.Map<java.lang.String,java.lang.String>
     */
    @SuppressWarnings("unused")
    @Deprecated
    private Map<String, String> splitFileDesc(String[] array, String sps, Map<String, String> returnMap) {
        for (String temp : array) {
            temp = temp.trim().replace(" ", "");
            String[] split = temp.split(sps);
            returnMap.put(split[0], split[1]);
        }
        return returnMap;
    }
}
