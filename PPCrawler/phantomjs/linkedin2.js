var webPage = require('webpage');
var fs = require('fs');
var page = webPage.create();

page.settings.javascriptEnabled = true;
page.settings.loadImages = true; 
phantom.cookiesEnabled = true;
phantom.javascriptEnabled = true;

var url = fs.read('C:/Users/Camirra/git/pp/PPCrawler/phantomjs/url.txt');

var x = 0;
var testindex = 0
var loadInProgress = false;

page.viewportSize = {
  width: 1280, height : 1024
};

console.log("start script")

page.onConsoleMessage = function(msg) {
  console.log("console message")
  console.log(msg);
};

page.onLoadStarted = function() {
  loadInProgress = true;
  page.includeJs("https://cdnjs.cloudflare.com/ajax/libs/babel-polyfill/6.26.0/polyfill.js")
  console.log("load started");
};

page.onLoadFinished = function() {
   page.render('test'+x+'.png');
   x++;
  loadInProgress = false;
  console.log("load finished");
  console.log(page.url);
  if(page.url=="https://www.linkedin.com/feed/"){
    setInterval(renderJobPage,5*1000)
  }else{

    if(page.url=="https://www.linkedin.com/feed/"){
    page.viewportSize = {
                  width: 1280, height : 1024
                };
                page.open("https://www.linkedin.com/jobs/",function(){
                    fs.write('1.html', page.content, 'w');
                });
              }
  }
};

page.onResourceReceived = function (res) {
    console.log('resource received')
    console.log(res.stage)
};

var login = function(callback){
  console.log("start login")
  //Load Login Page
    page.open(url,function(){
      page.includeJs("https://cdnjs.cloudflare.com/ajax/libs/babel-polyfill/6.26.0/polyfill.js")
    });

    var interval1 = setInterval(function(){
      //Enter Credentials
      page.evaluate(function() {
          var arr = document.getElementsByClassName("login-form");
          var i;

          for (i=0; i < arr.length; i++) {
            if (arr[i].getAttribute('method') == "POST") {

              arr[i].elements["session_key"].value="albertmoise12@gmail.com";
              arr[i].elements["session_password"].value="albert21407212almoise";
              arr[i].submit();
              return;
            }
          }
      });
      console.log("render test")
      page.render('test.png');
        
      clearInterval(interval1)
    },2*1000)
}


login();


  var renderJobPage = function() {
        var render = false;
        page.onCallback = function(){
           
            console.log('render callback')
            page.render('example.png');
            //phantom.exit();
        }
        console.log(page.url)
        /*if(page.url != "https://www.linkedin.com/jobs/"){
            console.log("going to job page")
            page.open("https://www.linkedin.com/jobs/")
        }*/


        page.evaluate(function() {
              window.callPhantom();
        });

      };
  
