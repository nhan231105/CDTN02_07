import { useEffect, useState } from 'react'
import api from '../api/apiService'
import { Feedback, Loading } from '../components/UI'
export default function AdminDashboard() {
  const [stats, setStats] = useState(null)
  const [error, setError] = useState('')
  useEffect(() => {
    api
      .stats()
      .then((r) => setStats(r.data || r))
      .catch((err) => setError(err.message))
  }, [])
  return (
    <div className="container section">
      <span className="eyebrow">TỔNG QUAN NỀN TẢNG</span>
      <h1>Không gian quản trị</h1>
      <p className="muted">Theo dõi tài khoản và hoạt động của CampusWork.</p>
      <Feedback error={error} />
      {stats ? (
        <div className="stats-grid">
          {[
            ['Tổng tài khoản', stats.totalUsers],
            ['Sinh viên', stats.totalStudents],
            ['Nhà tuyển dụng', stats.totalEmployers],
            ['Đang hoạt động', stats.activeUsers],
            ['Đã khóa', stats.lockedUsers],
          ].map(([label, value]) => (
            <div className="surface stat-card" key={label}>
              <span>{label}</span>
              <strong>{value}</strong>
            </div>
          ))}
        </div>
      ) : (
        !error && <Loading />
      )}
      <div className="surface admin-note">
        <h2>Phạm vi triển khai</h2>
        <p className="muted">
          Dashboard tổng quan hỗ trợ xác thực và điều hướng quản trị. Các chức
          năng quản trị mở rộng không thuộc 7 Use Case của phiên bản này.
        </p>
      </div>
    </div>
  )
}
