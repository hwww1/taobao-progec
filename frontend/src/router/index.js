import { createRouter, createWebHistory } from 'vue-router'
import { getCurrentUser } from '../api/auth'
import { USER_TYPE } from '../utils/constants'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('../views/customer/Home.vue')
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/auth/Login.vue')
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/auth/Register.vue')
  },
  {
    path: '/product/:id',
    name: 'ProductDetail',
    component: () => import('../views/customer/ProductDetail.vue')
  },
  {
    path: '/category/:id',
    name: 'Category',
    component: () => import('../views/customer/Category.vue')
  },
  {
    path: '/cart',
    name: 'Cart',
    component: () => import('../views/customer/Cart.vue'),
    meta: { requiresAuth: true, requiresCustomer: true }
  },
  {
    path: '/orders',
    name: 'Orders',
    component: () => import('../views/customer/Orders.vue'),
    meta: { requiresAuth: true, requiresCustomer: true }
  },
  {
    path: '/profile',
    name: 'Profile',
    component: () => import('../views/auth/Profile.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/order/:id',
    name: 'OrderDetail',
    component: () => import('../views/customer/OrderDetail.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/shop/dashboard',
    name: 'ShopDashboard',
    component: () => import('../views/shop/Dashboard.vue'),
    meta: { requiresAuth: true, requiresShop: true }
  },
  {
    path: '/shop/products',
    name: 'ShopProducts',
    component: () => import('../views/shop/Products.vue'),
    meta: { requiresAuth: true, requiresShop: true }
  },
  {
    path: '/shop/orders',
    name: 'ShopOrders',
    component: () => import('../views/shop/Orders.vue'),
    meta: { requiresAuth: true, requiresShop: true }
  },
  {
    path: '/shop/info',
    name: 'ShopInfo',
    component: () => import('../views/shop/ShopInfo.vue'),
    meta: { requiresAuth: true, requiresShop: true }
  },
  {
    path: '/operator/dashboard',
    name: 'OperatorDashboard',
    component: () => import('../views/operator/Dashboard.vue'),
    meta: { requiresAuth: true, requiresOperator: true }
  },
  {
    path: '/operator/management',
    name: 'OperatorManagement',
    component: () => import('../views/operator/Management.vue'),
    meta: { requiresAuth: true, requiresOperator: true }
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach(async (to, from, next) => {
  if (to.meta.requiresAuth) {
    try {
      const response = await getCurrentUser()
      const user = response && response.success && typeof response.data === 'object' ? response.data : null
      if (!user) {
        next({ name: 'Login' })
      } else {
        // 店铺用户不能访问顾客专用页面
        if (to.meta.requiresCustomer && user.userType === USER_TYPE.SHOP) {
          next({ name: 'ShopDashboard' })
          return
        }
        // 普通用户不能访问店铺专用页面
        if (to.meta.requiresShop && user.userType !== USER_TYPE.SHOP) {
          next({ name: 'Home' })
          return
        }
        // 非运营商不能访问运营商页面
        if (to.meta.requiresOperator && user.userType !== USER_TYPE.OPERATOR) {
          next({ name: 'Home' })
          return
        }
        next()
      }
    } catch (error) {
      next({ name: 'Login' })
    }
  } else {
    next()
  }
})

export default router

