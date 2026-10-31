import { useAuth } from '../context/auth'
import { date } from '../utils/format'
import { useCallback, useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import api from '../api/apiService'
import { Empty, Feedback, Loading, Pagination } from '../components/UI'
export default function Notifications() {
  const { user } = useAuth()
  const [page, setPage] = useState(null)
  const [number, setNumber] = useState(0)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(null)
  const load = useCallback(async () => {
    setError('')
    try {
      setPage(await api.notifications(number))
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }, [number])
  useEffect(() => {
    let active = true
    const refresh = () => {
      if (active && !document.hidden) load()
    }
    refresh()
    const timer = setInterval(refresh, 30000)
    return () => {
      active = false
      clearInterval(timer)
    }
  }, [load])
  const read = async (id) => {
    setBusy(id)
    setError('')
    try {
      await api.read(id)
      await load()
      window.dispatchEvent(new Event('notifications-updated'))
    } catch (err) {
      setError(err.message)
    } finally {
      setBusy(null)
    }
  }
  return (
    <div className="container section narrow">
      <div className="section-heading">
        <div>
          <span className="eyebrow">KHÔNG BỎ LỠ CẬP NHẬT</span>
          <h1>Thông báo</h1>
          <p className="muted">
            Kết quả ứng tuyển và những cập nhật dành cho bạn.
          </p>
        </div>
        <button className="btn secondary" onClick={load}>
          ↻ Làm mới
        </button>
      </div>
      <Feedback error={error} />
      {loading ? (
        <Loading />
      ) : page?.content?.length ? (
        <div className="notification-list">
          {page.content.map((n) => (
            <article
              className={
                'notification-card ' + (n.status === 'UNREAD' ? 'unread' : '')
              }
              key={n.id}
            >
              <span className="notification-symbol">↗</span>
              <div>
                <p>{n.message}</p>
                <small>
                  {date(n.createdAt)} ·{' '}
                  {n.status === 'UNREAD' ? 'Chưa đọc' : 'Đã đọc'}
                </small>
                <div className="notification-actions">
                  <Link
                    to={
                      user.role === 'EMPLOYER'
                        ? '/employer/jobs'
                        : user.role === 'ADMIN'
                          ? '/admin'
                          : '/student/applications'
                    }
                  >
                    Xem đơn ứng tuyển →
                  </Link>
                  {n.status === 'UNREAD' && (
                    <button
                      className="text-button"
                      disabled={busy === n.id}
                      onClick={() => read(n.id)}
                    >
                      {busy === n.id ? 'Đang lưu…' : 'Đánh dấu đã đọc'}
                    </button>
                  )}
                </div>
              </div>
              {n.status === 'UNREAD' && (
                <span className="unread-dot" aria-label="Chưa đọc" />
              )}
            </article>
          ))}
        </div>
      ) : (
        !error && (
          <Empty title="Bạn đã xem hết cập nhật">
            Thông báo mới sẽ xuất hiện ở đây khi có kết quả ứng tuyển.
          </Empty>
        )
      )}
      <Pagination page={page} onChange={setNumber} />
    </div>
  )
}
