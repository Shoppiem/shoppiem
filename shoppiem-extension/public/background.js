const MESSAGE_TYPE = {
  CHAT: "CHAT",
  READY: "READY",
  HEART_BEAT: "HEART_BEAT",
  PRODUCT_INIT: "PRODUCT_INIT",
  REGISTRATION_TOKEN: "REGISTRATION_TOKEN",
}
const PATHS = {
  base: "http:localhost:8080",
  extension: "/extension"
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
  const host = `${PATHS.base}${PATHS.extension}`
  const requestBody = {
    product_url: url,
    html: html,
    token: chrome.storage.local.get("rId"),
    type: MESSAGE_TYPE.PRODUCT_INIT
  };
  post(host, requestBody)
}

function sendQuery(message) {
  const host = `${PATHS.base}${PATHS.extension}`
  const requestBody = {
    token: chrome.storage.local.get("rId"),
    type: MESSAGE_TYPE.CHAT,
    message: message,
    productSku: "INSERT_PRODUCT_SKU_HERE"
  };
  post(host, requestBody)
}

function heartbeatACK() {
  const host = `${PATHS.base}${PATHS.extension}`
  const requestBody = {
    token: chrome.storage.local.get("rId"),
    type: MESSAGE_TYPE.HEART_BEAT
  };
  post(host, requestBody)
}

function tokenRegistered(registration_id) {
  chrome.storage.local.set({"rId": registration_id})
  const host = `${PATHS.base}${PATHS.extension}`
  const requestBody = {
    token: registration_id,
    type: MESSAGE_TYPE.REGISTRATION_TOKEN
  };
  post(host, requestBody)
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
    heartbeatACK()
  } else if (message.data.type === MESSAGE_TYPE.READY) {
    chrome.storage.local.set({"isReady": true})
  }
  console.log("GCM message received: ", message)
  // chrome.gcm.send({
  //   data: {
  //     "sender": "shoppiem-extension",
  //     "content": "Your message has been received!"
  //   },
  //
  // }, sendQueryCb)
})

chrome.runtime.onMessage.addListener(function (request, sender, sendResponse) {
  if (request.type === MESSAGE_TYPE.CHAT) {
    sendQuery(request.message)
    sendResponse({ status: "done" });
  }
});