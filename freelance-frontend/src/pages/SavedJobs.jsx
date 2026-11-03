import { useCallback, useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import api from '../api/apiService'
import { Empty, Feedback, Loading, Pagination } from '../components/UI'
import JobCard from '../components/JobCard'

export default function SavedJobs() {
  const [page, setPage] = useState(null)
  const [number, setNumber] = useState(0)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const load = useCallback(async () => {
    setError('')
    try {
      setPage(await api.savedJobs(number))
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }, [number])
  useEffect(() => {
    Promise.resolve().then(load)
  }, [load])
  const remove = async (jobId) => {
    try {
      await api.unsaveJob(jobId)
      await load()
    } catch (err) {
      setError(err.message)
    }
  }
  return (
    <div className="container section">
      <div className="section-heading">
        <div>
          <span className="eyebrow">DANH SÁCH YÊU THÍCH</span>
          <h1>Việc đã lưu</h1>
          <p className="muted">
            Quay lại các cơ hội bạn quan tâm bất cứ lúc nào.
          </p>
        </div>
        <Link className="btn primary" to="/jobs">
          Tìm thêm việc
        </Link>
      </div>
      <Feedback error={error} />
      {loading ? (
        <Loading />
      ) : page?.content?.length ? (
        <div className="job-grid">
          {page.content.map((item, index) => (
            <div key={item.id} className="saved-item">
              <JobCard
                job={item.job}
                index={index}
                saved
                onToggleSave={() => remove(item.job.id)}
              />
              {!item.available && (
                <p className="muted small-text">
                  Tin này đã đóng hoặc hết hạn ứng tuyển.
                </p>
              )}
            </div>
          ))}
        </div>
      ) : (
        !error && (
          <Empty title="Bạn chưa lưu việc làm nào">
            Nhấn biểu tượng ♡ ở mỗi tin để lưu lại và xem sau.
          </Empty>
        )
      )}
      <Pagination page={page} onChange={setNumber} />
    </div>
  )
}
