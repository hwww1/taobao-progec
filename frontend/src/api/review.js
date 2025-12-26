import request from './request'

export const getReviewsByProductId = (productId) => {
  return request.get(`/reviews/product/${productId}`)
}

export const getProductReviewStats = (productId) => {
  return request.get(`/reviews/product/${productId}/stats`)
}

export const addReview = (review) => {
  return request.post('/reviews', review)
}

export const updateReview = (reviewId, review) => {
  return request.put(`/reviews/${reviewId}`, review)
}

export const deleteReview = (reviewId) => {
  return request.delete(`/reviews/${reviewId}`)
}

export const canReview = (productId) => {
  return request.get(`/reviews/product/${productId}/can-review`)
}

