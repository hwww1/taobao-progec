<template>
  <div class="product-detail">
    <el-header class="header">
      <div class="header-content">
        <h1>商品详情</h1>
        <div class="header-actions">
          <el-button @click="$router.push('/')">返回首页</el-button>
          <template v-if="store.user">
            <el-button @click="() => handleLogout(router)">退出登录</el-button>
          </template>
          <template v-else>
            <el-button @click="$router.push('/login')">登录</el-button>
          </template>
        </div>
      </div>
    </el-header>
    
    <el-main v-loading="loading">
      <el-empty v-if="!loading && !product" description="商品不存在或加载失败">
        <el-button type="primary" @click="$router.push('/')">返回首页</el-button>
      </el-empty>
      
      <template v-if="product && !loading">
        <div class="product-content">
          <div class="product-image-section">
            <el-image
              :src="getProductImage(product.imageUrl)"
              :preview-src-list="[getProductImage(product.imageUrl)]"
              fit="cover"
              class="product-image"
            />
          </div>
          <div class="product-info-section">
            <h2>{{ product.name }}</h2>
            <p class="price">¥{{ product.price }}</p>
            <p class="description">{{ product.description }}</p>
            <p class="stock">库存: {{ product.stock }}</p>
            <div class="quantity-selector" v-if="product.stock > 0">
              <span>数量:</span>
              <el-input-number v-model="quantity" :min="1" :max="product.stock" />
            </div>
            <div v-else class="out-of-stock-notice">
              <el-alert type="warning" :closable="false">该商品暂时缺货</el-alert>
            </div>
            <div class="action-buttons">
              <el-button type="primary" size="large" @click="addToCart" :disabled="product.stock === 0">
                加入购物车
              </el-button>
              <el-button type="danger" size="large" @click="buyNow" :disabled="product.stock === 0">
                立刻购买
              </el-button>
            </div>
          </div>
        </div>
        
        <!-- 评价区域 -->
        <el-card style="margin-top: 40px;">
        <template #header>
          <div class="review-header">
            <span>商品评价</span>
            <div>
              <span style="color: #f56c6c; font-size: 20px; font-weight: bold;">
                {{ reviewStats.averageRating ? reviewStats.averageRating.toFixed(1) : '0.0' }}
              </span>
              <span style="color: #909399; margin-left: 5px;">
                ({{ reviewStats.reviewCount }}条评价)
              </span>
            </div>
          </div>
        </template>
        
        <!-- 评价表单 -->
        <div v-if="isCustomer(store.user)" class="review-form">
          <el-form :model="reviewForm" label-width="80px">
            <el-form-item label="评分">
              <el-rate v-model="reviewForm.rating" />
            </el-form-item>
            <el-form-item label="评价内容">
              <el-input v-model="reviewForm.content" type="textarea" :rows="4" placeholder="请输入您的评价..." />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="submitReview">提交评价</el-button>
            </el-form-item>
          </el-form>
        </div>
        
        <!-- 评价列表 -->
        <div class="review-list">
          <div v-for="review in reviews" :key="review.reviewId" class="review-item">
            <div class="review-user">
              <span class="username">{{ review.customerName || '匿名用户' }}</span>
              <el-rate :model-value="review.rating" disabled show-score />
              <span class="review-date">{{ formatDate(review.reviewDate) }}</span>
              <div v-if="store.user && review.customerId === store.user.userId" class="review-actions">
                <el-button type="primary" link size="small" @click="openEditReview(review)">编辑</el-button>
                <el-popconfirm title="确定删除这条评价吗？" @confirm="removeReview(review.reviewId)">
                  <template #reference>
                    <el-button type="danger" link size="small">删除</el-button>
                  </template>
                </el-popconfirm>
              </div>
            </div>
            <div class="review-content">{{ review.content }}</div>
          </div>
          <el-empty v-if="reviews.length === 0" description="暂无评价" />
        </div>
      </el-card>
      </template>

      <!-- 地址选择对话框 -->
      <el-dialog title="选择收货地址" v-model="addressDialogVisible" width="700px">
        <el-empty v-if="addresses.length === 0" description="暂无收货地址，请先添加">
          <el-button type="primary" @click="goToAddresses">去添加地址</el-button>
        </el-empty>
        <el-radio-group v-model="selectedAddressId" v-else>
          <el-card v-for="address in addresses" :key="address.addressId" class="address-option" 
                   :class="{ 'selected': selectedAddressId === address.addressId }">
            <el-radio :label="address.addressId" class="address-radio">
              <div class="address-content">
                <div class="address-header">
                  <span class="receiver-name">{{ address.receiverName }}</span>
                  <span class="receiver-phone">{{ address.receiverPhone }}</span>
                </div>
                <div class="address-detail">
                  {{ address.province }} {{ address.city }} {{ address.district }} {{ address.detailAddress }}
                </div>
              </div>
            </el-radio>
          </el-card>
        </el-radio-group>
        <template #footer>
          <el-button @click="addressDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="goToAddresses">管理地址</el-button>
          <el-button type="primary" @click="confirmBuyNow" :disabled="!selectedAddressId">确认下单</el-button>
        </template>
      </el-dialog>

      <!-- 编辑评价弹窗 -->
      <el-dialog v-model="editDialogVisible" title="编辑评价" width="500px">
        <el-form :model="editForm" label-width="80px">
          <el-form-item label="评分">
            <el-rate v-model="editForm.rating" />
          </el-form-item>
          <el-form-item label="评价内容">
            <el-input v-model="editForm.content" type="textarea" :rows="4" placeholder="请输入您的评价..." />
          </el-form-item>
        </el-form>
        <template #footer>
          <span class="dialog-footer">
            <el-button @click="editDialogVisible = false">取消</el-button>
            <el-button type="primary" @click="submitEditReview">保存</el-button>
          </span>
        </template>
      </el-dialog>
    </el-main>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getProductById } from '../../api/product'
