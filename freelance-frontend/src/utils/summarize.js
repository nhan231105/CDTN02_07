export function summarize(jobs) {
  const count = (key) =>
    Object.entries(
      jobs.reduce((acc, job) => {
        const value = job[key]
        if (value) acc[value] = (acc[value] || 0) + 1
        return acc
      }, {}),
    ).sort((a, b) => b[1] - a[1])
  const employers = Object.values(
    jobs.reduce((acc, job) => {
      if (!job.employerId) return acc
      const e = acc[job.employerId] || {
        id: job.employerId,
        name: job.employerName || 'Nhà tuyển dụng',
        jobs: 0,
      }
      e.jobs += 1
      acc[job.employerId] = e
      return acc
    }, {}),
  ).sort((a, b) => b.jobs - a.jobs)
  return {
    categories: count('category'),
    locations: count('location'),
    employers,
  }
}
