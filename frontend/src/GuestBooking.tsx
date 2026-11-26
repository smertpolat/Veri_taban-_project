import { useEffect, useState, type FormEvent } from 'react'
import { ArrowRight, BedDouble, CalendarDays, CheckCircle2, ClipboardCheck, LogOut, Search, UsersRound, XCircle } from 'lucide-react'

type RoomType = { id: number; ad: string; kapasite: number; aciklama: string | null; temelFiyat: number }
type Room = { id: number; numara: string; odaTipi: string; kapasite: number; temelFiyat: number }
type Reservation = {
  id: number
  odaId: number
  girisTarihi: string
  cikisTarihi: string
  kisiSayisi: number
  durum: string
}
type Search = { giris: string; cikis: string; kisi: number }

const today = new Date().toLocaleDateString('sv-SE')
const statusText: Record<string, string> = {
  beklemede: 'Beklemede',
  onayli: 'Onaylı',
  iptal: 'İptal edildi',
  tamamlandi: 'Tamamlandı',
}
const roomImages: Record<string, string> = {
  standart: '/images/standard-room.jpg',
  deluxe: '/images/deluxe-room.jpg',
  suit: '/images/suite-room.jpg',
}
const roomImage = (name: string) => {
  const key = name.toLocaleLowerCase('tr-TR').normalize('NFD').replace(/[\u0300-\u036f]/g, '')
  return Object.entries(roomImages).find(([type]) => key.includes(type))?.[1] ?? '/images/hotel-hero.jpg'
}
const formatDate = (value: string) => new Date(`${value}T00:00:00`).toLocaleDateString('tr-TR', { day: 'numeric', month: 'short', year: 'numeric' })

