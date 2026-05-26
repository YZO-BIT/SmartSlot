import React, { useState, useEffect, useRef } from "react";
import { Link, NavLink, useNavigate } from "react-router-dom";
import QuickStats from "../components.jsx/QuickStats";
import TimetableGrid from "../components.jsx/TimetableGrid";
import RecentBookings from "../components.jsx/RecentBookings";
import ConflictAlerts from "../components.jsx/ConflictAlerts";
import Bookingmodal from "../components.jsx/Bookingmodal";
import TicketModal from "../components.jsx/TicketModal";
import {
  getAllTeachers, getAllRooms, getAllSections,
  getBookingsByFilter, createBooking, getTeacherTickets
} from "../api/api";
import "../Dashboard.css";

const Dashboard = () => {
  const [teachers, setTeachers] = useState([]);
  const [rooms, setRooms] = useState([]);
  const [sections, setSections] = useState([]);
  const [bookings, setBookings] = useState([]);
  const [isTicketModalOpen, setIsTicketModalOpen] = useState(false);
  const [isBookingOpen, setIsBookingOpen] = useState(false);
  const [quickSlot, setQuickSlot] = useState({ day: null, slotId: null });

  // Ticket states
  const [tickets, setTickets] = useState([]);
  const [notification, setNotification] = useState(null);
  const prevTicketsRef = useRef([]);

  const user = JSON.parse(localStorage.getItem('user') || '{}');
  const isTeacher = user.role?.toUpperCase() === 'TEACHER';

  const [selectedType, setSelectedType] = useState("teacher");
  const [selectedId, setSelectedId] = useState("");
  const [loading, setLoading] = useState(false);
  const [refreshKey, setRefreshKey] = useState(0);
  const navigate = useNavigate();
  const selectedEntityName = React.useMemo(() => {
    if (selectedType === "teacher") {
      const t = teachers.find(t => t.id === selectedId);
      return t ? t.name : "";
    } else if (selectedType === "section") {
      const s = sections.find(s => s.id === selectedId);
      return s ? s.name : "";
    } else if (selectedType === "room") {
      const r = rooms.find(r => r.id === selectedId);
      return r ? r.roomNumber : "";
    }
    return "";
  }, [selectedType, selectedId, teachers, sections, rooms]);

  const fetchMetaData = async () => {
    try {
      const storedUser = localStorage.getItem('user');
      if (!storedUser) {
        navigate('/login');
        return;
      }
      const [tRes, rRes, sRes] = await Promise.all([
        getAllTeachers(),
        getAllRooms(),
        getAllSections()
      ]);
      setTeachers(Array.isArray(tRes.data) ? tRes.data : []);
      setRooms(Array.isArray(rRes.data) ? rRes.data : []);
      setSections(Array.isArray(sRes.data) ? sRes.data : []);

      if (isTeacher) {
        setSelectedType("teacher");
        setSelectedId(user.id);
      } else if (!selectedId && Array.isArray(tRes.data) && tRes.data.length > 0) {
        setSelectedId(tRes.data[0].id);
      }
    } catch (err) {
      console.error("Error loading dashboard metadata:", err);
    }
  };

  useEffect(() => {
    fetchMetaData();
  }, [navigate, refreshKey]);

  const toLocalYYYYMMDD = (date) => {
    const y = date.getFullYear();
    const m = String(date.getMonth() + 1).padStart(2, '0');
    const d = String(date.getDate()).padStart(2, '0');
    return `${y}-${m}-${d}`;
  };

  const getWeekRange = () => {
    const now = new Date();
    const day = now.getDay();
    const diffToMon = now.getDate() - day + (day === 0 ? -6 : 1);
    const monday = new Date(now);
    monday.setDate(diffToMon);
    const saturday = new Date(monday);
    saturday.setDate(monday.getDate() + 5);

    return {
      start: toLocalYYYYMMDD(monday),
      end: toLocalYYYYMMDD(saturday)
    };
  };

  // Poll visual schedule bookings and teacher's override tickets in real time
  useEffect(() => {
    let isFirst = true;

    const pollData = async () => {
      if (isFirst) setLoading(true);
      try {
        if (selectedType && selectedId) {
          const { start, end } = getWeekRange();
          const res = await getBookingsByFilter(selectedType, selectedId, null, start, end);
          setBookings(res.data);
        }

        if (isTeacher && user.id) {
          const tRes = await getTeacherTickets(user.id);
          const newTickets = tRes.data || [];

          // Detect status changes for notifications
          if (prevTicketsRef.current.length > 0) {
            newTickets.forEach(newT => {
              const oldT = prevTicketsRef.current.find(t => t.id === newT.id);
              if (oldT && oldT.status !== newT.status) {
                setNotification({
                  message: `Your override request for ${newT.requestedSection?.name || 'Section'} on ${newT.requestedDate} (Slot ${newT.requestedSlotId}) has been ${newT.status}!`,
                  type: newT.status
                });
              }
            });
          }
          prevTicketsRef.current = newTickets;
          setTickets(newTickets);
        }
      } catch (err) {
        console.error("Error polling dashboard updates:", err);
      } finally {
        if (isFirst) {
          setLoading(false);
          isFirst = false;
        }
      }
    };

    pollData();
    const interval = setInterval(pollData, 5000);
    return () => clearInterval(interval);
  }, [selectedType, selectedId, refreshKey]);

  const handleCellClick = (day, slotId) => {
    if (!isTeacher) return;
    setQuickSlot({ day, slotId });
    setIsBookingOpen(true);
  };

  const handleQuickSave = async (data) => {
    try {
      if (data.sections && data.sections.length > 0) {
        await Promise.all(data.sections.map(sec => {
          const bookingData = {
            teacher: data.teacher,
            room: data.room,
            subject: data.subject,
            section: { id: sec.id },
            slotId: data.slotId,
            bookingDate: data.bookingDate
          };
          return createBooking(bookingData);
        }));
      } else {
        await createBooking(data);
      }
      setIsBookingOpen(false);
      setRefreshKey(prev => prev + 1);
      alert("Slot Booked Successfully!");
    } catch (err) {
      alert("Booking Error: " + (err.response?.data?.message || "Slot conflict"));
    }
  };

  const handleTicketSaved = () => {
    setIsTicketModalOpen(false);
    setRefreshKey(prev => prev + 1);
    alert("Oversight Ticket Submitted Successfully!");
  };

  return (
    <div className="dashboard-wrapper">
      <aside className="sidebar">
        <Link to="/dashboard" style={{ textDecoration: 'none' }}>
          <h2>SmartSlot</h2>
        </Link>
        <ul>
          {isTeacher ? (
            <>
              <li><NavLink to="/sections" className={({ isActive }) => isActive ? "active" : ""}>Sections</NavLink></li>
              <li><NavLink to="/subjects" className={({ isActive }) => isActive ? "active" : ""}>Subjects</NavLink></li>
            </>
          ) : (
            <>
              <li><NavLink to="/dashboard" className={({ isActive }) => isActive ? "active" : ""}>Dashboard</NavLink></li>
              <li><NavLink to="/teachers" className={({ isActive }) => isActive ? "active" : ""}>Staff</NavLink></li>
              <li><NavLink to="/rooms" className={({ isActive }) => isActive ? "active" : ""}>Rooms</NavLink></li>
              <li><NavLink to="/sections" className={({ isActive }) => isActive ? "active" : ""}>Sections</NavLink></li>
              <li><NavLink to="/subjects" className={({ isActive }) => isActive ? "active" : ""}>Subjects</NavLink></li>
              <li><NavLink to="/bookings" className={({ isActive }) => isActive ? "active" : ""}>Bookings</NavLink></li>
            </>
          )}
        </ul>
      </aside>
      <div className="dashboard-main">
        {/* Real-time Status Alert Banner */}
        {notification && (
          <div style={{
            background: notification.type === 'APPROVED' ? '#ecfdf5' : '#fef2f2',
            border: `1px solid ${notification.type === 'APPROVED' ? '#10b981' : '#ef4444'}`,
            color: notification.type === 'APPROVED' ? '#065f46' : '#991b1b',
            padding: '16px',
            borderRadius: '10px',
            marginBottom: '20px',
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'center',
            boxShadow: '0 4px 6px -1px rgba(0, 0, 0, 0.05), 0 2px 4px -1px rgba(0, 0, 0, 0.06)',
            animation: 'slideIn 0.3s ease-out',
            zIndex: 1000
          }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
              <span style={{ fontSize: '20px' }}>{notification.type === 'APPROVED' ? '🎉' : '❌'}</span>
              <div>
                <strong style={{ display: 'block', fontSize: '14px' }}>
                  Ticket Request {notification.type === 'APPROVED' ? 'Approved' : 'Rejected'}
                </strong>
                <span style={{ fontSize: '13px' }}>{notification.message}</span>
              </div>
            </div>
            <button
              onClick={() => setNotification(null)}
              style={{ background: 'transparent', border: 'none', color: 'inherit', cursor: 'pointer', fontSize: '16px', fontWeight: 'bold' }}
            >
              ✕
            </button>
          </div>
        )}

        <div className="dashboard-header" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
          <div>
            <h1 style={{ fontSize: '24px', fontWeight: '700', color: '#1e293b' }}>
              {isTeacher ? `Welcome Prof. ${user.name}` : "Admin Dashboard"}
            </h1>
            <p className="sub">{isTeacher ? "Manage your visual schedule and claim slots instantly" : "Manage and monitor institution timetable in real-time"}</p>
          </div>
          {isTeacher && (
            <div style={{ display: 'flex', gap: '10px' }}>
              <button
                className="add-btn"
                style={{ background: '#6366f1' }}
                onClick={() => {
                  const element = document.querySelector('.timetable-section');
                  if (element) element.scrollIntoView({ behavior: 'smooth' });
                }}
              >
                🖱️ Book from Grid
              </button>
              <button className="add-btn" style={{ background: '#f59e0b' }} onClick={() => setIsTicketModalOpen(true)}> Raise Ticket</button>
            </div>
          )}
        </div>

        <QuickStats userRole={user.role} userId={user.id} refreshKey={refreshKey} />

        {!isTeacher && (
          <div className="filter-row ss-card" style={{ marginBottom: '20px', padding: '15px' }}>
            <div style={{ display: 'flex', gap: '20px', alignItems: 'center' }}>
              <span style={{ fontSize: '14px', fontWeight: '600', color: '#64748b' }}>FILTER VIEW:</span>
              <select className="filter-select" value={selectedType} onChange={(e) => { setSelectedType(e.target.value); setSelectedId(""); }}>
                <option value="teacher">Teacher</option>
                <option value="section">Section</option>
                <option value="room">Room</option>
              </select>
              <select className="filter-select" value={selectedId} onChange={(e) => setSelectedId(e.target.value)} disabled={loading}>
                <option value="">Select entity...</option>
                {selectedType === "teacher" && teachers.map(t => <option key={t.id} value={t.id}>{t.name}</option>)}
                {selectedType === "room" && rooms.map(r => <option key={r.id} value={r.id}>{r.roomNumber}</option>)}
                {selectedType === "section" && sections.map(s => <option key={s.id} value={s.id}>{s.name}</option>)}
              </select>
            </div>
          </div>
        )}

        <div className="timetable-section ss-card">
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
            <div>
              <h2 style={{ fontSize: '20px', fontWeight: '700', color: '#0f172a' }}>
                {isTeacher ? "Weekly Visual Schedule" : `Monitoring: ${selectedType.toUpperCase()} - ${selectedEntityName}`}
              </h2>
              {isTeacher && <p style={{ fontSize: '13px', color: '#64748b' }}>Click any empty <span style={{ color: '#3b82f6', fontWeight: '600' }}>[+]</span> slot to claim it instantly.</p>}
            </div>
            <button
              onClick={() => setRefreshKey(prev => prev + 1)}
              style={{ background: '#f1f5f9', border: '1px solid #e2e8f0', color: '#475569', padding: '8px 16px', borderRadius: '6px', cursor: 'pointer', display: 'flex', alignItems: 'center', gap: '8px', fontWeight: '600' }}
            >
              <span>🔄</span> Sync Data
            </button>
          </div>

          <TimetableGrid
            bookings={bookings}
            onCellClick={handleCellClick}
          />
        </div>

        {/* Teacher Request Tickets History Section */}
        {isTeacher && (
          <div className="ss-card" style={{ marginTop: '20px', padding: '20px' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '15px' }}>
              <h2 style={{ fontSize: '20px', fontWeight: '700', color: '#0f172a' }}>🎫 My Request Tickets & Statuses</h2>
              <span style={{ fontSize: '12px', background: '#eff6ff', color: '#2563eb', padding: '4px 10px', borderRadius: '12px', fontWeight: '600' }}>Real-time Syncing</span>
            </div>
            {tickets.length === 0 ? (
              <p style={{ color: '#64748b', textAlign: 'center', padding: '20px' }}>No oversight tickets raised yet.</p>
            ) : (
              <div style={{ overflowX: 'auto' }}>
                <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left' }}>
                  <thead style={{ backgroundColor: '#f8fafc', borderBottom: '1px solid #e2e8f0' }}>
                    <tr>
                      <th style={{ padding: '12px', fontSize: '13px', color: '#475569' }}>Date</th>
                      <th style={{ padding: '12px', fontSize: '13px', color: '#475569' }}>Slot</th>
                      <th style={{ padding: '12px', fontSize: '13px', color: '#475569' }}>Section</th>
                      <th style={{ padding: '12px', fontSize: '13px', color: '#475569' }}>Reason</th>
                      <th style={{ padding: '12px', fontSize: '13px', color: '#475569' }}>Details</th>
                      <th style={{ padding: '12px', fontSize: '13px', color: '#475569' }}>Status</th>
                    </tr>
                  </thead>
                  <tbody>
                    {tickets.map(ticket => (
                      <tr key={ticket.id} style={{ borderBottom: '1px solid #f1f5f9' }}>
                        <td style={{ padding: '12px', fontSize: '13px', fontWeight: '600' }}>{ticket.requestedDate}</td>
                        <td style={{ padding: '12px', fontSize: '13px' }}>Slot {ticket.requestedSlotId}</td>
                        <td style={{ padding: '12px', fontSize: '13px' }}>{ticket.requestedSection?.name || 'N/A'}</td>
                        <td style={{ padding: '12px', fontSize: '13px', fontWeight: '500' }}>{ticket.reason}</td>
                        <td style={{ padding: '12px', fontSize: '12px', color: '#64748b' }}>{ticket.conflictDetails}</td>
                        <td style={{ padding: '12px' }}>
                          <span style={{
                            fontSize: '11px',
                            fontWeight: '600',
                            padding: '4px 8px',
                            borderRadius: '6px',
                            backgroundColor: ticket.status === 'APPROVED' ? '#dcfce7' : ticket.status === 'REJECTED' ? '#fee2e2' : '#fef3c7',
                            color: ticket.status === 'APPROVED' ? '#166534' : ticket.status === 'REJECTED' ? '#991b1b' : '#92400e'
                          }}>
                            {ticket.status}
                          </span>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        )}

        {isBookingOpen && (
          <Bookingmodal
            onClose={() => setIsBookingOpen(false)}
            onSave={handleQuickSave}
            prefilledDay={quickSlot.day}
            prefilledSlot={quickSlot.slotId}
          />
        )}

        {isTicketModalOpen && (
          <TicketModal
            onClose={() => setIsTicketModalOpen(false)}
            onSave={handleTicketSaved}
          />
        )}

        {!isTeacher && (
          <div className="dashboard-grid">
            <RecentBookings key={`rb-${refreshKey}`} />
            <ConflictAlerts key={`ca-${refreshKey}`} />
          </div>
        )}
      </div>
    </div>
  );
};

export default Dashboard;
