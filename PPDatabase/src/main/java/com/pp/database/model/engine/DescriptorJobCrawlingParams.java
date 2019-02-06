package com.pp.database.model.engine;

import lombok.Data;

import java.util.List;

@Data
public class DescriptorJobCrawlingParams {

    private String url;
    private String httpMethod;
    private List<HttpParam> httpParams;
    private boolean scriptGeneratedURL;
    private String urlGeneratorScript;
    private long sleepTime = 1000 * 60;

}