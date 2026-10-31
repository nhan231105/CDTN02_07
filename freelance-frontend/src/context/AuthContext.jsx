import { useCallback, useEffect, useState } from 'react'
import api, { TOKEN_KEY } from '../api/apiService'
import { AuthContext } from './auth'
export function AuthProvider({ children }) {
  const [user, setUser] = useState(null)
  const [loading, setLoading] = useState(true)
  const logout = useCallback(() => {
    sessionStorage.removeItem(TOKEN_KEY)
    setUser(null)
  }, [])
  const refresh = useCallback(async () => {
    const response = await api.profile()
    const profile = response.data || response
    setUser(profile)
    return profile
  }, [])
  useEffect(() => {
    let active = true
    if (sessionStorage.getItem(TOKEN_KEY)) {
      api
        .profile()
        .then((response) => {
          if (active) setUser(response.data || response)
        })
        .catch(() => {
          if (active) logout()
        })
        .finally(() => {
          if (active) setLoading(false)
        })
    } else
      Promise.resolve().then(() => {
        if (active) setLoading(false)
      })
    window.addEventListener('session-expired', logout)
    return () => {
      active = false
      window.removeEventListener('session-expired', logout)
    }
  }, [logout])
  const login = async (credentials) => {
    const response = await api.login(credentials)
    const data = response.data || response
    sessionStorage.setItem(TOKEN_KEY, data.accessToken)
    setUser(data)
    return data
  }
  return (
    <AuthContext.Provider
      value={{ user, loading, login, logout, refresh, register: api.register }}
    >
      {children}
    </AuthContext.Provider>
  )
}
