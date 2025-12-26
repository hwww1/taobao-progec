<template>
  <div class="home">
    <el-header class="header">
      <div class="header-content">
        <h1>拼东宝</h1>
        <div class="header-actions">
          <!-- 普通用户显示购物车 -->
          <template v-if="user && !isShopUser(user)">
            <el-button @click="$router.push('/cart')" :icon="ShoppingCart">购物车</el-button>
            <el-button @click="goToOrders">我的订单</el-button>
          </template>
          <!-- 店铺用户显示店铺管理 -->
          <template v-if="isShopUser(user)">
            <el-button @click="$router.push('/shop/dashboard')">店铺管理</el-button>
            <el-button @click="$router.push('/shop/orders')">订单管理</el-button>
            <el-button @click="$router.push('/shop/products')">商品管理</el-button>
          </template>
          <!-- 运营商显示管理入口 -->
          <template v-if="user && user.userType === USER_TYPE.OPERATOR">
            <el-button @click="$router.push('/operator/dashboard')">运营商管理</el-button>
          </template>
          <el-button @click="goToProfile">个人信息</el-button>
          <template v-if="user">
            <el-dropdown>
              <span class="user-info">{{ user.username }}</span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="$router.push('/profile')">个人信息</el-dropdown-item>
                  <el-dropdown-item v-if="!isShopUser(user)" @click="$router.push('/orders')">我的订单</el-dropdown-item>
                  <el-dropdown-item v-if="isShopUser(user)" @click="$router.push('/shop/dashboard')">店铺管理</el-dropdown-item>
                  <el-dropdown-item v-if="isShopUser(user)" @click="$router.push('/shop/orders')">订单管理</el-dropdown-item>
                  <el-dropdown-item v-if="user.userType === USER_TYPE.OPERATOR" @click="$router.push('/operator/dashboard')">运营商管理</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
            <el-button @click="handleLogout">退出登录</el-button>
          </template>
          <template v-else>
            <el-button @click="$router.push('/login')">登录</el-button>
            <el-button type="primary" @click="$router.push('/register')">注册</el-button>
          </template>
        </div>
      </div>
    </el-header>
    
    <el-main>
      <!-- 店铺用户显示自己店铺的商品 -->
      <template v-if="isShopUser(user)">
        <div class="page-header">
          <h2>我的商品</h2>
          <el-button type="primary" @click="$router.push('/shop/products')">管理商品</el-button>
        </div>
        <el-row :gutter="20" v-loading="loading">
          <el-col :xs="12" :sm="8" :md="6" :lg="6" v-for="product in products" :key="product.productId">
            <el-card class="product-card">
              <img :src="getProductImage(product.imageUrl)" class="product-image" />
              <div class="product-info">
                <h3>{{ product.name }}</h3>
                <p class="price">¥{{ product.price }}</p>
                <p class="stock">库存: {{ product.stock }}</p>
                <p class="status">
                  <el-tag :type="product.isOnSale ? 'success' : 'danger'">
                    {{ product.isOnSale ? '上架' : '下架' }}
                  </el-tag>
                </p>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </template>
      
      <!-- 普通用户显示首页分类 + 轮播 + 商品列表 -->
      <template v-else>
        <div class="search-bar">
          <el-input
            v-model="keyword"
            placeholder="搜索商品"
            @keyup.enter="searchProducts"
            clearable
          >
            <template #append>
              <el-button @click="searchProducts" :icon="Search">搜索</el-button>
            </template>
          </el-input>
        </div>

        <div class="home-hero">
          <div class="category-left">
            <div class="category-title">全部分类</div>
            <ul>
              <li
                :class="{ active: leftSelectedCategoryId === null }"
                @click="selectCategoryFromLeft(null)"
              >
                全部
              </li>
              <li
                v-for="cat in categoryList"
                :key="cat.categoryId || cat.name"
                :class="{ active: leftSelectedCategoryId === cat.categoryId }"
                @click="selectCategoryFromLeft(cat.categoryId)"
              >
                {{ cat.name }}
              </li>
            </ul>
          </div>

          <div class="hero-center">
            <el-carousel height="320px" indicator-position="outside" trigger="click">
              <el-carousel-item v-for="(banner, index) in carouselBanners" :key="index">
                <div
                  class="banner"
                  :style="{ backgroundImage: 'url(' + banner.image + ')' }"
                  @click="selectCategory(banner.categoryId)"
                >
                  <div class="banner-text">
                    <h3>{{ banner.title }}</h3>
                    <p>{{ banner.desc }}</p>
                  </div>
                </div>
              </el-carousel-item>
            </el-carousel>
          </div>

          <div class="category-right">
            <div
              class="right-card"
              v-for="card in rightCards"
              :key="card.categoryId"
              @click="selectCategory(card.categoryId)"
            >
              <img :src="card.image" alt="" />
              <div class="card-text">
                <h4>{{ card.title }}</h4>
                <p>{{ card.desc }}</p>
              </div>
            </div>
          </div>
        </div>
        
        <el-row :gutter="20" v-loading="loading">
          <el-col :xs="12" :sm="8" :md="6" :lg="6" v-for="product in products" :key="product.productId">
            <el-card 
              class="product-card" 
              @click="goToProduct(product.productId)"
              style="cursor: pointer;"
            >
              <img :src="getProductImage(product.imageUrl)" class="product-image" />
              <div class="product-info">
                <h3>{{ product.name }}</h3>
                <p class="price">¥{{ product.price }}</p>
                <p class="stock">库存: {{ product.stock }}</p>
              </div>
            </el-card>
          </el-col>
        </el-row>

        <el-empty
          v-if="!loading && products.length === 0"
          :description="keyword ? '未找到商品，请搜索其他关键词' : (selectedCategoryId ? '该分类暂无商品' : '暂无商品')"
          class="empty-tip"
        />
      </template>
    </el-main>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Search, ShoppingCart } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getProducts, getProductsByShopId, getCategories } from '../../api/product'
