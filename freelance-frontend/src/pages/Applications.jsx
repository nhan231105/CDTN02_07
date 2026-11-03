import { useMessage } from '../context/toast'
import { date, datetime, salaryText } from '../utils/format'
import { useCallback, useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import api, { download } from '../api/apiService'
import {
  Empty,
  Feedback,
  Field,
  Loading,
  Modal,
  Pagination,
  Status,
} from '../components/UI'
export default function Applications() {
  const [page, setPage] = useState(null)
  const [number, setNumber] = useState(0)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [, setMessage] = useMessage()
  const [selected, setSelected] = useState(null)
  const [rating, setRating] = useState(5)
  const [content, setContent] = useState('')
  const [busy, setBusy] = useState(false)
  const [filter, setFilter] = useState('')
  const [withdrawing, setWithdrawing] = useState(null)
  const load = useCallback(async () => {
    setError('')
    setLoading(true)
    try {
      setPage(await api.applications(number))
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }, [number])
  useEffect(() => {
    Promise.resolve().then(load)
  }, [load])
  const review = async (e) => {
    e.preventDefault()
    setBusy(true)
    setError('')
    try {
      await api.review(selected.id, { rating, content })
      setSelected(null)
      setMessage('Cảm ơn bạn! Đánh giá đã được gửi.')
      await load()
    } catch (err) {
      setError(err.message)
    } finally {
      setBusy(false)
    }
  }
  const withdraw = async () => {
    setBusy(true)
    setError('')
    try {
      await api.withdraw(withdrawing.id)
      setWithdrawing(null)
      setMessage('Đã rút đơn ứng tuyển.')
      await load()
    } catch (err) {
      setError(err.message)
      setWithdrawing(null)
    } finally {
      setBusy(false)
    }
  }
  const visible = (page?.content || []).filter(
    (a) => !filter || a.status === filter,
  )
  return (
    <div className="container section narrow">
      <div className="section-heading">
        <div>
          <span className="eyebrow">THEO DÕI HÀNH TRÌNH</span>
          <h1>Đơn ứng tuyển của tôi</h1>
          <p className="muted">Mỗi đơn ứng tuyển là một cơ hội mới.</p>
        </div>
        <Link className="btn primary" to="/jobs">
          Tìm việc mới
        </Link>
      </div>
      <Feedback error={!selected ? error : ''} />
      <div className="tabs" role="tablist" aria-label="Lọc theo trạng thái">
        {[
          ['', 'Tất cả'],
          ['PENDING', 'Chờ duyệt'],
          ['APPROVED', 'Đã duyệt'],
          ['REJECTED', 'Từ chối'],
          ['WITHDRAWN', 'Đã rút'],
        ].map(([value, label]) => (
          <button
            key={value}
            role="tab"
            aria-selected={filter === value}
            className={'tab' + (filter === value ? ' active' : '')}
            onClick={() => setFilter(value)}
          >
            {label}
          </button>
        ))}
      </div>
      {loading ? (
        <Loading />
      ) : visible.length ? (
        <div className="stack">
          {visible.map((app) => (
            <article className="surface application-card" key={app.id}>
              <div className="application-top">
                <div>
                  <p className="company-name">
                    {app.employerName || 'Nhà tuyển dụng'}
                  </p>
                  <h2>
                    <Link to={'/jobs/' + app.jobId}>
                      {app.jobTitle || 'Công việc #' + app.jobId}
                    </Link>
                  </h2>
                </div>
                <Status value={app.status} />
              </div>
              <div className="application-meta">
                <span>Đã nộp: {date(app.createdAt)}</span>
                <span>
                  Công việc: <Status value={app.jobStatus} />
                </span>
              </div>
              <p className="muted small-text">
                {app.jobLocation} ·{' '}
                {salaryText({ salary: app.jobSalary, payType: app.jobPayType })}
              </p>
              <ol className="timeline">
                <li className="done">Đã nộp · {datetime(app.createdAt)}</li>
                <li className={app.viewedAt ? 'done' : ''}>
                  {app.viewedAt
                    ? 'Nhà tuyển dụng đã xem · ' + datetime(app.viewedAt)
                    : 'Chờ nhà tuyển dụng xem hồ sơ'}
                </li>
                {app.status === 'WITHDRAWN' ? (
                  <li className="done">
                    Bạn đã rút đơn · {datetime(app.withdrawnAt)}
                  </li>
                ) : (
                  <li
                    className={
                      app.status === 'APPROVED'
                        ? 'done ok'
                        : app.status === 'REJECTED'
                          ? 'done bad'
                          : ''
                    }
                  >
                    {app.status === 'APPROVED'
                      ? 'Được duyệt · ' + datetime(app.decidedAt)
                      : app.status === 'REJECTED'
                        ? 'Bị từ chối · ' + datetime(app.decidedAt)
                        : 'Chờ kết quả'}
                  </li>
                )}
              </ol>
              {app.coverLetter && <p className="muted">{app.coverLetter}</p>}
              <div className="card-actions">
                <button
                  className="text-button"
                  onClick={() =>
                    download(
                      '/applications/' + app.id + '/cv',
                      app.cvOriginalName,
                    ).catch((err) => setError(err.message))
                  }
                >
                  ▤ Xem CV đã nộp
                </button>
                {app.status === 'PENDING' && (
                  <button
                    className="btn secondary small"
                    onClick={() => {
                      setError('')
                      setWithdrawing(app)
                    }}
                  >
                    Rút đơn
                  </button>
                )}
                {app.reviewSubmitted ? (
                  <span className="review-sent">★ Đã đánh giá</span>
                ) : app.status === 'APPROVED' &&
                  app.jobStatus === 'COMPLETED' ? (
                  <button
                    className="btn primary small"
                    onClick={() => {
                      setSelected(app)
                      setContent('')
                      setRating(5)
                      setError('')
                    }}
                  >
                    Đánh giá nhà tuyển dụng
                  </button>
                ) : (
                  <span className="muted small-text">
                    Đánh giá sau khi công việc hoàn thành
                  </span>
                )}
              </div>
            </article>
          ))}
        </div>
      ) : (
        !error && (
          <Empty title="Bạn chưa có đơn ứng tuyển">
            Khám phá việc làm và tìm một cơ hội phù hợp với bạn.
          </Empty>
        )
      )}
      {error && !selected && (
        <button className="btn secondary" onClick={load}>
          Thử lại
        </button>
      )}
      <Pagination page={page} onChange={setNumber} />
      {withdrawing && (
        <Modal title="Rút đơn ứng tuyển" onClose={() => setWithdrawing(null)}>
          <p>
            Bạn chắc chắn muốn rút đơn <strong>{withdrawing.jobTitle}</strong>?
            Sau khi rút, bạn không thể ứng tuyển lại tin này.
          </p>
          <div className="modal-actions">
            <button
              className="btn secondary"
              onClick={() => setWithdrawing(null)}
            >
              Quay lại
            </button>
            <button className="btn primary" disabled={busy} onClick={withdraw}>
              {busy ? 'Đang xử lý…' : 'Xác nhận rút đơn'}
            </button>
          </div>
        </Modal>
      )}
      {selected && (
        <Modal
          title="Đánh giá nhà tuyển dụng"
          onClose={() => setSelected(null)}
        >
          <p className="muted">{selected.jobTitle}</p>
          <Feedback error={error} />
          <form onSubmit={review}>
            <fieldset className="rating-field">
              <legend>Trải nghiệm của bạn</legend>
              <div className="stars">
                {[1, 2, 3, 4, 5].map((n) => (
                  <button
                    type="button"
                    key={n}
                    aria-label={n + ' sao'}
                    aria-pressed={rating === n}
                    className={n <= rating ? 'filled' : ''}
                    onClick={() => setRating(n)}
                  >
                    ★
                  </button>
                ))}
              </div>
            </fieldset>
            <Field id="review-content" label="Chia sẻ trải nghiệm">
              <textarea
                id="review-content"
                rows={5}
                maxLength={2000}
                required
                placeholder="Điều gì khiến trải nghiệm này đáng nhớ?"
                value={content}
                onChange={(e) => setContent(e.target.value)}
              />
            </Field>
            <p className="muted small-text">
              Bạn chỉ có thể đánh giá một lần cho mỗi công việc.
            </p>
            <div className="modal-actions">
              <button
                type="button"
                className="btn secondary"
                onClick={() => setSelected(null)}
              >
                Hủy
              </button>
              <button className="btn primary" disabled={busy}>
                {busy ? 'Đang gửi…' : 'Gửi đánh giá'}
              </button>
            </div>
          </form>
        </Modal>
      )}
    </div>
  )
}
