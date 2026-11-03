const bullet = /^\s*(?:[-•*+]|\d+[.)])\s+/

export default function RichText({ text, fallback }) {
  const lines = String(text || '')
    .split(/\r?\n/)
    .map((line) => line.trim())
    .filter(Boolean)
  if (!lines.length) return <p className="muted">{fallback}</p>
  const blocks = []
  let items = []
  const flush = () => {
    if (items.length) blocks.push({ list: items })
    items = []
  }
  lines.forEach((line) => {
    if (bullet.test(line)) items.push(line.replace(bullet, ''))
    else {
      flush()
      blocks.push({ text: line })
    }
  })
  flush()
  return (
    <>
      {blocks.map((block, i) =>
        block.list ? (
          <ul key={i}>
            {block.list.map((item, j) => (
              <li key={j}>{item}</li>
            ))}
          </ul>
        ) : (
          <p key={i}>{block.text}</p>
        ),
      )}
    </>
  )
}
