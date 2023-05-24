import React, { ReactElement, useEffect, useState } from 'react';
import SendIcon from '@rsuite/icons/Send';
import { nanoid } from 'nanoid'

import {
  Drawer,
  Button,
  Row,
  Col,
  Form,
  Input
} from 'rsuite';
import 'rsuite/dist/rsuite.min.css';
import {useRef} from "react/index";


// @ts-ignore
import cartLogo from './assets/img/shoppiem_cart.png';
// @ts-ignore
import fullLogo from './assets/img/shoppiem_full_blue.png'
const Textarea = React.forwardRef((props, ref) => <Input {...props} as="textarea" ref={ref} />);

export interface Chat {
  message?: string,
  from_user: boolean,
  id: string,
  boundary: boolean
}

interface ProductInfo {
  name: string,
  imageUrl: string,
  productUrl: string
}

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

  const messageEndRef = useRef()

  useEffect(() => {
    setChatHistory(loadHistory())
  }, [])

  useEffect(() => {
    scrollToBottom()
    if (chatHistory.length > 0) {
      console.log("setting to false")
      setShowStartSessionButton(false)
    }

  }, [chatHistory])

  const scrollToBottom = () => {
    // @ts-ignore
    messageEndRef.current?.scrollIntoView({ behavior: "smooth" });
  }

  const historyWithBoundary = () => {
    return [...chatHistory, {
      message: undefined,
      from_user: false,
      id: boundaryId,
      boundary: true
    }]
  }

  const loadHistory = () => {
    return chatHistory
    try {
      // TODO: load history from the server
      // @ts-ignore
      const _history: Chat[] = JSON.parse(localStorage.getItem("history"))
      return _history ? _history : []
    } catch (e) {
      console.log(e)
    }
    return []
  }

  const truncate = (value: string): string => {
    const limit = 100
    if (value && value.length > limit) return value.substring(0, limit)+ "..."
    return value
  }

  const handleKeyDown = (e) => {
    if (e.key === 'Enter') {
      e.preventDefault()
      handleSubmit()
    }
  }

  
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
    const prevHistory = loadHistory()
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
        <Row style={{marginBottom: "1rem"}}>
          <Col xs={9}>
            <img  src={productMetadata?.imageUrl}  alt="Shoppiem" style={{width: "128px"}}/>
          </Col>
          <Col xs={15}>
            <p>
              <small>{truncate(productMetadata?.name)}</small>
            </p>
          </Col>
        </Row>
      </Drawer.Header>
      <Drawer.Body style={{bottom: 0}}>
        <div className="disclaimer">
          Shoppiem AI is in Beta. Please give us a feedback if you see something wrong.
        </div>
        <div className="section-header">
          Please confirm the information below
        </div>
        <div className="break-2"></div>
        <Form className="product-info-form">
          <Form.Group controlId="product-name">
            <Form.ControlLabel>Product Name</Form.ControlLabel>
            <Form.Control 
                name="textarea" 
                accepter={Textarea} 
                rows={3} 
                value={productMetadata?.name}
                onChange={(e) => handleProductInfoChange(e, "name")}/>
          </Form.Group>
        </Form>
        <div className="break-2"></div>
        <Form className="product-info-form">
          <Form.Group controlId="product-url">
            <Form.ControlLabel>Product URL</Form.ControlLabel>
            <Form.Control 
                name="product-url" 
                value={productMetadata?.productUrl}
                onChange={(e) => handleProductInfoChange(e, "productUrl")}
            />
          </Form.Group>
        </Form>
        <div className="break-1"></div>
        {
            showStartSessionButton &&
            <Button appearance="primary" style={{width: "100%"}} onClick={handleStartNewSession}>
              Submit
            </Button>
        }

        <div className="message-container">
          {
            historyWithBoundary().map(item => {
              if (item.boundary) {
                // @ts-ignore
                return <span className="chat-msg right empty" ref={messageEndRef} key={item.id}>BOUNDARY</span>
              } else if (item.from_user) {
                return <span className="chat-msg right" key={item.id}>{item.message}</span>
              } else {
                return <span className="chat-msg left" key={item.id}>{item.message}</span>
              }
            })
          }


          {/*<span className="chat-msg left">Hi! I'm Shoppiem AI! I can answer specific questions about products on Amazon.</span>*/}
          {/*<span className="chat-msg right">Hi! I'm Shoppiem AI! I can answer all your questions about most products on Amazon.</span>*/}
          {/*<span className="chat-msg right">Hi!</span>*/}
          {/*<span className="chat-msg right">(:)</span>*/}
          {/*<span className="chat-msg left">Hey there sir!</span>*/}
          {/*<span className="chat-msg left">Hey there sir!</span>*/}
          {/*<span className="chat-msg left">Hey there sir!</span>*/}
          {/*<span className="chat-msg right">Hi!</span>*/}
          {/*<span className="chat-msg right">(:)</span>*/}
        </div>
      </Drawer.Body>
      <Drawer.Footer>
        {
            showBubbleAnimation ?
            <div className="typing">
              <span className="circle scaling"></span>
              <span className="circle scaling"></span>
              <span className="circle scaling"></span>
            </div> : <div className="typing-empty"/>
        }
        <div className="chat-form-container">
          <Form className="chat-form">
            <Form.Group controlId="chat-box">
              <Form.Control
                  disabled={!serverReady}
                  name="textarea" 
                  accepter={Textarea} 
                  rows={2}
                  onChange={(e) => handleRawMessageChange(e)}
                  value={rawMessage}
                  onKeyDown={handleKeyDown}
                  placeholder="Ask a question"/>
            </Form.Group>
          </Form>
            <Button disabled={!serverReady}>
              <SendIcon />
            </Button>
        </div>
      </Drawer.Footer>
    </Drawer>

    <div className="absolute bottom-0 right-0 w-16 z-1000 mb-6 mr-4 flex justify-center items-center p-1">
      <Button active={true} onClick={handleOpen} size={"lg"}>
        <img  src={cartLogo} style={{background: "#3498ff", padding: "8px", borderRadius: '15px'}} alt="Shoppiem"/>
      </Button>
    </div>
      </div>
  )
}
