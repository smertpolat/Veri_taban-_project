import { useEffect, useState } from 'react'
import { ArrowDownRight, ArrowUpRight, BedDouble, CalendarDays, CheckCircle2, CircleDollarSign, DoorOpen, Hotel, LogOut, RefreshCw, Sparkles, UsersRound } from 'lucide-react'

type Role = 'yonetici' | 'resepsiyon'
type Report = { doluOdaGecesi: number; toplamOdaGecesi: number; dolulukYuzdesi: number; gelir: number }
type RoomStatus = { id: number; numara: string; kat: number; odaTipi: string; durum: string; sonTemizlikZamani: string | null }
type Movement = { rezervasyonId: number; ad: string; soyad: string; odaNumarasi: string; tur: 'giris' | 'cikis'; tarih: string; konaklamaId: number | null }

const monthNow = new Date().toLocaleDateString('sv-SE').slice(0, 7)
const monthName = (value: string) => new Date(`${value}-01T00:00:00`).toLocaleDateString('tr-TR', { month: 'long', year: 'numeric' })
const money = (value: number) => `${value.toLocaleString('tr-TR')} TL`
const roomPhoto = (name: string) => {
  const normalized = name.toLocaleLowerCase('tr-TR').normalize('NFD').replace(/[\u0300-\u036f]/g, '')
  if (normalized.includes('standart')) return '/images/standard-room.jpg'
  if (normalized.includes('deluxe')) return '/images/deluxe-room.jpg'
  if (normalized.includes('suit')) return '/images/suite-room.jpg'
  return '/images/hotel-hero.jpg'
}
const statusNames: Record<string, string> = { bos: 'Boş', dolu: 'Dolu', temizlikte: 'Temizlikte', bakimda: 'Bakımda' }

