import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import api from '../api/apiService'
import { Empty, Feedback, Loading } from '../components/UI'
import JobCard from '../components/JobCard'
import RichText from '../components/RichText'
import CompanyLogo from '../components/CompanyLogo'
import { brandImage } from '../utils/brand'

export default function EmployerPublic() {
  const { id } = useParams()
  const [employer, setEmployer] = useState(null)
  const [error, setError] = useState('')
  const [tab, setTab] = useState('about')
  const [keyword, setKeyword] = useState('')
  useEffect(() => {
    let live = true
    api
      .employerPublic(id)
      .then((data) => live && setEmployer(data))
      .catch((err) => live && setError(err.message))
    return () => {
      live = false
    }
  }, [id])
  if (error)
    return (
      <main className="container section">
        <Feedback error={error} />
      </main>
    )
  if (!employer) return <Loading />
  const jobs = (employer.openJobs || []).filter((job) =>
    (job.title + ' ' + (job.category || ''))
      .toLowerCase()
      .includes(keyword.trim().toLowerCase()),
  )
  const info = [
    ['Lĩnh vực', employer.industry],
    ['Quy mô', employer.companySize],
    ['Năm thành lập', employer.foundedYear],
    ['Địa chỉ', employer.address],
    ['Website', employer.website],
    ['Liên hệ', employer.email],
  ].filter(([, value]) => value)
  return (
    <main className="company-page">
      <div
        className={'company-cover' + (employer.coverRef ? ' has-image' : '')}
        style={
          employer.coverRef
            ? {
                backgroundImage:
                  'url(' + brandImage(id, 'cover', employer.coverRef) + ')',
              }
            : undefined
        }
      />
      <div className="container">
        <section className="surface company-head">
          <CompanyLogo
            id={id}
            name={employer.name}
            className="large brand"
            version={employer.logoRef}
          />
          <div className="company-head-text">
            <h1>{employer.name}</h1>
            <p className="company-tagline">
              {employer.tagline ||
                employer.industry ||
                'Nhà tuyển dụng trên CampusWork'}
            </p>
            <p className="company-meta">
              <span>
                {employer.averageRating
                  ? 'Đánh giá ' +
                    employer.averageRating +
                    '/5 (' +
                    employer.reviewCount +
                    ')'
                  : 'Chưa có đánh giá'}
              </span>
              <span>{employer.openJobCount} việc đang tuyển</span>
              {employer.companySize && <span>{employer.companySize}</span>}
            </p>
          </div>
        </section>
        <nav className="company-tabs">
          <button
            className={tab === 'about' ? 'active' : ''}
            onClick={() => setTab('about')}
          >
            Về chúng tôi
          </button>
          <button
            className={tab === 'jobs' ? 'active' : ''}
            onClick={() => setTab('jobs')}
          >
            Vị trí đang tuyển ({employer.openJobCount})
          </button>
        </nav>
        {tab === 'about' ? (
          <div className="company-columns">
            <div className="company-main">
              <section className="surface rich-content">
                <h2>Giới thiệu công ty</h2>
                <RichText
                  text={employer.description}
                  fallback="Công ty chưa cập nhật phần giới thiệu."
                />
              </section>
              {employer.benefits?.length > 0 && (
                <section className="surface company-benefits">
                  <h2>Vì sao nên làm tại đây</h2>
                  <ul>
                    {employer.benefits.map((b) => (
                      <li key={b}>
                        <i aria-hidden="true">✓</i>
                        {b}
                      </li>
                    ))}
                  </ul>
                </section>
              )}
            </div>
            <aside className="surface company-info">
              <h2>Thông tin chung</h2>
              {info.length ? (
                <dl>
                  {info.map(([label, value]) => (
                    <div key={label}>
                      <dt>{label}</dt>
                      <dd>
                        {label === 'Website' ? (
                          <a href={value} target="_blank" rel="noreferrer">
                            {value}
                          </a>
                        ) : (
                          value
                        )}
                      </dd>
                    </div>
                  ))}
                </dl>
              ) : (
                <p className="muted">Chưa có thông tin.</p>
              )}
            </aside>
          </div>
        ) : (
          <section>
            <div className="company-search">
              <input
                aria-label="Tìm trong các vị trí của công ty"
                placeholder="Nhập chức danh"
                value={keyword}
                onChange={(e) => setKeyword(e.target.value)}
              />
            </div>
            {jobs.length ? (
              <div className="job-grid">
                {jobs.map((job, i) => (
                  <JobCard key={job.id} job={job} index={i} />
                ))}
              </div>
            ) : (
              <Empty title="Không có vị trí phù hợp" />
            )}
          </section>
        )}
        <p className="company-back">
          <Link to="/jobs">Quay lại danh sách việc làm</Link>
        </p>
      </div>
    </main>
  )
}
