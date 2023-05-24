import React, {FC} from "react";
import {Button, Form, Input} from "rsuite";
import {ProductInfo} from "../../types";

export interface ProductFormProps {
  handleStartNewSession: () => void,
  handleProductInfoChange: (e: any, field: string) => void,
  showStartSessionButton: boolean,
  productMetadata?: ProductInfo
}
const Textarea = React.forwardRef((props, ref) => <Input {...props} as="textarea" ref={ref} />);

const ProductForm: FC<ProductFormProps> =  (props) => {

  return (
      <>
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
                value={props.productMetadata?.name}
                onChange={(e) => props.handleProductInfoChange(e, "name")}/>
          </Form.Group>
        </Form>
        <div className="break-2"></div>
        <Form className="product-info-form">
          <Form.Group controlId="product-url">
            <Form.ControlLabel>Product URL</Form.ControlLabel>
            <Form.Control
                name="product-url"
                value={props.productMetadata?.productUrl}
                onChange={(e) => props.handleProductInfoChange(e, "productUrl")}
            />
          </Form.Group>
        </Form>
        <div className="break-1"></div>
        {
            props.showStartSessionButton &&
            <Button appearance="primary" style={{width: "100%"}} onClick={props.handleStartNewSession}>
              Submit
            </Button>
        }
      </>
  );
}
export default ProductForm