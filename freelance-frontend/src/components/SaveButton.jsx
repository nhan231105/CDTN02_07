export default function SaveButton({ saved, onToggle, label = false }) {
  return (
    <button
      type="button"
      className={'save-btn' + (saved ? ' is-saved' : '')}
      aria-pressed={saved}
      aria-label={saved ? 'Bỏ lưu việc làm' : 'Lưu việc làm'}
      onClick={(e) => {
        e.preventDefault()
        e.stopPropagation()
        onToggle()
      }}
    >
      <span aria-hidden="true">{saved ? '♥' : '♡'}</span>
      {label && <span>{saved ? ' Đã lưu' : ' Lưu việc'}</span>}
    </button>
  )
}
