import { useEffect, useMemo, useRef, useState } from 'react'
import { Link } from 'react-router-dom'
import api from '../api/apiService'
import { useAuth, homeFor } from '../context/auth'
import { salaryText, shiftLabels } from '../utils/format'
import JobSpotlight from '../components/JobSpotlight'
import useMyProfile from '../hooks/useMyProfile'

const DAYS = ['T2', 'T3', 'T4', 'T5', 'T6', 'T7', 'CN']
const ROWS = [
  { key: 'MORNING', label: 'Sáng', time: '7h – 11h' },
  { key: 'AFTERNOON', label: 'Chiều', time: '13h – 17h' },
  { key: 'EVENING', label: 'Tối', time: '18h – 22h' },
]

function useCountUp(target, run) {
  const [value, setValue] = useState(0)
  useEffect(() => {
    if (!run || !target) return
    let frame
    const t0 = performance.now()
    const tick = (t) => {
      const p = Math.min(1, (t - t0) / 1400)
      setValue(Math.round(target * (1 - Math.pow(1 - p, 3))))
      if (p < 1) frame = requestAnimationFrame(tick)
    }
    frame = requestAnimationFrame(tick)
    return () => cancelAnimationFrame(frame)
  }, [target, run])
  return value
}

function Stat({ n, label, run }) {
  const v = useCountUp(n, run)
  return (
    <div className="lp-stat">
      <b>{v}</b>
      <span>{label}</span>
    </div>
  )
}

