package com.pp.structureDetector.abstractStructure;

import com.pp.framework.dataStructure.ObservableTreeSet;
import com.pp.framework.dataStructure.tree.ProcessingTreeNodeValue;
import com.pp.framework.dataStructure.tree.TreeNode;
import com.pp.framework.dataStructure.tree.TreeNodeValue;
import com.pp.framework.propagation.Message;
import com.pp.framework.propagation.PropagationEvent;
import com.pp.framework.propagation.PropagationResponse;
import org.jsoup.nodes.Element;

import java.util.*;

public class StatNode extends ProcessingTreeNodeValue{

	private String tagName;
	private String ownText;
	private long fullTextLength;
	private Set<String> classNames;
	private ObservableTreeSet<ClassRate> classRates;
	private TreeSet<ClassRate> pureClassRates;
	private TreeSet<ClassRate> relevantClassRates;
	private Element jsoupElement;
	private List<WebContentTypes> tags;
	
	public StatNode(Element jsoupElement){
		this.jsoupElement 		= jsoupElement;
		this.classRates 		= new ObservableTreeSet<>();
		this.pureClassRates 	= new TreeSet<ClassRate>();
		this.relevantClassRates = new TreeSet<>();
		this.classNames 		= new HashSet<String>();
		this.tags 				= new ArrayList<>(); 
	}
	
	public StatNode(Element jsoupElement,String tagName){
		this(jsoupElement);
		this.tagName = tagName;
	}
	
	public void initStats(){
		// Init text length
		this.fullTextLength = this.ownText.length();
		//Init class rates
		Iterator<String> iter = this.classNames.iterator();
		while(iter.hasNext()){
			String tempClassName = iter.next(); 
			if(!tempClassName.trim().isEmpty()){
				boolean found = false;
				for(ClassRate classRate : this.classRates){
					if(classRate.getClassName().equals(tempClassName)){
						found = true;
						classRate.incrementRate(1);
						break;
					}
				}
				if(!found){
					this.classRates.add(new ClassRate(tempClassName, 1));
				}
			}
		}
	}
	
	public void incrementFullTextLength(long length){
		this.fullTextLength += length;
	}
	
	public void addClassRates(TreeSet<ClassRate> classes){
		Iterator<ClassRate> iter = classes.iterator();
		while(iter.hasNext()){
			ClassRate tempClassRate = iter.next(); 
			if(!tempClassRate.getClassName().trim().isEmpty()){
				boolean found = false;
				for(ClassRate classRate : this.classRates){
					if(classRate.getClassName().equalsIgnoreCase(tempClassRate.getClassName())){
						found = true;
						classRate.incrementRate(tempClassRate.getRate());
						break;
					}
				}
				if(!found){
					this.classRates.add(new ClassRate(tempClassRate.getClassName(), tempClassRate.getRate()));
				}
			}
		}
	}
	
	public void generatePureClassRates(){
		TreeSet<ClassRate> tempClassRates = (TreeSet<ClassRate>) this.classRates.clone();
		for(TreeNode<TreeNodeValue> treeNode : this.treeNode.getChildren()){
			StatNode statNode = (StatNode) treeNode.getValue();
			TreeSet<ClassRate> childClassRates = statNode.getClassRate();
			for(ClassRate classRate : childClassRates){
				if(tempClassRates.contains(classRate)){
					// if child contains the same ClassRate so it belongs to the children => remove it from parent pure class rates
					tempClassRates.remove(classRate);
				}
			}
		}
		this.setPureClassRates(tempClassRates);
	}
	
	public static TreeSet<ClassRate> removeNoisyClassRates(TreeSet<ClassRate> classRates){
		TreeSet<ClassRate> noNoisyClassRates = (TreeSet<ClassRate>) classRates.clone();
		Iterator<ClassRate> iterator = noNoisyClassRates.iterator();
		while(iterator.hasNext()){
			ClassRate cr = iterator.next();
			if(cr.isNoise()){
				iterator.remove();
			}
		}
		return noNoisyClassRates;
	}
	
	public void addTag(WebContentTypes tag){
		this.tags.add(tag);
	}
	
	@Override
	public void onMessageReceived(Message message) {
		
	}
	
	@Override
	public PropagationResponse onRequestReceived(Message message) {
		switch(message.getPropagationEvent().getId()){
		case Event.GENERATE_PURE_CLASS_RATES:
			PropagationResponse response = new PropagationResponse();
			this.generatePureClassRates();
			response.setResult(this.treeNode);
			return response;
		}
		return null;
	}

	@Override
	public Set<PropagationEvent> registeredListeningPropagationEvents() {
		Set<PropagationEvent> propagationEvents = new HashSet<PropagationEvent>();
		propagationEvents.add(new PropagationEvent(StatNode.Event.GENERATE_PURE_CLASS_RATES));
		propagationEvents.add(new PropagationEvent(StatNode.Event.CHECK_NOISY_CLASS_RATE));
		return propagationEvents;
	}
	
	@Override
	public String toString(){
		return this.ownText+" "+this.fullTextLength+" "+this.classNames+" "+this.classRates;
	}
	
	
	public static final class Event{
		public static final int GENERATE_PURE_CLASS_RATES 	= 1;
		public static final int CHECK_NOISY_CLASS_RATE 		= 2;
	}
	
	
	// -------------------------------- GETTER / SETTER --------------------------------

	public List<WebContentTypes> getTags() {
		return tags;
	}

	public void setTags(List<WebContentTypes> tags) {
		this.tags = tags;
	}

	public TreeSet<ClassRate> getRelevantClassRates() {
		return relevantClassRates;
	}

	public void setRelevantClassRates(TreeSet<ClassRate> relevantClassRates) {
		this.relevantClassRates = relevantClassRates;
	}

	public TreeSet<ClassRate> getPureClassRates() {
		return pureClassRates;
	}

	public void setPureClassRates(TreeSet<ClassRate> pureClassRates) {
		this.pureClassRates = pureClassRates;
	}

	public Element getJsoupElement() {
		return jsoupElement;
	}

	public void setJsoupElement(Element jsoupElement) {
		this.jsoupElement = jsoupElement;
	}

	public Set<String> getClassNames() {
		return classNames;
	}

	public void setClassNames(Set<String> classes) {
		this.classNames = classes;
	}

	public String getOwnText() {
		return ownText;
	}


	public void setOwnText(String ownText) {
		this.ownText = ownText;
	}


	public TreeSet<ClassRate> getClassRate() {
		return classRates;
	}

	public String getTagName() {
		return tagName;
	}


	public void setTagName(String tagName) {
		this.tagName = tagName;
	}


	public long getFullTextLength() {
		return fullTextLength;
	}


	public void setFullTextLength(long fullTextLength) {
		this.fullTextLength = fullTextLength;
	}
}
