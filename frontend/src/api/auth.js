import request from './request'

export const login = (username, password) => {
  return request.post('/auth/login', { username, password })
}

export const register = (username, password, userType) => {
  return request.post('/auth/register', { username, password, userType })
}

export const logout = () => {
  return request.post('/auth/logout')
}

export const getCurrentUser = () => {
  return request.get('/auth/current')
}

export const changePassword = (newPassword) => {
  return request.post('/auth/change-password', { newPassword })
}

export const updateProfile = (username) => {
  return request.put('/auth/profile', { username })
}

