package com.pp.framework.dataStructure;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Couple<K,V> {

	private K key;
	private V value;
	
	public Couple(K key,V value){
		this.key = key;
		this.value = value;
	}
}
