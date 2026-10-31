import { strongPassword } from '../utils/format'
import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/auth'
import { Feedback, Field } from '../components/UI'
export default function Register() {
  const { register } = useAuth()
  const navigate = useNavigate()
  const [form, setForm] = useState({
    fullName: '',
    email: '',
    phone: '',
    password: '',
    role: 'STUDENT',
  })
  const [confirm, setConfirm] = useState('')
  const [terms, setTerms] = useState(false)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')
  const update = (e) => setForm({ ...form, [e.target.name]: e.target.value })
  const submit = async (e) => {
    e.preventDefault()
    setError('')
    if (!strongPassword(form.password))
      return setError(
        'Mật khẩu cần ít nhất 8 ký tự, có chữ hoa, chữ thường và ký tự đặc biệt.',
      )
    if (form.password !== confirm)
      return setError('Mật khẩu xác nhận chưa khớp.')
    if (!terms) return setError('Vui lòng đồng ý với điều khoản sử dụng.')
    setBusy(true)
    try {
      await register(form)
      navigate('/login', { state: { registered: true } })
    } catch (err) {
      setError(err.message)
    } finally {
      setBusy(false)
    }
  }
  return (
    <div className="auth-layout container">
      <aside className="auth-story">
        <span className="eyebrow">KẾT NỐI TIỀM NĂNG</span>
        <h1>
          Cơ hội dành cho
          <br />
          người dám bắt đầu.
        </h1>
        <p>
          Dù đang tìm trải nghiệm đầu tiên hay đồng đội mới, CampusWork giúp bạn
          tìm thấy kết nối phù hợp.
        </p>
        <div className="register-benefits">
          <p>
            <span>01</span> Việc làm phù hợp lịch học
          </p>
          <p>
            <span>02</span> Hồ sơ và CV tại một nơi
          </p>
          <p>
            <span>03</span> Theo dõi từng bước ứng tuyển
          </p>
        </div>
      </aside>
      <section className="auth-form">
        <span className="eyebrow">TẠO TÀI KHOẢN MIỄN PHÍ</span>
        <h2>Bắt đầu cùng CampusWork</h2>
        <p className="muted">Một tài khoản. Nhiều cơ hội phía trước.</p>
        <Feedback error={error} />
        <form onSubmit={submit}>
          <div className="role-picker" aria-label="Loại tài khoản">
            <button
              type="button"
              className={form.role === 'STUDENT' ? 'selected' : ''}
              onClick={() => setForm({ ...form, role: 'STUDENT' })}
            >
              ↗ Tôi tìm việc
            </button>
            <button
              type="button"
              className={form.role === 'EMPLOYER' ? 'selected' : ''}
              onClick={() => setForm({ ...form, role: 'EMPLOYER' })}
            >
              ▣ Tôi tuyển dụng
            </button>
          </div>
          <Field id="fullName" label="Họ và tên">
            <input
              id="fullName"
              name="fullName"
              autoComplete="name"
              minLength={3}
              maxLength={100}
              required
              value={form.fullName}
              onChange={update}
            />
          </Field>
          <div className="form-grid">
            <Field id="email" label="Email">
              <input
                id="email"
                name="email"
                type="email"
                autoComplete="email"
                required
                value={form.email}
                onChange={update}
              />
            </Field>
            <Field id="phone" label="Số điện thoại">
              <input
                id="phone"
                name="phone"
                type="tel"
                pattern="0[0-9]{9}"
                title="10 chữ số, bắt đầu bằng 0"
                autoComplete="tel"
                required
                value={form.phone}
                onChange={update}
              />
            </Field>
          </div>
          <Field
            id="reg-password"
            label="Mật khẩu"
            hint="Ít nhất 8 ký tự, gồm chữ hoa, chữ thường và ký tự đặc biệt."
          >
            <input
              id="reg-password"
              name="password"
              type="password"
              autoComplete="new-password"
              minLength={8}
              maxLength={50}
              required
              value={form.password}
              onChange={update}
            />
          </Field>
          <Field id="confirmPassword" label="Xác nhận mật khẩu">
            <input
              id="confirmPassword"
              type="password"
              autoComplete="new-password"
              required
              value={confirm}
              onChange={(e) => setConfirm(e.target.value)}
            />
          </Field>
          <label className="check-label">
            <input
              type="checkbox"
              checked={terms}
              onChange={(e) => setTerms(e.target.checked)}
              required
            />
            Tôi đồng ý cung cấp thông tin chính xác và sử dụng nền tảng có trách
            nhiệm.
          </label>
          <button className="btn primary full" disabled={busy}>
            {busy ? 'Đang tạo tài khoản…' : 'Tạo tài khoản →'}
          </button>
        </form>
        <p className="auth-bottom">
          Đã có tài khoản? <Link to="/login">Đăng nhập</Link>
        </p>
      </section>
    </div>
  )
}
