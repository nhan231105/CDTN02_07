import { useState } from 'react'
import { useAuth } from '../context/AuthContext'

export default function Login({ onSuccess, onRegisterClick }) {
  const { login } = useAuth()
  const [formData, setFormData] = useState({
    identifier: '',
    password: '',
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
      await login(formData)
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
        <h2>Đăng Nhập</h2>
        {error && <div className="error-message">{error}</div>}
        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label>Email hoặc Số Điện Thoại:</label>
            <input
              type="text"
              name="identifier"
              value={formData.identifier}
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

          <button type="submit" disabled={loading} className="btn btn-primary">
            {loading ? 'Đang Xử Lý...' : 'Đăng Nhập'}
          </button>
        </form>

        <div className="auth-footer">
          <p>Chưa có tài khoản? <a href="#" onClick={(e) => { e.preventDefault(); if (onRegisterClick) onRegisterClick(); }}>Đăng Ký</a></p>
        </div>
      </div>
    </div>
  )
}
