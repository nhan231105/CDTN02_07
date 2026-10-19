import { useState, useEffect } from 'react'
import apiService from '../api/apiService'
import { useAuth } from '../context/AuthContext'

export default function Jobs() {
  const { user } = useAuth()
  const [jobs, setJobs] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [searchParams, setSearchParams] = useState({
    keyword: '',
    location: '',
    category: '',
  })
  const [appliedJobs, setAppliedJobs] = useState(new Set())
  const [showApplicationForm, setShowApplicationForm] = useState(null)
  const [applicationData, setApplicationData] = useState({
    fullName: user?.fullName || '',
    email: user?.email || '',
    phone: user?.phone || '',
    coverLetter: '',
    cvFileRef: '',
    consentDataUsage: false,
  })

  useEffect(() => {
    loadJobs()
  }, [searchParams])

  const loadJobs = async () => {
    try {
      setLoading(true)
      const params = {}
      if (searchParams.keyword) params.keyword = searchParams.keyword
      if (searchParams.location) params.location = searchParams.location
      if (searchParams.category && searchParams.category !== '') params.category = searchParams.category

      const response = await apiService.getJobs(params)
      setJobs(response.content || response.data || [])
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  const handleApply = async (jobId) => {
    try {
      const data = {
        ...applicationData,
        jobId,
        studentId: user.id,
      }
      await apiService.applyForJob(jobId, data)
      setAppliedJobs(new Set([...appliedJobs, jobId]))
      setShowApplicationForm(null)
      alert('Ứng tuyển thành công!')
    } catch (err) {
      alert(`Ứng tuyển thất bại: ${err.message}`)
    }
  }

  if (loading) return <div className="loading">Đang tải công việc...</div>
  if (error) return <div className="error">{error}</div>

  return (
    <div className="jobs-page">
      <h1>Danh Sách Công Việc</h1>

      {/* Search Filters */}
      <div className="search-filters">
        <input
          type="text"
          placeholder="Tìm kiếm công việc..."
          value={searchParams.keyword}
          onChange={(e) => setSearchParams({ ...searchParams, keyword: e.target.value })}
        />
        <input
          type="text"
          placeholder="Địa điểm"
          value={searchParams.location}
          onChange={(e) => setSearchParams({ ...searchParams, location: e.target.value })}
        />
        <select
          value={searchParams.category}
          onChange={(e) => setSearchParams({ ...searchParams, category: e.target.value })}
        >
          <option value="">Tất cả danh mục</option>
          <option value="Development">Development</option>
          <option value="Design">Design</option>
          <option value="Marketing">Marketing</option>
        </select>
      </div>

      {/* Jobs List */}
      <div className="jobs-list">
        {jobs.length === 0 ? (
          <p>Không tìm thấy công việc nào.</p>
        ) : (
          jobs.map((job) => (
            <div key={job.id} className="job-card">
              <h3>{job.title}</h3>
              <p className="job-location">📍 {job.location}</p>
              <p className="job-category">🏷️ {job.category}</p>
              <p className="job-salary">💰 ${job.salary} USD</p>
              <p className="job-desc">{job.description.substring(0, 150)}...</p>
              <p className="job-deadline">Hạn chót: {job.deadline}</p>

              {appliedJobs.has(job.id) ? (
                <button className="btn btn-disabled" disabled>
                  ✓ Đã ứng tuyển
                </button>
              ) : (
                <>
                  {showApplicationForm === job.id ? (
                    <div className="application-form">
                      <textarea
                        placeholder="Thư ứng tuyển"
                        value={applicationData.coverLetter}
                        onChange={(e) =>
                          setApplicationData({
                            ...applicationData,
                            coverLetter: e.target.value,
                          })
                        }
                      />
                      <input
                        type="text"
                        placeholder="Link CV"
                        value={applicationData.cvFileRef}
                        onChange={(e) =>
                          setApplicationData({
                            ...applicationData,
                            cvFileRef: e.target.value,
                          })
                        }
                      />
                      <label>
                        <input
                          type="checkbox"
                          checked={applicationData.consentDataUsage}
                          onChange={(e) =>
                            setApplicationData({
                              ...applicationData,
                              consentDataUsage: e.target.checked,
                            })
                          }
                        />
                        Tôi đồng ý chia sẻ dữ liệu
                      </label>
                      <button
                        className="btn btn-primary"
                        onClick={() => handleApply(job.id)}
                      >
                        Nộp Ứng Tuyển
                      </button>
                      <button
                        className="btn btn-secondary"
                        onClick={() => setShowApplicationForm(null)}
                      >
                        Hủy
                      </button>
                    </div>
                  ) : (
                    <button
                      className="btn btn-primary"
                      onClick={() => setShowApplicationForm(job.id)}
                    >
                      Ứng Tuyển
                    </button>
                  )}
                </>
              )}
            </div>
          ))
        )}
      </div>
    </div>
  )
}
