import { useMessage } from '../context/toast'
import { useNavigate } from 'react-router-dom'
import { useCallback, useEffect, useState } from 'react'
import api, { download, request, upload } from '../api/apiService'
import { useAuth } from '../context/auth'
import { Feedback, Field, Loading } from '../components/UI'
import MapPicker from '../components/MapPicker'
import { shiftLabels, strongPassword } from '../utils/format'
export default function StudentProfile({ employer = false }) {
  const { refresh, logout } = useAuth()
  const navigate = useNavigate()
  const [profile, setProfile] = useState(null)
  const [form, setForm] = useState({})
  const [error, setError] = useState('')
  const [, setMessage] = useMessage()
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
      school: p.school || '',
      major: p.major || '',
      studyYear: p.studyYear || '',
      skills: p.skills || '',
      preferredArea: p.preferredArea || '',
      lat: p.lat ?? null,
      lng: p.lng ?? null,
      availableShifts: p.availableShifts || [],
      openToWork: !!p.openToWork,
      examMode: !!p.examMode,
      examStartDate: p.examStartDate || '',
      examEndDate: p.examEndDate || '',
      website: p.website || '',
      industry: p.industry || '',
      companySize: p.companySize || '',
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
      await api.saveProfile({
        ...form,
        openToWork: form.openToWork && form.availableShifts.length > 0,
        studyYear: form.studyYear ? Number(form.studyYear) : null,
        examStartDate: form.examStartDate || null,
        examEndDate: form.examEndDate || null,
      })
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
      <Feedback error={error} />
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
          {profile.completeness != null && (
            <div className="completeness">
              <div className="progress" aria-label="Mức độ hoàn thiện hồ sơ">
                <span style={{ width: profile.completeness + '%' }} />
              </div>
              <strong>Hồ sơ hoàn thiện {profile.completeness}%</strong>
              {profile.missingFields?.length > 0 && (
                <small>Còn thiếu: {profile.missingFields.join(', ')}</small>
              )}
            </div>
          )}
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
                  <Field id="industry" label="Lĩnh vực hoạt động">
                    <input
                      id="industry"
                      value={form.industry}
                      maxLength={100}
                      onChange={(e) =>
                        setForm({ ...form, industry: e.target.value })
                      }
                    />
                  </Field>
                  <Field id="companySize" label="Quy mô nhân sự">
                    <select
                      id="companySize"
                      value={form.companySize}
                      onChange={(e) =>
                        setForm({ ...form, companySize: e.target.value })
                      }
                    >
                      <option value="">Chọn quy mô</option>
                      {['1-10', '10-50', '50-200', '200+'].map((x) => (
                        <option key={x} value={x + ' nhân viên'}>
                          {x} nhân viên
                        </option>
                      ))}
                      {form.companySize &&
                        !/^(1-10|10-50|50-200|200\+) nhân viên$/.test(
                          form.companySize,
                        ) && <option>{form.companySize}</option>}
                    </select>
                  </Field>
                  <Field id="website" label="Website">
                    <input
                      id="website"
                      type="url"
                      placeholder="https://"
                      value={form.website}
                      maxLength={255}
                      onChange={(e) =>
                        setForm({ ...form, website: e.target.value })
                      }
                    />
                  </Field>
                  <Field id="description" label="Giới thiệu công ty">
                    {' '}
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
                <>
                  <div className="form-grid">
                    <Field id="school" label="Trường đang học">
                      <input
                        id="school"
                        maxLength={150}
                        value={form.school}
                        onChange={(e) =>
                          setForm({ ...form, school: e.target.value })
                        }
                      />
                    </Field>
                    <Field id="major" label="Ngành học">
                      <input
                        id="major"
                        maxLength={150}
                        value={form.major}
                        onChange={(e) =>
                          setForm({ ...form, major: e.target.value })
                        }
                      />
                    </Field>
                  </div>
                  <div className="form-grid">
                    <Field id="studyYear" label="Năm học">
                      <select
                        id="studyYear"
                        value={form.studyYear}
                        onChange={(e) =>
                          setForm({ ...form, studyYear: e.target.value })
                        }
                      >
                        <option value="">Chọn năm</option>
                        {[1, 2, 3, 4, 5].map((y) => (
                          <option key={y} value={y}>
                            Năm {y}
                          </option>
                        ))}
                      </select>
                    </Field>
                  </div>
                  <div className="field">
                    <label>Nơi ở / khu vực muốn làm việc</label>
                    <MapPicker
                      value={
                        form.lat != null
                          ? { lat: form.lat, lng: form.lng }
                          : null
                      }
                      onChange={(p) =>
                        setForm((f) => ({
                          ...f,
                          lat: p.lat,
                          lng: p.lng,
                          preferredArea: p.label || f.preferredArea,
                        }))
                      }
                    />
                    <small>
                      {form.preferredArea
                        ? 'Khu vực: ' + form.preferredArea + '. '
                        : ''}
                      CampusWork dùng vị trí này để gợi ý việc gần bạn và tính
                      độ phù hợp. Nhà tuyển dụng chỉ thấy tên khu vực, không
                      thấy vị trí chính xác.
                    </small>
                  </div>
                  <Field
                    id="skills"
                    label="Kỹ năng"
                    hint="Ngăn cách bằng dấu phẩy, VD: Giao tiếp, Excel, Tiếng Anh"
                  >
                    <input
                      id="skills"
                      maxLength={500}
                      value={form.skills}
                      onChange={(e) =>
                        setForm({ ...form, skills: e.target.value })
                      }
                    />
                  </Field>
                  <fieldset className="check-group">
                    <legend>Ca có thể làm việc</legend>
                    {Object.entries(shiftLabels).map(([k, v]) => (
                      <label className="check-label" key={k}>
                        <input
                          type="checkbox"
                          checked={form.availableShifts.includes(k)}
                          onChange={(e) =>
                            setForm({
                              ...form,
                              availableShifts: e.target.checked
                                ? [...form.availableShifts, k]
                                : form.availableShifts.filter((x) => x !== k),
                            })
                          }
                        />
                        {v}
                      </label>
                    ))}
                  </fieldset>
                  <label
                    className={'open-work' + (form.openToWork ? ' on' : '')}
                  >
                    <input
                      type="checkbox"
                      checked={form.openToWork}
                      disabled={!form.availableShifts.length}
                      onChange={(e) =>
                        setForm({ ...form, openToWork: e.target.checked })
                      }
                    />
                    <span className="open-work-switch" />
                    <span>
                      <strong>Đang tìm việc</strong>
                      <small>
                        {form.availableShifts.length
                          ? 'Nhà tuyển dụng có thể thấy tên, trường, kỹ năng và ca làm của bạn để gửi lời mời. SĐT và email luôn được ẩn.'
                          : 'Hãy chọn ít nhất một ca có thể làm để bật tính năng này.'}
                      </small>
                    </span>
                  </label>
                  <label
                    className={
                      'open-work exam-mode' + (form.examMode ? ' on' : '')
                    }
                  >
                    <input
                      type="checkbox"
                      checked={form.examMode}
                      onChange={(e) =>
                        setForm({ ...form, examMode: e.target.checked })
                      }
                    />
                    <span className="open-work-switch" />
                    <span>
                      <strong>Chế độ mùa thi</strong>
                      <small>
                        Tạm ẩn bớt gợi ý việc làm mới để bạn tập trung ôn thi.
                        Có thể đặt sẵn khoảng ngày để tự bật/tắt đúng kỳ thi.
                      </small>
                    </span>
                  </label>
                  {form.examMode && (
                    <div className="exam-mode-range">
                      <Field id="examStartDate" label="Từ ngày">
                        <input
                          id="examStartDate"
                          type="date"
                          value={form.examStartDate}
                          onChange={(e) =>
                            setForm({ ...form, examStartDate: e.target.value })
                          }
                        />
                      </Field>
                      <Field id="examEndDate" label="Đến ngày">
                        <input
                          id="examEndDate"
                          type="date"
                          value={form.examEndDate}
                          onChange={(e) =>
                            setForm({ ...form, examEndDate: e.target.value })
                          }
                        />
                      </Field>
                      <small className="exam-mode-hint">
                        Để trống nếu muốn tự tắt thủ công thay vì theo ngày.
                      </small>
                    </div>
                  )}
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
                      onChange={(e) =>
                        setForm({ ...form, bio: e.target.value })
                      }
                    />
                  </Field>
                </>
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
