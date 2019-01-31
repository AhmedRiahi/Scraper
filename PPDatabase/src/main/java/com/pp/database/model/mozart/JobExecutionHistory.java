package com.pp.database.model.mozart;

import java.util.Date;

import com.pp.database.model.common.DescriptorsPortfolio;
import com.pp.database.model.engine.DescriptorJob;
import lombok.Data;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Reference;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.pp.database.kernel.PPEntity;
import com.pp.database.model.scrapper.descriptor.DescriptorModel;

import javax.validation.constraints.NotNull;

@Data
@Entity
public class JobExecutionHistory extends PPEntity{

    @Reference
	private DescriptorsPortfolio portfolio;
	private DescriptorJob descriptorJob;
	@Reference
	private DescriptorWorkflowDataPackage dwdp;
	private Date startTime;
	private Date finishTime;
	private boolean inError;
	
}
