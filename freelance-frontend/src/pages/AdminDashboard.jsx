import { useCallback, useEffect, useMemo, useState } from 'react'
import api from '../api/apiService'
import {
  Empty,
  Feedback,
  Field,
  Loading,
  Modal,
  Pagination,
  Status,
} from '../components/UI'
import { date } from '../utils/format'

const pageSize = 12
const roleLabels = {
  STUDENT: 'Sinh viên',
  EMPLOYER: 'Nhà tuyển dụng',
  ADMIN: 'Quản trị viên',
}
const reportTargetLabels = { USER: 'Tài khoản', JOB: 'Tin tuyển dụng' }
const unwrap = (response) =>
  response?.data !== undefined ? response.data : response

export default function AdminDashboard() {
  const [stats, setStats] = useState(null)
  const [tab, setTab] = useState('users')
  const [error, setError] = useState('')
  const [message, setMessage] = useState('')
  const [loadingStats, setLoadingStats] = useState(true)

  const [users, setUsers] = useState([])
  const [usersLoading, setUsersLoading] = useState(true)
  const [userKeyword, setUserKeyword] = useState('')
  const [userRole, setUserRole] = useState('')
  const [userStatus, setUserStatus] = useState('')
  const [userPage, setUserPage] = useState(0)

  const [jobs, setJobs] = useState(null)
  const [jobsLoading, setJobsLoading] = useState(true)
  const [reports, setReports] = useState(null)
  const [reportsLoading, setReportsLoading] = useState(true)
  const [reportPage, setReportPage] = useState(0)
  const [action, setAction] = useState(null)
  const [actionValue, setActionValue] = useState('')
  const [actionError, setActionError] = useState('')
  const [saving, setSaving] = useState(false)

  const loadStats = useCallback(async () => {
    try {
      const response = unwrap(await api.stats())
      setStats(response)
    } catch (err) {
      setError(err.message)
    } finally {
      setLoadingStats(false)
    }
  }, [])

  const loadUsers = useCallback(async (filters = {}) => {
    try {
      const keyword = (filters.keyword ?? '').trim()
      const role = filters.role || ''
      const status = filters.status || ''
      let found
      if (keyword) {
        found = unwrap(await api.adminUsers({ keyword }))
      } else if (role) {
        found = unwrap(await api.adminUsers({ role }))
      } else {
        const [students, employers] = await Promise.all([
          api.adminUsers({ role: 'STUDENT' }),
          api.adminUsers({ role: 'EMPLOYER' }),
        ])
        found = [...unwrap(students), ...unwrap(employers)]
      }
      const filtered = (Array.isArray(found) ? found : []).filter(
        (user) =>
          (role ? user.role === role : user.role !== 'ADMIN') &&
          (!status || user.status === status),
      )
      setUsers(filtered)
      setUserPage(0)
    } catch (err) {
      setError(err.message)
    } finally {
      setUsersLoading(false)
    }
  }, [])

  const loadJobs = useCallback(async (page = 0) => {
    try {
      setJobs(await api.adminAllJobs(page))
    } catch (err) {
      setError(err.message)
    } finally {
      setJobsLoading(false)
    }
  }, [])

  const loadReports = useCallback(async (page = 0) => {
    try {
      setReports(await api.adminReports(page))
      setReportPage(page)
    } catch (err) {
      setError(err.message)
    } finally {
      setReportsLoading(false)
    }
  }, [])

  useEffect(() => {
    Promise.resolve().then(loadStats)
  }, [loadStats])
  useEffect(() => {
    Promise.resolve().then(loadUsers)
  }, [loadUsers])

  useEffect(() => {
    if (tab === 'jobs' && !jobs) Promise.resolve().then(() => loadJobs())
    if (tab === 'reports' && !reports)
      Promise.resolve().then(() => loadReports())
  }, [tab, jobs, reports, loadJobs, loadReports])

  const visibleUsers = useMemo(
    () => users.slice(userPage * pageSize, (userPage + 1) * pageSize),
    [users, userPage],
  )
  const userTotalPages = Math.ceil(users.length / pageSize)

  const openAction = (type, target) => {
    setAction({ type, target })
    setActionValue('')
    setActionError('')
  }
  const closeAction = () => {
    if (!saving) {
      setAction(null)
      setActionError('')
    }
  }

  const submitAction = async (event) => {
    event.preventDefault()
    if (!action) return
    const needsValue = ['lock', 'removeJob', 'resolveReport'].includes(
      action.type,
    )
    if (needsValue && !actionValue.trim()) {
      setActionError(
        action.type === 'resolveReport'
          ? 'Vui lòng nhập quyết định xử lý.'
          : 'Vui lòng nhập lý do.',
      )
      return
    }
    setSaving(true)
    setActionError('')
    setError('')
    setMessage('')
    try {
      if (action.type === 'lock') {
        await api.adminSetUserStatus(
          action.target.id,
          'LOCKED',
          actionValue.trim(),
        )
        setUsers((current) => {
          const updated = current.map((user) =>
            user.id === action.target.id ? { ...user, status: 'LOCKED' } : user,
          )
          return userStatus === 'ACTIVE'
            ? updated.filter((user) => user.id !== action.target.id)
            : updated
        })
        setMessage('Đã khóa tài khoản.')
      } else if (action.type === 'unlock') {
        await api.adminSetUserStatus(action.target.id, 'ACTIVE')
        setUsers((current) => {
          const updated = current.map((user) =>
            user.id === action.target.id ? { ...user, status: 'ACTIVE' } : user,
          )
          return userStatus === 'LOCKED'
            ? updated.filter((user) => user.id !== action.target.id)
            : updated
        })
        setMessage('Đã mở khóa tài khoản.')
      } else if (action.type === 'removeJob') {
        await api.adminRemoveJob(action.target.id, actionValue.trim())
        await loadJobs(jobs?.number || 0)
        setMessage('Đã gỡ tin tuyển dụng.')
      } else if (action.type === 'restoreJob') {
        await api.adminRestoreJob(action.target.id)
        await loadJobs(jobs?.number || 0)
        setMessage('Đã duyệt lại tin tuyển dụng.')
      } else if (action.type === 'deleteUser') {
        await api.adminDeleteUser(action.target.id)
        setUsers((current) =>
          current.filter((user) => user.id !== action.target.id),
        )
        setMessage('Đã xóa tài khoản.')
      } else if (action.type === 'resolveReport') {
        await api.adminResolveReport(action.target.id, actionValue.trim())
        await loadReports(reportPage)
        setMessage('Đã ghi nhận quyết định xử lý báo cáo.')
      } else if (action.type === 'rejectReport') {
        await api.adminRejectReport(action.target.id)
        await loadReports(reportPage)
        setMessage('Đã từ chối báo cáo.')
      }
      setAction(null)
      loadStats()
    } catch (err) {
      setActionError(err.message)
    } finally {
      setSaving(false)
    }
  }

  return (
    <div className="container section admin-dashboard">
      <span className="eyebrow">QUẢN LÝ NỀN TẢNG</span>
      <h1>Không gian quản trị</h1>
      <p className="muted">
        Quản lý tài khoản, kiểm duyệt tin tuyển dụng và xử lý báo cáo vi phạm.
      </p>
      <Feedback error={error} message={message} />

      {stats ? (
        <div className="stats-grid">
          {[
            ['Tổng tài khoản', stats.totalUsers],
            ['Sinh viên', stats.totalStudents],
            ['Nhà tuyển dụng', stats.totalEmployers],
            ['Đang hoạt động', stats.activeUsers],
            ['Đã khóa', stats.lockedUsers],
          ].map(([label, value]) => (
            <div className="surface stat-card" key={label}>
              <span>{label}</span>
              <strong>{value}</strong>
            </div>
          ))}
        </div>
      ) : (
        loadingStats && <Loading />
      )}

      <nav className="admin-tabs" aria-label="Chức năng quản trị">
        {[
          ['users', 'Tài khoản'],
          ['jobs', 'Tin tuyển dụng'],
          ['reports', 'Báo cáo vi phạm'],
        ].map(([key, label]) => (
          <button
            type="button"
            className={tab === key ? 'admin-tab active' : 'admin-tab'}
            key={key}
            aria-pressed={tab === key}
            onClick={() => {
              setTab(key)
              setError('')
              setMessage('')
            }}
          >
            {label}
          </button>
        ))}
      </nav>

      {tab === 'users' && (
        <section className="surface admin-panel" aria-labelledby="users-title">
          <div className="admin-panel-heading">
            <div>
              <h2 id="users-title">Quản lý tài khoản người dùng</h2>
              <p className="muted">
                Tìm kiếm sinh viên và nhà tuyển dụng; khóa hoặc mở khóa tài
                khoản vi phạm.
              </p>
            </div>
          </div>
          <form
            className="admin-filters"
            onSubmit={(event) => {
              event.preventDefault()
              setUsersLoading(true)
              setError('')
              loadUsers({
                keyword: userKeyword,
                role: userRole,
                status: userStatus,
              })
            }}
          >
            <Field label="Tìm tài khoản" id="admin-user-search">
              <input
                id="admin-user-search"
                value={userKeyword}
                onChange={(event) => setUserKeyword(event.target.value)}
                placeholder="Tên, email hoặc số điện thoại"
              />
            </Field>
            <Field label="Vai trò" id="admin-user-role">
              <select
                id="admin-user-role"
                value={userRole}
                onChange={(event) => setUserRole(event.target.value)}
              >
                <option value="">Tất cả</option>
                <option value="STUDENT">Sinh viên</option>
                <option value="EMPLOYER">Nhà tuyển dụng</option>
                <option value="ADMIN">Quản trị viên</option>
              </select>
            </Field>
            <Field label="Trạng thái" id="admin-user-status">
              <select
                id="admin-user-status"
                value={userStatus}
                onChange={(event) => setUserStatus(event.target.value)}
              >
                <option value="">Tất cả</option>
                <option value="ACTIVE">Hoạt động</option>
                <option value="LOCKED">Đã khóa</option>
              </select>
            </Field>
            <button className="btn primary admin-search-button" type="submit">
              Tìm kiếm
            </button>
          </form>

          {usersLoading ? (
            <Loading />
          ) : users.length ? (
            <>
              <div className="admin-table-wrap">
                <table className="admin-table">
                  <thead>
                    <tr>
                      <th>Tài khoản</th>
                      <th>Liên hệ</th>
                      <th>Vai trò</th>
                      <th>Trạng thái</th>
                      <th>Thao tác</th>
                    </tr>
                  </thead>
                  <tbody>
                    {visibleUsers.map((user) => (
                      <tr key={user.id}>
                        <td>
                          <strong>{user.fullName}</strong>
                          <small>#{user.id}</small>
                        </td>
                        <td>
                          <span>{user.email}</span>
                          <small>{user.phone}</small>
                        </td>
                        <td>{roleLabels[user.role] || user.role}</td>
                        <td>
                          <Status value={user.status} />
                        </td>
                        <td>
                          <div className="admin-row-actions">
                            {user.status === 'LOCKED' ? (
                              <button
                                className="btn secondary small"
                                type="button"
                                onClick={() => openAction('unlock', user)}
                              >
                                Mở khóa
                              </button>
                            ) : (
                              <button
                                className="btn secondary small"
                                type="button"
                                onClick={() => openAction('lock', user)}
                              >
                                Khóa tài khoản
                              </button>
                            )}
                            <button
                              className="btn danger small"
                              type="button"
                              onClick={() => openAction('deleteUser', user)}
                            >
                              Xóa
                            </button>
                          </div>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
              {userTotalPages > 1 && (
                <Pagination
                  page={{ number: userPage, totalPages: userTotalPages }}
                  onChange={setUserPage}
                />
              )}
            </>
          ) : (
            <Empty title="Không tìm thấy tài khoản">
              Thử thay đổi từ khóa hoặc bộ lọc tìm kiếm.
            </Empty>
          )}
        </section>
      )}

      {tab === 'jobs' && (
        <section className="surface admin-panel" aria-labelledby="jobs-title">
          <div className="admin-panel-heading">
            <div>
              <h2 id="jobs-title">Kiểm duyệt tin tuyển dụng</h2>
              <p className="muted">
                Xem toàn bộ tin tuyển dụng, gỡ nội dung vi phạm hoặc duyệt lại
                tin đã gỡ trước đó.
              </p>
            </div>
            <button
              className="btn secondary small"
              type="button"
              onClick={() => {
                setJobsLoading(true)
                setError('')
                loadJobs(jobs?.number || 0)
              }}
            >
              Làm mới
            </button>
          </div>
          {jobsLoading ? (
            <Loading />
          ) : jobs?.content?.length ? (
            <>
              <div className="admin-table-wrap">
                <table className="admin-table">
                  <thead>
                    <tr>
                      <th>Tin tuyển dụng</th>
                      <th>Nhà tuyển dụng</th>
                      <th>Hạn ứng tuyển</th>
                      <th>Trạng thái</th>
                      <th>Thao tác</th>
                    </tr>
                  </thead>
                  <tbody>
                    {jobs.content.map((job) => (
                      <tr key={job.id}>
                        <td>
                          <strong>{job.title}</strong>
                          <small>
                            {job.category || 'Chưa phân loại'} ·{' '}
                            {job.location || 'Chưa có địa điểm'}
                          </small>
                        </td>
                        <td>{job.employerName || `#${job.employerId}`}</td>
                        <td>{date(job.deadline)}</td>
                        <td>
                          {job.removedByAdmin ? (
                            <span className="status-locked">Đã gỡ</span>
                          ) : (
                            <Status value={job.status} />
                          )}
                        </td>
                        <td>
                          {job.removedByAdmin ? (
                            <button
                              className="btn secondary small"
                              type="button"
                              onClick={() => openAction('restoreJob', job)}
                            >
                              Duyệt lại
                            </button>
                          ) : (
                            <button
                              className="btn secondary small"
                              type="button"
                              onClick={() => openAction('removeJob', job)}
                            >
                              Gỡ tin
                            </button>
                          )}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
              <Pagination
                page={jobs}
                onChange={(page) => {
                  setJobsLoading(true)
                  setError('')
                  loadJobs(page)
                }}
              />
            </>
          ) : (
            <Empty title="Không có tin tuyển dụng">
              Hiện không có tin công khai cần kiểm duyệt.
            </Empty>
          )}
        </section>
      )}

      {tab === 'reports' && (
        <section
          className="surface admin-panel"
          aria-labelledby="reports-title"
        >
          <div className="admin-panel-heading">
            <div>
              <h2 id="reports-title">Xử lý báo cáo vi phạm</h2>
              <p className="muted">
                Tiếp nhận báo cáo đang chờ và lưu quyết định xử lý.
              </p>
            </div>
            <button
              className="btn secondary small"
              type="button"
              onClick={() => {
                setReportsLoading(true)
                setError('')
                loadReports(reportPage)
              }}
            >
              Làm mới
            </button>
          </div>
          {reportsLoading ? (
            <Loading />
          ) : reports?.content?.length ? (
            <>
              <div className="admin-table-wrap">
                <table className="admin-table">
                  <thead>
                    <tr>
                      <th>Báo cáo</th>
                      <th>Đối tượng</th>
                      <th>Lý do</th>
                      <th>Ngày gửi</th>
                      <th>Thao tác</th>
                    </tr>
                  </thead>
                  <tbody>
                    {reports.content.map((report) => (
                      <tr key={report.id}>
                        <td>
                          <strong>Báo cáo #{report.id}</strong>
                          <small>Người gửi: #{report.reporterId}</small>
                        </td>
                        <td>
                          {reportTargetLabels[report.targetType] ||
                            report.targetType}{' '}
                          #{report.targetId}
                        </td>
                        <td className="admin-report-reason">{report.reason}</td>
                        <td>{date(report.createdAt)}</td>
                        <td>
                          <div className="admin-row-actions">
                            <button
                              className="btn primary small"
                              type="button"
                              onClick={() =>
                                openAction('resolveReport', report)
                              }
                            >
                              Xử lý
                            </button>
                            <button
                              className="btn secondary small"
                              type="button"
                              onClick={() => openAction('rejectReport', report)}
                            >
                              Từ chối
                            </button>
                          </div>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
              <Pagination
                page={reports}
                onChange={(page) => {
                  setReportsLoading(true)
                  setError('')
                  loadReports(page)
                }}
              />
            </>
          ) : (
            <Empty title="Không có báo cáo đang chờ">
              Các báo cáo vi phạm mới sẽ xuất hiện tại đây.
            </Empty>
          )}
        </section>
      )}

      {action && (
        <Modal
          title={
            action.type === 'lock'
              ? 'Khóa tài khoản'
              : action.type === 'unlock'
                ? 'Mở khóa tài khoản'
                : action.type === 'deleteUser'
                  ? 'Xóa tài khoản'
                  : action.type === 'removeJob'
                    ? 'Gỡ tin tuyển dụng'
                    : action.type === 'restoreJob'
                      ? 'Duyệt lại tin tuyển dụng'
                      : action.type === 'resolveReport'
                        ? 'Xử lý báo cáo'
                        : 'Từ chối báo cáo'
          }
          onClose={closeAction}
        >
          <form onSubmit={submitAction}>
            <p className="muted">
              {action.type === 'lock' &&
                `Tài khoản: ${action.target.fullName} (${action.target.email})`}
              {action.type === 'unlock' &&
                `Mở khóa tài khoản ${action.target.fullName}?`}
              {action.type === 'deleteUser' &&
                `Xác nhận xóa vĩnh viễn tài khoản ${action.target.fullName} (${action.target.email})? Thao tác này sẽ bị từ chối nếu tài khoản còn dữ liệu liên quan (tin tuyển dụng, đơn ứng tuyển, đánh giá, báo cáo).`}
              {action.type === 'removeJob' &&
                `Tin tuyển dụng: ${action.target.title}`}
              {action.type === 'restoreJob' &&
                `Xác nhận duyệt lại tin tuyển dụng "${action.target.title}"?`}
              {action.type === 'resolveReport' &&
                `Báo cáo #${action.target.id} về ${reportTargetLabels[action.target.targetType] || action.target.targetType} #${action.target.targetId}.`}
              {action.type === 'rejectReport' &&
                `Xác nhận từ chối báo cáo #${action.target.id}?`}
            </p>
            {action.type === 'lock' && (
              <Field label="Lý do khóa" id="admin-action-value">
                <textarea
                  id="admin-action-value"
                  rows="3"
                  maxLength="500"
                  required
                  value={actionValue}
                  onChange={(event) => setActionValue(event.target.value)}
                />
              </Field>
            )}
            {action.type === 'removeJob' && (
              <Field label="Lý do gỡ tin" id="admin-action-value">
                <textarea
                  id="admin-action-value"
                  rows="3"
                  maxLength="500"
                  required
                  value={actionValue}
                  onChange={(event) => setActionValue(event.target.value)}
                />
              </Field>
            )}
            {action.type === 'resolveReport' && (
              <Field
                label="Quyết định xử lý (ví dụ: cảnh cáo, khóa tài khoản)"
                id="admin-action-value"
              >
                <textarea
                  id="admin-action-value"
                  rows="3"
                  maxLength="100"
                  required
                  value={actionValue}
                  onChange={(event) => setActionValue(event.target.value)}
                />
              </Field>
            )}
            <Feedback error={actionError} />
            <div className="admin-modal-actions">
              <button
                className="btn secondary"
                type="button"
                onClick={closeAction}
                disabled={saving}
              >
                Hủy
              </button>
              <button className="btn primary" type="submit" disabled={saving}>
                {saving ? 'Đang xử lý…' : 'Xác nhận'}
              </button>
            </div>
          </form>
        </Modal>
      )}
    </div>
  )
}
