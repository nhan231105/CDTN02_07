import { Link } from 'react-router-dom'

const categoryMeta = {
  Hospitality: 'Nhà hàng – Quán cà phê',
  Sales: 'Bán hàng – Thu ngân',
  Marketing: 'Marketing – Content',
  Education: 'Gia sư – Trợ giảng',
  Design: 'Thiết kế – Sáng tạo',
  Development: 'Công nghệ thông tin',
}

export function HeroStats({ total, employers, locations }) {
  const tiles = [
    [total, 'Việc làm đang mở'],
    [employers, 'Nhà tuyển dụng'],
    [locations, 'Khu vực tuyển'],
    ['0đ', 'Phí cho sinh viên'],
  ]
  return (
    <div className="hero-stats">
      {tiles.map(([value, label]) => (
        <div className="stat-tile" key={label}>
          <strong>{value}</strong>
          <span>{label}</span>
        </div>
      ))}
      <div className="stat-banner">
        <strong>Lịch học bận? Không sao.</strong>
        <span>
          Chọn ca sáng, chiều, tối hoặc cuối tuần – công việc linh hoạt theo
          thời khóa biểu của bạn.
        </span>
      </div>
    </div>
  )
}

export default function Showcase({ summary, onPick }) {
  if (!summary) return null
  const { categories, locations, employers } = summary
  return (
    <>
      {categories.length > 0 && (
        <section className="container section showcase">
          <div className="section-heading">
            <div>
              <h2>Ngành nghề phổ biến</h2>
              <p className="muted">Chọn lĩnh vực bạn quan tâm</p>
            </div>
          </div>
          <div className="category-grid">
            {categories.map(([name, n]) => {
              const label = categoryMeta[name] || name
              return (
                <button
                  key={name}
                  className="category-card"
                  onClick={() => onPick({ category: name })}
                >
                  <strong>{label}</strong>
                  <span>{n} việc làm</span>
                </button>
              )
            })}
          </div>
        </section>
      )}
      {employers.length > 0 && (
        <section className="container section showcase">
          <div className="section-heading">
            <div>
              <h2>Nhà tuyển dụng hàng đầu</h2>
              <p className="muted">Doanh nghiệp đang tuyển sinh viên</p>
            </div>
          </div>
          <div className="employer-grid">
            {employers.slice(0, 6).map((e, i) => (
              <Link
                key={e.id}
                to={'/employers/' + e.id}
                className="employer-tile"
              >
                <span className={'company-logo large color-' + (i % 4)}>
                  {e.name.charAt(0)}
                </span>
                <strong>{e.name}</strong>
                <span className="badge-pill green">
                  {e.jobs} việc đang tuyển
                </span>
                <span className="tile-link">Xem công ty</span>
              </Link>
            ))}
          </div>
        </section>
      )}
      {locations.length > 0 && (
        <section className="container section showcase">
          <div className="section-heading">
            <div>
              <h2>Việc làm theo khu vực</h2>
            </div>
          </div>
          <div className="location-chips">
            {locations.map(([name, n]) => (
              <button
                key={name}
                className="location-chip"
                onClick={() => onPick({ location: name })}
              >
                {name} <span>{n}</span>
              </button>
            ))}
          </div>
        </section>
      )}
    </>
  )
}
