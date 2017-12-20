package controller.workflow;

import com.bonc.ioc.common.util.PageUtil;
import org.activiti.engine.ManagementService;
import org.activiti.engine.event.EventLogEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @project_name：bonc_ycioc_omp
 * @package_name：EventLogController
 * @describe：全局事件日志
 * @creater wangze (1215360909@qq.com)
 * @creat_time 2017-9-6 19:53
 * @changer wangze
 * @change_time 2017-9-6 19:53
 * @remark
 * @version V0.1
 */
@Controller
@RequestMapping("/EventLogController")
public class EventLogController {
    
    @Autowired
    ManagementService managementService;

    /**
     * @Description: TODO(日志列表)
     * @method_name: eventLogList
     * @author wangze
     * @param processInstanceId
     * @param request
     * @date 2017/9/7 15:13
     * @return java.util.Map<java.lang.String,java.lang.Object>
     */
    @RequestMapping(value = "eventLogList", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> eventLogList(@RequestParam(value = "processInstanceId", required = false) String processInstanceId,
                                            HttpServletRequest request) {
        try {
            Map<String, Object> result = new HashMap<String, Object>();
            Map<Long, String> datas = new HashMap<Long, String>();
            List<EventLogEntry> allLogEntries = null;
            if (processInstanceId != null || !"null".equals(processInstanceId) || !"".equals(processInstanceId)) {
                allLogEntries = managementService.getEventLogEntries(0L, 100000L);
            } else {
                allLogEntries = managementService.getEventLogEntriesByProcessInstanceId(processInstanceId);
            }
            List<EventLogEntry> logEntries = PageUtil.paging(request, allLogEntries);
            
            for (EventLogEntry logEntry : logEntries) {
                datas.put(logEntry.getLogNumber(), new String(logEntry.getData(), "UTF-8"));
            }
    
            result.put("count", allLogEntries.size());
            result.put("datas", datas);
            
            return result;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }
    
}
