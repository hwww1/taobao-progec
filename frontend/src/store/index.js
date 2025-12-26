import { reactive } from 'vue'
import { getCurrentUser } from '../api/auth'

const store = reactive({
  user: null,
  cart: new Map(),
  
  async init() {
    try {
      const response = await getCurrentUser()
      if (response && response.success && typeof response.data === 'object') {
        this.user = response.data
        // 清理旧的购物车数据（如果存在）
        this.cleanOldCartData()
        // 加载当前用户的购物车
        this.loadCartForUser(response.data.userId)
      } else {
        this.user = null
        // 如果没有登录，清空购物车并清理旧数据
        this.cart.clear()
        this.cleanOldCartData()
      }
    } catch (error) {
      // 静默失败
      this.user = null
      this.cart.clear()
      this.cleanOldCartData()
    }
  },
  
  cleanOldCartData() {
    // 清理旧的购物车数据（使用固定键 'cart' 的旧数据）
    try {
      localStorage.removeItem('cart')
    } catch (error) {
      // 忽略错误
    }
  },
  
  loadCartForUser(userId) {
    // 根据用户ID加载购物车数据
    const cartKey = `cart_${userId}`
    const cartData = localStorage.getItem(cartKey)
    if (cartData) {
      const cartMap = JSON.parse(cartData)
      this.cart = new Map(Object.entries(cartMap).map(([k, v]) => [parseInt(k), v]))
    } else {
      // 如果没有该用户的购物车数据，清空购物车
      this.cart.clear()
    }
  },
  
  setUser(user) {
    // 切换用户时，清空旧购物车，加载新用户的购物车
    const oldUserId = this.user ? this.user.userId : null
    this.user = user
    if (user && user.userId) {
      // 保存旧用户的购物车（如果有）
      if (oldUserId && oldUserId !== user.userId) {
        this.saveCartForUser(oldUserId)
      }
      // 加载新用户的购物车
      this.loadCartForUser(user.userId)
    } else {
      // 如果没有用户，清空购物车
      this.cart.clear()
    }
  },
  
  clearUser() {
    // 登出时，保存当前用户的购物车，然后清空
    if (this.user && this.user.userId) {
      this.saveCartForUser(this.user.userId)
    }
    this.user = null
    this.cart.clear()
  },
  
  addToCart(productId, quantity) {
    const current = this.cart.get(productId) || 0
    this.cart.set(productId, current + quantity)
    this.saveCart()
  },
  
  updateCart(productId, quantity) {
    if (quantity <= 0) {
      this.cart.delete(productId)
    } else {
      this.cart.set(productId, quantity)
    }
    this.saveCart()
  },
  
  removeFromCart(productId) {
    this.cart.delete(productId)
    this.saveCart()
  },
  
  clearCart() {
    this.cart.clear()
    this.saveCart()
  },
  
  saveCart() {
    // 如果有登录用户，保存到该用户的购物车
    if (this.user && this.user.userId) {
      this.saveCartForUser(this.user.userId)
    }
  },
  
  saveCartForUser(userId) {
    // 保存指定用户的购物车数据
    const cartKey = `cart_${userId}`
    const cartObj = Object.fromEntries(this.cart)
    localStorage.setItem(cartKey, JSON.stringify(cartObj))
  },
  
  getCartSize() {
    return Array.from(this.cart.values()).reduce((sum, qty) => sum + qty, 0)
  }
})

export default store

