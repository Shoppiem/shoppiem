import React, { ReactElement, useEffect, useState } from 'react';
import { nanoid } from 'nanoid'
import 'rsuite/dist/rsuite.min.css';
import CloseIcon from '@rsuite/icons/Close';
import { Drawer } from 'rsuite';
import {Chat, ProductInfo} from "./types";
import ChatInput from "./components/chat/input";
import ChatMessages from "./components/chat/messages";
import {loadHistory} from "./components/chat/utils";
import FloatingButton from "./components/logo/FloatingButton";
import ProductCard from "./components/product/card";
import FullLogo from "./components/logo/FullLogo";
import Hero from './components/product/hero';
import { Placeholder } from 'rsuite';

export default function App(): ReactElement {
  const [open, setOpen] = useState(false);
  const [chatHistory, setChatHistory] = useState<Chat[]>([])
  const [productMetadata, setProductMetadata] = useState<ProductInfo | undefined>(undefined)
  const [productSku, setProductSku] = useState("")
  const [showBubbleAnimation, setShowBubbleAnimation] = useState(false)
  const [rawMessage, setRawMessage] = useState('')
  const [serverReady, setServerReady] = useState(false)
  const [productStatusUpdated, setProductStatusUpdated] = useState(false)
  const boundaryId = nanoid()

  useEffect(() => {
    setChatHistory(loadHistory(chatHistory))
    setTimeout(() => setServerReady(true), 10000);
  }, [])

  useEffect(() => {
    const href = window.location.href
    if (href.includes("/dp/")) {
      setProductSku(window.location.href.split("/dp/")[1].split("/")[0]);
    }
    return undefined;
  }, [])

  useEffect(() => {
    // @ts-ignore
    chrome?.runtime?.onMessage?.addListener(function (request, sender, sendResponse) {
      if (request.type === "PRODUCT_INFO_REQUEST" && !productMetadata?.name) {
        console.log("Setting product metadata: ", request)
        setProductMetadata({
          name: request.name,
          imageUrl: request.imageUrl
        })
        addToChatHistory(`What would you like to know about <strong>${request.name}</strong>?`, false)
        sendResponse(true)
      } else if (request.type === "PRODUCT_STATUS") {
        if (!request.type.status && !productStatusUpdated) {
          setProductStatusUpdated(true)
          addToChatHistory("We are currently processing this product. Please wait a minute or two.", false)
          setShowBubbleAnimation(true)
        }
      } else if (request.type === "CHAT") {
        addToChatHistory(request.content, false)
        setShowBubbleAnimation(false)
      }
    });

    if (productSku) {
      (async () => {
        // @ts-ignore
        await chrome?.runtime?.sendMessage({
          type: "PRODUCT_INFO_REQUEST",
          productSku: productSku
        });
      })();
    }
  }, [productSku])

  const handleRawMessageChange = (value: string) => {
    setRawMessage(value)
  }

  const handleSubmit = () => {
    if (rawMessage) {
      (async () => {
        // @ts-ignore
        await chrome?.runtime?.sendMessage({
          type: "CHAT",
          query: rawMessage
        });
      })()
      setShowBubbleAnimation(true)
      addToChatHistory(rawMessage, true)
      setRawMessage('')
    }
  }

  const addToChatHistory = (message: string, fromUser: boolean) => {
    const prevHistory = loadHistory(chatHistory)
    const newHistory = [...prevHistory,
      {
        message,
        from_user: fromUser,
        id: nanoid()
      } ]
    localStorage.setItem("history", JSON.stringify(newHistory))
    setChatHistory(newHistory)
  }

  const handleOpen = () => {
    setOpen(!open);
  };

  // @ts-ignore
  return (<div>
        <Drawer size={"xs"} placement={"right"} open={open} onClose={() => setOpen(false)} className="z-max b">
      <Drawer.Header closeButton={false}>
        <Drawer.Title>
          <FullLogo/>
        </Drawer.Title>
        <Drawer.Actions>
          <CloseIcon onClick={() => setOpen(false)} className="close-btn"/>
        </Drawer.Actions>
      </Drawer.Header>
        <Drawer.Header style={{marginTop: "1rem"}}>
          {productMetadata?.name && productMetadata?.imageUrl ?
          <ProductCard productMetadata={productMetadata}/> :
              <Placeholder.Paragraph rows={3} graph="image" active />
          }
        </Drawer.Header>
      <Drawer.Body style={{bottom: 0}}>
        <Hero/>
        {productSku &&
          <ChatMessages
              chatHistory={chatHistory}
              boundaryId={boundaryId}
          />
        }
      </Drawer.Body>
      <Drawer.Footer>
        {productSku &&
          <ChatInput
              handleSubmit={handleSubmit}
              handleRawMessageChange={handleRawMessageChange}
              serverReady={serverReady}
              showBubbleAnimation={showBubbleAnimation}
              rawMessage={rawMessage}
          />
        }
      </Drawer.Footer>
    </Drawer>
        <FloatingButton handleOpen={handleOpen}/>
  </div>
  )
}
