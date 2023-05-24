import React, { ReactElement, useEffect, useState } from 'react';
import { nanoid } from 'nanoid'
import {
  Drawer,
  Button,
} from 'rsuite';
import 'rsuite/dist/rsuite.min.css';


// @ts-ignore
import cartLogo from './assets/img/shoppiem_cart.png';
// @ts-ignore
import fullLogo from './assets/img/shoppiem_full_blue.png'
import {Chat, ProductInfo} from "./types";
import ChatInput from "./components/chat/input";
import ChatMessages from "./components/chat/messages";
import {loadHistory} from "./components/chat/utils";
import FloatingButton from "./components/FloatingButton";
import ProductCard from "./components/product/card";
import ProductForm from "./components/product/form";

const productInfo: ProductInfo = {
  name: "Dell Inspiron 15 3000 Series 3511 Laptop, 15.6\" FHD Touchscreen, Intel Core i5-1035G1, 32GB DDR4 RAM, 1TB PCIe SSD, SD Card Reader, Webcam, HDMI, Wi-Fi, Windows 11 Home, Black",
  imageUrl: "https://m.media-amazon.com/images/I/61KwCmF0bdL._AC_SL1500_.jpg",
  productUrl: "https://www.amazon.com/Dell-Inspiron-3511-Touchscreen-i5-1135G7/dp/B0B29C364N"
}


export default function App(): ReactElement {
  const [open, setOpen] = useState(false);
  const [chatHistory, setChatHistory] = useState<Chat[]>([])
  const [productId, setProductId] = useState('')
  const [productMetadata, setProductMetadata] = useState<ProductInfo>(productInfo)
  const [showBubbleAnimation, setShowBubbleAnimation] = useState(false)
  const [showStartSessionButton, setShowStartSessionButton] = useState(true)
  const [rawMessage, setRawMessage] = useState('')
  const [serverReady, setServerReady] = useState(false)
  const boundaryId = nanoid()

  useEffect(() => {
    setChatHistory(loadHistory(chatHistory))
  }, [])
  
  const handleProductInfoChange = (value: string, field: string) => {
    const newMetadata = {...productMetadata}
    // @ts-ignore
    newMetadata[field] = value
    setProductMetadata(newMetadata)
  }

  const handleRawMessageChange = (value: string) => {
    setRawMessage(value)
  }

  const onServerReady = () => {
    setShowBubbleAnimation(false)
    setServerReady(true)
    addToChatHistory("What would you like to know about this product?", false, false)
  }

  const handleStartNewSession = () => {
    setShowBubbleAnimation(true)
    setShowStartSessionButton(false)
    addToChatHistory("Processing this product. This may take up to two minutes.", false, false)
    setTimeout(() => onServerReady(), 5000);
  }

  const handleSubmit = () => {
    if (rawMessage) {
      // TODO send it to the server
      setShowBubbleAnimation(true)
      addToChatHistory(rawMessage, true, false)
      setRawMessage('')
    }
  }

  const addToChatHistory = (message: string, fromUser: boolean, boundary: boolean) => {
    const prevHistory = loadHistory(chatHistory)
    const newHistory = [...prevHistory,
      {
        message,
        from_user: fromUser,
        id: nanoid(),
        boundary: boundary
      } ]
    setChatHistory(newHistory)
  }

  const handleOpen = () => {
    setOpen(!open);
  };

  // @ts-ignore
  return (<div className="rounded-tl-lg">
    <Drawer size={"xs"} placement={"right"} open={open} onClose={() => setOpen(false)} className="z-max b">
      <Drawer.Header closeButton={false}>
        <Drawer.Title>
          <img className="shoppiem-full-logo" src={fullLogo} alt="Shoppiem"/>
        </Drawer.Title>
        <Drawer.Actions>
          <Button onClick={() => setOpen(false)}>Close</Button>
        </Drawer.Actions>
      </Drawer.Header>
      <Drawer.Header style={{marginTop: "1rem"}}>
        <ProductCard productMetadata={productMetadata}/>
      </Drawer.Header>
      <Drawer.Body style={{bottom: 0}}>
        <ProductForm
            handleStartNewSession={handleStartNewSession}
            handleProductInfoChange={handleProductInfoChange}
            showStartSessionButton={showStartSessionButton}
            productMetadata={productMetadata}
        />
        <ChatMessages
            chatHistory={chatHistory}
            boundaryId={boundaryId}
            setShowStartSessionButton={setShowStartSessionButton}
        />
      </Drawer.Body>
      <Drawer.Footer>
        <ChatInput
            handleSubmit={handleSubmit}
            handleRawMessageChange={handleRawMessageChange}
            serverReady={serverReady}
            showBubbleAnimation={showBubbleAnimation}
            rawMessage={rawMessage}
        />
      </Drawer.Footer>
    </Drawer>
        <FloatingButton handleOpen={handleOpen}/>
  </div>
  )
}