import { logout } from '../../api/auth'
import { getProductImage } from '../../utils/helpers'
import { isShopUser } from '../../utils/userHelpers'
import { USER_TYPE } from '../../utils/constants'
import store from '../../store'

const router = useRouter()
const keyword = ref('')
const products = ref([])
const loading = ref(false)
const categories = ref([])
const carouselBanners = ref([])
const rightCards = ref([])
const selectedCategoryId = ref(null)
const leftSelectedCategoryId = ref(null)

const user = computed(() => store.user)

const categoryList = computed(() => {
  if (categories.value.length > 0) {
    return categories.value
  }
  const result = []
  for (const c of defaultCategoryVisuals) {
    result.push({ categoryId: c.categoryId, name: c.name })
  }
  return result
})

const defaultCategoryVisuals = [
  { categoryId: 1, name: '女装', image: 'https://picsum.photos/750/500?random=1', title: '女装上新', desc: '秋冬穿搭精选' },
  { categoryId: 2, name: '男装', image: 'https://picsum.photos/750/500?random=2', title: '男装优选', desc: '商务休闲必备' },
  { categoryId: 3, name: '鞋靴', image: 'https://picsum.photos/750/500?random=3', title: '鞋靴好物', desc: '舒适保暖' },
  { categoryId: 4, name: '美妆', image: 'https://picsum.photos/750/500?random=4', title: '美妆护肤', desc: '焕亮新肌' },
  { categoryId: 5, name: '数码', image: 'https://picsum.photos/390/260?random=5', title: '数码潮品', desc: '爆款直降' },
  { categoryId: 6, name: '家电', image: 'https://picsum.photos/390/260?random=6', title: '家电精选', desc: '生活升级' },
  { categoryId: 7, name: '家居', image: 'https://picsum.photos/390/260?random=7', title: '家居好物', desc: '舒适居家' },
  { categoryId: 8, name: '食品', image: 'https://picsum.photos/390/260?random=8', title: '美味零食', desc: '囤货必备' },
  { categoryId: 8, name: '食品', image: 'https://picsum.photos/390/260?random=8', title: '美味零食', desc: '囤货必备' }
]

