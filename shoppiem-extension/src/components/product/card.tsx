import React, {FC} from "react";
import {Col, Row} from "rsuite";
import {ProductInfo} from "../../types";

export interface ProductCardProps {
  productMetadata?: ProductInfo
}
const ProductCard: FC<ProductCardProps> =  (props) => {

  const truncate = (value: string | undefined): string | undefined => {
    const limit = 100
    if (value && value.length > limit) return value.substring(0, limit)+ "..."
    return value
  }

  return (
      <Row style={{marginBottom: "1rem"}}>
        <Col xs={9}>
          <img  src={props.productMetadata?.imageUrl}  alt="Shoppiem" style={{width: "128px"}}/>
        </Col>
        <Col xs={15}>
          <p>
            <small>{truncate(props.productMetadata?.name)}</small>
          </p>
        </Col>
      </Row>
  );
}
export default ProductCard