import { useEffect } from 'react'

const targets = [
  '.job-card',
  '.category-card',
  '.employer-tile',
  '.stat-tile',
  '.stat-banner',
  '.location-chips',
  '.section-heading',
  '.surface',
  '.hero-copy',
  '.filter-panel',
].join(',')

export default function useScrollReveal() {
  useEffect(() => {
    if (
      typeof IntersectionObserver === 'undefined' ||
      window.matchMedia?.('(prefers-reduced-motion: reduce)').matches
    )
      return undefined
    const observer = new IntersectionObserver(
      (entries) =>
        entries.forEach((entry) => {
          if (entry.isIntersecting) {
            entry.target.classList.add('reveal-in')
            observer.unobserve(entry.target)
          }
        }),
      { threshold: 0.08, rootMargin: '0px 0px -40px 0px' },
    )
    const seen = new WeakSet()
    const scan = () =>
      document.querySelectorAll(targets).forEach((el) => {
        if (seen.has(el) || el.classList.contains('reveal-in')) return
        seen.add(el)
        const siblings = el.parentElement ? [...el.parentElement.children] : []
        el.style.setProperty(
          '--reveal-delay',
          Math.min(siblings.indexOf(el), 6) * 60 + 'ms',
        )
        el.classList.add('reveal')
        observer.observe(el)
      })
    scan()
    const mutations = new MutationObserver(scan)
    mutations.observe(document.body, { childList: true, subtree: true })
    return () => {
      document
        .querySelectorAll('.reveal:not(.reveal-in)')
        .forEach((el) => el.classList.remove('reveal'))
      observer.disconnect()
      mutations.disconnect()
    }
  }, [])
}
