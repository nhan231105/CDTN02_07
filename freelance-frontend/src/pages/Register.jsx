import { useState } from 'react'
import { useAuth } from '../context/AuthContext'

export default function Register({ onSuccess, onLoginClick }) {
  const { register } = useAuth()
  const [formData, setFormData] = useState({
    fullName: '',
    email: '',
    phone: '',
    password: '',
    role: 'STUDENT',
  })
  const [error, setError] = useState(null)
  const [loading, setLoading] = useState(false)

  const handleChange = (e) => {
    const { name, value } = e.target
    setFormData(prev => ({
      ...prev,
      [name]: value,
    }))
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError(null)
    setLoading(true)

    try {
      await register(formData)
      onSuccess()
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="auth-page">
      <div className="auth-form-container">
        <h2>Đăng Ký</h2>
        {error && <div className="error-message">{error}</div>}
        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label>Tên Đầy Đủ:</label>
            <input
              type="text"
              name="fullName"
              value={formData.fullName}
              onChange={handleChange}
              required
            />
          </div>

          <div className="form-group">
            <label>Email:</label>
            <input
              type="email"
              name="email"
              value={formData.email}
              onChange={handleChange}
              required
            />
          </div>

          <div className="form-group">
            <label>Số Điện Thoại:</label>
            <input
              type="tel"
              name="phone"
              value={formData.phone}
              onChange={handleChange}
              required
            />
          </div>

          <div className="form-group">
            <label>Mật Khẩu:</label>
            <input
              type="password"
              name="password"
              value={formData.password}
              onChange={handleChange}
              required
            />
          </div>

          <div className="form-group">
            <label>Vai Trò:</label>
            <select name="role" value={formData.role} onChange={handleChange}>
              <option value="STUDENT">Sinh Viên</option>
              <option value="EMPLOYER">Nhà Tuyển Dụng</option>
            </select>
          </div>

          <button type="submit" disabled={loading} className="btn btn-primary">
            {loading ? 'Đang Xử Lý...' : 'Đăng Ký'}
          </button>
        </form>

        <div className="auth-footer">
          <p>Đã có tài khoản? <a href="#" onClick={(e) => { e.preventDefault(); if (onLoginClick) onLoginClick(); }}>Đăng Nhập</a></p>
        </div>
      </div>
    </div>
  )
}
