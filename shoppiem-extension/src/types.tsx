export interface Chat {
  message?: string,
  from_user: boolean,
  id: string,
  boundary?: boolean
}

export interface ProductInfo {
  name: string,
  imageUrl?: string,
  productUrl?: string
}

export const MESSAGE_TYPE = {
  CHAT: "CHAT",
  PRODUCT_STATUS: "PRODUCT_STATUS",
  HEART_BEAT: "HEART_BEAT",
  PRODUCT_INIT: "PRODUCT_INIT",
  REGISTRATION_TOKEN: "REGISTRATION_TOKEN",
  PRODUCT_INFO_REQUEST: "PRODUCT_INFO_REQUEST"
}