export default function GuestBooking({ username, onLogout }: { username: string; onLogout: () => void }) {
  const [roomTypes, setRoomTypes] = useState<RoomType[]>([])
  const [reservations, setReservations] = useState<Reservation[]>([])
  const [rooms, setRooms] = useState<Room[]>([])
  const [search, setSearch] = useState<Search | null>(null)
  const [loading, setLoading] = useState(true)
  const [searching, setSearching] = useState(false)
  const [busyId, setBusyId] = useState<number | null>(null)
  const [error, setError] = useState('')
  const [notice, setNotice] = useState('')

  useEffect(() => {
    const token = sessionStorage.getItem('otelToken')
    Promise.all([
      fetch('/api/oda-tipleri'),
      fetch('/api/rezervasyonlar', { headers: { Authorization: `Bearer ${token}` } }),
    ]).then(async ([typesResponse, reservationsResponse]) => {
      if (!typesResponse.ok || !reservationsResponse.ok) throw new Error('Bilgiler yüklenemedi. Sayfayı yenileyin.')
      setRoomTypes(await typesResponse.json() as RoomType[])
      setReservations(await reservationsResponse.json() as Reservation[])
    }).catch(reason => setError(reason.message)).finally(() => setLoading(false))
  }, [])

  async function findRooms(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    const values = new FormData(event.currentTarget)
    const criteria = {
      giris: String(values.get('giris')),
      cikis: String(values.get('cikis')),
      kisi: Number(values.get('kisi')),
    }
    if (criteria.giris < today || criteria.cikis <= criteria.giris) {
      setError('Çıkış tarihi girişten sonra olmalı ve giriş geçmişte olmamalı.')
      return
    }

    setError('')
    setNotice('')
    setSearching(true)
    setRooms([])
    setSearch(null)
    try {
      const params = new URLSearchParams({
        giris: criteria.giris,
        cikis: criteria.cikis,
        odaTipiId: String(values.get('odaTipiId')),
      })
      const response = await fetch(`/api/odalar/musait?${params}`)
      if (!response.ok) throw new Error('Oda araması tamamlanamadı. Tekrar deneyin.')
      const available = await response.json() as Room[]
      setRooms(available.filter(room => room.kapasite >= criteria.kisi))
      setSearch(criteria)
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : 'Oda araması tamamlanamadı.')
    } finally {
      setSearching(false)
    }
  }

  async function book(roomId: number) {
    if (!search) return
    setBusyId(roomId)
    setError('')
    setNotice('')
    try {
      const response = await fetch('/api/rezervasyonlar', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${sessionStorage.getItem('otelToken')}`,
        },
        body: JSON.stringify({
          odaId: roomId,
          girisTarihi: search.giris,
          cikisTarihi: search.cikis,
          kisiSayisi: search.kisi,
        }),
      })
      if (!response.ok) {
        throw new Error(response.status === 409
          ? 'Bu oda artık uygun değil. Yeniden arayın.'
          : 'Rezervasyon oluşturulamadı. Tekrar deneyin.')
      }
      const reservation = await response.json() as Reservation
      setReservations(previous => [reservation, ...previous])
      setRooms(previous => previous.filter(room => room.id !== roomId))
      setNotice(`Rezervasyon #${reservation.id} oluşturuldu.`)
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : 'Rezervasyon oluşturulamadı.')
    } finally {
      setBusyId(null)
    }
  }

  async function cancel(id: number) {
    setBusyId(id)
    setError('')
    setNotice('')
    try {
      const response = await fetch(`/api/rezervasyonlar/${id}/iptal`, {
        method: 'PUT',
        headers: { Authorization: `Bearer ${sessionStorage.getItem('otelToken')}` },
      })
      if (!response.ok) throw new Error('Rezervasyon iptal edilemedi. Sayfayı yenileyip tekrar deneyin.')
      setReservations(previous => previous.map(item => item.id === id ? { ...item, durum: 'iptal' } : item))
      setNotice(`Rezervasyon #${id} iptal edildi.`)
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : 'Rezervasyon iptal edilemedi.')
    } finally {
      setBusyId(null)
    }
  }

  return (
    <main id="main" className="guest-page">
      <section className="guest-hero" aria-label="Konaklama planlama">
        <img src="/images/hotel-hero.jpg" alt="Deniz kıyısında gün batımında bir otel" width="1672" height="941" />
        <div className="guest-hero-content">
          <span className="eyebrow light">Misafir hesabı · Merhaba, {username}</span>
          <h1>Konaklamanızı<br /><em>planlayın.</em></h1>
          <p>Tarihleri seçin, size uygun odayı bulun.</p>
        </div>
        <button className="hero-logout" type="button" onClick={onLogout}><LogOut size={17} aria-hidden="true" /> Çıkış yap</button>
      </section>
      {error && <p className="page-message error" role="alert">{error}</p>}
      {notice && <p className="page-message success" role="status">{notice}</p>}
      <section className="booking-panel" aria-labelledby="search-heading">
        <div className="panel-title"><span className="panel-icon"><Search size={20} aria-hidden="true" /></span><div><span className="eyebrow">Bir sonraki konaklamanız</span><h2 id="search-heading">Uygun oda ara</h2></div></div>
        {loading ? <p role="status">Oda tipleri yükleniyor…</p> : (
          <form onSubmit={findRooms} aria-busy={searching}>
            <fieldset className="room-choice-fieldset">
              <legend><BedDouble size={19} aria-hidden="true" /> Önce oda tipinizi seçin</legend>
              {roomTypes.length === 0 ? <p>Şu anda listelenen oda tipi yok.</p> : <div className="room-choices">
                {roomTypes.map(type => <label className="room-choice" key={type.id}>
                  <input type="radio" name="odaTipiId" value={type.id} required />
                  <span className="room-choice-body">
                    <img src={roomImage(type.ad)} alt="" width="1672" height="941" loading="lazy" />
                    <span className="room-choice-caption"><strong>{type.ad}</strong><span><UsersRound size={15} aria-hidden="true" /> En fazla {type.kapasite} kişi</span></span>
                    <span className="room-choice-price">{type.temelFiyat.toLocaleString('tr-TR')} TL <small>/ temel gecelik fiyat</small></span>
                    {type.aciklama && <span className="room-choice-description">{type.aciklama}</span>}
                    <CheckCircle2 className="choice-check" size={23} aria-hidden="true" />
                  </span>
                </label>)}
              </div>}
            </fieldset>
            <div className="search-form">
              <div><label htmlFor="giris"><CalendarDays size={17} aria-hidden="true" /> Giriş</label><input id="giris" name="giris" type="date" min={today} required /></div>
              <div><label htmlFor="cikis"><CalendarDays size={17} aria-hidden="true" /> Çıkış</label><input id="cikis" name="cikis" type="date" min={today} required /></div>
              <div><label htmlFor="kisi"><UsersRound size={17} aria-hidden="true" /> Kişi sayısı</label><input id="kisi" name="kisi" type="number" min="1" defaultValue="1" required /></div>
              <button type="submit" disabled={searching || roomTypes.length === 0}>{searching ? 'Odalar aranıyor…' : <>Odaları göster <ArrowRight size={18} aria-hidden="true" /></>}</button>
            </div>
          </form>
        )}
        {search && (
          <div className="results" aria-live="polite">
            <div className="results-heading"><div><span className="eyebrow">Arama sonuçları</span><h3>{formatDate(search.giris)} – {formatDate(search.cikis)} için uygun odalar</h3></div><span className="result-count">{rooms.length} oda</span></div>
            {rooms.length === 0 ? <div className="empty-state"><Search size={28} aria-hidden="true" /><h4>Bu tarihler dolu görünüyor.</h4><p>Başka bir tarih veya oda tipi seçerek tekrar arayın.</p></div> : (
              <ul className="room-grid">
                {rooms.map(room => <li className="room-card" key={room.id}>
                  <div className="room-photo"><img src={roomImage(room.odaTipi)} alt={`${room.odaTipi} oda tipi için örnek görsel`} width="1672" height="941" loading="lazy" /><span className="photo-tag">{room.odaTipi}</span></div>
                  <div className="room-content"><div className="room-title"><div><span className="eyebrow">Oda {room.numara}</span><h4>{room.odaTipi} oda</h4></div><span className="room-capacity"><UsersRound size={15} aria-hidden="true" /> {room.kapasite} kişi</span></div>
                    <p className="room-price"><strong>{room.temelFiyat.toLocaleString('tr-TR')} TL</strong><span> / temel gecelik fiyat</span></p>
                    <button type="button" disabled={busyId !== null} onClick={() => book(room.id)}>
                      {busyId === room.id ? 'Kaydediliyor…' : <>Rezervasyon yap <ArrowRight size={18} aria-hidden="true" /></>}
                    </button>
                  </div>
                </li>)}
              </ul>
            )}
          </div>
        )}
      </section>
      <section className="booking-panel" aria-labelledby="reservations-heading">
        <div className="panel-title"><span className="panel-icon"><CalendarDays size={20} aria-hidden="true" /></span><div><span className="eyebrow">Planlarınız</span><h2 id="reservations-heading">Rezervasyonlarım</h2></div></div>
        {loading ? <p role="status">Rezervasyonlar yükleniyor…</p> : reservations.length === 0 ? (
          <div className="reservation-empty"><img src="/images/suite-room.jpg" alt="Deniz manzaralı suit oda için örnek görsel" width="1672" height="941" loading="lazy" /><div><ClipboardCheck size={24} aria-hidden="true" /><h3>İlk konaklamanız burada başlasın.</h3><p>Bir oda ve tarih seçtiğinizde rezervasyonunuz burada görünecek.</p><a href="#search-heading">Odalara göz at <ArrowRight size={16} aria-hidden="true" /></a></div></div>
        ) : (
          <ul className="reservation-list">
            {reservations.map(item => <li key={item.id}>
              <img src="/images/hotel-hero.jpg" alt="Otelin dış görünümü" width="1672" height="941" loading="lazy" />
              <div className="reservation-details"><span className="eyebrow">Rezervasyon #{item.id}</span><strong>{formatDate(item.girisTarihi)} – {formatDate(item.cikisTarihi)}</strong><span><UsersRound size={15} aria-hidden="true" /> {item.kisiSayisi} kişi</span></div>
              <span className={`status-badge ${item.durum}`}>
                {item.durum === 'iptal' ? <XCircle size={15} aria-hidden="true" /> : <CheckCircle2 size={15} aria-hidden="true" />}
                {statusText[item.durum] ?? item.durum}
              </span>
              {['beklemede', 'onayli'].includes(item.durum) && <button className="secondary-button" type="button" disabled={busyId !== null} onClick={() => cancel(item.id)}>
                {busyId === item.id ? 'İptal ediliyor…' : 'İptal et'}
              </button>}
            </li>)}
          </ul>
        )}
      </section>
    </main>
  )
}