export default function StaffDashboard({ role, username, onLogout }: { role: Role; username: string; onLogout: () => void }) {
  const [month, setMonth] = useState(monthNow)
  const [report, setReport] = useState<Report | null>(null)
  const [rooms, setRooms] = useState<RoomStatus[]>([])
  const [movements, setMovements] = useState<Movement[]>([])
  const [loading, setLoading] = useState(true)
  const [refresh, setRefresh] = useState(0)
  const [busyId, setBusyId] = useState<number | null>(null)
  const [error, setError] = useState('')
  const [notice, setNotice] = useState('')
  const isManager = role === 'yonetici'

  useEffect(() => {
    let active = true
    const token = sessionStorage.getItem('otelToken')
    const headers = { Authorization: `Bearer ${token}` }
    setLoading(true)
    setError('')
    if (isManager) { setReport(null); setRooms([]) }
    else setMovements([])

    async function load() {
      try {
        if (isManager) {
          const [reportResponse, roomsResponse] = await Promise.all([
            fetch(`/api/yonetici/raporlar/aylik?ay=${month}-01`, { headers }),
            fetch('/api/yonetici/raporlar/oda-durumlari', { headers }),
          ])
          if (!reportResponse.ok || !roomsResponse.ok) throw new Error('Yönetim verileri yüklenemedi. Yeniden deneyin.')
          const [newReport, newRooms] = await Promise.all([reportResponse.json() as Promise<Report>, roomsResponse.json() as Promise<RoomStatus[]>])
          if (active) { setReport(newReport); setRooms(newRooms) }
        } else {
          const response = await fetch('/api/resepsiyon/gunluk-giris-cikis', { headers })
          if (!response.ok) throw new Error('Günlük hareketler yüklenemedi. Yeniden deneyin.')
          const data = await response.json() as Movement[]
          if (active) setMovements(data)
        }
      } catch (reason) {
        if (active) setError(reason instanceof Error ? reason.message : 'Bilgiler yüklenemedi.')
      } finally {
        if (active) setLoading(false)
      }
    }
    load()
    return () => { active = false }
  }, [isManager, month, refresh])

  async function checkIn(id: number) {
    setBusyId(id)
    setError('')
    setNotice('')
    try {
      const response = await fetch(`/api/resepsiyon/rezervasyonlar/${id}/check-in`, {
        method: 'POST', headers: { Authorization: `Bearer ${sessionStorage.getItem('otelToken')}` },
      })
      if (!response.ok) throw new Error('Giriş kaydedilemedi. Rezervasyon ve oda durumunu kontrol edin.')
      setNotice(`Rezervasyon #${id} için giriş kaydedildi.`)
      setRefresh(value => value + 1)
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : 'Giriş kaydedilemedi.')
    } finally {
      setBusyId(null)
    }
  }

  const arrivals = movements.filter(item => item.tur === 'giris')
  const departures = movements.filter(item => item.tur === 'cikis')
  const occupied = rooms.filter(room => room.durum === 'dolu').length
  const available = rooms.filter(room => room.durum === 'bos').length

  return <main id="main" className="staff-page">
    <section className="staff-hero" aria-label="Çalışma alanı">
      <img src="/images/hotel-hero.jpg" alt="Deniz kıyısındaki otel için temsili görsel" width="1672" height="941" />
      <div className="staff-hero-content"><span className="eyebrow light"><Sparkles size={15} aria-hidden="true" /> {isManager ? 'Yönetim alanı' : 'Resepsiyon alanı'}</span><h1>{isManager ? 'Otelinize genel bakış.' : 'Bugünün akışı elinizde.'}</h1><p>Merhaba, {username}. {isManager ? 'Doluluk, gelir ve oda durumlarını tek yerden izleyin.' : 'Bugünkü giriş ve çıkışları kolayca takip edin.'}</p></div>
      <button className="hero-logout" type="button" onClick={onLogout}><LogOut size={17} aria-hidden="true" /> Çıkış yap</button>
    </section>
    <div className="staff-body">
      <div className="staff-toolbar"><div><span className="eyebrow">{isManager ? 'Yönetim özeti' : 'Günlük operasyon'}</span><h2>{isManager ? 'Genel görünüm' : 'Bugünkü hareketler'}</h2></div><button className="staff-refresh" type="button" onClick={() => setRefresh(value => value + 1)} disabled={loading}><RefreshCw size={17} aria-hidden="true" /> Yenile</button></div>
      {error && <p className="page-message error" role="alert">{error}</p>}
      {notice && <p className="page-message success" role="status">{notice}</p>}
      {isManager ? <>
        <div className="staff-filter"><label htmlFor="report-month"><CalendarDays size={17} aria-hidden="true" /> Rapor ayı</label><input id="report-month" type="month" value={month} onChange={event => { if (event.target.value) setMonth(event.target.value) }} /></div>
        <div className="staff-stats" aria-busy={loading}>
          <article><span className="stat-icon"><BedDouble size={22} aria-hidden="true" /></span><small>Toplam oda</small><strong>{loading ? '…' : rooms.length}</strong><p>Otel envanterindeki odalar</p></article>
          <article><span className="stat-icon"><DoorOpen size={22} aria-hidden="true" /></span><small>Şu an boş</small><strong>{loading ? '…' : available}</strong><p>Şu an dolu {loading ? '…' : occupied} oda</p></article>
          <article><span className="stat-icon"><Hotel size={22} aria-hidden="true" /></span><small>Aylık doluluk</small><strong>{loading || !report ? '…' : `%${Number(report.dolulukYuzdesi).toLocaleString('tr-TR')}`}</strong><p>{monthName(month)} dönemi</p></article>
          <article><span className="stat-icon"><CircleDollarSign size={22} aria-hidden="true" /></span><small>Aylık gelir</small><strong>{loading || !report ? '…' : money(Number(report.gelir))}</strong><p>Kaydedilen ödemeler</p></article>
        </div>
        <section className="staff-panel" aria-labelledby="rooms-heading"><div className="staff-panel-heading"><div><span className="eyebrow">Oda envanteri</span><h2 id="rooms-heading">Oda durumları</h2></div><span className="result-count">{rooms.length} oda</span></div>
          {loading ? <p role="status">Oda durumları yükleniyor…</p> : rooms.length === 0 ? <p>Listelenecek oda yok.</p> : <ul className="staff-room-grid">{rooms.map(room => <li key={room.id}><img src={roomPhoto(room.odaTipi)} alt="" width="1672" height="941" loading="lazy" /><div><span className="eyebrow">{room.kat}. kat · Oda {room.numara}</span><h3>{room.odaTipi}</h3><span className={`staff-status ${room.durum}`}>{statusNames[room.durum] ?? room.durum}</span></div></li>)}</ul>}
        </section>
      </> : <>
        <div className="staff-stats reception-stats" aria-busy={loading}>
          <article><span className="stat-icon"><ArrowDownRight size={22} aria-hidden="true" /></span><small>Bugünkü giriş</small><strong>{loading ? '…' : arrivals.length}</strong><p>Planlanan varışlar</p></article>
          <article><span className="stat-icon"><ArrowUpRight size={22} aria-hidden="true" /></span><small>Bugünkü çıkış</small><strong>{loading ? '…' : departures.length}</strong><p>Planlanan ayrılışlar</p></article>
          <article><span className="stat-icon"><CheckCircle2 size={22} aria-hidden="true" /></span><small>Giriş kaydı açılan</small><strong>{loading ? '…' : arrivals.filter(item => item.konaklamaId !== null).length}</strong><p>Bugünkü varışlardan</p></article>
        </div>
        <section className="staff-panel" aria-labelledby="movements-heading"><div className="staff-panel-heading"><div><span className="eyebrow">Resepsiyon listesi</span><h2 id="movements-heading">Giriş ve çıkışlar</h2></div><span className="result-count">{movements.length} kayıt</span></div>
          {loading ? <p role="status">Günlük hareketler yükleniyor…</p> : movements.length === 0 ? <div className="staff-empty"><CalendarDays size={30} aria-hidden="true" /><h3>Bugün planlanan hareket yok.</h3><p>Yeni giriş ve çıkışlar burada görünecek.</p></div> : <ul className="movement-list">{movements.map(item => <li key={`${item.tur}-${item.rezervasyonId}`}><span className={`movement-icon ${item.tur}`}>{item.tur === 'giris' ? <ArrowDownRight size={20} aria-hidden="true" /> : <ArrowUpRight size={20} aria-hidden="true" />}</span><div><span className="eyebrow">Rezervasyon #{item.rezervasyonId} · Oda {item.odaNumarasi}</span><h3>{item.ad} {item.soyad}</h3><span className="movement-kind"><UsersRound size={14} aria-hidden="true" /> {item.tur === 'giris' ? 'Bugünkü giriş' : 'Bugünkü çıkış'}</span></div>{item.tur === 'giris' && (item.konaklamaId ? <span className="staff-status bos"><CheckCircle2 size={15} aria-hidden="true" /> Giriş yapıldı</span> : <button type="button" onClick={() => checkIn(item.rezervasyonId)} disabled={busyId !== null}>{busyId === item.rezervasyonId ? 'Kaydediliyor…' : 'Giriş yap'}</button>)}</li>)}</ul>}
        </section>
      </>}
    </div>
  </main>
}
