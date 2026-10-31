import { money, date } from '../utils/format'
import { useEffect, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import api from '../api/apiService'
import { hasApplication } from '../utils/applications'
import { useAuth } from '../context/auth'
import { Feedback, Field, Loading, Modal, Status } from '../components/UI'
export default function JobDetail() {
  const { id } = useParams()
  const { user } = useAuth()
  const navigate = useNavigate()
  const [job, setJob] = useState(null)
  const [error, setError] = useState('')
  const [message, setMessage] = useState('')
  const [open, setOpen] = useState(false)
  const [busy, setBusy] = useState(false)
  const [reviews, setReviews] = useState(null)
  const [coverLetter, setCoverLetter] = useState('')
  const [consent, setConsent] = useState(false)
  const [cv, setCv] = useState(null)
  const [applied, setApplied] = useState(false)
  const [applicationChecking, setApplicationChecking] = useState(
    user?.role === 'STUDENT',
  )
  const [reportOpen, setReportOpen] = useState(false)
  const [reportReason, setReportReason] = useState('')
  const [reportBusy, setReportBusy] = useState(false)
  const [reportError, setReportError] = useState('')
  useEffect(() => {
    let active = true
    Promise.resolve().then(() => {
      if (active) {
        setJob(null)
        setError('')
      }
    })
    api
      .job(id)
      .then((data) => {
        if (active) setJob(data)
      })
      .catch((err) => {
        if (active) setError(err.message)
      })
    return () => {
      active = false
    }
  }, [id])
  useEffect(() => {
    let active = true
    Promise.resolve().then(async () => {
      if (!active) return
      setApplied(false)
      setApplicationChecking(user?.role === 'STUDENT')
      if (user?.role !== 'STUDENT') return
      try {
        const found = await hasApplication(id, api.applications)
        if (active) setApplied(found)
      } catch (err) {
        if (active) setError(err.message)
      } finally {
        if (active) setApplicationChecking(false)
      }
    })
    return () => {
      active = false
    }
  }, [user, id])
  useEffect(() => {
    if (!job?.employerId) return
    let active = true
    api
      .reviews(job.employerId)
      .then((data) => {
        if (active) setReviews(data)
      })
      .catch(() => {})
    return () => {
      active = false
    }
  }, [job?.employerId])
  const start = async () => {
    if (!user) return navigate('/login', { state: { from: '/jobs/' + id } })
    try {
      const profile = await api.profile()
      setCv(profile.data || profile)
      setError('')
      setOpen(true)
    } catch (err) {
      setError(err.message)
    }
  }
  const submit = async (e) => {
    e.preventDefault()
    if (!consent) return setError('Bạn cần đồng ý chia sẻ dữ liệu ứng tuyển.')
    setBusy(true)
    setError('')
    try {
      await api.apply(id, {
        fullName: user.fullName,
        email: user.email,
        phone: user.phone,
        coverLetter,
        consentDataUsage: consent,
        cvFileRef: cv?.cvFileRef,
      })
      setApplied(true)
      setOpen(false)
      setMessage('Ứng tuyển thành công! Theo dõi kết quả trong Đơn của tôi.')
    } catch (err) {
      setError(err.message)
    } finally {
      setBusy(false)
    }
  }
  const submitReport = async (e) => {
    e.preventDefault()
    if (!reportReason.trim())
      return setReportError('Vui lòng mô tả lý do báo cáo.')
    setReportBusy(true)
    setReportError('')
    try {
      await api.submitReport({
        targetType: 'JOB',
        targetId: job.id,
        reason: reportReason.trim(),
      })
      setReportOpen(false)
      setReportReason('')
      setMessage('Đã gửi báo cáo vi phạm tới quản trị viên.')
    } catch (err) {
      setReportError(err.message)
    } finally {
      setReportBusy(false)
    }
  }
  if (!job)
    return (
      <div className="container section">
        {error ? <Feedback error={error} /> : <Loading />}
      </div>
    )
  return (
    <div className="container section">
      <Link className="back-link" to="/jobs">
        ← Khám phá việc làm
      </Link>
      <div className="detail-layout">
        <article>
          <div className="surface job-detail-head">
            <span className="company-logo large">
              {(job.employerName || 'C').charAt(0)}
            </span>
            <div>
              <p className="company-name">
                {job.employerName || 'Nhà tuyển dụng'}
              </p>
              <h1>{job.title}</h1>
              <div className="job-tags">
                <span>⌖ {job.location}</span>
                <span>◷ {job.workSchedule}</span>
                <Status value={job.status} />
              </div>
            </div>
          </div>
          <Feedback message={message} error={!open ? error : ''} />
          <div className="surface rich-content">
            <h2>Mô tả công việc</h2>
            <p>{job.description}</p>
            <h2>Yêu cầu ứng viên</h2>
            <p>
              {job.requirements || 'Trao đổi trực tiếp với nhà tuyển dụng.'}
            </p>
            <h2>Quyền lợi</h2>
            <p>
              {job.benefits || 'Môi trường làm việc và trải nghiệm thực tế.'}
            </p>
          </div>
          {reviews?.content?.length > 0 && (
            <section className="surface employer-reviews">
              <h2>Đánh giá nhà tuyển dụng</h2>
              <p className="muted">{reviews.totalElements} đánh giá</p>
              {reviews.content.slice(0, 3).map((review) => (
                <blockquote key={review.id}>
                  <span className="review-sent">
                    {'★'.repeat(review.rating)}
                    {'☆'.repeat(5 - review.rating)}
                  </span>
                  <p>{review.content}</p>
                </blockquote>
              ))}
            </section>
          )}
        </article>
        <aside className="surface apply-panel">
          <span className="eyebrow">THÔNG TIN TUYỂN DỤNG</span>
          <div className="salary-large">{money(job.salary)}</div>
          <dl>
            <dt>Ngành nghề</dt>
            <dd>{job.category}</dd>
            <dt>Địa điểm</dt>
            <dd>{job.location}</dd>
            <dt>Thời gian</dt>
            <dd>{job.workSchedule}</dd>
            <dt>Hạn ứng tuyển</dt>
            <dd>{date(job.deadline)}</dd>
          </dl>
          {(!user || user.role === 'STUDENT') && (
            <button
              className="btn primary full"
              disabled={job.status !== 'OPEN' || applied || applicationChecking}
              onClick={start}
            >
              {applied
                ? '✓ Đã ứng tuyển'
                : job.status !== 'OPEN'
                  ? 'Đã đóng ứng tuyển'
                  : 'Ứng tuyển ngay ↗'}
            </button>
          )}
          {user?.role === 'STUDENT' && (
            <Link className="center-link" to="/student/applications">
              Xem đơn của tôi →
            </Link>
          )}
          <p className="muted small-text">
            Kiểm tra kỹ thông tin công việc. Không chuyển tiền đặt cọc khi ứng
            tuyển.
          </p>
          {user && (
            <button
              type="button"
              className="center-link report-link"
              onClick={() => {
                setReportOpen(true)
                setReportError('')
              }}
            >
              ⚑ Báo cáo tin tuyển dụng vi phạm
            </button>
          )}
        </aside>
      </div>
      {open && (
        <Modal title="Xác nhận ứng tuyển" onClose={() => setOpen(false)}>
          <p className="muted">
            {job.title} · {job.employerName}
          </p>
          <Feedback error={error} />
          <form onSubmit={submit}>
            <div className="cv-summary">
              <span>▤</span>
              <div>
                <strong>{cv?.cvOriginalName || 'Bạn chưa tải CV'}</strong>
                <p>CV hiện tại trong hồ sơ của bạn</p>
              </div>
              <Link to="/student/profile">Cập nhật</Link>
            </div>
            <Field label="Thư giới thiệu" id="coverLetter">
              <textarea
                id="coverLetter"
                maxLength={2000}
                rows={5}
                placeholder="Chia sẻ ngắn gọn lý do bạn phù hợp với công việc…"
                value={coverLetter}
                onChange={(e) => setCoverLetter(e.target.value)}
              />
            </Field>
            <label className="check-label">
              <input
                type="checkbox"
                required
                checked={consent}
                onChange={(e) => setConsent(e.target.checked)}
              />
              Tôi đồng ý chia sẻ hồ sơ và CV với nhà tuyển dụng của công việc
              này.
            </label>
            <div className="modal-actions">
              <button
                type="button"
                className="btn secondary"
                onClick={() => setOpen(false)}
              >
                Quay lại
              </button>
              <button className="btn primary" disabled={busy || !cv?.cvFileRef}>
                {busy ? 'Đang gửi…' : 'Xác nhận nộp đơn'}
              </button>
            </div>
          </form>
        </Modal>
      )}
      {reportOpen && (
        <Modal
          title="Báo cáo tin tuyển dụng vi phạm"
          onClose={() => !reportBusy && setReportOpen(false)}
        >
          <p className="muted">
            {job.title} · {job.employerName}
          </p>
          <Feedback error={reportError} />
          <form onSubmit={submitReport}>
            <Field label="Mô tả vi phạm" id="report-reason">
              <textarea
                id="report-reason"
                rows={4}
                maxLength={500}
                required
                placeholder="Mô tả nội dung vi phạm (lừa đảo, sai sự thật, thu phí trái phép…)"
                value={reportReason}
                onChange={(e) => setReportReason(e.target.value)}
              />
            </Field>
            <div className="modal-actions">
              <button
                type="button"
                className="btn secondary"
                onClick={() => setReportOpen(false)}
                disabled={reportBusy}
              >
                Hủy
              </button>
              <button className="btn primary" disabled={reportBusy}>
                {reportBusy ? 'Đang gửi…' : 'Gửi báo cáo'}
              </button>
            </div>
          </form>
        </Modal>
      )}
    </div>
  )
}
