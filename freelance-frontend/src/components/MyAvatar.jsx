import { useEffect, useState } from 'react'
import { request } from '../api/apiService'
import { brandImage } from '../utils/brand'

// Ảnh đại diện của người dùng đang đăng nhập: sinh viên lấy ảnh riêng tư
// qua /me/avatar, nhà tuyển dụng dùng logo công khai của công ty mình.
export default function MyAvatar({ user, className = '' }) {
  const [url, setUrl] = useState('')
  const isStudent = user?.role === 'STUDENT'
  const ref = isStudent ? user?.avatarRef : null

  useEffect(() => {
    if (!isStudent || !ref) {
      setUrl('')
      return
    }
    let active = true
    let blobUrl
    request('GET', '/me/avatar', null, null, { blob: true })
      .then((file) => {
        blobUrl = URL.createObjectURL(file)
        if (active) setUrl(blobUrl)
      })
      .catch(() => {
        if (active) setUrl('')
      })
    return () => {
      active = false
      if (blobUrl) URL.revokeObjectURL(blobUrl)
    }
  }, [isStudent, ref])

  if (!isStudent && user?.role === 'EMPLOYER' && user?.logoRef) {
    return (
      <span className={'avatar avatar-img ' + className}>
        <img src={brandImage(user.id, 'logo', user.logoRef)} alt="" />
      </span>
    )
  }
  if (isStudent && url) {
    return (
      <span className={'avatar avatar-img ' + className}>
        <img src={url} alt="" />
      </span>
    )
  }
  return (
    <span className={'avatar ' + className}>{user?.fullName?.charAt(0)}</span>
  )
}
