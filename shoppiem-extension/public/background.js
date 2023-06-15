const MESSAGE_TYPE = {
  HEART_BEAT: "HEART_BEAT"
}
chrome.runtime.onInstalled.addListener(() => {
  console.log('Extension Installed')
});

function post(host, body) {
  fetch(host, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(body)
  })
  .then(response => response.json())
  .then(data => {
    console.log(data);
  })
  .catch(error => {
    console.error(error);
  });
}

function initProduct(url, html) {
  const host = "http://localhost:8080/product"
  const requestBody = {
    product_url: url,
    html: html
  };
  post(host, requestBody)
}

function heartbeat() {
  const host = "http://localhost:8080/heartbeat"
  const requestBody = {
    rId: chorme.storage.local.get("rId"),
    type: MESSAGE_TYPE.HEART_BEAT
  };
  post(host, requestBody)
}

function tokenRegistered(registration_id) {
  chrome.storage.local.set({"rId": registration_id})
}

function getHtml() { return document.documentElement.outerHTML; }


chrome.tabs.onUpdated.addListener(
    function(tabId, changeInfo, tab) {
      const url = changeInfo.url
      if (url && url.includes("amazon.com") && url.includes("/dp/")) {
        chrome.scripting
        .executeScript({
          target : {tabId : tab.id, allFrames : true},
          func : getHtml,
        })
        .then(injectionResults => {
          for (const {frameId, result} of injectionResults) {
            initProduct(url, result)
            break
          }
        });
      }
    }
);
chrome.gcm.register(["658613891142"], tokenRegistered)

chrome.gcm.onMessage.addListener((message) => {
  if (message.data.type === MESSAGE_TYPE.HEART_BEAT) {
    heartbeat()
  }
  console.log("GCM message received: ", message)
    // chrome.gcm.send({
    //   data: {
    //     "sender": "shoppiem-extension",
    //     "content": "Your message has been received!"
    //   },
    //
    // }, sendMessageCb)
})