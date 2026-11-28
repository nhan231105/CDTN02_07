import { useState } from 'react'
import api from '../api/apiService'
import useCooldown from '../hooks/useCooldown'
import OtpInput from './OtpInput'

/**
 * Bước xác minh email khi đổi mật khẩu (dùng chung cho Sinh viên và Nhà tuyển dụng):
 * gửi mã 6 số tới email hiện tại rồi nhập mã trước khi lưu mật khẩu mới.
 */
export default function PasswordChangeCode({ value, onChange, onError }) {
  const [sending, setSending] = useState(false)
  const [sentTo, setSentTo] = useState(false)
  const [cooldown, startCooldown] = useCooldown(60)

  const send = async () => {
    setSending(true)
    onError('')
    try {
      await api.requestPasswordChangeCode()
      setSentTo(true)
      startCooldown()
    } catch (err) {
      if (err.status === 429 && err.code === 'OTP_COOLDOWN') startCooldown()
      onError(err.message)
    } finally {
      setSending(false)
    }
  }

  return (
    <div className="password-change-code">
      <p className="muted">
        Để đổi mật khẩu, hãy nhận mã xác minh gửi tới email hiện tại của bạn.
      </p>
      <button
        type="button"
        className="btn secondary"
        disabled={sending || cooldown > 0}
        onClick={send}
      >
        {sending
          ? 'Đang gửi mã…'
          : cooldown > 0
            ? `Gửi lại mã sau ${cooldown}s`
            : sentTo
              ? 'Gửi lại mã'
              : 'Gửi mã xác minh'}
      </button>
      {sentTo && (
        <p role="status">Đã gửi mã. Vui lòng kiểm tra hộp thư (cả mục Spam).</p>
      )}
      <OtpInput value={value} onChange={onChange} />
    </div>
  )
}
