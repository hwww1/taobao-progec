/**
 * 订单相关工具函数
 */

/**
 * 获取订单状态的标签类型（用于Element Plus的Tag组件）
 */
export const getOrderStatusType = (status) => {
  const statusMap = {
    '待支付': 'warning',
    '待发货': 'info',
    '待收货': 'primary',
    '已完成': 'success',
    '已取消': 'danger'
  }
  return statusMap[status] || ''
}

