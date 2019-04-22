package com.pp.renderer.core;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

import java.util.stream.IntStream;

@Slf4j
public class DriverUtils {


    public static void driverWait(WebDriver driver,long millis) throws InterruptedException {
        synchronized (driver){
            driver.wait(millis);
        }
    }

    public static void downScroll(WebDriver driver,int count){
        JavascriptExecutor js = (JavascriptExecutor) driver;
        //scroll down
        IntStream.range(1, count).forEach(i -> {
            try {
                js.executeScript("window.scrollBy(0," + (i * 1000) + ")");
                DriverUtils.driverWait(driver,1000);
            } catch (InterruptedException e) {
                log.error(e.getMessage(), e);
            }
        });
    }

    public static void upDownScroll(WebDriver driver,int count) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        //scroll down
        IntStream.range(1, count).forEach(i -> {
            try {
                js.executeScript("window.scrollBy(0," + (i * 1000) + ")");
                DriverUtils.driverWait(driver,1000);
            } catch (InterruptedException e) {
                log.error(e.getMessage(), e);
            }
        });

        //scroll up
        IntStream.range(1, count).forEach(i -> {
            try {
                js.executeScript("window.scrollBy(0," + (i * -1000) + ")");
                DriverUtils.driverWait(driver,1000);
            } catch (InterruptedException e) {
                log.error(e.getMessage(), e);
            }
        });
    }
}
