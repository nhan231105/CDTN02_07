import { useCallback, useEffect, useRef, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import api, { upload } from '../api/apiService'
import { useAuth } from '../context/auth'
import { useMessage } from '../context/toast'
import { Feedback, Field, Loading } from '../components/UI'
import { brandImage } from '../utils/brand'
import { strongPassword } from '../utils/format'

const sizes = ['1-10', '10-50', '50-200', '200+']
const perkIdeas = [
  'Lương trả đúng hạn',
  'Chọn ca linh hoạt',
  'Đào tạo từ đầu',
  'Thưởng theo hiệu suất',
  'Bữa ăn miễn phí',
  'Cấp đồng phục',
  'Xác nhận thực tập',
  'Môi trường trẻ trung',
]
const IMAGE_RULE = {
  logo: { max: 2, label: 'Logo' },
  cover: { max: 4, label: 'Ảnh bìa' },
}

const toForm = (p) => ({
  fullName: p.fullName || '',
  orgName: p.orgName || '',
  tagline: p.tagline || '',
  industry: p.industry || '',
  companySize: p.companySize || '',
  foundedYear: p.foundedYear || '',
  website: p.website || '',
  address: p.address || '',
  description: p.description || '',
  benefits: p.benefits || [],
})

export default function EmployerProfile() {
  const { refresh, logout } = useAuth()
  const navigate = useNavigate()
  const [profile, setProfile] = useState(null)
  const [form, setForm] = useState(null)
  const [saved, setSaved] = useState('')
  const [error, setError] = useState('')
  const [, setMessage] = useMessage()
  const [busy, setBusy] = useState('')
  const [perk, setPerk] = useState('')
  const [images, setImages] = useState({ logo: '', cover: '' })
  const [account, setAccount] = useState({
    email: '',
    phone: '',
    password: '',
    currentPassword: '',
  })
  const previews = useRef([])

  const load = useCallback(async (keepForm = false) => {
    const response = await api.profile()
    const p = response.data || response
    setProfile(p)
    if (keepForm) return
    const next = toForm(p)
    setForm(next)
    setSaved(JSON.stringify(next))
  }, [])
  useEffect(() => {
    Promise.resolve()
      .then(() => load())
      .catch((err) => setError(err.message))
  }, [load])
  useEffect(() => {
    const urls = previews.current
    return () => urls.forEach((u) => URL.revokeObjectURL(u))
  }, [])

  const dirty = form && JSON.stringify(form) !== saved
  const set = (key) => (e) => setForm({ ...form, [key]: e.target.value })
  const addPerk = (text) => {
    const value = text.trim().slice(0, 60)
    if (!value || form.benefits.includes(value) || form.benefits.length >= 8)
      return
    setForm({ ...form, benefits: [...form.benefits, value] })
    setPerk('')
  }

  const run = async (name, action) => {
    setBusy(name)
    setError('')
    try {
      await action()
    } catch (err) {
      setError(err.message)
    } finally {
      setBusy('')
    }
  }

  const save = (e) => {
    e.preventDefault()
    run('save', async () => {
      await api.saveProfile({
        ...form,
        foundedYear: form.foundedYear ? Number(form.foundedYear) : null,
        benefits: form.benefits.join('|'),
      })
      await load()
      await refresh()
      setMessage('Đã lưu hồ sơ doanh nghiệp.')
    })
  }

  const pickImage = (e, kind) => {
    const file = e.target.files?.[0]
    e.target.value = ''
    if (!file) return
    const rule = IMAGE_RULE[kind]
    if (
      !['image/png', 'image/jpeg'].includes(file.type) ||
      file.size > rule.max * 1024 * 1024
    )
      return setError(
        rule.label + ' phải là PNG/JPEG, tối đa ' + rule.max + ' MB.',
      )
    const preview = URL.createObjectURL(file)
    previews.current.push(preview)
    setImages((current) => ({ ...current, [kind]: preview }))
    run(kind, async () => {
      await upload('/me/' + kind, file)
      await load(true)
      await refresh()
      setMessage(rule.label + ' đã được cập nhật.')
    })
  }

  const saveAccount = (e) => {
    e.preventDefault()
    if (account.password && !strongPassword(account.password))
      return setError(
        'Mật khẩu mới cần ít nhất 8 ký tự, có chữ hoa, chữ thường và ký tự đặc biệt.',
      )
    if (!account.email && !account.phone && !account.password)
      return setError('Nhập ít nhất một thông tin cần thay đổi.')
    run('account', async () => {
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
      setMessage('Đã cập nhật tài khoản. Có thể đổi lại sau 48 giờ.')
    })
  }

  if (!profile || !form)
    return (
      <div className="container section">
        {error ? <Feedback error={error} /> : <Loading />}
      </div>
    )

  const name = form.orgName || profile.fullName
  const logoSrc =
    images.logo ||
    (profile.logoRef ? brandImage(profile.id, 'logo', profile.logoRef) : '')
  const coverSrc =
    images.cover ||
    (profile.coverRef ? brandImage(profile.id, 'cover', profile.coverRef) : '')
  const facts = [form.industry, form.companySize, form.address].filter(Boolean)

  return (
    <div className="container section brand-studio">
      <div className="section-heading">
        <div>
          <span className="eyebrow">THƯƠNG HIỆU TUYỂN DỤNG</span>
          <h1>Hồ sơ doanh nghiệp</h1>
          <p className="muted">
            Đây là trang sinh viên nhìn thấy trước khi quyết định ứng tuyển.
          </p>
        </div>
        <Link className="btn secondary" to={'/employers/' + profile.id}>
          Xem trang công khai
        </Link>
      </div>
      <Feedback error={error} />

      <section className="surface brand-hero">
        <div
          className={'brand-cover' + (coverSrc ? ' has-image' : '')}
          style={coverSrc ? { backgroundImage: 'url(' + coverSrc + ')' } : null}
        >
          <label className="brand-edit cover-edit">
            <span aria-hidden="true">▣</span>
            {busy === 'cover'
              ? 'Đang tải…'
              : coverSrc
                ? 'Đổi ảnh bìa'
                : 'Thêm ảnh bìa'}
            <input
              type="file"
              accept="image/png,image/jpeg"
              disabled={Boolean(busy)}
              onChange={(e) => pickImage(e, 'cover')}
            />
          </label>
          {!coverSrc && <p>Ảnh bìa rộng, tỉ lệ 4:1, tối đa 4 MB</p>}
        </div>
        <div className="brand-id">
          <label className="brand-logo" title="Đổi logo">
            {logoSrc ? (
              <img src={logoSrc} alt="Logo công ty" />
            ) : (
              name.charAt(0)
            )}
            <span className="brand-logo-hint">
              {busy === 'logo' ? 'Đang tải…' : 'Đổi logo'}
            </span>
            <input
              type="file"
              accept="image/png,image/jpeg"
              disabled={Boolean(busy)}
              onChange={(e) => pickImage(e, 'logo')}
            />
          </label>
          <div className="brand-id-text">
            <h2>{name}</h2>
            <p>{form.tagline || 'Thêm một câu giới thiệu thật ấn tượng'}</p>
            {facts.length > 0 && (
              <div className="brand-facts">
                {facts.map((f) => (
                  <span key={f}>{f}</span>
                ))}
              </div>
            )}
          </div>
          <div className="brand-progress">
            <strong>{profile.completeness}%</strong>
            <div className="progress" aria-label="Mức độ hoàn thiện hồ sơ">
              <span style={{ width: profile.completeness + '%' }} />
            </div>
            <small>hồ sơ hoàn thiện</small>
          </div>
        </div>
        {profile.missingFields?.length > 0 && (
          <p className="brand-missing">
            Còn thiếu: {profile.missingFields.join(' · ')}
          </p>
        )}
      </section>

      <form className="brand-form" onSubmit={save}>
        <section className="surface">
          <h2>Thông tin chung</h2>
          <div className="form-grid">
            <Field id="orgName" label="Tên công ty">
              <input
                id="orgName"
                maxLength={150}
                value={form.orgName}
                onChange={set('orgName')}
              />
            </Field>
            <Field id="fullName" label="Người đại diện">
              <input
                id="fullName"
                required
                minLength={3}
                maxLength={100}
                value={form.fullName}
                onChange={set('fullName')}
              />
            </Field>
          </div>
          <Field
            id="tagline"
            label="Khẩu hiệu"
            hint={form.tagline.length + '/150 · một câu ngắn gọn, dễ nhớ'}
          >
            <input
              id="tagline"
              maxLength={150}
              placeholder="VD: Nơi sinh viên bắt đầu sự nghiệp đầu tiên"
              value={form.tagline}
              onChange={set('tagline')}
            />
          </Field>
          <div className="form-grid">
            <Field id="industry" label="Lĩnh vực hoạt động">
              <input
                id="industry"
                maxLength={100}
                value={form.industry}
                onChange={set('industry')}
              />
            </Field>
            <Field id="companySize" label="Quy mô nhân sự">
              <select
                id="companySize"
                value={form.companySize}
                onChange={set('companySize')}
              >
                <option value="">Chọn quy mô</option>
                {sizes.map((x) => (
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
          </div>
          <div className="form-grid">
            <Field id="foundedYear" label="Năm thành lập">
              <input
                id="foundedYear"
                type="number"
                min={1900}
                max={2100}
                placeholder="VD: 2018"
                value={form.foundedYear}
                onChange={set('foundedYear')}
              />
            </Field>
            <Field id="website" label="Website">
              <input
                id="website"
                type="url"
                maxLength={255}
                placeholder="https://"
                value={form.website}
                onChange={set('website')}
              />
            </Field>
          </div>
          <Field id="address" label="Địa chỉ">
            <input
              id="address"
              maxLength={255}
              value={form.address}
              onChange={set('address')}
            />
          </Field>
        </section>

        <section className="surface">
          <h2>Câu chuyện của bạn</h2>
          <Field
            id="description"
            label="Giới thiệu công ty"
            hint={
              form.description.length +
              '/3000 · nên có: bạn làm gì, văn hóa, cơ hội cho sinh viên'
            }
          >
            <textarea
              id="description"
              rows={7}
              maxLength={3000}
              value={form.description}
              onChange={set('description')}
            />
          </Field>
        </section>

        <section className="surface">
          <h2>Phúc lợi dành cho sinh viên</h2>
          <p className="muted">
            Tối đa 8 mục. Chúng hiển thị thành các thẻ nổi bật trên trang công
            ty.
          </p>
          <div className="perk-list">
            {form.benefits.map((b) => (
              <span className="perk" key={b}>
                {b}
                <button
                  type="button"
                  aria-label={'Xóa ' + b}
                  onClick={() =>
                    setForm({
                      ...form,
                      benefits: form.benefits.filter((x) => x !== b),
                    })
                  }
                >
                  ×
                </button>
              </span>
            ))}
            {form.benefits.length === 0 && (
              <span className="muted">Chưa có phúc lợi nào.</span>
            )}
          </div>
          <div className="perk-add">
            <input
              aria-label="Thêm phúc lợi"
              maxLength={60}
              placeholder="Nhập phúc lợi rồi nhấn Enter"
              value={perk}
              onChange={(e) => setPerk(e.target.value)}
              onKeyDown={(e) => {
                if (e.key === 'Enter') {
                  e.preventDefault()
                  addPerk(perk)
                }
              }}
            />
            <button
              type="button"
              className="btn secondary"
              onClick={() => addPerk(perk)}
            >
              Thêm
            </button>
          </div>
          <div className="perk-ideas">
            <small>Gợi ý nhanh</small>
            {perkIdeas
              .filter((x) => !form.benefits.includes(x))
              .map((x) => (
                <button type="button" key={x} onClick={() => addPerk(x)}>
                  + {x}
                </button>
              ))}
          </div>
        </section>

        <div className={'brand-savebar' + (dirty ? ' show' : '')}>
          <span>Bạn có thay đổi chưa lưu</span>
          <button
            type="button"
            className="btn secondary"
            onClick={() => setForm(JSON.parse(saved))}
          >
            Hoàn tác
          </button>
          <button className="btn primary" disabled={busy === 'save'}>
            {busy === 'save' ? 'Đang lưu…' : 'Lưu hồ sơ'}
          </button>
        </div>
      </form>

      <section className="surface brand-account">
        <h2>Bảo mật tài khoản</h2>
        <p className="muted">
          Đổi email, số điện thoại hoặc mật khẩu. Mỗi lần cập nhật cách nhau ít
          nhất 48 giờ. Email hiện tại: {profile.email}
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
          <div className="form-grid">
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
          </div>
          <button className="btn secondary" disabled={busy === 'account'}>
            Cập nhật tài khoản
          </button>
        </form>
      </section>
    </div>
  )
}
