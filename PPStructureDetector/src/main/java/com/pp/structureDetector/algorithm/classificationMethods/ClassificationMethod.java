package com.pp.structureDetector.algorithm.classificationMethods;

import com.pp.framework.dataStructure.tree.Tree;
import com.pp.framework.dataStructure.tree.TreeNode;
import com.pp.structureDetector.abstractStructure.StatNode;
import com.pp.structureDetector.exception.NoCapacityToDetectException;

import java.util.List;

public abstract class ClassificationMethod {
	
	protected Tree<StatNode> statTree;
	
	public ClassificationMethod(Tree<StatNode> statTree){
		this.statTree = statTree;
	}
	
	public abstract void init();
	public abstract TreeNode<StatNode> detectMainContainer() throws NoCapacityToDetectException;
	public abstract List<TreeNode<StatNode>> detectRepetetionElements() throws NoCapacityToDetectException;
	public abstract TreeNode<StatNode> detectPagination() throws NoCapacityToDetectException;
	public abstract TreeNode<StatNode> detectMainNavigation() throws NoCapacityToDetectException;
}
