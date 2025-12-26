<template>
  <div class="shop-info">
    <el-header class="header">
      <div class="header-content">
        <h1>店铺信息管理</h1>
        <el-button @click="$router.push('/shop/dashboard')">返回</el-button>
      </div>
    </el-header>
    
    <el-main>
      <div class="shop-content">
        <el-card class="shop-card" v-loading="loading">
          <template #header>
            <div class="card-header">
              <span class="card-title">店铺信息</span>
            </div>
          </template>
          
          <el-form :model="formData" label-width="120px" class="shop-form">
            <el-form-item label="店铺名称">
              <div class="form-field">
                <el-input 
                  v-if="!isEditing" 
                  :value="formData.shopName" 
                  readonly 
                  class="readonly-input"
                />
                <el-input 
                  v-else 
                  v-model="formData.shopName" 
                  placeholder="请输入店铺名称"
                  clearable
                />
              </div>
            </el-form-item>
            
            <el-form-item label="密码">
              <div class="form-field">
                <el-input 
                  v-if="!isEditing" 
                  type="password" 
                  value="••••••••" 
                  readonly 
                  class="readonly-input"
                />
                <el-input 
                  v-else 
                  v-model="formData.newPassword" 
                  type="password" 
                  placeholder="留空则不修改密码"
                  show-password
                  clearable
                />
              </div>
              <div v-if="isEditing" class="form-tip">留空则不修改密码</div>
            </el-form-item>
            
            <el-form-item>
              <div class="form-actions">
                <el-button 
                  v-if="!isEditing" 
                  type="primary" 
                  size="large"
                  @click="startEdit"
                >
                  修改信息
                </el-button>
                <template v-else>
                  <el-button 
                    type="primary" 
                    size="large"
                    @click="saveInfo" 
                    :loading="saving"
                  >
                    保存修改
                  </el-button>
                  <el-button 
                    size="large"
                    @click="cancelEdit"
                  >
                    取消
                  </el-button>
                </template>
              </div>
            </el-form-item>
          </el-form>
        </el-card>
      </div>
    </el-main>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getShopInfo, updateShopInfo } from '../../api/shop'
import { changePassword, logout } from '../../api/auth'
import store from '../../store'

const router = useRouter()

const formData = ref({
  shopName: '',
  newPassword: ''
})
const originalData = ref({
  shopName: ''
})
const loading = ref(false)
const saving = ref(false)
const isEditing = ref(false)

const loadShopInfo = async () => {
  loading.value = true
  try {
    const response = await getShopInfo()
    if (response.success) {
      formData.value.shopName = response.data.shopName
      originalData.value.shopName = response.data.shopName
    } else {
      ElMessage.error(response.message || '加载店铺信息失败')
    }
  } catch (error) {
    ElMessage.error('加载店铺信息失败')
  } finally {
    loading.value = false
  }
}

const startEdit = () => {
  isEditing.value = true
  formData.value.newPassword = ''
}

const cancelEdit = () => {
  formData.value.shopName = originalData.value.shopName
  formData.value.newPassword = ''
  isEditing.value = false
}

const saveInfo = async () => {
  saving.value = true
  try {
    if (formData.value.shopName !== originalData.value.shopName) {
      const shopResponse = await updateShopInfo({ shopName: formData.value.shopName })
      if (!shopResponse.success) {
        ElMessage.error(shopResponse.message || '店铺名称修改失败')
        saving.value = false
        return
      }
      originalData.value.shopName = formData.value.shopName
    }
    
    if (formData.value.newPassword && formData.value.newPassword.trim()) {
      const passwordResponse = await changePassword(formData.value.newPassword)
      if (!passwordResponse.success) {
        ElMessage.error(passwordResponse.message || '密码修改失败')
        saving.value = false
        return
      }
      ElMessage.success('修改成功，请重新登录')
      await logout().catch(() => {})
      store.clearUser()
      router.push('/login')
      return
    }
    
    ElMessage.success('修改成功')
    isEditing.value = false
  } catch (error) {
    ElMessage.error('修改失败')
  } finally {
    saving.value = false
  }
}

onMounted(async () => {
  await store.init()
  await loadShopInfo()
})
</script>

<style scoped>
.shop-info {
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

.header-content h1 {
  margin: 0;
  font-size: 24px;
}

.el-main {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.shop-content {
  display: flex;
  justify-content: center;
}

.shop-card {
  width: 100%;
  max-width: 600px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  font-size: 18px;
  font-weight: 600;
}

.shop-form {
  padding: 20px 0;
}

.form-field {
  width: 100%;
  max-width: 400px;
}

.readonly-input :deep(.el-input__inner) {
  background-color: #f5f7fa;
  color: #606266;
  cursor: not-allowed;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}

.form-actions {
  margin-top: 20px;
  display: flex;
  gap: 15px;
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

  .shop-card {
    max-width: 100%;
  }

  .form-field {
    max-width: 100%;
  }

  .form-actions {
    flex-direction: column;
  }

  .form-actions .el-button {
    width: 100%;
  }

  /* 对话框在手机端全宽 */
  :deep(.el-dialog) {
    width: 90% !important;
    margin: 5vh auto;
  }
}
</style>

