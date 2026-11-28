export default function ProjectDataNotice({ id }) {
  return (
    <p id={id} className="project-data-notice">
      <svg viewBox="0 0 20 20" fill="none" aria-hidden="true">
        <circle cx="10" cy="10" r="7.25" />
        <path d="M10 9v4.5m0-7h.01" />
      </svg>
      <span>Dữ liệu tuyển dụng minh họa phục vụ đồ án, không phải tuyển dụng thực tế.</span>
    </p>
  )
}
