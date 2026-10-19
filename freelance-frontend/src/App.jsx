import { useState } from 'react'
import { useAuth } from './context/AuthContext'
import './App.css'

// Pages
import Login from './pages/Login'
import Register from './pages/Register'
import Jobs from './pages/Jobs'
import EmployerDashboard from './pages/EmployerDashboard'
import StudentProfile from './pages/StudentProfile'
import EmployerProfile from './pages/EmployerProfile'
import AdminDashboard from './pages/AdminDashboard'

export default function App() {
  const { user, isAuthenticated, logout } = useAuth()
  const [currentPage, setCurrentPage] = useState('jobs')

  // Redirect based on authentication and role
  if (!isAuthenticated) {
    const page = currentPage
    if (page === 'login') return <Login onSuccess={() => setCurrentPage('jobs')} onRegisterClick={() => setCurrentPage('register')} />
    if (page === 'register') return <Register onSuccess={() => setCurrentPage('login')} onLoginClick={() => setCurrentPage('login')} />
    return (
      <div className="auth-redirect">
        <div className="auth-container">
          <h1>Freelance Marketplace</h1>
          <p>Sàn giao dịch việc làm cho sinh viên</p>
          <div className="auth-buttons">
            <button className="btn btn-primary" onClick={() => setCurrentPage('login')}>
              Đăng Nhập
            </button>
            <button className="btn btn-secondary" onClick={() => setCurrentPage('register')}>
              Đăng Ký
            </button>
          </div>
        </div>
      </div>
    )
  }

  return (
    <div className="app">
      {/* Header */}
      <header className="header">
        <div className="header-container">
          <h1 className="logo">Freelance Marketplace</h1>
          <nav className="nav">
            {!user.role || user.role === 'STUDENT' ? (
              <>
                <button className={`nav-item ${currentPage === 'jobs' ? 'active' : ''}`} 
                  onClick={() => setCurrentPage('jobs')}>
                  Công Việc
                </button>
                <button className={`nav-item ${currentPage === 'profile' ? 'active' : ''}`}
                  onClick={() => setCurrentPage('profile')}>
                  Hồ Sơ
                </button>
              </>
            ) : null}

            {user.role === 'EMPLOYER' && (
              <>
                <button className={`nav-item ${currentPage === 'dashboard' ? 'active' : ''}`}
                  onClick={() => setCurrentPage('dashboard')}>
                  Dashboard
                </button>
                <button className={`nav-item ${currentPage === 'profile' ? 'active' : ''}`}
                  onClick={() => setCurrentPage('profile')}>
                  Hồ Sơ Công Ty
                </button>
              </>
            )}

            {user.role === 'ADMIN' && (
              <button className={`nav-item ${currentPage === 'admin' ? 'active' : ''}`}
                onClick={() => setCurrentPage('admin')}>
                Quản Lý
              </button>
            )}
          </nav>
          <div className="user-info">
            <span>{user?.fullName}</span>
            <button className="btn-logout" onClick={logout}>Đăng Xuất</button>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <main className="main-content">
        {currentPage === 'jobs' && (!user.role || user.role === 'STUDENT') && (
          <Jobs />
        )}

        {currentPage === 'dashboard' && user.role === 'EMPLOYER' && (
          <EmployerDashboard />
        )}

        {currentPage === 'profile' && user.role === 'STUDENT' && (
          <StudentProfile />
        )}

        {currentPage === 'profile' && user.role === 'EMPLOYER' && (
          <EmployerProfile />
        )}

        {currentPage === 'admin' && user.role === 'ADMIN' && (
          <AdminDashboard />
        )}
      </main>

      {/* Footer */}
      <footer className="footer">
        <p>&copy; 2026 Freelance Marketplace. All rights reserved.</p>
      </footer>
    </div>
  )
}
