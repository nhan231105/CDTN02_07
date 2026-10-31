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
  if (token) headers.Authorization = 'Bearer ' + token
  const isForm = data instanceof FormData
  if (data != null && !isForm) headers['Content-Type'] = 'application/json'
  const response = await fetch(url, {
    method,
    headers,
    signal,
    body: data == null ? undefined : isForm ? data : JSON.stringify(data),
  })
  if (response.status === 401 && !endpoint.startsWith('/auth/')) {
    sessionStorage.removeItem(TOKEN_KEY)
    window.dispatchEvent(new Event('session-expired'))
  }
  if (response.status === 204) return null
  if (!response.ok)
    throw new ApiError(response.status, await response.json().catch(() => ({})))
  return blob ? response.blob() : response.json()
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
}
export default api
