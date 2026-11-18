import { useState, useEffect } from 'react';
import { api, getStoredUser, setAuth, clearAuth } from './api';
import type { User, Equipment, Reservation, ClearanceLevel } from './types';
import './App.css';

export default function App() {
  const [currentUser, setCurrentUser] = useState<User | null>(getStoredUser());
  const [activeTab, setActiveTab] = useState<'catalog' | 'my-reservations' | 'all-reservations' | 'users'>('catalog');

  // Auth Form State
  const [isRegister, setIsRegister] = useState(false);
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [firstName, setFirstName] = useState('');
  const [lastName, setLastName] = useState('');
  const [authError, setAuthError] = useState<string | null>(null);

  // Equipment List State
  const [equipmentList, setEquipmentList] = useState<Equipment[]>([]);
  const [searchQuery, setSearchQuery] = useState('');
  const [loadingEquipment, setLoadingEquipment] = useState(false);

  // Reservation Dialog / Booking State
  const [selectedItem, setSelectedItem] = useState<Equipment | null>(null);
  const [startDate, setStartDate] = useState('');
  const [endDate, setEndDate] = useState('');
  const [availabilityMessage, setAvailabilityMessage] = useState<string | null>(null);
  const [bookingError, setBookingError] = useState<string | null>(null);
  const [bookingSuccess, setBookingSuccess] = useState<string | null>(null);

  // Reservations State
  const [myReservations, setMyReservations] = useState<Reservation[]>([]);
  const [allReservations, setAllReservations] = useState<Reservation[]>([]);
  const [loadingReservations, setLoadingReservations] = useState(false);

  // Users State (Admin)
  const [usersList, setUsersList] = useState<User[]>([]);
  const [loadingUsers, setLoadingUsers] = useState(false);

  // Global notice
  const [message, setMessage] = useState<{ text: string; type: 'success' | 'error' } | null>(null);
  const todayStr = new Date().toISOString().split('T')[0];

  const showNotification = (text: string, type: 'success' | 'error' = 'success') => {
    setMessage({ text, type });
    setTimeout(() => setMessage(null), 4000);
  };

  const loadEquipment = async () => {
    setLoadingEquipment(true);
    try {
      const res = await api.listEquipment({ q: searchQuery, size: 50 });
      setEquipmentList(res.content || []);
    } catch (err: any) {
      console.error(err);
    } finally {
      setLoadingEquipment(false);
    }
  };

  const loadMyReservations = async () => {
    setLoadingReservations(true);
    try {
      const res = await api.listMyReservations();
      setMyReservations(res.content || []);
    } catch (err: any) {
      showNotification(err.message, 'error');
    } finally {
      setLoadingReservations(false);
    }
  };

  const loadAllReservations = async () => {
    setLoadingReservations(true);
    try {
      const res = await api.listAllReservations();
      setAllReservations(res.content || []);
    } catch (err: any) {
      showNotification(err.message, 'error');
    } finally {
      setLoadingReservations(false);
    }
  };

  const loadUsers = async () => {
    setLoadingUsers(true);
    try {
      const res = await api.listUsers();
      setUsersList(res.content || []);
    } catch (err: any) {
      showNotification(err.message, 'error');
    } finally {
      setLoadingUsers(false);
    }
  };

  // Load equipment on mount or query change
  useEffect(() => {
    if (currentUser) {
      loadEquipment();
      loadMyReservations();
    }
  }, [currentUser, searchQuery]);

  // Load reservations / users when switching tabs
  useEffect(() => {
    if (!currentUser) return;
    if (activeTab === 'my-reservations') {
      loadMyReservations();
    } else if (activeTab === 'all-reservations' && currentUser.role === 'ADMIN') {
      loadAllReservations();
    } else if (activeTab === 'users' && currentUser.role === 'ADMIN') {
      loadUsers();
    }
  }, [activeTab, currentUser]);

  const handleLogin = async (e?: React.FormEvent) => {
    if (e) e.preventDefault();
    setAuthError(null);
    try {
      const res = await api.login({ email, password });
      setAuth(res);
      setCurrentUser(res.user);
      showNotification(`Witaj, ${res.user.firstName}! Zalogowano pomyslnie.`);
    } catch (err: any) {
      setAuthError(err.message);
    }
  };

  const handleQuickLogin = async (loginEmail: string) => {
    setEmail(loginEmail);
    setPassword('password123');
    setAuthError(null);
    try {
      const res = await api.login({ email: loginEmail, password: 'password123' });
      setAuth(res);
      setCurrentUser(res.user);
      showNotification(`Zalogowano jako ${res.user.firstName} (${res.user.role})`);
    } catch (err: any) {
      setAuthError(err.message);
    }
  };

  const handleRegister = async (e: React.FormEvent) => {
    e.preventDefault();
    setAuthError(null);
    try {
      await api.register({
        firstName,
        lastName,
        email,
        password,
      });
      // Auto login after registration
      const loginRes = await api.login({ email, password });
      setAuth(loginRes);
      setCurrentUser(loginRes.user);
      showNotification('Konto utworzone pomyslnie! Domyslny poziom uprawnien: BASIC.');
    } catch (err: any) {
      setAuthError(err.message);
    }
  };

  const handleLogout = () => {
    clearAuth();
    setCurrentUser(null);
    setActiveTab('catalog');
    showNotification('Wylogowano pomyslnie.');
  };

  const handleCheckAvailability = async () => {
    if (!selectedItem || !startDate || !endDate) return;
    setAvailabilityMessage(null);
    setBookingError(null);
    try {
      const res = await api.checkAvailability(selectedItem.id, startDate, endDate);
      if (res.available) {
        setAvailabilityMessage('Sprzet jest dostepny w wybranym terminie.');
      } else {
        setBookingError(`Sprzet niedostepny: ${res.reason || 'Termin zajety'}`);
      }
    } catch (err: any) {
      setBookingError(err.message);
    }
  };

  const handleCreateReservation = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedItem || !startDate || !endDate) return;
    setBookingError(null);
    setBookingSuccess(null);
    try {
      await api.createReservation({
        itemId: selectedItem.id,
        startDate,
        endDate,
      });
      setBookingSuccess('Rezerwacja zostala pomyslnie utworzona.');
      showNotification('Rezerwacja utworzona.');
      loadMyReservations();
      loadEquipment();
      setTimeout(() => {
        setSelectedItem(null);
        setBookingSuccess(null);
        setStartDate('');
        setEndDate('');
        setAvailabilityMessage(null);
      }, 1200);
    } catch (err: any) {
      setBookingError(err.message);
    }
  };

  const handleCancelReservation = async (id: number) => {
    if (!confirm('Czy na pewno chcesz anulowac te rezerwacje?')) return;
    try {
      await api.cancelReservation(id);
      showNotification('Rezerwacja zostala anulowana.');
      loadMyReservations();
      if (currentUser?.role === 'ADMIN') loadAllReservations();
    } catch (err: any) {
      showNotification(err.message, 'error');
    }
  };

  const handleApprove = async (id: number) => {
    try {
      await api.approveReservation(id);
      showNotification('Rezerwacja zatwierdzona.');
      loadAllReservations();
    } catch (err: any) {
      showNotification(err.message, 'error');
    }
  };

  const handleReject = async (id: number) => {
    if (!confirm('Czy na pewno chcesz odrzucic te rezerwacje?')) return;
    try {
      await api.rejectReservation(id);
      showNotification('Rezerwacja odrzucona.');
      loadAllReservations();
    } catch (err: any) {
      showNotification(err.message, 'error');
    }
  };

  const handleIssue = async (id: number) => {
    const note = prompt('Notatka wydania sprzetu (opcjonalnie):') || undefined;
    try {
      await api.issueEquipment(id, note);
      showNotification('Sprzet zostal wydany.');
      loadAllReservations();
    } catch (err: any) {
      showNotification(err.message, 'error');
    }
  };

  const handleReturn = async (id: number) => {
    const note = prompt('Notatka zwrotu sprzetu (opcjonalnie):') || undefined;
    try {
      await api.returnEquipment(id, note);
      showNotification('Sprzet zostal zwrocony.');
      loadAllReservations();
    } catch (err: any) {
      showNotification(err.message, 'error');
    }
  };

  const handleChangeClearance = async (userId: number, newLevel: ClearanceLevel) => {
    try {
      await api.changeUserClearance(userId, newLevel);
      showNotification(`Zmieniono poziom uprawnien uzytkownika #${userId} na ${newLevel}.`);
      loadUsers();
      if (currentUser?.id === userId) {
        setCurrentUser({ ...currentUser, clearanceLevel: newLevel });
      }
    } catch (err: any) {
      showNotification(err.message, 'error');
    }
  };

  // If not logged in, show Auth Screen
  if (!currentUser) {
    return (
      <div className="auth-wrapper">
        <div className="auth-card">
          <div className="brand">
            <h2>UniEquip System</h2>
            <p className="subtitle">Uczelniany system rezerwacji sprzetu laboratoryjnego</p>
          </div>

          {authError && <div className="alert alert-error">{authError}</div>}

          {!isRegister ? (
            <form onSubmit={handleLogin} className="auth-form">
              <h3>Logowanie</h3>
              <div className="form-group">
                <label>Adres e-mail</label>
                <input
                  type="email"
                  required
                  placeholder="np. admin@example.com"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                />
              </div>

              <div className="form-group">
                <label>Haslo</label>
                <input
                  type="password"
                  required
                  placeholder="Twoje haslo"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                />
              </div>

              <button type="submit" className="btn btn-primary">
                Zaloguj sie
              </button>

              <div className="quick-logins">
                <p className="hint">Szybkie logowanie testowe (haslo: password123):</p>
                <div className="btn-group">
                  <button
                    type="button"
                    className="btn btn-outline"
                    onClick={() => handleQuickLogin('admin@example.com')}
                  >
                    Admin (Administrator)
                  </button>
                  <button
                    type="button"
                    className="btn btn-outline"
                    onClick={() => handleQuickLogin('user@example.com')}
                  >
                    Jan Kowalski (User)
                  </button>
                </div>
              </div>

              <div className="auth-toggle">
                Nie masz jeszcze konta?{' '}
                <button type="button" className="link-btn" onClick={() => setIsRegister(true)}>
                  Zarejestruj sie
                </button>
              </div>
            </form>
          ) : (
            <form onSubmit={handleRegister} className="auth-form">
              <h3>Rejestracja konta</h3>
              <div className="form-row">
                <div className="form-group">
                  <label>Imie</label>
                  <input
                    type="text"
                    required
                    placeholder="Jan"
                    value={firstName}
                    onChange={(e) => setFirstName(e.target.value)}
                  />
                </div>
                <div className="form-group">
                  <label>Nazwisko</label>
                  <input
                    type="text"
                    required
                    placeholder="Nowak"
                    value={lastName}
                    onChange={(e) => setLastName(e.target.value)}
                  />
                </div>
              </div>

              <div className="form-group">
                <label>E-mail</label>
                <input
                  type="email"
                  required
                  placeholder="jan.nowak@uczelnia.edu.pl"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                />
              </div>

              <div className="form-group">
                <label>Haslo (min. 8 znakow)</label>
                <input
                  type="password"
                  required
                  minLength={8}
                  placeholder="Minimum 8 znakow"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                />
              </div>

              <div className="info-box">
                Nowo zarejestrowane konto otrzymuje podstawowy poziom uprawnien (BASIC). Wyzsze uprawnienia nadawane sa wylacznie przez administratora.
              </div>

              <button type="submit" className="btn btn-primary">
                Zarejestruj i zaloguj
              </button>

              <div className="auth-toggle">
                Masz juz konto?{' '}
                <button type="button" className="link-btn" onClick={() => setIsRegister(false)}>
                  Wroc do logowania
                </button>
              </div>
            </form>
          )}
        </div>
      </div>
    );
  }

  // Logged-in Dashboard
  return (
    <div className="app-container">
      {/* Top Navbar */}
      <header className="navbar">
        <div className="nav-brand">
          <span className="brand-title">UniEquip</span>
        </div>

        <nav className="nav-links">
          <button
            className={`nav-btn ${activeTab === 'catalog' ? 'active' : ''}`}
            onClick={() => setActiveTab('catalog')}
          >
            Katalog sprzetu
          </button>
          <button
            className={`nav-btn ${activeTab === 'my-reservations' ? 'active' : ''}`}
            onClick={() => setActiveTab('my-reservations')}
          >
            Moje rezerwacje
          </button>
          {currentUser.role === 'ADMIN' && (
            <>
              <button
                className={`nav-btn ${activeTab === 'all-reservations' ? 'active' : ''}`}
                onClick={() => setActiveTab('all-reservations')}
              >
                Rezerwacje (Admin)
              </button>
              <button
                className={`nav-btn ${activeTab === 'users' ? 'active' : ''}`}
                onClick={() => setActiveTab('users')}
              >
                Uzytkownicy i Uprawnienia
              </button>
            </>
          )}
        </nav>

        <div className="user-profile">
          <div className="user-info">
            <span className="user-name">
              {currentUser.firstName} {currentUser.lastName}
            </span>
            <span className="user-badge role-badge">{currentUser.role}</span>
            <span className="user-badge clearance-badge">{currentUser.clearanceLevel}</span>
          </div>
          <button onClick={handleLogout} className="btn btn-logout" title="Wyloguj">
            Wyloguj
          </button>
        </div>
      </header>

      {/* Global Alerts */}
      {message && (
        <div className={`notification toast-${message.type}`}>
          {message.text}
        </div>
      )}

      {/* Main Content Area */}
      <main className="content">
        {/* Tab 1: Equipment Catalog */}
        {activeTab === 'catalog' && (
          <div className="tab-pane">
            <div className="section-header">
              <div>
                <h2>Dostepny sprzet i aparatura</h2>
                <p>Wybierz sprzet, aby sprawdzic dostepnosc i zarezerwowac termin.</p>
              </div>

              <div className="search-bar">
                <input
                  type="text"
                  placeholder="Szukaj po nazwie lub nr inwentarzowym..."
                  value={searchQuery}
                  onChange={(e) => setSearchQuery(e.target.value)}
                />
              </div>
            </div>

            {loadingEquipment ? (
              <div className="loader">Wczytywanie listy sprzetu...</div>
            ) : equipmentList.length === 0 ? (
              <div className="empty-state">Brak sprzetu spelniajacego kryteria wyszukiwania.</div>
            ) : (
              <div className="equipment-grid">
                {equipmentList.map((item) => {
                  const activeRes = myReservations.find(
                    (r) => r.itemId === item.id && (r.status === 'PENDING' || r.status === 'APPROVED' || r.status === 'ISSUED')
                  );
                  return (
                  <div key={item.id} className="equipment-card">
                    <div className="card-top">
                      <span className="badge category-badge">{item.categoryName || 'Brak kategorii'}</span>
                      <span className={`badge status-badge status-${item.status.toLowerCase()}`}>
                        {item.status === 'IN_SERVICE' ? 'W UZYCIU (IN_SERVICE)' : item.status}
                      </span>
                    </div>

                    <h3 className="card-title">{item.name}</h3>
                    <p className="inventory-no">Nr inw: <code>{item.inventoryNumber}</code></p>

                    {activeRes && (
                      <div className="reservation-notice">
                        Twoja rezerwacja: <strong>{activeRes.status}</strong> ({activeRes.startDate} do {activeRes.endDate})
                      </div>
                    )}

                    <p className="card-desc">{item.description || 'Brak opisu urzadzenia.'}</p>

                    <div className="card-meta">
                      <div><strong>Wydzial:</strong> {item.departmentName || 'Ogolny'}</div>
                      {item.hazardClassName && (
                        <div className="hazard-info"><strong>Wymagane szkolenie/klasa:</strong> {item.hazardClassName}</div>
                      )}
                    </div>

                    {item.specs && Object.keys(item.specs).length > 0 && (
                      <div className="specs-box">
                        <strong>Parametry techniczne:</strong>
                        <ul>
                          {Object.entries(item.specs).map(([k, v]) => (
                            <li key={k}>
                              <span className="spec-key">{k}:</span> {String(v)}
                            </li>
                          ))}
                        </ul>
                      </div>
                    )}

                    <div className="card-actions">
                      <button
                        className="btn btn-primary btn-block"
                        disabled={item.status !== 'AVAILABLE'}
                        onClick={() => {
                          setSelectedItem(item);
                          setAvailabilityMessage(null);
                          setBookingError(null);
                          setBookingSuccess(null);
                        }}
                      >
                        {item.status === 'AVAILABLE' ? 'Zarezerwuj ten sprzet' : 'Sprzet niedostepny'}
                      </button>
                    </div>
                  </div>
                  );
                })}
              </div>
            )}
          </div>
        )}

        {/* Tab 2: My Reservations */}
        {activeTab === 'my-reservations' && (
          <div className="tab-pane">
            <div className="section-header">
              <div>
                <h2>Moje rezerwacje</h2>
                <p>Lista Twoich aktywnych i zrealizowanych wypozyczen.</p>
              </div>
            </div>

            {loadingReservations ? (
              <div className="loader">Wczytywanie rezerwacji...</div>
            ) : myReservations.length === 0 ? (
              <div className="empty-state">
                Nie masz jeszcze zadnych rezerwacji. Przejdz do katalogu, aby dokonac rezerwacji.
              </div>
            ) : (
              <div className="table-responsive">
                <table className="data-table">
                  <thead>
                    <tr>
                      <th>ID</th>
                      <th>Sprzet</th>
                      <th>Nr inwentarzowy</th>
                      <th>Okres od</th>
                      <th>Okres do</th>
                      <th>Status</th>
                      <th>Akcje</th>
                    </tr>
                  </thead>
                  <tbody>
                    {myReservations.map((res) => (
                      <tr key={res.id}>
                        <td>#{res.id}</td>
                        <td><strong>{res.modelName || 'Sprzet #' + res.itemId}</strong></td>
                        <td><code>{res.inventoryNumber || '-'}</code></td>
                        <td>{res.startDate}</td>
                        <td>{res.endDate}</td>
                        <td>
                          <span className={`badge status-badge status-${res.status.toLowerCase()}`}>
                            {res.status}
                          </span>
                        </td>
                        <td>
                          {(res.status === 'PENDING' || res.status === 'APPROVED') && (
                            <button
                              className="btn btn-danger btn-sm"
                              onClick={() => handleCancelReservation(res.id)}
                            >
                              Anuluj
                            </button>
                          )}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        )}

        {/* Tab 3: Admin All Reservations */}
        {activeTab === 'all-reservations' && currentUser.role === 'ADMIN' && (
          <div className="tab-pane">
            <div className="section-header">
              <div>
                <h2>Panel Zarzadzania Rezerwacjami</h2>
                <p>Zatwierdzanie, odrzucanie, wydawanie oraz przyjmowanie zwrotow sprzetu.</p>
              </div>
            </div>

            {loadingReservations ? (
              <div className="loader">Wczytywanie rezerwacji...</div>
            ) : allReservations.length === 0 ? (
              <div className="empty-state">Brak zarejestrowanych rezerwacji w systemie.</div>
            ) : (
              <div className="table-responsive">
                <table className="data-table">
                  <thead>
                    <tr>
                      <th>ID</th>
                      <th>Uzytkownik</th>
                      <th>Sprzet</th>
                      <th>Od</th>
                      <th>Do</th>
                      <th>Status</th>
                      <th>Akcje Administratora</th>
                    </tr>
                  </thead>
                  <tbody>
                    {allReservations.map((res) => (
                      <tr key={res.id}>
                        <td>#{res.id}</td>
                        <td>{res.userName || 'Uzytkownik #' + res.userId}</td>
                        <td>
                          <strong>{res.modelName || 'Sprzet #' + res.itemId}</strong>
                          <br />
                          <small className="text-muted"><code>{res.inventoryNumber}</code></small>
                        </td>
                        <td>{res.startDate}</td>
                        <td>{res.endDate}</td>
                        <td>
                          <span className={`badge status-badge status-${res.status.toLowerCase()}`}>
                            {res.status}
                          </span>
                        </td>
                        <td className="actions-cell">
                          {res.status === 'PENDING' && (
                            <>
                              <button
                                className="btn btn-success btn-sm"
                                onClick={() => handleApprove(res.id)}
                              >
                                Zatwierdz
                              </button>
                              <button
                                className="btn btn-danger btn-sm"
                                onClick={() => handleReject(res.id)}
                              >
                                Odrzuc
                              </button>
                            </>
                          )}
                          {res.status === 'APPROVED' && (
                            <button
                              className="btn btn-primary btn-sm"
                              onClick={() => handleIssue(res.id)}
                            >
                              Wydaj sprzet
                            </button>
                          )}
                          {res.status === 'ISSUED' && (
                            <button
                              className="btn btn-warning btn-sm"
                              onClick={() => handleReturn(res.id)}
                            >
                              Przyjmij zwrot
                            </button>
                          )}
                          {res.status === 'RETURNED' && (
                            <span className="text-muted">Zakonczono</span>
                          )}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        )}

        {/* Tab 4: Admin Users & Clearance Management */}
        {activeTab === 'users' && currentUser.role === 'ADMIN' && (
          <div className="tab-pane">
            <div className="section-header">
              <div>
                <h2>Uzytkownicy i Poziomy Uprawnien (Clearance)</h2>
                <p>Przydzielanie poziomow dostepu do aparatury specjalistycznej.</p>
              </div>
            </div>

            {loadingUsers ? (
              <div className="loader">Wczytywanie uzytkownikow...</div>
            ) : usersList.length === 0 ? (
              <div className="empty-state">Brak uzytkownikow w systemie.</div>
            ) : (
              <div className="table-responsive">
                <table className="data-table">
                  <thead>
                    <tr>
                      <th>ID</th>
                      <th>Imie i nazwisko</th>
                      <th>E-mail</th>
                      <th>Rola</th>
                      <th>Aktualny Clearance</th>
                      <th>Zmien Clearance</th>
                    </tr>
                  </thead>
                  <tbody>
                    {usersList.map((u) => (
                      <tr key={u.id}>
                        <td>#{u.id}</td>
                        <td><strong>{u.firstName} {u.lastName}</strong></td>
                        <td><code>{u.email}</code></td>
                        <td>
                          <span className="user-badge role-badge">{u.role}</span>
                        </td>
                        <td>
                          <span className="user-badge clearance-badge">{u.clearanceLevel}</span>
                        </td>
                        <td>
                          <select
                            value={u.clearanceLevel}
                            onChange={(e) => handleChangeClearance(u.id, e.target.value as ClearanceLevel)}
                            className="select-sm"
                          >
                            <option value="BASIC">BASIC</option>
                            <option value="ADVANCED">ADVANCED</option>
                            <option value="SPECIALIST">SPECIALIST</option>
                          </select>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        )}
      </main>

      {/* Booking Modal */}
      {selectedItem && (
        <div className="modal-backdrop" onClick={() => setSelectedItem(null)}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h3>Rezerwacja: {selectedItem.name}</h3>
              <button className="btn-close" onClick={() => setSelectedItem(null)}>Zamknij</button>
            </div>

            <form onSubmit={handleCreateReservation} className="booking-form">
              <p className="item-summary">
                Nr inwentarzowy: <code>{selectedItem.inventoryNumber}</code>
                <br />
                Wydzial: <strong>{selectedItem.departmentName}</strong>
              </p>

              {bookingError && <div className="alert alert-error">{bookingError}</div>}
              {availabilityMessage && <div className="alert alert-success">{availabilityMessage}</div>}
              {bookingSuccess && <div className="alert alert-success">{bookingSuccess}</div>}

              <div className="form-row">
                <div className="form-group">
                  <label>Data rozpoczecia</label>
                  <input
                    type="date"
                    required
                    value={startDate}
                    min={todayStr}
                    onChange={(e) => {
                      setStartDate(e.target.value);
                      setAvailabilityMessage(null);
                    }}
                  />
                </div>
                <div className="form-group">
                  <label>Data zakonczenia</label>
                  <input
                    type="date"
                    required
                    value={endDate}
                    min={startDate || todayStr}
                    onChange={(e) => {
                      setEndDate(e.target.value);
                      setAvailabilityMessage(null);
                    }}
                  />
                </div>
              </div>

              <div className="modal-actions">
                <button
                  type="button"
                  className="btn btn-outline"
                  onClick={handleCheckAvailability}
                  disabled={!startDate || !endDate}
                >
                  Sprawdz dostepnosc terminu
                </button>
                <button
                  type="submit"
                  className="btn btn-primary"
                  disabled={!startDate || !endDate}
                >
                  Potwierdz rezerwacje
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
