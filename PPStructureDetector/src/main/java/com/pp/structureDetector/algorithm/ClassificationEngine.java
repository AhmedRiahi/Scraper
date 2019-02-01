package com.pp.structureDetector.algorithm;

import com.pp.framework.dataStructure.tree.Tree;
import com.pp.framework.dataStructure.tree.TreeNode;
import com.pp.structureDetector.abstractStructure.ClassRate;
import com.pp.structureDetector.abstractStructure.StatNode;
import com.pp.structureDetector.abstractStructure.WebContentTypes;
import com.pp.structureDetector.algorithm.classificationMethods.implementation.CssClassRateMethod;
import com.pp.structureDetector.algorithm.classificationMethods.implementation.TextLengthMethod;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ClassificationEngine {

	private Tree<StatNode> statTree;
	private Element domTree;
	private TextLengthMethod textLengthMethod;
	private CssClassRateMethod cssClassRateMethod;
	
	public ClassificationEngine(){
		this.statTree = new Tree<StatNode>();
	}
	
	
	public void generateStatTree(){
		this.createStatTree();
		this.fillStatTree();
	}
	
	
	private void createStatTree(){
		// init root element
		Element currentElement = this.domTree;
		StatNode rootNode = this.jsoupElementelementToStatNode(currentElement);
		this.statTree.createRootNode(currentElement.tagName(),rootNode);
		
		// Fill Stat Tree with nodes
		TreeNode<StatNode> currentNode = this.statTree.getRootNode();
		List<Element> children = new ArrayList<Element>();
		List<TreeNode<StatNode>> nextNode = new ArrayList<TreeNode<StatNode>>();
		
		children.addAll(currentElement.children().stream().filter(Objects::nonNull).collect(Collectors.toList()));
		do{
			currentElement = children.remove(0);
			if(currentElement == null){
				// prepare next Stat node
				currentNode = nextNode.get(0);
				nextNode.remove(currentNode);
			}else{
				// Prepare for next iterations
				children.add(null);
				children.addAll(currentElement.children().stream().filter(Objects::nonNull).collect(Collectors.toList()));
				
				// Create new StatNode Child
				StatNode statNode = this.jsoupElementelementToStatNode(currentElement);
				TreeNode<StatNode> childNode = currentNode.addChild(currentElement.tagName(),statNode);
				nextNode.add(childNode);
			}
		}while(children.size() != 0);
	}
	
	
	private StatNode jsoupElementelementToStatNode(Element element){
		StatNode statNode = new StatNode(element);
		statNode.setTagName(element.tagName());
		statNode.setOwnText(element.ownText());
		statNode.setClassNames(element.classNames());
		return statNode;
	}

	
	private void fillStatTree(){
		TreeSet<TreeNode<StatNode>> nodes = (TreeSet<TreeNode<StatNode>>) this.statTree.getLeafNodes().clone();
		do{
			TreeNode<StatNode> currentNode = nodes.last();
			nodes.remove(currentNode);
			if( currentNode != null ){
				StatNode statNode = currentNode.getValue();
				statNode.initStats();
				
				for(TreeNode<StatNode> child : currentNode.getChildren()){
					StatNode childStatNode = child.getValue();
					statNode.incrementFullTextLength(childStatNode.getFullTextLength());
					statNode.addClassRates(childStatNode.getClassRate());
				}

				if(!currentNode.isRootNode()){
					nodes.add(currentNode.getParent());
				}
			}
			
		}while(nodes.size() != 0);
	}

	public Map<WebContentTypes,TreeNode<StatNode>> detectStructures(){
		Map<WebContentTypes,TreeNode<StatNode>> classificationResult = new HashMap<>();
		// Init classification methods
		this.textLengthMethod	 	= new TextLengthMethod(this.statTree);
		this.cssClassRateMethod 	= new CssClassRateMethod(this.statTree);
		
		// Launch text length classification Method
		this.textLengthMethod.init();
		TreeNode<StatNode> mainContainer = this.textLengthMethod.detectMainContainer();
		mainContainer.getValue().addTag(WebContentTypes.CONTAINER);
		System.out.println("Main container :"+mainContainer.getValue().getJsoupElement().cssSelector());
		classificationResult.put(WebContentTypes.MAIN_CONTAINER,mainContainer);

		// Launch css class rates classification Method
		this.cssClassRateMethod.init();
		List<TreeNode<StatNode>> repetitionElements = this.cssClassRateMethod.detectRepetetionElements();
		for(TreeNode<StatNode> elem : repetitionElements){
			StatNode statNode = elem.getValue();
			statNode.addTag(WebContentTypes.REPETITION_ELEMENT);
			TreeSet<ClassRate> relevantClassRates = StatNode.removeNoisyClassRates(statNode.getRelevantClassRates());
			if(relevantClassRates.size() > 0){
				System.out.println(statNode.getTagName()+" "+statNode.getClassNames()+" "+elem.getLevel());
				for(ClassRate cr : relevantClassRates){
					if(cr.getRate() >= this.cssClassRateMethod.getRepetetionThreshold()){
						System.out.println(cr);
					}
				}
				System.out.println("----------------");
			}
		}
		return classificationResult;
	}
	
	
	// -------------------------------- GETTER / SETTER --------------------------------
	
	public Tree<StatNode> getStatTree() {
		return statTree;
	}

	public void setStatTree(Tree<StatNode> statTree) {
		this.statTree = statTree;
	}

	public Element getDomTree() {
		return this.domTree;
	}

	public void setDomTree(Element domTree) {
		this.domTree = domTree;
	}
}
