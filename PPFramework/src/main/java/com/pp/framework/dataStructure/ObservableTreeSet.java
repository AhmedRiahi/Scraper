package com.pp.framework.dataStructure;

import java.util.Observable;
import java.util.Observer;
import java.util.TreeSet;

public class ObservableTreeSet<O extends Observable> extends TreeSet<O> implements Observer {

	@Override
	public boolean add(O e) {
		e.addObserver(this);
		return super.add(e);
	}
	
	@Override
	public void update(Observable o, Object arg) {
		this.remove(o);
		this.add((O) arg);
	}

}
