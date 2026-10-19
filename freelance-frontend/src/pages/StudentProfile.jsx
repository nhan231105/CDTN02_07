import { useAuth } from '../context/AuthContext'

export default function StudentProfile() {
  const { user } = useAuth()

  return (
    <div className="profile-page">
      <h1>Hồ Sơ Sinh Viên</h1>
      <div className="profile-info">
        <p><strong>Tên:</strong> {user?.fullName}</p>
        <p><strong>Email:</strong> {user?.email}</p>
        <p><strong>Số Điện Thoại:</strong> {user?.phone}</p>
        <p><strong>Vai Trò:</strong> Sinh Viên</p>
      </div>
    </div>
  )
}
