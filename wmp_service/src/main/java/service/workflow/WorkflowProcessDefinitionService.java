package service.workflow;

import com.bonc.ioc.common.util.Object2Map;
import com.bonc.ioc.common.util.PageData;
import com.bonc.ioc.common.util.StringUtil;
import com.bonc.ioc.common.util.WorkflowUtils;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.NativeProcessDefinitionQuery;
import org.activiti.engine.repository.ProcessDefinition;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipInputStream;

/**
 * 工作流中流程以及流程实例相关Service
 *
 * @author HenryYan
 */
@Service
public class WorkflowProcessDefinitionService {
    
    protected Logger logger = LoggerFactory.getLogger(getClass());
    
    @Autowired
    protected RuntimeService runtimeService;
    
    @Autowired
    protected RepositoryService repositoryService;
    
    @Autowired
    protected HistoryService historyService;
    
    /**
     * 根据流程实例ID查询流程定义对象{@link ProcessDefinition}
     *
     * @param processInstanceId 流程实例ID
     * @return 流程定义对象{@link ProcessDefinition}
     */
    public ProcessDefinition findProcessDefinitionByPid(String processInstanceId) {
        HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
        String processDefinitionId = historicProcessInstance.getProcessDefinitionId();
        ProcessDefinition processDefinition = findProcessDefinition(processDefinitionId);
        return processDefinition;
    }
    
    /**
     * 根据流程定义ID查询流程定义对象{@link ProcessDefinition}
     *
     * @param processDefinitionId 流程定义对象ID
     * @return 流程定义对象{@link ProcessDefinition}
     */
    public ProcessDefinition findProcessDefinition(String processDefinitionId) {
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionId(processDefinitionId).singleResult();
        return processDefinition;
    }
    
    /**
     * 部署classpath下面的流程定义
     * <p>
     * 从属性配置文件中获取属性<b>workflow.modules</b>扫描**deployments**
     * </p>
     * <p>
     * 然后从每个**deployments/${module}**查找在属性配置文件中的属性**workflow.module.keys.${
     * submodule}**
     * <p>
     * 配置实例：
     * <p/>
     * <pre>
     * #workflow for deploy
     * workflow.modules=budget,erp,oa
     * workflow.module.keys.budget=budget
     * workflow.module.keys.erp=acceptInsurance,billing,effectInsurance,endorsement,payment
     * workflow.module.keys.oa=caruse,leave,officalstamp,officesupply,out,overtime
     * </pre>
     * <p/>
     * </p>
     *
     * @param processKey 流程定义KEY
     * @throws Exception
     */
    public void deployFromClasspath(String exportDir, String... processKey) throws Exception {
        ResourceLoader resourceLoader = new DefaultResourceLoader();
        String[] processKeys = {"leave", "leave-dynamic-from", "leave-formkey", "dispatch"};
        for (String loopProcessKey : processKeys) {

      /*
       * 需要过滤指定流程
       */
            if (ArrayUtils.isNotEmpty(processKey)) {
                if (ArrayUtils.contains(processKey, loopProcessKey)) {
                    logger.debug("hit module of {}", (Object[]) processKey);
                    deploySingleProcess(resourceLoader, loopProcessKey, exportDir);
                } else {
                    logger.debug("module: {} not equals process key: {}, ignore and continue find next.", loopProcessKey, processKey);
                }
            } else {
        /*
         * 所有流程
         */
                deploySingleProcess(resourceLoader, loopProcessKey, exportDir);
            }
        }
    }
    
    /**
     * 部署单个流程定义
     *
     * @param resourceLoader {@link ResourceLoader}
     * @param processKey     模块名称
     * @throws IOException 找不到zip文件时
     */
    private void deploySingleProcess(ResourceLoader resourceLoader, String processKey, String exportDir) throws IOException {
        String classpathResourceUrl = "classpath:/deployments/" + processKey + ".bar";
        logger.debug("read workflow from: {}", classpathResourceUrl);
        Resource resource = resourceLoader.getResource(classpathResourceUrl);
        InputStream inputStream = resource.getInputStream();
        if (inputStream == null) {
            logger.warn("ignore deploy workflow module: {}", classpathResourceUrl);
        } else {
            logger.debug("finded workflow module: {}, deploy it!", classpathResourceUrl);
            ZipInputStream zis = new ZipInputStream(inputStream);
            Deployment deployment = repositoryService.createDeployment().addZipInputStream(zis).deploy();
            
            // export diagram
            List<ProcessDefinition> list = repositoryService.createProcessDefinitionQuery().deploymentId(deployment.getId()).list();
            for (ProcessDefinition processDefinition : list) {
                WorkflowUtils.exportDiagramToFile(repositoryService, processDefinition, exportDir);
            }
        }
    }
    
