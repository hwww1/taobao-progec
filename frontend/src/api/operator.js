import request from './request'

export const getDashboard = () => {
  return request.get('/operator/dashboard')
}

export const getAllShops = () => {
  return request.get('/operator/shops')
}

export const getAllUsers = () => {
  return request.get('/operator/users')
}

export const deleteUser = (userId) => {
  return request.delete(`/operator/users/${userId}`)
}

export const deleteShop = (shopId, password) => {
  return request.delete(`/operator/shops/${shopId}`, {
    data: { password }
  })
}

