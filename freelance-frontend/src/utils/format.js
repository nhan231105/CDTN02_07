export const money = (amount) =>
  amount == null
    ? 'Thỏa thuận'
    : new Intl.NumberFormat('vi-VN').format(amount) + ' ₫'
export const date = (value) =>
  value ? new Date(value).toLocaleDateString('vi-VN') : '—'
export const labels = {
  OPEN: 'Đang tuyển',
  FILLED: 'Đã nhận người',
  COMPLETED: 'Hoàn thành',
  PENDING: 'Chờ duyệt',
  APPROVED: 'Đã duyệt',
  REJECTED: 'Từ chối',
}
export const strongPassword = (value) =>
  value.length >= 8 &&
  /[A-Z]/.test(value) &&
  /[a-z]/.test(value) &&
  /[^A-Za-z0-9]/.test(value)
