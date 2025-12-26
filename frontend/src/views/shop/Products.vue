<template>
  <div class="shop-products">
    <el-header class="header">
      <div class="header-content">
        <h1>商品管理</h1>
        <div>
          <el-button type="primary" @click="showAddDialog = true">添加商品</el-button>
          <el-button @click="$router.push('/shop/dashboard')">返回</el-button>
        </div>
      </div>
    </el-header>
    
    <el-main>
      <el-table :data="products" v-loading="loading">
        <el-table-column prop="name" label="商品名称" />
        <el-table-column prop="price" label="价格">
          <template #default="{ row }">
            ¥{{ row.price }}
          </template>
        </el-table-column>
        <el-table-column prop="stock" label="库存" />
        <el-table-column prop="isOnSale" label="状态">
          <template #default="{ row }">
            <el-tag :type="row.isOnSale ? 'success' : 'danger'">
              {{ row.isOnSale ? '上架' : '下架' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作">
          <template #default="{ row }">
            <el-button size="small" @click="editProduct(row)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-main>
    
    <el-dialog v-model="showAddDialog" title="添加商品" width="500px">
      <el-form :model="productForm" label-width="80px">
        <el-form-item label="商品名称">
          <el-input v-model="productForm.name" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="productForm.description" type="textarea" />
        </el-form-item>
        <el-form-item label="价格">
          <el-input-number v-model="productForm.price" :min="0" :precision="2" />
        </el-form-item>
        <el-form-item label="库存">
          <el-input-number v-model="productForm.stock" :min="0" />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="productForm.categoryId" placeholder="选择分类" style="width: 100%;">
            <el-option
              v-for="cat in categories"
              :key="cat.categoryId"
              :label="cat.name"
              :value="cat.categoryId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="商品图片">
          <el-upload
            class="image-uploader"
            :action="uploadAction"
            :show-file-list="false"
            :on-success="handleImageSuccess"
            :with-credentials="true"
            name="file"
          >
            <img v-if="productForm.imageUrl" :src="getProductImage(productForm.imageUrl)" class="image-preview" />
            <el-icon v-else class="image-uploader-icon"><Plus /></el-icon>
          </el-upload>
          <div class="upload-tip">支持 jpg/png/gif 格式，大小不超过5MB</div>
        </el-form-item>
        <el-form-item label="上架">
          <el-switch v-model="productForm.isOnSale" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddDialog = false">取消</el-button>
        <el-button type="primary" @click="saveProduct">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getProductsByShopId, addProduct, updateProduct, getCategories } from '../../api/product'
import { getShopInfo } from '../../api/shop'
import { getProductImage } from '../../utils/helpers'
import store from '../../store'

const products = ref([])
const loading = ref(false)
const showAddDialog = ref(false)
const editingProduct = ref(null)

const productForm = ref({
  name: '',
  description: '',
  price: 0,
  stock: 0,
  imageUrl: '',
  isOnSale: true,
  categoryId: null
})

const categories = ref([])

const uploadAction = computed(() => '/api/upload/image')

const handleImageSuccess = (response) => {
  if (response.success) {
    productForm.value.imageUrl = response.data
    ElMessage.success('图片上传成功')
  } else {
    ElMessage.error(response.message || '上传失败')
  }
}

const loadProducts = async () => {
  if (!store.user) return
  loading.value = true
  try {
    const shopInfoResponse = await getShopInfo()
    if (shopInfoResponse.success && shopInfoResponse.data) {
      const response = await getProductsByShopId(shopInfoResponse.data.shopId)
      products.value = (response.success && response.data) ? response.data : []
    }
  } catch (error) {
    ElMessage.error('加载商品失败')
    products.value = []
  } finally {
    loading.value = false
  }
}

const loadCategories = async () => {
  try {
    const response = await getCategories()
    if (response.success) {
      categories.value = response.data
      if (!productForm.value.categoryId && categories.value.length) {
        productForm.value.categoryId = categories.value[0].categoryId
      }
    }
  } catch (error) {
    categories.value = []
  }
}

const editProduct = (product) => {
  editingProduct.value = product
  productForm.value = { ...product }
  showAddDialog.value = true
}

const saveProduct = async () => {
  try {

    // 确保imageUrl字段被包含（即使为空字符串）
    const productData = {
      ...productForm.value,
      imageUrl: productForm.value.imageUrl || null  // 空字符串转为null
    }
    
    // 调试：打印要发送的数据
    console.log('保存商品数据:', productData)
    
    let response
    if (editingProduct.value) {
      response = await updateProduct(editingProduct.value.productId, productData)
    } else {
      response = await addProduct(productData)
    }
    
    if (response.success) {
      if (editingProduct.value) {
        ElMessage.success('更新成功')
      } else {
        ElMessage.success('添加成功')
      }
      showAddDialog.value = false
      editingProduct.value = null
      productForm.value = {
        name: '',
        description: '',
        price: 0,
        stock: 0,
        imageUrl: '',
        isOnSale: true,
        categoryId: categories.value.length > 0 ? categories.value[0].categoryId : null
      }
      loadProducts()
    } else {
      ElMessage.error(response.message || '操作失败')
    }
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

onMounted(async () => {
  await store.init()
  await loadCategories()
  await loadProducts()
})
</script>

<style scoped>
.shop-products {
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

.image-uploader {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  width: 178px;
  height: 178px;
}

.image-uploader:hover {
  border-color: #409eff;
}

.image-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 178px;
  height: 178px;
  line-height: 178px;
  text-align: center;
}

.image-preview {
  width: 178px;
  height: 178px;
  display: block;
  object-fit: cover;
}

.upload-tip {
  color: #909399;
  font-size: 12px;
  margin-top: 5px;
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

  .el-main {
    padding: 10px;
  }

  .image-uploader,
  .image-preview {
    width: 120px;
    height: 120px;
  }

  .image-uploader-icon {
    width: 120px;
    height: 120px;
    line-height: 120px;
  }
}
</style>

