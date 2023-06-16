export interface Chat {
  message?: string,
  from_user: boolean,
  id: string,
  boundary?: boolean
}

export interface ProductInfo {
  name: string,
  imageUrl: string,
  productUrl: string
}