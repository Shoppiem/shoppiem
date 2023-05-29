chrome.runtime.onInstalled.addListener(() => {
  console.log('Extension Installed')
});

function initProduct(url) {
  const host = "http://localhost:8080/product"
  const requestBody = {
    product_url: url
  };

  fetch(host, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(requestBody)
  })
  .then(response => response.json())
  .then(data => {
    console.log(data);
  })
  .catch(error => {
    console.error(error);
  });
}

chrome.tabs.onUpdated.addListener(
    function(tabId, changeInfo, tab) {
      const url = changeInfo.url
      if (url && url.includes("amazon.com")) {
        initProduct(url)
      }
    }
);