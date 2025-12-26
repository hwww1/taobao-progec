<template>
  <div class="orders-container">
    <el-header class="header">
      <div class="header-content">
        <h1>我的订单</h1>
        <div class="header-actions">
          <el-button @click="$router.push('/')">首页</el-button>
          <el-button @click="$router.push('/profile')">个人信息</el-button>
          <el-button @click="$router.push('/cart')">购物车</el-button>
        </div>
      </div>
    </el-header>
    
    <el-main>
      <el-table :data="orders" v-loading="loading">
        <el-table-column prop="orderId" label="订单号" width="120" />
        <el-table-column prop="totalAmount" label="总金额">
          <template #default="{ row }">
            ¥{{ row.totalAmount }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" />
        <el-table-column prop="orderDate" label="下单时间" />
        <el-table-column label="操作">
          <template #default="{ row }">
            <el-button size="small" @click="$router.push(`/order/${row.orderId}`)">查看详情</el-button>
            <el-button 
              v-if="row.status === '待支付'" 
              type="primary" 
              size="small" 
              @click="handlePayment(row.orderId)"
            >
              支付
            </el-button>
            <el-button 
              v-if="row.status === '待支付' || row.status === '待发货'" 
              type="danger" 
              size="small" 
              @click="handleCancel(row.orderId)"
            >
              取消订单
            </el-button>
            <el-button 
              v-if="row.status === '待收货'" 
              type="success" 
              size="small" 
              @click="handleReceipt(row.orderId)"
            >
              确认收货
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-main>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getCustomerOrders, processPayment, confirmReceipt, cancelOrder } from '../../api/order'

const orders = ref([])
const loading = ref(false)

// 加载当前用户的所有订单
const loadOrders = async () => {
  loading.value = true
  try {
    const response = await getCustomerOrders()
    orders.value = (response.success && response.data) ? response.data : []
  } catch (error) {
    ElMessage.error('加载订单失败')
    orders.value = []
  } finally {
    loading.value = false
  }
}

// 处理订单支付
const handlePayment = async (orderId) => {
  try {
    const response = await processPayment(orderId)
    if (response.success) {
      ElMessage.success('支付成功')
      loadOrders()
    } else {
      ElMessage.error(response.message || '支付失败')
    }
  } catch (error) {
    ElMessage.error('支付失败')
  }
}

// 确认收货
const handleReceipt = async (orderId) => {
  try {
    const response = await confirmReceipt(orderId)
    if (response.success) {
      ElMessage.success('确认收货成功')
      loadOrders()
    } else {
      ElMessage.error(response.message || '确认收货失败')
    }
  } catch (error) {
    ElMessage.error('确认收货失败')
  }
}

// 取消订单（仅待支付或待发货状态可取消）
const handleCancel = async (orderId) => {
  try {
    const response = await cancelOrder(orderId)
    if (response.success) {
      ElMessage.success('订单取消成功')
      loadOrders()
    } else {
      ElMessage.error(response.message || '订单取消失败')
    }
  } catch (error) {
    ElMessage.error('订单取消失败')
  }
}

onMounted(() => {
  loadOrders()
})
</script>

<style scoped>
.orders-container {
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
}

.el-main {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
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

  /* 表格在手机端横向滚动 */
  .el-table {
    font-size: 12px;
  }

  .el-table .el-button {
    font-size: 10px;
    padding: 4px 8px;
  }
}
</style>

