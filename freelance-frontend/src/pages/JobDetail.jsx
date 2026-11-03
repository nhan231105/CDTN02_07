import { useMessage } from '../context/toast'
import {
  date,
  jobTypeLabels,
  payTypeLabels,
  salaryText,
  shiftLabels,
} from '../utils/format'
import { useEffect, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import api from '../api/apiService'
import { hasApplication } from '../utils/applications'
import { useAuth } from '../context/auth'
import { Feedback, Field, Loading, Modal, Status } from '../components/UI'
import SaveButton from '../components/SaveButton'
import RichText from '../components/RichText'
import CompanyLogo from '../components/CompanyLogo'
import { brandImage } from '../utils/brand'
import useSavedJobs from '../hooks/useSavedJobs'
import useMyProfile from '../hooks/useMyProfile'
import { matchJob } from '../utils/match'
export default function JobDetail() {
  const { id } = useParams()
  const { user } = useAuth()
  const saved = useSavedJobs()
  const myProfile = useMyProfile()
  const [employer, setEmployer] = useState(null)
  const navigate = useNavigate()
  const [job, setJob] = useState(null)
  const match = matchJob(job, myProfile)
  const [error, setError] = useState('')
  const [, setMessage] = useMessage()
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
  useEffect(() => {
    if (!job?.employerId) return
    let active = true
    api
      .employerPublic(job.employerId)
      .then((data) => {
        if (active) setEmployer(data)
      })
      .catch(() => {})
    return () => {
      active = false
    }
  }, [job?.employerId])
  const [others, setOthers] = useState([])
  const [stuck, setStuck] = useState(false)
  const [now] = useState(() => Date.now())
  useEffect(() => {
    let active = true
    api
      .jobs({ size: 100 })
      .then((data) => {
        if (active) setOthers(data.content || [])
      })
      .catch(() => {})
    return () => {
      active = false
    }
  }, [])
  useEffect(() => {
    const onScroll = () => setStuck(window.scrollY > 420)
    window.addEventListener('scroll', onScroll, { passive: true })
    return () => window.removeEventListener('scroll', onScroll)
  }, [])
  const toggleSave = async () => {
    try {
      await saved.toggle(job.id)
      setError('')
    } catch (err) {
      setError(err.message)
    }
  }
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
  const daysLeft = job.deadline
    ? Math.ceil((new Date(job.deadline) - now) / 86400000)
    : null
  const deadlineText =
    daysLeft === null
      ? ''
      : daysLeft < 0
        ? 'Đã hết hạn'
        : daysLeft === 0
          ? 'Hết hạn hôm nay'
          : 'Hết hạn trong ' + daysLeft + ' ngày'
  const canApply = !user || user.role === 'STUDENT'
  const applyLabel = applied
    ? 'Đã ứng tuyển'
    : job.status !== 'OPEN'
      ? 'Đã đóng ứng tuyển'
      : 'Ứng tuyển ngay'
  const sameEmployer = others
    .filter((j) => j.employerId === job.employerId && j.id !== job.id)
    .slice(0, 4)
  const similar = others
    .filter(
      (j) =>
        j.id !== job.id &&
        j.employerId !== job.employerId &&
        j.category === job.category,
    )
    .slice(0, 5)
  const infoRows = [
    ['Ngày đăng', date(job.createdAt)],
    ['Ngành nghề', job.category],
    ['Loại hình', jobTypeLabels[job.jobType]],
    ['Hình thức trả lương', payTypeLabels[job.payType]],
    ['Thời gian làm', job.workSchedule],
    ['Số giờ mỗi tuần', job.hoursPerWeek ? job.hoursPerWeek + ' giờ' : null],
    ['Số lượng tuyển', job.headcount || 1],
    ['Ngày bắt đầu', job.startDate ? date(job.startDate) : null],
    ['Thời hạn', job.durationMonths ? job.durationMonths + ' tháng' : null],
    [
      'Kinh nghiệm',
      job.noExperienceRequired ? 'Không yêu cầu' : 'Theo yêu cầu công việc',
    ],
  ].filter((row) => row[1])
  const fullAddress = job.address
    ? job.address + ', ' + job.location
    : job.location
  const jobLink = (j) => (
    <Link className="mini-job" to={'/jobs/' + j.id} key={j.id}>
      <strong>{j.title}</strong>
      <span>{j.employerName}</span>
      <em>{salaryText(j)}</em>
    </Link>
  )
  return (
    <div className="container section jd-page">
      <Link className="back-link" to="/jobs">
        ← Khám phá việc làm
      </Link>
      <div className="detail-layout">
        <article>
          <div className="surface job-detail-head jd-head">
            <div className="jd-head-main">
              <h1>{job.title}</h1>
              <p className="company-name">
                <Link to={'/employers/' + job.employerId}>
                  {job.employerName || 'Nhà tuyển dụng'}
                </Link>
              </p>
              <div className="jd-facts">
                <div>
                  <span>Mức lương</span>
                  <strong className="jd-salary">{salaryText(job)}</strong>
                </div>
                <div>
                  <span>Địa điểm</span>
                  <strong>{job.location}</strong>
                </div>
                <div>
                  <span>Hạn nộp</span>
                  <strong>{date(job.deadline)}</strong>
                  {deadlineText && <em>{deadlineText}</em>}
                </div>
              </div>
              <div className="job-tags">
                <span>{jobTypeLabels[job.jobType] || job.workSchedule}</span>
                <Status value={job.status} />
              </div>
              <div className="job-badges">
                {job.noExperienceRequired && (
                  <span className="badge-pill green">
                    Không cần kinh nghiệm
                  </span>
                )}
                {job.studentFriendly && (
                  <span className="badge-pill blue">Thân thiện sinh viên</span>
                )}
                {(job.shifts || []).map((s) => (
                  <span className="badge-pill" key={s}>
                    {shiftLabels[s] || s}
                  </span>
                ))}
              </div>
            </div>
            <div className="jd-actions">
              {canApply && (
                <button
                  className="btn primary"
                  disabled={
                    job.status !== 'OPEN' || applied || applicationChecking
                  }
                  onClick={start}
                >
                  {applyLabel}
                </button>
              )}
              {saved.isStudent && (
                <SaveButton
                  label
                  saved={saved.isSaved(job.id)}
                  onToggle={toggleSave}
                />
              )}
            </div>
          </div>
          <Feedback error={!open ? error : ''} />
          <div className="surface rich-content">
            <h2>Mô tả công việc</h2>
            <RichText text={job.description} />
            <h2>Yêu cầu ứng viên</h2>
            <RichText
              text={job.requirements}
              fallback="Trao đổi trực tiếp với nhà tuyển dụng."
            />
            <h2>Quyền lợi</h2>
            <RichText
              text={job.benefits}
              fallback="Môi trường làm việc và trải nghiệm thực tế."
            />
          </div>
          <section className="surface">
            <h2 className="jd-h2">Thông tin việc làm</h2>
            <dl className="jd-info">
              {infoRows.map(([k, v]) => (
                <div key={k}>
                  <dt>{k}</dt>
                  <dd>{v}</dd>
                </div>
              ))}
            </dl>
            {(job.shifts || []).length > 0 && (
              <div className="jd-chips">
                {job.shifts.map((s) => (
                  <span className="badge-pill" key={s}>
                    {shiftLabels[s] || s}
                  </span>
                ))}
              </div>
            )}
          </section>
          <section className="surface">
            <h2 className="jd-h2">Địa điểm làm việc</h2>
            <p>{fullAddress}</p>
          </section>
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
        <aside className="jd-side">
          {match && (
            <section className={'surface jd-match ' + match.level}>
              <div className="jd-match-score">
                <strong>{match.score}%</strong>
                <span>phù hợp với hồ sơ của bạn</span>
              </div>
              <div className="match-track">
                <span style={{ width: match.score + '%' }} />
              </div>
              <ul>
                {match.reasons.map((r) => (
                  <li key={r} className="ok">
                    {r}
                  </li>
                ))}
                {match.gaps.map((r) => (
                  <li key={r} className="gap">
                    {r}
                  </li>
                ))}
              </ul>
              <Link to="/student/profile">Cập nhật ca rảnh và kỹ năng</Link>
            </section>
          )}
          {user?.role === 'STUDENT' && !match && myProfile && (
            <section className="surface jd-match">
              <h3>Mức độ phù hợp</h3>
              <p className="muted">
                Điền ca rảnh, khu vực và kỹ năng để xem việc này hợp với bạn bao
                nhiêu phần trăm.
              </p>
              <Link to="/student/profile">Hoàn thiện hồ sơ</Link>
            </section>
          )}
          <section className="surface jd-company">
            <div
              className="jd-cover"
              style={
                employer?.coverRef
                  ? {
                      backgroundImage:
                        'url(' +
                        brandImage(job.employerId, 'cover', employer.coverRef) +
                        ')',
                    }
                  : undefined
              }
            />
            <CompanyLogo
              id={job.employerId}
              name={job.employerName}
              className="large jd-logo"
              version={employer?.logoRef}
            />
            <h3>
              <Link to={'/employers/' + job.employerId}>
                {employer?.name || job.employerName}
              </Link>
            </h3>
            {employer && (
              <p className="muted">
                {employer.averageRating
                  ? '★ ' +
                    employer.averageRating +
                    ' (' +
                    employer.reviewCount +
                    ' đánh giá) · '
                  : ''}
                {employer.openJobCount} việc đang tuyển
              </p>
            )}
            {employer?.industry && <p>Lĩnh vực: {employer.industry}</p>}
            {employer?.companySize && <p>Quy mô: {employer.companySize}</p>}
            {employer?.address && <p>Địa chỉ: {employer.address}</p>}
            <Link
              className="btn secondary full"
              to={'/employers/' + job.employerId}
            >
              Xem công ty
            </Link>
          </section>
          <section
            className={
              'surface jd-scam' +
              (job.riskLevel && job.riskLevel !== 'NONE' ? ' alert' : '')
            }
          >
            <h3>
              {job.riskLevel === 'HIGH'
                ? 'Cảnh báo: dấu hiệu lừa đảo'
                : job.riskLevel === 'LOW'
                  ? 'Lưu ý: có điểm cần cẩn trọng'
                  : 'Lưu ý khi ứng tuyển'}
            </h3>
            {job.riskFlags?.length > 0 && (
              <ul className="risk-flags">
                {job.riskFlags.map((f) => (
                  <li key={f}>{f}</li>
                ))}
              </ul>
            )}
            <ul>
              <li>Không chuyển tiền đặt cọc hoặc phí hồ sơ.</li>
              <li>Cẩn trọng với lời hứa lương cao, việc nhẹ.</li>
              <li>Không cung cấp mã OTP, thông tin ngân hàng.</li>
            </ul>
            {user && (
              <button
                type="button"
                className="btn secondary full"
                onClick={() => {
                  setReportOpen(true)
                  setReportError('')
                }}
              >
                Báo cáo tin tuyển dụng
              </button>
            )}
          </section>
          {sameEmployer.length > 0 && (
            <section className="surface">
              <h3>Việc làm cùng công ty</h3>
              {sameEmployer.map(jobLink)}
            </section>
          )}
          {similar.length > 0 && (
            <section className="surface">
              <h3>Việc làm tương tự</h3>
              {similar.map(jobLink)}
            </section>
          )}
        </aside>
      </div>
      {canApply && (
        <div className={'jd-sticky' + (stuck ? ' show' : '')}>
          <div className="container jd-sticky-in">
            <div>
              <strong>{job.title}</strong>
              <span>
                {salaryText(job)} · {job.location}
                {deadlineText ? ' · ' + deadlineText : ''}
              </span>
            </div>
            <button
              className="btn primary"
              disabled={job.status !== 'OPEN' || applied || applicationChecking}
              onClick={start}
            >
              {applyLabel}
            </button>
          </div>
        </div>
      )}
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
