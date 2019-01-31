package com.pp.crawler.payload;

import lombok.Data;

@Data
public class RendererPayload {

    private String workflowId;
    private String url;
    private String contents;
}