import { getReviewsByProductId, getProductReviewStats, addReview } from '../../api/review'
import { createOrder } from '../../api/order'
import { getAddresses } from '../../api/address'
import { getProductImage, formatDate } from '../../utils/helpers'
import { isCustomer, handleLogout } from '../../utils/userHelpers'
import { useReviewEditor } from '../../utils/reviewHelpers'
import store from '../../store'

const route = useRoute()
const router = useRouter()
const product = ref(null)
const quantity = ref(1)
const loading = ref(false)
const reviews = ref([])
const reviewStats = ref({ averageRating: 0, reviewCount: 0 })

const reviewForm = ref({
  rating: 5,
  content: ''
})

const addressDialogVisible = ref(false)
const addresses = ref([])
const selectedAddressId = ref(null)

// 加载商品详情、评价数据
const loadProduct = async () => {
  const productId = route.params.id
  if (!productId) {
    router.push('/')
    return
  }
  
  loading.value = true
  try {
    const productResponse = await getProductById(productId)
    if (!productResponse.success || !productResponse.data) {
      ElMessage.error(productResponse.message || '商品不存在')
      router.push('/')
      return
    }
    
    product.value = productResponse.data
    
    // 并行加载评价列表和评价统计
    try {
      const [reviewsResponse, statsResponse] = await Promise.all([
        getReviewsByProductId(productId),
        getProductReviewStats(productId)
      ])
      if (reviewsResponse.success && reviewsResponse.data) {
        reviews.value = reviewsResponse.data
      }
      if (statsResponse.success && statsResponse.data) {
        reviewStats.value = statsResponse.data
      }
    } catch (error) {
      // 静默失败
    }
    
  } catch (error) {
    ElMessage.error('加载商品失败')
    router.push('/')
  } finally {
    loading.value = false
  }
}

const submitReview = async () => {
  try {
    const response = await addReview({
      productId: product.value.productId,
      rating: reviewForm.value.rating,
      content: reviewForm.value.content
    })
    
    if (response.success) {
      ElMessage.success('评价成功')
      reviewForm.value = { rating: 5, content: '' }
      await loadProduct()
    } else {
      ElMessage.error(response.message || '评价失败')
    }
  } catch (error) {
    ElMessage.error('评价失败')
  }
}

const addToCart = () => {
  if (!store.user) {
    ElMessage.warning('请先登录后再加入购物车')
    router.push('/login')
    return
  }
  store.addToCart(product.value.productId, quantity.value)
  ElMessage.success('已加入购物车')
}

