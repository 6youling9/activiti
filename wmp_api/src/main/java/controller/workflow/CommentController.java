package controller.workflow;


import common.util.Object2Map;
import org.activiti.engine.HistoryService;
import org.activiti.engine.IdentityService;
import org.activiti.engine.TaskService;
import org.activiti.engine.task.Comment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;

/**
 * @project_name：bonc_ycioc_omp
 * @package_name：CommentController
 * @describe：流程中意见及建议的增加和查询
 * @creater wangze (1215360909@qq.com)
 * @creat_time 2017-9-6 19:53
 * @changer wangze
 * @change_time 2017-9-6 19:53
 * @remark
 * @version V0.1
 */
@Controller
@RequestMapping(value = "/CommentController")
public class CommentController {
    @Autowired
    TaskService taskService;
    @Autowired
    IdentityService identityService;
    @Autowired
    HistoryService historyService;
    
    /**
     * @Description: TODO(新增意见)
     * @method_name: addComment
     * @author wangze
     * @param taskId
     * @param processInstanceId
     * @param message
     * @param session
     * @date 2017/9/6 21:22
     * @return int
     */
    @RequestMapping(value = "addComment", method = RequestMethod.POST)
    @ResponseBody
    public int addComment(
		    @RequestParam("taskId") String taskId,
		    @RequestParam(value = "processInstanceId", required = false) String processInstanceId,
		    @RequestParam("message") String message, HttpSession session) {
        try {
            identityService.setAuthenticatedUserId("admin");
            taskService.addComment(taskId, processInstanceId, message);
            return 1;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
    
    /**
     * @Description: TODO(批量删除某一节点或某一流程意见)
     * @method_name: deleteComments
     * @author wangze
     * @param taskId
     * @param processInstanceId
     * @param message
     * @param session
     * @date 2017/9/6 21:22
     * @return int
     */
    @RequestMapping(value = "deleteComment1", method = RequestMethod.POST)
    @ResponseBody
    public int deleteComments(
		    @RequestParam("taskId") String taskId,
		    @RequestParam(value = "processInstanceId", required = false) String processInstanceId,
		    @RequestParam("message") String message, HttpSession session) {
        try {
            identityService.setAuthenticatedUserId("admin");
            taskService.deleteComments(taskId, processInstanceId);
            return 1;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
    
    /**
     * @Description: TODO(删除一条意见)
     * @method_name: deleteComment
     * @author wangze
     * @param commentId
     * @param message
     * @param session
     * @date 2017/9/6 21:22
     * @return int
     */
    @RequestMapping(value = "deleteComment2", method = RequestMethod.POST)
    @ResponseBody
    public int deleteComment(
		    @RequestParam("commentId") String commentId,
		    @RequestParam("message") String message, HttpSession session) {
        try {
            identityService.setAuthenticatedUserId("admin");
            Comment comment = taskService.getComment(commentId);
            taskService.deleteComment(commentId);
            return 1;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
    
    /**
     * @Description: TODO(修改意见)
     * @method_name: deleteComment
     * @author wangze
     * @param commentId
     * @param message
     * @param session
     * @date 2017/9/6 21:22
     * @return int
     */
    @RequestMapping(value = "deleteComment3", method = RequestMethod.POST)
    @ResponseBody
    public int updateComment(
		    @RequestParam("commentId") String commentId,
		    @RequestParam("message") String message, HttpSession session) {
        try {
            identityService.setAuthenticatedUserId("admin");
            Comment comment = taskService.getComment(commentId);
            taskService.deleteComment(commentId);
            taskService.addComment(comment.getTaskId(), comment.getProcessInstanceId(), message);
            return 1;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
    
    /**
     * @Description: TODO(查询某一条意见)
     * @method_name: selectComment
     * @author wangze
     * @param commentId
     * @date 2017/9/6 21:22
     * @return Object
     */
    @RequestMapping(value = "selectComment", method = RequestMethod.POST)
    @ResponseBody
    public Object selectComment(
            @RequestParam("commentId") String commentId) {
        try {
            Comment comment = taskService.getComment(commentId);
            return Object2Map.Obj2Map(comment);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
}
