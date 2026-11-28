import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import api from '../api/apiService'
import { Feedback, Field } from '../components/UI'
import OtpInput from '../components/OtpInput'
import PasswordInput from '../components/PasswordInput'
import { strongPassword } from '../utils/format'
import useCooldown from '../hooks/useCooldown'

export default function ForgotPassword() {
  const navigate = useNavigate()
  const [email, setEmail] = useState('')
  const [sent, setSent] = useState(false)
  const [code, setCode] = useState('')
  const [password, setPassword] = useState('')
  const [confirm, setConfirm] = useState('')
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')
  const [message, setMessage] = useState('')
  const [cooldown, startCooldown] = useCooldown(60)

  async function send() {
    setBusy(true)
    setError('')
    try {
      await api.forgotPassword({ email })
      setSent(true)
      setCode('')
      startCooldown()
      setMessage(
        'Nếu tài khoản có thể đặt lại mật khẩu, bạn sẽ nhận được email.',
      )
    } catch (err) {
      if (err.status === 429 && err.code === 'OTP_COOLDOWN') startCooldown()
      setError(err.message)
    } finally {
      setBusy(false)
    }
  }

  async function submit(event) {
    event.preventDefault()
    if (!sent) return send()
    if (!strongPassword(password))
      return setError(
        'Mật khẩu cần ít nhất 8 ký tự, gồm chữ hoa, chữ thường và ký tự đặc biệt.',
      )
    if (password !== confirm) return setError('Mật khẩu xác nhận chưa khớp.')
    setBusy(true)
    setError('')
    try {
      await api.resetPassword({ email, code, password })
      navigate('/login', { replace: true, state: { passwordReset: true } })
    } catch (err) {
      setError(err.message)
    } finally {
      setBusy(false)
    }
  }

  return (
    <div className="container section narrow">
      <section className="auth-form">
        <h1>Đặt lại mật khẩu</h1>
        <Feedback error={error} />
        {message && <p role="status">{message}</p>}
        <form onSubmit={submit}>
          <Field id="reset-email" label="Email tài khoản">
            <input
              id="reset-email"
              type="email"
              autoComplete="email"
              maxLength={150}
              value={email}
              readOnly={sent}
              required
              onChange={(event) => setEmail(event.target.value)}
            />
          </Field>
          {sent && (
            <>
              <OtpInput value={code} onChange={setCode} />
              <Field id="reset-password" label="Mật khẩu mới">
                <PasswordInput
                  id="reset-password"
                  autoComplete="new-password"
                  maxLength={50}
                  value={password}
                  required
                  onChange={(event) => setPassword(event.target.value)}
                />
              </Field>
              <Field id="reset-confirm" label="Xác nhận mật khẩu mới">
                <PasswordInput
                  id="reset-confirm"
                  autoComplete="new-password"
                  maxLength={50}
                  value={confirm}
                  required
                  onChange={(event) => setConfirm(event.target.value)}
                />
              </Field>
            </>
          )}
          <button className="btn primary full" disabled={busy}>
            {busy
              ? 'Đang xử lý…'
              : sent
                ? 'Lưu mật khẩu mới'
                : 'Gửi mã xác minh'}
          </button>
          {sent && (
            <button
              type="button"
              className="text-button"
              disabled={busy || cooldown > 0}
              onClick={send}
            >
              {cooldown > 0 ? `Gửi lại mã sau ${cooldown}s` : 'Gửi lại mã'}
            </button>
          )}
        </form>
        <p className="auth-bottom">
          <Link to="/login">Quay lại đăng nhập</Link>
        </p>
      </section>
    </div>
  )
}
