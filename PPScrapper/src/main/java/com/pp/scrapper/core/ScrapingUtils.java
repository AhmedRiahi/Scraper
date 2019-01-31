package com.pp.scrapper.core;

import com.pp.database.model.scrapper.descriptor.DescriptorModel;
import com.pp.database.model.scrapper.descriptor.listeners.ContentListenerModel;
import com.pp.database.model.scrapper.descriptor.relation.ContentListenersRelation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class ScrapingUtils {

    public static List<ContentListenerModel> generateOrderedContentListeners(DescriptorModel descriptor, int type){
        List<ContentListenerModel> orderedContentListeners = new ArrayList<>();
        List<ContentListenerModel> clCopy = new ArrayList<>(descriptor.getContentListeners());
        // Remove Joined Listeners
        clCopy = clCopy.stream().filter(cl -> !cl.isJoinable()).collect(Collectors.toList());
        List<ContentListenersRelation> relationsCopy =  new ArrayList<>(type == DescriptorModel.SEMANTIC_LISTENER ? descriptor.getSemanticRelations() : descriptor.getStructureRelations());
        while(clCopy.size() > 0){
            Iterator<ContentListenerModel> clIterator = clCopy.iterator();
            while(clIterator.hasNext()){
                ContentListenerModel currentCL = clIterator.next();
                // search if contentListeners do not exists as target CL
                boolean found = false;
                Iterator<ContentListenersRelation> iter = relationsCopy.iterator();
                while(iter.hasNext()){
                    if(iter.next().getTarget().equals(currentCL)){
                        found = true;
                        break;
                    }
                }
                if(!found){
                    orderedContentListeners.add(currentCL);
                    clIterator.remove();
                    // Remove all currentCL related relation in order to eliminate theirs processing next time
                    iter = relationsCopy.iterator();
                    while(iter.hasNext()){
                        if(iter.next().doConcerns(currentCL)){
                            iter.remove();
                        }
                    }
                }
            }
        }
        return orderedContentListeners;
    }
}
