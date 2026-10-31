import { fireEvent, render, screen, waitFor } from '@testing-library/react'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import api from '../api/apiService'
import AdminDashboard from './AdminDashboard'

vi.mock('../api/apiService', () => ({
  default: {
    stats: vi.fn(),
    adminUsers: vi.fn(),
    adminSetUserStatus: vi.fn(),
    adminDeleteUser: vi.fn(),
    adminAllJobs: vi.fn(),
    adminRemoveJob: vi.fn(),
    adminRestoreJob: vi.fn(),
    adminReports: vi.fn(),
    adminResolveReport: vi.fn(),
    adminRejectReport: vi.fn(),
  },
}))

const student = {
  id: 8,
  fullName: 'Nguyen Van A',
  email: 'student@example.com',
  phone: '0912345678',
  role: 'STUDENT',
  status: 'ACTIVE',
}

describe('admin dashboard', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    api.stats.mockResolvedValue({
      data: {
        totalUsers: 1,
        totalStudents: 1,
        totalEmployers: 0,
        activeUsers: 1,
        lockedUsers: 0,
      },
    })
    api.adminUsers.mockImplementation(({ role }) =>
      Promise.resolve({ data: role === 'STUDENT' ? [student] : [] }),
    )
    api.adminSetUserStatus.mockResolvedValue(null)
    api.adminDeleteUser.mockResolvedValue(null)
    api.adminRestoreJob.mockResolvedValue(null)
    api.adminResolveReport.mockResolvedValue(null)
    api.adminRejectReport.mockResolvedValue(null)
    api.adminAllJobs.mockResolvedValue({
      content: [],
      number: 0,
      totalPages: 0,
    })
    api.adminReports.mockResolvedValue({
      content: [],
      number: 0,
      totalPages: 0,
    })
  })

  it('lists users and locks an account with a required reason', async () => {
    render(<AdminDashboard />)

    expect(await screen.findByText('Nguyen Van A')).toBeInTheDocument()
    fireEvent.click(screen.getByRole('button', { name: 'Khóa tài khoản' }))
    fireEvent.change(screen.getByLabelText('Lý do khóa'), {
      target: { value: 'Vi phạm quy định nền tảng' },
    })
    fireEvent.click(screen.getByRole('button', { name: 'Xác nhận' }))

    await waitFor(() =>
      expect(api.adminSetUserStatus).toHaveBeenCalledWith(
        8,
        'LOCKED',
        'Vi phạm quy định nền tảng',
      ),
    )
    expect(
      await screen.findByText('Đã khóa', { selector: '.status' }),
    ).toBeInTheDocument()
  })

  it('loads pending reports and records the admin decision', async () => {
    api.adminReports.mockResolvedValue({
      content: [
        {
          id: 14,
          reporterId: 3,
          targetId: 9,
          targetType: 'JOB',
          reason: 'Tin có dấu hiệu lừa đảo',
          createdAt: '2026-10-30T12:00:00',
        },
      ],
      number: 0,
      totalPages: 1,
    })
    render(<AdminDashboard />)

    fireEvent.click(screen.getByRole('button', { name: 'Báo cáo vi phạm' }))
    expect(
      await screen.findByText('Tin có dấu hiệu lừa đảo'),
    ).toBeInTheDocument()
    fireEvent.click(screen.getByRole('button', { name: 'Xử lý' }))
    fireEvent.change(
      screen.getByLabelText(
        'Quyết định xử lý (ví dụ: cảnh cáo, khóa tài khoản)',
      ),
      { target: { value: 'Cảnh cáo tài khoản' } },
    )
    fireEvent.click(screen.getByRole('button', { name: 'Xác nhận' }))

    await waitFor(() =>
      expect(api.adminResolveReport).toHaveBeenCalledWith(
        14,
        'Cảnh cáo tài khoản',
      ),
    )
  })

  it('restores a job removed by admin', async () => {
    api.adminAllJobs.mockResolvedValue({
      content: [
        {
          id: 21,
          title: 'Tin bị gỡ',
          removedByAdmin: true,
          removalReason: 'Nội dung không phù hợp',
        },
      ],
      number: 0,
      totalPages: 1,
    })
    render(<AdminDashboard />)

    fireEvent.click(screen.getByRole('button', { name: 'Tin tuyển dụng' }))
    expect(await screen.findByText('Tin bị gỡ')).toBeInTheDocument()
    fireEvent.click(screen.getByRole('button', { name: 'Duyệt lại' }))
    fireEvent.click(screen.getByRole('button', { name: 'Xác nhận' }))

    await waitFor(() => expect(api.adminRestoreJob).toHaveBeenCalledWith(21))
  })

  it('deletes an account after confirmation', async () => {
    render(<AdminDashboard />)

    expect(await screen.findByText('Nguyen Van A')).toBeInTheDocument()
    fireEvent.click(screen.getByRole('button', { name: 'Xóa' }))
    fireEvent.click(screen.getByRole('button', { name: 'Xác nhận' }))

    await waitFor(() => expect(api.adminDeleteUser).toHaveBeenCalledWith(8))
  })

  it('shows the error when the account still has data', async () => {
    api.adminDeleteUser.mockRejectedValue(
      new Error('Tài khoản còn dữ liệu liên quan'),
    )
    render(<AdminDashboard />)

    expect(await screen.findByText('Nguyen Van A')).toBeInTheDocument()
    fireEvent.click(screen.getByRole('button', { name: 'Xóa' }))
    fireEvent.click(screen.getByRole('button', { name: 'Xác nhận' }))

    expect(
      await screen.findByText('Tài khoản còn dữ liệu liên quan'),
    ).toBeInTheDocument()
  })
})
