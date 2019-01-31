package com.pp.framework.dataStructure.tree;

public abstract class TreeNodeValue{

	protected TreeNode<TreeNodeValue> treeNode;
	
	// -------------------------------- GETTER / SETTER --------------------------------
	
	public TreeNode<TreeNodeValue> getTreeNode() {
		return treeNode;
	}

	public void setTreeNode(TreeNode<TreeNodeValue> treeNode) {
		this.treeNode = treeNode;
	}
}
