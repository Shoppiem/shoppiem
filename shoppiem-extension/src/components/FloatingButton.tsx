import React, {FC} from "react";
import {Button, Input} from "rsuite";
import WechatIcon from '@rsuite/icons/Wechat';

export interface FloatingButtonProps {
  handleOpen: () => void
}

const FloatingButton: FC<FloatingButtonProps> =  (props) => {

  return (
      <div className="absolute bottom-0 right-0 w-16 z-1000 mb-6 mr-4 flex justify-center items-center p-1">
        <Button active={true} onClick={props.handleOpen} size={"lg"}>
          {/*<WechatIcon/>*/}
          <img src="/shoppiem_cart.png" style={{background: "#3498ff", padding: "8px", borderRadius: '15px'}} alt="Shoppiem"/>
        </Button>
      </div>
  );
}
export default FloatingButton