const buildVisuals = () => {
  const list = categoryList.value
  carouselBanners.value = []
  for (let i = 0; i < 4 && i < list.length; i++) {
    const cat = list[i]
    const visual = defaultCategoryVisuals[i]
    carouselBanners.value.push({
      categoryId: cat.categoryId,
      title: visual ? visual.title : cat.name,
      desc: visual ? visual.desc : '',
      image: visual ? visual.image : ''
    })
  }
  rightCards.value = []
  for (let i = 4; i < 8 && i < list.length; i++) {
    const cat = list[i]
    const visual = defaultCategoryVisuals[i]
    rightCards.value.push({
      categoryId: cat.categoryId,
      title: visual ? visual.title : cat.name,
      desc: visual ? visual.desc : '',
      image: visual ? visual.image : ''
    })
  }
}

// 搜索商品：店铺用户显示自己店铺的商品，普通用户显示所有在售商品
const searchProducts = async () => {
  loading.value = true
  try {
    if (isShopUser(store.user)) {
      // 店铺用户：显示自己店铺的所有商品
      const { getShopInfo } = await import('../../api/shop')
      const shopInfoResponse = await getShopInfo()
      if (shopInfoResponse.success && shopInfoResponse.data) {
        const response = await getProductsByShopId(shopInfoResponse.data.shopId)
        if (response.success) {
          products.value = response.data || []
        }
      }
    } else {
      // 普通用户：根据关键词和分类筛选在售商品
      const response = await getProducts({
        keyword: keyword.value || undefined,
        categoryId: selectedCategoryId.value || undefined
      })
      if (response.success) {
        products.value = response.data || []
      }
    }
  } catch (error) {
    ElMessage.error('加载商品失败')
  } finally {
    loading.value = false
  }
}

const selectCategoryFromLeft = (categoryId) => {
  const id = categoryId ? Number(categoryId) : null
  leftSelectedCategoryId.value = id
  selectedCategoryId.value = id
  searchProducts()
}

const selectCategory = (categoryId) => {
  selectedCategoryId.value = categoryId ? Number(categoryId) : null
  searchProducts()
}

const goToProduct = (id) => {
  if (!id) return
  if (isShopUser(store.user)) {
    router.push('/shop/products')
  } else {
    router.push(`/product/${id}`).catch(() => {})
  }
}

const goToOrders = () => {
  if (!store.user) {
    ElMessage.warning('请先登录后查看订单')
    router.push('/login')
    return
  }
  // 店铺用户跳转到店铺订单管理
  if (isShopUser(store.user)) {
    router.push('/shop/orders')
  } else {
    router.push('/orders')
  }
}

const goToProfile = () => {
  if (!store.user) {
    ElMessage.warning('请先登录')
    router.push('/login')
  } else {
    router.push('/profile')
  }
}

const handleLogout = async () => {
  try {
    await logout()
  } catch (error) {
    // 忽略错误
  }
  store.clearUser()
  router.push('/').then(() => {
    searchProducts()
  })
}

const loadCategories = async () => {
  try {
    const response = await getCategories()
    if (response && response.success && Array.isArray(response.data)) {
      categories.value = response.data
    } else {
      categories.value = []
    }
  } catch (error) {
    categories.value = []
  } finally {
    buildVisuals()
  }
}

onMounted(async () => {
  await store.init()
  await loadCategories()
  await searchProducts()
})
</script>

<style scoped>
.home {
  min-height: 100vh;
}

.home-hero {
  display: grid;
  grid-template-columns: 220px 1fr 240px;
  gap: 16px;
  margin-bottom: 20px;
}

.category-left {
  background: #f5f7fa;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 12px;
}

