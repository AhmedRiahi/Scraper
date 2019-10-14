package com.pp.renderer.core;


import com.pp.database.model.engine.DescriptorJobCrawlingParams;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.stream.IntStream;

@Slf4j
@Service
public class LinkedinDownloader {

    private WebDriver webDriver;
    private long webDriverLivingTime;

    @Value("^${linkedin.login}")
    private String linkedinLogin;

    @Value("^${linkedin.password}")
    private String linkedinPassword;


    private void login(String login, String password) {
        try {
            this.webDriver.get("https://www.linkedin.com");
            DriverUtils.driverWait(this.webDriver,1000 * 5);
            WebElement loginField = this.webDriver.findElement(By.id("login-email"));
            loginField.click();
            loginField.sendKeys(login);

            WebElement passwordField = this.webDriver.findElement(By.id("login-password"));
            passwordField.click();
            passwordField.sendKeys(password);

            loginField.click();
            DriverUtils.driverWait(this.webDriver,1000 * 5);
            this.webDriver.findElement(By.id("login-submit")).click();
            this.webDriverLivingTime = System.currentTimeMillis();
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
            this.webDriver.close();
        }
    }


    public synchronized String download(String url) {
        try {
            this.checkWebDriver();
            this.webDriver.get(url);
            DriverUtils.driverWait(this.webDriver,1000 * 5);
            DriverUtils.upDownScroll(this.webDriver,5);
            if(url.contains("results/people")){
                DriverUtils.downScroll(this.webDriver,5);
            }
            if(url.contains("recent-activity")){
                DriverUtils.upDownScroll(this.webDriver,15);
                this.webDriver.findElements(By.className("feed-shared-social-counts__num-comments")).stream().forEach(webElement -> {
                    try {
                        DriverUtils.driverWait(this.webDriver,1000 );
                        try {
                            webElement.click();
                        }catch(RuntimeException e){
                            log.error(e.getMessage(),e);
                        }
                    } catch (InterruptedException e) {
                        log.error(e.getMessage(), e);
                    }
                });
            }

            return this.webDriver.getPageSource();
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }



    private void checkWebDriver() {
        if (this.webDriver == null) {
            this.webDriver = new ChromeDriver();
            this.login(this.linkedinLogin, this.linkedinPassword);
        } else {
            long aliveTimeout = System.currentTimeMillis() - this.webDriverLivingTime;
            if (aliveTimeout > 1000 * 60 * 30) {
                this.webDriver.close();
                this.webDriver = new ChromeDriver();
                this.login(this.linkedinLogin, this.linkedinPassword);
            }
        }
    }
}
