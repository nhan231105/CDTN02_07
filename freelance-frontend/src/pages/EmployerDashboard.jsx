import { money, date } from '../utils/format'
import { useCallback, useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import api from '../api/apiService'
import { useAuth } from '../context/auth'
import {
  Empty,
  Feedback,
  Field,
  Loading,
  Modal,
  Pagination,
  Status,
} from '../components/UI'
const blank = {
  title: '',
  description: '',
  requirements: '',
  benefits: '',
  category: 'Development',
  location: '',
  workSchedule: 'Part-time',
  salary: '',
  deadline: '',
}
export default function EmployerDashboard() {
  const { user } = useAuth()
  const [today] = useState(() => new Date().toISOString().slice(0, 10))
  const [page, setPage] = useState(null)
  const [number, setNumber] = useState(0)
  const [loading, setLoading] = useState(true)
  const [edit, setEdit] = useState(null)
  const [form, setForm] = useState(blank)
  const [error, setError] = useState('')
  const [message, setMessage] = useState('')
  const [busy, setBusy] = useState(false)
  const [transition, setTransition] = useState(null)
  const load = useCallback(async () => {
    setLoading(true)
    try {
      setPage(await api.employerJobs(user.id, number))
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }, [number, user.id])
  useEffect(() => {
    Promise.resolve().then(load)
  }, [load])
  const start = (job) => {
    setError('')
    setEdit(job || {})
    setForm(
      job
        ? Object.fromEntries(Object.keys(blank).map((k) => [k, job[k] ?? '']))
        : { ...blank },
    )
  }
  const save = async (e) => {
    e.preventDefault()
    setBusy(true)
    setError('')
    try {
      const data = { ...form, salary: Number(form.salary) }
      if (edit.id) await api.updateJob(edit.id, data)
      else await api.createJob(data)
      setEdit(null)
      setMessage('Tin tuyển dụng đã được lưu.')
      await load()
    } catch (err) {
      setError(err.message)
    } finally {
      setBusy(false)
    }
  }
  const changeStatus = async () => {
    setBusy(true)
    setError('')
    try {
      await api.status(transition.id, transition.next)
      setTransition(null)
      setMessage('Đã cập nhật trạng thái công việc.')
      await load()
    } catch (err) {
      setError(err.message)
    } finally {
      setBusy(false)
    }
  }
  return (
    <div className="container section">
      <div className="section-heading">
        <div>
          <span className="eyebrow">KHÔNG GIAN NHÀ TUYỂN DỤNG</span>
          <h1>Tìm đồng đội phù hợp</h1>
          <p className="muted">
            Quản lý tin, kết nối ứng viên và theo dõi công việc.
          </p>
        </div>
        <button className="btn primary" onClick={() => start(null)}>
          + Đăng tin mới
        </button>
      </div>
      <Feedback error={!edit ? error : ''} message={message} />
      {loading ? (
        <Loading />
      ) : page?.content?.length ? (
        <div className="stack">
          {page.content.map((job) => (
            <article className="surface employer-job" key={job.id}>
              <div className="application-top">
                <div>
                  <p className="company-name">
                    {job.category} · {job.location}
                  </p>
                  <h2>{job.title}</h2>
                </div>
                <Status value={job.status} />
              </div>
              <div className="application-meta">
                <span>{money(job.salary)}</span>
                <span>◷ {job.workSchedule}</span>
                <span>Hạn: {date(job.deadline)}</span>
              </div>
              <div className="card-actions">
                <Link
                  className="btn primary small"
                  to={'/employer/jobs/' + job.id + '/applications'}
                >
                  Xem ứng viên →
                </Link>
                <div className="inline-actions">
                  {job.status === 'OPEN' && (
                    <button
                      className="btn secondary small"
                      onClick={() => start(job)}
                    >
                      Sửa tin
                    </button>
                  )}
                  {job.status !== 'COMPLETED' && (
                    <button
                      className="btn secondary small"
                      onClick={() => {
                        setError('')
                        setTransition({
                          ...job,
                          next: job.status === 'OPEN' ? 'FILLED' : 'COMPLETED',
                        })
                      }}
                    >
                      {job.status === 'OPEN'
                        ? 'Đã nhận người'
                        : 'Hoàn thành công việc'}
                    </button>
                  )}
                </div>
              </div>
            </article>
          ))}
        </div>
      ) : (
        !error && (
          <Empty title="Tin tuyển dụng đầu tiên của bạn">
            Đăng một cơ hội và gặp gỡ các ứng viên tiềm năng.
          </Empty>
        )
      )}
      <Pagination page={page} onChange={setNumber} />
      {edit && (
        <Modal
          title={edit.id ? 'Chỉnh sửa tin tuyển dụng' : 'Đăng tin tuyển dụng'}
          onClose={() => setEdit(null)}
        >
          <Feedback error={error} />
          <form onSubmit={save}>
            <Field id="job-title" label="Tiêu đề công việc">
              <input
                id="job-title"
                required
                minLength={5}
                maxLength={200}
                value={form.title}
                onChange={(e) => setForm({ ...form, title: e.target.value })}
              />
            </Field>
            <div className="form-grid">
              <Field id="job-category" label="Ngành nghề">
                <select
                  id="job-category"
                  value={form.category}
                  onChange={(e) =>
                    setForm({ ...form, category: e.target.value })
                  }
                >
                  {[
                    'Development',
                    'Design',
                    'Marketing',
                    'Education',
                    'Hospitality',
                    'Sales',
                  ].map((x) => (
                    <option key={x}>{x}</option>
                  ))}
                </select>
              </Field>
              <Field id="job-location" label="Địa điểm">
                <input
                  id="job-location"
                  required
                  maxLength={100}
                  value={form.location}
                  onChange={(e) =>
                    setForm({ ...form, location: e.target.value })
                  }
                />
              </Field>
            </div>
            <div className="form-grid">
              <Field id="job-salary" label="Mức lương (VNĐ)">
                <input
                  id="job-salary"
                  type="number"
                  min="0"
                  required
                  value={form.salary}
                  onChange={(e) => setForm({ ...form, salary: e.target.value })}
                />
              </Field>
              <Field id="job-deadline" label="Hạn ứng tuyển">
                <input
                  id="job-deadline"
                  type="date"
                  required
                  min={today}
                  value={form.deadline}
                  onChange={(e) =>
                    setForm({ ...form, deadline: e.target.value })
                  }
                />
              </Field>
            </div>
            <Field id="job-schedule" label="Lịch làm việc">
              <input
                id="job-schedule"
                required
                maxLength={200}
                value={form.workSchedule}
                onChange={(e) =>
                  setForm({ ...form, workSchedule: e.target.value })
                }
              />
            </Field>
            <Field id="job-description" label="Mô tả công việc">
              <textarea
                id="job-description"
                required
                minLength={20}
                maxLength={5000}
                rows={4}
                value={form.description}
                onChange={(e) =>
                  setForm({ ...form, description: e.target.value })
                }
              />
            </Field>
            <Field id="job-requirements" label="Yêu cầu">
              <textarea
                id="job-requirements"
                maxLength={2000}
                rows={3}
                value={form.requirements}
                onChange={(e) =>
                  setForm({ ...form, requirements: e.target.value })
                }
              />
            </Field>
            <Field id="job-benefits" label="Quyền lợi">
              <textarea
                id="job-benefits"
                maxLength={1000}
                rows={3}
                value={form.benefits}
                onChange={(e) => setForm({ ...form, benefits: e.target.value })}
              />
            </Field>
            <div className="modal-actions">
              <button
                type="button"
                className="btn secondary"
                onClick={() => setEdit(null)}
              >
                Hủy
              </button>
              <button className="btn primary" disabled={busy}>
                {busy ? 'Đang lưu…' : 'Lưu tin tuyển dụng'}
              </button>
            </div>
          </form>
        </Modal>
      )}
      {transition && (
        <Modal
          title="Cập nhật trạng thái công việc"
          onClose={() => setTransition(null)}
        >
          <Feedback error={error} />
          <p>
            <strong>{transition.title}</strong>
          </p>
          <p className="muted">
            {transition.next === 'FILLED'
              ? 'Xác nhận đã tuyển đủ ứng viên? Tin sẽ ngừng nhận đơn mới. Cần ít nhất một ứng viên đã được duyệt.'
              : 'Xác nhận công việc đã hoàn thành? Sinh viên được duyệt có thể đánh giá trải nghiệm.'}
          </p>
          <div className="modal-actions">
            <button
              className="btn secondary"
              onClick={() => setTransition(null)}
            >
              Quay lại
            </button>
            <button
              className="btn primary"
              disabled={busy}
              onClick={changeStatus}
            >
              {busy ? 'Đang cập nhật…' : 'Xác nhận trạng thái'}
            </button>
          </div>
        </Modal>
      )}
    </div>
  )
}
