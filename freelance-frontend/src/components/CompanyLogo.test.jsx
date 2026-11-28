import { fireEvent, render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { describe, expect, it } from 'vitest'
import JobSpotlight from './JobSpotlight'
import CompanyLogo from './CompanyLogo'

describe('employer logos', () => {
  it('renders the company logo in a featured job', () => {
    const job = {
      id: 2,
      status: 'OPEN',
      title: 'Nhân viên pha chế',
      employerId: 3,
      employerName: 'Cà phê Mây Sớm',
      employerLogoRef: 'logo-123',
    }
    const { container } = render(<MemoryRouter><JobSpotlight jobs={[job]} /></MemoryRouter>)
    expect(container.querySelector('.spot-logo img')).toHaveAttribute(
      'src', '/api/employers/3/logo?revision=2&v=logo-123',
    )
  })

  it('falls back for a missing logo and tries again when the logo changes', () => {
    const { container, rerender } = render(<CompanyLogo id={3} name="Cà phê Mây Sớm" version={null} />)
    expect(screen.getByText('C')).toBeInTheDocument()
    expect(container.querySelector('img')).toBeNull()
    rerender(<CompanyLogo id={3} name="Cà phê Mây Sớm" version="broken" />)
    fireEvent.error(container.querySelector('img'))
    expect(screen.queryByText('C')).not.toBeInTheDocument()
    rerender(<CompanyLogo id={3} name="Cà phê Mây Sớm" version="new" />)
    expect(container.querySelector('img')).toHaveAttribute('src', '/api/employers/3/logo?revision=2&v=new')
  })
})
