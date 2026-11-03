import { useCallback, useMemo, useRef, useState } from 'react'
import { ToastContext } from './toast'

const LIFETIME = 4200
const EXIT = 320

export function ToastProvider({ children }) {
  const [items, setItems] = useState([])
  const seq = useRef(0)

  const close = useCallback((id) => {
    setItems((list) => list.map((t) => (t.id === id ? { ...t, out: true } : t)))
    setTimeout(() => setItems((list) => list.filter((t) => t.id !== id)), EXIT)
  }, [])

  const push = useCallback(
    (text) => {
      const id = ++seq.current
      setItems((list) => [...list.slice(-3), { id, text }])
      setTimeout(() => close(id), LIFETIME)
    },
    [close],
  )
  const value = useMemo(() => ({ push }), [push])

  return (
    <ToastContext.Provider value={value}>
      {children}
      <div className="toast-region" role="status" aria-live="polite">
        {items.map((t) => (
          <div
            key={t.id}
            className={'toast' + (t.out ? ' out' : '')}
            style={{ '--life': LIFETIME + 'ms' }}
          >
            <span className="toast-ico" aria-hidden="true">
              <svg viewBox="0 0 24 24" width="16" height="16" fill="none">
                <path
                  d="M5 12.5l4.2 4.2L19 7"
                  stroke="currentColor"
                  strokeWidth="3"
                  strokeLinecap="round"
                  strokeLinejoin="round"
                />
              </svg>
            </span>
            <span className="toast-text">{t.text}</span>
            <button
              type="button"
              className="toast-x"
              aria-label="Đóng thông báo"
              onClick={() => close(t.id)}
            >
              ×
            </button>
            <i className="toast-bar" />
          </div>
        ))}
      </div>
    </ToastContext.Provider>
  )
}
