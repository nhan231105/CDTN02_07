import { useCallback, useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import api from '../api/apiService'
import { useMessage } from '../context/toast'
import { datetime } from '../utils/format'
import { Empty, Feedback, Loading } from '../components/UI'
import CompanyLogo from '../components/CompanyLogo'

const stateText = {
  PENDING: 'Đang chờ bạn phản hồi',
  ACCEPTED: 'Đã nhận lời',
  DECLINED: 'Đã từ chối',
}

export default function StudentInvitations() {
  const [items, setItems] = useState(null)
  const [error, setError] = useState('')
  const [needCv, setNeedCv] = useState(false)
  const [busy, setBusy] = useState(null)
  const [, setMessage] = useMessage()

  const load = useCallback(async () => {
    try {
      setItems(await api.myInvitations())
    } catch (err) {
      setError(err.message)
    }
  }, [])
  useEffect(() => {
    Promise.resolve().then(load)
  }, [load])

  const respond = async (item, accept) => {
    setBusy(item.id)
    setError('')
    setNeedCv(false)
    try {
      await api.respondInvitation(item.id, accept)
      setMessage(
        accept
          ? 'Đã nhận lời mời, đơn ứng tuyển của bạn đã được gửi.'
          : 'Đã từ chối lời mời.',
      )
      await load()
    } catch (err) {
      setError(err.message)
      setNeedCv(/CV/i.test(err.message))
    } finally {
      setBusy(null)
    }
  }

  const pending = items?.filter((i) => i.status === 'PENDING').length || 0

  return (
    <div className="container section narrow">
      <div className="section-heading">
        <div>
          <span className="eyebrow">NHÀ TUYỂN DỤNG TÌM BẠN</span>
          <h1>Lời mời ứng tuyển</h1>
          <p className="muted">
            {pending
              ? `Bạn có ${pending} lời mời đang chờ phản hồi.`
              : 'Bật “Đang tìm việc” trong hồ sơ để nhà tuyển dụng có thể tìm thấy bạn.'}
          </p>
        </div>
        <Link className="btn secondary" to="/student/profile">
          Hồ sơ của tôi
        </Link>
      </div>
      <Feedback error={error} />
      {needCv && (
        <p className="muted">
          <Link to="/student/profile">Tải CV lên hồ sơ</Link> rồi thử nhận lời
          mời lại nhé.
        </p>
      )}
      {!items && !error ? (
        <Loading />
      ) : items?.length === 0 ? (
        <Empty title="Chưa có lời mời nào">
          Hoàn thiện hồ sơ và bật chế độ tìm việc để được nhà tuyển dụng chú ý.
        </Empty>
      ) : (
        <div className="invite-list">
          {items?.map((i) => (
            <article
              className={'invite-card is-' + i.status.toLowerCase()}
              key={i.id}
            >
              <CompanyLogo id={i.employerId} name={i.employerName} />
              <div className="invite-body">
                <div className="invite-top">
                  <div>
                    <Link to={'/employers/' + i.employerId}>
                      <strong>{i.employerName}</strong>
                    </Link>
                    <span className="muted"> mời bạn ứng tuyển</span>
                  </div>
                  <span className={'invite-state s-' + i.status.toLowerCase()}>
                    {stateText[i.status]}
                  </span>
                </div>
                <Link className="invite-job" to={'/jobs/' + i.jobId}>
                  {i.jobTitle}
                </Link>
                <span className="muted small">
                  {i.location} · {datetime(i.createdAt)}
                </span>
                {i.message && <blockquote>{i.message}</blockquote>}
                {i.status === 'PENDING' && (
                  <div className="invite-actions">
                    <button
                      className="btn primary"
                      disabled={busy === i.id}
                      onClick={() => respond(i, true)}
                    >
                      Nhận lời mời và ứng tuyển
                    </button>
                    <button
                      className="btn secondary"
                      disabled={busy === i.id}
                      onClick={() => respond(i, false)}
                    >
                      Từ chối
                    </button>
                  </div>
                )}
              </div>
            </article>
          ))}
        </div>
      )}
    </div>
  )
}
