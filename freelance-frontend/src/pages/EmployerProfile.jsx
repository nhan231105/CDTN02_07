import { useAuth } from '../context/AuthContext'

export default function EmployerProfile() {
  const { user } = useAuth()

  return (
    <div className="profile-page">
      <h1>Hồ Sơ Công Ty</h1>
      <div className="profile-info">
        <p><strong>Tên Công Ty:</strong> {user?.fullName}</p>
        <p><strong>Email:</strong> {user?.email}</p>
        <p><strong>Số Điện Thoại:</strong> {user?.phone}</p>
        <p><strong>Vai Trò:</strong> Nhà Tuyển Dụng</p>
      </div>
    </div>
  )
}
