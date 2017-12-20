package common.util;

import java.lang.reflect.Field;
import java.util.*;

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
public class Object2Map {
    
    final static private int DEPT = 2;//默认挖掘熟读为2
    private int dept = DEPT;//实际挖掘深度
    
    public static List<Map<String, Object>> Obj2MapList(List list) {
        Object2Map object2Map = new Object2Map();
        object2Map.setDept(DEPT);
        return object2Map.Obj2MapList(0,list);
    }
    
    public static List<Map<String, Object>> Obj2MapList(List list,int dept) {
        Object2Map object2Map = new Object2Map();
        object2Map.setDept(DEPT);
        return object2Map.Obj2MapList(0,list);
    }
    
    private List<Map<String, Object>> Obj2MapList(int num,List list) {
        List<Map<String, Object>> mapList = new ArrayList<Map<String, Object>>();
        for (Object obj : list) {
            mapList.add(Obj2Map(num,obj));
        }
        return mapList;
    }
    
    public static Map<String, Object> Obj2Map(Object obj) {
        Object2Map object2Map = new Object2Map();
        object2Map.setDept(DEPT);
        return object2Map.Obj2Map(0,obj);
    }
    
    public static Map<String, Object> Obj2Map(Object obj,int dept) {
        Object2Map object2Map = new Object2Map();
        object2Map.setDept(dept);
        return object2Map.Obj2Map(0,obj);
    }
    
    private Map<String, Object> Obj2Map(int num,Object obj) {
        //限制迭代不能超过挖掘深度，避免死循环
        if(num>dept){
            return null;
        }
        Map<String, Object> reMap = new HashMap<>();
        if (obj == null)
            return null;
        try {
            try {
                Field[] fields1 = obj.getClass().getDeclaredFields();
                for (int i = 0; i < fields1.length; i++) {
                    Field f = obj.getClass().getDeclaredField(fields1[i].getName());
                    f.setAccessible(true);
                    Object o = f.get(obj);
                    if (isBaseType(o)||o==null) {
                        if(!reMap.containsKey(fields1[i].getName())) {
                            reMap.put(fields1[i].getName(), o);
                        }
                    }else{
                        if(o instanceof List){
                            if(!reMap.containsKey(fields1[i].getName())) {
                                reMap.put(fields1[i].getName(), Obj2MapList(num + 1, (List) o));
                            }
                        }else {
                            if(!reMap.containsKey(fields1[i].getName())) {
                                reMap.put(fields1[i].getName(), Obj2Map(num + 1, o));
                            }
                        }
                    }
                }
                hasSupperClass(obj,obj.getClass(),reMap,num);
            } catch (NoSuchFieldException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } catch (SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return reMap;
    }
    
    private void hasSupperClass(Object obj, Class cls, Map<String, Object> reMap, int num) throws NoSuchFieldException, IllegalAccessException {
        if (cls.getSuperclass() != null) {
            Class superClass = cls.getSuperclass();
            Field[] fields = superClass.getDeclaredFields();
            for (int i = 0; i < fields.length; i++) {
                Field f = superClass.getDeclaredField(fields[i].getName());
                f.setAccessible(true);
                Object o = f.get(obj);
                if (isBaseType(o)||o==null) {
                    if(!reMap.containsKey(fields[i].getName())) {
                        reMap.put(fields[i].getName(), o);
                    }
                }else{
                    if(o instanceof List){
                        if(!reMap.containsKey(fields[i].getName())) {
                            reMap.put(fields[i].getName(), Obj2MapList(num + 1, (List) o));
                        }
                    }else {
                        if(!reMap.containsKey(fields[i].getName())) {
                            reMap.put(fields[i].getName(), Obj2Map(num + 1, o));
                        }
                    }
                }
            }
            hasSupperClass(obj,superClass,reMap,num);
        }
    }
    
    //判断obj是否为基本数据类型
    public boolean isBaseType(Object obj) {
        if (obj instanceof Integer || obj instanceof Integer[]) {
            return true;
        } else if (obj instanceof Long || obj instanceof Long[]) {
            return true;
        } else if (obj instanceof Double || obj instanceof Double[]) {
            return true;
        } else if (obj instanceof Float || obj instanceof Float[]) {
            return true;
        } else if (obj instanceof String || obj instanceof String[]) {
            return true;
        } else if (obj instanceof Boolean || obj instanceof Boolean[]) {
            return true;
        } else if (obj instanceof Byte || obj instanceof Byte[]) {
            return true;
        } else if (obj instanceof Short || obj instanceof Short[]) {
            return true;
        } else if (obj instanceof Character || obj instanceof Character[]) {
            return true;
        } else if (obj instanceof Date || obj instanceof Date[]) {
            return true;
        } else if (obj instanceof List) {
            if(((List) obj).size()==0){
                return true;
            }
            for (Object o : (List) obj) {
                if (!isBaseType(o)||o!=null) {
                    return false;
                }
            }
            return true;
        } else if (obj instanceof Map) {
            Set<Map.Entry<String, Object>> entrySet = ((Map) obj).entrySet();
            for (Map.Entry<String, Object> entry : entrySet) {
                Object value = entry.getValue();
                if (!isBaseType(value)) {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }
    
    private void setDept(int dept){
        this.dept = dept;
    }
}
