<template>
  <div class="operator-dashboard">
    <el-header class="header">
      <div class="header-content">
        <h1>运营商管理后台</h1>
        <div>
          <el-button @click="$router.push('/operator/management')">用户与店铺管理</el-button>
          <el-button @click="() => handleLogout(router)">退出登录</el-button>
        </div>
      </div>
    </el-header>
    
    <el-main>
      <el-row :gutter="20">
        <el-col :span="12">
          <el-card>
            <template #header>总用户数</template>
            <div class="stat-value">{{ stats.totalUsers }}</div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card>
            <template #header>总店铺数</template>
            <div class="stat-value">{{ stats.totalShops }}</div>
          </el-card>
        </el-col>
      </el-row>
      
      <el-row :gutter="20" style="margin-top: 20px;">
        <el-col :span="12">
          <el-card>
            <template #header>热销商品 TOP 10</template>
            <el-table :data="topProductsList" size="small">
              <el-table-column prop="name" label="商品名称" />
              <el-table-column prop="sales" label="销量" />
            </el-table>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card>
            <template #header>热门店铺 TOP 10</template>
            <el-table :data="topShopsList" size="small">
              <el-table-column prop="name" label="店铺名称" />
              <el-table-column prop="orders" label="订单数" />
            </el-table>
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
import { getDashboard } from '../../api/operator'
import { handleLogout } from '../../utils/userHelpers'

const router = useRouter()

const stats = ref({
  totalUsers: 0,
  totalShops: 0
})

const topProductsList = ref([])
const topShopsList = ref([])

const loadDashboard = async () => {
  try {
    const response = await getDashboard()
    const data = response.data || {}
    
    stats.value = {
      totalUsers: data.totalUsers || 0,
      totalShops: data.totalShops || 0
    }
    
    // 处理热销商品数据
    topProductsList.value = []
    if (data.topProducts) {
      for (const name in data.topProducts) {
        topProductsList.value.push({
          name: name,
          sales: data.topProducts[name]
        })
      }
    }
    
    // 处理热门店铺数据
    topShopsList.value = []
    if (data.topShops) {
      for (const name in data.topShops) {
        topShopsList.value.push({
          name: name,
          orders: data.topShops[name]
        })
      }
    }
  } catch (error) {
    ElMessage.error('加载数据失败')
  }
}

onMounted(async () => {
  await loadDashboard()
})
</script>

<style scoped>
.operator-dashboard {
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
</style>

