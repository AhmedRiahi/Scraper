var webPage = require('webpage');
var fs = require('fs');
var page = webPage.create();
var url = fs.readFile('url.txt',function(err,data){
	console.log(data)
});

page.open(url, function(status) {
   if (status === "success") { 
      fs.write('C:/Users/Camirra/git/pp/PPCrawler/phantomjs/output.html', page.content, 'w');
      phantom.exit()
   } 
});