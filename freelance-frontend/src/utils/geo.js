const API = 'https://nominatim.openstreetmap.org'

export function distanceKm(a, b) {
  const rad = (d) => (d * Math.PI) / 180
  const dLat = rad(b.lat - a.lat)
  const dLng = rad(b.lng - a.lng)
  const h =
    Math.sin(dLat / 2) ** 2 +
    Math.cos(rad(a.lat)) * Math.cos(rad(b.lat)) * Math.sin(dLng / 2) ** 2
  return 6371 * 2 * Math.asin(Math.sqrt(h))
}

export const kmText = (km) =>
  km < 1
    ? Math.round(km * 1000) + ' m'
    : km.toFixed(1).replace('.', ',') + ' km'

// Trung tâm vài thành phố lớn, dùng làm dự phòng khi không tra được địa chỉ qua mạng.
const CITY_ANCHORS = [
  { lat: 21.0285, lng: 105.8542, name: 'Hà Nội' },
  { lat: 16.0544, lng: 108.2022, name: 'Đà Nẵng' },
  { lat: 10.7769, lng: 106.7009, name: 'TP. Hồ Chí Minh' },
  { lat: 10.0452, lng: 105.7469, name: 'Cần Thơ' },
]

export function nearestCityLabel(lat, lng) {
  let best = CITY_ANCHORS[0]
  let bestKm = Infinity
  for (const c of CITY_ANCHORS) {
    const km = distanceKm({ lat, lng }, c)
    if (km < bestKm) {
      bestKm = km
      best = c
    }
  }
  return bestKm <= 60 ? best.name : 'Gần ' + best.name
}

// Rút gọn địa chỉ của OpenStreetMap thành "Quận, Thành phố".
function shortLabel(address = {}, fallback = '') {
  const city = address.city || address.state || address.province || ''
  const district =
    address.city_district ||
    address.suburb ||
    address.quarter ||
    address.county ||
    address.town ||
    ''
  const label = [district, city].filter(Boolean).join(', ')
  return (label || fallback).slice(0, 100)
}

export async function reverseGeocode(lat, lng, signal) {
  const res = await fetch(
    `${API}/reverse?format=jsonv2&zoom=15&accept-language=vi&lat=${lat}&lon=${lng}`,
    { signal },
  )
  if (!res.ok) throw new Error('Không tra được địa chỉ')
  const data = await res.json()
  return {
    label: shortLabel(data.address, data.display_name || ''),
    full: data.display_name || '',
  }
}

export async function searchPlaces(query, signal) {
  const res = await fetch(
    `${API}/search?format=jsonv2&addressdetails=1&limit=5&countrycodes=vn&accept-language=vi&q=${encodeURIComponent(query)}`,
    { signal },
  )
  if (!res.ok) throw new Error('Không tìm được địa điểm')
  const list = await res.json()
  return list.map((p) => ({
    lat: Number(p.lat),
    lng: Number(p.lon),
    name: p.display_name,
    label: shortLabel(p.address, p.display_name),
  }))
}
