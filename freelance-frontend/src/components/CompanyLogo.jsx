import { useState } from 'react'
import { brandImage } from '../utils/brand'

// Use the company initial when the API does not provide an image.
export default function CompanyLogo({ id, name, className = '', version }) {
  const [failedSrc, setFailedSrc] = useState('')
  const src = id && version !== null ? brandImage(id, 'logo', version) : ''
  const hasImage = src && failedSrc !== src
  return (
    <span className={'company-logo ' + className}>
      {hasImage ? (
        <img
          src={src}
          alt=""
          loading="lazy"
          onError={() => setFailedSrc(src)}
        />
      ) : (
        version ? '—' : (name || 'C').charAt(0)
      )}
    </span>
  )
}
