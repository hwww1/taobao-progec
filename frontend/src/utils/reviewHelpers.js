import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { updateReview, deleteReview } from '../api/review'

/**
 * 评价编辑功能复用工具
 * 用于减少 ProductDetail.vue 和 OrderDetail.vue 中的重复代码
 */
export function useReviewEditor(reloadCallback) {
  const editDialogVisible = ref(false)
  const editForm = ref({
    reviewId: null,
    productId: null,
    rating: 5,
    content: ''
  })

  const openEditReview = (review) => {
    editForm.value = {
      reviewId: review.reviewId,
      productId: review.productId ? review.productId : null,
      rating: review.rating,
      content: review.content
    }
    editDialogVisible.value = true
  }

  const submitEditReview = async () => {
    try {
      const response = await updateReview(editForm.value.reviewId, {
        rating: editForm.value.rating,
        content: editForm.value.content
      })
      if (response.success) {
        ElMessage.success('修改成功')
        editDialogVisible.value = false
        if (reloadCallback) {
          await reloadCallback()
        }
      } else {
        ElMessage.error(response.message || '修改失败')
      }
    } catch (error) {
      ElMessage.error('修改失败，请重试')
    }
  }

  const removeReview = async (reviewId) => {
    try {
      const response = await deleteReview(reviewId)
      if (response.success) {
        ElMessage.success('删除成功')
        if (reloadCallback) {
          await reloadCallback()
        }
      } else {
        ElMessage.error(response.message || '删除失败')
      }
    } catch (error) {
      ElMessage.error('删除失败，请重试')
    }
  }

  return {
    editDialogVisible,
    editForm,
    openEditReview,
    submitEditReview,
    removeReview
  }
}

