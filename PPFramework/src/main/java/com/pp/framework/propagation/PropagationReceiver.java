package com.pp.framework.propagation;

import java.util.Set;

public interface PropagationReceiver {

	public void onMessageReceived(Message message);
	public PropagationResponse onRequestReceived(Message message);
	public Set<PropagationEvent> registeredListeningPropagationEvents();
	
}
