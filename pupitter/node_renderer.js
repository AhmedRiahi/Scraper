const puppeteer = require('puppeteer');
const { URL } = require('url');
const fse = require('fs-extra');
var fs = require('fs');
const path = require('path');
var kafka = require('kafka-node');


var Consumer = kafka.ConsumerGroup;
var Producer = kafka.Producer;

var client = new kafka.KafkaClient("localhost:9092/");


var consumerTopic = "renderer.download.in";
var producerTopic = "renderer.download.out";


var consumer = new Consumer(
  { kafkaHost: '127.0.0.1:9092' },consumerTopic
);

consumer.on('message', function (message) {
  console.log('got message')
  console.log(message);
  if(message.value){
    var payload = JSON.parse(message.value);
    console.log('payload : ')
    console.log(payload)
    download(payload)
  }else{
    console.log("error message without value !!!")
  }
});


var producer = new Producer(client);

producer.on('ready', function () {
    console.log("Producer is ready");
});
  
producer.on('error', function (err) {
  console.error("Problem with producing Kafka message "+err);
})

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
  const html = await page.content();
  
  payload.contents=html;
  console.log(html)
  payloads = [
    { topic: producerTopic, messages: JSON.stringify(payload)},
  ];
  producer.send(payloads, function (err, data) {
    console.log('sending data');
  	console.log(data);
  });
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