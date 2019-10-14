package com.pp.crawler.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/crawler")
public class CrawlerRestController {

    @Autowired
    private CrawlerApiService crawlerApiService;

    @RequestMapping("/downloadImage")
    public String downloadImage(@RequestParam String url) throws IOException {
        return this.crawlerApiService.downloadImage(url);
    }
}
