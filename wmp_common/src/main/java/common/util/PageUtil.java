package common.util;

import org.activiti.engine.task.Comment;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @project_name：bonc_ycioc_flow
 * @package_name：com.bonc.ioc.common.util
 * @describe：***
 * @creater wangze (1215360909@qq.com) 
 * @creat_time 2017/9/7 11:06 
 * @changer   ***  
 * @change_time 2017/9/7 11:06 
 * @remark   ***
 * @version V0.1
 */
public class PageUtil {
    
    private Map pd = new HashMap();
    private final static String PAGESIZENAME = "pageSize";//默认的分页变量的名字
    private final static String PAGENUMNAME = "pageNum";//默认的分页变量的名字
    private final static int PAGESIZE = 10;//默认每页10条
    private final static int PAGENUM = 1;//默认第一页
    
    public static Integer[] init(Map pd) {
        return init(pd, PAGESIZENAME, PAGENUMNAME);
    }
    
    public static Integer[] init(HttpServletRequest request) {
        return init(new PageData(request), PAGESIZENAME, PAGENUMNAME);
    }
    
    public static Integer[] init(HttpServletRequest request, String pageSizeName, String pageNumName) {
        return init(new PageData(request), pageSizeName, pageNumName);
    }
    
    public static Integer[] init(Map pd, String pageSizeName, String pageNumName) {
        Integer[] page = new Integer[3];
        Integer pageSize = Integer.valueOf((String) pd.get(pageSizeName));
        Integer pageNum = Integer.valueOf((String) pd.get(pageNumName));
        if (pageSize == null && pageNum == null) {
            //如果起始页和页面条数的信息都没有，则认为不需要进行分页
            return null;
        } else if (pageSize == null || pageNum == null) {
            //如果起始页和页面条数的信息缺了一个，则使用默认配置
            page[0] = PAGESIZE * (PAGENUM - 1);
            page[1] = PAGESIZE;
            page[2] = PAGESIZE * PAGENUM;
        } else {
            //正常的分页计算
            page[0] = pageSize * (pageNum - 1);
            page[1] = pageSize;
            page[2] = pageSize * pageNum;
        }
        return page;
    }
    
    /** 后台分页神器，自动判断要不要分页 **/
    public static List paging(Map pd, List list) {
        return paging(pd, PAGESIZENAME, PAGENUMNAME, list);
    }
    
    public static List paging(HttpServletRequest request, List list) {
        return paging(new PageData(request), PAGESIZENAME, PAGENUMNAME, list);
    }
    
    public static List paging(HttpServletRequest request, String pageSizeName, String pageNumName, List list) {
        return paging(new PageData(request), pageSizeName, pageNumName, list);
    }
    
    public static List paging(Map pd, String pageSizeName, String pageNumName, List list) {
        List result = new ArrayList<Comment>();
        Integer[] pageinfo = init(pd, pageSizeName, pageNumName);
        if (pageinfo != null) {
            for (int i = pageinfo[0]; i < pageinfo[3]; i++) {
                result.add(list.get(i));
            }
        } else {
            result = list;
        }
        return result;
    }
}
