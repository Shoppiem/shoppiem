const MESSAGE_TYPE = {
  CHAT: "CHAT",
  PRODUCT_STATUS: "PRODUCT_STATUS",
  HEART_BEAT: "HEART_BEAT",
  PRODUCT_INIT: "PRODUCT_INIT",
  REGISTRATION_TOKEN: "REGISTRATION_TOKEN",
  PRODUCT_INFO_REQUEST: "PRODUCT_INFO_REQUEST"
}
const PATHS = {
  base: "http:localhost:8080",
  extension: "/extension"
}
chrome.runtime.onInstalled.addListener(() => {
  console.log('Extension Installed')
});

async function getCurrentTab(productSku) {
  const tabs = await chrome.tabs.query({active: true})
  if (tabs) {
    for (let i = 0; i < tabs.length; i++) {
      const tab = tabs[i];
      if (tab.url?.includes(productSku)) {
        return tab;
      }
    }
  }
  return undefined;
}

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
    // console.log(data);
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
  console.log("Calling server")
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

function getTitle() { return document.title; }

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
            initProduct(url, result);
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
  } else if (message.data.type === MESSAGE_TYPE.PRODUCT_STATUS) {
    chrome.storage.local.set({"isReady": message.data.status})
  } else if (message.data.type === MESSAGE_TYPE.CHAT) {

  }
  console.log("GCM message received: ", message)
})

async function sendProductInfo(productSku) {
  const tab = await getCurrentTab(productSku);
  if (tab) {
    await chrome.tabs.sendMessage(tab.id, {
      type: MESSAGE_TYPE.PRODUCT_INFO_REQUEST,
      name: tab.title.replace("Amazon.com:", "").trim(),
      imageUrl: "https://m.media-amazon.com/images/I/61KwCmF0bdL._AC_SL1500_.jpg"
    });
  }
}

chrome.runtime.onMessage.addListener(function (request, sender, sendResponse) {
  if (request.type === MESSAGE_TYPE.CHAT) {
    sendQuery(request.message)
    sendResponse({ status: "done" });
  } else if (request.type === MESSAGE_TYPE.PRODUCT_INFO_REQUEST) {
    sendProductInfo(request.productSku)
    .catch(e => console.log(e));
    sendResponse(true)
  }
});
