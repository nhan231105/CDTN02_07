import { useState } from 'react'
import { brandImage } from '../utils/brand'

const missing = new Set()

// Logo công ty; chưa có logo thì hiện chữ cái đầu.
export default function CompanyLogo({ id, name, className = '', version }) {
  const [, rerender] = useState(0)
  const key = id + ':' + (version || '')
  const hasImage = id && !missing.has(key)
  return (
    <span className={'company-logo ' + className}>
      {hasImage ? (
        <img
          src={brandImage(id, 'logo', version)}
          alt=""
          loading="lazy"
          onError={() => {
            missing.add(key)
            rerender((n) => n + 1)
          }}
        />
      ) : (
        (name || 'C').charAt(0)
      )}
    </span>
  )
}
