<template>
  <div class="category-page">
    <div class="page-header">
      <div class="left">
        <el-button link @click="goBack" :icon="ArrowLeft">返回</el-button>
        <h2>{{ categoryTitle }}</h2>
      </div>
      <div class="right">
        <el-input
          v-model="keyword"
          placeholder="搜索该分类商品"
          @keyup.enter="fetchProducts"
          clearable
          class="search-input"
        >
          <template #append>
            <el-button @click="fetchProducts" :icon="Search">搜索</el-button>
          </template>
        </el-input>
        <template v-if="store.user">
          <el-button @click="() => handleLogout(router)" style="margin-left: 10px">退出登录</el-button>
        </template>
        <template v-else>
          <el-button @click="$router.push('/login')" style="margin-left: 10px">登录</el-button>
        </template>
      </div>
    </div>

    <el-row :gutter="20" v-loading="loading">
      <el-col :xs="12" :sm="8" :md="6" :lg="6" v-for="product in products" :key="product.productId">
        <el-card
          class="product-card"
          @click="goToProduct(product.productId)"
          shadow="hover"
        >
          <img :src="getProductImage(product.imageUrl)" class="product-image" />
          <div class="product-info">
            <h3>{{ product.name }}</h3>
            <p class="price">¥{{ product.price }}</p>
            <p class="stock">库存: {{ product.stock }}</p>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-empty
      v-if="!loading && products.length === 0"
      :description="emptyText"
      class="empty-tip"
    />
  </div>
</template>

<script setup>
import { ref, onMounted, watch, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search, ArrowLeft } from '@element-plus/icons-vue'
import { getProducts, getCategories } from '../../api/product'
import { getProductImage } from '../../utils/helpers'
import { handleLogout } from '../../utils/userHelpers'
import store from '../../store'

const route = useRoute()
const router = useRouter()

const keyword = ref(route.query.keyword || '')
const products = ref([])
const loading = ref(false)
const categories = ref([])

const categoryId = computed(() => {
  const id = route.params.id
  if (!id) return null
  const numId = Number(id)
  return isNaN(numId) ? null : numId
})

const categoryTitle = computed(() => {
  for (const cat of categories.value) {
    if (cat.categoryId === categoryId.value) {
      return cat.name
    }
  }
  return '分类商品'
})

const emptyText = computed(() => {
  if (keyword.value && keyword.value.trim()) {
    return '未找到商品，请搜索其他关键词'
  }
  return '该分类暂无商品'
})

const fetchCategories = async () => {
  try {
    const res = await getCategories()
    if (res.success) categories.value = res.data
  } catch (e) {
    categories.value = []
  }
}

const fetchProducts = async () => {
  loading.value = true
  try {
    const res = await getProducts({
      categoryId: categoryId.value,
      keyword: keyword.value || undefined
    })
    if (res.success) {
      products.value = res.data
    } else {
      ElMessage.error(res.message || '加载失败')
    }
  } catch (e) {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

const goToProduct = (id) => {
  if (!id) return
  router.push(`/product/${id}`)
}

const goBack = () => {
  router.back()
}


watch(
  () => route.params.id,
  () => {
    keyword.value = route.query.keyword || ''
    fetchProducts()
  }
)

onMounted(async () => {
  await fetchCategories()
  await fetchProducts()
})
</script>

<style scoped>
.category-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  margin-bottom: 16px;
}

.page-header .left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.page-header .right {
  display: flex;
  align-items: center;
  gap: 10px;
}

.search-input {
  max-width: 400px;
}

.product-card {
  margin-bottom: 20px;
}

.product-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.product-image {
  width: 100%;
  height: 200px;
  object-fit: cover;
}

.product-info h3 {
  margin: 10px 0;
  font-size: 16px;
}

.price {
  color: #f56c6c;
  font-size: 20px;
  font-weight: bold;
}

.stock {
  color: #909399;
  font-size: 12px;
}

/* 响应式布局 - 手机端 */
@media (max-width: 768px) {
  .category-page {
    padding: 10px;
  }

  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }

  .page-header .right {
    width: 100%;
    flex-direction: column;
    align-items: stretch;
  }

  .search-input {
    max-width: 100%;
    width: 100%;
  }

  .product-image {
    height: 150px;
  }

  .product-info h3 {
    font-size: 14px;
  }

  .price {
    font-size: 18px;
  }
}
</style>


