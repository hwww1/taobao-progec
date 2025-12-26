import request from './request'

export const getAddresses = () => {
  return request.get('/addresses')
}

export const addAddress = (address) => {
  return request.post('/addresses', address)
}

export const updateAddress = (id, address) => {
  return request.put(`/addresses/${id}`, address)
}

export const deleteAddress = (id) => {
  return request.delete(`/addresses/${id}`)
}

