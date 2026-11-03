import { useState } from 'react'

const KEY = 'campuswork-theme'

function initialTheme() {
  try {
    return localStorage.getItem(KEY) === 'light' ? 'light' : 'dark'
  } catch {
    return 'dark'
  }
}

function apply(next) {
  document.documentElement.dataset.theme = next
  try {
    localStorage.setItem(KEY, next)
  } catch {
    /* bỏ qua khi trình duyệt chặn lưu trữ */
  }
}

// Chuyển giao diện bằng View Transition (mờ dần ~0,8s); trình duyệt cũ dùng transition CSS.
export default function ThemeToggle() {
  const [theme, setTheme] = useState(
    () => document.documentElement.dataset.theme || initialTheme(),
  )
  const next = theme === 'dark' ? 'light' : 'dark'

  const toggle = () => {
    const swap = () => {
      apply(next)
      setTheme(next)
    }
    const reduce = window.matchMedia('(prefers-reduced-motion: reduce)').matches
    if (document.startViewTransition && !reduce) {
      document.startViewTransition(swap)
      return
    }
    const root = document.documentElement
    root.classList.add('theme-fade')
    swap()
    setTimeout(() => root.classList.remove('theme-fade'), 900)
  }

  return (
    <button
      type="button"
      className={'theme-toggle ' + theme}
      onClick={toggle}
      role="switch"
      aria-checked={theme === 'dark'}
      aria-label={
        theme === 'dark'
          ? 'Chuyển sang giao diện sáng'
          : 'Chuyển sang giao diện tối'
      }
      title={theme === 'dark' ? 'Giao diện sáng' : 'Giao diện tối'}
    >
      <span className="tt-stars" aria-hidden="true">
        <i />
        <i />
        <i />
      </span>
      <span className="tt-cloud" aria-hidden="true" />
      <span className="tt-knob" aria-hidden="true">
        <svg
          className="tt-sun"
          viewBox="0 0 24 24"
          width="16"
          height="16"
          fill="currentColor"
        >
          <circle cx="12" cy="12" r="4.5" />
          <g stroke="currentColor" strokeWidth="2" strokeLinecap="round">
            <path d="M12 2.5v2.5M12 19v2.5M2.5 12H5M19 12h2.5M5.3 5.3l1.8 1.8M16.9 16.9l1.8 1.8M5.3 18.7l1.8-1.8M16.9 7.1l1.8-1.8" />
          </g>
        </svg>
        <svg
          className="tt-moon"
          viewBox="0 0 24 24"
          width="16"
          height="16"
          fill="currentColor"
        >
          <path d="M20.5 14.5A8.5 8.5 0 0 1 9.5 3.5a8.5 8.5 0 1 0 11 11Z" />
        </svg>
      </span>
    </button>
  )
}
