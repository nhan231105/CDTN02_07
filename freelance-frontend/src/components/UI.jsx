import { labels } from '../utils/format'
import { useEffect, useRef } from 'react'
export function Status({ value }) {
  return (
    <span className={'status status-' + value?.toLowerCase()}>
      {labels[value] || value}
    </span>
  )
}
export function Feedback({ error, message }) {
  return error ? (
    <div className="feedback error" role="alert">
      {error}
    </div>
  ) : message ? (
    <div className="feedback success" role="status">
      {message}
    </div>
  ) : null
}
export function Empty({ title = 'Chưa có dữ liệu', children }) {
  return (
    <div className="empty">
      <span className="empty-icon">◇</span>
      <h3>{title}</h3>
      <p>{children}</p>
    </div>
  )
}
export function Loading() {
  return (
    <div className="loading" role="status">
      <span className="spinner" />
      Đang tải dữ liệu…
    </div>
  )
}
export function Pagination({ page, onChange }) {
  if (!page || page.totalPages <= 1) return null
  return (
    <nav className="pagination" aria-label="Phân trang">
      <button
        className="btn secondary"
        disabled={page.number === 0}
        onClick={() => onChange(page.number - 1)}
      >
        ← Trước
      </button>
      <span>
        Trang {page.number + 1} / {page.totalPages}
      </span>
      <button
        className="btn secondary"
        disabled={page.number + 1 >= page.totalPages}
        onClick={() => onChange(page.number + 1)}
      >
        Sau →
      </button>
    </nav>
  )
}
export function Field({ label, id, children, hint }) {
  return (
    <div className="field">
      <label htmlFor={id}>{label}</label>
      {children}
      {hint && <small>{hint}</small>}
    </div>
  )
}
export function Modal({ title, onClose, children }) {
  const dialog = useRef(null)
  useEffect(() => {
    const previous = document.activeElement
    dialog.current.showModal()
    return () => {
      if (previous instanceof HTMLElement) previous.focus()
    }
  }, [])
  return (
    <dialog
      ref={dialog}
      onCancel={onClose}
      className="modal"
      aria-label={title}
    >
      <div className="modal-heading">
        <h2>{title}</h2>
        <button
          type="button"
          className="icon-button"
          aria-label="Đóng"
          onClick={onClose}
        >
          ×
        </button>
      </div>
      {children}
    </dialog>
  )
}
