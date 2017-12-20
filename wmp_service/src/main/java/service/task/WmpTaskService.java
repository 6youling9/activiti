package service.task;

import com.bonc.ioc.common.base.dao.BaseDao;
import com.bonc.ioc.common.base.service.BaseService;
import com.bonc.ioc.common.util.PageData;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class WmpTaskService extends BaseService {
    
    @Resource
    private BaseDao dao;
    @Autowired
    protected RuntimeService runtimeService;
    @Autowired
    protected RepositoryService repositoryService;
    
    /**
     * @Description: 根据各种条件查询审批节点信息
     * @method_name: approval
     * @author wangze
     * @param pd
     * @date 2017/9/8 22:35
     * @return java.util.List<com.bonc.ioc.common.util.PageData>
     * @throws Exception
     */
    public List<PageData> approval(PageData pd) throws Exception {
        Object list = dao.findForList("TaskMapper.selectAreaInfo", pd);
        return (List<PageData>) list;
    }
    
    /**
     * @Description: 待办以及被驳回
     * @method_name: approval
     * @author wangze
     * @param pd
     * @date 2017/9/8 22:35
     * @return java.util.List<com.bonc.ioc.common.util.PageData>
     * @throws Exception
     */
    public List<String> backlog(PageData pd) throws Exception {
        Object list = dao.findForList("TaskMapper.backlog", pd);
        return (List<String>)list;
    }
    
    /**
     * @Description: 已办
     * @method_name: approval
     * @author wangze
     * @param pd
     * @date 2017/9/8 22:35
     * @return java.util.List<com.bonc.ioc.common.util.PageData>
     * @throws Exception
     */
    public List<String> haveDone(PageData pd) throws Exception {
        Object list = dao.findForList("TaskMapper.haveDone", pd);
        return (List<String>)list;
    }
    
    /**
     * @Description: 根据任务id查询办理人员
     * @method_name: approval
     * @author wangze
     * @param pd
     * @date 2017/9/8 22:35
     * @return java.util.List<com.bonc.ioc.common.util.PageData>
     * @throws Exception
     */
    public List<String> getApplayerByTask(PageData pd) throws Exception {
        Object list = dao.findForList("TaskMapper.getApplayerByTask", pd);
        return (List<String>)list;
    }
}
