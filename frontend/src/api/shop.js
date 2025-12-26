import request from './request'

export const getShopInfo = () => {
  return request.get('/shop/info')
}

export const updateShopInfo = (shop) => {
  return request.put('/shop/info', shop)
}

