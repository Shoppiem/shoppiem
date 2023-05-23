import React, { ReactElement, useEffect, useState } from 'react';
import { APP_COLLAPSE_WIDTH, APP_EXTEND_WIDTH, URLS } from './const';
import SendIcon from '@rsuite/icons/Send';

import {
  Drawer,
  Button,
  Row,
  Col,
  Message,
  Form,
  Input,
  Divider} from 'rsuite';

import cartLogo from './assets/img/shoppiem_cart.png';
import fullLogo from './assets/img/shoppiem_full_blue.png'
const Textarea = React.forwardRef((props, ref) => <Input {...props} as="textarea" ref={ref} />);


import 'rsuite/dist/rsuite.min.css';
const styles = {
  radioGroupLabel: {
    padding: '8px 12px',
    display: 'inline-block',
    verticalAlign: 'middle'
  }
};

// const inputStyles = {
//   width: "100%",
//   height: "48px",
//   marginBottom: 300,
//   overflowX: "break-word",
//   border: "1px solid #cecece"
// };

const productInfo = {
  name: "Dell Inspiron 15 3000 Series 3511 Laptop, 15.6\" FHD Touchscreen, Intel Core i5-1035G1, 32GB DDR4 RAM, 1TB PCIe SSD, SD Card Reader, Webcam, HDMI, Wi-Fi, Windows 11 Home, Black",
  imageUrl: "https://m.media-amazon.com/images/I/61KwCmF0bdL._AC_SL1500_.jpg",
  productUrl: "https://www.amazon.com/Dell-Inspiron-3511-Touchscreen-i5-1135G7/dp/B0B29C364N"
}


export default function App({ onWidthChange, initialEnabled }: { onWidthChange: (value: number) => void, initialEnabled: boolean }): ReactElement {
  const [enabled, setEnabled] = useState(initialEnabled);
  const [sidePanelWidth, setSidePanelWidth] = useState(enabled ? APP_EXTEND_WIDTH: APP_COLLAPSE_WIDTH);
  const [open, setOpen] = useState(false);

  function handleOnToggle(enabled: boolean) {
    const value = enabled ? APP_EXTEND_WIDTH : APP_COLLAPSE_WIDTH;
    setSidePanelWidth(value);
    onWidthChange(value);

    window['chrome'].storage?.local.set({enabled});
  }

  const truncate = (value: string): string => {
    const limit = 100
    if (value && value.length > limit) return value.substring(0, limit)+ "..."
    return value
  }

  const handleOpen = () => {
    setOpen(!open);
  };

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
            <img  src={productInfo.imageUrl}  alt="Shoppiem" style={{width: "128px"}}/>
          </Col>
          <Col xs={15}>
            <p>
              <small>{truncate(productInfo.name)}</small>
            </p>
          </Col>
        </Row>
      </Drawer.Header>
      <Drawer.Body style={{bottom: 0}}>
        <Message type="info">
          Please provide the information below
        </Message>
        <br></br>
        <br></br>
        <Form className="product-info-form">
          <Form.Group controlId="product-name">
            <Form.ControlLabel>Product Name</Form.ControlLabel>
            <Form.Control name="textarea" accepter={Textarea} rows={3} value={productInfo.name}/>
          </Form.Group>
        </Form>
        <br></br>
        <br></br>
        <Form className="product-info-form">
          <Form.Group controlId="product-url">
            <Form.ControlLabel>Product URL</Form.ControlLabel>
            <Form.Control name="product-url" value={productInfo.productUrl}/>
          </Form.Group>
        </Form>
        <br></br>
        {/*<br></br>*/}
        <Button appearance="primary" style={{width: "100%"}}>
          Submit
        </Button>
      </Drawer.Body>
      <Drawer.Footer>

        {/*<Form className="product-info-form">*/}
        {/*  <Form.Group controlId="chat-box">*/}
        {/*    /!*<Form.ControlLabel>Product Name</Form.ControlLabel>*!/*/}
        {/*    <Form.Control name="textarea" accepter={Textarea} rows={5}*/}
        {/*                  placeholder="Ask a question"/>*/}
        {/*  </Form.Group>*/}
        {/*</Form>*/}

        {/*<Form className="chat-form">*/}
        {/*  <Form.Group controlId="chat-box">*/}
        {/*    <Form.ControlLabel>Product Name</Form.ControlLabel>*/}
        {/*    <Form.Control name="textarea" accepter={Textarea} rows={5} placeholder="Ask a question"/>*/}
        {/*  </Form.Group>*/}
        {/*</Form>*/}

        <div className="chat-form-container">
          <Form className="chat-form">
            <Form.Group controlId="chat-box">
              <Form.Control name="textarea" accepter={Textarea} rows={3} placeholder="Ask a question"/>
            </Form.Group>
          </Form>
            <Button>
              <SendIcon />
            </Button>
        </div>
      </Drawer.Footer>
    </Drawer>



  {/*  <Drawer open={open} onClose={() => setOpen(false)}>*/}
  {/*  <Drawer.Header>*/}
  {/*    <Drawer.Title>Drawer Title</Drawer.Title>*/}
  {/*  </Drawer.Header>*/}
  {/*  <Drawer.Body>*/}
  {/*    /!*<Paragraph />*!/*/}
  {/*  </Drawer.Body>*/}
  {/*  <Drawer.Footer>*/}
  {/*    <Button appearance="primary">Confirm</Button>*/}
  {/*    <Button appearance="subtle">Cancel</Button>*/}
  {/*  </Drawer.Footer>*/}
  {/*</Drawer>*/}


    <div className="absolute bottom-0 right-0 w-16 z-1000 mb-6 mr-4 flex justify-center items-center p-1">
      <Button active={true} onClick={handleOpen} size={"lg"}>
        <img  src={cartLogo} style={{background: "#3498ff", padding: "8px", borderRadius: '15px'}} alt="Shoppiem"/>
      </Button>
    </div>
      </div>
  )
}