export default function Landing() {
  const { user } = useAuth()
  const myProfile = useMyProfile()
  const profileLink =
    user?.role === 'STUDENT'
      ? '/student/profile'
      : user?.role === 'EMPLOYER'
        ? '/employer/profile'
        : user
          ? homeFor(user.role)
          : '/register'
  const [jobs, setJobs] = useState([])
  const [picked, setPicked] = useState(() => new Set(['0-1', '1-1', '2-2']))
  const [statsOn, setStatsOn] = useState(false)
  const statsRef = useRef(null)

  useEffect(() => {
    let active = true
    api
      .jobs({ size: 100 })
      .then((d) => active && setJobs(d.content || []))
      .catch(() => {})
    return () => {
      active = false
    }
  }, [])

  useEffect(() => {
    const el = statsRef.current
    if (!el) return
    const io = new IntersectionObserver(
      ([e]) => e.isIntersecting && setStatsOn(true),
      { threshold: 0.4 },
    )
    io.observe(el)
    return () => io.disconnect()
  }, [])

  const toggle = (key) =>
    setPicked((prev) => {
      const next = new Set(prev)
      if (next.has(key)) next.delete(key)
      else next.add(key)
      return next
    })

  const shifts = useMemo(() => {
    const s = new Set()
    picked.forEach((k) => {
      const [r, c] = k.split('-').map(Number)
      s.add(ROWS[r].key)
      if (c >= 5) s.add('WEEKEND')
    })
    return s
  }, [picked])

  const matches = useMemo(() => {
    if (!shifts.size) return []
    return jobs
      .map((j) => {
        const hit = (j.shifts || []).filter(
          (x) => shifts.has(x) || x === 'FLEXIBLE',
        )
        return { job: j, hit: hit.length }
      })
      .filter((m) => m.hit > 0)
      .sort((a, b) => b.hit - a.hit)
  }, [jobs, shifts])

  const employers = new Set(jobs.map((j) => j.employerId)).size
  const categories = [...new Set(jobs.map((j) => j.category).filter(Boolean))]
  const names = [...new Set(jobs.map((j) => j.employerName).filter(Boolean))]
  const ribbon = [...categories, ...names]
  const ribbonItems = ribbon.length
    ? ribbon
    : ['Bán hàng', 'Gia sư', 'Marketing', 'Công nghệ', 'Thiết kế']

  return (
    <div className="lp">
      <section className="lp-hero">
        <div className="lp-blob b1" />
        <div className="lp-blob b2" />
        <div className="lp-grain" />
        <div className="container lp-hero-grid">
          <div className="lp-copy">
            <span className="lp-tag">Dành riêng cho sinh viên</span>
            <h1>
              Học xong <em>ca nào</em>,<br />
              làm luôn <em>ca đó</em>.
            </h1>
            <p>
              Đừng chọn việc rồi mới xếp lịch học. Hãy tô lịch rảnh của bạn,
              CampusWork tìm những công việc part-time khớp từng buổi.
            </p>
            <div className="lp-cta">
              {user ? (
                <>
                  <Link className="lp-btn solid" to="/jobs">
                    Xem việc làm
                  </Link>
                  <Link className="lp-btn ghost" to={profileLink}>
                    Hồ sơ của tôi
                  </Link>
                </>
              ) : (
                <>
                  <Link className="lp-btn solid" to="/register">
                    Tạo hồ sơ miễn phí
                  </Link>
                  <Link className="lp-btn ghost" to="/jobs">
                    Xem việc đang tuyển
                  </Link>
                </>
              )}
            </div>
            <ul className="lp-trust">
              <li>Không thu phí sinh viên</li>
              <li>Cảnh báo tin lừa đảo</li>
              <li>Ứng tuyển một chạm</li>
            </ul>
          </div>

          <div className="lp-board" aria-label="Chọn lịch rảnh">
            <div className="lp-board-head">
              <strong>Tô lịch rảnh của bạn</strong>
              <span>Bấm vào ô để chọn buổi</span>
            </div>
            <div className="lp-table">
              <i />
              {DAYS.map((d) => (
                <b key={d}>{d}</b>
              ))}
              {ROWS.map((row, r) => (
                <div className="lp-row" key={row.key}>
                  <span className="lp-rowlabel">
                    {row.label}
                    <small>{row.time}</small>
                  </span>
                  {DAYS.map((d, c) => {
                    const key = r + '-' + c
                    return (
                      <button
                        type="button"
                        key={key}
                        className={'lp-cell' + (picked.has(key) ? ' on' : '')}
                        onClick={() => toggle(key)}
                        aria-pressed={picked.has(key)}
                        aria-label={row.label + ' ' + d}
                      />
                    )
                  })}
                </div>
              ))}
            </div>
            <div className="lp-result">
              <div className="lp-result-count">
                <b>{matches.length}</b>
                <span>việc hợp lịch của bạn</span>
              </div>
              <div className="lp-result-list">
                {myProfile?.examModeActive ? (
                  <p className="lp-exam-mode">
                    Bạn đang bật <strong>chế độ mùa thi</strong>, gợi ý việc làm
                    mới tạm được ẩn bớt. Vào hồ sơ để tắt khi ôn thi xong.
                  </p>
                ) : (
                  <>
                    {matches.slice(0, 3).map(({ job }) => (
                      <Link to={'/jobs/' + job.id} key={job.id}>
                        <strong>{job.title}</strong>
                        <span>
                          {job.employerName} · {salaryText(job)}
                        </span>
                      </Link>
                    ))}
                    {!matches.length && (
                      <p>Chọn ít nhất một buổi để xem gợi ý.</p>
                    )}
                  </>
                )}
              </div>
              {shifts.size > 0 && (
                <div className="lp-chips">
                  {[...shifts].map((s) => (
                    <span key={s}>{shiftLabels[s]}</span>
                  ))}
                </div>
              )}
            </div>
          </div>
        </div>

        <div className="container lp-spot">
          <div className="lp-spot-in">
            <div>
              <span className="lp-tag">Mới cập nhật</span>
              <h2>
                Việc <em>đang chờ</em> bạn ngay hôm nay
              </h2>
              <p>
                Những tin tuyển dụng thật, cập nhật liên tục. Rê chuột để dừng,
                bấm vào để xem chi tiết.
              </p>
            </div>
            <JobSpotlight jobs={jobs} />
          </div>
        </div>

        <div className="lp-ribbon" aria-hidden="true">
          <div className="lp-ribbon-track">
            {[...ribbonItems, ...ribbonItems].map((t, i) => (
              <span key={i}>{t}</span>
            ))}
          </div>
        </div>
      </section>

      <section className="lp-stats" ref={statsRef}>
        <div className="container lp-stats-in">
          <Stat n={jobs.length} label="việc làm đang mở" run={statsOn} />
          <Stat n={employers} label="nhà tuyển dụng" run={statsOn} />
          <Stat n={categories.length} label="ngành nghề" run={statsOn} />
          <div className="lp-stat">
            <b>0đ</b>
            <span>phí cho sinh viên</span>
          </div>
        </div>
      </section>

      <section className="lp-steps">
        <div className="container">
          <h2>
            Ba bước, <em>không vòng vo</em>
          </h2>
          <div className="lp-step-grid">
            <article>
              <span className="lp-num">01</span>
              <h3>Cho biết bạn rảnh khi nào</h3>
              <p>
                Chọn ca, khu vực và kỹ năng. Mất chưa đến hai phút và bạn không
                phải dán CV dài dòng.
              </p>
            </article>
            <article>
              <span className="lp-num">02</span>
              <h3>Nhận điểm phù hợp từng việc</h3>
              <p>
                Mỗi việc có phần trăm hợp với bạn kèm lý do cụ thể: trùng ca,
                gần nhà, đúng ngành.
              </p>
            </article>
            <article>
              <span className="lp-num">03</span>
              <h3>Ứng tuyển và theo dõi</h3>
              <p>
                Biết nhà tuyển dụng đã xem hồ sơ chưa. Có thể rút đơn bất cứ lúc
                nào.
              </p>
            </article>
          </div>
        </div>
      </section>

      <section className="lp-safe">
        <div className="container lp-safe-in">
          <div>
            <h2>
              Việc <em>thật</em>, nhà tuyển dụng <em>thật</em>.
            </h2>
            <p>
              Mọi tin đăng đều qua kiểm duyệt. Gặp tin đáng ngờ, bạn báo cáo
              ngay trên trang việc làm và quản trị viên sẽ xử lý.
            </p>
          </div>
          <ul>
            <li>
              <b>Kiểm duyệt</b>
              <span>Tin vi phạm bị gỡ kèm lý do công khai.</span>
            </li>
            <li>
              <b>Đánh giá công khai</b>
              <span>Xem sinh viên khác nói gì về nhà tuyển dụng.</span>
            </li>
            <li>
              <b>Bạn kiểm soát dữ liệu</b>
              <span>CV chỉ chia sẻ khi bạn đồng ý ứng tuyển.</span>
            </li>
          </ul>
        </div>
      </section>

      <section className="lp-final">
        <div className="container">
          <h2>
            Lịch học của bạn đã sẵn sàng.
            <br />
            <em>Việc làm thì sao?</em>
          </h2>
          <div className="lp-cta center">
            {user ? (
              <Link className="lp-btn solid" to="/jobs">
                Xem việc làm ngay
              </Link>
            ) : (
              <>
                <Link className="lp-btn solid" to="/register">
                  Bắt đầu miễn phí
                </Link>
                <Link className="lp-btn ghost" to="/login">
                  Tôi đã có tài khoản
                </Link>
              </>
            )}
          </div>
        </div>
      </section>
    </div>
  )
}
