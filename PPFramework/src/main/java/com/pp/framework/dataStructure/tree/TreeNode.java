package com.pp.framework.dataStructure.tree;

import com.pp.framework.dataStructure.Couple;
import com.pp.framework.propagation.Exception.InvalidReceiverListeningEventsException;
import com.pp.framework.propagation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class TreeNode<T extends TreeNodeValue> extends PropagationSender implements PropagationReceiver,Comparable<TreeNode<T>> {

	protected int level;
	protected int number;
	protected String hash;
	protected String tagName;
	protected T value;
	protected TreeNode<T> parent;
	protected List<TreeNode<T>> children;
	protected boolean isPropagationEnabled;

	
	public TreeNode(String tagName,T value,boolean isPropagationEnabled){
		this.children = new ArrayList<TreeNode<T>>();
		this.tagName = tagName;
		this.value = value;
		this.isPropagationEnabled = isPropagationEnabled;
		this.value.setTreeNode((TreeNode<TreeNodeValue>) this);
	}
	
	public TreeNode<T> addChild(String tagName, T value) {
		TreeNode<T> child = new TreeNode<>(tagName,value,this.isPropagationEnabled);
		this.children.add(child);
		child.parent 		= this;
		child.level 		= this.level+1;
		child.number 		= this.children.size();
		child.hash 			= this.hash+child.level+child.number;
		
		if(this.isPropagationEnabled){
			child.controller 	= this.controller;
			try {
				// Inform receivers by the new arrival child ^^
				Message message = new Message(Message.Type.NOTIFICATION,Tree.Event.NEW_CHILD_CREATED);
				Couple<TreeNode<T>,TreeNode<T>> couple = new Couple<TreeNode<T>, TreeNode<T>>(this,child);
				message.setObject(couple);
				this.broadcast(message);
			} catch (InvalidReceiverListeningEventsException e) {
				e.printStackTrace();
			}
		}
		
		return child;
	}
	
	public boolean isRootNode() {
		return this.level == 0 && this.number == 0;
	}
	
	public boolean hasSiblingChild(TreeNode<T> child){
		if( child.getLevel() <= this.getLevel()){
			return false;
		}else{
			TreeNode<T> parent = child.getParentAtLevel(this.getLevel());
			if(this.equals(parent)){
				return true;
			}else{
				return false;
			}
		}
	}
	
	public TreeNode<T> getParentAtLevel(int level){
		if(this.getLevel() > level){
			TreeNode<T> parent = this.getParent();
			while(parent.getLevel() > level){
				parent = parent.getParent();
			}
			return parent;
		}
		return null;
	}
	
	@Override
	public void onMessageReceived(Message message) {
		if(value instanceof ProcessingTreeNodeValue)
			((ProcessingTreeNodeValue)this.value).onMessageReceived(message);
	}
	
	@Override
	public PropagationResponse onRequestReceived(Message message) {
		if(value instanceof ProcessingTreeNodeValue)
			return ((ProcessingTreeNodeValue)this.value).onRequestReceived(message);
		return null;
	};

	@Override
	public Set<PropagationEvent> registeredListeningPropagationEvents() {
		if(value instanceof ProcessingTreeNodeValue)
			return ((ProcessingTreeNodeValue)this.value).registeredListeningPropagationEvents();
		return null;
	}
	

	@Override
	public boolean equals(Object obj) {
		TreeNode<T> temp = (TreeNode<T>) obj;
		return this.hash.equals(temp.hash);
	}
	
	@Override
	public int compareTo(TreeNode<T> o) {
		return this.hash.compareTo(o.hash);
	}
	
	@Override
	public String toString() {
		return "TreeNode {Level :"+this.level+" Number:"+this.number+" parent"+this.parent.getLevel()+"}";
	}
	
	
	// -------------------------------- GETTER / SETTER --------------------------------

	public boolean isPropagationEnabled() {
		return isPropagationEnabled;
	}

	public void setPropagationEnabled(boolean isPropagationEnabled) {
		this.isPropagationEnabled = isPropagationEnabled;
	}

	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

	public String getTagName() {
		return tagName;
	}

	public void setTagName(String tagName) {
		this.tagName = tagName;
	}

	public TreeNode<T> getParent() {
		return parent;
	}

	public void setParent(TreeNode<T> parent) {
		this.parent = parent;
	}

	public List<TreeNode<T>> getChildren() {
		return children;
	}

	public void setChildren(List<TreeNode<T>> children) {
		this.children = children;
	}

	public int getLevel() {
		return level;
	}
	
	public void setLevel(int level) {
		this.level = level;
	}
	
	public int getNumber() {
		return number;
	}
	
	public void setNumber(int number) {
		this.number = number;
	}
	
	public T getValue() {
		return value;
	}
	
	public void setValue(T value) {
		this.value = value;
	}
}
