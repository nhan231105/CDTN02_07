import { jobTypeLabels, payTypeLabels, shiftLabels } from '../utils/format'
import { useEffect, useRef, useState } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import api from '../api/apiService'
import { Empty, Feedback, Loading, Pagination } from '../components/UI'
import JobCard from '../components/JobCard'
import useSavedJobs from '../hooks/useSavedJobs'
import useMyProfile from '../hooks/useMyProfile'
import { matchJob } from '../utils/match'
const keys = [
  'keyword',
  'category',
  'location',
  'minSalary',
  'maxSalary',
  'workSchedule',
  'jobType',
  'payType',
  'shift',
  'maxHoursPerWeek',
  'noExperience',
  'studentFriendly',
  'sortBy',
  'near',
  'radius',
]
const sortOptions = {
  '': 'Mới nhất',
  salary_desc: 'Lương cao → thấp',
  salary_asc: 'Lương thấp → cao',
  deadline: 'Sắp hết hạn',
}
export default function Jobs() {
  const [params, setParams] = useSearchParams()
  const saved = useSavedJobs()
  const profile = useMyProfile()
  const sortByMatch =
    params.get('sortBy') === 'match' && !!profile && !profile.examModeActive
  const [actionError, setActionError] = useState('')
  const [draft, setDraft] = useState(
    Object.fromEntries(keys.map((k) => [k, params.get(k) || ''])),
  )
  const [page, setPage] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [retry, setRetry] = useState(0)
  const [filtersOpen, setFiltersOpen] = useState(false)
  const query = params.toString()
  const [geo, setGeo] = useState(null)
  const origin =
    geo ||
    (profile?.lat != null ? { lat: profile.lat, lng: profile.lng } : null)
  const near = params.get('near') === '1'
  const originKey = origin ? origin.lat + ',' + origin.lng : ''
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
    const apiParams = {
      ...Object.fromEntries(new URLSearchParams(query)),
      size: 12,
    }
    if (apiParams.sortBy === 'match') delete apiParams.sortBy
    if (apiParams.near && !originKey) return () => controller.abort()
    const request = apiParams.near
      ? api.nearbyJobs(
          {
            lat: originKey.split(',')[0],
            lng: originKey.split(',')[1],
            radiusKm: apiParams.radius || 15,
            page: apiParams.page,
            size: 12,
          },
          controller.signal,
        )
      : api.jobs(apiParams, controller.signal)
    request
      .then(setPage)
      .catch((err) => {
        if (err.name !== 'AbortError') setError(err.message)
      })
      .finally(() => {
        if (!controller.signal.aborted) setLoading(false)
      })
    return () => controller.abort()
  }, [query, retry, originKey])
  const timer = useRef(null)
  const toggleFilters = () => {
    const next = !filtersOpen
    setFiltersOpen(next)
    if (next && window.innerWidth > 900)
      document
        .getElementById('job-results')
        ?.scrollIntoView({ behavior: 'smooth' })
  }
  const apply = (values) => {
    clearTimeout(timer.current)
    setParams(
      Object.fromEntries(Object.entries(values).filter(([, v]) => v !== '')),
    )
  }
  const filter = (e) => {
    e.preventDefault()
    apply(draft)
  }
  const clearAll = () => {
    clearTimeout(timer.current)
    setParams({})
    setDraft(Object.fromEntries(keys.map((k) => [k, ''])))
  }
  const activeCount = keys.filter(
    (k) =>
      !['keyword', 'sortBy', 'near', 'radius'].includes(k) && params.get(k),
  ).length
  const changePage = (number) => {
    const next = new URLSearchParams(params)
    next.set('page', number)
    setParams(next)
    document
      .getElementById('job-results')
      ?.scrollIntoView({ behavior: 'smooth' })
  }
  // Chọn là lọc ngay; ô gõ chữ/số chờ 0,45s sau lần gõ cuối.
  const update = (e) => {
    const { name, type, checked, value } = e.target
    const next = {
      ...draft,
      [name]: type === 'checkbox' ? (checked ? 'true' : '') : value,
    }
    setDraft(next)
    if (type === 'text' || type === 'number') {
      clearTimeout(timer.current)
      timer.current = setTimeout(() => apply(next), 450)
    } else apply(next)
  }
  const quick = (patch) => {
    const next = { ...Object.fromEntries(params), ...patch }
    delete next.page
    setParams(Object.fromEntries(Object.entries(next).filter(([, v]) => v)))
  }
  const toggleNear = () => {
    setActionError('')
    if (near) return quick({ near: '', radius: '' })
    if (origin)
      return quick({ near: '1', radius: params.get('radius') || '15' })
    if (!navigator.geolocation)
      return setActionError('Trình duyệt không hỗ trợ định vị.')
    navigator.geolocation.getCurrentPosition(
      (pos) => {
        setGeo({ lat: pos.coords.latitude, lng: pos.coords.longitude })
        quick({ near: '1', radius: '15' })
      },
      () =>
        setActionError(
          'Không lấy được vị trí. Sinh viên có thể chấm vị trí nơi ở trong hồ sơ.',
        ),
    )
  }
  const toggleSave = async (jobId) => {
    setActionError('')
    try {
      await saved.toggle(jobId)
    } catch (err) {
      setActionError(err.message)
    }
  }
  return (
    <div className="jobs-dark">
      <section className="jobs-top">
        <div className="lp-blob b1" />
        <div className="lp-blob b2" />
        <div className="lp-grain" />
        <div className="container jobs-top-in">
          <span className="lp-tag">
            {loading
              ? 'Đang tải…'
              : (page?.totalElements || 0) + ' việc đang mở'}
          </span>
          <h1>
            Tìm việc <em>hợp lịch học</em> của bạn
          </h1>
          <div className="jobs-searchrow">
            <form className="hero-search" onSubmit={filter}>
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
              <button className="btn primary">Tìm việc</button>
            </form>
            <button
              type="button"
              className={'filter-btn' + (filtersOpen ? ' on' : '')}
              onClick={toggleFilters}
              aria-expanded={filtersOpen}
              aria-controls="filter-drop"
            >
              <svg
                viewBox="0 0 24 24"
                width="16"
                height="16"
                fill="none"
                stroke="currentColor"
                strokeWidth="2"
                strokeLinecap="round"
                aria-hidden="true"
              >
                <path d="M4 6h16M7 12h10M10 18h4" />
              </svg>
              Bộ lọc
              {activeCount > 0 && <b>{activeCount}</b>}
            </button>
          </div>
          <div className="jobs-chips">
            <button className={near ? 'on' : ''} onClick={toggleNear}>
              Gần tôi
            </button>
            <button onClick={() => quick({ noExperience: 'true' })}>
              Không cần kinh nghiệm
            </button>
            <button onClick={() => quick({ shift: 'EVENING' })}>Ca tối</button>
            <button onClick={() => quick({ shift: 'WEEKEND' })}>
              Cuối tuần
            </button>
            <button onClick={() => quick({ jobType: 'SEASONAL' })}>
              Thời vụ
            </button>
            <button onClick={() => quick({ studentFriendly: 'true' })}>
              Thân thiện sinh viên
            </button>
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
        </div>
        <div className={'jobs-layout' + (filtersOpen ? ' filters-open' : '')}>
          <div className="job-results">
            <div className="sort-bar">
              <label htmlFor="sortBy">Sắp xếp</label>
              <select
                id="sortBy"
                value={params.get('sortBy') || ''}
                onChange={(e) => quick({ sortBy: e.target.value })}
              >
                {Object.entries(
                  profile && !profile.examModeActive
                    ? { match: 'Phù hợp với tôi nhất', ...sortOptions }
                    : sortOptions,
                ).map(([k, v]) => (
                  <option key={k} value={k}>
                    {v}
                  </option>
                ))}
              </select>
            </div>
            {near && (
              <div className="near-bar">
                <span>
                  Việc trong bán kính <b>{params.get('radius') || 15} km</b>
                  {geo ? ' quanh vị trí hiện tại' : ' quanh nơi ở của bạn'}
                </span>
                <input
                  type="range"
                  min="2"
                  max="50"
                  step="1"
                  aria-label="Bán kính tìm việc"
                  value={params.get('radius') || 15}
                  onChange={(e) => quick({ radius: e.target.value })}
                />
              </div>
            )}
            <Feedback error={error || actionError} />
            {profile?.examModeActive && (
              <div className="match-hint exam-hint">
                <div>
                  <strong>Đang bật chế độ mùa thi</strong>
                  <p>
                    Gợi ý việc làm mới tạm được thu gọn để bạn tập trung ôn thi.
                    Vào hồ sơ để tắt chế độ này khi thi xong.
                  </p>
                </div>
                <Link className="btn secondary" to="/student/profile">
                  Quản lý trong hồ sơ
                </Link>
              </div>
            )}
            {profile &&
              !profile.examModeActive &&
              !(
                profile.availableShifts?.length ||
                profile.preferredArea ||
                profile.skills ||
                profile.major
              ) && (
                <div className="match-hint">
                  <div>
                    <strong>Xem mức độ phù hợp của từng việc</strong>
                    <p>
                      Chọn ca rảnh, khu vực và kỹ năng trong hồ sơ, CampusWork
                      sẽ chấm điểm và xếp việc hợp với bạn lên đầu.
                    </p>
                  </div>
                  <Link className="btn primary" to="/student/profile">
                    Cập nhật hồ sơ
                  </Link>
                </div>
              )}
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
                      {(sortByMatch
                        ? [...page.content].sort(
                            (a, b) =>
                              (matchJob(b, profile)?.score ?? -1) -
                              (matchJob(a, profile)?.score ?? -1),
                          )
                        : page.content
                      ).map((job, index) => (
                        <JobCard
                          key={job.id}
                          job={job}
                          index={index}
                          match={matchJob(job, profile)}
                          saved={saved.isSaved(job.id)}
                          onToggleSave={
                            saved.isStudent ? () => toggleSave(job.id) : null
                          }
                        />
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
          <aside
            className={'filter-side' + (filtersOpen ? ' open' : '')}
            aria-hidden={!filtersOpen}
          >
            <div className="filter-drop" id="filter-drop">
              <div className="fd-head">
                <div>
                  <h3>Bộ lọc nhanh</h3>
                  <p>Chọn là kết quả đổi ngay.</p>
                </div>
                <button
                  type="button"
                  className="text-button"
                  onClick={clearAll}
                >
                  Xóa tất cả
                </button>
              </div>
              <div className="fd-grid">
                <div className="fd-field">
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
                </div>
                <div className="fd-field">
                  <label htmlFor="location">Khu vực</label>
                  <input
                    id="location"
                    name="location"
                    placeholder="VD: Hồ Chí Minh"
                    value={draft.location}
                    onChange={update}
                  />
                </div>
                <div className="fd-field">
                  <label htmlFor="jobType">Loại hình</label>
                  <select
                    id="jobType"
                    name="jobType"
                    value={draft.jobType}
                    onChange={update}
                  >
                    <option value="">Tất cả loại hình</option>
                    {Object.entries(jobTypeLabels).map(([k, v]) => (
                      <option key={k} value={k}>
                        {v}
                      </option>
                    ))}
                  </select>
                </div>
                <div className="fd-field">
                  <label htmlFor="shift">Ca làm việc</label>
                  <select
                    id="shift"
                    name="shift"
                    value={draft.shift}
                    onChange={update}
                  >
                    <option value="">Mọi ca</option>
                    {Object.entries(shiftLabels).map(([k, v]) => (
                      <option key={k} value={k}>
                        {v}
                      </option>
                    ))}
                  </select>
                </div>
                <div className="fd-field">
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
                </div>
                <div className="fd-field">
                  <label htmlFor="payType">Hình thức trả lương</label>
                  <select
                    id="payType"
                    name="payType"
                    value={draft.payType}
                    onChange={update}
                  >
                    <option value="">Tất cả</option>
                    {Object.entries(payTypeLabels).map(([k, v]) => (
                      <option key={k} value={k}>
                        Theo {v.replace('/', '')}
                      </option>
                    ))}
                  </select>
                </div>
                <div className="fd-field">
                  <label htmlFor="maxHoursPerWeek">Tối đa giờ/tuần</label>
                  <input
                    id="maxHoursPerWeek"
                    name="maxHoursPerWeek"
                    type="number"
                    min="1"
                    max="80"
                    placeholder="VD: 20"
                    value={draft.maxHoursPerWeek}
                    onChange={update}
                  />
                </div>
                <div className="fd-field fd-checks">
                  <label className="fd-switch">
                    <input
                      type="checkbox"
                      name="noExperience"
                      checked={draft.noExperience === 'true'}
                      onChange={update}
                    />
                    <span>Không cần kinh nghiệm</span>
                  </label>
                  <label className="fd-switch">
                    <input
                      type="checkbox"
                      name="studentFriendly"
                      checked={draft.studentFriendly === 'true'}
                      onChange={update}
                    />
                    <span>Thân thiện sinh viên</span>
                  </label>
                </div>
              </div>
              <div className="fd-foot">
                <span>
                  {loading
                    ? 'Đang cập nhật…'
                    : (page?.totalElements || 0) + ' việc phù hợp'}
                </span>
                <button
                  type="button"
                  className="btn primary small"
                  onClick={() => setFiltersOpen(false)}
                >
                  Đóng
                </button>
              </div>
            </div>
          </aside>
        </div>
      </section>
    </div>
  )
}
