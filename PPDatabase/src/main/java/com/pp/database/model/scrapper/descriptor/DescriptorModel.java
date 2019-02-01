package com.pp.database.model.scrapper.descriptor;

import com.pp.database.kernel.PPEntity;
import com.pp.database.model.crawler.Cookie;
import com.pp.database.model.scrapper.descriptor.listeners.ContentListenerModel;
import com.pp.database.model.scrapper.descriptor.relation.SemanticRelation;
import com.pp.database.model.scrapper.descriptor.relation.StructureRelation;
import lombok.Data;
import lombok.NonNull;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@XmlRootElement
@Entity("Descriptor")
public class DescriptorModel extends PPEntity{

    @NonNull
	private String name;
    private List<ContentListenerModel> contentListeners = new ArrayList<>();
    private List<SemanticRelation> semanticRelations = new ArrayList<>();
    private List<StructureRelation> structureRelations = new ArrayList<>();
	private List<Cookie> cookies = new ArrayList<>();
	@Embedded
	private List<DescriptorSemanticMapping> descriptorSemanticMappings = new ArrayList<>();
	private boolean useJSRendering = false;
	
	public static final int SEMANTIC_LISTENER = 1;
	public static final int STRUCTURED_LISTENER = 2;
	
	public DescriptorModel(){}
	
	
	public Set<String> getIndividualSchemas(){
		List<ContentListenerModel> individualCls =  this.contentListeners.stream().filter(cl -> cl.isIndividual()).collect(Collectors.toList());
		return individualCls.stream().map(cl -> this.descriptorSemanticMappings.stream().map(dsm -> dsm.getClSemanticName(cl)).collect(Collectors.toSet())).flatMap(map -> map.stream()).collect(Collectors.toSet());
	}
	
	public List<ContentListenerModel> getStructureRelatedSources(ContentListenerModel cl){
		return this.structureRelations.stream().filter(s -> s.getTarget().equals(cl)).map(s -> s.getSource()).collect(Collectors.toList());
	}

	public List<SemanticRelation> getSemanticRelationsAsSource(ContentListenerModel cl){
		return this.semanticRelations.stream().filter(s -> s.getSource().equals(cl)).collect(Collectors.toList());
	}
	
	public List<SemanticRelation> getSemanticRelationsAsTarget(ContentListenerModel cl){
		return this.semanticRelations.stream().filter(s -> s.getTarget().equals(cl)).collect(Collectors.toList());
	}

	public Optional<DescriptorSemanticMapping> getSemanticMappingById(String dsmId){
		return this.descriptorSemanticMappings.stream().filter(dsm -> dsm.getStringId().equals(dsmId)).findFirst();
	}

	public Optional<ContentListenerModel> getDSMContentListenerBySemanticName(String dsmId, String semanticName){
	    return this.contentListeners.stream().filter(cl -> cl.getName().equals(this.getSemanticMappingById(dsmId).get().getClNameBySemanticName(semanticName))).findAny();
    }
}
