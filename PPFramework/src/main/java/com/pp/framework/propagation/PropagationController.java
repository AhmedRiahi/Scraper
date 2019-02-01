package com.pp.framework.propagation;

import com.pp.framework.propagation.Exception.InvalidReceiverListeningEventsException;

import java.util.ArrayList;
import java.util.List;

public class PropagationController {

	protected List<PropagationReceiver> receivers;
	protected List<PropagationReceiver> tempReceivers;
	protected boolean forcedMode = false;
	protected boolean isBroadcasting = false;
	
	public PropagationController(){
		this.receivers = new ArrayList<>();
		this.tempReceivers = new ArrayList<>();
	}
	
	public List<PropagationResponse> broadcast(Message message) throws InvalidReceiverListeningEventsException{
		this.isBroadcasting = true;
		List<PropagationResponse> responses = new ArrayList<>();
		for(PropagationReceiver receiver : this.receivers){
			if(!forcedMode && (receiver.registeredListeningPropagationEvents() == null || receiver.registeredListeningPropagationEvents().size() == 0)){
				throw new InvalidReceiverListeningEventsException("No propagation received was registered");
			}
			if(receiver.registeredListeningPropagationEvents().contains(message.getPropagationEvent())){
				switch(message.getType()){
				case NOTIFICATION:
					receiver.onMessageReceived(message);
					break;
					
				case REQUEST:
					PropagationResponse response = receiver.onRequestReceived(message);
					responses.add(response);
					break;
					
				default:
					break;
				
				}
			}
		}
		this.broadcastFinished();
		
		return responses;
	}
	
	
	protected void broadcastFinished(){
		this.isBroadcasting = false;
		for(PropagationReceiver receiver : this.tempReceivers){
			this.receivers.add(receiver);
		}
		this.tempReceivers = new ArrayList<>();
	}
	
	
	public void registerSender(PropagationSender sender){
		sender.setController(this);
	}
	
	public void registerReceiver(PropagationReceiver receiver){
		if(this.isBroadcasting){
			this.tempReceivers.add(receiver);
		}else{
			this.receivers.add(receiver);
		}
	}
	
	// -------------------------------- GETTER / SETTER --------------------------------

	public boolean isForcedMode() {
		return forcedMode;
	}

	public void setForcedMode(boolean forcedMode) {
		this.forcedMode = forcedMode;
	}
}
