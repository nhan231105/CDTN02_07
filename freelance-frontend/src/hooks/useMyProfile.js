import { useEffect, useState } from 'react'
import api from '../api/apiService'
import { useAuth } from '../context/auth'

// Hồ sơ sinh viên đang đăng nhập (null nếu không phải sinh viên).
export default function useMyProfile() {
  const { user } = useAuth() || {}
  const isStudent = user?.role === 'STUDENT'
  const [profile, setProfile] = useState(null)
  useEffect(() => {
    if (!isStudent) return
    let active = true
    api
      .profile()
      .then((r) => {
        if (active) setProfile(r.data || r)
      })
      .catch(() => {})
    return () => {
      active = false
    }
  }, [isStudent])
  return isStudent ? profile : null
}
