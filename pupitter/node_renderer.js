const puppeteer = require('puppeteer');
const { URL } = require('url');
const fse = require('fs-extra');
var fs = require('fs');
const path = require('path');
var Stomp = require('stomp-client');



var consumerTopic = "/queue/renderer.download.in";
var producerTopic = "/queue/renderer.download.out";


var stompClient = new Stomp('127.0.0.1', 61613, 'admin', 'admin');

stompClient.connect(function(sessionId){
  console.log('client connected')
  stompClient.subscribe(consumerTopic, function(message, headers){
    console.log('got message')
    console.log(message);
    if(message){
      var payload = JSON.parse(message);
      console.log('payload : ')
      console.log(payload)
      download(payload)
    }else{
      console.log("error message without value !!!")
    }
  });

});



linkedInPage = null;
normalPage = null;
browser = null;

getLinkedInPage();
prepareNewPage();



async function download(payload) {
  
  var page = null;
  
  if(payload.descriptorJobCrawlingParams.url.startsWith("https://www.linkedin")){
     page = linkedInPage;
  }else{
    page = normalPage;
  }
  
  
  await page.goto(payload.descriptorJobCrawlingParams.url);
  await page.waitFor(1000);
  page.evaluate(_ => {
    window.scrollBy(0,1000);
  });
  await page.waitFor(500);
  page.evaluate(_ => {
    window.scrollBy(0,1000);
  });
  await page.waitFor(500);
  page.evaluate(_ => {
    window.scrollBy(0,1000);
  });
  await page.waitFor(500);
  page.evaluate(_ => {
    window.scrollBy(0,1000);
  });


  await page.waitFor(1000);
  page.evaluate(_ => {
    window.scrollBy(0,-1000);
  });
  await page.waitFor(500);
  page.evaluate(_ => {
    window.scrollBy(0,-1000);
  });
  await page.waitFor(500);
  page.evaluate(_ => {
    window.scrollBy(0,-1000);
  });
  await page.waitFor(500);
  page.evaluate(_ => {
    window.scrollBy(0,-1000);
  });
  await page.waitFor(10000);

  if(payload.descriptorJobCrawlingParams.url.includes("recent-activity")){
    console.log('linkedin feeds')
    await page.evaluate( () => {
      console.log('looking for comments buttons')
      var buttons = document.getElementsByClassName('feed-shared-social-counts__num-comments');
      console.log(buttons);
      for(var i=0 ; i< buttons.length;i++){
        buttons[i].click();
      }
    })
  }
  console.log('BEFORE WAIT');
  await page.waitFor(30000);
  console.log('after middle WAIT');
  const html = await page.content();
  console.log('AFTER WAIT');
  
  payload.contents=html;
  console.log("sending html content")
  stompClient.publish(producerTopic, JSON.stringify(payload));
}



async function prepareNewPage(){
  browser = await puppeteer.launch();
  normalPage = await browser.newPage();
  await normalPage.setRequestInterception(true);
  normalPage.on('request', request => {
    console.log('GOT NEW REQUEST', request.url);
    request.continue();
  });
}



async function getLinkedInPage(){
  console.log("start getLinkedInPage")
  const browserLinkedIn = await puppeteer.launch({headless:false});
  linkedInPage = await browserLinkedIn.newPage();
  await linkedInPage.setRequestInterception(true);

  linkedInPage.on('request', request => {
      ///console.log('GOT NEW REQUEST', request.url);
      request.continue();
  });

  await linkedInPage.goto("https://www.linkedin.com");
  await linkedInPage.click("#login-email");
  await linkedInPage.keyboard.type("albertmoise12@gmail.com");

  await linkedInPage.click("#login-password");
  await linkedInPage.keyboard.type("a21407212Ri");

  await linkedInPage.waitFor(5000);
  console.log("submit form ")
  await linkedInPage.click("#login-submit");
  await linkedInPage.waitFor(5000);
  console.log("reloading page")
  await linkedInPage.reload();
  console.log('linkedInPage reloaded')
  return linkedInPage;
}