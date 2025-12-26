<template>
  <div class="order-detail">
    <el-header class="header">
      <div class="header-content">
        <h1>订单详情</h1>
        <el-button @click="$router.push('/orders')">返回订单列表</el-button>
      </div>
    </el-header>
    
    <el-main v-loading="loading">
      <el-card v-if="order">
        <template #header>
          <div class="order-header">
            <span>订单号: {{ order.orderId }}</span>
            <el-tag :type="getOrderStatusType(order.status)">{{ order.status }}</el-tag>
          </div>
        </template>
        <div class="order-info">
          <p>总金额: <span class="price">¥{{ order.totalAmount }}</span></p>
          <p>下单时间: {{ order.orderDate }}</p>
          <div class="order-actions" style="margin-top: 15px;">
            <el-button 
              v-if="order.status === '待支付'" 
              type="primary" 
              @click="handlePayment"
            >
              立即支付
            </el-button>
            <el-button 
              v-if="order.status === '待支付' || order.status === '待发货'" 
              type="danger" 
              @click="handleCancel"
            >
              取消订单
            </el-button>
            <el-button 
              v-if="order.status === '待收货'" 
              type="success" 
              @click="handleReceipt"
            >
              确认收货
            </el-button>
          </div>
        </div>
        <el-table :data="orderItems" style="margin-top: 20px" v-loading="itemsLoading">
          <el-table-column prop="productId" label="商品ID" width="100" />
          <el-table-column prop="productName" label="商品名称" />
          <el-table-column prop="quantity" label="数量" width="100" />
          <el-table-column prop="priceAtPurchase" label="单价" width="120">
            <template #default="{ row }">
              ¥{{ row.priceAtPurchase }}
            </template>
          </el-table-column>
          <el-table-column label="小计" width="120">
            <template #default="{ row }">
              ¥{{ (row.priceAtPurchase * row.quantity).toFixed(2) }}
            </template>
          </el-table-column>
        </el-table>
      </el-card>

      <!-- 简化：只提供“去评价”按钮，跳转到商品详情页评价 -->
      <el-card v-if="order && isCustomer(store.user)" style="margin-top: 20px">
        <template #header>
          <h3>商品评价</h3>
        </template>
        <div class="review-simple">
          <p>在商品详情页完成评价，支持多次评价。</p>
          <el-button type="primary" @click="goToProductForReview">去评价</el-button>
        </div>
      </el-card>
      
    </el-main>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getOrderById, processPayment, confirmReceipt, cancelOrder } from '../../api/order'
import { getProductById } from '../../api/product'
import { isCustomer } from '../../utils/userHelpers'
import { getOrderStatusType } from '../../utils/orderHelpers'
import store from '../../store'

const route = useRoute()
const router = useRouter()
const order = ref(null)
const orderItems = ref([])
const productAvailable = ref(true)
const loading = ref(false)
const itemsLoading = ref(false)

const loadOrder = async () => {
  loading.value = true
  try {
    const orderResponse = await getOrderById(route.params.id)
    if (orderResponse.success) {
      order.value = orderResponse.data
    }
    
    await loadOrderItems()
  } catch (error) {
    ElMessage.error('加载订单详情失败')
  } finally {
    loading.value = false
  }
}

// 加载订单商品信息，包括商品名称和评价状态
const loadOrderItems = async () => {
  itemsLoading.value = true
  try {
    if (!order.value || !order.value.productId) {
      orderItems.value = [{
        productId: 0,
        productName: '商品信息已丢失',
        quantity: order.value?.quantity || 0,
        priceAtPurchase: order.value?.priceAtPurchase || 0,
        review: null
      }]
      return
    }
    
    const item = {
      productId: order.value.productId,
      quantity: order.value.quantity,
      priceAtPurchase: order.value.priceAtPurchase
    }
    
    // 尝试获取商品名称（商品可能已下架）
    let productName = '商品信息已丢失'
    try {
      const productResponse = await getProductById(item.productId)
      if (productResponse.success && productResponse.data) {
        productName = productResponse.data.name
        productAvailable.value = productResponse.data.isOnSale !== false
      } else {
        productAvailable.value = false
      }
    } catch (error) {
      // 静默失败
      productAvailable.value = false
    }
    
    orderItems.value = [{
      ...item,
      productName,
      review: null
    }]
  } catch (error) {
    ElMessage.error('加载订单商品失败')
  } finally {
    itemsLoading.value = false
  }
}

const goToProductForReview = () => {
  if (!productAvailable.value) {
    ElMessage.warning('商品已下架或店铺已删除，无法评价')
    return
  }
  if (order.value && order.value.productId) {
    router.push(`/product/${order.value.productId}`)
  }
}

const handlePayment = async () => {
  try {
    const response = await processPayment(order.value.orderId)
    if (response.success) {
      ElMessage.success('支付成功')
      await loadOrder()
    } else {
      ElMessage.error(response.message || '支付失败')
    }
  } catch (error) {
    ElMessage.error('支付失败')
  }
}

const handleReceipt = async () => {
  try {
    const response = await confirmReceipt(order.value.orderId)
    if (response.success) {
      ElMessage.success('确认收货成功')
      await loadOrder()
    } else {
      ElMessage.error(response.message || '确认收货失败')
    }
  } catch (error) {
    ElMessage.error('确认收货失败')
  }
}

const handleCancel = async () => {
  try {
    const response = await cancelOrder(order.value.orderId)
    if (response.success) {
      ElMessage.success('订单取消成功')
      await loadOrder()
    } else {
      ElMessage.error(response.message || '订单取消失败')
    }
  } catch (error) {
    ElMessage.error('订单取消失败')
  }
}

onMounted(async () => {
  await store.init()
  await loadOrder()
})
</script>

<style scoped>
.order-detail {
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

.el-main {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.order-info {
  margin-bottom: 20px;
}

.price {
  color: #f56c6c;
  font-size: 20px;
  font-weight: bold;
}

.review-item-card {
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 1px solid #ebeef5;
}

.review-item-card:last-child {
  border-bottom: none;
  margin-bottom: 0;
  padding-bottom: 0;
}

.product-review-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.product-name {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}

.review-display {
  padding: 15px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.review-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.review-actions {
  display: flex;
  gap: 10px;
}

.review-content {
  margin: 10px 0;
  color: #606266;
  line-height: 1.6;
}

.review-date {
  color: #909399;
  font-size: 12px;
}

.review-form {
  padding: 15px 0;
}
</style>

