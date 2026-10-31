import { describe, expect, it, vi } from 'vitest'
import { hasApplication } from './applications'

describe('application eligibility across history pages', () => {
  it('finds an old application after the first history page', async () => {
    const getPage = vi
      .fn()
      .mockResolvedValueOnce({ content: [{ jobId: 20 }], totalPages: 3 })
      .mockResolvedValueOnce({ content: [{ jobId: 10 }], totalPages: 3 })
      .mockResolvedValueOnce({ content: [{ jobId: 1 }], totalPages: 3 })
    expect(await hasApplication('1', getPage)).toBe(true)
    expect(getPage.mock.calls).toEqual([[0], [1], [2]])
  })
  it('stops as soon as an application is found', async () => {
    const getPage = vi
      .fn()
      .mockResolvedValue({ content: [{ jobId: 1 }], totalPages: 3 })
    expect(await hasApplication(1, getPage)).toBe(true)
    expect(getPage).toHaveBeenCalledOnce()
  })
  it('returns false for an empty history', async () => {
    expect(
      await hasApplication(1, async () => ({ content: [], totalPages: 0 })),
    ).toBe(false)
  })
})
