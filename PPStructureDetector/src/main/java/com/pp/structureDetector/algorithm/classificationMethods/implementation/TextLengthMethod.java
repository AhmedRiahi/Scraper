package com.pp.structureDetector.algorithm.classificationMethods.implementation;

import java.util.List;

import com.pp.framework.dataStructure.tree.Tree;
import com.pp.framework.dataStructure.tree.TreeNode;
import com.pp.structureDetector.abstractStructure.StatNode;
import com.pp.structureDetector.algorithm.classificationMethods.ClassificationMethod;
import com.pp.structureDetector.exception.NoCapacityToDetectException;

public class TextLengthMethod extends ClassificationMethod{

	private double threasholdPercentage = 70;
	
	public TextLengthMethod(Tree<StatNode> statTree) {
		super(statTree);
	}

	@Override
	public void init() {
		
	}
	
	@Override
	public TreeNode<StatNode> detectMainContainer(){
		TreeNode<StatNode> currentNode = this.statTree.getRootNode();
		TreeNode<StatNode> lastNode = null;
		
		do{
			lastNode = currentNode;
			StatNode parentStatNode = currentNode.getValue();
			for(TreeNode<StatNode> treeNode : currentNode.getChildren()){
				StatNode statNode = treeNode.getValue();
				double textLengthPercentage = (statNode.getFullTextLength()*1.0 / parentStatNode.getFullTextLength()) * 100;
				if(textLengthPercentage >= this.threasholdPercentage){
					lastNode = currentNode;
					currentNode = treeNode;
					break;
				}
			}
		}while(lastNode != currentNode);
		return currentNode;
	}

	@Override
	public List<TreeNode<StatNode>> detectRepetetionElements() throws NoCapacityToDetectException {
		throw new NoCapacityToDetectException(this.getClass().getName(),"Repetition elements detection");
	}

	@Override
	public TreeNode<StatNode> detectPagination() throws NoCapacityToDetectException {
		throw new NoCapacityToDetectException(this.getClass().getName(),"Pagination detection");
	}

	@Override
	public TreeNode<StatNode> detectMainNavigation() throws NoCapacityToDetectException {
		throw new NoCapacityToDetectException(this.getClass().getName(),"Main navigation detection");
	}

	
	// -------------------------------- GETTER / SETTER --------------------------------
	
	public double getThreasholdPercentage() {
		return threasholdPercentage;
	}

	public void setThreasholdPercentage(double threasholdPercentage) {
		this.threasholdPercentage = threasholdPercentage;
	}

}
