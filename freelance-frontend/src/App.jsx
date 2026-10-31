import {
  BrowserRouter,
  Navigate,
  Outlet,
  Route,
  Routes,
  useLocation,
} from 'react-router-dom'
import { useAuth } from './context/auth'
import Layout from './components/Layout'
import { Empty, Loading } from './components/UI'
import Login from './pages/Login'
import Register from './pages/Register'
import Jobs from './pages/Jobs'
import JobDetail from './pages/JobDetail'
import StudentProfile from './pages/StudentProfile'
import EmployerProfile from './pages/EmployerProfile'
import EmployerDashboard from './pages/EmployerDashboard'
import Applicants from './pages/Applicants'
import Applications from './pages/Applications'
import Notifications from './pages/Notifications'
import AdminDashboard from './pages/AdminDashboard'
import './App.css'
export function RequireAuth({ roles }) {
  const { user, loading } = useAuth()
  const location = useLocation()
  if (loading) return <Loading />
  if (!user)
    return (
      <Navigate
        to="/login"
        state={{ from: location.pathname + location.search }}
        replace
      />
    )
  if (roles && !roles.includes(user.role))
    return (
      <div className="container section">
        <Empty title="Bạn không có quyền truy cập">
          Hãy sử dụng chức năng phù hợp với tài khoản của bạn.
        </Empty>
      </div>
    )
  return <Outlet />
}
export function AppRoutes() {
  return (
    <Routes>
      <Route element={<Layout />}>
        <Route index element={<Navigate to="/jobs" replace />} />
        <Route path="jobs" element={<Jobs />} />
        <Route path="jobs/:id" element={<JobDetail />} />
        <Route path="login" element={<Login />} />
        <Route path="register" element={<Register />} />
        <Route element={<RequireAuth />}>
          <Route path="notifications" element={<Notifications />} />
        </Route>
        <Route element={<RequireAuth roles={['STUDENT']} />}>
          <Route path="student/profile" element={<StudentProfile />} />
          <Route path="student/applications" element={<Applications />} />
        </Route>
        <Route element={<RequireAuth roles={['EMPLOYER']} />}>
          <Route path="employer/jobs" element={<EmployerDashboard />} />
          <Route
            path="employer/jobs/:id/applications"
            element={<Applicants />}
          />
          <Route path="employer/profile" element={<EmployerProfile />} />
        </Route>
        <Route element={<RequireAuth roles={['ADMIN']} />}>
          <Route path="admin" element={<AdminDashboard />} />
        </Route>
        <Route
          path="*"
          element={
            <div className="container section">
              <Empty title="Không tìm thấy trang">
                Bạn có thể quay lại mục Khám phá việc làm.
              </Empty>
            </div>
          }
        />
      </Route>
    </Routes>
  )
}
export default function App() {
  return (
    <BrowserRouter>
      <AppRoutes />
    </BrowserRouter>
  )
}
