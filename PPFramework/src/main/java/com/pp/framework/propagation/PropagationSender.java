package com.pp.framework.propagation;

import com.pp.framework.propagation.Exception.InvalidReceiverListeningEventsException;

import java.util.List;

public class PropagationSender {

	protected PropagationController controller;
	
	
	public List<PropagationResponse> broadcast(Message message) throws InvalidReceiverListeningEventsException{
		message.setSender(this);
		return this.controller.broadcast(message);
	}

	// -------------------------------- GETTER / SETTER --------------------------------

	public PropagationController getController() {
		return controller;
	}


	public void setController(PropagationController controller) {
		this.controller = controller;
	}
}
