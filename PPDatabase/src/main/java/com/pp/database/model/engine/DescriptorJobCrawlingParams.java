package com.pp.database.model.engine;

import lombok.Data;

import java.util.List;

@Data
public class DescriptorJobCrawlingParams {

    private String url;
    private String httpMethod;
    private List<HttpParam> httpParams;


}