const puppeteer = require('puppeteer');
const { URL } = require('url');
const fse = require('fs-extra');
var fs = require('fs');
const path = require('path');
var kafka = require('kafka-node')


async function run(url) {
  const browser = await puppeteer.launch({headless:false});
  const page = await browser.newPage();
  await page.setRequestInterception(true);

  page.on('request', request => {
      console.log('GOT NEW REQUEST', request.url);
      request.continue();
  });

  await page.goto(url);
  //await page.waitFor(1000);

await page.click("#login-email");
await page.keyboard.type("albertmoise12@gmail.com");

await page.click("#login-password");
await page.keyboard.type("a21407212Ri");

await page.waitFor(5000);
console.log("await 1 ")

const html = await page.content();
  await fse.outputFile("C:\\Users\\Camirra\\git\\pupitter\\output.html", html);


await page.click("#login-submit");
await page.waitFor(5000);
await page.reload();
console.log('page reloaded')


await page.waitFor(5000);
console.log('searching ...')
await page.click(".ember-view > input[type='text']");
await page.keyboard.type("software engineer");
await page.keyboard.press('Enter');

}


run("https://www.linkedin.com")