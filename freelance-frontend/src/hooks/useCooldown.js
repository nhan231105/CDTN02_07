import { useCallback, useEffect, useState } from 'react'

/** Đếm ngược (giây) giữa các lần gửi lại mã OTP; khớp cooldown 60s của backend. */
export default function useCooldown(seconds = 60) {
  const [left, setLeft] = useState(0)
  useEffect(() => {
    if (left <= 0) return undefined
    const timer = setTimeout(() => setLeft((value) => value - 1), 1000)
    return () => clearTimeout(timer)
  }, [left])
  const start = useCallback(() => setLeft(seconds), [seconds])
  return [left, start]
}
