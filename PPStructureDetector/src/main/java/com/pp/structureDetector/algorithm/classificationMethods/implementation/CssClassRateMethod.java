package com.pp.structureDetector.algorithm.classificationMethods.implementation;

import com.pp.framework.dataStructure.tree.Tree;
import com.pp.framework.dataStructure.tree.TreeNode;
import com.pp.framework.propagation.Exception.InvalidReceiverListeningEventsException;
import com.pp.framework.propagation.Message;
import com.pp.framework.propagation.PropagationResponse;
import com.pp.structureDetector.abstractStructure.ClassRate;
import com.pp.structureDetector.abstractStructure.StatNode;
import com.pp.structureDetector.algorithm.classificationMethods.ClassificationMethod;
import com.pp.structureDetector.exception.NoCapacityToDetectException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

public class CssClassRateMethod extends ClassificationMethod{

	private int repetetionThreshold = 10;
	
	public CssClassRateMethod(Tree<StatNode> statTree) {
		super(statTree);
	}

	@Override
	public void init() {
		// Threshold adjustement
		Iterator<ClassRate> iterator = this.statTree.getRootNode().getValue().getClassRate().descendingIterator();
		int step = 0;
		int lastThreshold = 0;
		while(iterator.hasNext() && step < 21){
			ClassRate cr = iterator.next();
			if(cr.getRate() != lastThreshold){
				step++;
				lastThreshold = cr.getRate();
			}
		}
		
		if(lastThreshold < 4 ){
			this.repetetionThreshold = 4;
		}else{
			this.repetetionThreshold = lastThreshold;
		}
		
		System.out.println(this.repetetionThreshold);
	}

	@Override
	public TreeNode<StatNode> detectMainContainer() throws NoCapacityToDetectException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<TreeNode<StatNode>> detectRepetetionElements() {
		try {
			List<TreeNode<StatNode>> repetetionElements = new ArrayList<TreeNode<StatNode>>();
			Message message = new Message(Message.Type.REQUEST,StatNode.Event.GENERATE_PURE_CLASS_RATES);
			List<PropagationResponse> responses = this.statTree.broadcast(message);
			for(PropagationResponse response:responses){
				TreeNode<StatNode> treeNode = (TreeNode<StatNode>) response.getResult();
				TreeSet<ClassRate> relevantClassRates = new TreeSet<>();
				for(ClassRate classRate : treeNode.getValue().getPureClassRates()){
					if(classRate.getRate() >= this.repetetionThreshold){
						relevantClassRates.add(classRate);
					}
				}
				if(relevantClassRates.size()>0){
					treeNode.getValue().setRelevantClassRates(relevantClassRates);
					repetetionElements.add(treeNode);
				}
			}
			
			// Check noisy class rates
			for(TreeNode<StatNode> treeNode : repetetionElements){
				for(TreeNode<StatNode> tempNode : repetetionElements){
					if(treeNode != tempNode){
						if(treeNode.hasSiblingChild(tempNode)){
							StatNode tempStatNode = tempNode.getValue();
							StatNode statNode = treeNode.getValue();
							for(ClassRate tempPureClassRate : tempStatNode.getPureClassRates()){
								statNode.getPureClassRates()
								.stream()
								.filter(
										pcr -> pcr.getClassName().equals(tempPureClassRate.getClassName())
								).forEach(pcr -> pcr.setNoise(true));
							}
						}
					}
				}
			}
			
			return repetetionElements;
		} catch (InvalidReceiverListeningEventsException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public TreeNode<StatNode> detectPagination() throws NoCapacityToDetectException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TreeNode<StatNode> detectMainNavigation() throws NoCapacityToDetectException {
		// TODO Auto-generated method stub
		return null;
	}

	// -------------------------------- GETTER / SETTER --------------------------------
	
	public int getRepetetionThreshold() {
		return repetetionThreshold;
	}

	public void setRepetetionThreshold(int repetetionThreshold) {
		this.repetetionThreshold = repetetionThreshold;
	}

}
