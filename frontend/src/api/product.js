import request from './request'

export const getProducts = (params = {}) => {
  return request.get('/products', { params })
}

export const getProductById = (id) => {
  return request.get(`/products/${id}`)
}

export const getProductsByShopId = (shopId) => {
  return request.get(`/products/shop/${shopId}`)
}

export const addProduct = (product) => {
  return request.post('/products', product)
}

export const updateProduct = (id, product) => {
  return request.put(`/products/${id}`, product)
}

// 分类
export const getCategories = () => {
  return request.get('/categories')
}

