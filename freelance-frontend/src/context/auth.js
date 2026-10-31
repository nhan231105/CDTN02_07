import { createContext, useContext } from 'react'
export const AuthContext = createContext(null)
export const homeFor = (role) =>
  role === 'EMPLOYER' ? '/employer/jobs' : role === 'ADMIN' ? '/admin' : '/jobs'
export function redirectAfterLogin(role, from) {
  if (!from || !from.startsWith('/') || from.startsWith('//'))
    return homeFor(role)
  if (from === '/notifications' || from.startsWith('/jobs')) return from
  const prefix =
    role === 'EMPLOYER'
      ? '/employer/'
      : role === 'ADMIN'
        ? '/admin'
        : '/student/'
  return from.startsWith(prefix) ? from : homeFor(role)
}
export function useAuth() {
  return useContext(AuthContext)
}
