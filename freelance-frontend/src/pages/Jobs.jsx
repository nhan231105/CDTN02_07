import { money } from '../utils/format'
import { useEffect, useState } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import api from '../api/apiService'
import { Empty, Feedback, Loading, Pagination, Status } from '../components/UI'
const keys = [
  'keyword',
  'category',
  'location',
  'minSalary',
  'maxSalary',
  'workSchedule',
]
export default function Jobs() {
  const [params, setParams] = useSearchParams()
  const [draft, setDraft] = useState(
    Object.fromEntries(keys.map((k) => [k, params.get(k) || ''])),
  )
  const [page, setPage] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [retry, setRetry] = useState(0)
  const [filtersOpen, setFiltersOpen] = useState(false)
  const query = params.toString()
  useEffect(() => {
    const controller = new AbortController()
    Promise.resolve().then(() => {
      if (!controller.signal.aborted) {
        setLoading(true)
        setError('')
      }
    })
    Promise.resolve().then(() => {
      if (!controller.signal.aborted)
        setDraft(
          Object.fromEntries(
            keys.map((k) => [k, new URLSearchParams(query).get(k) || '']),
          ),
        )
    })
    api
      .jobs(
        { ...Object.fromEntries(new URLSearchParams(query)), size: 12 },
        controller.signal,
      )
      .then(setPage)
      .catch((err) => {
        if (err.name !== 'AbortError') setError(err.message)
      })
      .finally(() => {
        if (!controller.signal.aborted) setLoading(false)
      })
    return () => controller.abort()
  }, [query, retry])
  const filter = (e) => {
    e.preventDefault()
    setParams(
      Object.fromEntries(Object.entries(draft).filter(([, v]) => v !== '')),
    )
    setFiltersOpen(false)
  }
  const changePage = (number) => {
    const next = new URLSearchParams(params)
    next.set('page', number)
    setParams(next)
    document
      .getElementById('job-results')
      ?.scrollIntoView({ behavior: 'smooth' })
  }
  const update = (e) => setDraft({ ...draft, [e.target.name]: e.target.value })
  return (
    <>
      <section className="hero">
        <div className="container hero-inner">
          <div className="hero-copy">
            <span className="eyebrow">
              <span className="live-dot" /> CƠ HỘI THẬT · TRẢI NGHIỆM THẬT
            </span>
            <h1>
              Việc làm phù hợp.
              <br />
              <span>Tương lai rộng mở.</span>
            </h1>
            <p>
              Tìm công việc linh hoạt dành cho sinh viên.
              <br className="desktop-only" /> Bắt đầu từ một cơ hội nhỏ, tạo nên
              hành trình lớn.
            </p>
            <form className="hero-search" onSubmit={filter}>
              <span aria-hidden="true">⌕</span>
              <label className="sr-only" htmlFor="keyword">
                Từ khóa tìm kiếm
              </label>
              <input
                id="keyword"
                name="keyword"
                placeholder="Vị trí hoặc kỹ năng…"
                value={draft.keyword}
                onChange={update}
              />
              <button className="btn primary">Tìm việc ↗</button>
            </form>
            <div className="hero-topics">
              <span>Khám phá:</span>
              {['Development', 'Design', 'Marketing'].map((topic) => (
                <button
                  key={topic}
                  onClick={() => setParams({ category: topic })}
                >
                  {topic}
                </button>
              ))}
            </div>
          </div>
          <div className="hero-art" aria-hidden="true">
            <div className="art-circle" />
            <div className="art-grid" />
            <div className="floating-label top">
              ✦ Chọn điều phù hợp với bạn
            </div>
            <div className="feature-card">
              <div className="feature-heading">
                <span className="company-logo">↗</span>
                <span className="status">Bước khởi đầu mới</span>
              </div>
              <h3>
                Tiềm năng của bạn.
                <br />
                Cơ hội của ngày mai.
              </h3>
              <div className="feature-line" />
              <div className="feature-meta">
                <span>◷ Lịch làm linh hoạt</span>
                <span>◎ Kinh nghiệm thực tế</span>
              </div>
              <div className="feature-bottom">
                <span>Học hỏi. Kết nối. Phát triển.</span>
                <span>↗</span>
              </div>
            </div>
            <div className="floating-label bottom">
              <span className="mini-avatar">✓</span> Sẵn sàng cho bước tiến tiếp
              theo
            </div>
          </div>
        </div>
      </section>
      <section className="container section" id="job-results">
        <div className="section-heading">
          <div>
            <span className="eyebrow">KHÁM PHÁ CƠ HỘI</span>
            <h2>Tìm công việc dành cho bạn</h2>
            <p className="muted">
              {loading
                ? 'Đang tìm cơ hội phù hợp…'
                : (page?.totalElements || 0) + ' công việc đang mở'}
            </p>
          </div>
          <button
            className="btn secondary mobile-filter"
            onClick={() => setFiltersOpen(!filtersOpen)}
          >
            ☷ Bộ lọc
          </button>
        </div>
        <div className="jobs-layout">
          <aside className={'filter-panel ' + (filtersOpen ? 'is-open' : '')}>
            <div className="filter-heading">
              <h3>Bộ lọc tìm kiếm</h3>
              <button
                className="text-button"
                onClick={() => {
                  setParams({})
                  setDraft(Object.fromEntries(keys.map((k) => [k, ''])))
                }}
              >
                Xóa lọc
              </button>
            </div>
            <form onSubmit={filter}>
              <label htmlFor="category">Ngành nghề</label>
              <select
                id="category"
                name="category"
                value={draft.category}
                onChange={update}
              >
                <option value="">Tất cả ngành nghề</option>
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
              <label htmlFor="location">Khu vực</label>
              <input
                id="location"
                name="location"
                placeholder="VD: Hồ Chí Minh"
                value={draft.location}
                onChange={update}
              />
              <label htmlFor="minSalary">Mức lương (VNĐ)</label>
              <div className="salary-range">
                <input
                  id="minSalary"
                  name="minSalary"
                  type="number"
                  min="0"
                  placeholder="Từ"
                  value={draft.minSalary}
                  onChange={update}
                />
                <input
                  aria-label="Lương tối đa"
                  name="maxSalary"
                  type="number"
                  min="0"
                  placeholder="Đến"
                  value={draft.maxSalary}
                  onChange={update}
                />
              </div>
              <label htmlFor="workSchedule">Thời gian làm việc</label>
              <input
                id="workSchedule"
                name="workSchedule"
                placeholder="VD: Part-time"
                value={draft.workSchedule}
                onChange={update}
              />
              <button className="btn primary full">Áp dụng bộ lọc</button>
            </form>
            <div className="filter-tip">
              <span>✦</span>
              <strong>Cơ hội tốt bắt đầu từ hồ sơ tốt.</strong>
              <p>Thêm CV để sẵn sàng khi tìm thấy công việc phù hợp.</p>
              <Link to="/student/profile">Hoàn thiện hồ sơ →</Link>
            </div>
          </aside>
          <div className="job-results">
            <Feedback error={error} />
            {error && (
              <button
                className="btn secondary"
                onClick={() => setRetry(retry + 1)}
              >
                Thử lại
              </button>
            )}
            {loading ? (
              <Loading />
            ) : (
              !error && (
                <>
                  {page?.content?.length ? (
                    <div className="job-grid">
                      {page.content.map((job, index) => (
                        <Link
                          className="job-card"
                          to={'/jobs/' + job.id}
                          key={job.id}
                        >
                          <div className="job-card-top">
                            <span
                              className={'company-logo color-' + (index % 4)}
                            >
                              {(job.employerName || job.category || 'C').charAt(
                                0,
                              )}
                            </span>
                            <Status value={job.status} />
                          </div>
                          <p className="company-name">
                            {job.employerName || 'Nhà tuyển dụng'}
                          </p>
                          <h3>{job.title}</h3>
                          <div className="job-tags">
                            <span>⌖ {job.location || 'Linh hoạt'}</span>
                            <span>◷ {job.workSchedule || 'Thỏa thuận'}</span>
                          </div>
                          <div className="job-card-bottom">
                            <strong>{money(job.salary)}</strong>
                            <span className="arrow-circle">↗</span>
                          </div>
                        </Link>
                      ))}
                    </div>
                  ) : (
                    <Empty title="Chưa tìm thấy công việc phù hợp">
                      Thử thay đổi từ khóa hoặc mở rộng bộ lọc của bạn.
                    </Empty>
                  )}
                  <Pagination page={page} onChange={changePage} />
                </>
              )
            )}
          </div>
        </div>
      </section>
    </>
  )
}
