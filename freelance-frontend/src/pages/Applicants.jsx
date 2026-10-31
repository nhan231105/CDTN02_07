import { date } from '../utils/format'
import { useCallback, useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
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
export default function Applicants() {
  const { id } = useParams()
  const [page, setPage] = useState(null)
  const [number, setNumber] = useState(0)
  const [job, setJob] = useState(null)
  const [error, setError] = useState('')
  const [message, setMessage] = useState('')
  const [loading, setLoading] = useState(true)
  const [decision, setDecision] = useState(null)
  const [busy, setBusy] = useState(false)
  const [report, setReport] = useState(null)
  const [reportReason, setReportReason] = useState('')
  const [reportBusy, setReportBusy] = useState(false)
  const [reportError, setReportError] = useState('')
  const load = useCallback(async () => {
    setLoading(true)
    try {
      const [p, j] = await Promise.all([
        api.applicants(id, number),
        api.job(id),
      ])
      setPage(p)
      setJob(j)
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }, [id, number])
  useEffect(() => {
    Promise.resolve().then(load)
  }, [load])
  const decide = async () => {
    setBusy(true)
    setError('')
    try {
      await api.decide(decision.id, decision.value)
      setDecision(null)
      setMessage('Đã cập nhật kết quả và gửi thông báo cho ứng viên.')
      await load()
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
        targetType: 'USER',
        targetId: report.studentId,
        reason: reportReason.trim(),
      })
      setReport(null)
      setReportReason('')
      setMessage('Đã gửi báo cáo vi phạm tới quản trị viên.')
    } catch (err) {
      setReportError(err.message)
    } finally {
      setReportBusy(false)
    }
  }
  return (
    <div className="container section narrow">
      <Link className="back-link" to="/employer/jobs">
        ← Tin tuyển dụng của tôi
      </Link>
      <div className="section-heading">
        <div>
          <span className="eyebrow">KẾT NỐI ỨNG VIÊN</span>
          <h1>Hồ sơ ứng tuyển</h1>
          <p className="muted">{job?.title}</p>
        </div>
        {job && <Status value={job.status} />}
      </div>
      <Feedback error={error} message={message} />
      {loading ? (
        <Loading />
      ) : page?.content?.length ? (
        <div className="stack">
          {page.content.map((app) => (
            <article className="surface application-card" key={app.id}>
              <div className="application-top">
                <div className="applicant-identity">
                  <span className="avatar large">
                    {app.fullName?.charAt(0)}
                  </span>
                  <div>
                    <h2>{app.fullName}</h2>
                    <p className="muted">
                      {app.email} · {app.phone}
                    </p>
                  </div>
                </div>
                <Status value={app.status} />
              </div>
              <p className="muted">Nộp ngày {date(app.createdAt)}</p>
              {app.coverLetter && <blockquote>{app.coverLetter}</blockquote>}
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
                  ▤ Tải CV ứng viên
                </button>
                <button
                  className="text-button report-link"
                  onClick={() => {
                    setReport(app)
                    setReportReason('')
                    setReportError('')
                  }}
                >
                  ⚑ Báo cáo ứng viên
                </button>
                {app.status === 'PENDING' && job?.status !== 'COMPLETED' && (
                  <div className="inline-actions">
                    <button
                      className="btn secondary small"
                      onClick={() => setDecision({ ...app, value: 'REJECTED' })}
                    >
                      Từ chối
                    </button>
                    {job?.status === 'OPEN' && (
                      <button
                        className="btn primary small"
                        onClick={() =>
                          setDecision({ ...app, value: 'APPROVED' })
                        }
                      >
                        Duyệt ứng viên
                      </button>
                    )}
                  </div>
                )}
              </div>
            </article>
          ))}
        </div>
      ) : (
        !error && (
          <Empty title="Chưa có ứng viên">
            Hồ sơ mới sẽ xuất hiện ở đây khi sinh viên nộp đơn.
          </Empty>
        )
      )}
      <Pagination page={page} onChange={setNumber} />
      {decision && (
        <Modal
          title={
            decision.value === 'APPROVED'
              ? 'Duyệt ứng viên'
              : 'Từ chối ứng viên'
          }
          onClose={() => setDecision(null)}
        >
          <Feedback error={error} />
          <p>
            Bạn xác nhận {decision.value === 'APPROVED' ? 'duyệt' : 'từ chối'}{' '}
            hồ sơ của <strong>{decision.fullName}</strong>?
          </p>
          <p className="muted">
            Kết quả sẽ được thông báo đến ứng viên. Quyết định chỉ thực hiện một
            lần.
          </p>
          <div className="modal-actions">
            <button className="btn secondary" onClick={() => setDecision(null)}>
              Quay lại
            </button>
            <button className="btn primary" disabled={busy} onClick={decide}>
              {busy ? 'Đang xử lý…' : 'Xác nhận quyết định'}
            </button>
          </div>
        </Modal>
      )}
      {report && (
        <Modal
          title="Báo cáo ứng viên vi phạm"
          onClose={() => !reportBusy && setReport(null)}
        >
          <p className="muted">{report.fullName}</p>
          <Feedback error={reportError} />
          <form onSubmit={submitReport}>
            <Field label="Mô tả vi phạm" id="applicant-report-reason">
              <textarea
                id="applicant-report-reason"
                rows={4}
                maxLength={500}
                required
                placeholder="Mô tả nội dung vi phạm (gian lận hồ sơ, hành vi không phù hợp…)"
                value={reportReason}
                onChange={(e) => setReportReason(e.target.value)}
              />
            </Field>
            <div className="modal-actions">
              <button
                type="button"
                className="btn secondary"
                onClick={() => setReport(null)}
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
