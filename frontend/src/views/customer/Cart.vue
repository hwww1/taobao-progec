<template>
  <div class="cart-container">
    <el-header class="header">
      <div class="header-content">
        <h1>购物车</h1>
        <div class="header-actions">
          <el-button @click="$router.push('/profile')">个人信息</el-button>
          <el-button @click="$router.push('/orders')">我的订单</el-button>
          <el-button @click="$router.push('/')">继续购物</el-button>
        </div>
      </div>
    </el-header>
    
    <el-main>
      <el-table :data="cartItems" v-loading="loading">
        <el-table-column prop="product.name" label="商品名称" />
        <el-table-column prop="product.price" label="单价">
          <template #default="{ row }">
            ¥{{ row.product.price }}
          </template>
        </el-table-column>
        <el-table-column label="数量" width="150">
          <template #default="{ row }">
            <el-input-number v-model="row.quantity" :min="1" @change="updateQuantity(row.product.productId, $event)" />
          </template>
        </el-table-column>
    <el-table-column label="收货地址" width="300">
          <template #default="{ row }">
            <el-select v-model="row.selectedAddressId" placeholder="选择地址" size="small" style="width: 100%">
              <el-option 
                v-for="address in addresses" 
                :key="address.addressId" 
                :label="`${address.province} ${address.city} ${address.district} ${address.detailAddress}`"
                :value="address.addressId"
              >
                <div>
                  <span>{{ address.province }} {{ address.city }} {{ address.district }} {{ address.detailAddress }}</span>
                </div>
              </el-option>
            </el-select>
          </template>
        </el-table-column>
        <el-table-column label="小计">
          <template #default="{ row }">
            ¥{{ (row.product.price * row.quantity).toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column label="操作">
          <template #default="{ row }">
            <el-button type="danger" size="small" @click="removeItem(row.product.productId)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="cart-footer">
        <div class="total">
          <span>总计: ¥{{ totalAmount.toFixed(2) }}</span>
        </div>
        <el-button type="primary" size="large" @click="confirmCheckout" :disabled="cartItems.length === 0">结算</el-button>
      </div>
    </el-main>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getProductById } from '../../api/product'
import { createOrder } from '../../api/order'
import { getAddresses } from '../../api/address'
import store from '../../store'

const router = useRouter()
const cartItems = ref([])
const loading = ref(false)
const addresses = ref([])

// 计算购物车总金额
const totalAmount = computed(() => {
  return cartItems.value.reduce((sum, item) => {
    return sum + item.product.price * item.quantity
  }, 0)
})

// 加载购物车：从store获取商品ID，查询商品详情用于显示
const loadCart = async () => {
  loading.value = true
  try {
    // 保存已选择的地址，避免重新加载时丢失
    const addressMap = {}
    for (const item of cartItems.value) {
      if (item.selectedAddressId) {
        addressMap[item.product.productId] = item.selectedAddressId
      }
    }
    
    // 查询每个商品的详情
    const items = []
    for (const [productId, quantity] of store.cart.entries()) {
      const response = await getProductById(productId)
      if (response.success && response.data) {
        items.push({
          product: response.data,
          quantity,
          selectedAddressId: addressMap[productId] || null
        })
      }
    }
    cartItems.value = items
    await loadAddresses()
  } catch (error) {
    ElMessage.error('加载购物车失败')
  } finally {
    loading.value = false
  }
}

const updateQuantity = (productId, quantity) => {
  store.updateCart(productId, quantity)
  loadCart()
}

const removeItem = (productId) => {
  store.removeFromCart(productId)
  loadCart()
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

// 确认结算：为每个商品使用各自的地址
const confirmCheckout = async () => {
  try {
    let successCount = 0
    for (const item of cartItems.value) {
      if (!item.selectedAddressId) {
        ElMessage.warning('请为每个商品选择收货地址')
        return
      }
      const response = await createOrder({
        cartItems: { [item.product.productId]: item.quantity },
        addressId: item.selectedAddressId
      })
      if (response.success) {
        successCount++
      } else {
        ElMessage.error(response.message || '订单创建失败')
      }
    }
    if (successCount > 0) {
      store.clearCart()
      ElMessage.success(`成功创建 ${successCount} 个订单`)
      router.push('/orders')
    }
  } catch (error) {
    ElMessage.error('订单创建失败')
  }
}

onMounted(() => {
  loadCart()
})
</script>

<style scoped>
.cart-container {
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

.cart-footer {
  margin-top: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.total {
  font-size: 20px;
  font-weight: bold;
  color: #f56c6c;
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

  .cart-footer {
    flex-direction: column;
    gap: 15px;
    align-items: stretch;
  }

  .cart-footer .el-button {
    width: 100%;
  }

  .total {
    text-align: center;
    font-size: 18px;
  }
}
</style>

