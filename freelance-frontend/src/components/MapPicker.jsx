import { useEffect, useRef, useState } from 'react'
import L from 'leaflet'
import 'leaflet/dist/leaflet.css'
import { nearestCityLabel, reverseGeocode, searchPlaces } from '../utils/geo'

const TILES = 'https://tile.openstreetmap.org/{z}/{x}/{y}.png'
const VIETNAM = [16.2, 106.3]

const pin = L.divIcon({
  className: 'map-pin',
  html: '<span></span>',
  iconSize: [34, 34],
  iconAnchor: [17, 34],
})

// Bản đồ chấm vị trí: bấm hoặc kéo ghim, tìm địa chỉ, hoặc dùng vị trí hiện tại.
export default function MapPicker({ value, onChange, height = 320 }) {
  const box = useRef(null)
  const map = useRef(null)
  const marker = useRef(null)
  const layer = useRef(null)
  const change = useRef(onChange)
  const requestId = useRef(0)
  const [query, setQuery] = useState('')
  const [results, setResults] = useState([])
  const [note, setNote] = useState('')
  const [busy, setBusy] = useState(false)

  useEffect(() => {
    change.current = onChange
  })

  // Mỗi lần bấm/kéo ghim đặt vị trí ngay, sau đó tra địa chỉ. Nếu người dùng
  // bấm liên tiếp nhiều chỗ, chỉ kết quả của lần bấm mới nhất được áp dụng
  // (tránh việc một yêu cầu cũ trả về chậm rồi ghi đè lên vị trí mới hơn).
  const pick = async (lat, lng, label) => {
    place(lat, lng)
    const id = ++requestId.current
    setNote('Đang tra địa chỉ…')
    let name = label
    if (!name) {
      try {
        name = (await reverseGeocode(lat, lng)).label
      } catch {
        name = nearestCityLabel(lat, lng)
      }
    }
    if (id !== requestId.current) return
    setNote(name)
    change.current({ lat, lng, label: name })
  }

  const place = (lat, lng, fly) => {
    if (!map.current) return
    if (!marker.current) {
      marker.current = L.marker([lat, lng], { icon: pin, draggable: true })
        .addTo(map.current)
        .on('dragend', (e) => {
          const p = e.target.getLatLng()
          pick(p.lat, p.lng)
        })
    } else marker.current.setLatLng([lat, lng])
    if (fly) map.current.flyTo([lat, lng], 15, { duration: 0.8 })
  }

  useEffect(() => {
    const m = L.map(box.current, {
      center: value?.lat != null ? [value.lat, value.lng] : VIETNAM,
      zoom: value?.lat != null ? 15 : 5,
      zoomControl: false,
      attributionControl: true,
    })
    L.control.zoom({ position: 'bottomright' }).addTo(m)
    layer.current = L.tileLayer(TILES, {
      maxZoom: 19,
      attribution: '© OpenStreetMap',
    }).addTo(m)
    map.current = m
    m.on('click', (e) => pick(e.latlng.lat, e.latlng.lng))
    if (value?.lat != null) place(value.lat, value.lng)
    return () => {
      m.remove()
      map.current = null
      marker.current = null
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  useEffect(() => {
    if (value?.lat != null && map.current) place(value.lat, value.lng)
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [value?.lat, value?.lng])

  const search = async (e) => {
    e.preventDefault()
    if (!query.trim()) return
    setBusy(true)
    setNote('')
    try {
      const list = await searchPlaces(query.trim())
      setResults(list)
      if (!list.length) setNote('Không tìm thấy địa điểm này.')
    } catch (err) {
      setNote(err.message)
    } finally {
      setBusy(false)
    }
  }

  const choose = (r) => {
    requestId.current++
    setResults([])
    setQuery(r.name)
    place(r.lat, r.lng, true)
    setNote(r.label)
    change.current({ lat: r.lat, lng: r.lng, label: r.label })
  }

  const locate = () => {
    if (!navigator.geolocation)
      return setNote('Trình duyệt không hỗ trợ định vị.')
    setNote('Đang xác định vị trí…')
    navigator.geolocation.getCurrentPosition(
      (pos) => {
        place(pos.coords.latitude, pos.coords.longitude, true)
        pick(pos.coords.latitude, pos.coords.longitude)
      },
      () =>
        setNote(
          'Không lấy được vị trí. Hãy cho phép định vị hoặc chấm trên bản đồ.',
        ),
      { enableHighAccuracy: true, timeout: 10000 },
    )
  }

  return (
    <div className="map-picker">
      <form className="map-search" onSubmit={search}>
        <input
          aria-label="Tìm địa chỉ"
          placeholder="Tìm địa chỉ, trường học, khu vực…"
          value={query}
          onChange={(e) => setQuery(e.target.value)}
        />
        <button className="btn secondary" disabled={busy}>
          Tìm
        </button>
        <button type="button" className="btn secondary" onClick={locate}>
          Vị trí của tôi
        </button>
        {results.length > 0 && (
          <ul className="map-results">
            {results.map((r) => (
              <li key={r.lat + ',' + r.lng}>
                <button type="button" onClick={() => choose(r)}>
                  {r.name}
                </button>
              </li>
            ))}
          </ul>
        )}
      </form>
      <div ref={box} className="map-canvas" style={{ height }} />
      <p className="map-note">
        {note ||
          (value?.lat != null
            ? 'Kéo ghim hoặc bấm vào bản đồ để đổi vị trí.'
            : 'Bấm vào bản đồ để chấm vị trí của bạn.')}
      </p>
    </div>
  )
}
