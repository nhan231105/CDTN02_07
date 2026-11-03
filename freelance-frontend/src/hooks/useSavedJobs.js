import { useCallback, useEffect, useState } from 'react'
import api from '../api/apiService'
import { useAuth } from '../context/auth'
import { useToast } from '../context/toast'

export default function useSavedJobs() {
  const { user } = useAuth() || {}
  const isStudent = user?.role === 'STUDENT'
  const { push } = useToast()
  const [ids, setIds] = useState([])
  useEffect(() => {
    if (!isStudent) return
    let active = true
    api
      .savedJobIds()
      .then((list) => {
        if (active) setIds(list || [])
      })
      .catch(() => {})
    return () => {
      active = false
    }
  }, [isStudent])
  const toggle = useCallback(
    async (jobId) => {
      const saved = ids.includes(jobId)
      if (saved) await api.unsaveJob(jobId)
      else await api.saveJob(jobId)
      setIds((current) =>
        saved ? current.filter((x) => x !== jobId) : [...current, jobId],
      )
      push(saved ? 'Đã bỏ lưu việc làm.' : 'Đã lưu việc làm.')
      return !saved
    },
    [ids, push],
  )
  return { isStudent, isSaved: (jobId) => ids.includes(jobId), toggle }
}
