<template>
  <div class="profile-container">
    <el-header class="header">
      <div class="header-content">
        <h1>个人信息</h1>
        <div class="header-actions">
          <el-button @click="$router.push('/')">首页</el-button>
          <el-button @click="$router.push('/orders')">我的订单</el-button>
          <el-button @click="$router.push('/cart')">购物车</el-button>
        </div>
      </div>
    </el-header>
    
    <el-main>
      <div class="profile-content">
        <el-card class="info-card" v-loading="loading">
          <template #header>
            <div class="card-header">
              <span class="card-title">基本信息</span>
            </div>
          </template>
          
          <el-form :model="formData" label-width="100px" class="profile-form">
            <el-form-item label="用户名">
              <el-input 
                v-if="!isEditing" 
                :value="formData.username" 
                readonly 
                class="readonly-input"
              />
              <el-input 
                v-else 
                v-model="formData.username" 
                placeholder="请输入用户名"
                clearable
              />
            </el-form-item>
            
            <el-form-item label="密码">
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
              <div v-if="isEditing" class="form-tip">留空则不修改密码</div>
            </el-form-item>
            
            <el-form-item>
              <el-button 
                v-if="!isEditing" 
                type="primary" 
                @click="startEdit"
              >
                修改信息
              </el-button>
              <template v-else>
                <el-button 
                  type="primary" 
                  @click="saveInfo" 
                  :loading="saving"
                >
                  保存修改
                </el-button>
                <el-button 
                  @click="cancelEdit"
                >
                  取消
                </el-button>
              </template>
            </el-form-item>
          </el-form>
        </el-card>

        <el-card class="address-card" v-loading="addressLoading">
          <template #header>
            <div class="card-header">
              <span class="card-title">收货地址</span>
              <el-button type="primary" size="small" @click="showAddDialog">添加地址</el-button>
            </div>
          </template>
          
          <div class="address-list">
            <el-empty v-if="addresses.length === 0 && !addressLoading" description="暂无收货地址" />
            
            <div v-else class="address-items">
              <div 
                v-for="address in addresses" 
                :key="address.addressId" 
                class="address-item"
              >
                <div class="address-header">
                  <span class="receiver-name">{{ address.receiverName }}</span>
                  <span class="receiver-phone">{{ address.receiverPhone }}</span>
                </div>
                <div class="address-detail">
                  {{ address.province }} {{ address.city }} {{ address.district }} {{ address.detailAddress }}
                </div>
                <div class="address-actions">
                  <el-button size="small" @click="editAddress(address)">编辑</el-button>
                  <el-button size="small" type="danger" @click="handleDelete(address.addressId)">删除</el-button>
                </div>
              </div>
            </div>
          </div>
        </el-card>
      </div>
    </el-main>

    <!-- 添加/编辑地址对话框 -->
    <el-dialog 
      :title="addressDialogTitle" 
      v-model="addressDialogVisible" 
      width="600px"
      @close="resetAddressForm"
    >
      <el-form :model="addressForm" label-width="100px">
        <el-form-item label="收货人姓名">
          <el-input v-model="addressForm.receiverName" placeholder="请输入收货人姓名" />
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="addressForm.receiverPhone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="省份">
          <el-input v-model="addressForm.province" placeholder="请输入省份" />
        </el-form-item>
        <el-form-item label="城市">
          <el-input v-model="addressForm.city" placeholder="请输入城市" />
        </el-form-item>
        <el-form-item label="区/县">
          <el-input v-model="addressForm.district" placeholder="请输入区/县" />
        </el-form-item>
        <el-form-item label="详细地址">
          <el-input 
            v-model="addressForm.detailAddress" 
            type="textarea" 
            :rows="3"
            placeholder="请输入详细地址" 
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addressDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAddressForm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getCurrentUser, changePassword, logout, updateProfile } from '../../api/auth'
import { getAddresses, addAddress, updateAddress, deleteAddress } from '../../api/address'
import store from '../../store'

const router = useRouter()

const formData = ref({
  username: '',
  newPassword: ''
})
const originalData = ref({
  username: ''
})
const loading = ref(false)
const saving = ref(false)
const isEditing = ref(false)

// 收货地址相关
const addresses = ref([])
const addressLoading = ref(false)
const addressDialogVisible = ref(false)
const addressEditingId = ref(null)
const addressForm = ref({
  receiverName: '',
  receiverPhone: '',
  province: '',
  city: '',
  district: '',
  detailAddress: ''
})

