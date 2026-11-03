import { useCallback, useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import api from '../api/apiService'
import { request } from '../api/apiService'
import { useAuth } from '../context/auth'
import { useMessage } from '../context/toast'
import { datetime, shiftLabels } from '../utils/format'
import { Empty, Feedback, Loading, Pagination } from '../components/UI'

function Avatar({ person }) {
  const [url, setUrl] = useState('')
  useEffect(() => {
    if (!person.hasAvatar) return
    let active = true
    let objectUrl
    request('GET', '/talent/' + person.id + '/avatar', null, null, {
      blob: true,
    })
      .then((file) => {
        objectUrl = URL.createObjectURL(file)
        if (active) setUrl(objectUrl)
      })
      .catch(() => {})
    return () => {
      active = false
      if (objectUrl) URL.revokeObjectURL(objectUrl)
    }
  }, [person.id, person.hasAvatar])
  return (
    <span className="talent-avatar">
      {url ? <img src={url} alt="" /> : person.name.charAt(0)}
    </span>
  )
}

const inviteText = {
  PENDING: 'Đang chờ',
  ACCEPTED: 'Đã nhận lời',
  DECLINED: 'Đã từ chối',
}

export default function Talent() {
  const { user } = useAuth()
  const [, setMessage] = useMessage()
  const [tab, setTab] = useState('pool')
  const [filters, setFilters] = useState({ keyword: '', shift: '', area: '' })
  const [applied, setApplied] = useState(filters)
  const [number, setNumber] = useState(0)
  const [page, setPage] = useState(null)
  const [sent, setSent] = useState(null)
  const [sentNumber, setSentNumber] = useState(0)
  const [error, setError] = useState('')
  const [open, setOpen] = useState(null)
  const [detail, setDetail] = useState(null)
  const [jobs, setJobs] = useState([])
  const [jobId, setJobId] = useState('')
  const [note, setNote] = useState('')
  const [busy, setBusy] = useState(false)

  useEffect(() => {
    const t = setTimeout(() => {
      setApplied(filters)
      setNumber(0)
    }, 350)
    return () => clearTimeout(t)
  }, [filters])

  useEffect(() => {
    let active = true
    api
      .talent({ ...applied, page: number })
      .then((p) => {
        if (active) {
          setPage(p)
          setError('')
        }
      })
      .catch((err) => active && setError(err.message))
    return () => {
      active = false
    }
  }, [applied, number])

  const loadSent = useCallback(async () => {
    try {
      setSent(await api.sentInvitations(sentNumber))
    } catch (err) {
      setError(err.message)
    }
  }, [sentNumber])
  useEffect(() => {
    if (tab === 'sent') Promise.resolve().then(loadSent)
  }, [tab, loadSent])

  useEffect(() => {
    api
      .employerJobs(user.id, 0)
      .then((p) =>
        setJobs((p.content || []).filter((j) => j.status === 'OPEN')),
      )
      .catch(() => {})
  }, [user.id])

  const show = async (person) => {
    setOpen(person)
    setDetail(null)
    setNote('')
    setError('')
    try {
      const d = await api.talentDetail(person.id)
      setDetail(d)
      const free = jobs.find((j) => !d.invitedJobIds.includes(j.id))
      setJobId(free ? String(free.id) : '')
    } catch (err) {
      setError(err.message)
    }
  }

  const send = async (e) => {
    e.preventDefault()
    setBusy(true)
    setError('')
    try {
      await api.invite({
        studentId: open.id,
        jobId: Number(jobId),
        message: note.trim() || null,
      })
      setMessage('Đã gửi lời mời đến ' + open.name + '.')
      setOpen(null)
    } catch (err) {
      setError(err.message)
    } finally {
      setBusy(false)
    }
  }

  const available = detail
    ? jobs.filter((j) => !detail.invitedJobIds.includes(j.id))
    : []

  return (
    <div className="container section talent-page">
      <div className="section-heading">
        <div>
          <span className="eyebrow">CHỦ ĐỘNG TÌM NGƯỜI PHÙ HỢP</span>
          <h1>Kho ứng viên</h1>
          <p className="muted">
            Sinh viên đang bật chế độ tìm việc. Thông tin liên hệ chỉ hiển thị
            khi họ ứng tuyển.
          </p>
        </div>
        <div className="seg">
          <button
            className={tab === 'pool' ? 'on' : ''}
            onClick={() => setTab('pool')}
          >
            Ứng viên
          </button>
          <button
            className={tab === 'sent' ? 'on' : ''}
            onClick={() => setTab('sent')}
          >
            Lời mời đã gửi
          </button>
        </div>
      </div>
      <Feedback error={!open ? error : ''} />

      {tab === 'pool' && (
        <>
          <div className="talent-filter">
            <input
              aria-label="Tìm theo kỹ năng, ngành, trường"
              placeholder="Kỹ năng, ngành, trường…"
              value={filters.keyword}
              onChange={(e) =>
                setFilters({ ...filters, keyword: e.target.value })
              }
            />
            <input
              aria-label="Khu vực"
              placeholder="Khu vực"
              value={filters.area}
              onChange={(e) => setFilters({ ...filters, area: e.target.value })}
            />
            <div className="shift-pills">
              {Object.entries(shiftLabels).map(([k, v]) => (
                <button
                  type="button"
                  key={k}
                  className={filters.shift === k ? 'on' : ''}
                  onClick={() =>
                    setFilters({
                      ...filters,
                      shift: filters.shift === k ? '' : k,
                    })
                  }
                >
                  {v}
                </button>
              ))}
            </div>
          </div>
          {!page && !error ? (
            <Loading />
          ) : page?.content.length === 0 ? (
            <Empty title="Chưa có ứng viên phù hợp">
              Thử bỏ bớt bộ lọc, hoặc quay lại sau khi có thêm sinh viên bật chế
              độ tìm việc.
            </Empty>
          ) : (
            <div className="talent-grid">
              {page?.content.map((p) => (
                <button
                  className="talent-card"
                  key={p.id}
                  onClick={() => show(p)}
                >
                  <div className="talent-head">
                    <Avatar person={p} />
                    <div>
                      <strong>{p.name}</strong>
                      <span>
                        {[p.major, p.school].filter(Boolean).join(' · ') ||
                          'Sinh viên'}
                        {p.studyYear ? ' · Năm ' + p.studyYear : ''}
                      </span>
                    </div>
                  </div>
                  {p.bio && <p>{p.bio}</p>}
                  <div className="talent-tags">
                    {p.skills.slice(0, 4).map((s) => (
                      <span key={s}>{s}</span>
                    ))}
                  </div>
                  <div className="talent-foot">
                    <span>
                      {p.availableShifts
                        .map((s) => shiftLabels[s] || s)
                        .join(', ')}
                    </span>
                    {p.preferredArea && <span>{p.preferredArea}</span>}
                  </div>
                </button>
              ))}
            </div>
          )}
          <Pagination page={page} onChange={setNumber} />
        </>
      )}

      {tab === 'sent' &&
        (!sent ? (
          <Loading />
        ) : sent.content.length === 0 ? (
          <Empty title="Bạn chưa gửi lời mời nào">
            Chọn một ứng viên trong kho để mời họ ứng tuyển.
          </Empty>
        ) : (
          <>
            <div className="invite-list">
              {sent.content.map((i) => (
                <article className="invite-card" key={i.id}>
                  <div className="invite-body">
                    <div className="invite-top">
                      <strong>{i.studentName}</strong>
                      <span
                        className={'invite-state s-' + i.status.toLowerCase()}
                      >
                        {inviteText[i.status]}
                      </span>
                    </div>
                    <Link className="invite-job" to={'/jobs/' + i.jobId}>
                      {i.jobTitle}
                    </Link>
                    <span className="muted small">{datetime(i.createdAt)}</span>
                  </div>
                </article>
              ))}
            </div>
            <Pagination page={sent} onChange={setSentNumber} />
          </>
        ))}

      {open && (
        <div className="talent-drawer-wrap" onClick={() => setOpen(null)}>
          <aside
            className="talent-drawer"
            role="dialog"
            aria-label={'Hồ sơ ' + open.name}
            onClick={(e) => e.stopPropagation()}
          >
            <button
              className="icon-button drawer-x"
              aria-label="Đóng"
              onClick={() => setOpen(null)}
            >
              ×
            </button>
            <div className="talent-head big">
              <Avatar person={open} />
              <div>
                <h2>{open.name}</h2>
                <span>
                  {[open.major, open.school].filter(Boolean).join(' · ')}
                </span>
              </div>
            </div>
            {!detail ? (
              <Loading />
            ) : (
              <>
                {detail.bio && <p className="talent-bio">{detail.bio}</p>}
                <h4>Kỹ năng</h4>
                <div className="talent-tags">
                  {detail.skills.map((s) => (
                    <span key={s}>{s}</span>
                  ))}
                </div>
                <h4>Có thể làm</h4>
                <div className="talent-tags">
                  {detail.availableShifts.map((s) => (
                    <span key={s}>{shiftLabels[s] || s}</span>
                  ))}
                </div>
                <form className="invite-form" onSubmit={send}>
                  <h4>Mời ứng tuyển</h4>
                  {available.length === 0 ? (
                    <p className="muted">
                      {jobs.length === 0
                        ? 'Bạn chưa có tin đang tuyển để mời.'
                        : 'Bạn đã mời ứng viên này vào tất cả tin đang tuyển.'}
                    </p>
                  ) : (
                    <>
                      <select
                        aria-label="Chọn tin tuyển dụng"
                        value={jobId}
                        onChange={(e) => setJobId(e.target.value)}
                      >
                        {available.map((j) => (
                          <option key={j.id} value={j.id}>
                            {j.title}
                          </option>
                        ))}
                      </select>
                      <textarea
                        rows={3}
                        maxLength={500}
                        placeholder="Lời nhắn (không bắt buộc)"
                        value={note}
                        onChange={(e) => setNote(e.target.value)}
                      />
                      <Feedback error={error} />
                      <button className="btn primary" disabled={busy || !jobId}>
                        {busy ? 'Đang gửi…' : 'Gửi lời mời'}
                      </button>
                    </>
                  )}
                </form>
              </>
            )}
          </aside>
        </div>
      )}
    </div>
  )
}
