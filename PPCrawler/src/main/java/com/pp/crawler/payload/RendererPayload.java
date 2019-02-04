package com.pp.crawler.payload;

import com.pp.database.model.engine.DescriptorJobCrawlingParams;
import lombok.Data;

@Data
public class RendererPayload {

    private String workflowId;
    private DescriptorJobCrawlingParams descriptorJobCrawlingParams;
    private String contents;
}