const addressDialogTitle = computed(() => addressEditingId.value ? '编辑地址' : '添加地址')

const loadUserInfo = async () => {
  loading.value = true
  try {
    const response = await getCurrentUser()
    if (response.success && response.data) {
      formData.value.username = response.data.username
      originalData.value.username = response.data.username
    } else {
      ElMessage.error(response.message || '获取用户信息失败')
    }
  } catch (error) {
    ElMessage.error('获取用户信息失败')
  } finally {
    loading.value = false
  }
}

const startEdit = () => {
  isEditing.value = true
  formData.value.newPassword = ''
}

const cancelEdit = () => {
  formData.value.username = originalData.value.username
  formData.value.newPassword = ''
  isEditing.value = false
}

// 保存用户信息：直接发送给后端处理
const saveInfo = async () => {
  saving.value = true
  try {
    // 先更新用户名（如果有变化）
    if (formData.value.username !== originalData.value.username) {
      const profileResponse = await updateProfile(formData.value.username)
      if (!profileResponse.success) {
        ElMessage.error(profileResponse.message || '用户名修改失败')
        saving.value = false
        return
      }
      originalData.value.username = formData.value.username
      if (store.user) {
        store.user.username = formData.value.username
      }
    }
    
    // 再更新密码（如果输入了）
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

const loadAddresses = async () => {
  addressLoading.value = true
  try {
    const response = await getAddresses()
    addresses.value = (response.success && response.data) ? response.data : []
  } catch (error) {
    addresses.value = []
  } finally {
    addressLoading.value = false
  }
}

const showAddDialog = () => {
  addressEditingId.value = null
  resetAddressForm()
  addressDialogVisible.value = true
}

const editAddress = (address) => {
  addressEditingId.value = address.addressId
  addressForm.value = {
    receiverName: address.receiverName,
    receiverPhone: address.receiverPhone,
    province: address.province,
    city: address.city,
    district: address.district,
    detailAddress: address.detailAddress
  }
  addressDialogVisible.value = true
}

const resetAddressForm = () => {
  addressForm.value = {
    receiverName: '',
    receiverPhone: '',
    province: '',
    city: '',
    district: '',
    detailAddress: ''
  }
}

const submitAddressForm = async () => {
  try {
    const response = addressEditingId.value 
      ? await updateAddress(addressEditingId.value, addressForm.value)
      : await addAddress(addressForm.value)
    if (response.success) {
      ElMessage.success(addressEditingId.value ? '地址更新成功' : '地址添加成功')
      addressDialogVisible.value = false
      loadAddresses()
    } else {
      ElMessage.error(response.message || '操作失败')
    }
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除这个地址吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const response = await deleteAddress(id)
    if (response.success) {
      ElMessage.success('地址删除成功')
      loadAddresses()
    } else {
      ElMessage.error(response.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

onMounted(async () => {
  await store.init()
  await loadUserInfo()
  await loadAddresses()
})
</script>

<style scoped>
.profile-container {
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

.header-actions {
  display: flex;
  gap: 10px;
}

.el-main {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.profile-content {
  display: flex;
  gap: 20px;
  justify-content: center;
  align-items: flex-start;
}

.info-card,
.address-card {
  flex: 1;
  max-width: 500px;
  min-height: 400px;
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

.profile-form {
  padding: 20px 0;
  min-height: 300px;
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

.address-list {
  min-height: 300px;
}

.address-items {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.address-item {
  padding: 15px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  background-color: #fafafa;
}

.address-item.default-address {
  border: 2px solid #409eff;
  background-color: #ecf5ff;
}

.address-header {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 10px;
}

.default-badge {
  background-color: #409eff;
  color: white;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
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
  margin-bottom: 10px;
}

.address-actions {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
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

  .profile-content {
    flex-direction: column;
  }

  .info-card,
  .address-card {
    max-width: 100%;
  }

  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }

  .card-header .el-button {
    width: 100%;
  }

  .form-actions {
    flex-direction: column;
  }

  .form-actions .el-button {
    width: 100%;
  }

  .address-item {
    padding: 10px;
  }

  .address-actions {
    flex-direction: column;
  }

  .address-actions .el-button {
    width: 100%;
  }

  /* 对话框在手机端全宽 */
  :deep(.el-dialog) {
    width: 90% !important;
    margin: 5vh auto;
  }
}
</style>

