import { USER_TYPE } from './constants'
import { logout } from '../api/auth'
import { ElMessage } from 'element-plus'
import store from '../store'

/**
 * 用户相关工具函数
 */

/**
 * 检查用户是否为店铺用户
 */
export const isShopUser = (user) => {
  return user && user.userType === USER_TYPE.SHOP
}

/**
 * 检查用户是否为普通顾客
 */
export const isCustomer = (user) => {
  return user && user.userType === USER_TYPE.CUSTOMER
}

/**
 * 统一的退出登录处理函数
 * @param {Object} router - Vue Router实例
 */
export const handleLogout = async (router) => {
  try {
    await logout()
    // clearUser 会保存当前用户的购物车并清空
    store.clearUser()
    ElMessage.success('退出登录成功')
    router.push('/')
  } catch (error) {
    // 即使登出失败，也要清空用户和购物车
    store.clearUser()
    router.push('/')
  }
}

