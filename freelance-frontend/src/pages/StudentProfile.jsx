import { useNavigate } from 'react-router-dom'
import { useCallback, useEffect, useState } from 'react'
import api, { download, request, upload } from '../api/apiService'
import { useAuth } from '../context/auth'
import { Feedback, Field, Loading } from '../components/UI'
import { strongPassword } from '../utils/format'
export default function StudentProfile({ employer = false }) {
  const { refresh, logout } = useAuth()
  const navigate = useNavigate()
  const [profile, setProfile] = useState(null)
  const [form, setForm] = useState({})
  const [error, setError] = useState('')
  const [message, setMessage] = useState('')
  const [busy, setBusy] = useState(false)
  const [account, setAccount] = useState({
    email: '',
    phone: '',
    password: '',
    currentPassword: '',
  })
  const [avatar, setAvatar] = useState('')
  const load = useCallback(async () => {
    const response = await api.profile()
    const p = response.data || response
    setProfile(p)
    setForm({
      fullName: p.fullName || '',
      bio: p.bio || '',
      orgName: p.orgName || '',
      address: p.address || '',
      description: p.description || '',
    })
    return p
  }, [])
  useEffect(() => {
    Promise.resolve()
      .then(load)
      .catch((err) => setError(err.message))
  }, [load])
  useEffect(() => {
    if (!profile?.avatarRef) return
    let active = true
    let url
    request('GET', '/me/avatar', null, null, { blob: true })
      .then((file) => {
        url = URL.createObjectURL(file)
        if (active) setAvatar(url)
      })
      .catch(() => {})
    return () => {
      active = false
      if (url) URL.revokeObjectURL(url)
    }
  }, [profile?.avatarRef])
  const run = async (action) => {
    setBusy(true)
    setError('')
    setMessage('')
    try {
      await action()
    } catch (err) {
      setError(err.message)
    } finally {
      setBusy(false)
    }
  }
  const save = (e) => {
    e.preventDefault()
    run(async () => {
      await api.saveProfile(form)
      await load()
      await refresh()
      setMessage('Đã lưu hồ sơ thành công.')
    })
  }
  const fileChange = (e, kind) => {
    const file = e.target.files?.[0]
    if (!file) return
    const valid =
      kind === 'cv'
        ? file.type === 'application/pdf' && file.size <= 5 * 1024 * 1024
        : ['image/png', 'image/jpeg'].includes(file.type) &&
          file.size <= 2 * 1024 * 1024
    if (!valid) {
      setError(
        kind === 'cv'
          ? 'CV phải là PDF, tối đa 5 MB.'
          : 'Ảnh phải là PNG/JPEG, tối đa 2 MB.',
      )
      e.target.value = ''
      return
    }
    run(async () => {
      await upload('/me/' + kind, file)
      await load()
      await refresh()
      setMessage(
        kind === 'cv'
          ? 'CV đã được tải lên.'
          : 'Ảnh đại diện đã được cập nhật.',
      )
    })
    e.target.value = ''
  }
  const saveAccount = (e) => {
    e.preventDefault()
    if (account.password && !strongPassword(account.password))
      return setError(
        'Mật khẩu mới cần ít nhất 8 ký tự, có chữ hoa, chữ thường và ký tự đặc biệt.',
      )
    if (!account.email && !account.phone && !account.password)
      return setError('Nhập ít nhất một thông tin cần thay đổi.')
    run(async () => {
      const data = Object.fromEntries(
        Object.entries(account).filter(([, v]) => v !== ''),
      )
      const result = await api.saveAccount(data)
      if (result.requiresLogin) {
        logout()
        navigate('/login', { state: { accountUpdated: true } })
        return
      }
      await load()
      await refresh()
      setAccount({ email: '', phone: '', password: '', currentPassword: '' })
      setMessage(
        'Đã cập nhật tài khoản. Thông tin được giới hạn thay đổi trong 48 giờ.',
      )
    })
  }
  if (!profile)
    return (
      <div className="container section">
        {error ? <Feedback error={error} /> : <Loading />}
      </div>
    )
  return (
    <div className="container section">
      <div className="section-heading">
        <div>
          <span className="eyebrow">KHÔNG GIAN CỦA BẠN</span>
          <h1>{employer ? 'Hồ sơ doanh nghiệp' : 'Hồ sơ cá nhân'}</h1>
          <p className="muted">
            Một hồ sơ đầy đủ mở ra nhiều kết nối phù hợp hơn.
          </p>
        </div>
      </div>
      <Feedback error={error} message={message} />
      <div className="profile-layout">
        <aside className="surface profile-summary">
          <div className="profile-avatar">
            {avatar ? (
              <img src={avatar} alt="Ảnh đại diện của bạn" />
            ) : (
              profile.fullName?.charAt(0)
            )}
          </div>
          <h2>{profile.fullName}</h2>
          <p>{employer ? 'Nhà tuyển dụng' : 'Sinh viên'}</p>
          <p className="muted">{profile.email}</p>
          {!employer && (
            <label className="btn secondary upload-label">
              {busy ? 'Đang tải…' : 'Thay ảnh đại diện'}
              <input
                type="file"
                accept="image/png,image/jpeg"
                disabled={busy}
                onChange={(e) => fileChange(e, 'avatar')}
              />
            </label>
          )}
          {!employer && <small>PNG, JPG · tối đa 2 MB</small>}
          <div className="profile-contact">
            <span>✉ {profile.email}</span>
            <span>☎ {profile.phone}</span>
          </div>
        </aside>
        <div className="profile-sections">
          <section className="surface">
            <h2>{employer ? 'Thông tin công ty' : 'Thông tin cá nhân'}</h2>
            <form onSubmit={save}>
              <Field id="profile-name" label="Họ và tên">
                <input
                  id="profile-name"
                  required
                  minLength={3}
                  maxLength={100}
                  value={form.fullName}
                  onChange={(e) =>
                    setForm({ ...form, fullName: e.target.value })
                  }
                />
              </Field>
              {employer ? (
                <>
                  <Field id="orgName" label="Tên công ty">
                    <input
                      id="orgName"
                      value={form.orgName}
                      maxLength={150}
                      onChange={(e) =>
                        setForm({ ...form, orgName: e.target.value })
                      }
                    />
                  </Field>
                  <Field id="address" label="Địa chỉ">
                    <input
                      id="address"
                      value={form.address}
                      maxLength={255}
                      onChange={(e) =>
                        setForm({ ...form, address: e.target.value })
                      }
                    />
                  </Field>
                  <Field id="description" label="Giới thiệu công ty">
                    <textarea
                      id="description"
                      rows={5}
                      value={form.description}
                      onChange={(e) =>
                        setForm({ ...form, description: e.target.value })
                      }
                    />
                  </Field>
                </>
              ) : (
                <Field
                  id="bio"
                  label="Giới thiệu bản thân"
                  hint="Chia sẻ kỹ năng, trải nghiệm và định hướng của bạn."
                >
                  <textarea
                    id="bio"
                    rows={5}
                    maxLength={2000}
                    value={form.bio}
                    onChange={(e) => setForm({ ...form, bio: e.target.value })}
                  />
                </Field>
              )}
              <button className="btn primary" disabled={busy}>
                {busy ? 'Đang lưu…' : 'Lưu hồ sơ'}
              </button>
            </form>
          </section>
          {!employer && (
            <section className="surface">
              <h2>CV của bạn</h2>
              <p className="muted">
                CV được chia sẻ với nhà tuyển dụng khi bạn xác nhận ứng tuyển.
              </p>
              <div className="cv-summary">
                <span>▤</span>
                <div>
                  <strong>{profile.cvOriginalName || 'Chưa có CV'}</strong>
                  <p>
                    {profile.cvSize
                      ? (profile.cvSize / 1024).toFixed(1) + ' KB'
                      : 'PDF · tối đa 5 MB'}
                  </p>
                </div>
                {profile.cvFileRef && (
                  <button
                    className="text-button"
                    onClick={() =>
                      run(() => download('/me/cv', profile.cvOriginalName))
                    }
                  >
                    Tải xuống
                  </button>
                )}
              </div>
              <label className="btn secondary upload-label">
                {busy
                  ? 'Đang tải…'
                  : profile.cvFileRef
                    ? 'Thay CV mới'
                    : 'Tải CV lên'}
                <input
                  type="file"
                  accept="application/pdf"
                  disabled={busy}
                  onChange={(e) => fileChange(e, 'cv')}
                />
              </label>
            </section>
          )}
          <section className="surface">
            <h2>Bảo mật tài khoản</h2>
            <p className="muted">
              Đổi email, số điện thoại hoặc mật khẩu. Mỗi lần cập nhật cách nhau
              ít nhất 48 giờ.
            </p>
            <form onSubmit={saveAccount}>
              <div className="form-grid">
                <Field id="account-email" label="Email mới">
                  <input
                    id="account-email"
                    type="email"
                    value={account.email}
                    onChange={(e) =>
                      setAccount({ ...account, email: e.target.value })
                    }
                  />
                </Field>
                <Field id="account-phone" label="Số điện thoại mới">
                  <input
                    id="account-phone"
                    type="tel"
                    pattern="0[0-9]{9}"
                    value={account.phone}
                    onChange={(e) =>
                      setAccount({ ...account, phone: e.target.value })
                    }
                  />
                </Field>
              </div>
              <Field id="newPassword" label="Mật khẩu mới (nếu muốn đổi)">
                <input
                  id="newPassword"
                  type="password"
                  autoComplete="new-password"
                  value={account.password}
                  onChange={(e) =>
                    setAccount({ ...account, password: e.target.value })
                  }
                />
              </Field>
              <Field id="currentPassword" label="Mật khẩu hiện tại">
                <input
                  id="currentPassword"
                  type="password"
                  autoComplete="current-password"
                  required
                  value={account.currentPassword}
                  onChange={(e) =>
                    setAccount({ ...account, currentPassword: e.target.value })
                  }
                />
              </Field>
              <button className="btn secondary" disabled={busy}>
                Cập nhật tài khoản
              </button>
            </form>
          </section>
        </div>
      </div>
    </div>
  )
}
