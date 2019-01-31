package com.pp.structureDetector.algorithm.classificationMethods.implementation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.pp.framework.dataStructure.tree.Tree;
import com.pp.framework.dataStructure.tree.TreeNode;
import com.pp.structureDetector.abstractStructure.StatNode;
import com.pp.structureDetector.abstractStructure.WebContentTypes;
import com.pp.structureDetector.algorithm.classificationMethods.ClassificationMethod;
import com.pp.structureDetector.exception.NoCapacityToDetectException;

public class CssClassNamesMethod extends ClassificationMethod{

	private Map<WebContentTypes,String[]> cssNameskeywords;
	
	public CssClassNamesMethod(Tree<StatNode> statTree) {
		super(statTree);
		this.cssNameskeywords = new HashMap<>();
	}
	
	
	public void initCssNameskeywords(){
		this.cssNameskeywords.put(WebContentTypes.MAIN_CONTAINER,new String[]{
				"main-container",
				"container",
				"main-wrapper",
				"wrapper",
				"content-Area"
		});
		
		this.cssNameskeywords.put(WebContentTypes.NAVIGATION_MENU_CONTAINER,new String[]{
			"navigation-menu",
			"navigation",
			"main-menu",
			"menu"
		});
		
	}

	@Override
	public void init() {
		this.initCssNameskeywords();
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
