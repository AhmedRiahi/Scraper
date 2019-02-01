package com.pp.structureDetector.algorithm.classificationMethods.implementation;

import com.pp.framework.dataStructure.tree.Tree;
import com.pp.framework.dataStructure.tree.TreeNode;
import com.pp.structureDetector.abstractStructure.StatNode;
import com.pp.structureDetector.algorithm.classificationMethods.ClassificationMethod;
import com.pp.structureDetector.exception.NoCapacityToDetectException;

import java.util.List;

public class DomTagsMethod extends ClassificationMethod{

	public DomTagsMethod(Tree<StatNode> statTree) {
		super(statTree);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void init() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public TreeNode<StatNode> detectMainContainer() throws NoCapacityToDetectException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<TreeNode<StatNode>> detectRepetetionElements() throws NoCapacityToDetectException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TreeNode<StatNode> detectPagination() throws NoCapacityToDetectException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TreeNode<StatNode> detectMainNavigation() throws NoCapacityToDetectException {
		// TODO Auto-generated method stub
		return null;
	}

}