    /**
     * 重新部署单个流程定义
     *
     * @param processKey 流程定义KEY
     * @throws Exception
     * @see #deployFromClasspath
     */
    public void redeploy(String exportDir, String... processKey) throws Exception {
        this.deployFromClasspath(exportDir, processKey);
    }
    
    /**
     * 重新部署所有流程定义，调用：{@link #deployFromClasspath(String exportDir)}完成功能
     *
     * @throws Exception
     * @see #deployFromClasspath
     */
    public void deployAllFromClasspath(String exportDir) throws Exception {
        this.deployFromClasspath(exportDir);
    }
    
    /**
     * <p>@Description: TODO(查看所有已部署的流程) </p>
     * <p>@author Shy</p>
     * <p>@param @param attId    设定文件 </p>
     * <p>@return void    返回类型 </p>
     * <p>@throws </p>
     */
    public Map<String, Object> getDeploied(PageData pd) {
        NativeProcessDefinitionQuery nativeProcessDefinitionQuery = repositoryService.createNativeProcessDefinitionQuery();
        Map<String, Object> result = new HashMap<String, Object>();
        StringBuilder sql = new StringBuilder(300);
            sql.append("SELECT ")
                    .append("pro.ID_ AS ID_,")
                    .append("pro.REV_ AS REV_,")
                    .append("pro.NAME_ AS NAME_,")
                    .append("pro.KEY_ AS KEY_,")
                    .append("pro.VERSION_ AS VERSION_," )
                    .append("pro.DEPLOYMENT_ID_ AS DEPLOYMENT_ID_,")
                    .append("pro.RESOURCE_NAME_ AS RESOURCE_NAME_,")
                    .append("pro.DGRM_RESOURCE_NAME_ AS DGRM_RESOURCE_NAME_," )
                    .append("pro.DESCRIPTION_ AS DESCRIPTION_,")
                    .append("pro.HAS_START_FORM_KEY_ AS HAS_START_FORM_KEY_,")
                    .append("pro.HAS_GRAPHICAL_NOTATION_ AS HAS_GRAPHICAL_NOTATION_,")
                    .append("pro.SUSPENSION_STATE_ AS SUSPENSION_STATE_,")
                    .append("pro.TENANT_ID_ AS TENANT_ID_,")
                    .append("dep.DEPLOY_TIME_ AS CATEGORY_ " )
                    .append("FROM ")
                    .append("act_re_procdef pro ")
                    .append("LEFT JOIN act_re_deployment dep ON pro.DEPLOYMENT_ID_ = dep.ID_ ")
                    .append("ORDER BY pro.SUSPENSION_STATE_ ASC  , pro.CATEGORY_ DESC ");
  
        if (StringUtil.isBlank(pd.getString("pageSize")) && StringUtil.isBlank(pd.getString("pageNumber"))) {
            int pageSize = Integer.valueOf(pd.getString("pageSize"));
            int pageNumber = Integer.valueOf(pd.getString("pageNumber"));
            List<ProcessDefinition> hisProcessInstList = nativeProcessDefinitionQuery.sql(sql.toString()).listPage(pageSize * (pageNumber - 1), pageSize);
            long total = nativeProcessDefinitionQuery.sql(sql.toString()).list().size();
            result.put("total", total);
            result.put("rows", Object2Map.Obj2MapList(hisProcessInstList));
        }else{
            List<ProcessDefinition> hisProcessInstList = nativeProcessDefinitionQuery.sql(sql.toString()).list();
            result.put("rows", Object2Map.Obj2MapList(hisProcessInstList));
        }
        return result;
    }
    
}
