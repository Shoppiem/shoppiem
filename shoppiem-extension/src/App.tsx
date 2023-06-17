import React, { ReactElement, useEffect, useState } from 'react';
import { nanoid } from 'nanoid'
import 'rsuite/dist/rsuite.min.css';
import CloseIcon from '@rsuite/icons/Close';
import { Drawer } from 'rsuite';

// @ts-ignore
import cartLogo from './assets/img/shoppiem_cart.png';
// @ts-ignore
import fullLogo from './assets/img/shoppiem_full_blue.png'
import {Chat, ProductInfo} from "./types";
import ChatInput from "./components/chat/input";
import ChatMessages from "./components/chat/messages";
import {loadHistory} from "./components/chat/utils";
import FloatingButton from "./components/logo/FloatingButton";
import ProductCard from "./components/product/card";
import ProductForm from "./components/product/hero";
import FullLogo from "./components/logo/FullLogo";
import Hero from './components/product/hero';

const productInfo: ProductInfo = {
  name: "Dell Inspiron 15 3000 Series 3511 Laptop, 15.6\" FHD Touchscreen, Intel Core i5-1035G1, 32GB DDR4 RAM, 1TB PCIe SSD, SD Card Reader, Webcam, HDMI, Wi-Fi, Windows 11 Home, Black",
  imageUrl: "https://m.media-amazon.com/images/I/61KwCmF0bdL._AC_SL1500_.jpg",
  productUrl: "https://www.amazon.com/Dell-Inspiron-3511-Touchscreen-i5-1135G7/dp/B0B29C364N"
}

export default function App(): ReactElement {
  const [open, setOpen] = useState(true);
  const [chatHistory, setChatHistory] = useState<Chat[]>([])
  const [productMetadata, setProductMetadata] = useState<ProductInfo | undefined>(productInfo)
  const [showBubbleAnimation, setShowBubbleAnimation] = useState(false)
  const [rawMessage, setRawMessage] = useState('')
  const [serverReady, setServerReady] = useState(false)
  const boundaryId = nanoid()

  useEffect(() => {
    setChatHistory(loadHistory(chatHistory))
    setTimeout(() => setServerReady(true), 10000);
  }, [])

  useEffect(() => {
    if (chatHistory.length == 0 && !serverReady) {
      addToChatHistory("We are currently processing this product. Please check again in a few minutes.", false)
      setShowBubbleAnimation(true)
    }
  }, [chatHistory])

  useEffect(() => {
    if (serverReady && chatHistory.length == 1) {
      setShowBubbleAnimation(false)
      addToChatHistory("What would you like to know about " + productMetadata?.name + "?", false)
    }
  }, [serverReady])

  const handleRawMessageChange = (value: string) => {
    setRawMessage(value)
  }

  const handleSubmit = () => {
    if (rawMessage) {
      // TODO send it to the server
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
          {productMetadata?.name &&
            <Drawer.Header style={{marginTop: "1rem"}}>
              <ProductCard productMetadata={productMetadata}/>
            </Drawer.Header>
          }
      <Drawer.Body style={{bottom: 0}}>
        <Hero/>
        {productMetadata?.name &&
          <ChatMessages
              chatHistory={chatHistory}
              boundaryId={boundaryId}
          />
        }
      </Drawer.Body>
      <Drawer.Footer>
        {productMetadata?.name &&
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
