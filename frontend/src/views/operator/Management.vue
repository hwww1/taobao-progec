<template>
  <div class="operator-management">
    <el-header class="header">
      <div class="header-content">
        <h1>用户与店铺管理</h1>
        <el-button @click="$router.push('/operator/dashboard')">返回</el-button>
      </div>
    </el-header>
    
    <el-main>
      <el-tabs v-model="activeTab">
        <el-tab-pane label="店铺管理" name="shops">
          <el-table :data="shopsWithUsers" v-loading="loading" style="width: 100%">
            <el-table-column prop="shopName" label="店铺名称" />
            <el-table-column prop="username" label="店铺用户名" />
            <el-table-column label="操作" width="120">
              <template #default="{ row }">
                <el-button type="danger" size="small" @click="handleDeleteShop(row.shopId)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
        
        <el-tab-pane label="顾客用户" name="customers">
          <el-table :data="customerUsers" v-loading="loading" style="width: 100%">
            <el-table-column prop="username" label="用户名" />
            <el-table-column label="操作" width="120">
              <template #default="{ row }">
                <el-button type="danger" size="small" @click="handleDeleteUser(row.userId)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-main>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAllShops, getAllUsers, deleteShop, deleteUser } from '../../api/operator'

const activeTab = ref('shops')
const shops = ref([])
const shopUsers = ref([])
const customerUsers = ref([])
const loading = ref(false)

// 合并店铺和店铺用户数据
const shopsWithUsers = computed(() => {
  const result = []
  for (const shop of shops.value) {
    // 查找对应的店铺用户
    let shopUser = null
    for (const user of shopUsers.value) {
      if (user.userId === shop.userId) {
        shopUser = user
        break
      }
    }
    result.push({
      ...shop,
      username: shopUser ? shopUser.username : '未知用户'
    })
  }
  return result
})

const loadData = async () => {
  loading.value = true
  try {
    const shopsResponse = await getAllShops()
    if (shopsResponse.success && shopsResponse.data) {
      shops.value = shopsResponse.data
    }
    
    const usersResponse = await getAllUsers()
    if (usersResponse.success && usersResponse.data) {
      shopUsers.value = usersResponse.data.shops || []
      customerUsers.value = usersResponse.data.customers || []
    }
  } catch (error) {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

const handleDeleteShop = async (shopId) => {
  try {
    await ElMessageBox.confirm('确定要删除该店铺吗？删除后该店铺的所有商品将不会出现在购买页面。', '删除确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const { value: password } = await ElMessageBox.prompt('请输入管理员密码以确认删除操作', '二次确认', {
      confirmButtonText: '确认删除',
      cancelButtonText: '取消',
      type: 'warning',
      inputType: 'password',
      inputPlaceholder: '请输入管理员密码'
    })
    
    const response = await deleteShop(shopId, password)
    if (response.success) {
      ElMessage.success('删除成功')
      await loadData()
    } else {
      ElMessage.error(response.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
      ElMessage.error('删除失败')
    }
  }
}

const handleDeleteUser = async (userId) => {
  try {
    await ElMessageBox.confirm('确定要删除该用户吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const response = await deleteUser(userId)
    if (response.success) {
      ElMessage.success('删除成功')
      await loadData()
    } else {
      ElMessage.error(response.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.operator-management {
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
</style>

