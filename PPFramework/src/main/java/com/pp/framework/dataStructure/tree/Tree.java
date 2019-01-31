package com.pp.framework.dataStructure.tree;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import com.pp.framework.dataStructure.Couple;
import com.pp.framework.propagation.Message;
import com.pp.framework.propagation.PropagationController;
import com.pp.framework.propagation.PropagationEvent;
import com.pp.framework.propagation.PropagationReceiver;
import com.pp.framework.propagation.PropagationResponse;
import com.pp.framework.propagation.PropagationSender;

public class Tree<T extends TreeNodeValue> extends PropagationSender implements PropagationReceiver{

	
	private TreeNode<T> rootNode;
	private TreeSet<TreeNode<T>> leafNodes;
	private PropagationController propagationController;
	private boolean isPropagationEnabled = true;

	
	public Tree(){
		this.leafNodes = new TreeSet<TreeNode<T>>();
		this.propagationController = new PropagationController();
		this.propagationController.registerSender(this);
		this.propagationController.registerReceiver(this);
	}
	
	public Tree(boolean isPropagationEnabled){
		super();
		this.isPropagationEnabled = isPropagationEnabled;
	}
	
	public TreeNode<T> createRootNode(String tagName,T value){
		this.rootNode = new TreeNode<>(tagName,value,this.isPropagationEnabled);
		this.rootNode.setLevel(0);
		this.rootNode.setLevel(0);
		this.rootNode.setHash("00");
		if(this.isPropagationEnabled){
			this.rootNode.setController(this.propagationController);
		}
		return this.rootNode;
	}
	
	@Override
	public void onMessageReceived(Message message) {
		switch(message.getPropagationEvent().getId()){
		case Event.NEW_CHILD_CREATED:
			Couple<TreeNode<T>,TreeNode<T>> couple = (Couple<TreeNode<T>,TreeNode<T>>) message.getObject();
			// if a child become a parent remove it
			if(this.leafNodes.contains(couple.getKey())){
				this.leafNodes.remove(couple.getKey());
			}
			//add the new child
			this.leafNodes.add(couple.getValue());
			this.propagationController.registerReceiver(couple.getValue());
			break;
		default:
			System.out.println("Unknown message !!!"+message);
		}
	}
	
	@Override
	public PropagationResponse onRequestReceived(Message message) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<PropagationEvent> registeredListeningPropagationEvents() {
		Set<PropagationEvent> events = new HashSet<>();
		events.add(new PropagationEvent(Event.NEW_CHILD_CREATED));
		return events;
	}
	
	
	public static final class Event{
		public final static int NEW_CHILD_CREATED = 10;
	}
	
	// -------------------------------- GETTER / SETTER --------------------------------
	
	public TreeSet<TreeNode<T>> getLeafNodes() {
		return leafNodes;
	}

	public void setLeafNodes(TreeSet<TreeNode<T>> leafNodes) {
		this.leafNodes = leafNodes;
	}

	public TreeNode<T> getRootNode() {
		return rootNode;
	}

	public void setRootNode(TreeNode<T> rootNode) {
		this.rootNode = rootNode;
	}
}
