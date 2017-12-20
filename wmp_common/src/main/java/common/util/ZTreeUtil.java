package common.util;

import java.util.ArrayList;
import java.util.List;

public class ZTreeUtil {
	public static List<ZTreeNode> encapsulate(List<ZTreeNode> list) {
		if( list!=null && !list.isEmpty() ){ 
			List<ZTreeNode> roots = new ArrayList<ZTreeNode>();
			for (ZTreeNode node : list) {
				if( node.getPid()==null || "".equals(node.getPid()) ) {
					roots.add(node);
				}
			}
			for (ZTreeNode root : roots) {
				findChildNodes(root,list);
			}
			return roots;
		}
		return null;
	}
	private static void findChildNodes(ZTreeNode root,List<ZTreeNode> nodeList) {
		if(nodeList==null||nodeList.isEmpty()){
			return ;
		}
		for (ZTreeNode node : nodeList) {
			if( node.getPid()!=null && root.getId().equals(node.getPid()) ) {
				if(root.getChildren()==null){
					List<ZTreeNode> children = new ArrayList<ZTreeNode>();
					children.add(node);
					root.setChildren(children);
				}else{
					root.getChildren().add(node);
				}
			}
		}
		if(root.getChildren()!=null){
			for (ZTreeNode child : root.getChildren()) {
				findChildNodes(child,nodeList);
			}
		}
	}
	public static List<ZTreeNode> getZTree(String rootName){
		return getZTree(null,rootName,"0");
	}
	public static List<ZTreeNode> getZTree(List<ZTreeNode> ztree,String rootName, String rootId){
		if(ztree==null){
			ztree = new ArrayList<ZTreeNode>();
		}
		ZTreeNode root = new ZTreeNode();
		root.setId(rootId);
		root.setPid(null);
		root.setName(rootName);
		root.setType("root");
		ztree.add(root);
		return ztree;
	}
}