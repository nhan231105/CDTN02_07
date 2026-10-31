import { test, expect } from '@playwright/test'

const API = 'http://localhost:8082/api'
const password = 'TestPass123!'
function pdfBuffer() {
  let pdf = '%PDF-1.4\n'
  const parts = [
    '<< /Type /Catalog /Pages 2 0 R >>',
    '<< /Type /Pages /Kids [3 0 R] /Count 1 >>',
    '<< /Type /Page /Parent 2 0 R /MediaBox [0 0 200 200] /Contents 4 0 R >>',
    '<< /Length 0 >>\nstream\n\nendstream',
  ]
  const offsets = [0]
  parts.forEach((part, index) => {
    offsets.push(Buffer.byteLength(pdf))
    pdf += index + 1 + ' 0 obj\n' + part + '\nendobj\n'
  })
  const xref = Buffer.byteLength(pdf)
  pdf +=
    'xref\n0 5\n0000000000 65535 f \n' +
    offsets
      .slice(1)
      .map((offset) => String(offset).padStart(10, '0') + ' 00000 n \n')
      .join('') +
    'trailer\n<< /Size 5 /Root 1 0 R >>\nstartxref\n' +
    xref +
    '\n%%EOF\n'
  return Buffer.from(pdf)
}
async function login(page, email, expected) {
  await page.goto('/login')
  await page
    .getByLabel('Email hoặc số điện thoại:')
    .count()
    .then(async (count) => {
      await page
        .getByLabel(
          count ? 'Email hoặc số điện thoại:' : 'Email hoặc số điện thoại',
          { exact: true },
        )
        .fill(email)
    })
  await page
    .getByLabel('Mật khẩu', { exact: true })
    .fill(email.endsWith('@demo.local') ? 'DemoPass123!' : password)
  await page.getByRole('button', { name: 'Đăng nhập →', exact: true }).click()
  await expect(page).toHaveURL(new RegExp(expected))
}

