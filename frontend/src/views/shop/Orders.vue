<template>
  <div class="shop-orders">
    <el-header class="header">
      <div class="header-content">
        <h1>订单管理</h1>
        <el-button @click="$router.push('/shop/dashboard')">返回</el-button>
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
        <el-table-column prop="status" label="状态">
          <template #default="{ row }">
            <el-tag :type="getOrderStatusType(row.status)">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="orderDate" label="下单时间" />
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button size="small" @click="$router.push(`/order/${row.orderId}`)">查看详情</el-button>
            <el-button 
              v-if="row.status === '待发货'" 
              type="primary" 
              size="small" 
              @click="handleShipment(row.orderId)"
            >
              发货
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
import { getShopOrders, processShipment } from '../../api/order'
import { getOrderStatusType } from '../../utils/orderHelpers'

const orders = ref([])
const loading = ref(false)

const loadOrders = async () => {
  loading.value = true
  try {
    const response = await getShopOrders()
    orders.value = (response.success && response.data) ? response.data : []
  } catch (error) {
    ElMessage.error('加载订单失败')
    orders.value = []
  } finally {
    loading.value = false
  }
}

const handleShipment = async (orderId) => {
  try {
    const response = await processShipment(orderId)
    if (response.success) {
      ElMessage.success('发货成功')
      loadOrders()
    } else {
      ElMessage.error(response.message || '发货失败')
    }
  } catch (error) {
    ElMessage.error('发货失败')
  }
}

onMounted(() => {
  loadOrders()
})
</script>

<style scoped>
.shop-orders {
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

  .header-content .el-button {
    font-size: 12px;
    padding: 5px 10px;
    width: 100%;
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

