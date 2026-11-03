import { shiftLabels } from './format'
import { distanceKm, kmText } from './geo'

const norm = (s) =>
  (s || '')
    .toString()
    .toLowerCase()
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .replace(/đ/g, 'd')

const tokens = (s) =>
  norm(s)
    .split(/[,;/\n]+/)
    .map((t) => t.trim())
    .filter((t) => t.length >= 2)

// Điểm phù hợp 0-100 giữa hồ sơ sinh viên và việc làm, kèm lý do dễ đọc.
export function matchJob(job, profile) {
  if (!job || !profile) return null
  const reasons = []
  const gaps = []
  let earned = 0
  let possible = 0

  const mine = profile.availableShifts || []
  const needed = job.shifts || []
  if (mine.length && needed.length) {
    possible += 40
    const common = needed.filter((s) => mine.includes(s))
    earned += Math.round((40 * common.length) / needed.length)
    if (common.length)
      reasons.push(
        'Trùng ca rảnh: ' + common.map((s) => shiftLabels[s] || s).join(', '),
      )
    else gaps.push('Không trùng ca rảnh của bạn')
  }

  if (profile.lat != null && job.lat != null) {
    possible += 25
    const km = distanceKm(profile, job)
    const pts = km <= 3 ? 25 : km <= 7 ? 20 : km <= 15 ? 12 : km <= 30 ? 5 : 0
    earned += pts
    if (pts >= 12) reasons.push('Cách nơi ở của bạn ' + kmText(km))
    else gaps.push('Xa nơi ở của bạn (' + kmText(km) + ')')
  } else if (profile.preferredArea && (job.location || job.address)) {
    possible += 25
    const area = norm(profile.preferredArea)
    const where = norm(job.location + ' ' + (job.address || ''))
    if (where.includes(area) || area.includes(norm(job.location))) {
      earned += 25
      reasons.push('Đúng khu vực mong muốn: ' + job.location)
    } else gaps.push('Ngoài khu vực mong muốn')
  }

  const words = [...tokens(profile.skills), ...tokens(profile.major)]
  if (words.length) {
    possible += 25
    const text = norm(
      [job.title, job.category, job.description, job.requirements].join(' '),
    )
    const hit = words.filter((w) => text.includes(w))
    if (hit.length) {
      earned += Math.min(25, 12 + hit.length * 6)
      reasons.push('Khớp kỹ năng/ngành: ' + hit.slice(0, 3).join(', '))
    }
  }

  possible += 10
  if (job.studentFriendly) {
    earned += 5
    reasons.push('Thân thiện sinh viên')
  }
  if (job.noExperienceRequired) {
    earned += 5
    reasons.push('Không cần kinh nghiệm')
  }

  if (possible <= 10) return null
  const score = Math.min(100, Math.round((earned / possible) * 100))
  const level = score >= 75 ? 'high' : score >= 50 ? 'mid' : 'low'
  return { score, level, reasons, gaps }
}
