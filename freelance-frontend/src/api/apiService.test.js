import { beforeEach, describe, expect, it, vi } from 'vitest'
import { ApiError, request, TOKEN_KEY, upload } from './apiService'
describe('HTTP client', () => {
  beforeEach(() => {
    vi.restoreAllMocks()
    sessionStorage.clear()
  })
  it('handles successful 204 without parsing JSON', async () => {
    const json = vi.fn()
    vi.stubGlobal(
      'fetch',
      vi.fn().mockResolvedValue({ status: 204, ok: true, json }),
    )
    expect(await request('PATCH', '/admin/users/1/status')).toBeNull()
    expect(json).not.toHaveBeenCalled()
  })
  it('adds bearer token and query parameters', async () => {
    sessionStorage.setItem(TOKEN_KEY, 'test-token')
    vi.stubGlobal(
      'fetch',
      vi.fn().mockResolvedValue({
        status: 200,
        ok: true,
        json: async () => ({ content: [] }),
      }),
    )
    await request('GET', '/jobs', null, {
      keyword: 'React',
      page: 1,
      category: '',
    })
    const [url, options] = fetch.mock.calls[0]
    expect(url.searchParams.get('keyword')).toBe('React')
    expect(url.searchParams.get('page')).toBe('1')
    expect(url.searchParams.has('category')).toBe(false)
    expect(options.headers.Authorization).toBe('Bearer test-token')
  })
  it('does not set JSON content type for upload', async () => {
    vi.stubGlobal(
      'fetch',
      vi.fn().mockResolvedValue({
        status: 201,
        ok: true,
        json: async () => ({ ref: 'file-id' }),
      }),
    )
    await upload(
      '/me/cv',
      new File(['pdf'], 'cv.pdf', { type: 'application/pdf' }),
    )
    expect(fetch.mock.calls[0][1].headers['Content-Type']).toBeUndefined()
    expect(fetch.mock.calls[0][1].body).toBeInstanceOf(FormData)
  })
  it('expires session on protected 401', async () => {
    sessionStorage.setItem(TOKEN_KEY, 'expired')
    const listener = vi.fn()
    window.addEventListener('session-expired', listener)
    vi.stubGlobal(
      'fetch',
      vi.fn().mockResolvedValue({
        status: 401,
        ok: false,
        json: async () => ({
          code: 'UNAUTHORIZED',
          message: 'Phiên hết hạn',
        }),
      }),
    )
    await expect(request('GET', '/me/profile')).rejects.toMatchObject({
      status: 401,
      code: 'UNAUTHORIZED',
    })
    expect(sessionStorage.getItem(TOKEN_KEY)).toBeNull()
    expect(listener).toHaveBeenCalledOnce()
    window.removeEventListener('session-expired', listener)
  })
  it('keeps a valid session on forbidden', async () => {
    sessionStorage.setItem(TOKEN_KEY, 'valid')
    vi.stubGlobal(
      'fetch',
      vi.fn().mockResolvedValue({
        status: 403,
        ok: false,
        json: async () => ({ message: 'Không có quyền' }),
      }),
    )
    await expect(request('GET', '/admin')).rejects.toBeInstanceOf(ApiError)
    expect(sessionStorage.getItem(TOKEN_KEY)).toBe('valid')
  })
  it('preserves backend validation errors', async () => {
    vi.stubGlobal(
      'fetch',
      vi.fn().mockResolvedValue({
        status: 400,
        ok: false,
        json: async () => ({
          code: 'VALIDATION_ERROR',
          message: 'Dữ liệu sai',
          fieldErrors: { email: 'Email sai' },
        }),
      }),
    )
    await expect(request('POST', '/auth/register', {})).rejects.toMatchObject({
      status: 400,
      fieldErrors: { email: 'Email sai' },
    })
  })
})
