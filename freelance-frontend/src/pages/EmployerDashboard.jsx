import { useState, useEffect } from 'react'
import apiService from '../api/apiService'
import { useAuth } from '../context/AuthContext'

export default function EmployerDashboard() {
  const { user } = useAuth()
  const [jobs, setJobs] = useState([])
  const [showJobForm, setShowJobForm] = useState(false)
  const [formData, setFormData] = useState({
    title: '',
    description: '',
    requirements: '',
    category: '',
    location: '',
    salary: '',
    deadline: '',
    workSchedule: '',
    benefits: '',
    employerId: user?.id,
  })

  useEffect(() => {
    loadEmployerJobs()
  }, [])

  const loadEmployerJobs = async () => {
    try {
      const response = await apiService.getEmployerJobs(user.id)
      setJobs(response.content || response.data || [])
    } catch (err) {
      console.error('Lỗi tải công việc:', err)
    }
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    try {
      await apiService.createJob(formData)
      alert('Tạo công việc thành công!')
      setShowJobForm(false)
      setFormData({
        title: '',
        description: '',
        requirements: '',
        category: '',
        location: '',
        salary: '',
        deadline: '',
        workSchedule: '',
        benefits: '',
        employerId: user?.id,
      })
      loadEmployerJobs()
    } catch (err) {
      alert(`Lỗi: ${err.message}`)
    }
  }

  return (
    <div className="dashboard-page">
      <h1>Dashboard Nhà Tuyển Dụng</h1>

      <div className="dashboard-header">
        <h2>Công Việc Của Tôi</h2>
        <button className="btn btn-primary" onClick={() => setShowJobForm(!showJobForm)}>
          {showJobForm ? 'Hủy' : 'Tạo Công Việc Mới'}
        </button>
      </div>

      {showJobForm && (
        <div className="job-form-container">
          <form onSubmit={handleSubmit}>
            <input
              type="text"
              placeholder="Tiêu đề công việc"
              value={formData.title}
              onChange={(e) => setFormData({ ...formData, title: e.target.value })}
              required
            />
            <textarea
              placeholder="Mô tả công việc"
              value={formData.description}
              onChange={(e) => setFormData({ ...formData, description: e.target.value })}
              required
            />
            <textarea
              placeholder="Yêu cầu"
              value={formData.requirements}
              onChange={(e) => setFormData({ ...formData, requirements: e.target.value })}
            />
            <select
              value={formData.category}
              onChange={(e) => setFormData({ ...formData, category: e.target.value })}
              required
            >
              <option value="">-- Chọn Danh Mục --</option>
              <option value="Development">Development</option>
              <option value="Design">Design</option>
              <option value="Marketing">Marketing</option>
            </select>
            <input
              type="text"
              placeholder="Địa điểm"
              value={formData.location}
              onChange={(e) => setFormData({ ...formData, location: e.target.value })}
            />
            <input
              type="number"
              placeholder="Lương"
              value={formData.salary}
              onChange={(e) => setFormData({ ...formData, salary: e.target.value })}
            />
            <input
              type="date"
              value={formData.deadline}
              onChange={(e) => setFormData({ ...formData, deadline: e.target.value })}
              required
            />
            <input
              type="text"
              placeholder="Lịch làm việc"
              value={formData.workSchedule}
              onChange={(e) => setFormData({ ...formData, workSchedule: e.target.value })}
            />
            <textarea
              placeholder="Lợi ích"
              value={formData.benefits}
              onChange={(e) => setFormData({ ...formData, benefits: e.target.value })}
            />
            <button type="submit" className="btn btn-primary">Tạo Công Việc</button>
          </form>
        </div>
      )}

      <div className="jobs-list">
        {jobs.map((job) => (
          <div key={job.id} className="job-card">
            <h3>{job.title}</h3>
            <p>Địa điểm: {job.location}</p>
            <p>Lương: ${job.salary}</p>
            <p>Trạng thái: {job.status}</p>
          </div>
        ))}
      </div>
    </div>
  )
}
