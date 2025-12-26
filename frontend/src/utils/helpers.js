/**
 * 工具函数集合
 */

/**
 * 获取商品图片URL
 * @param {string} url - 图片URL
 * @returns {string} 完整的图片URL
 */
export const getProductImage = (url) => {
  if (!url) return 'data:image/svg+xml,%3Csvg xmlns="http://www.w3.org/2000/svg" width="200" height="200"%3E%3Crect fill="%23ddd" width="200" height="200"/%3E%3Ctext fill="%23999" font-family="sans-serif" font-size="14" x="50%25" y="50%25" text-anchor="middle" dy=".3em"%3E暂无图片%3C/text%3E%3C/svg%3E'
  
  // 如果包含localhost:8080，替换为相对路径
  if (url.includes('localhost:8080')) {
    url = url.replace(/https?:\/\/localhost:8080/, '')
  }
  
  // 如果已经是完整URL（且不是localhost），直接返回
  if (url.startsWith('http://') || url.startsWith('https://')) {
    return url
  }
  
  // 如果是相对路径，直接返回（通过Nginx代理访问）
  if (url.startsWith('/')) {
    return url
  }
  
  // 否则添加 / 前缀
  return '/' + url
}

/**
 * 格式化日期
 * @param {string} dateStr - 日期字符串
 * @returns {string} 格式化后的日期字符串
 */
export const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN')
}

