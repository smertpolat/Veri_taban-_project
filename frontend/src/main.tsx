import React, { useEffect, useState, type FormEvent } from 'react'
import ReactDOM from 'react-dom/client'
import './style.css'

type Session = { kullaniciAdi: string; rol: string }

function App() {
  const [session, setSession] = useState<Session | null>(null)
  const [checking, setChecking] = useState(true)
  const [pending, setPending] = useState(false)
  const [error, setError] = useState('')
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [registerMode, setRegisterMode] = useState(false)
  const [notice, setNotice] = useState('')

  useEffect(() => {
    const token = sessionStorage.getItem('otelToken')
    if (!token) {
      setChecking(false)
      return
    }

    fetch('/api/auth/ben', { headers: { Authorization: `Bearer ${token}` } })
      .then(response => {
        if (!response.ok) throw new Error('Oturum süresi doldu.')
        return response.json() as Promise<Session>
      })
      .then(setSession)
      .catch(() => sessionStorage.removeItem('otelToken'))
      .finally(() => setChecking(false))
  }, [])

  async function handleLogin(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setError('')
    setPending(true)

    try {
      const response = await fetch('/api/auth/giris', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ kullaniciAdi: username, parola: password }),
      })
      if (!response.ok) {
        throw new Error(response.status === 401 ? 'Kullanıcı adı veya parola hatalı.' : 'Giriş yapılamadı. Tekrar deneyin.')
      }

      const { token } = await response.json() as { token: string }
      const userResponse = await fetch('/api/auth/ben', {
        headers: { Authorization: `Bearer ${token}` },
      })
      if (!userResponse.ok) throw new Error('Oturum doğrulanamadı. Tekrar deneyin.')

      const user = await userResponse.json() as Session
      sessionStorage.setItem('otelToken', token)
      setPassword('')
      setSession(user)
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : 'Giriş yapılamadı. Tekrar deneyin.')
    } finally {
      setPending(false)
    }
  }

  async function handleRegister(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setError('')
    setPending(true)

    const values = new FormData(event.currentTarget)
    const registration = Object.fromEntries(values.entries())

    try {
      const response = await fetch('/api/auth/kayit', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(registration),
      })
      if (!response.ok) {
        throw new Error(response.status === 409
          ? 'Kullanıcı adı, e-posta veya kimlik numarası zaten kayıtlı.'
          : 'Kayıt oluşturulamadı. Bilgilerinizi kontrol edip tekrar deneyin.')
      }

      setUsername(String(registration.kullaniciAdi))
      setPassword('')
      setRegisterMode(false)
      setNotice('Kaydınız oluşturuldu. Şimdi giriş yapabilirsiniz.')
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : 'Kayıt oluşturulamadı. Tekrar deneyin.')
    } finally {
      setPending(false)
    }
  }

  function switchMode() {
    setRegisterMode(!registerMode)
    setError('')
    setNotice('')
    setPassword('')
  }

  function logout() {
    sessionStorage.removeItem('otelToken')
    setSession(null)
    setUsername('')
    setPassword('')
  }

  return (
    <>
      <a className="skip-link" href="#main">İçeriğe geç</a>
      <header className="site-header">
        <span className="brand-mark" aria-hidden="true">O</span>
        <span className="brand-name">Otel Rezervasyon</span>
      </header>
      <main id="main" className="welcome">
        <div className="welcome-copy">
          <span className="eyebrow">Konaklama yönetimi</span>
          <h1>Her konaklama iyi bir başlangıcı hak eder.</h1>
          <p>Oda arama, rezervasyon ve konaklama işlemleri için tek bir yer.</p>
        </div>
        <section className="login-card" aria-labelledby="login-heading">
          {checking ? (
            <p role="status">Oturum kontrol ediliyor…</p>
          ) : session ? (
            <>
              <span className="card-eyebrow">Hoş geldiniz</span>
              <h2 id="login-heading">{session.kullaniciAdi}</h2>
              <p className="account-role">Hesap rolü: {session.rol}</p>
              <button type="button" onClick={logout}>Çıkış yap</button>
            </>
          ) : (
            <>
              <span className="card-eyebrow">Hesabınız</span>
              <h2 id="login-heading">{registerMode ? 'Misafir kaydı' : 'Giriş yapın'}</h2>
              <p>{registerMode ? 'Bilgilerinizi girerek misafir hesabı oluşturun.' : 'Rezervasyonlarınıza ve konaklama bilgilerinize ulaşın.'}</p>
              {notice && <p className="form-notice" role="status">{notice}</p>}
              {registerMode ? (
                <form onSubmit={handleRegister}>
                  {error && <p className="form-error" role="alert">{error}</p>}
                  <div className="form-row">
                    <div><label htmlFor="ad">Ad</label><input id="ad" name="ad" autoComplete="given-name" required maxLength={100} /></div>
                    <div><label htmlFor="soyad">Soyad</label><input id="soyad" name="soyad" autoComplete="family-name" required maxLength={100} /></div>
                  </div>
                  <label htmlFor="kimlikNo">Kimlik numarası</label>
                  <input id="kimlikNo" name="kimlikNo" required maxLength={40} />
                  <label htmlFor="telefon">Telefon</label>
                  <input id="telefon" name="telefon" type="tel" autoComplete="tel" required maxLength={40} />
                  <label htmlFor="eposta">E-posta</label>
                  <input id="eposta" name="eposta" type="email" autoComplete="email" required maxLength={254} />
                  <label htmlFor="dogumTarihi">Doğum tarihi</label>
                  <input id="dogumTarihi" name="dogumTarihi" type="date" autoComplete="bday" required />
                  <label htmlFor="newUsername">Kullanıcı adı</label>
                  <input id="newUsername" name="kullaniciAdi" autoComplete="username" required
                    minLength={3} maxLength={50} pattern="[a-zA-Z0-9._-]+" title="Harf, rakam, nokta, alt çizgi veya kısa çizgi kullanın." />
                  <label htmlFor="newPassword">Parola</label>
                  <input id="newPassword" name="parola" type="password" autoComplete="new-password"
                    required minLength={8} maxLength={72} />
                  <button type="submit" disabled={pending}>{pending ? 'Kayıt oluşturuluyor…' : 'Kayıt ol'}</button>
                </form>
              ) : (
                <form onSubmit={handleLogin}>
                  {error && <p className="form-error" role="alert">{error}</p>}
                  <label htmlFor="username">Kullanıcı adı</label>
                  <input id="username" name="username" autoComplete="username" required minLength={3}
                    value={username} onChange={event => setUsername(event.target.value)} />
                  <label htmlFor="password">Parola</label>
                  <input id="password" name="password" type="password" autoComplete="current-password" required
                    value={password} onChange={event => setPassword(event.target.value)} />
                  <button type="submit" disabled={pending}>{pending ? 'Giriş yapılıyor…' : 'Giriş yap'}</button>
                </form>
              )}
              <button className="switch-mode" type="button" onClick={switchMode}>
                {registerMode ? 'Hesabım var, giriş yap' : 'Hesabım yok, kayıt ol'}
              </button>
            </>
          )}
        </section>
      </main>
    </>
  )
}

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode><App /></React.StrictMode>,
)
