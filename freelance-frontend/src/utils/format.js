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
  ACTIVE: 'Hoạt động',
  LOCKED: 'Đã khóa',
  WITHDRAWN: 'Đã rút đơn',
}
export const jobTypeLabels = {
  PART_TIME: 'Part-time',
  SEASONAL: 'Thời vụ',
  INTERNSHIP: 'Thực tập',
  FREELANCE: 'Freelance',
}
export const payTypeLabels = {
  HOURLY: '/giờ',
  SHIFT: '/ca',
  DAILY: '/ngày',
  MONTHLY: '/tháng',
  PROJECT: '/dự án',
}
export const shiftLabels = {
  MORNING: 'Ca sáng',
  AFTERNOON: 'Ca chiều',
  EVENING: 'Ca tối',
  WEEKEND: 'Cuối tuần',
  FLEXIBLE: 'Linh hoạt',
}
export const salaryText = (job) =>
  job.salary == null
    ? 'Thỏa thuận'
    : new Intl.NumberFormat('vi-VN').format(job.salary) +
      ' ₫' +
      (payTypeLabels[job.payType] || '')
export const datetime = (value) =>
  value ? new Date(value).toLocaleString('vi-VN') : '—'
export const strongPassword = (value) =>
  value.length >= 8 &&
  /[A-Z]/.test(value) &&
  /[a-z]/.test(value) &&
  /[^A-Za-z0-9]/.test(value)
