const puppeteer = require('puppeteer');
const { URL } = require('url');
const fse = require('fs-extra');
var fs = require('fs');
const path = require('path');
var kafka = require('kafka-node');


var Consumer = kafka.Consumer;
var Producer = kafka.Producer;

var client = new kafka.Client("localhost:2181/");


var consumerTopic = "renderer.download.in";
var producerTopic = "renderer.download.out";

var consumer = new Consumer(
  client,
  [],
  {fromOffset: true}
);

var producer = new Producer(client);

producer.on('ready', function () {
    console.log("Producer is ready");
});
  
producer.on('error', function (err) {
  console.error("Problem with producing Kafka message "+err);
})

consumer.on('message', function (message) {
  console.log(message);
  var payload = JSON.parse(message.value);
  download(payload)
  
});

consumer.addTopics([
  { topic: consumerTopic, partition: 0, offset: 0},
], () => console.log("topic "+consumerTopic+" added to consumer for listening"));



linkedInPage = null;
normalPage = null;
browser = null;

getLinkedInPage();
prepareNewPage();

async function download(payload) {
  
  var page = null;
  
  if(payload.url.startsWith("https://www.linkedin")){
     page = linkedInPage;
  }else{
    page = normalPage;
  }
  
  
  await page.goto(payload.url);
  await page.waitFor(1000);
  const html = await page.content();
  
  payload.contents=html;
  payloads = [
    { topic: producerTopic, messages: JSON.stringify(payload), partition: 0 },
  ];
  producer.send(payloads, function (err, data) {
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