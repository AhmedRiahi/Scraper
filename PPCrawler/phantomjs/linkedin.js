var webPage = require('webpage');
var fs = require('fs');
var page = webPage.create();
var url = fs.read('C:/Users/Camirra/git/pp/PPCrawler/phantomjs/url.txt');

var testindex = 0, loadInProgress = false;

page.viewportSize = {
  width: 1080,
  height: 1920
};

page.onConsoleMessage = function(msg) {
    console.log("console message")
  console.log(msg);
};

page.onLoadStarted = function() {
  loadInProgress = true;
  console.log("load started");
};

page.onLoadFinished = function() {
  loadInProgress = false;
  console.log("load finished");
};

var steps = [
  function() {
    //Load Login Page
    page.open(url);
  },
  function() {
    //Enter Credentials
    page.evaluate(function() {

      var arr = document.getElementsByClassName("login-form");
      var i;

      for (i=0; i < arr.length; i++) {
        if (arr[i].getAttribute('method') == "POST") {

          arr[i].elements["session_key"].value="albertmoise12@gmail.com";
          arr[i].elements["session_password"].value="albert21407212almoise";
          return;
        }
      }
    });
  },
  function() {
    //Login
    page.evaluate(function() {
      var arr = document.getElementsByClassName("login-form");
      var i;

      for (i=0; i < arr.length; i++) {
        if (arr[i].getAttribute('method') == "POST") {
          arr[i].submit();
          return;
        }
      }

    });
  },
  function() {
    // Output content of page to stdout after form has been submitted
    setInterval(function() {
        var render = false;
        page.onCallback = function(){
            if(page.url != "https://www.linkedin.com/jobs/"){
                //$("#jobs-nav-item").click();

                page.viewportSize = {
                  width: 1080,
                  height: 1920
                };
                page.open("https://www.linkedin.com/jobs/",function(){
                    fs.write('1.html', page.content, 'w');
                });

            }
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

      },1000 * 5);
  }
];


interval = setInterval(function() {
  if (!loadInProgress && typeof steps[testindex] == "function") {
    console.log("step " + (testindex + 1));
    steps[testindex]();
    testindex++;
  }
  if (typeof steps[testindex] != "function") {
    console.log("test complete!");
       clearInterval(interval)
  }
}, 50);
