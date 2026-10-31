import { fireEvent, render, screen, waitFor } from '@testing-library/react'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { describe, expect, it, vi } from 'vitest'
import { AuthContext, homeFor, redirectAfterLogin } from '../context/auth'
import { RequireAuth } from '../App'
import Register from '../pages/Register'
import Jobs from '../pages/Jobs'
import { Pagination } from '../components/UI'
import { strongPassword } from '../utils/format'
import api from '../api/apiService'
vi.mock('../api/apiService', () => ({
  default: { jobs: vi.fn(), unread: vi.fn() },
}))
const withAuth = (component, value) =>
  render(
    <AuthContext.Provider value={{ loading: false, ...value }}>
      <MemoryRouter>{component}</MemoryRouter>
    </AuthContext.Provider>,
  )
describe('security and registration', () => {
  it('does not return a newly logged-in role to the previous account dashboard', () => {
    expect(redirectAfterLogin('ADMIN', '/employer/jobs')).toBe('/admin')
    expect(redirectAfterLogin('STUDENT', '/employer/jobs')).toBe('/jobs')
    expect(redirectAfterLogin('STUDENT', '/jobs/2')).toBe('/jobs/2')
    expect(redirectAfterLogin('STUDENT', '//external.test')).toBe('/jobs')
  })
  it.each([
    ['STUDENT', '/jobs'],
    ['EMPLOYER', '/employer/jobs'],
    ['ADMIN', '/admin'],
  ])('routes %s to its dashboard', (role, path) =>
    expect(homeFor(role)).toBe(path),
  )
  it('requires authentication before protected routes', () => {
    withAuth(
      <Routes>
        <Route element={<RequireAuth />}>
          <Route path="/" element={<p>Secret</p>} />
        </Route>
        <Route path="/login" element={<p>Login required</p>} />
      </Routes>,
      { user: null },
    )
    expect(screen.getByText('Login required')).toBeInTheDocument()
    expect(screen.queryByText('Secret')).not.toBeInTheDocument()
  })
  it('blocks the wrong role', () => {
    withAuth(
      <Routes>
        <Route element={<RequireAuth roles={['EMPLOYER']} />}>
          <Route path="/" element={<p>Secret</p>} />
        </Route>
      </Routes>,
      { user: { role: 'STUDENT' } },
    )
    expect(screen.getByText('Bạn không có quyền truy cập')).toBeInTheDocument()
  })
  it.each(['123456', 'password!', 'PASSWORD!', 'Password123'])(
    'rejects weak password %s',
    (password) => expect(strongPassword(password)).toBe(false),
  )
  it('accepts strong password', () =>
    expect(strongPassword('Password123!')).toBe(true))
  it('registers and redirects to login without creating a session', async () => {
    const register = vi.fn().mockResolvedValue({ success: true })
    withAuth(
      <Routes>
        <Route path="/" element={<Register />} />
        <Route path="/login" element={<p>Registered login</p>} />
      </Routes>,
      { register },
    )
    fireEvent.change(screen.getByLabelText('Họ và tên'), {
      target: { value: 'Student Test' },
    })
    fireEvent.change(screen.getByLabelText('Email'), {
      target: { value: 'student@test.local' },
    })
    fireEvent.change(screen.getByLabelText('Số điện thoại'), {
      target: { value: '0912345678' },
    })
    fireEvent.change(screen.getByLabelText('Mật khẩu'), {
      target: { value: 'Password123!' },
    })
    fireEvent.change(screen.getByLabelText('Xác nhận mật khẩu'), {
      target: { value: 'Password123!' },
    })
    fireEvent.click(screen.getByRole('checkbox'))
    fireEvent.click(screen.getByRole('button', { name: 'Tạo tài khoản →' }))
    expect(await screen.findByText('Registered login')).toBeInTheDocument()
    expect(register).toHaveBeenCalledWith(
      expect.objectContaining({ role: 'STUDENT' }),
    )
    expect(sessionStorage.length).toBe(0)
  })
})
describe('job browsing', () => {
  it('uses all filters and keeps search input mounted while loading', async () => {
    let resolveRequest
    api.jobs.mockImplementation(
      () =>
        new Promise((resolve) => {
          resolveRequest = resolve
        }),
    )
    render(
      <MemoryRouter initialEntries={['/?category=Design&location=Hue']}>
        <Jobs />
      </MemoryRouter>,
    )
    await waitFor(() => expect(api.jobs).toHaveBeenCalled())
    expect(api.jobs.mock.calls.at(-1)[0]).toMatchObject({
      category: 'Design',
      location: 'Hue',
      size: 12,
    })
    expect(screen.getByLabelText('Từ khóa tìm kiếm')).toBeInTheDocument()
    resolveRequest({ content: [], totalElements: 0, totalPages: 0 })
    expect(
      await screen.findByText('Chưa tìm thấy công việc phù hợp'),
    ).toBeInTheDocument()
  })
  it('renders pagination and allows next page', () => {
    const change = vi.fn()
    render(<Pagination page={{ number: 0, totalPages: 2 }} onChange={change} />)
    expect(screen.getByRole('button', { name: '← Trước' })).toBeDisabled()
    fireEvent.click(screen.getByRole('button', { name: 'Sau →' }))
    expect(change).toHaveBeenCalledWith(1)
  })
})
