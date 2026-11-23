export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api'
export const TOKEN_KEY = 'campuswork.token'
export class ApiError extends Error {
  constructor(status, data) {
    super(data?.message || 'Không thể kết nối máy chủ. Vui lòng thử lại.')
    this.status = status
    this.code = data?.code
    this.fieldErrors = data?.fieldErrors
  }
}
export async function request(
  method,
  endpoint,
  data,
  params,
  { signal, blob = false } = {},
) {
  const url = new URL(API_BASE_URL + endpoint, window.location.origin)
  Object.entries(params || {}).forEach(([key, value]) => {
    if (value !== '' && value !== null && value !== undefined)
      url.searchParams.set(key, value)
  })
  const token = sessionStorage.getItem(TOKEN_KEY)
  const headers = {}
  const publicAuth =
    /^\/auth\/(?:login|register(?:\/verify)?|password\/(?:forgot|reset))$/.test(
      endpoint,
    )
  if (token && !publicAuth) headers.Authorization = 'Bearer ' + token
  const isForm = data instanceof FormData
  if (data != null && !isForm) headers['Content-Type'] = 'application/json'
  let response
  try {
    response = await fetch(url, {
      method,
      headers,
      signal,
      body: data == null ? undefined : isForm ? data : JSON.stringify(data),
    })
  } catch (error) {
    if (signal?.aborted) throw error
    throw new ApiError(0, {
      message:
        'Chưa kết nối được với CampusWork. Vui lòng thử lại sau ít phút.',
    })
  }
  if (response.status === 401 && !endpoint.startsWith('/auth/')) {
    sessionStorage.removeItem(TOKEN_KEY)
    window.dispatchEvent(new Event('session-expired'))
  }
  if (response.status === 204) return null
  if ([502, 503, 504].includes(response.status)) {
    const details = await response.json().catch(() => null)
    throw new ApiError(
      response.status,
      details?.code === 'EMAIL_UNAVAILABLE'
        ? details
        : {
            message:
              'Máy chủ có thể đang khởi động. Vui lòng thử lại sau ít phút.',
          },
    )
  }
  if (!response.ok)
    throw new ApiError(response.status, await response.json().catch(() => ({})))
  if (blob) return response.blob()
  return response.json().catch(() => {
    throw new ApiError(response.status, {
      message: 'Máy chủ trả về dữ liệu không hợp lệ. Vui lòng thử lại.',
    })
  })
}
export function upload(endpoint, file) {
  const form = new FormData()
  form.append('file', file)
  return request('POST', endpoint, form)
}
export async function download(endpoint, name) {
  const file = await request('GET', endpoint, null, null, { blob: true })
  const url = URL.createObjectURL(file)
  const link = document.createElement('a')
  link.href = url
  link.download = name || 'CV.pdf'
  link.click()
  setTimeout(() => URL.revokeObjectURL(url), 1000)
}
const api = {
  register: (data) => request('POST', '/auth/register', data),
  verifyRegistration: (data) => request('POST', '/auth/register/verify', data),
  forgotPassword: (data) => request('POST', '/auth/password/forgot', data),
  resetPassword: (data) => request('POST', '/auth/password/reset', data),
  conversation: (id, page) =>
    request('GET', '/applications/' + id + '/messages', null, { page }),
  sendMessage: (id, body) =>
    request('POST', '/applications/' + id + '/messages', { body }),
  readMessages: (id, through) =>
    request('PATCH', '/applications/' + id + '/messages/read', { through }),
  login: (data) => request('POST', '/auth/login', data),
  profile: () => request('GET', '/me/profile'),
  saveProfile: (data) => request('PUT', '/me/profile', data),
  saveAccount: (data) => request('PATCH', '/me/account', data),
  jobs: (params, signal) => request('GET', '/jobs', null, params, { signal }),
  job: (id) => request('GET', '/jobs/' + id),
  apply: (id, data) => request('POST', '/jobs/' + id + '/applications', data),
  applications: (page) =>
    request('GET', '/me/applications', null, { page, size: 12 }),
  review: (id, data) =>
    request('POST', '/applications/' + id + '/reviews', data),
  reviews: (id) => request('GET', '/reviews/users/' + id, null, { size: 12 }),
  notifications: (page) =>
    request('GET', '/me/notifications', null, { page, size: 12 }),
  unread: () => request('GET', '/me/notifications/count/unread'),
  read: (id) => request('PATCH', '/me/notifications/' + id + '/read'),
  employerJobs: (id, page) =>
    request('GET', '/jobs/employer/' + id, null, { page, size: 12 }),
  createJob: (data) => request('POST', '/jobs', data),
  updateJob: (id, data) => request('PUT', '/jobs/' + id, data),
  status: (id, status) =>
    request('PATCH', '/jobs/' + id + '/status', { status }),
  applicants: (id, page) =>
    request('GET', '/jobs/' + id + '/applications', null, { page, size: 12 }),
  decide: (id, decision) =>
    request('PATCH', '/applications/' + id + '/decision', { decision }),
  stats: () => request('GET', '/auth/admin/stats'),
  adminUsers: (params) => request('GET', '/auth/admin/search', null, params),
  adminSetUserStatus: (id, status, reason) =>
    request('PATCH', '/admin/users/' + id + '/status', null, {
      status,
      reason,
    }),
  adminRemoveJob: (id, reason) =>
    request('PATCH', '/admin/jobs/' + id + '/moderation', null, { reason }),
  adminAllJobs: (page) =>
    request('GET', '/admin/jobs', null, { page, size: 12 }),
  adminRestoreJob: (id) => request('PATCH', '/admin/jobs/' + id + '/restore'),
  adminDeleteUser: (id) => request('DELETE', '/admin/users/' + id),
  adminReports: (page) =>
    request('GET', '/admin/reports', null, { page, size: 12 }),
  adminResolveReport: (id, adminAction) =>
    request('PATCH', '/admin/reports/' + id + '/resolve', null, {
      adminAction,
    }),
  adminRejectReport: (id) =>
    request('PATCH', '/admin/reports/' + id + '/reject'),
  submitReport: (data) => request('POST', '/reports', data),
  savedJobs: (page) =>
    request('GET', '/me/saved-jobs', null, { page, size: 12 }),
  savedJobIds: () => request('GET', '/me/saved-jobs/ids'),
  saveJob: (id) => request('PUT', '/me/saved-jobs/' + id),
  unsaveJob: (id) => request('DELETE', '/me/saved-jobs/' + id),
  withdraw: (id) => request('PATCH', '/applications/' + id + '/withdraw'),
  readAll: () => request('PATCH', '/me/notifications/read-all'),
  employerPublic: (id) => request('GET', '/employers/' + id + '/public'),
  nearbyJobs: (params, signal) =>
    request('GET', '/jobs/nearby', null, params, { signal }),
  talent: (params) => request('GET', '/talent', null, params),
  talentDetail: (id) => request('GET', '/talent/' + id),
  invite: (data) => request('POST', '/invitations', data),
  sentInvitations: (page) =>
    request('GET', '/invitations/sent', null, { page, size: 12 }),
  myInvitations: () => request('GET', '/me/invitations'),
  respondInvitation: (id, accept) =>
    request('PATCH', '/invitations/' + id + '/respond', { accept }),
}
export default api
