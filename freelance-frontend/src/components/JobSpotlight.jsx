import { useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import { jobTypeLabels, salaryText, shiftLabels } from '../utils/format'

const SLIDE_MS = 5000

// Băng chuyền việc làm nổi bật: tự chuyển sau vài giây, dừng khi rê chuột.
export default function JobSpotlight({ jobs, limit = 6 }) {
  const slides = useMemo(() => {
    const open = (jobs || []).filter((j) => !j.status || j.status === 'OPEN')
    const pool = open.length ? open : jobs || []
    return pool.slice(0, limit)
  }, [jobs, limit])
  const [index, setIndex] = useState(0)
  const [paused, setPaused] = useState(false)
  const count = slides.length
  if (!count) {
    return (
      <div className="spot spot-empty" aria-hidden="true">
        <div className="spot-skel" />
        <div className="spot-skel short" />
      </div>
    )
  }
  const current = index % count
  const go = (i) => setIndex((i + count) % count)

  return (
    <section
      className="spot"
      aria-roledescription="carousel"
      aria-label="Việc làm nổi bật"
      onMouseEnter={() => setPaused(true)}
      onMouseLeave={() => setPaused(false)}
      onFocus={() => setPaused(true)}
      onBlur={() => setPaused(false)}
    >
      <div className="spot-head">
        <span className="spot-live">
          <i /> Đang tuyển nổi bật
        </span>
        <span className="spot-count">
          {String(current + 1).padStart(2, '0')}
          <small> / {String(count).padStart(2, '0')}</small>
        </span>
      </div>

      <div className="spot-stage">
        {slides.map((job, i) => {
          const state =
            i === current
              ? 'active'
              : i === (current + count - 1) % count
                ? 'prev'
                : 'next'
          const shifts = (job.shifts || [])
            .slice(0, 3)
            .map((s) => shiftLabels[s] || s)
          return (
            <Link
              key={job.id}
              to={'/jobs/' + job.id}
              className={'spot-slide ' + state}
              tabIndex={state === 'active' ? 0 : -1}
              aria-hidden={state !== 'active'}
            >
              <span className={'spot-logo color-' + (i % 4)}>
                {(job.employerName || job.category || 'C').charAt(0)}
              </span>
              <span className="spot-company">
                {job.employerName || 'Nhà tuyển dụng'}
              </span>
              <strong className="spot-title">{job.title}</strong>
              <span className="spot-salary">{salaryText(job)}</span>
              <span className="spot-tags">
                <em>{job.location || 'Linh hoạt'}</em>
                <em>{jobTypeLabels[job.jobType] || job.workSchedule}</em>
                {shifts.map((s) => (
                  <em key={s}>{s}</em>
                ))}
              </span>
              <span className="spot-go">Xem chi tiết →</span>
            </Link>
          )
        })}
      </div>

      <div className="spot-foot">
        <div className="spot-bars" role="tablist" aria-label="Chọn việc làm">
          {slides.map((job, i) => (
            <button
              key={job.id}
              type="button"
              role="tab"
              aria-selected={i === current}
              aria-label={'Việc ' + (i + 1) + ': ' + job.title}
              className={'spot-bar' + (i < current ? ' done' : '')}
              onClick={() => go(i)}
            >
              <span
                className={i === current ? 'fill run' : 'fill'}
                style={{
                  animationDuration: SLIDE_MS + 'ms',
                  animationPlayState: paused ? 'paused' : 'running',
                }}
                onAnimationEnd={() => go(current + 1)}
              />
            </button>
          ))}
        </div>
        <div className="spot-nav">
          <button
            type="button"
            onClick={() => go(current - 1)}
            aria-label="Việc trước"
          >
            ‹
          </button>
          <button
            type="button"
            onClick={() => go(current + 1)}
            aria-label="Việc kế tiếp"
          >
            ›
          </button>
        </div>
      </div>
    </section>
  )
}
