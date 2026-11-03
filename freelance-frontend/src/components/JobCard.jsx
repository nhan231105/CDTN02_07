import { Link } from 'react-router-dom'
import { Status } from './UI'
import SaveButton from './SaveButton'
import CompanyLogo from './CompanyLogo'
import { kmText } from '../utils/geo'
import { jobTypeLabels, salaryText, shiftLabels } from '../utils/format'

export default function JobCard({
  job,
  index = 0,
  saved,
  onToggleSave,
  match,
}) {
  const shifts = (job.shifts || []).map((s) => shiftLabels[s] || s)
  return (
    <Link className="job-card" to={'/jobs/' + job.id}>
      <div className="job-card-top">
        <CompanyLogo
          id={job.employerId}
          name={job.employerName || job.category}
          className={'color-' + (index % 4)}
        />
        <div className="job-card-actions">
          <Status value={job.status} />
          {onToggleSave && <SaveButton saved={saved} onToggle={onToggleSave} />}
        </div>
      </div>
      {job.riskLevel && job.riskLevel !== 'NONE' && (
        <span className={'risk-tag ' + job.riskLevel.toLowerCase()}>
          Cẩn trọng: có dấu hiệu đáng ngờ
        </span>
      )}
      <p className="company-name">{job.employerName || 'Nhà tuyển dụng'}</p>
      <h3>{job.title}</h3>
      <div className="job-tags">
        {job.distanceKm != null && (
          <span className="near-tag">Cách bạn {kmText(job.distanceKm)}</span>
        )}
        <span>{job.location || 'Linh hoạt'}</span>
        <span>{jobTypeLabels[job.jobType] || job.workSchedule}</span>
        {job.hoursPerWeek ? <span>{job.hoursPerWeek} giờ/tuần</span> : null}
      </div>
      <div className="job-badges">
        {job.noExperienceRequired && (
          <span className="badge-pill green">Không cần kinh nghiệm</span>
        )}
        {job.studentFriendly && (
          <span className="badge-pill blue">Thân thiện sinh viên</span>
        )}
        {shifts.slice(0, 3).map((s) => (
          <span className="badge-pill" key={s}>
            {s}
          </span>
        ))}
      </div>
      {match && (
        <div className={'match-bar ' + match.level}>
          <div className="match-track">
            <span style={{ width: match.score + '%' }} />
          </div>
          <b>Phù hợp {match.score}%</b>
          <small>{match.reasons[0] || match.gaps[0]}</small>
        </div>
      )}
      <div className="job-card-bottom">
        <strong>{salaryText(job)}</strong>
        <span className="card-cta">Xem chi tiết</span>
      </div>
    </Link>
  )
}
