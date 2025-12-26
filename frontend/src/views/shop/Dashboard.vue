<template>
  <div class="shop-dashboard">
    <el-header class="header">
      <div class="header-content">
        <h1>店铺管理</h1>
        <div>
          <el-button @click="$router.push('/shop/products')">商品管理</el-button>
          <el-button @click="$router.push('/shop/orders')">订单管理</el-button>
          <el-button @click="$router.push('/shop/info')">店铺信息</el-button>
          <el-button @click="() => handleLogout(router)">退出登录</el-button>
        </div>
      </div>
    </el-header>
    
    <el-main>
      <el-row :gutter="20">
        <el-col :span="8">
          <el-card>
            <template #header>商品总数</template>
            <div class="stat-value">{{ productCount }}</div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card>
            <template #header>订单总数</template>
            <div class="stat-value">{{ orderCount }}</div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card>
            <template #header>总销售额</template>
            <div class="stat-value">¥{{ totalSales }}</div>
          </el-card>
        </el-col>
      </el-row>
    </el-main>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getProductsByShopId } from '../../api/product'
import { getShopOrders } from '../../api/order'
import { getShopInfo } from '../../api/shop'
import { handleLogout } from '../../utils/userHelpers'
import store from '../../store'

const router = useRouter()

const productCount = ref(0)
const orderCount = ref(0)
const totalSales = ref(0)

const loadStats = async () => {
  if (!store.user) return
  
  try {
    const shopInfoResponse = await getShopInfo()
    if (!shopInfoResponse.success || !shopInfoResponse.data) return
    
    const [productsResponse, ordersResponse] = await Promise.all([
      getProductsByShopId(shopInfoResponse.data.shopId),
      getShopOrders()
    ])
    
    if (productsResponse.success && productsResponse.data) {
      productCount.value = productsResponse.data.length
    }
    
    if (ordersResponse.success && ordersResponse.data) {
      const orders = ordersResponse.data
      orderCount.value = orders.length
      
      // 计算总销售额（只统计已完成的订单）
      let sales = 0
      for (const order of orders) {
        if (order.status === '已完成') {
          const amount = parseFloat(order.totalAmount)
          if (!isNaN(amount)) {
            sales = sales + amount
          }
        }
      }
      totalSales.value = sales.toFixed(2)
    }
  } catch (error) {
    // 静默失败
  }
}


onMounted(async () => {
  await store.init()
  await loadStats()
})
</script>

<style scoped>
.shop-dashboard {
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

.stat-value {
  font-size: 32px;
  font-weight: bold;
  color: #409eff;
  text-align: center;
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

  .header-content > div {
    display: flex;
    flex-wrap: wrap;
    gap: 5px;
    width: 100%;
    justify-content: center;
  }

  .header-content .el-button {
    font-size: 12px;
    padding: 5px 10px;
  }

  .el-main {
    padding: 10px;
  }

  .stat-value {
    font-size: 24px;
  }

  /* 统计卡片在手机端单列显示 */
  :deep(.el-col) {
    margin-bottom: 15px;
  }
}
</style>

