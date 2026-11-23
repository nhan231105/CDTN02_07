import PasswordInput from '../components/PasswordInput'
import { useState } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { redirectAfterLogin, useAuth } from '../context/auth'
import { Feedback, Field } from '../components/UI'
export default function Login() {
  const { login } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const [form, setForm] = useState({ identifier: '', password: '' })
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')
  const submit = async (e) => {
    e.preventDefault()
    setError('')
    setBusy(true)
    try {
      const user = await login(form)
      navigate(redirectAfterLogin(user.role, location.state?.from), {
        replace: true,
      })
    } catch (err) {
      setError(err.message)
    } finally {
      setBusy(false)
    }
  }
  return (
    <div className="auth-layout container">
      <aside className="auth-story">
        <span className="eyebrow">CƠ HỘI MỚI ĐANG CHỜ BẠN</span>
        <h1>
          Bước tiếp theo
          <br />
          bắt đầu từ đây.
        </h1>
        <p>
          Tìm một công việc phù hợp lịch học, tích lũy kinh nghiệm và xây dựng
          hành trình của riêng bạn.
        </p>
        <div className="story-illustration">
          <div className="orbit one" />
          <div className="orbit two" />
          <div className="story-card">
            <span className="mini-logo">↗</span>
            <strong>
              Đúng cơ hội.
              <br />
              Đúng thời điểm.
            </strong>
            <span className="status">Sẵn sàng cho tương lai</span>
          </div>
        </div>
        <span className="story-bottom">
          HỌC TẬP · TRẢI NGHIỆM · TRƯỞNG THÀNH
        </span>
      </aside>
      <section className="auth-form">
        <span className="eyebrow">CHÀO MỪNG TRỞ LẠI</span>
        <h2>Đăng nhập</h2>
        <p className="muted">Tiếp tục hành trình nghề nghiệp của bạn.</p>
        <Feedback error={error} />
        {location.state?.registered && (
          <p role="status">Email đã xác minh. Bạn có thể đăng nhập ngay.</p>
        )}
        {location.state?.passwordReset && (
          <p role="status">Đã đổi mật khẩu. Hãy đăng nhập bằng mật khẩu mới.</p>
        )}
        <form onSubmit={submit}>
          <Field id="identifier" label="Email hoặc số điện thoại">
            <input
              id="identifier"
              autoComplete="username"
              required
              value={form.identifier}
              onChange={(e) => setForm({ ...form, identifier: e.target.value })}
              placeholder="ban@email.com"
            />
          </Field>
          <Field id="password" label="Mật khẩu">
            <PasswordInput
              id="password"
              autoComplete="current-password"
              required
              value={form.password}
              onChange={(e) => setForm({ ...form, password: e.target.value })}
            />
          </Field>
          <p>
            <Link to="/forgot-password">Quên mật khẩu?</Link>
          </p>
          <button className="btn primary full" disabled={busy}>
            {busy ? 'Đang đăng nhập…' : 'Đăng nhập →'}
          </button>
        </form>
        <p className="auth-bottom">
          Chưa có tài khoản? <Link to="/register">Đăng ký miễn phí</Link>
        </p>
        <div className="auth-note">
          Thông tin của bạn được bảo vệ. Chỉ kết nối với những cơ hội bạn lựa
          chọn.
        </div>
      </section>
    </div>
  )
}
