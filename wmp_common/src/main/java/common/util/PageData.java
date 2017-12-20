package common.util;

import net.sf.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import java.util.*;


public class PageData extends HashMap implements Map {
    
    private static final long serialVersionUID = 1L;
    
    Map map = null;
    HttpServletRequest request;
    
    public PageData(HttpServletRequest request) {
        this(request, true);
    }
    
    public PageData(HttpServletRequest request, boolean flag) {
        this.request = request;
        Map properties = request.getParameterMap();
        Map returnMap = new HashMap();
        Iterator entries = properties.entrySet().iterator();
        Entry entry;
        String name = "";
        String value = "";
        while (entries.hasNext()) {
            entry = (Entry) entries.next();
            name = (String) entry.getKey();
            if ("[]".equals(name.substring(name.length() - 2, name.length()))) {
                name = name.substring(0, name.length() - 2);
            }
            Object valueObj = entry.getValue();
            if (null == valueObj) {
                value = "";
            } else if (valueObj instanceof String[]) {
                String[] values = (String[]) valueObj;
                if (!flag) {
                    for (int i = 0; i < values.length; i++) {
                        value += "'" + values[i] + "',";
                    }
                    value = value.substring(0, value.length() - 1);
                    returnMap.put(name, value);
                    continue;
                } else {
                    if (values != null && values.length == 1) {
                        returnMap.put(name, values[0]);
                    } else {
                        returnMap.put(name, values);
                    }
                    continue;
                }
            } else {
                value = valueObj.toString();
            }
            returnMap.put(name, value);
        }
        map = returnMap;
        Object pageSize = map.get("pageSize");
        Object pageNum = map.get("pageNum");
        if (pageSize != null && pageNum != null && pageSize instanceof Integer && pageNum instanceof Integer) {
            returnMap.put("startNum", (int) pageSize * ((int) pageNum - 1));
            returnMap.put("endNum", (int) pageSize * (int) pageNum);
        }
    }
    
    public PageData() {
        map = new HashMap();
    }
    
    @Override
    public Object get(Object key) {
        Object obj = null;
        if (map.get(key) instanceof Object[]) {
            Object[] arr = (Object[]) map.get(key);
            obj = arr;
        } else {
            obj = map.get(key);
        }
        return obj;
    }
    
    public String getString(Object key) {
        return String.valueOf(get(key));
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public Object put(Object key, Object value) {
        return map.put(key, value);
    }
    
    @Override
    public Object remove(Object key) {
        return map.remove(key);
    }
    
    public void clear() {
        map.clear();
    }
    
    public boolean containsKey(Object key) {
        // TODO Auto-generated method stub
        return map.containsKey(key);
    }
    
    public boolean containsValue(Object value) {
        // TODO Auto-generated method stub
        return map.containsValue(value);
    }
    
    public Set entrySet() {
        // TODO Auto-generated method stub
        return map.entrySet();
    }
    
    public boolean isEmpty() {
        // TODO Auto-generated method stub
        return map.isEmpty();
    }
    
    public Set keySet() {
        // TODO Auto-generated method stub
        return map.keySet();
    }
    
    @SuppressWarnings("unchecked")
    public void putAll(Map t) {
        // TODO Auto-generated method stub
        map.putAll(t);
    }
    
    public int size() {
        // TODO Auto-generated method stub
        return map.size();
    }
    
    public Collection values() {
        // TODO Auto-generated method stub
        return map.values();
    }
    
    public static JSONObject toJson(PageData pd) {
        JSONObject json = JSONObject.fromObject(pd);
        return json;
    }
}