test('7 Use Cases persist through UI, API and H2', async ({
  browser,
  request,
}) => {
  test.setTimeout(120000)
  const unique = Date.now().toString().slice(-8)
  const studentEmail = 'ui.student.' + unique + '@test.local'
  const employerEmail = 'ui.employer.' + unique + '@test.local'
  const employerRegistration = await request.post(API + '/auth/register', {
    data: {
      fullName: 'UI Employer ' + unique,
      email: employerEmail,
      phone: '09' + unique,
      password,
      role: 'EMPLOYER',
    },
  })
  expect(employerRegistration.status()).toBe(201)
  const studentContext = await browser.newContext()
  const employerContext = await browser.newContext()
  const student = await studentContext.newPage()
  const employer = await employerContext.newPage()
  const browserErrors = []
  student.on('pageerror', (e) => browserErrors.push(e.message))
  employer.on('pageerror', (e) => browserErrors.push(e.message))

  await student.goto('/register')
  await student
    .getByLabel('Họ và tên', { exact: true })
    .fill('UI Student ' + unique)
  await student.getByLabel('Email', { exact: true }).fill(studentEmail)
  await student.getByLabel('Số điện thoại', { exact: true }).fill('08' + unique)
  await student.getByLabel('Mật khẩu', { exact: true }).fill(password)
  await student.getByLabel('Xác nhận mật khẩu').fill(password)
  await student.getByRole('checkbox').check()
  await student
    .getByRole('button', { name: 'Tạo tài khoản →', exact: true })
    .click()
  await expect(student).toHaveURL(/\/login$/)
  await expect(student.getByRole('status')).toContainText('Đăng ký thành công')
  expect(
    await student.evaluate(() => sessionStorage.getItem('campuswork.token')),
  ).toBeNull()
  await login(student, studentEmail, '/jobs')
  await student.goto('/student/profile')
  await student
    .getByLabel('Giới thiệu bản thân')
    .fill('Sinh viên yêu thích thiết kế và phát triển sản phẩm số.')
  await student.getByRole('button', { name: 'Lưu hồ sơ', exact: true }).click()
  await expect(student.getByRole('status')).toHaveText(
    'Đã lưu hồ sơ thành công.',
  )
  await student
    .locator('input[type=file][accept="application/pdf"]')
    .setInputFiles({
      name: 'student-cv.pdf',
      mimeType: 'application/pdf',
      buffer: pdfBuffer(),
    })
  await expect(student.getByRole('status')).toHaveText('CV đã được tải lên.')
  await student
    .locator('input[type=file][accept="image/png,image/jpeg"]')
    .setInputFiles('src/assets/hero.png')
  await expect(student.getByRole('status')).toHaveText(
    'Ảnh đại diện đã được cập nhật.',
  )
  await student.reload()
  await expect(student.getByLabel('Giới thiệu bản thân')).toHaveValue(
    'Sinh viên yêu thích thiết kế và phát triển sản phẩm số.',
  )
  await expect(
    student.getByText('student-cv.pdf', { exact: true }),
  ).toBeVisible()
  await expect(student.getByAltText('Ảnh đại diện của bạn')).toBeVisible()

  await login(employer, employerEmail, '/employer/jobs')
  await employer.getByRole('button', { name: '+ Đăng tin mới' }).click()
  const title = 'UI Product Designer ' + unique
  await employer.getByLabel('Tiêu đề công việc').fill(title)
  await employer
    .getByLabel('Ngành nghề', { exact: true })
    .selectOption('Design')
  await employer.getByLabel('Địa điểm', { exact: true }).fill('UI City')
  await employer.getByLabel('Mức lương (VNĐ)').fill('3000000')
  await employer
    .getByLabel('Hạn ứng tuyển')
    .fill(new Date(Date.now() + 30 * 86400000).toISOString().slice(0, 10))
  await employer
    .getByLabel('Mô tả công việc')
    .fill(
      'Thiết kế giao diện sản phẩm số và phối hợp nhóm phát triển ứng dụng.',
    )
  await employer
    .getByRole('button', { name: 'Lưu tin tuyển dụng', exact: true })
    .click()
  await expect(
    employer.getByRole('heading', { name: title, exact: true }),
  ).toBeVisible()

  await student.goto('/jobs')
  await student.getByLabel('Từ khóa tìm kiếm').fill(title)
  await student.getByRole('button', { name: 'Tìm việc ↗', exact: true }).click()
  await expect(
    student.getByRole('heading', { name: title, exact: true }),
  ).toBeVisible()
  await expect(student).toHaveURL(/keyword=/)
  await student.getByRole('heading', { name: title, exact: true }).click()
  await student
    .getByRole('button', { name: 'Ứng tuyển ngay ↗', exact: true })
    .click()
  await student
    .getByLabel('Thư giới thiệu', { exact: true })
    .fill('Tôi mong muốn phát triển kỹ năng thiết kế trong môi trường thực tế.')
  await student.getByRole('checkbox').check()
  await student
    .getByRole('button', { name: 'Xác nhận nộp đơn', exact: true })
    .click()
  await expect(student.getByRole('status')).toContainText(
    'Ứng tuyển thành công',
  )
  await expect(
    student.getByRole('button', { name: '✓ Đã ứng tuyển', exact: true }),
  ).toBeDisabled()
  await student.reload()
  await expect(
    student.getByRole('button', { name: '✓ Đã ứng tuyển', exact: true }),
  ).toBeDisabled()

  const jobCard = employer.locator('article').filter({
    has: employer.getByRole('heading', { name: title, exact: true }),
  })
  await jobCard
    .getByRole('link', { name: 'Xem ứng viên →', exact: true })
    .click()
  await expect(
    employer.getByRole('heading', {
      name: 'UI Student ' + unique,
      exact: true,
    }),
  ).toBeVisible()
  await employer
    .getByRole('button', { name: 'Duyệt ứng viên', exact: true })
    .click()
  await employer
    .getByRole('button', { name: 'Xác nhận quyết định', exact: true })
    .click()
  await expect(
    employer.getByText('Đã cập nhật kết quả và gửi thông báo cho ứng viên.', {
      exact: true,
    }),
  ).toBeVisible()

  await student.goto('/notifications')
  await expect(
    student.locator('.notification-card.unread').filter({ hasText: title }),
  ).toHaveCount(1)
  await student
    .locator('.notification-card')
    .filter({ hasText: title })
    .getByRole('button', { name: 'Đánh dấu đã đọc' })
    .click()
  await expect(
    student.locator('.notification-card.unread').filter({ hasText: title }),
  ).toHaveCount(0)
  await student.reload()
  await expect(
    student.locator('.notification-card').filter({ hasText: title }),
  ).toContainText('Đã đọc')

  await employer.goto('/employer/jobs')
  const card = employer.locator('article').filter({
    has: employer.getByRole('heading', { name: title, exact: true }),
  })
  await card.getByRole('button', { name: 'Đã nhận người', exact: true }).click()
  await employer
    .getByRole('button', { name: 'Xác nhận trạng thái', exact: true })
    .click()
  await expect(card.locator('.status')).toHaveText('Đã nhận người')
  await card
    .getByRole('button', { name: 'Hoàn thành công việc', exact: true })
    .click()
  await employer
    .getByRole('button', { name: 'Xác nhận trạng thái', exact: true })
    .click()
  await expect(card.locator('.status')).toHaveText('Hoàn thành')

  await student.goto('/student/applications')
  await student
    .getByRole('button', { name: 'Đánh giá nhà tuyển dụng', exact: true })
    .click()
  await student.getByRole('button', { name: '4 sao', exact: true }).click()
  await student
    .getByLabel('Chia sẻ trải nghiệm')
    .fill('Nhà tuyển dụng hỗ trợ tận tình, công việc đúng mô tả.')
  await student
    .getByRole('button', { name: 'Gửi đánh giá', exact: true })
    .click()
  await expect(
    student.getByText('★ Đã đánh giá', { exact: true }),
  ).toBeVisible()
  await student.reload()
  await expect(
    student.getByText('★ Đã đánh giá', { exact: true }),
  ).toBeVisible()
  await expect(
    student.getByRole('button', {
      name: 'Đánh giá nhà tuyển dụng',
      exact: true,
    }),
  ).toHaveCount(0)
  await student.screenshot({
    path: 'test-results/student-applications.png',
    fullPage: true,
  })
  expect(browserErrors).toEqual([])
  await studentContext.close()
  await employerContext.close()
})