.category-title {
  font-weight: 600;
  margin-bottom: 8px;
}

.category-left ul {
  list-style: none;
  padding: 0;
  margin: 0;
}

.category-left li {
  padding: 8px 10px;
  cursor: pointer;
  border-radius: 4px;
  color: #606266;
}

.category-left li:hover {
  background: #ecf5ff;
  color: #409eff;
}

.category-left li.active {
  background: #409eff;
  color: #fff;
}

.hero-center {
  border-radius: 8px;
  overflow: hidden;
}

.banner {
  height: 320px;
  background-size: cover;
  background-position: center;
  border-radius: 8px;
  position: relative;
  cursor: pointer;
}

.banner-text {
  position: absolute;
  left: 20px;
  bottom: 20px;
  color: #fff;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.4);
}

.banner-text h3 {
  margin: 0 0 4px 0;
  font-size: 20px;
}

.banner-text p {
  margin: 0;
}

.category-right {
  display: grid;
  grid-template-rows: repeat(4, 1fr);
  gap: 10px;
}

.right-card {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
}

.right-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.right-card img {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 6px;
}

.card-text h4 {
  margin: 0;
  font-size: 16px;
}

.card-text p {
  margin: 4px 0 0 0;
  color: #909399;
  font-size: 12px;
}

.header {
  background-color: #409eff;
  color: white;
  padding: 0;
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  height: 100%;
}

.header-content h1 {
  margin: 0;
  font-size: 24px;
}

.header-actions {
  display: flex;
  gap: 10px;
  align-items: center;
}

.user-info {
  cursor: pointer;
  color: white;
}

.el-main {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.search-bar {
  margin-bottom: 20px;
}

.product-card {
  cursor: pointer;
  margin-bottom: 20px;
}

.product-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.product-image {
  width: 100%;
  height: 200px;
  object-fit: cover;
}

.product-info h3 {
  margin: 10px 0;
  font-size: 16px;
}

.price {
  color: #f56c6c;
  font-size: 20px;
  font-weight: bold;
}

.stock {
  color: #909399;
  font-size: 12px;
}

.shop-notice {
  margin-bottom: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  font-size: 20px;
}

.status {
  margin-top: 10px;
}

/* 响应式布局 - 手机端 */
@media (max-width: 768px) {
  .header-content {
    flex-direction: column;
    padding: 10px;
    height: auto;
  }

  .header-content h1 {
    font-size: 18px;
    margin-bottom: 10px;
  }

  .header-actions {
    flex-wrap: wrap;
    gap: 5px;
    width: 100%;
    justify-content: center;
  }

  .header-actions .el-button {
    font-size: 12px;
    padding: 5px 10px;
  }

  .el-main {
    padding: 10px;
  }

  .home-hero {
    display: flex;
    flex-direction: column;
    gap: 15px;
  }

  .category-left {
    width: 100%;
    max-height: 200px;
    overflow-y: auto;
  }

  .category-left ul {
    display: flex;
    flex-wrap: wrap;
    gap: 5px;
  }

  .category-left li {
    padding: 8px 12px;
    font-size: 14px;
  }

  .hero-center {
    width: 100%;
  }

  .banner {
    height: 200px;
  }

  .category-right {
    width: 100%;
    grid-template-columns: repeat(2, 1fr);
    grid-template-rows: auto;
  }

  .right-card {
    flex-direction: column;
    text-align: center;
  }

  .right-card img {
    width: 60px;
    height: 60px;
  }

  .product-image {
    height: 150px;
  }

  .product-info h3 {
    font-size: 14px;
  }

  .price {
    font-size: 18px;
  }

  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }

  .search-bar {
    margin-bottom: 15px;
  }
}

/* 平板端 */
@media (min-width: 769px) and (max-width: 1024px) {
  .home-hero {
    gap: 15px;
  }

  .category-left {
    width: 120px;
  }

  .hero-center {
    flex: 1;
  }

  .category-right {
    width: 200px;
  }
}
</style>

