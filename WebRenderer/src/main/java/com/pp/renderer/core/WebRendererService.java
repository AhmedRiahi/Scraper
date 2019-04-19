package com.pp.renderer.core;


import com.pp.database.model.engine.DescriptorJobCrawlingParams;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Slf4j
@Service
public class WebRendererService {

    @Value("${web-driver.path}")
    private String webDriverPath;

    @Autowired
    private LinkedinDownloader linkedinDownloader;

    @PostConstruct
    public void init(){
        System.setProperty("webdriver.chrome.driver",webDriverPath);
    }

    public String download(DescriptorJobCrawlingParams descriptorJobCrawlingParams){
        if(descriptorJobCrawlingParams.getUrl().startsWith("https://www.linkedin")){
            return this.linkedinDownloader.download(descriptorJobCrawlingParams.getUrl());
        }else{
            WebDriver webDriver = new ChromeDriver();
            webDriver.get(descriptorJobCrawlingParams.getUrl());
            try {
                DriverUtils.driverWait(webDriver,1000 * 5);
            } catch (InterruptedException e) {
                log.error(e.getMessage(),e);
            }
            String contents = webDriver.getPageSource();
            webDriver.close();
            return contents;
        }
    }
}
