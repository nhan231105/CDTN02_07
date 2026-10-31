import { useEffect, useState } from 'react'
import { Link, NavLink, Outlet, useNavigate } from 'react-router-dom'
import { useAuth, homeFor } from '../context/auth'
import api from '../api/apiService'
const currentYear = new Date().getFullYear()
export default function Layout() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()
  const [unread, setUnread] = useState(0)
  useEffect(() => {
    if (!user) return
    let active = true
    const load = () => {
      if (!document.hidden)
        api
          .unread()
          .then((n) => {
            if (active) setUnread(typeof n === 'number' ? n : n.data || 0)
          })
          .catch(() => {})
    }
    load()
    const timer = setInterval(load, 30000)
    window.addEventListener('notifications-updated', load)
    document.addEventListener('visibilitychange', load)
    return () => {
      active = false
      clearInterval(timer)
      window.removeEventListener('notifications-updated', load)
      document.removeEventListener('visibilitychange', load)
    }
  }, [user])
  const signOut = () => {
    logout()
    setUnread(0)
    navigate('/login')
  }
  return (
    <div className="site-shell">
      <a className="skip-link" href="#main">
        Đi đến nội dung
      </a>
      <header className="site-header">
        <div className="nav-wrap">
          <Link to="/jobs" className="brand" aria-label="CampusWork trang chủ">
            <span className="brand-mark">
              c<span>·</span>
            </span>
            campus<span>work</span>
          </Link>
          <nav className="main-nav" aria-label="Điều hướng chính">
            <NavLink to="/jobs">Khám phá việc làm</NavLink>
            {user?.role === 'STUDENT' && (
              <NavLink to="/student/applications">Đơn của tôi</NavLink>
            )}
            {user?.role === 'EMPLOYER' && (
              <NavLink to="/employer/jobs">Quản lý tuyển dụng</NavLink>
            )}
            {user?.role === 'ADMIN' && <NavLink to="/admin">Quản trị</NavLink>}
          </nav>
          <div className="nav-actions">
            {user ? (
              <>
                <NavLink
                  to="/notifications"
                  className="notification-link"
                  aria-label={'Thông báo, ' + unread + ' chưa đọc'}
                >
                  <svg
                    viewBox="0 0 24 24"
                    width="21"
                    height="21"
                    fill="none"
                    stroke="currentColor"
                    strokeWidth="1.8"
                    aria-hidden="true"
                  >
                    <path d="M18 8a6 6 0 0 0-12 0c0 7-3 7-3 9h18c0-2-3-2-3-9M10 21h4" />
                  </svg>
                  {unread > 0 && <span className="badge">{unread}</span>}
                </NavLink>
                <Link
                  className="user-pill"
                  to={
                    user.role === 'STUDENT'
                      ? '/student/profile'
                      : user.role === 'EMPLOYER'
                        ? '/employer/profile'
                        : homeFor(user.role)
                  }
                >
                  <span className="avatar">{user.fullName?.charAt(0)}</span>
                  <span>{user.fullName?.split(' ').slice(-2).join(' ')}</span>
                </Link>
                <button className="text-button" onClick={signOut}>
                  Đăng xuất
                </button>
              </>
            ) : (
              <>
                <Link className="text-button" to="/login">
                  Đăng nhập
                </Link>
                <Link className="btn primary small" to="/register">
                  Tạo tài khoản ↗
                </Link>
              </>
            )}
          </div>
        </div>
      </header>
      <main id="main">
        <Outlet />
      </main>
      <footer className="site-footer">
        <div>
          <Link to="/jobs" className="brand">
            campus<span>work</span>
          </Link>
          <p>Mỗi trải nghiệm nhỏ. Một bước tiến lớn.</p>
        </div>
        <span>Kết nối sinh viên và cơ hội nghề nghiệp.</span>
        <small>© {currentYear} CampusWork</small>
      </footer>
    </div>
  )
}
