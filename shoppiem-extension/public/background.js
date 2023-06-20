const MESSAGE_TYPE = {
  CHAT: "CHAT",
  HEART_BEAT: "HEART_BEAT",
  PRODUCT_INIT: "PRODUCT_INIT",
  FCM_TOKEN: "FCM_TOKEN",
  PRODUCT_INFO_REQUEST: "PRODUCT_INFO_REQUEST"
}
const PATHS = {
  base: "http://localhost:8080",
  extension: "/extension"
}
chrome.runtime.onInstalled.addListener(() => {
  console.log('Extension Installed')
});

function getHtml() {return document.documentElement.outerHTML; }

function getProductInfoKey(productSku) {
  return "productInfo-" + productSku;
}

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

async function initProduct(url, html) {
  const host = `${PATHS.base}${PATHS.extension}`
  const token = await chrome.storage.local.get("rId")
  const requestBody = {
    product_url: url,
    html: html,
    token: token.rId,
    type: MESSAGE_TYPE.PRODUCT_INIT
  };
  post(host, requestBody)
}

async function sendQuery(request) {
  const host = `${PATHS.base}${PATHS.extension}`
  const token = await chrome.storage.local.get("rId")
  const requestBody = {
    token: token.rId,
    type: MESSAGE_TYPE.CHAT,
    query: request.query,
    product_sku: request.productSku
  };
  post(host, requestBody)
}

async function sendProductInfoRequest(productSku) {
  const host = `${PATHS.base}${PATHS.extension}`
  const token = await chrome.storage.local.get("rId")
  const requestBody = {
    token: token.rId,
    type: MESSAGE_TYPE.PRODUCT_INFO_REQUEST,
    product_sku: productSku
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

async function tokenRegistered(registration_id) {
  await chrome.storage.local.set({"rId": registration_id})
  const host = `${PATHS.base}${PATHS.extension}`
  const requestBody = {
    token: registration_id,
    type: MESSAGE_TYPE.FCM_TOKEN
  };
  post(host, requestBody)
}

chrome.tabs.onUpdated.addListener(
    function(tabId, changeInfo, tab) {
      const url = changeInfo.url
      if (url && url.includes("amazon.com") && url.includes("/dp/")) {
        chrome.scripting
        .executeScript({
          target : {tabId : tab.id, allFrames : true},
          func: getHtml,
        })
        .then(injectionResults => {
          for (const {frameId, result} of injectionResults) {
            initProduct(url, result)
            .catch(err => console.log(err));
            break
          }
        });
      }
    }
);
chrome.gcm.register(["658613891142"], tokenRegistered)

async function sendMessageToClient(message) {
  const tab = await getCurrentTab(message.data.productSku);
  await chrome.tabs.sendMessage(tab.id, {
    type: MESSAGE_TYPE.CHAT,
    content: message.data.content
  });
}

chrome.gcm.onMessage.addListener((message) => {
  if (message.data.type === MESSAGE_TYPE.HEART_BEAT) {
    heartbeatACK()
  } else if (message.data.type === MESSAGE_TYPE.CHAT) {
    sendMessageToClient(message)
    .catch(err => console.log(err));
  } else if (message.data.type === MESSAGE_TYPE.PRODUCT_INFO_REQUEST) {
    const obj = {}
    obj[getProductInfoKey(message.data.productSku)] = {
      name: message.data.name,
      imageUrl: message.data.imageUrl
    }
    chrome.storage.local.set(obj)
  }
})

async function sendProductInfo(productSku) {
  const tab = await getCurrentTab(productSku);
  if (tab) {
    const key = getProductInfoKey(productSku);
    const productInfo = await chrome.storage.local.get(key)
    if (productInfo && productInfo[key]) {
      await chrome.tabs.sendMessage(tab.id, {
        type: MESSAGE_TYPE.PRODUCT_INFO_REQUEST,
        name: productInfo[key]["name"],
        imageUrl: productInfo[key]["imageUrl"]
      });
    } else {
      await sendProductInfoRequest(productSku);
    }
  }
}

chrome.runtime.onMessage.addListener(function (request, sender, sendResponse) {
  if (request.type === MESSAGE_TYPE.CHAT) {
    sendQuery(request)
    .catch(err => console.log(err));
    sendResponse({ status: "done" });
  } else if (request.type === MESSAGE_TYPE.PRODUCT_INFO_REQUEST) {
    sendProductInfo(request.productSku)
    .catch(e => console.log(e));
    sendResponse(true)
  }
});
