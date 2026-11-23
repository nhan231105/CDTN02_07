import { useCallback, useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import api from '../api/apiService'
import { useAuth } from '../context/auth'
import { Empty, Feedback, Loading } from '../components/UI'
import { datetime } from '../utils/format'

export default function Conversation() {
  const { id } = useParams()
  return <Thread key={id} id={id} />
}

function Thread({ id }) {
  const { user } = useAuth()
  const [thread, setThread] = useState(null)
  const [older, setOlder] = useState([])
  const [page, setPage] = useState(0)
  const [hasOlder, setHasOlder] = useState(null)
  const [text, setText] = useState('')
  const [sending, setSending] = useState(false)
  const [loadingHistory, setLoadingHistory] = useState(false)
  const [error, setError] = useState('')
  const load = useCallback(async () => {
    try {
      const latest = await api.conversation(id, 0)
      setOlder((previous) => {
        const merged = new Map(
          [...previous, ...latest.messages].map((item) => [item.id, item]),
        )
        return [...merged.values()]
      })
      setThread(latest)
      setError('')
      if (latest.unread && latest.messages[0] && !document.hidden) {
        await api.readMessages(id, latest.messages[0].id)
      }
    } catch (err) {
      setError(err.message)
    }
  }, [id])

  useEffect(() => {
    const refresh = () => {
      if (!document.hidden) load()
    }
    const initial = setTimeout(refresh, 0)
    const timer = setInterval(refresh, 10000)
    document.addEventListener('visibilitychange', refresh)
    return () => {
      clearTimeout(initial)
      clearInterval(timer)
      document.removeEventListener('visibilitychange', refresh)
    }
  }, [load])

  async function history() {
    setLoadingHistory(true)
    try {
      const next = await api.conversation(id, page + 1)
      setOlder((previous) => {
        const merged = new Map(
          [...next.messages, ...previous].map((item) => [item.id, item]),
        )
        return [...merged.values()]
      })
      setPage(page + 1)
      setHasOlder(next.hasOlder)
    } catch (err) {
      setError(err.message)
    } finally {
      setLoadingHistory(false)
    }
  }

  async function send(event) {
    event.preventDefault()
    if (!text.trim()) return
    setSending(true)
    try {
      const message = await api.sendMessage(id, text)
      setOlder((previous) =>
        previous.some((item) => item.id === message.id)
          ? previous
          : [...previous, message],
      )
      setText('')
      setError('')
    } catch (err) {
      setError(err.message)
    } finally {
      setSending(false)
    }
  }

  return (
    <div className="container section narrow">
      <Link
        className="back-link"
        to={
          user.role === 'EMPLOYER' ? '/employer/jobs' : '/student/applications'
        }
      >
        ← Quay lại ứng tuyển
      </Link>
      <h1>Trao đổi công việc</h1>
      <Feedback error={error} />
      {!thread && !error && <Loading />}
      {!thread && error && (
        <button className="btn secondary" onClick={load}>
          Thử lại
        </button>
      )}
      {thread && (
        <>
          <p>
            {thread.jobTitle} · {thread.participant}
          </p>
          {(hasOlder ?? thread.hasOlder) && (
            <button
              className="btn secondary small"
              disabled={loadingHistory}
              onClick={history}
            >
              Tải tin nhắn cũ hơn
            </button>
          )}
          {older.length ? (
            <ol
              className="conversation-messages"
              aria-label="Lịch sử hội thoại"
            >
              {[...older]
                .sort((a, b) => a.id - b.id)
                .map((item) => (
                  <li key={item.id} className={item.mine ? 'mine' : ''}>
                    <small>
                      {item.mine ? 'Bạn' : thread.participant} ·{' '}
                      {datetime(item.sentAt)}
                      {item.mine && item.readAt ? ' · Đã đọc' : ''}
                    </small>
                    <p>{item.body}</p>
                  </li>
                ))}
            </ol>
          ) : (
            <Empty title="Chưa có tin nhắn">
              Bạn có thể bắt đầu trao đổi về lịch và địa điểm làm việc.
            </Empty>
          )}
          <form className="conversation-compose" onSubmit={send}>
            <label htmlFor="message-body">Tin nhắn</label>
            <textarea
              id="message-body"
              rows={3}
              maxLength={2000}
              value={text}
              disabled={sending}
              onChange={(event) => setText(event.target.value)}
              required
            />
            <button className="btn primary" disabled={sending || !text.trim()}>
              {sending ? 'Đang gửi…' : 'Gửi tin nhắn'}
            </button>
          </form>
        </>
      )}
    </div>
  )
}
