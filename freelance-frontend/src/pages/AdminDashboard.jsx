import { useState, useEffect } from 'react'
import apiService from '../api/apiService'

export default function AdminDashboard() {
  const [stats, setStats] = useState(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    loadStats()
  }, [])

  const loadStats = async () => {
    try {
      const response = await apiService.getAdminStats()
      setStats(response.data)
    } catch (err) {
      console.error('Lỗi tải thống kê:', err)
    } finally {
      setLoading(false)
    }
  }

  if (loading) return <div>Đang tải...</div>

  return (
    <div className="admin-page">
      <h1>Dashboard Quản Lý</h1>

      {stats && (
        <div className="stats-grid">
          <div className="stat-card">
            <h3>Tổng Người Dùng</h3>
            <p className="stat-value">{stats.totalUsers}</p>
          </div>
          <div className="stat-card">
            <h3>Sinh Viên</h3>
            <p className="stat-value">{stats.totalStudents}</p>
          </div>
          <div className="stat-card">
            <h3>Nhà Tuyển Dụng</h3>
            <p className="stat-value">{stats.totalEmployers}</p>
          </div>
          <div className="stat-card">
            <h3>Người Dùng Hoạt Động</h3>
            <p className="stat-value">{stats.activeUsers}</p>
          </div>
          <div className="stat-card">
            <h3>Người Dùng Bị Khóa</h3>
            <p className="stat-value">{stats.lockedUsers}</p>
          </div>
        </div>
      )}
    </div>
  )
}
