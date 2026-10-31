// Application history is paginated; checking only its first page loses old submissions.
export async function hasApplication(jobId, getPage) {
  let page = 0
  while (true) {
    const result = await getPage(page)
    if (
      result.content?.some(
        (application) => String(application.jobId) === String(jobId),
      )
    )
      return true
    page += 1
    if (page >= (result.totalPages || 0)) return false
  }
}
