import { API_BASE_URL } from '../api/apiService'

export function brandImage(id, kind, version) {
  return (
    API_BASE_URL +
    '/employers/' +
    id +
    '/' +
    kind +
    (version ? '?v=' + encodeURIComponent(version) : '')
  )
}
