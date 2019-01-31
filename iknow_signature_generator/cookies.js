

chrome.tabs.onUpdated.addListener(function (tabId, changeInfo, tab) {  
  console.log(changeInfo)        
   if (changeInfo.status == 'complete') {   
      chrome.tabs.query({active: true, currentWindow: true}, function(tabs){
        console.log('sending message')
        chrome.tabs.getSelected(function(tab){
          chrome.cookies.getAll({url:tab.url},function(cookies){
            console.log(tab)
            chrome.tabs.sendMessage(tab.id, {'cookies': cookies}, function(response) {}); 
          })
          
        })
          
      });
   }
});