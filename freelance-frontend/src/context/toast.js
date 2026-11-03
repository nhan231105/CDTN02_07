import { createContext, useCallback, useContext, useState } from 'react'

export const ToastContext = createContext({ push: () => {} })

export function useToast() {
  return useContext(ToastContext)
}

// Giữ API giống useState('') nhưng mỗi lần đặt thông báo sẽ bật một toast.
export function useMessage() {
  const { push } = useToast()
  const [message, setMessage] = useState('')
  const set = useCallback(
    (text) => {
      setMessage(text)
      if (text) push(text)
    },
    [push],
  )
  return [message, set]
}