for (const width of [360, 768, 1440]) {
  test('responsive public marketplace at ' + width + 'px', async ({ page }) => {
    await page.setViewportSize({ width, height: 900 })
    const started = Date.now()
    await page.goto('/jobs')
    await expect(page.locator('.job-card').first()).toBeVisible()
    const elapsed = Date.now() - started
    test.info().annotations.push({
      type: 'job-list-render-ms',
      description: String(elapsed),
    })
    expect(elapsed).toBeLessThanOrEqual(3000)
    expect(
      await page.evaluate(
        () => document.documentElement.scrollWidth <= window.innerWidth,
      ),
    ).toBe(true)
    await page.screenshot({
      path: 'test-results/jobs-' + width + '.png',
      fullPage: true,
    })
    await page.goto('/register')
    await expect(page.getByLabel('Họ và tên', { exact: true })).toBeVisible()
    expect(
      await page.evaluate(
        () => document.documentElement.scrollWidth <= window.innerWidth,
      ),
    ).toBe(true)
  })
}

test('public pagination, URL filters and expired session recovery', async ({
  page,
}) => {
  await page.goto('/jobs')
  await expect(page.locator('.job-card')).toHaveCount(12)
  await page.getByRole('button', { name: 'Sau →', exact: true }).click()
  await expect(page).toHaveURL(/page=1/)
  await expect(page.getByText('Trang 2 / 2', { exact: true })).toBeVisible()
  await page.evaluate(() =>
    sessionStorage.setItem('campuswork.token', 'invalid-expired-token'),
  )
  await page.goto('/student/profile')
  await expect(page).toHaveURL(/\/login$/)
  expect(
    await page.evaluate(() => sessionStorage.getItem('campuswork.token')),
  ).toBeNull()
})

test('seed employer and admin route to usable dashboards', async ({ page }) => {
  await login(page, 'employer.a@demo.local', '/employer/jobs')
  await expect(
    page.getByRole('heading', { name: 'Tìm đồng đội phù hợp', exact: true }),
  ).toBeVisible()
  await page.getByRole('button', { name: 'Đăng xuất', exact: true }).click()
  await login(page, 'admin@demo.local', '/admin')
  await expect(
    page.getByRole('heading', { name: 'Không gian quản trị', exact: true }),
  ).toBeVisible()
  await expect(page.getByText('Tổng tài khoản', { exact: true })).toBeVisible()
})
