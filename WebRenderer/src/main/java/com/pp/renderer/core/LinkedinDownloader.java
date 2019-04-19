package com.pp.renderer.core;


import com.pp.database.model.engine.DescriptorJobCrawlingParams;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class LinkedinDownloader {

    private WebDriver webDriver;
    private long webDriverLivingTime;

    @Value("^${linkedin.login}")
    private String linkedinLogin;

    @Value("^${linkedin.password}")
    private String linkedinPassword;


    private void login(String login,String password){
        try {
            this.webDriver.get("https://www.linkedin.com");
            this.driverWait(1000 * 5);
            WebElement loginField = this.webDriver.findElement(By.id("login-email"));
            loginField.click();
            loginField.sendKeys(login);

            WebElement passwordField = this.webDriver.findElement(By.id("login-password"));
            passwordField.click();
            passwordField.sendKeys(password);

            loginField.click();
            this.driverWait(1000*5);
            this.webDriver.findElement(By.id("login-submit")).click();
            this.webDriverLivingTime = System.currentTimeMillis();
        } catch (InterruptedException e) {
            log.error(e.getMessage(),e);
            this.webDriver.close();
        }
    }

    private void driverWait(long millis) throws InterruptedException {
        synchronized (this.webDriver){
            this.webDriver.wait(millis);
        }
    }

    public String download(String url){
        this.checkWebDriver();
        this.webDriver.get(url);
        return this.webDriver.getPageSource();
    }

    private void checkWebDriver(){
        if(this.webDriver == null){
            this.webDriver = new ChromeDriver();
            this.login(this.linkedinLogin,this.linkedinPassword);
        }else{
            long aliveTimeout = System.currentTimeMillis() - this.webDriverLivingTime;
            if(aliveTimeout > 1000 * 60 * 30){
                this.webDriver.close();
                this.webDriver = new ChromeDriver();
                this.login(this.linkedinLogin,this.linkedinPassword);
            }
        }
    }
}
