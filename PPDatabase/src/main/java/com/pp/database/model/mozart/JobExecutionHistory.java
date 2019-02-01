package com.pp.database.model.mozart;

import com.pp.database.kernel.PPEntity;
import com.pp.database.model.common.DescriptorsPortfolio;
import com.pp.database.model.engine.DescriptorJob;
import lombok.Data;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Reference;

import java.util.Date;

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
