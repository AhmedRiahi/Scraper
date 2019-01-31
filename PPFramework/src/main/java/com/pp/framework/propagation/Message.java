package com.pp.framework.propagation;

public class Message {

	
	protected PropagationEvent propagationEvent;
	protected PropagationSender sender;
	protected Type type;
	protected Object object;
	
	public Message(Type type,PropagationEvent propagationEvent){
		this.type = type;
		this.propagationEvent = propagationEvent;
	}
	
	public Message(Type type,int peId){
		this(type,new PropagationEvent(peId));
	}
	
	public static enum Type {
		NOTIFICATION,
		REQUEST;
	}
	
	@Override
	public String toString() {
		return "Message From : "+this.sender+" || PropagationEvent"+this.propagationEvent;
	}
	
	
	// -------------------------------- GETTER / SETTER --------------------------------
	
	public Object getObject() {
		return object;
	}

	public void setObject(Object object) {
		this.object = object;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public PropagationSender getSender() {
		return sender;
	}

	public void setSender(PropagationSender sender) {
		this.sender = sender;
	}

	public PropagationEvent getPropagationEvent() {
		return propagationEvent;
	}

	public void setPropagationEvent(PropagationEvent propagationEvent) {
		this.propagationEvent = propagationEvent;
	}
}
