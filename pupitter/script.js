const puppeteer = require('puppeteer');
const { URL } = require('url');
const fse = require('fs-extra');
var fs = require('fs');
const path = require('path');

fs.readFile('C:\\Users\\Camirra\\git\\pupitter\\url.txt','utf8',function(err,data){
	var url = data;
	run(url);
});


async function run(url) {
  const browser = await puppeteer.launch();
  const page = await browser.newPage();
  await page.setRequestInterception(true);

  page.on('request', request => {
      console.log('GOT NEW REQUEST', request.url);
      request.continue();
  });

  await page.goto(url);
  await page.waitFor(1000);
  const html = await page.content();
  await fse.outputFile("C:\\Users\\Camirra\\git\\pupitter\\output.html", html);
  browser.close();
}