const loadAddresses = async () => {
  try {
    const response = await getAddresses()
    if (response.success && response.data) {
      addresses.value = response.data
      if (addresses.value.length > 0) {
        selectedAddressId.value = addresses.value[0].addressId
      }
    }
  } catch (error) {
    // 失败不处理
  }
}

// 立即购买：加载地址后弹出选择对话框
const buyNow = async () => {
  if (!store.user) {
    ElMessage.warning('请先登录后再购买')
    router.push('/login')
    return
  }
  
  await loadAddresses()
  if (addresses.value.length === 0) {
    ElMessage.warning('请先添加收货地址')
    router.push('/profile')
    return
  }
  
  // 默认选择第一个地址
  if (addresses.value.length > 0) {
    selectedAddressId.value = addresses.value[0].addressId
  }
  addressDialogVisible.value = true
}

const goToAddresses = () => {
  addressDialogVisible.value = false
  router.push('/profile')
}

// 确认购买：发送订单数据给后端处理
const confirmBuyNow = async () => {
  if (!selectedAddressId.value) {
    ElMessage.warning('请选择收货地址')
    return
  }

  try {
    const response = await createOrder({
      cartItems: {
        [product.value.productId]: quantity.value
      },
      addressId: selectedAddressId.value
    })
    
    if (response.success) {
      ElMessage.success('订单创建成功')
      addressDialogVisible.value = false
      router.push('/orders')
    } else {
      ElMessage.error(response.message || '订单创建失败')
    }
  } catch (error) {
    ElMessage.error('订单创建失败')
  }
}

const { editDialogVisible, editForm, openEditReview, submitEditReview, removeReview } = useReviewEditor(() => loadProduct())

onMounted(() => {
  loadProduct()
})
</script>

<style scoped>
.product-detail {
  min-height: 100vh;
}

.header {
  background-color: #409eff;
  color: white;
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

.header-actions {
  display: flex;
  gap: 10px;
  align-items: center;
}

.el-main {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.product-content {
  display: flex;
  gap: 40px;
}

.product-image-section {
  flex: 1;
}

.product-image {
  width: 100%;
  max-width: 500px;
}

.product-info-section {
  flex: 1;
}

.product-info-section h2 {
  font-size: 24px;
  margin-bottom: 20px;
}

.price {
  color: #f56c6c;
  font-size: 32px;
  font-weight: bold;
  margin-bottom: 20px;
}

.description {
  color: #606266;
  line-height: 1.8;
  margin-bottom: 20px;
}

.stock {
  color: #909399;
  margin-bottom: 20px;
}

.quantity-selector {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 20px;
}

.action-buttons {
  display: flex;
  gap: 15px;
}

.review-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.review-form {
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 1px solid #ebeef5;
}

.review-list {
  margin-top: 20px;
}

.review-item {
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid #f0f0f0;
}

.review-user {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 10px;
}

.username {
  font-weight: bold;
  color: #409eff;
}

.review-date {
  color: #909399;
  font-size: 12px;
  margin-left: auto;
}

.review-content {
  color: #606266;
  line-height: 1.6;
}

.address-option {
  margin-bottom: 15px;
  cursor: pointer;
  transition: all 0.3s;
}

.address-option.selected {
  border-color: #409eff;
  background-color: #ecf5ff;
}

.address-radio {
  width: 100%;
}

.address-content {
  width: 100%;
}

.address-header {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 8px;
}

.receiver-name {
  font-weight: bold;
  font-size: 16px;
}

.receiver-phone {
  color: #666;
}

.address-detail {
  color: #333;
  line-height: 1.6;
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

  .el-main {
    padding: 10px;
  }

  .product-content {
    flex-direction: column;
    gap: 20px;
  }

  .product-image-section {
    width: 100%;
  }

  .product-image {
    max-width: 100%;
  }

  .product-info-section h2 {
    font-size: 20px;
  }

  .price {
    font-size: 24px;
  }

  .action-buttons {
    flex-direction: column;
  }

  .action-buttons .el-button {
    width: 100%;
  }
}
</style>

