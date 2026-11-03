import { describe, expect, it } from 'vitest'
import { matchJob } from './match'

const profile = {
  availableShifts: ['EVENING'],
  preferredArea: 'Quận 1',
  skills: 'Giao tiếp, Excel',
}

describe('matchJob', () => {
  it('trả null khi thiếu hồ sơ', () => {
    expect(matchJob({ title: 'x' }, null)).toBeNull()
  })

  it('điểm cao khi trùng ca, khu vực và kỹ năng', () => {
    const r = matchJob(
      {
        title: 'Nhập liệu Excel',
        location: 'Quận 1',
        shifts: ['EVENING'],
        studentFriendly: true,
      },
      profile,
    )
    expect(r.score).toBeGreaterThanOrEqual(75)
    expect(r.level).toBe('high')
  })

  it('điểm thấp khi lệch ca và khu vực', () => {
    const r = matchJob(
      { title: 'Bảo vệ', location: 'Hà Nội', shifts: ['MORNING'] },
      profile,
    )
    expect(r.score).toBeLessThan(50)
    expect(r.gaps.length).toBeGreaterThan(0)
  })
})
