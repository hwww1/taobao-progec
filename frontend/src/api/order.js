import request from './request'

export const createOrder = (cartItems) => {
  return request.post('/orders', cartItems)
}

export const getCustomerOrders = () => {
  return request.get('/orders/customer')
}

export const getShopOrders = () => {
  return request.get('/orders/shop')
}

export const getOrderById = (id) => {
  return request.get(`/orders/${id}`)
}

export const processPayment = (id) => {
  return request.post(`/orders/${id}/payment`)
}

export const processShipment = (id) => {
  return request.post(`/orders/${id}/shipment`)
}

export const confirmReceipt = (id) => {
  return request.post(`/orders/${id}/receipt`)
}

export const cancelOrder = (id) => {
  return request.post(`/orders/${id}/cancel`)
}

